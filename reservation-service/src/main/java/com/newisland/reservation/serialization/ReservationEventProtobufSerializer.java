package com.newisland.reservation.serialization;

import com.newisland.common.messages.event.ReservationEventOuterClass;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class ReservationEventProtobufSerializer implements Serializer<ReservationEventOuterClass.ReservationEvent> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, ReservationEventOuterClass.ReservationEvent data) {
        return data.toByteArray();
    }

    @Override
    public byte[] serialize(String topic, Headers headers, ReservationEventOuterClass.ReservationEvent data) {
        return data.toByteArray();
    }

    @Override
    public void close() {

    }
}
