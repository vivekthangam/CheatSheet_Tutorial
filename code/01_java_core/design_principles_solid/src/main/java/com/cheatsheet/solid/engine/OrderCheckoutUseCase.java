package com.cheatsheet.solid.engine;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Single Responsibility Principle (SRP) Use Case orchestrator.
 * Orchestrates payment, state transition, persistence, and event notifications.
 */
public class OrderCheckoutUseCase {
    private final OrderRepository repository;
    private final PaymentGatewayRegistry gatewayRegistry;
    private final OrderEventPublisher eventPublisher;

    public OrderCheckoutUseCase(OrderRepository repository,
                                PaymentGatewayRegistry gatewayRegistry,
                                OrderEventPublisher eventPublisher) {
        this.repository = Objects.requireNonNull(repository, "repository required");
        this.gatewayRegistry = Objects.requireNonNull(gatewayRegistry, "gatewayRegistry required");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher required");
    }

    public PaymentReceipt execute(String orderId, String providerName) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        BigDecimal total = order.calculateTotal();
        PaymentGateway gateway = gatewayRegistry.getGateway(providerName);
        PaymentReceipt receipt = gateway.charge(order.getOrderId(), total);

        if (receipt.successful()) {
            order.markAsPaid();
            repository.save(order);
            eventPublisher.publishOrderPaid(order.getOrderId());
        }

        return receipt;
    }
}
