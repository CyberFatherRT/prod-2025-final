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
        .route("/{booking_id}/qr", get(routes::get_booking_qr))
        .route("/verify", post(routes::verify_booking_qr))
        .with_state(state)
}
