package tk.ifroz.loctrackcar.ui.view

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import tk.ifroz.loctrackcar.BuildConfig.MAPKIT_API_KEY

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
    }
}