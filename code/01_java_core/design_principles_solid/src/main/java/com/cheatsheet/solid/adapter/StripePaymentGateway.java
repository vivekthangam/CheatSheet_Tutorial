package com.cheatsheet.solid.adapter;

import com.cheatsheet.solid.engine.PaymentReceipt;
import com.cheatsheet.solid.engine.RefundReceipt;
import com.cheatsheet.solid.engine.RefundablePaymentGateway;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Adapter implementation supporting both charges and refunds.
 */
public class StripePaymentGateway implements RefundablePaymentGateway {
    @Override
    public PaymentReceipt charge(String orderId, BigDecimal amount) {
        String txId = "stripe_ch_" + UUID.randomUUID();
        return new PaymentReceipt(txId, true, "AUTH_" + orderId);
    }

    @Override
    public RefundReceipt refund(String transactionId, BigDecimal amount) {
        String reId = "stripe_rf_" + UUID.randomUUID();
        return new RefundReceipt(reId, true);
    }
}
