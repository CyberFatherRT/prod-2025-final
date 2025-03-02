use axum::{
    extract::State,
    http::{HeaderMap, StatusCode},
    Json,
};

use crate::{
    db::Db,
    errors::ProdError,
    forms::bookings::CreateBookingForm,
    jwt::{generate::claims_from_headers, models::Claims},
    models::{BookingModel, RoleModel},
    util::ValidatedJson,
    AppState,
};

/// Create booking (only for verified users)
#[utoipa::path(
    post,
    path = "/booking/create",
    tag = "Bookings",
    request_body = CreateBookingForm,
    responses(
        (status = 201, body = BookingModel, description = "Successully create booking"),
        (status = 400, description = "Wrong request"),
        (status = 403, description = "Unverified guest can not create booking"),
        (status = 404, description = "Coworking or coworking_item not found")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn create_booking(
    header: HeaderMap,
    State(state): State<AppState>,
    ValidatedJson(form): ValidatedJson<CreateBookingForm>,
) -> Result<(StatusCode, Json<BookingModel>), ProdError> {
    let mut conn = state.pool.conn().await?;
    let Claims {
        user_id,
        company_id,
        role,
        ..
    } = claims_from_headers(&header)?;

    if role == RoleModel::Guest {
        return Err(ProdError::Forbidden(
            "Unverified guest can not create booking".to_string(),
        ));
    }

    let booking = sqlx::query_as!(
        BookingModel,
        r#"
        INSERT INTO bookings(user_id, coworking_space_id, coworking_item_id, company_id, time_start, time_end)
        VALUES ($1, $2, $3, $4, $5, $6)
        RETURNING id, user_id, coworking_space_id, coworking_item_id, company_id, time_start, time_end
        "#,
        user_id,
        form.coworking_id,
        form.coworking_item_id,
        company_id,
        form.time_start,
        form.time_end
    )
    .fetch_one(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::Database(e) if e.is_foreign_key_violation() => ProdError::ShitHappened("Coworking or coworking_item not found".to_string()),
        _ => ProdError::DatabaseError(err)
    })?;

    Ok((StatusCode::CREATED, Json(booking)))
}
