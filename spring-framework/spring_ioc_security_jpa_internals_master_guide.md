# 🍃 Spring Internals Master Guide: Bean IoC Container, Spring Security 6 & Spring Data JPA Under the Hood

> **Target Audience**: Staff Java/Spring Architects, Senior Backend Engineers, and Tech Leads.  
> **Prerequisites**: Solid understanding of Java syntax and basic Spring Boot annotations. Zero prior framework internal bytecode or plumbing knowledge assumed. We dissect the source code, thread execution paths, data structures, and memory models governing how Spring manages beans, secures endpoints, and persists entities.

---

## 🗺️ Master Catalog & Repository Index Updates

This flagship guide is fully indexed across the architecture catalog:

* **Repository Categorization Index**: [`all_markdown_files_categorized.md`](../all_markdown_files_categorized.md) — Category 2 (Spring Boot & Spring Framework Ecosystem) updated to 17 documents with this entry added.
* **Root Repository Architecture Index**: [`README.md`](../README.md) — Section 2 table updated with technical highlights and breakdown.
* **Omni-Protocol Platform Documentation**: [`projects/omni-api-realtime-platform/README.md`](../projects/omni-api-realtime-platform/README.md) — Section 7.12 added detailing IoC, Security 6, and JPA internal engines.
* **Platform Walkthrough Artifact**: `walkthrough.md` — Registered as Guide #12 with complete verification status.

---

## ⚡ The Holy Trinity: End-to-End Architectural Lifecycle

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                   THE COMPLETE SPRING INTERNALS ECOSYSTEM                                       │
├─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                                 │
│   [1. SPRING BEAN IOC ENGINE]                                                                                   │
│   @Configuration / @Component                                                                                   │
│           │                                                                                                     │
│           ▼                                                                                                     │
│   ClassPathBeanDefinitionScanner ──► BeanDefinitionRegistry (DefaultListableBeanFactory)                        │
│           │                                                                                                     │
│           ▼                                                                                                     │
│   BeanFactoryPostProcessors      ──► Modifies bean definitions (@Value placeholders, @Configuration classes)    │
│           │                                                                                                     │
│           ▼                                                                                                     │
│   The 12-Step Bean Lifecycle     ──► Instantiation -> Populate -> Aware -> BPP Before -> @PostConstruct        │
│           │                          -> InitializingBean -> BPP After (JDK/CGLIB AOP Proxy Creation)            │
│           ▼                                                                                                     │
│   Three-Level Singleton Cache    ──► singletonObjects (1) | earlySingletonObjects (2) | singletonFactories (3)  │
│                                                                                                                 │
├─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                                 │
│   [2. SPRING SECURITY 6 ENGINE]                                                                                 │
│   Incoming HTTP Request (Tomcat Socket)                                                                         │
│           │                                                                                                     │
│           ▼                                                                                                     │
│   DelegatingFilterProxy          ──► Delegates from Servlet Container to Spring ApplicationContext              │
│           │                                                                                                     │
│           ▼                                                                                                     │
│   FilterChainProxy               ──► SecurityFilterChain (Selects chain via SecurityMatcher)                    │
│           │                                                                                                     │
│           ▼                                                                                                     │
│   AuthenticationFilter           ──► AuthenticationManager (ProviderManager) -> DaoAuthenticationProvider       │
│           │                          -> UserDetailsService -> PasswordEncoder -> Authenticated Token            │
│           ▼                                                                                                     │
│   SecurityContextHolder          ──► Stores Authentication in ThreadLocal (SecurityContextHolderStrategy)       │
│           │                                                                                                     │
│           ▼                                                                                                     │
│   AuthorizationFilter            ──► AuthorizationManager checks URI permissions -> Controller Dispatch        │
│   Method Security AOP            ──► @PreAuthorize evaluated via AuthorizationManagerBeforeMethodInterceptor    │
│                                                                                                                 │
├─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                                 │
│   [3. SPRING DATA JPA & HIBERNATE 6 ENGINE]                                                                     │
│   OrderRepository.findById(1L) / save(order)                                                                    │
│           │                                                                                                     │
│           ▼                                                                                                     │
│   JpaRepositoryFactoryBean       ──► JDK Dynamic Proxy with QueryExecutorMethodInterceptor                     │
│           │                                                                                                     │
│           ▼                                                                                                     │
│   TransactionInterceptor         ──► TransactionSynchronizationManager binds Connection & EntityManager to      │
│           │                          current ThreadLocal (PlatformTransactionManager -> JpaTransactionManager)  │
│           ▼                                                                                                     │
│   EntityManager (SessionImpl)    ──► Persistence Context (First-Level Cache + Identity Map)                    │
│           │                          Entity States: Transient -> Managed -> Detached -> Removed                 │
│           ▼                                                                                                     │
│   Flush & Dirty Checking         ──► Compares Managed Entity against Snapshot Array                             │
│           │                          ActionQueue orders: Inserts -> Updates -> Deletes                          │
│           ▼                                                                                                     │
│   HikariCP JDBC Connection       ──► PreparedStatement -> Database Socket Transmission -> Commit / Rollback 🚀  │
│                                                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

# Track 1: Spring Bean IoC Container Internals

## 1.1 What is a `BeanDefinition`?
Spring does **not** instantiate classes directly from `.class` files. It converts all metadata into a blueprint called `org.springframework.beans.factory.config.BeanDefinition`:

```java
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {
    String getBeanClassName();             // "com.enterprise.service.OrderServiceImpl"
    String getScope();                     // "singleton", "prototype", "request"
    boolean isLazyInit();                  // false
    String[] getDependsOn();               // explicitly declared dependencies
    boolean isPrimary();                   // @Primary resolution
    ConstructorArgumentValues getConstructorArgumentValues();
    MutablePropertyValues getPropertyValues();
    int getRole();                         // ROLE_APPLICATION, ROLE_INFRASTRUCTURE
}
```

