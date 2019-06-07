package ru.ifr0z.core.livedata

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkRequest.Builder
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.N
import androidx.lifecycle.LiveData

class ConnectivityLiveData(context: Context) : LiveData<Boolean>() {

    private var connectivityManager: ConnectivityManager =
        context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : NetworkCallback() {
        override fun onAvailable(network: Network?) = postValue(true)

        override fun onLost(network: Network?) = postValue(false)
    }

    override fun onActive() {
        super.onActive()
        when {
            SDK_INT >= N -> connectivityManager.registerDefaultNetworkCallback(networkCallback)
            else -> connectivityManager.registerNetworkCallback(Builder().build(), networkCallback)
        }
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}