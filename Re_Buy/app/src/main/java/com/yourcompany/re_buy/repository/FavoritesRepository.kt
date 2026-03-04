package com.yourcompany.re_buy.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yourcompany.re_buy.Product
import com.yourcompany.re_buy.models.CommunityPost
import com.yourcompany.re_buy.models.Favorite
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing user favorites (both products and posts)
 */
class FavoritesRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val favoritesCollection = db.collection("favorites")
    private val postsCollection = db.collection("posts")

    /**
     * Check if a product is favorited by current user
     */
    suspend fun isProductFavorited(productLink: String): Result<Boolean> {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            android.util.Log.e("FavoritesRepo", "User not logged in")
            return Result.failure(Exception("Not logged in"))
        }

        return try {
            android.util.Log.d("FavoritesRepo", "Checking if product is favorited: $productLink for user: $userId")
            val snapshot = favoritesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("itemId", productLink)
                .whereEqualTo("itemType", "product")
                .get()
                .await()

            val isFavorited = !snapshot.isEmpty
            android.util.Log.d("FavoritesRepo", "Product favorited status: $isFavorited")
            Result.success(isFavorited)
        } catch (e: Exception) {
            android.util.Log.e("FavoritesRepo", "Error checking favorite status: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Check if a post is favorited by current user
     */
    suspend fun isPostFavorited(postId: String): Result<Boolean> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))

        return try {
            val snapshot = favoritesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("itemId", postId)
                .whereEqualTo("itemType", "post")
                .get()
                .await()

            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Add a product to favorites
     */
    suspend fun addProductToFavorites(product: Product): Result<Unit> {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            android.util.Log.e("FavoritesRepo", "Cannot add favorite - user not logged in")
            return Result.failure(Exception("Not logged in"))
        }

        return try {
            android.util.Log.d("FavoritesRepo", "Adding product to favorites: ${product.title} for user: $userId")
            val favorite = Favorite(
                userId = userId,
                itemId = product.link,
                itemType = "product",
                itemTitle = product.title,
                itemImage = product.image
            )

            favoritesCollection.add(favorite).await()
            android.util.Log.d("FavoritesRepo", "Successfully added product to favorites")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FavoritesRepo", "Error adding product to favorites: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Remove a product from favorites
     */
    suspend fun removeProductFromFavorites(productLink: String): Result<Unit> {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            android.util.Log.e("FavoritesRepo", "Cannot remove favorite - user not logged in")
            return Result.failure(Exception("Not logged in"))
        }

        return try {
            android.util.Log.d("FavoritesRepo", "Removing product from favorites: $productLink for user: $userId")
            val snapshot = favoritesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("itemId", productLink)
                .whereEqualTo("itemType", "product")
                .get()
                .await()

            android.util.Log.d("FavoritesRepo", "Found ${snapshot.size()} favorites to remove")
            for (document in snapshot.documents) {
                document.reference.delete().await()
            }

            android.util.Log.d("FavoritesRepo", "Successfully removed product from favorites")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FavoritesRepo", "Error removing product from favorites: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Toggle product favorite status
     */
    suspend fun toggleProductFavorite(product: Product): Result<Boolean> {
        val isFavorited = isProductFavorited(product.link).getOrElse { return Result.failure(it) }

        return if (isFavorited) {
            removeProductFromFavorites(product.link).map { false }
        } else {
            addProductToFavorites(product).map { true }
        }
    }

    /**
     * Add a post to favorites
     */
    suspend fun addPostToFavorites(post: CommunityPost): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))

        return try {
            val favorite = Favorite(
                userId = userId,
                itemId = post.id,
                itemType = "post",
                itemTitle = post.title,
                itemImage = post.imageUrls.firstOrNull() ?: ""
            )

            favoritesCollection.add(favorite).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Remove a post from favorites
     */
    suspend fun removePostFromFavorites(postId: String): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))

        return try {
            val snapshot = favoritesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("itemId", postId)
                .whereEqualTo("itemType", "post")
                .get()
                .await()

            for (document in snapshot.documents) {
                document.reference.delete().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggle post favorite status
     */
    suspend fun togglePostFavorite(post: CommunityPost): Result<Boolean> {
        val isFavorited = isPostFavorited(post.id).getOrElse { return Result.failure(it) }

        return if (isFavorited) {
            removePostFromFavorites(post.id).map { false }
        } else {
            addPostToFavorites(post).map { true }
        }
    }

    /**
     * Get all favorited products for current user
     */
    suspend fun getFavoritedProducts(): Result<List<Favorite>> {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            android.util.Log.e("FavoritesRepo", "Cannot get favorited products - user not logged in")
            return Result.failure(Exception("Not logged in"))
        }

        return try {
            android.util.Log.d("FavoritesRepo", "Getting favorited products for user: $userId")

            val snapshot = favoritesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("itemType", "product")
                .get()
                .await()

            android.util.Log.d("FavoritesRepo", "Found ${snapshot.size()} favorite documents")
            val favorites = snapshot.toObjects(Favorite::class.java)
                .sortedByDescending { it.createdAt } // Sort in memory instead of Firestore

            android.util.Log.d("FavoritesRepo", "Successfully retrieved ${favorites.size} favorited products")
            Result.success(favorites)
        } catch (e: Exception) {
            android.util.Log.e("FavoritesRepo", "Error getting favorited products: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get all favorited posts for current user
     */
    suspend fun getFavoritedPosts(): Result<List<CommunityPost>> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))

        return try {
            // Get favorite post IDs
            val favSnapshot = favoritesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("itemType", "post")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val postIds = favSnapshot.documents.map { it.getString("itemId") ?: "" }.filter { it.isNotEmpty() }

            if (postIds.isEmpty()) {
                return Result.success(emptyList())
            }

            // Fetch actual posts (Firestore 'in' query supports up to 10 items)
            val posts = mutableListOf<CommunityPost>()
            postIds.chunked(10).forEach { chunk ->
                val postSnapshot = postsCollection
                    .whereIn("id", chunk)
                    .get()
                    .await()
                posts.addAll(postSnapshot.toObjects(CommunityPost::class.java))
            }

            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
