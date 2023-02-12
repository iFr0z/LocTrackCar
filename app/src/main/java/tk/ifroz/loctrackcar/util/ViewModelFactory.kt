package tk.ifroz.loctrackcar.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import tk.ifroz.loctrackcar.data.api.AddressApiHelper
import tk.ifroz.loctrackcar.data.repository.AddressRepository
import tk.ifroz.loctrackcar.ui.viewmodel.AddressViewModel

class ViewModelFactory(private val addressApiHelper: AddressApiHelper) : ViewModelProvider.Factory {

    @ExperimentalCoroutinesApi
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddressViewModel::class.java)) {
            return AddressViewModel(AddressRepository(addressApiHelper)) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}