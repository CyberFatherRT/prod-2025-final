use crate::routes::{
    admin::routes::{__path_delete_user, __path_patch_user, __path_verify_guest},
    users::{
        routes::{__path_login, __path_patch_profile, __path_profile, __path_register},
        validate::__path_upload_document,
    },
};
use utoipa::OpenApi;

#[derive(OpenApi)]
#[openapi(
    paths(
        login, register, profile, patch_profile, upload_document,
        verify_guest, delete_user, patch_user
    ),
    tags(
        (name = "Users", description = "User management"),
        (name = "Admin", description = "Admin related functionality")
    )
)]
pub struct ApiDoc;
