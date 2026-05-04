package com.gnaanaa.mtimer.data.sync

import android.content.Context
import android.util.Log
import com.gnaanaa.mtimer.data.db.PresetDao
import com.gnaanaa.mtimer.data.db.PresetEntity
import com.gnaanaa.mtimer.data.db.SessionDao
import com.gnaanaa.mtimer.data.db.SessionEntity
import com.gnaanaa.mtimer.data.db.toDomain
import com.gnaanaa.mtimer.data.db.toEntity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.HttpRequestInitializer
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

import com.google.gson.annotations.SerializedName

private const val TAG = "DriveSync"
private const val BACKUP_FILE_NAME = "mtimer_backup.json"

data class BackupData(
    @SerializedName("presets") val presets: List<PresetEntity>? = emptyList(),
    @SerializedName("sessions") val sessions: List<SessionEntity>? = emptyList()
)

@Singleton
class DriveSync @Inject constructor(
    @ApplicationContext private val context: Context,
    private val presetDao: PresetDao,
    private val sessionDao: SessionDao
) {
    private val gson = Gson()

    private fun getDriveService(): Drive? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(DriveScopes.DRIVE_APPDATA)
        ).apply {
            selectedAccount = account.account
        }

        val requestInitializer = HttpRequestInitializer { request ->
            credential.initialize(request)
            request.connectTimeout = 60000 // 60s
            request.readTimeout = 60000    // 60s
        }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            requestInitializer
        ).setApplicationName("MTimer").build()
    }

    suspend fun syncPresets() = withContext(Dispatchers.IO) {
        val service = getDriveService() ?: run {
            Log.w(TAG, "No Google Account signed in, skipping Drive sync")
            return@withContext
        }

        try {
            // 1. Get local state
            val localPresets = presetDao.getAllPresetsList()
            val localSessions = sessionDao.getAllSessionsList()
            
            // 2. Find existing backup file on Drive
            val existingFile = findBackupFile(service)
            
            if (existingFile != null) {
                // 3. Download and merge
                val remoteJson = downloadFile(service, existingFile.id)
                val remoteData: BackupData = gson.fromJson(
                    remoteJson, 
                    object : TypeToken<BackupData>() {}.type
                )
                
                // --- RESTORE LOGIC ---
                
                // A. Restore Sessions (History)
                // History is additive. We merge by startTime to avoid duplicates.
                val localSessionTimes = localSessions.map { it.startTime }.toSet()
                remoteData.sessions?.forEach { remote ->
                    if (remote.startTime !in localSessionTimes) {
                        // Reset ID to 0 so Room auto-generates a new local ID
                        sessionDao.insertSession(remote.copy(id = 0))
                        Log.d(TAG, "Restored session history from cloud: ${remote.startTime}")
                    }
                }

                // B. Restore Presets
                // To fix the "deletion being restored" issue:
                // We only auto-restore presets if the local database is EMPTY (Fresh Install).
                // If local has data, we treat Local as the source of truth for presets.
                if (localPresets.isEmpty()) {
                    remoteData.presets?.forEach { remote ->
                        presetDao.insertPreset(remote.toDomain().toEntity())
                        Log.d(TAG, "Fresh restore of preset from cloud: ${remote.name}")
                    }
                }
            }
            
            // 4. Upload current state back to cloud
            // This ensures deletions (if Local has data) are synced up to the cloud file.
            val updatedPresets = presetDao.getAllPresetsList()
            val updatedSessions = sessionDao.getAllSessionsList()
            val backupData = BackupData(presets = updatedPresets, sessions = updatedSessions)
            val jsonToUpload = gson.toJson(backupData)
            
            if (existingFile != null) {
                updateBackupFile(service, existingFile.id, jsonToUpload)
            } else {
                createBackupFile(service, jsonToUpload)
            }
            
            Log.i(TAG, "Drive sync/backup completed successfully")
        } catch (e: GoogleJsonResponseException) {
            if (e.statusCode == 403) {
                Log.e(TAG, "Google Drive API access denied. Enable it in Cloud Console.")
            } else {
                Log.e(TAG, "Drive API error (${e.statusCode}): ${e.message}")
            }
            throw e
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error during Drive sync", e)
            throw e
        }
    }

    private fun findBackupFile(service: Drive): File? {
        // Query for the specific filename in the appDataFolder
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
