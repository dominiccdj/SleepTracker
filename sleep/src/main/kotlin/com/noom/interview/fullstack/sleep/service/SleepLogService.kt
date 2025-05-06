package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class SleepLogService(
    private val sleepLogRepository: SleepLogRepository,
    private val userRepository: UserRepository
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun createSleepLog(request: CreateSleepLogRequest): SleepLogResponse {
        val today = LocalDate.now()
        val user = userRepository.findById(request.userId)
            .orElseThrow { IllegalArgumentException("User not found with ID: ${request.userId}") }

        // Calculate time in bed
        val timeInBedMinutes = Duration.between(request.bedTime, request.wakeTime).toMinutes()

        // Create and save the sleep log
        val sleepLog = SleepLog(
            date = today,
            bedTime = request.bedTime,
            wakeTime = request.wakeTime,
            timeInBedMinutes = timeInBedMinutes,
            morningFeeling = request.morningFeeling,
            user = user
        )

        val savedSleepLog = sleepLogRepository.save(sleepLog)

        // Convert to response DTO
        return SleepLogResponse(
            id = savedSleepLog.id,
            date = savedSleepLog.date.format(dateFormatter),
            bedTime = savedSleepLog.bedTime.format(timeFormatter),
            wakeTime = savedSleepLog.wakeTime.format(timeFormatter),
            timeInBedMinutes = savedSleepLog.timeInBedMinutes,
            morningFeeling = savedSleepLog.morningFeeling,
            userId = savedSleepLog.user.id!!
        )
    }

    fun getLastNightSleepByUserId(userId: Long): SleepLogResponse? {
        val lastSleepLog = sleepLogRepository.findTopByUserIdOrderByDateDescWakeTimeDesc(userId) ?: return null

        return SleepLogResponse(
            id = lastSleepLog.id,
            date = lastSleepLog.date.format(dateFormatter),
            bedTime = lastSleepLog.bedTime.format(timeFormatter),
            wakeTime = lastSleepLog.wakeTime.format(timeFormatter),
            timeInBedMinutes = lastSleepLog.timeInBedMinutes,
            morningFeeling = lastSleepLog.morningFeeling,
            userId = lastSleepLog.user.id!!
        )
    }

    fun getAllSleepLogsByUserId(userId: Long): List<SleepLogResponse> {
        return sleepLogRepository.findAllByUserId(userId).map { sleepLog ->
            SleepLogResponse(
                id = sleepLog.id,
                date = sleepLog.date.format(dateFormatter),
                bedTime = sleepLog.bedTime.format(timeFormatter),
                wakeTime = sleepLog.wakeTime.format(timeFormatter),
                timeInBedMinutes = sleepLog.timeInBedMinutes,
                morningFeeling = sleepLog.morningFeeling,
                userId = sleepLog.user.id!!
            )
        }
    }
}