* When Spring boots, `ClassPathBeanDefinitionScanner` scans `@Component`, `@Service`, `@Repository`, `@Controller`.
* `AnnotatedBeanDefinitionReader` parses `@Configuration` classes and `@Bean` methods.
* Every definition is registered into `DefaultListableBeanFactory`:
  ```java
  // Map of beanName -> BeanDefinition
  private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
  // List of bean definition names in registration order
  private final List<String> beanDefinitionNames = new ArrayList<>(256);
  ```

---

## 1.2 The 12 Stages of `ApplicationContext.refresh()`

The entire Spring container boots inside `AbstractApplicationContext.refresh()`. Every Staff engineer must know this method by heart:

```
[1. prepareRefresh()]              ──► Initialize property sources, validate required environment variables
[2. obtainFreshBeanFactory()]      ──► Refresh internal DefaultListableBeanFactory, load BeanDefinitions
[3. prepareBeanFactory()]          ──► Configure SpEL resolver, PropertyEditors, ApplicationContextAware callbacks
[4. postProcessBeanFactory()]      ──► Subclass hook (e.g. web context servlet registration)
[5. invokeBeanFactoryPostProcessors()] ──► Execute BeanDefinitionRegistryPostProcessor & BeanFactoryPostProcessor
[6. registerBeanPostProcessors()]   ──► Instantiate and sort BeanPostProcessors (PriorityOrdered, Ordered)
[7. initMessageSource()]           ──► Setup i18n internationalization message resolution
[8. initApplicationEventMulticaster()] ──► Setup SimpleApplicationEventMulticaster for event pub/sub
[9. onRefresh()]                   ──► Initialize theme, or start Embedded Tomcat (in Spring Boot)
[10. registerListeners()]          ──► Register ApplicationListener beans with multicaster
[11. finishBeanFactoryInitialization()] ──► INSTANTIATE ALL REMAINING NON-LAZY SINGLETON BEANS!
[12. finishRefresh()]              ──► LifecycleProcessor.onRefresh(), publish ContextRefreshedEvent
```

---

## 1.3 `BeanFactoryPostProcessor` vs. `BeanPostProcessor`

These two interfaces represent the fundamental extension points of the Spring container:

| Characteristic | `BeanFactoryPostProcessor` (BFPP) | `BeanPostProcessor` (BPP) |
| :--- | :--- | :--- |
| **Execution Stage** | Stage 5 of `refresh()`, **before** any beans are instantiated | Stage 11 of `refresh()`, **during** bean instantiation |
| **Input Target** | `ConfigurableListableBeanFactory` (reads/mutates `BeanDefinition`) | Individual raw Java Object instances |
| **Key Examples** | `PropertySourcesPlaceholderConfigurer` (resolves `${db.url}`), `ConfigurationClassPostProcessor` (parses `@Configuration` & `@Bean`) | `AutowiredAnnotationBeanPostProcessor` (injects `@Autowired`), `CommonAnnotationBeanPostProcessor` (runs `@PostConstruct`), `AbstractAutoProxyCreator` (creates AOP/Tx Proxies) |
| **Can Modify Class?**| Yes, can change class name or properties in `BeanDefinition` | Yes, can return a JDK Dynamic Proxy or CGLIB Proxy wrapping the original object |

---

## 1.4 The Full 12-Step Spring Bean Lifecycle

When a singleton bean is requested via `getBean(name)` during Stage 11:

```
                  ┌────────────────────────────────────────────────────────┐
                  │ 1. Instantiate Bean (Reflection / Constructor)         │
                  └──────────────────────────┬─────────────────────────────┘
                                             ▼
                  ┌────────────────────────────────────────────────────────┐
                  │ 2. Populate Properties (@Autowired / @Value Injection) │
                  └──────────────────────────┬─────────────────────────────┘
                                             ▼
                  ┌────────────────────────────────────────────────────────┐
                  │ 3. Invoke Aware Interfaces                             │
                  │    - BeanNameAware.setBeanName()                       │
                  │    - BeanClassLoaderAware.setBeanClassLoader()         │
                  │    - BeanFactoryAware.setBeanFactory()                 │
                  └──────────────────────────┬─────────────────────────────┘
                                             ▼
                  ┌────────────────────────────────────────────────────────┐
                  │ 4. ApplicationContextAware Callbacks                   │
                  │    - EnvironmentAware / ApplicationContextAware        │
                  └──────────────────────────┬─────────────────────────────┘
                                             ▼
                  ┌────────────────────────────────────────────────────────┐
                  │ 5. BeanPostProcessor.postProcessBeforeInitialization() │
                  │    (Executes @PostConstruct methods)                   │
                  └──────────────────────────┬─────────────────────────────┘
                                             ▼
                  ┌────────────────────────────────────────────────────────┐
                  │ 6. InitializingBean.afterPropertiesSet()               │
                  └──────────────────────────┬─────────────────────────────┘
                                             ▼
                  ┌────────────────────────────────────────────────────────┐
                  │ 7. Custom init-method (defined in @Bean(initMethod=..))│
                  └──────────────────────────┬─────────────────────────────┘
                                             ▼
                  ┌────────────────────────────────────────────────────────┐
                  │ 8. BeanPostProcessor.postProcessAfterInitialization()  │
                  │    (CREATES JDK DYNAMIC PROXY OR CGLIB BYTECODE PROXY!)│
                  └──────────────────────────┬─────────────────────────────┘
                                             ▼
                  ┌────────────────────────────────────────────────────────┐
                  │ 9. Bean is Ready for Use (Served from Singleton Cache) │
                  └──────────────────────────┬─────────────────────────────┘
                                             │  Application Shutdown Triggered
                                             ▼
                  ┌────────────────────────────────────────────────────────┐
                  │ 10. @PreDestroy annotated methods                      │
                  └──────────────────────────┬─────────────────────────────┘
                                             ▼
                  ┌────────────────────────────────────────────────────────┐
                  │ 11. DisposableBean.destroy()                           │
                  └──────────────────────────┬─────────────────────────────┘
                                             ▼
                  ┌────────────────────────────────────────────────────────┐
                  │ 12. Custom destroy-method                              │
                  └────────────────────────────────────────────────────────┘
```

