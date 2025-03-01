use axum::{extract::Path, extract::State, http::HeaderMap, Json};
use axum_macros::debug_handler;
use uuid::Uuid;

use crate::{db::Db, errors::ProdError, AppState};

#[debug_handler]
pub async fn verify_guest(
    headers: HeaderMap,
    Path(user_id): Path<Uuid>,
    State(state): State<AppState>,
) -> Result<(), ProdError> {
    let mut conn = state.pool.conn().await?;

    let _ = sqlx::query!(
        r#"
        UPDATE users
        SET role = 'verified_guest'
        WHERE id = $1
        "#,
        user_id
    )
    .execute(&mut *conn)
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound(err.to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    Ok(())
}
