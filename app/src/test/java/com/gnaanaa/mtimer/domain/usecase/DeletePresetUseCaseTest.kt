package com.gnaanaa.mtimer.domain.usecase

import com.gnaanaa.mtimer.data.repository.PresetRepository
import com.gnaanaa.mtimer.domain.model.Preset
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeletePresetUseCaseTest {

    private val repository: PresetRepository = mockk(relaxed = true)
    private val deletePresetUseCase = DeletePresetUseCase(repository)

    @Test
    fun `when use case is called then it should call repository deletePreset`() = runTest {
        // Given
        val preset = Preset(name = "Test Preset")

        // When
        deletePresetUseCase(preset)

        // Then
        coVerify { repository.deletePreset(preset) }
    }
}
