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

    Ok(())
}
