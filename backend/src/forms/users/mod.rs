use serde::{Deserialize, Serialize};
use uuid::Uuid;
use validator::Validate;

#[derive(Serialize, Deserialize, Validate)]
pub struct LoginForm {
    email: String,
    password: String,
}

#[derive(Serialize, Deserialize, Validate)]
pub struct RegisterForm {
    username: String,
    email: String,
    password: String,
    company_id: Uuid,
}
