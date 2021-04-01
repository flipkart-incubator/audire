package com.flipkart.audire.stream.commons;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateUtilsTest {

    @Test
    void testGetUTCInstantFromUTCTimestamp() {
        Instant instant = DateUtils.getUTCInstantFromUTCTimestamp("2022-03-08T08:14:29Z");
        assertEquals("2022-03-08T08:14:29Z", instant.toString());
        assertEquals(1646727269, instant.getEpochSecond());
    }

    @Test
    void testConvertFormattedDateToIST() {
        assertThrows(DateTimeParseException.class, () -> DateUtils.convertFormattedDateToIST("2020-11-15T13:34:34"));
        assertEquals("2020-11-15T19:04:34", DateUtils.convertFormattedDateToIST("2020-11-15T13:34:34Z"));
    }
}
