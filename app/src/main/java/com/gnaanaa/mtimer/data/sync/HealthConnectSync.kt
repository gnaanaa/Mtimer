package com.gnaanaa.mtimer.data.sync

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.feature.ExperimentalMindfulnessSessionApi
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.MindfulnessSessionRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.gnaanaa.mtimer.domain.model.Session
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

private const val TAG = "HealthConnectSync"

private fun isHealthConnectAvailable(context: Context): Boolean {
    return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
}

private suspend fun getGrantedPermissions(context: Context): Set<String> {
    val client = HealthConnectClient.getOrCreate(context)
    return client.permissionController.getGrantedPermissions()
}

@OptIn(ExperimentalMindfulnessSessionApi::class)
suspend fun hasAnyWritePermission(context: Context): Boolean {
    if (HealthConnectClient.getSdkStatus(context) != HealthConnectClient.SDK_AVAILABLE) return false
    
    val granted = getGrantedPermissions(context)
    val writeMindfulness = HealthPermission.getWritePermission(MindfulnessSessionRecord::class)
    
    return granted.contains(writeMindfulness)
}

suspend fun fetchHeartRateRange(context: Context, startTime: Instant, endTime: Instant): List<HeartRateRecord.Sample> {
    val client = HealthConnectClient.getOrCreate(context)
    val granted = getGrantedPermissions(context)
    
    if (!granted.contains(HealthPermission.getReadPermission(HeartRateRecord::class))) {
        return emptyList()
    }

    return try {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        response.records.flatMap { it.samples }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to read heart rate records", e)
        emptyList()
    }
}

private fun hasPermission(granted: Set<String>, permission: String): Boolean {
    return granted.contains(permission)
}

@OptIn(ExperimentalMindfulnessSessionApi::class)
private suspend fun isMindfulnessSupported(client: HealthConnectClient): Boolean {
    return client.features.getFeatureStatus(
        HealthConnectFeatures.FEATURE_MINDFULNESS_SESSION
    ) == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
}

private fun buildMetadata(sessionId: Long, type: String): Metadata {
    return Metadata.activelyRecorded(
        device = Device(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            type = Device.TYPE_PHONE
        ),
        clientRecordId = "com.gnaanaa.mtimer:${type}_$sessionId",
        clientRecordVersion = 1
    )
}

@OptIn(ExperimentalMindfulnessSessionApi::class)
suspend fun hasAllPermissions(context: Context): Boolean {
    if (HealthConnectClient.getSdkStatus(context) != HealthConnectClient.SDK_AVAILABLE) return false
    
    val granted = getGrantedPermissions(context)
    val permissions = setOf(
        HealthPermission.getWritePermission(MindfulnessSessionRecord::class),
        HealthPermission.getReadPermission(MindfulnessSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class)
    )
    
    return granted.containsAll(permissions)
}

fun openHealthConnectSettings(context: Context) {
    try {
        val intent = android.content.Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to open Health Connect settings", e)
    }
}

@OptIn(ExperimentalMindfulnessSessionApi::class)
suspend fun getWeeklyMindfulnessMinutes(context: Context): Int {
    if (HealthConnectClient.getSdkStatus(context) != HealthConnectClient.SDK_AVAILABLE) return 0
    
    val client = HealthConnectClient.getOrCreate(context)
    val granted = getGrantedPermissions(context)
    
    if (!granted.contains(HealthPermission.getReadPermission(MindfulnessSessionRecord::class))) {
        return 0
    }

    val now = Instant.now()
    val weekAgo = now.minus(7, ChronoUnit.DAYS)

    return try {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = MindfulnessSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.after(weekAgo)
            )
        )
        response.records
            .sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }
            .toInt()
    } catch (e: Exception) {
        Log.e(TAG, "Failed to read mindfulness records", e)
        0
    }
}

