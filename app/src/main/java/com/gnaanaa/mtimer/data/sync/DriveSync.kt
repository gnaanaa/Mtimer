package com.gnaanaa.mtimer.data.sync

import android.content.Context
import android.util.Log
import com.gnaanaa.mtimer.data.db.PresetDao
import com.gnaanaa.mtimer.data.db.PresetEntity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DriveSync"
private const val BACKUP_FILE_NAME = "presets_backup.json"

@Singleton
class DriveSync @Inject constructor(
    @ApplicationContext private val context: Context,
    private val presetDao: PresetDao
) {
    private val gson = Gson()

    private fun getDriveService(): Drive? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(DriveScopes.DRIVE_APPDATA)
        ).apply {
            selectedAccount = account.account
        }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("MTimer").build()
    }

    suspend fun syncPresets() = withContext(Dispatchers.IO) {
        val service = getDriveService() ?: run {
            Log.w(TAG, "No Google Account signed in, skipping Drive sync")
            return@withContext
        }

        try {
            // 1. Get local presets
            val localPresets = presetDao.getAllPresetsList()
            
            // 2. Find existing backup file on Drive
            val existingFile = findBackupFile(service)
            
            if (existingFile != null) {
                // 3. Download and merge if necessary
                val remoteJson = downloadFile(service, existingFile.id)
                val remotePresets: List<PresetEntity> = gson.fromJson(
                    remoteJson, 
                    object : TypeToken<List<PresetEntity>>() {}.type
                )
                
                // Simple merge: add missing ones to local DB
                val localIds = localPresets.map { it.id }.toSet()
                remotePresets.forEach { remote ->
                    if (remote.id !in localIds) {
                        presetDao.insertPreset(remote)
                        Log.d(TAG, "Restored preset from cloud: ${remote.name}")
                    }
                }
            }
            
            // 4. Upload updated local presets back to cloud
            val updatedPresets = presetDao.getAllPresetsList()
            val jsonToUpload = gson.toJson(updatedPresets)
            
            if (existingFile != null) {
                updateBackupFile(service, existingFile.id, jsonToUpload)
            } else {
                createBackupFile(service, jsonToUpload)
            }
            
            Log.i(TAG, "Drive sync completed successfully")
        } catch (e: GoogleJsonResponseException) {
            if (e.statusCode == 403) {
                Log.e(TAG, "Google Drive API is not enabled or access denied. Please enable it in the Google Cloud Console: ${e.details?.message ?: e.message}")
            } else {
                Log.e(TAG, "Google Drive API error (${e.statusCode}): ${e.message}", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during Drive sync", e)
        }
    }

    private fun findBackupFile(service: Drive): File? {
        val result = service.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$BACKUP_FILE_NAME'")
            .setFields("files(id, name)")
            .execute()
        return result.files.firstOrNull()
    }

    private fun downloadFile(service: Drive, fileId: String): String {
        return service.files().get(fileId).executeMediaAsInputStream().bufferedReader().use { it.readText() }
    }

    private fun createBackupFile(service: Drive, content: String) {
        val fileMetadata = File().apply {
            name = BACKUP_FILE_NAME
            parents = listOf("appDataFolder")
        }
        val mediaContent = ByteArrayContent.fromString("application/json", content)
        service.files().create(fileMetadata, mediaContent).execute()
        Log.d(TAG, "Created new backup file on Drive")
    }

    private fun updateBackupFile(service: Drive, fileId: String, content: String) {
        val mediaContent = ByteArrayContent.fromString("application/json", content)
        service.files().update(fileId, null, mediaContent).execute()
        Log.d(TAG, "Updated existing backup file on Drive")
    }
}
