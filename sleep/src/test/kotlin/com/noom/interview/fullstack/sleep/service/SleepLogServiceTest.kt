package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.model.MorningFeeling
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

class SleepLogServiceTest {

    @MockK
    private lateinit var sleepLogRepository: SleepLogRepository

    private lateinit var sleepLogService: SleepLogService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        sleepLogService = SleepLogService(sleepLogRepository)
    }

    @Test
    fun `should create sleep log with correct time calculation`() {
        // Given
        val bedTime = LocalDateTime.of(2025, Month.MAY, 5, 22, 30)
        val wakeTime = LocalDateTime.of(2025, Month.MAY, 6, 6, 45)
        val request = CreateSleepLogRequest(
            bedTime = bedTime,
            wakeTime = wakeTime,
            morningFeeling = MorningFeeling.GOOD
        )

        val sleepLogSlot = slot<SleepLog>()
        val today = LocalDate.now()

        val savedSleepLog = SleepLog(
            id = 1L,
            date = today,
            bedTime = bedTime,
            wakeTime = wakeTime,
            timeInBedMinutes = 495,
            morningFeeling = MorningFeeling.GOOD
        )

        every { sleepLogRepository.save(capture(sleepLogSlot)) } returns savedSleepLog

        // When
        val response = sleepLogService.createSleepLog(request)

        // Then
        verify(exactly = 1) { sleepLogRepository.save(any()) }

        val capturedSleepLog = sleepLogSlot.captured
        assertEquals(today, capturedSleepLog.date)
        assertEquals(bedTime, capturedSleepLog.bedTime)
        assertEquals(wakeTime, capturedSleepLog.wakeTime)
        assertEquals(495L, capturedSleepLog.timeInBedMinutes)
        assertEquals(MorningFeeling.GOOD, capturedSleepLog.morningFeeling)

        assertEquals(1L, response.id)
        assertEquals(today.toString(), response.date)
        assertEquals("2025-05-05 22:30", response.bedTime)
        assertEquals("2025-05-06 06:45", response.wakeTime)
        assertEquals(495L, response.timeInBedMinutes)
        assertEquals(MorningFeeling.GOOD, response.morningFeeling)
    }

    @Test
    fun `should handle invalid time interval`() {
        // Given
        val bedTime = LocalDateTime.of(2025, Month.MAY, 6, 8, 0)
        val wakeTime = LocalDateTime.of(2025, Month.MAY, 5, 22, 0) // Wake time before bed time
        val request = CreateSleepLogRequest(
            bedTime = bedTime,
            wakeTime = wakeTime,
            morningFeeling = MorningFeeling.BAD
        )

        // When & Then
        every { sleepLogRepository.save(any()) } returns SleepLog(
            id = 1L,
            date = LocalDate.now(),
            bedTime = bedTime,
            wakeTime = wakeTime,
            timeInBedMinutes = -600, // Negative time
            morningFeeling = MorningFeeling.BAD
        )

        val response = sleepLogService.createSleepLog(request)
        assertEquals(-600L, response.timeInBedMinutes) // We're just testing that the calculation happens correctly

        verify(exactly = 1) { sleepLogRepository.save(any()) }
    }
}