package com.yourcompany.re_buy.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourcompany.re_buy.models.Achievement
import com.yourcompany.re_buy.models.AchievementType
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Repository for managing user achievements and badges
 */
class AchievementRepository(
    private val purchasesRepo: EnvironmentalImpactRepository = EnvironmentalImpactRepository(),
    private val donationsRepo: DonationRepository = DonationRepository()
) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val achievementsCollection = firestore.collection("achievements")

    companion object {
        private const val TAG = "AchievementRepo"
    }

    /**
     * Check and award achievements based on user activity
     */
    suspend fun checkAndAwardAchievements(): Result<List<Achievement>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val newAchievements = mutableListOf<Achievement>()

            // Check purchase-based achievements
            val purchasesResult = purchasesRepo.getUserPurchases()
            if (purchasesResult.isSuccess) {
                val purchases = purchasesResult.getOrNull() ?: emptyList()
                val purchaseCount = purchases.size

                // Milestone achievements
                val milestones = mapOf(
                    1 to AchievementType.FIRST_PURCHASE,
                    5 to AchievementType.PURCHASES_5,
                    10 to AchievementType.PURCHASES_10,
                    25 to AchievementType.PURCHASES_25,
                    50 to AchievementType.PURCHASES_50
                )

                milestones.forEach { (threshold, type) ->
                    if (purchaseCount >= threshold && !hasAchievement(type)) {
                        newAchievements.add(awardAchievement(type, threshold))
                    }
                }

                // Category specialists
                val applianceCount = purchases.count { it.productCategory in listOf("냉장고", "세탁기", "전자레인지", "TV/영상기기") }
                if (applianceCount >= 10 && !hasAchievement(AchievementType.APPLIANCE_EXPERT)) {
                    newAchievements.add(awardAchievement(AchievementType.APPLIANCE_EXPERT, applianceCount))
                }

                val furnitureCount = purchases.count { it.productCategory in listOf("침대", "소파", "화장대", "서랍장/수납가구", "테이블/책상") }
                if (furnitureCount >= 10 && !hasAchievement(AchievementType.FURNITURE_COLLECTOR)) {
                    newAchievements.add(awardAchievement(AchievementType.FURNITURE_COLLECTOR, furnitureCount))
                }

                // Environmental impact
                val totalCarbon = purchases.sumOf { it.carbonSavedKg }
                if (totalCarbon >= 1000 && !hasAchievement(AchievementType.CARBON_SAVER_1000KG)) {
                    newAchievements.add(awardAchievement(AchievementType.CARBON_SAVER_1000KG, totalCarbon.toInt()))
                } else if (totalCarbon >= 500 && !hasAchievement(AchievementType.CARBON_SAVER_500KG)) {
                    newAchievements.add(awardAchievement(AchievementType.CARBON_SAVER_500KG, totalCarbon.toInt()))
                } else if (totalCarbon >= 100 && !hasAchievement(AchievementType.CARBON_SAVER_100KG)) {
                    newAchievements.add(awardAchievement(AchievementType.CARBON_SAVER_100KG, totalCarbon.toInt()))
                }

                val totalWater = purchases.sumOf { it.waterSavedLiters }
                if (totalWater >= 50000 && !hasAchievement(AchievementType.WATER_SAVER_50000L)) {
                    newAchievements.add(awardAchievement(AchievementType.WATER_SAVER_50000L, totalWater.toInt()))
                } else if (totalWater >= 10000 && !hasAchievement(AchievementType.WATER_SAVER_10000L)) {
                    newAchievements.add(awardAchievement(AchievementType.WATER_SAVER_10000L, totalWater.toInt()))
                }
            }

            // Check donation-based achievements
            val donationsResult = donationsRepo.getUserDonations()
            if (donationsResult.isSuccess) {
                val donations = donationsResult.getOrNull() ?: emptyList()
                val donationCount = donations.count { it.isComplete() }

                if (donationCount >= 1 && !hasAchievement(AchievementType.FIRST_DONATION)) {
                    newAchievements.add(awardAchievement(AchievementType.FIRST_DONATION, 1))
                }
                if (donationCount >= 5 && !hasAchievement(AchievementType.DONATIONS_5)) {
                    newAchievements.add(awardAchievement(AchievementType.DONATIONS_5, 5))
                }
                if (donationCount >= 20 && !hasAchievement(AchievementType.GENEROUS_GIVER)) {
                    newAchievements.add(awardAchievement(AchievementType.GENEROUS_GIVER, 20))
                }
            }

            Result.success(newAchievements)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking achievements: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get all achievements earned by current user
     */
    suspend fun getUserAchievements(): Result<List<Achievement>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val snapshot = achievementsCollection
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val achievements = snapshot.toObjects(Achievement::class.java)
                .onEach { it.id = snapshot.documents.find { doc -> doc.data?.get("userId") == it.userId }?.id ?: "" }

            Log.d(TAG, "Found ${achievements.size} achievements for user")
            Result.success(achievements)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting achievements: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Check if user has a specific achievement
     */
    private suspend fun hasAchievement(type: AchievementType): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false

            val snapshot = achievementsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("achievementType", type.name)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Award an achievement to the user
     */
    private suspend fun awardAchievement(type: AchievementType, progress: Int): Achievement {
        val currentUser = auth.currentUser!!

        val achievement = Achievement(
            userId = currentUser.uid,
            achievementType = type,
            earnedAt = Date(),
            progress = progress
        )

        val docRef = achievementsCollection.add(achievement).await()
        achievement.id = docRef.id

        Log.d(TAG, "Awarded achievement: ${type.name}")
        return achievement
    }
}
