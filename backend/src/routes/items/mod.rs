use axum::{
    middleware::from_fn,
    routing::{delete, post},
    Router,
};
use routes::{create_items_type, delete_item_type};

use crate::{middlewares::auth_admin, AppState};

pub mod routes;
pub mod selecting;

pub fn get_routes(state: AppState) -> Router {
    Router::new()
        .route("/new", post(create_items_type))
        .route("/{item_id}", delete(delete_item_type))
        .layer(from_fn(auth_admin))
        .with_state(state)
}
