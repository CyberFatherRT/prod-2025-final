use axum::{
    extract::{Path, State},
    http::{HeaderMap, StatusCode},
    Json,
};
use sqlx::Acquire;
use uuid::Uuid;

use crate::{
    db::Db,
    errors::ProdError,
    forms::bookings::{CreateBookingForm, PatchBookingForm},
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
    headers: HeaderMap,
    State(state): State<AppState>,
    ValidatedJson(form): ValidatedJson<CreateBookingForm>,
) -> Result<(StatusCode, Json<BookingModel>), ProdError> {
    let mut conn = state.pool.conn().await?;
    let Claims {
        user_id,
        company_id,
        role,
        ..
    } = claims_from_headers(&headers)?;

    if role == RoleModel::Guest {
        return Err(ProdError::Forbidden(
            "Unverified guest can not create booking".to_string(),
        ));
    }

    let mut tx = conn.begin().await?;

    let bookable = sqlx::query!(
        r#"
        SELECT i.bookable
        FROM coworking_items ci
        JOIN items i ON i.id = ci.item_id
        WHERE ci.id = $1 AND ci.coworking_id = $2
    "#,
        form.coworking_item_id,
        form.coworking_id
    )
    .fetch_one(tx.as_mut())
    .await
    .map(|record| record.bookable)
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound(format!(
            "No item was found with that uuid - {}",
            form.coworking_item_id
        )),
        sqlx::Error::Database(e) if e.is_check_violation() => {
            ProdError::ShitHappened(e.to_string())
        }
        _ => ProdError::DatabaseError(err),
    })?;

    if !bookable {
        return Err(ProdError::Forbidden(format!(
            "You can not book item with that id - {}",
            form.coworking_item_id,
        )));
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
    .fetch_one(tx.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::Database(e) if e.is_foreign_key_violation() => ProdError::ShitHappened("Coworking or coworking_item not found".to_string()),
        _ => ProdError::DatabaseError(err)
    })?;

    tx.commit().await?;

    Ok((StatusCode::CREATED, Json(booking)))
}

/// Delete booking
#[utoipa::path(
    delete,
    tag = "Bookings",
    path = "/booking/{booking_id}",
    params(
        ("booking_id" = Uuid, Path)
    ),
    responses(
        (status = 204, description = "Booking was successully deleted"),
        (status = 403, description = "User doesn't own that booking"),
        (status = 404, description = "No booking was found with booking_id"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn delete_booking(
    headers: HeaderMap,
    State(state): State<AppState>,
    Path(booking_id): Path<Uuid>,
) -> Result<StatusCode, ProdError> {
    let mut conn = state.pool.conn().await?;
    let claim = claims_from_headers(&headers)?;

    let mut tx = conn.begin().await?;

    let (user_id, company_id) = sqlx::query!(
        r#"
        DELETE FROM bookings
        WHERE id = $1
        RETURNING user_id, company_id
        "#,
        booking_id
    )
    .fetch_one(tx.as_mut())
    .await
    .map(|record| (record.user_id, record.company_id))
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound("No booking was found".to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    if claim.user_id != user_id || claim.company_id != company_id {
        return Err(ProdError::Forbidden(
            "You don't have permission to that booking".to_string(),
        ));
    }

    tx.commit().await?;

    Ok(StatusCode::NO_CONTENT)
}

/// Upadte booking place and time
#[utoipa::path(
    patch,
    tag = "Bookings",
    path = "/booking/{booking_id}",
    request_body = PatchBookingForm,
    params(
        ("booking_id" = Uuid, Path)
    ),
    responses(
        (status = 200, body = BookingModel, description = "Successully update booking"),
        (status = 400, description = "Wrong request"),
        (status = 403, description = "User doesn't own that booking"),
        (status = 404, description = "Coworking or coworking_item not found")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn patch_booking(
    headers: HeaderMap,
    State(state): State<AppState>,
    Path(booking_id): Path<Uuid>,
    ValidatedJson(form): ValidatedJson<PatchBookingForm>,
) -> Result<Json<BookingModel>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let claim = claims_from_headers(&headers)?;
    let mut tx = conn.begin().await?;

    let booking = sqlx::query_as!(
        BookingModel,
        r#"
        UPDATE bookings
        SET
            coworking_space_id = COALESCE($2, coworking_space_id),
            coworking_item_id = COALESCE($3, coworking_item_id),
            time_start = COALESCE($4, time_start),
            time_end = COALESCE($5, time_end)
        WHERE id = $1
        RETURNING id, user_id, coworking_space_id, coworking_item_id,
                  company_id, time_start, time_end
        "#,
        booking_id,
        form.coworking_id,
        form.coworking_item_id,
        form.time_start,
        form.time_end
    )
    .fetch_one(tx.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound("No booking was found".to_string()),
        sqlx::Error::Database(e) if e.is_check_violation() => {
            ProdError::ShitHappened(e.to_string())
        }
        _ => ProdError::DatabaseError(err),
    })?;

    if claim.user_id != booking.user_id || claim.company_id != booking.company_id {
        return Err(ProdError::Forbidden(
            "You don't have permission to that booking".to_string(),
        ));
    }

    tx.commit().await?;

    Ok(Json(booking))
}
