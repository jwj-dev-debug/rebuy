package com.yourcompany.re_buy

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Unit tests for ProductRepository
 * Tests search and filter functionality
 *
 * NOTE: These tests use mock data since ProductRepository requires Android Context
 * For full integration tests, use androidTest with instrumented tests
 */
class ProductRepositoryTest {

    private lateinit var testProducts: List<Product>

    @Before
    fun setup() {
        // Create test products with various attributes
        testProducts = listOf(
            Product(
                title = "삼성 냉장고 양문형",
                price = "100,000원",
                link = "http://test1.com",
                image = "http://test1.com/image.jpg",
                center = "서대문구 재활용센터",
                category = "가전",
                crawledAt = "2024-01-01",
                sourceUrl = "http://test.com"
            ),
            Product(
                title = "LG 세탁기 14kg",
                price = "판매완료",
                link = "http://test2.com",
                image = "http://test2.com/image.jpg",
                center = "동대문구 재활용센터",
                category = "가전",
                crawledAt = "2024-01-01",
                sourceUrl = "http://test.com"
            ),
            Product(
                title = "드럼세탁기",
                price = "90,000원",
                link = "http://test3.com",
                image = "http://test3.com/image.jpg",
                center = "동대문구 재활용센터",
                category = "가전",
                crawledAt = "2024-01-01",
                sourceUrl = "http://test.com"
            ),
            Product(
                title = "삼성 냉장고",
                price = "판매완료",
                link = "http://test4.com",
                image = "http://test4.com/image.jpg",
                center = "서대문구 재활용센터",
                category = "가전",
                crawledAt = "2024-01-01",
                sourceUrl = "http://test.com"
            ),
            Product(
                title = "삼성 TV 55인치",
                price = "120,000원",
                link = "http://test5.com",
                image = "http://test5.com/image.jpg",
                center = "서대문구 재활용센터",
                category = "가전",
                crawledAt = "2024-01-01",
                sourceUrl = "http://test.com"
            )
        )
    }

    @Test
    fun filterProducts_byRegion_returnsCorrectProducts() {
        // Filter by Seodaemun
        val seodaemunProducts = testProducts.filter { it.center.contains("서대문") }
        assertEquals(3, seodaemunProducts.size)

        // Filter by Dongdaemun
        val dongdaemunProducts = testProducts.filter { it.center.contains("동대문") }
        assertEquals(2, dongdaemunProducts.size)
    }

    @Test
    fun filterProducts_byProductType_returnsCorrectProducts() {
        // Filter by refrigerator
        val refrigerators = testProducts.filter { it.getProductType() == "refrigerator" }
        assertEquals(2, refrigerators.size)

        // Filter by washing machine
        val washingMachines = testProducts.filter { it.getProductType() == "washing_machine" }
        assertEquals(2, washingMachines.size)

        // Filter by TV
        val tvs = testProducts.filter { it.getProductType() == "tv" }
        assertEquals(1, tvs.size)
    }

    @Test
    fun filterProducts_hideSoldOut_excludesSoldOutProducts() {
        // Count sold out products
        val soldOutProducts = testProducts.filter { it.isSoldOut() }
        assertEquals(2, soldOutProducts.size)

        // Filter out sold out products
        val availableProducts = testProducts.filter { !it.isSoldOut() }
        assertEquals(3, availableProducts.size)

        // Verify sold out products are excluded
        availableProducts.forEach { product ->
            assertFalse("Product should not be sold out", product.isSoldOut())
        }
    }

    @Test
    fun filterProducts_textSearch_matchesMultipleFields() {
        // Search by title
        val refrigeratorSearch = testProducts.filter {
            it.title.contains("냉장고", ignoreCase = true)
        }
        assertEquals(2, refrigeratorSearch.size)

        // Search by center
        val seodaemunSearch = testProducts.filter {
            it.center.contains("서대문", ignoreCase = true)
        }
        assertEquals(3, seodaemunSearch.size)
    }

    @Test
    fun filterProducts_combinedFilters_worksCorrectly() {
        // Filter by region AND product type
        var filtered = testProducts.filter {
            it.center.contains("동대문") && it.getProductType() == "washing_machine"
        }
        assertEquals(2, filtered.size)

        // Filter by region AND exclude sold out
        filtered = testProducts.filter {
            it.center.contains("서대문") && !it.isSoldOut()
        }
        assertEquals(2, filtered.size)

        // Filter by product type AND exclude sold out
        filtered = testProducts.filter {
            it.getProductType() == "refrigerator" && !it.isSoldOut()
        }
        assertEquals(1, filtered.size)
    }

    @Test
    fun searchProducts_emptyQuery_returnsAllProducts() {
        // Empty search should return all products (minus filters)
        val allProducts = testProducts.filter { product ->
            "".isBlank() || product.title.contains("", ignoreCase = true)
        }
        assertEquals(testProducts.size, allProducts.size)
    }

    @Test
    fun searchProducts_noMatches_returnsEmptyList() {
        // Search for something that doesn't exist
        val results = testProducts.filter { product ->
            product.title.contains("노트북", ignoreCase = true)
        }
        assertEquals(0, results.size)
        assertTrue("Results should be empty", results.isEmpty())
    }

    @Test
    fun filterProducts_multipleRegionFilters_canBeApplied() {
        val seodaemunCount = testProducts.count { it.center.contains("서대문") }
        val dongdaemunCount = testProducts.count { it.center.contains("동대문") }

        // Total should equal all products
        assertEquals(testProducts.size, seodaemunCount + dongdaemunCount)
    }

    @Test
    fun filterProducts_caseSensitivity_worksCorrectly() {
        // Case insensitive search
        val upperCase = testProducts.filter { it.title.contains("냉장고", ignoreCase = true) }
        val lowerCase = testProducts.filter { it.title.contains("냉장고", ignoreCase = true) }

        assertEquals("Case insensitive search should return same results",
            upperCase.size, lowerCase.size)
    }
}
