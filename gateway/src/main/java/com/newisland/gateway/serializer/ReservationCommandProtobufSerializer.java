package com.newisland.gateway.serializer;

import com.newisland.common.messages.command.ReservationCommandOuterClass;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class ReservationCommandProtobufSerializer implements Serializer<ReservationCommandOuterClass.ReservationCommand> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, ReservationCommandOuterClass.ReservationCommand data) {
        return data.toByteArray();
    }

    @Override
    public void close() {

    }
}
