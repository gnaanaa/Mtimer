package com.gnaanaa.mtimer.domain.usecase

import com.gnaanaa.mtimer.data.repository.PresetRepository
import com.gnaanaa.mtimer.domain.model.Preset
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SavePresetUseCaseTest {

    private val repository: PresetRepository = mockk(relaxed = true)
    private val savePresetUseCase = SavePresetUseCase(repository)

    @Test
    fun `when use case is called then it should call repository savePreset`() = runTest {
        // Given
        val preset = Preset(name = "Test Preset")

        // When
        savePresetUseCase(preset)

        // Then
        coVerify { repository.savePreset(preset) }
    }
}