---

## 1.5 The Three-Level Cache: How Spring Resolves Circular Dependencies

If `ServiceA` injects `ServiceB` via setter/field, and `ServiceB` injects `ServiceA`, how does Spring avoid infinite loops?
Through the **Three-Level Cache** in `DefaultSingletonBeanRegistry`:

```java
// Level 1: Fully initialized singletons (Ready for use)
private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

// Level 2: Early instantiated singletons (Instantiated, properties NOT populated yet, early proxy if needed)
private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);

// Level 3: Singleton Factories (Holds ObjectFactory<?> lambdas that can produce early AOP proxies)
private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

// Set of beans currently undergoing creation
private final Set<String> singletonsCurrentlyInCreation = 
    Collections.newSetFromMap(new ConcurrentHashMap<>(16));
```

### The Step-by-Step Circular Resolution Flow:
1. `ServiceA` creation starts: Added to `singletonsCurrentlyInCreation`.
2. `ServiceA` is instantiated via constructor (raw object in memory).
3. `ServiceA` exposes an `ObjectFactory` in **Level 3 Cache (`singletonFactories`)**:
   ```java
   addSingletonFactory("serviceA", () -> getEarlyBeanReference("serviceA", mbd, bean));
   ```
4. `ServiceA` populates properties $\to$ discovers it needs `ServiceB`.
5. Spring calls `getBean("serviceB")`.
6. `ServiceB` creation starts: Added to `singletonsCurrentlyInCreation`.
7. `ServiceB` is instantiated and puts factory in Level 3.
8. `ServiceB` populates properties $\to$ discovers it needs `ServiceA`.
9. Spring calls `getBean("serviceA")`:
   - Checks Level 1 (`singletonObjects`): Not found.
   - Checks Level 2 (`earlySingletonObjects`): Not found.
   - Checks Level 3 (`singletonFactories`): **Found!**
   - Calls `factory.getObject()`: Runs `SmartInstantiationAwareBeanPostProcessor` to create an **early AOP proxy of ServiceA** if needed.
   - Moves `ServiceA` (or its proxy) into **Level 2 (`earlySingletonObjects`)** and removes from Level 3.
10. `ServiceB` receives `ServiceA` reference and finishes initialization $\to$ Moves to **Level 1**.
11. Control returns to `ServiceA`: Injects `ServiceB` from Level 1, completes lifecycle $\to$ Moves to **Level 1**.

> [!IMPORTANT]
> **Why Constructor Injection Circular Dependencies Fail**:
> If circular dependencies use **Constructor Injection**, `ServiceA` cannot even be instantiated (Step 2 never happens), so no factory can be placed in Level 3. The JVM throws:
> `BeanCurrentlyInCreationException: Error creating bean with name 'serviceA': Requested bean is currently in creation: Is there an unresolvable circular reference?`

---

## 1.6 Dynamic Proxies: JDK Dynamic Proxy vs. CGLIB / Byte Buddy

When `@Transactional`, `@Async`, or `@SecurityCheck` is present, Spring replaces the raw bean with a proxy during `BeanPostProcessor.postProcessAfterInitialization()`:

```
                            Target Bean Class
                                   │
              ┌────────────────────┴────────────────────┐
              ▼                                         ▼
    Target implements Interfaces?              Target does NOT implement Interfaces
    (or spring.aop.proxy-target-class=false)    (or spring.aop.proxy-target-class=true [Default])
              │                                         │
              ▼                                         ▼
     JDK Dynamic Proxy                           CGLIB / Byte Buddy Proxy
  (java.lang.reflect.Proxy)                  (Generates subclass bytecode dynamically)
              │                                         │
  - Implements same interface                - Subclasses target class
  - Delegates via InvocationHandler          - Overrides non-final methods
  - Cannot proxy concrete classes            - Final classes/methods CANNOT be proxied!
```

* **Spring Boot 2.x & 3.x Default**: `spring.aop.proxy-target-class=true`. Spring Boot defaults to **CGLIB/Byte Buddy** across all beans, even if they implement interfaces, to prevent `ClassCastException` when injecting concrete classes.

---

# Track 2: Spring Security 6 Architecture & Internal Plumbing

Spring Security does **not** replace the servlet container; it sits directly inside the Tomcat servlet pipeline as a series of filters.

## 2.1 The Bridge: `DelegatingFilterProxy` to `FilterChainProxy`

