package com.example.data.model

data class BookmarkItem(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class NewsNotificationItem(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val body: String,
    val url: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class NewsCategory(
    val id: String,
    val nameBn: String,
    val url: String
)
