package com.example.loanstats.service

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest
class StatsServiceTest(@Autowired private val service: StatsService) {

    private fun stats(
        start: String? = null,
        end: String? = null,
        state: String? = null,
        grade: String? = null,
        ficoBand: String? = null,
    ) = service.stats(start, end, state, grade, ficoBand)

    @Test
    fun `state and grade are accepted in any case and blank means no filter`() {
        assertEquals(3, stats(state = "ca", grade = "a").single().count)
        assertEquals(4, stats(start = "2017-11", end = "2017-11").single().count)
        assertEquals(13, stats(state = " ", grade = "", ficoBand = "").single().count)
    }
}
