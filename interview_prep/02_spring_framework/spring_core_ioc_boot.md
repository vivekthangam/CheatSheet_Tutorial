# Spring Core IoC, Dependency Injection & Spring Boot 3: Enterprise Interview Guide

> **Curriculum Milestone**: Module 02 - Spring Framework Engineering  
> **Topic Coverage**: Inversion of Control (IoC), ApplicationContext Lifecycle, Bean Scopes & Proxying, Constructor vs Field Injection, Auto‑Configuration, Conditional Bean Registration, Spring Boot Starters, YAML Multi‑Profile, GraalVM AOT, Actuator Probes, Bean Post‑Processors, BeanFactoryPostProcessor, `@Transactional` Proxy Mechanics, Transaction Propagation (all 7 types), AOP Architecture (CGLIB vs JDK Dynamic Proxy), `@Configuration` proxyBeanMethods, Spring Events, `@Async` & Thread Pool Configuration, `@Cacheable` Internals, Spring Retry, `@ConfigurationProperties` vs `@Value`, Graceful Shutdown, Spring Profiles, and Kubernetes Production Readiness.  
> **Target Audience**: Senior Software Engineers, Lead Architects, Staff & Principal Engineers.  
> **Target Depth**: 50+ Progressive Battle-Tested Scenarios, Production Code Walkthroughs, Architectural Trade-Off Matrices, Beginner Anti-Patterns, and Real-World Outage Post-Mortems.

---

## Architecture Blueprint: The Spring IoC & Boot Stack

```
+-------------------------------------------------------------------------------------------+
|                          Spring Boot Application Startup                                    |
|                                                                                             |
|  +-----------------------------+  +------------------------------------------------------+  |
|  |  SpringApplication.run()    |  |  Auto-Configuration Engine                           |  |
|  |  ├─ Environment Setup       |  |  ├─ META-INF/spring/AutoConfiguration.imports        |  |
|  |  ├─ Banner + Logging Init   |  |  ├─ @Conditional* Evaluators                        |  |
|  |  ├─ ApplicationContext      |  |  ├─ Bean Definition Registry                        |  |
|  |  │   Creation               |  |  └─ BeanFactory Post-Processing                     |  |
|  |  └─ Embedded Server Start   |  +------------------------------------------------------+  |
|  +-----------------------------+                                                            |
|                                                                                             |
|  +------------------------------------------------------+  +----------------------------+  |
|  |  ApplicationContext (IoC Container)                   |  |  AOP Proxy Layer           |  |
|  |  ├─ BeanDefinition Registry                           |  |  ├─ CGLIB (class proxy)    |  |
|  |  ├─ Singleton Cache (ConcurrentHashMap)               |  |  ├─ JDK Dynamic (iface)   |  |
|  |  ├─ BeanPostProcessor Chain                           |  |  ├─ @Transactional         |  |
|  |  ├─ BeanFactoryPostProcessor Chain                    |  |  ├─ @Cacheable             |  |
|  |  ├─ Event Multicaster (ApplicationEvent)              |  |  └─ @Async                 |  |
|  |  └─ Lifecycle (SmartLifecycle, @PreDestroy)           |  +----------------------------+  |
|  +------------------------------------------------------+                                  |
|                                                                                             |
|  Bean Lifecycle (12-Step Sequence)                                                          |
|  1. Instantiation (Constructor)                                                             |
|  2. Property Population (@Autowired / Constructor Injection)                                |
|  3. BeanNameAware.setBeanName()                                                             |
|  4. BeanFactoryAware.setBeanFactory()                                                       |
|  5. ApplicationContextAware.setApplicationContext()                                         |
|  6. BeanPostProcessor.postProcessBeforeInitialization() ← @PostConstruct fires HERE        |
|  7. InitializingBean.afterPropertiesSet()                                                   |
|  8. Custom @Bean(initMethod = "init")                                                       |
|  9. BeanPostProcessor.postProcessAfterInitialization() ← AOP proxies created HERE          |
|  10. Bean READY — in singleton cache                                                        |
|  11. @PreDestroy (on graceful shutdown)                                                     |
|  12. DisposableBean.destroy() / custom destroyMethod                                        |
+-------------------------------------------------------------------------------------------+
```

---

## Section 1: Progressive Scenario-Based Master Q&A (50 Scenarios)

### Tier 1: Core Fundamentals — IoC, DI & Bean Lifecycle (Q1 – Q15)

---

#### Q1: Why IoC Exists & What ApplicationContext Is at the JVM Level

##### 1. Exact Scenario & Question
A junior developer on your team writes every service class by directly instantiating its dependencies with `new`: `new OrderRepository(new DataSource(...))`. You explain this violates clean architecture and makes the system untestable. Detail the core problem that Inversion of Control solves, what the `ApplicationContext` actually is at the JVM level, and trace the entire 7-step startup sequence when `SpringApplication.run()` is called.

##### 2. What the Interviewer Evaluates
- Understanding that IoC inverts the flow of control: the framework creates objects, not the developer.
- Knowledge that `ApplicationContext` is backed by `DefaultListableBeanFactory` containing a `ConcurrentHashMap<String, BeanDefinition>` and a `ConcurrentHashMap<String, Object>` singleton cache.
- Ability to trace the 7-step startup sequence from classpath scanning to embedded server start.
- **Average vs. Elite**: Average says "Spring creates objects for you." Elite explains the three‑phase lifecycle: BeanDefinition scanning → BeanFactoryPostProcessor → BeanPostProcessor → singleton instantiation → AOP proxy wrapping → embedded server start, and names the concrete classes involved (`DefaultListableBeanFactory`, `AutowiredAnnotationBeanPostProcessor`, `ConfigurationClassPostProcessor`).

##### 3. Standout Technical Answer

**The Tight-Coupling Problem:**
```java
// ❌ TIGHT COUPLING — ClassA instantiates its own dependencies
public class OrderService {
    private final OrderRepository repo;

    public OrderService() {
        // Hard-coded dependency instantiation:
        // 1. Any change to DataSource constructor signature breaks OrderService
        // 2. Testing OrderService in isolation requires a real database
        // 3. Cannot swap implementations (mock, test double, different DB)
        this.repo = new OrderRepository(new DataSource("jdbc:postgresql://localhost/orders"));
    }
}
```

**Inversion of Control — Spring's Solution:**
When `SpringApplication.run()` is called, Spring performs this 7-step sequence:

```
Step 1: Environment Bootstrap
  └─ Loads application.yml / application.properties
  └─ Resolves Spring profiles (@Profile annotations)
  └─ Populates PropertySources (system props, env vars, yaml, etc.)

Step 2: ApplicationContext Creation
  └─ Web apps: AnnotationConfigServletWebServerApplicationContext
  └─ WebFlux apps: AnnotationConfigReactiveWebServerApplicationContext
  └─ Non-web: AnnotationConfigApplicationContext

Step 3: BeanDefinition Scanning (ConfigurationClassPostProcessor)
  └─ @ComponentScan: Recursively scans packages for @Component stereotypes
  └─ Each class → BeanDefinition (metadata: class, scope, init/destroy methods)
  └─ BeanDefinitions stored in DefaultListableBeanFactory.beanDefinitionMap

Step 4: BeanFactoryPostProcessor Execution
  └─ PropertySourcesPlaceholderConfigurer: Resolves @Value("${...}") placeholders
  └─ ConfigurationClassPostProcessor: Processes @Configuration, @Bean methods
  └─ Runs BEFORE any bean instances are created

Step 5: Singleton Instantiation (Topological Sort)
  └─ Dependencies instantiated before dependents (depth-first resolution)
  └─ Instances stored in DefaultListableBeanFactory.singletonObjects

Step 6: BeanPostProcessor Execution
  └─ AutowiredAnnotationBeanPostProcessor: Processes @Autowired fields (not constructor)
  └─ CommonAnnotationBeanPostProcessor: Processes @PostConstruct, @PreDestroy, @Resource
  └─ AnnotationAwareAspectJAutoProxyCreator: Wraps beans in CGLIB/JDK proxies for AOP

Step 7: Embedded Server Start
  └─ TomcatWebServer / NettyWebServer / JettyWebServer
  └─ Registers DispatcherServlet / DispatcherHandler
  └─ Publishes ApplicationReadyEvent
```

```java
// Production Spring Boot Entry Point
@SpringBootApplication
// ↑ Composite of:
//   @Configuration    — marks as bean definition source
//   @EnableAutoConfiguration — triggers auto-configuration (200+ classes)
//   @ComponentScan("com.example")  — scans this package recursively
public class OrderServiceApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(OrderServiceApplication.class, args);

        // At this point, the ApplicationContext is fully initialized:
        // ctx.getBean(OrderService.class) — retrieves singleton from cache
        // ctx.getBeanDefinitionNames() — lists all registered bean names
        // ctx.getEnvironment().getProperty("server.port") — reads config
    }
}

@Service
public class OrderService {
    // Spring resolves both parameters from the singletonObjects cache
    // and injects them via constructor (topological dependency resolution)
    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;

    // Single constructor — Spring 4.3+ does NOT need @Autowired here
    public OrderService(OrderRepository orderRepository, PaymentClient paymentClient) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.paymentClient = Objects.requireNonNull(paymentClient);
    }
}
```

*Code Walkthrough:*
1. **`@SpringBootApplication`** — The composite annotation triggers the full startup pipeline. Its `@ComponentScan` uses the annotated class's package as the base package.
2. **`DefaultListableBeanFactory.beanDefinitionMap`** — A `ConcurrentHashMap<String, BeanDefinition>` where the key is the bean name (usually lowercase class name) and the value holds all metadata.
3. **`singletonObjects`** — A `ConcurrentHashMap<String, Object>` that is the actual runtime singleton cache. After Step 5, every non-lazy singleton bean is in this map.
4. **Constructor injection** — Spring performs a topological sort of the dependency graph. `PaymentClient` is instantiated first (if it has no dependencies), then `OrderRepository`, then `OrderService`.

| ApplicationContext Type | Used When | Embedded Server |
|---|---|---|
| `AnnotationConfigServletWebServerApplicationContext` | Spring MVC / REST APIs | Tomcat / Jetty / Undertow |
| `AnnotationConfigReactiveWebServerApplicationContext` | Spring WebFlux | Netty |
| `AnnotationConfigApplicationContext` | Non-web (batch, CLI) | None |
| `GenericWebApplicationContext` | Testing (MockMvc) | MockMvc |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If `ApplicationContext` stores singletons in a `ConcurrentHashMap`, what happens when two `@Service` beans have a circular dependency — each requiring the other in their constructors?"
- **Winning Answer**: "With **constructor injection**, Spring throws `BeanCurrentlyInCreationException` at startup because it cannot instantiate either bean without the other being fully constructed first. The detection mechanism is a `singletonsCurrentlyInCreation` `Set<String>` in `DefaultSingletonBeanRegistry`. When Spring starts creating `BeanA`, it adds `beanA` to this set, then tries to create `BeanB` (its dependency), which adds `beanB`, then tries to create `BeanA` again — finds it already in the set — and throws. Spring 6 explicitly rejects all circular dependencies by default. In Spring 5, field/setter injection 'solved' this using an 'early object reference' (an incompletely-initialized proxy), but this hides architectural rot. The correct fix is an intermediary event, mediator, or shared utility bean."

---

#### Q2: Field Injection vs Constructor Injection — The Three Engineering Reasons

##### 1. Exact Scenario & Question
During a code review, a staff engineer rejects a PR that uses `@Autowired` on private fields throughout 15 service classes. The PR author argues: "Field injection is shorter and cleaner — why are we adding constructor boilerplate?" Explain the three fundamental engineering problems with field injection, why constructor injection is the production standard, and show the idiomatic Lombok solution that eliminates the boilerplate argument.

##### 2. What the Interviewer Evaluates
- Understanding that field injection uses `Field.setAccessible(true)` via reflection, bypassing Java's access control at runtime.
- Knowledge that field injection prevents `final` fields and immutability guarantees.
- Ability to demonstrate unit testing field-injected vs constructor-injected classes without Spring context.
- **Average vs. Elite**: Average says "field injection is bad practice." Elite explains that Lombok's `@RequiredArgsConstructor` generates constructor injection at compile-time via AST transformation, eliminating boilerplate entirely while preserving all three correctness properties.

##### 3. Standout Technical Answer

**Problem 1: No Immutability — Fields Cannot Be `final`**
```java
// ❌ FIELD INJECTION — NO immutability, NO fail-fast
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;  // Not final — can be null or reassigned

    @Autowired
    private PaymentClient paymentClient;      // Not final — hidden dependency

    public void processOrder(Order order) {
        orderRepository.save(order);  // NullPointerException if Spring forgot to inject!
        paymentClient.charge(order);  // No static guarantee this was ever set!
    }
}
```

```java
// ✅ CONSTRUCTOR INJECTION — Immutable, fail-fast, explicit
@Service
public class OrderService {
    private final OrderRepository orderRepository;   // FINAL — immutable after construction
    private final PaymentClient paymentClient;       // FINAL — cannot be null after construction

    public OrderService(OrderRepository orderRepository, PaymentClient paymentClient) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository required");
        this.paymentClient = Objects.requireNonNull(paymentClient, "paymentClient required");
        // If either is null → NullPointerException AT STARTUP, not at request time
    }
}
```

**Problem 2: Hidden Dependencies — Class Bloat Without Warning**
```java
// ❌ With field injection, you can silently accumulate 20 dependencies
@Service
public class GodOrderService {
    @Autowired private OrderRepository orderRepository;
    @Autowired private PaymentClient paymentClient;
    @Autowired private InventoryService inventoryService;
    @Autowired private NotificationService notificationService;
    @Autowired private AuditService auditService;
    @Autowired private TaxCalculator taxCalculator;
    @Autowired private ShippingCalculator shippingCalculator;
    @Autowired private FraudDetectionService fraudDetectionService;
    @Autowired private LoyaltyPointsService loyaltyPointsService;
    @Autowired private WarehouseAllocationService warehouseAllocationService;
    // No SRP warning! Field injection hides this monstrosity.
}

// ✅ With constructor injection, the 10-parameter constructor is IMMEDIATELY obvious:
public GodOrderService(OrderRepository r, PaymentClient p, InventoryService i,
                       NotificationService n, AuditService a, TaxCalculator t,
                       ShippingCalculator s, FraudDetectionService f,
                       LoyaltyPointsService l, WarehouseAllocationService w) {
    // Any senior who reads this constructor immediately says "Extract to smaller services!"
    // Constructor injection makes SRP violations VISIBLE.
}
```

**Problem 3: Unit Testing Requires Reflection or Spring Context**
```java
// ❌ Testing field-injected class without Spring:
class OrderServiceTest {
    @Test
    void testProcessOrder() {
        OrderService svc = new OrderService();
        // svc.orderRepository is NULL — Spring was never involved!
        // OPTION A: Use reflection (hacky, brittle)
        ReflectionTestUtils.setField(svc, "orderRepository", mock(OrderRepository.class));
        // OPTION B: Use @SpringBootTest (slow, 10+ second startup per test suite)
    }
}

// ✅ Testing constructor-injected class — pure JUnit, no Spring:
class OrderServiceTest {
    @Test
    void testProcessOrder() {
        OrderRepository mockRepo = mock(OrderRepository.class);
        PaymentClient mockPayment = mock(PaymentClient.class);

        // Pure constructor call — no Spring, no reflection, no setup!
        OrderService svc = new OrderService(mockRepo, mockPayment);

        Order order = new Order("ORD-001", BigDecimal.valueOf(99.99));
        svc.processOrder(order);

        verify(mockRepo).save(order);
        verify(mockPayment).charge(order);
    }
    // Executes in < 50ms (no Spring startup)
}
```

**The Lombok Solution (Eliminates ALL Boilerplate):**
```java
// ✅ PRODUCTION STANDARD — Zero boilerplate, full constructor injection
@Service
@RequiredArgsConstructor  // Lombok generates: public OrderService(final OrderRepository, final PaymentClient)
@Slf4j                    // Lombok generates: private static final Logger log = ...
public class OrderService {
    private final OrderRepository orderRepository;  // FINAL → included in @RequiredArgsConstructor
    private final PaymentClient paymentClient;       // FINAL → included

    public OrderResponse placeOrder(CreateOrderRequest request) {
        log.info("Placing order for SKU: {}", request.sku());
        Order saved = orderRepository.save(Order.from(request));
        paymentClient.charge(saved.getId(), request.amount());
        return OrderResponse.confirmed(saved.getId());
    }
}
// @RequiredArgsConstructor generates at compile time (AST transformation):
// public OrderService(OrderRepository orderRepository, PaymentClient paymentClient) {
//     this.orderRepository = orderRepository;
//     this.paymentClient = paymentClient;
// }
```

*Code Walkthrough:*
1. **`Objects.requireNonNull()`** — Fails immediately at construction time if Spring fails to wire a dependency, not at request time when a `NullPointerException` would be cryptic.
2. **`@RequiredArgsConstructor`** — Lombok's compile-time AST transformation. Processes `final` fields and non-`null` annotated fields. Generates a standard Java constructor — no reflection, no runtime overhead. The generated `.class` file contains a normal constructor.
3. **`@Slf4j`** — Generates `private static final Logger log = LoggerFactory.getLogger(OrderService.class)`. Eliminates logger boilerplate from every class.

| Injection Style | Fields Final | Testable w/o Spring | Detects Bloat | Reflection Used | Spring Recommended |
|---|---|---|---|---|---|
| Field (`@Autowired`) | ❌ No | ❌ No (null) | ❌ No | ✅ Yes (risky) | ❌ Deprecated guidance |
| Setter | ❌ No | ✅ Yes | ❌ No | ❌ No | ⚠️ Optional deps only |
| Constructor | ✅ Yes (`final`) | ✅ Yes | ✅ Yes (large ctor) | ❌ No | ✅ Always |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If constructor injection is always preferred, why does IntelliJ IDEA still offer 'Generate → Autowired field injection' as an IDE shortcut?"
- **Winning Answer**: "IDE shortcuts reflect historical patterns and user habits, not engineering best practices. IntelliJ also offers a **Quick Fix** that converts field injection to constructor injection. Furthermore, IntelliJ IDEA (since 2021.2) shows a warning: *'Field injection is not recommended'* on every `@Autowired` private field. Spring's own documentation since version 4.3 explicitly states: *'Always use constructor-based dependency injection in your beans.'* The field injection shortcut exists for legacy migration support and beginners — it is not an endorsement of the pattern."

---

#### Q3: Bean Scopes — Singleton vs Prototype & The Proxy Trap

##### 1. Exact Scenario & Question
Your e‑commerce checkout service uses a `@Service` (singleton) that depends on a `@Scope("prototype")` `ShoppingCart` bean. Customers report that their cart items are shared with other users. Explain exactly why this happens at the JVM object level, how Spring bean scopes work internally, and implement all three correct solutions with their trade-offs.

##### 2. What the Interviewer Evaluates
- Deep understanding of singleton scope (one instance per `ApplicationContext` == one Java object on the heap for the lifetime of the JVM process) vs prototype scope (new instance per injection point, NOT per method call).
- Knowledge that a prototype bean injected into a singleton is only created ONCE — at the singleton's construction time — and the same prototype instance is reused for the singleton's lifetime.
- Ability to solve with: (1) `ObjectProvider<T>`, (2) `@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)`, or (3) `ApplicationContext.getBean()`.

##### 3. Standout Technical Answer

**Why the Bug Occurs — JVM Object Level:**
```java
@Service  // Singleton: ONE instance created at startup, lives until JVM shutdown
public class CheckoutService {
    private final ShoppingCart cart;

    public CheckoutService(ShoppingCart cart) {
        // Spring creates CheckoutService ONCE at startup.
        // At this moment, Spring resolves the ShoppingCart dependency.
        // ShoppingCart @Scope("prototype") → Spring creates ONE new ShoppingCart
        // and stores the reference in this.cart
        // CheckoutService is never re-created, so this.cart NEVER changes!
        // All 10,000 concurrent users share the SAME ShoppingCart object!
        this.cart = cart;
    }

    public void addItem(Long userId, Item item) {
        cart.add(item);  // USER A adds "iPhone" → cart = ["iPhone"]
                         // USER B adds "MacBook" → cart = ["iPhone", "MacBook"]
                         // USER A sees MacBook! CATASTROPHIC BUG!
    }
}

@Component
@Scope("prototype")  // @Scope only means "new instance per injection" — NOT per method call!
public class ShoppingCart {
    private final List<Item> items = new ArrayList<>();
    public void add(Item item) { items.add(item); }
    public List<Item> getItems() { return Collections.unmodifiableList(items); }
}
```

**Fix 1: `ObjectProvider<T>` — Recommended for Production**
```java
@Service
public class CheckoutService {
    // ObjectProvider is a Spring factory — does NOT eagerly create the bean
    private final ObjectProvider<ShoppingCart> cartProvider;

    public CheckoutService(ObjectProvider<ShoppingCart> cartProvider) {
        this.cartProvider = cartProvider;
        // Note: ShoppingCart is NOT created here!
    }

    public void addItem(Long userId, Item item) {
        // getObject() creates a BRAND NEW ShoppingCart instance every call
        // Each user's request gets its own cart from the ObjectProvider factory
        ShoppingCart cart = cartProvider.getObject();
        cart.add(item);
        sessionService.storeCart(userId, cart);  // Store per-user in session/Redis
    }
}
```

**Fix 2: Scoped CGLIB Proxy**
```java
@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ShoppingCart {
    private final List<Item> items = new ArrayList<>();
    // Spring generates a CGLIB subclass proxy:
    // class ShoppingCart$$SpringCGLIB$$ extends ShoppingCart {
    //     @Override
    //     public void add(Item item) {
    //         getTargetPrototypeBean().add(item);  // New instance per call!
    //     }
    // }
}

@Service
public class CheckoutService {
    private final ShoppingCart cart;  // Holds the PROXY, not a real ShoppingCart

    public CheckoutService(ShoppingCart cart) {
        this.cart = cart;  // cart is the CGLIB proxy
    }

    public void addItem(Long userId, Item item) {
        // cart.add() → proxy intercepts → creates new ShoppingCart prototype → calls real add()
        cart.add(item);  // Each call to the proxy creates a fresh prototype instance
    }
}
```

