package tk.ifroz.loctrackcar.viewmodel

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.ifr0z.core.viewmodel.BaseViewModel
import tk.ifroz.loctrackcar.api.GeocoderApiClient.getClient
import tk.ifroz.loctrackcar.model.Result
import tk.ifroz.loctrackcar.repository.GeocoderRepository

class GeocoderViewModel : BaseViewModel() {

    private val repository: GeocoderRepository = GeocoderRepository(getClient())

    val geocoders = MutableLiveData<Result>()

    fun getStreetName(geocoder: String, params: Map<String, String>) = launch {
        val streetName = withContext(Dispatchers.IO) {
            repository.getStreetName(geocoder, params)
        }
        geocoders.setValue(streetName)
    }
}