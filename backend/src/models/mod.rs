use chrono::NaiveDateTime;
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

#[derive(Serialize, Deserialize, FromRow, ToSchema)]
pub struct CoworkingSpacesModel {
    pub id: Uuid,
    pub address: String,
    pub height: i32,
    pub width: i32,
    pub building_id: Uuid,
    pub company_id: Uuid,
}

#[derive(Serialize, Deserialize, ToSchema, Type, FromRow)]
#[sqlx(type_name = "POINT")]
pub struct Point {
    x: i32,
    y: i32,
}

#[derive(Serialize, Deserialize, FromRow, Validate, ToSchema)]
pub struct ItemsModel {
    pub id: Uuid,
    pub name: String,
    pub description: Option<String>,
    pub icon: Option<String>,
    pub offsets: Vec<Point>,
    pub bookable: bool,
    pub company_id: Uuid,
}

#[derive(Serialize, Deserialize, FromRow, Validate)]
pub struct CoworkingItemsModel {
    pub id: Uuid,
    pub items_id: Uuid,
    pub base_point: Point,
    pub coworking_id: Uuid,
}

#[derive(Serialize, Deserialize, FromRow, Validate, ToSchema)]
pub struct BookingModel {
    pub id: Uuid,
    pub user_id: Uuid,
    pub coworking_space_id: Uuid,
    pub coworking_item_id: Uuid,
    pub company_id: Uuid,
    pub time_start: NaiveDateTime,
    pub time_end: NaiveDateTime,
}

#[derive(Serialize, Deserialize, FromRow, Validate, ToSchema)]
pub struct PublicBookingModel {
    pub id: Uuid,
    pub user_id: Uuid,

    pub coworking_space_id: Uuid,
    pub coworking_item_id: Uuid,
    pub company_id: Uuid,

    pub time_start: NaiveDateTime,
    pub time_end: NaiveDateTime,

    pub company_name: String,
    pub building_address: String,

    pub booking_item_name: String,
    pub booking_item_description: Option<String>,
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

#[derive(Serialize, Deserialize)]
pub struct BuildingModel {
    id: Uuid,
    address: String,
    company_id: Uuid,
}
