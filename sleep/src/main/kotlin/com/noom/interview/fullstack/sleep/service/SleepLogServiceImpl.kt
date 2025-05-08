package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepAveragesResponse
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.model.MorningFeeling
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
class SleepLogServiceImpl(
    private val sleepLogRepository: SleepLogRepository,
    private val userRepository: UserRepository
) : SleepLogService {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    override fun createSleepLog(request: CreateSleepLogRequest): SleepLogResponse {
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

    override fun getLastNightSleepByUserId(userId: Long): SleepLogResponse? {
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

    override fun getAllSleepLogsByUserId(userId: Long): List<SleepLogResponse> {
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

    override fun getLast30DayAverages(userId: Long): SleepAveragesResponse {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(29) // 30 days including today

        val sleepLogs = sleepLogRepository.findByUserIdAndDateBetween(userId, startDate, endDate)

        if (sleepLogs.isEmpty()) {
            return SleepAveragesResponse(
                startDate = startDate.toString(),
                endDate = endDate.toString(),
                averageTimeInBedMinutes = 0.0,
                averageBedTime = "00:00",
                averageWakeTime = "00:00",
                morningFeelingFrequencies = mapOf("BAD" to 0, "OK" to 0, "GOOD" to 0)
            )
        }

        // Calculate average time in bed
        val avgTimeInBed = sleepLogs.map { it.timeInBedMinutes }.average()

        // Calculate average bedtime and wake time
        val avgBedTime = calculateAverageTime(sleepLogs.map { it.bedTime.toLocalTime() })
        val avgWakeTime = calculateAverageTime(sleepLogs.map { it.wakeTime.toLocalTime() })

        // Calculate feeling frequencies
        val feelingFrequencies = sleepLogs
            .groupBy { it.morningFeeling }
            .mapValues { it.value.size }
            .withDefault { 0 }

        return SleepAveragesResponse(
            startDate = startDate.toString(),
            endDate = endDate.toString(),
            averageTimeInBedMinutes = avgTimeInBed,
            averageBedTime = avgBedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            averageWakeTime = avgWakeTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            morningFeelingFrequencies = mapOf(
                "BAD" to (feelingFrequencies.getValue(MorningFeeling.BAD)),
                "OK" to (feelingFrequencies.getValue(MorningFeeling.OK)),
                "GOOD" to (feelingFrequencies.getValue(MorningFeeling.GOOD))
            )
        )
    }

    private fun calculateAverageTime(times: List<LocalTime>): LocalTime {
        if (times.isEmpty()) return LocalTime.MIDNIGHT

        val totalSeconds = times.sumOf {
            it.toSecondOfDay().toLong()
        }

        return LocalTime.ofSecondOfDay((totalSeconds / times.size))
    }
}
