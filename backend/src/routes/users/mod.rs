use crate::AppState;
use axum::routing::{delete, post};
use axum::{
    routing::{get, patch},
    Router,
};
use validate::upload_document;

pub mod routes;
pub mod validate;

use crate::routes::users::routes::{get_avatar, register};
use routes::{delete_user, login, patch_profile, profile};

pub fn get_routes(state: AppState) -> Router {
    Router::new()
        .route("/{user_id}/avatar", get(get_avatar))
        .route("/login", post(login))
        .route("/register", post(register))
        .route("/profile", get(profile))
        .route("/profile", patch(patch_profile))
        .route("/upload_document", post(upload_document))
        .route("/", delete(delete_user))
        .with_state(state)
}
