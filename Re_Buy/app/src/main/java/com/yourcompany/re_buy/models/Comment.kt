package com.yourcompany.re_buy.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Data model for comments on community posts
 */
data class Comment(
    @DocumentId
    val id: String = "",
    val postId: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorEmail: String = "",
    val content: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null,
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList()
) {
    // No-argument constructor required for Firestore
    constructor() : this("", "", "", "", "", "", null, null, 0, emptyList())

    fun isLikedByUser(userId: String): Boolean {
        return likedBy.contains(userId)
    }
}
