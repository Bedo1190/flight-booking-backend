package com.spheretech.flight_booking_backend.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CardUtilsTest {

    @Test
    void shouldMaskStandardCardNumber() {
        String raw = "4221161122330005";
        String expected = "422116******0005";
        assertEquals(expected, CardUtils.maskCardNumber(raw));
    }

    @Test
    void shouldMaskCardNumberWithDashes() {
        String raw = "4221-1611-2233-0005";
        String expected = "422116******0005";
        assertEquals(expected, CardUtils.maskCardNumber(raw));
    }

    @Test
    void shouldMaskCardNumberWithCommasAndSpaces() {
        String raw = "4221, 1611, 2233, 0005";
        String expected = "422116******0005";
        assertEquals(expected, CardUtils.maskCardNumber(raw));
    }

    @Test
    void shouldReturnCleanStringIfTooShort() {
        String raw = "12345";
        assertEquals("12345", CardUtils.maskCardNumber(raw));
    }
}