use axum::{
    extract::{Path, State},
    http::{HeaderMap, StatusCode},
    Json,
};
use sqlx::Acquire;
use tracing::info;
use uuid::Uuid;

use crate::forms::bookings::{BookingDisplayData, QrToken, Verdict};
use crate::jwt::generate::{create_qr_token, validate_qr_token};
use crate::models::PublicBookingModel;
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
    path = "/backend_api/booking/create",
    tag = "Bookings",
    request_body = CreateBookingForm,
    responses(
        (status = 201, body = BookingModel, description = "Successully create booking"),
        (status = 400, description = "Wrong request"),
        (status = 403, description = "Unverified guest can not create booking"),
        (status = 404, description = "Coworking or coworking_item not found"),
        (status = 409, description = "Your booking conflicts with existing bookings")
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
        JOIN item_types i ON i.id = ci.item_id
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
        sqlx::Error::Database(ref e) => {
            if e.is_foreign_key_violation() {
                return ProdError::ShitHappened("Coworking or coworking_item not found".to_string())
            } else if e.is_check_violation() {
                return ProdError::ShitHappened("Booking time should be divided by 15 minutes. You can book only in future. Start time should be less than end time.".to_string())
            } else if e.constraint() == Some("bookings_coworking_item_id_tsrange_excl") {
                return ProdError::Conflict("Your booking conflicts with existing bookings".to_string())
            }
            ProdError::DatabaseError(err)
        },
        _ => ProdError::DatabaseError(err)
    })?;

    tx.commit().await?;

    Ok((StatusCode::CREATED, Json(booking)))
}

/// Delete booking
#[utoipa::path(
    delete,
    tag = "Bookings",
    path = "/backend_api/booking/{booking_id}",
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

/// Update booking place and time
#[utoipa::path(
    patch,
    tag = "Bookings",
    path = "/backend_api/booking/{booking_id}",
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

/// List active future bookings
#[utoipa::path(
    get,
    tag = "Bookings",
    path = "/backend_api/booking/list",
    responses(
        (status = 200, body = Vec<PublicBookingModel>, description = "List of user's bookings"),
        (status = 403, description = "No auth"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn list_bookings(
    headers: HeaderMap,
    State(state): State<AppState>,
) -> Result<Json<Vec<PublicBookingModel>>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let claim = claims_from_headers(&headers)?;

    let bookings = sqlx::query_as!(
        PublicBookingModel,
        r#"
        SELECT
            b.id,
            b.user_id,
            b.coworking_space_id,
            b.coworking_item_id,
            b.time_start,
            b.time_end,
            bu.address as building_address,
            i.name as coworking_item_name,
            i.description as coworking_item_description,
            s.address as coworking_space_name
        FROM bookings b
        JOIN companies c ON c.id = b.company_id
        JOIN coworking_spaces s ON s.id = b.coworking_space_id
        JOIN buildings bu ON bu.id = s.building_id
        JOIN coworking_items i ON i.id = b.coworking_item_id
        WHERE b.user_id = $1 AND b.time_end > NOW()
        "#,
        claim.user_id,
    )
    .fetch_all(conn.as_mut())
    .await?;

    Ok(Json(bookings))
}

/// Generate QR for booking
#[utoipa::path(
    get,
    tag = "Bookings",
    path = "/backend_api/booking/{booking_id}/qr",
    params(
        ("booking_id" = Uuid, Path)
    ),
    responses(
        (status = 200, body = QrToken, description = "Booking QR"),
        (status = 400, description = "Wrong request"),
        (status = 403, description = "No auth"),
        (status = 404, description = "Coworking or coworking_item not found")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn get_booking_qr(
    headers: HeaderMap,
    State(state): State<AppState>,
    Path(booking_id): Path<Uuid>,
) -> Result<Json<QrToken>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let claim = claims_from_headers(&headers)?;

    let booking = sqlx::query_as!(
        BookingModel,
        r#"
        SELECT
        id, user_id, coworking_space_id, coworking_item_id, company_id, time_start, time_end
        FROM bookings
        WHERE user_id = $1 AND id = $2
        "#,
        claim.user_id,
        booking_id,
    )
    .fetch_one(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound("No booking found".to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    let token = create_qr_token(&booking)?;
    Ok(Json(QrToken { token }))
}

/// Verify booking qr
#[utoipa::path(
    post,
    tag = "Bookings",
    path = "/backend_api/booking/verify",
    responses(
        (status = 200, body = Verdict, description = "Booking QR validation data"),
        (status = 400, description = "Wrong request"),
        (status = 403, description = "No auth"),
        (status = 404, description = "Coworking or coworking_item not found")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn verify_booking_qr(
    State(state): State<AppState>,
    Json(form): Json<QrToken>,
) -> Result<Json<Verdict>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let pre_claims = validate_qr_token(&form.token);
    info!("here {:?}", pre_claims);
    if pre_claims.is_err() {
        return Ok(Json(Verdict {
            valid: false,
            booking_data: None,
        }));
    }
    let claims = pre_claims?;

    let booking = sqlx::query_as!(
        BookingDisplayData,
        r#"
        SELECT
        u.email as "user_email",
        b.address as "building_name",
        s.address as "space_name",
        i.name as "item_name",
        bo.time_start,
        bo.time_end
        FROM bookings bo
        JOIN users u ON u.id = bo.user_id
        JOIN coworking_spaces s ON s.id = bo.coworking_space_id
        JOIN buildings b ON b.id = s.building_id
        JOIN coworking_items i ON i.id = bo.coworking_item_id
        WHERE bo.id = $1
        "#,
        claims.booking_id,
    )
    .fetch_one(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound("No booking found".to_string()),
        _ => ProdError::DatabaseError(err),
    });
    info!("booking is {}", booking.is_err());

    booking.map_or_else(
        |_| {
            Ok(Json(Verdict {
                valid: false,
                booking_data: None,
            }))
        },
        |booking| {
            Ok(Json(Verdict {
                valid: true,
                booking_data: Option::from(booking),
            }))
        },
    )
}
