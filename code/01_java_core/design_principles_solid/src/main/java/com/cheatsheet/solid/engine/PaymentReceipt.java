package com.cheatsheet.solid.engine;

public record PaymentReceipt(String transactionId, boolean successful, String authorizationCode) {}
