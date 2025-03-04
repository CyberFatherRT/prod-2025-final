use crate::models::RoleModel;
use axum::body::Body;
use axum::extract::Multipart;
use axum::http::header::CONTENT_TYPE;
use axum::http::HeaderMap;
use axum::response::Response;
use axum::{extract::Path, extract::State, Json};
use sqlx::Acquire;
use uuid::Uuid;

use crate::controllers::users::update_user;
use crate::forms::admin::VerificationRequest;
use crate::forms::users::{PatchProfileFormData, PublicUserData};
use crate::jwt::generate::claims_from_headers;
use crate::models::UserModel;
use crate::s3::utils::get_file;
use crate::{db::Db, errors::ProdError, AppState, BASE_URL};

/// Get user by id
#[utoipa::path(
    get,
    tag = "Admin",
    path = "/backend_api/admin/user/{user_id}",
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
    path = "/backend_api/admin/user/{user_id}/verify",
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
    headers: HeaderMap,
    Path(user_id): Path<Uuid>,
    State(state): State<AppState>,
) -> Result<(), ProdError> {
    let mut conn = state.pool.conn().await?;
    let mut tx = conn.begin().await?;
    let company_id = claims_from_headers(&headers)?.company_id;

    let _ = sqlx::query!(
        r#"
        SELECT user_id FROM pending_verifications WHERE
        user_id = $1 AND company_id = $2
        "#,
        user_id,
        company_id
    )
    .fetch_one(tx.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => {
            ProdError::NotFound("No active request from that user".to_string())
        }
        _ => ProdError::DatabaseError(err),
    })?;

    let _ = sqlx::query!(
        r#"
        UPDATE users
        SET role = 'verified_guest'
        WHERE id = $1 AND role = 'guest' AND company_id = $2
        "#,
        user_id,
        company_id
    )
    .execute(tx.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound(err.to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    let _ = sqlx::query!(
        r#"
        DELETE FROM pending_verifications
        WHERE user_id = $1 AND company_id = $2
        "#,
        user_id,
        company_id,
    )
    .execute(tx.as_mut())
    .await?;

    tx.commit().await?;

    Ok(())
}

/// Delete user
#[utoipa::path(
    delete,
    tag = "Admin",
    path = "/backend_api/admin/user/{user_id}",
    responses(
        (status = 200),
        (status = 403, description = "not admin / no auth"),
        (status = 404, description = "user not found"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn admin_delete_user(
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
    path = "/backend_api/admin/user/{user_id}",
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
    path = "/backend_api/admin/user/list",
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

/// List active verification requests
#[utoipa::path(
    get,
    tag = "Admin",
    path = "/backend_api/admin/list_requests",
    responses(
        (status = 200, body = Vec<VerificationRequest>),
        (status = 403, description = "not admin / no auth"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn get_verify_requests(
    headers: HeaderMap,
    State(state): State<AppState>,
) -> Result<Json<Vec<VerificationRequest>>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let company_id = claims_from_headers(&headers)?.company_id;
    let users = sqlx::query_as!(
        PublicUserData,
        r#"
        SELECT
        id, name, surname, email, avatar, role as "role: RoleModel"
        FROM users u
        JOIN pending_verifications pv ON pv.user_id = u.id
        WHERE u.company_id = $1
        "#,
        company_id
    )
    .fetch_all(conn.as_mut())
    .await?;

    let mut requests: Vec<VerificationRequest> = Vec::new();
    for user in users {
        requests.push(VerificationRequest {
            document: format!("{BASE_URL}/admin/documents/{}", user.id),
            user,
        });
    }

    Ok(Json(requests))
}

/// Get user document
#[utoipa::path(
    get,
    tag = "Admin",
    path = "/backend_api/admin/documents/{user_id}",
    responses(
        (status = 200, description = "user document", content_type = "application/pdf"),
        (status = 403, description = "not admin / no auth"),
        (status = 404, description = "no such user")
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn get_user_document(
    Path(user_id): Path<Uuid>,
    headers: HeaderMap,
    State(state): State<AppState>,
) -> Result<Response, ProdError> {
    let mut conn = state.pool.conn().await?;
    let company_id = claims_from_headers(&headers)?.company_id;
    let _ = sqlx::query_as!(
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
        sqlx::Error::RowNotFound => ProdError::NotFound("No such user exists".to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    let file_name = format!("users/{user_id}/document.pdf");

    let (stream, content_type) = get_file(&state, &file_name).await?;
    let response = Response::builder()
        .header(CONTENT_TYPE, content_type)
        .body(Body::from_stream(stream))
        .map_err(|e| ProdError::Unknown(e.into()))?;

    Ok(response)
}
