#![deny(clippy::unwrap_used)]
#![warn(clippy::all, clippy::pedantic, clippy::nursery)]
#![allow(async_fn_in_trait)]
#![allow(clippy::missing_errors_doc)]
#![allow(clippy::missing_panics_doc)]
#![allow(clippy::must_use_candidate)]

pub mod db;
pub mod errors;
pub mod forms;
pub mod jwt;
pub mod middlewares;
pub mod models;
mod openapi;
pub mod routes;
pub mod s3;
pub mod util;

use axum::{http::StatusCode, middleware::from_fn, routing::get, Router};
use middlewares::log_request;
use openapi::ApiDoc;
use routes::{admin, users};
use s3::setup_s3;
use sqlx::PgPool;
use tokio::net::TcpListener;
use tracing::Level;
use util::env;
use utoipa::OpenApi;
use utoipa_swagger_ui::SwaggerUi;

#[derive(Clone)]
pub struct AppState {
    pub pool: PgPool,
    pub s3: minio::s3::Client,
    pub bucket_name: String,
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    dotenvy::dotenv().ok();

    tracing_subscriber::fmt()
        .compact()
        .with_target(true)
        .with_max_level(Level::DEBUG)
        .init();

    let port = env("PORT");
    let db_url = env("DATABASE_URL");

    let pool = PgPool::connect(&db_url).await?;
    sqlx::migrate!("./migrations").run(&pool).await?;

    let (s3, bucket_name) = setup_s3().await?;

    let app_state = AppState {
        pool,
        s3,
        bucket_name,
    };

    let router = Router::new()
        .route("/healthz", get(StatusCode::OK))
        .nest("/user", users::get_routes(app_state.clone()))
        .nest("/admin", admin::get_routes(app_state.clone()))
        .layer(from_fn(log_request))
        .merge(SwaggerUi::new("/api/swagger-ui").url("/api-doc/openapi.json", ApiDoc::openapi()));

    let listener = TcpListener::bind(&format!("0.0.0.0:{port}")).await?;
    axum::serve(listener, router).await?;

    Ok(())
}
