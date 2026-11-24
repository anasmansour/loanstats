package com.example.loanstats.model

import java.math.BigDecimal

data class StatsRow(
    val group: String?,
    val count: Long,
    val totalAmount: BigDecimal?,
    val avgAmount: BigDecimal?,
    val avgRate: BigDecimal?,
    val medianRate: BigDecimal?,
    val avgDti: BigDecimal?,
)
