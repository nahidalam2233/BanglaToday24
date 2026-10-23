package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.BookmarkItem
import com.example.data.model.NewsNotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("bangla_today_prefs", Context.MODE_PRIVATE)

    private val _bookmarks = MutableStateFlow<List<BookmarkItem>>(emptyList())
    val bookmarks: StateFlow<List<BookmarkItem>> = _bookmarks.asStateFlow()

    private val _notifications = MutableStateFlow<List<NewsNotificationItem>>(emptyList())
    val notifications: StateFlow<List<NewsNotificationItem>> = _notifications.asStateFlow()

    private val _fcmToken = MutableStateFlow<String>(prefs.getString(KEY_FCM_TOKEN, "") ?: "")
    val fcmToken: StateFlow<String> = _fcmToken.asStateFlow()

    private val _breakingNewsEnabled = MutableStateFlow(prefs.getBoolean(KEY_TOPIC_BREAKING, true))
    val breakingNewsEnabled: StateFlow<Boolean> = _breakingNewsEnabled.asStateFlow()

    private val _nationalNewsEnabled = MutableStateFlow(prefs.getBoolean(KEY_TOPIC_NATIONAL, true))
    val nationalNewsEnabled: StateFlow<Boolean> = _nationalNewsEnabled.asStateFlow()

    private val _sportsNewsEnabled = MutableStateFlow(prefs.getBoolean(KEY_TOPIC_SPORTS, true))
    val sportsNewsEnabled: StateFlow<Boolean> = _sportsNewsEnabled.asStateFlow()

    private val _allNewsEnabled = MutableStateFlow(prefs.getBoolean(KEY_TOPIC_ALL, true))
    val allNewsEnabled: StateFlow<Boolean> = _allNewsEnabled.asStateFlow()

    init {
        loadBookmarks()
        loadNotifications()
    }

    // --- Bookmarks ---
    private fun loadBookmarks() {
        val jsonStr = prefs.getString(KEY_BOOKMARKS, null) ?: return
        try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<BookmarkItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    BookmarkItem(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        title = obj.getString("title"),
                        url = obj.getString("url"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            _bookmarks.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addBookmark(title: String, url: String) {
        val current = _bookmarks.value.toMutableList()
        // Prevent duplicate URLs
        if (current.any { it.url == url }) return

        current.add(0, BookmarkItem(title = title, url = url))
        _bookmarks.value = current
        saveBookmarks(current)
    }

    fun removeBookmark(url: String) {
        val updated = _bookmarks.value.filter { it.url != url }
        _bookmarks.value = updated
        saveBookmarks(updated)
    }

    fun isBookmarked(url: String): Boolean {
        return _bookmarks.value.any { it.url == url }
    }

    private fun saveBookmarks(items: List<BookmarkItem>) {
        val arr = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("url", item.url)
                put("timestamp", item.timestamp)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_BOOKMARKS, arr.toString()).apply()
    }

    // --- Notifications History ---
    private fun loadNotifications() {
        val jsonStr = prefs.getString(KEY_NOTIFICATIONS, null) ?: return
        try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<NewsNotificationItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    NewsNotificationItem(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        body = obj.optString("body"),
                        url = if (obj.has("url")) obj.getString("url") else null,
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isRead = obj.optBoolean("isRead", false)
                    )
                )
            }
            _notifications.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addNotification(item: NewsNotificationItem) {
        val current = _notifications.value.toMutableList()
        current.add(0, item)
        // Keep max 50 recent notifications
        val trimmed = if (current.size > 50) current.subList(0, 50) else current
        _notifications.value = trimmed

        val arr = JSONArray()
        for (n in trimmed) {
            val obj = JSONObject().apply {
                put("id", n.id)
                put("title", n.title)
                put("body", n.body)
                n.url?.let { put("url", it) }
                put("timestamp", n.timestamp)
                put("isRead", n.isRead)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_NOTIFICATIONS, arr.toString()).apply()
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
        prefs.edit().remove(KEY_NOTIFICATIONS).apply()
    }

    // --- FCM Token ---
    fun saveFcmToken(token: String) {
        _fcmToken.value = token
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    // --- Topic Preferences ---
    fun setTopicBreaking(enabled: Boolean) {
        _breakingNewsEnabled.value = enabled
        prefs.edit().putBoolean(KEY_TOPIC_BREAKING, enabled).apply()
    }

    fun setTopicNational(enabled: Boolean) {
        _nationalNewsEnabled.value = enabled
        prefs.edit().putBoolean(KEY_TOPIC_NATIONAL, enabled).apply()
    }

    fun setTopicSports(enabled: Boolean) {
        _sportsNewsEnabled.value = enabled
        prefs.edit().putBoolean(KEY_TOPIC_SPORTS, enabled).apply()
    }

    fun setTopicAll(enabled: Boolean) {
        _allNewsEnabled.value = enabled
        prefs.edit().putBoolean(KEY_TOPIC_ALL, enabled).apply()
    }

    companion object {
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_TOPIC_BREAKING = "topic_breaking"
        private const val KEY_TOPIC_NATIONAL = "topic_national"
        private const val KEY_TOPIC_SPORTS = "topic_sports"
        private const val KEY_TOPIC_ALL = "topic_all"

        @Volatile
        private var INSTANCE: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
