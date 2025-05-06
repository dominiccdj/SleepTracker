package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.service.SleepLogService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
}