```
 [Tomcat Servlet Engine]
           │
           ▼
 [DelegatingFilterProxy] (Standard Servlet Filter in web.xml or WebMvc AutoConfig)
           │
           │ Looks up Spring Bean named "springSecurityFilterChain" in ApplicationContext
           ▼
 [FilterChainProxy] (Spring Security Core Engine)
           │
           ├── Matches URL via SecurityMatcher (e.g. "/api/**")
           ▼
 [SecurityFilterChain 1] (DefaultSecurityFilterChain)
     ├── 1. ChannelProcessingFilter (Enforces HTTPS)
     ├── 2. WebAsyncManagerIntegrationFilter
     ├── 3. SecurityContextHolderFilter (Loads existing SecurityContext from Session/Store)
     ├── 4. HeaderWriterFilter (Sets X-Content-Type-Options, HSTS, CSP)
     ├── 5. CorsFilter
     ├── 6. CsrfFilter (Validates CSRF token for stateful sessions)
     ├── 7. LogoutFilter
     ├── 8. JwtAuthenticationFilter (Custom OncePerRequestFilter)
     ├── 9. UsernamePasswordAuthenticationFilter (Parses /login POST)
     ├── 10. DefaultLoginPageGeneratingFilter
     ├── 11. BasicAuthenticationFilter (Parses Authorization: Basic ...)
     ├── 12. RequestCacheAwareFilter
     ├── 13. SecurityContextHolderAwareRequestFilter
     ├── 14. AnonymousAuthenticationFilter (Assigns "anonymousUser" if unauthenticated)
     ├── 15. SessionManagementFilter
     ├── 16. ExceptionTranslationFilter (Catches AuthenticationException / AccessDeniedException)
     └── 17. AuthorizationFilter (Enforces requestMatchers(..).hasRole(..))
```

---

## 2.2 Authentication Flow Internals: Step-by-Step

How does an incoming HTTP request authenticate?

```
[Raw HTTP Request: Authorization: Bearer eyJhbGci...]
                       │
                       ▼
             [JwtAuthenticationFilter]
                       │  1. Extracts token, builds unauthenticated JwtAuthenticationToken
                       ▼
             [AuthenticationManager] (ProviderManager)
                       │  2. Iterates over list of AuthenticationProvider beans
                       ▼
             [JwtAuthenticationProvider / DaoAuthenticationProvider]
                       │  3. Validates signature / password
                       │  4. Calls UserDetailsService.loadUserByUsername()
                       │  5. Compares hashed passwords via PasswordEncoder.matches()
                       ▼
             [Authenticated Authentication Token]
                       │  Contains: Principal, Authorities (ROLE_ADMIN), isAuthenticated=true
                       ▼
             [SecurityContextHolder.getContext().setAuthentication(auth)]
                       │  Stored in ThreadLocal (SecurityContextHolderStrategy)
                       ▼
             [SecurityContextRepository.saveContext(context, req, res)]
```

### Key Components:
1. **`Authentication`**: Interface holding credentials, principal, and granted authorities (`GrantedAuthority`).
2. **`AuthenticationManager` (`ProviderManager`)**: Contains `List<AuthenticationProvider>`. Dispatches the token to the provider that returns `true` for `provider.supports(token.getClass())`.
3. **`SecurityContextHolder`**: Facade using a `SecurityContextHolderStrategy`:
   - Default: `ThreadLocalSecurityContextHolderStrategy`.
   - In Virtual Threads (Java 21+): Virtual threads preserve `ThreadLocal`, but async thread handoffs require `DelegatingSecurityContextAsyncTaskExecutor` or `SecurityContextHolder.setDeferredContext()`.

---

## 2.3 Authorization Internals: Request vs. Method Security

In Spring Security 6, the legacy `AccessDecisionManager` and `Voter` hierarchy was completely replaced by **`AuthorizationManager<T>`**:

### 1. Request-Level Authorization (`AuthorizationFilter`)
* Positioned at the very end of `SecurityFilterChain`.
* Inspects `SecurityContextHolder.getContext().getAuthentication()`.
* Delegates to `RequestMatcherDelegatingAuthorizationManager`.
* If access is denied: Throws `AccessDeniedException`.
* Caught by `ExceptionTranslationFilter`:
  - If anonymous: Triggers `AuthenticationEntryPoint.commence()` (HTTP 401 Unauthorized).
  - If authenticated: Triggers `AccessDeniedHandler.handle()` (HTTP 403 Forbidden).

### 2. Method-Level Security (`@PreAuthorize`)
Enabled via `@EnableMethodSecurity`:
* Uses Spring AOP!
* `AuthorizationManagerBeforeMethodInterceptor` intercepts method invocations matching `@PreAuthorize("hasRole('ADMIN')")`.
* Evaluates SpEL expressions against the `MethodInvocation` and current `Authentication`.

---

# Track 3: Spring Data JPA & Hibernate 6 Internals

## 3.1 The Magic Behind Spring Data JPA Interfaces
When you declare:
```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdAndStatus(Long customerId, String status);
}
```
**No developer writes implementation code. How does Spring execute queries?**

```
[Spring Boot Startup]
        │
        ▼
[JpaRepositoriesRegistrar] ──► Discovers interfaces extending JpaRepository
        │
        ▼
[JpaRepositoryFactoryBean] ──► Calls RepositoryFactorySupport.getRepository()
        │
        ▼
[JDK Dynamic Proxy Created]
        │
        ├── Target Base Class: org.springframework.data.jpa.repository.support.SimpleJpaRepository
        │   (Provides implementations for save(), findById(), delete(), findAll())
        │
        └── QueryExecutorMethodInterceptor
            │  (Intercepts non-CRUD methods like findByCustomerIdAndStatus)
            ▼
        [PartTree Query Parser]
            │  Tokenizes method name: ["find", "By", "CustomerId", "And", "Status"]
            ▼
        [Generates JPQL / Criteria Query]
            "SELECT o FROM Order o WHERE o.customerId = :customerId AND o.status = :status"
```

---

## 3.2 The Hibernate 6 Persistence Context (`SessionImpl`)

The `EntityManager` in Spring Boot with Hibernate is a proxy (`SharedEntityManagerCreator`) delegating to the underlying Hibernate `org.hibernate.internal.SessionImpl`.

