package ru.prodcontest.booq.presentation.map

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import kotlinx.serialization.Serializable

@Serializable
object MapScreenDestination

data class Space(
    val width: Int,
    val height: Int,
    val items: List<SpaceItem>
)

data class SpaceItem(val basePoint: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    var offset by remember { mutableStateOf(Offset(10f, 50f)) }
    val ctx = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        Toast.makeText(ctx, "asdasdasd", Toast.LENGTH_SHORT).show()
    }
    val mtoast = { txt: String ->
        Toast.makeText(ctx, txt, Toast.LENGTH_SHORT).show()
    }

    val rect = Rect(Offset(0f, 0f), Size(200f, 200f))

    Canvas(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    offset += dragAmount
                }
            }
            .pointerInput(Unit) {
                detectTapGestures {
                    Log.d("MEOW", "$it ${rect.left} ${rect.top} ${rect.right} ${rect.bottom}")
                    mtoast("$it $it ${rect.left} ${rect.top} ${rect.right} ${rect.bottom}")
                    if (it.x >= rect.left + offset.x && it.y >= rect.top + offset.y && it.x <= rect.right + offset.x && it.y <= rect.bottom + offset.y) {
//                    mtoast("tapped")
//                    offset += Offset(100f, 100f)
                        showBottomSheet = true
                        Log.d("MEOW", "tapped!")
                    }
                }
            }) {
        translate(offset.x, offset.y) {
            val a = size / 5F
            drawRect(color = Color.Yellow, topLeft = rect.topLeft, size = rect.size)
        }
    }
    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = {}, sheetState = sheetState) {
            Text("${rect.topLeft}")
        }
    }
}