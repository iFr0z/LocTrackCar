package tk.ifroz.loctrackcar.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.work.Data
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.reminder_fragment.view.*
import ru.ifr0z.core.extension.snackBarBottom
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

    private val addressViewModel: AddressViewModel by activityViewModels()
    private val reminderViewModel: ReminderViewModel by activityViewModels()
    private val carViewModel: CarViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        view.apply {
            val titleNotification = getString(R.string.notification)
            collapsing_toolbar_l.title = titleNotification

            done_fab.setOnClickListener {
                val customCalendar = Calendar.getInstance().apply {
                    set(date_p.year, date_p.month, date_p.dayOfMonth, time_p.hour, time_p.minute, 0)
                }
                val currentTime = System.currentTimeMillis()
                val customTime = customCalendar.timeInMillis
                if (customTime > currentTime) {
                    addressViewModel.addressName.observe(
                        viewLifecycleOwner, Observer { addressName ->
                            val data = Data.Builder().putInt(NOTIFICATION_ID, 0)
                                .putString(NOTIFICATION_ADDRESS, addressName).build()

                            reminderViewModel.scheduleNotification(customCalendar, data)
                        }
                    )

                    reminderViewModel.outputStatus.observe(
                        viewLifecycleOwner, Observer { listOfWorkInfo ->
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

                                    val notificationCreated =
                                        getString(R.string.notification_created)

                                    coordinator_l.snackBarBottom(
                                        notificationCreated, Snackbar.LENGTH_SHORT
                                    ) {}
                                }
                            }
                        }
                    )
                } else {
                    val notificationError = getString(R.string.notification_error)
                    coordinator_l.snackBarBottom(notificationError, Snackbar.LENGTH_SHORT) {}
                }
            }
        }
    }

    companion object {
        const val datePattern = "dd.MM.yy \u00B7 HH:mm"
    }
}