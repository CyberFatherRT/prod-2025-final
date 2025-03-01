use crate::routes::{
    admin::routes::{__path_delete_user, __path_verify_guest},
    users::routes::{__path_login, __path_patch_profile, __path_profile, __path_register},
};
use utoipa::OpenApi;

#[derive(OpenApi)]
#[openapi(
    paths(
        login, register, profile, patch_profile,
        verify_guest, delete_user
    ),
    tags(
        (name = "Users", description = "User management: register, login, patch"),
        (name = "Admin", description = "Admin related functionality")
    )
)]
pub struct ApiDoc;
