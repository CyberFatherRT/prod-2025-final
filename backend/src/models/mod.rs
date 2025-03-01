use serde::{Deserialize, Serialize};
use sqlx::{prelude::FromRow, Type};
use uuid::Uuid;
use validator::Validate;

#[derive(Serialize, Deserialize, FromRow, Validate)]
pub struct CompaniesModel {
    id: Uuid,

    #[validate(length(
        min = 1,
        max = 120,
        message = "Company name length must be between 1 and 120"
    ))]
    name: String,

    avatar: Option<String>,
}

#[derive(Serialize, Deserialize, Type)]
#[sqlx(type_name = "ROLE", rename_all = "UPPERCASE")]
#[serde(rename_all = "UPPERCASE")]
pub enum RoleModel {
    Admin,
    Student,
    Guest,
    VerifiedGuest,
}

#[derive(Serialize, Deserialize, FromRow, Validate)]
pub struct UsersModel {
    id: Uuid,

    #[validate(length(
        min = 1,
        max = 120,
        message = "Username length must be between 1 and 120"
    ))]
    username: String,

    #[validate(
        email,
        length(min = 1, max = 120, message = "Email length must be between 1 and 120")
    )]
    email: String,

    password: String,
    avatar: Option<String>,
    company_id: Uuid,
    role: RoleModel,
}

#[derive(Serialize, Deserialize, FromRow)]
pub struct CoworkingSpacesModel {
    id: Uuid,
    company_id: Uuid,
}

#[derive(Serialize, Deserialize, FromRow, Validate)]
pub struct ItemsModel {
    id: Uuid,
    name: Option<String>,
    description: Option<String>,
    icon: Option<String>,
    company_id: Uuid,
}

#[derive(Serialize, Deserialize, FromRow, Validate)]
pub struct CoworkingItemsModel {
    id: Uuid,
    items_id: Uuid,
    coworking_id: Uuid,
}

#[derive(Serialize, Deserialize, FromRow, Validate)]
pub struct BookingModel {
    id: Uuid,
    user_id: Uuid,
    coworking_space_id: Uuid,
    coworking_item_id: Uuid,
    time_start: chrono::DateTime<chrono::Utc>,
    time_end: chrono::DateTime<chrono::Utc>,
}

#[derive(Serialize, Deserialize, FromRow, Validate)]
pub struct PendingVerificationsModel {
    user_id: Uuid,
    company_id: Uuid,
    document_name: Option<String>,
}
