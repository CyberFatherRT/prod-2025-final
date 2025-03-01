use serde::{Deserialize, Serialize};
use utoipa::ToSchema;
use validator::Validate;

#[derive(ToSchema, Validate, Serialize, Deserialize)]
pub struct CompanyRegisterData {
    #[validate(length(
        min = 1,
        max = 120,
        message = "Company name length must be between 1 and 120"
    ))]
    pub name: String,
    #[validate(length(min = 3, max = 30, message = "Domaing should be 1 to 30 chars long"))]
    pub domain: String,
}
