package ru.ifr0z.core.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import ru.ifr0z.core.interfaces.ViewScope
import ru.ifr0z.core.livedata.singleLive
import kotlin.coroutines.CoroutineContext

open class BaseViewModel : ViewModel(), ViewScope {

    private val job = SupervisorJob()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError(throwable)
    }
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job + exceptionHandler

    private val liveError = singleLive<Throwable>()

    /**
     * Make a call the specific [block] function in [Dispatchers.Default],
     * suspend until it completes and return the value
     */
    protected suspend fun <T> call(block: suspend CoroutineScope.() -> T): T {
        return withContext(block = block, context = Dispatchers.Default)
    }

    override fun onClose() {
        coroutineContext.cancelChildren()
    }

    override fun onError(throwable: Throwable) {
        Log.w("ScopeDelegate", "onError coroutine:", throwable)
        liveError.value = throwable
    }

    override fun onCleared() {
        onClose()
        super.onCleared()
    }
}