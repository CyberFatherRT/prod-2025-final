use serde::{Deserialize, Serialize};
use utoipa::ToSchema;
use validator::Validate;

#[derive(Serialize, Deserialize, ToSchema, Validate)]
pub struct CreateCoworkingForm {
    pub address: String,
    #[validate(range(min = 1, max = 100))]
    pub height: i32,
    #[validate(range(min = 1, max = 100))]
    pub width: i32,
}

#[derive(Serialize, Deserialize, ToSchema, Validate)]
pub struct UpdateCoworkingForm {
    pub address: Option<String>,
    #[validate(range(min = 1, max = 100))]
    pub height: Option<i32>,
    #[validate(range(min = 1, max = 100))]
    pub width: Option<i32>,
}
