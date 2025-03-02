use crate::models::RoleModel;
use axum::extract::Multipart;
use axum::http::HeaderMap;
use axum::{extract::Path, extract::State, Json};
use uuid::Uuid;

use crate::controllers::users::update_user;
use crate::forms::users::{PatchProfileFormData, PublicUserData};
use crate::jwt::generate::claims_from_headers;
use crate::models::UserModel;
use crate::{db::Db, errors::ProdError, AppState};

/// Get user by id
#[utoipa::path(
    get,
    tag = "Admin",
    path = "/admin/user/{user_id}",
    responses(
        (status = 200, body = PublicUserData),
        (status = 403, description = "not admin / no auth"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn get_user(
    headers: HeaderMap,
    Path(user_id): Path<Uuid>,
    State(state): State<AppState>,
) -> Result<Json<PublicUserData>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let company_id = claims_from_headers(&headers)?.company_id;
    let user = sqlx::query_as!(
        PublicUserData,
        r#"
        SELECT 
        id, name, surname, email, avatar, role as "role: RoleModel"
        FROM users
        WHERE company_id = $1 AND id = $2
        "#,
        company_id,
        user_id
    )
    .fetch_one(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound("Such user does not exist".to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    Ok(Json(user))
}

/// Verify guest user
#[utoipa::path(
    post,
    tag = "Admin",
    path = "/admin/verify_guest/{user_id}",
    responses(
        (status = 200),
        (status = 403, description = "not admin / no auth"),
        (status = 404, description = "guest not found"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn verify_guest(
    Path(user_id): Path<Uuid>,
    State(state): State<AppState>,
) -> Result<(), ProdError> {
    let mut conn = state.pool.conn().await?;

    let _ = sqlx::query!(
        r#"
        UPDATE users
        SET role = 'verified_guest'
        WHERE id = $1 AND role = 'guest'
        "#,
        user_id
    )
    .execute(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound(err.to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    Ok(())
}

/// Delete user
#[utoipa::path(
    delete,
    tag = "Admin",
    path = "/admin/user/{user_id}",
    responses(
        (status = 200),
        (status = 403, description = "not admin / no auth"),
        (status = 404, description = "user not found"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn delete_user(
    Path(user_id): Path<Uuid>,
    State(state): State<AppState>,
) -> Result<(), ProdError> {
    let mut conn = state.pool.conn().await?;

    let _ = sqlx::query!(
        r#"
        DELETE FROM users
        WHERE id = $1
        "#,
        user_id
    )
    .execute(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound(err.to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    Ok(())
}

/// Patch user
#[utoipa::path(
    patch,
    tag = "Admin",
    path = "/admin/user/{user_id}",
    request_body = PatchProfileForm,
    responses(
        (status = 200, body = UserModel),
        (status = 400, description = "wrong data format"),
        (status = 403, description = "not admin / no auth"),
    ),
    request_body(content = PatchProfileFormData, content_type = "multipart/form-data"),
    responses(
        (status = 200, description = "Profile updated", body = UserModel),
        (status = 400, description = "Wrong body", body = String),
        (status = 403, description = "not admin / no auth"),
        (status = 404, description = "user not found"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn patch_user(
    Path(user_id): Path<Uuid>,
    State(state): State<AppState>,
    multipart: Multipart,
) -> Result<Json<UserModel>, ProdError> {
    let updated_user = update_user(user_id, multipart, state).await?;
    Ok(Json(updated_user))
}

/// List users
#[utoipa::path(
    get,
    tag = "Admin",
    path = "/admin/user/list",
    responses(
        (status = 200, body = Vec<PublicUserData>),
        (status = 403, description = "not admin / no auth"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn list_users(
    headers: HeaderMap,
    State(state): State<AppState>,
) -> Result<Json<Vec<PublicUserData>>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let company_id = claims_from_headers(&headers)?.company_id;
    let users = sqlx::query_as!(
        PublicUserData,
        r#"
        SELECT
        id, name, surname, email, avatar, role as "role: RoleModel"
        FROM users
        WHERE company_id = $1
        "#,
        company_id
    )
    .fetch_all(conn.as_mut())
    .await?;

    Ok(Json(users))
}
