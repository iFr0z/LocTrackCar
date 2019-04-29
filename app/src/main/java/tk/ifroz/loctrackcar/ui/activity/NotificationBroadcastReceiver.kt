package tk.ifroz.loctrackcar.ui.activity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color.RED
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O

class NotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val intentNotification = intent.getParcelableExtra<Notification>(NOTIFICATION)
        val intentId = intent.getIntExtra(NOTIFICATION_ID, 0)
        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (SDK_INT >= O) {
            val channel =
                NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, IMPORTANCE_HIGH)

            channel.enableLights(true)
            channel.lightColor = RED
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)

            manager.createNotificationChannel(channel)
        }
        manager.notify(intentId, intentNotification)
    }

    companion object {
        const val NOTIFICATION = "LocTrackCar_notification"
        const val NOTIFICATION_ID = "LocTrackCar_notification_id"
        const val NOTIFICATION_NAME = "LocTrackCar"
        const val NOTIFICATION_CHANNEL = "LocTrackCar_channel_01"
    }
}