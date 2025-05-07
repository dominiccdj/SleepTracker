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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(UNIT_TEST_PROFILE)
class SleepLogAveragesIntegrationTest {

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

        // Create a test user
        val createUserRequest = CreateUserRequest(
            username = "averagesuser",
            email = "averages@example.com"
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

        // Create multiple sleep logs for the last 30 days
        val today = LocalDate.now()

        // Create 5 sleep logs with different patterns
        createSleepLogForDate(today.minusDays(1), "22:30", "06:45", MorningFeeling.GOOD)
        createSleepLogForDate(today.minusDays(2), "23:00", "07:00", MorningFeeling.OK)
        createSleepLogForDate(today.minusDays(3), "22:45", "06:30", MorningFeeling.GOOD)
        createSleepLogForDate(today.minusDays(4), "23:15", "07:15", MorningFeeling.BAD)
        createSleepLogForDate(today.minusDays(5), "22:00", "06:00", MorningFeeling.GOOD)
    }

    @AfterEach
    fun cleanup() {
        sleepLogRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `should calculate and return 30-day averages`() {
        // When & Then
        mockMvc.perform(
            get("/api/sleep-logs/users/$userId/averages/30-day")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.startDate").exists())
            .andExpect(jsonPath("$.endDate").exists())
            .andExpect(jsonPath("$.averageTimeInBedMinutes").value(480.0))
            .andExpect(jsonPath("$.averageBedTime").value("22:42"))
            .andExpect(jsonPath("$.averageWakeTime").value("06:42"))
            .andExpect(jsonPath("$.morningFeelingFrequencies.GOOD").value(3))
            .andExpect(jsonPath("$.morningFeelingFrequencies.OK").value(1))
            .andExpect(jsonPath("$.morningFeelingFrequencies.BAD").value(1))
    }

    @Test
    fun `should return empty statistics for user with no sleep logs`() {
        // Create a new user with no sleep logs
        val createUserRequest = CreateUserRequest(
            username = "emptyuser",
            email = "empty@example.com"
        )

        val result = mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val emptyUserId = objectMapper.readTree(result.response.contentAsString).get("id").asLong()

        // When & Then
        mockMvc.perform(
            get("/api/sleep-logs/users/$emptyUserId/averages/30-day")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.averageTimeInBedMinutes").value(0.0))
            .andExpect(jsonPath("$.averageBedTime").value("00:00"))
            .andExpect(jsonPath("$.averageWakeTime").value("00:00"))
            .andExpect(jsonPath("$.morningFeelingFrequencies.GOOD").value(0))
            .andExpect(jsonPath("$.morningFeelingFrequencies.OK").value(0))
            .andExpect(jsonPath("$.morningFeelingFrequencies.BAD").value(0))
    }

    private fun createSleepLogForDate(date: LocalDate, bedTimeStr: String, wakeTimeStr: String, feeling: MorningFeeling) {
        val bedTime = LocalDateTime.of(
            date.minusDays(1),
            LocalTime.parse(bedTimeStr)
        )
        val wakeTime = LocalDateTime.of(
            date,
            LocalTime.parse(wakeTimeStr)
        )

        val request = CreateSleepLogRequest(
            bedTime = bedTime,
            wakeTime = wakeTime,
            morningFeeling = feeling,
            userId = userId
        )

        mockMvc.perform(
            post("/api/sleep-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
    }
}
