package com.gnaanaa.mtimer.data.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessActivities
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.SessionInsertRequest
import com.google.android.gms.fitness.data.Session as FitSession
import com.gnaanaa.mtimer.domain.model.Session
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

private const val TAG = "GoogleFitSync"

fun getGoogleFitOptions(): FitnessOptions {
    return FitnessOptions.builder()
        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_WRITE)
        .build()
}

fun hasGoogleFitPermissions(context: Context): Boolean {
    val account = GoogleSignIn.getAccountForExtension(context, getGoogleFitOptions())
    return GoogleSignIn.hasPermissions(account, getGoogleFitOptions())
}

suspend fun syncSessionToGoogleFit(
    context: Context,
    session: Session,
    heartRateSamples: List<Pair<Long, Double>> = emptyList()
): Boolean {
    val fitnessOptions = getGoogleFitOptions()
    val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
        Log.w(TAG, "No Google Fit permissions granted")
        return false
    }

    val startTime = session.startTime
    val endTime = if (session.endTime > session.startTime) {
        session.endTime
    } else {
        session.startTime + (session.durationSeconds * 1000L)
    }

    val fitSession = FitSession.Builder()
        .setName("MTimer Meditation")
        .setIdentifier("mtimer-${session.id}")
        .setDescription("Meditation session via MTimer")
        .setStartTime(startTime, TimeUnit.MILLISECONDS)
        .setEndTime(endTime, TimeUnit.MILLISECONDS)
        .setActivity(FitnessActivities.MEDITATION)
        .build()

    val insertRequestBuilder = SessionInsertRequest.Builder()
        .setSession(fitSession)

    if (heartRateSamples.isNotEmpty()) {
        val hrDataSource = DataSource.Builder()
            .setAppPackageName(context)
            .setDataType(DataType.TYPE_HEART_RATE_BPM)
            .setType(DataSource.TYPE_RAW)
            .build()

        val hrDataSet = DataSet.create(hrDataSource)
        for (sample in heartRateSamples) {
            val dp = hrDataSet.createDataPoint()
                .setTimestamp(sample.first, TimeUnit.MILLISECONDS)
            dp.getValue(Field.FIELD_BPM).setFloat(sample.second.toFloat())
            hrDataSet.add(dp)
        }
        insertRequestBuilder.addDataSet(hrDataSet)
    }

    return try {
        Fitness.getSessionsClient(context, account)
            .insertSession(insertRequestBuilder.build())
            .await()
        Log.d(TAG, "Meditation session written successfully to Google Fit")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Failed to write meditation session to Google Fit", e)
        false
    }
}
