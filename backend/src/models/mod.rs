use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::error::BoxDynError;
use sqlx::postgres::{PgArgumentBuffer, PgHasArrayType, PgTypeInfo, PgValueRef};
use sqlx::{prelude::FromRow, Decode, Encode, Postgres, Type};
use std::ops::Add;
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
    pub height: i64,
    pub width: i64,
    pub building_id: Uuid,
    pub company_id: Uuid,
}

#[derive(Serialize, Deserialize, ToSchema, FromRow, Debug, Clone, PartialEq, Eq, Hash)]
#[sqlx(type_name = "POINT")]
pub struct Point {
    pub x: i64,
    pub y: i64,
}

impl Add for Point {
    type Output = Self;

    fn add(self, rhs: Self) -> Self::Output {
        Point {
            x: self.x + rhs.x,
            y: self.y + rhs.y,
        }
    }
}

impl Type<Postgres> for Point {
    fn type_info() -> PgTypeInfo {
        PgTypeInfo::with_name("point")
    }
}

impl Encode<'_, Postgres> for Point {
    fn encode_by_ref(
        &self,
        buf: &mut PgArgumentBuffer,
    ) -> Result<sqlx::encode::IsNull, Box<(dyn std::error::Error + Send + Sync + 'static)>> {
        let point_str = format!("({}, {})", self.x, self.y);
        buf.extend_from_slice(point_str.as_bytes());
        Ok(sqlx::encode::IsNull::No)
    }
}

impl<'r> Decode<'r, Postgres> for Point {
    fn decode(value: PgValueRef<'r>) -> Result<Self, BoxDynError> {
        let (part1, part2) = value.as_bytes()?.split_at(8);
        let x = decode_f64_from_bytes(part1);
        let y = decode_f64_from_bytes(part2);
        Ok(Self { x, y })
    }
}

fn decode_f64_from_bytes(bytes: &[u8]) -> i64 {
    let bits = u64::from_be_bytes(bytes.try_into().expect("Byte slice has wrong length"));
    f64::from_bits(bits).round() as i64
}

impl PgHasArrayType for Point {
    fn array_type_info() -> PgTypeInfo {
        PgTypeInfo::with_name("_point")
    }
}

#[derive(Serialize, Deserialize, FromRow, Validate, ToSchema)]
pub struct ItemsModel {
    pub id: Uuid,
    pub name: String,
    pub description: Option<String>,
    pub color: String,
    pub icon: Option<String>,
    pub offsets: Vec<Point>,
    pub bookable: bool,
    pub company_id: Uuid,
}

#[derive(Serialize, Deserialize, FromRow, Validate, ToSchema)]
pub struct CoworkingItemsModel {
    pub id: Uuid,
    pub item_id: Uuid,
    pub name: String,
    pub description: Option<String>,
    pub base_point: Point,
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

    pub time_start: NaiveDateTime,
    pub time_end: NaiveDateTime,

    pub building_address: String,

    pub coworking_item_name: String,
    pub coworking_item_description: Option<String>,
    pub coworking_space_name: String,
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

#[derive(Serialize, Deserialize, ToSchema)]
pub struct BuildingModel {
    pub id: Uuid,
    pub address: String,
    pub company_id: Uuid,
}

#[derive(Serialize, Deserialize, FromRow, Validate, ToSchema, Clone)]
pub struct Coordinates {
    pub base_point: Point,
    pub offsets: Vec<Point>,
}

#[derive(Serialize, Deserialize, FromRow, Validate, ToSchema, Clone)]
pub struct Offsets {
    pub offsets: Vec<Point>,
}
