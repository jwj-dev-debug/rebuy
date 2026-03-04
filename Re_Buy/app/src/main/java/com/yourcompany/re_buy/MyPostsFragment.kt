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
import com.yourcompany.re_buy.databinding.FragmentMyPostsBinding
import com.yourcompany.re_buy.repository.CommunityRepository
import kotlinx.coroutines.launch

class MyPostsFragment : Fragment() {
    private var _binding: FragmentMyPostsBinding? = null
    private val binding get() = _binding!!
    private val repository = CommunityRepository()
    private lateinit var postAdapter: CommunityPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadMyPosts()
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

        binding.rvMyPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    private fun loadMyPosts() {
        android.util.Log.d("MyPostsFragment", "Loading my posts...")
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = repository.getMyPosts()

            binding.progressBar.visibility = View.GONE

            result.onSuccess { posts ->
                android.util.Log.d("MyPostsFragment", "Successfully loaded ${posts.size} posts")
                if (posts.isEmpty()) {
                    android.util.Log.d("MyPostsFragment", "No posts found, showing empty state")
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.rvMyPosts.visibility = View.GONE
                } else {
                    android.util.Log.d("MyPostsFragment", "Displaying ${posts.size} posts")
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.rvMyPosts.visibility = View.VISIBLE
                    postAdapter.updatePosts(posts)
                }
            }.onFailure { e ->
                android.util.Log.e("MyPostsFragment", "Failed to load posts: ${e.message}", e)
                Toast.makeText(
                    requireContext(),
                    "게시글을 불러올 수 없습니다: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.rvMyPosts.visibility = View.GONE
            }
        }
    }

    private fun toggleFavorite(post: com.yourcompany.re_buy.models.CommunityPost) {
        lifecycleScope.launch {
            val result = repository.toggleFavoritePost(post)
            result.onSuccess {
                // Reload to update favorite status
                loadMyPosts()
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
        // Reload posts when returning to this fragment
        loadMyPosts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
