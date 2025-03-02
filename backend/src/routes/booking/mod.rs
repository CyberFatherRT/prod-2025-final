use crate::AppState;
use axum::{routing::post, Router};

pub mod routes;

pub fn get_routes(state: AppState) -> Router {
    Router::new()
        .route("/create", post(routes::create_booking))
        .with_state(state)
}
