package tk.ifroz.loctrackcar.repository

import ru.ifr0z.core.repository.BaseRepository
import tk.ifroz.loctrackcar.api.GeocoderApi
import tk.ifroz.loctrackcar.model.Result

class GeocoderRepository(private val api: GeocoderApi) : BaseRepository() {

    suspend fun getStreetName(geocode: String, format: String, results: String): Result? {
        return safeApiCall(
            call = {
                api.getStreetNameAsync(geocode, format, results)
            },
            errorMessage = "Error get street name"
        )
    }
}