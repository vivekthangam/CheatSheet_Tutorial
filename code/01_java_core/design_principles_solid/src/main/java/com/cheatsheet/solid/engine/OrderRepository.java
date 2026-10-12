package com.cheatsheet.solid.engine;

import java.util.Optional;

/**
 * Dependency Inversion Principle (DIP): Port owned by the Domain Layer.
 */
public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String orderId);
}