### The 4 Entity States:
```
                ┌──────────────┐
                │  Transient   │ (New POJO created with `new Order()`, no DB identity)
                └──────┬───────┘
                       │ em.persist(entity)
                       ▼
  em.detach()   ┌──────────────┐   em.remove(entity)
 ┌──────────────┤   Managed    ├─────────────────────┐
 │              │ (Persistent) │                     │
 │              └──────▲───────┘                     ▼
 ▼                     │ em.merge(detachedEntity) ┌──────────────┐
┌──────────────┐       │                          │   Removed    │
│   Detached   ├───────┘                          └──────┬───────┘
└──────────────┘                                         │ DB Transaction Commit
                                                         ▼
                                                    [DELETED IN DB]
```

### The First-Level Cache & Dirty Checking:
Inside `SessionImpl` sits the `PersistenceContext`:
1. **Identity Map**: `Map<EntityKey, Object> entitiesByKey`. Ensures `em.find(Order.class, 1L) == em.find(Order.class, 1L)` evaluates to `true` (reference equality!).
2. **Loaded State Snapshot Array**: `Object[] loadedState`. When an entity is loaded, Hibernate saves a copy of every field.
3. **Dirty Checking Physics**:
   - During `em.flush()`, Hibernate compares the entity's current in-memory fields against the snapshot array.
   - If any value changed, Hibernate automatically creates an `EntityUpdateAction` and places it into the `ActionQueue`.
   - **You do NOT need to call `repository.save()` on a managed entity inside `@Transactional`!**

---

## 3.3 The `ActionQueue` & Flush Mechanics
Hibernate does **not** send SQL to the database the millisecond you call `persist()` or update a field. It defers execution until flush time to optimize batching and connection holding:

```
[EntityManager.flush()]
          │
          ▼
[ActionQueue.executeActions()]
    Executes in STRICT deterministic order:
    1. OrphanRemovalAction
    2. EntityInsertAction
    3. EntityUpdateAction
    4. CollectionRemoveAction
    5. CollectionUpdateAction
    6. CollectionRecreateAction
    7. EntityDeleteAction
          │
          ▼
[JDBC PreparedStatement Batch] ──► HikariCP Connection ──► SQL Sent to Database Wire
```

---

## 3.4 Declarative Transaction Management (`@Transactional`) Internals

```
[Client calls OrderService.createOrder()]
                    │
                    ▼
       [CGLIB Proxy: OrderService$$SpringCGLIB]
                    │
                    ▼
         [TransactionInterceptor]
                    │
                    ├── 1. PlatformTransactionManager.getTransaction(def)
                    │      (JpaTransactionManager checks ThreadLocal)
                    │
                    ├── 2. Opens JDBC Connection from HikariCPDataSource
                    │      Sets connection.setAutoCommit(false)
                    │
                    ├── 3. TransactionSynchronizationManager.bindResource():
                    │      Binds Connection & EntityManager to current ThreadLocal!
                    │
                    ├── 4. Invokes actual OrderServiceImpl.createOrder()
                    │
                    ├── 5. [Success]: transactionManager.commit(status)
                    │      - Triggers Session.flush() (Dirty check updates sent to DB)
                    │      - connection.commit()
                    │
                    └── 6. [Exception]: transactionManager.rollback(status)
                           - connection.rollback()
                           - Unbinds ThreadLocal resources, closes Connection
```

---

# Track 4: Cross-Cutting Architectural Integration Matrix

| Mechanism | Spring Bean IoC Container | Spring Security 6 | Spring Data JPA / Hibernate 6 |
| :--- | :--- | :--- | :--- |
| **Core Abstraction** | `BeanDefinition`, `BeanFactory` | `SecurityFilterChain`, `Authentication` | `Repository`, `EntityManager` (`SessionImpl`) |
| **Primary Proxy Type** | CGLIB / Byte Buddy (`ProxyTargetClass`) | Spring AOP Interceptor (`@PreAuthorize`) | JDK Dynamic Proxy (`RepositoryFactorySupport`) |
| **Thread Context Storage**| Singleton Cache (`ConcurrentHashMap`) | `SecurityContextHolder` (`ThreadLocal`) | `TransactionSynchronizationManager` (`ThreadLocal`) |
| **Primary Extension Point**| `BeanPostProcessor` | `SecurityFilterChain` / Custom Filters | `EntityListener` / Hibernate `Interceptor` |
| **Default Lifecycle Scope**| Singleton (1 instance per JVM context) | Request / Filter Lifecycle | Transaction / PersistenceContext Scope |
| **Exception Handling** | `BeansException`, `BeanCreationException` | `AuthenticationException` (401), `AccessDeniedException` (403) | `DataAccessException`, `OptimisticLockException` |

---

# Track 5: Production-Ready Implementations

## 5.1 Custom `BeanPostProcessor` with Dynamic Proxy Telemetry

```java
package com.enterprise.internals.ioc;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class PerformanceAuditBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Intercept services to inject microsecond execution timers
        if (bean.getClass().isAnnotationPresent(AuditedService.class)) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(bean.getClass());
            enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
                long start = System.nanoTime();
                try {
                    return proxy.invokeSuper(obj, args);
                } finally {
                    long durationUs = (System.nanoTime() - start) / 1_000;
                    if (durationUs > 10_000) { // Log slow calls > 10ms
                        System.out.printf("[SLOW:BEAN] %s#%s took %d µs%n", 
                            beanName, method.getName(), durationUs);
                    }
                }
            });
            return enhancer.create();
        }
        return bean;
    }
}
```

---

## 5.2 Production Spring Security 6 Stateless JWT Configuration

