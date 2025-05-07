package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepAveragesResponse
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.service.SleepLogService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sleep-logs")
class SleepLogController(private val sleepLogService: SleepLogService) {

    @PostMapping
    fun createSleepLog(@RequestBody request: CreateSleepLogRequest): ResponseEntity<SleepLogResponse> {
        val response = sleepLogService.createSleepLog(request)
        return ResponseEntity(response, HttpStatus.CREATED)
    }

    @GetMapping("/users/{userId}")
    fun getAllSleepLogsByUserId(@PathVariable userId: Long): ResponseEntity<List<SleepLogResponse>> {
        val sleepLogs = sleepLogService.getAllSleepLogsByUserId(userId)
        return ResponseEntity.ok(sleepLogs)
    }

    @GetMapping("/users/{userId}/last-night")
    fun getLastNightSleepByUserId(@PathVariable userId: Long): ResponseEntity<SleepLogResponse> {
        val sleepLog = sleepLogService.getLastNightSleepByUserId(userId)

        return if (sleepLog != null) {
            ResponseEntity.ok(sleepLog)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/users/{userId}/averages/30-day")
    fun getLast30DayAverages(@PathVariable userId: Long): ResponseEntity<SleepAveragesResponse> {
        val averages = sleepLogService.getLast30DayAverages(userId)
        return ResponseEntity.ok(averages)
    }
}
