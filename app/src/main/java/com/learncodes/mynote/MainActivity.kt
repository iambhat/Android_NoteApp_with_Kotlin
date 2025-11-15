package com.learncodes.mynote

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.learncodes.mynote.data.Category
import com.learncodes.mynote.data.Note
import com.learncodes.mynote.databinding.ActivityMainBinding
import com.learncodes.mynote.ui.NoteAdapter
import com.learncodes.mynote.ui.NoteViewModel
import com.learncodes.mynote.ui.theme.ThemeManager
import com.learncodes.mynote.utils.BiometricHelper
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var adapter: NoteAdapter
    private lateinit var biometricHelper: BiometricHelper
    private lateinit var themeManager: ThemeManager
    private var isSearching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        biometricHelper = BiometricHelper(this)
        themeManager = ThemeManager(this)
        setupViewModel()
        setupRecyclerView()
        setupFab()
        setupCategoryChips()
        observeNotes()
        observeTheme()
    }

    private fun observeTheme() {
        lifecycleScope.launch {
            themeManager.selectedTheme.collect { theme ->
                applyTheme(theme)
            }
        }
    }

    private fun applyTheme(theme: com.learncodes.mynote.ui.theme.AppTheme) {
        window.statusBarColor = theme.primaryColor
        binding.toolbar.setBackgroundColor(theme.primaryColor)
        binding.root.setBackgroundColor(theme.backgroundColor)
        binding.fabAdd.backgroundTintList = android.content.res.ColorStateList.valueOf(theme.accentColor)
    }

    private fun setupViewModel() {
        noteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            onNoteClick = { note ->
                if (note.isLocked && biometricHelper.canAuthenticate()) {
                    biometricHelper.authenticate(
                        onSuccess = { openNote(note) },
                        onError = { msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
                    )
                } else {
                    openNote(note)
                }
            },
            onNoteLongClick = { note ->
                showNoteOptionsDialog(note)
            }
        )

        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = this@MainActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddEditNoteActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupCategoryChips() {
        noteViewModel.allCategories.observe(this) { categories ->
            binding.categoryChipGroup.removeAllViews()

            // Add "All" chip
            val allChip = createCategoryChip("All", true)
            binding.categoryChipGroup.addView(allChip)

            // Add category chips
            categories.forEach { category ->
                val chip = createCategoryChip(category.name, false)
                binding.categoryChipGroup.addView(chip)
            }
        }
    }

    private fun createCategoryChip(name: String, isDefault: Boolean): Chip {
        return Chip(this).apply {
            text = name
            isCheckable = true
            isChecked = isDefault
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    noteViewModel.setCategory(if (name == "All") null else name)
                }
            }
        }
    }

    private fun observeNotes() {
        noteViewModel.filteredNotes.observe(this) { notes ->
            updateNotesList(notes)
        }
    }

    private fun updateNotesList(notes: List<Note>?) {
        notes?.let {
            adapter.submitList(it)
            binding.tvEmptyState.visibility = if (it.isEmpty()) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }
    }

    private fun openNote(note: Note) {
        val intent = Intent(this, AddEditNoteActivity::class.java).apply {
            putExtra("NOTE_ID", note.id)
        }
        startActivity(intent)
    }

    private fun showNoteOptionsDialog(note: Note) {
        val options = mutableListOf(
            "Edit",
            if (note.isPinned) "Unpin" else "Pin",
            if (note.isArchived) "Unarchive" else "Archive",
            if (note.isLocked) "Unlock" else "Lock",
            "Share",
            "Move to Trash",
            "Delete Permanently"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(note.title.ifEmpty { "Untitled" })
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Edit" -> openNote(note)
                    "Pin", "Unpin" -> noteViewModel.togglePin(note)
                    "Archive", "Unarchive" -> noteViewModel.toggleArchive(note)
                    "Lock", "Unlock" -> toggleNoteLock(note)
                    "Share" -> shareNote(note)
                    "Move to Trash" -> moveToTrash(note)
                    "Delete Permanently" -> showDeleteDialog(note)
                }
            }
            .show()
    }

    private fun toggleNoteLock(note: Note) {
        if (!note.isLocked && biometricHelper.canAuthenticate()) {
            noteViewModel.update(note.copy(isLocked = true))
            Toast.makeText(this, "Note locked", Toast.LENGTH_SHORT).show()
        } else if (note.isLocked) {
            biometricHelper.authenticate(
                onSuccess = {
                    noteViewModel.update(note.copy(isLocked = false))
                    Toast.makeText(this, "Note unlocked", Toast.LENGTH_SHORT).show()
                },
                onError = { msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
            )
        } else {
            Toast.makeText(this, "Biometric authentication not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareNote(note: Note) {
        val shareText = "${note.title}\n\n${note.content}"
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share note via"))
    }

    private fun moveToTrash(note: Note) {
        noteViewModel.moveToTrash(note)
        Toast.makeText(this, "Note moved to trash", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteDialog(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Delete Note Permanently")
            .setMessage("This action cannot be undone. Are you sure?")
            .setPositiveButton("Delete") { _, _ ->
                noteViewModel.delete(note)
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                noteViewModel.setSearchQuery(newText ?: "")
                isSearching = !newText.isNullOrBlank()

                if (isSearching) {
                    noteViewModel.searchResults.observe(this@MainActivity) { results ->
                        updateNotesList(results)
                    }
                } else {
                    observeNotes()
                }
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_archive -> {
                startActivity(Intent(this, ArchiveActivity::class.java))
                true
            }
            R.id.action_trash -> {
                startActivity(Intent(this, TrashActivity::class.java))
                true
            }
            R.id.action_categories -> {
                showCategoryManagementDialog()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showCategoryManagementDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val input = dialogView.findViewById<EditText>(R.id.et_category_name)

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Category")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val categoryName = input.text.toString().trim()
                if (categoryName.isNotEmpty()) {
                    val category = Category(name = categoryName)
                    noteViewModel.insertCategory(category)
                    Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Manage") { _, _ ->
                showManageCategoriesDialog()
            }
            .show()
    }

    private fun showManageCategoriesDialog() {
        noteViewModel.allCategories.observe(this) { categories ->
            val categoryNames = categories.map { it.name }.toTypedArray()

            MaterialAlertDialogBuilder(this)
                .setTitle("Manage Categories")
                .setItems(categoryNames) { _, which ->
                    val category = categories[which]
                    showDeleteCategoryDialog(category)
                }
                .setNegativeButton("Close", null)
                .show()
        }
    }

    private fun showDeleteCategoryDialog(category: Category) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete ${category.name}?")
            .setMessage("Notes in this category will be moved to 'General'")
            .setPositiveButton("Delete") { _, _ ->
                noteViewModel.deleteCategory(category)
                Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}