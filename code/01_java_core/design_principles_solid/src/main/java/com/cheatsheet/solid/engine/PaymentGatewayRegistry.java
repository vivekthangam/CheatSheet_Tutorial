package com.cheatsheet.solid.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Open/Closed Principle (OCP): Dynamic strategy registry for payment gateways.
 */
public class PaymentGatewayRegistry {
    private final Map<String, PaymentGateway> gateways = new ConcurrentHashMap<>();

    public void register(String providerName, PaymentGateway gateway) {
        if (providerName == null || gateway == null) {
            throw new IllegalArgumentException("Provider name and gateway cannot be null");
        }
        gateways.put(providerName.toUpperCase(), gateway);
    }

    public PaymentGateway getGateway(String providerName) {
        if (providerName == null) {
            throw new IllegalArgumentException("Provider name cannot be null");
        }
        PaymentGateway gateway = gateways.get(providerName.toUpperCase());
        if (gateway == null) {
            throw new UnsupportedOperationException("No payment gateway registered for provider: " + providerName);
        }
        return gateway;
    }
}
