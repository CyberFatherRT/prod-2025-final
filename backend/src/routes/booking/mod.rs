use crate::AppState;
use axum::Router;

pub mod routes;
pub fn get_routes(state: AppState) -> Router {
    Router::new().with_state(state)
}
