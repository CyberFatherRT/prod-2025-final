use serde::{Deserialize, Serialize};
use uuid::Uuid;
use validator::Validate;

use crate::models::RoleModel;

#[derive(Serialize, Deserialize, Validate)]
pub struct LoginForm {
    pub email: String,
    pub password: String,
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
