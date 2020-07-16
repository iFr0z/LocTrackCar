package tk.ifroz.loctrackcar.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import tk.ifroz.loctrackcar.data.model.address.AddressResult

interface AddressApiService {

    @GET("?apikey=fdab91eb-db19-4f34-b4ff-d7c4a0932ca1&format=json&results=1")
    suspend fun getAddress(@Query("geocode") geocode: String): Response<AddressResult>
}