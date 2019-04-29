package tk.ifroz.loctrackcar.util

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.net.NetworkInfo
import android.net.NetworkRequest.Builder
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.os.Build.VERSION_CODES.N
import androidx.lifecycle.LiveData

class ConnectionUtils(private val context: Context) : LiveData<Boolean>() {

    private var connectivityManager: ConnectivityManager =
        context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

    private lateinit var connectivityManagerCallback: NetworkCallback

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateConnection()
        }
    }

    override fun onActive() {
        super.onActive()

        updateConnection()

        when {
            SDK_INT >= N -> {
                connectivityManager.registerDefaultNetworkCallback(getConnectivityManagerCallback())
            }
            SDK_INT >= LOLLIPOP -> {
                lollipopNetworkAvailableRequest()
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        if (SDK_INT >= LOLLIPOP) {
            connectivityManager.unregisterNetworkCallback(connectivityManagerCallback)
        } else {
            context.unregisterReceiver(networkReceiver)
        }
    }

    @TargetApi(LOLLIPOP)
    private fun lollipopNetworkAvailableRequest() {
        val builder = Builder().addTransportType(TRANSPORT_CELLULAR)
            .addTransportType(TRANSPORT_WIFI)

        connectivityManager.registerNetworkCallback(
            builder.build(), getConnectivityManagerCallback()
        )
    }

    private fun getConnectivityManagerCallback(): NetworkCallback {
        if (SDK_INT >= LOLLIPOP) {
            connectivityManagerCallback = object : NetworkCallback() {
                override fun onAvailable(network: Network?) {
                    postValue(true)
                }

                override fun onLost(network: Network?) {
                    postValue(false)
                }
            }
            return connectivityManagerCallback
        } else {
            throw IllegalAccessError("Should not happened")
        }
    }

    private fun updateConnection() {
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        postValue(activeNetwork?.isConnected == true)
    }
}