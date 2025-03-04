use crate::jwt::generate::claims_from_headers;
use crate::jwt::models::Claims;
use crate::{
    db::Db, errors::ProdError, forms::places::building::CreateBuildingForm, models::BuildingModel,
    AppState,
};
use axum::extract::Path;
use axum::{
    extract::State,
    http::{HeaderMap, StatusCode},
    Json,
};
use uuid::Uuid;

/// Create building (place)
#[utoipa::path(
    post,
    tag = "Coworkings",
    path = "/backend_api/place/new",
    request_body = CreateBuildingForm,
    responses(
        (status = 201, body = BuildingModel, description = "Created building"),
        (status = 403, description = "No auth / not admin"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn create_building(
    headers: HeaderMap,
    State(state): State<AppState>,
    Json(form): Json<CreateBuildingForm>,
) -> Result<(StatusCode, Json<BuildingModel>), ProdError> {
    let mut conn = state.pool.conn().await?;
    let Claims { company_id, .. } = claims_from_headers(&headers)?;

    let building = sqlx::query_as!(
        BuildingModel,
        r#"INSERT INTO buildings (
            address,
            company_id
        ) VALUES (
            $1, $2
        ) RETURNING id, address, company_id"#,
        form.address,
        company_id
    )
    .fetch_one(conn.as_mut())
    .await?;

    Ok((StatusCode::CREATED, Json(building)))
}

/// List buildings (everybody can use)
#[utoipa::path(
    get,
    tag = "Coworkings",
    path = "/backend_api/place/list",
    responses(
        (status = 200, body = Vec<BuildingModel>, description = "List of buildings"),
        (status = 403, description = "No auth"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn list_buildings(
    headers: HeaderMap,
    State(state): State<AppState>,
) -> Result<Json<Vec<BuildingModel>>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let Claims { company_id, .. } = claims_from_headers(&headers)?;

    let buildings = sqlx::query_as!(
        BuildingModel,
        r#"SELECT id, address, company_id FROM buildings WHERE company_id = $1"#,
        company_id
    )
    .fetch_all(conn.as_mut())
    .await?;

    Ok(Json(buildings))
}

/// Get building by id (everybody can use)
#[utoipa::path(
    get,
    tag = "Coworkings",
    path = "/backend_api/place/{building_id}",
    params(
        ("building_id" = Uuid, Path)
    ),
    responses(
        (status = 200, body = BuildingModel, description = "Building"),
        (status = 403, description = "No auth"),
        (status = 404, description = "No such building"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn get_building(
    headers: HeaderMap,
    Path(building_id): Path<Uuid>,
    State(state): State<AppState>,
) -> Result<Json<BuildingModel>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let Claims { company_id, .. } = claims_from_headers(&headers)?;

    let building = sqlx::query_as!(
        BuildingModel,
        r#"SELECT id, address, company_id FROM buildings WHERE company_id = $1 AND id = $2"#,
        company_id,
        building_id
    )
    .fetch_one(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound("No such building".to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    Ok(Json(building))
}

/// Update building
#[utoipa::path(
    patch,
    tag = "Coworkings",
    path = "/backend_api/place/{building_id}",
    request_body = CreateBuildingForm,
    params(
        ("building_id" = Uuid, Path)
    ),
    responses(
        (status = 200, body = BuildingModel, description = "Building"),
        (status = 403, description = "No auth"),
        (status = 404, description = "No such building"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn patch_building(
    headers: HeaderMap,
    Path(building_id): Path<Uuid>,
    State(state): State<AppState>,
    Json(form): Json<CreateBuildingForm>,
) -> Result<Json<BuildingModel>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let Claims { company_id, .. } = claims_from_headers(&headers)?;

    let building = sqlx::query_as!(
        BuildingModel,
        r#"UPDATE buildings SET address = $1 WHERE company_id = $2 AND id = $3 RETURNING id, address, company_id"#,
        form.address,
        company_id,
        building_id
    )
    .fetch_one(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound("No such building".to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    Ok(Json(building))
}

/// Delete building
#[utoipa::path(
    delete,
    tag = "Coworkings",
    path = "/backend_api/place/{building_id}",
    params(
        ("building_id" = Uuid, Path)
    ),
    responses(
        (status = 204, description = "Coworking successfully deleted"),
        (status = 403, description = "Not admin"),
        (status = 404, description = "No such building"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn delete_building(
    headers: HeaderMap,
    Path(building_id): Path<Uuid>,
    State(state): State<AppState>,
) -> Result<StatusCode, ProdError> {
    let mut conn = state.pool.conn().await?;
    let Claims { company_id, .. } = claims_from_headers(&headers)?;

    let _ = sqlx::query_as!(
        BuildingModel,
        r#"
        DELETE FROM buildings
        WHERE company_id = $1 AND id = $2
        RETURNING id, address, company_id"#,
        company_id,
        building_id
    )
    .fetch_one(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound("No such building".to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    Ok(StatusCode::NO_CONTENT)
}
