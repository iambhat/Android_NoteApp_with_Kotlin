package com.learncodes.mynote.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.learncodes.mynote.databinding.ItemImageBinding

class ImageAdapter(
    private val images: List<String>,
    private val onImageClick: (String) -> Unit,
    private val onImageDelete: (String) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount() = images.size

    inner class ImageViewHolder(
        private val binding: ItemImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(imagePath: String) {
            binding.apply {
                Glide.with(itemView.context)
                    .load(imagePath)
                    .centerCrop()
                    .into(imageView)

                imageView.setOnClickListener {
                    onImageClick(imagePath)
                }

                btnDelete.setOnClickListener {
                    onImageDelete(imagePath)
                }
            }
        }
    }
}