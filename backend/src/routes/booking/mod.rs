use crate::{middlewares, AppState};
use axum::routing::patch;
use axum::{
    routing::{delete, post},
    Router,
};

pub mod routes;
pub fn get_routes(state: AppState) -> Router {
    Router::new().with_state(state)
}
