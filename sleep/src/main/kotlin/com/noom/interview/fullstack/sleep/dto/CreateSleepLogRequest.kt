package com.noom.interview.fullstack.sleep.dto

import com.noom.interview.fullstack.sleep.model.MorningFeeling
import java.time.LocalDateTime

data class CreateSleepLogRequest(
    val bedTime: LocalDateTime,
    val wakeTime: LocalDateTime,
    val morningFeeling: MorningFeeling,
    val userId: Long
)