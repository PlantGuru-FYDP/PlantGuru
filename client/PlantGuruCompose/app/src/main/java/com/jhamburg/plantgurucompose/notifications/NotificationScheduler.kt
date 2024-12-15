package com.jhamburg.plantgurucompose.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jhamburg.plantgurucompose.utils.PreferenceManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleNotification(
        title: String,
        message: String,
        delayInMinutes: Long,
        notificationId: Int = 0
    ) {
        if (!PreferenceManager.areNotificationsEnabled(context)) {
            return
        }

        val inputData = Data.Builder()
            .putString(NotificationWorker.KEY_TITLE, title)
            .putString(NotificationWorker.KEY_MESSAGE, message)
            .putInt(NotificationWorker.KEY_NOTIFICATION_ID, notificationId)
            .build()

        val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayInMinutes, TimeUnit.MINUTES)
            .setInputData(inputData)
            .build()

        workManager.enqueueUniqueWork(
            "notification_$notificationId",
            ExistingWorkPolicy.REPLACE,
            notificationWork
        )
    }

    fun cancelScheduledNotification(notificationId: Int) {
        workManager.cancelUniqueWork("notification_$notificationId")
    }
}