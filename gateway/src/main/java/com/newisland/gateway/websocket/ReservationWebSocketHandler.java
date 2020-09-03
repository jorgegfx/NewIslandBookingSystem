package com.newisland.gateway.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.newisland.common.messages.command.ReservationCommandOuterClass;
import com.newisland.common.messages.event.ReservationEventOuterClass;
import com.newisland.gateway.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ReservationWebSocketHandler this is a web socket handler that has the responsibility
 * of sending the commands to the command topic and listen the events from those commands
 * NOTE is might be required to implement a retention policy on the events since
 * they might not be consume by the web socket or other system
 */
@Slf4j
@Component
public class ReservationWebSocketHandler implements WebSocketHandler {

    @Autowired
    private KafkaTemplate<String, ReservationCommandOuterClass.ReservationCommand> kafkaTemplate;

    @Value("${reservation-commands-topic}")
    private String reservationCommandsTopic;

    @Value("${reservation-response-timeout:5}")
    private int timeOutMinutes;

    private ObjectMapper objectMapper = createObjectMapper();

    private Map<UUID, CountDownLatch> createRequestLocksMap = new ConcurrentHashMap<>();
    private Map<UUID, CountDownLatch> updateRequestLocksMap = new ConcurrentHashMap<>();
    private Map<UUID, CountDownLatch> cancelRequestLocksMap = new ConcurrentHashMap<>();

    private Map<UUID, String> createResponseMap = new ConcurrentHashMap<>();
    private Map<UUID, String> updateResponseMap = new ConcurrentHashMap<>();
    private Map<UUID, String> cancelResponseMap = new ConcurrentHashMap<>();

    private ObjectMapper createObjectMapper(){
        ObjectMapper mapper =
                new ObjectMapper()
                        .registerModule(new ParameterNamesModule())
                        .registerModule(new Jdk8Module())
                        .registerModule(new JavaTimeModule());
        return mapper;
    }

    public void onCreate(ReservationEventOuterClass.ReservationEvent event){
        ReservationEventOuterClass.ReservationCreatedEvent reservationCreatedEvent = event.getCreated();
        Optional<ReservationResponse> res = Optional.empty();
        switch (event.getResultType()){
            case ERROR:
                res = Optional.of(ReservationResponse.builder().
                        referenceId(UUID.fromString(reservationCreatedEvent.getReferenceId())).
                        status(ReservationStatus.ERROR).
                        errorMessage(Optional.of(event.getErrorMessage())).
                        build());
                break;
            case SUCCESS:
                res = Optional.of(ReservationResponse.builder().
                        referenceId(UUID.fromString(reservationCreatedEvent.getReferenceId())).
                        status(ReservationStatus.SUCCESS).
                        build());
                break;
        }
        res.ifPresent(response->{
            try {
                String json = objectMapper.writeValueAsString(response);
                UUID refId = UUID.fromString(reservationCreatedEvent.getReferenceId());
                if(createRequestLocksMap.containsKey(refId)) {
                    createResponseMap.put(refId,json);
                    createRequestLocksMap.get(refId).countDown();
                    createRequestLocksMap.remove(refId);
                }
            }catch (Exception ex){
                log.error("Error",ex);
            }
        });
    }

    public void onUpdate(ReservationEventOuterClass.ReservationEvent event){
        ReservationEventOuterClass.ReservationUpdatedEvent reservationUpdatedEvent = event.getUpdated();
        Optional<ReservationResponse> res = Optional.empty();
        switch (event.getResultType()){
            case ERROR:
                res = Optional.of(ReservationResponse.builder().
                        referenceId(UUID.fromString(reservationUpdatedEvent.getReferenceId())).
                        status(ReservationStatus.ERROR).
                        errorMessage(Optional.of(event.getErrorMessage())).
                        build());
                break;
            case SUCCESS:
                res = Optional.of(ReservationResponse.builder().
                        referenceId(UUID.fromString(reservationUpdatedEvent.getReferenceId())).
                        status(ReservationStatus.SUCCESS).
                        build());
                break;
        }
        res.ifPresent(response-> {
            try {
                String json = objectMapper.writeValueAsString(response);
                UUID refId = UUID.fromString(reservationUpdatedEvent.getReferenceId());
                if(updateRequestLocksMap.containsKey(refId)) {
                    updateResponseMap.put(refId,json);
                    updateRequestLocksMap.get(refId).countDown();
                    updateRequestLocksMap.remove(refId);
                }
            } catch (Exception ex) {
                log.error("Error", ex);
            }
        });
    }

