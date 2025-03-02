use crate::controllers::users::{register_user, update_user};
use crate::forms::users::{LoginForm, PatchProfileFormData, RegisterForm, Token, UserLoginData};
use crate::jwt::generate::create_token;
use crate::jwt::hashing::Argon;

use crate::util::ValidatedJson;
use crate::{
    db::Db,
    errors::ProdError,
    forms::users::ProfileResponseForm,
    jwt::generate::claims_from_headers,
    models::{RoleModel, UserModel},
    AppState,
};
use axum::extract::Multipart;
use axum::{extract::State, http::HeaderMap, Json};

/// Login user
#[utoipa::path(
    post,
    tag = "Users",
    path = "/user/login",
    request_body = LoginForm,
    responses(
        (status = 200, body = Token),
        (status = 403, description = "wrong credentials"),
    )
)]
pub async fn login(
    State(state): State<AppState>,
    ValidatedJson(form): ValidatedJson<LoginForm>,
) -> Result<Json<Token>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let UserLoginData {
        id,
        password,
        company_id,
        role,
        ..
    } = sqlx::query_as!(
        UserLoginData,
        r#"
        SELECT email,
               id,
               password,
               company_id,
               role as "role: RoleModel"
        FROM users
        WHERE users.email = $1 AND users.company_domain = $2
        "#,
        form.email,
        form.domain
    )
    .fetch_one(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound("No such user".to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    if !Argon::verify(form.password.as_bytes(), &password)? {
        return Err(ProdError::Forbidden("wrong password".to_string()));
    }

    let token = create_token(&id, &company_id, &role)?;

    Ok(Json(Token { jwt: token }))
}

/// Register user
#[utoipa::path(
    post,
    tag = "Users",
    path = "/user/register",
    request_body = RegisterForm,
    responses(
        (status = 200, body = Token),
        (status = 400, description = "wrong data format"),
        (status = 409, description = "conflict")
    )
)]
pub async fn register(
    State(state): State<AppState>,
    ValidatedJson(form): ValidatedJson<RegisterForm>,
) -> Result<Json<Token>, ProdError> {
    let user = register_user(state, form, RoleModel::Guest).await?;
    let token = create_token(&user.id, &user.company_id, &user.role)?;

    Ok(Json(Token { jwt: token }))
}

/// Get user profile
#[utoipa::path(
    get,
    tag = "Users",
    path = "/user/profile",
    responses(
        (status = 200, body = ProfileResponseForm),
        (status = 403, description = "no auth / invalid auth"),
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn profile(
    headers: HeaderMap,
    State(state): State<AppState>,
) -> Result<Json<ProfileResponseForm>, ProdError> {
    let mut conn = state.pool.conn().await?;

    let user_id = claims_from_headers(&headers)?.user_id;

    let user_profile = sqlx::query_as!(
        ProfileResponseForm,
        r#"
        SELECT name,
               surname,
               email,
               avatar,
               company_id,
               role as "role: RoleModel",
               (EXISTS (SELECT 1 FROM pending_verifications pv WHERE pv.user_id = u.id))::boolean as pending_verification
        FROM users u
        WHERE u.id = $1
        "#,
        user_id
    )
        .fetch_one(conn.as_mut())
        .await
        .map_err(|err| match err {
            sqlx::Error::RowNotFound => ProdError::NotFound(err.to_string()),
            _ => ProdError::DatabaseError(err)
        })?;

    Ok(Json(user_profile))
}

/// Update user profile with mutlipart body
#[utoipa::path(
    patch,
    tag = "Users",
    path = "/user/profile",
    request_body = PatchProfileForm,
    responses(
        (status = 200, body = UserModel),
        (status = 400, description = "wrong data format"),
        (status = 403, description = "no auth / invalid auth"),
    ),
    request_body(content = PatchProfileFormData, content_type = "multipart/form-data"),
    responses(
        (status = 200, description = "Profile updated", body = UserModel),
        (status = 400, description = "Wrong body", body = String)
    ),
    security(
        ("bearerAuth" = [])
    )
)]
pub async fn patch_profile(
    headers: HeaderMap,
    State(state): State<AppState>,
    multipart: Multipart,
) -> Result<Json<UserModel>, ProdError> {
    let user_id = claims_from_headers(&headers)?.user_id;
    let updated_user = update_user(user_id, multipart, state).await?;
    Ok(Json(updated_user))
}
