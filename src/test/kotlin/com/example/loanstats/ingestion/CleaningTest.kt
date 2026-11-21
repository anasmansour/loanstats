package com.example.loanstats.ingestion

import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CleaningTest {

    @Test
    fun `cleanText trims whitespace`() {
        assertEquals("A", cleanText("  A "))
    }

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
    fun `cleanState trims and upper-cases`() {
        assertEquals("CA", cleanState(" ca "))
        assertNull(cleanState(""))
    }

    @Test
    fun `parseDecimal returns null for missing or bad values`() {
        assertNull(parseDecimal("abc"))
        assertNull(parseDecimal(null))
    }

    @Test
    fun `parseInt returns null for bad values`() {
        assertNull(parseInt(""))
        assertNull(parseInt("66x"))
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
    fun `ficoBand puts a score in its 20-point band`() {
        assertEquals("660-679", ficoBand(660))
        assertEquals("660-679", ficoBand(679))
        assertEquals("680-699", ficoBand(680))
    }
}
