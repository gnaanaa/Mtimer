package com.gnaanaa.mtimer.domain.usecase

import android.content.Context
import com.gnaanaa.mtimer.data.sync.getWeeklyMindfulnessMinutes
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetWeeklyStatsUseCaseTest {

    private val context: Context = mockk()
    private val getWeeklyStatsUseCase = GetWeeklyStatsUseCase(context)

    @Test
    fun `when invoke is called then it should return value from getWeeklyMindfulnessMinutes`() = runTest {
        // Given
        mockkStatic("com.gnaanaa.mtimer.data.sync.HealthConnectSyncKt")
        coEvery { getWeeklyMindfulnessMinutes(context) } returns 42

        // When
        val result = getWeeklyStatsUseCase()

        // Then
        assertEquals(42, result)
    }

    @Test
    fun `when getWeeklyMindfulnessMinutes throws exception then it should return 0`() = runTest {
        // Given
        mockkStatic("com.gnaanaa.mtimer.data.sync.HealthConnectSyncKt")
        coEvery { getWeeklyMindfulnessMinutes(context) } throws Exception("Test Exception")

        // When
        val result = getWeeklyStatsUseCase()

        // Then
        assertEquals(0, result)
    }
}
