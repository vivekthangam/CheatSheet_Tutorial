package com.tutorial.frameworks;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * =========================================================================================
 * PANAMA NATIVE MEMORY ENGINE (Java 22+ FFM API Reference Framework)
 * =========================================================================================
 * 
 * Demonstrates the modern replacement for JNI (Java Native Interface) and sun.misc.Unsafe:
 *  1. Arenas & Lifecycles: Confined, Shared, and Automatic arenas.
 *  2. MemorySegment & MemoryLayout: Structured off-heap native memory allocation.
 *  3. Downcall MethodHandles: Direct invocation of standard C library functions (e.g. strlen, qsort).
 *  4. Upcall Stubs: Passing Java lambda callbacks into native C function pointers.
 *  5. Zero-Copy Off-Heap Ring Buffer: High-throughput native buffer without GC overhead.
 * 
 * Requirements: OpenJDK 22+ (Standard feature JEP 454) or Java 21 preview.
 * =========================================================================================
 */
public class PanamaNativeMemoryEngineDemo {

    public static void main(String[] args) throws Throwable {
        System.out.println("================================================================================");
        System.out.println("🚀 [PANAMA FFM] PROJECT PANAMA FOREIGN FUNCTION & MEMORY API DEMO");
        System.out.println("================================================================================\n");

        // 1. Off-Heap Memory Allocation & Struct Layout
        demonstrateOffHeapMemoryAndLayout();

        // 2. Direct C Standard Library Downcall (strlen)
        demonstrateDowncallStrlen();

        // 3. Native C qsort with Java Upcall Function Pointer Callback
        demonstrateNativeQsortWithJavaCallback();

        // 4. Off-Heap High-Throughput Native Slab Allocator
        demonstrateNativeSlabAllocator();

        System.out.println("\n================================================================================");
        System.out.println("✅ [PANAMA FFM] ALL FOREIGN FUNCTION & MEMORY WORKFLOWS COMPLETED SUCCESSFULLY!");
        System.out.println("================================================================================");
    }

    /**
     * WORKFLOW 1: Off-Heap Memory Segments, Memory Layouts, and Safe Lifecycles
     */
    private static void demonstrateOffHeapMemoryAndLayout() {
        System.out.println("--- [1] OFF-HEAP MEMORY ALLOCATION & STRUCTED LAYOUTS ---");

        // Define a C struct layout:
        // struct OrderPacket {
        //     long orderId;       // 8 bytes (offset 0)
        //     double price;       // 8 bytes (offset 8)
        //     int quantity;       // 4 bytes (offset 16)
        //     byte status;        // 1 byte  (offset 20)
        //     byte padding[3];    // 3 bytes padding (alignment to 8 bytes)
        // };
        GroupLayout orderLayout = MemoryLayout.structLayout(
                ValueLayout.JAVA_LONG.withName("orderId"),
                ValueLayout.JAVA_DOUBLE.withName("price"),
                ValueLayout.JAVA_INT.withName("quantity"),
                ValueLayout.JAVA_BYTE.withName("status"),
                MemoryLayout.paddingLayout(3)
        ).withByteAlignment(8);

        System.out.println("OrderPacket Struct Byte Size: " + orderLayout.byteSize() + " bytes");

        // Using Confined Arena (deterministic lifecycle, bound to current thread)
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(orderLayout);

            // VarHandles derived directly from MemoryLayout path elements
            var orderIdHandle = orderLayout.varHandle(MemoryLayout.PathElement.groupElement("orderId"));
            var priceHandle = orderLayout.varHandle(MemoryLayout.PathElement.groupElement("price"));
            var quantityHandle = orderLayout.varHandle(MemoryLayout.PathElement.groupElement("quantity"));
            var statusHandle = orderLayout.varHandle(MemoryLayout.PathElement.groupElement("status"));

            // Write fields into native off-heap memory
            orderIdHandle.set(segment, 0L, 9876543210L);
            priceHandle.set(segment, 0L, 452.75);
            quantityHandle.set(segment, 0L, 500);
            statusHandle.set(segment, 0L, (byte) 1); // 1 = FILLED

            // Read fields back from native off-heap memory
            long readOrderId = (long) orderIdHandle.get(segment, 0L);
            double readPrice = (double) priceHandle.get(segment, 0L);
            int readQuantity = (int) quantityHandle.get(segment, 0L);
            byte readStatus = (byte) statusHandle.get(segment, 0L);

            System.out.printf("Read Off-Heap Order: ID=%d, Price=%.2f, Qty=%d, Status=%d%n",
                    readOrderId, readPrice, readQuantity, readStatus);
            System.out.println("Native Memory Base Address: 0x" + Long.toHexString(segment.address()));
        } // Arena closes here: native memory is instantly freed, 0 GC pause, 0 memory leak!

