package com.learncodes.mynote.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.learncodes.mynote.data.Category
import com.learncodes.mynote.data.Note
import com.learncodes.mynote.data.NoteDatabase
import com.learncodes.mynote.data.NoteRepository
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository
    val allNotes: LiveData<List<Note>>
    val archivedNotes: LiveData<List<Note>>
    val trashedNotes: LiveData<List<Note>>
    val allCategories: LiveData<List<Category>>

    private val _selectedCategory = MutableLiveData<String?>()
    val selectedCategory: LiveData<String?> = _selectedCategory

    private val _searchQuery = MutableLiveData<String>()
    val searchResults: LiveData<List<Note>> = _searchQuery.switchMap { query ->
        if (query.isBlank()) {
            allNotes
        } else {
            repository.searchNotes(query)
        }
    }

    val filteredNotes: LiveData<List<Note>> = _selectedCategory.switchMap { category ->
        if (category == null || category == "All") {
            allNotes
        } else {
            repository.getNotesByCategory(category)
        }
    }

    init {
        val noteDao = NoteDatabase.getDatabase(application, viewModelScope).noteDao()
        val categoryDao = NoteDatabase.getDatabase(application, viewModelScope).categoryDao()
        repository = NoteRepository(noteDao, categoryDao)
        allNotes = repository.allNotes
        archivedNotes = repository.archivedNotes
        trashedNotes = repository.trashedNotes
        allCategories = repository.allCategories
        _selectedCategory.value = null
    }

    fun insert(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        repository.update(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }

    fun moveToTrash(note: Note) = viewModelScope.launch {
        repository.update(note.copy(isTrashed = true))
    }

    fun restoreFromTrash(note: Note) = viewModelScope.launch {
        repository.update(note.copy(isTrashed = false))
    }

    fun toggleArchive(note: Note) = viewModelScope.launch {
        repository.update(note.copy(isArchived = !note.isArchived))
    }

    fun togglePin(note: Note) = viewModelScope.launch {
        repository.update(note.copy(isPinned = !note.isPinned))
    }

    fun emptyTrash() = viewModelScope.launch {
        repository.emptyTrash()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    suspend fun getNoteById(id: Long): Note? {
        return repository.getNoteById(id)
    }

    fun insertCategory(category: Category) = viewModelScope.launch {
        repository.insertCategory(category)
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    suspend fun getCategoryNoteCount(categoryName: String): Int {
        return repository.getCategoryNoteCount(categoryName)
    }

    suspend fun getAllNotesForBackup(): List<Note> {
        return repository.getAllNotesForBackup()
    }

    suspend fun getAllCategoriesForBackup(): List<Category> {
        return repository.getAllCategoriesForBackup()
    }
}