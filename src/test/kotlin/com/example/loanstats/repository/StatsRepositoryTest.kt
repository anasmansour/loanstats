package com.example.loanstats.repository

import com.example.loanstats.model.StatsFilter
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

@SpringBootTest
class StatsRepositoryTest(@Autowired private val repository: StatsRepository) {

    private fun assertDecimal(expected: String, actual: BigDecimal?) =
        assertEquals(0, BigDecimal(expected).compareTo(actual), "expected $expected but was $actual")

    @Test
    fun `no filters returns one overall row`() {
        val rows = repository.stats(StatsFilter())

        assertEquals(1, rows.size)
        val row = rows.single()
        assertNull(row.group)
        assertEquals(13, row.count)
        assertDecimal("247000", row.totalAmount)
        assertDecimal("19000.00", row.avgAmount)
        assertDecimal("15.50", row.avgRate)
        assertDecimal("14.50", row.medianRate)
        assertDecimal("51.36", row.avgDti)
    }

    @Test
    fun `filters are combined with AND`() {
        val ca = repository.stats(StatsFilter(state = "CA")).single()
        assertEquals(6, ca.count)
        assertDecimal("87000", ca.totalAmount)

        val caGradeA = repository.stats(StatsFilter(state = "CA", grade = "A")).single()
        assertEquals(3, caGradeA.count)
    }

    @Test
    fun `start and end are inclusive months`() {
        val november = repository.stats(StatsFilter(start = LocalDate.of(2017, 11, 1), end = LocalDate.of(2017, 11, 1))).single()

        assertEquals(4, november.count)
        assertDecimal("85000", november.totalAmount)
    }

    @Test
    fun `no matching loans gives one row with count 0 and null values`() {
        val row = repository.stats(StatsFilter(state = "IA")).single()

        assertEquals(0, row.count)
        assertNull(row.totalAmount)
        assertNull(row.avgRate)
        assertNull(row.medianRate)
        assertNull(row.avgDti)
    }
}
