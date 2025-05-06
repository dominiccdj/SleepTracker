package com.noom.interview.fullstack.sleep.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Duration
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

enum class MorningFeeling {
    BAD, OK, GOOD
}

@Entity
@Table(name = "sleep_logs")
data class SleepLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val date: LocalDate,

    @Column(name = "bed_time", nullable = false)
    val bedTime: LocalDateTime,

    @Column(name = "wake_time", nullable = false)
    val wakeTime: LocalDateTime,

    @Column(name = "time_in_bed_minutes", nullable = false)
    val timeInBedMinutes: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val morningFeeling: MorningFeeling
)