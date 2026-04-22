package com.gnaanaa.mtimer.domain.model

data class Session(
    val id: Long = 0,
    val presetId: String? = null,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Int,
    val completed: Boolean,
    val healthConnectSynced: Boolean = false,
    val healthConnectRecordId: String? = null
)
