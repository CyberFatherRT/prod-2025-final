use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use utoipa::ToSchema;
use uuid::Uuid;
use validator::{Validate, ValidationError};

#[derive(Serialize, Deserialize, Validate, ToSchema)]
#[validate(schema(
    function = "validate_create_booking_struct",
    skip_on_field_errors = false
))]
pub struct CreateBookingForm {
    pub coworking_id: Uuid,
    pub coworking_item_id: Uuid,
    pub time_start: NaiveDateTime,
    pub time_end: NaiveDateTime,
}

fn validate_create_booking_struct(form: &CreateBookingForm) -> Result<(), ValidationError> {
    if form.time_start > form.time_end {
        return Err(ValidationError::new("time_start must be before time_end"));
    }

    if form
        .time_end
        .signed_duration_since(form.time_start)
        .num_minutes()
        % 15
        != 0
    {
        return Err(ValidationError::new(
            "You can book items with duration divided by 15 minutes",
        ));
    }

    Ok(())
}
