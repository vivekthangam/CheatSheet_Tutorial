package com.cheatsheet.solid.engine;

import java.math.BigDecimal;

/**
 * Interface Segregation Principle (ISP) Lean Role Interface
 */
public interface PaymentGateway {
    PaymentReceipt charge(String orderId, BigDecimal amount);
}
