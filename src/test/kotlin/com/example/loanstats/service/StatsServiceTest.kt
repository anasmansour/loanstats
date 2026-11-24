package com.example.loanstats.service

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@SpringBootTest
class StatsServiceTest(@Autowired private val service: StatsService) {

    private fun stats(
        start: String? = null,
        end: String? = null,
        state: String? = null,
        grade: String? = null,
        ficoBand: String? = null,
        groupBy: String? = null,
    ) = service.stats(start, end, state, grade, ficoBand, groupBy)

    @Test
    fun `a value that is not allowed is rejected`() {
        assertFailsWith<InvalidRequestException> { stats(groupBy = "color") }
        assertFailsWith<InvalidRequestException> { stats(grade = "z") }
        assertFailsWith<InvalidRequestException> { stats(state = "CAL") }
        assertFailsWith<InvalidRequestException> { stats(ficoBand = "Great") }
    }

    @Test
    fun `start and end must be real months in yyyy-MM form`() {
        assertFailsWith<InvalidRequestException> { stats(start = "2017-13") }
        assertFailsWith<InvalidRequestException> { stats(end = "Nov-2017") }
        assertFailsWith<InvalidRequestException> { stats(start = "2017-11-15") }
    }

    @Test
    fun `start must not be after end`() {
        assertFailsWith<InvalidRequestException> { stats(start = "2017-12", end = "2017-11") }
    }

    @Test
    fun `valid values are accepted in any case and blank means no filter`() {
        assertEquals(3, stats(state = "ca", grade = "a").single().count)
        assertEquals(2, stats(ficoBand = "very good").single().count)
        assertEquals(4, stats(start = "2017-11", end = "2017-11").single().count)
        assertEquals(13, stats(state = " ", grade = "", ficoBand = "", groupBy = " ").single().count)
    }
}
