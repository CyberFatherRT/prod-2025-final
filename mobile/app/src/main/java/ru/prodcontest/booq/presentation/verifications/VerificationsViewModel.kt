package ru.prodcontest.booq.presentation.verifications

import ru.prodcontest.booq.BuildConfig
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.prodcontest.booq.R
import ru.prodcontest.booq.domain.repository.ApiRepository
import ru.prodcontest.booq.domain.util.ResultWrapper
import ru.prodcontest.booq.domain.util.UploadProgress
import ru.prodcontest.booq.presentation.BaseViewModel
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject

@HiltViewModel
class VerificationsViewModel @Inject constructor(
    private val apiRepository: ApiRepository,
    @ApplicationContext private val context: Context,
    val httpClient: HttpClient
) : BaseViewModel<VerificationsScreenState, ProfileScreenAction>() {
    override fun setInitialState() = VerificationsScreenState(
        verificationsInfo = null,
        isLoading = true,
        error = null,
    )

    init {
        getVerificationsInfo()
    }

    fun downloadPDF(link: String) = viewModelScope.launch{
        httpClient.get(link).bodyAsBytes().inputStream()
    }

    fun openPDFContent(context: Context, inputStream: InputStream, fileName: String) {//saving in cache directory
        val filePath = context.externalCacheDir?.absolutePath ?: context.cacheDir.absolutePath
        val fileNameExtension =
            if (fileName != "") fileName else context.getString(R.string.app_name) + ".pdf"

        inputStream.use { input ->
            Files.copy(input, Paths.get(filePath + fileNameExtension))
        }

        val file = File(filePath + fileNameExtension);

        val uri = FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider", file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(uri, "application/pdf")
        }
        context.startActivity(Intent.createChooser(intent, "Select app"))
    }

    private fun getVerificationsInfo() = viewModelScope.launch {
        apiRepository.getVerifications().onEach {
            when (it) {
                is ResultWrapper.Ok -> {
                    setState { copy(verificationsInfo = it.data, isLoading = false, error = null) }
                }

                is ResultWrapper.Error -> {
                    setState { copy(verificationsInfo = null, isLoading = false, error = it.message) }
                }

                ResultWrapper.Loading -> {
                    setState { copy(verificationsInfo = null, isLoading = true, error = null) }
                }
            }
        }.collect()
    }

    fun approveUser(userId: String) {
        Log.d("MEOW", userId)

        viewModelScope.launch {
            apiRepository.verifyGuest(userId).collect {
                when(it) {
                    is ResultWrapper.Error -> {}
                    ResultWrapper.Loading -> {}
                    is ResultWrapper.Ok -> {}
                }
            }
        }
    }

    fun declineUser(userId: String) {
        Log.d("MEOW", userId)

        viewModelScope.launch {
            apiRepository.declineGuest(userId).collect {
                when(it) {
                    is ResultWrapper.Error -> {}
                    ResultWrapper.Loading -> {}
                    is ResultWrapper.Ok -> {}
                }
            }
        }
    }

}

sealed class ProfileScreenAction {
    data class ShowError(val message: String) : ProfileScreenAction()
}