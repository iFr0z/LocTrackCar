package tk.ifroz.loctrackcar.util

import android.util.Log
import retrofit2.Response
import tk.ifroz.loctrackcar.ui.viewstate.MainState
import tk.ifroz.loctrackcar.ui.viewstate.MainState.Success
import tk.ifroz.loctrackcar.ui.viewstate.MainState.Error

open class BaseRepository {

    private suspend fun <T : Any> safeApiResult(
        call: suspend () -> Response<T>, errorMessage: String
    ): MainState<T> {
        val response = call.invoke()
        if (response.isSuccessful) {
            return Success(response.body()!!)
        }

        return Error(errorMessage)
    }

    suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>, errorMessage: String): T? {
        val result: MainState<T> = safeApiResult(call, errorMessage)
        var data: T? = null
        when (result) {
            is Success -> data = result.data
            is Error -> {
                Error(errorMessage)
                Log.d("BaseRepository: ", "${result.error}")
            }
        }
        return data
    }
}