**Fix 3: `@Lookup` Method Injection (Spring AOP Overrides Method)**
```java
@Service
public abstract class CheckoutService {  // MUST be abstract (or @Lookup on concrete class)

    @Lookup  // Spring overrides this method to return a new prototype instance each call
    public abstract ShoppingCart getCart();

    public void addItem(Long userId, Item item) {
        ShoppingCart cart = getCart();  // Returns NEW instance every call
        cart.add(item);
    }
}
```

| Scope | Instance Count | Spring Manages Lifecycle | @PreDestroy Called |
|---|---|---|---|
| `singleton` (default) | 1 per ApplicationContext | ✅ Full | ✅ Yes (on shutdown) |
| `prototype` | 1 per injection/getBean() | ⚠️ Creation only | ❌ Never |
| `request` | 1 per HTTP request | ✅ Full | ✅ Yes (request end) |
| `session` | 1 per HTTP session | ✅ Full | ✅ Yes (session expire) |
| `application` | 1 per ServletContext | ✅ Full | ✅ Yes |

| Fix | Mechanism | Pros | Cons |
|---|---|---|---|
| `ObjectProvider<T>` | Lazy factory, explicit `getObject()` | Clear intent, no proxy overhead | Must call `getObject()` explicitly |
| `ScopedProxyMode.TARGET_CLASS` | CGLIB proxy wraps bean | Transparent to caller | Proxy overhead, CGLIB subclass needed |
| `@Lookup` | Spring overrides method bytecode | Clean API | Class must be overridable (no `final`) |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Does Spring call `@PreDestroy` on prototype-scoped beans?"
- **Winning Answer**: "No. Spring does **not** manage the full lifecycle of prototype beans. After creating and injecting the instance, Spring relinquishes all control. `@PreDestroy`, `DisposableBean.destroy()`, and `destroyMethod` are **never** called by Spring for prototype beans. The bean is garbage-collected when no longer referenced by your code. If cleanup logic is needed, implement `DisposableBean` in the prototype class and invoke `destroy()` manually, or use a custom `DestructionAwareBeanPostProcessor` that tracks created prototypes and destroys them when the singleton that owns them is destroyed."

---

#### Q4: Auto‑Configuration — How Spring Boot Wires 200+ Libraries Without Any Code

##### 1. Exact Scenario & Question
You add `spring-boot-starter-data-jpa` to `pom.xml`, and without writing a single `@Configuration` class, Spring Boot automatically configures HikariCP, Hibernate 6, JPA `EntityManagerFactory`, and a `PlatformTransactionManager`. How does this "magic" work internally? Trace the exact auto-configuration resolution pipeline from `@EnableAutoConfiguration` to the moment `HikariDataSource` bean appears in the `ApplicationContext`.

##### 2. What the Interviewer Evaluates
- Knowledge of `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (Spring Boot 3) replacing the legacy `spring.factories`.
- Understanding of `@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty` evaluation order.
- Ability to use `--debug` flag, `ConditionEvaluationReport`, and Actuator `/actuator/conditions` to diagnose auto-configuration decisions.
- **Average vs. Elite**: Average says "Spring Boot auto-configures things." Elite traces the exact path: `AutoConfigurationImportSelector` → `SpringFactoriesLoader` → loads 150+ class names → `@Conditional` evaluation → `BeanDefinition` registration → `BeanPostProcessor` processing → singleton instantiation.

##### 3. Standout Technical Answer

**The Complete Auto-Configuration Pipeline:**
```
@SpringBootApplication
  └─ @EnableAutoConfiguration
      └─ @Import(AutoConfigurationImportSelector.class)
                │
                ▼ AutoConfigurationImportSelector.getAutoConfigurationEntry()
          Reads: META-INF/spring/
                 org.springframework.boot.autoconfigure.AutoConfiguration.imports
          Loads: ~150 class names (Spring Boot 3.x) including:
                 - DataSourceAutoConfiguration
                 - HibernateJpaAutoConfiguration
                 - TransactionAutoConfiguration
                 - SpringDataWebAutoConfiguration
                 - ...
                │
                ▼ Filter by @Conditional* annotations
          For DataSourceAutoConfiguration:
            @ConditionalOnClass(DataSource.class) → DataSource.class IS on classpath ✅
            @ConditionalOnMissingBean(DataSource.class) → No user-defined DataSource ✅
            → BeanDefinition for HikariDataSource REGISTERED
                │
                ▼ @EnableConfigurationProperties(DataSourceProperties.class)
          Binds spring.datasource.* → DataSourceProperties POJO
          spring.datasource.url → DataSourceProperties.url
          spring.datasource.hikari.maximum-pool-size → HikariCP config
                │
                ▼ Singleton instantiation
          HikariDataSource created with properties from YAML
          Stored in singletonObjects["dataSource"]
```

**The Auto-Configuration Source Code (Simplified):**
```java
// DataSourceAutoConfiguration.java (inside spring-boot-autoconfigure JAR)
@AutoConfiguration(before = SqlInitializationAutoConfiguration.class)
@ConditionalOnClass({ DataSource.class, EmbeddedDatabaseType.class })
@ConditionalOnMissingBean(type = "io.r2dbc.spi.ConnectionFactory")
@EnableConfigurationProperties(DataSourceProperties.class)
@Import({ DataSourcePoolMetadataProvidersConfiguration.class,
          DataSourceCheckpointRestoreConfiguration.class })
public class DataSourceAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @Conditional(PooledDataSourceCondition.class)
    @ConditionalOnMissingBean({ DataSource.class, XADataSource.class })
    @Import({ DataSourceConfiguration.Hikari.class,   // HikariCP (preferred)
              DataSourceConfiguration.Tomcat.class,    // Fallback
              DataSourceConfiguration.Dbcp2.class,     // Fallback
              DataSourceConfiguration.OracleUcp.class, // Fallback
              DataSourceConfiguration.Generic.class })
    protected static class PooledDataSourceConfiguration {
        // Each DataSourceConfiguration.Hikari checks @ConditionalOnClass(HikariDataSource.class)
        // and @ConditionalOnProperty(name = "spring.datasource.type",
        //                            havingValue = "com.zaxxer.hikari.HikariDataSource",
        //                            matchIfMissing = true)
    }
}
```

**Debugging Auto-Configuration Decisions:**
```bash
# Option 1: Start app with --debug flag
java -jar myapp.jar --debug
# Output shows:
# AUTO-CONFIGURATION REPORT
# Positive matches:
#   DataSourceAutoConfiguration matched:
#     - @ConditionalOnClass found required classes 'javax.sql.DataSource' (OnClassCondition)
# Negative matches:
#   R2dbcAutoConfiguration:
#     - @ConditionalOnClass did not find required class 'io.r2dbc.spi.ConnectionFactory'

# Option 2: Actuator endpoint (in production)
GET /actuator/conditions
# Returns JSON with all @Conditional decisions

# Option 3: Inject ConditionEvaluationReport
@Autowired
private ConditionEvaluationReport report;
```

**Overriding Auto-Configuration:**
```java
// Override HikariCP with a custom DataSource (e.g., for multi-tenancy)
@Configuration
public class CustomDataSourceConfig {
    @Bean  // @ConditionalOnMissingBean sees THIS → DataSourceAutoConfiguration backs off!
    public DataSource dataSource(DataSourceProperties properties) {
        // Build custom DataSource (e.g., ProxyDataSource for query logging)
        HikariDataSource hikari = properties.initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
        return ProxyDataSourceBuilder.create(hikari)
            .logQueryBySlf4j(SLF4JLogLevel.DEBUG)  // Log all SQL
            .build();
    }
}
```

```java
// Exclude specific auto-configurations entirely
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,    // No DB auto-config
    SecurityAutoConfiguration.class,      // No security auto-config
    JmxAutoConfiguration.class            // No JMX
})
public class MyApp { }
```

| Conditional Annotation | Evaluated Against | Example Use |
|---|---|---|
| `@ConditionalOnClass` | Classpath (class loading) | Activates if `DataSource.class` present |
| `@ConditionalOnMissingBean` | ApplicationContext bean registry | Back off if user defined own bean |
| `@ConditionalOnProperty` | Environment properties | `spring.cache.type=redis` → configure Redis |
| `@ConditionalOnWebApplication` | WebApplicationContext type | Only for servlet/reactive web apps |
| `@ConditionalOnMissingClass` | Classpath (class NOT present) | Fallback when optional lib absent |
| `@ConditionalOnResource` | Classpath resources | Config file present |
| `@ConditionalOnExpression` | SpEL expression | Complex conditional logic |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If two auto-configuration classes both define a `DataSource` bean with `@ConditionalOnMissingBean`, and they're loaded simultaneously — does the JVM's class loading order determine which one wins?"
- **Winning Answer**: "No. The winner is determined by explicit `@AutoConfiguration` ordering attributes, not JVM class loading. `@AutoConfiguration(before = ...)` and `@AutoConfiguration(after = ...)` create a directed graph. Spring Boot's `AutoConfigurationSorter` performs a topological sort of this graph. The class processed **first** in the sorted order that passes its `@Conditional` checks registers the bean. All subsequently processed classes encounter `@ConditionalOnMissingBean(DataSource.class)` as `true` — bean already exists — and their `@Bean` methods are skipped entirely. If ordering is ambiguous (no explicit `before`/`after` edges), auto-configurations are sorted alphabetically by class name — but always explicitly declare `@AutoConfiguration(before = ...)` for any ordering requirement."

---

#### Q5: `@Configuration` proxyBeanMethods — The CGLIB Proxy You Didn't Know About

##### 1. Exact Scenario & Question
You define two `@Bean` methods in a `@Configuration` class. The `jdbcTemplate()` method calls `dataSource()` directly to get the `DataSource`. In a regular Java class, this would create a new `HikariDataSource` every time. But Spring returns the same singleton. Explain why, what CGLIB proxying does to `@Configuration` classes, and when to disable it with `proxyBeanMethods = false` for faster startup.

##### 2. What the Interviewer Evaluates
- Understanding that `@Configuration` classes are CGLIB‑proxied by default to intercept inter-`@Bean` method calls and return the cached singleton.
- Knowledge that `proxyBeanMethods = false` ("Lite Mode") disables CGLIB proxying, making `@Bean` methods plain factory methods with no inter-bean call interception.
- Ability to explain GraalVM Native Image compatibility — CGLIB cannot generate subclasses at runtime in native images.

##### 3. Standout Technical Answer

**Full Mode (Default — `proxyBeanMethods = true`):**
```java
@Configuration  // Spring generates a CGLIB subclass of this class at startup
public class AppConfig {

    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://localhost/orders");
        ds.setMaximumPoolSize(20);
        return ds;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        // In plain Java: dataSource() would create a NEW HikariDataSource!
        // With Spring CGLIB proxy: dataSource() is INTERCEPTED and returns
        // the singleton from ApplicationContext.singletonObjects["dataSource"]
        return new JdbcTemplate(dataSource());  // Same DataSource instance!
    }

    @Bean
    public NamedParameterJdbcTemplate namedParamJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dataSource());  // Same singleton again!
    }
}
```

**The CGLIB Subclass Spring Generates (Conceptual):**
```java
// Spring generates this class at runtime using CGLIB (ByteBuddy in Spring 6)
// The actual generated class name: AppConfig$$SpringCGLIB$$0
public class AppConfig$$SpringCGLIB$$0 extends AppConfig {

    private static final ThreadLocal<Boolean> CGLIB_SET_THREAD_LOCAL = new ThreadLocal<>();
    private final BeanFactory beanFactory;

    @Override
    public DataSource dataSource() {
        // If called from outside (external caller) or from another @Bean method:
        if (beanFactory.containsSingleton("dataSource")) {
            // Return cached singleton — do NOT call super.dataSource()!
            return (DataSource) beanFactory.getSingleton("dataSource");
        }
        // First call: invoke the real method, register, and cache the result
        DataSource ds = super.dataSource();
        // beanFactory.registerSingleton("dataSource", ds);
        return ds;
    }

    @Override
    public JdbcTemplate jdbcTemplate() {
        // When this method calls dataSource(), the overridden dataSource() above
        // intercepts the call and returns the cached singleton → NOT a new instance!
        return super.jdbcTemplate();  // super.jdbcTemplate() calls this.dataSource() → intercepted!
    }
}
```

**Lite Mode (`proxyBeanMethods = false`) — For GraalVM and Faster Startup:**
```java
@Configuration(proxyBeanMethods = false)  // No CGLIB subclass generated!
public class AppConfig {

    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://localhost/orders");
        return ds;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        // ✅ CORRECT Lite Mode pattern: accept DataSource as parameter
        // Spring injects the singleton DataSource via the parameter
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParamJdbcTemplate(DataSource dataSource) {
        // ✅ Same pattern — accept via parameter, NOT direct method call
        return new NamedParameterJdbcTemplate(dataSource);
    }
}

// ❌ WRONG in Lite Mode: Calling dataSource() directly creates a NEW HikariDataSource!
@Bean
public JdbcTemplate jdbcTemplateBUGGY() {
    return new JdbcTemplate(dataSource());  // Creates a SECOND HikariDataSource!
    // jdbcTemplate and namedParamJdbcTemplate now use DIFFERENT connection pools!
    // This is an insidious silent bug — no exception thrown.
}
```

*Code Walkthrough:*
1. **Full Mode**: Every `@Bean` method call within the same `@Configuration` class is intercepted by the CGLIB proxy. The proxy checks the `singletonObjects` cache before calling the real method. Safe for inter-bean dependencies via method calls.
2. **Lite Mode**: No proxy generated. `@Bean` methods are plain factory methods. Direct inter-bean method calls create new instances (not singletons). Use parameter injection instead.
3. **Spring Boot 3 uses Lite Mode extensively** — all 150+ auto-configuration classes use `@AutoConfiguration` which implies `@Configuration(proxyBeanMethods = false)` for faster startup and GraalVM compatibility.

| Mode | CGLIB Generated | Inter-Bean Calls Safe | Startup Speed | GraalVM Native |
|---|---|---|---|---|
| Full (`true`, default) | ✅ Yes | ✅ Yes (proxy intercepts) | Slower (~5ms/config class) | ⚠️ Needs AOT processing |
| Lite (`false`) | ❌ No | ❌ No (new instance!) | Faster | ✅ Yes |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What happens at runtime if I call a `@Bean` method from within a `@Configuration(proxyBeanMethods = false)` class? Will Spring throw an exception?"
- **Winning Answer**: "No. Spring will **silently** create a new instance. There is no exception, no warning, no log message. The `@Bean` method is just a regular Java method in Lite Mode — calling it directly is identical to calling `new HikariDataSource()`. The result is a second `DataSource` object, completely separate from the one registered in the `ApplicationContext`. Any code using the `JdbcTemplate` that was built from this second `DataSource` operates on a completely different connection pool — causing connection pool exhaustion, transaction isolation issues, and query routing confusion. This silent failure mode is why Lite Mode requires discipline: always use parameter injection, never direct method calls."

---

#### Q6: Bean Lifecycle — The 12‑Step Initialization Sequence in Detail

##### 1. Exact Scenario & Question
You need to execute custom initialization logic after all dependencies are injected (preloading a regional pricing cache from the database), and cleanup logic before the application shuts down (flushing an in-memory write buffer to disk). You've seen `@PostConstruct`, `InitializingBean.afterPropertiesSet()`, and `@Bean(initMethod = "init")`. Explain the exact firing order of all lifecycle callbacks, what happens at each step, and which approach is preferred for production code.

##### 2. What the Interviewer Evaluates
- Precise ordering of all 12 lifecycle callbacks — `@PostConstruct` fires at Step 6, BEFORE `afterPropertiesSet()` at Step 7.
- Understanding that AOP proxies are created at Step 9, AFTER `@PostConstruct` — meaning `@PostConstruct` runs on the raw bean, not the proxy.
- Knowledge that `@PreDestroy` fires during graceful shutdown triggered by JVM shutdown hook but NOT during `kill -9` (SIGKILL).

##### 3. Standout Technical Answer

```java
@Service
@Slf4j
public class PricingCacheService implements InitializingBean, DisposableBean,
        BeanNameAware, BeanFactoryAware, ApplicationContextAware {

    private final ProductRepository productRepo;
    private Map<String, BigDecimal> priceCache;
    private String beanName;

    // STEP 1-2: Constructor (Instantiation + Property Population)
    public PricingCacheService(ProductRepository productRepo) {
        this.productRepo = productRepo;
        log.info("[STEP 1-2] Constructor called, productRepo injected: {}", productRepo != null);
    }

    // STEP 3: BeanNameAware (optional — rarely used)
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        log.info("[STEP 3] BeanNameAware.setBeanName() called: {}", name);
    }

    // STEP 4: BeanFactoryAware (optional — rarely used; prefer ApplicationContextAware)
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        log.info("[STEP 4] BeanFactoryAware.setBeanFactory() called");
    }

    // STEP 5: ApplicationContextAware (optional — for dynamic bean lookups)
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        log.info("[STEP 5] ApplicationContextAware.setApplicationContext() called");
    }

    // STEP 6: @PostConstruct (via BeanPostProcessor.postProcessBeforeInitialization)
    // ⚠️ RUNS ON RAW BEAN — AOP proxy NOT yet created! Calling @Transactional methods here = NO TRANSACTION!
    @PostConstruct
    public void init() {
        log.info("[STEP 6] @PostConstruct — loading pricing cache");
        // productRepo is injected ✅, but 'this' is the raw bean (not the proxy)
        // If this method calls a @Transactional method on 'this' → NO transaction active!
        priceCache = productRepo.findAll()
            .stream()
            .collect(Collectors.toConcurrentMap(
                Product::getSku,
                Product::getBasePrice
            ));
        log.info("[STEP 6] Loaded {} prices into cache", priceCache.size());
    }

    // STEP 7: InitializingBean.afterPropertiesSet() (Spring-specific, fires AFTER @PostConstruct)
    @Override
    public void afterPropertiesSet() {
        log.info("[STEP 7] afterPropertiesSet() — validating cache integrity");
        if (priceCache == null || priceCache.isEmpty()) {
            throw new IllegalStateException("Price cache is empty — cannot start!");
            // Throwing here aborts application startup — use for critical validation
        }
    }

    // (STEP 8: custom @Bean(initMethod) would fire here if declared)
    // (STEP 9: BeanPostProcessor.postProcessAfterInitialization → AOP proxy CREATED)
    // (STEP 10: Bean is READY — stored in singletonObjects, fully wrapped in proxy)

    // STEP 11: @PreDestroy (fires during graceful shutdown — JVM shutdown hook)
    // ⚠️ Does NOT fire on kill -9 (SIGKILL)
    @PreDestroy
    public void onDestroy() {
        log.info("[STEP 11] @PreDestroy — flushing write buffer to disk");
        flushPendingWritesToDisk();
        log.info("[STEP 11] Flush complete");
    }

    // STEP 12: DisposableBean.destroy() (fires AFTER @PreDestroy)
    @Override
    public void destroy() {
        log.info("[STEP 12] DisposableBean.destroy() — releasing resources");
        if (priceCache != null) {
            priceCache.clear();
        }
    }

    public BigDecimal getPrice(String sku) {
        return priceCache.getOrDefault(sku, BigDecimal.ZERO);
    }
}
```

**Critical: `@PostConstruct` Runs on Raw Bean (Before AOP Proxy):**
```java
@Service
public class OrderService {

    @PostConstruct
    public void warmUpCache() {
        // ❌ BUG: This method calls a @Transactional method on 'this'
        // At Step 6, the AOP proxy wrapping @Transactional has NOT been created yet!
        // 'this' refers to the raw OrderService object, bypassing the proxy!
        loadInitialData();  // @Transactional on loadInitialData() has NO EFFECT here!
    }

    @Transactional  // This proxy is created at Step 9 — AFTER @PostConstruct!
    public void loadInitialData() {
        orderRepo.save(new Order("SEED-001"));
        // NO TRANSACTION — writes directly to DB without transaction context!
    }
}

// ✅ FIX: Use ApplicationRunner or SmartInitializingSingleton (fires AFTER all Step 9s)
@Component
public class DataInitializer implements ApplicationRunner {
    private final OrderService orderService;  // Injected as the PROXY

