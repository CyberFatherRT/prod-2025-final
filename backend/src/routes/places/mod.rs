use crate::{middlewares::auth_admin, AppState};
use axum::{middleware::from_fn, routing::post, Router};
use building::create_building;
use coworking::create_coworking;

pub mod building;
pub mod coworking;

pub fn get_routes(state: AppState) -> Router {
    Router::new()
        .route("/new", post(create_building))
        .route("/{building_id}/coworking", post(create_coworking))
        .layer(from_fn(auth_admin))
        .with_state(state)
}
