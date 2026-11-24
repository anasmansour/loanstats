package com.example.loanstats.service

import com.example.loanstats.ingestion.cleanState
import com.example.loanstats.ingestion.cleanText
import com.example.loanstats.model.GroupBy
import com.example.loanstats.model.StatsFilter
import com.example.loanstats.model.StatsRow
import com.example.loanstats.repository.StatsRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
class StatsService(private val repository: StatsRepository) {

    fun stats(
        start: String?,
        end: String?,
        state: String?,
        grade: String?,
        ficoBand: String?,
        groupBy: String?,
    ): List<StatsRow> {
        val filter = StatsFilter(
            start = parseMonth(start),
            end = parseMonth(end),
            state = cleanState(state),
            grade = cleanText(grade)?.uppercase(),
            ficoBand = cleanText(ficoBand),
        )
        return repository.stats(filter, parseGroupBy(groupBy))
    }

    private fun parseGroupBy(input: String?): GroupBy? =
        cleanText(input)?.let { GroupBy.fromKey(it) ?: throw IllegalArgumentException("Unknown groupBy '$it'") }

    private fun parseMonth(input: String?): LocalDate? =
        cleanText(input)?.let { YearMonth.parse(it).atDay(1) }
}