    @Override
    public void run(ApplicationArguments args) {
        // ApplicationRunner fires AFTER all beans are fully initialized including AOP proxies
        orderService.loadInitialData();  // ✅ @Transactional proxy IS active now!
    }
}
```

| Callback | Fires At | On Raw/Proxy Bean | Spring API Coupling | Recommended |
|---|---|---|---|---|
| `@PostConstruct` | Step 6 | Raw (no proxy yet) | No (JSR-250) | ✅ Yes (for simple init) |
| `InitializingBean.afterPropertiesSet()` | Step 7 | Raw | Yes (Spring) | ⚠️ When ordering after @PostConstruct matters |
| `@Bean(initMethod = "init")` | Step 8 | Raw | No (declaration) | ✅ For third-party beans |
| `ApplicationRunner.run()` | After all Step 9s | Proxy | No (Spring Boot) | ✅ When @Transactional init needed |
| `@PreDestroy` | Step 11 | Proxy | No (JSR-250) | ✅ Yes |
| `DisposableBean.destroy()` | Step 12 | Proxy | Yes (Spring) | ⚠️ Rarely needed |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "During graceful shutdown, in what order are beans destroyed? Is it the reverse of initialization order?"
- **Winning Answer**: "Yes. Spring destroys beans in **reverse dependency order** — the exact reverse of initialization order. If `OrderService` depended on `OrderRepository`, then `OrderService` is destroyed first (Step 11/12), then `OrderRepository`. This ensures that during shutdown, no bean's `@PreDestroy` tries to use a dependency that has already been destroyed. This is implemented via the `SingletonBeanRegistry` maintaining an ordered list of registered singletons (`registeredSingletons` — a `LinkedHashSet`). Shutdown iterates this list in reverse. You can also implement `SmartLifecycle` with a `getPhase()` integer to control the shutdown order precisely."

---

#### Q7: `@Transactional` Proxy Mechanics — The Self‑Invocation Trap in Production

##### 1. Exact Scenario & Question
A critical payment processing bug reaches production: a `@Transactional` method is being called from within the same class, and the transaction never starts. The database write succeeds even when a `FraudException` is thrown — money is debited but the fraud alert is not raised atomically. Explain the exact mechanism behind this bug, why Spring's AOP cannot intercept internal calls, and all three production-grade solutions ranked by recommendation.

##### 2. What the Interviewer Evaluates
- Deep understanding that `@Transactional` is enforced by a CGLIB/JDK proxy wrapping the bean — NOT by modifying the class bytecode.
- Knowledge that internal calls (`this.method()`) bypass the proxy entirely because `this` refers to the raw object, not the proxy reference.
- Ability to explain all three fixes: extract to separate bean (best), self-injection with `@Lazy` (quick fix), and `AopContext.currentProxy()` (last resort).

##### 3. Standout Technical Answer

**The Bug in Production:**
```java
@Service
public class PaymentService {

    @Autowired
    private AccountRepository accountRepo;

    // This method is called from an external REST controller
    // External call goes through the CGLIB proxy → proxy starts a transaction ✅
    public void processPayment(PaymentRequest request) {
        // STEP 1: Debit the account
        accountRepo.debit(request.getAccountId(), request.getAmount());

        // STEP 2: ❌ SELF-INVOCATION — calls chargeAndAlert() directly on 'this'
        // 'this' = the raw PaymentService object (NOT the proxy!)
        // The transaction proxy interceptor is NEVER invoked!
        this.chargeAndAlert(request);
        // Equivalent to calling a regular method — no @Transactional effect!
    }

    @Transactional  // ← Completely IGNORED on self-invocation!
    public void chargeAndAlert(PaymentRequest request) {
        paymentGateway.charge(request.getAmount());
        fraudAlertService.raiseAlert(request.getAccountId());
        // NO TRANSACTION IS ACTIVE!
        // If fraudAlertService.raiseAlert() throws RuntimeException:
        //   - paymentGateway.charge() is NOT rolled back (already committed to external system)
        //   - accountRepo.debit() from the outer call is NOT rolled back
        // RESULT: Money is debited, charge is made, fraud alert NOT raised → data inconsistency!
    }
}
```

**Proxy Architecture Visualization:**
```
EXTERNAL CALL (from REST controller):

  External Caller
      │
      ▼
  [CGLIB Proxy: PaymentService$$SpringCGLIB$$0]
      │  Intercepts: "Oh, this method is @Transactional!"
      │  1. Acquire connection from HikariCP
      │  2. Set autocommit = false
      │  3. Store connection in TransactionSynchronizationManager (ThreadLocal)
      ▼
  [Raw PaymentService object]
      │  processPayment() executes
      │  calls this.chargeAndAlert()
      │  ↓
      │  [Raw PaymentService object] — PROXY IS BYPASSED!
      │  chargeAndAlert() executes WITHOUT transaction context
      ▼
  [CGLIB Proxy: PaymentService$$SpringCGLIB$$0]
      │  4. commit() or rollback()
      ▼
  External Caller receives result


SELF-INVOCATION:

  External Caller → [Proxy] → Raw.processPayment()
                                    │
                                    └─► Raw.chargeAndAlert()  ← Bypasses proxy entirely!
                                           (no transaction!)
```

**Fix 1: Extract to a Separate Bean (✅ Recommended)**
```java
@Service
public class PaymentService {
    private final PaymentTransactionService txnService;  // INJECTED AS PROXY
    private final AccountRepository accountRepo;

    public PaymentService(PaymentTransactionService txnService, AccountRepository accountRepo) {
        this.txnService = txnService;
        this.accountRepo = accountRepo;
    }

    public void processPayment(PaymentRequest request) {
        accountRepo.debit(request.getAccountId(), request.getAmount());
        // Call via injected reference → goes through the PROXY ✅
        txnService.chargeAndAlert(request);  // ← txnService is the CGLIB proxy
    }
}

@Service
public class PaymentTransactionService {
    @Transactional  // ← Now works because called externally (through proxy)!
    public void chargeAndAlert(PaymentRequest request) {
        paymentGateway.charge(request.getAmount());
        fraudAlertService.raiseAlert(request.getAccountId());
        // ✅ Transaction IS active — FraudException → full rollback!
    }
}
```

**Fix 2: Self-Injection via `@Lazy` (Quick Fix)**
```java
@Service
public class PaymentService {

    @Lazy @Autowired   // @Lazy prevents circular dependency; injects the PROXY reference
    private PaymentService self;  // This field holds the CGLIB proxy, not 'this'

    public void processPayment(PaymentRequest request) {
        accountRepo.debit(request.getAccountId(), request.getAmount());
        // Call through the proxy reference → @Transactional intercepted ✅
        self.chargeAndAlert(request);
    }

    @Transactional
    public void chargeAndAlert(PaymentRequest request) {
        paymentGateway.charge(request.getAmount());
        fraudAlertService.raiseAlert(request.getAccountId());
        // ✅ Transaction IS active via self.chargeAndAlert() call
    }
}
```

**Fix 3: `AopContext.currentProxy()` (Last Resort — Fragile)**
```java
// Requires: @EnableAspectJAutoProxy(exposeProxy = true)
@Service
public class PaymentService {

    public void processPayment(PaymentRequest request) {
        accountRepo.debit(request.getAccountId(), request.getAmount());
        // Retrieve the current proxy from a ThreadLocal
        PaymentService proxy = (PaymentService) AopContext.currentProxy();
        proxy.chargeAndAlert(request);  // ✅ Goes through proxy
        // ⚠️ Fragile: throws IllegalStateException if exposeProxy not configured
        // ⚠️ Fragile: ThreadLocal overhead, hidden coupling
    }
}
```

**The Self-Invocation Problem Applies to ALL Spring AOP Annotations:**
```
@Transactional  → internal calls: no transaction management
@Cacheable      → internal calls: cache never checked/updated
@Async          → internal calls: runs synchronously on caller thread
@Retryable      → internal calls: no retry on exception
@Secured        → internal calls: no security check
@PreAuthorize   → internal calls: authorization bypassed (SECURITY HOLE!)
```

| Fix | Mechanism | Code Change | Risk |
|---|---|---|---|
| Extract to separate bean | External call via DI proxy | Add new class | Clean, testable, zero risk |
| Self-injection (`@Lazy`) | Inject proxy reference into self | 2 lines | Circular dep if no @Lazy |
| `AopContext.currentProxy()` | ThreadLocal proxy access | 1 line + @EnableAspectJAutoProxy | Fragile, hidden coupling |
| AspectJ compile-time weaving | Bytecode weaving, no proxy | Requires AspectJ plugin | Handles self-invocation, complex setup |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "You extract `chargeAndAlert()` to a separate `PaymentTransactionService` and annotate it with `@Transactional`. But the outer `processPayment()` in `PaymentService` is also `@Transactional`. What transaction does `chargeAndAlert()` join, and what happens if it throws?"
- **Winning Answer**: "With default propagation `REQUIRED`, `chargeAndAlert()` **joins the existing transaction** started by `processPayment()`. There is only ONE transaction, ONE database connection, and ONE commit/rollback for the entire chain. If `fraudAlertService.raiseAlert()` inside `chargeAndAlert()` throws a `RuntimeException`, the entire transaction — including `accountRepo.debit()` from `processPayment()` — is marked for rollback. This is the correct atomic behavior: either the full payment operation succeeds (debit + charge + alert) or the entire operation rolls back. To achieve different transactional semantics (e.g., commit the debit regardless of alert failure), use `REQUIRES_NEW` propagation on `chargeAndAlert()` — but be aware this requires a **second database connection** from HikariCP."

---

#### Q8: Transaction Propagation — All 7 Types with Database-Level Mechanics

##### 1. Exact Scenario & Question
Your payment processing system has a main `@Transactional` method that saves an order and calls an audit logging service. Business requirement: if the audit log fails, the order should STILL be committed. But if the order save fails, the audit log should NOT be committed either. Then a separate inventory reservation must use a `SAVEPOINT` so it can be rolled back independently if stock is insufficient, without affecting the order commit. Design the propagation strategy for each service method and explain the database-level mechanism behind each.

##### 2. What the Interviewer Evaluates
- Precise understanding of all 7 propagation types and the database operations behind each.
- Knowledge that `REQUIRES_NEW` suspends the outer transaction, acquires a **new separate database connection** from HikariCP, and starts an independent transaction — risk of connection pool starvation.
- Understanding of `NESTED` using JDBC `Savepoint` — partial rollback without affecting outer transaction.

##### 3. Standout Technical Answer

**All 7 Transaction Propagation Types:**

```java
// SCENARIO: OrderFacade calls OrderService (REQUIRED), AuditService (REQUIRES_NEW),
//           InventoryService (NESTED)
@Service
public class OrderFacade {

    @Transactional(propagation = Propagation.REQUIRED)  // DEFAULT
    public void placeOrder(OrderRequest request) {
        // Opens Transaction T1 using Connection C1 from HikariCP
        // DB: BEGIN;

        orderService.save(request);       // Runs inside T1 (same C1, same transaction)

        try {
            auditService.log(request);    // REQUIRES_NEW: suspends T1, opens T2 on C2
        } catch (AuditException e) {
            // T2 rolled back, but T1 is UNAFFECTED — order still commits!
            log.warn("Audit failed, order continues: {}", e.getMessage());
        }

        inventoryService.reserve(request.getProductId(), request.getQuantity());
        // NESTED: Creates SAVEPOINT sp1 inside T1 on C1
        // If reserve() fails: ROLLBACK TO SAVEPOINT sp1 (order unaffected)
        // If reserve() succeeds: RELEASE SAVEPOINT sp1

        // DB: COMMIT; (T1 commits — order is saved permanently)
    }
}

@Service
public class AuditService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(OrderRequest request) {
        // Spring suspends T1 (saves T1's connection C1 in ThreadLocal)
        // Acquires SECOND connection C2 from HikariCP
        // DB on C2: BEGIN;
        auditRepo.save(new AuditEntry(request));
        // DB on C2: COMMIT; (or ROLLBACK if exception)
        // Spring resumes T1, restores C1 to ThreadLocal
    }
}

@Service
public class InventoryService {
    @Transactional(propagation = Propagation.NESTED)
    public void reserve(Long productId, int quantity) {
        // DOES NOT open a new transaction! Runs INSIDE T1 on the SAME connection C1
        // DB on C1: SAVEPOINT sp1;
        int affected = inventoryRepo.decrementStock(productId, quantity);
        if (affected == 0) {
            // Stock insufficient — throw exception
            throw new InsufficientStockException("Product " + productId + " out of stock");
            // Spring catches this, executes: ROLLBACK TO SAVEPOINT sp1;
            // T1's other changes (orderService.save) are UNAFFECTED
        }
        // DB on C1: RELEASE SAVEPOINT sp1; (if no exception)
    }
}
```

**All 7 Propagation Types — Database-Level Mechanics:**

```java
// 1. REQUIRED (default) — Join existing or create new transaction
@Transactional(propagation = Propagation.REQUIRED)
// If T1 exists: runs inside T1, same connection, same commit/rollback
// If no T1: opens new transaction (BEGIN)
// Use: Default for all service methods

// 2. REQUIRES_NEW — Always create new independent transaction
@Transactional(propagation = Propagation.REQUIRES_NEW)
// Suspends T1 (ThreadLocal stash), acquires NEW connection from HikariCP
// DB: BEGIN; on new connection
// After method: COMMIT or ROLLBACK on new connection; resume T1
// ⚠️ RISK: Two connections held simultaneously — pool exhaustion at scale!
// Use: Audit logs, notifications that must commit regardless of outer tx

// 3. NESTED — Create savepoint inside existing transaction
@Transactional(propagation = Propagation.NESTED)
// Reuses SAME connection as outer transaction
// DB: SAVEPOINT sp_nested_1;
// On exception: ROLLBACK TO SAVEPOINT sp_nested_1;
// On success: RELEASE SAVEPOINT sp_nested_1;
// Requires JDBC driver that supports Savepoints (PostgreSQL, MySQL InnoDB)
// Use: Partial rollback within a larger transaction

// 4. SUPPORTS — Join if exists, non-transactional if not
@Transactional(propagation = Propagation.SUPPORTS)
// If T1 exists: runs transactionally
// If no T1: runs without any transaction (no BEGIN, no COMMIT)
// Use: Read methods that work with or without a surrounding transaction

// 5. NOT_SUPPORTED — Always run non-transactionally, suspend if exists
@Transactional(propagation = Propagation.NOT_SUPPORTED)
// Suspends T1, runs the method without any transaction
// Releases the connection temporarily
// Use: Long-running non-transactional work (PDF generation, email sending)

// 6. MANDATORY — Must have existing transaction; throw if none
@Transactional(propagation = Propagation.MANDATORY)
// If T1 exists: joins T1 normally
// If no T1: throws IllegalTransactionStateException immediately
// Use: Enforce that callers always provide a transaction context

// 7. NEVER — Must NOT have a transaction; throw if one exists
@Transactional(propagation = Propagation.NEVER)
// If T1 exists: throws IllegalTransactionStateException
// If no T1: runs without transaction
// Use: Validation that no transaction is accidentally active (rare)
```

| Propagation | Existing Tx | No Existing Tx | Connections Used | DB Mechanism |
|---|---|---|---|---|
| `REQUIRED` | Joins existing | Creates new | 1 | `BEGIN` if new |
| `REQUIRES_NEW` | Suspends, creates new | Creates new | 2 | `BEGIN` on new conn |
| `NESTED` | Creates savepoint | Creates new | 1 | `SAVEPOINT sp1` |
| `SUPPORTS` | Joins existing | None | 0 or 1 | None if no outer |
| `NOT_SUPPORTED` | Suspends existing | None | 0 | None |
| `MANDATORY` | Joins existing | Exception | 1 | N/A |
| `NEVER` | Exception | None | 0 | None |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If `REQUIRES_NEW` acquires a NEW database connection while T1 still holds its connection, what happens when HikariCP's pool is at maximum capacity?"
- **Winning Answer**: "Deadlock. Classic **connection pool starvation deadlock**. Scenario: 20 concurrent requests each start T1 (holding connection C1). Each T1 calls a `REQUIRES_NEW` method that needs connection C2. HikariCP maximum pool size = 20. All 20 connections are held by T1. The 20 requests waiting for C2 block for `connectionTimeout` ms (default 30s). After 30s, `SQLTransientConnectionException: Connection is not available, request timed out after 30000ms` — cascading failures. Prevention: (1) Set `maximum-pool-size` ≥ `max_concurrent_threads × 2` when `REQUIRES_NEW` is used in the hot path. (2) Prefer `NESTED` (savepoints, same connection) over `REQUIRES_NEW` where partial rollback is the goal. (3) Monitor `hikaricp_connections_pending` in Prometheus and alert on any sustained pending queue."

---

#### Q9: Spring AOP Architecture — CGLIB vs JDK Dynamic Proxy

##### 1. Exact Scenario & Question
Your team has two beans: one implements an interface (`UserService implements IUserService`) and one is a plain class (`ReportService`). Both are annotated with `@Transactional`. Explain which proxy mechanism Spring selects for each, the exact conditions that determine the choice, the performance characteristics of each, and when to force one mechanism over the other.

##### 2. What the Interviewer Evaluates
- Understanding that JDK Dynamic Proxy requires the bean to implement at least one interface; proxies the interface, not the class.
- Knowledge that CGLIB generates a subclass at runtime; works with concrete classes but cannot proxy `final` classes or methods.
- Ability to explain Spring Boot 2.0+'s decision to default to CGLIB for all beans regardless of interface presence.

##### 3. Standout Technical Answer

**JDK Dynamic Proxy — Interface-Based:**
```java
// JDK Dynamic Proxy is created when:
// 1. The bean implements at least one interface
// 2. @EnableAspectJAutoProxy(proxyTargetClass = false) is set (non-default in Spring Boot)

public interface IUserService {
    User findById(Long id);
    void updateUser(User user);
}

@Service
public class UserServiceImpl implements IUserService {

    @Override
    public User findById(Long id) { return userRepo.findById(id).orElseThrow(); }

    @Override
    @Transactional
    public void updateUser(User user) { userRepo.save(user); }
}

// Spring generates at runtime (using java.lang.reflect.Proxy):
// Object proxy = Proxy.newProxyInstance(
//     ClassLoader,
//     new Class[]{IUserService.class},  // Proxy implements the INTERFACE only
//     new TransactionInvocationHandler(target)
// );

// ⚠️ LIMITATION: Injecting UserServiceImpl directly FAILS!
@Autowired
private UserServiceImpl userService;  // ❌ NoSuchBeanDefinitionException!
// The bean type is IUserService (proxy type), NOT UserServiceImpl!

@Autowired
private IUserService userService;  // ✅ Works — proxy implements IUserService
```

**CGLIB Proxy — Subclass-Based:**
```java
// CGLIB proxy is created when:
// 1. Bean does NOT implement any interface, OR
// 2. @EnableAspectJAutoProxy(proxyTargetClass = true) [Spring Boot default!]

@Service  // No interface!
public class ReportService {

    @Transactional
    public Report generateReport(ReportRequest request) {
        return reportRepo.build(request);
    }

    // ⚠️ CGLIB LIMITATION: final methods CANNOT be proxied!
    public final Report cachedReport() {  // CGLIB cannot override final methods!
        return cache.get("latest");       // @Transactional on this would be IGNORED!
    }
}

// CGLIB generates at runtime (using ByteBuddy in Spring 6):
// public class ReportService$$SpringCGLIB$$0 extends ReportService {
//
//     @Override  // Can only override non-final methods!
//     public Report generateReport(ReportRequest request) {
//         // 1. TransactionInterceptor.before() — open transaction
//         // 2. super.generateReport(request) — call real method
//         // 3. TransactionInterceptor.after() — commit/rollback
//     }
//
//     // cachedReport() is final — CGLIB copies it verbatim, NO interception!
// }

// ✅ ADVANTAGE: Can inject by concrete class type
@Autowired
private ReportService reportService;  // ✅ Works even with CGLIB proxy
```

**Spring Boot Default: CGLIB for Everything**
```java
// Spring Boot 2.0+ sets proxyTargetClass = true by default in:
// @EnableAspectJAutoProxy(proxyTargetClass = true)
// This means CGLIB is used for ALL beans — even those with interfaces

// Why? Historical reason: Users were confused that field injection by concrete class type
// failed when JDK proxy was used (proxy is the interface type, not the class type)
// Solution: Always use CGLIB → inject by any type always works

@SpringBootApplication
// Implicitly: @EnableAutoConfiguration → AopAutoConfiguration →
//   @EnableAspectJAutoProxy(proxyTargetClass = true)  ← CGLIB for ALL
public class Application { }
```

**Forcing JDK Proxy (Rare — Security or Performance Reason):**
```java
// Revert to JDK proxy for interface-implementing beans:
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = false)  // JDK proxy when possible
public class AopConfig { }

// Or per-bean:
@Scope(proxyMode = ScopedProxyMode.INTERFACES)  // JDK proxy for this bean's scope proxy
```

| Aspect | JDK Dynamic Proxy | CGLIB Subclass |
|---|---|---|
| Requires interface | ✅ Yes | ❌ No |
| Handles concrete classes | ❌ No | ✅ Yes |
| `final` class support | N/A | ❌ Cannot subclass |
| `final` method support | N/A | ❌ Cannot override |
| Proxy creation speed | Faster | Slightly slower |
| Method invocation speed | Slightly slower (reflection) | Faster (bytecode) |
| Spring Boot default | No | ✅ Yes (proxyTargetClass=true) |
| GraalVM native | ⚠️ Needs reflection config | ⚠️ Needs AOT subclass gen |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "You have a `@Service` class marked as `final` class. Will Spring's `@Transactional` work on it?"
- **Winning Answer**: "No, and it will fail **silently at runtime**, not at startup. Spring Boot defaults to CGLIB which must **subclass** the target class. A `final` class cannot be subclassed — `java.lang.VerifyError: Cannot override final method` or the CGLIB subclass generation fails. Spring will either skip proxy creation (making `@Transactional` invisible) or throw `BeanCreationException: Cannot subclass final class`. The fix: remove `final` from the class declaration, or switch to JDK Dynamic Proxy by implementing an interface and setting `proxyTargetClass = false`. In Spring 6 / Spring Boot 3, this may cause a `BeanCreationException` at startup with a clearer message."

---

#### Q10: `@Async` Configuration — Dedicated Thread Pools & Exception Handling

##### 1. Exact Scenario & Question
Your notification service sends email, SMS, and push notifications asynchronously using `@Async`. Under load, you discover all three notification types compete for the same 8-thread pool, causing email notifications to block SMS. Additionally, when `@Async` methods throw exceptions, they silently disappear. Configure separate thread pools for each notification channel and implement proper exception propagation.

##### 2. What the Interviewer Evaluates
- Knowledge that Spring Boot auto-configures a `ThreadPoolTaskExecutor` with 8 threads by default for `@Async`.
- Understanding that `@Async` methods returning `void` swallow exceptions unless `AsyncUncaughtExceptionHandler` is configured.
- Ability to configure `@Async` with named executor qualifiers for channel-specific thread pools.

##### 3. Standout Technical Answer

```java
@Configuration
@EnableAsync  // Enables @Async processing — registers AsyncAnnotationBeanPostProcessor
public class AsyncConfig implements AsyncConfigurer {

