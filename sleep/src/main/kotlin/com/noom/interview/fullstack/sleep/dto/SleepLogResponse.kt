package com.noom.interview.fullstack.sleep.dto

import com.noom.interview.fullstack.sleep.model.MorningFeeling

data class SleepLogResponse(
    val id: Long?,
    val date: String,
    val bedTime: String,
    val wakeTime: String,
    val timeInBedMinutes: Long,
    val morningFeeling: MorningFeeling,
    val userId: Long
)