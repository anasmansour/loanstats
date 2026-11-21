package com.example.loanstats.ingestion

import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

const val MAX_DTI = 100
const val MIN_DTI = 0

data class FicoBand(val low: Int, val high: Int) {
    val label = "$low-$high"
}

private const val FICO_FIRST_BAND_START = 300
private const val FICO_LAST_BAND_START = 840
private const val FICO_BAND_WIDTH = 20

private val MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH)

val FICO_BANDS: List<FicoBand> = (FICO_FIRST_BAND_START..FICO_LAST_BAND_START step FICO_BAND_WIDTH)
    .map { FicoBand(it, it + FICO_BAND_WIDTH - 1) }

private val MISSING_MARKERS = setOf("", ".", "n/a")

fun cleanText(input: String?): String? {
    val value = input?.trim() ?: return null
    return if (value.lowercase() in MISSING_MARKERS) null else value
}

fun cleanState(input: String?): String? = cleanText(input)?.uppercase()

fun parseDecimal(input: String?): BigDecimal? = cleanText(input)?.toBigDecimalOrNull()

fun parseInt(input: String?): Int? = cleanText(input)?.toIntOrNull()

fun parsePercent(input: String?): BigDecimal? = cleanText(input)?.removeSuffix("%")?.trim()?.toBigDecimalOrNull()

fun parseMonth(input: String?): LocalDate? {
    val value = cleanText(input) ?: return null

    return try {
        val normalized = value.lowercase().replaceFirstChar { it.uppercase() }
        YearMonth.parse(normalized, MONTH_YEAR_FORMATTER).atDay(1)
    } catch (_: DateTimeParseException) {
        null
    }
}

fun cleanDti(raw: String?): BigDecimal? = parseDecimal(raw)?.takeIf {it >= BigDecimal(MIN_DTI) && it <= BigDecimal(MAX_DTI)}

fun ficoBand(score: Int?): String? =
    FICO_BANDS.firstOrNull { score != null && score in it.low..it.high }?.label
