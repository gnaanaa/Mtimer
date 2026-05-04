package com.gnaanaa.mtimer.domain.model

data class Preset(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val prepareSeconds: Int = 0,
    val startSoundId: String = "bell_tibetan",
    val durationSeconds: Int = 600,
    val intervalSeconds: Int = 0, // 0 means no interval chimes
    val intervalSoundId: String = "chime_soft",
    val endSoundId: String = "bell_tibetan",
    val colorAccent: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)
