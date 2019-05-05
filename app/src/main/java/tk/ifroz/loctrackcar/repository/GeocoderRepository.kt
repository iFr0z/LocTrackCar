package tk.ifroz.loctrackcar.repository

import ru.ifr0z.core.repository.BaseRepository
import tk.ifroz.loctrackcar.api.GeocoderApi
import tk.ifroz.loctrackcar.model.Result

class GeocoderRepository(private val api: GeocoderApi) : BaseRepository() {

    suspend fun getStreetName(geocode: String, params: Map<String, String>): Result? {
        return safeApiCall(
            call = {
                api.getStreetNameAsync(geocode, params).await()
            },
            errorMessage = "Error get street name"
        )
    }
}