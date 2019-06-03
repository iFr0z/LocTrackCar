package tk.ifroz.loctrackcar.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders.of
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar.make
import kotlinx.android.synthetic.main.activity_notification.*
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.db.entity.Reminder
import tk.ifroz.loctrackcar.ui.work.NotificationWork
import tk.ifroz.loctrackcar.ui.work.NotificationWork.Companion.NOTIFICATION_ID
import tk.ifroz.loctrackcar.viewmodel.MarkerCarViewModel
import java.lang.System.currentTimeMillis
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.getDefault
import java.util.concurrent.TimeUnit.MILLISECONDS

class NotificationActivity : AppCompatActivity() {

    private lateinit var markerCarViewModel: MarkerCarViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        userInterface()
    }

    private fun userInterface() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val titleNotification = getString(R.string.notification_title)
        collapsing_toolbar_l.title = titleNotification

        done_fab.setOnClickListener {
            val currentCalendar = Calendar.getInstance()
            val specificCalendarToTrigger = Calendar.getInstance()
            specificCalendarToTrigger.set(
                date_p.year, date_p.month, date_p.dayOfMonth, time_p.hour, time_p.minute, 0
            )

            if (specificCalendarToTrigger >= currentCalendar) {
                val data = Data.Builder().putInt(NOTIFICATION_ID, 0).build()
                val currentTime = currentTimeMillis()
                val specificTimeToTrigger = specificCalendarToTrigger.timeInMillis
                val delay = specificTimeToTrigger - currentTime
                scheduleNotification(delay, data, NOTIFICATION_ID)

                val patternNotification = getString(R.string.notification_pattern)
                markerCarViewModel = of(this).get(MarkerCarViewModel::class.java)
                markerCarViewModel.upsertReminder(
                    Reminder(
                        SimpleDateFormat(
                            patternNotification, getDefault()
                        ).format(specificCalendarToTrigger.time).toString()
                    )
                )

                finish()
            } else {
                val errorNotification = getString(R.string.notification_error)
                make(coordinator_l, errorNotification, LENGTH_LONG).show()
            }
        }
    }

    private fun scheduleNotification(delay: Long, data: Data, tag: String) {
        val notificationWork = OneTimeWorkRequest.Builder(NotificationWork::class.java).addTag(tag)
            .setInitialDelay(delay, MILLISECONDS).setInputData(data).build()

        val instanceWorkManager = WorkManager.getInstance(this)
        instanceWorkManager.cancelAllWorkByTag(tag)
        instanceWorkManager.enqueue(notificationWork)
    }
}