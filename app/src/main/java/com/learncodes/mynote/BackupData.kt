package com.learncodes.mynote

import com.learncodes.mynote.data.Category
import com.learncodes.mynote.data.Note

data class BackupData(
    val notes: List<Note>,
    val categories: List<Category>
)