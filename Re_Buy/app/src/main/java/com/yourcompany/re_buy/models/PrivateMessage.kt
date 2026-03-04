package com.yourcompany.re_buy.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Data model for private messages between users
 */
data class PrivateMessage(
    @DocumentId
    val id: String = "",
    val conversationId: String = "", // Unique ID for conversation between two users
    val senderUid: String = "",
    val senderName: String = "",
    val receiverUid: String = "",
    val receiverName: String = "",
    val message: String = "",
    val imageUrl: String? = null, // Optional image in message
    @ServerTimestamp
    val timestamp: Date? = null,
    val isRead: Boolean = false,
    val isDeleted: Boolean = false
) {
    // No-argument constructor required for Firestore
    constructor() : this("", "", "", "", "", "", "", null, null, false, false)

    companion object {
        /**
         * Generate a unique conversation ID for two users
         * Always generates the same ID regardless of sender/receiver order
         */
        fun generateConversationId(uid1: String, uid2: String): String {
            return if (uid1 < uid2) {
                "${uid1}_${uid2}"
            } else {
                "${uid2}_${uid1}"
            }
        }
    }
}
