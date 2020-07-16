package tk.ifroz.loctrackcar.data.api

import retrofit2.Response
import tk.ifroz.loctrackcar.data.model.address.AddressResult

interface AddressApiHelper {

    suspend fun getAddress(geocode: String): Response<AddressResult>
}