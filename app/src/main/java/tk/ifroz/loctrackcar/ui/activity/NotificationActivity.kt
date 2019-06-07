package tk.ifroz.loctrackcar.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders.of
import androidx.work.Data
import androidx.work.WorkInfo
import kotlinx.android.synthetic.main.activity_notification.*
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.db.entity.Reminder
import tk.ifroz.loctrackcar.ui.work.NotificationWork.Companion.NOTIFICATION_ID
import tk.ifroz.loctrackcar.viewmodel.CarViewModel
import tk.ifroz.loctrackcar.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.getDefault

class NotificationActivity : AppCompatActivity() {

    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var carViewModel: CarViewModel

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
            notificationViewModel = of(this).get(NotificationViewModel::class.java)
            val customCalendar = Calendar.getInstance()
            customCalendar.set(
                date_p.year, date_p.month, date_p.dayOfMonth, time_p.hour, time_p.minute, 0
            )
            val data = Data.Builder().putInt(NOTIFICATION_ID, 0).build()
            val errorNotification = getString(R.string.notification_error)
            notificationViewModel.scheduleNotification(
                customCalendar, data, NOTIFICATION_ID, coordinator_l, errorNotification
            )
            notificationViewModel.outputStatus.observe(
                this, Observer<List<WorkInfo>> { listOfWorkInfo ->
                    listOfWorkInfo?.let {
                        if (listOfWorkInfo.isNullOrEmpty()) {
                            return@Observer
                        }
                        val workInfo = listOfWorkInfo[0]
                        if (!workInfo.state.isFinished) {
                            val patternNotification = getString(R.string.notification_pattern)
                            carViewModel = of(this).get(carViewModel::class.java)
                            carViewModel.upsertReminder(
                                Reminder(
                                    SimpleDateFormat(
                                        patternNotification, getDefault()
                                    ).format(customCalendar.time).toString()
                                )
                            )

                            finish()
                        }
                    }
                }
            )
        }
    }
}