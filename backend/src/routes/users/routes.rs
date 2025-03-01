use crate::forms::users::{LoginForm, RegisterForm, Token, UserLoginData};
use crate::jwt::generate::create_token;
use crate::jwt::hashing::Argon;
use crate::models::{CompanyUuid, TokenData};
use crate::{
    db::Db,
    errors::ProdError,
    forms::users::{PatchProfileForm, ProfileResponseForm},
    jwt::generate::claims_from_headers,
    models::{RoleModel, UserModel},
    AppState,
};
use axum::{extract::State, http::HeaderMap, Json};
use sqlx::Acquire;

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
    Json(form): Json<LoginForm>,
) -> Result<Json<Token>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let credentials = sqlx::query_as!(
        UserLoginData,
        r#"
        SELECT email,
               id,
               password,
               role as "role: RoleModel"
        FROM users
        WHERE users.email = $1
        "#,
        form.email
    )
    .fetch_one(&mut *conn)
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound(err.to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    if !Argon::verify(form.password.as_bytes(), &credentials.password)? {
        return Err(ProdError::Forbidden("wrong password".to_string()));
    }

    let token = create_token(&credentials.id, &credentials.role)?;

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
    Json(form): Json<RegisterForm>,
) -> Result<Json<Token>, ProdError> {
    let mut conn = state.pool.conn().await?;

    let mut tx = conn.begin().await?;
    let company = sqlx::query_as!(
        CompanyUuid,
        r#"
        SELECT id FROM companies
        WHERE companies.domain = $1
        "#,
        form.company_domain
    )
    .fetch_one(tx.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::ShitHappened(err.to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    let user = sqlx::query_as!(
        TokenData,
        r#"
        INSERT INTO users (
            name,
            surname,
            email,
            password,
            company_id,
            company_domain
        ) VALUES (
            $1,
            $2,
            $3,
            $4,
            $5,
            $6
        )
        RETURNING id, role as "role: RoleModel"
        "#,
        form.name,
        form.surname,
        form.email,
        Argon::hash_password(form.password.as_bytes())?,
        company.id,
        form.company_domain
    )
    .fetch_one(tx.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound(err.to_string()),
        sqlx::Error::Database(e) if e.is_unique_violation() => ProdError::Conflict(e.to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    let token = create_token(&user.id, &user.role)?;

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
    )
)]
pub async fn profile(
    headers: HeaderMap,
    State(state): State<AppState>,
) -> Result<Json<ProfileResponseForm>, ProdError> {
    let mut conn = state.pool.conn().await?;

    let user_id = claims_from_headers(&headers)?.id;

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
        .fetch_one(&mut *conn)
        .await
        .map_err(|err| match err {
            sqlx::Error::RowNotFound => ProdError::NotFound(err.to_string()),
            _ => ProdError::DatabaseError(err)
        })?;

    Ok(Json(user_profile))
}

/// Patch user profile
#[utoipa::path(
    patch,
    tag = "Users",
    path = "/user/profile",
    request_body = PatchProfileForm,
    responses(
        (status = 200, body = UserModel),
        (status = 400, description = "wrong data format"),
        (status = 403, description = "no auth / invalid auth"),
    )
)]
pub async fn patch_profile(
    headers: HeaderMap,
    State(state): State<AppState>,
    Json(form): Json<PatchProfileForm>,
) -> Result<Json<UserModel>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let user_id = claims_from_headers(&headers)?.id;

    let hashed_password = match form.password {
        Some(password) => Some(Argon::hash_password(password.as_bytes())?),
        None => None,
    };

    let user = sqlx::query_as!(
        UserModel,
        r#"
        UPDATE users
        SET
            name = COALESCE($2, name),
            surname = COALESCE($3, surname),
            password = COALESCE($4, password),
            avatar = COALESCE($5, avatar)
        WHERE id = $1
        RETURNING id, name, surname,
                  email, password, avatar,
                  company_id, company_domain, role as "role: RoleModel"
        "#,
        user_id,
        form.name,
        form.surname,
        hashed_password,
        form.avatar
    )
    .fetch_one(&mut *conn)
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound(err.to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    Ok(Json(user))
}
