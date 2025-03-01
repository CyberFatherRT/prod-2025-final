use axum::extract::Multipart;
use axum::{extract::Path, extract::State, Json};
use uuid::Uuid;

use crate::controllers::users::update_user;
use crate::models::UserModel;
use crate::{db::Db, errors::ProdError, AppState};

/// Verify guest user
#[utoipa::path(
    post,
    tag = "Admin",
    path = "/admin/verify_guest/{user_id}",
    responses(
        (status = 200),
        (status = 403, description = "not admin / no auth"),
        (status = 404, description = "guest not found"),
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
    path = "/user/{user_id}",
    responses(
        (status = 200),
        (status = 403, description = "not admin / no auth"),
        (status = 404, description = "user not found"),
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
    path = "/user/{user_id}",
    responses(
        (status = 200),
        (status = 403, description = "not admin / no auth"),
        (status = 404, description = "user not found"),
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
