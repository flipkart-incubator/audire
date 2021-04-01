package com.flipkart.audire.stream.commons;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateUtils {

    private final String ZONE_IST = "Asia/Kolkata";

    public Instant getUTCInstantFromUTCTimestamp(String timestamp) {
        return ZonedDateTime.parse(timestamp).toInstant();
    }

    public String convertFormattedDateToIST(String date) {
        return Instant.parse(date)
                .atZone(ZoneId.of(ZONE_IST))
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
