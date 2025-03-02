use std::sync::LazyLock;

use regex::Regex;
use serde::{Deserialize, Serialize};
use utoipa::ToSchema;
use uuid::Uuid;
use validator::Validate;

use crate::models::RoleModel;

static PASSWORD_REGEX: LazyLock<Regex> = LazyLock::new(|| {
    Regex::new(r"[a-zA-Z0-9$&+,:;=?@#|'<>.^*()%!-]{8,}").expect("Invalid regex for password")
});

static DOMAIN_REGEX: LazyLock<Regex> =
    LazyLock::new(|| Regex::new(r"[a-zA-Z]{3,30}").expect("Invalid regex for domain"));

#[derive(Serialize, Deserialize, Validate, ToSchema)]
pub struct LoginForm {
    #[validate(
        email,
        length(
            min = 1,
            max = 120,
            message = "User name length must be between 1 and 120"
        )
    )]
    pub email: String,

    #[validate(regex(path = *PASSWORD_REGEX, message = "Invalid password"))]
    pub password: String,

    #[validate(regex(path = *DOMAIN_REGEX, message = "Invalid company domain"))]
    pub domain: String,
}

#[derive(Serialize, Deserialize, Validate)]
pub struct UserLoginData {
    pub id: Uuid,
    pub email: String,
    pub password: String,
    pub company_id: Uuid,
    pub role: RoleModel,
}

#[derive(Serialize, Deserialize, Validate, ToSchema)]
pub struct RegisterForm {
    #[validate(length(
        min = 1,
        max = 120,
        message = "User name length must be between 1 and 120"
    ))]
    pub name: String,

    #[validate(length(
        min = 1,
        max = 120,
        message = "User name length must be between 1 and 120"
    ))]
    pub surname: String,

    #[validate(email(message = "Email is invalid"))]
    pub email: String,

    #[validate(regex(path = *PASSWORD_REGEX, message = "Invalid password"))]
    pub password: String,
    #[validate(regex(path = *DOMAIN_REGEX, message = "Invalid company domain"))]
    pub company_domain: String,
}

#[derive(Serialize, Deserialize, Validate, ToSchema)]
pub struct ProfileResponseForm {
    pub name: String,
    pub surname: String,
    pub email: String,
    pub avatar: Option<String>,
    pub role: RoleModel,
    pub pending_verification: Option<bool>,
    pub company_id: Uuid,
}

#[derive(Serialize, Deserialize, Validate, ToSchema)]
pub struct PatchProfileForm {
    #[validate(length(
        min = 1,
        max = 120,
        message = "User name length must be between 1 and 120"
    ))]
    pub name: Option<String>,

    #[validate(length(
        min = 1,
        max = 120,
        message = "User name length must be between 1 and 120"
    ))]
    pub surname: Option<String>,

    #[validate(regex(path = *PASSWORD_REGEX, message = "Invalid password"))]
    pub password: Option<String>,
}

#[derive(ToSchema)]
pub struct PatchProfileFormData {
    #[schema(value_type = PatchProfileForm, required = false)]
    pub json: Option<String>,

    #[schema(value_type = String, format = "binary", required = false)]
    pub avatar: Option<Vec<u8>>,
}

#[derive(ToSchema)]
pub struct UploadedDocument {
    #[schema(value_type = String, format = "binary")]
    pub document: String,
}

#[derive(Serialize, Deserialize, Validate, ToSchema)]
pub struct Token {
    pub jwt: String,
}

#[derive(Serialize, Deserialize, Validate, ToSchema)]
pub struct PublicUserData {
    pub id: Uuid,
    pub name: String,
    pub surname: String,
    pub email: String,
    pub avatar: Option<String>,
    pub role: RoleModel,
}
