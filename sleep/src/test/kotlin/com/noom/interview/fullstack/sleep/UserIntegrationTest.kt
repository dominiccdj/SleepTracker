package com.noom.interview.fullstack.sleep

import com.fasterxml.jackson.databind.ObjectMapper
import com.noom.interview.fullstack.sleep.SleepApplication.Companion.UNIT_TEST_PROFILE
import com.noom.interview.fullstack.sleep.dto.CreateUserRequest
import com.noom.interview.fullstack.sleep.repository.UserRepository
import org.junit.jupiter.api.AfterEach
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(UNIT_TEST_PROFILE)
class UserIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @AfterEach
    fun cleanup() {
        userRepository.deleteAll()
    }

    @Test
    fun `should create user and retrieve it`() {
        // Given
        val request = CreateUserRequest(
            username = "integrationuser",
            email = "integration@example.com"
        )

        // Create user
        val createResult = mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.username").value("integrationuser"))
            .andReturn()

        // Extract user ID from response
        val responseJson = createResult.response.contentAsString
        val userId = objectMapper.readTree(responseJson).get("id").asLong()

        // Retrieve created user
        mockMvc.perform(
            get("/api/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.username").value("integrationuser"))
            .andExpect(jsonPath("$.email").value("integration@example.com"))

        // Verify user exists in database
        val users = userRepository.findAll()
        assert(users.size == 1)
        assert(users[0].username == "integrationuser")
        assert(users[0].email == "integration@example.com")
    }

    @Test
    fun `should create multiple users and retrieve all`() {
        // Given
        val requests = listOf(
            CreateUserRequest(username = "user1", email = "user1@example.com"),
            CreateUserRequest(username = "user2", email = "user2@example.com"),
            CreateUserRequest(username = "user3", email = "user3@example.com")
        )

        // Create users
        for (request in requests) {
            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
        }

        // Retrieve all users
        mockMvc.perform(
            get("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].username").value("user1"))
            .andExpect(jsonPath("$[1].username").value("user2"))
            .andExpect(jsonPath("$[2].username").value("user3"))

        // Verify users exist in database
        val users = userRepository.findAll()
        assert(users.size == 3)
    }
}