        System.out.println("Arena successfully closed. Native memory reclaimed deterministically.\n");
    }

    /**
     * WORKFLOW 2: Downcall MethodHandle - Calling C standard library strlen
     */
    private static void demonstrateDowncallStrlen() throws Throwable {
        System.out.println("--- [2] DIRECT C STDLIB DOWNCALL (strlen) ---");

        Linker linker = Linker.nativeLinker();
        SymbolLookup stdlib = linker.defaultLookup();

        // Find native function pointer to "strlen"
        MemorySegment strlenAddress = stdlib.find("strlen")
                .orElseThrow(() -> new IllegalStateException("C symbol strlen not found in standard library"));

        // Function Descriptor: size_t strlen(const char *s)
        // Return type: JAVA_LONG (size_t on 64-bit platforms), Parameter: ADDRESS (pointer)
        FunctionDescriptor descriptor = FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS);

        // Obtain downcall MethodHandle (pure direct call without any JNI C stub!)
        MethodHandle strlenHandle = linker.downcallHandle(strlenAddress, descriptor);

        try (Arena arena = Arena.ofConfined()) {
            String testString = "Project Panama Foreign Function & Memory API 2026";
            MemorySegment cString = arena.allocateFrom(testString);

            // Invoke native strlen directly via MethodHandle!
            long nativeCalculatedLength = (long) strlenHandle.invokeExact(cString);

            System.out.println("Java String: \"" + testString + "\"");
            System.out.println("Java String.length(): " + testString.length());
            System.out.println("Native C strlen():    " + nativeCalculatedLength);
            assert nativeCalculatedLength == testString.length() : "Length mismatch!";
        }
        System.out.println();
    }

    /**
     * WORKFLOW 3: Upcall Stubs - Passing Java Lambda Callback into Native C qsort
     */
    private static void demonstrateNativeQsortWithJavaCallback() throws Throwable {
        System.out.println("--- [3] NATIVE C qsort WITH JAVA LAMBDA UPCALL CALLBACK ---");

        Linker linker = Linker.nativeLinker();
        SymbolLookup stdlib = linker.defaultLookup();

        MemorySegment qsortAddress = stdlib.find("qsort")
                .orElseThrow(() -> new IllegalStateException("C symbol qsort not found"));

        // void qsort(void *base, size_t nmemb, size_t size, int (*compar)(const void *, const void *))
        FunctionDescriptor qsortDescriptor = FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS,    // base array pointer
                ValueLayout.JAVA_LONG,  // nmemb (element count)
                ValueLayout.JAVA_LONG,  // size (element byte size)
                ValueLayout.ADDRESS     // compar function pointer
        );

        MethodHandle qsortHandle = linker.downcallHandle(qsortAddress, qsortDescriptor);

        // Descriptor for comparator callback: int compar(const void *a, const void *b)
        FunctionDescriptor comparDescriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS
        );

        try (Arena arena = Arena.ofConfined()) {
            int[] rawData = { 87, 12, 95, 34, 5, 62, 19, 44 };
            System.out.println("Unsorted Java Array: " + Arrays.toString(rawData));

            // Allocate native array off-heap
            MemorySegment nativeArray = arena.allocateFrom(ValueLayout.JAVA_INT, rawData);

            // Define Java comparison method to be invoked by native C qsort
            MethodHandle compareTarget = MethodHandles.lookup().findStatic(
                    PanamaNativeMemoryEngineDemo.class,
                    "nativeIntComparator",
                    java.lang.invoke.MethodType.methodType(int.class, MemorySegment.class, MemorySegment.class)
            );

            // Create an UpcallStub (turns Java MethodHandle into a C function pointer!)
            MemorySegment comparStub = linker.upcallStub(compareTarget, comparDescriptor, arena);

            // Call native qsort: base, nmemb, size, compar
            qsortHandle.invokeExact(nativeArray, (long) rawData.length, (long) ValueLayout.JAVA_INT.byteSize(), comparStub);

            // Read sorted native array back into Java
            int[] sortedData = nativeArray.toArray(ValueLayout.JAVA_INT);
            System.out.println("Sorted by C qsort:   " + Arrays.toString(sortedData));
        }
        System.out.println();
    }

    /**
     * Java comparator invoked by native C qsort via Panama UpcallStub
     */
    public static int nativeIntComparator(MemorySegment addrA, MemorySegment addrB) {
        // Read 32-bit integers from pointers
        int valA = addrA.get(ValueLayout.JAVA_INT, 0);
        int valB = addrB.get(ValueLayout.JAVA_INT, 0);
        return Integer.compare(valA, valB);
    }

    /**
     * WORKFLOW 4: High-Throughput Zero-Copy Native Slab Allocator
     */
    private static void demonstrateNativeSlabAllocator() {
        System.out.println("--- [4] ZERO-COPY OFF-HEAP SLAB ALLOCATOR ---");

        int slabCapacityBytes = 1024 * 1024; // 1 MB off-heap slab
        NativeSlabAllocator slab = new NativeSlabAllocator(slabCapacityBytes);

        try {
            // Allocate 3 off-heap chunks sequentially
            MemorySegment chunk1 = slab.allocate(128);
            MemorySegment chunk2 = slab.allocate(256);
            MemorySegment chunk3 = slab.allocate(512);

            // Write arbitrary binary data
            chunk1.setUtf8String(0, "High Frequency Trading Order #1");
            chunk2.setUtf8String(0, "High Frequency Trading Order #2");
            chunk3.setUtf8String(0, "High Frequency Trading Order #3");

            System.out.println("Chunk 1 Payload: " + chunk1.getUtf8String(0));
            System.out.println("Chunk 2 Payload: " + chunk2.getUtf8String(0));
            System.out.println("Chunk 3 Payload: " + chunk3.getUtf8String(0));
            System.out.println("Allocated Bytes from Slab: " + slab.getAllocatedBytes() + " / " + slabCapacityBytes);

            // Reset allocator in O(1) time without any GC overhead
            slab.reset();
            System.out.println("Slab reset in O(1) time. Allocated Bytes: " + slab.getAllocatedBytes());
        } finally {
            slab.close();
            System.out.println("Slab closed and native memory unmapped.");
        }
    }

    /**
     * Custom Off-Heap Bump Slab Allocator using Panama Arena & MemorySegment
     */
    public static class NativeSlabAllocator implements AutoCloseable {
        private final Arena arena;
        private final MemorySegment rootSegment;
        private final long capacity;
        private long offset = 0;

        public NativeSlabAllocator(long capacityBytes) {
            this.capacity = capacityBytes;
            this.arena = Arena.ofShared(); // Shared across threads
            this.rootSegment = arena.allocate(capacityBytes, 8); // 8-byte aligned
        }

        public synchronized MemorySegment allocate(long byteSize) {
            long alignedSize = (byteSize + 7) & ~7; // 8-byte alignment
            if (offset + alignedSize > capacity) {
                throw new OutOfMemoryError("Native slab exhausted! Capacity: " + capacity + ", Requested: " + alignedSize);
            }
            MemorySegment slice = rootSegment.asSlice(offset, alignedSize);
            offset += alignedSize;
            return slice;
        }

        public synchronized void reset() {
            this.offset = 0; // O(1) bump-pointer reset
        }

        public synchronized long getAllocatedBytes() {
            return offset;
        }

        @Override
        public void close() {
            arena.close();
        }
    }
}
