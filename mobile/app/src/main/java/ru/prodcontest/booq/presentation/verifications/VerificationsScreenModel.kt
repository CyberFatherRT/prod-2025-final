package ru.prodcontest.booq.presentation.verifications

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
class VerificationsScreenModel @Inject constructor(
    private val apiRepository: ApiRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel<VerificationsScreenState, Nothing>() {
    override fun setInitialState() = VerificationsScreenState(
        verificationsInfo = null,
        isLoading = true,
        error = null
    )

    init {
        getVerificationsInfo()
    }

    fun getVerificationsInfo() = viewModelScope.launch {
        apiRepository.getVerifications().onEach {
            Log.d("Getting pending verifications", it.toString())
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

//    fun uploadFile(fileUri: Uri) {
//        Log.d("MEOW", fileUri.toString())
//        val a = context.contentResolver.openInputStream(fileUri)!!
//        val data = a.readBytes()
//        a.close()
//
//        Log.d("MEOW", "${data.size}")
//    }
}