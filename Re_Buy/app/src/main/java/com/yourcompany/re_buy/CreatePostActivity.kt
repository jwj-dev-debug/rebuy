package com.yourcompany.re_buy

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.yourcompany.re_buy.databinding.ActivityCreatePostBinding
import com.yourcompany.re_buy.models.CommunityPost
import com.yourcompany.re_buy.repository.CommunityRepository
import kotlinx.coroutines.launch

class CreatePostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreatePostBinding
    private val auth = FirebaseAuth.getInstance()
    private val repository = CommunityRepository()

    private var selectedImages = mutableListOf<Uri>()
    private var isEditMode = false
    private var editingPostId: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                if (selectedImages.size < 3) {
                    selectedImages.add(uri)
                    updateImagePreview()
                } else {
                    Toast.makeText(this, "최대 3장까지만 업로드할 수 있습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is authenticated
        if (auth.currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRegionSpinner()
        setupListeners()

        // Check if editing existing post
        intent.getStringExtra("POST_ID")?.let { postId ->
            isEditMode = true
            editingPostId = postId
            loadPostForEditing(postId)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (isEditMode) "게시글 수정" else "새 게시글 작성"
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRegionSpinner() {
        val regions = arrayOf("전체", "서대문구", "동대문구")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, regions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRegion.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnAddImage.setOnClickListener {
            if (selectedImages.size < 3) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                imagePickerLauncher.launch(intent)
            } else {
                Toast.makeText(this, "최대 3장까지만 업로드할 수 있습니다", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRemoveImage1.setOnClickListener { removeImage(0) }
        binding.btnRemoveImage2.setOnClickListener { removeImage(1) }
        binding.btnRemoveImage3.setOnClickListener { removeImage(2) }

        binding.btnPost.setOnClickListener {
            if (isEditMode) {
                updatePost()
            } else {
                createPost()
            }
        }
    }

    private fun removeImage(index: Int) {
        if (index < selectedImages.size) {
            selectedImages.removeAt(index)
            updateImagePreview()
        }
    }

    private fun updateImagePreview() {
        // Show/hide image preview containers
        binding.framePreview1.visibility = if (selectedImages.size > 0) View.VISIBLE else View.GONE
        binding.framePreview2.visibility = if (selectedImages.size > 1) View.VISIBLE else View.GONE
        binding.framePreview3.visibility = if (selectedImages.size > 2) View.VISIBLE else View.GONE

        // Set images using Glide
        if (selectedImages.size > 0) {
            Glide.with(this)
                .load(selectedImages[0])
                .centerCrop()
                .into(binding.ivPreview1)
        }
        if (selectedImages.size > 1) {
            Glide.with(this)
                .load(selectedImages[1])
                .centerCrop()
                .into(binding.ivPreview2)
        }
        if (selectedImages.size > 2) {
            Glide.with(this)
                .load(selectedImages[2])
                .centerCrop()
                .into(binding.ivPreview3)
        }

        // Update button text
        binding.btnAddImage.text = "사진 추가 (${selectedImages.size}/3)"
    }

    private fun createPost() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTitle.error = "제목을 입력하세요"
            return
        }

        if (content.isEmpty()) {
            binding.etContent.error = "내용을 입력하세요"
            return
        }

        val region = when (binding.spinnerRegion.selectedItemPosition) {
            1 -> "seodaemun"
            2 -> "dongdaemun"
            else -> "all"
        }

        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        binding.btnPost.isEnabled = false

        lifecycleScope.launch {
            try {
                // Upload images first
                val imageUrls = mutableListOf<String>()
                for (imageUri in selectedImages) {
                    val result = repository.uploadImage(imageUri)
                    result.onSuccess { url ->
                        imageUrls.add(url)
                    }.onFailure { e ->
                        throw e
                    }
                }

                // Create post
                val currentUser = auth.currentUser!!
                val post = CommunityPost(
                    title = title,
                    content = content,
                    authorUid = currentUser.uid,
                    authorName = currentUser.displayName ?: currentUser.email ?: "익명",
                    authorEmail = currentUser.email ?: "",
                    region = region,
                    imageUrls = imageUrls
                )

                val result = repository.createPost(post)
                result.onSuccess {
                    Toast.makeText(this@CreatePostActivity, "게시글이 작성되었습니다", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }.onFailure { e ->
                    Toast.makeText(this@CreatePostActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CreatePostActivity, "이미지 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnPost.isEnabled = true
            }
        }
    }

    private fun updatePost() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        val region = when (binding.spinnerRegion.selectedItemPosition) {
            1 -> "seodaemun"
            2 -> "dongdaemun"
            else -> "all"
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnPost.isEnabled = false

        lifecycleScope.launch {
            val updates = mapOf(
                "title" to title,
                "content" to content,
                "region" to region
            )

            val result = repository.updatePost(editingPostId!!, updates)
            result.onSuccess {
                Toast.makeText(this@CreatePostActivity, "게시글이 수정되었습니다", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }.onFailure { e ->
                Toast.makeText(this@CreatePostActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            binding.progressBar.visibility = View.GONE
            binding.btnPost.isEnabled = true
        }
    }

    private fun loadPostForEditing(postId: String) {
        lifecycleScope.launch {
            val result = repository.getPostById(postId)
            result.onSuccess { post ->
                binding.etTitle.setText(post.title)
                binding.etContent.setText(post.content)

                when (post.region) {
                    "seodaemun" -> binding.spinnerRegion.setSelection(1)
                    "dongdaemun" -> binding.spinnerRegion.setSelection(2)
                    else -> binding.spinnerRegion.setSelection(0)
                }

                // Hide image upload for edit mode (to keep it simple)
                binding.btnAddImage.visibility = View.GONE
                binding.imagePreviewContainer.visibility = View.GONE

                binding.btnPost.text = "수정하기"
            }.onFailure {
                Toast.makeText(this@CreatePostActivity, "게시글을 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
