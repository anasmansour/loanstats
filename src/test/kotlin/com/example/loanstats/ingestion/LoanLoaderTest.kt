package com.example.loanstats.ingestion

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

@SpringBootTest
class LoanLoaderTest(@Autowired private val jdbc: JdbcTemplate) {

    private fun count(sql: String): Long = jdbc.queryForObject(sql, Long::class.javaObjectType)!!

    private fun decimal(sql: String): BigDecimal? = jdbc.queryForObject(sql, BigDecimal::class.java)

    private fun text(sql: String): String? = jdbc.queryForObject(sql, String::class.java)

    @Test
    fun `loads the good rows and drops the footer and the unusable rows`() {
        assertEquals(13, count("SELECT COUNT(*) FROM loans"))
        assertEquals(0, count("SELECT COUNT(*) FROM loans WHERE loan_amnt IN (8000, 9000)"))
    }

    @Test
    fun `parses the rate, the month and the state`() {
        assertEquals(0, BigDecimal("9.00").compareTo(decimal("SELECT int_rate FROM loans WHERE loan_amnt = 20000 AND grade = 'A'")))
        assertEquals(
            LocalDate.of(2017, 12, 1),
            jdbc.queryForObject("SELECT issue_d FROM loans WHERE loan_amnt = 5000", LocalDate::class.java),
        )
        assertEquals("NY", text("SELECT addr_state FROM loans WHERE loan_amnt = 25000"))
    }

    @Test
    fun `a blank rate and a blank score load as null`() {
        assertNull(decimal("SELECT int_rate FROM loans WHERE loan_amnt = 12000"))
        assertNull(text("SELECT fico_band FROM loans WHERE loan_amnt = 12000"))
    }
}
