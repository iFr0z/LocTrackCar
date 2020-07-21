package tk.ifroz.loctrackcar.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.work.Data
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import kotlinx.android.synthetic.main.reminder_fragment.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.data.api.AddressApiBuilder
import tk.ifroz.loctrackcar.data.api.AddressApiHelperImpl
import tk.ifroz.loctrackcar.data.db.entity.Reminder
import tk.ifroz.loctrackcar.data.work.ReminderWork.Companion.NOTIFICATION_ADDRESS
import tk.ifroz.loctrackcar.data.work.ReminderWork.Companion.NOTIFICATION_ID
import tk.ifroz.loctrackcar.ui.viewmodel.AddressViewModel
import tk.ifroz.loctrackcar.ui.viewmodel.CarViewModel
import tk.ifroz.loctrackcar.ui.viewmodel.ReminderViewModel
import tk.ifroz.loctrackcar.util.ViewModelFactory
import tk.ifroz.loctrackcar.util.extension.snackBarBottom
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.getDefault

@ExperimentalCoroutinesApi
class ReminderFragment : BottomSheetDialogFragment() {

    private val addressViewModel: AddressViewModel by activityViewModels {
        ViewModelFactory(AddressApiHelperImpl(AddressApiBuilder.addressApiService))
    }
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
                    val addressName = addressViewModel.fetchAddressName().value
                    val data = Data.Builder().putInt(NOTIFICATION_ID, 0)
                        .putString(NOTIFICATION_ADDRESS, addressName.toString()).build()

                    reminderViewModel.scheduleNotification(customCalendar, data)
                    reminderViewModel.outputStatus.observe(viewLifecycleOwner, Observer {
                        it?.let {
                            if (it.isNullOrEmpty()) {
                                return@Observer
                            }
                            val workInfo = it[0]
                            if (!workInfo.state.isFinished) {
                                val dateFormat = SimpleDateFormat(datePattern, getDefault())
                                carViewModel.upsertReminder(
                                    Reminder(dateFormat.format(customCalendar.time).toString())
                                )

                                val notificationCreated = getString(R.string.notification_created)
                                coordinator_l.snackBarBottom(notificationCreated, LENGTH_SHORT) {}
                            }
                        }
                    })
                } else {
                    val notificationError = getString(R.string.notification_error)
                    coordinator_l.snackBarBottom(notificationError, LENGTH_SHORT) {}
                }
            }
        }
    }

    companion object {
        const val datePattern = "dd.MM.yy \u00B7 HH:mm"
    }
}