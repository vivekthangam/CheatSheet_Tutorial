package com.cheatsheet.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentEventStreamingApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentEventStreamingApplication.class, args);
    }
}
