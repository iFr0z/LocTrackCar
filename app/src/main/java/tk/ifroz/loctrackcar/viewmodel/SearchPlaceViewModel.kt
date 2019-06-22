package tk.ifroz.loctrackcar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchPlaceViewModel : ViewModel() {

    private var searchPlaceResults = MutableLiveData<List<String>>()
    internal val searchPlaceResult: LiveData<List<String>>
        get() = searchPlaceResults

    internal fun update(searchPlaceResult: List<String>?) {
        searchPlaceResults.value = searchPlaceResult
    }

    internal fun clear() {
        searchPlaceResults.postValue(null)
    }
}
