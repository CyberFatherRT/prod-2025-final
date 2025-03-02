use axum::{
    extract::{Path, State},
    http::{HeaderMap, StatusCode},
    Json,
};
use uuid::Uuid;

use crate::{
    db::Db,
    errors::ProdError,
    forms::coworkings::CreateCoworkingForm,
    jwt::{generate::claims_from_headers, models::Claims},
    models::CoworkingSpacesModel,
    AppState,
};

/// Create coworking
#[utoipa::path(
    post,
    tag = "Coworkings",
    path = "/place/{building_id}/coworking",
    params(
        ("building_id" = Uuid, Path)
    ),
    responses(
        (status = 201, body = CoworkingSpacesModel, description = "Successully create coworking"),
        (status = 400, description = "Wrong request"),
        (status = 403, description = "You are not an admin"),
    ),
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
