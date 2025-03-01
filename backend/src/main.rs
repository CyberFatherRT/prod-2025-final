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
pub mod routes;
pub mod s3;
pub mod util;

use axum::{http::StatusCode, middleware::from_fn, routing::get, Router};
use middlewares::log_request;
use routes::{admin, users};
use sqlx::PgPool;
use tokio::net::TcpListener;
use tracing::Level;
use util::env;

#[derive(Clone)]
pub struct AppState {
    pub pool: PgPool,
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

    let app_state = AppState { pool };

    let router = Router::new()
        .route("/healthz", get(StatusCode::OK))
        .nest("/users", users::get_routes(app_state.clone()))
        .nest("/admin", admin::get_routes(app_state.clone()))
        .layer(from_fn(log_request));

    let listener = TcpListener::bind(&format!("0.0.0.0:{port}")).await?;
    axum::serve(listener, router).await?;

    Ok(())
}