```java
package com.enterprise.internals.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class Security6Config {

    private final JwtAuthenticationFilter jwtFilter;

    public Security6Config(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/actuator/health").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

---

## 5.3 Spring Data JPA Custom Repository with EntityGraph & Atomic Specs

```java
package com.enterprise.internals.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.OneToMany;
import jakarta.persistence.FetchType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Solves N+1 Problem in single query via SQL LEFT OUTER JOIN
    @EntityGraph(attributePaths = {"items", "customer"})
    Optional<Order> findWithDetailsById(Long id);

    // Atomic DB-level decrement avoiding lost updates without Pessimistic Lock
    @Modifying
    @Query("UPDATE Inventory i SET i.stock = i.stock - :qty WHERE i.sku = :sku AND i.stock >= :qty")
    int decrementStockIfAvailable(@Param("sku") String sku, @Param("qty") int qty);
}
```

---

# Track 6: 10 Deep Production Failure Modes & War Stories

### 1. The Circular Dependency with `@Async` / `@Lazy` Proxy Failure
* **Mechanism**: When `ServiceA` and `ServiceB` depend on each other, Spring resolves the raw instance via Level 3 cache. However, `@Async` generates an independent proxy *after* Level 2 cache population.
* **Failure**: `BeanCurrentlyInCreationException: Bean with name 'serviceA' has been injected into other beans [serviceB] in its raw form, but has eventually been wrapped`.
* **Fix**: Eliminate circular architecture, or inject `ObjectProvider<ServiceA>` or `@Lazy`.

### 2. Self-Invocation `@Transactional` Bypass
* **Mechanism**: Spring proxies intercept calls from *outside* the bean. Calling `this.nestedTxMethod()` calls the raw class instance, bypassing the `TransactionInterceptor`.
* **Failure**: `nestedTxMethod()` runs with no transaction! Rollbacks and propagations silently fail.
* **Fix**: Inject self (`@Autowired private OrderService self; self.nestedTxMethod()`) or refactor to dedicated helper component.

### 3. Self-Invocation `@PreAuthorize` Security Bypass
* **Mechanism**: Same proxy bypass physics. Calling a `@PreAuthorize` method from within the same class bypasses `AuthorizationManagerBeforeMethodInterceptor`.
* **Failure**: Unauthenticated users execute privileged methods without check.

### 4. `LazyInitializationException` in JSON Serialization
* **Mechanism**: Jackson reads an uninitialized Hibernate `ByteBuddyInterceptor` proxy outside of `@Transactional` (after session closed).
* **Failure**: `LazyInitializationException: could not initialize proxy - no Session`.
* **Fix**: Use `@EntityGraph`, `JOIN FETCH`, or DTO projection. **Never enable `spring.jpa.open-in-view=true` in production** (leaks DB connections across HTTP wait times).

### 5. Accidental Dirty Checking DB Writes
* **Mechanism**: Loading an entity in `@Transactional`, mutating a field for a calculation, and intending not to save it.
* **Failure**: Hibernate flush automatically persists the mutation!
* **Fix**: Mark transaction `@Transactional(readOnly = true)` (sets Hibernate flush mode to `MANUAL` and disables snapshot tracking).

### 6. ThreadLocal Security Context Leak Across Worker Pools
* **Mechanism**: Storing authentication in `SecurityContextHolder` without clearing it when using shared `ThreadPoolTaskExecutor`.
* **Failure**: Thread is reused for another tenant/user, inheriting the previous user's `Authentication`!
* **Fix**: Always clear in `finally` block or configure `DelegatingSecurityContextAsyncTaskExecutor`.

### 7. Premature Bean Instantiation by `BeanPostProcessor`
* **Mechanism**: A `BeanPostProcessor` depends directly on a business bean (`@Autowired MyService service`).
* **Failure**: `MyService` is instantiated before other `BeanFactoryPostProcessors` have run. `@Value` properties remain unpopulated and AOP proxies are never applied!

### 8. `ActionQueue` Constraint Violation from Flush Ordering
* **Mechanism**: Deleting an entity and re-inserting an entity with the same unique key in the same transaction.
* **Failure**: Hibernate `ActionQueue` executes all `EntityInsertAction`s *before* `EntityDeleteAction`s. DB throws `UniqueConstraintViolationException`!
* **Fix**: Explicitly call `repository.flush()` between the delete and insert.

### 9. N+1 Query Disaster on `@OneToMany` in Batch Processing
* **Mechanism**: Loading 1,000 orders with lazy line items. Accessing items in a loop triggers 1,000 extra SQL queries.
* **Failure**: Database connection pool exhaustion and 30-second API timeouts.
* **Fix**: Fetch via `@EntityGraph` or configure `@BatchSize(size = 100)`.

### 10. Security Context Lost in Project Loom Virtual Threads
* **Mechanism**: Virtual threads inherit standard `ThreadLocal` semantics, but async continuations or unstructured tasks fail to inherit context.
* **Fix**: Use `SecurityContextHolder.setDeferredContext()` and Java 21 `ScopedValue` integrations in Spring Framework 6.x.

---

# Track 7: 10 Beginner Mistakes vs. 10 Advanced Anti-Patterns

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                   BEGINNER MISTAKES VS. ADVANCED TRAPS                           │
├────────────────────────────────┬────────────────────────────────────────────────────────────────┤
│ Beginner Mistake               │ Why It Bites & What Happens                                    │
├────────────────────────────────┼────────────────────────────────────────────────────────────────┤
│ 1. Field Injection (@Autowired)│ Impossible to mock cleanly without Spring context, hides cycles│
│ 2. new MyService() in Spring   │ Bypasses IoC container: no @Value, no @Transactional, no AOP   │
│ 3. Calling save() for update   │ Redundant: Dirty checking already updates managed entities     │
│ 4. Catching Exception in @Tx   │ Swallows exception, prevents TransactionInterceptor rollback   │
│ 5. Using Open Session In View  │ Holds HikariCP connections open during slow template/HTTP I/O  │
│ 6. Hardcoding Role Strings     │ Fragile: "ROLE_USER" vs "USER" mismatch in hasRole()           │
│ 7. Exposing JPA Entities in API│ Leaks DB schema, causes Jackson lazy serialization loops       │
│ 8. Missing equals()/hashCode() │ Hibernate sets break when entities lack natural ID equality    │
│ 9. Ignoring CGLIB final methods│ Final methods on proxies silently execute un-proxied           │
│ 10. Singletons holding state   │ Multiple concurrent HTTP requests corrupt shared instance fields│
├────────────────────────────────┼────────────────────────────────────────────────────────────────┤
│ Advanced Enterprise Trap       │ Architectural Pathology                                        │
├────────────────────────────────┼────────────────────────────────────────────────────────────────┤
│ 1. Self-Invocation @Tx Bypass  │ Calling inner @Transactional method ignores proxy interceptor  │
│ 2. Dirty Check Snapshot Bloat  │ Loading 10,000 entities copies 10,000 snapshots to heap        │
│ 3. ActionQueue Insert/Del Race │ Inserts execute before deletes causing unique key clash        │
│ 4. Shared DB Connection Leak   │ Filters performing slow REST calls inside @Transactional       │
│ 5. Security Context in @Async  │ Background threads execute with null Authentication            │
│ 6. N+1 in GraphQL Resolvers    │ Every field resolver triggers independent JPA query            │
│ 7. Level 3 Cache AOP Mismatch  │ Circular reference early proxy doesn't match final post-process│
│ 8. Optimistic Lock on Parent   │ Updating children doesn't bump parent @Version automatically   │
│ 9. Misconfigured Propagation   │ REQUIRES_NEW inside loop exhausts HikariCP connection pool     │
│ 10. Static SecurityContext     │ Shared security context across tenants in reactive WebFlux     │
└────────────────────────────────┴────────────────────────────────────────────────────────────────┘
```

