package tk.ifroz.loctrackcar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.Data
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import tk.ifroz.loctrackcar.work.ReminderWork
import tk.ifroz.loctrackcar.work.ReminderWork.Companion.NOTIFICATION_WORK
import java.lang.System.currentTimeMillis
import java.util.*
import java.util.concurrent.TimeUnit.MILLISECONDS

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager: WorkManager = WorkManager.getInstance(application)
    internal val outputStatus: LiveData<List<WorkInfo>>
        get() = workManager.getWorkInfosForUniqueWorkLiveData(NOTIFICATION_WORK)

    internal fun scheduleNotification(customCalendar: Calendar, data: Data) {
        val currentTime = currentTimeMillis()
        val customTime = customCalendar.timeInMillis
        if (customTime > currentTime) {
            val delay = customTime - currentTime
            val reminderWork = OneTimeWorkRequest.Builder(ReminderWork::class.java)
                .setInitialDelay(delay, MILLISECONDS).setInputData(data).build()

            workManager.beginUniqueWork(NOTIFICATION_WORK, REPLACE, reminderWork).enqueue()
        }
    }

    internal fun cancel() {
        workManager.cancelUniqueWork(NOTIFICATION_WORK)
    }
}