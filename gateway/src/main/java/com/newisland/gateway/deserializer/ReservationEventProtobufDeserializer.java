package com.newisland.gateway.deserializer;

import com.newisland.common.messages.event.ReservationEventOuterClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;
@Slf4j
public class ReservationEventProtobufDeserializer implements Deserializer<ReservationEventOuterClass.ReservationEvent> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public ReservationEventOuterClass.ReservationEvent deserialize(String topic, byte[] data) {
        try{
            return ReservationEventOuterClass.ReservationEvent.parseFrom(data);
        }catch (Exception ex){
            log.error("Error deserializing ReservationEvent",ex);
        }
        return null;
    }

    @Override
    public void close() {

    }
}
