package com.cheatsheet.kafka.designlab;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Architectural Anti-Patterns & Refactorings for Kafka (DesignLab)
 */
public class KafkaDesignLab {

    public record Order(String id, String customerId, double amount) {}
    public record OutboxRecord(String topic, String key, String payload) {}

    public interface OrderRepository {
        void save(Order order);
    }

    public interface OutboxRepository {
        void save(OutboxRecord outbox);
    }

    // =========================================================================
    // ANTI-PATTERN 1: Dual-Write Hazard Inside @Transactional
    // =========================================================================

    @Service
    public static class TransactionalDualWriteHazard {
        private final OrderRepository orderRepository;
        private final KafkaTemplate<String, Object> kafkaTemplate;
        private final OutboxRepository outboxRepository;

        public TransactionalDualWriteHazard(OrderRepository orderRepository,
                                           KafkaTemplate<String, Object> kafkaTemplate,
                                           OutboxRepository outboxRepository) {
            this.orderRepository = orderRepository;
            this.kafkaTemplate = kafkaTemplate;
            this.outboxRepository = outboxRepository;
        }

        // ⚠️ ANTI-PATTERN: Direct Kafka emission inside DB transaction can fail or roll back
        @Transactional
        public void placeOrderHazardous(Order order) {
            orderRepository.save(order);
            // 💥 If DB transaction rolls back during commit, message was ALREADY sent to Kafka!
            kafkaTemplate.send("orders.placed", order.id(), order);
        }

        // 🏗️ REFACTOR: Transactional Outbox Pattern guaranteed by database atomicity
        @Transactional
        public void placeOrderHardened(Order order) {
            orderRepository.save(order);
            // Write into outbox table in same atomic DB transaction
            outboxRepository.save(new OutboxRecord("orders.placed", order.id(), order.toString()));
            // CDC Debezium connector streams outbox table to Kafka with 100% guarantee
        }
    }

    // =========================================================================
    // ANTI-PATTERN 2: Unkeyed Messages Creating Out-of-Order Chaos
    // =========================================================================

    @Service
    public static class UnkeyedPartitioningAntiPattern {
        private final KafkaTemplate<String, Object> kafkaTemplate;

        public UnkeyedPartitioningAntiPattern(KafkaTemplate<String, Object> kafkaTemplate) {
            this.kafkaTemplate = kafkaTemplate;
        }

        // ⚠️ ANTI-PATTERN: Null partition key round-robins across partitions, race condition
        public void updateOrderStatusUnkeyed(Order order) {
            kafkaTemplate.send("orders.status-updates", order);
        }

        // 🏗️ REFACTOR: Provide consistent entity key to guarantee per-order FIFO ordering
        public void updateOrderStatusKeyed(Order order) {
            kafkaTemplate.send("orders.status-updates", order.id(), order);
        }
    }
}
