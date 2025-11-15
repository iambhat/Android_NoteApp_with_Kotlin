// AddEditNoteActivity.kt
package com.learncodes.mynote

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.learncodes.mynote.data.ChecklistItem
import com.learncodes.mynote.data.Note
import com.learncodes.mynote.databinding.ActivityAddEditNoteBinding
import com.learncodes.mynote.ui.ChecklistAdapter
import com.learncodes.mynote.ui.ImageAdapter
import com.learncodes.mynote.ui.NoteViewModel
import com.learncodes.mynote.utils.ChecklistUtils
import com.learncodes.mynote.utils.FileUtils
import com.learncodes.mynote.utils.RichTextHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditNoteBinding
    private lateinit var noteViewModel: NoteViewModel
    private var currentNote: Note? = null
    private var selectedColor: Int = Color.parseColor("#FFFFFF")
    private var selectedCategory: String = "General"
    private val imagePaths = mutableListOf<String>()
    private val checklistItems = mutableListOf<ChecklistItem>()
    private lateinit var checklistAdapter: ChecklistAdapter
    private lateinit var imageAdapter: ImageAdapter
    private var isChecklistMode = false

    private val colors = listOf(
        "#FFFFFF", "#FFCDD2", "#F8BBD0", "#E1BEE7",
        "#D1C4E9", "#C5CAE9", "#BBDEFB", "#B3E5FC",
        "#B2EBF2", "#B2DFDB", "#C8E6C9", "#DCEDC8",
        "#F0F4C3", "#FFF9C4", "#FFECB3", "#FFE0B2"
    )

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { addImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        noteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        setupColorPicker()
        setupCategorySpinner()
        setupImageRecyclerView()
        setupChecklistRecyclerView()
        setupBottomButtons()
        loadNoteIfEdit()
    }

    private fun setupColorPicker() {
        binding.colorPicker.removeAllViews()

        colors.forEach { colorHex ->
            val colorView = layoutInflater.inflate(
                R.layout.item_color,
                binding.colorPicker,
                false
            )

            val color = Color.parseColor(colorHex)
            colorView.setBackgroundColor(color)

            colorView.setOnClickListener {
                selectedColor = color
                binding.root.setBackgroundColor(color)
            }

            binding.colorPicker.addView(colorView)
        }
    }

    private fun setupCategorySpinner() {
        noteViewModel.allCategories.observe(this) { categories ->
            val categoryNames = categories.map { it.name }
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                categoryNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter

            val currentIndex = categoryNames.indexOf(selectedCategory)
            if (currentIndex >= 0) {
                binding.spinnerCategory.setSelection(currentIndex)
            }
        }
    }

    private fun setupImageRecyclerView() {
        imageAdapter = ImageAdapter(
            images = imagePaths,
            onImageClick = { path -> showImagePreview(path) },
            onImageDelete = { path -> removeImage(path) }
        )

        binding.recyclerImages.apply {
            layoutManager = LinearLayoutManager(
                this@AddEditNoteActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = imageAdapter
        }
    }

    private fun setupChecklistRecyclerView() {
        checklistAdapter = ChecklistAdapter(
            items = checklistItems,
            onItemChecked = { item, isChecked ->
                val index = checklistItems.indexOfFirst { it.id == item.id }
                if (index >= 0) {
                    checklistItems[index] = item.copy(isChecked = isChecked)
                }
            },
            onItemDeleted = { item ->
                checklistItems.remove(item)
                checklistAdapter.notifyDataSetChanged()
            }
        )

        binding.recyclerChecklist.apply {
            layoutManager = LinearLayoutManager(this@AddEditNoteActivity)
            adapter = checklistAdapter
        }
    }

    private fun setupBottomButtons() {
        binding.btnAddImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnAddChecklist.setOnClickListener {
            toggleChecklistMode()
        }

        binding.btnAddChecklistItem.setOnClickListener {
            showAddChecklistItemDialog()
        }

        // Rich text formatting buttons
        binding.btnBold.setOnClickListener {
            RichTextHelper.applyBold(binding.etContent)
        }

        binding.btnItalic.setOnClickListener {
            RichTextHelper.applyItalic(binding.etContent)
        }

        binding.btnUnderline.setOnClickListener {
            RichTextHelper.applyUnderline(binding.etContent)
        }

        binding.btnStrikethrough.setOnClickListener {
            RichTextHelper.applyStrikethrough(binding.etContent)
        }
    }

    private fun toggleChecklistMode() {
        isChecklistMode = !isChecklistMode

        if (isChecklistMode) {
            binding.etContent.visibility = View.GONE
            binding.checklistContainer.visibility = View.VISIBLE
            binding.btnAddChecklistItem.visibility = View.VISIBLE
        } else {
            binding.etContent.visibility = View.VISIBLE
            binding.checklistContainer.visibility = View.GONE
            binding.btnAddChecklistItem.visibility = View.GONE
        }
    }

    private fun showAddChecklistItemDialog() {
        val input = EditText(this)
        input.hint = "Enter item"

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Checklist Item")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) {
                    checklistItems.add(ChecklistItem(text = text))
                    checklistAdapter.notifyDataSetChanged()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addImage(uri: Uri) {
        val path = FileUtils.saveImageToInternalStorage(this, uri)
        path?.let {
            imagePaths.add(it)
            imageAdapter.notifyDataSetChanged()
            binding.recyclerImages.visibility = View.VISIBLE
        } ?: Toast.makeText(this, "Failed to add image", Toast.LENGTH_SHORT).show()
    }

    private fun removeImage(path: String) {
        imagePaths.remove(path)
        FileUtils.deleteImage(path)
        imageAdapter.notifyDataSetChanged()

        if (imagePaths.isEmpty()) {
            binding.recyclerImages.visibility = View.GONE
        }
    }

    private fun showImagePreview(path: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_image_preview, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.iv_preview)

        Glide.with(this)
            .load(path)
            .into(imageView)

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun loadNoteIfEdit() {
        val noteId = intent.getLongExtra("NOTE_ID", -1L)

        if (noteId != -1L) {
            supportActionBar?.title = "Edit Note"
            lifecycleScope.launch {
                currentNote = noteViewModel.getNoteById(noteId)
                currentNote?.let { note ->
                    binding.etTitle.setText(note.title)
                    binding.etContent.setText(note.content)
                    selectedColor = note.color
                    selectedCategory = note.category
                    binding.root.setBackgroundColor(selectedColor)

                    // Load images
                    val paths = FileUtils.getImagePaths(note.imagePaths)
                    imagePaths.addAll(paths)
                    if (imagePaths.isNotEmpty()) {
                        binding.recyclerImages.visibility = View.VISIBLE
                        imageAdapter.notifyDataSetChanged()
                    }

                    // Load checklist
                    val items = ChecklistUtils.fromJson(note.checklistItems)
                    if (items.isNotEmpty()) {
                        checklistItems.addAll(items)
                        toggleChecklistMode()
                        checklistAdapter.notifyDataSetChanged()
                    }

                    setupCategorySpinner()
                }
            }
        } else {
            supportActionBar?.title = "New Note"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_save -> {
                saveNote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveNote() {
        val title = binding.etTitle.text.toString().trim()
        val content = if (isChecklistMode) {
            checklistItems.joinToString("\n") {
                "${if (it.isChecked) "☑" else "☐"} ${it.text}"
            }
        } else {
            binding.etContent.text.toString().trim()
        }

        if (title.isEmpty() && content.isEmpty() && imagePaths.isEmpty()) {
            Toast.makeText(this, "Note is empty", Toast.LENGTH_SHORT).show()
            return
        }

        selectedCategory = binding.spinnerCategory.selectedItem?.toString() ?: "General"

        val note = Note(
            id = currentNote?.id ?: 0,
            title = title.ifEmpty { "Untitled" },
            content = content,
            createdAt = currentNote?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            color = selectedColor,
            category = selectedCategory,
            isArchived = currentNote?.isArchived ?: false,
            isTrashed = currentNote?.isTrashed ?: false,
            isLocked = currentNote?.isLocked ?: false,
            isPinned = currentNote?.isPinned ?: false,
            imagePaths = FileUtils.pathsToString(imagePaths),
            checklistItems = ChecklistUtils.toJson(checklistItems)
        )

        if (currentNote != null) {
            noteViewModel.update(note)
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
        } else {
            noteViewModel.insert(note)
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}