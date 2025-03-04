use crate::db::Db;
use crate::errors::ProdError;
use crate::forms::items::CreateItemForm;
use crate::jwt::generate::claims_from_headers;
use crate::models::{Coordinates, CoworkingItemsModel, CoworkingSpacesModel};
use crate::models::{Offsets, Point};
use crate::util::ValidatedJson;
use crate::AppState;
use axum::extract::{Path, State};
use axum::http::{HeaderMap, StatusCode};
use axum::Json;
use sqlx::Acquire;
use std::collections::HashSet;
use tracing::info;
use uuid::Uuid;

/// List all items from coworking by id (everybody can use)
#[utoipa::path(
    get,
    tag = "Coworkings",
    path = "/backend_api/place/{building_id}/coworking/{coworking_id}/items",
    params(
        ("building_id" = Uuid, Path),
        ("coworking_id" = Uuid, Path)
    ),
    responses(
        (status = 200, body = Vec<CoworkingItemsModel>, description = "List of items in coworking"),
        (status = 403, description = "no auth"),
        (status = 404, description = "No such coworking / building")
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
        WHERE c.company_id = $3 AND ci.coworking_id = $2
        "#,
        building_id,
        coworking_id,
        company_id
    )
    .fetch_all(conn.as_mut())
    .await?;

    Ok(Json(items))
}

