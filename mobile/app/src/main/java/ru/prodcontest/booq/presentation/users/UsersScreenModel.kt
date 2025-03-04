package ru.prodcontest.booq.presentation.users


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
import ru.prodcontest.booq.presentation.verifications.VerificationsScreenState
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject

@HiltViewModel
class UsersScreenModel @Inject constructor(
    private val apiRepository: ApiRepository,
    @ApplicationContext private val context: Context,
) : BaseViewModel<UsersScreenState, ProfileScreenAction>() {
    override fun setInitialState() = UsersScreenState(
        usersInfo = null,
        isLoading = true,
        error = null,
    )

    init {
        getUsersInfo()
    }

    private fun getUsersInfo() = viewModelScope.launch {
        apiRepository.listUsers().onEach {
            when (it) {
                is ResultWrapper.Ok -> {
                    setState { copy(usersInfo = it.data, isLoading = false, error = null) }
                }

                is ResultWrapper.Error -> {
                    setState { copy(usersInfo = null, isLoading = false, error = it.message) }
                }

                ResultWrapper.Loading -> {
                    setState { copy(usersInfo = null, isLoading = true, error = null) }
                }
            }
        }.collect()
    }

//    fun approveUser(userId: String) {
//        Log.d("MEOW", userId)
//
//        viewModelScope.launch {
//            apiRepository.verifyGuest(userId).collect {
//                when(it) {
//                    is ResultWrapper.Error -> {}
//                    ResultWrapper.Loading -> {}
//                    is ResultWrapper.Ok -> {}
//                }
//            }
//        }
//    }

    fun deleteUser(userId: String) {
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