package com.newisland.common.dto.utils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class TimeUtils {
    public static ZonedDateTime convertToZonedDateTime(Instant instant, TimeZone timeZone){
        return ZonedDateTime.ofInstant(instant,timeZone.toZoneId());
    }

    public static Instant getUTCInstant(ZonedDateTime zonedDateTime){
        return Instant.from(zonedDateTime.toLocalDateTime().atZone(ZoneOffset.UTC));
    }
}