/// Put new items' positions in coworking
#[utoipa::path(
    put,
    tag = "Coworkings",
    path = "/backend_api/place/{building_id}/coworking/{coworking_id}/items/put",
    request_body = Vec<CreateItemForm>,
    params(
        ("building_id" = Uuid, Path),
        ("coworking_id" = Uuid, Path)
    ),
    responses(
        (status = 201, body = Vec<CoworkingItemsModel>, description = "New items"),
        (status = 403, description = "no auth / not admin"),
        (status = 404, description = "No such coworking / building"),
        (status = 409, description = "Items overlaps with borders / other items")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn put_items_in_coworking(
    headers: HeaderMap,
    State(state): State<AppState>,
    Path((building_id, coworking_id)): Path<(Uuid, Uuid)>,
    ValidatedJson(form): ValidatedJson<Vec<CreateItemForm>>,
) -> Result<(StatusCode, Json<Vec<CoworkingItemsModel>>), ProdError> {
    let mut conn = state.pool.conn().await?;
    let company_id = claims_from_headers(&headers)?.company_id;

    let mut tx = conn.begin().await?;

    let space = sqlx::query_as!(
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

    let _ = sqlx::query!(
        r#"
        DELETE
        FROM coworking_items c
        WHERE c.coworking_id = $1
        "#,
        coworking_id
    )
    .execute(tx.as_mut())
    .await?;

    let mut created_items: Vec<CoworkingItemsModel> = Vec::new();
    let mut abs_coords: Vec<Point> = Vec::new();
    for item in form {
        let item_offsets = sqlx::query_as!(
            Offsets,
            r#"
        SELECT i.offsets as "offsets: Vec<Point>"
        FROM item_types i
        WHERE i.id = $1
        "#,
            item.item_id
        )
        .fetch_one(tx.as_mut())
        .await?;

        let mut item_coords: Vec<Point> = Vec::new();
        for offset in item_offsets.offsets {
            item_coords.push(item.base_point.clone() + offset);
        }
        abs_coords.extend(item_coords.clone());

        if item_coords
            .iter()
            .any(|p| p.x >= space.width || p.y >= space.height)
        {
            return Err(ProdError::Conflict(
                "Item overlaps with borders".to_string(),
            ));
        }

        let item = sqlx::query_as::<_, CoworkingItemsModel>(&format!(
            r"
        INSERT INTO coworking_items
        (name, description, item_id, base_point, coworking_id)
        VALUES  ($1, $2, $3, {}::point, $4)
        RETURNING id, item_id, name, description,
                  base_point
        ",
            format!("point({}, {})", item.base_point.x, item.base_point.y)
        ))
        .bind(item.name)
        .bind(item.description)
        .bind(item.item_id)
        .bind(coworking_id)
        .fetch_one(tx.as_mut())
        .await
        .map_err(|err| {
            info!("returned {:?}", err);
            ProdError::DatabaseError(err)
        })?;
        created_items.push(item);
    }

    let unique_coords: HashSet<Point> = abs_coords.clone().into_iter().collect();
    if abs_coords.len() != unique_coords.len() {
        return Err(ProdError::Conflict(
            "Item overlaps with other item".to_string(),
        ));
    }

    tx.commit().await?;

    Ok((StatusCode::CREATED, Json(created_items)))
}

/// Create new item in coworking
#[utoipa::path(
    post,
    tag = "Coworkings",
    path = "/backend_api/place/{building_id}/coworking/{coworking_id}/items/new",
    request_body = CreateItemForm,
    params(
        ("building_id" = Uuid, Path),
        ("coworking_id" = Uuid, Path)
    ),
    responses(
        (status = 201, body = CoworkingItemsModel, description = "Created item"),
        (status = 403, description = "no auth / not admin"),
        (status = 404, description = "No such coworking / building"),
        (status = 409, description = "Item overlaps with borders / other items")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn add_item_to_coworking(
    headers: HeaderMap,
    State(state): State<AppState>,
    Path((building_id, coworking_id)): Path<(Uuid, Uuid)>,
    ValidatedJson(form): ValidatedJson<CreateItemForm>,
) -> Result<(StatusCode, Json<CoworkingItemsModel>), ProdError> {
    let mut conn = state.pool.conn().await?;
    let company_id = claims_from_headers(&headers)?.company_id;

    let mut tx = conn.begin().await?;

    let space = sqlx::query_as!(
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

    let item_offsets = sqlx::query_as!(
        Offsets,
        r#"
        SELECT i.offsets as "offsets: Vec<Point>"
        FROM item_types i
        WHERE i.id = $1
        "#,
        form.item_id
    )
    .fetch_one(tx.as_mut())
    .await?;

    let mut item_coords: Vec<Point> = Vec::new();
    for offset in item_offsets.offsets {
        item_coords.push(form.base_point.clone() + offset);
    }

    if item_coords
        .iter()
        .any(|p| p.x >= space.width || p.y >= space.height)
    {
        return Err(ProdError::Conflict(
            "Item overlaps with borders".to_string(),
        ));
    }

    for coord in coordinates {
        for offset in coord.offsets {
            if item_coords.contains(&(coord.base_point.clone() + offset)) {
                return Err(ProdError::Conflict(
                    "Item overlaps with other item".to_string(),
                ));
            }
        }
    }

    let item = sqlx::query_as::<_, CoworkingItemsModel>(&format!(
        r"
        INSERT INTO coworking_items
        (name, description, item_id, base_point, coworking_id)
        VALUES  ($1, $2, $3, {}::point, $4)
        RETURNING id, item_id, name, description,
                  base_point
        ",
        format!("point({}, {})", form.base_point.x, form.base_point.y)
    ))
    .bind(form.name)
    .bind(form.description)
    .bind(form.item_id)
    .bind(coworking_id)
    .fetch_one(tx.as_mut())
    .await
    .map_err(|err| {
        info!("returned {:?}", err);
        ProdError::DatabaseError(err)
    })?;

    tx.commit().await?;

    Ok((StatusCode::CREATED, Json(item)))
}

/// Delete item from coworking
#[utoipa::path(
    delete,
    tag = "Coworkings",
    path = "/backend_api/place/{building_id}/coworking/{coworking_id}/items/{item_id}",
    params(
        ("building_id" = Uuid, Path),
        ("coworking_id" = Uuid, Path),
        ("item_id" = Uuid, Path)
    ),
    responses(
        (status = 204, description = "Item successfully deleted"),
        (status = 403, description = "no auth / not admin"),
        (status = 404, description = "No such coworking / building / item")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn delete_item_from_coworking(
    headers: HeaderMap,
    State(state): State<AppState>,
    Path((building_id, coworking_id, item_id)): Path<(Uuid, Uuid, Uuid)>,
) -> Result<StatusCode, ProdError> {
    let mut conn = state.pool.conn().await?;
    let company_id = claims_from_headers(&headers)?.company_id;

    let _ = sqlx::query!(
        r#"
        SELECT id FROM coworking_spaces
        WHERE company_id = $1 AND building_id = $2 AND id = $3
        "#,
        company_id,
        building_id,
        coworking_id
    )
    .fetch_one(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => {
            ProdError::NotFound("No such coworking or building exists".to_string())
        }
        _ => ProdError::DatabaseError(err),
    })?;

    let _ = sqlx::query!(
        r#"
        DELETE FROM coworking_items
        WHERE coworking_id = $1 and id = $2
        RETURNING id
        "#,
        coworking_id,
        item_id
    )
    .fetch_one(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound("No such item exists".to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    Ok(StatusCode::NO_CONTENT)
}
