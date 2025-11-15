package com.learncodes.mynote

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.learncodes.mynote.databinding.ActivitySettingsBinding
import com.learncodes.mynote.sync.GoogleDriveSync
import com.learncodes.mynote.ui.NoteViewModel
import com.learncodes.mynote.ui.theme.AppTheme
import com.learncodes.mynote.ui.theme.ThemeAdapter
import com.learncodes.mynote.ui.theme.ThemeManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var driveSync: GoogleDriveSync
    private lateinit var themeManager: ThemeManager
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var themeAdapter: ThemeAdapter

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)

            lifecycleScope.launch {
                val success = driveSync.initializeDriveService(account)
                if (success) {
                    updateSyncUI()
                    Toast.makeText(this@SettingsActivity, "Signed in successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SettingsActivity, "Failed to initialize Drive", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        driveSync = GoogleDriveSync(this)
        themeManager = ThemeManager(this)
        noteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        setupThemeSelection()
        setupSyncSettings()
        observeTheme()
    }

    private fun setupThemeSelection() {
        themeAdapter = ThemeAdapter(AppTheme.values().toList()) { theme ->
            lifecycleScope.launch {
                themeManager.setTheme(theme)
                Toast.makeText(this@SettingsActivity, "Theme applied", Toast.LENGTH_SHORT).show()
                recreate()
            }
        }

        binding.recyclerThemes.apply {
            layoutManager = LinearLayoutManager(this@SettingsActivity)
            adapter = themeAdapter
        }
    }

    private fun setupSyncSettings() {
        updateSyncUI()

        binding.btnSignIn.setOnClickListener {
            if (driveSync.isSignedIn()) {
                GoogleSignIn.getClient(this, driveSync.getSignInOptions()).signOut()
                updateSyncUI()
                Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
            } else {
                val signInIntent = GoogleSignIn.getClient(this, driveSync.getSignInOptions()).signInIntent
                signInLauncher.launch(signInIntent)
            }
        }

        binding.btnBackup.setOnClickListener {
            performBackup()
        }

        binding.btnRestore.setOnClickListener {
            performRestore()
        }

        binding.switchAutoSync.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("auto_sync", isChecked)
                    .apply()

                if (isChecked) {
                    performBackup()
                }
            }
        }
    }

    private fun updateSyncUI() {
        val isSignedIn = driveSync.isSignedIn()
        val account = driveSync.getSignedInAccount()

        binding.btnSignIn.text = if (isSignedIn) "Sign Out" else "Sign In with Google"
        binding.tvAccountName.text = if (isSignedIn) {
            "Signed in as: ${account?.email}"
        } else {
            "Not signed in"
        }

        binding.btnBackup.isEnabled = isSignedIn
        binding.btnRestore.isEnabled = isSignedIn
        binding.switchAutoSync.isEnabled = isSignedIn

        val autoSync = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getBoolean("auto_sync", false)
        binding.switchAutoSync.isChecked = autoSync

        if (isSignedIn) {
            lifecycleScope.launch {
                val account = driveSync.getSignedInAccount()
                if (account != null && driveSync.initializeDriveService(account)) {
                    val lastSync = driveSync.getLastSyncTime()
                    if (lastSync != null) {
                        val date = Date(lastSync)
                        val format = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                        binding.tvLastSync.text = "Last synced: ${format.format(date)}"
                    } else {
                        binding.tvLastSync.text = "Never synced"
                    }
                }
            }
        } else {
            binding.tvLastSync.text = ""
        }
    }

    private fun performBackup() {
        binding.btnBackup.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE

        lifecycleScope.launch {
            try {
                val account = driveSync.getSignedInAccount()
                if (account != null) {
                    driveSync.initializeDriveService(account)

                    val notes = noteViewModel.getAllNotesForBackup()
                    val categories = noteViewModel.getAllCategoriesForBackup()

                    val backupData = BackupData(notes, categories)
                    val json = Gson().toJson(backupData)

                    val success = driveSync.uploadNotes(json)

                    if (success) {
                        Toast.makeText(this@SettingsActivity, "Backup successful", Toast.LENGTH_SHORT).show()
                        updateSyncUI()
                    } else {
                        Toast.makeText(this@SettingsActivity, "Backup failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnBackup.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun performRestore() {
        binding.btnRestore.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE

        lifecycleScope.launch {
            try {
                val account = driveSync.getSignedInAccount()
                if (account != null) {
                    driveSync.initializeDriveService(account)

                    val json = driveSync.downloadNotes()

                    if (json != null) {
                        val backupData = Gson().fromJson(json, BackupData::class.java)

                        backupData.categories.forEach { category ->
                            noteViewModel.insertCategory(category)
                        }

                        backupData.notes.forEach { note ->
                            noteViewModel.insert(note)
                        }

                        Toast.makeText(this@SettingsActivity, "Restore successful", Toast.LENGTH_SHORT).show()
                        updateSyncUI()
                    } else {
                        Toast.makeText(this@SettingsActivity, "No backup found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnRestore.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun observeTheme() {
        lifecycleScope.launch {
            themeManager.selectedTheme.collect { theme ->
                applyThemeColors(theme)
            }
        }
    }

    private fun applyThemeColors(theme: AppTheme) {
        window.statusBarColor = theme.primaryColor
        binding.toolbar.setBackgroundColor(theme.primaryColor)
        binding.root.setBackgroundColor(theme.backgroundColor)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}