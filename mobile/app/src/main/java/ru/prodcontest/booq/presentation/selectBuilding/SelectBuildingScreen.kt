package ru.prodcontest.booq.presentation.selectBuilding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.data.remote.dto.PlaceDto
import ru.prodcontest.booq.presentation.map.MapScreenDestination
import java.util.UUID

@Serializable
object SelectBuildingScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectBuildingScreen(
    navController: NavController,
    viewModel: SelectBuildingViewModel = hiltViewModel()
) {
    val state = viewModel.viewState.value

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text("Выберите здание")
            },
            navigationIcon = {
                IconButton(onClick = {
                    navController.navigateUp()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
            }
        )
    }) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            state.buildings?.let { buildings ->
                LazyColumn {
                    items(buildings) {
                        PlaceCard(it, {
                            navController.navigate(MapScreenDestination(it.id))
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceCard(place: PlaceDto, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(
        onClick = onClick, modifier = modifier
            .fillMaxWidth()
            .padding(10.dp, 16.dp, 10.dp, 4.dp)
    ) {
        Row(
            Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    AsyncImage(
                        "https://atlas-content-cdn.pixelsquid.com/stock-images/apartment-building-XleR2JD-600.jpg",
                        null,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Text(
                        place.address,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(10.dp, 6.dp).fillMaxSize(0.8f)
                    )
                }
                Text("Тут будет описание. Верю в это", modifier = Modifier.padding(6.dp))
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceCardPreview1() {
    PlaceCard(
        PlaceDto(
            address = "Т-Банк Белорусская",
            id = UUID.randomUUID().toString(),
            companyId = UUID.randomUUID().toString()
        ), {})
}