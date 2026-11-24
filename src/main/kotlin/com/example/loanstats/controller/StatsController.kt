package com.example.loanstats.controller

import com.example.loanstats.model.StatsRow
import com.example.loanstats.service.StatsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/loans")
@Tag(name = "Loan stats")
class StatsController(private val service: StatsService) {

    @GetMapping("/stats")
    @Operation(
        summary = "Aggregate loan statistics"
    )
    fun stats(
        @Parameter(description = "Start month, format yyyy-MM (inclusive)", example = "2017-10")
        @RequestParam(required = false) start: String?,
        @Parameter(description = "End month, format yyyy-MM (inclusive)", example = "2017-12")
        @RequestParam(required = false) end: String?,
        @Parameter(description = "US state code", example = "NY")
        @RequestParam(required = false) state: String?,
        @Parameter(
            description = "Loan grade",
            example = "B",
            schema = Schema(allowableValues = ["A", "B", "C", "D", "E", "F", "G"]),
        )
        @RequestParam(required = false) grade: String?,
        @Parameter(
            description = "FICO band: Poor (300-579), Fair (580-669), Good (670-739), " +
                "Very Good (740-799) or Exceptional (800-850).",
            example = "Good",
            schema = Schema(allowableValues = ["Poor", "Fair", "Good", "Very Good", "Exceptional"]),
        )
        @RequestParam(required = false) ficoBand: String?,
        @Parameter(
            description = "Grouping key",
            example = "purpose",
            schema = Schema(allowableValues = ["grade", "state", "month", "ficoBand", "purpose"]),
        )
        @RequestParam(required = false) groupBy: String?,
    ): List<StatsRow> = service.stats(start, end, state, grade, ficoBand, groupBy)
}
