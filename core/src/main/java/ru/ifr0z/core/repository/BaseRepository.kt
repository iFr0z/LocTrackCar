package ru.ifr0z.core.repository

import android.util.Log
import retrofit2.Response
import ru.ifr0z.core.api.Result
import ru.ifr0z.core.api.Result.Error
import ru.ifr0z.core.api.Result.Success
import java.io.IOException

open class BaseRepository {

    private suspend fun <T : Any> safeApiResult(
        call: suspend () -> Response<T>, errorMessage: String
    ): Result<T> {
        val response = call.invoke()
        if (response.isSuccessful) {
            return Success(response.body()!!)
        }

        return Error(
            IOException(
                "Error Occurred during getting safe Api result, Custom ERROR - $errorMessage"
            )
        )
    }

    suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>, errorMessage: String): T? {
        val result: Result<T> = safeApiResult(call, errorMessage)
        var data: T? = null
        when (result) {
            is Success -> data = result.data
            is Error -> Log.d("BaseRepository", "$errorMessage & Exception - ${result.exception}")
        }
        return data
    }
}