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

    // New DOM layout (real card, VARGAS PEREZ): cols 5-13 hold an alphanumeric card number
    // (CV0067804); the 11-digit cédula 00109416016 sits at the END of line 1.
    private val domNewRaw = td1(
        "IDDOMCV0067804<71<00109416016",
        "7605149M4205146DOM",
        "VARGAS<PEREZ<<JOEL<ELIGIO"
    )

    // Old DOM layout (real card, MATEO): cédula 00119462497 in the standard cols 5-16 field.
    private val domOldRaw2 = td1(
        "IDDOM001194624<975",
        "9507111M2407119DOM",
        "MATEO<<HEECHUL<ANN"
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

    // TD3 passport (2 x 44): shows rawMrz round-trips beyond TD1 ID cards.
    private val passportRaw = listOf(
        "P<UTOERIKSSON<<ANNA<MARIA",
        "L898902C36UTO7408122F1204159ZE184226B"
    ).joinToString("\n") { it.padEnd(44, '<') }

    @Test
    fun `rawMrz preserves the exact parsed string`() {
        assertEquals(domRaw, MrzParser.parse(domRaw).rawMrz)
    }

    @Test
    fun `rawMrz preserves the exact parsed string for a TD3 passport`() {
        assertEquals(passportRaw, MrzParser.parse(passportRaw).rawMrz)
    }

    @Test
    fun `DOM overflow document number keeps all 11 digits`() {
        val record = MrzParser.parse(domRaw)
        assertEquals("40222828713", record.documentNumber)
        // toMrz() is a lossy rebuild; for an overflow number it must NOT equal the raw scan.
        assertNotEquals(record.toMrz(), record.rawMrz)
    }

    @Test
    fun `new DOM layout reads the cedula from the end of line 1`() {
        val record = MrzParser.parse(domNewRaw)
        assertEquals("00109416016", record.documentNumber)
        assertEquals(domNewRaw, record.rawMrz)
    }

    @Test
    fun `old DOM layout still reads the cedula from the standard field`() {
        assertEquals("00119462497", MrzParser.parse(domOldRaw2).documentNumber)
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
