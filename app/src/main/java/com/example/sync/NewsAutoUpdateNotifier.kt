package com.example.sync

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.util.Log
import com.example.data.preferences.AppPreferences
import com.example.fcm.NotificationHelper
import com.example.receiver.NewsCheckReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

object NewsAutoUpdateNotifier {
    private const val TAG = "NewsAutoUpdate"
    private const val WP_POSTS_API = "https://banglatoday24.xyz/wp-json/wp/v2/posts?per_page=5"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Checks for newly published posts from the banglatoday24.xyz WordPress API.
     * If a new post is detected, triggers an Android system notification.
     */
    fun checkForNewPosts(context: Context, onComplete: ((newPostsFound: Int) -> Unit)? = null) {
        val prefs = AppPreferences.getInstance(context)
        if (!prefs.autoNotifyEnabled.value) {
            onComplete?.invoke(0)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url(WP_POSTS_API)
                    .header("User-Agent", "BanglaToday24-Android-App/1.0.3")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w(TAG, "Failed to fetch WP posts: HTTP ${response.code}")
                        onComplete?.invoke(0)
                        return@launch
                    }

                    val bodyStr = response.body?.string() ?: ""
                    if (bodyStr.isBlank() || !bodyStr.trim().startsWith("[")) {
                        onComplete?.invoke(0)
                        return@launch
                    }

                    val postsArray = JSONArray(bodyStr)
                    if (postsArray.length() == 0) {
                        onComplete?.invoke(0)
                        return@launch
                    }

                    val lastSeenId = prefs.getLastSeenPostId()
                    val firstPost = postsArray.getJSONObject(0)
                    val newestId = firstPost.optLong("id", 0L)

                    // First run initialization: save latest post ID without spamming
                    if (lastSeenId == 0L) {
                        if (newestId > 0) {
                            prefs.saveLastSeenPostId(newestId)
                            Log.d(TAG, "Initialized lastSeenPostId to $newestId")
                        }
                        onComplete?.invoke(0)
                        return@launch
                    }

                    var newPostsCount = 0
                    // Check if new post published (newestId > lastSeenId)
                    if (newestId > lastSeenId) {
                        // Notify for the latest new post
                        val rawTitle = firstPost.optJSONObject("title")?.optString("rendered", "") ?: ""
                        val cleanTitle = cleanHtmlTitle(rawTitle)
                        val postLink = firstPost.optString("link", "https://banglatoday24.xyz")

                        if (cleanTitle.isNotBlank()) {
                            NotificationHelper.showNotification(
                                context = context,
                                title = cleanTitle,
                                body = "বাংলা টুডে ২৪-এ নতুন সংবাদ প্রকাশিত হয়েছে। বিস্তারিত জানতে ট্যাপ করুন।",
                                url = postLink
                            )
                            newPostsCount++
                            Log.d(TAG, "Notified new post: $cleanTitle ($newestId)")
                        }

                        // Update stored ID to the newest
                        prefs.saveLastSeenPostId(newestId)
                    }

                    onComplete?.invoke(newPostsCount)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking for new posts: ${e.message}", e)
                onComplete?.invoke(0)
            }
        }
    }

    /**
     * Schedules periodic background checks (every 15-20 minutes) via AlarmManager.
     */
    fun schedulePeriodicCheck(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, NewsCheckReceiver::class.java).apply {
                action = NewsCheckReceiver.ACTION_CHECK_NEWS
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Trigger every 15 minutes
            val interval = 15 * 60 * 1000L
            val triggerTime = System.currentTimeMillis() + interval

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    interval,
                    pendingIntent
                )
            }
            Log.d(TAG, "Periodic news check scheduled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule news check alarm: ${e.message}", e)
        }
    }

    private fun cleanHtmlTitle(raw: String): String {
        return try {
            val decoded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(raw, Html.FROM_HTML_MODE_LEGACY).toString()
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(raw).toString()
            }
            decoded.replace("\n", " ").trim()
        } catch (e: Exception) {
            raw
        }
    }
}
