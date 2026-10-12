package com.cheatsheet.solid.engine;

import java.math.BigDecimal;
import java.util.List;

/**
 * Rich Domain Entity adhering to Tell, Don't Ask, Defensive Copying, and Invariant Protection.
 */
public class Order {
    public enum Status { CREATED, PAID, SHIPPED, CANCELLED }

    private final String orderId;
    private final String customerId;
    private final List<OrderItem> items;
    private Status status;

    public Order(String orderId, String customerId, List<OrderItem> items) {
        if (orderId == null || orderId.isBlank()) throw new IllegalArgumentException("orderId required");
        if (customerId == null || customerId.isBlank()) throw new IllegalArgumentException("customerId required");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Order must have at least one item");
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = List.copyOf(items); // Defensive copying for immutability
        this.status = Status.CREATED;
    }

    // Tell, Don't Ask: Domain logic encapsulated inside entity
    public void markAsPaid() {
        if (this.status != Status.CREATED) {
            throw new IllegalStateException("Cannot transition to PAID from status: " + this.status);
        }
        this.status = Status.PAID;
    }

    public BigDecimal calculateTotal() {
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public Status getStatus() { return status; }
    public List<OrderItem> getItems() { return items; }
}
