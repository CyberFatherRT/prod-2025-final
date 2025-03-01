use axum::Router;

use crate::AppState;

mod routes;

pub fn get_routes(state: AppState) -> Router {
    Router::new()
}
