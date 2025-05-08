package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateUserRequest
import com.noom.interview.fullstack.sleep.dto.UserResponse
import com.noom.interview.fullstack.sleep.model.User
import com.noom.interview.fullstack.sleep.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(private val userRepository: UserRepository) : UserService {

    override fun createUser(request: CreateUserRequest): UserResponse {
        // Check if username already exists
        userRepository.findByUsername(request.username)?.let {
            throw IllegalArgumentException("Username '${request.username}' is already taken")
        }

        // Check if email already exists
        userRepository.findByEmail(request.email)?.let {
            throw IllegalArgumentException("Email '${request.email}' is already registered")
        }

        val user = User(
            username = request.username,
            email = request.email
        )

        val savedUser = userRepository.save(user)

        return UserResponse(
            id = savedUser.id,
            username = savedUser.username,
            email = savedUser.email
        )
    }

    override fun getUserById(id: Long): UserResponse? {
        val user = userRepository.findById(id).orElse(null) ?: return null

        return UserResponse(
            id = user.id,
            username = user.username,
            email = user.email
        )
    }

    override fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { user ->
            UserResponse(
                id = user.id,
                username = user.username,
                email = user.email
            )
        }
    }
}
