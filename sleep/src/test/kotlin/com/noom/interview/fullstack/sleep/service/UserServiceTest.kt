package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateUserRequest
import com.noom.interview.fullstack.sleep.model.User
import com.noom.interview.fullstack.sleep.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class UserServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        userService = UserService(userRepository)
    }

    @Test
    fun `should create user successfully`() {
        // Given
        val request = CreateUserRequest(
            username = "testuser",
            email = "test@example.com"
        )

        val userSlot = slot<User>()
        val savedUser = User(
            id = 1L,
            username = "testuser",
            email = "test@example.com"
        )

        every { userRepository.save(capture(userSlot)) } returns savedUser

        // When
        val response = userService.createUser(request)

        // Then
        verify(exactly = 1) { userRepository.save(any()) }

        val capturedUser = userSlot.captured
        assertEquals("testuser", capturedUser.username)
        assertEquals("test@example.com", capturedUser.email)

        assertEquals(1L, response.id)
        assertEquals("testuser", response.username)
        assertEquals("test@example.com", response.email)
    }

    @Test
    fun `should get user by id when user exists`() {
        // Given
        val userId = 1L
        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com"
        )

        every { userRepository.findById(userId) } returns Optional.of(user)

        // When
        val response = userService.getUserById(userId)

        // Then
        verify(exactly = 1) { userRepository.findById(userId) }

        assertNotNull(response)
        assertEquals(userId, response?.id)
        assertEquals("testuser", response?.username)
        assertEquals("test@example.com", response?.email)
    }

    @Test
    fun `should return null when user does not exist`() {
        // Given
        val userId = 999L
        every { userRepository.findById(userId) } returns Optional.empty()

        // When
        val response = userService.getUserById(userId)

        // Then
        verify(exactly = 1) { userRepository.findById(userId) }
        assertNull(response)
    }

    @Test
    fun `should get all users`() {
        // Given
        val users = listOf(
            User(id = 1L, username = "user1", email = "user1@example.com"),
            User(id = 2L, username = "user2", email = "user2@example.com"),
            User(id = 3L, username = "user3", email = "user3@example.com")
        )

        every { userRepository.findAll() } returns users

        // When
        val responses = userService.getAllUsers()

        // Then
        verify(exactly = 1) { userRepository.findAll() }

        assertEquals(3, responses.size)
        assertEquals(1L, responses[0].id)
        assertEquals("user1", responses[0].username)
        assertEquals("user1@example.com", responses[0].email)
        assertEquals(2L, responses[1].id)
        assertEquals(3L, responses[2].id)
    }

    @Test
    fun `should return empty list when no users exist`() {
        // Given
        every { userRepository.findAll() } returns emptyList()

        // When
        val responses = userService.getAllUsers()

        // Then
        verify(exactly = 1) { userRepository.findAll() }
        assertTrue(responses.isEmpty())
    }
}