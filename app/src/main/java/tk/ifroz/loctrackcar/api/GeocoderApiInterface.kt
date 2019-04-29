package tk.ifroz.loctrackcar.api

import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import tk.ifroz.loctrackcar.model.Result

interface GeocoderApiInterface {
    @GET("1.x/?format=json&results=1&apikey=fdab91eb-db19-4f34-b4ff-d7c4a0932ca1")
    fun getStreetNameAsync(@Query("geocode") geocode: String): Deferred<Response<Result>>
}