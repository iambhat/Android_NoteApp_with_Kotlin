package com.learncodes.mynote.ui.theme

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.learncodes.mynote.databinding.ItemThemeBinding

class ThemeAdapter(
    private val themes: List<AppTheme>,
    private val onThemeSelected: (AppTheme) -> Unit
) : RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val binding = ItemThemeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ThemeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        holder.bind(themes[position])
    }

    override fun getItemCount() = themes.size

    inner class ThemeViewHolder(
        private val binding: ItemThemeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(theme: AppTheme) {
            binding.apply {
                tvThemeName.text = theme.themeName
                viewPrimaryColor.setBackgroundColor(theme.primaryColor)
                viewAccentColor.setBackgroundColor(theme.accentColor)
                viewBackgroundColor.setBackgroundColor(theme.backgroundColor)

                root.setOnClickListener {
                    onThemeSelected(theme)
                }
            }
        }
    }
}