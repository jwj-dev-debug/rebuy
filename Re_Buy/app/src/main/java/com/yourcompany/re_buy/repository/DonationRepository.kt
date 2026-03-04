package com.yourcompany.re_buy.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yourcompany.re_buy.models.Donation
import com.yourcompany.re_buy.models.DonationStatus
import com.yourcompany.re_buy.models.DonationType
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Repository for managing item donations to recycling centers
 */
class DonationRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val donationsCollection = firestore.collection("donations")

    companion object {
        private const val TAG = "DonationRepo"

        // Environmental impact estimates for donations
        private val DONATION_IMPACT_FACTORS = mapOf(
            "냉장고" to Triple(150.0, 2500.0, 45.0),
            "세탁기" to Triple(120.0, 2000.0, 35.0),
            "TV/영상기기" to Triple(80.0, 1200.0, 25.0),
            "전자레인지" to Triple(40.0, 600.0, 15.0),
            "침대" to Triple(50.0, 800.0, 30.0),
            "소파" to Triple(45.0, 700.0, 28.0),
            "기타" to Triple(30.0, 400.0, 10.0)
        )
    }

    /**
     * Submit a donation
     */
    suspend fun submitDonation(
        itemTitle: String,
        itemCategory: String,
        itemCondition: String,
        itemDescription: String,
        itemImages: List<String>,
        estimatedValue: String,
        centerName: String,
        centerId: String,
        donationType: DonationType,
        pickupAddress: String = "",
        pickupDate: Date? = null,
        pickupTimeSlot: String = "",
        pickupNotes: String = ""
    ): Result<Donation> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "Submitting donation: $itemTitle")

            // Calculate environmental impact
            val (carbon, water, waste) = DONATION_IMPACT_FACTORS[itemCategory] ?: DONATION_IMPACT_FACTORS["기타"]!!

            val donation = Donation(
                userId = currentUser.uid,
                userName = currentUser.displayName ?: "Anonymous",
                userEmail = currentUser.email ?: "",
                userPhone = "",
                itemTitle = itemTitle,
                itemCategory = itemCategory,
                itemCondition = itemCondition,
                itemDescription = itemDescription,
                itemImages = itemImages,
                estimatedValue = estimatedValue,
                centerId = centerId,
                centerName = centerName,
                donationType = donationType,
                status = DonationStatus.PENDING,
                pickupAddress = pickupAddress,
                pickupDate = pickupDate,
                pickupTimeSlot = pickupTimeSlot,
                pickupNotes = pickupNotes,
                carbonSavedKg = carbon,
                waterSavedLiters = water,
                wastePreventedKg = waste,
                createdAt = Date(),
                updatedAt = Date()
            )

            val docRef = donationsCollection.add(donation).await()
            donation.id = docRef.id

            Log.d(TAG, "Donation submitted with ID: ${docRef.id}")
            Result.success(donation)
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting donation: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get all donations by current user
     */
    suspend fun getUserDonations(): Result<List<Donation>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val snapshot = donationsCollection
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val donations = snapshot.toObjects(Donation::class.java)
                .onEach { donation ->
                    donation.id = snapshot.documents.find { it.data?.get("userId") == donation.userId }?.id ?: ""
                }

            Log.d(TAG, "Found ${donations.size} donations for user")
            Result.success(donations)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting donations: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Cancel a donation
     */
    suspend fun cancelDonation(donationId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val docRef = donationsCollection.document(donationId)
            val snapshot = docRef.get().await()
            val donation = snapshot.toObject(Donation::class.java)

            if (donation?.userId != currentUser.uid) {
                return Result.failure(Exception("Unauthorized"))
            }

            docRef.update(
                mapOf(
                    "status" to DonationStatus.CANCELLED.name,
                    "updatedAt" to Date()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling donation: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get total environmental impact from donations
     */
    suspend fun getUserDonationImpact(): Result<Triple<Double, Double, Double>> {
        return try {
            val donationsResult = getUserDonations()
            if (donationsResult.isFailure) {
                return Result.failure(donationsResult.exceptionOrNull()!!)
            }

            val donations = donationsResult.getOrNull() ?: emptyList()
            val completedDonations = donations.filter { it.isComplete() }

            val totalCarbon = completedDonations.sumOf { it.carbonSavedKg }
            val totalWater = completedDonations.sumOf { it.waterSavedLiters }
            val totalWaste = completedDonations.sumOf { it.wastePreventedKg }

            Result.success(Triple(totalCarbon, totalWater, totalWaste))
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating donation impact: ${e.message}", e)
            Result.failure(e)
        }
    }
}
