package com.stats.restservice.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
public class CustomDateTimeUtils {

    public static boolean isTransactionOlderThanMinute(Long ts) {
        LocalDateTime date = Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime oneMinBefore = LocalDateTime.now().minus(60, ChronoUnit.SECONDS);
        return date.isAfter(oneMinBefore);
    }
    
}
