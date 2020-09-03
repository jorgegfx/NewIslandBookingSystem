package com.newisland.reservation;

import com.newisland.common.messages.command.ReservationCommandOuterClass;
import com.newisland.common.messages.event.ReservationEventOuterClass;
import com.newisland.reservation.deserializer.ReservationCommandProtobufDeserializer;
import com.newisland.reservation.serialization.ReservationEventProtobufSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class ReservationApp {
    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder();
    }

    @Value("${spring-kafka-producer-bootstrap-servers}")
    private String bootstrapProducerServers;

    @Value("${spring-kafka-consumer-bootstrap-servers}")
    private String bootstrapConsumerServers;

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapConsumerServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ReservationCommandProtobufDeserializer.class);
        return props;
    }

    @Bean
    public ConsumerFactory<String, ReservationCommandOuterClass.ReservationCommand> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReservationCommandOuterClass.ReservationCommand> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ReservationCommandOuterClass.ReservationCommand> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapProducerServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ReservationEventProtobufSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, ReservationEventOuterClass.ReservationEvent> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, ReservationEventOuterClass.ReservationEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    public static void main(String[] args) {
        SpringApplication.run(ReservationApp.class, args);
    }
}
