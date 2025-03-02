use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use utoipa::ToSchema;
use uuid::Uuid;
use validator::Validate;

#[derive(Serialize, Deserialize, Validate, ToSchema)]
pub struct CreateBookingForm {
    pub coworking_id: Uuid,
    pub coworking_item_id: Uuid,
    pub time_start: NaiveDateTime,
    pub time_end: NaiveDateTime,
}

#[derive(Serialize, Deserialize, Validate, ToSchema)]
pub struct PatchBookingForm {
    pub coworking_id: Option<Uuid>,
    pub coworking_item_id: Option<Uuid>,
    pub time_start: Option<NaiveDateTime>,
    pub time_end: Option<NaiveDateTime>,
}

#[derive(Serialize, Deserialize, Validate, ToSchema)]
pub struct QrToken {
    pub token: String,
}

#[derive(Serialize, Deserialize, Validate, ToSchema)]
pub struct BookingDisplayData {
    pub user_email: String,
    pub building_name: String,
    pub space_name: String,
    pub item_name: String,
    pub time_start: NaiveDateTime,
    pub time_end: NaiveDateTime,
}

#[derive(Serialize, Deserialize, Validate, ToSchema)]
pub struct Verdict {
    pub valid: bool,
    pub booking_data: Option<BookingDisplayData>,
}
