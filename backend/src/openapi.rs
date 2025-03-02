use crate::routes::{
    admin::routes::{__path_delete_user, __path_patch_user, __path_verify_guest},
    companies::routes::__path_company_register,
    users::{
        routes::{__path_login, __path_patch_profile, __path_profile, __path_register},
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
        login, register, profile, patch_profile, upload_document,
        verify_guest, delete_user, patch_user,
        company_register
    ),
    tags(
        (name = "Users", description = "User management"),
        (name = "Admin", description = "Admin related functionality"),
        (name = "Companies", description = "Companies related functionality")
    ),
    modifiers(&SecurityAddon)
)]
pub struct ApiDoc;
