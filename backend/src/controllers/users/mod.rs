use crate::db::Db;
use crate::errors::ProdError;
use crate::forms::users::PatchProfileForm;
use crate::jwt::hashing::Argon;
use crate::models::RoleModel;
use crate::models::UserModel;
use crate::s3::utils::upload_file;
use crate::AppState;
use axum::body::Bytes;
use axum::extract::Multipart;
use tracing::log::warn;
use uuid::Uuid;

pub async fn update_user(
    user_id: Uuid,
    mut multipart: Multipart,
    state: AppState,
) -> Result<UserModel, ProdError> {
    let mut conn = state.pool.conn().await?;

    let mut form: Option<PatchProfileForm> = None;
    let mut image: Option<Bytes> = None;
    let mut image_content_type: Option<String> = None;

    while let Ok(Some(field)) = multipart.next_field().await {
        if let Some(field_name) = field.name() {
            match field_name {
                "json" => {
                    if let Ok(text) = field.text().await {
                        match serde_json::from_str::<PatchProfileForm>(&text) {
                            Ok(data) => form = Some(data),
                            Err(err) => {
                                warn!("Failed to parse JSON: {}", err);
                            }
                        }
                    }
                }
                "avatar" => {
                    if let Some(content_type) = field.content_type() {
                        let valid = ["image/png", "image/jpeg", "image/bmp", "image/svg"]
                            .contains(&content_type);

                        if !valid {
                            return Err(ProdError::ShitHappened("Wrong image format".to_string()));
                        }

                        image_content_type = Some(content_type.to_string());
                        image = Some(
                            field
                                .bytes()
                                .await
                                .map_err(|err| ProdError::ShitHappened(err.to_string()))?,
                        );
                    }
                }
                _ => {
                    warn!("Unknown field: {}", field_name);
                }
            }
        }
    }

    let mut avatar_url: Option<String> = None;

    if let Some(image) = image {
        let name = format!("users/{user_id}/avatar");
        upload_file(&state, &name, image_content_type.expect("Really???"), image).await?;
        avatar_url = Some(name);
    }

    let hashed_password = match form.as_ref().and_then(|data| data.password.as_ref()) {
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
        form.as_ref().and_then(|data| data.name.as_ref()),
        form.as_ref().and_then(|data| data.surname.as_ref()),
        hashed_password,
        avatar_url,
    )
    .fetch_one(conn.as_mut())
    .await
    .map_err(|err| match err {
        sqlx::Error::RowNotFound => ProdError::NotFound(err.to_string()),
        _ => ProdError::DatabaseError(err),
    })?;

    Ok(user)
}
