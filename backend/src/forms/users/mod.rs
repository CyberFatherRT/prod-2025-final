use std::sync::LazyLock;

use regex::Regex;
use serde::{Deserialize, Serialize};
use uuid::Uuid;
use validator::Validate;

use crate::models::RoleModel;

static PASSWORD_REGEX: LazyLock<Regex> = LazyLock::new(|| {
    Regex::new(r#"[a-zA-Z0-9$&+,:;=?@#|'<>.^*()%!-]{8,}"#).expect("Invalid regex for password")
});

#[derive(Serialize, Deserialize, Validate)]
pub struct LoginForm {
    pub email: String,
    pub password: String,
}

#[derive(Serialize, Deserialize, Validate)]
pub struct UserLoginData {
    pub id: Uuid,
    pub email: String,
    pub password: String,
    pub role: RoleModel,
}

#[derive(Serialize, Deserialize, Validate)]
pub struct RegisterForm {
    pub username: String,
    pub email: String,
    pub password: String,
    pub company_id: Uuid,
}

#[derive(Serialize, Deserialize, Validate)]
pub struct ProfileResponseForm {
    pub name: String,
    pub surname: String,
    pub email: String,
    pub avatar: Option<String>,
    pub role: RoleModel,
    pub pending_verification: Option<bool>,
    pub company_id: Uuid,
}

#[derive(Serialize, Deserialize, Validate)]
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
    pub avatar: Option<String>,
}

#[derive(Serialize, Deserialize, Validate)]
pub struct Token {
    pub jwt: String,
}