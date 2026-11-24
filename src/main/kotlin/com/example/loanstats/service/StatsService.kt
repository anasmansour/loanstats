package com.example.loanstats.service

import com.example.loanstats.ingestion.FICO_BANDS
import com.example.loanstats.ingestion.cleanText
import com.example.loanstats.model.GroupBy
import com.example.loanstats.model.StatsFilter
import com.example.loanstats.model.StatsRow
import com.example.loanstats.repository.StatsRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException

private val GRADES: List<String> = ('A'..'G').map { it.toString() }

private val STATE_FORMAT = Regex("[A-Z]{2}")

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
            state = parseState(state),
            grade = parseGrade(grade),
            ficoBand = parseFicoBand(ficoBand),
        )
        if (filter.start != null && filter.end != null && filter.start > filter.end) {
            throw InvalidRequestException("Invalid date range")
        }
        return repository.stats(filter, parseGroupBy(groupBy))
    }

    private fun parseGroupBy(input: String?): GroupBy? {
        val value = cleanText(input) ?: return null
        return GroupBy.fromKey(value) ?: throw InvalidRequestException(
            "Invalid groupBy '$value'. Allowed values: ${GroupBy.entries.joinToString { it.key }}",
        )
    }

    private fun parseGrade(input: String?): String? {
        val value = cleanText(input) ?: return null
        val grade = value.uppercase()
        if (grade !in GRADES) {
            throw InvalidRequestException("Invalid grade '$value'. Allowed values: ${GRADES.joinToString()}")
        }
        return grade
    }

    private fun parseState(input: String?): String? {
        val value = cleanText(input) ?: return null
        val state = value.uppercase()
        if (!STATE_FORMAT.matches(state)) {
            throw InvalidRequestException("Invalid state '$value'")
        }
        return state
    }

    private fun parseFicoBand(input: String?): String? {
        val value = cleanText(input) ?: return null
        return FICO_BANDS.firstOrNull { it.label.equals(value, ignoreCase = true) }?.label
            ?: throw InvalidRequestException(
                "Invalid ficoBand '$value'. Allowed values: ${FICO_BANDS.joinToString { it.label }}",
            )
    }

    private fun parseMonth(input: String?): LocalDate? {
        val value = cleanText(input) ?: return null
        return try {
            YearMonth.parse(value).atDay(1)
        } catch (_: DateTimeParseException) {
            throw InvalidRequestException("Invalid '$value'. Use a month in yyyy-MM format")
        }
    }
}
