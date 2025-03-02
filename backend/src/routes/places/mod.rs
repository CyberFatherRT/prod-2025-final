use crate::routes::places::building::{get_building, list_buildings, patch_building};
use crate::routes::places::coworking::list_coworkings;
use crate::{middlewares::auth_admin, AppState};
use axum::routing::{get, patch};
use axum::{middleware::from_fn, routing::post, Router};
use building::create_building;
use coworking::create_coworking;

pub mod building;
pub mod coworking;

pub fn get_routes(state: AppState) -> Router {
    let admin_routes = Router::new()
        .route("/new", post(create_building))
        .route("/{building_id}/coworking/new", post(create_coworking))
        .route("/{building_id}", patch(patch_building))
        .layer(from_fn(auth_admin))
        .with_state(state.clone());

    Router::new()
        .route("/list", get(list_buildings))
        .route("/{building_id}", get(get_building))
        .route("/{building_id}/coworking/list", get(list_coworkings))
        .merge(admin_routes)
        .with_state(state)
}
