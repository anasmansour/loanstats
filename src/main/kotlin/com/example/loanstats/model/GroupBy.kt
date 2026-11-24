package com.example.loanstats.model

enum class GroupBy(val key: String, val sql: String, val orderBy: String = sql) {
    GRADE("grade", "grade"),
    STATE("state", "addr_state"),
    MONTH("month", "TO_CHAR(issue_d, 'YYYY-MM')"),
    FICO_BAND("ficoBand", "fico_band", orderBy = "MIN(fico_range_low)"),
    PURPOSE("purpose", "purpose");

    companion object {
        fun fromKey(key: String): GroupBy? = entries.firstOrNull { it.key == key }
    }
}
