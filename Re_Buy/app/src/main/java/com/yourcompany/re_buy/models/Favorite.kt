package com.yourcompany.re_buy.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Data model for user favorites (posts and products)
 */
data class Favorite(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val itemId: String = "", // Post ID or Product link
    val itemType: String = "", // "post" or "product"
    val itemTitle: String = "", // Cached title for display
    val itemImage: String = "", // Cached image URL
    @ServerTimestamp
    val createdAt: Date? = null
) {
    // No-argument constructor required for Firestore
    constructor() : this("", "", "", "", "", "", null)
}
