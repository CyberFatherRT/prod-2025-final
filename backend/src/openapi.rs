use crate::routes::{admin::routes::*, users::routes::__path_login};
use utoipa::OpenApi;

#[derive(OpenApi)]
#[openapi(
    paths(login, verify_guest, delete_user),
    tags(
        (name = "Users", description = "User management: register, login, patch"),
        (name = "Admin", description = "Admin related functionality")
    )
)]
pub struct ApiDoc;
