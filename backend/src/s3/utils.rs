use axum::body::Bytes;
use minio::s3::types::S3Api;
use minio::s3::{args::PutObjectApiArgs, error, utils::Multimap};
use std::io::Error;
use std::pin::Pin;
use tonic::codegen::tokio_stream::Stream;
use tracing::info;

use crate::{errors::ProdError, AppState};

pub async fn upload_file(
    state: &AppState,
    name: &str,
    content_type: String,
    content: Bytes,
) -> Result<(), ProdError> {
    let client = state.s3.clone();
    let bucket_name = state.bucket_name.clone();

    let mut args = PutObjectApiArgs::new(&bucket_name, name, content.as_ref())
        .map_err(|err| ProdError::S3Error(format!("Failed to create PutObjectApiArgs - {err}")))?;

    let headers = Multimap::from_iter(vec![("Content-Type".to_string(), content_type.clone())]);
    args.headers = Some(&headers);

    let response = client
        .put_object_api(&args)
        .await
        .map_err(|err| ProdError::S3Error(err.to_string()))?;

    info!("Response: {:?}", response);

    Ok(())
}

pub async fn get_file(
    state: &AppState,
    name: &str,
) -> Result<
    (
        Pin<Box<dyn Stream<Item = Result<Bytes, Error>> + Send>>,
        String,
    ),
    ProdError,
> {
    let client = state.s3.clone();
    let bucket_name = state.bucket_name.clone();

    let response = client
        .get_object(&bucket_name, name)
        .send()
        .await
        .map_err(|err| match err {
            error::Error::S3Error(err) if err.code == "NoSuchKey" => {
                ProdError::NotFound(format!("File not found: {name}"))
            }
            _ => ProdError::Unknown(err.into()),
        })?;

    let headers = response.headers;
    let content_type = headers
        .get("Content-Type")
        .map_or("application/octet-stream", |v| {
            v.to_str().unwrap_or("application/octet-stream")
        });

    let (content, _) = response
        .content
        .to_stream()
        .await
        .map_err(|e| ProdError::Unknown(e.into()))?;
    Ok((content, content_type.to_string()))
}
