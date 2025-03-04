use crate::errors::ProdError;
use crate::jwt::generate::{validate_qr_token, validate_token};
use crate::models::{BookingModel, RoleModel};
use chrono::{Duration, Utc};
use serde::{Deserialize, Serialize};
use std::str::FromStr;
use uuid::Uuid;

const JWT_EXPIRY_HOURS: i64 = 24;

#[derive(Serialize, Deserialize, Clone)]
pub struct Claims {
    pub role: RoleModel,
    pub user_id: Uuid,
    pub company_id: Uuid,
    pub iat: i64,
    pub exp: i64,
}

#[derive(Serialize, Deserialize, Clone, Debug)]
pub struct QrClaims {
    pub booking_id: Uuid,
    pub iat: i64,
    pub exp: i64,
}

impl QrClaims {
    pub fn new(booking: &BookingModel) -> Self {
        let iat = Utc::now();
        let exp = booking.time_end.and_utc();

        Self {
            booking_id: booking.id,
            iat: iat.timestamp(),
            exp: exp.timestamp(),
        }
    }
}

impl Claims {
    pub fn new(user_id: &Uuid, company_id: &Uuid, role: &RoleModel) -> Self {
        let iat = Utc::now();
        let exp = iat + Duration::hours(JWT_EXPIRY_HOURS);

        Self {
            role: role.clone(),
            user_id: *user_id,
            company_id: *company_id,
            iat: iat.timestamp(),
            exp: exp.timestamp(),
        }
    }
}

impl FromStr for QrClaims {
    type Err = ProdError;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let claims: Vec<_> = s.split(' ').collect();
        let token = claims.get(1).ok_or(ProdError::ShitHappened(
            "Wrong authorization Bearer format".to_string(),
        ))?;
        validate_qr_token(token)
    }
}

impl FromStr for Claims {
    type Err = ProdError;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let claims: Vec<_> = s.split(' ').collect();
        let token = claims.get(1).ok_or(ProdError::ShitHappened(
            "Wrong authorization Bearer format".to_string(),
        ))?;
        validate_token(token)
    }
}
