use crate::routes::users::routes::__path_login;
use utoipa::OpenApi;

#[derive(OpenApi)]
#[openapi(
    paths(login),
    tags(
        (name = "Users", description = "User managment: register, login, patch")
    )
)]
pub struct ApiDoc;
