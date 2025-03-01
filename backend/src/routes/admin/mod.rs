use axum::{
    routing::{delete, post},
    Router,
};

use crate::{middlewares::auth_admin, AppState};

pub mod routes;
pub fn get_routes(state: AppState) -> Router {
    Router::new()
        .route("/verify_guest/{user_id}", post(routes::verify_guest))
        .route("/user/{user_id}", delete(routes::delete_user))
        .route("/user/{user_id}", delete(routes::patch_user))
        .with_state(state)
        .layer(axum::middleware::from_fn(auth_admin))
}
