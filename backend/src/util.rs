pub fn env(key: &str) -> String {
    dotenvy::var(key).unwrap_or_else(|_| panic!("`{key}` environment variable not found"))
}
