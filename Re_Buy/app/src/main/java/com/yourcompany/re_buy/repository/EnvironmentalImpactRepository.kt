package com.yourcompany.re_buy.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourcompany.re_buy.Product
import com.yourcompany.re_buy.models.ImpactMetrics
import com.yourcompany.re_buy.models.Purchase
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Repository for managing environmental impact tracking
 * Calculates and stores environmental savings from recycled product purchases
 */
class EnvironmentalImpactRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val purchasesCollection = firestore.collection("purchases")

    companion object {
        private const val TAG = "ImpactRepo"

        /**
         * Environmental impact factors per product type
         * Based on average manufacturing carbon footprint, water usage, and material weight
         */
        private val IMPACT_FACTORS = mapOf(
            // Refrigerator: Large appliance, high manufacturing impact
            "refrigerator" to Triple(
                150.0,  // kg CO2 saved
                2500.0, // liters water saved
                45.0    // kg waste prevented
            ),
            // Washing machine: Medium-large appliance
            "washing_machine" to Triple(
                120.0,  // kg CO2 saved
                2000.0, // liters water saved
                35.0    // kg waste prevented
            ),
            // TV: Electronics with significant carbon footprint
            "tv" to Triple(
                80.0,   // kg CO2 saved
                1200.0, // liters water saved
                25.0    // kg waste prevented
            ),
            // Microwave: Smaller appliance
            "microwave" to Triple(
                40.0,   // kg CO2 saved
                600.0,  // liters water saved
                15.0    // kg waste prevented
            ),
            // Other: Conservative estimate
            "other" to Triple(
                30.0,   // kg CO2 saved
                400.0,  // liters water saved
                10.0    // kg waste prevented
            )
        )
    }

    /**
     * Calculate environmental impact for a product
     */
    fun calculateImpact(product: Product): Triple<Double, Double, Double> {
        val productType = product.getProductType()
        val factors = IMPACT_FACTORS[productType] ?: IMPACT_FACTORS["other"]!!

        Log.d(TAG, "Calculated impact for $productType: CO2=${factors.first}kg, Water=${factors.second}L, Waste=${factors.third}kg")

        return factors
    }

    /**
     * Record a product purchase and track environmental impact
     */
    suspend fun recordPurchase(product: Product): Result<Purchase> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "Cannot record purchase - user not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "Recording purchase for product: ${product.title}")

            // Calculate environmental impact
            val (carbonSaved, waterSaved, wastePrevented) = calculateImpact(product)

            // Create purchase record
            val purchase = Purchase(
                userId = currentUser.uid,
                productTitle = product.title,
                productCategory = product.category,
                productType = product.getProductType(),
                productLink = product.link,
                productImage = product.image,
                centerName = product.center,
                region = product.getRegion(),
                purchaseDate = Date(),
                priceKrw = product.price,
                carbonSavedKg = carbonSaved,
                waterSavedLiters = waterSaved,
                wastePreventedKg = wastePrevented,
                createdAt = Date()
            )

            // Save to Firestore
            val docRef = purchasesCollection.add(purchase).await()
            purchase.id = docRef.id

            Log.d(TAG, "Purchase recorded successfully with ID: ${docRef.id}")
            Log.d(TAG, "Environmental impact - CO2: ${carbonSaved}kg, Water: ${waterSaved}L, Waste: ${wastePrevented}kg")

            Result.success(purchase)
        } catch (e: Exception) {
            Log.e(TAG, "Error recording purchase: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get all purchases for current user
     */
    suspend fun getUserPurchases(): Result<List<Purchase>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "Cannot get purchases - user not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "Getting purchases for user: ${currentUser.uid}")

            val snapshot = purchasesCollection
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val purchases = snapshot.toObjects(Purchase::class.java)
                .sortedByDescending { it.purchaseDate }

            Log.d(TAG, "Found ${purchases.size} purchases for user")

            Result.success(purchases)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting purchases: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Calculate aggregated impact metrics for current user
     */
    suspend fun getUserImpactMetrics(): Result<ImpactMetrics> {
        return try {
            val purchasesResult = getUserPurchases()

            if (purchasesResult.isFailure) {
                return Result.failure(purchasesResult.exceptionOrNull()!!)
            }

            val purchases = purchasesResult.getOrNull() ?: emptyList()

            if (purchases.isEmpty()) {
                Log.d(TAG, "No purchases found, returning empty metrics")
                return Result.success(ImpactMetrics())
            }

            // Aggregate metrics
            val totalCarbon = purchases.sumOf { it.carbonSavedKg }
            val totalWater = purchases.sumOf { it.waterSavedLiters }
            val totalWaste = purchases.sumOf { it.wastePreventedKg }
            val latestPurchase = purchases.maxByOrNull { it.purchaseDate }

            // Count purchases by category
            val purchasesByCategory = purchases.groupingBy { it.productCategory }
                .eachCount()

            val metrics = ImpactMetrics(
                totalCarbonSavedKg = totalCarbon,
                totalWaterSavedLiters = totalWater,
                totalWastePreventedKg = totalWaste,
                totalPurchases = purchases.size,
                purchasesByCategory = purchasesByCategory,
                latestPurchaseDate = latestPurchase?.purchaseDate
            )

            Log.d(TAG, "Calculated metrics - Purchases: ${metrics.totalPurchases}, CO2: ${metrics.totalCarbonSavedKg}kg, Level: ${metrics.getAchievementLevel()}")

            Result.success(metrics)
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating metrics: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a purchase record
     */
    suspend fun deletePurchase(purchaseId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "Cannot delete purchase - user not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "Deleting purchase: $purchaseId")

            // Verify ownership before deleting
            val doc = purchasesCollection.document(purchaseId).get().await()
            val purchase = doc.toObject(Purchase::class.java)

            if (purchase?.userId != currentUser.uid) {
                Log.e(TAG, "Cannot delete purchase - not owned by current user")
                return Result.failure(Exception("Unauthorized"))
            }

            purchasesCollection.document(purchaseId).delete().await()

            Log.d(TAG, "Purchase deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting purchase: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Check if a product has already been purchased by user
     * (Based on product link to avoid duplicate purchases)
     */
    suspend fun isProductPurchased(productLink: String): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.success(false)
            }

            val snapshot = purchasesCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("productLink", productLink)
                .limit(1)
                .get()
                .await()

            val isPurchased = !snapshot.isEmpty

            Log.d(TAG, "Product ${if (isPurchased) "already" else "not"} purchased")

            Result.success(isPurchased)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking purchase status: ${e.message}", e)
            Result.failure(e)
        }
    }
}
