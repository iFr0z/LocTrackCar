package tk.ifroz.loctrackcar.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import tk.ifroz.loctrackcar.model.Result

interface GeocoderApi {

    @GET("?apikey=fdab91eb-db19-4f34-b4ff-d7c4a0932ca1")
    suspend fun getStreetNameAsync(
        @Query("geocode") geocode: String,
        @Query("format") format: String,
        @Query("results") results: String
    ): Response<Result>
}