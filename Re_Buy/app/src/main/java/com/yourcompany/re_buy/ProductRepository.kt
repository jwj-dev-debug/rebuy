package com.yourcompany.re_buy

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class ProductRepository(private val context: Context) {

    private var allProducts: List<Product> = emptyList()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        try {
            val inputStream = context.resources.openRawResource(R.raw.products)
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<Product>>() {}.type
            allProducts = Gson().fromJson(reader, type)
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
            allProducts = emptyList()
        }
    }

    fun getAllProducts(): List<Product> {
        return allProducts
    }

    fun getRandomProducts(count: Int = 10): List<Product> {
        return allProducts.shuffled().take(count)
    }

    fun searchProducts(
        query: String = "",
        region: String = "all",
        category: String = "all",
        hideSoldOut: Boolean = false
    ): List<Product> {
        var filteredProducts = allProducts

        // Filter by region
        if (region != "all") {
            filteredProducts = filteredProducts.filter { product ->
                when (region) {
                    "dongdaemun" -> product.center.contains("동대문")
                    "seodaemun" -> product.center.contains("서대문")
                    else -> true
                }
            }
        }

        // Filter by category using the category field
        if (category != "all") {
            filteredProducts = filteredProducts.filter { product ->
                product.category == category
            }
        }

        // Filter by search query
        if (query.isNotBlank()) {
            filteredProducts = filteredProducts.filter { product ->
                product.title.contains(query, ignoreCase = true) ||
                        product.center.contains(query, ignoreCase = true) ||
                        product.category.contains(query, ignoreCase = true)
            }
        }

        // Filter out sold-out products if requested
        if (hideSoldOut) {
            filteredProducts = filteredProducts.filter { product ->
                !product.isSoldOut()
            }
        }

        return filteredProducts
    }

    fun getProductById(link: String): Product? {
        return allProducts.find { it.link == link }
    }
}
