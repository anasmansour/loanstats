package com.example.loanstats.model

import java.time.LocalDate

data class StatsFilter(
    val start: LocalDate? = null,
    val end: LocalDate? = null,
    val state: String? = null,
    val grade: String? = null,
    val ficoBand: String? = null,
)
