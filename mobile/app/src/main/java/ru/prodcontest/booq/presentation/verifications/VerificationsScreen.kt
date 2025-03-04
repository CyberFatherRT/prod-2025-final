package ru.prodcontest.booq.presentation.verifications

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.domain.model.UserRole
import ru.prodcontest.booq.domain.model.VerificationModel
import ru.prodcontest.booq.domain.model.VerificationUserModel
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Serializable
object VerificationsScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationsScreen(
    navController: NavController, vm: VerificationsViewModel = hiltViewModel()
) {
    val state = vm.viewState.value
//    val state = ProfileScreenState(
//        isLoading = false, error = null, profileInfo = UserModel(
//            name = "Ivan",
//            surname = "Kuznetsov",
//            avatarUrl = "https://resources.jetbrains.com/storage/products/company/brand/logos/Ktor_icon.png",
//            role = UserRole.Guest,
//            companyId = UUID.randomUUID(),
//            email = "meow@nya.ru",
//            pendingVerification = false
//        )
//    )
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()



    Scaffold(topBar = {
        TopAppBar(title = { Text("Активные верификации") }, navigationIcon = {
            IconButton({
                navController.navigateUp()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        })
    }, snackbarHost = {
        SnackbarHost(snackbarHostState)
    }) { innerPadding ->
        if (state.isLoading) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            }
        }

        state.verificationsInfo?.let { verifications ->
            val sigma =
                remember { mutableStateListOf<VerificationModel>(*verifications.toTypedArray()) }

            LazyColumn(Modifier.padding(innerPadding)) {

                items(items = sigma, key = { verification ->
                    // Return a stable + unique key for the item
                    verification.user.id
                }) { verification ->
                    VerificationRow(vm, verification, {
                        vm.approveUser(verification.user.id.toString())
                        sigma.remove(verification)
                    }, {
                        vm.declineUser(verification.user.id.toString())
                        sigma.remove(verification)
                    })
                }
            }
        }
    }
}

@Composable
fun debugPlaceholder(@DrawableRes debugPreview: Int) = if (LocalInspectionMode.current) {
    painterResource(id = debugPreview)
} else {
    null
}

@SuppressLint("UnrememberedMutableState")
@Composable
private fun VerificationRow(
    vm: VerificationsViewModel,
    verification: VerificationModel,
    //onOpenFile: () -> Unit,
    onApprove: () -> Unit, onDecline: () -> Unit, modifier: Modifier = Modifier
) {
    val bottomButtonModifier =
        Modifier
            .padding(top = 6.dp, bottom = 6.dp, start = 6.dp, end = 6.dp)
            .size(30.dp)
    //.clip(CircleShape)
    val context = LocalContext.current

    var pdfFile by remember { mutableStateOf<File?>(null) }

    // Use `remember` to create an instance of HttpClient

    // Clean up the HttpClient when the composable is

    // Function to download the PDF using Ktor
    suspend fun downloadPdf(url: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val response: HttpResponse = vm.httpClient.get(url.replace("https://https://", "https://"))
                if (response.status.value in 200..299) {
                    val cacheDir = context.cacheDir
                    Log.d("here", "here")
                    val pdfFile = File(cacheDir, "document.pdf")
                    val outputStream = FileOutputStream(pdfFile)
                    outputStream.use { stream ->
                        stream.write(response.readBytes())
                    }
                    pdfFile
                } else {
                    Log.d("here2", "${response.status}")
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // Function to open the PDF
    fun openPdf(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }
    var isClicked by mutableStateOf(false)


    Card(modifier = modifier) {
        Row {
            AsyncImage(
                model = verification.user.avatarUrl
                    ?: "https://i.pinimg.com/736x/cb/45/72/cb4572f19ab7505d552206ed5dfb3739.jpg",
                contentDescription = null,
                modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
                    .padding(5.dp)
                    .wrapContentWidth(Alignment.Start)
            )

            Column {
                Text(
                    text = "${verification.user.surname} ${verification.user.name}",
                    modifier = Modifier,
                    fontSize = 15.sp
                    //.align(Alignment.CenterVertically)
                )

                Text(
                    text = verification.user.email, fontSize = 10.sp
                )
            }
        }
        Row(Modifier.fillMaxWidth()) {

            if (isClicked) {
                LaunchedEffect(Unit) {
                    Log.d("lol", verification.document)
                    pdfFile = downloadPdf(verification.document)
                    pdfFile?.let { openPdf(it) }
                    isClicked = false
                }
            }

            Button(
                onClick = {
                    isClicked = true
                },
                shape = RoundedCornerShape(30),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                modifier = Modifier
                    .padding(start = 6.dp, top = 6.dp, bottom = 6.dp)
                    .size(150.dp, 30.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(modifier = Modifier.align(Alignment.CenterVertically)) {
                    Text(
                        text = if (isClicked) "Загрузка..." else "Просмотреть документ",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onApprove,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                modifier = bottomButtonModifier,
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(modifier = Modifier.align(Alignment.CenterVertically)) {
                    Icon(Icons.Default.Check, null)
//                    Text(
//                        text = "",
//                        color = Color.White,
//                        fontSize = 10.sp,
//                        modifier = Modifier.align(Alignment.CenterVertically)
//                    )
                }
            }

            Button(
                onClick = onDecline,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = bottomButtonModifier,
                contentPadding = PaddingValues(0.dp)
            ) {

                Row(modifier = Modifier.align(Alignment.CenterVertically)) {
                    Icon(Icons.Default.Close, null)
                }
            }
        }
    }
}
//
//@Preview
//@Composable
//private fun VerificationRowPreview1() {
//    VerificationRow(onApprove = {}, onDecline = {}, verification = VerificationModel(
//        document = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
//        user = VerificationUserModel(
//            avatarUrl = "https://upload.wikimedia.org/wikipedia/commons/7/70/Example.png",
//            email = "s25e_zaborov@179.ru",
//            id = UUID.fromString("122cc42d-f38c-427f-9ef3-168e667681ff"),
//            name = "George",
//            role = UserRole.Guest,
//            surname = "Zaborov"
//        )
//    )
//    )
//}