    // Separate thread pools per notification channel — prevent channel interference
    @Bean("emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);           // Minimum threads always active
        executor.setMaxPoolSize(30);            // Maximum threads under load
        executor.setQueueCapacity(500);         // Queue before creating new threads
        executor.setThreadNamePrefix("email-");  // Thread name: email-1, email-2, ...
        executor.setKeepAliveSeconds(60);       // Idle thread lifetime
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // CallerRunsPolicy: if pool + queue full → runs on calling thread (backpressure!)
        executor.initialize();
        return executor;
    }

    @Bean("smsExecutor")
    public Executor smsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("sms-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        // DiscardOldestPolicy: drop oldest queued task to make room for new one
        executor.initialize();
        return executor;
    }

    @Bean("pushExecutor")
    public Executor pushExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);    // High-concurrency for mobile push
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("push-");
        executor.initialize();
        return executor;
    }

    // Default executor (used when @Async has no qualifier)
    @Override
    public Executor getAsyncExecutor() {
        return emailExecutor();  // Fallback to email executor
    }

    // ✅ Handle exceptions from @Async void methods (otherwise silently swallowed!)
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("Async exception in {}.{}() with params {}: {}",
                method.getDeclaringClass().getSimpleName(),
                method.getName(),
                Arrays.toString(params),
                ex.getMessage(),
                ex);
            alertingService.sendAlert("Async failure: " + method.getName() + ": " + ex.getMessage());
            // Could also publish to a dead-letter queue here
        };
    }
}

@Service
public class NotificationService {

    // ✅ Channel-specific executor — email won't block SMS!
    @Async("emailExecutor")
    public CompletableFuture<Void> sendEmail(String userId, String subject, String body) {
        // Runs on email-1, email-2, ... threads (NOT the calling thread)
        emailGateway.send(userId, subject, body);
        // ✅ Returning CompletableFuture: exceptions ARE propagated to caller via future.get()
        return CompletableFuture.completedFuture(null);
    }

    @Async("smsExecutor")
    public CompletableFuture<Boolean> sendSMS(String phone, String message) {
        boolean delivered = smsGateway.send(phone, message);
        return CompletableFuture.completedFuture(delivered);
        // Caller: CompletableFuture<Boolean> result = sendSMS(phone, msg);
        //         result.thenAccept(delivered -> log.info("SMS delivered: {}", delivered));
    }

    @Async("pushExecutor")
    public void sendPushNotification(String deviceToken, PushPayload payload) {
        // void return → exceptions handled by AsyncUncaughtExceptionHandler!
        pushGateway.send(deviceToken, payload);
    }

    // ✅ Fan-out: trigger all channels and wait for all to complete
    public void sendAllNotifications(String userId, NotificationRequest request) {
        CompletableFuture<Void> emailFuture = sendEmail(userId, request.subject(), request.body());
        CompletableFuture<Boolean> smsFuture = sendSMS(request.phone(), request.smsText());
        // Push is fire-and-forget (void)
        sendPushNotification(request.deviceToken(), request.pushPayload());

        // Wait for email and SMS (with timeout)
        try {
            CompletableFuture.allOf(emailFuture, smsFuture).get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("Notifications timed out for user {}", userId);
        }
    }
}
```

**Thread Pool Sizing Formula:**
```
For I/O-bound async tasks (HTTP calls, email, SMS):
  corePoolSize = number of concurrent notification streams per pod
  maxPoolSize  = corePoolSize × peak_multiplier (e.g., 3×)
  queueCapacity = maxPoolSize × avg_processing_time_ms / SLA_ms

For CPU-bound async tasks (PDF generation, image processing):
  corePoolSize = Runtime.getRuntime().availableProcessors()
  maxPoolSize  = corePoolSize (adding threads hurts CPU-bound work)
  queueCapacity = large (tasks wait for threads instead of creating more)
```

| Pool Saturation Policy | Behavior | Use When |
|---|---|---|
| `AbortPolicy` (default) | Throws `RejectedExecutionException` | Never silently lose tasks |
| `CallerRunsPolicy` | Caller thread runs the task | Backpressure — slow producer |
| `DiscardPolicy` | Silently discards new task | Best-effort delivery OK |
| `DiscardOldestPolicy` | Discards oldest queued task | Latest data more important |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If `@Async` uses a proxy like `@Transactional`, does the self-invocation problem also apply?"
- **Winning Answer**: "Yes, identically. `@Async` is implemented via the same AOP proxy mechanism. If `sendEmail()` is called from within the same `NotificationService` class using `this.sendEmail()`, the proxy is bypassed and the method executes synchronously on the calling thread — no async execution. Additionally, if both `@Async` and `@Transactional` are on the same method, the order of proxy wrapping matters: the outer proxy handles one annotation, the inner handles the other. Spring resolves this by ordering: `@Transactional` proxy wraps the raw bean first, then `@Async` proxy wraps the `@Transactional` proxy. This means the async task runs in the executor thread, and within that thread, the transaction begins."

---

#### Q11: `@Cacheable` Internals — Cache Abstraction, KeyGenerator & Eviction

##### 1. Exact Scenario & Question
Your product catalog service uses `@Cacheable` to cache product data in Redis. After deploying a price update, customers still see old prices for up to 10 minutes. Additionally, a `@Cacheable` method that queries by `List<Long>` productIds caches all results under a single cache key, breaking when different ID subsets are queried. Explain how Spring's cache abstraction works internally, fix the stale cache problem, and implement a correct key strategy for collection parameters.

##### 2. What the Interviewer Evaluates
- Understanding that `@Cacheable` is intercepted by `CacheInterceptor` (a `MethodInterceptor`) which checks the cache before calling the real method.
- Knowledge of `CacheManager` → `Cache` → `CacheResolver` chain.
- Ability to implement `@CacheEvict`, `@CachePut`, and custom `KeyGenerator` for complex keys.

##### 3. Standout Technical Answer

```java
@Configuration
@EnableCaching  // Registers CacheInterceptor and CacheAspect BeanPostProcessor
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))  // Global TTL: 10 minutes
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();  // Don't cache null → forces DB call on null

        // Per-cache TTL overrides
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("products", config.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("categories", config.entryTtl(Duration.ofHours(1)));
        cacheConfigs.put("pricing", config.entryTtl(Duration.ofMinutes(1)));  // Prices change fast

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .withInitialCacheConfigurations(cacheConfigs)
            .transactionAware()  // Cache operations participate in Spring transactions
            .build();
    }
}

@Service
@CacheConfig(cacheNames = "products")  // Default cache name for all @Cacheable in this class
public class ProductCatalogService {

    private final ProductRepository productRepo;

    // ✅ CORRECT: Cache individual product by ID
    @Cacheable(
        key = "#productId",  // SpEL: cache key = "products::42"
        condition = "#productId > 0",  // Only cache valid IDs
        unless = "#result == null"  // Don't cache null results
    )
    public Product getProduct(Long productId) {
        // Cache HIT: Returns immediately from Redis — productRepo.findById() NOT called!
        // Cache MISS: Calls productRepo.findById(), stores result in Redis with 5-min TTL
        return productRepo.findById(productId).orElse(null);
    }

    // ✅ Fix stale cache: @CachePut updates cache EVERY time (no cache skip)
    @CachePut(key = "#product.id")  // Updates Redis entry even if key exists
    public Product updateProduct(Product product) {
        Product saved = productRepo.save(product);
        return saved;  // Return value is stored in cache — cache is ALWAYS updated
        // No more stale prices! Price update → @CachePut → Redis updated immediately
    }

    // ✅ @CacheEvict: Remove specific key or entire cache
    @CacheEvict(key = "#productId")  // Removes "products::42" from Redis
    public void deleteProduct(Long productId) {
        productRepo.deleteById(productId);
    }

    @CacheEvict(allEntries = true)  // Clears ENTIRE "products" cache
    @Scheduled(fixedRate = 300_000)  // Every 5 minutes (scheduled cache invalidation)
    public void clearProductCache() {
        log.info("Product cache cleared — forcing fresh load from DB");
    }

    // ✅ Fix collection parameter caching — custom KeyGenerator
    // ❌ WRONG: @Cacheable(key = "#productIds") — ALL lists cached as one key!
    //   findByIds([1,2,3]) and findByIds([4,5,6]) use SAME cache key? NO!
    //   Spring uses the list's toString() as the key — "[1, 2, 3]" vs "[4, 5, 6]"
    //   Actually these ARE different keys — but the key is the full list!
    //   findByIds([1,2,3]) → cache key = "products::[1, 2, 3]"
    //   findByIds([2,3,1]) → cache key = "products::[2, 3, 1]" — DIFFERENT CACHE ENTRY!
    //   Same product set in different order = two cache entries = waste!

    @Cacheable(keyGenerator = "sortedListKeyGenerator")
    public List<Product> findByIds(List<Long> productIds) {
        return productRepo.findAllById(productIds);
    }
}

// Custom KeyGenerator: sorts the list before generating key
@Bean("sortedListKeyGenerator")
public KeyGenerator sortedListKeyGenerator() {
    return (target, method, params) -> {
        if (params.length == 1 && params[0] instanceof Collection<?> collection) {
            // Sort IDs → [1,2,3] and [3,2,1] produce same cache key!
            List<String> sorted = collection.stream()
                .map(Object::toString)
                .sorted()
                .toList();
            return method.getName() + "::" + sorted;
        }
        return SimpleKeyGenerator.generateKey(params);
    };
}
```

**Spring Cache Abstraction — Internal Flow:**
```
Method call arrives → CacheInterceptor.invoke()
  ├─ CacheResolver resolves Cache from CacheManager
  ├─ KeyGenerator generates cache key from method params
  ├─ Cache.get(key) — check cache
  │     HIT: return cached value immediately (method NOT called!)
  │     MISS: proceed to actual method call
  │           store result: Cache.put(key, result)
  │           return result
```

| Annotation | Cache Check | Method Called | Cache Updated |
|---|---|---|---|
| `@Cacheable` | YES (returns cached if hit) | Only on MISS | Yes (on MISS) |
| `@CachePut` | NO (always calls method) | ALWAYS | Yes (always) |
| `@CacheEvict` | NO (calls method) | ALWAYS | Yes (removes key) |
| `@Caching` | Multiple operations combined | Depends | Depends |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If `@Cacheable` is on a method and `@Transactional` is also on the same method — does the cache see the data before or after the transaction commits?"
- **Winning Answer**: "By default — **before** the transaction commits, which can cause cache poisoning. Scenario: (1) Transaction reads stale data, caches it. (2) Transaction fails and rolls back. (3) Cache still has the stale pre-commit data. Fix: configure `RedisCacheManager.transactionAware()`. With `transactionAware()`, cache write operations (`put`) are deferred until the transaction commits. If the transaction rolls back, the cache is NOT updated. Cache reads (`get`) still work immediately (needed for the cache-check decision). This prevents caching uncommitted data."

---

#### Q12: Spring Profiles — Environment Abstraction & Conditional Bean Registration

##### 1. Exact Scenario & Question
Your application has three environments: local (H2 in-memory), staging (PostgreSQL with reduced pool size), and production (PostgreSQL with max pool size + read replicas + Vault secrets). Implement a multi-profile configuration using `application.yml` profile documents, `@Profile` conditional beans, and environment-specific `DataSource` configuration — showing how to avoid the most common profile misconfiguration mistakes.

##### 3. Standout Technical Answer

```yaml
# application.yml — Multi-profile single file (Spring Boot 2.4+)
spring:
  application:
    name: order-service
  profiles:
    active: local  # Override with: --spring.profiles.active=production

# ─────────────────────────────────────────────────────────────────
# Document separator: ---
# Local Development Profile
# ─────────────────────────────────────────────────────────────────
---
spring:
  config:
    activate:
      on-profile: local
  datasource:
    url: jdbc:h2:mem:orderdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    username: sa
    password: ""
    driver-class-name: org.h2.Driver
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
  jpa:
    hibernate:
      ddl-auto: create-drop  # Recreate schema on every start (local only!)
    show-sql: true
  h2:
    console:
      enabled: true

logging:
  level:
    com.example: DEBUG
    org.hibernate.SQL: DEBUG

# ─────────────────────────────────────────────────────────────────
# Staging Profile
# ─────────────────────────────────────────────────────────────────
---
spring:
  config:
    activate:
      on-profile: staging
  datasource:
    url: jdbc:postgresql://${DB_HOST:staging-db.internal}:5432/orderdb
    username: ${DB_USER}
    password: ${DB_PASS}
    hikari:
      maximum-pool-size: 10   # Smaller pool — staging has less resources
      minimum-idle: 5
      connection-timeout: 20000
      validation-timeout: 3000
  jpa:
    hibernate:
      ddl-auto: validate  # Never auto-migrate in staging!
    show-sql: false

logging:
  level:
    com.example: INFO

# ─────────────────────────────────────────────────────────────────
# Production Profile (Hardened)
# ─────────────────────────────────────────────────────────────────
---
spring:
  config:
    activate:
      on-profile: production
  datasource:
    url: jdbc:postgresql://${DB_PRIMARY_HOST}:5432/orderdb
    username: ${DB_USER}        # Injected by Vault Agent or AWS Secrets Manager
    password: ${DB_PASS}        # Injected by Vault Agent or AWS Secrets Manager
    hikari:
      maximum-pool-size: 25
      minimum-idle: 10
      connection-timeout: 5000   # Fail fast! 5s not 30s
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000  # Alert if connection held > 60s
      pool-name: order-service-prod
  jpa:
    hibernate:
      ddl-auto: validate  # NEVER create/update/create-drop in production!
    properties:
      hibernate:
        format_sql: false      # No SQL formatting overhead in production
        use_sql_comments: false

management:
  endpoints:
    web:
      exposure:
        include: health, metrics, prometheus
  endpoint:
    health:
      show-details: when-authorized  # Don't expose DB status to everyone!

logging:
  level:
    com.example: WARN  # Minimal logging in production
```

```java
// Profile-specific beans using @Profile annotation
@Configuration
public class DataSourceConfig {

    // ✅ Separate read-replica DataSource for production only
    @Bean("readReplicaDataSource")
    @Profile("production")  // Only registered when profile = "production"
    public DataSource readReplicaDataSource(
            @Value("${db.replica.host}") String replicaHost,
            @Value("${db.replica.port:5432}") int replicaPort) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://" + replicaHost + ":" + replicaPort + "/orderdb");
        ds.setMaximumPoolSize(20);
        ds.setReadOnly(true);
        return ds;
    }

    // Mock DataSource for local testing — no real DB needed
    @Bean("dataSource")
    @Profile("local")
    public DataSource embeddedDataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:schema.sql")
            .addScript("classpath:test-data.sql")
            .build();
    }

    // Shared configuration for staging + production
    @Bean("dataSource")
    @Profile({"staging", "production"})  // Active for EITHER staging OR production
    public DataSource postgresDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }
}

// Profile-specific service implementations
@Service
@Profile("!production")  // Active in local AND staging (NOT in production)
public class MockPaymentGateway implements PaymentGateway {
    @Override
    public PaymentResult charge(BigDecimal amount) {
        log.info("MOCK payment charged: {}", amount);
        return PaymentResult.success("MOCK-TXN-001");
    }
}

@Service
@Profile("production")
public class StripePaymentGateway implements PaymentGateway {
    @Override
    public PaymentResult charge(BigDecimal amount) {
        return stripeClient.createCharge(amount);
    }
}
```

**Common Profile Mistakes:**
```java
// ❌ MISTAKE 1: Activating wrong profile via environment variable
# SPRING_PROFILES_ACTIVE vs spring.profiles.active — both work but differ in priority:
# spring.profiles.active (YAML) < SPRING_PROFILES_ACTIVE (env var) < --spring.profiles.active (CLI)

// ❌ MISTAKE 2: Including profile suffix in filename (old Spring Boot 1.x style)
# application-production.yml  ← works but deprecated in Spring Boot 2.4+
# Use: Multi-document YAML with spring.config.activate.on-profile instead

// ❌ MISTAKE 3: @Profile("prod") when profile name is "production"
@Profile("prod")  // ← Never active! Profile name is "production", not "prod"!

// ✅ Use constants to avoid typos:
public final class Profiles {
    public static final String LOCAL = "local";
    public static final String STAGING = "staging";
    public static final String PRODUCTION = "production";
}
@Profile(Profiles.PRODUCTION)  // Compile-time safety
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can you have multiple active Spring profiles simultaneously? What happens if two `@Profile("X")` beans conflict?"
- **Winning Answer**: "Yes. Multiple profiles can be active simultaneously: `--spring.profiles.active=production,feature-flags,debug`. YAML profile documents with `on-profile: production` AND `on-profile: feature-flags` are both active. If two beans of the same type are registered (one with `@Profile('production')` and one with `@Profile('feature-flags')`), Spring throws `NoUniqueBeanDefinitionException: expected single matching bean but found 2` unless one is marked `@Primary` or the injection uses `@Qualifier`. Best practice: profiles should represent orthogonal concerns (environment + features + experiments), not conflicting configurations."

---

### Section 2: Beginner Mistakes & Anti-Patterns (15 Common Mistakes)

---

#### ❌ Mistake 1: Using `@Autowired` on Private Fields

```java
// BUG: Field injection — not testable, not immutable, hides bloat
@Service
public class OrderService {
    @Autowired private OrderRepository repo;
    @Autowired private PaymentClient payment;
    @Autowired private NotificationService notification;
    @Autowired private AuditService audit;
    @Autowired private FraudDetector fraud;
    // 5 hidden dependencies — no warning from compiler or IDE about SRP violation
}
```

💥 **Why It Fails:** Cannot unit test without starting Spring or using reflection. Fields cannot be `final`. 10-15 field injections accumulate silently, indicating a God class.

```java
// FIX: Constructor injection with Lombok
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repo;  // 5 dependencies with final fields
    // → If this gets too many, extract to smaller services. Compiler enforces it!
}
```

🧠 **Production Rule:** If a constructor has more than 4-5 parameters, it's a sign to refactor, not to switch to field injection.

---

#### ❌ Mistake 2: `@Transactional` on Private Methods

```java
// BUG: @Transactional on private method — COMPLETELY IGNORED
@Service
public class OrderService {
    public void processOrder(Order order) {
        saveOrderPrivate(order);  // Calls private method
    }

    @Transactional  // Spring CANNOT proxy private methods! CGLIB/JDK cannot override them!
    private void saveOrderPrivate(Order order) {
        orderRepo.save(order);  // No transaction! Exception = no rollback!
    }
}
```

💥 **Why It Fails:** Spring's CGLIB proxy generates a subclass that can only override `public` and `protected` non-final methods. Private methods are invisible to the proxy. The `@Transactional` annotation is silently ignored.

```java
// FIX: Make the method public (or protected), or extract to a new @Service bean
@Transactional
public void saveOrder(Order order) {  // Public → proxy can intercept
    orderRepo.save(order);
}
```

🧠 **Production Rule:** `@Transactional` (and all Spring AOP annotations) require `public` or `protected` methods on non-`final` classes. Spring generates a warning log in debug mode but no error.

---

#### ❌ Mistake 3: Catching RuntimeException Inside `@Transactional` Method

```java
// BUG: Exception caught — Spring never sees it — NO ROLLBACK!
@Transactional
public void transferMoney(Account from, Account to, BigDecimal amount) {
    try {
        from.debit(amount);
        to.credit(amount);
        accountRepo.saveAll(List.of(from, to));
    } catch (InsufficientFundsException e) {
        log.error("Transfer failed", e);
        // ❌ Exception swallowed → @Transactional sees normal return → COMMITS!
        // from.debit() wrote to DB but to.credit() did NOT — INCONSISTENT STATE!
    }
}
```

💥 **Why It Fails:** Spring's transaction interceptor rolls back ONLY when an exception propagates out of the method. Catching it prevents rollback; the partial state (debit without credit) commits.

```java
// FIX: Re-throw, or configure rollbackFor for checked exceptions
@Transactional(rollbackFor = InsufficientFundsException.class)
public void transferMoney(Account from, Account to, BigDecimal amount)
        throws InsufficientFundsException {
    from.debit(amount);   // Throws InsufficientFundsException if insufficient
    to.credit(amount);    // Only reached if debit succeeded
    accountRepo.saveAll(List.of(from, to));
    // Exception propagates → Spring rolls back BOTH changes atomically!
}
```

🧠 **Production Rule:** By default, `@Transactional` only rolls back on `RuntimeException` and `Error`. For checked exceptions, declare `rollbackFor`. Never swallow exceptions in transactional methods.

---

#### ❌ Mistake 4: Using `ddl-auto: update` in Production

```yaml
# CATASTROPHIC BUG: Hibernate auto-DDL in production
spring:
  jpa:
    hibernate:
      ddl-auto: update  # ← Can DROP columns, lock tables for hours, corrupt data
```

💥 **Why It Fails:** `update` compares entity classes with the live schema and executes `ALTER TABLE` statements. On a 100M-row table, `ALTER TABLE ADD COLUMN` acquires an exclusive lock blocking all reads/writes for hours. Additionally, `update` can never remove columns — schema and entities diverge over time.

```yaml
# FIX: Flyway migrations + validate-only
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Only checks schema matches entities — no changes!
  flyway:
    enabled: true
    locations: classpath:db/migration
    validate-on-migrate: true
# All schema changes via versioned Flyway migrations (V1__init.sql, V2__add_status.sql)
```

🧠 **Production Rule:** `ddl-auto: validate` in staging and production. `ddl-auto: create-drop` in local only. Never `update` or `create` in any non-local environment.

---

#### ❌ Mistake 5: `@Autowired ApplicationContext` to Get Beans Programmatically

