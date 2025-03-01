use axum::Router;

use crate::AppState;

mod routes;
pub fn get_routes(state: AppState) -> Router {
    Router::new()
        .route("/verify-gueroutes::verify_guest)
        .with_state(state)
}
