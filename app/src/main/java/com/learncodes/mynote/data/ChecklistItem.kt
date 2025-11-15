package com.learncodes.mynote.data

data class ChecklistItem(
    val id: String = System.currentTimeMillis().toString(),
    val text: String,
    val isChecked: Boolean = false
)