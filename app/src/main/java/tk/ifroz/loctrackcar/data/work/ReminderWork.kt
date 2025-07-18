package tk.ifroz.loctrackcar.data.work

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color.RED
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION
import android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE
import android.media.RingtoneManager.TYPE_NOTIFICATION
import android.media.RingtoneManager.getDefaultUri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Build.VERSION_CODES.S
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_ALL
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.work.ListenableWorker.Result.success
import androidx.work.Worker
import androidx.work.WorkerParameters
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.ui.view.activity.MainActivity
import tk.ifroz.loctrackcar.util.extension.vectorDrawableToBitmap

class ReminderWork(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val id = inputData.getLong(NOTIFICATION_ID, 0).toInt()
        val addressName = inputData.getString(NOTIFICATION_ADDRESS)
        addressName?.let {
            showNotification(id, it)
        }

        return success()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun showNotification(id: Int, addressName: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
            putExtra(NOTIFICATION_ID, id)
        }

        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val bitmap =
            applicationContext.vectorDrawableToBitmap(R.drawable.ic_marker_with_outline_45dp)

        val titleNotification = applicationContext.getString(R.string.notification_title)
        val pendingIntent = if (SDK_INT >= S) {
            getActivity(applicationContext, 0, intent, FLAG_MUTABLE)
        } else {
            getActivity(applicationContext, 0, intent, FLAG_UPDATE_CURRENT)
        }
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setLargeIcon(bitmap).setSmallIcon(R.drawable.ic_marker_notification_white)
            .setContentTitle(titleNotification).setContentText(addressName)
            .setDefaults(DEFAULT_ALL).setContentIntent(pendingIntent).setAutoCancel(true)

        notification.priority = PRIORITY_MAX

        notification.setChannelId(NOTIFICATION_CHANNEL)

        val ringtoneManager = getDefaultUri(TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder().setUsage(USAGE_NOTIFICATION_RINGTONE)
            .setContentType(CONTENT_TYPE_SONIFICATION).build()

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL, NOTIFICATION_NAME, IMPORTANCE_HIGH
        ).apply {
            enableLights(true)
            lightColor = RED
            enableVibration(true)
            vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            setSound(ringtoneManager, audioAttributes)
        }
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(id, notification.build())
    }

    companion object {
        const val NOTIFICATION_ID = "LocTrackCar_notification_id"
        const val NOTIFICATION_NAME = "LocTrackCar"
        const val NOTIFICATION_CHANNEL = "LocTrackCar_channel_01"
        const val NOTIFICATION_WORK = "LocTrackCar_notification_work"
        const val NOTIFICATION_ADDRESS = "LocTrackCar_address"
    }
}