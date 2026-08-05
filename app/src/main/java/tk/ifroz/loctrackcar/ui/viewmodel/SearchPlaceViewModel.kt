package tk.ifroz.loctrackcar.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchPlaceViewModel : ViewModel() {

    private val _searchPlaceResults = MutableLiveData<List<String>?>()
    val searchPlaceResults: LiveData<List<String>?>
        get() = _searchPlaceResults

    internal fun insertSearchPlaceResult(searchPlaceResult: List<String>?) {
        _searchPlaceResults.value = searchPlaceResult
    }

    internal fun deleteSearchPlaceResult() {
        _searchPlaceResults.value = null
    }
}
