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
import kotlinx.android.synthetic.main.reminder_fragment.view.*
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.db.entity.Reminder
import tk.ifroz.loctrackcar.viewmodel.AddressViewModel
import tk.ifroz.loctrackcar.viewmodel.CarViewModel
import tk.ifroz.loctrackcar.viewmodel.ReminderViewModel
import tk.ifroz.loctrackcar.work.ReminderWork.Companion.NOTIFICATION_ADDRESS
import tk.ifroz.loctrackcar.work.ReminderWork.Companion.NOTIFICATION_ID
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.getDefault

class ReminderFragment : BottomSheetDialogFragment() {

    private lateinit var addressViewModel: AddressViewModel
    private lateinit var reminderViewModel: ReminderViewModel
    private lateinit var carViewModel: CarViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)

        addressViewModel = of(this.activity!!).get(AddressViewModel::class.java)
        reminderViewModel = of(this.activity!!).get(ReminderViewModel::class.java)
        carViewModel = of(this.activity!!).get(CarViewModel::class.java)
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
        val titleNotification = getString(R.string.notification)
        view.collapsing_toolbar_l.title = titleNotification

        view.done_fab.setOnClickListener {
            val customCalendar = Calendar.getInstance()
            customCalendar.set(
                view.date_p.year,
                view.date_p.month,
                view.date_p.dayOfMonth,
                view.time_p.hour,
                view.time_p.minute,
                0
            )

            addressViewModel.addressName.observe(this, Observer { addressName ->
                val data = Data.Builder().putInt(NOTIFICATION_ID, 0)
                    .putString(NOTIFICATION_ADDRESS, addressName).build()

                reminderViewModel.scheduleNotification(customCalendar, data)
            })
            reminderViewModel.outputStatus.observe(
                this, Observer<List<WorkInfo>> { listOfWorkInfo ->
                    listOfWorkInfo?.let {
                        if (listOfWorkInfo.isNullOrEmpty()) {
                            return@Observer
                        }
                        val workInfo = listOfWorkInfo[0]
                        if (!workInfo.state.isFinished) {
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
        const val datePattern = "dd.MM.yy \u00B7 HH:mm"
    }
}