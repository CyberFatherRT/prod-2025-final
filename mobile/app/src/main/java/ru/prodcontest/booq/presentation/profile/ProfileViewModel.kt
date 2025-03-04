package ru.prodcontest.booq.presentation.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.prodcontest.booq.domain.repository.ApiRepository
import ru.prodcontest.booq.domain.usecase.SetTokenUseCase
import ru.prodcontest.booq.domain.util.ResultWrapper
import ru.prodcontest.booq.domain.util.UploadProgress
import ru.prodcontest.booq.presentation.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiRepository: ApiRepository,
    private val setTokenUseCase: SetTokenUseCase,
    @ApplicationContext private val context: Context
) : BaseViewModel<ProfileScreenState, ProfileScreenAction>() {
    override fun setInitialState() = ProfileScreenState(
        profileInfo = null,
        isLoading = true,
        error = null,
        documentUploaded = false,
        documentLoadingProgress = null
    )

    init {
        getProfileInfo()
    }

    private fun getProfileInfo() = viewModelScope.launch {
        apiRepository.getProfile().onEach {
            when (it) {
                is ResultWrapper.Ok -> {
                    setState { copy(profileInfo = it.data, isLoading = false, error = null) }
                }

                is ResultWrapper.Error -> {
                    setState { copy(profileInfo = null, isLoading = false, error = it.message) }
                }

                ResultWrapper.Loading -> {
                    setState { copy(profileInfo = null, isLoading = true, error = null) }
                }
            }
        }.collect()
    }

    fun exit() {
        viewModelScope.launch {
            setTokenUseCase("")
            setAction { ProfileScreenAction.NavigateToLoginScreen }
        }
    }

    fun uploadFile(fileUri: Uri) {
        Log.d("MEOW", fileUri.toString())
        val a = context.contentResolver.openInputStream(fileUri)!!
        val data = a.readBytes()
        a.close()

        viewModelScope.launch {
            apiRepository.uploadDocument(data).collect {
                when(it) {
                    UploadProgress.Completed -> {
                        setState { copy(documentLoadingProgress = null, documentUploaded = true) }
                    }
                    is UploadProgress.Progress -> {
                        setState { copy(documentLoadingProgress = it.percent, documentUploaded = false) }
                    }
                    UploadProgress.Unknown -> {
                        setState { copy(documentLoadingProgress = 1f, documentUploaded = false) }
                    }
                    is UploadProgress.Error -> {
                        setAction { ProfileScreenAction.ShowError(it.message) }
                    }
                }
            }
        }

    }
}

sealed class ProfileScreenAction {
    data object NavigateToLoginScreen : ProfileScreenAction()
    data class ShowError(val message: String) : ProfileScreenAction()
}