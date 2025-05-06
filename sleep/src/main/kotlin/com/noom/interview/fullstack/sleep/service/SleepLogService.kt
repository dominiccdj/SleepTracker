package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class SleepLogService(private val sleepLogRepository: SleepLogRepository) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun createSleepLog(request: CreateSleepLogRequest): SleepLogResponse {
        val today = LocalDate.now()

        // Calculate time in bed
        val timeInBedMinutes = Duration.between(request.bedTime, request.wakeTime).toMinutes()

        // Create and save the sleep log
        val sleepLog = SleepLog(
            date = today,
            bedTime = request.bedTime,
            wakeTime = request.wakeTime,
            timeInBedMinutes = timeInBedMinutes,
            morningFeeling = request.morningFeeling
        )

        val savedSleepLog = sleepLogRepository.save(sleepLog)

        // Convert to response DTO
        return SleepLogResponse(
            id = savedSleepLog.id,
            date = savedSleepLog.date.format(dateFormatter),
            bedTime = savedSleepLog.bedTime.format(timeFormatter),
            wakeTime = savedSleepLog.wakeTime.format(timeFormatter),
            timeInBedMinutes = savedSleepLog.timeInBedMinutes,
            morningFeeling = savedSleepLog.morningFeeling
        )
    }
}