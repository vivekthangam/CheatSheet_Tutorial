package com.cheatsheet.kafka.engine;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.backoff.ExponentialBackOff;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentEventStreamingEngine {

    public record PaymentInitiatedEvent(
        String paymentId,
        String sourceAccount,
        String targetAccount,
        BigDecimal amount,
        String currency,
        Instant timestamp
    ) {}

    public record PaymentSettledEvent(String paymentId, String status, Instant settledAt) {}

    // ============================================================================
    // HARDENED PRODUCER CONFIGURATION
    // ============================================================================
    @Configuration
    public static class ResilientProducerConfig {

        @Bean
        public ProducerFactory<String, Object> producerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

            // Durability & Idempotence
            props.put(ProducerConfig.ACKS_CONFIG, "all");
            props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
            props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
            props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
            props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120_000);

            // Micro-Batching & Compression
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536);
            props.put(ProducerConfig.LINGER_MS_CONFIG, 20);
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "zstd");
            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 67108864);

            return new DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> factory) {
            return new KafkaTemplate<>(factory);
        }
    }

    // ============================================================================
    // HARDENED CONSUMER CONFIGURATION
    // ============================================================================
    @Configuration
    @EnableKafka
    public static class ResilientConsumerConfig {

        @Bean
        public ConsumerFactory<String, PaymentInitiatedEvent> consumerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-settlement-service");
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

            props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
                    "org.apache.kafka.clients.consumer.CooperativeStickyAssignor");

            props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 45_000);
            props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10_000);
            props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300_000);
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 250);

            // Poison Pill Deserializer Armor
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
            props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());
            props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
            props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.cheatsheet.kafka.*");

            return new DefaultKafkaConsumerFactory<>(props);
        }

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, PaymentInitiatedEvent> kafkaListenerContainerFactory(
                ConsumerFactory<String, PaymentInitiatedEvent> consumerFactory,
                KafkaTemplate<String, Object> kafkaTemplate) {

            var factory = new ConcurrentKafkaListenerContainerFactory<String, PaymentInitiatedEvent>();
            factory.setConsumerFactory(consumerFactory);
            factory.setConcurrency(3);
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

            DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                    (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));

            ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
            backOff.setMaxElapsedTime(10000L);

            DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
            errorHandler.addNotRetryableExceptions(org.springframework.kafka.support.serializer.DeserializationException.class);

            factory.setCommonErrorHandler(errorHandler);
            return factory;
        }
    }

    // ============================================================================
    // IDEMPOTENT TRANSACTION CONSUMER SERVICE
    // ============================================================================
    @Service
    public static class PaymentProcessingConsumer {

        private final Set<String> processedTransactions = ConcurrentHashMap.newKeySet();
        private final KafkaTemplate<String, Object> kafkaTemplate;

        public PaymentProcessingConsumer(KafkaTemplate<String, Object> kafkaTemplate) {
            this.kafkaTemplate = kafkaTemplate;
        }

        @KafkaListener(
            topics = "payments.initiated",
            groupId = "payment-settlement-service",
            containerFactory = "kafkaListenerContainerFactory"
        )
        public void processPaymentEvent(ConsumerRecord<String, PaymentInitiatedEvent> record, Acknowledgment ack) {
            PaymentInitiatedEvent event = record.value();

            if (!processedTransactions.add(event.paymentId())) {
                System.out.println("Duplicate payment skipped via Idempotency Cache: " + event.paymentId());
                ack.acknowledge();
                return;
            }

            System.out.println("Settling payment " + event.paymentId() + " for " + event.amount() + " " + event.currency());

            kafkaTemplate.send("payments.settled", event.paymentId(),
                    new PaymentSettledEvent(event.paymentId(), "COMPLETED", Instant.now()));

            ack.acknowledge();
        }

        @KafkaListener(topics = "payments.initiated.DLT", groupId = "payment-dlt-monitoring")
        public void processDeadLetterQueue(ConsumerRecord<String, Object> record, Acknowledgment ack) {
            System.err.println("ALARM: Poison Pill routed to DLT from offset: " + record.offset() +
                    " on partition: " + record.partition());
            ack.acknowledge();
        }
    }
}
