package pl.mora.app.integration

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {
    private const val CHANNEL_ID = "mora_general"

    fun showTestNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Powiadomienia Mory",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Powiadomienia i przypomnienia asystenta Mora"
                }
            )
        }
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }.setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Mora")
            .setContentText("Kanał powiadomień działa poprawnie.")
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
    }
}
