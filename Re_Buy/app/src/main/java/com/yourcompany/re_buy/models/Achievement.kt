package com.yourcompany.re_buy.models

import java.util.Date

/**
 * User achievement/badge for environmental contributions
 */
data class Achievement(
    var id: String = "",
    val userId: String = "",
    val achievementType: AchievementType,
    val earnedAt: Date = Date(),
    val progress: Int = 0, // For multi-level achievements
    val metadata: Map<String, String> = emptyMap()
) {
    /**
     * Get display information for this achievement
     */
    fun getDisplayInfo(): AchievementInfo {
        return achievementType.getInfo()
    }
}

/**
 * Types of achievements users can earn
 */
enum class AchievementType {
    // Purchase milestones
    FIRST_PURCHASE,
    PURCHASES_5,
    PURCHASES_10,
    PURCHASES_25,
    PURCHASES_50,

    // Category specialists
    APPLIANCE_EXPERT,
    FURNITURE_COLLECTOR,

    // Environmental impact
    CARBON_SAVER_100KG,
    CARBON_SAVER_500KG,
    CARBON_SAVER_1000KG,
    WATER_SAVER_10000L,
    WATER_SAVER_50000L,

    // Community engagement
    ACTIVE_COMMENTER,
    COMMUNITY_CONTRIBUTOR,
    HELPFUL_REVIEWER,

    // Donation achievements
    FIRST_DONATION,
    DONATIONS_5,
    GENEROUS_GIVER,

    // Consistency
    MONTHLY_BUYER,
    ECO_WARRIOR,
    PLATFORM_VETERAN;

    /**
     * Get display information for each achievement
     */
    fun getInfo(): AchievementInfo {
        return when (this) {
            FIRST_PURCHASE -> AchievementInfo(
                "첫 구매", "첫 번째 재활용 제품 구매", "🎉", 1
            )
            PURCHASES_5 -> AchievementInfo(
                "스마트 쇼퍼", "재활용 제품 5개 구매", "🛒", 5
            )
            PURCHASES_10 -> AchievementInfo(
                "에코 수집가", "재활용 제품 10개 구매", "📦", 10
            )
            PURCHASES_25 -> AchievementInfo(
                "지속가능한 소비자", "재활용 제품 25개 구매", "♻️", 25
            )
            PURCHASES_50 -> AchievementInfo(
                "재활용 마스터", "재활용 제품 50개 구매", "👑", 50
            )
            APPLIANCE_EXPERT -> AchievementInfo(
                "가전 전문가", "가전제품 10개 구매", "🔌", 10
            )
            FURNITURE_COLLECTOR -> AchievementInfo(
                "가구 수집가", "가구 10개 구매", "🪑", 10
            )
            CARBON_SAVER_100KG -> AchievementInfo(
                "탄소 절감 시작", "탄소 100kg 절감", "🌱", 100
            )
            CARBON_SAVER_500KG -> AchievementInfo(
                "탄소 절감 영웅", "탄소 500kg 절감", "🌳", 500
            )
            CARBON_SAVER_1000KG -> AchievementInfo(
                "탄소 절감 전설", "탄소 1톤 절감", "🌲", 1000
            )
            WATER_SAVER_10000L -> AchievementInfo(
                "물 절약가", "물 10,000L 절약", "💧", 10000
            )
            WATER_SAVER_50000L -> AchievementInfo(
                "물 절약 전문가", "물 50,000L 절약", "🌊", 50000
            )
            ACTIVE_COMMENTER -> AchievementInfo(
                "활발한 참여자", "커뮤니티 댓글 20개 작성", "💬", 20
            )
            COMMUNITY_CONTRIBUTOR -> AchievementInfo(
                "커뮤니티 기여자", "커뮤니티 게시글 10개 작성", "📝", 10
            )
            HELPFUL_REVIEWER -> AchievementInfo(
                "도움되는 리뷰어", "리뷰 10개 작성", "⭐", 10
            )
            FIRST_DONATION -> AchievementInfo(
                "첫 기부", "첫 번째 물품 기부", "🎁", 1
            )
            DONATIONS_5 -> AchievementInfo(
                "관대한 기부자", "물품 5개 기부", "💝", 5
            )
            GENEROUS_GIVER -> AchievementInfo(
                "기부 천사", "물품 20개 기부", "👼", 20
            )
            MONTHLY_BUYER -> AchievementInfo(
                "꾸준한 구매자", "3개월 연속 구매", "📅", 3
            )
            ECO_WARRIOR -> AchievementInfo(
                "환경 전사", "모든 활동 활발히 참여", "🦸", 1
            )
            PLATFORM_VETERAN -> AchievementInfo(
                "플랫폼 베테랑", "가입 1년 기념", "🎖️", 365
            )
        }
    }
}

/**
 * Display information for an achievement
 */
data class AchievementInfo(
    val title: String,
    val description: String,
    val icon: String,
    val requirement: Int
)
