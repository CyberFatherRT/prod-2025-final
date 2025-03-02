use crate::AppState;
use axum::{
    routing::{delete, post},
    Router,
};

pub mod routes;

pub fn get_routes(state: AppState) -> Router {
    Router::new()
        .route("/create", post(routes::create_booking))
        .route("/{booking_id}", delete(routes::delete_booking))
        .with_state(state)
}
