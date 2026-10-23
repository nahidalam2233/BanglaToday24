package com.example.fcm

import android.util.Log
import com.example.data.preferences.AppPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class BanglaTodayMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token received: $token")
        AppPreferences.getInstance(this).saveFcmToken(token)

        // Automatically subscribe to primary news topics
        NotificationHelper.subscribeTopic("news")
        NotificationHelper.subscribeTopic("breaking")
        NotificationHelper.subscribeTopic("bangladesh")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // 1. Check if message contains a notification payload
        var title = remoteMessage.notification?.title
        var body = remoteMessage.notification?.body

        // 2. Check if message contains data payload
        val data = remoteMessage.data
        if (title.isNullOrBlank() && data.containsKey("title")) {
            title = data["title"]
        }
        if (body.isNullOrBlank() && data.containsKey("body")) {
            body = data["body"]
        }

        // 3. Fallbacks if completely empty
        if (title.isNullOrBlank()) {
            title = "Bangla Today - তাজা খবর"
        }
        if (body.isNullOrBlank()) {
            body = "নতুন সংবাদ প্রকাশিত হয়েছে। বিস্তারিত পড়তে ক্লিক করুন।"
        }

        // 4. Extract target URL from data payload (e.g. "url" or "link")
        val targetUrl = data["url"] ?: data["link"] ?: remoteMessage.notification?.clickAction

        NotificationHelper.showNotification(
            context = this,
            title = title,
            body = body,
            url = targetUrl
        )
    }

    companion object {
        private const val TAG = "BanglaTodayFCM"
    }
}