    public void onCancel(ReservationEventOuterClass.ReservationEvent event){
        ReservationEventOuterClass.ReservationCancelledEvent reservationCancelledEvent = event.getCancelled();
        Optional<ReservationResponse> res = Optional.empty();
        switch (event.getResultType()){
            case ERROR:
                res = Optional.of(ReservationResponse.builder().
                        referenceId(UUID.fromString(reservationCancelledEvent.getReferenceId())).
                        status(ReservationStatus.ERROR).
                        errorMessage(Optional.of(event.getErrorMessage())).
                        build());
                break;
            case SUCCESS:
                res = Optional.of(ReservationResponse.builder().
                        referenceId(UUID.fromString(reservationCancelledEvent.getReferenceId())).
                        status(ReservationStatus.SUCCESS).
                        build());
                break;
        }
        res.ifPresent(response-> {
            try {
                String json = objectMapper.writeValueAsString(response);
                UUID refId = UUID.fromString(reservationCancelledEvent.getReferenceId());
                if(cancelRequestLocksMap.containsKey(refId)) {
                    cancelResponseMap.put(refId,json);
                    cancelRequestLocksMap.get(refId).countDown();
                    cancelRequestLocksMap.remove(refId);
                }
            } catch (Exception ex) {
                log.error("Error", ex);
            }
        });
    }

    @KafkaListener(topics = "${reservation-events-topic}", groupId = "${reservation.events.consumer.group}")
    public void consume(ConsumerRecord<String, ReservationEventOuterClass.ReservationEvent> message){
        ReservationEventOuterClass.ReservationEvent event = message.value();
        switch (event.getActionType()){
            case CREATED: onCreate(event);break;
            case UPDATED: onUpdate(event);break;
            case CANCELLED: onCancel(event);
        }
    }

    public UUID submitCreate(CreateReservationRequest createReservationRequest){
        UUID referenceId = UUID.randomUUID();
        kafkaTemplate.send(reservationCommandsTopic, createReservationRequest.getCampsiteId(),
                createReservationRequest.toProtobuf(referenceId));
        createRequestLocksMap.put(referenceId,new CountDownLatch(1));
        return referenceId;
    }

    public UUID submitUpdate(UpdateReservationRequest updateReservationRequest){
        UUID referenceId = UUID.randomUUID();
        kafkaTemplate.send(reservationCommandsTopic, updateReservationRequest.getCampsiteId(),
                updateReservationRequest.toProtobuf());
        updateRequestLocksMap.put(referenceId, new CountDownLatch(1));
        return referenceId;
    }

    public UUID submitCancel(CancelReservationRequest cancelReservationRequest){
        UUID referenceId = UUID.randomUUID();
        kafkaTemplate.send(reservationCommandsTopic, cancelReservationRequest.getCampsiteId(),
                cancelReservationRequest.toProtobuf());
        UUID id = UUID.fromString(cancelReservationRequest.getId());
        cancelRequestLocksMap.put(id,new CountDownLatch(1));
        return referenceId;
    }

    private WebSocketMessage createWebSocketMessage(UUID txId,
                                                    WebSocketSession webSocketSession,
                                                    Map<UUID,CountDownLatch> lockRequestMap,
                                                    Map<UUID,String> responseMap) throws InterruptedException {
        lockRequestMap.get(txId).await(timeOutMinutes, TimeUnit.MINUTES);
        WebSocketMessage webSocketMessage = webSocketSession.textMessage(responseMap.get(txId));
        responseMap.remove(txId);
        return webSocketMessage;
    }

    private WebSocketMessage onReceive(String payload,WebSocketSession webSocketSession){
        try {
            ReservationRequest reservationRequest = objectMapper.readValue(payload, CreateReservationRequest.class);
            switch (reservationRequest.getType()){
                case CREATE:
                    UUID createTxId = submitCreate((CreateReservationRequest) reservationRequest);
                    return this.createWebSocketMessage(
                            createTxId,webSocketSession,createRequestLocksMap,createResponseMap);
                case UPDATE:
                    UUID updateTxId = submitUpdate((UpdateReservationRequest) reservationRequest);
                    return this.createWebSocketMessage(
                            updateTxId,webSocketSession,updateRequestLocksMap,updateResponseMap);
                case CANCEL:
                    UUID cancelTxId = submitCancel((CancelReservationRequest) reservationRequest);
                    return this.createWebSocketMessage(
                            cancelTxId,webSocketSession,cancelRequestLocksMap,cancelResponseMap);
            }
        }catch (Exception ex){
            log.error("Error",ex);
        }
        return webSocketSession.textMessage("An error has occurred, please try again later");
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        return webSocketSession.send(webSocketSession.receive().
                map(message -> onReceive(message.getPayloadAsText(),webSocketSession)));
    }
}
