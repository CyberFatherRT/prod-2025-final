use axum::{
    extract::{Multipart, Path, State},
    http::{HeaderMap, StatusCode},
    Json,
};
use bytes::Bytes;
use sqlx::Acquire;
use tracing::warn;
use uuid::{NoContext, Timestamp, Uuid};

use crate::{
    db::Db,
    errors::ProdError,
    forms::items::{CreateItemTypeForm, CreateItemTypeFormData},
    jwt::generate::claims_from_headers,
    models::{ItemsModel, Point},
    s3::utils::upload_file,
    AppState,
};

/// Create new item type for company
#[utoipa::path(
    post,
    tag = "Items",
    path = "/items/new",
    request_body(content = CreateItemTypeFormData, content_type = "multipart/form-data"),
    responses(
        (status = 201, description = "Item type was successfully created", body = ItemsModel),
        (status = 403, description = "You are not an admin")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn create_items_type(
    headers: HeaderMap,
    State(state): State<AppState>,
    mut multipart: Multipart,
) -> Result<(StatusCode, Json<ItemsModel>), ProdError> {
    let mut conn = state.pool.conn().await?;
    let company_id = claims_from_headers(&headers)?.company_id;

    let mut form: Option<CreateItemTypeForm> = None;
    let mut image: Option<Bytes> = None;

    while let Ok(Some(field)) = multipart.next_field().await {
        if let Some(field_name) = field.name() {
            match field_name {
                "json" => {
                    let text = field
                        .text()
                        .await
                        .map_err(|_| ProdError::ShitHappened("Bad request".to_string()))?;

                    form = Some(serde_json::from_str(&text).map_err(|err| {
                        ProdError::ShitHappened(format!("Failed to parse json - {err}"))
                    })?);
                }
                "icon" => {
                    if field.content_type() != Some("image/svg") {
                        return Err(ProdError::ShitHappened(
                            "Icon must be of type image/svg".to_string(),
                        ));
                    }

                    image = Some(
                        field
                            .bytes()
                            .await
                            .map_err(|err| ProdError::ShitHappened(err.to_string()))?,
                    );
                }
                _ => warn!("Unknwon field: {}", field_name),
            }
        }
    }

    let Some(form) = form else {
        return Err(ProdError::ShitHappened(
            "Json body should be specified".to_string(),
        ));
    };

    let item_id = Uuid::new_v7(Timestamp::now(NoContext));
    let mut icon_name: Option<String> = None;

    if let Some(image) = image {
        let name = format!("items/{item_id}/icon.svg");
        upload_file(&state, &name, "image/svg".to_string(), image).await?;
        icon_name = Some(name);
    }

    let item = sqlx::query_as!(
        ItemsModel,
        r#"
        INSERT INTO item_types(name, description, icon, offsets, bookable, company_id)
        VALUES ($1, $2, $3, $4, $5, $6)
        RETURNING id, name, description, icon,
                  offsets as "offsets: _",
                  bookable, company_id
        "#,
        form.name,
        form.description,
        icon_name,
        form.offsets as Vec<Point>,
        form.bookable,
        company_id,
    )
    .fetch_one(conn.as_mut())
    .await?;

    Ok((StatusCode::CREATED, Json(item)))
}

/// Delete item type by id
#[utoipa::path(
    delete,
    tag = "Items",
    path = "/items/{item_id}",
    responses(
        (status = 204, description = "Item type was successfully deleted"),
        (status = 403, description = "Forbidden")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn delete_item_type(
    headers: HeaderMap,
    Path(item_id): Path<Uuid>,
    State(state): State<AppState>,
) -> Result<StatusCode, ProdError> {
    let mut conn = state.pool.conn().await?;
    let claim = claims_from_headers(&headers)?;

    let mut tx = conn.begin().await?;

    let company_id = sqlx::query!(
        r#"
        DELETE FROM item_types
        WHERE id = $1
        RETURNING company_id
        "#,
        item_id
    )
    .fetch_one(tx.as_mut())
    .await
    .map(|record| record.company_id)?;

    if claim.company_id != company_id {
        return Err(ProdError::Forbidden(
            "You don't have access to item from another company.".to_string(),
        ));
    }

    tx.commit().await?;

    Ok(StatusCode::NO_CONTENT)
}
