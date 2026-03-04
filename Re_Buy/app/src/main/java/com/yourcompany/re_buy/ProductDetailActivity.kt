package com.yourcompany.re_buy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.yourcompany.re_buy.databinding.ActivityProductDetailBinding
import com.yourcompany.re_buy.repository.EnvironmentalImpactRepository
import com.yourcompany.re_buy.repository.FavoritesRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var product: Product
    private lateinit var productRepository: ProductRepository
    private val favoritesRepository = FavoritesRepository()
    private val impactRepository = EnvironmentalImpactRepository()
    private val auth = FirebaseAuth.getInstance()
    private var isFavorited = false
    private var isPurchased = false

    companion object {
        const val EXTRA_PRODUCT_LINK = "product_link"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repository
        productRepository = ProductRepository(this)

        // Get product link from intent
        val productLink = intent.getStringExtra(EXTRA_PRODUCT_LINK)
        if (productLink == null) {
            Toast.makeText(this, "제품 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load product from repository
        product = productRepository.getProductById(productLink) ?: run {
            Toast.makeText(this, "제품을 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupButtons()
        checkFavoriteStatus()
        checkPurchaseStatus()
    }

    private fun setupUI() {
        // Set product information
        binding.tvProductTitle.text = product.title
        binding.tvProductPrice.text = product.price
        binding.tvProductCenter.text = product.center
        binding.tvProductCategory.text = product.category
        binding.tvProductCondition.text = product.condition.displayName

        // Show status badge
        if (product.isAvailable()) {
            binding.tvStatusBadge.visibility = View.VISIBLE
            binding.tvStatusBadge.text = "예약 가능"
            binding.tvStatusBadge.setBackgroundColor(getColor(R.color.green_primary))
        } else if (product.isReserved()) {
            binding.tvStatusBadge.visibility = View.VISIBLE
            binding.tvStatusBadge.text = "예약됨"
            binding.tvStatusBadge.setBackgroundColor(getColor(android.R.color.holo_orange_dark))
        }

        // Format and display date
        try {
            val dateString = product.getDateString()
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN)
            val date = if (dateString.isNotEmpty()) {
                inputFormat.parse(dateString)
            } else {
                Date()
            }
            binding.tvProductDate.text = date?.let { outputFormat.format(it) } ?: "날짜 정보 없음"
        } catch (e: Exception) {
            binding.tvProductDate.text = "날짜 정보 없음"
        }

        // Show sold out badge if applicable
        if (product.isSoldOut()) {
            binding.tvSoldOutBadge.visibility = View.VISIBLE
            binding.tvProductPrice.alpha = 0.5f
        }

        // Load product image
        Glide.with(this)
            .load(product.image)
            .placeholder(R.drawable.image_placeholder)
            .error(R.drawable.image_placeholder)
            .timeout(30000)
            .into(binding.ivProductImage)

        // Show buttons only if logged in
        if (auth.currentUser != null) {
            binding.btnFavorite.visibility = View.VISIBLE
            binding.btnMarkPurchased.visibility = View.VISIBLE
        }
    }

    private fun setupButtons() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Visit website button
        binding.btnVisitWebsite.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(product.link))
            startActivity(intent)
        }

        // Favorite button
        binding.btnFavorite.setOnClickListener {
            toggleFavorite()
        }

        // Mark as purchased button
        binding.btnMarkPurchased.setOnClickListener {
            showPurchaseDialog()
        }

        // Long press on image to mark as purchased
        binding.ivProductImage.setOnLongClickListener {
            if (auth.currentUser != null) {
                showPurchaseDialog()
            } else {
                Toast.makeText(this, "구매를 기록하려면 로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    private fun checkFavoriteStatus() {
        if (auth.currentUser == null) return

        lifecycleScope.launch {
            val result = favoritesRepository.isProductFavorited(product.link)
            result.onSuccess { favorited ->
                isFavorited = favorited
                updateFavoriteIcon()
            }
        }
    }

    private fun checkPurchaseStatus() {
        if (auth.currentUser == null) return

        lifecycleScope.launch {
            val result = impactRepository.isProductPurchased(product.link)
            result.onSuccess { purchased ->
                isPurchased = purchased
                if (purchased) {
                    binding.tvPurchaseHint.text = "✅ 구매 완료한 제품입니다"
                    binding.tvPurchaseHint.setBackgroundColor(getColor(R.color.green_primary))
                    binding.tvPurchaseHint.setTextColor(getColor(android.R.color.white))
                    binding.btnMarkPurchased.isEnabled = false
                    binding.btnMarkPurchased.text = "구매 완료"
                }
            }
        }
    }

    private fun toggleFavorite() {
        lifecycleScope.launch {
            val result = favoritesRepository.toggleProductFavorite(product)
            result.onSuccess { favorited ->
                isFavorited = favorited
                updateFavoriteIcon()
                Toast.makeText(
                    this@ProductDetailActivity,
                    if (favorited) "즐겨찾기에 추가되었습니다" else "즐겨찾기에서 제거되었습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure { error ->
                Toast.makeText(
                    this@ProductDetailActivity,
                    "즐겨찾기 오류: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateFavoriteIcon() {
        if (isFavorited) {
            binding.btnFavorite.setImageResource(android.R.drawable.star_big_on)
            binding.btnFavorite.setColorFilter(
                getColor(R.color.green_primary),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else {
            binding.btnFavorite.setImageResource(android.R.drawable.star_big_off)
            binding.btnFavorite.clearColorFilter()
        }
    }

    private fun showPurchaseDialog() {
        if (isPurchased) {
            Toast.makeText(this, "이미 구매한 제품입니다", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("구매 기록")
            .setMessage("이 제품을 구매하셨나요?\n\n환경 영향 대시보드에 기록되고 환경 보호 통계에 반영됩니다.\n\n제품: ${product.title}")
            .setPositiveButton("구매 완료") { _, _ ->
                markAsPurchased()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun markAsPurchased() {
        lifecycleScope.launch {
            val result = impactRepository.recordPurchase(product)

            result.onSuccess { purchase ->
                isPurchased = true
                binding.tvPurchaseHint.text = "✅ 구매 완료한 제품입니다"
                binding.tvPurchaseHint.setBackgroundColor(getColor(R.color.green_primary))
                binding.tvPurchaseHint.setTextColor(getColor(android.R.color.white))
                binding.btnMarkPurchased.isEnabled = false
                binding.btnMarkPurchased.text = "구매 완료"

                // Show success message with environmental impact
                Toast.makeText(
                    this@ProductDetailActivity,
                    "구매가 기록되었습니다!\n\n탄소 ${purchase.carbonSavedKg}kg 절감\n물 ${purchase.waterSavedLiters}L 절약\n\n환경 영향 대시보드를 확인하세요!",
                    Toast.LENGTH_LONG
                ).show()
            }.onFailure { error ->
                Toast.makeText(
                    this@ProductDetailActivity,
                    "구매 기록 실패: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}
