package com.yourcompany.re_buy.models

import java.util.Date

/**
 * User review for a purchased recycled product
 */
data class ProductReview(
    var id: String = "",
    val userId: String = "",
    val userName: String = "",
    val productLink: String = "",
    val productTitle: String = "",
    val centerName: String = "",

    // Review content
    val rating: Float = 0f, // 0-5 stars
    val conditionRating: Float = 0f, // How accurate was the condition description
    val comment: String = "",
    val reviewImages: List<String> = emptyList(),

    // Verification
    val verified: Boolean = false, // Verified purchase
    val purchaseId: String = "",

    // Helpfulness
    val helpfulCount: Int = 0,
    val reportCount: Int = 0,

    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
