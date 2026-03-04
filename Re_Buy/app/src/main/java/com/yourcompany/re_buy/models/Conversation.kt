package com.yourcompany.re_buy.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Data model for conversation summary (used to list all conversations for a user)
 */
data class Conversation(
    @DocumentId
    val id: String = "", // Same as conversationId
    val participantUids: List<String> = emptyList(), // List of 2 UIDs
    val participantNames: Map<String, String> = emptyMap(), // Map of UID to Name
    val lastMessage: String = "",
    val lastMessageSenderUid: String = "",
    @ServerTimestamp
    val lastMessageTime: Date? = null,
    val unreadCount: Map<String, Int> = emptyMap() // Map of UID to unread count for that user
) {
    // No-argument constructor required for Firestore
    constructor() : this("", emptyList(), emptyMap(), "", "", null, emptyMap())

    /**
     * Get the other participant's UID (the person you're chatting with)
     */
    fun getOtherParticipantUid(currentUserUid: String): String? {
        return participantUids.firstOrNull { it != currentUserUid }
    }

    /**
     * Get the other participant's name
     */
    fun getOtherParticipantName(currentUserUid: String): String {
        val otherUid = getOtherParticipantUid(currentUserUid)
        return participantNames[otherUid] ?: "Unknown User"
    }

    /**
     * Get unread count for specific user
     */
    fun getUnreadCountForUser(userUid: String): Int {
        return unreadCount[userUid] ?: 0
    }
}
