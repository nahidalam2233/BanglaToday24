package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sync.NewsAutoUpdateNotifier

class NewsCheckReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(TAG, "NewsCheckReceiver triggered: ${intent?.action}")

        val pendingResult = goAsync()

        // 1. Check for newly published news
        NewsAutoUpdateNotifier.checkForNewPosts(context) { count ->
            Log.d(TAG, "Background check completed. New posts found: $count")

            // 2. Schedule next periodic check
            NewsAutoUpdateNotifier.schedulePeriodicCheck(context)

            pendingResult.finish()
        }
    }

    companion object {
        const val ACTION_CHECK_NEWS = "com.example.ACTION_CHECK_NEWS"
        private const val TAG = "NewsCheckReceiver"
    }
}
