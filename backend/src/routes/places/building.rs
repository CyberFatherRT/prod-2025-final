use axum::{
    extract::State,
    http::{HeaderMap, StatusCode},
    Json,
};

use crate::{
    db::Db, errors::ProdError, forms::places::building::CreateBuildingForm, models::BuildingModel,
    AppState,
};

pub async fn create_building(
    headers: HeaderMap,
    State(state): State<AppState>,
    Json(form): Json<CreateBuildingForm>,
) -> Result<(StatusCode, Json<BuildingModel>), ProdError> {
    let mut conn = state.pool.conn().await?;
    todo!()
}
