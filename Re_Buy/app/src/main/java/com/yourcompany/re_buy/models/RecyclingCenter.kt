package com.yourcompany.re_buy.models

/**
 * Data model for recycling centers across South Korea
 */
data class RecyclingCenter(
    val name: String,
    val district: String, // e.g., "서대문구", "동대문구"
    val latitude: Double,
    val longitude: Double,
    val websiteUrl: String,
    val address: String = "",
    val phone: String = ""
) {
    companion object {
        /**
         * Get all recycling centers in Seoul
         * Updated with actual recycling center locations and contact information
         */
        fun getSeoulCenters(): List<RecyclingCenter> {
            return listOf(
                // 1. Jongno-gu Recycling Center
                RecyclingCenter(
                    name = "종로구 재활용센터",
                    district = "종로구",
                    latitude = 37.5716,
                    longitude = 126.9843,
                    websiteUrl = "http://remarketc.co.kr/",
                    address = "종로구 대신로 174 성동상가 102호",
                    phone = "02-2233-7281"
                ),
                // 2. Jung-gu Recycling Center
                RecyclingCenter(
                    name = "중구 재활용센터",
                    district = "중구",
                    latitude = 37.5658,
                    longitude = 127.0179,
                    websiteUrl = "http://www.recyclecn.co.kr/",
                    address = "서울시 중구 신당동 174-1 신당상가 101",
                    phone = "02-833-8299"
                ),
                // 3. Yongsan-gu Recycling Center
                RecyclingCenter(
                    name = "용산구 재활용센터",
                    district = "용산구",
                    latitude = 37.5501,
                    longitude = 126.9783,
                    websiteUrl = "http://ywspjhj.lbsy.kr/",
                    address = "용산구 후암동 246",
                    phone = "02-400-8133"
                ),
                // 4. Dobong-gu Recycling Center
                RecyclingCenter(
                    name = "도봉구 재활용센터",
                    district = "도봉구",
                    latitude = 37.6536,
                    longitude = 127.0408,
                    websiteUrl = "http://www.xn--2e0b383j5keo4hn0g24t0j.com/",
                    address = "서울시 도봉구 창동 101-102",
                    phone = "02-902-8272"
                ),
                // 5. Seodaemun-gu Recycling Center
                RecyclingCenter(
                    name = "서대문구 재활용센터",
                    district = "서대문구",
                    latitude = 37.5887,
                    longitude = 126.9291,
                    websiteUrl = "http://www.s8272.co.kr/",
                    address = "서울시 서대문구 홍은동 426-8",
                    phone = "02-394-8272"
                ),
                // 6. Seongbuk-gu Recycling Center
                RecyclingCenter(
                    name = "성북구 재활용센터",
                    district = "성북구",
                    latitude = 37.6062,
                    longitude = 127.0291,
                    websiteUrl = "http://aputopy.lbsy.kr/",
                    address = "서울 성북구 하월곡동 42-47",
                    phone = "02-941-8272"
                ),
                // 7. Gangdong-gu Recycling Center
                RecyclingCenter(
                    name = "강동구 재활용센터",
                    district = "강동구",
                    latitude = 37.5387,
                    longitude = 127.1235,
                    websiteUrl = "http://hypenyu.lbsy.kr/",
                    address = "서울시 강동구 천호동 102-495",
                    phone = "02-488-4595"
                ),
                // 8. Mapo-gu Recycling Center
                RecyclingCenter(
                    name = "마포구 재활용센터",
                    district = "마포구",
                    latitude = 37.5496,
                    longitude = 126.9512,
                    websiteUrl = "http://www.zungko.co.kr/",
                    address = "서울시 마포구 공덕동 4-16",
                    phone = "02-713-7289"
                ),
                // 9. Jungnang-gu Recycling Center
                RecyclingCenter(
                    name = "중랑구 재활용센터",
                    district = "중랑구",
                    latitude = 37.5987,
                    longitude = 127.0736,
                    websiteUrl = "https://www.jungnang.go.kr/portal/main/contents.do?menuNo=200052",
                    address = "중랑구 면목동 377",
                    phone = "02-435-7272"
                ),
                // 10. Yeongdeungpo-gu Recycling Center
                RecyclingCenter(
                    name = "영등포구 재활용센터",
                    district = "영등포구",
                    latitude = 37.5164,
                    longitude = 126.9076,
                    websiteUrl = "https://www.ydp.go.kr/www/contents.do?key=2733",
                    address = "영등포구 영등포동 1가 108-5",
                    phone = "02-2677-8277"
                ),
                // 11. Eunpyeong-gu Recycling Center
                RecyclingCenter(
                    name = "은평구 재활용센터",
                    district = "은평구",
                    latitude = 37.6028,
                    longitude = 126.9181,
                    websiteUrl = "http://ae-waste.ep.go.kr/",
                    address = "은평구 응암동 102-351-614",
                    phone = "02-351-6114"
                ),
                // 12. Gangseo-gu Recycling Center
                RecyclingCenter(
                    name = "강서구 재활용센터",
                    district = "강서구",
                    latitude = 37.5414,
                    longitude = 126.8397,
                    websiteUrl = "http://www.gangseo.seoul.kr/env/env020102",
                    address = "강서구 화곡동 102-458",
                    phone = "02-2692-4581"
                ),
                // 13. Dongdaemun-gu Recycling Center
                RecyclingCenter(
                    name = "동대문구 재활용센터",
                    district = "동대문구",
                    latitude = 37.5747,
                    longitude = 127.0442,
                    websiteUrl = "http://www.dm8272.co.kr/",
                    address = "동대문구 답십리동 102-282",
                    phone = "02-2248-7282"
                )
            )
        }
    }
}
