package com.learncodes.mynote.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Int = 0xFF6200EE.toInt(),
    val createdAt: Long = System.currentTimeMillis()
)