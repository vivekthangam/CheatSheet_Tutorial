package com.tutorial.frameworks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;

/**
 * =========================================================================================
 * JMH MICROBENCHMARK HARNESS & JIT OPTIMIZATION SIMULATOR
 * =========================================================================================
 * 
 * Demonstrates:
 *  1. The 4 Deadly Sins of Naive Java Microbenchmarking:
 *     - Dead Code Elimination (DCE)
 *     - Constant Folding & Constant Propagation
 *     - Monomorphic Inlining Distortion
 *     - Warm-up & Tiered Compilation Inaccuracy
 *  2. The Blackhole Mechanics: How JMH prevents DCE without volatile overhead.
 *  3. State Scope Emulation: Benchmark-level vs Thread-level vs Invocation-level states.
 *  4. High-Resolution Latency & Throughput Metering.
 * =========================================================================================
 */
public class JmhBenchmarkHarnessDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("⏱️ [JMH HARNESS] JAVA MICROBENCHMARK HARNESS & JIT MECHANICS SIMULATOR");
        System.out.println("================================================================================\n");

        // 1. Pitfall Demo: Dead Code Elimination (DCE) in Naive Loops
        demonstrateDeadCodeElimination();

        // 2. Pitfall Demo: Constant Folding
        demonstrateConstantFolding();

        // 3. The Blackhole Solution: Preventing Optimization Distortion
        demonstrateBlackholeConsumption();

        // 4. End-to-End Mini Benchmark: Math.hypot vs Custom Fast Hypotenuse
        runMiniBenchmarkSuite();

        System.out.println("\n================================================================================");
        System.out.println("✅ [JMH HARNESS] BENCHMARK DEMONSTRATION & OPTIMIZATION SIMULATION COMPLETE!");
        System.out.println("================================================================================");
    }

    /**
     * PITFALL 1: Dead Code Elimination
     * In a naive loop, if the result is never returned or consumed, C2 JIT erases the entire loop!
     */
    private static void demonstrateDeadCodeElimination() {
        System.out.println("--- [1] PITFALL: DEAD CODE ELIMINATION (DCE) ---");

        int iterations = 10_000_000;

        // Naive run: calculation result is ignored
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            double dummy = Math.sin(i) * Math.cos(i); // Dead code! JIT can erase this!
        }
        long durationNs = System.nanoTime() - start;

        System.out.printf("Naive DCE Loop (10M iterations):  %10d ns (Avg: %.4f ns/op)%n",
                durationNs, (double) durationNs / iterations);
        System.out.println("⚠️ WARNING: JIT C2 compiler recognizes 'dummy' is unobserved and can eliminate the computation entirely!\n");
    }

    /**
     * PITFALL 2: Constant Folding
     * If inputs to a computation are compile-time constants, javac or C2 replaces the calculation with a precomputed literal.
     */
    private static void demonstrateConstantFolding() {
        System.out.println("--- [2] PITFALL: CONSTANT FOLDING ---");

        final double a = 3.14159;
        final double b = 2.71828;
        int iterations = 10_000_000;

        long start = System.nanoTime();
        double sum = 0;
        for (int i = 0; i < iterations; i++) {
            sum += a * b; // Constant folding: JIT replaces (a * b) with 8.5397342252 at compile time!
        }
        long durationNs = System.nanoTime() - start;

        System.out.printf("Constant-Folded Loop (10M ops):   %10d ns (Avg: %.4f ns/op, Sum: %.2f)%n",
                durationNs, (double) durationNs / iterations, sum);
        System.out.println("⚠️ WARNING: JIT evaluated 'a * b' once, not 10 million times!\n");
    }

    /**
     * THE SOLUTION: JMH Blackhole Pattern
     * Consumes values through an opaque sink to prevent DCE without adding volatile memory fences.
     */
    private static void demonstrateBlackholeConsumption() {
        System.out.println("--- [3] THE FIX: JMH BLACKHOLE PATTERN ---");

        Blackhole bh = new Blackhole();
        int iterations = 10_000_000;

        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            double result = Math.sin(i) * Math.cos(i);
            bh.consume(result); // Blackhole forces JIT to preserve computation!
        }
        long durationNs = System.nanoTime() - start;

        System.out.printf("Blackhole Protected Loop (10M ops): %10d ns (Avg: %.4f ns/op)%n",
                durationNs, (double) durationNs / iterations);
        System.out.println("🛡️ TRUE WORKLOAD MEASURED: JIT cannot optimize away the Math calculations.\n");
    }

    /**
     * WORKFLOW 4: Running a Realistic Controlled Benchmark Suite
     */
    private static void runMiniBenchmarkSuite() {
        System.out.println("--- [4] MINI BENCHMARK: Math.hypot() vs FastHypot() ---");

        int warmupIterations = 50_000;
        int measurementIterations = 2_000_000;

        BenchmarkState state = new BenchmarkState();
        Blackhole bh = new Blackhole();

        // 1. Warm-up Phase (Trigger Tier 1-3 C1 compilation and Tier 4 C2 JIT optimization)
        System.out.println("⚡ Warming up JIT compiler (" + warmupIterations + " iterations)...");
        for (int i = 0; i < warmupIterations; i++) {
            bh.consume(Math.hypot(state.nextX(), state.nextY()));
            bh.consume(fastHypot(state.nextX(), state.nextY()));
        }
        System.out.println("🔥 JIT Warm-up complete. Starting high-precision measurement...\n");

        // 2. Measure Standard Library Math.hypot (Accurate but slower due to overflow/underflow checks)
        long startStd = System.nanoTime();
        for (int i = 0; i < measurementIterations; i++) {
            bh.consume(Math.hypot(state.nextX(), state.nextY()));
        }
        long durationStd = System.nanoTime() - startStd;
        double nsPerOpStd = (double) durationStd / measurementIterations;
        double opsPerSecStd = (measurementIterations / (durationStd / 1_000_000_000.0));

        // 3. Measure Custom Fast Hypot (Direct Pythagorean without IEEE overflow protection)
        long startFast = System.nanoTime();
        for (int i = 0; i < measurementIterations; i++) {
            bh.consume(fastHypot(state.nextX(), state.nextY()));
        }
        long durationFast = System.nanoTime() - startFast;
        double nsPerOpFast = (double) durationFast / measurementIterations;
        double opsPerSecFast = (measurementIterations / (durationFast / 1_000_000_000.0));

        // 4. Output Results Table
        System.out.printf("%-25s | %-15s | %-20s%n", "Benchmark Method", "Latency (ns/op)", "Throughput (ops/sec)");
        System.out.println("------------------------------------------------------------------");
        System.out.printf("%-25s | %15.2f | %20.0f%n", "Math.hypot (Standard)", nsPerOpStd, opsPerSecStd);
        System.out.printf("%-25s | %15.2f | %20.0f%n", "fastHypot (Custom Math)", nsPerOpFast, opsPerSecFast);
        System.out.printf("Speedup: %.2fx faster!%n", nsPerOpStd / nsPerOpFast);
    }

    private static double fastHypot(double x, double y) {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * JMH Blackhole Implementation Pattern
     * Uses non-constant internal state and compiler-opaque operations to prevent Dead Code Elimination.
     */
    public static class Blackhole {
        public int b1 = 0x04030201;
        public int b2 = 0x08070605;

        public void consume(byte b) {
            b1 += b;
        }

        public void consume(int i) {
            b1 += i;
        }

        public void consume(long l) {
            b1 += (int) l;
            b2 += (int) (l >>> 32);
        }

        public void consume(double d) {
            consume(Double.doubleToLongBits(d));
        }

        public void consume(Object obj) {
            b1 += (obj != null ? obj.hashCode() : 0);
        }
    }

    /**
     * Benchmark State Holder (Simulates JMH @State(Scope.Thread))
     */
    public static class BenchmarkState {
        private final double[] xValues = new double[1024];
        private final double[] yValues = new double[1024];
        private int cursor = 0;

        public BenchmarkState() {
            for (int i = 0; i < 1024; i++) {
                xValues[i] = ThreadLocalRandom.current().nextDouble(1.0, 1000.0);
                yValues[i] = ThreadLocalRandom.current().nextDouble(1.0, 1000.0);
            }
        }

        public double nextX() {
            cursor = (cursor + 1) & 1023;
            return xValues[cursor];
        }

        public double nextY() {
            return yValues[cursor];
        }
    }
}
