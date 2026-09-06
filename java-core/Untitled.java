package com.tutorial.jvm;

import java.util.concurrent.ThreadLocalRandom;

/**
 * JVM Garbage Collection & VisualVM Memory Load Simulator
 * Demonstrates high-throughput allocation of short-lived objects in Eden space.
 */
public class Untitled {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Load Test Starting... Connect VisualVM / JConsole now.");
        
        // Simulate a web server processing thousands of requests per second
        while (true) {
            generateTraffic();
            // Small sleep to prevent immediate CPU saturation
            Thread.sleep(1); 
        }
    }

    private static void generateTraffic() {
        // Create "Short-lived" objects (like HTTP request/response objects)
        for (int i = 0; i < 5000; i++) {
            String data = "RequestID-" + ThreadLocalRandom.current().nextInt();
            processData(data);
        }
    }

    private static void processData(Object obj) {
        // Dummy consumer method to prevent JIT dead-code elimination
        if (obj == null) {
            System.out.println("Empty");
        }
    }
}