package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepAveragesResponse
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse

/**
 * Service interface for sleep log operations.
 */
interface SleepLogService {
    /**
     * Creates a new sleep log.
     *
     * @param request The sleep log creation request
     * @return The created sleep log response
     * @throws IllegalArgumentException if the user is not found
     */
    fun createSleepLog(request: CreateSleepLogRequest): SleepLogResponse

    /**
     * Retrieves the most recent sleep log for a user.
     *
     * @param userId The ID of the user
     * @return The most recent sleep log, or null if none exists
     */
    fun getLastNightSleepByUserId(userId: Long): SleepLogResponse?

    /**
     * Retrieves all sleep logs for a user.
     *
     * @param userId The ID of the user
     * @return A list of all sleep logs for the user
     */
    fun getAllSleepLogsByUserId(userId: Long): List<SleepLogResponse>

    /**
     * Calculates and retrieves sleep statistics for the last 30 days.
     *
     * @param userId The ID of the user
     * @return Sleep statistics including averages and frequencies
     */
    fun getLast30DayAverages(userId: Long): SleepAveragesResponse
}
