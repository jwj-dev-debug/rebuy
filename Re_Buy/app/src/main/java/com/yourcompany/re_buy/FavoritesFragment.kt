package com.yourcompany.re_buy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourcompany.re_buy.adapters.CommunityPostAdapter
import com.yourcompany.re_buy.databinding.FragmentFavoritesBinding
import com.yourcompany.re_buy.repository.CommunityRepository
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val repository = CommunityRepository()
    private lateinit var postAdapter: CommunityPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadFavoritePosts()
    }

    private fun setupRecyclerView() {
        postAdapter = CommunityPostAdapter(
            posts = emptyList(),
            onPostClick = { post ->
                val intent = Intent(requireContext(), PostDetailActivity::class.java)
                intent.putExtra("POST_ID", post.id)
                startActivity(intent)
            },
            onFavoriteClick = { post ->
                toggleFavorite(post)
            },
            lifecycleOwner = viewLifecycleOwner
        )

        binding.rvFavorites.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    private fun loadFavoritePosts() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = repository.getFavoritedPosts()

            binding.progressBar.visibility = View.GONE

            result.onSuccess { posts ->
                if (posts.isEmpty()) {
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.rvFavorites.visibility = View.GONE
                } else {
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.rvFavorites.visibility = View.VISIBLE
                    postAdapter.updatePosts(posts)
                }
            }.onFailure { e ->
                Toast.makeText(
                    requireContext(),
                    "즐겨찾기 목록을 불러올 수 없습니다: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.rvFavorites.visibility = View.GONE
            }
        }
    }

    private fun toggleFavorite(post: com.yourcompany.re_buy.models.CommunityPost) {
        lifecycleScope.launch {
            val result = repository.toggleFavoritePost(post)
            result.onSuccess {
                // Reload to update list
                loadFavoritePosts()
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
        // Reload favorites when returning to this fragment
        loadFavoritePosts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
