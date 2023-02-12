package tk.ifroz.loctrackcar.ui.view.fragment

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.work.Data
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.data.api.AddressApiBuilder
import tk.ifroz.loctrackcar.data.api.AddressApiHelperImpl
import tk.ifroz.loctrackcar.data.db.entity.Reminder
import tk.ifroz.loctrackcar.data.work.ReminderWork.Companion.NOTIFICATION_ADDRESS
import tk.ifroz.loctrackcar.data.work.ReminderWork.Companion.NOTIFICATION_ID
import tk.ifroz.loctrackcar.databinding.ReminderFragmentBinding
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

    private var _binding: ReminderFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var checkNotificationPermission: ActivityResultLauncher<String>
    private var isPermission = false

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
    ): View {
        _binding = ReminderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkNotificationPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            isPermission = isGranted
        }

        userInterface()

        checkPermission(view)
    }

    private fun checkPermission(view: View) {
        if (SDK_INT >= TIRAMISU) {
            if (checkSelfPermission(view.context, POST_NOTIFICATIONS) == PERMISSION_GRANTED) {
                isPermission = true
            } else {
                isPermission = false

                checkNotificationPermission.launch(POST_NOTIFICATIONS)
            }
        } else {
            isPermission = true
        }
    }

    private fun userInterface() {
        val titleNotification = getString(R.string.notification)
        binding.collapsingToolbar.title = titleNotification

        binding.doneFab.setOnClickListener {
            if (isPermission) {
                val customCalendar = Calendar.getInstance().apply {
                    set(
                        binding.datePicker.year,
                        binding.datePicker.month,
                        binding.datePicker.dayOfMonth,
                        binding.timePicker.hour,
                        binding.timePicker.minute,
                        0
                    )
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
                            if (it.isEmpty()) {
                                return@Observer
                            }
                            val workInfo = it[0]
                            if (!workInfo.state.isFinished) {
                                val dateFormat = SimpleDateFormat(datePattern, getDefault())
                                carViewModel.upsertReminder(
                                    Reminder(dateFormat.format(customCalendar.time).toString())
                                )

                                val notificationCreated = getString(R.string.notification_created)
                                binding.coordinatorLayout.snackBarBottom(
                                    notificationCreated, LENGTH_SHORT
                                ) {}
                            }
                        }
                    })
                } else {
                    val notificationError = getString(R.string.notification_error)
                    binding.coordinatorLayout.snackBarBottom(notificationError, LENGTH_SHORT) {}
                }
            } else {
                if (SDK_INT >= TIRAMISU) {
                    checkNotificationPermission.launch(POST_NOTIFICATIONS)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val datePattern = "dd.MM.yy \u00B7 HH:mm"
    }
}