package com.cheatsheet.kafka.buglab;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Real-World Kafka Production Outage Bugs & Fixes (BugLab)
 */
public class KafkaBugLab {

    public record InvoiceRecord(String invoiceId, double amount) {}

    // =========================================================================
    // BUG 1: Sequential Blocking I/O Inside Listener Exceeding max.poll.interval.ms
    // =========================================================================

    public interface TaxService {
        void calculateTaxSync(InvoiceRecord invoice);
        void calculateTaxWithTimeout(InvoiceRecord invoice, Duration timeout);
    }

    public static class MaxPollIntervalRebalanceBug {
        private final TaxService taxService;

        public MaxPollIntervalRebalanceBug(TaxService taxService) {
            this.taxService = taxService;
        }

        // ❌ BUG: Sequential blocking calls take 500s (> default 300s max.poll.interval.ms)
        public void processInvoicesBug(List<InvoiceRecord> records, Acknowledgment ack) {
            for (InvoiceRecord invoice : records) {
                // 💥 Blocks thread; broker assumes consumer died and initiates infinite rebalance!
                taxService.calculateTaxSync(invoice);
            }
            ack.acknowledge();
        }

        // ✅ REFACTORED: Parallel bounded dispatch with strict per-item timeout
        public void processInvoicesHardened(List<InvoiceRecord> records, Acknowledgment ack) {
            records.parallelStream().forEach(invoice ->
                    taxService.calculateTaxWithTimeout(invoice, Duration.ofMillis(800)));
            ack.acknowledge();
        }
    }

    // =========================================================================
    // BUG 2: Poison Pill Causing Infinite Deserialization Crash Loop
    // =========================================================================

    public static class PoisonPillDeserializationBug {
        // ❌ BUG: Standard JsonDeserializer throws uncaught exception; consumer crashes on same offset
        public void configureVulnerableConsumer(Map<String, Object> props) {
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        }

        // ✅ REFACTORED: Wrap with ErrorHandlingDeserializer to catch poison pills and forward to DLT
        public void configureHardenedConsumer(Map<String, Object> props) {
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
            props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        }
    }
}
