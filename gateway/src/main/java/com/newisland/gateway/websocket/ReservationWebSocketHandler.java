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

    private Optional<ReservationResponse> createResponse(UUID correlationId,
                                                         ReservationEventOuterClass.ReservationEvent event){

        switch (event.getResultType()){
            case ERROR:
                return Optional.of(ReservationResponse.builder().
                        correlationId(correlationId).
                        status(ReservationStatus.ERROR).
                        errorMessage(Optional.of(event.getErrorMessage())).
                        build());
            case SUCCESS:
                return Optional.of(ReservationResponse.builder().
                        correlationId(correlationId).
                        status(ReservationStatus.SUCCESS).
                        build());
        }
        return Optional.empty();
    }

    public void onCreate(ReservationEventOuterClass.ReservationEvent event){
        UUID correlationId = UUID.fromString(event.getCorrelationId());
        Optional<ReservationResponse> res = createResponse(correlationId,event);
        res.ifPresent(response->{
            try {
                String json = objectMapper.writeValueAsString(response);
                if(createRequestLocksMap.containsKey(correlationId)) {
                    createResponseMap.put(correlationId,json);
                    createRequestLocksMap.get(correlationId).countDown();
                    createRequestLocksMap.remove(correlationId);
                }
            }catch (Exception ex){
                log.error("Error",ex);
            }
        });
    }

    public void onUpdate(ReservationEventOuterClass.ReservationEvent event){
        UUID correlationId = UUID.fromString(event.getCorrelationId());
        Optional<ReservationResponse> res = createResponse(correlationId,event);
        res.ifPresent(response-> {
            try {
                String json = objectMapper.writeValueAsString(response);
                if(updateRequestLocksMap.containsKey(correlationId)) {
                    updateResponseMap.put(correlationId,json);
                    log.info("releasing:"+correlationId);
                    updateRequestLocksMap.get(correlationId).countDown();
                    updateRequestLocksMap.remove(correlationId);
                }else{
                    log.info("No lock for :"+correlationId);
                }
            } catch (Exception ex) {
                log.error("Error", ex);
            }
        });
    }



    public void onCancel(ReservationEventOuterClass.ReservationEvent event){
        UUID correlationId = UUID.fromString(event.getCorrelationId());
        ReservationEventOuterClass.ReservationCancelledEvent reservationCancelledEvent = event.getCancelled();
        Optional<ReservationResponse> res = createResponse(correlationId,event);
        res.ifPresent(response-> {
            try {
                String json = objectMapper.writeValueAsString(response);
                if(cancelRequestLocksMap.containsKey(correlationId)) {
                    cancelResponseMap.put(correlationId,json);
                    cancelRequestLocksMap.get(correlationId).countDown();
                    cancelRequestLocksMap.remove(correlationId);
                }
            } catch (Exception ex) {
                log.error("Error", ex);
            }
        });
    }

    @KafkaListener(topics = "${reservation-events-topic}", groupId = "${reservation.events.consumer.group}")
    public void consume(ConsumerRecord<String, ReservationEventOuterClass.ReservationEvent> message){
        ReservationEventOuterClass.ReservationEvent event = message.value();
        log.info("New Message:"+event);
        switch (event.getActionType()){
            case CREATED: onCreate(event);break;
            case UPDATED: onUpdate(event);break;
            case CANCELLED: onCancel(event);
        }
    }

    public UUID submitCreate(CreateReservationRequest createReservationRequest){
        UUID correlationId = UUID.randomUUID();
        kafkaTemplate.send(reservationCommandsTopic, createReservationRequest.getCampsiteId(),
                createReservationRequest.toProtobuf(correlationId));
        createRequestLocksMap.put(correlationId,new CountDownLatch(1));
        return correlationId;
    }

    public UUID submitUpdate(UpdateReservationRequest updateReservationRequest){
        UUID correlationId = UUID.randomUUID();
        kafkaTemplate.send(reservationCommandsTopic, updateReservationRequest.getCampsiteId(),
                updateReservationRequest.toProtobuf(correlationId.toString()));
        updateRequestLocksMap.put(correlationId, new CountDownLatch(1));
        return correlationId;
    }

    public UUID submitCancel(CancelReservationRequest cancelReservationRequest){
        UUID correlationId = UUID.randomUUID();
        kafkaTemplate.send(reservationCommandsTopic, cancelReservationRequest.getCampsiteId(),
                cancelReservationRequest.toProtobuf(correlationId.toString()));
        cancelRequestLocksMap.put(correlationId,new CountDownLatch(1));
        return correlationId;
    }

    private WebSocketMessage createWebSocketMessage(UUID txId,
                                                    WebSocketSession webSocketSession,
                                                    Map<UUID,CountDownLatch> lockRequestMap,
                                                    Map<UUID,String> responseMap) throws InterruptedException {
        log.info("Waiting for :"+txId);
        lockRequestMap.get(txId).await(timeOutMinutes, TimeUnit.MINUTES);
        WebSocketMessage webSocketMessage = webSocketSession.textMessage(responseMap.get(txId));
        responseMap.remove(txId);
        return webSocketMessage;
    }

    private WebSocketMessage onReceive(String payload,WebSocketSession webSocketSession){
        try {
            ReservationRequest reservationRequest = objectMapper.readValue(payload, ReservationRequest.class);
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
