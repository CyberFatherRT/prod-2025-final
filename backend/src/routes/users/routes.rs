use axum::{extract::State, http::HeaderMap};

use crate::{
    db::Db, errors::ProdError, forms::users::ProfileResponseForm, models::RoleModel, AppState,
};

pub async fn profile(
    headers: HeaderMap,
    State(state): State<AppState>,
) -> Result<ProfileResponseForm, ProdError> {
    let mut conn = state.pool.conn().await?;

    let _ = sqlx::query_as!(
        ProfileResponseForm,
        r#"
        SELECT username,
               email,
               avatar,
               company_id,
               role as "role: RoleModel",
               (EXISTS (SELECT 1 FROM pending_verifications pv WHERE pv.user_id = u.id))::boolean as pending_verification
        FROM users u
        "#
    )
    .fetch_one(&mut *conn)
    .await;

    todo!()
}
