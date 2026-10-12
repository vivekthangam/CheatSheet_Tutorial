package com.cheatsheet.ioc.buglab;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 🛠️ IoC BugLab: Real-World Production Outage Bugs & Forensic Fixes
 */
public class IocBugLab {

    public record OrderCompletedEvent(String orderId) {}

    // =========================================================================
    // BUG 1: Constructor-Injected Circular Dependency
    // =========================================================================
    public static class Bug1_CircularDependency {

        // ❌ BUG: Mutually dependent constructor injection triggers immediate boot failure
        public static class BuggyOrderService {
            private final BuggyInvoiceService invoiceService;
            public BuggyOrderService(BuggyInvoiceService invoiceService) {
                this.invoiceService = invoiceService;
            }
        }

        public static class BuggyInvoiceService {
            private final BuggyOrderService orderService;
            public BuggyInvoiceService(BuggyOrderService orderService) {
                this.orderService = orderService;
            }
        }

        // ✅ REFACTORED: Break cyclic coupling using ApplicationEventPublisher
        @Service
        public static class RefactoredOrderService {
            private final ApplicationEventPublisher eventPublisher;

            public RefactoredOrderService(ApplicationEventPublisher eventPublisher) {
                this.eventPublisher = eventPublisher;
            }

            public void completeOrder(String orderId) {
                eventPublisher.publishEvent(new OrderCompletedEvent(orderId));
            }
        }

        @Service
        public static class RefactoredInvoiceService {
            @EventListener
            public void onOrderCompleted(OrderCompletedEvent event) {
                System.out.println("Processing invoice asynchronously for order: " + event.orderId());
            }
        }
    }

    // =========================================================================
    // BUG 2: Stale Prototype Injected Inside Singleton Causing Data Bleed
    // =========================================================================
    public static class Bug2_StalePrototype {

        @Component
        @Scope("prototype")
        public static class TaxCalculator {
            private String customerExemptCode;

            public void setCustomerExemptCode(String customerExemptCode) {
                this.customerExemptCode = customerExemptCode;
            }

            public BigDecimal computeTax(BigDecimal amount) {
                return "EXEMPT".equalsIgnoreCase(customerExemptCode) ? BigDecimal.ZERO : amount.multiply(BigDecimal.valueOf(0.08));
            }
        }

        // ❌ BUG: Prototype injected once into Singleton; instance is reused and mutated across threads
        @Service
        public static class BuggyOrderProcessor {
            @Autowired
            private TaxCalculator taxCalculator; // 💥 Stale instance! Shared across all concurrent HTTP requests!

            public BigDecimal calculate(BigDecimal amount, String exemptCode) {
                taxCalculator.setCustomerExemptCode(exemptCode);
                return taxCalculator.computeTax(amount);
            }
        }

        // ✅ REFACTORED: Use ObjectProvider<T> to obtain a fresh prototype per request
        @Service
        public static class RefactoredOrderProcessor {
            private final ObjectProvider<TaxCalculator> taxCalculatorProvider;

            public RefactoredOrderProcessor(ObjectProvider<TaxCalculator> taxCalculatorProvider) {
                this.taxCalculatorProvider = taxCalculatorProvider;
            }

            public BigDecimal calculate(BigDecimal amount, String exemptCode) {
                TaxCalculator freshCalculator = taxCalculatorProvider.getObject(); // Fresh instance
                freshCalculator.setCustomerExemptCode(exemptCode);
                return freshCalculator.computeTax(amount);
            }
        }
    }

    // =========================================================================
    // BUG 3: Heavy Blocking External REST Call in @PostConstruct
    // =========================================================================
    public static class Bug3_BlockingPostConstruct {

        // ❌ BUG: Blocking I/O inside @PostConstruct freezes Spring boot coordinator thread
        @Component
        public static class BuggyCacheWarmer {
            @PostConstruct
            public void init() {
                try {
                    // Simulating blocking HTTP call to remote provider
                    Thread.sleep(5000); // 💥 Kubernetes readiness probe times out
                } catch (InterruptedException ignored) {}
            }
        }

        // ✅ REFACTORED: Defer non-critical warm-up to ApplicationReadyEvent on background worker
        @Component
        public static class RefactoredCacheWarmer {
            private final ExecutorService workerPool = Executors.newVirtualThreadPerTaskExecutor();

            @EventListener(ApplicationReadyEvent.class)
            public void onApplicationReady() {
                workerPool.submit(() -> {
                    try {
                        Thread.sleep(5000); // Warm-up executes asynchronously off main thread
                        System.out.println("Asynchronous warm-up completed successfully.");
                    } catch (InterruptedException ignored) {}
                });
            }
        }
    }
}
