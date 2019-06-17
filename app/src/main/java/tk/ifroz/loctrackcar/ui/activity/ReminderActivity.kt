package tk.ifroz.loctrackcar.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders.of
import androidx.work.Data
import androidx.work.WorkInfo
import kotlinx.android.synthetic.main.activity_reminder.*
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.db.entity.Reminder
import tk.ifroz.loctrackcar.ui.work.ReminderWork.Companion.NOTIFICATION_ID
import tk.ifroz.loctrackcar.viewmodel.CarViewModel
import tk.ifroz.loctrackcar.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.getDefault

class ReminderActivity : AppCompatActivity() {

    private lateinit var reminderViewModel: ReminderViewModel
    private lateinit var carViewModel: CarViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

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
            reminderViewModel = of(this).get(ReminderViewModel::class.java)
            val customCalendar = Calendar.getInstance()
            customCalendar.set(
                date_p.year, date_p.month, date_p.dayOfMonth, time_p.hour, time_p.minute, 0
            )
            val data = Data.Builder().putInt(NOTIFICATION_ID, 0).build()
            val errorReminder = getString(R.string.reminder_error)
            reminderViewModel.scheduleNotification(
                customCalendar, data, NOTIFICATION_ID, coordinator_l, errorReminder
            )
            reminderViewModel.outputStatus.observe(
                this, Observer<List<WorkInfo>> { listOfWorkInfo ->
                    listOfWorkInfo?.let {
                        if (listOfWorkInfo.isNullOrEmpty()) {
                            return@Observer
                        }
                        val workInfo = listOfWorkInfo[0]
                        if (!workInfo.state.isFinished) {
                            val patternReminder = getString(R.string.reminder_pattern)
                            carViewModel = of(this).get(CarViewModel::class.java)
                            carViewModel.upsertReminder(
                                Reminder(
                                    SimpleDateFormat(
                                        patternReminder, getDefault()
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