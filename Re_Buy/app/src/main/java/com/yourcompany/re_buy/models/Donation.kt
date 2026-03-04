package com.yourcompany.re_buy.models

import java.util.Date

/**
 * Represents an item donated by a user to a recycling center
 */
data class Donation(
    var id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",

    // Donated item details
    val itemTitle: String = "",
    val itemCategory: String = "",
    val itemCondition: String = "",
    val itemDescription: String = "",
    val itemImages: List<String> = emptyList(),
    val estimatedValue: String = "",

    // Center information
    val centerId: String = "",
    val centerName: String = "",

    // Donation logistics
    val donationType: DonationType = DonationType.DROP_OFF,
    val status: DonationStatus = DonationStatus.PENDING,

    // For pickups
    val pickupAddress: String = "",
    val pickupDate: Date? = null,
    val pickupTimeSlot: String = "",
    val pickupNotes: String = "",

    // For drop-offs
    val dropOffDate: Date? = null,
    val dropOffTimeSlot: String = "",

    // Environmental impact
    val carbonSavedKg: Double = 0.0,
    val waterSavedLiters: Double = 0.0,
    val wastePreventedKg: Double = 0.0,

    // Status tracking
    val acceptedByCenter: Boolean = false,
    val acceptedAt: Date? = null,
    val completedAt: Date? = null,
    val rejectedAt: Date? = null,
    val rejectionReason: String = "",

    // Receipt
    val receiptIssued: Boolean = false,
    val receiptNumber: String = "",

    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    /**
     * Check if donation is pending action
     */
    fun isPending(): Boolean {
        return status == DonationStatus.PENDING
    }

    /**
     * Check if donation is complete
     */
    fun isComplete(): Boolean {
        return status == DonationStatus.COMPLETED
    }
}

/**
 * Type of donation logistics
 */
enum class DonationType(val displayName: String) {
    DROP_OFF("직접 배송"),
    PICKUP("픽업 요청")
}

/**
 * Status of a donation
 */
enum class DonationStatus(val displayName: String) {
    PENDING("검토중"),
    ACCEPTED("수락됨"),
    SCHEDULED("일정 확정"),
    IN_TRANSIT("이동중"),
    COMPLETED("완료됨"),
    REJECTED("거절됨"),
    CANCELLED("취소됨")
}
