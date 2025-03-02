use axum::{
    extract::{Path, State},
    http::{HeaderMap, StatusCode},
    Json,
};
use uuid::Uuid;

use crate::forms::places::coworking::UpdateCoworkingForm;
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
    path = "/place/{building_id}/coworking/new",
    params(
        ("building_id" = Uuid, Path)
    ),
    request_body = CreateCoworkingForm,
    responses(
        (status = 201, body = CoworkingSpacesModel, description = "Successully create coworking"),
        (status = 400, description = "Wrong request"),
        (status = 403, description = "You are not an admin"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn create_coworking(
    headers: HeaderMap,
    Path(building_id): Path<Uuid>,
    State(state): State<AppState>,
    Json(form): Json<CreateCoworkingForm>,
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
    .await?;

    Ok((StatusCode::CREATED, Json(coworking)))
}

/// List coworkings (everybody can use)
#[utoipa::path(
    post,
    tag = "Coworkings",
    path = "/place/{building_id}/coworking/list",
    params(
        ("building_id" = Uuid, Path)
    ),
    responses(
        (status = 200, body = Vec<CoworkingSpacesModel>, description = "List of coworkings"),
        (status = 403, description = "You are not an admin"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn list_coworkings(
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
    .await?;

    Ok(Json(coworkings))
}

/// Get coworking by id (everybody can use)
#[utoipa::path(
    get,
    tag = "Coworkings",
    path = "/place/{building_id}/coworking/{coworking_id}",
    params(
        ("building_id" = Uuid, Path),
        ("coworking_id" = Uuid, Path)
    ),
    responses(
        (status = 200, body = CoworkingSpacesModel, description = "Coworking model"),
        (status = 403, description = "You are not an admin"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn get_coworking_by_id(
    headers: HeaderMap,
    Path(building_id): Path<Uuid>,
    Path(coworking_id): Path<Uuid>,
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
    path = "/place/{building_id}/coworking/{coworking_id}",
    params(
        ("building_id" = Uuid, Path),
        ("coworking_id" = Uuid, Path)
    ),
    request_body=UpdateCoworkingForm,
    responses(
        (status = 200, body = CoworkingSpacesModel, description = "Updated coworking model"),
        (status = 403, description = "You are not an admin"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn patch_coworking(
    headers: HeaderMap,
    Path(building_id): Path<Uuid>,
    Path(coworking_id): Path<Uuid>,
    State(state): State<AppState>,
    Json(form): Json<UpdateCoworkingForm>,
) -> Result<Json<CoworkingSpacesModel>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let Claims { company_id, .. } = claims_from_headers(&headers)?;

    let building = sqlx::query_as!(
        CoworkingSpacesModel,
        r#"UPDATE coworking_spaces SET address = $1
        WHERE
        company_id = $2 AND building_id = $3 AND id = $4
        RETURNING id, address, company_id, building_id, height, width"#,
        form.address,
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

    Ok(Json(building))
}
