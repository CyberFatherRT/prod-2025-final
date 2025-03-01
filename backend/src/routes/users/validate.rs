use axum::{
    extract::{Multipart, State},
    http::HeaderMap,
};

use crate::{
    db::Db,
    errors::ProdError,
    jwt::{generate::claims_from_headers, models::Claims},
    models::RoleModel,
    s3::utils::upload_file,
    AppState,
};

#[utoipa::path(post, tag = "User", path = "/user/upload_document")]
pub async fn upload_document(
    headers: HeaderMap,
    State(state): State<AppState>,
    mut multipart: Multipart,
) -> Result<(), ProdError> {
    let mut conn = state.pool.conn().await?;
    let Claims {
        role,
        user_id,
        company_id,
        ..
    } = claims_from_headers(&headers)?;

    if role != RoleModel::Guest {
        return Err(ProdError::Forbidden(
            "You are already verified user".to_string(),
        ));
    }

    while let Some(field) = multipart.next_field().await.unwrap_or(None) {
        let Some(content_type) = field.content_type().map(ToString::to_string) else {
            return Err(ProdError::ShitHappened(
                "Can not find content type".to_string(),
            ));
        };

        if content_type != "application/pdf" {
            return Err(ProdError::ShitHappened(
                "Document must be a PDF file".to_string(),
            ));
        }

        let content = field
            .bytes()
            .await
            .map_err(|err| ProdError::ShitHappened(err.to_string()))?;

        let name = format!("users/{user_id}/document.pdf");
        upload_file(&state, &name, content_type, content).await?;
    }

    let _ = sqlx::query!(
        r#"
        INSERT INTO pending_verifications (user_id, company_id)
        VALUES ($1, $2)
        "#,
        user_id,
        company_id,
    )
    .execute(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::Database(e) if e.is_unique_violation() => ProdError::VerificationError,
        _ => ProdError::DatabaseError(err),
    })?;

    Ok(())
}
