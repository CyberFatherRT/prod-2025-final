use axum::{
    routing::{get, patch},
    Router,
};

use crate::AppState;

mod routes;

use routes::{patch_profile, profile};

pub fn get_routes(state: AppState) -> Router {
    Router::new()
        .route("/profile", get(profile))
        .route("/profile", patch(patch_profile))
        .with_state(state)
}
