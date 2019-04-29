package tk.ifroz.loctrackcar.ui.activity

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.Notification
import android.app.PendingIntent.getActivity
import android.app.PendingIntent.getBroadcast
import android.content.Intent
import android.media.RingtoneManager.TYPE_NOTIFICATION
import android.media.RingtoneManager.getDefaultUri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Bundle
import androidx.core.app.NotificationCompat.Builder
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.ViewModelProviders.of
import com.klinker.android.sliding.SlidingActivity
import kotlinx.android.synthetic.main.activity_notification.*
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.db.entity.Reminder
import tk.ifroz.loctrackcar.ui.extension.vectorDrawableToBitmap
import tk.ifroz.loctrackcar.viewmodel.MarkerCarViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.getDefault

class NotificationActivity : SlidingActivity() {

    private lateinit var markerCarViewModel: MarkerCarViewModel

    override fun init(savedInstanceState: Bundle?) {
        setContent(R.layout.activity_notification)

        userInterface()
    }

    private fun userInterface() {
        val titleNotification = getString(R.string.notification_title)
        title = titleNotification

        val colorPrimary = getColor(this, R.color.colorPrimary)
        val colorPrimaryDark = getColor(this, R.color.colorPrimaryDark)
        setPrimaryColors(colorPrimary, colorPrimaryDark)

        enableFullscreen()

        setFab(colorPrimaryDark, R.drawable.ic_done_white_24dp) {
            val calendar = Calendar.getInstance()
            calendar.set(
                date_p.year, date_p.month, date_p.dayOfMonth, time_p.hour, time_p.minute, 0
            )

            val currentCalendar = Calendar.getInstance()
            if (calendar >= currentCalendar) {
                val bitmap = this.vectorDrawableToBitmap(R.drawable.ic_marker_black_24dp)
                val subtitleNotification = getString(R.string.notification_subtitle)
                val intent = Intent(this, NotificationBroadcastReceiver::class.java)
                    .putExtra("title", titleNotification).putExtra("subtitle", subtitleNotification)

                val pendingIntent = getActivity(this, 0, intent, 0)
                val ringtoneManager = getDefaultUri(TYPE_NOTIFICATION)
                val builder = Builder(this, NotificationBroadcastReceiver.NOTIFICATION_CHANNEL)
                    .setLargeIcon(bitmap).setSmallIcon(R.drawable.ic_marker_notification_white)
                    .setContentTitle(titleNotification).setContentText(subtitleNotification)
                    .setContentIntent(pendingIntent).setSound(ringtoneManager)
                    .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
                    .setAutoCancel(true)

                if (SDK_INT >= O) {
                    builder.setChannelId(NotificationBroadcastReceiver.NOTIFICATION_CHANNEL)
                }
                scheduleNotification(builder.build(), calendar)

                finish()
            }
        }
    }

    private fun scheduleNotification(notification: Notification, calendar: Calendar) {
        markerCarViewModel = of(this).get(MarkerCarViewModel::class.java)
        markerCarViewModel.insertReminder(
            Reminder(
                SimpleDateFormat(
                    "dd.MM.yy \u00B7 HH:mm", getDefault()
                ).format(calendar.time).toString()
            )
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationBroadcastReceiver::class.java)
            .putExtra(NotificationBroadcastReceiver.NOTIFICATION_ID, 1)
            .putExtra(NotificationBroadcastReceiver.NOTIFICATION, notification)

        val pendingIntent = getBroadcast(this, 0, intent, 0)
        alarmManager.setExact(RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}