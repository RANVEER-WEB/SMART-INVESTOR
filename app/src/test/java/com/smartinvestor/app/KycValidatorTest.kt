package com.smartinvestor.app

import com.smartinvestor.app.ui.screens.kyc.KycValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KycValidatorTest {

    @Test
    fun testPanValidation_ValidFormats() {
        assertTrue(KycValidator.panRegex.matches("ABCDE1234F"))
        assertTrue(KycValidator.panRegex.matches("AAAPA1111A"))
        assertTrue(KycValidator.panRegex.matches("ZZZZZ9999Z"))
    }

    @Test
    fun testPanValidation_InvalidFormats() {
        assertFalse(KycValidator.panRegex.matches("ABCD1234F")) // only 4 chars
        assertFalse(KycValidator.panRegex.matches("ABCDE12345")) // ends with digit
        assertFalse(KycValidator.panRegex.matches("12345ABCDE")) // starts with digits
        assertFalse(KycValidator.panRegex.matches("ABCDE1234FA")) // 11 chars
    }

    @Test
    fun testAadhaarValidation_ValidFormats() {
        assertTrue(KycValidator.aadhaarRegex.matches("234567890123"))
        assertTrue(KycValidator.aadhaarRegex.matches("987654321098"))
    }

    @Test
    fun testAadhaarValidation_InvalidFormats() {
        assertFalse(KycValidator.aadhaarRegex.matches("012345678901")) // starts with 0
        assertFalse(KycValidator.aadhaarRegex.matches("123456789012")) // starts with 1
        assertFalse(KycValidator.aadhaarRegex.matches("23456789012")) // 11 digits
        assertFalse(KycValidator.aadhaarRegex.matches("2345678901234")) // 13 digits
    }

    @Test
    fun testIfscValidation() {
        assertTrue(KycValidator.ifscRegex.matches("HDFC0001234"))
        assertTrue(KycValidator.ifscRegex.matches("SBIN0000456"))
        assertTrue(KycValidator.ifscRegex.matches("ICIC0000999"))

        assertFalse(KycValidator.ifscRegex.matches("HDF0001234")) // 3 letters
        assertFalse(KycValidator.ifscRegex.matches("HDFC1001234")) // 5th char not zero
        assertFalse(KycValidator.ifscRegex.matches("HDFC000123")) // 10 chars
    }

    @Test
    fun testDematValidation() {
        assertTrue(KycValidator.dematRegex.matches("1208160012345678"))
        assertFalse(KycValidator.dematRegex.matches("120816001234567")) // 15 digits
        assertFalse(KycValidator.dematRegex.matches("12081600123456789")) // 17 digits
        assertFalse(KycValidator.dematRegex.matches("IN30012612345678")) // Contains letters
    }
}
