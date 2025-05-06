package com.noom.interview.fullstack.sleep

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.noom.interview.fullstack.sleep.SleepApplication.Companion.UNIT_TEST_PROFILE
import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.model.MorningFeeling
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
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

    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
    }

    @AfterEach
    fun cleanup() {
        sleepLogRepository.deleteAll()
    }

    @Test
    fun `should create sleep log and store in database`() {
        // Given
        val bedTime = LocalDateTime.of(2025, Month.MAY, 5, 22, 30)
        val wakeTime = LocalDateTime.of(2025, Month.MAY, 6, 6, 45)
        val request = CreateSleepLogRequest(
            bedTime = bedTime,
            wakeTime = wakeTime,
            morningFeeling = MorningFeeling.GOOD
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

        // Verify data was saved to the database
        val sleepLogs = sleepLogRepository.findAll()
        assert(sleepLogs.size == 1)
        assert(sleepLogs[0].timeInBedMinutes == 495L)
        assert(sleepLogs[0].morningFeeling == MorningFeeling.GOOD)
    }
}