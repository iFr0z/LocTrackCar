package tk.ifroz.loctrackcar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ListenableWorker.Result.success
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import tk.ifroz.loctrackcar.api.GeocoderApiClient.getClient
import tk.ifroz.loctrackcar.repository.GeocoderRepository

class GeocoderWork(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val repository: GeocoderRepository = GeocoderRepository(getClient())

    override suspend fun doWork(): Result = coroutineScope {
        val geocode = inputData.getString(GEOCODE_DATA)
        val format = inputData.getString(FORMAT_DATA)
        val results = inputData.getString(RESULTS_DATA)
        val streetName = withContext(Dispatchers.IO) {
            repository.getStreetName(geocode!!, format!!, results!!)
        }
        val outputStreetName = outputData(streetName)
        return@coroutineScope success(outputStreetName)
    }

    private fun outputData(geocode: tk.ifroz.loctrackcar.model.Result?): Data {
        val streetName = geocode!!.response.geoObjectCollection.featureMember[0].geoObject.name
        return Data.Builder().putString(OUTPUT_DATA, streetName).build()
    }

    companion object {
        const val GEOCODE_DATA = "LocTrackCar_geocode_data"
        const val FORMAT_DATA = "LocTrackCar_json_data"
        const val RESULTS_DATA = "LocTrackCar_results_data"
        const val OUTPUT_DATA = "LocTrackCar_street_data"
    }
}