use axum::{routing::post, Router};

use crate::AppState;

mod routes;
pub fn get_routes(state: AppState) -> Router {
    Router::new()
        .route("/verify_guest/{user_id}", post(routes::verify_guest))
        .with_state(state)
}
