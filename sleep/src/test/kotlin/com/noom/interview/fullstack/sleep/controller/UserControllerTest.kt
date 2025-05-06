package com.noom.interview.fullstack.sleep.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.noom.interview.fullstack.sleep.dto.CreateUserRequest
import com.noom.interview.fullstack.sleep.dto.UserResponse
import com.noom.interview.fullstack.sleep.service.UserService
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserController::class)
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var userService: UserService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should create user and return 201 status`() {
        // Given
        val request = CreateUserRequest(
            username = "testuser",
            email = "test@example.com"
        )

        val response = UserResponse(
            id = 1L,
            username = "testuser",
            email = "test@example.com"
        )

        every { userService.createUser(any()) } returns response

        // When & Then
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))

        verify(exactly = 1) { userService.createUser(any()) }
    }

    @Test
    fun `should get user by id and return 200 status when user exists`() {
        // Given
        val userId = 1L
        val response = UserResponse(
            id = userId,
            username = "testuser",
            email = "test@example.com"
        )

        every { userService.getUserById(userId) } returns response

        // When & Then
        mockMvc.perform(
            get("/api/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))

        verify(exactly = 1) { userService.getUserById(userId) }
    }

    @Test
    fun `should return 404 status when user does not exist`() {
        // Given
        val userId = 999L
        every { userService.getUserById(userId) } returns null

        // When & Then
        mockMvc.perform(
            get("/api/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)

        verify(exactly = 1) { userService.getUserById(userId) }
    }

    @Test
    fun `should get all users and return 200 status`() {
        // Given
        val responses = listOf(
            UserResponse(id = 1L, username = "user1", email = "user1@example.com"),
            UserResponse(id = 2L, username = "user2", email = "user2@example.com"),
            UserResponse(id = 3L, username = "user3", email = "user3@example.com")
        )

        every { userService.getAllUsers() } returns responses

        // When & Then
        mockMvc.perform(
            get("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].username").value("user1"))
            .andExpect(jsonPath("$[0].email").value("user1@example.com"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[2].id").value(3))

        verify(exactly = 1) { userService.getAllUsers() }
    }

    @Test
    fun `should return empty array when no users exist`() {
        // Given
        every { userService.getAllUsers() } returns emptyList()

        // When & Then
        mockMvc.perform(
            get("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(0))

        verify(exactly = 1) { userService.getAllUsers() }
    }

    @Test
    fun `should handle invalid request format`() {
        // Given - an invalid JSON request
        val invalidJson = """
            {
                "username": "testuser"
                // missing email field and has invalid JSON format
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { userService.createUser(any()) }
    }
}