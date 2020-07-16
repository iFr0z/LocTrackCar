package tk.ifroz.loctrackcar.ui.viewstate

sealed class MainState<out T : Any> {

    object Idle : MainState<Nothing>()
    object Loading : MainState<Nothing>()
    data class Success<out T : Any>(val data: T) : MainState<T>()
    data class Error(val error: String?) : MainState<Nothing>()
}