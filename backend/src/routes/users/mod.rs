use crate::AppState;
use axum::routing::post;
use axum::{
    routing::{get, patch},
    Router,
};
use validate::upload_document;

pub mod routes;
pub mod validate;

use crate::routes::users::routes::register;
use routes::{login, patch_profile, profile};

pub fn get_routes(state: AppState) -> Router {
    Router::new()
        .route("/login", post(login))
        .route("/register", post(register))
        .route("/profile", get(profile))
        .route("/profile", patch(patch_profile))
        .route("/upload_document", post(upload_document))
        .with_state(state)
}
