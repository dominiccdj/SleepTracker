package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateUserRequest
import com.noom.interview.fullstack.sleep.dto.UserResponse

/**
 * Service interface for user-related operations.
 */
interface UserService {
    /**
     * Creates a new user.
     *
     * @param request The user creation request containing username and email
     * @return The created user response with generated ID
     */
    fun createUser(request: CreateUserRequest): UserResponse

    /**
     * Retrieves a user by their ID.
     *
     * @param id The ID of the user to retrieve
     * @return The user response if found, null otherwise
     */
    fun getUserById(id: Long): UserResponse?

    /**
     * Retrieves all users.
     *
     * @return A list of all users
     */
    fun getAllUsers(): List<UserResponse>
}
