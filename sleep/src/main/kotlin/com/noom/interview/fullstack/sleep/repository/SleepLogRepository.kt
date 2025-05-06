package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.model.SleepLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface SleepLogRepository : JpaRepository<SleepLog, Long> {
    fun findByDateAndUserId(date: LocalDate, userId: Long): SleepLog?

    fun findTopByUserIdOrderByDateDescWakeTimeDesc(userId: Long): SleepLog?

    fun findAllByUserId(userId: Long): List<SleepLog>
}