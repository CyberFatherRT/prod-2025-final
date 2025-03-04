package ru.prodcontest.booq.presentation.qrScanner

import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Serializable
object QrScannerScreenDestination

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(navController: NavController, viewModel: QrScannerViewModel = hiltViewModel()) {
    val state = viewModel.viewState.value

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text("Проверка QR")
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
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Наведите камеру на QR код из приложения гостя",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(0.dp, 32.dp)
            )
            ScanCode(
                onQrCodeDetected = {
                    if (!state.modalOpened) {
                        viewModel.checkQr(it)
                    }
                },
                modifier = Modifier.fillMaxSize(0.6f)
            )
        }
        if (state.modalOpened) {
            state.verdict?.let { verdict ->
                AlertDialog(
                    onDismissRequest = {
                        viewModel.closeModal()
                    },
                    title = {
                        if (verdict.valid) {
                            Text("Всё хорошо")
                        } else {
                            Text("Некорректный код")
                        }
                        if (state.errorVerdict != null) {
                            Text("Ошибка при загрузке")
                        }
                    },
                    icon = {
                        if (verdict.valid) {
                            Icon(Icons.Default.Check, null)
                        } else {
                            Icon(Icons.Default.Close, null)
                        }
                        if (state.errorVerdict != null) {
                            Icon(Icons.Default.Warning, null)
                        }
                    },
                    text = {
                        state.errorVerdict?.let { error ->
                            Text("Ошибка при загрузке данных: $error")
                        }
                        @Composable
                        fun RowInfoComponent(
                            icon: ImageVector,
                            text: String,
                            modifier: Modifier = Modifier
                        ) {
                            Row(
                                modifier = modifier.padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .size(25.dp),
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                )
                                Spacer(
                                    modifier = Modifier.width(8.dp)
                                )
                                Text(
                                    text = text,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )

                            }
                        }
                        verdict.bookingData?.let { data ->
                            Column {
                                RowInfoComponent(Icons.Default.Home, "Здание: ${data.buildingName}")
                                RowInfoComponent(Icons.Default.LocationOn, "Коворкинг: ${data.spaceName}")
                                RowInfoComponent(Icons.Default.LocationOn, "Место: ${data.itemName}")
                                RowInfoComponent(Icons.Default.Email, "Почта: ${data.userEmail}")
                                RowInfoComponent(Icons.Default.DateRange, "Начало: ${data.timeStart.format(
                                    DateTimeFormatter.ofLocalizedDateTime(
                                        FormatStyle.MEDIUM
                                    )
                                )}")
                                RowInfoComponent(Icons.Default.DateRange, "Конец: ${data.timeEnd.format(
                                    DateTimeFormatter.ofLocalizedDateTime(
                                        FormatStyle.MEDIUM
                                    )
                                )}")
                            }
                        }
                    }, confirmButton = {
                        Button(
                            onClick = {
                                viewModel.closeModal()
                            }
                        ) {
                            Text("Закрыть")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanCode(
    onQrCodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var barcode by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var qrCodeDetected by remember { mutableStateOf(false) }

    var boundingRect by remember { mutableStateOf<Rect?>(null) }

    val cameraController = remember {
        LifecycleCameraController(context)
    }

    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    SideEffect {
        if(!cameraPermissionState.status.isGranted and !cameraPermissionState.status.shouldShowRationale) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Box(modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            Barcode.FORMAT_QR_CODE,
                            Barcode.FORMAT_CODABAR,
                            Barcode.FORMAT_CODE_93,
                            Barcode.FORMAT_CODE_39,
                            Barcode.FORMAT_CODE_128,
                            Barcode.FORMAT_EAN_8,
                            Barcode.FORMAT_EAN_13,
                            Barcode.FORMAT_AZTEC
                        )
                        .build()

                    val barcodeScanner = BarcodeScanning.getClient(options)

                    cameraController.setImageAnalysisAnalyzer(
                        ContextCompat.getMainExecutor(ctx),
                        MlKitAnalyzer(
                            listOf(barcodeScanner),
                            ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                            ContextCompat.getMainExecutor(ctx)
                        ) { result: MlKitAnalyzer.Result? ->
                            val barcodeResults = result?.getValue(barcodeScanner)
                            if (!barcodeResults.isNullOrEmpty()) {
                                barcode = barcodeResults.first().rawValue
                                qrCodeDetected = true
                                boundingRect = barcodeResults.first().boundingBox
                                Log.d(
                                    "Looking for Barcode ",
                                    barcodeResults.first().boundingBox.toString()
                                )
                            } else {
                                qrCodeDetected = false
                                barcode = null
                            }
                        }
                    )

                    cameraController.bindToLifecycle(lifecycleOwner)
                    this.controller = cameraController
                }
            }
        )
        if (qrCodeDetected) {
            DrawRectangle(rect = boundingRect)
        }
    }
    LaunchedEffect(qrCodeDetected) {
        delay(100)
        barcode?.let { onQrCodeDetected(it) }
    }
}

@Composable
fun DrawRectangle(rect: Rect?) {
    val composeRect = rect?.toComposeRect()

    composeRect?.let {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = Color.Red,
                topLeft = Offset(it.left, it.top),
                size = Size(it.width, it.height),
                style = Stroke(width = 5f)
            )
        }
    }
}