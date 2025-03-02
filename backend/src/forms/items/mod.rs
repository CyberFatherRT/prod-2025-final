use serde::{Deserialize, Serialize};
use utoipa::ToSchema;
use validator::Validate;

use crate::models::Point;

#[derive(Serialize, Deserialize, ToSchema, Validate)]
pub struct CreateItemTypeForm {
    #[validate(length(min = 1, max = 10))]
    pub name: String,
    pub description: Option<String>,
    pub offsets: Vec<Point>,
    pub bookable: bool,
}

#[derive(Serialize, Deserialize, ToSchema, Validate)]
pub struct CreateItemTypeFormData {
    #[schema(value_type = CreateItemTypeForm, required = true)]
    pub json: CreateItemTypeForm,

    #[schema(value_type = String, format = "binary", required = false)]
    pub icon: Option<Vec<u8>>,
}
