package com.newisland.common.dto.utils;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class TimeUtilsTest {
    @Test
    public void testConvertToZonedDateTime(){
        ZonedDateTime localTime = ZonedDateTime.now();
        ZonedDateTime zonedDateTime =
                TimeUtils.convertToZonedDateTime(Instant.now(), TimeZone.getTimeZone("GMT+8"));
        assertTrue(localTime.isBefore(zonedDateTime));
    }

    @Test
    public void testConvertUTC(){
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("America/Toronto"));
        Instant instant = TimeUtils.getUTCInstant(zonedDateTime);
    }

    @Test
    public void testConvertProtoTimeStamp(){
        ZonedDateTime localTime = ZonedDateTime.now();
        Timestamp timestamp = TimeUtils.convertTimestamp(localTime);
        Instant instant = TimeUtils.convertToInstant(timestamp);
        System.out.println(instant);
    }
}