use std::time::Instant;

use axum::{
    extract::Request, http::StatusCode, middleware::from_fn, response::Response, routing::get,
    Router,
};
use tokio::net::TcpListener;
use tracing::{info, Level};

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

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    dotenvy::dotenv().ok();

    tracing_subscriber::fmt()
        .compact()
        .with_target(true)
        .with_max_level(Level::DEBUG)
        .init();

    let addr = "0.0.0.0:8000";

    let router = Router::new()
        .route("/healthz", get(async || StatusCode::OK))
        .layer(from_fn(log_request));

    let listener = TcpListener::bind(&addr).await?;
    axum::serve(listener, router).await?;

    Ok(())
}
