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
        val bedTime = LocalDateTime.of(2025, Month.MAY, 5, 22, 30)
        val wakeTime = LocalDateTime.of(2025, Month.MAY, 6, 6, 45)
        val request = CreateSleepLogRequest(
            bedTime = bedTime,
            wakeTime = wakeTime,
            morningFeeling = MorningFeeling.GOOD
        )

        val response = SleepLogResponse(
            id = 1L,
            date = "2025-05-06",
            bedTime = "2025-05-05 22:30",
            wakeTime = "2025-05-06 06:45",
            timeInBedMinutes = 495,
            morningFeeling = MorningFeeling.GOOD
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

        verify(exactly = 1) { sleepLogService.createSleepLog(any()) }
    }

    @Test
    fun `should handle invalid request format`() {
        // Given - an invalid JSON request
        val invalidJson = """
            {
                "bedTime": "not-a-date-time",
                "wakeTime": "2025-05-06T06:45:00",
                "morningFeeling": "GOOD"
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
}