package com.learncodes.mynote

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.learncodes.mynote.data.Note
import com.learncodes.mynote.databinding.ActivityTrashBinding
import com.learncodes.mynote.ui.NoteAdapter
import com.learncodes.mynote.ui.NoteViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TrashActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrashBinding
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Trash"

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
                showOptionsDialog(note)
            },
            onNoteLongClick = { note ->
                showOptionsDialog(note)
            }
        )

        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = this@TrashActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun observeNotes() {
        noteViewModel.trashedNotes.observe(this) { notes ->
            adapter.submitList(notes)
            binding.tvEmptyState.visibility = if (notes.isEmpty()) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }
    }

    private fun showOptionsDialog(note: Note) {
        val options = arrayOf("Restore", "Delete Permanently")

        MaterialAlertDialogBuilder(this)
            .setTitle(note.title.ifEmpty { "Untitled" })
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        noteViewModel.restoreFromTrash(note)
                        Toast.makeText(this, "Note restored", Toast.LENGTH_SHORT).show()
                    }
                    1 -> showDeleteDialog(note)
                }
            }
            .show()
    }

    private fun showDeleteDialog(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Delete Permanently")
            .setMessage("This action cannot be undone. Are you sure?")
            .setPositiveButton("Delete") { _, _ ->
                noteViewModel.delete(note)
                Toast.makeText(this, "Note deleted permanently", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_trash, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_empty_trash -> {
                showEmptyTrashDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showEmptyTrashDialog() {
        AlertDialog.Builder(this)
            .setTitle("Empty Trash")
            .setMessage("All notes in trash will be deleted permanently. This cannot be undone.")
            .setPositiveButton("Empty Trash") { _, _ ->
                noteViewModel.emptyTrash()
                Toast.makeText(this, "Trash emptied", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}