---

# Track 8: 2 Real-World Sev-1 Outage Post-Mortems

## Post-Mortem 1: The Black Friday Self-Invocation `@Transactional` Phantom Rollback Disaster
* **Incident Summary**: During Peak Black Friday checkout traffic, 4,200 payment transactions succeeded at the payment gateway, but zero orders were created in the database.
* **Root Cause Analysis**:
  ```java
  @Service
  public class CheckoutService {
      public void processCheckout(OrderReq req) {
          chargeCard(req);
          createOrder(req); // SELF-INVOCATION!
      }

      @Transactional(propagation = Propagation.REQUIRES_NEW)
      public void createOrder(OrderReq req) {
          orderRepository.save(new Order(req));
          inventoryService.deduct(req);
      }
  }
  ```
  `processCheckout()` was public but lacked `@Transactional`. When it called `createOrder()`, it invoked `this.createOrder()`, bypassing the CGLIB proxy! When `inventoryService.deduct()` failed downstream with an out-of-stock exception, no transaction rolled back the credit card charge, and order records were never committed.
* **Resolution**: Separated `OrderCreationService` into a distinct Spring bean, ensuring the CGLIB proxy wraps the invocation.

## Post-Mortem 2: The Leaked `SecurityContextHolder` High-Privilege Account Takeover
* **Incident Summary**: A SaaS customer support agent logged in, and subsequent regular users randomly received full super-admin capabilities on unrelated HTTP requests.
* **Root Cause Analysis**:
  A legacy `AuthFilter` manually set:
  ```java
  SecurityContextHolder.getContext().setAuthentication(adminAuth);
  ```
  Tomcat worker threads (`catalina-exec-*`) were pooled and recycled. When an unauthenticated request arrived on the recycled thread, the `SecurityContext` was never cleared! The unauthenticated user inherited `adminAuth` from the thread's `ThreadLocal`.
* **Resolution**: Replaced manual `ThreadLocal` assignment with standard Spring Security `SecurityContextHolderFilter`, which strictly cleans `SecurityContextHolder.clearContext()` in a `finally` block.

---

# Track 9: 40+ Core Terms Technical Glossary

