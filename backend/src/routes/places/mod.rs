use crate::routes::places::building::{
    delete_building, get_building, list_buildings, patch_building,
};
use crate::routes::places::coworking::{
    delete_coworking, get_coworking_bookings, get_coworking_by_id, list_coworkings, patch_coworking,
};
use crate::routes::places::items::{
    add_item_to_coworking, delete_item_from_coworking, get_items_by_coworking,
    put_items_in_coworking,
};
use crate::{middlewares::auth_admin, AppState};
use axum::routing::{delete, get, patch, post, put};
use axum::{middleware::from_fn, Router};
use building::create_building;
use coworking::{create_coworking, list_coworkings_by_building};

pub mod building;
pub mod coworking;
pub mod items;

pub fn get_routes(state: AppState) -> Router {
    let admin_routes = Router::new()
        .route("/new", post(create_building))
        .route("/{building_id}", patch(patch_building))
        .route("/{building_id}", delete(delete_building))
        .route("/{building_id}/coworking/new", post(create_coworking))
        .route(
            "/{building_id}/coworking/{coworking_id}",
            patch(patch_coworking),
        )
        .route(
            "/{building_id}/coworking/{coworking_id}",
            delete(delete_coworking),
        )
        .route(
            "/{building_id}/coworking/{coworking_id}/items/new",
            post(add_item_to_coworking),
        )
        .route(
            "/{building_id}/coworking/{coworking_id}/items/put",
            put(put_items_in_coworking),
        )
        .route(
            "/{building_id}/coworking/{coworking_id}/items/{item_id}",
            delete(delete_item_from_coworking),
        )
        .layer(from_fn(auth_admin))
        .with_state(state.clone());

    Router::new()
        .route("/list", get(list_buildings))
        .route("/coworking/list", get(list_coworkings))
        .route("/{building_id}", get(get_building))
        .route(
            "/{building_id}/coworking/list",
            get(list_coworkings_by_building),
        )
        .route(
            "/{building_id}/coworking/{coworking_id}",
            get(get_coworking_by_id),
        )
        .route(
            "/{building_id}/coworking/{coworking_id}/items",
            get(get_items_by_coworking),
        )
        .route(
            "/{building_id}/coworking/{coworking_id}/bookings",
            get(get_coworking_bookings),
        )
        .merge(admin_routes)
        .with_state(state)
}
