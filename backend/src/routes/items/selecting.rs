use axum::{extract::State, http::HeaderMap, Json};

use crate::{
    db::Db,
    errors::ProdError,
    jwt::generate::claims_from_headers,
    models::{ItemsModel, Point},
    AppState,
};

/// List all item types for company by id
#[utoipa::path(
    get,
    tag = "Items",
    path = "/backend_api/items",
    responses(
        (status = 200, body = Vec<ItemsModel>, description = "List of items in coworking"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn list_items_by_company(
    headers: HeaderMap,
    State(state): State<AppState>,
) -> Result<Json<Vec<ItemsModel>>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let company_id = claims_from_headers(&headers)?.company_id;

    let items = sqlx::query_as!(
        ItemsModel,
        r#"
        SELECT id, name, description, color, bookable, icon,
               offsets as "offsets: Vec<Point>", company_id
        FROM item_types
        WHERE company_id = $1
        "#,
        company_id
    )
    .fetch_all(conn.as_mut())
    .await?;

    Ok(Json(items))
}
