package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateUserRequest
import com.noom.interview.fullstack.sleep.dto.UserResponse
import com.noom.interview.fullstack.sleep.model.User
import com.noom.interview.fullstack.sleep.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    fun createUser(request: CreateUserRequest): UserResponse {
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

    fun getUserById(id: Long): UserResponse? {
        val user = userRepository.findById(id).orElse(null) ?: return null

        return UserResponse(
            id = user.id,
            username = user.username,
            email = user.email
        )
    }

    fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { user ->
            UserResponse(
                id = user.id,
                username = user.username,
                email = user.email
            )
        }
    }
}