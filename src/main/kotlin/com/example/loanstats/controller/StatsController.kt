package com.example.loanstats.controller

import com.example.loanstats.model.StatsRow
import com.example.loanstats.service.StatsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/loans")
class StatsController(private val service: StatsService) {

    @GetMapping("/stats")
    fun stats(
        @RequestParam(required = false) start: String?,
        @RequestParam(required = false) end: String?,
        @RequestParam(required = false) state: String?,
        @RequestParam(required = false) grade: String?,
        @RequestParam(required = false) ficoBand: String?,
    ): List<StatsRow> = service.stats(start, end, state, grade, ficoBand)
}
