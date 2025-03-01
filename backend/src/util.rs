use axum::{
    extract::{FromRequest, Request},
    Json,
};
use serde::de::DeserializeOwned;
use validator::Validate;

use crate::errors::ProdError;

pub fn env(key: &str) -> String {
    dotenvy::var(key).unwrap_or_else(|_| panic!("`{key}` environment variable not found"))
}

pub struct ValidatedJson<T>(pub T);

impl<S, T> FromRequest<S> for ValidatedJson<T>
where
    S: Send + Sync,
    T: DeserializeOwned + Validate,
{
    type Rejection = ProdError;

    async fn from_request(req: Request, state: &S) -> Result<Self, Self::Rejection> {
        let Json(value): Json<T> = Json::from_request(req, state)
            .await
            .map_err(|err| ProdError::ShitHappened(err.to_string()))?;

        value.validate()?;

        Ok(Self(value))
    }
}
