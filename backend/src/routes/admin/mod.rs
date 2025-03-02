use crate::{middlewares::auth_admin, AppState};
use axum::routing::{get, patch};
use axum::{
    routing::{delete, post},
    Router,
};

pub mod routes;
pub fn get_routes(state: AppState) -> Router {
    Router::new()
        .route("/documents/{user_id}", get(routes::get_user_document))
        .route("/list_requests", get(routes::get_verify_requests))
        .route("/user/{user_id}/verify", post(routes::verify_guest))
        .route("/user/{user_id}", get(routes::get_user))
        .route("/user/{user_id}", delete(routes::admin_delete_user))
        .route("/user/{user_id}", patch(routes::patch_user))
        .route("/user/list", get(routes::list_users))
        .with_state(state)
        .layer(axum::middleware::from_fn(auth_admin))
}
