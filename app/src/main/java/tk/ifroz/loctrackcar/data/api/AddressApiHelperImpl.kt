package tk.ifroz.loctrackcar.data.api

import retrofit2.Response
import tk.ifroz.loctrackcar.data.model.address.AddressResult

class AddressApiHelperImpl(private val addressApiService: AddressApiService) : AddressApiHelper {

    override suspend fun getAddress(geocode: String): Response<AddressResult> {
        return addressApiService.getAddress(geocode)
    }
}