package com.newisland.gateway.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newisland.common.messages.command.ReservationCommandOuterClass;
import com.newisland.common.messages.event.ReservationEventOuterClass;
import com.newisland.gateway.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ReservationWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private KafkaTemplate<String, ReservationCommandOuterClass.ReservationCommand> kafkaTemplate;

    @Value("${reservation-commands-topic}")
    private String reservationCommandsTopic;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Map<UUID,WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    private Map<String,UUID> correlationSessionMap = new ConcurrentHashMap<>();

    public void onCreate(ReservationEventOuterClass.ReservationCreatedEvent reservationCreatedEvent,
                         ReservationEventOuterClass.ReservationEvent event){
        if(sessionMap.containsKey(reservationCreatedEvent.getReferenceId())){
            WebSocketSession session = sessionMap.get(reservationCreatedEvent.getReferenceId());
            ReservationResponse res = null;
            switch (event.getResultType()){
                case ERROR:
                    res = ReservationResponse.builder().
                            referenceId(UUID.fromString(reservationCreatedEvent.getReferenceId())).
                            status(ReservationStatus.ERROR).
                            errorMessage(Optional.of(event.getErrorMessage())).
                            build();
                    break;
                case SUCCESS:
                    res = ReservationResponse.builder().
                            referenceId(UUID.fromString(reservationCreatedEvent.getReferenceId())).
                            status(ReservationStatus.SUCCESS).
                            build();
                    break;
            }
            try {
                String json = objectMapper.writeValueAsString(res);
                session.sendMessage(new TextMessage(json));
                session.close();
            }catch (Exception ex){
                log.error("Error",ex);
            }
        }
    }

    public void onUpdate(ReservationEventOuterClass.ReservationUpdatedEvent reservationUpdatedEvent,
                         ReservationEventOuterClass.ReservationEvent event){
        if(sessionMap.containsKey(reservationUpdatedEvent.getId())){
            WebSocketSession session = sessionMap.get(reservationUpdatedEvent.getId());
            ReservationResponse res = null;
            switch (event.getResultType()){
                case ERROR:
                    res = ReservationResponse.builder().
                            referenceId(UUID.fromString(reservationUpdatedEvent.getId())).
                            status(ReservationStatus.ERROR).
                            errorMessage(Optional.of(event.getErrorMessage())).
                            build();
                    break;
                case SUCCESS:
                    res = ReservationResponse.builder().
                            referenceId(UUID.fromString(reservationUpdatedEvent.getId())).
                            status(ReservationStatus.SUCCESS).
                            build();
                    break;
            }
            try {
                String json = objectMapper.writeValueAsString(res);
                session.sendMessage(new TextMessage(json));
                session.close();
            }catch (Exception ex){
                log.error("Error",ex);
            }
        }
    }

    @KafkaListener(topics = "${reservation-events-topic}", groupId = "ReservationEventConsumerGroup")
    public void consume(ConsumerRecord<String, ReservationEventOuterClass.ReservationEvent> message){
        ReservationEventOuterClass.ReservationEvent event = message.value();
        switch (event.getActionType()){
            case CREATED: onCreate(event.getCreated(),event);break;
            case UPDATED: onUpdate(event.getUpdated(),event);break;
        }
    }

    public void submitCreate(CreateReservationDto createReservationDto, WebSocketSession session){
        UUID referenceId = UUID.randomUUID();
        kafkaTemplate.send(reservationCommandsTopic, createReservationDto.getCampsiteId(),
                createReservationDto.toProtobuf(referenceId));
        sessionMap.put(referenceId,session);
        correlationSessionMap.put(session.getId(),referenceId);
    }

    public void submitUpdate(UpdateReservationDto updateReservationDto, WebSocketSession session){
        kafkaTemplate.send(reservationCommandsTopic, updateReservationDto.getCampsiteId(),
                updateReservationDto.toProtobuf());
        UUID id = UUID.fromString(updateReservationDto.getId());
        sessionMap.put(id,session);
        correlationSessionMap.put(session.getId(),id);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        try {
            String json = message.getPayload();
            ReservationRequest reservationRequest = objectMapper.readValue(json, CreateReservationDto.class);
            if(reservationRequest instanceof CreateReservationDto){
                submitCreate((CreateReservationDto) reservationRequest,session);
            }
            if(reservationRequest instanceof UpdateReservationDto){
                submitUpdate((UpdateReservationDto) reservationRequest,session);
            }
        }catch (Exception ex){
            log.error("Error",ex);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        correlationSessionMap.computeIfPresent(session.getId(),(sessionId,referenceId)->{
            if(sessionMap.containsKey(sessionMap)) {
                sessionMap.remove(referenceId);
            }
            return referenceId;
        });
        correlationSessionMap.remove(session.getId());
    }

}
