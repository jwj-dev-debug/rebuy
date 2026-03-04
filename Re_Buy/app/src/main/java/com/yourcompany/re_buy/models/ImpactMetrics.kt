package com.yourcompany.re_buy.models

/**
 * Aggregated environmental impact metrics for a user
 * Calculates total impact across all purchases
 */
data class ImpactMetrics(
    val totalCarbonSavedKg: Double = 0.0,
    val totalWaterSavedLiters: Double = 0.0,
    val totalWastePreventedKg: Double = 0.0,
    val totalPurchases: Int = 0,
    val purchasesByCategory: Map<String, Int> = emptyMap(),
    val latestPurchaseDate: java.util.Date? = null
) {
    /**
     * Get achievement level based on total impact
     * Bronze: < 100kg CO2
     * Silver: 100-499kg CO2
     * Gold: 500-999kg CO2
     * Platinum: 1000-4999kg CO2
     * Diamond: 5000kg+ CO2
     */
    fun getAchievementLevel(): String {
        return when {
            totalCarbonSavedKg >= 5000.0 -> "diamond"
            totalCarbonSavedKg >= 1000.0 -> "platinum"
            totalCarbonSavedKg >= 500.0 -> "gold"
            totalCarbonSavedKg >= 100.0 -> "silver"
            else -> "bronze"
        }
    }

    /**
     * Get display name for achievement level
     */
    fun getAchievementLevelDisplayName(): String {
        return when (getAchievementLevel()) {
            "diamond" -> "다이아몬드"
            "platinum" -> "플래티넘"
            "gold" -> "골드"
            "silver" -> "실버"
            else -> "브론즈"
        }
    }

    /**
     * Get achievement icon color resource
     */
    fun getAchievementColor(): Int {
        return when (getAchievementLevel()) {
            "diamond" -> android.graphics.Color.parseColor("#B9F2FF") // Light blue
            "platinum" -> android.graphics.Color.parseColor("#E5E4E2") // Platinum
            "gold" -> android.graphics.Color.parseColor("#FFD700") // Gold
            "silver" -> android.graphics.Color.parseColor("#C0C0C0") // Silver
            else -> android.graphics.Color.parseColor("#CD7F32") // Bronze
        }
    }

    /**
     * Calculate equivalent trees planted
     * Average tree absorbs ~21kg CO2 per year
     */
    fun getEquivalentTreesPlanted(): Double {
        return totalCarbonSavedKg / 21.0
    }

    /**
     * Calculate equivalent car miles not driven
     * Average car emits ~0.404kg CO2 per mile
     */
    fun getEquivalentCarMilesNotDriven(): Double {
        return totalCarbonSavedKg / 0.404
    }

    /**
     * Get percentage to next achievement level
     */
    fun getProgressToNextLevel(): Double {
        val currentLevel = getAchievementLevel()
        val nextThreshold = when (currentLevel) {
            "bronze" -> 100.0
            "silver" -> 500.0
            "gold" -> 1000.0
            "platinum" -> 5000.0
            "diamond" -> return 100.0 // Already at max
            else -> 100.0
        }

        val previousThreshold = when (currentLevel) {
            "bronze" -> 0.0
            "silver" -> 100.0
            "gold" -> 500.0
            "platinum" -> 1000.0
            "diamond" -> 5000.0
            else -> 0.0
        }

        val progress = ((totalCarbonSavedKg - previousThreshold) / (nextThreshold - previousThreshold)) * 100.0
        return progress.coerceIn(0.0, 100.0)
    }

    /**
     * Get message for sharing impact
     */
    fun getShareMessage(): String {
        return """
            🌱 Re:Buy 환경 영향 리포트

            재활용 제품 ${totalPurchases}개 구매로 지구를 보호했습니다!

            🌍 탄소 절감: ${String.format("%.1f", totalCarbonSavedKg)}kg CO2
            💧 물 절약: ${String.format("%.0f", totalWaterSavedLiters)}L
            ♻️ 폐기물 감소: ${String.format("%.1f", totalWastePreventedKg)}kg

            🌳 ${String.format("%.1f", getEquivalentTreesPlanted())}그루의 나무를 심은 것과 같은 효과!

            달성 레벨: ${getAchievementLevelDisplayName()}

            #ReReuse #재활용 #환경보호 #지속가능성
        """.trimIndent()
    }
}
