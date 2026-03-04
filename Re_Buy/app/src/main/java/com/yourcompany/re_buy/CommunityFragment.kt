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
import com.google.firebase.auth.FirebaseAuth
import com.yourcompany.re_buy.adapters.CommunityPostAdapter
import com.yourcompany.re_buy.databinding.FragmentCommunityBinding
import com.yourcompany.re_buy.models.CommunityPost
import com.yourcompany.re_buy.repository.CommunityRepository
import kotlinx.coroutines.launch

class CommunityFragment : Fragment() {
    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val repository = CommunityRepository()
    private lateinit var postAdapter: CommunityPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFAB()
        loadPosts()
    }

    private fun setupRecyclerView() {
        postAdapter = CommunityPostAdapter(
            posts = emptyList(),
            onPostClick = { post ->
                // Navigate to PostDetailActivity
                val intent = Intent(requireContext(), PostDetailActivity::class.java)
                intent.putExtra("POST_ID", post.id)
                startActivity(intent)
            },
            onLikeClick = { post ->
                toggleLike(post)
            },
            onAuthorClick = { post ->
                // TODO: Show option to send private message or view profile
                Toast.makeText(context, "작성자: ${post.authorName}", Toast.LENGTH_SHORT).show()
            },
            onFavoriteClick = { post ->
                toggleFavorite(post)
            },
            lifecycleOwner = viewLifecycleOwner
        )

        binding.rvCommunityPosts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }
    }

    private fun setupFAB() {
        binding.fabNewPost.setOnClickListener {
            if (auth.currentUser != null) {
                // Navigate to CreatePostActivity
                val intent = Intent(requireContext(), CreatePostActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                // Optionally navigate to LoginActivity
            }
        }
    }

    private fun loadPosts() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = repository.getAllPosts()

            binding.progressBar.visibility = View.GONE

            result.onSuccess { posts ->
                if (posts.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvCommunityPosts.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvCommunityPosts.visibility = View.VISIBLE
                    postAdapter.updatePosts(posts)
                }
            }.onFailure { e ->
                Toast.makeText(
                    context,
                    "게시글을 불러올 수 없습니다: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun toggleLike(post: CommunityPost) {
        if (auth.currentUser == null) {
            Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val result = repository.toggleLikePost(post.id)
            result.onSuccess {
                // Reload posts to update like counts
                loadPosts()
            }.onFailure { e ->
                Toast.makeText(context, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleFavorite(post: CommunityPost) {
        if (auth.currentUser == null) {
            Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val result = repository.toggleFavoritePost(post)
            result.onSuccess {
                Toast.makeText(context, "즐겨찾기가 업데이트되었습니다", Toast.LENGTH_SHORT).show()
                // Optionally reload to update favorite status
                loadPosts()
            }.onFailure { e ->
                Toast.makeText(context, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload posts when returning to this fragment
        loadPosts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
