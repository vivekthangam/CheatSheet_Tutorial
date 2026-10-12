package com.cheatsheet.ioc.designlab;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 🏗️ IoC DesignLab: Architectural Anti-Patterns & Refactorings
 */
public class IocDesignLab {

    public record OrderCommand(String orderId, BigDecimal amount, String customerId) {}
    public record PaymentReceipt(String transactionId, boolean approved) {}
    public record OrderResult(String orderId, boolean success, String trackingNumber) {}

    // =========================================================================
    // ARCH 1: God-Service Mega-Bean Decomposition
    // =========================================================================
    public static class Arch1_GodServiceDecomposition {

        @Service
        public static class OrderValidationDelegate {
            public void validate(OrderCommand cmd) {
                if (cmd.amount().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Order amount must be positive");
                }
            }
        }

        @Service
        public static class PaymentSettlementDelegate {
            public PaymentReceipt charge(OrderCommand cmd) {
                return new PaymentReceipt("TX-" + cmd.orderId(), true);
            }
        }

        @Service
        public static class OrderFulfillmentDelegate {
            public OrderResult fulfill(OrderCommand cmd, PaymentReceipt receipt) {
                return new OrderResult(cmd.orderId(), receipt.approved(), "TRACK-" + cmd.orderId());
            }
        }

        // 🏗️ REFACTOR: Cohesive Domain Facade orchestrating dedicated, testable domain delegates
        @Service
        public static class OrderProcessingFacade {
            private final OrderValidationDelegate validator;
            private final PaymentSettlementDelegate settlement;
            private final OrderFulfillmentDelegate fulfillment;

            public OrderProcessingFacade(OrderValidationDelegate validator,
                                         PaymentSettlementDelegate settlement,
                                         OrderFulfillmentDelegate fulfillment) {
                this.validator = validator;
                this.settlement = settlement;
                this.fulfillment = fulfillment;
            }

            public OrderResult process(OrderCommand cmd) {
                validator.validate(cmd);
                PaymentReceipt receipt = settlement.charge(cmd);
                return fulfillment.fulfill(cmd, receipt);
            }
        }
    }

    // =========================================================================
    // ARCH 2: Lightweight Configuration (proxyBeanMethods = false)
    // =========================================================================
    // 🏗️ REFACTOR: proxyBeanMethods = false eliminates CGLIB proxy overhead for light configs
    @Configuration(proxyBeanMethods = false)
    public static class LightweightInvoicingConfiguration {

        @Bean
        public String defaultCurrencyCode() {
            return "USD";
        }
    }
}
