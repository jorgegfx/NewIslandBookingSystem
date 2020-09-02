package com.newisland.reservation.deserializer;

import com.newisland.common.messages.command.ReservationCommandOuterClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

@Slf4j
public class ReservationCommandProtobufDeserializer implements Deserializer<ReservationCommandOuterClass.ReservationCommand> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public ReservationCommandOuterClass.ReservationCommand deserialize(String topic, byte[] data) {
        try{
            return ReservationCommandOuterClass.ReservationCommand.parseFrom(data);
        }catch (Exception ex){
            log.error("Error reading Command",ex);
            return null;
        }
    }

    @Override
    public ReservationCommandOuterClass.ReservationCommand deserialize(String topic, Headers headers, byte[] data) {
        try{
            return ReservationCommandOuterClass.ReservationCommand.parseFrom(data);
        }catch (Exception ex){
            log.error("Error reading Command",ex);
            return null;
        }
    }

    @Override
    public void close() {

    }
}
