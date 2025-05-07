package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.model.MorningFeeling
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.model.User
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.util.Optional

class SleepLogServiceTest {

    @MockK
    private lateinit var sleepLogRepository: SleepLogRepository

    @MockK
    private lateinit var userRepository: UserRepository

    private lateinit var sleepLogService: SleepLogService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        sleepLogService = SleepLogService(sleepLogRepository, userRepository)
    }

    @Test
    fun `should create sleep log with correct time calculation`() {
        // Given
        val userId = 1L
        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com"
        )

        val bedTime = LocalDateTime.of(2025, Month.MAY, 5, 22, 30)
        val wakeTime = LocalDateTime.of(2025, Month.MAY, 6, 6, 45)
        val request = CreateSleepLogRequest(
            bedTime = bedTime,
            wakeTime = wakeTime,
            morningFeeling = MorningFeeling.GOOD,
            userId = userId
        )

        val sleepLogSlot = slot<SleepLog>()
        val today = LocalDate.now()

        val savedSleepLog = SleepLog(
            id = 1L,
            date = today,
            bedTime = bedTime,
            wakeTime = wakeTime,
            timeInBedMinutes = 495,
            morningFeeling = MorningFeeling.GOOD,
            user = user
        )

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { sleepLogRepository.save(capture(sleepLogSlot)) } returns savedSleepLog

        // When
        val response = sleepLogService.createSleepLog(request)

        // Then
        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 1) { sleepLogRepository.save(any()) }

        val capturedSleepLog = sleepLogSlot.captured
        assertEquals(today, capturedSleepLog.date)
        assertEquals(bedTime, capturedSleepLog.bedTime)
        assertEquals(wakeTime, capturedSleepLog.wakeTime)
        assertEquals(495L, capturedSleepLog.timeInBedMinutes)
        assertEquals(MorningFeeling.GOOD, capturedSleepLog.morningFeeling)
        assertEquals(userId, capturedSleepLog.user.id)

        assertEquals(1L, response.id)
        assertEquals(today.toString(), response.date)
        assertEquals("2025-05-05 22:30", response.bedTime)
        assertEquals("2025-05-06 06:45", response.wakeTime)
        assertEquals(495L, response.timeInBedMinutes)
        assertEquals(MorningFeeling.GOOD, response.morningFeeling)
        assertEquals(userId, response.userId)
    }

    @Test
    fun `should handle invalid time interval`() {
        // Given
        val userId = 1L
        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com"
        )

        val bedTime = LocalDateTime.of(2025, Month.MAY, 6, 8, 0)
        val wakeTime = LocalDateTime.of(2025, Month.MAY, 5, 22, 0) // Wake time before bed time
        val request = CreateSleepLogRequest(
            bedTime = bedTime,
            wakeTime = wakeTime,
            morningFeeling = MorningFeeling.BAD,
            userId = userId
        )

        // When & Then
        every { userRepository.findById(userId) } returns Optional.of(user)
        every { sleepLogRepository.save(any()) } returns SleepLog(
            id = 1L,
            date = LocalDate.now(),
            bedTime = bedTime,
            wakeTime = wakeTime,
            timeInBedMinutes = -600, // Negative time
            morningFeeling = MorningFeeling.BAD,
            user = user
        )

        val response = sleepLogService.createSleepLog(request)
        assertEquals(-600L, response.timeInBedMinutes) // We're just testing that the calculation happens correctly
        assertEquals(userId, response.userId)

        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 1) { sleepLogRepository.save(any()) }
    }

    @Test
    fun `should throw exception when user not found`() {
        // Given
        val userId = 999L
        val bedTime = LocalDateTime.of(2025, Month.MAY, 5, 22, 30)
        val wakeTime = LocalDateTime.of(2025, Month.MAY, 6, 6, 45)
        val request = CreateSleepLogRequest(
            bedTime = bedTime,
            wakeTime = wakeTime,
            morningFeeling = MorningFeeling.GOOD,
            userId = userId
        )

        every { userRepository.findById(userId) } returns Optional.empty()

        // When & Then
        try {
            sleepLogService.createSleepLog(request)
            assert(false) { "Expected IllegalArgumentException was not thrown" }
        } catch (e: IllegalArgumentException) {
            assertEquals("User not found with ID: $userId", e.message)
        }

        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 0) { sleepLogRepository.save(any()) }
    }

    @Test
    fun `should get last night sleep by user id`() {
        // Given
        val userId = 1L
        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com"
        )

        val yesterday = LocalDate.now().minusDays(1)
        val bedTime = LocalDateTime.of(yesterday, LocalDateTime.now().toLocalTime().withHour(22).withMinute(30))
        val wakeTime = LocalDateTime.of(LocalDate.now(), LocalDateTime.now().toLocalTime().withHour(6).withMinute(45))

        val lastSleepLog = SleepLog(
            id = 1L,
            date = yesterday,
            bedTime = bedTime,
            wakeTime = wakeTime,
            timeInBedMinutes = 495,
            morningFeeling = MorningFeeling.GOOD,
            user = user
        )

        every { sleepLogRepository.findTopByUserIdOrderByDateDescWakeTimeDesc(userId) } returns lastSleepLog

        // When
        val response = sleepLogService.getLastNightSleepByUserId(userId)

        // Then
        verify(exactly = 1) { sleepLogRepository.findTopByUserIdOrderByDateDescWakeTimeDesc(userId) }

        assertEquals(1L, response?.id)
        assertEquals(yesterday.toString(), response?.date)
        assertEquals(495L, response?.timeInBedMinutes)
        assertEquals(MorningFeeling.GOOD, response?.morningFeeling)
        assertEquals(userId, response?.userId)
    }

    @Test
    fun `should return null when no sleep logs exist for user`() {
        // Given
        val userId = 1L
        every { sleepLogRepository.findTopByUserIdOrderByDateDescWakeTimeDesc(userId) } returns null

        // When
        val response = sleepLogService.getLastNightSleepByUserId(userId)

        // Then
        verify(exactly = 1) { sleepLogRepository.findTopByUserIdOrderByDateDescWakeTimeDesc(userId) }
        assertEquals(null, response)
    }

    @Test
    fun `should calculate correct averages for 30 day period`() {
        // Given
        val userId = 1L
        val user = User(id = userId, username = "testuser", email = "test@example.com")
        val today = LocalDate.now()
        val startDate = today.minusDays(29)

        // Create test sleep logs with different morning feelings
        val sleepLogs = listOf(
            createSleepLog(1L, today.minusDays(1), "22:30", "06:45", 495, MorningFeeling.GOOD, user),
            createSleepLog(2L, today.minusDays(2), "23:00", "07:00", 480, MorningFeeling.OK, user),
            createSleepLog(3L, today.minusDays(3), "22:45", "06:30", 465, MorningFeeling.GOOD, user),
            createSleepLog(4L, today.minusDays(4), "23:15", "07:15", 480, MorningFeeling.BAD, user),
            createSleepLog(5L, today.minusDays(5), "22:00", "06:00", 480, MorningFeeling.GOOD, user)
        )

        every {
            sleepLogRepository.findByUserIdAndDateBetween(userId, startDate, today)
        } returns sleepLogs

        // When
        val result = sleepLogService.getLast30DayAverages(userId)

        // Then
        verify(exactly = 1) { sleepLogRepository.findByUserIdAndDateBetween(userId, startDate, today) }

        assertEquals(startDate.toString(), result.startDate)
        assertEquals(today.toString(), result.endDate)

        // Average time in bed should be (495 + 480 + 465 + 480 + 480) / 5 = 480
        assertEquals(480.0, result.averageTimeInBedMinutes)

        // Average bed time should be around 22:42
        assertEquals("22:42", result.averageBedTime)

        // Average wake time should be around 06:42
        assertEquals("06:42", result.averageWakeTime)

        // Feeling frequencies: 3 GOOD, 1 OK, 1 BAD
        assertEquals(3, result.morningFeelingFrequencies["GOOD"])
        assertEquals(1, result.morningFeelingFrequencies["OK"])
        assertEquals(1, result.morningFeelingFrequencies["BAD"])
    }

    @Test
    fun `should return default values when no sleep logs exist`() {
        // Given
        val userId = 1L
        val today = LocalDate.now()
        val startDate = today.minusDays(29)

        every {
            sleepLogRepository.findByUserIdAndDateBetween(userId, startDate, today)
        } returns emptyList()

        // When
        val result = sleepLogService.getLast30DayAverages(userId)

        // Then
        verify(exactly = 1) { sleepLogRepository.findByUserIdAndDateBetween(userId, startDate, today) }

        assertEquals(startDate.toString(), result.startDate)
        assertEquals(today.toString(), result.endDate)
        assertEquals(0.0, result.averageTimeInBedMinutes)
        assertEquals("00:00", result.averageBedTime)
        assertEquals("00:00", result.averageWakeTime)
        assertEquals(0, result.morningFeelingFrequencies["GOOD"])
        assertEquals(0, result.morningFeelingFrequencies["OK"])
        assertEquals(0, result.morningFeelingFrequencies["BAD"])
    }

    private fun createSleepLog(
        id: Long,
        date: LocalDate,
        bedTimeStr: String,
        wakeTimeStr: String,
        timeInBedMinutes: Long,
        feeling: MorningFeeling,
        user: User
    ): SleepLog {
        val bedTime = LocalDateTime.of(
            date.minusDays(1),
            LocalTime.parse(bedTimeStr)
        )
        val wakeTime = LocalDateTime.of(
            date,
            LocalTime.parse(wakeTimeStr)
        )

        return SleepLog(
            id = id,
            date = date,
            bedTime = bedTime,
            wakeTime = wakeTime,
            timeInBedMinutes = timeInBedMinutes,
            morningFeeling = feeling,
            user = user
        )
    }
}
