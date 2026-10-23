package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.BookmarkItem
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class BookmarkRepository(context: Context) {
    private val db = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    )
    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeBookmarks(): Flow<List<BookmarkItem>> = flow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            emit(emptyList())
            return@flow
        }
        val ref = db.collection("users").document(uid).collection("bookmarks")
        emitAll(
            ref.snapshots().map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val url = doc.getString("url") ?: return@mapNotNull null
                    val time = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                    BookmarkItem(
                        id = doc.id,
                        title = title,
                        url = url,
                        timestamp = time
                    )
                }
            }
        )
    }

    suspend fun addBookmark(title: String, url: String) {
        val uid = requireUserId()
        val payload = mapOf(
            "userId" to uid,
            "title" to title,
            "url" to url,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(uid).collection("bookmarks").add(payload).await()
    }

    suspend fun removeBookmark(bookmarkId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("bookmarks").document(bookmarkId).delete().await()
    }
}
