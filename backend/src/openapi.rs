use crate::routes::{
    admin::routes::{
        __path_admin_delete_user, __path_get_user, __path_get_user_document,
        __path_get_verify_requests, __path_list_users, __path_patch_user, __path_verify_guest,
    },
    booking::routes::{
        __path_create_booking, __path_delete_booking, __path_get_booking_qr, __path_list_bookings,
        __path_patch_booking, __path_verify_booking_qr,
    },
    companies::routes::__path_company_register,
    items::{
        routes::{__path_create_items_type, __path_delete_item_type},
        selecting::__path_list_items_by_company,
    },
    places::{
        building::{
            __path_create_building, __path_delete_building, __path_get_building,
            __path_list_buildings, __path_patch_building,
        },
        coworking::{
            __path_create_coworking, __path_delete_coworking, __path_get_coworking_bookings,
            __path_get_coworking_by_id, __path_list_coworkings, __path_list_coworkings_by_building,
            __path_patch_coworking,
        },
        items::{
            __path_add_item_to_coworking, __path_delete_item_from_coworking,
            __path_get_items_by_coworking, __path_put_items_in_coworking,
        },
    },
    users::{
        routes::{
            __path_delete_user, __path_get_avatar, __path_login, __path_patch_profile,
            __path_profile, __path_register,
        },
        validate::__path_upload_document,
    },
};
use utoipa::openapi::security::{Http, HttpAuthScheme, SecurityScheme};
use utoipa::{Modify, OpenApi};

struct SecurityAddon;
impl Modify for SecurityAddon {
    fn modify(&self, openapi: &mut utoipa::openapi::OpenApi) {
        let components: &mut utoipa::openapi::Components = openapi
            .components
            .as_mut()
            .expect("shit happened at SecurityAddon"); // we can unwrap safely since there already is components registered.
        components.add_security_scheme(
            "bearerAuth",
            SecurityScheme::Http(Http::new(HttpAuthScheme::Bearer)),
        );
    }
}

#[derive(OpenApi)]
#[openapi(
    paths(
        login, register, profile, patch_profile, upload_document, delete_user, get_avatar,
        verify_guest, admin_delete_user, patch_user, list_users, get_user, get_verify_requests, get_user_document,
        create_booking, delete_booking, patch_booking, list_bookings, get_booking_qr, verify_booking_qr,
        create_coworking, create_building, list_buildings, get_building, patch_building, patch_coworking, get_coworking_by_id, delete_building,
        delete_coworking, get_coworking_bookings, list_coworkings_by_building, list_coworkings,
        create_items_type, delete_item_type, get_items_by_coworking, list_items_by_company, add_item_to_coworking, delete_item_from_coworking, put_items_in_coworking,
        company_register,
    ),
    tags(
        (name = "Users", description = "User management"),
        (name = "Admin", description = "Admin user related functionality"),
        (name = "Coworkings", description = "Coworking related functionality"),
        (name = "Items", description = "Items related functionality"),
        (name = "Bookings", description = "Booking related functionality"),
        (name = "Companies", description = "Company related functionality"),
    ),
    info(
        title = "BooQ",
        license(
            name = "BookIT case solution",
            identifier = "GPL-3.0-or-later"
        )
    ),
    modifiers(&SecurityAddon)
)]
pub struct ApiDoc;
