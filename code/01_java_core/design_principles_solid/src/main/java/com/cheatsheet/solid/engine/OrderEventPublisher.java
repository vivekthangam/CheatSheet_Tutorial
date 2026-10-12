package com.cheatsheet.solid.engine;

/**
 * Dependency Inversion Principle (DIP): Event publisher port.
 */
public interface OrderEventPublisher {
    void publishOrderPaid(String orderId);
}
