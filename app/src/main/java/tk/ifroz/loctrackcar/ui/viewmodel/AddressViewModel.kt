package tk.ifroz.loctrackcar.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import tk.ifroz.loctrackcar.data.repository.AddressRepository
import tk.ifroz.loctrackcar.ui.intent.MainIntent
import tk.ifroz.loctrackcar.ui.viewstate.MainState
import tk.ifroz.loctrackcar.ui.viewstate.MainState.*

@ExperimentalCoroutinesApi
class AddressViewModel(private val addressRepository: AddressRepository) : ViewModel() {

    val addressIntent = Channel<MainIntent>(Channel.UNLIMITED)
    private val _state = MutableStateFlow<MainState<Any>>(Idle)
    val state: StateFlow<MainState<Any>>
        get() = _state

    private val _geocode = MutableLiveData<String?>()

    private val _addressName = MutableLiveData<String?>()

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            addressIntent.consumeAsFlow().collect {
                when (it) {
                    is MainIntent.FetchAddress -> fetchAddress()
                }
            }
        }
    }

    private fun fetchAddress() {
        viewModelScope.launch {
            _state.value = Loading
            _state.value = try {
                Success(
                    addressRepository.getAddress(
                        _geocode.value.toString()
                    )?.response?.geoObjectCollection?.featureMember?.get(0)?.geoObject?.name!!
                )
            } catch (e: Exception) {
                Error(e.localizedMessage)
            }
        }
    }

    internal fun insertGeocode(geocode: String) {
        _geocode.value = geocode
    }

    internal fun deleteGeocode() {
        _geocode.postValue(null)
    }

    internal fun insertAddressName(addressName: String) {
        _addressName.value = addressName
    }

    internal fun fetchAddressName(): MutableLiveData<String?> {
        return _addressName
    }

    internal fun deleteAddressName() {
        _addressName.postValue(null)
    }
}