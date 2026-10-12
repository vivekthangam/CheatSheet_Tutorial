package com.enterprise.typeflux;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * =========================================================================================
 * 🧬 TYPEFLUX FRAMEWORK: REAL-WORLD GENERICS, PECS & TYPE-SYSTEM MASTER SHOWCASE
 * =========================================================================================
 * A complete, production-grade Event-Driven Architecture & Generic Persistence Framework
 * built from scratch to demonstrate every advanced concept in Java Generics:
 *
 * Core Features Implemented:
 * 1. The PECS Principle (Producer Extends, Consumer Super) in an Enterprise EventBus.
 * 2. Neal Gafter's Super Type Token Pattern (TypeReference<T>) to capture erased types.
 * 3. Type-Safe Heterogeneous Container (Joshua Bloch Effective Java Item 33).
 * 4. Synthetic Bridge Method Inspection & Polymorphic Covariance Mechanics.
 * 5. Bounded Wildcards & Generic Pipeline Transformations with full type safety.
 * =========================================================================================
 */
public class TypeFluxGenericsDemo {

    // =====================================================================================
    // 1. NEAL GAFTER'S SUPER TYPE TOKEN PATTERN (Capturing Erased Types)
    // =====================================================================================

    public static abstract class TypeReference<T> {
        private final Type type;

        protected TypeReference() {
            Type superclass = getClass().getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new IllegalArgumentException("TypeReference must be instantiated with generic brackets: new TypeReference<T>() {}");
            }
            this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
        }

        public Type getType() {
            return this.type;
        }

