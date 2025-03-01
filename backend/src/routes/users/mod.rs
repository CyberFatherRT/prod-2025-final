use crate::AppState;
use axum::routing::post;
use axum::{
    routing::{get, patch},
    Router,
};

mod routes;

use routes::{login, patch_profile, profile};

pub fn get_routes(state: AppState) -> Router {
    Router::new()
        .route("/profile", get(profile))
        .route("/profile", patch(patch_profile))
        .route("/login", post(login))
        .with_state(state)
}
