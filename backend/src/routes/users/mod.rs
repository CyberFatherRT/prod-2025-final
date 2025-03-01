use crate::AppState;
use axum::routing::post;
use axum::{
    routing::{get, patch},
    Router,
};

pub mod routes;

use crate::routes::users::routes::register;
use routes::{login, patch_profile, profile};

pub fn get_routes(state: AppState) -> Router {
    Router::new()
        .route("/login", post(login))
        .route("/register", post(register))
        .route("/profile", get(profile))
        .route("/profile", patch(patch_profile))
        .with_state(state)
}
