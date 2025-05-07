package com.noom.interview.fullstack.sleep.dto

data class SleepAveragesResponse(
    val startDate: String,
    val endDate: String,
    val averageTimeInBedMinutes: Double,
    val averageBedTime: String,
    val averageWakeTime: String,
    val morningFeelingFrequencies: Map<String, Int>
)