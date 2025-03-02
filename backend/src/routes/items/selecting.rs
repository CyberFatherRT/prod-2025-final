use axum::{
    extract::{Path, State},
    http::HeaderMap,
    Json,
};
use uuid::Uuid;

use crate::{
    db::Db,
    errors::ProdError,
    jwt::generate::claims_from_headers,
    models::{CoworkingItemsModel, Point},
    AppState,
};

/// List all items from coworking by id
#[utoipa::path(
    get,
    tag = "Coworkings",
    path = "/place/{building_id}/coworking/{coworking_id}/items",
    params(
        ("building_id" = Uuid, Path),
        ("coworking_id" = Uuid, Path)
    ),
    responses(
        (status = 200, body = Vec<CoworkingItemsModel>, description = "List of items in coworking"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn get_items_by_coworking(
    headers: HeaderMap,
    State(state): State<AppState>,
    Path((building_id, coworking_id)): Path<(Uuid, Uuid)>,
) -> Result<Json<Vec<CoworkingItemsModel>>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let company_id = claims_from_headers(&headers)?.company_id;

    let items = sqlx::query_as!(
        CoworkingItemsModel,
        r#"
        SELECT ci.id, ci.item_id, ci.name, ci.description,
               ci.base_point as "base_point: Point"
        FROM coworking_items ci
        JOIN buildings b ON b.id = $1
        JOIN coworking_spaces c ON c.id = $2 AND c.building_id = b.id
        WHERE c.company_id = $3
        "#,
        building_id,
        coworking_id,
        company_id
    )
    .fetch_all(conn.as_mut())
    .await?;

    Ok(Json(items))
}
