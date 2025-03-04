#![deny(clippy::unwrap_used)]
#![warn(clippy::all, clippy::pedantic, clippy::nursery)]
#![allow(async_fn_in_trait)]
#![allow(clippy::missing_errors_doc)]
#![allow(clippy::missing_panics_doc)]
#![allow(clippy::must_use_candidate)]

pub mod controllers;
pub mod db;
pub mod errors;
pub mod forms;
pub mod jwt;
pub mod middlewares;
pub mod models;
pub mod openapi;
pub mod routes;
pub mod s3;
pub mod util;

use crate::routes::{companies, items};
use axum::{
    http::{StatusCode, Uri},
    middleware::from_fn,
    response::IntoResponse,
    routing::get,
    Router,
};
use middlewares::log_request;
use openapi::ApiDoc;
use routes::{admin, booking, places, users};
use s3::setup_s3;
use sqlx::PgPool;
use tokio::net::TcpListener;
use tower_http::cors::CorsLayer;
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

pub const BASE_URL: &str = "https://https://prod-team-13-cltnksuj.final.prodcontest.ru/backend_api";

async fn not_found_handler(uri: Uri) -> impl IntoResponse {
    println!("404 - Requested path: {}", uri.path());
    (StatusCode::NOT_FOUND, format!("No route found for {}", uri))
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
        .route("/", get("<h1>Hello World</h1>"))
        .route("/healthz", get(StatusCode::OK))
        .nest("/user", users::get_routes(app_state.clone()))
        .nest("/admin", admin::get_routes(app_state.clone()))
        .nest("/booking", booking::get_routes(app_state.clone()))
        .nest("/company", companies::get_routes(app_state.clone()))
        .nest("/place", places::get_routes(app_state.clone()))
        .nest("/items", items::get_routes(app_state.clone()))
        .layer(from_fn(log_request))
        .layer(CorsLayer::permissive());

    let app = Router::new()
        .nest("/backend_api", router)
        .merge(
            SwaggerUi::new("/backend_api/api/swagger-ui")
                .url("/backend_api/api-doc/openapi.json", ApiDoc::openapi()),
        )
        .fallback(not_found_handler);

    let listener = TcpListener::bind(&format!("0.0.0.0:{port}")).await?;
    axum::serve(listener, app).await?;
    Ok(())
}
