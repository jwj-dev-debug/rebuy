package com.yourcompany.re_buy

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.yourcompany.re_buy.databinding.ItemProductBinding
import com.yourcompany.re_buy.repository.EnvironmentalImpactRepository
import com.yourcompany.re_buy.repository.FavoritesRepository
import kotlinx.coroutines.launch

class ProductAdapter(
    private var products: List<Product> = emptyList(),
    private val lifecycleOwner: LifecycleOwner? = null,
    private val onFavoriteClick: ((Product, Boolean) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val favoritesRepository = FavoritesRepository()
    private val impactRepository = EnvironmentalImpactRepository()
    private val auth = FirebaseAuth.getInstance()
    private val favoriteStates = mutableMapOf<String, Boolean>()
    private val purchasedStates = mutableMapOf<String, Boolean>()

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductTitle.text = product.title
            binding.tvProductPrice.text = product.price
            binding.tvProductCenter.text = product.center

            // Show sold out badge if the product is sold out
            if (product.isSoldOut()) {
                binding.tvSoldOut.visibility = View.VISIBLE
                binding.tvProductPrice.alpha = 0.5f
            } else {
                binding.tvSoldOut.visibility = View.GONE
                binding.tvProductPrice.alpha = 1.0f
            }

            // Load product image with Glide
            Glide.with(binding.root.context)
                .load(product.image)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .timeout(30000) // 30 second timeout
                .into(binding.ivProductImage)

            // Set click listener to open product detail page
            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, ProductDetailActivity::class.java)
                intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_LINK, product.link)
                binding.root.context.startActivity(intent)
            }

            // Set long-press listener to mark as purchased
            binding.root.setOnLongClickListener {
                showPurchaseDialog(product)
                true
            }

            // Setup favorite button
            setupFavoriteButton(product)

            // Check if already purchased
            checkPurchaseStatus(product)
        }

        private fun setupFavoriteButton(product: Product) {
            // Check if user is logged in
            if (auth.currentUser == null) {
                binding.btnFavorite.visibility = View.GONE
                return
            }

            binding.btnFavorite.visibility = View.VISIBLE

            // Load favorite state
            lifecycleOwner?.lifecycleScope?.launch {
                val result = favoritesRepository.isProductFavorited(product.link)
                result.onSuccess { isFavorited ->
                    favoriteStates[product.link] = isFavorited
                    updateFavoriteIcon(isFavorited)
                }.onFailure { error ->
                    android.util.Log.e("ProductAdapter", "Failed to check favorite status: ${error.message}", error)
                    // Default to not favorited if check fails
                    updateFavoriteIcon(false)
                }
            }

            // Set click listener
            binding.btnFavorite.setOnClickListener {
                lifecycleOwner?.lifecycleScope?.launch {
                    android.util.Log.d("ProductAdapter", "Toggling favorite for: ${product.title}")
                    val result = favoritesRepository.toggleProductFavorite(product)
                    result.onSuccess { isFavorited ->
                        android.util.Log.d("ProductAdapter", "Favorite toggled successfully: $isFavorited")
                        favoriteStates[product.link] = isFavorited
                        updateFavoriteIcon(isFavorited)
                        onFavoriteClick?.invoke(product, isFavorited)
                        // Show success message
                        android.widget.Toast.makeText(
                            binding.root.context,
                            if (isFavorited) "즐겨찾기에 추가되었습니다" else "즐겨찾기에서 제거되었습니다",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }.onFailure { error ->
                        android.util.Log.e("ProductAdapter", "Failed to toggle favorite: ${error.message}", error)
                        // Show detailed error to user
                        android.widget.Toast.makeText(
                            binding.root.context,
                            "즐겨찾기 오류: ${error.message}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        private fun updateFavoriteIcon(isFavorited: Boolean) {
            if (isFavorited) {
                binding.btnFavorite.setImageResource(android.R.drawable.star_big_on)
                binding.btnFavorite.setColorFilter(
                    binding.root.context.getColor(R.color.green_primary),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                binding.btnFavorite.setImageResource(android.R.drawable.star_big_off)
                binding.btnFavorite.clearColorFilter()
            }
        }

        private fun checkPurchaseStatus(product: Product) {
            // Check if product was already purchased
            lifecycleOwner?.lifecycleScope?.launch {
                val result = impactRepository.isProductPurchased(product.link)
                result.onSuccess { isPurchased ->
                    purchasedStates[product.link] = isPurchased
                    if (isPurchased) {
                        // Add a subtle indicator that this was purchased
                        binding.root.alpha = 0.7f
                        binding.tvPurchaseHint.visibility = View.GONE
                    } else {
                        binding.root.alpha = 1.0f
                        binding.tvPurchaseHint.visibility = View.VISIBLE
                    }
                }
            }
        }

        private fun showPurchaseDialog(product: Product) {
            // Check if user is logged in
            if (auth.currentUser == null) {
                Toast.makeText(
                    binding.root.context,
                    "구매를 기록하려면 로그인이 필요합니다",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            // Check if already purchased
            if (purchasedStates[product.link] == true) {
                Toast.makeText(
                    binding.root.context,
                    "이미 구매한 제품입니다",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            // Show confirmation dialog
            AlertDialog.Builder(binding.root.context)
                .setTitle("구매 기록")
                .setMessage("이 제품을 구매하셨나요?\n\n환경 영향 대시보드에 기록되고 환경 보호 통계에 반영됩니다.\n\n제품: ${product.title}")
                .setPositiveButton("구매 완료") { _, _ ->
                    markAsPurchased(product)
                }
                .setNegativeButton("취소", null)
                .show()
        }

        private fun markAsPurchased(product: Product) {
            lifecycleOwner?.lifecycleScope?.launch {
                android.util.Log.d("ProductAdapter", "Marking product as purchased: ${product.title}")

                val result = impactRepository.recordPurchase(product)

                result.onSuccess { purchase ->
                    android.util.Log.d("ProductAdapter", "Purchase recorded successfully")
                    purchasedStates[product.link] = true
                    binding.root.alpha = 0.7f
                    binding.tvPurchaseHint.visibility = View.GONE

                    // Show success message with environmental impact
                    Toast.makeText(
                        binding.root.context,
                        "구매가 기록되었습니다!\n\n탄소 ${purchase.carbonSavedKg}kg 절감\n물 ${purchase.waterSavedLiters}L 절약\n\n환경 영향 대시보드를 확인하세요!",
                        Toast.LENGTH_LONG
                    ).show()
                }.onFailure { error ->
                    android.util.Log.e("ProductAdapter", "Failed to record purchase: ${error.message}", error)
                    Toast.makeText(
                        binding.root.context,
                        "구매 기록 실패: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    /**
     * Refresh favorite states for all currently displayed products
     * Call this when returning from My Page or after favorite changes
     */
    fun refreshFavoriteStates() {
        android.util.Log.d("ProductAdapter", "Refreshing favorite states for ${products.size} products")
        favoriteStates.clear()
        notifyDataSetChanged() // This will trigger onBindViewHolder which will check favorite status
    }
}
