package com.jhamburg.plantgurucompose.notifications

import android.app.PendingIntent
import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jhamburg.plantgurucompose.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlantGuruFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var fcmTokenManager: FCMTokenManager

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notificationId = System.currentTimeMillis().toInt()

        // Handle the notification based on type
        when (message.data["type"]) {
            "WATERING_EVENT" -> {
                val plantId = message.data["plantId"]
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("navigation", "plantDetail/$plantId")
                }

                val pendingIntent = PendingIntent.getActivity(
                    this,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )

                notificationManager.showNotification(
                    title = message.notification?.title ?: "Plant Guru",
                    message = message.notification?.body ?: "",
                    notificationId = notificationId,
                    pendingIntent = pendingIntent
                )
            }

            else -> {
                // Handle other notification types or default case
                message.notification?.let { notification ->
                    notificationManager.showNotification(
                        title = notification.title ?: "Plant Guru",
                        message = notification.body ?: "",
                        notificationId = notificationId
                    )
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val success = fcmTokenManager.registerTokenWithBackend(token)
            if (success) {
                println("New FCM Token registered successfully: $token")
            } else {
                println("Failed to register new FCM token")
            }
        }
    }
} 