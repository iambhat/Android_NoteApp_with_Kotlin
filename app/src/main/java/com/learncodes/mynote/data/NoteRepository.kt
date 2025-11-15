package com.learncodes.mynote.data

import androidx.lifecycle.LiveData

class NoteRepository(
    private val noteDao: NoteDao,
    private val categoryDao: CategoryDao
) {

    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()
    val archivedNotes: LiveData<List<Note>> = noteDao.getArchivedNotes()
    val trashedNotes: LiveData<List<Note>> = noteDao.getTrashedNotes()
    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()

    fun getNotesByCategory(category: String): LiveData<List<Note>> {
        return noteDao.getNotesByCategory(category)
    }

    fun searchNotes(query: String): LiveData<List<Note>> {
        return noteDao.searchNotes(query)
    }

    suspend fun insert(note: Note): Long {
        return noteDao.insert(note)
    }

    suspend fun update(note: Note) {
        noteDao.update(note)
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }

    suspend fun getNoteById(id: Long): Note? {
        return noteDao.getNoteById(id)
    }

    suspend fun emptyTrash() {
        noteDao.emptyTrash()
    }

    suspend fun insertCategory(category: Category) {
        categoryDao.insert(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.delete(category)
    }

    suspend fun getCategoryNoteCount(categoryName: String): Int {
        return categoryDao.getNoteCount(categoryName)
    }

    suspend fun getAllNotesForBackup(): List<Note> {
        return noteDao.getAllNotesForBackup()
    }

    suspend fun getAllCategoriesForBackup(): List<Category> {
        return categoryDao.getAllCategoriesForBackup()
    }
}