1. **`ApplicationContext`**: Spring's central IoC container interface providing bean factory, i18n, and event publication capabilities.
2. **`DefaultListableBeanFactory`**: The core default implementation of `ConfigurableListableBeanFactory` containing bean definitions and singleton caches.
3. **`BeanDefinition`**: Metadata object detailing bean class, scope, constructor arguments, and property values.
4. **`BeanFactoryPostProcessor`**: Container hook allowing modification of bean definitions before bean instantiation.
5. **`BeanPostProcessor`**: Container hook allowing wrapping, modifying, or proxying beans during instantiation.
6. **Three-Level Cache**: Spring's three-tier map hierarchy for resolving circular dependencies.
7. **`singletonObjects`**: First-level cache containing fully initialized singleton beans.
8. **`earlySingletonObjects`**: Second-level cache containing early instantiated beans or proxies.
9. **`singletonFactories`**: Third-level cache containing `ObjectFactory<?>` lambdas for early reference resolution.
10. **JDK Dynamic Proxy**: Proxy created via `java.lang.reflect.Proxy` implementing target interfaces.
11. **CGLIB / Byte Buddy**: Bytecode enhancement libraries that generate dynamic subclasses for proxying.
12. **`DelegatingFilterProxy`**: Servlet filter bridging standard Tomcat servlet requests to Spring-managed filter beans.
13. **`FilterChainProxy`**: Spring Security root filter executing security filter chains against matching request URLs.
14. **`SecurityFilterChain`**: Ordered list of security filters matched against a URL pattern.
15. **`SecurityContext`**: Holder containing the active `Authentication` token.
16. **`SecurityContextHolder`**: Facade holding the `SecurityContext` via a pluggable strategy.
17. **`ThreadLocalSecurityContextHolderStrategy`**: Default strategy binding security context to the current thread.
18. **`Authentication`**: Core token representing user identity, credentials, and granted authorities.
19. **`AuthenticationManager`**: Interface defining authentication contract, implemented by `ProviderManager`.
20. **`AuthenticationProvider`**: Strategy validating specific `Authentication` token implementations.
21. **`UserDetailsService`**: Core interface loading user data by username.
22. **`PasswordEncoder`**: Interface performing one-way password hashing (BCrypt, Argon2).
23. **`AuthorizationManager`**: Spring Security 6 interface replacing legacy voters for authorization decisions.
24. **`@PreAuthorize`**: AOP-based method security annotation evaluating SpEL expressions before method execution.
25. **`JpaRepositoryFactoryBean`**: Spring Data factory bean creating dynamic repository proxies.
26. **`SimpleJpaRepository`**: Default implementation class backing all Spring Data JPA repositories.
27. **`QueryExecutorMethodInterceptor`**: AOP interceptor routing repository calls to custom queries or methods.
28. **`EntityManager`**: JPA interface for interacting with the persistence context.
29. **`SessionImpl`**: Core Hibernate implementation of `EntityManager` and `Session`.
30. **Persistence Context**: Hibernate's first-level cache managing entity identity and state.
31. **Identity Map**: Pattern ensuring identical database rows map to the exact same Java object reference.
32. **Dirty Checking**: Hibernate mechanism comparing current entity fields with initial loaded snapshots.
33. **`ActionQueue`**: Ordered queue in Hibernate executing inserts, updates, and deletes at flush time.
34. **`em.flush()`**: Forces persistence context state synchronization to the underlying database connection.
35. **Transient State**: Entity newly created in memory with no database identifier or session attachment.
36. **Managed State**: Entity associated with an active persistence context and monitored for dirty checking.
37. **Detached State**: Entity with a database identifier whose session has closed.
38. **Removed State**: Entity scheduled for deletion in the database at the next flush.
39. **`LazyInitializationException`**: Hibernate exception thrown when navigating uninitialized proxies outside an active session.
40. **`@EntityGraph`**: JPA annotation defining fetch plans to solve the N+1 query problem via outer joins.
41. **`TransactionSynchronizationManager`**: Spring utility binding JDBC connections and sessions to `ThreadLocal`.
42. **`PlatformTransactionManager`**: Spring SPI managing commit and rollback operations across resource managers.

---

# Track 10: 30-Point Enterprise Production Audit Checklist

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│             SPRING IOC, SECURITY & JPA ENTERPRISE PRODUCTION AUDIT CHECKLIST           │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ [ ] 01. Constructor injection is used exclusively; zero @Autowired on fields.          │
│ [ ] 02. No circular dependencies exist in production configurations.                   │
│ [ ] 03. All singleton beans are strictly stateless and thread-safe.                    │
│ [ ] 04. Classes with @Transactional or @PreAuthorize avoid inner self-invocation.      │
│ [ ] 05. spring.jpa.open-in-view is explicitly disabled (spring.jpa.open-in-view=false).│
│ [ ] 06. Read-only queries use @Transactional(readOnly = true) to disable dirty checks. │
│ [ ] 07. All @OneToMany and @ManyToMany relationships default to FetchType.LAZY.        │
│ [ ] 08. N+1 queries are audited via Hibernate statistics and solved with @EntityGraph. │
│ [ ] 09. @Modifying queries executing bulk DML specify clearAutomatically = true.       │
│ [ ] 10. HikariCP maximumPoolSize is sized according to (core_count * 2) + effective_spindle.│
│ [ ] 11. Security filter chains explicitly configure SessionCreationPolicy.STATELESS.   │
│ [ ] 12. CSRF is disabled only for stateless APIs that authenticate via tokens.         │
│ [ ] 13. Password hashing uses Argon2id or BCrypt with a minimum cost factor of 12.     │
│ [ ] 14. SecurityContextHolder is explicitly cleared in finally blocks of custom filters.│
│ [ ] 15. Method security is activated via @EnableMethodSecurity(prePostEnabled = true). │
│ [ ] 16. Actuator endpoints are guarded by strict role-based access checks.             │
│ [ ] 17. No sensitive credentials or JWT secrets are committed to version control.      │
│ [ ] 18. Database optimistic locking is implemented on concurrent entities (@Version).  │
│ [ ] 19. Long-running batch operations call entityManager.clear() periodically.         │
│ [ ] 20. BeanPostProcessor implementations avoid early injection of business beans.     │
│ [ ] 21. CGLIB proxies do not declare final methods or final classes.                   │
│ [ ] 22. Custom Repository methods handle non-unique results without runtime crashes.   │
│ [ ] 23. Application events use asynchronous task executors for non-critical side effects.│
│ [ ] 24. Thread pool executors pass SecurityContext via DelegatingSecurityContextExecutor.│
│ [ ] 25. Database schema migrations are executed via Flyway or Liquibase, not ddl-auto. │
│ [ ] 26. Custom exceptions thrown in @Transactional extend RuntimeException.            │
│ [ ] 27. Spring Data pagination uses keyset cursor pagination for large datasets.       │
│ [ ] 28. JWT validation filters check signature, expiration, and token blacklist status.│
│ [ ] 29. Virtual threads (Java 21+) are enabled and tested against HikariCP pinning.    │
│ [ ] 30. All entity natural keys implement deterministic equals() and hashCode().       │
└────────────────────────────────────────────────────────────────────────────────────────┘
```
