package com.noom.interview.fullstack.sleep.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.ninjasquad.springmockk.MockkBean
import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.model.MorningFeeling
import com.noom.interview.fullstack.sleep.service.SleepLogService
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.time.Month

@WebMvcTest(SleepLogController::class)
class SleepLogControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var sleepLogService: SleepLogService

    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
    }

    @Test
    fun `should create sleep log and return 201 status`() {
        // Given
        val userId = 1L
        val bedTime = LocalDateTime.of(2025, Month.MAY, 5, 22, 30)
        val wakeTime = LocalDateTime.of(2025, Month.MAY, 6, 6, 45)
        val request = CreateSleepLogRequest(
            bedTime = bedTime,
            wakeTime = wakeTime,
            morningFeeling = MorningFeeling.GOOD,
            userId = userId
        )

        val response = SleepLogResponse(
            id = 1L,
            date = "2025-05-06",
            bedTime = "2025-05-05 22:30",
            wakeTime = "2025-05-06 06:45",
            timeInBedMinutes = 495,
            morningFeeling = MorningFeeling.GOOD,
            userId = userId
        )

        every { sleepLogService.createSleepLog(any()) } returns response

        // When & Then
        mockMvc.perform(
            post("/api/sleep-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.date").value("2025-05-06"))
            .andExpect(jsonPath("$.bedTime").value("2025-05-05 22:30"))
            .andExpect(jsonPath("$.wakeTime").value("2025-05-06 06:45"))
            .andExpect(jsonPath("$.timeInBedMinutes").value(495))
            .andExpect(jsonPath("$.morningFeeling").value("GOOD"))
            .andExpect(jsonPath("$.userId").value(userId))

        verify(exactly = 1) { sleepLogService.createSleepLog(any()) }
    }

    @Test
    fun `should handle invalid request format`() {
        // Given - an invalid JSON request
        val invalidJson = """
            {
                "bedTime": "not-a-date-time",
                "wakeTime": "2025-05-06T06:45:00",
                "morningFeeling": "GOOD",
                "userId": 1
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(
            post("/api/sleep-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { sleepLogService.createSleepLog(any()) }
    }

    @Test
    fun `should get last night sleep by user id`() {
        // Given
        val userId = 1L
        val response = SleepLogResponse(
            id = 1L,
            date = "2025-05-06",
            bedTime = "2025-05-05 22:30",
            wakeTime = "2025-05-06 06:45",
            timeInBedMinutes = 495,
            morningFeeling = MorningFeeling.GOOD,
            userId = userId
        )

        every { sleepLogService.getLastNightSleepByUserId(userId) } returns response

        // When & Then
        mockMvc.perform(
            get("/api/sleep-logs/users/$userId/last-night")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.date").value("2025-05-06"))
            .andExpect(jsonPath("$.bedTime").value("2025-05-05 22:30"))
            .andExpect(jsonPath("$.wakeTime").value("2025-05-06 06:45"))
            .andExpect(jsonPath("$.timeInBedMinutes").value(495))
            .andExpect(jsonPath("$.morningFeeling").value("GOOD"))
            .andExpect(jsonPath("$.userId").value(userId))

        verify(exactly = 1) { sleepLogService.getLastNightSleepByUserId(userId) }
    }

    @Test
    fun `should return 404 when no sleep logs exist for user`() {
        // Given
        val userId = 999L
        every { sleepLogService.getLastNightSleepByUserId(userId) } returns null

        // When & Then
        mockMvc.perform(
            get("/api/sleep-logs/users/$userId/last-night")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)

        verify(exactly = 1) { sleepLogService.getLastNightSleepByUserId(userId) }
    }

    @Test
    fun `should get all sleep logs by user id`() {
        // Given
        val userId = 1L
        val responses = listOf(
            SleepLogResponse(
                id = 1L,
                date = "2025-05-06",
                bedTime = "2025-05-05 22:30",
                wakeTime = "2025-05-06 06:45",
                timeInBedMinutes = 495,
                morningFeeling = MorningFeeling.GOOD,
                userId = userId
            ),
            SleepLogResponse(
                id = 2L,
                date = "2025-05-07",
                bedTime = "2025-05-06 23:00",
                wakeTime = "2025-05-07 07:00",
                timeInBedMinutes = 480,
                morningFeeling = MorningFeeling.OK,
                userId = userId
            )
        )

        every { sleepLogService.getAllSleepLogsByUserId(userId) } returns responses

        // When & Then
        mockMvc.perform(
            get("/api/sleep-logs/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].date").value("2025-05-06"))
            .andExpect(jsonPath("$[0].userId").value(userId))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].date").value("2025-05-07"))
            .andExpect(jsonPath("$[1].userId").value(userId))

        verify(exactly = 1) { sleepLogService.getAllSleepLogsByUserId(userId) }
    }

    @Test
    fun `should return empty array when user has no sleep logs`() {
        // Given
        val userId = 1L
        every { sleepLogService.getAllSleepLogsByUserId(userId) } returns emptyList()

        // When & Then
        mockMvc.perform(
            get("/api/sleep-logs/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(0))

        verify(exactly = 1) { sleepLogService.getAllSleepLogsByUserId(userId) }
    }
}
