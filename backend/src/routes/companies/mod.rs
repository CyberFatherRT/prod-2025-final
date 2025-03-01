use crate::AppState;
use axum::routing::post;
use axum::Router;

pub mod routes;

pub fn get_routes(state: AppState) -> Router {
    Router::new()
        .route("/register", post(routes::company_register))
        .with_state(state)
}
