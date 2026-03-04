package com.yourcompany.re_buy

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourcompany.re_buy.adapters.PurchaseAdapter
import com.yourcompany.re_buy.databinding.ActivityImpactDashboardBinding
import com.yourcompany.re_buy.models.ImpactMetrics
import com.yourcompany.re_buy.models.Purchase
import com.yourcompany.re_buy.repository.EnvironmentalImpactRepository
import kotlinx.coroutines.launch

class ImpactDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImpactDashboardBinding
    private val repository = EnvironmentalImpactRepository()
    private lateinit var purchaseAdapter: PurchaseAdapter
    private var currentMetrics: ImpactMetrics? = null
    private var currentPurchases: List<Purchase> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImpactDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadImpactData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "환경 영향 대시보드"
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        purchaseAdapter = PurchaseAdapter(
            purchases = emptyList(),
            onDeleteClick = { purchase ->
                confirmDeletePurchase(purchase)
            }
        )

        binding.rvRecentPurchases.apply {
            layoutManager = LinearLayoutManager(this@ImpactDashboardActivity)
            adapter = purchaseAdapter
        }
    }

    private fun setupListeners() {
        binding.btnShareImpact.setOnClickListener {
            shareImpact()
        }
    }

    private fun loadImpactData() {
        android.util.Log.d("ImpactDashboard", "Loading environmental impact data")
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            // Load metrics
            val metricsResult = repository.getUserImpactMetrics()

            metricsResult.onSuccess { metrics ->
                android.util.Log.d("ImpactDashboard", "Loaded metrics: ${metrics.totalPurchases} purchases, ${metrics.totalCarbonSavedKg}kg CO2")
                currentMetrics = metrics
                displayMetrics(metrics)
            }.onFailure { e ->
                android.util.Log.e("ImpactDashboard", "Failed to load metrics: ${e.message}", e)
                Toast.makeText(
                    this@ImpactDashboardActivity,
                    "데이터를 불러올 수 없습니다: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Load purchases
            val purchasesResult = repository.getUserPurchases()

            purchasesResult.onSuccess { purchases ->
                android.util.Log.d("ImpactDashboard", "Loaded ${purchases.size} purchases")
                currentPurchases = purchases
                displayPurchases(purchases)
            }.onFailure { e ->
                android.util.Log.e("ImpactDashboard", "Failed to load purchases: ${e.message}", e)
            }

            binding.progressBar.visibility = View.GONE
        }
    }

    private fun displayMetrics(metrics: ImpactMetrics) {
        // Achievement level
        binding.tvAchievementLevel.text = metrics.getAchievementLevelDisplayName()
        binding.tvProgressText.text =
            "다음 레벨까지 ${String.format("%.0f", metrics.getProgressToNextLevel())}%"
        binding.progressAchievement.progress = metrics.getProgressToNextLevel().toInt()

        // Set badge color based on achievement level
        binding.ivAchievementBadge.setColorFilter(
            metrics.getAchievementColor(),
            PorterDuff.Mode.SRC_IN
        )

        // Environmental impact totals
        binding.tvCarbonSaved.text =
            "${String.format("%.1f", metrics.totalCarbonSavedKg)} kg CO₂"
        binding.tvWaterSaved.text =
            "${String.format("%.0f", metrics.totalWaterSavedLiters)} L"
        binding.tvWastePrevented.text =
            "${String.format("%.1f", metrics.totalWastePreventedKg)} kg"
        binding.tvTotalPurchases.text = "${metrics.totalPurchases}개"

        // Equivalent impact
        binding.tvTreesEquivalent.text =
            "🌳 ${String.format("%.1f", metrics.getEquivalentTreesPlanted())} 그루의 나무를 심은 효과"
        binding.tvCarMilesEquivalent.text =
            "🚗 ${String.format("%.1f", metrics.getEquivalentCarMilesNotDriven())} 마일을 운전하지 않은 효과"
    }

    private fun displayPurchases(purchases: List<Purchase>) {
        if (purchases.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvRecentPurchases.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvRecentPurchases.visibility = View.VISIBLE
            purchaseAdapter.updatePurchases(purchases)
        }
    }

    private fun confirmDeletePurchase(purchase: Purchase) {
        AlertDialog.Builder(this)
            .setTitle("구매 내역 삭제")
            .setMessage("이 구매 내역을 삭제하시겠습니까?\n환경 영향 통계에서도 제거됩니다.")
            .setPositiveButton("삭제") { _, _ ->
                deletePurchase(purchase)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deletePurchase(purchase: Purchase) {
        android.util.Log.d("ImpactDashboard", "Deleting purchase: ${purchase.id}")
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = repository.deletePurchase(purchase.id)

            binding.progressBar.visibility = View.GONE

            result.onSuccess {
                android.util.Log.d("ImpactDashboard", "Purchase deleted successfully")
                Toast.makeText(
                    this@ImpactDashboardActivity,
                    "구매 내역이 삭제되었습니다",
                    Toast.LENGTH_SHORT
                ).show()
                loadImpactData() // Reload data
            }.onFailure { e ->
                android.util.Log.e("ImpactDashboard", "Failed to delete purchase: ${e.message}", e)
                Toast.makeText(
                    this@ImpactDashboardActivity,
                    "삭제 실패: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun shareImpact() {
        val metrics = currentMetrics
        if (metrics == null) {
            Toast.makeText(this, "공유할 데이터가 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        android.util.Log.d("ImpactDashboard", "Sharing environmental impact")

        val shareMessage = metrics.getShareMessage()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            putExtra(Intent.EXTRA_SUBJECT, "Re:Buy 환경 영향 리포트")
        }

        startActivity(Intent.createChooser(shareIntent, "환경 영향 공유하기"))
    }

    override fun onResume() {
        super.onResume()
        // Reload data when returning to this activity
        loadImpactData()
    }
}
