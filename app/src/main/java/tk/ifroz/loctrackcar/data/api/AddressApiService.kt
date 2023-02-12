package tk.ifroz.loctrackcar.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import tk.ifroz.loctrackcar.data.model.address.AddressResult

interface AddressApiService {

    @GET("?apikey=d8aca5f4-f66d-4207-b8ab-d26c33f96ea2&format=json&results=1")
    suspend fun getAddress(@Query("geocode") geocode: String): Response<AddressResult>
}