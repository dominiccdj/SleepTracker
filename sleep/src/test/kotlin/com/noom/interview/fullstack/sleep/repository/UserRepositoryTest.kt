package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.SleepApplication.Companion.UNIT_TEST_PROFILE
import com.noom.interview.fullstack.sleep.model.User
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles
import org.junit.jupiter.api.Assertions.*

@DataJpaTest
@ActiveProfiles(UNIT_TEST_PROFILE)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `test findByUsername returns user when exists`() {
        // Given
        val username = "testuser"
        val user = User(username = username, email = "testuser@example.com")
        userRepository.save(user)

        // When
        val foundUser = userRepository.findByUsername(username)

        // Then
        assertNotNull(foundUser)
        assertEquals(username, foundUser?.username)
    }

    @Test
    fun `test findByUsername returns null when user does not exist`() {
        // When
        val foundUser = userRepository.findByUsername("nonexistent")

        // Then
        assertNull(foundUser)
    }

    @Test
    fun `test findByEmail returns user when exists`() {
        // Given
        val email = "testemail@example.com"
        val user = User(username = "useremail", email = email)
        userRepository.save(user)

        // When
        val foundUser = userRepository.findByEmail(email)

        // Then
        assertNotNull(foundUser)
        assertEquals(email, foundUser?.email)
    }

    @Test
    fun `test findByEmail returns null when email does not exist`() {
        // When
        val foundUser = userRepository.findByEmail("nonexistent@example.com")

        // Then
        assertNull(foundUser)
    }

    @Test
    fun `test case sensitivity of username search`() {
        // Given
        val username = "TestUser"
        val user = User(username = username, email = "testcase@example.com")
        userRepository.save(user)

        // When
        val foundExactCase = userRepository.findByUsername("TestUser")
        val foundLowerCase = userRepository.findByUsername("testuser")

        // Then
        assertNotNull(foundExactCase)
        assertNull(foundLowerCase) // JPA's default behavior is case-sensitive
    }

    @Test
    fun `test case sensitivity of email search`() {
        // Given
        val email = "Test.Email@example.com"
        val user = User(username = "emailuser", email = email)
        userRepository.save(user)

        // When
        val foundExactCase = userRepository.findByEmail("Test.Email@example.com")
        val foundLowerCase = userRepository.findByEmail("test.email@example.com")

        // Then
        assertNotNull(foundExactCase)
        assertNull(foundLowerCase) // JPA's default behavior is case-sensitive
    }
}
