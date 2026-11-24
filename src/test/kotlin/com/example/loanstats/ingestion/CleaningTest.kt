package com.example.loanstats.ingestion

import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CleaningTest {

    @Test
    fun `cleanText turns blank, dot and n-a into null`() {
        assertNull(cleanText(""))
        assertNull(cleanText("   "))
        assertNull(cleanText("."))
        assertNull(cleanText("n/a"))
        assertNull(cleanText(" N/A "))
        assertNull(cleanText(null))
    }

    @Test
    fun `parsePercent strips the percent sign and the padding`() {
        assertEquals(BigDecimal("6.08"), parsePercent("  6.08%"))
        assertEquals(BigDecimal("13.5"), parsePercent("13.5%"))
    }

    @Test
    fun `parseMonth turns Mon-YYYY into the first day of the month`() {
        assertEquals(LocalDate.of(2017, 12, 1), parseMonth("Dec-2017"))
        assertEquals(LocalDate.of(2017, 10, 1), parseMonth(" Oct-2017 "))
    }

    @Test
    fun `cleanDti keeps exactly 100 but drops anything above it`() {
        assertEquals(BigDecimal("100"), cleanDti("100"))
        assertNull(cleanDti("100.01"))
        assertNull(cleanDti("999"))
    }

    @Test
    fun `cleanDti drops negative values but keeps zero`() {
        assertEquals(BigDecimal("0"), cleanDti("0"))
        assertNull(cleanDti("-0.01"))
    }

    @Test
    fun `ficoBand puts a score in its FICO category, edges included`() {
        assertEquals("Fair", ficoBand(669))
        assertEquals("Good", ficoBand(670))
        assertEquals("Good", ficoBand(739))
        assertEquals("Very Good", ficoBand(740))
        assertEquals("Very Good", ficoBand(799))
        assertEquals("Exceptional", ficoBand(800))
        assertEquals("Exceptional", ficoBand(850))
    }
}
