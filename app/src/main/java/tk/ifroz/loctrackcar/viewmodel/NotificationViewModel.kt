package tk.ifroz.loctrackcar.viewmodel

import android.app.Application
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import tk.ifroz.loctrackcar.ui.work.NotificationWork
import tk.ifroz.loctrackcar.ui.work.NotificationWork.Companion.NOTIFICATION_ID
import java.util.*
import java.util.concurrent.TimeUnit.MILLISECONDS

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager: WorkManager = WorkManager.getInstance(application)
    internal val outputStatus: LiveData<List<WorkInfo>>
        get() = workManager.getWorkInfosByTagLiveData(NOTIFICATION_ID)

    internal fun scheduleNotification(
        customCalendar: Calendar,
        data: Data,
        tag: String,
        coordinatorLayout: CoordinatorLayout,
        error: String
    ) {
        val currentTime = System.currentTimeMillis()
        val customTime = customCalendar.timeInMillis
        if (customTime > currentTime) {
            val delay = customTime - currentTime
            val notificationWork = OneTimeWorkRequest.Builder(NotificationWork::class.java)
                .addTag(tag).setInitialDelay(delay, MILLISECONDS).setInputData(data).build()

            cancel()
            workManager.enqueue(notificationWork)
        } else {
            Snackbar.make(coordinatorLayout, error, Snackbar.LENGTH_LONG).show()
        }
    }

    internal fun cancel() {
        workManager.cancelAllWorkByTag(NOTIFICATION_ID)
    }
}