        @Override
        public String toString() {
            return "TypeReference[" + type.getTypeName() + "]";
        }
    }

    // =====================================================================================
    // 2. TYPE-SAFE HETEROGENEOUS CONTAINER (Joshua Bloch Pattern)
    // =====================================================================================

    public static class TypeSafeConfigurationRegistry {
        private final Map<Class<?>, Object> registry = new ConcurrentHashMap<>();

        // Typesafe Put
        public <T> void putConfig(Class<T> type, T value) {
            Objects.requireNonNull(type, "Type token must not be null");
            Objects.requireNonNull(value, "Value must not be null");
            registry.put(type, value);
        }

        // Typesafe Get (Zero unchecked casts for callers!)
        public <T> T getConfig(Class<T> type) {
            Objects.requireNonNull(type, "Type token must not be null");
            return type.cast(registry.get(type));
        }

        public boolean containsConfig(Class<?> type) {
            return registry.containsKey(type);
        }
    }

    // =====================================================================================
    // 3. ENTERPRISE EVENT BUS ENGINE (Demonstrating PECS in Production)
    // =====================================================================================

    public static class TypeFluxEventBus {

        // Internal subscription map storing consumers of any supertype
        private final Map<Class<?>, List<Consumer<?>>> subscribers = new ConcurrentHashMap<>();

        /**
         * PECS RULE IN ACTION:
         * - 'eventType': Producer of events -> Upper Bounded: Class<? extends E>
         * - 'handler':   Consumer of events -> Lower Bounded: Consumer<? super E>
         *
         * Why Consumer<? super E>?
         * Because if someone publishes a 'OrderCreatedEvent', a general handler accepting
         * 'BaseDomainEvent' (its supertype) is completely qualified to consume it!
         */
        @SuppressWarnings("unchecked")
        public <E> void subscribe(Class<? extends E> eventType, Consumer<? super E> handler) {
            Objects.requireNonNull(eventType, "Event type cannot be null");
            Objects.requireNonNull(handler, "Handler cannot be null");

            subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                       .add((Consumer<Object>) handler);

            System.out.println("📥 [SUBSCRIBER REGISTERED] Event: " + eventType.getSimpleName() 
                    + " -> Handler: " + handler.getClass().getName());
        }

        /**
         * Dispatches an event to all exact matches and all compatible supertype handlers.
         */
        @SuppressWarnings("unchecked")
        public <E> void publish(E event) {
            Objects.requireNonNull(event, "Event cannot be null");
            Class<?> eventClass = event.getClass();
            int dispatchCount = 0;

            for (Map.Entry<Class<?>, List<Consumer<?>>> entry : subscribers.entrySet()) {
                Class<?> subscribedType = entry.getKey();

                // If published event is an instance of the subscribed type
                if (subscribedType.isAssignableFrom(eventClass)) {
                    for (Consumer<?> rawConsumer : entry.getValue()) {
                        Consumer<E> typedConsumer = (Consumer<E>) rawConsumer;
                        typedConsumer.accept(event);
                        dispatchCount++;
                    }
                }
            }

            System.out.println("📢 [EVENT PUBLISHED] " + eventClass.getSimpleName() 
                    + " dispatched to " + dispatchCount + " handler(s).\n");
        }
    }

    // =====================================================================================
    // 4. GENERIC ENTITY REPOSITORY WITH RECURSIVE BOUNDS
    // =====================================================================================

    public interface Identifiable<ID extends Comparable<ID>> {
        ID getId();
    }

    public interface Repository<T extends Identifiable<ID>, ID extends Comparable<ID>> {
        void save(T entity);
        Optional<T> findById(ID id);
        List<T> findAllSorted();
    }

    public static class InMemoryRepository<T extends Identifiable<ID>, ID extends Comparable<ID>> 
            implements Repository<T, ID> {

        private final Map<ID, T> database = new ConcurrentHashMap<>();
        private final TypeReference<T> entityToken;

        public InMemoryRepository(TypeReference<T> token) {
            this.entityToken = token;
        }

        @Override
        public void save(T entity) {
            database.put(entity.getId(), entity);
            System.out.println("💾 [PERSISTENCE] Saved entity of type " + entityToken.getType().getTypeName() 
                    + " with Primary Key: " + entity.getId());
        }

        @Override
        public Optional<T> findById(ID id) {
            return Optional.ofNullable(database.get(id));
        }

        // Uses recursive type bound ID.compareTo() to guarantee sortability!
        @Override
        public List<T> findAllSorted() {
            List<T> list = new ArrayList<>(database.values());
            list.sort((a, b) -> a.getId().compareTo(b.getId()));
            return list;
        }
    }

    // =====================================================================================
    // 5. SYNTHETIC BRIDGE METHOD INSPECTION SHOWCASE
    // =====================================================================================

    public interface GenericTransformer<INPUT, OUTPUT> {
        OUTPUT transform(INPUT input);
    }

    // Concrete specialization where compiler generates a Bridge Method
    public static class StringToIntegerTransformer implements GenericTransformer<String, Integer> {
        @Override
        public Integer transform(String input) {
            return Integer.parseInt(input);
        }
    }

    public static void inspectSyntheticBridgeMethods(Class<?> clazz) {
        System.out.println("================================================================================");
        System.out.println("🔬 [BYTECODE INSPECTION] Inspecting Synthetic Bridge Methods for: " + clazz.getSimpleName());
        System.out.println("================================================================================");

        for (Method method : clazz.getDeclaredMethods()) {
            System.out.printf("Method: %-25s | Return: %-15s | Synthetic? %-5s | Bridge? %-5s%n",
                    method.getName(),
                    method.getReturnType().getSimpleName(),
                    method.isSynthetic(),
                    method.isBridge());
        }
        System.out.println("Notice: javac generated a synthetic bridge method 'Object transform(Object)' to");
        System.out.println("preserve polymorphic dispatch for GenericTransformer callers!\n");
    }

    // =====================================================================================
    // 6. BUSINESS DOMAIN & EVENT HIERARCHY
    // =====================================================================================

    // Root Event
    public interface DomainEvent {
        long getTimestamp();
        String getEventId();
    }

    // Base Order Event
    public static class BaseOrderEvent implements DomainEvent {
        private final String eventId = UUID.randomUUID().toString();
        private final long timestamp = System.currentTimeMillis();
        private final String orderId;

        public BaseOrderEvent(String orderId) { this.orderId = orderId; }
        @Override public long getTimestamp() { return timestamp; }
        @Override public String getEventId() { return eventId; }
        public String getOrderId() { return orderId; }
    }

    // Sub-Event 1: Order Created
    public static class OrderCreatedEvent extends BaseOrderEvent {
        private final double amount;
        public OrderCreatedEvent(String orderId, double amount) {
            super(orderId);
            this.amount = amount;
        }
        public double getAmount() { return amount; }
    }

    // Sub-Event 2: Order Shipped
    public static class OrderShippedEvent extends BaseOrderEvent {
        private final String trackingNumber;
        public OrderShippedEvent(String orderId, String trackingNumber) {
            super(orderId);
            this.trackingNumber = trackingNumber;
        }
        public String getTrackingNumber() { return trackingNumber; }
    }

    // Entity implementing Identifiable with Long ID
    public static class CustomerAccount implements Identifiable<Long> {
        private final Long id;
        private final String email;

        public CustomerAccount(Long id, String email) {
            this.id = id;
            this.email = email;
        }

        @Override public Long getId() { return id; }
        public String getEmail() { return email; }

        @Override public String toString() {
            return "CustomerAccount[id=" + id + ", email=" + email + "]";
        }
    }

    // =====================================================================================
    // 7. RUNNABLE APPLICATION ENTRYPOINT
    // =====================================================================================

    public static void main(String[] args) {
        System.out.println("********************************************************************************");
        System.out.println("       🧬 TYPEFLUX GENERICS, PECS & METAPROGRAMMING DEMO ENGINE                 ");
        System.out.println("********************************************************************************\n");

        // ---------------------------------------------------------------------------------
        // DEMO 1: Bridge Method Inspection
        // ---------------------------------------------------------------------------------
        inspectSyntheticBridgeMethods(StringToIntegerTransformer.class);

        // ---------------------------------------------------------------------------------
        // DEMO 2: Type-Safe Heterogeneous Container
        // ---------------------------------------------------------------------------------
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("DEMO 2: Type-Safe Heterogeneous Configuration Container");
        System.out.println("--------------------------------------------------------------------------------");
        TypeSafeConfigurationRegistry config = new TypeSafeConfigurationRegistry();
        config.putConfig(String.class, "https://api.enterprise.com/v1");
        config.putConfig(Integer.class, 5000); // Timeout ms
        config.putConfig(Boolean.class, true);  // TLS enabled

        String endpoint = config.getConfig(String.class);
        Integer timeout = config.getConfig(Integer.class);
        System.out.println("Retrieved Endpoint: " + endpoint);
        System.out.println("Retrieved Timeout:  " + timeout + " ms\n");

        // ---------------------------------------------------------------------------------
        // DEMO 3: Enterprise EventBus with PECS Principle
        // ---------------------------------------------------------------------------------
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("DEMO 3: TypeFlux EventBus with PECS (Producer Extends, Consumer Super)");
        System.out.println("--------------------------------------------------------------------------------");
        TypeFluxEventBus bus = new TypeFluxEventBus();

        // Subscriber A: Listens ONLY to specific OrderCreatedEvent
        bus.subscribe(OrderCreatedEvent.class, (OrderCreatedEvent event) -> {
            System.out.println("  [Specialized Listener] Received OrderCreatedEvent: OrderId=" 
                    + event.getOrderId() + ", Amount=$" + event.getAmount());
        });

        // Subscriber B: General Listener that consumes ANY BaseOrderEvent (Consumer Super!)
        Consumer<BaseOrderEvent> auditLogHandler = (BaseOrderEvent event) -> {
            System.out.println("  [General Audit Log] Auditing order event: OrderId=" 
                    + event.getOrderId() + " (Timestamp: " + event.getTimestamp() + ")");
        };
        bus.subscribe(BaseOrderEvent.class, auditLogHandler);

        // Subscriber C: Global Metric Listener that consumes ANY DomainEvent
        Consumer<DomainEvent> metricsCollector = (DomainEvent event) -> {
            System.out.println("  [Global Metric Collector] Captured event ID: " + event.getEventId());
        };
        bus.subscribe(DomainEvent.class, metricsCollector);

        System.out.println("\nPublishing Events into EventBus:\n");
        bus.publish(new OrderCreatedEvent("ORD-8812", 249.99));
        bus.publish(new OrderShippedEvent("ORD-8812", "TRACK-FDX-99410"));

        // ---------------------------------------------------------------------------------
        // DEMO 4: Generic Repository with Super Type Token
        // ---------------------------------------------------------------------------------
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("DEMO 4: Generic Repository with Super Type Token (TypeReference<T>)");
        System.out.println("--------------------------------------------------------------------------------");
        Repository<CustomerAccount, Long> customerRepo = 
                new InMemoryRepository<>(new TypeReference<CustomerAccount>() {});

        customerRepo.save(new CustomerAccount(103L, "alice@enterprise.com"));
        customerRepo.save(new CustomerAccount(101L, "charlie@enterprise.com"));
        customerRepo.save(new CustomerAccount(102L, "bob@enterprise.com"));

        System.out.println("\nSorted Entities (Guaranteed by Recursive Bound ID extends Comparable<ID>):");
        for (CustomerAccount customer : customerRepo.findAllSorted()) {
            System.out.println("  -> " + customer);
        }

        System.out.println("\n********************************************************************************");
        System.out.println("             ✅ ALL TYPEFLUX GENERICS DEMOS COMPLETED SUCCESSFULLY!             ");
        System.out.println("********************************************************************************");
    }
}
