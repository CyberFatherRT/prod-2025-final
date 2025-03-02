use crate::routes::{
    admin::routes::{
        __path_admin_delete_user, __path_get_user, __path_get_user_document,
        __path_get_verify_requests, __path_list_users, __path_patch_user, __path_verify_guest,
    },
    booking::routes::{
        __path_create_booking, __path_delete_booking, __path_list_bookings, __path_patch_booking,
    },
    companies::routes::__path_company_register,
    coworkings::routes::__path_create_coworking,
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
        create_booking, delete_booking, patch_booking, list_bookings,
        company_register, create_coworking
    ),
    tags(
        (name = "Users", description = "User management"),
        (name = "Admin", description = "Admin user related functionality"),
        (name = "Bookings", description = "Booking related functionality"),
        (name = "Companies", description = "Company related functionality"),
        (name = "Coworkings", description = "Coworking related functionality")
    ),
    info(
        title = "BooQ",
        license(
            name = "BookIT solution",
        )
    ),
    modifiers(&SecurityAddon)
)]
pub struct ApiDoc;
