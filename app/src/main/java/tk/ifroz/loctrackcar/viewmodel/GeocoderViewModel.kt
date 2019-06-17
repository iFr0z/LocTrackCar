package tk.ifroz.loctrackcar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.*
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.NetworkType.CONNECTED
import tk.ifroz.loctrackcar.ui.work.GeocoderWork

class GeocoderViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager: WorkManager = WorkManager.getInstance(application)
    internal val outputStatus: LiveData<List<WorkInfo>>
        get() = workManager.getWorkInfosForUniqueWorkLiveData(GEOCODER_WORK)

    internal fun getStreetName(data: Data) {
        val constraints = Constraints.Builder().setRequiredNetworkType(CONNECTED).build()
        val streetNameWork = OneTimeWorkRequest.Builder(GeocoderWork::class.java)
            .setInputData(data).setConstraints(constraints).build()

        workManager.beginUniqueWork(GEOCODER_WORK, REPLACE, streetNameWork).enqueue()
    }

    internal fun cancel() {
        workManager.cancelUniqueWork(GEOCODER_WORK)
    }

    companion object {
        const val GEOCODER_WORK = "LocTrackCar_geocoder_work"
    }
}