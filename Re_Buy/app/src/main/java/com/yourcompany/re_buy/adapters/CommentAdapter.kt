package com.yourcompany.re_buy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.yourcompany.re_buy.databinding.ItemCommentBinding
import com.yourcompany.re_buy.models.Comment
import java.text.SimpleDateFormat
import java.util.Locale

class CommentAdapter(
    private var comments: List<Comment> = emptyList(),
    private val onDeleteClick: (Comment) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.tvCommentAuthor.text = comment.authorName
            binding.tvCommentContent.text = comment.content
            binding.tvCommentTimestamp.text = comment.createdAt?.let { dateFormat.format(it) } ?: ""

            // Show delete button only for comment author
            val currentUser = auth.currentUser
            if (currentUser != null && comment.authorUid == currentUser.uid) {
                binding.btnDeleteComment.visibility = View.VISIBLE
                binding.btnDeleteComment.setOnClickListener {
                    onDeleteClick(comment)
                }
            } else {
                binding.btnDeleteComment.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }
}
