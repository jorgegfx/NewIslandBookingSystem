package com.newisland.common.dto.utils;

import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class TimeUtils {
    public static Timestamp convertTimestamp(ZonedDateTime zonedDateTime){
        Instant time = zonedDateTime.toInstant();
        return Timestamp.newBuilder().setSeconds(time.getEpochSecond())
                .setNanos(time.getNano()).build();
    }

    public static ZonedDateTime convertToZonedDateTime(Instant instant, TimeZone timeZone){
        return ZonedDateTime.ofInstant(instant,timeZone.toZoneId());
    }

    public static Instant getUTCInstant(ZonedDateTime zonedDateTime){
        return Instant.from(zonedDateTime.toLocalDateTime().atZone(ZoneOffset.UTC));
    }
}
