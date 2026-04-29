package com.gnaanaa.mtimer.ui.home

import com.gnaanaa.mtimer.data.repository.PresetRepository
import com.gnaanaa.mtimer.data.repository.SessionRepository
import com.gnaanaa.mtimer.domain.model.Preset
import com.gnaanaa.mtimer.domain.usecase.StartTimerUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val sessionRepository: SessionRepository = mockk()
    private val presetRepository: PresetRepository = mockk()
    private val startTimerUseCase: StartTimerUseCase = mockk(relaxed = true)
    
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { sessionRepository.getAllSessions() } returns flowOf(emptyList())
        every { presetRepository.getAllPresets() } returns flowOf(emptyList())
        
        viewModel = HomeViewModel(sessionRepository, presetRepository, startTimerUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when startTimer is called then it should call startTimerUseCase`() = runTest {
        // Given
        val preset = Preset(name = "Test")

        // When
        viewModel.startTimer(preset)

        // Then
        coVerify { startTimerUseCase(preset) }
    }
}
