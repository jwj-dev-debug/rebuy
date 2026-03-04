package com.yourcompany.re_buy.models

import com.google.firebase.firestore.DocumentId
import java.util.Date

/**
 * Represents a product purchase/acquisition by a user
 * Tracks when users acquire recycled products for environmental impact calculation
 */
data class Purchase(
    @DocumentId
    var id: String = "",

    val userId: String = "",

    // Product information
    val productTitle: String = "",
    val productCategory: String = "",
    val productType: String = "", // refrigerator, washing_machine, tv, microwave, other
    val productLink: String = "",
    val productImage: String = "",
    val centerName: String = "",
    val region: String = "",

    // Purchase details
    val purchaseDate: Date = Date(),
    val priceKrw: String = "",

    // Environmental impact (calculated at purchase time)
    val carbonSavedKg: Double = 0.0,
    val waterSavedLiters: Double = 0.0,
    val wastePreventedKg: Double = 0.0,

    // Metadata
    val createdAt: Date = Date()
) {
    /**
     * Get display name for product type
     */
    fun getProductTypeDisplayName(): String {
        return when (productType) {
            "refrigerator" -> "냉장고"
            "washing_machine" -> "세탁기"
            "microwave" -> "전자렌지"
            "tv" -> "TV"
            else -> "기타"
        }
    }

    /**
     * Get total impact score for achievements
     */
    fun getTotalImpactScore(): Double {
        return carbonSavedKg + (waterSavedLiters / 1000.0) + wastePreventedKg
    }
}
