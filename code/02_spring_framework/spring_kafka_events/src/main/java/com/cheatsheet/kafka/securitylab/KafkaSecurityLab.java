package com.cheatsheet.kafka.securitylab;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

/**
 * Real-World Kafka Security Vulnerabilities & Mitigations (SecurityLab)
 */
public class KafkaSecurityLab {

    // =========================================================================
    // VULNERABILITY 1: Plaintext Network Sniffing & Missing Transport Encryption
    // =========================================================================

    public static class TransportSecurityDefense {
        // 🚨 VULNERABILITY: Plaintext transmission allows packet interception inside VPC
        public void configureInsecureTransport(Map<String, Object> props) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");
        }

        // 🛡️ MITIGATION: Enforce SSL/TLS mutual authentication
        public void configureSecureTransport(Map<String, Object> props, String keyStorePass, String trustStorePass) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "/var/private/ssl/kafka.keystore.jks");
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keyStorePass);
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "/var/private/ssl/kafka.truststore.jks");
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, trustStorePass);
        }
    }

    // =========================================================================
    // VULNERABILITY 2: Replay Attack on Financial Event Consumer
    // =========================================================================

    @Service
    public static class DistributedReplayAttackDefense {
        private final StringRedisTemplate redisTemplate;

        public DistributedReplayAttackDefense(StringRedisTemplate redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        // 🛡️ MITIGATION: Validate timestamp threshold and acquire distributed atomic nonce lock
        public boolean verifyAndLockPayment(String paymentId, String nonce, long eventTimestampMs) {
            // Check 5-minute skew window
            if (Math.abs(System.currentTimeMillis() - eventTimestampMs) > 300_000) {
                throw new SecurityException("Expired transaction timestamp replay rejected");
            }

            // Atomic SETNX prevents double execution
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent("tx_nonce:" + paymentId + ":" + nonce, "LOCKED", Duration.ofHours(24));
            return Boolean.TRUE.equals(acquired);
        }
    }
}
