package com.example.loanstats.repository

import com.example.loanstats.model.GroupBy
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

    private fun groups(groupBy: GroupBy, filter: StatsFilter = StatsFilter()) =
        repository.stats(filter, groupBy).map { it.group to it.count }

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
    fun `groups by grade in ascending order`() {
        assertEquals(listOf("A" to 3L, "B" to 3L, "C" to 3L, "D" to 2L, "E" to 2L), groups(GroupBy.GRADE))
    }

    @Test
    fun `groups by month chronologically`() {
        assertEquals(listOf("2017-10" to 4L, "2017-11" to 4L, "2017-12" to 5L), groups(GroupBy.MONTH))
    }

    @Test
    fun `groups by fico band from the lowest score up`() {
        assertEquals(
            listOf("Fair" to 1L, "Good" to 8L, "Very Good" to 2L, "Exceptional" to 1L, null to 1L),
            groups(GroupBy.FICO_BAND),
        )
    }

    @Test
    fun `filters and groupBy work together`() {
        assertEquals(listOf("A" to 3L, "B" to 2L, "D" to 1L), groups(GroupBy.GRADE, StatsFilter(state = "CA")))
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
