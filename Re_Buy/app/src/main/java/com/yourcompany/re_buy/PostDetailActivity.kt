package com.yourcompany.re_buy

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.yourcompany.re_buy.adapters.CommentAdapter
import com.yourcompany.re_buy.databinding.ActivityPostDetailBinding
import com.yourcompany.re_buy.models.Comment
import com.yourcompany.re_buy.models.CommunityPost
import com.yourcompany.re_buy.repository.CommunityRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class PostDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostDetailBinding
    private val auth = FirebaseAuth.getInstance()
    private val repository = CommunityRepository()
    private lateinit var commentAdapter: CommentAdapter
    private var currentPost: CommunityPost? = null
    private var postId: String = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getStringExtra("POST_ID") ?: run {
            Toast.makeText(this, "게시글을 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupCommentRecyclerView()
        setupListeners()
        loadPost()
        loadComments()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "게시글"
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupCommentRecyclerView() {
        commentAdapter = CommentAdapter(
            comments = emptyList(),
            onDeleteClick = { comment ->
                deleteComment(comment)
            }
        )

        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(this@PostDetailActivity)
            adapter = commentAdapter
        }
    }

    private fun setupListeners() {
        binding.btnLike.setOnClickListener {
            toggleLike()
        }

        binding.btnFavorite.setOnClickListener {
            toggleFavorite()
        }

        binding.btnAddComment.setOnClickListener {
            addComment()
        }
    }

    private fun loadPost() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = repository.getPostById(postId)

            binding.progressBar.visibility = View.GONE

            result.onSuccess { post ->
                currentPost = post
                displayPost(post)
                updateFavoriteButton()
                // Recreate options menu now that we have the post data
                invalidateOptionsMenu()
            }.onFailure { e ->
                Toast.makeText(
                    this@PostDetailActivity,
                    "게시글을 불러올 수 없습니다: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun displayPost(post: CommunityPost) {
        binding.tvTitle.text = post.title
        binding.tvContent.text = post.content
        binding.tvAuthor.text = post.authorName
        binding.tvRegion.text = post.getRegionDisplayName()
        binding.tvTimestamp.text = post.createdAt?.let { dateFormat.format(it) } ?: ""
        binding.tvLikeCount.text = "${post.likeCount}"
        binding.tvCommentCount.text = "${post.commentCount}"

        // Update like button state
        val currentUser = auth.currentUser
        if (currentUser != null && post.isLikedByUser(currentUser.uid)) {
            binding.btnLike.setColorFilter(getColor(R.color.green_primary))
        } else {
            binding.btnLike.clearColorFilter()
        }

        // Load images if available
        if (post.imageUrls.isNotEmpty()) {
            binding.imageContainer.visibility = View.VISIBLE

            if (post.imageUrls.size > 0) {
                binding.ivPostImage1.visibility = View.VISIBLE
                Glide.with(this).load(post.imageUrls[0]).into(binding.ivPostImage1)
            }
            if (post.imageUrls.size > 1) {
                binding.ivPostImage2.visibility = View.VISIBLE
                Glide.with(this).load(post.imageUrls[1]).into(binding.ivPostImage2)
            }
            if (post.imageUrls.size > 2) {
                binding.ivPostImage3.visibility = View.VISIBLE
                Glide.with(this).load(post.imageUrls[2]).into(binding.ivPostImage3)
            }
        } else {
            binding.imageContainer.visibility = View.GONE
        }

        // Show comment input only for logged in users
        if (auth.currentUser != null) {
            binding.commentInputLayout.visibility = View.VISIBLE
        } else {
            binding.commentInputLayout.visibility = View.GONE
        }
    }

    private fun loadComments() {
        lifecycleScope.launch {
            val result = repository.getCommentsForPost(postId)

            result.onSuccess { comments ->
                android.util.Log.d("PostDetail", "Loaded ${comments.size} comments for post $postId")
                if (comments.isEmpty()) {
                    binding.tvNoComments.visibility = View.VISIBLE
                    binding.rvComments.visibility = View.GONE
                } else {
                    binding.tvNoComments.visibility = View.GONE
                    binding.rvComments.visibility = View.VISIBLE
                    commentAdapter.updateComments(comments)
                }
            }.onFailure { e ->
                android.util.Log.e("PostDetail", "Failed to load comments for post $postId", e)
                Toast.makeText(
                    this@PostDetailActivity,
                    "댓글을 불러올 수 없습니다: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun toggleLike() {
        if (auth.currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val result = repository.toggleLikePost(postId)
            result.onSuccess {
                // Reload post to update like count
                loadPost()
            }.onFailure { e ->
                Toast.makeText(this@PostDetailActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleFavorite() {
        if (auth.currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            return
        }

        val post = currentPost
        if (post == null) {
            Toast.makeText(this, "게시글을 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val result = repository.toggleFavoritePost(post)
            result.onSuccess {
                Toast.makeText(this@PostDetailActivity, "즐겨찾기가 업데이트되었습니다", Toast.LENGTH_SHORT).show()
                updateFavoriteButton()
            }.onFailure { e ->
                Toast.makeText(this@PostDetailActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFavoriteButton() {
        lifecycleScope.launch {
            val result = repository.isPostFavorited(postId)
            result.onSuccess { isFavorited ->
                if (isFavorited) {
                    binding.btnFavorite.setImageResource(android.R.drawable.star_big_on)
                    binding.btnFavorite.setColorFilter(
                        getColor(R.color.green_primary),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                } else {
                    binding.btnFavorite.setImageResource(android.R.drawable.star_big_off)
                    binding.btnFavorite.clearColorFilter()
                }
            }
        }
    }

    private fun addComment() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            android.util.Log.e("PostDetail", "Cannot add comment - user not logged in")
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            return
        }

        val commentText = binding.etComment.text.toString().trim()
        if (commentText.isEmpty()) {
            binding.etComment.error = "댓글을 입력하세요"
            return
        }

        android.util.Log.d("PostDetail", "Adding comment to post $postId: $commentText")
        binding.btnAddComment.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Get user name from Firestore
                android.util.Log.d("PostDetail", "Fetching user name for uid: ${currentUser.uid}")
                val userDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val userName = userDoc.getString("name")
                    ?: currentUser.displayName
                    ?: currentUser.email?.substringBefore("@")
                    ?: "익명"

                android.util.Log.d("PostDetail", "User name resolved: $userName")

                val comment = Comment(
                    postId = postId,
                    authorUid = currentUser.uid,
                    authorName = userName,
                    authorEmail = currentUser.email ?: "",
                    content = commentText
                )

                android.util.Log.d("PostDetail", "Calling repository.addComment()")
                val result = repository.addComment(comment)

                result.onSuccess {
                    android.util.Log.d("PostDetail", "Comment added successfully")
                    binding.etComment.text?.clear()
                    Toast.makeText(this@PostDetailActivity, "댓글이 등록되었습니다", Toast.LENGTH_SHORT).show()
                    loadComments()
                    loadPost() // Reload to update comment count
                }.onFailure { e ->
                    android.util.Log.e("PostDetail", "Failed to add comment: ${e.message}", e)
                    Toast.makeText(this@PostDetailActivity, "댓글 등록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("PostDetail", "Error adding comment: ${e.message}", e)
                Toast.makeText(this@PostDetailActivity, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnAddComment.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun deleteComment(comment: Comment) {
        AlertDialog.Builder(this)
            .setTitle("댓글 삭제")
            .setMessage("이 댓글을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                lifecycleScope.launch {
                    val result = repository.deleteComment(comment.id, postId)
                    result.onSuccess {
                        Toast.makeText(this@PostDetailActivity, "댓글이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                        loadComments()
                        loadPost() // Reload to update comment count
                    }.onFailure { e ->
                        Toast.makeText(this@PostDetailActivity, "삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Only show menu if user owns this post
        val currentUser = auth.currentUser
        val post = currentPost

        android.util.Log.d("PostDetail", "onCreateOptionsMenu called - user: ${currentUser?.uid}, post: ${post?.id}, author: ${post?.authorUid}")

        if (currentUser != null && post != null && post.authorUid == currentUser.uid) {
            android.util.Log.d("PostDetail", "User owns post, inflating menu")
            menuInflater.inflate(R.menu.menu_post_detail, menu)
            return true
        } else {
            android.util.Log.d("PostDetail", "User doesn't own post or post not loaded yet")
            return super.onCreateOptionsMenu(menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                editPost()
                true
            }
            R.id.action_delete -> {
                deletePost()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editPost() {
        val intent = Intent(this, CreatePostActivity::class.java)
        intent.putExtra("POST_ID", postId)
        startActivity(intent)
    }

    private fun deletePost() {
        AlertDialog.Builder(this)
            .setTitle("게시글 삭제")
            .setMessage("이 게시글을 삭제하시겠습니까? 모든 댓글도 함께 삭제됩니다.")
            .setPositiveButton("삭제") { _, _ ->
                lifecycleScope.launch {
                    val result = repository.deletePost(postId)
                    result.onSuccess {
                        Toast.makeText(this@PostDetailActivity, "게시글이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                        finish()
                    }.onFailure { e ->
                        Toast.makeText(this@PostDetailActivity, "삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Reload post when returning (in case it was edited)
        loadPost()
        loadComments()
    }
}
