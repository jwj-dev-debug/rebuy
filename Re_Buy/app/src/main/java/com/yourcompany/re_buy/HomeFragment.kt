package com.yourcompany.re_buy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourcompany.re_buy.databinding.FragmentHomeBinding
import com.yourcompany.re_buy.repository.CommunityRepository
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var productRepository: ProductRepository
    private lateinit var productAdapter: ProductAdapter
    private val communityRepository = CommunityRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ProductRepository
        productRepository = ProductRepository(requireContext())

        // Setup RecyclerView with ProductAdapter in Linear layout (scrollable within NestedScrollView)
        productAdapter = ProductAdapter(
            lifecycleOwner = viewLifecycleOwner
        )
        binding.rvHomeProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productAdapter
            isNestedScrollingEnabled = false // Disable nested scrolling for proper scroll behavior
        }

        // Load random products
        loadRandomProducts()

        // Load latest community post for preview
        loadLatestCommunityPost()

        // Notices card - can be expanded later to show list of notices
        // For now, it shows a static welcome message

        // View all community link click - switch to Community tab
        binding.tvViewAllCommunity.setOnClickListener {
            (activity as? MainActivity)?.switchToTab(2)
        }
    }

    private fun loadRandomProducts() {
        // Get random products from repository
        val randomProducts = productRepository.getRandomProducts(10)
        productAdapter.updateProducts(randomProducts)
    }

    private fun loadLatestCommunityPost() {
        lifecycleScope.launch {
            val result = communityRepository.getAllPosts()
            result.onSuccess { posts ->
                if (posts.isNotEmpty()) {
                    val latestPost = posts.first()
                    // Update preview card with latest post
                    binding.tvCommunityPostTitle.text = latestPost.title
                    binding.tvCommunityPostPreview.text = latestPost.content
                    binding.tvCommunityPostAuthor.text = latestPost.authorName

                    // Make card clickable to open post detail
                    binding.cardCommunityPreview.setOnClickListener {
                        val intent = Intent(requireContext(), PostDetailActivity::class.java)
                        intent.putExtra("POST_ID", latestPost.id)
                        startActivity(intent)
                    }
                } else {
                    // No posts yet, make card navigate to community tab
                    binding.cardCommunityPreview.setOnClickListener {
                        (activity as? MainActivity)?.switchToTab(2)
                    }
                }
            }.onFailure {
                // On error, make card navigate to community tab
                binding.cardCommunityPreview.setOnClickListener {
                    (activity as? MainActivity)?.switchToTab(2)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload community post when returning to this fragment
        loadLatestCommunityPost()
        // Refresh favorite states in case they changed in My Page
        productAdapter.refreshFavoriteStates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}