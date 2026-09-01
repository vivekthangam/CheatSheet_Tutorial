public static void main(String[] args) throws InterruptedException {
        System.out.println("Load Test Starting... Connect VisualVM now.");
        
        // Simulate a web server processing thousands of requests per second
        while (true) {
            generateTraffic();
            // Small sleep to prevent immediate CPU meltdown
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
        // Just a dummy method so the compiler doesn't optimize the code away
        if (obj == null) System.out.println("Empty");
    }