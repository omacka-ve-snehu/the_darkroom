package cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import cz.cvut.fit.phamgiab.filmdevassistant.MainActivity
import cz.cvut.fit.phamgiab.filmdevassistant.R
import cz.cvut.fit.phamgiab.filmdevassistant.core.domain.toTimerString

class TimerNotificationHelper(
    private val context : Context
) {
    private val TIMER_CHANNEL_ID = context.packageName + "-timer"
    private val notificationId = 1
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createTimerChannel()
    }

    fun updateTimerNotification(
        stageName: String,
        remainingSeconds: Int
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        if (ActivityCompat.checkSelfPermission(
            context,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED)
            return

        val builder = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.timer)
            .setContentTitle(context.getString(R.string.development_in_progress))
            .setContentText("$stageName - ${remainingSeconds.toTimerString()}")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId, builder.build())
    }

    fun dismiss() {
        notificationManager.cancel(notificationId)
    }

    private fun createTimerChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.timer_channel_notification_name)
            val descriptionText = context.getString(R.string.timer_channel_notification_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(TIMER_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(false)
                setSound(null, null)
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}