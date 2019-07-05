package tk.ifroz.loctrackcar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddressViewModel : ViewModel() {

    private var addressNames = MutableLiveData<String>()
    internal val addressName: LiveData<String>
        get() = addressNames

    internal fun update(addressName: String?) {
        addressNames.value = addressName
    }

    internal fun clear() {
        addressNames.postValue(null)
    }
}