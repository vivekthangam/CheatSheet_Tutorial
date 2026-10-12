package com.cheatsheet.solid.engine;

import java.math.BigDecimal;

/**
 * Interface Segregation Principle (ISP) Extended Role Interface for Refundable Providers
 */
public interface RefundablePaymentGateway extends PaymentGateway {
    RefundReceipt refund(String transactionId, BigDecimal amount);
}
