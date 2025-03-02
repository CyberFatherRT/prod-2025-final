use crate::AppState;
use axum::routing::get;
use axum::{
    routing::{delete, patch, post},
    Router,
};

pub mod routes;

pub fn get_routes(state: AppState) -> Router {
    Router::new()
        .route("/list", get(routes::list_bookings))
        .route("/create", post(routes::create_booking))
        .route("/{booking_id}", delete(routes::delete_booking))
        .route("/{booking_id}", patch(routes::patch_booking))
        .with_state(state)
}
