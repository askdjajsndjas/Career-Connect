package com.careerconnect

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ListenerRegistration

object StatusNotifier {

    private const val CHANNEL_ID = "application_status"
    private var registration: ListenerRegistration? = null
    private val lastStatus = mutableMapOf<String, String>()
    private var firstSnapshot = true

    fun start(activity: Activity) {
        createChannel(activity)
        askForPermission(activity)

        registration?.remove()
        lastStatus.clear()
        firstSnapshot = true
        val appContext = activity.applicationContext

        registration = ApplicationRepository.listenToMyApplications(onChange = { apps ->
            ApplicationRepository.archiveStale(apps)
            for (app in apps) {
                val before = lastStatus[app.id]
                // Only notify on real changes, not when the app first loads
                if (!firstSnapshot && before != null && before != app.status) {
                    showNotification(appContext, app)
                }
                lastStatus[app.id] = app.status
            }
            firstSnapshot = false
        })
    }

    fun stop() {
        registration?.remove()
        registration = null
    }

    private fun showNotification(context: Context, app: JobApplication) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val text = when (app.status) {
            ApplicationRepository.INTERVIEW -> "Good news! You've been invited to interview."
            ApplicationRepository.REJECTED -> "Unfortunately your application was not successful."
            ApplicationRepository.ARCHIVED -> "Archived: the employer did not respond in time."
            else -> "Status updated to ${app.status}"
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${app.jobTitle} at ${app.company}")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(app.id.hashCode(), notification)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Application status", NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun askForPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001
            )
        }
    }
}