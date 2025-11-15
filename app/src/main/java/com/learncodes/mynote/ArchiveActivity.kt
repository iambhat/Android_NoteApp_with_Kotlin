package com.learncodes.mynote

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.learncodes.mynote.data.Note
import com.learncodes.mynote.databinding.ActivityArchiveBinding
import com.learncodes.mynote.ui.NoteAdapter
import com.learncodes.mynote.ui.NoteViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ArchiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArchiveBinding
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Archive"

        setupViewModel()
        setupRecyclerView()
        observeNotes()
    }

    private fun setupViewModel() {
        noteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            onNoteClick = { note ->
                openNote(note)
            },
            onNoteLongClick = { note ->
                showOptionsDialog(note)
            }
        )

        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = this@ArchiveActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun observeNotes() {
        noteViewModel.archivedNotes.observe(this) { notes ->
            adapter.submitList(notes)
            binding.tvEmptyState.visibility = if (notes.isEmpty()) {
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

    private fun showOptionsDialog(note: Note) {
        val options = arrayOf("Unarchive", "Delete")

        MaterialAlertDialogBuilder(this)
            .setTitle(note.title.ifEmpty { "Untitled" })
            .setItems(options) { _, which ->
                when (which) {
                    0 -> noteViewModel.toggleArchive(note)
                    1 -> noteViewModel.moveToTrash(note)
                }
            }
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}