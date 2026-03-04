package com.yourcompany.re_buy.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.yourcompany.re_buy.models.Conversation
import com.yourcompany.re_buy.models.PrivateMessage
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Repository for managing private messages between users
 */
class MessagingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val messagesCollection = db.collection("messages")
    private val conversationsCollection = db.collection("conversations")
    private val usersCollection = db.collection("users")

    /**
     * Get all conversations for the current user
     */
    suspend fun getConversationsForUser(): Result<List<Conversation>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val snapshot = conversationsCollection
                .whereArrayContains("participantUids", currentUser.uid)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .get()
                .await()

            val conversations = snapshot.toObjects(Conversation::class.java)
            Result.success(conversations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get messages for a specific conversation
     */
    suspend fun getMessagesForConversation(conversationId: String): Result<List<PrivateMessage>> {
        return try {
            val snapshot = messagesCollection
                .whereEqualTo("conversationId", conversationId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            val messages = snapshot.toObjects(PrivateMessage::class.java)
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send a message to another user
     */
    suspend fun sendMessage(
        receiverUid: String,
        receiverName: String,
        messageText: String,
        imageUri: Uri? = null
    ): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            // Get current user's name from Firestore
            val userDoc = usersCollection.document(currentUser.uid).get().await()
            val senderName = userDoc.getString("name") ?: currentUser.email ?: "Unknown"

            val conversationId = PrivateMessage.generateConversationId(currentUser.uid, receiverUid)

            // Upload image if provided
            val imageUrl = if (imageUri != null) {
                uploadMessageImage(imageUri).getOrNull()
            } else null

            // Create message
            val message = PrivateMessage(
                conversationId = conversationId,
                senderUid = currentUser.uid,
                senderName = senderName,
                receiverUid = receiverUid,
                receiverName = receiverName,
                message = messageText,
                imageUrl = imageUrl
            )

            // Add message to Firestore
            val messageDoc = messagesCollection.add(message).await()

            // Update or create conversation
            updateConversation(
                conversationId = conversationId,
                senderUid = currentUser.uid,
                senderName = senderName,
                receiverUid = receiverUid,
                receiverName = receiverName,
                lastMessage = if (imageUrl != null) "📷 Photo" else messageText
            )

            Result.success(messageDoc.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update conversation metadata (last message, timestamp, unread count)
     */
    private suspend fun updateConversation(
        conversationId: String,
        senderUid: String,
        senderName: String,
        receiverUid: String,
        receiverName: String,
        lastMessage: String
    ) {
        try {
            val conversationRef = conversationsCollection.document(conversationId)
            val conversationDoc = conversationRef.get().await()

            if (conversationDoc.exists()) {
                // Update existing conversation
                val conversation = conversationDoc.toObject(Conversation::class.java)
                val unreadCount = conversation?.unreadCount?.toMutableMap() ?: mutableMapOf()

                // Increment unread count for receiver
                val receiverUnread = (unreadCount[receiverUid] ?: 0) + 1
                unreadCount[receiverUid] = receiverUnread

                conversationRef.update(mapOf(
                    "lastMessage" to lastMessage,
                    "lastMessageSenderUid" to senderUid,
                    "lastMessageTime" to com.google.firebase.Timestamp.now(),
                    "unreadCount" to unreadCount
                )).await()
            } else {
                // Create new conversation
                val conversation = Conversation(
                    id = conversationId,
                    participantUids = listOf(senderUid, receiverUid),
                    participantNames = mapOf(
                        senderUid to senderName,
                        receiverUid to receiverName
                    ),
                    lastMessage = lastMessage,
                    lastMessageSenderUid = senderUid,
                    unreadCount = mapOf(
                        senderUid to 0,
                        receiverUid to 1
                    )
                )
                conversationRef.set(conversation).await()
            }
        } catch (e: Exception) {
            // Log error but don't fail the message send
            e.printStackTrace()
        }
    }

    /**
     * Mark messages as read in a conversation
     */
    suspend fun markMessagesAsRead(conversationId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            // Update unread messages
            val snapshot = messagesCollection
                .whereEqualTo("conversationId", conversationId)
                .whereEqualTo("receiverUid", currentUser.uid)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()

            // Update conversation unread count
            val conversationRef = conversationsCollection.document(conversationId)
            db.runTransaction { transaction ->
                val conversation = transaction.get(conversationRef).toObject(Conversation::class.java)
                val unreadCount = conversation?.unreadCount?.toMutableMap() ?: mutableMapOf()
                unreadCount[currentUser.uid] = 0
                transaction.update(conversationRef, "unreadCount", unreadCount)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload image for a message
     */
    private suspend fun uploadMessageImage(imageUri: Uri): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val filename = "messages/${currentUser.uid}/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(filename)

            val uploadTask = storageRef.putFile(imageUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a message
     */
    suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            messagesCollection.document(messageId).update("isDeleted", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get total unread message count for current user
     */
    suspend fun getUnreadMessageCount(): Result<Int> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val snapshot = conversationsCollection
                .whereArrayContains("participantUids", currentUser.uid)
                .get()
                .await()

            val conversations = snapshot.toObjects(Conversation::class.java)
            val totalUnread = conversations.sumOf { it.getUnreadCountForUser(currentUser.uid) }

            Result.success(totalUnread)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
