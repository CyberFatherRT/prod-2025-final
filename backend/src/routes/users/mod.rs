use axum::{routing::get, Router};

use crate::AppState;

mod routes;

use routes::profile;

pub fn get_routes(state: AppState) -> Router {
    Router::new()
        .route("/profile", get(profile))
        .with_state(state)
}
