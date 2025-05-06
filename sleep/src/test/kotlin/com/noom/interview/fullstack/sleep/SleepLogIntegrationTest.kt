package com.noom.interview.fullstack.sleep

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.noom.interview.fullstack.sleep.SleepApplication.Companion.UNIT_TEST_PROFILE
import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.CreateUserRequest
import com.noom.interview.fullstack.sleep.model.MorningFeeling
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.time.Month

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(UNIT_TEST_PROFILE)
class SleepLogIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var sleepLogRepository: SleepLogRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var objectMapper: ObjectMapper

    private var userId: Long = 0

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())

        // Create a test user for all tests
        val createUserRequest = CreateUserRequest(
            username = "testuser",
            email = "test@example.com"
        )

        val result = mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        // Extract the user ID from the response
        val responseJson = result.response.contentAsString
        userId = objectMapper.readTree(responseJson).get("id").asLong()
    }

    @AfterEach
    fun cleanup() {
        sleepLogRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `should create sleep log and store in database`() {
        // Given
        val bedTime = LocalDateTime.of(2025, Month.MAY, 5, 22, 30)
        val wakeTime = LocalDateTime.of(2025, Month.MAY, 6, 6, 45)
        val request = CreateSleepLogRequest(
            bedTime = bedTime,
            wakeTime = wakeTime,
            morningFeeling = MorningFeeling.GOOD,
            userId = userId
        )

        // When & Then
        mockMvc.perform(
            post("/api/sleep-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.date").isNotEmpty)
            .andExpect(jsonPath("$.bedTime").value("2025-05-05 22:30"))
            .andExpect(jsonPath("$.wakeTime").value("2025-05-06 06:45"))
            .andExpect(jsonPath("$.timeInBedMinutes").value(495))
            .andExpect(jsonPath("$.morningFeeling").value("GOOD"))
            .andExpect(jsonPath("$.userId").value(userId))

        // Verify data was saved to the database
        val sleepLogs = sleepLogRepository.findAll()
        assert(sleepLogs.size == 1)
        assert(sleepLogs[0].timeInBedMinutes == 495L)
        assert(sleepLogs[0].morningFeeling == MorningFeeling.GOOD)
        assert(sleepLogs[0].user.id == userId)
    }

    @Test
    fun `should create multiple sleep logs and retrieve them by user`() {
        // Given - create first sleep log
        val bedTime1 = LocalDateTime.of(2025, Month.MAY, 5, 22, 30)
        val wakeTime1 = LocalDateTime.of(2025, Month.MAY, 6, 6, 45)
        val request1 = CreateSleepLogRequest(
            bedTime = bedTime1,
            wakeTime = wakeTime1,
            morningFeeling = MorningFeeling.GOOD,
            userId = userId
        )

        mockMvc.perform(
            post("/api/sleep-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1))
        )
            .andExpect(status().isCreated)

        // Create second sleep log
        val bedTime2 = LocalDateTime.of(2025, Month.MAY, 6, 23, 0)
        val wakeTime2 = LocalDateTime.of(2025, Month.MAY, 7, 7, 0)
        val request2 = CreateSleepLogRequest(
            bedTime = bedTime2,
            wakeTime = wakeTime2,
            morningFeeling = MorningFeeling.OK,
            userId = userId
        )

        mockMvc.perform(
            post("/api/sleep-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2))
        )
            .andExpect(status().isCreated)

        // When & Then - retrieve all sleep logs for the user
        mockMvc.perform(
            get("/api/sleep-logs/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].userId").value(userId))
            .andExpect(jsonPath("$[1].userId").value(userId))

        // Verify data was saved to the database
        val sleepLogs = sleepLogRepository.findAllByUserId(userId)
        assert(sleepLogs.size == 2)
    }

    @Test
    fun `should retrieve last night sleep log`() {
        // Given - create two sleep logs on different dates
        val bedTime1 = LocalDateTime.of(2025, Month.MAY, 4, 22, 0)
        val wakeTime1 = LocalDateTime.of(2025, Month.MAY, 5, 6, 0)
        val request1 = CreateSleepLogRequest(
            bedTime = bedTime1,
            wakeTime = wakeTime1,
            morningFeeling = MorningFeeling.BAD,
            userId = userId
        )

        mockMvc.perform(
            post("/api/sleep-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1))
        )
            .andExpect(status().isCreated)

        // Create more recent sleep log
        val bedTime2 = LocalDateTime.of(2025, Month.MAY, 5, 23, 0)
        val wakeTime2 = LocalDateTime.of(2025, Month.MAY, 6, 7, 0)
        val request2 = CreateSleepLogRequest(
            bedTime = bedTime2,
            wakeTime = wakeTime2,
            morningFeeling = MorningFeeling.GOOD,
            userId = userId
        )

        mockMvc.perform(
            post("/api/sleep-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2))
        )
            .andExpect(status().isCreated)

        // When & Then - retrieve last night's sleep log
        mockMvc.perform(
            get("/api/sleep-logs/users/$userId/last-night")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.morningFeeling").value("GOOD"))
            .andExpect(jsonPath("$.userId").value(userId))

        // The most recent sleep log should have the GOOD feeling
        val lastSleepLog = sleepLogRepository.findTopByUserIdOrderByDateDescWakeTimeDesc(userId)
        assert(lastSleepLog?.morningFeeling == MorningFeeling.GOOD)
    }

    @Test
    fun `should return 404 when retrieving last night sleep for non-existent user`() {
        // Given - a user ID that doesn't exist
        val nonExistentUserId = 999L

        // When & Then
        mockMvc.perform(
            get("/api/sleep-logs/users/$nonExistentUserId/last-night")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
    }
}
