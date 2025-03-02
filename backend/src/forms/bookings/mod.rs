use serde::{Deserialize, Serialize};
use uuid::Uuid;

#[derive(Serialize, Deserialize)]
pub struct CreateBookingForm {
    pub coworking_id: Uuid,
    pub coworking_item_id: Uuid,
    pub time_start: chrono::DateTime<chrono::Utc>,
    pub time_end: chrono::DateTime<chrono::Utc>,
}
