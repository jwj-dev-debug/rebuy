package com.yourcompany.re_buy.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.yourcompany.re_buy.models.Comment
import com.yourcompany.re_buy.models.CommunityPost
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.Date

/**
 * Repository for managing community posts and comments in Firestore
 */
class CommunityRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val postsCollection = db.collection("posts")
    private val commentsCollection = db.collection("comments")

    /**
     * Get all posts ordered by creation date (newest first)
     */
    suspend fun getAllPosts(): Result<List<CommunityPost>> {
        return try {
            val snapshot = postsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val posts = snapshot.toObjects(CommunityPost::class.java)
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get posts filtered by region
     */
    suspend fun getPostsByRegion(region: String): Result<List<CommunityPost>> {
        return try {
            val snapshot = if (region == "all") {
                postsCollection
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
            } else {
                postsCollection
                    .whereEqualTo("region", region)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
            }

            val posts = snapshot.toObjects(CommunityPost::class.java)
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get a single post by ID
     */
    suspend fun getPostById(postId: String): Result<CommunityPost> {
        return try {
            val snapshot = postsCollection.document(postId).get().await()
            val post = snapshot.toObject(CommunityPost::class.java)
            if (post != null) {
                Result.success(post)
            } else {
                Result.failure(Exception("Post not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a new post
     */
    suspend fun createPost(post: CommunityPost): Result<String> {
        return try {
            val docRef = postsCollection.add(post).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing post
     */
    suspend fun updatePost(postId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            // Verify the user owns this post
            val post = getPostById(postId).getOrNull()
            if (post == null || post.authorUid != currentUser.uid) {
                return Result.failure(Exception("Unauthorized"))
            }

            postsCollection.document(postId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a post
     */
    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            // Verify the user owns this post
            val post = getPostById(postId).getOrNull()
            if (post == null || post.authorUid != currentUser.uid) {
                return Result.failure(Exception("Unauthorized"))
            }

            // Delete all comments for this post
            val comments = commentsCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            val batch = db.batch()
            comments.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            // Delete the post
            batch.delete(postsCollection.document(postId))
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload image to Firebase Storage and return the download URL
     */
    suspend fun uploadImage(imageUri: Uri): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val filename = "posts/${currentUser.uid}/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(filename)

            val uploadTask = storageRef.putFile(imageUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get comments for a specific post
     */
    suspend fun getCommentsForPost(postId: String): Result<List<Comment>> {
        return try {
            android.util.Log.d("CommunityRepo", "Loading comments for post: $postId")
            val snapshot = commentsCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            android.util.Log.d("CommunityRepo", "Found ${snapshot.size()} comment documents")
            val comments = snapshot.toObjects(Comment::class.java)
                .sortedBy { it.createdAt ?: Date(0) } // Sort in app to avoid needing composite index
            android.util.Log.d("CommunityRepo", "Parsed ${comments.size} comments successfully")
            Result.success(comments)
        } catch (e: Exception) {
            android.util.Log.e("CommunityRepo", "Failed to load comments for post $postId", e)
            Result.failure(e)
        }
    }

    /**
     * Add a comment to a post
     */
    suspend fun addComment(comment: Comment): Result<String> {
        return try {
            android.util.Log.d("CommunityRepo", "Adding comment to post ${comment.postId}: ${comment.content}")
            val docRef = commentsCollection.add(comment).await()
            android.util.Log.d("CommunityRepo", "Comment added successfully with ID: ${docRef.id}")

            // Update comment count on the post
            val postRef = postsCollection.document(comment.postId)
            db.runTransaction { transaction ->
                val post = transaction.get(postRef).toObject(CommunityPost::class.java)
                val newCount = (post?.commentCount ?: 0) + 1
                transaction.update(postRef, "commentCount", newCount)
            }.await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            android.util.Log.e("CommunityRepo", "Failed to add comment", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a comment
     */
    suspend fun deleteComment(commentId: String, postId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            // Verify the user owns this comment
            val commentDoc = commentsCollection.document(commentId).get().await()
            val comment = commentDoc.toObject(Comment::class.java)
            if (comment == null || comment.authorUid != currentUser.uid) {
                return Result.failure(Exception("Unauthorized"))
            }

            // Delete comment and update count
            commentsCollection.document(commentId).delete().await()

            // Update comment count on the post
            val postRef = postsCollection.document(postId)
            db.runTransaction { transaction ->
                val post = transaction.get(postRef).toObject(CommunityPost::class.java)
                val newCount = maxOf(0, (post?.commentCount ?: 0) - 1)
                transaction.update(postRef, "commentCount", newCount)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggle like on a post
     */
    suspend fun toggleLikePost(postId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val postRef = postsCollection.document(postId)
            db.runTransaction { transaction ->
                val post = transaction.get(postRef).toObject(CommunityPost::class.java)
                    ?: throw Exception("Post not found")

                val likedBy = post.likedBy.toMutableList()
                val newLikeCount = if (likedBy.contains(currentUser.uid)) {
                    // Unlike
                    likedBy.remove(currentUser.uid)
                    maxOf(0, post.likeCount - 1)
                } else {
                    // Like
                    likedBy.add(currentUser.uid)
                    post.likeCount + 1
                }

                transaction.update(postRef, mapOf(
                    "likedBy" to likedBy,
                    "likeCount" to newLikeCount
                ))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggle favorite on a post
     */
    suspend fun toggleFavoritePost(post: CommunityPost): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                android.util.Log.e("CommunityRepo", "Cannot toggle favorite - user not logged in")
                return Result.failure(Exception("User not authenticated"))
            }

            android.util.Log.d("CommunityRepo", "Toggling favorite for post: ${post.title} (${post.id}) for user: ${currentUser.uid}")

            val favoritesCollection = db.collection("favorites")
            val querySnapshot = favoritesCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("itemId", post.id)
                .whereEqualTo("itemType", "post")
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                // Add to favorites
                android.util.Log.d("CommunityRepo", "Adding post to favorites")
                val favorite = com.yourcompany.re_buy.models.Favorite(
                    userId = currentUser.uid,
                    itemId = post.id,
                    itemType = "post",
                    itemTitle = post.title,
                    itemImage = post.imageUrls.firstOrNull() ?: ""
                )
                favoritesCollection.add(favorite).await()
                android.util.Log.d("CommunityRepo", "Successfully added post to favorites")
            } else {
                // Remove from favorites
                android.util.Log.d("CommunityRepo", "Removing post from favorites (${querySnapshot.size()} documents)")
                querySnapshot.documents.first().reference.delete().await()
                android.util.Log.d("CommunityRepo", "Successfully removed post from favorites")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("CommunityRepo", "Error toggling post favorite: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Check if a post is favorited by current user
     */
    suspend fun isPostFavorited(postId: String): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                android.util.Log.d("CommunityRepo", "User not logged in, returning false for isPostFavorited")
                return Result.success(false)
            }

            android.util.Log.d("CommunityRepo", "Checking if post is favorited: $postId for user: ${currentUser.uid}")

            val querySnapshot = db.collection("favorites")
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("itemId", postId)
                .whereEqualTo("itemType", "post")
                .get()
                .await()

            val isFavorited = !querySnapshot.isEmpty
            android.util.Log.d("CommunityRepo", "Post favorited status: $isFavorited")
            Result.success(isFavorited)
        } catch (e: Exception) {
            android.util.Log.e("CommunityRepo", "Error checking post favorite status: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get all favorited posts for current user
     */
    suspend fun getFavoritedPosts(): Result<List<CommunityPost>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                android.util.Log.e("CommunityRepo", "Cannot get favorited posts - user not logged in")
                return Result.failure(Exception("User not authenticated"))
            }

            android.util.Log.d("CommunityRepo", "Getting favorited posts for user: ${currentUser.uid}")

            // Get favorite post IDs
            val favoritesSnapshot = db.collection("favorites")
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("itemType", "post")
                .get()
                .await()

            val postIds = favoritesSnapshot.documents.mapNotNull { it.getString("itemId") }
            android.util.Log.d("CommunityRepo", "Found ${postIds.size} favorited post IDs")

            if (postIds.isEmpty()) {
                return Result.success(emptyList())
            }

            // Get the actual posts
            val posts = mutableListOf<CommunityPost>()
            for (postId in postIds) {
                val postDoc = postsCollection.document(postId).get().await()
                postDoc.toObject(CommunityPost::class.java)?.let { posts.add(it) }
            }

            android.util.Log.d("CommunityRepo", "Successfully retrieved ${posts.size} favorited posts")
            Result.success(posts)
        } catch (e: Exception) {
            android.util.Log.e("CommunityRepo", "Error getting favorited posts: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get all posts created by current user
     */
    suspend fun getMyPosts(): Result<List<CommunityPost>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                android.util.Log.e("CommunityRepo", "Cannot get my posts - user not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }

            android.util.Log.d("CommunityRepo", "Getting posts for user: ${currentUser.uid}")

            val snapshot = postsCollection
                .whereEqualTo("authorUid", currentUser.uid)
                .get()
                .await()

            android.util.Log.d("CommunityRepo", "Found ${snapshot.size()} posts by user")

            // Sort in memory to avoid index requirement
            val posts = snapshot.toObjects(CommunityPost::class.java)
                .sortedByDescending { it.createdAt }

            android.util.Log.d("CommunityRepo", "Successfully retrieved ${posts.size} posts by current user")
            Result.success(posts)
        } catch (e: Exception) {
            android.util.Log.e("CommunityRepo", "Error getting my posts: ${e.message}", e)
            Result.failure(e)
        }
    }
}
