use crate::errors::ProdError;
use crate::jwt::models::{Claims, QrClaims};
use crate::models::{BookingModel, RoleModel};
use axum::http::HeaderMap;
use jsonwebtoken::{decode, encode, Algorithm, DecodingKey, EncodingKey, Header, Validation};
use rand::RngCore;
use std::{env, sync::LazyLock};
use tonic::metadata::MetadataMap;
use uuid::Uuid;

static SECRET: LazyLock<Vec<u8>> = LazyLock::new(|| {
    env::var("JWT_SECRET").map_or_else(|_| generate_bytes(32), |data| data.as_bytes().to_vec())
});

pub fn create_token(
    user_id: &Uuid,
    company_id: &Uuid,
    role: &RoleModel,
) -> Result<String, ProdError> {
    let claims = Claims::new(user_id, company_id, role);

    encode(
        &Header::new(Algorithm::HS256),
        &claims,
        &EncodingKey::from_secret(&SECRET),
    )
    .map_err(ProdError::InvalidToken)
}

pub fn create_qr_token(booking_model: &BookingModel) -> Result<String, ProdError> {
    let claims = QrClaims::new(booking_model);

    encode(
        &Header::new(Algorithm::HS256),
        &claims,
        &EncodingKey::from_secret(&SECRET),
    )
    .map_err(ProdError::InvalidToken)
}

pub fn validate_token(token: &str) -> Result<Claims, ProdError> {
    decode::<Claims>(
        token,
        &DecodingKey::from_secret(&SECRET),
        &Validation::new(Algorithm::HS256),
    )
    .map(|data| data.claims)
    .map_err(ProdError::InvalidToken)
}

pub fn validate_qr_token(token: &str) -> Result<QrClaims, ProdError> {
    decode::<QrClaims>(
        token,
        &DecodingKey::from_secret(&SECRET),
        &Validation::new(Algorithm::HS256),
    )
    .map(|data| data.claims)
    .map_err(ProdError::InvalidToken)
}

pub fn claims_from_headers(headers: &impl Map) -> Result<Claims, ProdError> {
    if !headers.contains_key("authorization") {
        return Err(ProdError::Forbidden(
            "No authorization token was found".to_string(),
        ));
    }

    headers
        .get("authorization")
        .map_err(|_| ProdError::ShitHappened("Wrong authorization Bearer format".to_string()))?
        .expect("meow token error")
        .parse()
}

pub fn generate_bytes(number: usize) -> Vec<u8> {
    let mut buf: Vec<u8> = vec![0; number];
    let mut rng = rand::thread_rng();
    rng.fill_bytes(&mut buf);
    buf
}

pub trait Map {
    fn get(&self, key: &str) -> anyhow::Result<Option<&str>>;
    fn contains_key(&self, key: &str) -> bool;
}

impl Map for HeaderMap {
    fn get(&self, key: &str) -> anyhow::Result<Option<&str>> {
        Ok(self.get(key).map(|x| x.to_str()).transpose()?)
    }

    fn contains_key(&self, key: &str) -> bool {
        self.contains_key(key)
    }
}

impl Map for MetadataMap {
    fn get(&self, key: &str) -> anyhow::Result<Option<&str>> {
        Ok(self.get(key).map(|x| x.to_str()).transpose()?)
    }

    fn contains_key(&self, key: &str) -> bool {
        self.contains_key(key)
    }
}
