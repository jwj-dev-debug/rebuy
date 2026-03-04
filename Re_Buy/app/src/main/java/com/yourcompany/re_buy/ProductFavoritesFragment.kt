package com.yourcompany.re_buy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourcompany.re_buy.databinding.FragmentProductFavoritesBinding
import com.yourcompany.re_buy.repository.FavoritesRepository
import kotlinx.coroutines.launch

class ProductFavoritesFragment : Fragment() {
    private var _binding: FragmentProductFavoritesBinding? = null
    private val binding get() = _binding!!
    private val repository = FavoritesRepository()
    private lateinit var adapter: ProductFavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadFavoriteProducts()
    }

    private fun setupRecyclerView() {
        adapter = ProductFavoritesAdapter(
            favorites = emptyList(),
            onItemClick = { favorite ->
                // Open product link in browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(favorite.itemId))
                startActivity(intent)
            },
            onRemoveClick = { favorite ->
                removeFavorite(favorite.itemId)
            }
        )

        binding.rvFavoriteProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ProductFavoritesFragment.adapter
        }
    }

    private fun loadFavoriteProducts() {
        android.util.Log.d("ProductFavoritesFragment", "Loading favorite products...")
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = repository.getFavoritedProducts()

            binding.progressBar.visibility = View.GONE

            result.onSuccess { favorites ->
                android.util.Log.d("ProductFavoritesFragment", "Successfully loaded ${favorites.size} favorites")
                if (favorites.isEmpty()) {
                    android.util.Log.d("ProductFavoritesFragment", "No favorites found, showing empty state")
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.rvFavoriteProducts.visibility = View.GONE
                } else {
                    android.util.Log.d("ProductFavoritesFragment", "Displaying ${favorites.size} favorites")
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.rvFavoriteProducts.visibility = View.VISIBLE
                    adapter.updateFavorites(favorites)
                }
            }.onFailure { e ->
                android.util.Log.e("ProductFavoritesFragment", "Failed to load favorites: ${e.message}", e)
                Toast.makeText(
                    requireContext(),
                    "즐겨찾기 목록을 불러올 수 없습니다: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.rvFavoriteProducts.visibility = View.GONE
            }
        }
    }

    private fun removeFavorite(productLink: String) {
        lifecycleScope.launch {
            val result = repository.removeProductFromFavorites(productLink)
            result.onSuccess {
                loadFavoriteProducts() // Reload list
                Toast.makeText(requireContext(), "즐겨찾기에서 제거되었습니다", Toast.LENGTH_SHORT).show()
            }.onFailure { e ->
                Toast.makeText(
                    requireContext(),
                    "오류: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadFavoriteProducts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
