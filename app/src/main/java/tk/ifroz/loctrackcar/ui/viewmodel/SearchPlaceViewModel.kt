package tk.ifroz.loctrackcar.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchPlaceViewModel : ViewModel() {

    val searchPlaceResults = MutableLiveData<List<String>?>()

    internal fun insertSearchPlaceResult(searchPlaceResult: List<String>?) {
        searchPlaceResults.value = searchPlaceResult
    }

    internal fun deleteSearchPlaceResult() {
        searchPlaceResults.postValue(null)
    }
}
