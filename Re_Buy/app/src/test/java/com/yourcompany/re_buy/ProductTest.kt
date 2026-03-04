package com.yourcompany.re_buy

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the Product data class
 * Tests product helper methods for region detection, product type classification, and sold-out status
 */
class ProductTest {

    @Test
    fun getRegion_dongdaemunCenter_returnsDongdaemun() {
        val product = Product(
            title = "Test Product",
            price = "50,000원",
            link = "http://test.com",
            image = "http://test.com/image.jpg",
            center = "동대문구 재활용센터",
            category = "가전",
            crawledAt = "2024-01-01",
            sourceUrl = "http://test.com"
        )
        assertEquals("dongdaemun", product.getRegion())
    }

    @Test
    fun getRegion_seodaemunCenter_returnsSeodaemun() {
        val product = Product(
            title = "Test Product",
            price = "50,000원",
            link = "http://test.com",
            image = "http://test.com/image.jpg",
            center = "서대문구 재활용센터",
            category = "가전",
            crawledAt = "2024-01-01",
            sourceUrl = "http://test.com"
        )
        assertEquals("seodaemun", product.getRegion())
    }

    @Test
    fun getRegion_unknownCenter_returnsUnknown() {
        val product = Product(
            title = "Test Product",
            price = "50,000원",
            link = "http://test.com",
            image = "http://test.com/image.jpg",
            center = "기타 센터",
            category = "가전",
            crawledAt = "2024-01-01",
            sourceUrl = "http://test.com"
        )
        assertEquals("unknown", product.getRegion())
    }

    @Test
    fun getProductType_refrigerator_returnsRefrigerator() {
        val product = Product(
            title = "삼성 냉장고 양문형",
            price = "100,000원",
            link = "http://test.com",
            image = "http://test.com/image.jpg",
            center = "서대문구 재활용센터",
            category = "가전",
            crawledAt = "2024-01-01",
            sourceUrl = "http://test.com"
        )
        assertEquals("refrigerator", product.getProductType())
    }

    @Test
    fun getProductType_washingMachine_returnsWashingMachine() {
        val product1 = Product(
            title = "LG 세탁기 14kg",
            price = "80,000원",
            link = "http://test.com",
            image = "http://test.com/image.jpg",
            center = "동대문구 재활용센터",
            category = "가전",
            crawledAt = "2024-01-01",
            sourceUrl = "http://test.com"
        )
        assertEquals("washing_machine", product1.getProductType())

        val product2 = Product(
            title = "드럼세탁기",
            price = "90,000원",
            link = "http://test.com",
            image = "http://test.com/image.jpg",
            center = "동대문구 재활용센터",
            category = "가전",
            crawledAt = "2024-01-01",
            sourceUrl = "http://test.com"
        )
        assertEquals("washing_machine", product2.getProductType())
    }

    @Test
    fun getProductType_microwave_returnsMicrowave() {
        val product = Product(
            title = "삼성 전자렌지",
            price = "30,000원",
            link = "http://test.com",
            image = "http://test.com/image.jpg",
            center = "서대문구 재활용센터",
            category = "가전",
            crawledAt = "2024-01-01",
            sourceUrl = "http://test.com"
        )
        assertEquals("microwave", product.getProductType())
    }

    @Test
    fun getProductType_tv_returnsTv() {
        val product1 = Product(
            title = "삼성 텔레비전 55인치",
            price = "120,000원",
            link = "http://test.com",
            image = "http://test.com/image.jpg",
            center = "서대문구 재활용센터",
            category = "가전",
            crawledAt = "2024-01-01",
            sourceUrl = "http://test.com"
        )
        assertEquals("tv", product1.getProductType())

        val product2 = Product(
            title = "LG TV 43인치",
            price = "100,000원",
            link = "http://test.com",
            image = "http://test.com/image.jpg",
            center = "동대문구 재활용센터",
            category = "가전",
            crawledAt = "2024-01-01",
            sourceUrl = "http://test.com"
        )
        assertEquals("tv", product2.getProductType())
    }

    @Test
    fun getProductType_other_returnsOther() {
        val product = Product(
            title = "책상",
            price = "50,000원",
            link = "http://test.com",
            image = "http://test.com/image.jpg",
            center = "서대문구 재활용센터",
            category = "가구",
            crawledAt = "2024-01-01",
            sourceUrl = "http://test.com"
        )
        assertEquals("other", product.getProductType())
    }

    @Test
    fun isSoldOut_soldOutProduct_returnsTrue() {
        val product = Product(
            title = "냉장고",
            price = "판매완료",
            link = "http://test.com",
            image = "http://test.com/image.jpg",
            center = "서대문구 재활용센터",
            category = "가전",
            crawledAt = "2024-01-01",
            sourceUrl = "http://test.com"
        )
        assertTrue(product.isSoldOut())
    }

    @Test
    fun isSoldOut_availableProduct_returnsFalse() {
        val product = Product(
            title = "냉장고",
            price = "100,000원",
            link = "http://test.com",
            image = "http://test.com/image.jpg",
            center = "서대문구 재활용센터",
            category = "가전",
            crawledAt = "2024-01-01",
            sourceUrl = "http://test.com"
        )
        assertFalse(product.isSoldOut())
    }

    @Test
    fun getProductType_caseInsensitive_worksCorrectly() {
        val product = Product(
            title = "SAMSUNG 냉장고",
            price = "100,000원",
            link = "http://test.com",
            image = "http://test.com/image.jpg",
            center = "서대문구 재활용센터",
            category = "가전",
            crawledAt = "2024-01-01",
            sourceUrl = "http://test.com"
        )
        assertEquals("refrigerator", product.getProductType())
    }
}
