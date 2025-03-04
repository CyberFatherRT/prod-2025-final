use crate::models::Point;
use axum::{
    extract::{Path, State},
    http::{HeaderMap, StatusCode},
    Json,
};
use sqlx::Acquire;
use uuid::Uuid;

use crate::forms::places::coworking::UpdateCoworkingForm;
use crate::models::{BookingModel, Coordinates};
use crate::util::ValidatedJson;
use crate::{
    db::Db,
    errors::ProdError,
    forms::places::coworking::CreateCoworkingForm,
    jwt::{generate::claims_from_headers, models::Claims},
    models::CoworkingSpacesModel,
    AppState,
};

/// Create coworking
#[utoipa::path(
    post,
    tag = "Coworkings",
    path = "/backend_api/place/{building_id}/coworking/new",
    params(
        ("building_id" = Uuid, Path)
    ),
    request_body = CreateCoworkingForm,
    responses(
        (status = 201, body = CoworkingSpacesModel, description = "Successully create coworking"),
        (status = 400, description = "Wrong request"),
        (status = 403, description = "You are not an admin"),
        (status = 404, description = "No such building")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn create_coworking(
    headers: HeaderMap,
    Path(building_id): Path<Uuid>,
    State(state): State<AppState>,
    ValidatedJson(form): ValidatedJson<CreateCoworkingForm>,
) -> Result<(StatusCode, Json<CoworkingSpacesModel>), ProdError> {
    let mut conn = state.pool.conn().await?;
    let Claims { company_id, .. } = claims_from_headers(&headers)?;

    let coworking = sqlx::query_as!(
        CoworkingSpacesModel,
        r#"
        INSERT INTO coworking_spaces (address,height, width, building_id, company_id)
        VALUES ($1, $2, $3, $4, $5)
        RETURNING id, address, height, width, building_id, company_id
        "#,
        form.address,
        form.height,
        form.width,
        building_id,
        company_id
    )
    .fetch_one(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::Database(e) if e.is_foreign_key_violation() => {
            ProdError::NotFound("No such building".to_string())
        }
        _ => ProdError::DatabaseError(err),
    })?;

    Ok((StatusCode::CREATED, Json(coworking)))
}