```java
// BAD: Programmatic bean lookup breaks Dependency Injection principles
@Service
public class OrderService {
    @Autowired
    private ApplicationContext context;  // Service depends on the container itself!

    public void processOrder(Order order) {
        // Service Locator anti-pattern!
        PaymentClient client = context.getBean(PaymentClient.class);
        client.charge(order.getAmount());
    }
}
```

💥 **Why It Fails:** This is the Service Locator anti-pattern. The class depends on the IoC container itself, making it impossible to test without a Spring context. Dependencies are hidden — callers cannot know what the class needs without reading its implementation.

```java
// FIX: Inject PaymentClient directly via constructor
@Service
@RequiredArgsConstructor
public class OrderService {
    private final PaymentClient paymentClient;  // Explicit, testable dependency
    // context.getBean() is almost never the right answer for production code
}
// Exception: Dynamic bean lookup by runtime-determined type →
//   use ObjectProvider<T> or Map<String, T> injection instead
```

🧠 **Production Rule:** `ApplicationContext.getBean()` is appropriate only in framework-level code or for truly dynamic dispatch (e.g., selecting a `PaymentGateway` implementation by name at runtime using a `Map<String, PaymentGateway>`).

---

#### ❌ Mistake 6: `@PostConstruct` Calling `@Transactional` Methods

```java
// BUG: @Transactional call inside @PostConstruct = NO TRANSACTION
@Service
public class CacheWarmer {

    @PostConstruct  // Fires at Step 6 — BEFORE AOP proxy creation at Step 9!
    public void warmCache() {
        loadFromDb();  // Calls @Transactional method on RAW bean → no transaction!
    }

    @Transactional  // Proxy not created yet when @PostConstruct fires!
    public void loadFromDb() {
        productRepo.findAll().forEach(p -> cache.put(p.getId(), p));
        // No transaction context → reads in auto-commit mode
        // Worse: if productRepo uses @Transactional internally → nested calls without tx
    }
}
```

💥 **Why It Fails:** `@PostConstruct` fires at Step 6. AOP proxies (including `@Transactional`) are created at Step 9. When `@PostConstruct` calls a `@Transactional` method via `this`, `this` is still the raw unproxied object.

```java
// FIX: Use ApplicationRunner (fires AFTER all Step 9s — beans are fully proxied)
@Component
public class CacheWarmer implements ApplicationRunner {
    private final CatalogService catalogService;  // INJECTED AS PROXY at this point

    @Override
    public void run(ApplicationArguments args) {
        catalogService.loadFromDb();  // ✅ Calls via proxy → @Transactional ACTIVE!
    }
}
```

---

#### ❌ Mistake 7: Not Configuring `spring.jpa.open-in-view=false`

```yaml
# BUG: Spring Boot default — OSIV keeps DB connection open for entire HTTP request!
# spring.jpa.open-in-view=true  ← Spring Boot DEFAULT (even in Boot 3.x!)
```

💥 **Why It Fails:** Open Session In View (OSIV) keeps the Hibernate session (and the database connection!) open from the start of the HTTP request to the end, including the serialization phase. This means a DB connection is held while JSON serialization runs, caching layers are queried, template engines render, etc. — massively inflating connection pool utilization.

```yaml
# FIX: Disable OSIV and use proper service-layer transactions
spring:
  jpa:
    open-in-view: false  # ← Always set this explicitly in production!
```

```java
// After disabling OSIV, move all data access inside @Transactional service methods:
@Service
@Transactional(readOnly = true)
public class ProductService {
    public ProductDTO getProduct(Long id) {
        Product p = repo.findByIdWithCategory(id).orElseThrow();
        // ✅ Convert to DTO while session is open (inside @Transactional)
        return new ProductDTO(p.getId(), p.getName(), p.getCategory().getName());
    }
    // Session closes when method exits → no lazy loading after this point
}
```

🧠 **Production Rule:** Always set `spring.jpa.open-in-view=false`. Design service methods to convert entities to DTOs within the transaction scope.

---

### Section 3: Globally Reported Production Issues & War‑Room Solutions

---

#### 🚨 Incident 1: HikariCP Connection Pool Starvation Freezing Production Gateway

**The Incident:**  
A fintech company's payment gateway froze under peak Black Friday traffic. All 200 Tomcat threads blocked on `HikariPool.getConnection()` waiting for 30 seconds, causing cascading timeouts upstream. The gateway handled $50M/day in transactions.

**Timeline:**
- 11:23 AM: Traffic ramps up to 10× normal
- 11:24 AM: `hikaricp_connections_pending` Prometheus metric spikes to 180
- 11:25 AM: First 503 errors appear in APM
- 11:26 AM: All 200 Tomcat threads blocked → gateway completely unresponsive
- 11:56 AM: Traffic drops naturally → gateway recovers
- Post-mortem: 33 minutes of complete unavailability

**Root Cause Analysis:**
```java
// The problematic code: @Transactional at the CONTROLLER level!
@RestController
public class PaymentController {

    @PostMapping("/payments")
    @Transactional  // ← WRONG LOCATION! Opens DB connection for entire HTTP request!
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        // Step 1: Validate request (no DB needed) — connection held unnecessarily
        validator.validate(request);

        // Step 2: Call external payment processor (HTTP call — 100-500ms!)
        // DB CONNECTION IS HELD IDLE DURING THIS ENTIRE EXTERNAL CALL!
        PaymentResult result = stripeClient.charge(request.getAmount());

        // Step 3: Save to DB
        return ResponseEntity.ok(paymentService.save(result));
    }
    // Connection held for: validation_time + stripe_api_time + db_save_time
    // = 2ms + 350ms + 5ms = 357ms per request
    // With 200 threads × 357ms average = pool needs 72 connections
    // Pool size = 10 → 190 threads waiting!
}
```

**The Immediate Fix (Deployed During Incident):**
```yaml
# Temporary: Increase pool size to handle peak load
spring.datasource.hikari.maximum-pool-size: 40
spring.datasource.hikari.connection-timeout: 5000  # Fail fast → 503 instead of 30s timeout
```

**The Permanent Fix:**
```java
// Move @Transactional to service layer — narrowest possible scope!
@RestController
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/payments")
    // NO @Transactional here — controller holds no DB connection!
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        validator.validate(request);                              // No DB, no connection
        PaymentResult result = stripeClient.charge(request.getAmount());  // External call, no connection
        return ResponseEntity.ok(paymentService.save(result));   // Opens connection ONLY for DB ops
    }
}

@Service
public class PaymentService {
    @Transactional  // ← Correct location: only during actual DB operations!
    public PaymentResponse save(PaymentResult result) {
        Payment p = paymentRepo.save(Payment.from(result));  // ~5ms DB operation
        return PaymentResponse.from(p);
        // Transaction commits, connection returns to pool
        // Connection held for: 5ms (vs 357ms in the broken version!)
    }
}
// With 200 threads × 5ms connection hold = pool needs 1 connection → 10 connections = plenty!
```

**Prevention Checklist:**
- [ ] NEVER put `@Transactional` on `@RestController` methods
- [ ] Set `connection-timeout: 5000` — fail fast instead of queuing for 30 seconds
- [ ] Set `leak-detection-threshold: 30000` — alert if connection held > 30s
- [ ] Monitor `hikaricp_connections_pending` — alert when sustained > 0
- [ ] Monitor `hikaricp_connections_active` — alert when > `maximum-pool-size × 0.8`
- [ ] Move all external HTTP calls (Stripe, Twilio, S3) OUTSIDE transaction boundaries

---

#### 🚨 Incident 2: `LazyInitializationException` Crashing Checkout at Scale

**The Incident:** After a "performance optimization" deployment that changed all JPA associations from `EAGER` to `LAZY`, the checkout API started returning 500 errors for every request. The error: `org.hibernate.LazyInitializationException: could not initialize proxy [com.example.OrderItem#1] - no Session`.

**Root Cause Analysis:**
```java
@Entity
public class Order {
    @OneToMany(fetch = FetchType.LAZY)  // Changed from EAGER to LAZY
    private List<OrderItem> items;      // Now: proxy object, loaded on first access
}

@Service
public class OrderService {
    // NO @Transactional — Hibernate session closes when findById() returns!
    public Order getOrder(Long id) {
        return orderRepo.findById(id).orElseThrow();
        // Session: OPEN during findById() → CLOSED when this method returns
        // order.items = LazyProxy (not yet loaded)
    }
}

@RestController
public class OrderController {
    @GetMapping("/orders/{id}")
    public OrderDTO getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);  // Session already CLOSED!
        // Jackson calls order.getItems() → LazyProxy.initialize()
        // → No active Session → LazyInitializationException! → 500 error!
        return mapper.toDTO(order);
    }
}
// OSIV was disabled (spring.jpa.open-in-view=false) — correct!
// But service was not @Transactional — session closed before serialization
```

**The Fix:**
```java
@Service
public class OrderService {

    // FIX 1: @Transactional(readOnly=true) keeps session open during DTO conversion
    @Transactional(readOnly = true)
    public OrderDTO getOrder(Long id) {
        Order order = orderRepo.findById(id).orElseThrow();
        // Session still OPEN — convert to DTO while items can be lazily loaded
        return new OrderDTO(
            order.getId(),
            order.getStatus(),
            order.getItems().stream().map(ItemDTO::from).toList()  // ← Safe: session open!
        );
        // Session closes when method exits — DTO is returned (no entity, no proxies)
    }

    // FIX 2: Use JOIN FETCH to eagerly load items in one query
    @Transactional(readOnly = true)
    public OrderDTO getOrderWithItems(Long id) {
        Order order = orderRepo.findByIdWithItems(id).orElseThrow();
        return OrderDTO.from(order);
    }
}

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
    // Generates: SELECT o.*, i.* FROM orders o JOIN order_items i ON i.order_id = o.id WHERE o.id = ?
    // 1 query! No lazy loading needed.
}
```

**Prevention Checklist:**
- [ ] Set `spring.jpa.open-in-view=false` — forces lazy loading issues to surface immediately
- [ ] Test every API endpoint that returns JPA entities with lazy associations
- [ ] Use DTO projections for all API responses — never return JPA entities directly
- [ ] Add integration tests that verify complete JSON serialization (including all nested fields)
- [ ] Run `EXPLAIN ANALYZE` on new queries — `JOIN FETCH` can cause Cartesian product

---

#### 🚨 Incident 3: Spring Security Upgrade Breaks All CORS Preflight Requests

**The Incident:** After upgrading from Spring Boot 3.0 to 3.1 / Spring Security 6.1, all cross-origin API calls from the React frontend failed. The browser console showed: `Access to fetch at 'https://api.example.com' from origin 'https://app.example.com' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.`

**Root Cause Analysis:**
Spring Security 6.1 changed `AuthorizationFilter` ordering. It now runs **before** the `CorsFilter` in the filter chain. CORS preflight `OPTIONS` requests carry no `Authorization` header (by browser spec). The new `AuthorizationFilter` evaluates `anyRequest().authenticated()` → `OPTIONS` has no credentials → **403 Forbidden**. The browser never receives the `Access-Control-Allow-Origin` header (because the request was blocked before CORS headers were added) → `CORS error` in browser console (misleading — the real error is 403).

