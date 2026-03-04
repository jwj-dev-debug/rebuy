package com.yourcompany.re_buy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourcompany.re_buy.databinding.ItemFavoriteProductBinding
import com.yourcompany.re_buy.models.Favorite

class ProductFavoritesAdapter(
    private var favorites: List<Favorite> = emptyList(),
    private val onItemClick: (Favorite) -> Unit,
    private val onRemoveClick: (Favorite) -> Unit
) : RecyclerView.Adapter<ProductFavoritesAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(private val binding: ItemFavoriteProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(favorite: Favorite) {
            binding.tvProductTitle.text = favorite.itemTitle

            // Load product image
            Glide.with(binding.root.context)
                .load(favorite.itemImage)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(binding.ivProductImage)

            // Set click listener
            binding.root.setOnClickListener {
                onItemClick(favorite)
            }

            // Set remove button click listener
            binding.btnRemove.setOnClickListener {
                onRemoveClick(favorite)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favorites[position])
    }

    override fun getItemCount(): Int = favorites.size

    fun updateFavorites(newFavorites: List<Favorite>) {
        favorites = newFavorites
        notifyDataSetChanged()
    }
}
