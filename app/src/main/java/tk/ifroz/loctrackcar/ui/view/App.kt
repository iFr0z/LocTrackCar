package tk.ifroz.loctrackcar.ui.view

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(mapKitApiKey)
    }

    companion object {
        const val mapKitApiKey = "bb20cb74-9351-4c60-a3c3-494214e391ac"
    }
}