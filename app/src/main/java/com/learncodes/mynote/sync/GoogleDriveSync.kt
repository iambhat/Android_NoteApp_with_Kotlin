package com.learncodes.mynote.sync

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

class GoogleDriveSync(private val context: Context) {

    private var driveService: Drive? = null
    private val appFolderName = "NoteAppBackup"
    private var appFolderId: String? = null

    companion object {
        const val REQUEST_CODE_SIGN_IN = 100
    }

    fun getSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
    }

    fun isSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null
    }

    fun getSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    suspend fun initializeDriveService(account: GoogleSignInAccount): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    listOf(DriveScopes.DRIVE_APPDATA)
                )
                credential.selectedAccount = account.account

                driveService = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
                )
                    .setApplicationName("Note App")
                    .build()

                appFolderId = getOrCreateAppFolder()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun getOrCreateAppFolder(): String? {
        return try {
            // Search for existing folder
            val result = driveService?.files()?.list()
                ?.setSpaces("appDataFolder")
                ?.setQ("name='$appFolderName' and mimeType='application/vnd.google-apps.folder'")
                ?.setFields("files(id, name)")
                ?.execute()

            val files = result?.files
            if (!files.isNullOrEmpty()) {
                files[0].id
            } else {
                // Create new folder
                val folder = File().apply {
                    name = appFolderName
                    mimeType = "application/vnd.google-apps.folder"
                    parents = listOf("appDataFolder")
                }
                driveService?.files()?.create(folder)
                    ?.setFields("id")
                    ?.execute()?.id
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun uploadNotes(jsonData: String, fileName: String = "notes_backup.json"): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Check if file exists
                val existingFile = findFileByName(fileName)

                val fileMetadata = File().apply {
                    name = fileName
                    if (existingFile == null) {
                        parents = listOf("appDataFolder")
                    }
                }

                val content = com.google.api.client.http.ByteArrayContent(
                    "application/json",
                    jsonData.toByteArray()
                )

                if (existingFile != null) {
                    // Update existing file
                    driveService?.files()?.update(existingFile.id, fileMetadata, content)?.execute()
                } else {
                    // Create new file
                    driveService?.files()?.create(fileMetadata, content)
                        ?.setFields("id")
                        ?.execute()
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun downloadNotes(fileName: String = "notes_backup.json"): String? {
        return withContext(Dispatchers.IO) {
            try {
                val file = findFileByName(fileName) ?: return@withContext null

                val outputStream = ByteArrayOutputStream()
                driveService?.files()?.get(file.id)?.executeMediaAndDownloadTo(outputStream)

                outputStream.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun findFileByName(fileName: String): File? {
        return try {
            val result = driveService?.files()?.list()
                ?.setSpaces("appDataFolder")
                ?.setQ("name='$fileName'")
                ?.setFields("files(id, name)")
                ?.execute()

            result?.files?.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getLastSyncTime(): Long? {
        return withContext(Dispatchers.IO) {
            try {
                val file = findFileByName("notes_backup.json") ?: return@withContext null

                val fileMetadata = driveService?.files()?.get(file.id)
                    ?.setFields("modifiedTime")
                    ?.execute()

                fileMetadata?.modifiedTime?.value
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun deleteBackup(fileName: String = "notes_backup.json"): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = findFileByName(fileName) ?: return@withContext false
                driveService?.files()?.delete(file.id)?.execute()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}