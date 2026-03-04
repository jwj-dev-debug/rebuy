package com.yourcompany.re_buy

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Product(
    @SerializedName("title")
    val title: String = "",

    @SerializedName("price")
    val price: String = "",

    @SerializedName("link")
    val link: String = "",

    @SerializedName("image")
    val image: String = "",

    @SerializedName("center")
    val center: String = "",

    @SerializedName("category")
    val category: String = "",

    @SerializedName("createdAt")
    val createdAt: String = "",

    @SerializedName("crawledAt")
    val crawledAt: String = "",

    @SerializedName("sourceUrl")
    val sourceUrl: String = "",

    // New fields for enhanced platform functionality (not in JSON)
    @Transient val status: ProductStatus = ProductStatus.AVAILABLE,
    @Transient val condition: ProductCondition = ProductCondition.GOOD,
    @Transient val quantity: Int = 1,
    @Transient val description: String = "",
    @Transient val centerId: String = "",
    @Transient val images: List<String> = emptyList(), // Multiple photos
    @Transient val lastUpdated: Date? = null,
    @Transient val reservedBy: String = "", // userId if reserved
    @Transient val reservedUntil: Date? = null
) {
    // Helper method to get region from center name
    fun getRegion(): String {
        return when {
            center.contains("동대문") -> "dongdaemun"
            center.contains("서대문") -> "seodaemun"
            else -> "unknown"
        }
    }

    // Helper method to determine product type
    fun getProductType(): String {
        return when {
            title.contains("냉장고", ignoreCase = true) -> "refrigerator"
            title.contains("세탁기", ignoreCase = true) -> "washing_machine"
            title.contains("드럼", ignoreCase = true) -> "washing_machine"
            title.contains("전자렌지", ignoreCase = true) -> "microwave"
            title.contains("텔레비", ignoreCase = true) || title.contains("TV", ignoreCase = true) -> "tv"
            else -> "other"
        }
    }

    // Check if product is sold out
    fun isSoldOut(): Boolean {
        return price.contains("판매완료")
    }

    // Get the date string (prefer createdAt over crawledAt)
    fun getDateString(): String {
        return when {
            createdAt.isNotEmpty() -> createdAt
            crawledAt.isNotEmpty() -> crawledAt
            else -> ""
        }
    }

    // Check if product is available for purchase/reservation
    fun isAvailable(): Boolean {
        return status == ProductStatus.AVAILABLE && quantity > 0 && !isSoldOut()
    }

    // Check if product is reserved
    fun isReserved(): Boolean {
        return status == ProductStatus.RESERVED && reservedUntil?.after(Date()) == true
    }
}

/**
 * Status of a product in the recycling center inventory
 */
enum class ProductStatus {
    AVAILABLE,      // Available for purchase/reservation
    RESERVED,       // Reserved by a user
    SOLD,          // Sold and awaiting pickup
    PICKED_UP,     // Picked up by customer
    REMOVED        // Removed from inventory
}

/**
 * Condition/quality grade of a recycled product
 */
enum class ProductCondition(val displayName: String, val description: String) {
    LIKE_NEW("거의 새것", "사용감이 거의 없는 상태"),
    EXCELLENT("최상", "약간의 사용감은 있으나 기능과 외관이 우수함"),
    GOOD("양호", "정상 작동하며 사용감이 있음"),
    FAIR("보통", "작동하나 외관상 흠집이나 손상이 있음"),
    AS_IS("현재상태", "작동 미확인 또는 수리 필요")
}
