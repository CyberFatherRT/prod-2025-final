use serde::{Deserialize, Serialize};
use sqlx::{prelude::FromRow, Type};
use utoipa::ToSchema;
use uuid::Uuid;
use validator::Validate;

#[derive(Serialize, Deserialize, FromRow, Validate)]
pub struct CompaniesModel {
    pub id: Uuid,
    pub name: String,
    pub domain: String,
    pub avatar: Option<String>,
}

#[derive(Serialize, Deserialize, Type, Clone, PartialEq, Eq, ToSchema)]
#[sqlx(type_name = "ROLE", rename_all = "lowercase")]
#[serde(rename_all = "UPPERCASE")]
pub enum RoleModel {
    Admin,
    Student,
    Guest,

    #[sqlx(rename = "verified_guest")]
    VerifiedGuest,
}

#[derive(Serialize, Deserialize, FromRow, Validate, ToSchema)]
pub struct UserModel {
    #[serde(skip)]
    pub id: Uuid,

    pub name: String,
    pub surname: String,
    pub email: String,

    #[serde(skip)]
    pub password: String,
    pub avatar: Option<String>,

    #[serde(skip)]
    pub company_id: Uuid,
    pub company_domain: String,
    pub role: RoleModel,
}

#[derive(Serialize, Deserialize, FromRow)]
pub struct CoworkingSpacesModel {
    pub id: Uuid,
    pub company_id: Uuid,
    pub height: i64,
    pub width: i64,
}

#[derive(Serialize, Deserialize, ToSchema, Type)]
#[sqlx(type_name = "point")]
pub struct Point {
    x: i64,
    y: i64,
}

#[derive(Serialize, Deserialize, FromRow, Validate)]
pub struct ItemsModel {
    pub id: Uuid,
    pub name: Option<String>,
    pub description: Option<String>,
    pub icon: Option<String>,
    pub offsets: Vec<Point>,
    pub bookable: Option<bool>,
    pub company_id: Uuid,
}

#[derive(Serialize, Deserialize, FromRow, Validate)]
pub struct CoworkingItemsModel {
    pub id: Uuid,
    pub items_id: Uuid,
    pub base_point: Point,
    pub coworking_id: Uuid,
}

#[derive(Serialize, Deserialize, FromRow, Validate)]
pub struct BookingModel {
    pub id: Uuid,
    pub user_id: Uuid,
    pub coworking_space_id: Uuid,
    pub coworking_item_id: Uuid,
    pub company_id: Uuid,
    pub time_start: chrono::DateTime<chrono::Utc>,
    pub time_end: chrono::DateTime<chrono::Utc>,
}

#[derive(Serialize, Deserialize, FromRow, Validate)]
pub struct PendingVerificationsModel {
    pub user_id: Uuid,
    pub company_id: Uuid,
}

#[derive(Serialize, Deserialize, FromRow)]
pub struct TokenData {
    pub id: Uuid,
    pub company_id: Uuid,
    pub role: RoleModel,
}
