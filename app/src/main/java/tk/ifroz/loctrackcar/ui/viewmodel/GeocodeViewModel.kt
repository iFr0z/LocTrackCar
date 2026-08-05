package tk.ifroz.loctrackcar.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GeocodeViewModel : ViewModel() {

    private val _geocode = MutableLiveData<String?>()

    internal fun insertGeocode(geocode: String) {
        _geocode.value = geocode
    }

    internal fun fetchGeocode(): LiveData<String?> {
        return _geocode
    }

    internal fun deleteGeocode() {
        _geocode.value = null
    }
}