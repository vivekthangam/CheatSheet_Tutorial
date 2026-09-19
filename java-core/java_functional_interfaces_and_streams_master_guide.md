[🏠 Back to Home](../README.md) | [⚡ Java Core Master Guide](java_master_guide.md) | [🌊 Collections & Streams Guide](java_collection_stream.md) | [🧵 Multithreading Masterclass](java_thread.md)

# 🚀 The Definitive Java Functional Interfaces & Stream API Master Guide

A complete, production-grade guide covering:
1. **What is a Functional Interface?** (SAM contract, rules, `@FunctionalInterface`, and syntax evolution).
2. **Comprehensive Catalog of ALL Built-in Functional Interfaces** in `java.util.function` with standalone code examples.
3. **The Exact Stream API Mapping**: Which Stream method uses which Functional Interface, with syntax, code, and under-the-hood execution mechanics.

---

## 📑 Master Table of Contents
1. [PART 1: What is a Functional Interface?](#part-1-what-is-a-functional-interface)
   - [1.1 The SAM Contract (Single Abstract Method)](#11-the-sam-contract-single-abstract-method)
   - [1.2 What is Allowed Inside a Functional Interface?](#12-what-is-allowed-inside-a-functional-interface)
   - [1.3 The `@FunctionalInterface` Annotation](#13-the-functionalinterface-annotation)
   - [1.4 The 3-Way Syntax Evolution (Anonymous Class ➔ Lambda ➔ Method Reference)](#14-the-3-way-syntax-evolution)
2. [PART 2: The Complete Catalog of ALL Functional Interfaces in Java](#part-2-the-complete-catalog-of-all-functional-interfaces)
   - [2.1 The 4 Primary Archetypes](#21-the-4-primary-archetypes)
   - [2.2 Deep Dive & Standalone Examples for the Big 4](#22-deep-dive--standalone-examples-for-the-big-4)
     - [Predicate<T> (The Decision Maker)](#1-predicatet-the-decision-maker)
     - [Function<T, R> (The Transformer)](#2-functiont-r-the-transformer)
     - [Consumer<T> (The Action Sink)](#3-consumert-the-action-sink)
     - [Supplier<T> (The Factory / Source)](#4-suppliert-the-factory--source)
   - [2.3 Two-Argument Variants (BiPredicate, BiFunction, BiConsumer)](#23-two-argument-variants)
   - [2.4 Operators (UnaryOperator, BinaryOperator)](#24-operators)
   - [2.5 Primitive Specializations (Why They Exist: Auto-Boxing Tax)](#25-primitive-specializations)
   - [2.6 Master Taxonomy Table: All 43 Built-In Interfaces](#26-master-taxonomy-table-all-43-built-in-interfaces)
3. [PART 3: Connecting Stream API to Functional Interfaces](#part-3-connecting-stream-api-to-functional-interfaces)
   - [3.1 Master Stream Mapping Matrix](#31-master-stream-mapping-matrix)
   - [3.2 Stream.filter() ➔ Predicate<T>](#32-streamfilter--predicatet)
   - [3.3 Stream.map() ➔ Function<T, R>](#33-streammap--functiont-r)
   - [3.4 Stream.flatMap() ➔ Function<T, Stream<R>>](#34-streamflatmap--functiont-streamr)
   - [3.5 Stream.forEach() & peek() ➔ Consumer<T>](#35-streamforeach--peek--consumert)
   - [3.6 Stream.generate() ➔ Supplier<T>](#36-streamgenerate--suppliert)
   - [3.7 Stream.reduce() ➔ BinaryOperator<T>](#37-streamreduce--binaryoperatort)
   - [3.8 Stream.allMatch() / anyMatch() / noneMatch() ➔ Predicate<T>](#38-streamallmatch--anymatch--nonematch--predicatet)
   - [3.9 Stream.collect(Collector) ➔ The 4-Interface Composite](#39-streamcollectcollector--the-4-interface-composite)
   - [3.10 Primitive Streams (mapToInt, etc.) ➔ ToIntFunction](#310-primitive-streams-maptoint-etc--tointfunction)
4. [PART 4: Complete End-to-End Enterprise Pipeline Example](#part-4-complete-end-to-end-enterprise-pipeline-example)
5. [PART 5: Common Pitfalls & Production Traps](#part-5-common-pitfalls--production-traps)

---

# PART 1: What is a Functional Interface?

## 1.1 The SAM Contract (Single Abstract Method)
A **Functional Interface** in Java is an interface that defines **exactly one abstract method**.
This single abstract method is known as the **SAM (Single Abstract Method)** contract.

Prior to Java 8, if you wanted to pass behavior (code logic) into a method, you had to instantiate an anonymous class:
```java
// Java 7 style: 6 lines of boilerplate just to pass a single action!
Collections.sort(names, new Comparator<String>() {
    @Override
    public int compare(String s1, String s2) {
        return s1.compareTo(s2);
    }
});
```

Because `Comparator` has **only one abstract method** (`compare`), Java 8 introduced **Lambdas**: the compiler knows that any lambda passed to `sort()` must be the implementation of that single method!
```java
// Java 8+ style: concise, readable, pure intent!
Collections.sort(names, (s1, s2) -> s1.compareTo(s2));
// Or via Method Reference:
Collections.sort(names, String::compareTo);
```

---

## 1.2 What is Allowed Inside a Functional Interface?

A functional interface is NOT limited to just one method total! It can contain:
1. **Exactly ONE Abstract Method** (Mandatory).
2. **Any number of `default` methods** (with default implementations).
3. **Any number of `static` utility methods**.
4. **Any public methods overriding methods from `java.lang.Object`** (e.g. `equals()`, `hashCode()`, `toString()`). These **do not count** against the SAM rule because any implementing class inherently inherits implementations from `Object`.

### Anatomy of a Valid Functional Interface:
```java
@FunctionalInterface
public interface DataFilter<T> {

    // 1. THE SINGLE ABSTRACT METHOD (SAM)
    boolean accept(T data);

    // 2. Default methods DO NOT break the SAM contract!
    default DataFilter<T> and(DataFilter<T> other) {
        return item -> this.accept(item) && other.accept(item);
    }

    default DataFilter<T> negate() {
        return item -> !this.accept(item);
    }

    // 3. Static helper methods DO NOT break the SAM contract!
    static <T> DataFilter<T> alwaysTrue() {
        return item -> true;
    }

    // 4. Object class overrides DO NOT break the SAM contract!
    @Override
    boolean equals(Object obj);
}
```

---

## 1.3 The `@FunctionalInterface` Annotation

The `@FunctionalInterface` annotation is **optional but strongly recommended**.
- **Role**: It acts as a compiler check (similar to `@Override`).
- **Safety**: If you annotate an interface with `@FunctionalInterface` and accidentally add a second abstract method, the Java compiler will refuse to compile with an error:
  ```
  Error: Unexpected @FunctionalInterface annotation; DataFilter is not a functional interface
  multiple non-overriding abstract methods found
  ```

---

## 1.4 The 3-Way Syntax Evolution

Every lambda expression in Java is an instance of a functional interface. You can write them in three equivalent ways:

```java
// Evolution 1: Anonymous Inner Class (Java 1.1 - 7)
Predicate<String> p1 = new Predicate<String>() {
    @Override
    public boolean test(String s) {
        return s.isEmpty();
    }
};

// Evolution 2: Lambda Expression (Java 8+)
Predicate<String> p2 = s -> s.isEmpty();

// Evolution 3: Method Reference (Java 8+)
Predicate<String> p3 = String::isEmpty;
```

---

# PART 2: The Complete Catalog of ALL Functional Interfaces

All standard functional interfaces reside in the package **`java.util.function`**.

## 2.1 The 4 Primary Archetypes

Every functional interface in Java is a variation of 4 basic mental models:

```
                  ┌────────────────────────────────────────────────────────┐
                  │              THE 4 CORE ARCHETYPES                     │
                  └────────────────────────────────────────────────────────┘
                    │                │                 │                │
                    ▼                ▼                 ▼                ▼
             ┌─────────────┐  ┌─────────────┐   ┌─────────────┐  ┌─────────────┐
             │  Predicate  │  │  Function   │   │  Consumer   │  │  Supplier   │
             │   T ➔ bool  │  │    T ➔ R    │   │   T ➔ void  │  │   void ➔ T  │
             └─────────────┘  └─────────────┘   └─────────────┘  └─────────────┘
              "The Bouncer"    "The Machine"     "The Black Hole" "The Factory"
```

1. **`Predicate<T>`**: Takes an input, tests a condition, returns `true` or `false`.
2. **`Function<T, R>`**: Takes an input of type $T$, transforms it, returns an output of type $R$.
3. **`Consumer<T>`**: Takes an input of type $T$, performs an action/side-effect, returns nothing (`void`).
4. **`Supplier<T>`**: Takes no input, produces/manufactures a new object of type $T$.

---

## 2.2 Deep Dive & Standalone Examples for the Big 4

### 1. `Predicate<T>` (The Decision Maker)
- **SAM Signature**: `boolean test(T t)`
- **Mental Model**: A security bouncer checking IDs at a club door.
- **Default Methods**: `.and(other)`, `.or(other)`, `.negate()`, `Predicate.not(...)`

```java
import java.util.function.Predicate;

public class PredicateDemo {
    public static void main(String[] args) {
        // Lambda
        Predicate<Integer> isEven = n -> n % 2 == 0;
        Predicate<Integer> isPositive = n -> n > 0;

        System.out.println("Is 4 even? " + isEven.test(4)); // true
        System.out.println("Is 7 even? " + isEven.test(7)); // false

        // Chaining
        Predicate<Integer> isPositiveAndEven = isPositive.and(isEven);
        System.out.println("Is -4 positive & even? " + isPositiveAndEven.test(-4)); // false
        System.out.println("Is 6 positive & even? " + isPositiveAndEven.test(6));   // true

        // Negate
        Predicate<Integer> isOdd = isEven.negate();
        System.out.println("Is 9 odd? " + isOdd.test(9)); // true
    }
}
```

---

### 2. `Function<T, R>` (The Transformer)
- **SAM Signature**: `R apply(T t)`
- **Mental Model**: A factory machine where you insert raw plastic ($T$) and get a toy car ($R$).
- **Default Methods**: `.andThen(after)`, `.compose(before)`, `Function.identity()`

```java
import java.util.function.Function;

public class FunctionDemo {
    public static void main(String[] args) {
        // Transform String to its Length (String -> Integer)
        Function<String, Integer> stringLength = String::length;
        System.out.println("Length of 'Enterprise': " + stringLength.apply("Enterprise")); // 10

        // Function Chaining: f(x) andThen g(x)
        Function<String, String> trim = String::trim;
        Function<String, String> toUpper = String::toUpperCase;
        Function<String, String> sanitize = trim.andThen(toUpper);

        System.out.println("Sanitized: " + sanitize.apply("  cloud native  ")); // "CLOUD NATIVE"
    }
}
```

---

### 3. `Consumer<T>` (The Action Sink)
- **SAM Signature**: `void accept(T t)`
- **Mental Model**: A paper shredder or shipping label printer. Data goes in, an action is executed, nothing is returned.
- **Default Methods**: `.andThen(after)`

```java
import java.util.function.Consumer;

public class ConsumerDemo {
    public static void main(String[] args) {
        // Print to console
        Consumer<String> logger = msg -> System.out.println("LOG [" + System.currentTimeMillis() + "]: " + msg);
        logger.accept("Service started on port 8080");

        // Consumer Chaining
        Consumer<String> auditLog = msg -> System.out.println("AUDIT: " + msg);
        Consumer<String> multiConsumer = logger.andThen(auditLog);

        multiConsumer.accept("User ID 42 updated billing address");
    }
}
```

---

### 4. `Supplier<T>` (The Factory / Source)
- **SAM Signature**: `T get()`
- **Mental Model**: A vending machine or coffee dispenser. Takes zero inputs, produces an object.
- **Methods**: `T get()`

```java
import java.util.UUID;
import java.util.function.Supplier;

public class SupplierDemo {
    public static void main(String[] args) {
        // Generates a random UUID on demand
        Supplier<String> transactionIdGenerator = () -> UUID.randomUUID().toString();

        System.out.println("Txn 1: " + transactionIdGenerator.get());
        System.out.println("Txn 2: " + transactionIdGenerator.get());
    }
}
```

---

## 2.3 Two-Argument Variants

When you need to pass **two arguments** instead of one:

| Two-Argument Interface | SAM Signature | Input $\to$ Output | Purpose |
| :--- | :--- | :--- | :--- |
| **`BiPredicate<T, U>`** | `boolean test(T t, U u)` | $(T, U) \to \text{boolean}$ | Test 2 items (e.g. check if username and password match). |
| **`BiFunction<T, U, R>`** | `R apply(T t, U u)` | $(T, U) \to R$ | Combine 2 items into a new result (e.g. calculate mortgage from principal and rate). |
| **`BiConsumer<T, U>`** | `void accept(T t, U u)` | $(T, U) \to \text{void}$ | Consume 2 items (e.g. `Map.forEach((key, value) -> ...)`). |

```java
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class BiVariantsDemo {
    public static void main(String[] args) {
        // BiPredicate: Compare two objects
        BiPredicate<String, Integer> isLongerThan = (str, len) -> str.length() > len;
        System.out.println("Is 'Docker' longer than 4? " + isLongerThan.test("Docker", 4)); // true

        // BiFunction: Combine two objects into a 3rd type
        BiFunction<String, Double, String> priceTag = (item, price) -> item + " = $" + String.format("%.2f", price);
        System.out.println(priceTag.apply("Laptop", 1299.99)); // "Laptop = $1299.99"

        // BiConsumer: Act on two inputs
        BiConsumer<String, String> keyValuePairLogger = (k, v) -> System.out.println(k + " ===> " + v);
        keyValuePairLogger.accept("DB_HOST", "postgres.prod.internal");
    }
}
```

---

## 2.4 Operators

When the **input and output are the same type**, Java provides clean shortcuts:

| Interface | Extends | SAM Signature | Purpose |
| :--- | :--- | :--- | :--- |
| **`UnaryOperator<T>`** | `Function<T, T>` | `T apply(T t)` | Transforms $T \to T$ (e.g., `String::toUpperCase`, `x -> x * 2`). |
| **`BinaryOperator<T>`**| `BiFunction<T, T, T>` | `T apply(T t1, T t2)`| Combines two $T$ items into one $T$ (e.g., `(a, b) -> a + b`). Used in `Stream.reduce()`. |

```java
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public class OperatorsDemo {
    public static void main(String[] args) {
        // UnaryOperator: Same type in and out
        UnaryOperator<String> normalizeEmail = email -> email.trim().toLowerCase();
        System.out.println(normalizeEmail.apply("  USER@Enterprise.IO  ")); // "user@enterprise.io"

        // BinaryOperator: Two inputs of type T produce one output of type T
        BinaryOperator<Integer> sum = (a, b) -> a + b;
        System.out.println("Sum: " + sum.apply(15, 25)); // 40
        
        // Static helpers
        BinaryOperator<Integer> max = BinaryOperator.maxBy(Integer::compareTo);
        System.out.println("Max: " + max.apply(42, 99)); // 99
    }
}
```

---

## 2.5 Primitive Specializations (Why They Exist: Auto-Boxing Tax)

In Java, standard generics `Function<Integer, Integer>` operate on **boxed objects** (`java.lang.Integer`).
Under heavy loops, boxing an `int` into an `Integer` object creates billions of temporary heap allocations, destroying CPU cache lines and triggering GC pressure!

Java provides **primitive specializations** for `int`, `long`, and `double` to avoid auto-boxing:

| Standard Generic | Primitive Specialization | SAM Signature | Benefit |
| :--- | :--- | :--- | :--- |
| `Predicate<Integer>` | **`IntPredicate`** | `boolean test(int value)` | Zero boxing |
| `Consumer<Double>` | **`DoubleConsumer`** | `void accept(double value)`| Zero boxing |
| `Supplier<Long>` | **`LongSupplier`** | `long getAsLong()` | Zero boxing |
| `Function<T, Integer>` | **`ToIntFunction<T>`** | `int applyAsInt(T value)` | Eliminates return boxing |
| `Function<Integer, R>` | **`IntFunction<R>`** | `R apply(int value)` | Eliminates input boxing |
| `UnaryOperator<Integer>` | **`IntUnaryOperator`** | `int applyAsInt(int operand)`| 100% pure primitive |
| `BinaryOperator<Long>` | **`LongBinaryOperator`** | `long applyAsLong(long a, long b)` | 100% pure primitive |

---

## 2.6 Master Taxonomy Table: All 43 Built-In Interfaces

Here is the complete catalog of all 43 functional interfaces in `java.util.function`:

| Category | Interface Name | SAM Signature |
| :--- | :--- | :--- |
| **Predicates (4)** | `Predicate<T>` | `boolean test(T t)` |
| | `BiPredicate<T, U>` | `boolean test(T t, U u)` |
| | `IntPredicate` | `boolean test(int value)` |
| | `LongPredicate`, `DoublePredicate` | `boolean test(long/double value)` |
| **Consumers (8)** | `Consumer<T>` | `void accept(T t)` |
| | `BiConsumer<T, U>` | `void accept(T t, U u)` |
| | `IntConsumer`, `LongConsumer`, `DoubleConsumer` | `void accept(int/long/double value)` |
| | `ObjIntConsumer<T>`, `ObjLongConsumer<T>`, `ObjDoubleConsumer<T>` | `void accept(T t, int/long/double value)` |
| **Suppliers (4)** | `Supplier<T>` | `T get()` |
| | `BooleanSupplier` | `boolean getAsBoolean()` |
| | `IntSupplier`, `LongSupplier`, `DoubleSupplier` | `int/long/double getAsInt/Long/Double()` |
| **Functions (17)** | `Function<T, R>` | `R apply(T t)` |
| | `BiFunction<T, U, R>` | `R apply(T t, U u)` |
| | `IntFunction<R>`, `LongFunction<R>`, `DoubleFunction<R>` | `R apply(int/long/double value)` |
| | `ToIntFunction<T>`, `ToLongFunction<T>`, `ToDoubleFunction<T>` | `int/long/double applyAsInt/Long/Double(T value)` |
| | `ToIntBiFunction<T, U>`, `ToLongBiFunction<T, U>`, `ToDoubleBiFunction<T, U>` | `int/long/double applyAsInt/Long/Double(T t, U u)` |
| | `IntToLongFunction`, `IntToDoubleFunction` | `long/double applyAsLong/Double(int value)` |
| | `LongToIntFunction`, `LongToDoubleFunction` | `int/double applyAsInt/Double(long value)` |
| | `DoubleToIntFunction`, `DoubleToLongFunction` | `int/long applyAsInt/Long(double value)` |
| **Operators (10)** | `UnaryOperator<T>` | `T apply(T t)` |
| | `BinaryOperator<T>` | `T apply(T t1, T t2)` |
| | `IntUnaryOperator`, `LongUnaryOperator`, `DoubleUnaryOperator` | `int/long/double applyAsInt/Long/Double(int/long/double x)` |
| | `IntBinaryOperator`, `LongBinaryOperator`, `DoubleBinaryOperator` | `int/long/double applyAsInt/Long/Double(int/long/double a, b)` |

---

# PART 3: Connecting Stream API to Functional Interfaces

The Java Stream API is fundamentally a **functional pipeline framework**. Every single Stream method is powered by one of the functional interfaces above!

## 3.1 Master Stream Mapping Matrix

| Stream API Method | Functional Interface Used | SAM Method Called | Role in the Pipeline |
| :--- | :--- | :--- | :--- |
| **`Stream.filter(...)`** | **`Predicate<? super T>`** | `boolean test(T t)` | **Intermediate**: Discards elements evaluating to `false`. |
| **`Stream.map(...)`** | **`Function<? super T, ? extends R>`**| `R apply(T t)` | **Intermediate**: Transforms each element from $T \to R$. |
| **`Stream.flatMap(...)`** | **`Function<? super T, ? extends Stream<? extends R>>`** | `Stream<R> apply(T t)` | **Intermediate**: Flattens nested collections/streams 1-to-many. |
| **`Stream.peek(...)`** | **`Consumer<? super T>`** | `void accept(T t)` | **Intermediate**: Debugging probe (observes elements without consuming). |
| **`Stream.forEach(...)`** | **`Consumer<? super T>`** | `void accept(T t)` | **Terminal**: Consumes every surviving element for side effects. |
| **`Stream.generate(...)`**| **`Supplier<T>`** | `T get()` | **Source**: Manufactures an infinite stream of new values. |
| **`Stream.reduce(...)`** | **`BinaryOperator<T>`** | `T apply(T a, T b)` | **Terminal**: Folds/combines all elements into a single aggregate result. |
| **`Stream.allMatch(...)`**| **`Predicate<? super T>`** | `boolean test(T t)` | **Terminal (Short-circuit)**: Checks if ALL items satisfy condition. |
| **`Stream.anyMatch(...)`**| **`Predicate<? super T>`** | `boolean test(T t)` | **Terminal (Short-circuit)**: Checks if AT LEAST ONE item satisfies condition. |
| **`Stream.noneMatch(...)`**| **`Predicate<? super T>`** | `boolean test(T t)` | **Terminal (Short-circuit)**: Checks if ZERO items satisfy condition. |
| **`Stream.mapToInt(...)`**| **`ToIntFunction<? super T>`** | `int applyAsInt(T t)` | **Intermediate**: Unboxes to primitive `IntStream` for numeric operations (`sum()`, `average()`). |

---

## 3.2 `Stream.filter()` ➔ `Predicate<T>`

- **Formal Signature**: `Stream<T> filter(Predicate<? super T> predicate)`
- **How it works**: For every element $t$, it calls `predicate.test(t)`. If `true`, the element advances down the conveyor belt. If `false`, it is immediately discarded.

```java
List<String> names = List.of("Alice", "Bob", "Alex", "Charlie", "Anna");

// Predicate: name starts with 'A'
Predicate<String> startsWithA = name -> name.startsWith("A");

List<String> result = names.stream()
    .filter(startsWithA) // Uses Predicate<String>
    .toList();

System.out.println(result); // [Alice, Alex, Anna]
```

---

## 3.3 `Stream.map()` ➔ `Function<T, R>`

- **Formal Signature**: `<R> Stream<R> map(Function<? super T, ? extends R> mapper)`
- **How it works**: For every element $t$, it calls `mapper.apply(t)` and emits the resulting object $r$. The stream type transforms from `Stream<T>` to `Stream<R>`.

```java
List<String> words = List.of("microservice", "kubernetes", "java");

// Function: String -> Integer (calculates word length)
Function<String, Integer> lengthMapper = String::length;

List<Integer> lengths = words.stream()
    .map(lengthMapper) // Uses Function<String, Integer>
    .toList();

System.out.println(lengths); // [12, 10, 4]
```

---

## 3.4 `Stream.flatMap()` ➔ `Function<T, Stream<R>>`

- **Formal Signature**: `<R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper)`
- **How it works**: When each element contains an inner collection or stream (e.g. `List<List<String>>` or an Order containing `List<LineItem>`), `flatMap` uses a `Function` that transforms each element into a stream, then flattens all inner streams into a single flat stream.

```java
List<List<String>> nestedTeams = List.of(
    List.of("Backend-Dev1", "Backend-Dev2"),
    List.of("Frontend-Dev1", "Frontend-Dev2"),
    List.of("DevOps-Engineer")
);

// Function: List<String> -> Stream<String>
Function<List<String>, Stream<String>> flattener = List::stream;

List<String> allEngineers = nestedTeams.stream()
    .flatMap(flattener) // Uses Function<List<String>, Stream<String>>
    .toList();

System.out.println(allEngineers);
// Output: [Backend-Dev1, Backend-Dev2, Frontend-Dev1, Frontend-Dev2, DevOps-Engineer]
```

---

## 3.5 `Stream.forEach()` & `peek()` ➔ `Consumer<T>`

- **`forEach()` Signature**: `void forEach(Consumer<? super T> action)` (Terminal operation: executes pipeline, consumes stream).
- **`peek()` Signature**: `Stream<T> peek(Consumer<? super T> action)` (Intermediate operation: inspects items for debugging without terminating the stream).

```java
List<String> devices = List.of("Router", "Switch", "Firewall");

// Consumer: Print with prefix
Consumer<String> displayAction = dev -> System.out.println("Configuring: " + dev);

devices.stream()
    .peek(dev -> System.out.println("🔍 Debug before action: " + dev)) // Intermediate Consumer
    .forEach(displayAction);                                            // Terminal Consumer
```

---

## 3.6 `Stream.generate()` ➔ `Supplier<T>`

- **Formal Signature**: `static <T> Stream<T> generate(Supplier<T> s)`
- **How it works**: Generates an infinite sequential unordered stream where each element is produced by calling `s.get()`. Must be bounded using `.limit(n)`!

```java
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class StreamGenerateDemo {
    public static void main(String[] args) {
        Random random = new Random();

        // Supplier: produces a random integer between 1000 and 9999
        Supplier<Integer> otpSupplier = () -> 1000 + random.nextInt(9000);

        List<Integer> otps = Stream.generate(otpSupplier) // Uses Supplier<Integer>
            .limit(3)                                     // Bound infinite stream to 3 items
            .toList();

        System.out.println("Generated OTPs: " + otps); // e.g. [4928, 8312, 1054]
    }
}
```

---

## 3.7 `Stream.reduce()` ➔ `BinaryOperator<T>`

- **Formal Signature**: `T reduce(T identity, BinaryOperator<T> accumulator)`
- **How it works**: Folds all elements into a single summary result by repeatedly calling `accumulator.apply(subtotal, nextElement)`.

```java
List<Integer> prices = List.of(100, 250, 40, 60);

// BinaryOperator: (subtotal, current) -> subtotal + current
BinaryOperator<Integer> sumAccumulator = (a, b) -> a + b;

int total = prices.stream()
    .reduce(0, sumAccumulator); // Uses BinaryOperator<Integer>

System.out.println("Total Invoice: $" + total); // $450
```

---

## 3.8 `Stream.allMatch()` / `anyMatch()` / `noneMatch()` ➔ `Predicate<T>`

All matching methods are **Short-Circuiting Terminal Operations** using `Predicate<T>`:
- `allMatch(Predicate)`: Returns `true` if every element passes `test(t)`. Stops immediately on the first `false`!
- `anyMatch(Predicate)`: Returns `true` if at least one element passes `test(t)`. Stops immediately on the first `true`!
- `noneMatch(Predicate)`: Returns `true` if zero elements pass `test(t)`.

```java
List<Integer> scores = List.of(85, 92, 78, 64, 99);

Predicate<Integer> isPassing = score -> score >= 60;
Predicate<Integer> isPerfect = score -> score == 100;

System.out.println("Did everyone pass? " + scores.stream().allMatch(isPassing)); // true
System.out.println("Any perfect scores? " + scores.stream().anyMatch(isPerfect)); // false
```

---

## 3.9 `Stream.collect(Collector)` ➔ The 4-Interface Composite

Under the hood, Java's `Collector<T, A, R>` is an aggregation engine built by combining **four functional interfaces together**:

```java
public interface Collector<T, A, R> {
    // 1. Supplier: Creates the empty container (e.g. new ArrayList())
    Supplier<A> supplier();

    // 2. BiConsumer: Adds an item into the container (e.g. list.add(item))
    BiConsumer<A, T> accumulator();

    // 3. BinaryOperator: Combines two containers in parallel processing
    BinaryOperator<A> combiner();

    // 4. Function: Performs final transformation (e.g. unmodifiable list)
    Function<A, R> finisher();
}
```

When you write:
```java
List<String> list = stream.collect(Collectors.toList());
```
The JVM uses:
- `Supplier`: `ArrayList::new`
- `BiConsumer`: `List::add`
- `BinaryOperator`: `(left, right) -> { left.addAll(right); return left; }`
- `Function`: `Function.identity()`

---

## 3.10 Primitive Streams (`mapToInt`, etc.) ➔ `ToIntFunction`

To avoid auto-boxing numbers, use primitive mapping:
```java
List<String> words = List.of("Microservices", "REST", "gRPC");

// ToIntFunction<String>: String -> primitive int (Zero boxing!)
ToIntFunction<String> toLength = String::length;

int totalCharacters = words.stream()
    .mapToInt(toLength) // Produces IntStream (primitive stream)
    .sum();             // Native primitive sum!

System.out.println("Total characters: " + totalCharacters); // 21
```

---

# PART 4: Complete End-to-End Enterprise Pipeline Example

Here is a complete, runnable enterprise payment audit program connecting **`Predicate`**, **`Function`**, and **`Consumer`** together:

```java
package com.demo.enterprise;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

enum TxnStatus { SUCCESS, FAILED, PENDING }

record Transaction(String txnId, String customerId, double amount, TxnStatus status) {}
record AuditReceiptDTO(String receiptId, String customer, double feeCharged) {}

public class EnterpriseStreamArchitecture {
    public static void main(String[] args) {
        List<Transaction> transactions = List.of(
            new Transaction("TXN-001", "CUST-A", 450.00, TxnStatus.SUCCESS),
            new Transaction("TXN-002", "CUST-B", 25.00, TxnStatus.FAILED),
            new Transaction("TXN-003", "CUST-C", 1200.00, TxnStatus.SUCCESS),
            new Transaction("TXN-004", "CUST-D", 80.00, TxnStatus.PENDING),
            new Transaction("TXN-005", "CUST-E", 3500.00, TxnStatus.SUCCESS)
        );

        System.out.println("=== 1. UNDER-THE-HOOD: EXPLICIT FUNCTIONAL INTERFACES ===");

        // Interface 1: Predicate<Transaction> (Filter Keep/Drop decision)
        Predicate<Transaction> isAuditEligible = new Predicate<Transaction>() {
            @Override
            public boolean test(Transaction t) {
                return t.status() == TxnStatus.SUCCESS && t.amount() >= 100.00;
            }
        };

        // Interface 2: Function<Transaction, AuditReceiptDTO> (Type Projection T -> R)
        Function<Transaction, AuditReceiptDTO> receiptConverter = new Function<Transaction, AuditReceiptDTO>() {
            @Override
            public AuditReceiptDTO apply(Transaction t) {
                double fee = t.amount() * 0.02; // 2% gateway processing fee
                String receiptNum = "RCP-" + t.txnId().replace("TXN-", "");
                return new AuditReceiptDTO(receiptNum, t.customerId(), fee);
            }
        };

        // Interface 3: Consumer<AuditReceiptDTO> (Terminal Action / Side-Effect)
        Consumer<AuditReceiptDTO> auditLogger = new Consumer<AuditReceiptDTO>() {
            @Override
            public void accept(AuditReceiptDTO r) {
                System.out.println("🧾 [AUDIT LEDGER] Receipt: " + r.receiptId() 
                    + " | Customer: " + r.customer() + " | Gateway Fee: $" + r.feeCharged());
            }
        };

        // Pipeline execution using explicit functional interface variables
        transactions.stream()
            .filter(isAuditEligible)    // Predicate<Transaction>
            .map(receiptConverter)      // Function<Transaction, AuditReceiptDTO>
            .forEach(auditLogger);      // Consumer<AuditReceiptDTO>

        System.out.println("\n=== 2. MODERN PRODUCTION STREAM (IDIOMATIC LAMBDA FORM) ===");

        // The exact same pipeline written as clean, modern, professional Java
        transactions.stream()
            .filter(t -> t.status() == TxnStatus.SUCCESS && t.amount() >= 100.00) // Predicate
            .map(t -> new AuditReceiptDTO("RCP-" + t.txnId(), t.customerId(), t.amount() * 0.02)) // Function
            .forEach(r -> System.out.println("🚀 [FAST DISPATCH] Billed $" + r.feeCharged() + " to " + r.customer())); // Consumer
    }
}
```

### Exact Terminal Output
```text
=== 1. UNDER-THE-HOOD: EXPLICIT FUNCTIONAL INTERFACES ===
🧾 [AUDIT LEDGER] Receipt: RCP-001 | Customer: CUST-A | Gateway Fee: $9.0
🧾 [AUDIT LEDGER] Receipt: RCP-003 | Customer: CUST-C | Gateway Fee: $24.0
🧾 [AUDIT LEDGER] Receipt: RCP-005 | Customer: CUST-E | Gateway Fee: $70.0

=== 2. MODERN PRODUCTION STREAM (IDIOMATIC LAMBDA FORM) ===
🚀 [FAST DISPATCH] Billed $9.0 to CUST-A
🚀 [FAST DISPATCH] Billed $24.0 to CUST-C
🚀 [FAST DISPATCH] Billed $70.0 to CUST-E
```

---

# PART 5: Common Pitfalls & Production Traps

1. **Stream Reuse Failure**:
   - Calling any method on a stream after a terminal operation (`forEach`, `collect`, `count`) throws `IllegalStateException: stream has already been operated upon or closed`. Streams are disposable single-use pipelines.
2. **Impure Lambdas in `filter()` or `map()`**:
   - Modifying external shared state (e.g. `list.add()` inside `map()`) introduces race conditions and undefined behavior when running `.parallelStream()`. Keep intermediate functions **pure, stateless, and side-effect free**.
3. **Using `forEach()` for Accumulation**:
   - Never write `stream.map(...).forEach(targetList::add)`. Always use `.toList()` or `.collect(Collectors.toList())` for optimal performance and thread safety.
4. **Checked Exception Barrier**:
   - SAM methods (`test`, `apply`, `accept`, `get`) do not declare `throws Exception`. Wrap checked exceptions in `try-catch` inside your lambda or rethrow as a `RuntimeException` / `UncheckedIOException`.
