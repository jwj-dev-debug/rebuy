package com.yourcompany.re_buy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourcompany.re_buy.databinding.ItemPurchaseBinding
import com.yourcompany.re_buy.models.Purchase
import java.text.SimpleDateFormat
import java.util.Locale

class PurchaseAdapter(
    private var purchases: List<Purchase>,
    private val onDeleteClick: (Purchase) -> Unit
) : RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

    inner class PurchaseViewHolder(val binding: ItemPurchaseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(purchase: Purchase) {
            binding.tvProductTitle.text = purchase.productTitle
            binding.tvPurchaseDate.text = dateFormat.format(purchase.purchaseDate)

            // Display environmental impact
            binding.tvCarbonImpact.text = "CO₂ ${String.format("%.0f", purchase.carbonSavedKg)}kg"
            binding.tvWaterImpact.text = "물 ${String.format("%.0f", purchase.waterSavedLiters)}L"

            // Load product image
            Glide.with(binding.root.context)
                .load(purchase.productImage)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivProductImage)

            // Delete button
            binding.btnDelete.setOnClickListener {
                onDeleteClick(purchase)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val binding = ItemPurchaseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PurchaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        holder.bind(purchases[position])
    }

    override fun getItemCount(): Int = purchases.size

    fun updatePurchases(newPurchases: List<Purchase>) {
        purchases = newPurchases
        notifyDataSetChanged()
    }
}
