// NoteAdapter.kt
package com.learncodes.mynote.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.learncodes.mynote.R
import com.learncodes.mynote.data.Note
import com.learncodes.mynote.databinding.ItemNoteBinding
import com.learncodes.mynote.utils.FileUtils
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.apply {
                tvTitle.text = note.title
                tvContent.text = note.content
                tvDate.text = formatDate(note.updatedAt)
                tvCategory.text = note.category
                cardView.setCardBackgroundColor(note.color)

                // Show indicators
                ivLocked.visibility = if (note.isLocked) View.VISIBLE else View.GONE
                ivPinned.visibility = if (note.isPinned) View.VISIBLE else View.GONE
                ivArchived.visibility = if (note.isArchived) View.VISIBLE else View.GONE

                // Show image indicator if note has images
                val imagePaths = FileUtils.getImagePaths(note.imagePaths)
                if (imagePaths.isNotEmpty()) {
                    ivImageIndicator.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(imagePaths.first())
                        .centerCrop()
                        .into(ivImageIndicator)
                } else {
                    ivImageIndicator.visibility = View.GONE
                }

                // Show checklist indicator
                ivChecklistIndicator.visibility = if (note.checklistItems.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                root.setOnClickListener { onNoteClick(note) }
                root.setOnLongClickListener {
                    onNoteLongClick(note)
                    true
                }
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}