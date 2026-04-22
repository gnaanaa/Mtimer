package com.gnaanaa.mtimer.data.sync

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.feature.ExperimentalMindfulnessSessionApi
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.MindfulnessSessionRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.gnaanaa.mtimer.domain.model.Session
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

private const val TAG = "HealthConnect"

/**
 * Checks SDK availability and that the required permissions are granted.
 * Call this before every read or write operation.
 */
@OptIn(ExperimentalMindfulnessSessionApi::class)
private suspend fun hasPermissions(
    client: HealthConnectClient,
    requireRead: Boolean = false,
    requireWrite: Boolean = false
): Boolean {
    return try {
        val granted = client.permissionController.getGrantedPermissions()

        if (requireRead) {
            val readPerm = HealthPermission.getReadPermission(MindfulnessSessionRecord::class)
            if (readPerm !in granted) {
                Log.w(TAG, "READ_MINDFULNESS not granted — skipping")
                return false
            }
        }

        if (requireWrite) {
            val writePerm = HealthPermission.getWritePermission(MindfulnessSessionRecord::class)
            if (writePerm !in granted) {
                Log.w(TAG, "WRITE_MINDFULNESS not granted — skipping")
                return false
            }
        }

        true
    } catch (e: Exception) {
        Log.e(TAG, "Could not check HC permissions", e)
        false
    }
}

@OptIn(ExperimentalMindfulnessSessionApi::class)
suspend fun syncSessionToHealthConnect(
    context: Context,
    session: Session,
    presetName: String?
) {
    // 1. Check SDK is present on this device
    if (HealthConnectClient.getSdkStatus(context) != HealthConnectClient.SDK_AVAILABLE) {
        Log.w(TAG, "Health Connect SDK not available")
        return
    }

    val client = HealthConnectClient.getOrCreate(context)

    // 2. Check write permission before attempting insert
    if (!hasPermissions(client, requireWrite = true)) return

    val startInstant = Instant.ofEpochMilli(session.startTime)
    var endInstant = Instant.ofEpochMilli(session.endTime)

    // Health Connect requires endTime strictly after startTime
    if (!endInstant.isAfter(startInstant)) {
        endInstant = startInstant.plusSeconds(
            session.durationSeconds.toLong().coerceAtLeast(1)
        )
        Log.w(TAG, "Adjusted end time to $endInstant")
    }

    try {
        client.insertRecords(
            listOf(
                MindfulnessSessionRecord(
                    startTime = startInstant,
                    startZoneOffset = ZoneOffset.systemDefault().rules.getOffset(startInstant),
                    endTime = endInstant,
                    endZoneOffset = ZoneOffset.systemDefault().rules.getOffset(endInstant),
                    mindfulnessSessionType =
                        MindfulnessSessionRecord.MINDFULNESS_SESSION_TYPE_MEDITATION,
                    title = presetName ?: "Meditation",
                    metadata = Metadata.manualEntry(
                        clientRecordId = "mtimer_${session.id}"
                    )
                )
            )
        )
        Log.i(TAG, "Synced session ${session.id} to Health Connect")
    } catch (e: Exception) {
        // Never rethrow — sync failure must not crash the app or the worker
        Log.e(TAG, "Failed to insert HC record for session ${session.id}", e)
    }
}

@OptIn(ExperimentalMindfulnessSessionApi::class)
suspend fun getWeeklyMindfulnessMinutes(context: Context): Int {
    // 1. Check SDK is present
    if (HealthConnectClient.getSdkStatus(context) != HealthConnectClient.SDK_AVAILABLE) {
        Log.d(TAG, "Health Connect not available — returning 0")
        return 0
    }

    val client = HealthConnectClient.getOrCreate(context)

    // 2. Check read permission — return 0 silently if not yet granted
    if (!hasPermissions(client, requireRead = true)) return 0

    return try {
        val weekAgo = Instant.now().minus(7, ChronoUnit.DAYS)
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = MindfulnessSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.after(weekAgo)
            )
        )
        val totalMinutes = response.records
            .sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }
            .toInt()
        Log.d(TAG, "Weekly HC mindfulness: ${response.records.size} sessions = $totalMinutes min")
        totalMinutes
    } catch (e: Exception) {
        // Do NOT rethrow — widget and UI must degrade gracefully to 0
        Log.e(TAG, "Unexpected error reading HC records", e)
        0
    }
}