use std::time::Instant;

use axum::{extract::Request, http::StatusCode, response::Response};
use tracing::info;

use crate::{errors::ProdError, jwt::generate::claims_from_headers, models::RoleModel};

pub async fn log_request(
    req: Request,
    next: axum::middleware::Next,
) -> Result<Response, StatusCode> {
    let start = Instant::now();
    let path = req.uri().path().to_string();
    let method = req.method().clone();

    let response = next.run(req).await;

    let status = response.status();
    let latency = start.elapsed();

    info!(
        target: "solution",
        method = %method,
        path = %path,
        status = status.as_u16(),
        latency = ?latency,
        "request"
    );

    Ok(response)
}

pub async fn auth_admin(req: Request, next: axum::middleware::Next) -> Result<Response, ProdError> {
    let headers = req.headers();
    let jwt = claims_from_headers(headers)?;

    if jwt.role != RoleModel::Admin {
        return Err(ProdError::Forbidden("You are not an admin".to_string()));
    }

    Ok(next.run(req).await)
}
