use serde::{Deserialize, Serialize};
use utoipa::ToSchema;

#[derive(Serialize, Deserialize, ToSchema)]
pub struct CreateCoworkingForm {
    pub address: String,
    pub height: i32,
    pub width: i32,
}

#[derive(Serialize, Deserialize, ToSchema)]
pub struct UpdateCoworkingForm {
    pub address: String,
}
