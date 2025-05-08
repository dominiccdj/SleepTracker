package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.SleepApplication.Companion.UNIT_TEST_PROFILE
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.model.MorningFeeling
import com.noom.interview.fullstack.sleep.model.User
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

@DataJpaTest
@ActiveProfiles(UNIT_TEST_PROFILE)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SleepLogRepositoryTest {

    @Autowired
    private lateinit var sleepLogRepository: SleepLogRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var user1: User
    private lateinit var user2: User
    private lateinit var user3: User

    @BeforeEach
    fun setup() {
        // Clear any existing data
        sleepLogRepository.deleteAll()
        userRepository.deleteAll()

        // Create and save users first
        user1 = userRepository.save(User(username = "user1", email = "user1@example.com"))
        user2 = userRepository.save(User(username = "user2", email = "user2@example.com"))
        user3 = userRepository.save(User(username = "user3", email = "user3@example.com"))
    }

    @Test
    fun `test findTopByUserIdOrderByDateDescWakeTimeDesc returns latest sleep log`() {
        // Create sleep logs for user1
        val sleepLog1 = SleepLog(
            date = LocalDate.of(2025, Month.MAY, 5),
            bedTime = LocalDateTime.of(2025, Month.MAY, 4, 22, 0),
            wakeTime = LocalDateTime.of(2025, Month.MAY, 5, 6, 0),
            timeInBedMinutes = 480,
            morningFeeling = MorningFeeling.GOOD,
            user = user1
        )
        val sleepLog2 = SleepLog(
            date = LocalDate.of(2025, Month.MAY, 6),
            bedTime = LocalDateTime.of(2025, Month.MAY, 5, 23, 0),
            wakeTime = LocalDateTime.of(2025, Month.MAY, 6, 7, 0),
            timeInBedMinutes = 480,
            morningFeeling = MorningFeeling.OK,
            user = user1
        )

        sleepLogRepository.save(sleepLog1)
        sleepLogRepository.save(sleepLog2)

        val latestSleepLog = sleepLogRepository.findTopByUserIdOrderByDateDescWakeTimeDesc(user1.id!!)

        assertNotNull(latestSleepLog)
        assertEquals(LocalDate.of(2025, Month.MAY, 6), latestSleepLog?.date)
        assertEquals(LocalDateTime.of(2025, Month.MAY, 6, 7, 0), latestSleepLog?.wakeTime)
    }

    @Test
    fun `test findAllByUserId returns all sleep logs for user`() {
        // Create sleep logs for user2
        val sleepLog1 = SleepLog(
            date = LocalDate.of(2025, Month.MAY, 1),
            bedTime = LocalDateTime.of(2025, Month.APRIL, 30, 22, 0),
            wakeTime = LocalDateTime.of(2025, Month.MAY, 1, 6, 0),
            timeInBedMinutes = 480,
            morningFeeling = MorningFeeling.BAD,
            user = user2
        )
        val sleepLog2 = SleepLog(
            date = LocalDate.of(2025, Month.MAY, 2),
            bedTime = LocalDateTime.of(2025, Month.MAY, 1, 23, 0),
            wakeTime = LocalDateTime.of(2025, Month.MAY, 2, 7, 0),
            timeInBedMinutes = 480,
            morningFeeling = MorningFeeling.OK,
            user = user2
        )

        sleepLogRepository.save(sleepLog1)
        sleepLogRepository.save(sleepLog2)

        // Create a sleep log for another user to ensure it's not returned
        sleepLogRepository.save(
            SleepLog(
                date = LocalDate.of(2025, Month.MAY, 2),
                bedTime = LocalDateTime.of(2025, Month.MAY, 1, 23, 0),
                wakeTime = LocalDateTime.of(2025, Month.MAY, 2, 7, 0),
                timeInBedMinutes = 480,
                morningFeeling = MorningFeeling.GOOD,
                user = user1
            )
        )

        val allSleepLogs = sleepLogRepository.findAllByUserId(user2.id!!)

        assertEquals(2, allSleepLogs.size)
        assertTrue(allSleepLogs.any { it.date == LocalDate.of(2025, Month.MAY, 1) })
        assertTrue(allSleepLogs.any { it.date == LocalDate.of(2025, Month.MAY, 2) })
    }

    @Test
    fun `test findByUserIdAndDateBetween returns sleep logs in date range`() {
        // Create sleep logs for user3 with different dates
        val sleepLog1 = SleepLog(
            date = LocalDate.of(2025, Month.APRIL, 25),
            bedTime = LocalDateTime.of(2025, Month.APRIL, 24, 22, 0),
            wakeTime = LocalDateTime.of(2025, Month.APRIL, 25, 6, 0),
            timeInBedMinutes = 480,
            morningFeeling = MorningFeeling.OK,
            user = user3
        )
        val sleepLog2 = SleepLog(
            date = LocalDate.of(2025, Month.MAY, 1),
            bedTime = LocalDateTime.of(2025, Month.APRIL, 30, 23, 0),
            wakeTime = LocalDateTime.of(2025, Month.MAY, 1, 7, 0),
            timeInBedMinutes = 480,
            morningFeeling = MorningFeeling.GOOD,
            user = user3
        )
        val sleepLog3 = SleepLog(
            date = LocalDate.of(2025, Month.MAY, 5),
            bedTime = LocalDateTime.of(2025, Month.MAY, 4, 22, 0),
            wakeTime = LocalDateTime.of(2025, Month.MAY, 5, 6, 0),
            timeInBedMinutes = 480,
            morningFeeling = MorningFeeling.BAD,
            user = user3
        )

        sleepLogRepository.save(sleepLog1)
        sleepLogRepository.save(sleepLog2)
        sleepLogRepository.save(sleepLog3)

        val startDate = LocalDate.of(2025, Month.APRIL, 25)
        val endDate = LocalDate.of(2025, Month.MAY, 1)

        val sleepLogsInRange = sleepLogRepository.findByUserIdAndDateBetween(user3.id!!, startDate, endDate)

        assertEquals(2, sleepLogsInRange.size)
        assertTrue(sleepLogsInRange.any { it.date == LocalDate.of(2025, Month.APRIL, 25) })
        assertTrue(sleepLogsInRange.any { it.date == LocalDate.of(2025, Month.MAY, 1) })
        assertFalse(sleepLogsInRange.any { it.date == LocalDate.of(2025, Month.MAY, 5) })
    }
}
