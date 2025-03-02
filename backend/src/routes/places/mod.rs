use crate::routes::places::building::{get_building, list_buildings, patch_building};
use crate::routes::places::coworking::{get_coworking_by_id, list_coworkings, patch_coworking};
use crate::{middlewares::auth_admin, AppState};
use axum::routing::{get, patch};
use axum::{middleware::from_fn, routing::post, Router};
use building::create_building;
use coworking::create_coworking;

use super::items::selecting::get_items_by_coworking;

pub mod building;
pub mod coworking;

pub fn get_routes(state: AppState) -> Router {
    let admin_routes = Router::new()
        .route("/new", post(create_building))
        .route("/{building_id}", patch(patch_building))
        .route("/{building_id}/coworking/new", post(create_coworking))
        .route(
            "/{building_id}/coworking/{coworking_id}",
            patch(patch_coworking),
        )
        .route(
            "/{building_id}/coworking/{coworking_id}/items",
            get(get_items_by_coworking),
        )
        .layer(from_fn(auth_admin))
        .with_state(state.clone());

    Router::new()
        .route("/list", get(list_buildings))
        .route("/{building_id}", get(get_building))
        .route("/{building_id}/coworking/list", get(list_coworkings))
        .route(
            "/{building_id}/coworking/{coworking_id}",
            get(get_coworking_by_id),
        )
        .merge(admin_routes)
        .with_state(state)
}
