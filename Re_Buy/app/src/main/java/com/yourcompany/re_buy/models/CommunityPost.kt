package com.yourcompany.re_buy.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Data model for community posts
 */
data class CommunityPost(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorEmail: String = "",
    val region: String = "", // "seodaemun" or "dongdaemun" or "all"
    val imageUrls: List<String> = emptyList(), // URLs of uploaded images
    val productTitle: String = "", // Optional: related product title
    val productLink: String = "", // Optional: link to product
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null,
    val commentCount: Int = 0,
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList() // List of user UIDs who liked this post
) {
    // No-argument constructor required for Firestore
    constructor() : this("", "", "", "", "", "", "", emptyList(), "", "", null, null, 0, 0, emptyList())

    fun isLikedByUser(userId: String): Boolean {
        return likedBy.contains(userId)
    }

    fun getRegionDisplayName(): String {
        return when (region) {
            "seodaemun" -> "서대문구"
            "dongdaemun" -> "동대문구"
            else -> "전체"
        }
    }
}