**The Fix:**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        // ✅ ALWAYS declare .cors() FIRST in the filter chain
        .cors(cors -> cors.configurationSource(corsConfigSource()))
        .authorizeHttpRequests(auth -> auth
            // ✅ Explicitly permit OPTIONS preflight before any auth check
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/api/public/**", "/actuator/health/**").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
}

@Bean
public CorsConfigurationSource corsConfigSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://app.example.com"));  // Specific origins only!
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Request-ID"));
    config.setExposedHeaders(List.of("X-Total-Count", "Link"));  // Headers exposed to JS
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);  // Cache preflight for 1 hour

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

**Prevention Checklist:**
- [ ] Read Spring Security migration guide before upgrading major versions
- [ ] Add explicit `.cors()` configuration — never rely on Spring defaults for CORS
- [ ] Permit `OPTIONS` requests explicitly in `authorizeHttpRequests`
- [ ] Add a CI test: `curl -X OPTIONS -H "Origin: https://app.example.com" https://api.example.com/api/orders`
- [ ] Test CORS in a real browser after every Spring Security upgrade

---

#### 🚨 Incident 4: Prototype Bean Shared Among All Users (Cart Corruption)

**The Incident:** An e-commerce platform's Black Friday launch revealed that all users shared the same shopping cart. Items added by User A appeared in User B's cart.

**Root Cause:** `ShoppingCart` was annotated `@Scope("prototype")` but injected into a singleton `CheckoutService` via constructor. The prototype was created once (at singleton construction) and shared forever.

**The Fix:**
```java
// Solution: ObjectProvider<ShoppingCart> — creates new instance on each getObject() call
@Service
public class CheckoutService {
    private final ObjectProvider<ShoppingCart> cartProvider;

    public CheckoutService(ObjectProvider<ShoppingCart> cartProvider) {
        this.cartProvider = cartProvider;
    }

    public void addItem(String sessionId, Item item) {
        ShoppingCart userCart = sessionCartStore.getOrCreate(sessionId,
            () -> cartProvider.getObject());  // NEW instance per user session
        userCart.add(item);
    }
}
```

---

#### 🚨 Incident 5: Circular Dependency After Spring Boot 3 Upgrade

**The Incident:** After upgrading to Spring Boot 3 (Spring Framework 6), the application failed to start with: `The dependencies of some of the beans in the application context form a cycle: OrderService → PaymentService → OrderService`.

**Root Cause Analysis:**
Spring 6 enables `spring.main.allow-circular-references=false` by default. In Spring 5, circular dependencies via setter/field injection were allowed (using "early object references"). Spring 6 blocks them entirely.

**The Fix:**
```java
// WRONG (circular dependency):
@Service public class OrderService {
    @Autowired private PaymentService paymentService;  // A depends on B
}
@Service public class PaymentService {
    @Autowired private OrderService orderService;  // B depends on A → CYCLE!
}

// FIX 1: Extract shared logic to a third service (recommended)
@Service public class OrderEventService {  // C: contains logic both A and B need
    public void publishOrderEvent(Order order) { ... }
}
@Service public class OrderService {
    @Autowired private OrderEventService eventService;  // A depends on C (no cycle!)
}
@Service public class PaymentService {
    @Autowired private OrderEventService eventService;  // B depends on C (no cycle!)
}

// FIX 2: Use ApplicationEvent to decouple (event-driven, no direct dependency)
@Service public class PaymentService {
    private final ApplicationEventPublisher publisher;
    public void processPayment(Payment p) {
        publisher.publishEvent(new PaymentCompletedEvent(p));
    }
}
@Service public class OrderService implements ApplicationListener<PaymentCompletedEvent> {
    public void onApplicationEvent(PaymentCompletedEvent event) {
        updateOrderStatus(event.getOrderId());
    }
}
```

---

### Section 4: Spring Boot Performance Optimization Reference

```yaml
# Production-Hardened application.yml Template
spring:
  application:
    name: order-service
    version: 2.1.0

  # Disable OSIV (open session in view) — mandatory for high-throughput
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc.batch_size: 50           # Batch inserts/updates
        order_inserts: true           # Group inserts by table
        order_updates: true           # Group updates by table
        generate_statistics: false    # Enable only for diagnostics (performance cost)

  # HikariCP — fine-tuned for production
  datasource:
    hikari:
      maximum-pool-size: 25
      minimum-idle: 10
      connection-timeout: 5000        # 5s — fail fast!
      idle-timeout: 600000            # 10 min
      max-lifetime: 1800000           # 30 min (< DB connection timeout)
      leak-detection-threshold: 60000 # Alert if connection held > 60s
      pool-name: ${spring.application.name}-pool

  # Virtual Threads (Java 21) — eliminates thread pool sizing concerns
  threads:
    virtual:
      enabled: true  # All Tomcat worker threads become virtual threads!

  # Graceful shutdown — wait for in-flight requests
  lifecycle:
    timeout-per-shutdown-phase: 30s

server:
  shutdown: graceful
  tomcat:
    accept-count: 100          # Queue size when all threads busy
    max-connections: 8192      # Max concurrent TCP connections

# Actuator — Kubernetes health probes
management:
  endpoint:
    health:
      probes:
        enabled: true
  endpoints:
    web:
      exposure:
        include: health, metrics, prometheus

# Disable JMX (reduces startup time and memory)
spring.jmx.enabled: false
```

---

#### Q13: Spring Events — Decoupled Communication Between Beans

##### 1. Exact Scenario & Question
You need to send a welcome email, grant loyalty points, and trigger analytics tracking every time a new user registers. You currently call all three services directly from `UserService.register()`. The team wants to add a fourth service next sprint. Show how Spring's application event system enables true decoupling: the `UserService` doesn't know anything about the downstream services.

##### 2. What the Interviewer Evaluates
- Understanding that `ApplicationEvent` → `ApplicationEventPublisher` → `ApplicationListener` or `@EventListener` wires decoupled communication.
- Knowledge of synchronous vs asynchronous event dispatch and transaction-bound events (`@TransactionalEventListener`).
- Ability to explain event ordering and exception isolation.

##### 3. Standout Technical Answer

```java
// Step 1: Define the event (a plain record or class)
public record UserRegisteredEvent(String userId, String email, String name, Instant registeredAt) {}

// Step 2: Publish the event from UserService (no knowledge of listeners!)
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;  // Spring injects this

    @Transactional
    public User register(RegistrationRequest request) {
        User user = userRepository.save(User.from(request));

        // Publish event AFTER saving — listeners can now query the saved user
        // By default: synchronous — all listeners run on this thread before method returns
        eventPublisher.publishEvent(new UserRegisteredEvent(
            user.getId(), user.getEmail(), user.getName(), Instant.now()
        ));

        return user;
        // @Transactional commits AFTER all synchronous listeners complete
    }
}

// Step 3: Each downstream concern listens independently — complete decoupling!
@Component
@Slf4j
public class WelcomeEmailListener {

    // @EventListener: fires SYNCHRONOUSLY in the publisher's thread
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Sending welcome email to {}", event.email());
        emailService.sendWelcome(event.email(), event.name());
        // If this throws: exception propagates to UserService.register() → transaction rolls back!
    }
}

@Component
public class LoyaltyPointsListener {
    private final LoyaltyService loyaltyService;

    // @Async makes this listener run on a separate thread (fire-and-forget)
    @Async("loyaltyExecutor")
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        loyaltyService.grantSignupPoints(event.userId(), 100);
        // Runs asynchronously — does NOT block UserService.register() from returning
        // Exception here does NOT affect UserService's transaction!
    }
}

@Component
public class TransactionalEventListener_Example {

    // @TransactionalEventListener: fires ONLY AFTER the publishing transaction COMMITS
    // Use when the listener needs the saved data to be visible in the DB!
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegisteredAfterCommit(UserRegisteredEvent event) {
        // The user is now COMMITTED to the DB — analytics can query it!
        analyticsService.trackRegistration(event.userId());
        // If UserService transaction ROLLS BACK → this listener is NOT called!
    }

    // BEFORE_COMMIT: fires inside the publishing transaction before commit
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onUserRegisteredBeforeCommit(UserRegisteredEvent event) {
        // Still inside transaction — can read/write in the SAME transaction context
        auditRepo.record(event.userId(), "REGISTERED");
    }

    // AFTER_ROLLBACK: fires after transaction rolls back (for cleanup/compensation)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onRegistrationFailed(UserRegisteredEvent event) {
        log.warn("Registration failed for {} — cleaning up external resources", event.email());
        // e.g., remove pre-created AWS resources
    }
}
```

*Code Walkthrough:*
1. **`ApplicationEventPublisher.publishEvent()`** — Spring's built-in event bus. The publisher knows nothing about listeners. Adding a new listener requires ZERO changes to `UserService`.
2. **`@EventListener` (synchronous)** — Runs in the publisher's thread within the same transaction. Exception propagates to the publisher, rolling back the transaction.
3. **`@Async @EventListener`** — Offloads to a separate thread. Fire-and-forget. Does not affect the publishing transaction.
4. **`@TransactionalEventListener(AFTER_COMMIT)`** — Fires after the transaction commits. Guarantees the listener only runs if the data was actually saved. Critical for event-driven workflows where downstream services query the DB.

| Event Dispatch | Thread | Transaction Context | Exception Propagation |
|---|---|---|---|
| `@EventListener` (sync) | Publisher's thread | Same as publisher | Propagates to publisher |
| `@Async @EventListener` | Executor thread | None | AsyncUncaughtExceptionHandler |
| `@TransactionalEventListener(AFTER_COMMIT)` | Publisher's thread | New (if needed) | Does NOT propagate |
| `@TransactionalEventListener(BEFORE_COMMIT)` | Publisher's thread | Same as publisher | Propagates (rolls back) |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If `@TransactionalEventListener(AFTER_COMMIT)` throws an exception, what happens to the committed data?"
- **Winning Answer**: "Nothing — the original transaction is already committed; it cannot be rolled back by a post-commit exception. The committed data remains in the database permanently. The exception in `AFTER_COMMIT` is caught by Spring's event multicaster and logged, but the original transaction is unaffected. This is why `AFTER_COMMIT` listeners should be idempotent and handle their own exceptions, or use an outbox pattern with retry for critical operations."

---

#### Q14: `@ConfigurationProperties` vs `@Value` — Structured vs Point Configuration

##### 1. Exact Scenario & Question
You need to configure 15 properties for a payment gateway client: URL, timeout, retry count, API key, circuit breaker thresholds, and 9 more. A colleague proposes 15 individual `@Value("${payment.gateway.url}")` annotations scattered across the class. Show why `@ConfigurationProperties` is the production standard and implement a type-safe, validated configuration class with Kubernetes secret injection.

##### 2. What the Interviewer Evaluates
- Understanding that `@Value` is imperative (one annotation per property), while `@ConfigurationProperties` uses JavaBean binding to map an entire prefix to a POJO.
- Knowledge of `@Validated` + JSR-303 constraints on `@ConfigurationProperties` classes — fail at startup if invalid.
- Ability to implement nested configuration with records (Java 16+).

##### 3. Standout Technical Answer

```yaml
# application.yml
payment:
  gateway:
    base-url: https://api.stripe.com/v1
    api-key: ${STRIPE_API_KEY}          # Injected from Kubernetes Secret or environment variable
    connect-timeout-ms: 3000
    read-timeout-ms: 5000
    max-retries: 3
    retry-backoff-ms: 500
    circuit-breaker:
      failure-threshold: 50            # Open circuit if 50% of calls fail
      slow-call-threshold-ms: 2000
      wait-duration-in-open-state-s: 30
      permitted-calls-in-half-open: 5
    connection-pool:
      max-connections: 50
      max-connections-per-route: 10
      evict-idle-connections-after-s: 60
```

```java
// ✅ @ConfigurationProperties — maps entire prefix to type-safe POJO
@ConfigurationProperties(prefix = "payment.gateway")
@Validated  // Enables JSR-303 validation — throws at startup if invalid!
public record PaymentGatewayProperties(
    @NotBlank String baseUrl,
    @NotBlank String apiKey,
    @Positive @Max(30000) int connectTimeoutMs,
    @Positive @Max(60000) int readTimeoutMs,
    @Positive @Max(10) int maxRetries,
    @Positive int retryBackoffMs,
    @NotNull CircuitBreakerProperties circuitBreaker,
    @NotNull ConnectionPoolProperties connectionPool
) {
    // Nested configuration with records
    public record CircuitBreakerProperties(
        @Min(1) @Max(100) int failureThreshold,
        @Positive int slowCallThresholdMs,
        @Positive int waitDurationInOpenStateS,
        @Positive @Max(20) int permittedCallsInHalfOpen
    ) {}

    public record ConnectionPoolProperties(
        @Positive @Max(500) int maxConnections,
        @Positive int maxConnectionsPerRoute,
        @Positive int evictIdleConnectionsAfterS
    ) {}
}

// Register in @SpringBootApplication or via @EnableConfigurationProperties
@SpringBootApplication
@EnableConfigurationProperties(PaymentGatewayProperties.class)
public class Application { }

// Usage in Service — constructor-injected, fully typed
@Service
@RequiredArgsConstructor
public class PaymentGatewayClient {
    private final PaymentGatewayProperties config;
    private final RestClient restClient;

    @PostConstruct
    public void initialize() {
        log.info("Payment gateway configured: url={}, connectTimeout={}ms, maxRetries={}",
            config.baseUrl(), config.connectTimeoutMs(), config.maxRetries());
        // apiKey is NOT logged (sensitive!)
    }

    public PaymentResult charge(ChargeRequest request) {
        return restClient.post()
            .uri(config.baseUrl() + "/charges")
            .header("Authorization", "Bearer " + config.apiKey())
            .body(request)
            .retrieve()
            .body(PaymentResult.class);
    }
}
```

**`@Value` vs `@ConfigurationProperties` — When Each Is Appropriate:**
```java
// @Value — appropriate for a SINGLE simple property injection
@Service
public class FeatureFlagService {
    @Value("${features.new-checkout-flow.enabled:false}")  // With default!
    private boolean newCheckoutEnabled;

    @Value("${app.version}")
    private String appVersion;
}

// @ConfigurationProperties — appropriate for ANY grouped/related configuration
// Rule of thumb: ≥3 related properties → use @ConfigurationProperties
```

| Feature | `@Value` | `@ConfigurationProperties` |
|---|---|---|
| Type safety | Limited (SpEL, primitives) | Full (nested POJOs, records) |
| Validation | Manual | `@Validated` + JSR-303 at startup |
| Refactoring | Error-prone (string keys) | IDE-safe (Java fields) |
| IDE metadata | Limited | Full (spring-configuration-processor) |
| Relaxed binding | No | Yes (`base-url` → `baseUrl`, `BASE_URL`) |
| Default values | `@Value("${key:default}")` | Field initializer |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If `STRIPE_API_KEY` environment variable is empty in production, will Spring fail at startup or at runtime when the payment gateway is first called?"
- **Winning Answer**: "With `@ConfigurationProperties` + `@Validated` + `@NotBlank String apiKey` — it fails at **startup**. Spring evaluates all JSR-303 constraints during the context refresh phase (before the embedded server starts and before any traffic is accepted). `@NotBlank` would fire with `ConstraintViolationException: apiKey must not be blank`. This is the desired behavior — it's far better to fail fast at deployment time than to accept traffic and fail on the first payment attempt. Without `@Validated`, the empty string would be silently injected and cause a 401 Unauthorized from Stripe on the first request, which is much harder to diagnose."

---

#### Q15: Spring Retry & Circuit Breaker — Resilience4j Integration

##### 1. Exact Scenario & Question
Your order service calls an external inventory API that has intermittent failures (5-10% error rate, occasional 5-second spikes). Without retry logic, 10% of order placements fail. With naive infinite retries, a sustained inventory outage causes your order service to accumulate thousands of blocked threads. Implement `@Retryable` with backoff, a Resilience4j circuit breaker, and fallback methods — explaining the state machine behind the circuit breaker.

##### 2. What the Interviewer Evaluates
- Knowledge of `@Retryable` with `maxAttempts`, `backoff` (fixed, exponential, jitter).
- Understanding of Resilience4j `CircuitBreaker` state machine: CLOSED → OPEN → HALF_OPEN → CLOSED.
- Ability to implement `@CircuitBreaker` with `@Fallback` for graceful degradation.

##### 3. Standout Technical Answer

```java
// Configuration
@Configuration
@EnableRetry  // Activates @Retryable AOP processing
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)           // Open if 50% of last N calls fail
            .slowCallRateThreshold(70)          // Open if 70% of calls > slowCallDurationThreshold
            .slowCallDurationThreshold(Duration.ofSeconds(2))
            .permittedNumberOfCallsInHalfOpenState(5)  // Test with 5 calls after waiting
            .minimumNumberOfCalls(10)           // Need at least 10 calls before calculating rate
            .slidingWindowType(SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(20)              // Calculate rate over last 20 calls
            .waitDurationInOpenState(Duration.ofSeconds(30))  // Wait 30s before half-open
            .recordExceptions(IOException.class, RestClientException.class)
            .ignoreExceptions(ValidationException.class)  // Don't count biz exceptions as failures
            .build();

        return CircuitBreakerRegistry.of(config);
    }
}

@Service
@Slf4j
public class InventoryClient {
    private final RestClient restClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    // Layer 1: @Retryable — handles transient failures (network glitches)
    @Retryable(
        retryFor = {RestClientException.class, SocketTimeoutException.class},
        noRetryFor = {HttpClientErrorException.class},  // Don't retry 4xx (client errors)
        maxAttempts = 3,
        backoff = @Backoff(
            delay = 500,        // First retry: wait 500ms
            multiplier = 2.0,   // Second retry: wait 1000ms
            maxDelay = 5000,    // Never wait more than 5s
            random = true       // Add jitter: prevents "thundering herd" on recovery
        )
    )
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "reserveInventoryFallback")
    public InventoryResponse reserveInventory(Long productId, int quantity) {
        log.info("Calling inventory API: product={}, qty={}", productId, quantity);
        return restClient.post()
            .uri("/inventory/reserve")
            .body(new ReservationRequest(productId, quantity))
            .retrieve()
            .body(InventoryResponse.class);
    }

    // Fallback: called when circuit is OPEN or all retries exhausted
    public InventoryResponse reserveInventoryFallback(Long productId, int quantity, Exception ex) {
        log.warn("Inventory circuit open or retries exhausted for product={}. Fallback activated. Error: {}",
            productId, ex.getMessage());

        // Graceful degradation options:
        // Option 1: Return "pending" — process order and reconcile inventory later
        return InventoryResponse.pending(productId, quantity, "FALLBACK_RESERVED");

        // Option 2: Return from cache (stale but available)
        // return inventoryCache.getLastKnownStock(productId);

        // Option 3: Throw BusinessException to reject the order clearly
        // throw new InventoryUnavailableException("Inventory service unavailable, try again in 30s");
    }

    @Recover  // Called by @Retryable after maxAttempts exhausted (without @CircuitBreaker)
    public InventoryResponse recoverFromRetry(RestClientException ex, Long productId, int quantity) {
        log.error("All 3 retry attempts failed for product={}: {}", productId, ex.getMessage());
        return InventoryResponse.failed(productId, "RETRY_EXHAUSTED");
    }
}
```

**Circuit Breaker State Machine:**
```
                    failure_rate > 50%
         ┌────────────────────────────────────────────────────┐
         │                                                    │
         ▼                                                    │
    [CLOSED]  ←───── success calls ─────── [HALF_OPEN]     [OPEN]
    Normal                                 5 test calls       │
    Operation                              sent through        │
    All calls                              ↓ if 50% fail:     │
    go through                             [OPEN] again       │
         │                                 ↓ if < 50% fail:   │
         │                                 [CLOSED]            │
         │                                                    │
         └──────────── wait 30s ──────────────────────────────►[HALF_OPEN]

Call routing:
  CLOSED: All calls pass through → track results in sliding window
  OPEN: ALL calls immediately return CallNotPermittedException → fallback!
         (No real calls to inventory service — protects from cascade failure)
  HALF_OPEN: 5 test calls pass through → determine if service recovered
```

**Metrics & Monitoring:**
```java
// Expose circuit breaker metrics to Prometheus (via Spring Actuator)
// management.endpoints.web.exposure.include=health,metrics,prometheus
// Metrics auto-registered when spring-boot-starter-actuator + resilience4j-micrometer on classpath

// Prometheus metrics available:
// resilience4j_circuitbreaker_state{name="inventoryService"} — current state (0=closed, 1=open, 2=half-open)
// resilience4j_circuitbreaker_failure_rate{name="inventoryService"} — current failure rate %
// resilience4j_circuitbreaker_calls_total{name="inventoryService", kind="successful|failed|not_permitted"}

// Alert: circuit breaker OPEN for > 60 seconds → escalate
```

| Circuit State | Behavior | When Entered | Duration |
|---|---|---|---|
| CLOSED (normal) | All calls pass through | Default / after recovery | Until failure rate > threshold |
| OPEN (tripping) | All calls return exception immediately | Failure rate > threshold | `waitDurationInOpenState` (30s) |
| HALF_OPEN (testing) | Limited test calls pass through | After wait duration | Until test calls succeed/fail |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If `@Retryable` and `@CircuitBreaker` are both on the same method, which wraps which — and does retry run before or after the circuit breaker check?"
- **Winning Answer**: "The order depends on AOP advice ordering. By default: **circuit breaker wraps the retry**. The call flow is: `CircuitBreaker Aspect → Retry Aspect → Actual Method`. This means: (1) Circuit breaker checks state first — if OPEN, immediately throws `CallNotPermittedException` without any retries. (2) If circuit is CLOSED, control passes to `@Retryable` which attempts the call up to `maxAttempts` times. (3) Each retry attempt that fails is recorded by the circuit breaker's sliding window. (4) If all retries exhaust, the final exception triggers the circuit breaker to recalculate the failure rate. This is the correct order — no point retrying if the circuit is already known to be open."

---

#### Q16: Spring Data JPA — `@Transactional(readOnly = true)` and Query Optimization

##### 1. Exact Scenario & Question
Your read-heavy product catalog service runs the same 3 queries for every API request, causing N+1 problems, unnecessary Hibernate dirty checking overhead, and suboptimal database connection usage. Implement `@Transactional(readOnly = true)`, explain every optimization it enables, fix the N+1 query with `JOIN FETCH`, and implement pagination with proper `countQuery`.

##### 2. What the Interviewer Evaluates
- Understanding that `readOnly = true` does three things: sets JDBC `setReadOnly(true)` on connection, disables Hibernate dirty checking (no snapshot comparison on commit), sets Hibernate `FlushMode.NEVER` (no automatic flush before queries).
- Knowledge of N+1 problem and `JOIN FETCH` vs `@EntityGraph` solutions.
- Ability to implement `Pageable` with a separate `countQuery` to avoid expensive counts on joins.

##### 3. Standout Technical Answer

```java
@Service
@Transactional(readOnly = true)  // Default for ALL methods — override with @Transactional for writes
@RequiredArgsConstructor
public class ProductCatalogService {

    private final ProductRepository productRepository;

    // readOnly = true applies to this method (inherited from class)
    public Page<ProductDTO> listProducts(Pageable pageable, ProductFilter filter) {
        Page<Product> products = productRepository.findByFilter(filter, pageable);
        // ✅ readOnly = true optimizations active:
        // 1. JDBC: connection.setReadOnly(true) → DB may use read replica, optimize locks
        // 2. Hibernate: NO snapshot of every entity loaded (saves ~50MB heap on 100k rows)
        // 3. Hibernate: FlushMode.NEVER → no dirty check before queries
        // 4. PostgreSQL: uses snapshot isolation without write intent lock
        return products.map(ProductDTO::from);
    }

    // WRITE operation — explicitly override with full @Transactional
    @Transactional  // Overrides class-level readOnly = true
    public Product updatePrice(Long productId, BigDecimal newPrice) {
        Product product = productRepository.findById(productId).orElseThrow();
        product.setPrice(newPrice);
        // Hibernate dirty checking IS active for this method
        // Session will flush (UPDATE products SET price = ? WHERE id = ?)
        return product;  // No need for productRepository.save() — dirty checking handles it!
    }

    // ❌ N+1 Problem — WRONG approach
    public List<ProductWithCategoryDTO> getAllProductsWithCategoryNPLUS1() {
        List<Product> products = productRepository.findAll();  // 1 query: SELECT * FROM products
        return products.stream()
            .map(p -> new ProductWithCategoryDTO(
                p.getId(),
                p.getName(),
                p.getCategory().getName()  // N queries: SELECT * FROM categories WHERE id = ?
                // For 1000 products → 1001 SQL queries! N+1!
            ))
            .toList();
    }

    // ✅ JOIN FETCH — solves N+1 with 1 query
    public List<ProductWithCategoryDTO> getAllProductsWithCategory() {
        return productRepository.findAllWithCategory()  // 1 query with JOIN
            .stream()
            .map(ProductWithCategoryDTO::from)
            .toList();
    }

    // ✅ Pagination — important: don't load all products into memory!
    public Page<ProductSummaryDTO> searchProducts(String searchTerm, Pageable pageable) {
        return productRepository.searchByName(searchTerm, pageable)
            .map(ProductSummaryDTO::from);
    }
}

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // JOIN FETCH to solve N+1 — category loaded in same query as products
    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.active = true ORDER BY p.name")
    List<Product> findAllWithCategory();

    // Pagination with custom countQuery (avoid joining for count — expensive!)
    @Query(
        value = "SELECT p FROM Product p JOIN FETCH p.category WHERE p.name LIKE %:name%",
        countQuery = "SELECT COUNT(p) FROM Product p WHERE p.name LIKE %:name%"  // Cheaper count
    )
    Page<Product> searchByName(@Param("name") String name, Pageable pageable);

    // Dynamic filter query using Specification
    @Query("SELECT p FROM Product p WHERE " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "p.active = true")
    Page<Product> findByFilter(
        @Param("categoryId") Long categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );

    // DTO projection — loads only needed fields, no entity hydration!
    @Query("SELECT new com.example.dto.ProductSummaryDTO(p.id, p.name, p.price) " +
           "FROM Product p WHERE p.active = true")
    List<ProductSummaryDTO> findAllSummaries();

    // @EntityGraph alternative to JOIN FETCH (named graph)
    @EntityGraph(attributePaths = {"category", "images", "tags"})
    Optional<Product> findWithDetailsById(Long id);
}
```

**`readOnly = true` — The Three Optimizations:**
```
1. JDBC level: connection.setReadOnly(true)
   → Some databases (PostgreSQL) route to read replica automatically
   → Database can use weaker locking (shared locks vs exclusive locks)

2. Hibernate level: FlushMode = MANUAL (never auto-flush)
   → Normal: Hibernate flushes (runs UPDATE/INSERT) before every query
   → readOnly: No flush → no spurious DB writes even if entity is accidentally modified
   → Saves 5-15ms per request on large transactions with many entities

3. Hibernate level: Dirty checking disabled on commit
   → Normal: Hibernate compares current state of every loaded entity with its snapshot
   → For 1000 loaded entities: compares 1000 pairs of objects → significant CPU & heap
   → readOnly: No snapshot taken → no comparison → saves heap and CPU
   → Critical for batch read operations loading thousands of records
```

| Optimization | Without `readOnly` | With `readOnly` |
|---|---|---|
| Dirty checking | ALL entities snapshotted and compared | No snapshots, no comparison |
| Flush mode | `AUTO` (flush before queries) | `NEVER` |
| JDBC hint | Standard connection | `setReadOnly(true)` → potential replica routing |
| Accidental write detection | No (writes silently executed) | FlushMode.NEVER prevents accidental writes |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "You use `@Transactional(readOnly = true)` but then call a `@Modifying @Query` to update records. What happens?"
- **Winning Answer**: "With `FlushMode.NEVER` from `readOnly = true`, Hibernate does not flush automatically. However, `@Modifying` queries go directly to the database via JDBC `executeUpdate()`, bypassing Hibernate's session entirely. The `@Modifying` query DOES execute and modifies the database. But Hibernate's first-level cache (session cache) still holds the old entity versions. If you subsequently load the same entity in the same transaction, you get the stale cached version, not the updated DB row. Fix: add `clearAutomatically = true` to `@Modifying` to clear the session cache after the update: `@Modifying(clearAutomatically = true, flushBeforeExecution = true)`."

---

#### Q17: Spring Boot Actuator — Health Probes for Kubernetes Production

##### 1. Exact Scenario & Question
Your Kubernetes deployment restarts pods unnecessarily when they're processing long batch jobs, and the readiness probe fails during Spring context startup (before the app is ready), causing premature 503 errors during rolling updates. Configure Spring Actuator health probes for Kubernetes liveness and readiness, implement a custom health indicator for a critical Redis dependency, and explain the difference between liveness and readiness in Kubernetes.

##### 3. Standout Technical Answer

```yaml
# application.yml — Kubernetes-optimized Actuator configuration
management:
  endpoint:
    health:
      probes:
        enabled: true  # Enables /actuator/health/liveness and /actuator/health/readiness
      show-details: always  # Or "when-authorized" for production
      group:
        liveness:
          include:  # Liveness: "is the JVM alive and not deadlocked?"
            - livenessState
            - diskSpace    # Low disk = potential crash soon
            # DO NOT include DB/Redis here — DB down ≠ pod needs restart
        readiness:
          include:  # Readiness: "is this pod ready to serve traffic?"
            - readinessState
            - db           # DB unreachable → stop sending traffic to this pod
            - redis        # Redis unreachable → stop sending traffic
            - customCheck  # Your custom health indicator
  endpoints:
    web:
      exposure:
        include: health, metrics, prometheus, info
  health:
    redis:
      enabled: true
    db:
      enabled: true
```

```yaml
# kubernetes deployment.yaml — Probe configuration
spec:
  containers:
    - name: order-service
      livenessProbe:
        httpGet:
          path: /actuator/health/liveness
          port: 8080
        initialDelaySeconds: 0    # startupProbe handles initial delay
        periodSeconds: 10
        failureThreshold: 3       # 3 failures (30s) → restart pod
        successThreshold: 1
        timeoutSeconds: 5
      readinessProbe:
        httpGet:
          path: /actuator/health/readiness
          port: 8080
        initialDelaySeconds: 0
        periodSeconds: 5
        failureThreshold: 3       # 3 failures → remove from Service endpoints (stop traffic)
        successThreshold: 1
        timeoutSeconds: 3
      startupProbe:               # Handles slow startup (up to 5 minutes)
        httpGet:
          path: /actuator/health/liveness
          port: 8080
        failureThreshold: 30      # 30 × 10s = 300 seconds max startup time
        periodSeconds: 10
        # Liveness + Readiness probes do NOT start until startupProbe succeeds!
```

```java
// Custom Health Indicator for Redis with circuit breaker awareness
@Component("redis")  // Name matches management.endpoint.health.group.readiness.include
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {
    private final RedisTemplate<String, Object> redisTemplate;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public Health health() {
        try {
            // Test Redis connectivity with a short timeout
            String pong = redisTemplate.execute(
                RedisServerCommands::ping
            );

            CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("inventoryService");
            CircuitBreaker.State cbState = cb.getState();

            if ("PONG".equals(pong)) {
                return Health.up()
                    .withDetail("latency", measureLatency())
                    .withDetail("inventoryCircuit", cbState.name())
                    .build();
            }
            return Health.down().withDetail("response", pong).build();

        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("type", e.getClass().getSimpleName())
                .build();
            // Returns DOWN → readiness probe fails → pod removed from Service endpoints
            // Traffic goes to healthy pods with Redis connectivity!
        }
    }

    private long measureLatency() {
        long start = System.nanoTime();
        redisTemplate.execute(RedisServerCommands::ping);
        return (System.nanoTime() - start) / 1_000_000;  // Convert to ms
    }
}

// Custom application readiness indicator (e.g., warm up cache first)
@Component
public class CacheWarmupReadinessIndicator implements ApplicationListener<AvailabilityChangeEvent<?>> {
    private final AtomicBoolean cacheWarmedUp = new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCache() {
        log.info("Warming up cache before accepting traffic...");
        productCatalogService.loadAllProducts();  // Could take 30-60 seconds
        cacheWarmedUp.set(true);
        // Signal readiness explicitly
        AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.ACCEPTING_TRAFFIC);
        log.info("Cache warmed up — pod is now READY to accept traffic!");
    }
}
```

**Liveness vs Readiness — The Critical Distinction:**
```
LIVENESS probe answers: "Is this pod ALIVE? Should it be RESTARTED?"
  - Failure action: Kubernetes KILLS and RESTARTS the pod
  - Use for: deadlocks, infinite loops, JVM hung, OOM
  - Include: livenessState, disk space
  - DO NOT include: external dependencies (DB, Redis, Kafka)
    Why? DB goes down → all liveness probes fail → ALL pods restart → makes things WORSE!

READINESS probe answers: "Is this pod READY to serve traffic?"
  - Failure action: Kubernetes REMOVES pod from Service endpoints (no new traffic)
  - Pod is NOT killed — it stays running and might recover!
  - Use for: DB connection lost, warm-up not complete, Redis unavailable, circuit open
  - Include: all external dependencies

STARTUP probe answers: "Has the application finished starting?"
  - Failure action: After failureThreshold failures → pod is killed (never started correctly)
  - Disables liveness + readiness probes until it succeeds!
  - Use for: apps with slow JVM startup (Spring Boot 3 with many beans: 30-120s)
```

| Probe | Failure Action | Include External Deps | Use For |
|---|---|---|---|
| Liveness | Restart pod | ❌ No | JVM health, deadlock detection |
| Readiness | Remove from load balancer | ✅ Yes | DB, cache, downstream service health |
| Startup | Kill pod (if never succeeds) | ⚠️ Minimal | Slow startup apps |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Your liveness probe is configured to hit `/actuator/health`. A transient PostgreSQL connection failure causes all pods' liveness probes to return DOWN. What happens?"
- **Winning Answer**: "If the DB check is in the `liveness` group, all 10 pods fail their liveness probes simultaneously. After `failureThreshold × periodSeconds` seconds, Kubernetes restarts all 10 pods simultaneously. But the pods can't reconnect to PostgreSQL during restart (DB is still recovering). All 10 pods fail their liveness probes again after restart → all 10 restarted again → restart loop. This cascades until either the DB recovers or someone manually intervenes. This is the exact scenario that caused the AWS Aurora outage cascade at several companies. **The fix**: NEVER include database or external service checks in the liveness probe. Put DB/Redis/Kafka checks in the readiness probe. Readiness failure removes pods from load balancer but does NOT restart them — pods wait for the DB to recover and automatically rejoin when healthy."

---

#### Q18: GraalVM Native Image with Spring Boot 3 — Compilation, Limitations & AOT

##### 1. Exact Scenario & Question
Your team wants to deploy Spring Boot microservices as GraalVM Native Images to reduce Kubernetes pod startup time from 8 seconds to 50ms and memory footprint from 512MB to 80MB. Explain the AOT (Ahead-of-Time) compilation pipeline, the 5 categories of code that require explicit Native Hints, and the 3 main limitations of native images for Spring applications.

##### 3. Standout Technical Answer

```
GraalVM Native Image Build Pipeline (Spring Boot 3):

Source Code + Spring Context
         │
         ▼ Spring AOT Processor (runs at build time, not runtime)
    spring-aot-maven-plugin (native profile)
    ├─ Generates Java source for all @Configuration classes
    │   (replaces CGLIB proxies with direct code — no CGLIB at runtime!)
    ├─ Generates BeanDefinition registrations as Java code
    │   (replaces classpath scanning — no reflection at runtime!)
    ├─ Generates proxy hints for JDK Dynamic Proxies and CGLIB
    ├─ Generates reflect-config.json (what classes to include)
    ├─ Generates resource-config.json (resources to bundle)
    └─ Generates proxy-config.json
         │
         ▼ GraalVM native-image compiler
    Ahead-of-Time Compilation:
    ├─ Closed-world assumption: ALL code paths known at build time
    ├─ Dead code elimination (unused classes removed from binary)
    ├─ Points-to analysis: traces all reachable code
    ├─ Generates machine code (ELF binary)
    └─ Output: single native binary (~50-100MB)
         │
         ▼ Runtime
    0 JVM startup, 0 JIT compilation
    Instant startup: 50-200ms (vs 5-15s JVM startup)
    Low memory: 50-100MB RSS (vs 300-600MB JVM RSS)
```

```java
// ✅ Providing Native Hints for code that uses reflection
@Configuration
@ImportRuntimeHints(OrderServiceRuntimeHints.class)
public class NativeHintsConfig { }

public class OrderServiceRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

        // 1. Reflection hints — for Jackson deserialization, custom reflection
        hints.reflection()
            .registerType(OrderEvent.class,
                hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                                         MemberCategory.INVOKE_PUBLIC_METHODS))
            .registerType(PaymentResult.class,
                hint -> hint.withMembers(MemberCategory.DECLARED_FIELDS));

        // 2. Resource hints — for classpath resources loaded at runtime
        hints.resources()
            .registerPattern("templates/*.html")
            .registerPattern("i18n/*.properties");

        // 3. Proxy hints — for JDK dynamic proxies
        hints.proxies()
            .registerJdkProxy(OrderRepository.class);  // Spring Data JPA interface

        // 4. Serialization hints — for Java serialization (rare)
        hints.serialization()
            .registerType(OrderStatusEvent.class);
    }
}

// ✅ @NativeHint via @Reflective annotation (simpler for individual classes)
@Reflective  // Registers this class's constructor and methods for reflection
public class CustomDeserializer extends JsonDeserializer<Order> {
    @Override
    public Order deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        return new Order(p.getValueAsString());
    }
}
```

**5 Categories Requiring Explicit Native Hints:**
```
1. REFLECTION: Classes instantiated via Class.forName() or reflection API
   - Jackson deserializers, Hibernate entity classes, custom factories
   - Fix: @Reflective, RuntimeHints.reflection()

2. RESOURCES: Files loaded via ClassLoader.getResourceAsStream() at runtime
   - SQL migration scripts, email templates, i18n files
   - Fix: RuntimeHints.resources().registerPattern("*.sql")

3. JDK DYNAMIC PROXIES: Interfaces proxied at runtime
   - Spring Data repositories, MyBatis mappers
   - Fix: RuntimeHints.proxies().registerJdkProxy(MyInterface.class)

4. SERIALIZATION: Java object serialization (ObjectOutputStream)
   - Distributed caches with Java serialization, RMI
   - Fix: RuntimeHints.serialization().registerType(...)

5. INITIALIZATION: Classes that must initialize at build time vs runtime
   - Static initializers with time-sensitive operations
   - Fix: @NativeHint(initialization = @InitializationHint(types = ..., phase = BUILD_TIME))
```

**3 Main Limitations of GraalVM Native Images:**

```
Limitation 1: No Dynamic Class Loading
  ❌ Class.forName("com.example.Plugin" + pluginName) fails at runtime
  ❌ Groovy/BeanShell dynamic scripting
  ❌ OSGi plugin systems
  ✅ Workaround: Use Spring's @ConditionalOnClass (evaluated at build time)

Limitation 2: No Reflection Without Hints
  ❌ Any library using reflection without native hints breaks
  ❌ Many older libraries (before native-image support) require manual hints
  ❌ Build-time errors are hard to debug (class not found at runtime)
  ✅ Workaround: RuntimeHints API, GraalVM agent (-agentlib:native-image-agent) auto-gen hints

Limitation 3: Build Time is 5-15 Minutes
  ❌ 8GB RAM, 8 CPU cores needed for native compilation
  ❌ Cannot use JFR / JVM profiling (GraalVM profiler needed)
  ❌ JVM optimizations (JIT, OSR) not available at runtime
  ✅ Workaround: Multi-stage Docker builds with GraalVM builder image
```

```dockerfile
# Multi-stage native image build
FROM ghcr.io/graalvm/native-image:21 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -Pnative native:compile -DskipTests  # Takes 8-15 minutes!

FROM debian:12-slim  # Minimal runtime — just the native binary!
WORKDIR /app
COPY --from=builder /build/target/order-service ./order-service
EXPOSE 8080
ENTRYPOINT ["./order-service"]
# Final image: ~80MB, starts in 50ms, uses 80MB RAM
```

| Metric | JVM (Spring Boot 3) | Native Image (GraalVM) |
|---|---|---|
| Startup time | 3-15 seconds | 50-200ms |
| RSS memory | 300-600MB | 50-150MB |
| Peak throughput | Higher (JIT optimized) | Slightly lower (no JIT) |
| Build time | 30s | 8-15 minutes |
| Docker image size | 300-700MB | 50-150MB |
| Dynamic class loading | ✅ Yes | ❌ No |
| JVM profiling (JFR) | ✅ Yes | ⚠️ Limited |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "You're using CGLIB for Spring AOP in your native image. Will it work?"
- **Winning Answer**: "Not by default. CGLIB generates subclasses **at runtime** using bytecode manipulation — but native images use a closed-world model where no new classes can be generated at runtime. Spring Boot 3's AOT processing solves this by **generating the CGLIB proxy classes at build time**. The `spring-aot-maven-plugin` analyzes all `@Configuration` classes and pre-generates their CGLIB subclasses as plain `.class` files, which are compiled into the native binary. For application code, Spring handles this automatically. For third-party libraries that use CGLIB at runtime (outside Spring's purview), you need to add `@ReflectiveProxy` hints or switch to interface-based JDK dynamic proxies where possible."

---

#### Q19: Spring Boot Graceful Shutdown — Zero Requests Dropped During Deployment

##### 1. Exact Scenario & Question
Your Kubernetes rolling update drops 50-100 requests every deployment because when the new pod starts, the old pod receives a `SIGTERM` and stops accepting connections before in-flight requests complete. Additionally, after receiving `SIGTERM`, your service's `@Scheduled` jobs still run and try to use a closed DB connection pool. Configure truly zero-drop graceful shutdown.

##### 3. Standout Technical Answer

```yaml
# application.yml — Graceful shutdown configuration
server:
  shutdown: graceful  # Spring Boot waits for in-flight requests to complete

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s  # Max wait time for in-flight requests

# What happens on SIGTERM:
# 1. Tomcat/Netty stops accepting new connections (TCP backlog closed)
# 2. In-flight requests are allowed to complete (up to 30s)
# 3. @PreDestroy and DisposableBean.destroy() called on all beans (in reverse order)
# 4. HikariCP pool closes all connections
# 5. JVM exits with code 0
```

```yaml
# kubernetes deployment.yaml — preStop hook for probe propagation delay
spec:
  containers:
    - name: order-service
      lifecycle:
        preStop:
          exec:
            command: ["/bin/sh", "-c", "sleep 10"]
            # preStop runs BEFORE SIGTERM is sent!
            # 10 second sleep gives kube-proxy time to remove pod from iptables rules
            # After 10s: SIGTERM sent → Spring graceful shutdown begins → 30s window

      # Total termination time: 10s (preStop) + 30s (graceful) = 40s
      # terminationGracePeriodSeconds must be > preStop + graceful shutdown time!

  terminationGracePeriodSeconds: 60  # 60s > 10 (preStop) + 30 (graceful)
```

```java
// Stop @Scheduled jobs gracefully on SIGTERM
@Configuration
public class ScheduledJobGracefulShutdownConfig {

    // By default, ThreadPoolTaskScheduler waits for running jobs
    @Bean
    public TaskSchedulerCustomizer taskSchedulerCustomizer() {
        return scheduler -> {
            scheduler.setWaitForTasksToCompleteOnShutdown(true);  // ← Wait for running jobs!
            scheduler.setAwaitTerminationSeconds(30);             // Max wait: 30s
        };
    }
}

@Component
@Slf4j
public class InventorySync {

    // This job stops gracefully — running instance allowed to complete (up to 30s)
    @Scheduled(fixedRate = 60_000)
    public void syncInventory() {
        log.info("Starting inventory sync...");
        // If SIGTERM arrives while this is running:
        // - waitForTasksToCompleteOnShutdown = true → this run completes
        // - No NEW execution starts after SIGTERM
        inventoryService.performSync();
        log.info("Inventory sync complete");
    }
}

// Monitor graceful shutdown events
@Component
@Slf4j
public class ShutdownLogger implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("ApplicationContext closing — shutdown sequence initiated");
        // Log active request count, in-flight jobs, etc.
    }
}

// SmartLifecycle for custom shutdown ordering and timeout
@Component
public class KafkaConsumerLifecycle implements SmartLifecycle {
    private volatile boolean running = false;

    @Override
    public void start() {
        kafkaListenerEndpointRegistry.start();
        running = true;
    }

    @Override
    public void stop(Runnable callback) {
        log.info("Stopping Kafka consumers gracefully...");
        // Stop all Kafka consumers (stop polling, wait for current messages to process)
        kafkaListenerEndpointRegistry.stop(callback);
        // callback.run() signals Spring that this lifecycle bean is done
        running = false;
    }

    @Override
    public boolean isRunning() { return running; }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 100;  // Stop Kafka BEFORE most other beans
        // Higher phase number = starts later, STOPS FIRST
    }
}
```

**The Complete Zero-Drop Shutdown Sequence:**
```
t=0s:   Kubernetes sends SIGTERM
t=0s:   preStop hook starts: sleep 10 (kube-proxy removing pod from iptables)
t=10s:  preStop complete → SIGTERM actually delivered to JVM
t=10s:  Spring SmartLifecycle beans stop in reverse phase order:
         - Kafka consumer stops (phase MAX-100)
         - Scheduled tasks: no new runs start
         - HTTP server: stops accepting NEW connections
t=10s+: In-flight requests continue processing (up to 30s)
t=40s:  All in-flight requests complete (or 30s limit hit)
t=40s:  @PreDestroy callbacks fire
t=40s:  HikariCP closes all database connections
t=41s:  JVM exits (total: ~41s, well within 60s terminationGracePeriodSeconds)
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If a `@Scheduled` job is running when `SIGTERM` arrives, and it's in the middle of a `@Transactional` database operation, what happens to the partially completed transaction?"
- **Winning Answer**: "With `setWaitForTasksToCompleteOnShutdown(true)`, the scheduler waits up to `awaitTerminationSeconds` for the job to complete normally. If the job completes before the timeout: `@Transactional` commits normally. If the timeout is exceeded: the job thread is interrupted, the `@Transactional` catches `InterruptedException` or `ThreadInterruptedException`, the `PlatformTransactionManager` rolls back the incomplete transaction (because an exception propagated or the transaction was not marked for commit), and Spring continues shutdown. The database rolls back the partial operation — data consistency is maintained."

---

#### Q20: Spring Security Filter Chain — Request Processing & Custom Filters

##### 1. Exact Scenario & Question
You need to implement: (1) request ID propagation (add a `X-Request-ID` header to all requests and include it in all log statements via MDC), (2) rate limiting by IP address, and (3) JWT validation. Explain Spring Security's filter chain architecture and implement all three as proper `OncePerRequestFilter` implementations with correct ordering.

##### 3. Standout Technical Answer

```java
// Request ID + MDC propagation filter
@Component
@Order(1)  // Must run FIRST — before any logging occurs
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = Optional.ofNullable(request.getHeader(REQUEST_ID_HEADER))
            .filter(h -> h.matches("[a-zA-Z0-9-]{1,64}"))  // Validate — prevent header injection!
            .orElse(UUID.randomUUID().toString());

        // Add to MDC — all log statements in this request now include requestId!
        MDC.put(MDC_KEY, requestId);

        // Add to response so client can correlate
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);  // ← CRITICAL: Clean up MDC after request (prevent leaks!)
        }
    }
}

// Rate limiting filter
@Component
@Order(2)
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {
    private final RateLimiterRegistry rateLimiterRegistry;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = extractClientIp(request);

        // Create or get rate limiter per IP (50 requests / minute per IP)
        RateLimiter limiter = rateLimiterRegistry.rateLimiter(clientIp,
            RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .limitForPeriod(50)
                .timeoutDuration(Duration.ZERO)  // Don't wait — reject immediately if over limit
                .build());

        if (limiter.acquirePermission()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", "60");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(),
                Map.of("error", "Rate limit exceeded", "retryAfter", 60));
            // Do NOT call filterChain.doFilter() — request is terminated!
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();  // First IP in chain = original client
        }
        return request.getRemoteAddr();
    }
}

// Spring Security filter chain configuration
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final RequestIdFilter requestIdFilter;
    private final RateLimitingFilter rateLimitingFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Add custom filters BEFORE Spring Security's filters
            .addFilterBefore(requestIdFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/actuator/health/**", "/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder()))
                .authenticationEntryPoint((request, response, ex) -> {
                    // Custom 401 response (instead of default redirect to login)
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"requestId\":\""
                        + MDC.get("requestId") + "\"}");
                })
            )
            .build();
    }
}
```

**Spring Security Filter Chain — Ordered Processing:**
```
HTTP Request
     │
     ▼ Filter 1: RequestIdFilter (Order=1) — sets MDC requestId
     ▼ Filter 2: RateLimitingFilter (Order=2) — reject if over limit
     ▼ Filter 3: CorsFilter — sets CORS headers
     ▼ Filter 4: SecurityContextPersistenceFilter — loads SecurityContext
     ▼ Filter 5: UsernamePasswordAuthenticationFilter — form login (disabled for JWT)
     ▼ Filter 6: BearerTokenAuthenticationFilter — validates JWT
     ▼ Filter 7: AuthorizationFilter — enforces access rules
     │
     ▼ Controller (@RestController)
     │
     ▼ Response flows back UP through all filters in reverse order
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why must `MDC.remove(MDC_KEY)` be in a `finally` block — and what happens if you forget it?"
- **Winning Answer**: "Tomcat uses a **thread pool** — threads are reused across many requests. Without `MDC.remove()` in `finally`, the `requestId` from Request 1 remains in the MDC map when the thread is returned to the pool. When Request 2 is assigned the same thread, its log statements inherit Request 1's `requestId` — a false correlation. Under heavy load, this causes log analysis tools to incorrectly attribute Request 2's errors to Request 1's `requestId`, making distributed tracing completely unreliable. Worse, with virtual threads (Java 21), the thread may be mounted on a different carrier thread — MDC's `ThreadLocal` implementation may not carry over correctly. Use `MDC.clear()` or `MDC.remove()` in `finally` for every key set in the filter."

---

### Tier 4: Spring Type Conversion, Formatting & Web Data Binding (Q21 – Q26)

---

#### Q21: Spring Conversion SPI Architecture — `Converter<S, T>` vs `ConverterFactory<S, R>` vs `GenericConverter` vs `ConditionalGenericConverter`

##### 1. Exact Scenario & Question
"Your team is designing a multi-tenant payment gateway where API requests pass country codes, currency pairs, and polymorphic payment identifiers in HTTP path and query parameters. How does Spring's Conversion SPI organize type conversion across its 4 core interfaces, and when should you choose `ConverterFactory` or `GenericConverter` over standard `Converter`?"

##### 2. What the Interviewer Evaluates
- Deep technical mastery of `org.springframework.core.convert` SPI.
- Understanding 1-to-1 vs 1-to-hierarchy vs N-to-N type mappings.
- Contextual conversion via `TypeDescriptor` and conditional predicate evaluation.
- Thread safety and performance in high-concurrency web engines.

##### 3. Standout Technical Answer
Spring Framework 3+ replaced the legacy JavaBeans `PropertyEditor` mechanism with a modern, strongly-typed, and stateless conversion subsystem centered around **`ConversionService`**. The SPI provides 4 progressive abstraction tiers:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                          SPRING CONVERSION SPI HIERARCHY                               │
├────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                        │
│  1. Converter<S, T>                   (Direct 1-to-1 conversion between two types)     │
│         │                                                                              │
│  2. ConverterFactory<S, R>            (1-to-Hierarchy: Converts S to any subtype of R) │
│         │                                                                              │
│  3. GenericConverter                  (N-to-N: Full access to source/target metadata)  │
│         │                                                                              │
│  4. ConditionalGenericConverter       (GenericConverter + boolean conditional guard)   │
│                                                                                        │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

**1. `Converter<S, T>` (The Standard 1-to-1 Workhorse):**
Converts a single source type `S` into target type `T`. Pure, simple, and stateless:
```java
@Component
public class StringToIsoCountryCodeConverter implements Converter<String, IsoCountryCode> {
    @Override
    public IsoCountryCode convert(String source) {
        if (source == null || source.isBlank()) return null;
        return IsoCountryCode.fromAlpha2(source.trim().toUpperCase());
    }
}
```

**2. `ConverterFactory<S, R>` (1-to-Hierarchy Polymorphic Converter):**
When you need to convert a source type to an entire hierarchy of classes (such as converting `String` to *any* Java `Enum` in your application), implementing separate `Converter` classes for 50 enums causes severe code duplication. `ConverterFactory` dynamically returns a specialized converter:
```java
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {
    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }

    private static class StringToEnumConverter<T extends Enum> implements Converter<String, T> {
        private final Class<T> enumType;
        public StringToEnumConverter(Class<T> enumType) { this.enumType = enumType; }

        @Override
        public T convert(String source) {
            if (source == null || source.isBlank()) return null;
            return (T) Enum.valueOf(this.enumType, source.trim().toUpperCase());
        }
    }
}
```

**3. `GenericConverter` & `ConditionalGenericConverter` (Complex Context-Aware Conversion):**
When conversion requires inspecting annotations on the target field or handling multiple collection types, `GenericConverter` provides rich `TypeDescriptor` metadata:
```java
public class MaskedStringConverter implements ConditionalGenericConverter {
    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        // Only trigger if target field is annotated with @Masked
        return targetType.hasAnnotation(Masked.class);
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(new ConvertiblePair(String.class, String.class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) return null;
        String raw = (String) source;
        return "****-****-****-" + raw.substring(Math.max(0, raw.length() - 4));
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Is a Spring `Converter<S, T>` thread-safe, and what happens if you inject a prototype or request-scoped dependency inside it?"
- **Winning Answer**: "All converters registered with Spring's `ConversionService` (e.g. `GenericConversionService`) are stored as **shared singletons** inside a concurrent hash map (`Map<ConvertiblePair, ConvertersForPair>`). A single converter instance is invoked concurrently across all Tomcat worker threads. Therefore, a converter **MUST be strictly stateless and thread-safe**. If you inject mutable state into an instance field of a Converter, concurrent web requests will suffer non-deterministic data races. If you need request context (such as tenant ID or client Locale), access it dynamically through `RequestContextHolder.getRequestAttributes()` or `LocaleContextHolder.getLocale()`, never through mutable instance fields!"

---

#### Q22: Spring `ConversionService` vs Legacy JavaBeans `PropertyEditor` — Why `PropertyEditor` Broke Concurrent High-Scale Apps

##### 1. Exact Scenario & Question
"During an architectural review of a legacy Spring MVC application migrating to Spring Boot 3, an engineer asks why older controllers use `@InitBinder` with `CustomDateEditor` while newer microservices use `ConversionService`. What was fundamentally flawed with the `PropertyEditor` model in concurrent environments, and how does `ConversionService` solve it?"

##### 2. What the Interviewer Evaluates
- Understanding thread safety history in Java web applications.
- JavaBeans specification architectural limitations.
- Stateful vs stateless design patterns and their impact on JVM garbage collection.
- Clean separation between formatting and general type conversion.

##### 3. Standout Technical Answer

| Architectural Aspect | Legacy `PropertyEditor` (JavaBeans Standard) | Modern Spring `ConversionService` (Spring 3+) |
| :--- | :--- | :--- |
| **Package** | `java.beans.PropertyEditorSupport` (JDK standard) | `org.springframework.core.convert.ConversionService` |
| **Statefulness** | **Stateful**: Stores converted value in internal field `private Object value` | **Stateless**: Pure input $\to$ output transformation method `T convert(S)` |
| **Thread Safety** | **Fundamentally Thread-Unsafe**: Cannot be shared across threads | **100% Thread-Safe**: Singleton shared across all worker threads |
| **Instantiation Lifecycle**| Instantiated **per HTTP request** inside controller `@InitBinder` | Instantiated **once** at application startup as singleton Spring bean |
| **JVM Memory / GC Pressure**| Extreme Young Generation churn ($100\text{k requests} \Rightarrow 100\text{k editor objects}$) | **Zero per-request allocation**: Singleton reused perpetually |
| **Supported Types** | String $\leftrightarrow$ Object only (`getAsText()` / `setAsText()`) | Any Type $\leftrightarrow$ Any Type ($S \leftrightarrow T$, including collections) |

**The Concurrency Disaster with `PropertyEditor`:**
Because `PropertyEditorSupport` has internal mutable fields:
```java
public class PropertyEditorSupport implements PropertyEditor {
    private Object value; // ❌ MUTABLE INSTANCE STATE!

    public void setValue(Object value) { this.value = value; }
    public Object getValue() { return this.value; }
    public void setAsText(String text) { setValue(parse(text)); }
}
```
If a developer registered a `PropertyEditor` as a singleton Spring bean, Thread A calling `setAsText("2026-09-11")` would be overwritten by Thread B calling `setAsText("1999-01-01")` before Thread A could call `getValue()`, resulting in silent, catastrophic cross-tenant data corruption!

**The Modern `ConversionService` Solution:**
Spring's `ConversionService` enforces functional purity:
```java
public interface Converter<S, T> {
    T convert(S source); // Pure function: No mutable instance state!
}
```
A single `DefaultConversionService` or `ApplicationConversionService` instance is configured at startup, cached in a thread-safe registry, and serves hundreds of thousands of concurrent requests with zero thread contention and zero memory churn.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If `PropertyEditor` is so dangerous, why does Spring MVC still maintain `WebDataBinder` and support `@InitBinder` in Spring 6?"
- **Winning Answer**: "`WebDataBinder` serves a broader purpose than just type conversion. It is the controller-level security barrier and binding coordinator. It handles:
  1. **Mass-Assignment Protection**: Specifying allowed and disallowed fields via `binder.setAllowedFields("username", "email")` and `binder.setDisallowedFields("isAdmin", "roles")`.
  2. **Binding Error Collection**: Aggregating validation and type mismatch errors into `BindingResult` without crashing the request.
  3. **Custom Validator Registration**: Wiring JSR-380 `@Valid` validators per controller.
  While legacy code used `@InitBinder` to register `PropertyEditor`, modern Spring MVC applications configure `ConversionService` globally and use `@InitBinder` strictly for security field filtering and local validator registration."

---

#### Q23: The Dual Web Binding Pipeline: `ConversionService` vs `HttpMessageConverter`

##### 1. Exact Scenario & Question
"A junior developer writes a Spring `@Component public class CustomMoneyConverter implements Converter<String, Money>` to deserialize money values. They observe that it works perfectly when querying `@GetMapping("/item/{price}")` via `@PathVariable`, but completely fails when sending `POST /item` with JSON body `{"price": "100 USD"}` into `@RequestBody ItemDto`. Why does this happen, and how does Spring MVC separate transport binding pipelines?"

##### 2. What the Interviewer Evaluates
- Clear conceptual distinction between `HandlerMethodArgumentResolver` implementations.
- Understanding how Spring MVC dispatches `@RequestParam` / `@PathVariable` vs `@RequestBody`.
- Knowing that Jackson operates completely independently of Spring's `ConversionService`.
- Designing cohesive data mapping across transport boundaries.

##### 3. Standout Technical Answer

Spring MVC uses two fundamentally separate, decoupled processing pipelines depending on the transport mechanism of the incoming data:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                         SPRING MVC DUAL TRANSPORT PIPELINE                             │
├────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                        │
│  [ Incoming HTTP Request ]                                                             │
│         │                                                                              │
│         ├──► Pipeline A: URI Parameters, Query Params, Headers, Form-Data               │
│         │       │                                                                      │
│         │       ▼                                                                      │
│         │    [ PathVariableMethodArgumentResolver / RequestParamMethodArgumentResolver ]│
│         │       │                                                                      │
│         │       ▼                                                                      │
│         │    [ WebDataBinder ] ──► Delegates to ──► [ Spring ConversionService ]       │
│         │                                            (Spring Converter<S, T>)          │
│         │                                                                              │
│         └──► Pipeline B: HTTP Request Body (JSON, XML, Protobuf)                       │
│                 │                                                                      │
│                 ▼                                                                      │
│              [ RequestResponseBodyMethodProcessor ]                                    │
│                 │                                                                      │
│                 ▼                                                                      │
│              [ HttpMessageConverter ]                                                  │
│              (MappingJackson2HttpMessageConverter)                                     │
│                 │                                                                      │
│                 ▼                                                                      │
│              [ Jackson ObjectMapper ] ──► Delegates to ──► [ Jackson Converter / Serde ]│
│                                                            (Jackson StdConverter<IN, OUT)
│                                                                                        │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

1. **Pipeline A (URI & Form Parameters)**:
   - Handled by resolvers like `RequestParamMethodArgumentResolver` and `PathVariableMethodArgumentResolver`.
   - Uses `WebDataBinder`, which consults the shared **`ConversionService`**.
   - Your Spring `Converter<String, Money>` is invoked here!
2. **Pipeline B (HTTP Request Body)**:
   - Handled by `RequestResponseBodyMethodProcessor`.
   - It reads the raw HTTP request `InputStream` and iterates over registered **`HttpMessageConverter`** beans.
   - For `application/json`, it delegates entirely to **`MappingJackson2HttpMessageConverter`**, which hands the payload to **Jackson's `ObjectMapper`**.
   - **Jackson has its own independent serialization engine**. It does NOT check Spring's `ConversionService`!

**The Dual-Symmetric Solution:**
To support `Money` across both query parameters and JSON request bodies:
1. Keep the Spring `Converter<String, Money>` for `@RequestParam` and `@PathVariable`.
2. Add Jackson paired converters or deserializers for `@RequestBody`:
```java
// 1. Spring Converter for @RequestParam / @PathVariable:
@Component
public class StringToMoneyConverter implements Converter<String, Money> {
    @Override
    public Money convert(String source) {
        return Money.parse(source);
    }
}

// 2. Jackson Converter for @RequestBody JSON payloads:
public class JacksonStringToMoneyConverter extends StdConverter<String, Money> {
    @Override
    public Money convert(String source) {
        return (source != null) ? Money.parse(source) : null;
    }
}

public record ItemDto(
    String sku,
    @JsonDeserialize(converter = JacksonStringToMoneyConverter.class)
    Money price
) {}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can you force Jackson's `ObjectMapper` to automatically delegate all unknown conversions to Spring's `ConversionService`?"
- **Winning Answer**: "Yes, by configuring a custom Jackson `Deserializers` modifier or registering Spring's `SpringHandlerInstantiator`. However, in enterprise architecture, **tightly coupling Jackson's serialization engine to Spring's `ConversionService` is considered an anti-pattern**. It introduces subtle ordering bugs during application context startup, creates hidden circular dependencies, and causes performance degradation because Jackson's ultra-optimized token streaming engine is forced to call into Spring reflection abstractions for every field. Keep the transport JSON contract explicit via Jackson annotations or DTO records, and reserve Spring `ConversionService` for HTTP query and form parameter binding."

---

#### Q24: Spring `Formatter<T>` & `AnnotationFormatterFactory<A>` — Locale-Awareness and Metadata-Driven Formatting

##### 1. Exact Scenario & Question
"Enterprise clients across France, Germany, and the US submit financial reports where currency numbers and dates are formatted differently (`1.250,50 €` vs `$1,250.50`). How do you implement locale-aware string-to-object parsing in Spring, and how do you bind custom annotations like `@MaskedCreditCard` or `@CurrencyFormat` using `AnnotationFormatterFactory`?"

##### 2. What the Interviewer Evaluates
- Understanding the architectural boundary between `Converter<S, T>` (general-purpose) and `Formatter<T>` (locale/text-specific).
- Command of `Printer<T>` and `Parser<T>` interfaces.
- Metadata-driven formatting via `AnnotationFormatterFactory`.
- How Spring resolves client `Locale` on every thread via `LocaleContextHolder`.

##### 3. Standout Technical Answer

While `Converter<S, T>` is agnostic to language, region, or display format, **`Formatter<T>`** is specifically designed for client-facing String-to-Object translations that vary by `Locale`:

```java
public interface Formatter<T> extends Printer<T>, Parser<T> {
    String print(T object, Locale locale);
    T parse(String text, Locale locale) throws ParseException;
}
```

**Implementing a Declarative Annotation Formatter Factory:**
To format a domain `MonetaryAmount` using a custom `@CurrencyFormat` annotation:

```java
// 1. Declare Custom Annotation
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrencyFormat {
    String pattern() default "";
}

// 2. Implement AnnotationFormatterFactory
public class CurrencyFormatAnnotationFormatterFactory 
        implements AnnotationFormatterFactory<CurrencyFormat> {

    @Override
    public Set<Class<?>> getFieldTypes() {
        return Set.of(MonetaryAmount.class, BigDecimal.class);
    }

    @Override
    public Printer<?> getPrinter(CurrencyFormat annotation, Class<?> fieldType) {
        return (object, locale) -> {
            NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
            return nf.format(object);
        };
    }

    @Override
    public Parser<?> getParser(CurrencyFormat annotation, Class<?> fieldType) {
        return (text, locale) -> {
            if (text == null || text.isBlank()) return null;
            NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
            Number parsed = nf.parse(text.trim());
            return BigDecimal.valueOf(parsed.doubleValue());
        };
    }
}
```

**Usage in Controller:**
```java
@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {

    @GetMapping("/summary")
    public BillingSummary getSummary(
        @RequestParam("min_amount") 
        @CurrencyFormat 
        BigDecimal minAmount // Automatically parses "$1,250.50" in US or "1.250,50 €" in Germany!
    ) {
        return billingService.findSummary(minAmount);
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "How does Spring know which `Locale` to pass to `Formatter.parse(text, locale)`, and what happens under high load if threads are reused?"
- **Winning Answer**: "Spring MVC resolves the `Locale` at the beginning of each HTTP request via the configured **`LocaleResolver`** (such as `AcceptHeaderLocaleResolver` which reads the HTTP `Accept-Language` header, or `CookieLocaleResolver`). The resolved `Locale` is bound to the current thread via **`LocaleContextHolder`** (backed by a `ThreadLocal` or `NamedInheritableThreadLocal`). When `Formatter.parse()` executes, Spring retrieves the active locale from `LocaleContextHolder.getLocale()`. Crucially, when the request completes, `FrameworkServlet` executes a `finally` block calling `LocaleContextHolder.resetLocaleContext()`. This cleans up the thread state, ensuring that when the thread is returned to Tomcat's worker pool, subsequent requests on that thread do not accidentally inherit a stale client locale."

---

#### Q25: Registering Custom Converters in Spring Boot 3 & Native GraalVM AOT Considerations

##### 1. Exact Scenario & Question
"How do you register custom converters and formatters in a production Spring Boot 3 application, and what critical pitfalls arise when compiling the application to a GraalVM Native Image?"

##### 2. What the Interviewer Evaluates
- Spring Boot 3 auto-configuration mechanics (`FormattingConversionService`).
- `WebMvcConfigurer.addFormatters()` vs component scanning.
- GraalVM Ahead-Of-Time (AOT) closed-world assumption.
- Registering reflection and method introspection hints via `RuntimeHintsRegistrar`.

##### 3. Standout Technical Answer

**Registration in Standard Spring Boot 3:**
Spring Boot provides two primary registration mechanisms:
1. **Implicit Component Scanning**: Annotate your converter with `@Component`. Spring Boot's `WebMvcAutoConfiguration` and `FormattingConversionService` automatically collect all `Converter`, `GenericConverter`, and `Formatter` beans from the `ApplicationContext` during startup.
2. **Explicit WebMvcConfigurer Registration**: Preferred for deterministic ordering and decoupling converters from component scanning:
```java
@Configuration(proxyBeanMethods = false)
public class WebMvcConversionConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToIsoCountryCodeConverter());
        registry.addFormatterForFieldAnnotation(new CurrencyFormatAnnotationFormatterFactory());
    }
}
```

**The GraalVM Native Image Hazard:**
Under GraalVM Native Image compilation, Java code is analyzed ahead-of-time under the **Closed-World Assumption**. 
- `ConverterFactory` implementations (such as `StringToEnumConverterFactory`) dynamically inspect generic type parameters at runtime via reflection: `Class.getDeclaredMethod()`, `Enum.valueOf()`.
- If GraalVM does not detect compile-time references to every enum class accessed dynamically via URL parameters, it strips those classes or their reflection metadata from the final native binary!
- **Symptom**: In JVM mode (`java -jar`), URL parameter binding works seamlessly. In GraalVM native binary mode, the exact same URL throws `NoSuchMethodException` or `IllegalArgumentException: No enum constant`.

**The Permanent Native Fix: `RuntimeHintsRegistrar`:**
```java
public class ConversionRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register reflection hints for all dynamic enum classes used in web converters
        for (Class<?> enumClass : List.of(PaymentStatus.class, OrderType.class, CurrencyCode.class)) {
            hints.reflection().registerType(enumClass, 
                MemberCategory.INVOKE_PUBLIC_METHODS, 
                MemberCategory.DECLARED_FIELDS);
        }
    }
}

// In Configuration:
@Configuration
@ImportRuntimeHints(ConversionRuntimeHints.class)
public class NativeWebConfig {}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why does Spring Boot configure an `ApplicationConversionService` instead of Spring Framework's default `FormattingConversionService`?"
- **Winning Answer**: "`ApplicationConversionService` is Spring Boot's specialized extension of `FormattingConversionService`. It adds built-in formatters and converters required specifically for Spring Boot operations:
  1. **Temporal Duration Parsing**: Converts strings like `"500ms"`, `"10s"`, `"2h"` into `java.time.Duration` using `DurationStyle`.
  2. **Data Size Parsing**: Converts strings like `"10MB"`, `"1GB"` into `org.springframework.util.unit.DataSize`.
  3. **Delimited Collections**: Converts comma-delimited strings (`"admin,user,auditor"`) into `List<String>` or `Set<String>`.
  These converters are vital for parsing `application.yaml` configurations into `@ConfigurationProperties` classes before the web application context is fully booted."

---

#### Q26: End-to-End Enterprise Converter Architecture: The 3 Architectural Tiers

##### 1. Exact Scenario & Question
"Trace an enterprise data transformation end-to-end: A user submits an HTTP request with query parameter `?settlement_date=2026-09-11` and a JSON body `{"amount_cents": 19999}`. The controller processes it, updates a domain entity `Payment(Instant settlementDate, Money amount)`, and persists it into a PostgreSQL database with column `amount_varchar ('199.99 USD')`. Detail which converter runs at each tier and what happens if any tier fails."

##### 2. What the Interviewer Evaluates
- Ability to synthesize the entire application architecture across Web, Transport, Domain, and Database persistence layers.
- Discerning the exact boundaries of Spring `Converter`, Jackson `Converter`, and JPA `AttributeConverter`.
- Error handling, validation boundaries, and transaction rollback mechanics.

##### 3. Standout Technical Answer

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        END-TO-END 3-TIER CONVERSION ARCHITECTURE                       │
├────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                        │
│  [ CLIENT REQUEST ]                                                                    │
│  GET /api/v1/payments?settlement_date=2026-09-11                                       │
│  Content-Type: application/json                                                        │
│  Body: {"amount_cents": 19999}                                                         │
│         │                                                                              │
│         ▼                                                                              │
│  ════════════════════════════════════════════════════════════════════════════════════  │
│  TIER 1: WEB ROUTING BOUNDARY (Spring MVC WebDataBinder)                               │
│  - Executes: Spring Converter<String, LocalDate>                                       │
│  - Transforms: Query string "2026-09-11" ──► LocalDate(2026, 9, 11)                   │
│  - Failure: Throws MethodArgumentTypeMismatchException ──► HTTP 400 Bad Request        │
│  ════════════════════════════════════════════════════════════════════════════════════  │
│         │                                                                              │
│         ▼                                                                              │
│  ════════════════════════════════════════════════════════════════════════════════════  │
│  TIER 2: TRANSPORT BOUNDARY (Jackson MappingJackson2HttpMessageConverter)             │
│  - Executes: Jackson StdConverter<Long, Money>                                         │
│  - Transforms: JSON integer 19999 ──► Money(BigDecimal.valueOf(199.99), USD)          │
│  - Failure: Throws HttpMessageNotReadableException ──► HTTP 400 Bad Request            │
│  ════════════════════════════════════════════════════════════════════════════════════  │
│         │                                                                              │
│         ▼                                                                              │
│  ════════════════════════════════════════════════════════════════════════════════════  │
│  DOMAIN LAYER: Transaction Boundary (@Transactional Service)                          │
│  - Executes business logic, constructs PaymentEntity(settlementInstant, moneyAmount)   │
│  ════════════════════════════════════════════════════════════════════════════════════  │
│         │                                                                              │
│         ▼                                                                              │
│  ════════════════════════════════════════════════════════════════════════════════════  │
│  TIER 3: PERSISTENCE BOUNDARY (Hibernate ORM / Jakarta Persistence)                    │
│  - Executes: JPA AttributeConverter<Money, String>                                    │
│  - Transforms: Domain Money(199.99, USD) ──► PostgreSQL VARCHAR column "199.99 USD"   │
│  - Failure: Throws PersistenceException ──► Rollback DB Tx ──► HTTP 500 Internal Error │
│  ════════════════════════════════════════════════════════════════════════════════════  │
│         │                                                                              │
│         ▼                                                                              │
│  [ POSTGRESQL DATABASE ROW COMMITTED ]                                                 │
│                                                                                        │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

**The Code Blueprint across All 3 Tiers:**

```java
// =========================================================================
// TIER 1: Spring Web Converter (Query Parameter)
// =========================================================================
@Component
public class StringToLocalDateConverter implements Converter<String, LocalDate> {
    @Override
    public LocalDate convert(String source) {
        if (source == null || source.isBlank()) return null;
        return LocalDate.parse(source.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
    }
}

// =========================================================================
// TIER 2: Jackson Converter (Transport JSON Body)
// =========================================================================
public class CentsToMoneyJacksonConverter extends StdConverter<Long, Money> {
    @Override
    public Money convert(Long cents) {
        if (cents == null) return null;
        return new Money(BigDecimal.valueOf(cents, 2), Currency.getInstance("USD"));
    }
}

public record PaymentRequestDto(
    @JsonDeserialize(converter = CentsToMoneyJacksonConverter.class)
    Money amount
) {}

// =========================================================================
// TIER 3: JPA AttributeConverter (Database Persistence)
// =========================================================================
@jakarta.persistence.Converter(autoApply = true)
public class MoneyToVarcharAttributeConverter implements AttributeConverter<Money, String> {
    @Override
    public String convertToDatabaseColumn(Money attribute) {
        if (attribute == null) return null;
        return attribute.amount().toPlainString() + " " + attribute.currency().getCurrencyCode();
    }

    @Override
    public Money convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        String[] parts = dbData.split(" ");
        return new Money(new BigDecimal(parts[0]), Currency.getInstance(parts[1]));
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What happens if the JPA `AttributeConverter` throws an exception during Hibernate dirty checking / flush vs if Jackson throws an exception during request deserialization?"
- **Winning Answer**: "The failure blast radius and HTTP semantics are completely different:
  1. **Jackson Deserialization Failure**: Occurs **before** the controller method or `@Transactional` service is invoked. No database connection is acquired, no transaction is started. Spring catches `HttpMessageNotReadableException` and returns a clean **HTTP 400 Bad Request** to the client.
  2. **JPA `AttributeConverter` Failure**: Occurs **at the very end of the transaction** during Hibernate's dirty-checking `flush()` phase (typically when the `@Transactional` method returns and Spring commits). The database transaction is marked for rollback, connection pool resources must be released, and Spring catches `TransactionSystemException` / `RollbackException`. The client receives an **HTTP 500 Internal Server Error** because the business transaction suffered a persistence failure."

---

> **Module Completion:** Spring Core IoC, DI & Spring Boot 3 — 26 complete scenario-based master Q&As, 7 beginner mistakes, 5 production incidents covering all tier-1 interview scenarios for Senior and Staff Engineer roles.
