package tk.ifroz.loctrackcar.data.repository

import tk.ifroz.loctrackcar.data.api.AddressApiHelper
import tk.ifroz.loctrackcar.data.model.address.AddressResult
import tk.ifroz.loctrackcar.util.BaseRepository

class AddressRepository(private val addressApiHelper: AddressApiHelper) : BaseRepository() {

    suspend fun getAddress(geocode: String): AddressResult? {
        return safeApiCall(
            call = {
                addressApiHelper.getAddress(geocode)
            },
            errorMessage = "Error get address"
        )
    }
}