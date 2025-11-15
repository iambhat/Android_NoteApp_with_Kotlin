package com.learncodes.mynote.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.learncodes.mynote.data.ChecklistItem
import com.learncodes.mynote.databinding.ItemChecklistBinding

class ChecklistAdapter(
    private val items: MutableList<ChecklistItem>,
    private val onItemChecked: (ChecklistItem, Boolean) -> Unit,
    private val onItemDeleted: (ChecklistItem) -> Unit
) : RecyclerView.Adapter<ChecklistAdapter.ChecklistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChecklistViewHolder {
        val binding = ItemChecklistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChecklistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChecklistViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ChecklistViewHolder(
        private val binding: ItemChecklistBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChecklistItem) {
            binding.apply {
                checkbox.text = item.text
                checkbox.isChecked = item.isChecked

                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    onItemChecked(item, isChecked)
                }

                btnDelete.setOnClickListener {
                    onItemDeleted(item)
                }
            }
        }
    }
}