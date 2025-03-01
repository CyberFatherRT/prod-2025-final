use crate::errors::ProdError;
use crate::jwt::generate::generate_bytes;

pub struct Argon;

impl Argon {
    pub fn hash_password(password: &[u8]) -> Result<String, ProdError> {
        let config = argon2::Config::original();
        let salt = generate_bytes(16);

        argon2::hash_encoded(password, &salt, &config).map_err(ProdError::HashingError)
    }

    pub fn verify(password: &[u8], hash: &str) -> Result<bool, ProdError> {
        argon2::verify_encoded(hash, password).map_err(ProdError::HashingError)
    }
}
