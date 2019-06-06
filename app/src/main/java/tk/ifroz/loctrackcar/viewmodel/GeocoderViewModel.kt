package tk.ifroz.loctrackcar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.*
import androidx.work.NetworkType.CONNECTED
import tk.ifroz.loctrackcar.ui.work.GeocoderWork

class GeocoderViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager: WorkManager = WorkManager.getInstance(application)
    internal val outputStatus: LiveData<List<WorkInfo>>
        get() = workManager.getWorkInfosByTagLiveData(DATA_ID)

    internal fun getStreetName(data: Data) {
        val constraints = Constraints.Builder().setRequiredNetworkType(CONNECTED).build()
        val streetNameWork = OneTimeWorkRequest.Builder(GeocoderWork::class.java).addTag(DATA_ID)
            .setInputData(data).setConstraints(constraints).build()

        workManager.enqueue(streetNameWork)
    }

    companion object {
        const val DATA_ID = "LocTrackCar_street_data_id"
    }
}