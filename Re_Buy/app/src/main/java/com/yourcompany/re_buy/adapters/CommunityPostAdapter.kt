package com.yourcompany.re_buy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.yourcompany.re_buy.R
import com.yourcompany.re_buy.databinding.ItemCommunityPostBinding
import com.yourcompany.re_buy.models.CommunityPost
import com.yourcompany.re_buy.repository.CommunityRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class CommunityPostAdapter(
    private var posts: List<CommunityPost> = emptyList(),
    private val onPostClick: (CommunityPost) -> Unit,
    private val onLikeClick: ((CommunityPost) -> Unit)? = null,
    private val onAuthorClick: ((CommunityPost) -> Unit)? = null,
    private val onFavoriteClick: ((CommunityPost) -> Unit)? = null,
    private val lifecycleOwner: LifecycleOwner? = null
) : RecyclerView.Adapter<CommunityPostAdapter.PostViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val repository = CommunityRepository()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)

    inner class PostViewHolder(private val binding: ItemCommunityPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: CommunityPost) {
            binding.tvTitle.text = post.title
            binding.tvContent.text = post.content
            binding.tvAuthor.text = post.authorName
            binding.tvRegion.text = post.getRegionDisplayName()
            binding.tvTimestamp.text = post.createdAt?.let { dateFormat.format(it) } ?: ""

            binding.tvLikeCount.text = "${post.likeCount}"
            binding.tvCommentCount.text = "${post.commentCount}"

            // Show like button filled if user has liked
            val currentUser = auth.currentUser
            if (currentUser != null && post.isLikedByUser(currentUser.uid)) {
                binding.btnLike.setColorFilter(binding.root.context.getColor(R.color.green_primary))
            } else {
                binding.btnLike.clearColorFilter()
            }

            // Load first image if available
            if (post.imageUrls.isNotEmpty()) {
                binding.ivPostImage.visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(post.imageUrls[0])
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.ivPostImage)
            } else {
                binding.ivPostImage.visibility = View.GONE
            }

            // Check if post is favorited and update button
            if (onFavoriteClick != null && lifecycleOwner != null) {
                lifecycleOwner.lifecycleScope.launch {
                    val result = repository.isPostFavorited(post.id)
                    result.onSuccess { isFavorited ->
                        updateFavoriteIcon(isFavorited)
                    }
                }
            }

            // Click listeners
            binding.root.setOnClickListener { onPostClick(post) }
            onLikeClick?.let { binding.btnLike.setOnClickListener { it(post) } }
            onAuthorClick?.let { binding.tvAuthor.setOnClickListener { it(post) } }
            onFavoriteClick?.let { binding.btnFavorite.setOnClickListener { it(post) } }
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemCommunityPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<CommunityPost>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    /**
     * Refresh favorite states for all currently displayed posts
     * Call this when returning from My Page or after favorite changes
     */
    fun refreshFavoriteStates() {
        android.util.Log.d("CommunityPostAdapter", "Refreshing favorite states for ${posts.size} posts")
        notifyDataSetChanged() // This will trigger onBindViewHolder which will check favorite status
    }
}