@OptIn(ExperimentalMindfulnessSessionApi::class)
suspend fun syncSessionToHealthConnect(
    context: Context,
    session: Session,
    presetName: String?
): Boolean {
    if (!isHealthConnectAvailable(context)) {
        Log.w(TAG, "Health Connect not available")
        return false
    }

    val client = HealthConnectClient.getOrCreate(context)
    val granted = getGrantedPermissions(context)

    val writeMindfulness = HealthPermission.getWritePermission(MindfulnessSessionRecord::class)
    val readHeartRate = HealthPermission.getReadPermission(HeartRateRecord::class)
    val writeHeartRate = HealthPermission.getWritePermission(HeartRateRecord::class)

    val canWriteMindfulness = hasPermission(granted, writeMindfulness)
    val canReadHeartRate = hasPermission(granted, readHeartRate)
    val canWriteHeartRate = hasPermission(granted, writeHeartRate)

    if (!canWriteMindfulness) {
        Log.w(TAG, "No mindfulness write permissions granted")
        return false
    }

    val mindfulnessSupported = isMindfulnessSupported(client)

    val start = Instant.ofEpochMilli(session.startTime)
    val end = if (session.endTime > session.startTime) {
        Instant.ofEpochMilli(session.endTime)
    } else {
        start.plusSeconds(session.durationSeconds.coerceAtLeast(1).toLong())
    }

    val startOffset = ZoneOffset.systemDefault().rules.getOffset(start)
    val endOffset = ZoneOffset.systemDefault().rules.getOffset(end)

    val title = presetName?.let { "MTimer: $it" } ?: "MTimer Meditation"

    val records = mutableListOf<Record>()

    // ✅ Fetch heart rate if permission exists to include in notes and re-write as our own record
    var heartRateSummary = ""
    var hrSamples = emptyList<HeartRateRecord.Sample>()

    if (canReadHeartRate) {
        try {
            // Read samples from other providers (e.g. Fitbit)
            val hrReadResponse = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            hrSamples = hrReadResponse.records.flatMap { it.samples }

            val hrAggResponse = client.aggregate(
                AggregateRequest(
                    metrics = setOf(HeartRateRecord.BPM_AVG, HeartRateRecord.BPM_MAX),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            val avg = hrAggResponse[HeartRateRecord.BPM_AVG]
            val max = hrAggResponse[HeartRateRecord.BPM_MAX]
            if (avg != null) {
                heartRateSummary = "\nHeart Rate: Avg ${avg} bpm, Max ${max} bpm"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read/aggregate HR", e)
        }
    }

    // ✅ Insert HeartRateRecord if samples were found and we have write permission
    // This helps Google Fit show the graph specifically for this time period
    if (canWriteHeartRate && hrSamples.isNotEmpty()) {
        val hrRecord = HeartRateRecord(
            startTime = start,
            startZoneOffset = startOffset,
            endTime = end,
            endZoneOffset = endOffset,
            metadata = buildMetadata(session.id, "heart_rate"),
            samples = hrSamples
        )
        records.add(hrRecord)
    }

    // ✅ Insert Mindfulness only if supported
    if (canWriteMindfulness && mindfulnessSupported) {
        val mindfulness = MindfulnessSessionRecord(
            startTime = start,
            startZoneOffset = startOffset,
            endTime = end,
            endZoneOffset = endOffset,
            metadata = buildMetadata(session.id, "mindfulness"),
            mindfulnessSessionType = MindfulnessSessionRecord.MINDFULNESS_SESSION_TYPE_MEDITATION,
            title = title,
            notes = "Meditation session recorded via MTimer$heartRateSummary"
        )
        records.add(mindfulness)
    }

    if (records.isEmpty()) {
        Log.w(TAG, "No records to insert")
        return false
    }

    return try {
        val response = client.insertRecords(records)
        Log.i(TAG, "Inserted ${response.recordIdsList.size} records for session ${session.id}")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Insert failed for session ${session.id}", e)
        false
    }
}