/// List coworkings by building (everybody can use)
#[utoipa::path(
    get,
    tag = "Coworkings",
    path = "/backend_api/place/{building_id}/coworking/list",
    params(
        ("building_id" = Uuid, Path)
    ),
    responses(
        (status = 200, body = Vec<CoworkingSpacesModel>, description = "List of coworkings"),
        (status = 403, description = "No auth"),
        (status = 404, description = "No such buliding found")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn list_coworkings_by_building(
    headers: HeaderMap,
    Path(building_id): Path<Uuid>,
    State(state): State<AppState>,
) -> Result<Json<Vec<CoworkingSpacesModel>>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let Claims { company_id, .. } = claims_from_headers(&headers)?;

    let coworkings = sqlx::query_as!(
        CoworkingSpacesModel,
        r#"
        SELECT id, address, height, width, building_id, company_id
        FROM coworking_spaces
        WHERE building_id = $1
        AND company_id = $2
        "#,
        building_id,
        company_id
    )
    .fetch_all(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound("No such buliding found".to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    Ok(Json(coworkings))
}

/// List coworkings (everybody can use)
#[utoipa::path(
    get,
    tag = "Coworkings",
    path = "/backend_api/place/coworking/list",
    responses(
        (status = 200, body = Vec<CoworkingSpacesModel>, description = "List of coworkings"),
        (status = 403, description = "No auth"),
        (status = 404, description = "No such buliding found")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn list_coworkings(
    headers: HeaderMap,
    State(state): State<AppState>,
) -> Result<Json<Vec<CoworkingSpacesModel>>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let Claims { company_id, .. } = claims_from_headers(&headers)?;

    let coworkings = sqlx::query_as!(
        CoworkingSpacesModel,
        r#"
        SELECT id, address, height, width, building_id, company_id
        FROM coworking_spaces
        WHERE company_id = $1
        "#,
        company_id,
    )
    .fetch_all(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound("No such coworking".to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    Ok(Json(coworkings))
}

/// Get coworking by id (everybody can use)
#[utoipa::path(
    get,
    tag = "Coworkings",
    path = "/backend_api/place/{building_id}/coworking/{coworking_id}",
    params(
        ("building_id" = Uuid, Path),
        ("coworking_id" = Uuid, Path)
    ),
    responses(
        (status = 200, body = CoworkingSpacesModel, description = "Coworking model"),
        (status = 403, description = "No auth"),
        (status = 404, description = "No such coworking or building found")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn get_coworking_by_id(
    headers: HeaderMap,
    Path((building_id, coworking_id)): Path<(Uuid, Uuid)>,
    State(state): State<AppState>,
) -> Result<Json<CoworkingSpacesModel>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let Claims { company_id, .. } = claims_from_headers(&headers)?;

    let coworkings = sqlx::query_as!(
        CoworkingSpacesModel,
        r#"
        SELECT id, address, height, width, building_id, company_id
        FROM coworking_spaces
        WHERE building_id = $1
        AND company_id = $2
        AND id = $3
        "#,
        building_id,
        company_id,
        coworking_id
    )
    .fetch_one(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound("No such coworking".to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    Ok(Json(coworkings))
}

/// Update coworking
#[utoipa::path(
    patch,
    tag = "Coworkings",
    path = "/backend_api/place/{building_id}/coworking/{coworking_id}",
    params(
        ("building_id" = Uuid, Path),
        ("coworking_id" = Uuid, Path)
    ),
    request_body=UpdateCoworkingForm,
    responses(
        (status = 200, body = CoworkingSpacesModel, description = "Updated coworking model"),
        (status = 403, description = "You are not an admin"),
        (status = 404, description = "No such coworking or building found"),
        (status = 409, description = "New height or width makes some objects inaccessible")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn patch_coworking(
    headers: HeaderMap,
    Path((building_id, coworking_id)): Path<(Uuid, Uuid)>,
    State(state): State<AppState>,
    ValidatedJson(form): ValidatedJson<UpdateCoworkingForm>,
) -> Result<Json<CoworkingSpacesModel>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let Claims { company_id, .. } = claims_from_headers(&headers)?;
    let mut tx = conn.begin().await?;

    let _ = sqlx::query_as!(
        CoworkingSpacesModel,
        r#"
        SELECT id, address, height, width, building_id, company_id
        FROM coworking_spaces
        WHERE building_id = $1 AND id = $2 AND company_id = $3
        "#,
        building_id,
        coworking_id,
        company_id
    )
    .fetch_one(tx.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => {
            ProdError::NotFound("No such coworking or building exists".to_string())
        }
        _ => ProdError::DatabaseError(err),
    })?;

    let coordinates = sqlx::query_as!(
        Coordinates,
        r#"
        SELECT c.base_point as "base_point: Point", i.offsets as "offsets: Vec<Point>"
        FROM coworking_items c
        JOIN item_types i ON i.id = c.item_id
        WHERE c.coworking_id = $1
        "#,
        coworking_id
    )
    .fetch_all(tx.as_mut())
    .await?;

    let mut abs_coords: Vec<Point> = Vec::new();
    for coord in coordinates {
        for offset in coord.offsets {
            abs_coords.push(coord.base_point.clone() + offset);
        }
    }
    if let Some(height) = form.height {
        if abs_coords.iter().any(|p| p.y >= height as i64) {
            return Err(ProdError::Conflict(
                "New height makes some items inaccessible. Delete those objects first.".to_string(),
            ));
        }
    }
    if let Some(width) = form.width {
        if abs_coords.iter().any(|p| p.x >= width as i64) {
            return Err(ProdError::Conflict(
                "New width makes some items inaccessible. Delete those objects first.".to_string(),
            ));
        }
    }

    let coworking = sqlx::query_as!(
        CoworkingSpacesModel,
        r#"UPDATE coworking_spaces SET
        address = COALESCE($1, address),
        height = COALESCE($2, height),
        width = COALESCE($3, width)
        WHERE
        company_id = $4 AND building_id = $5 AND id = $6
        RETURNING id, address, company_id, building_id, height, width"#,
        form.address,
        form.height,
        form.width,
        company_id,
        building_id,
        coworking_id
    )
    .fetch_one(tx.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound("No such coworking".to_string()),
        _ => ProdError::DatabaseError(err),
    })?;
    tx.commit().await?;

    Ok(Json(coworking))
}

/// Delete coworking
#[utoipa::path(
    delete,
    tag = "Coworkings",
    path = "/backend_api/place/{building_id}/coworking/{coworking_id}",
    params(
        ("building_id" = Uuid, Path),
        ("coworking_id" = Uuid, Path)
    ),
    responses(
        (status = 204, description = "Coworking successfully deleted"),
        (status = 403, description = "You are not an admin"),
        (status = 404, description = "No such coworking or building found")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn delete_coworking(
    headers: HeaderMap,
    Path((building_id, coworking_id)): Path<(Uuid, Uuid)>,
    State(state): State<AppState>,
) -> Result<StatusCode, ProdError> {
    let mut conn = state.pool.conn().await?;
    let Claims { company_id, .. } = claims_from_headers(&headers)?;

    let _ = sqlx::query_as!(
        CoworkingSpacesModel,
        r#"DELETE FROM coworking_spaces
        WHERE company_id = $1 AND building_id = $2 AND id = $3
        RETURNING id, address, company_id, building_id, height, width"#,
        company_id,
        building_id,
        coworking_id
    )
    .fetch_one(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound("No such coworking".to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    Ok(StatusCode::NO_CONTENT)
}

/// Get coworking bookings by id (everybody can use)
#[utoipa::path(
    get,
    tag = "Coworkings",
    path = "/backend_api/place/{building_id}/coworking/{coworking_id}/bookings",
    params(
        ("building_id" = Uuid, Path),
        ("coworking_id" = Uuid, Path)
    ),
    responses(
        (status = 200, body = Vec<BookingModel>, description = "List of bookings"),
        (status = 403, description = "no auth"),
        (status = 404, description = "No such building or coworking found")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn get_coworking_bookings(
    headers: HeaderMap,
    Path((building_id, coworking_id)): Path<(Uuid, Uuid)>,
    State(state): State<AppState>,
) -> Result<Json<Vec<BookingModel>>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let Claims { company_id, .. } = claims_from_headers(&headers)?;

    let _ = sqlx::query!(
        r#"SELECT id FROM coworking_spaces WHERE building_id = $1 AND id = $2 AND company_id = $3"#,
        building_id,
        coworking_id,
        company_id,
    )
    .fetch_one(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => {
            ProdError::NotFound("No such coworking or building".to_string())
        }
        _ => ProdError::DatabaseError(err),
    })?;

    let bookings = sqlx::query_as!(
        BookingModel,
        r#"
        SELECT id, user_id, coworking_space_id, coworking_item_id, company_id, time_start, time_end
        FROM bookings
        WHERE company_id = $1
        AND coworking_space_id = $2
        AND time_end > NOW()
        "#,
        company_id,
        coworking_id
    )
    .fetch_all(conn.as_mut())
    .await?;

    Ok(Json(bookings))
}
