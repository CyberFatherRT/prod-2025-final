use crate::forms::users::PublicUserData;
use serde::{Deserialize, Serialize};
use utoipa::ToSchema;
use validator::Validate;

#[derive(Serialize, Deserialize, Validate, ToSchema)]
pub struct VerificationRequest {
    pub user: PublicUserData,
    pub document: String,
}
