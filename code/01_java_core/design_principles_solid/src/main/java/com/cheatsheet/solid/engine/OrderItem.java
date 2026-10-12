package com.cheatsheet.solid.engine;

import java.math.BigDecimal;

/**
 * Value Object enforcing self-validation invariants.
 */
public record OrderItem(String sku, int quantity, BigDecimal unitPrice) {
    public OrderItem {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("SKU cannot be blank");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
