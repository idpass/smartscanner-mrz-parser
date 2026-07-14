package org.idpass.smartscanner.mrz.parser

import org.idpass.smartscanner.mrz.parser.innovatrics.MrzParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Regression tests for two fixes:
 *  - [MrzRecord.rawMrz] carries the exact parsed (cleaned) MRZ, so callers no longer depend on the
 *    lossy [MrzRecord.toMrz] reconstruction (which truncates overflow document numbers).
 *  - Guatemala DPI "new" format: the 4 extra document-number digits held in the optional field are
 *    appended to the 9-digit field (13 total); the "old" format keeps the 9-digit number.
 *
 * Check digits are irrelevant here (parse() sets validity booleans but never throws on them), so the
 * samples pad each line to 30 chars with fillers rather than carry real check digits.
 */
class MrzParserFixTest {

    /** Builds a 3-line TD1 record, padding each line to 30 chars with the filler '<'. */
    private fun td1(line0: String, line1: String, line2: String) =
        listOf(line0, line1, line2).joinToString("\n") { it.padEnd(30, '<') }

    // Real DOM cédula: document number 40222828713 overflows the 9-char field (11 digits).
    private val domRaw = td1(
        "IDDOM402228287<139",
        "9401194M1404306DOM",
        "PIMENTEL<DIAZ<<MAXIMO<DAVID"
    )

    // GTM DPI, new format: 4 extra document-number digits (0123) sit in the optional field.
    private val gtmNewRaw = td1(
        "IDGTM12345678900123",
        "9401194M1404306GTM",
        "GARCIA<<JUAN"
    )

    // GTM DPI, old format: optional field is fillers -> keep the 9-digit number.
    private val gtmOldRaw = td1(
        "IDGTM123456789",
        "9401194M1404306GTM",
        "GARCIA<<JUAN"
    )

    @Test
    fun `rawMrz preserves the exact parsed string`() {
        assertEquals(domRaw, MrzParser.parse(domRaw).rawMrz)
    }

    @Test
    fun `DOM overflow document number keeps all 11 digits`() {
        val record = MrzParser.parse(domRaw)
        assertEquals("40222828713", record.documentNumber)
        // toMrz() is a lossy rebuild; for an overflow number it must NOT equal the raw scan.
        assertNotEquals(record.toMrz(), record.rawMrz)
    }

    @Test
    fun `GTM new format appends the 4 optional digits for a 13-digit document number`() {
        assertEquals("1234567890123", MrzParser.parse(gtmNewRaw).documentNumber)
    }

    @Test
    fun `GTM old format keeps the 9-digit document number`() {
        assertEquals("123456789", MrzParser.parse(gtmOldRaw).documentNumber)
    }
}
