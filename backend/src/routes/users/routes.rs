use crate::forms::users::{LoginForm, Token, UserLoginData};
use crate::jwt::generate::create_token;
use crate::jwt::hashing::Argon;
use crate::{
    db::Db,
    errors::ProdError,
    forms::users::{PatchProfileForm, ProfileResponseForm},
    jwt::generate::claims_from_headers,
    models::{RoleModel, UserModel},
    AppState,
};
use axum::{extract::State, http::HeaderMap, Json};

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
            _ => ProdError::DatabaseError(err)
        })?;

    if !Argon::verify(form.password.as_bytes(), &credentials.password)? {
        return Err(ProdError::Forbidden("wrong password".to_string()));
    }

    let token = create_token(&credentials.id, &credentials.role)?;

    Ok(Json(Token {
        jwt: token,
    }))
}

pub async fn patch_profile(
    headers: HeaderMap,
    State(state): State<AppState>,
    Json(form): Json<PatchProfileForm>,
) -> Result<Json<UserModel>, ProdError> {
    let mut conn = state.pool.conn().await?;
    let user_id = claims_from_headers(&headers)?.id;

    // TODO: implement argon2 hashing
    let hashed_password = form.password;

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
                  company_id, role as "role: RoleModel"
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
