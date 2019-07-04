package tk.ifroz.loctrackcar.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders.of
import androidx.work.Data
import androidx.work.WorkInfo
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.reminder_fragment.*
import kotlinx.android.synthetic.main.reminder_fragment.view.*
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.db.entity.Reminder
import tk.ifroz.loctrackcar.viewmodel.CarViewModel
import tk.ifroz.loctrackcar.viewmodel.ReminderViewModel
import tk.ifroz.loctrackcar.work.ReminderWork
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.getDefault

class ReminderFragment : BottomSheetDialogFragment() {

    private lateinit var reminderViewModel: ReminderViewModel
    private lateinit var carViewModel: CarViewModel

    init {
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.reminder_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userInterface(view)
    }

    private fun userInterface(view: View) {
        val titleNotification = getString(R.string.notification_title)
        view.collapsing_toolbar_l.title = titleNotification

        view.done_fab.setOnClickListener {
            reminderViewModel = of(this.activity!!).get(ReminderViewModel::class.java)
            val customCalendar = Calendar.getInstance()
            customCalendar.set(
                date_p.year, date_p.month, date_p.dayOfMonth, time_p.hour, time_p.minute, 0
            )
            val data = Data.Builder().putInt(ReminderWork.NOTIFICATION_ID, 0).build()
            val errorReminder = getString(R.string.reminder_error)
            reminderViewModel.scheduleNotification(
                customCalendar, data, coordinator_l, errorReminder
            )
            reminderViewModel.outputStatus.observe(
                this, Observer<List<WorkInfo>> { listOfWorkInfo ->
                    listOfWorkInfo?.let {
                        if (listOfWorkInfo.isNullOrEmpty()) {
                            return@Observer
                        }
                        val workInfo = listOfWorkInfo[0]
                        if (!workInfo.state.isFinished) {
                            carViewModel = of(this.activity!!).get(CarViewModel::class.java)
                            val dateFormat = SimpleDateFormat(datePattern, getDefault())
                            carViewModel.upsertReminder(
                                Reminder(dateFormat.format(customCalendar.time).toString())
                            )

                            dialog?.onBackPressed()
                        }
                    }
                }
            )
        }
    }

    companion object {
        fun newInstance() = ReminderFragment()
        const val datePattern = "dd.MM.yy \u00B7 HH:mm"
    }
}