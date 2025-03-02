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
import ru.prodcontest.booq.domain.util.ResultWrapper
import ru.prodcontest.booq.presentation.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiRepository: ApiRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel<ProfileScreenState, Nothing>() {
    override fun setInitialState() = ProfileScreenState(
        profileInfo = null,
        isLoading = true,
        error = null
    )

    init {
        getProfileInfo()
    }

    fun getProfileInfo() = viewModelScope.launch {
        apiRepository.getProfile().onEach {
            Log.d("CHLEEEN", it.toString())
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

    fun uploadFile(fileUri: Uri) {
        Log.d("MEOW", fileUri.toString())
        val a = context.contentResolver.openInputStream(fileUri)!!
        val data = a.readBytes()
        a.close()

        Log.d("MEOW", "${data.size}")
    }
}