package com.example.detector.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

abstract class BaseViewModel<EVENT> : ViewModel() {

    private val _errorFlow = MutableSharedFlow<Error>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val errorFlow: SharedFlow<Error> = _errorFlow

    private val handler = CoroutineExceptionHandler { _, exception ->
        handleError(ContentError(exception))
    }

    internal open fun onTriggerEvent(eventType: EVENT) {
        //By default, this function nothing does
    }

    internal open fun collectErrorFlow() {
        //By default, this function nothing does
    }

    internal fun executeSuspend(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        function: suspend () -> Unit,
    ) {
        viewModelScope.launch(handler) {
            withContext(dispatcher) {
                function.invoke()
            }
        }
    }

    internal fun handleError(error: Error) {
        _errorFlow.tryEmit(error)

        if (error is ContentError) {
            Log.e("FbaViewModel", "handleError: ", error.throwable)
        }
    }

    internal fun <V, T : StateFlow<V>> MutableStateFlow<V>.updateErrorState(value: V) {
        update { value }
    }
}
