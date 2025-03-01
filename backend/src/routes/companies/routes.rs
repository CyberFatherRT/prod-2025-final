use crate::controllers::users::register_user;
use crate::db::Db;
use crate::errors::ProdError;
use crate::forms::companies::CompanyRegisterData;
use crate::forms::users::{RegisterForm, Token};
use crate::jwt::generate::create_token;
use crate::AppState;
use axum::extract::State;
use axum::Json;
use validator::Validate;

/// Add new company
#[utoipa::path(
    post,
    tag = "Companies",
    path = "/company/register",
    request_body = CompanyRegisterData,
    responses(
        (status = 200, body = Token),
        (status = 400, description = "wrong data format"),
        (status = 409, description = "conflict")
    )
)]
pub async fn company_register(
    State(state): State<AppState>,
    Json(form): Json<CompanyRegisterData>,
) -> Result<Json<Token>, ProdError> {
    form.validate()?;
    let mut conn = state.pool.conn().await?;

    let _ = sqlx::query!(
        r#"
        INSERT INTO companies (name, domain)
        VALUES ($1, $2)
        "#,
        form.name,
        form.domain,
    )
    .execute(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::Database(e) if e.is_unique_violation() => ProdError::Conflict(e.to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    let user = register_user(
        state,
        RegisterForm {
            name: form.name + " Admin",
            surname: "Admin".to_string(),
            email: form.domain.clone() + "@nonexistentemail.com",
            password: "password".to_string(),
            company_domain: form.domain,
        },
    )
    .await?;
    let token = create_token(&user.id, &user.company_id, &user.role)?;

    Ok(Json(Token { jwt: token }))
}
