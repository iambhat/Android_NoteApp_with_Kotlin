package com.learncodes.mynote.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT COUNT(*) FROM notes WHERE category = :categoryName AND isTrashed = 0")
    suspend fun getNoteCount(categoryName: String): Int

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesForBackup(): List<Category>
}