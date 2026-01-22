# 🍃 Ultimate Spring Boot Annotations Handbook

This guide provides a comprehensive list of annotations used across the Spring ecosystem, categorized by their functional domain.

---

## 1. Core Framework (Inversion of Control & DI)
These annotations manage the lifecycle and injection of your Java beans.

* `@Configuration`: Indicates that a class declares one or more `@Bean` methods.
* `@Bean`: Defines a bean to be managed by the Spring container.
* `@Component`: Marks a class as a candidate for auto-detection during classpath scanning.
* `@Service`: Specialized `@Component` for business logic.
* `@Repository`: Specialized `@Component` for DAOs; provides automatic exception translation.
* `@Controller`: Marks a class as a web controller.
* `@Autowired`: Marks a constructor, field, or setter for dependency injection.
* `@Qualifier`: Used when multiple beans of the same type exist to specify which one to inject.
* `@Primary`: Gives priority to a specific bean when multiple candidates are available.
* `@Value`: Used for injecting values into fields from property files or system variables.
* `@Lazy`: Prevents a bean from being initialized until it is first requested.
* `@Scope`: Defines the lifecycle (Singleton, Prototype, Request, Session).
* `@Profile`: Maps beans to specific environments (e.g., `dev`, `prod`).
* `@Import`: Allows for importing other `@Configuration` classes.
* `@PropertySource`: Provides a mechanism for adding a `PropertySource` to Spring’s Environment.
* `@AliasFor`: Declares aliases for annotation attributes.

---

## 2. Spring Boot Auto-Configuration
These annotations drive the "Convention over Configuration" approach.

* `@SpringBootApplication`: Combines `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`.
* `@EnableAutoConfiguration`: Tells Spring Boot to start adding beans based on classpath settings.
* `@ComponentScan`: Configures which packages to scan for components.
* `@ConfigurationProperties`: Binds external configuration (YAML/properties) to a Java object.
* `@ConditionalOnClass`: Matches only if a specific class is present on the classpath.
* `@ConditionalOnMissingBean`: Matches only if a specific bean hasn't been defined by the user.
* `@ConditionalOnProperty`: Matches only if a specific property has a specific value.
* `@ConditionalOnExpression`: Matches based on a SpEL (Spring Expression Language) expression.
* `@EntityScan`: Configures base packages used to auto-configure Hibernate/JPA.
* `@EnableCaching`: Enables Spring's annotation-driven cache management.

---

## 3. Spring MVC & REST (Web Layer)
Handling HTTP requests, responses, and API mapping.

* `@RestController`: Combines `@Controller` and `@ResponseBody`.
* `@RequestMapping`: Maps HTTP requests to handler methods.
* `@GetMapping`: Shortcut for `@RequestMapping(method = RequestMethod.GET)`.
* `@PostMapping`: Shortcut for `@RequestMapping(method = RequestMethod.POST)`.
* `@PutMapping`: Shortcut for `@RequestMapping(method = RequestMethod.PUT)`.
* `@DeleteMapping`: Shortcut for `@RequestMapping(method = RequestMethod.DELETE)`.
* `@PatchMapping`: Shortcut for `@RequestMapping(method = RequestMethod.PATCH)`.
* `@RequestBody`: Maps the HTTP request body to a domain object.
* `@RequestParam`: Binds a query parameter to a method argument.
* `@PathVariable`: Extracts data from the URI path.
* `@RequestHeader`: Binds a request header to a method argument.
* `@CookieValue`: Binds an HTTP cookie value to a method argument.
* `@ResponseStatus`: Marks a method or exception with a specific HTTP status code.
* `@ResponseBody`: Tells Spring to write the return type directly to the HTTP response body.
* `@ControllerAdvice`: Intercepts exceptions and data binding globally for all controllers.
* `@ExceptionHandler`: Declares a method as an exception handler.
* `@CrossOrigin`: Enables Cross-Origin Resource Sharing (CORS).

---

## 4. Spring Data JPA & Transactions
Database management and ORM mapping.

* `@Transactional`: Defines the scope of a single database transaction.
* `@NoRepositoryBean`: Prevents Spring from creating a bean for a base repository interface.
* `@Query`: Allows defining a custom JPQL or native SQL query.
* `@Modifying`: Used with `@Query` to indicate the query performs an update or delete.
* `@Param`: Binds a method parameter to a named parameter in a query.
* `@Entity`: Marks a class as a JPA entity.
* `@Table`: Specifies the primary table for the annotated entity.
* `@Id`: Specifies the primary key of an entity.
* `@GeneratedValue`: Provides the strategy for primary key generation.
* `@Column`: Maps the field to a database column.
* `@JoinColumn`: Specifies a column for joining an entity association.
* `@OneToMany` / `@ManyToOne`: Defines the cardinality of the relationship.
* `@ManyToMany`: Defines a many-to-many relationship.
* `@Enumerated`: Specifies that a persistent property should be persisted as an enumerated type.
* `@Transient`: Indicates that the field is not to be persisted in the database.

---

## 5. Spring Security
Handling authentication and authorization.

* `@EnableWebSecurity`: Enables web security support.
* `@EnableMethodSecurity`: Enables `@PreAuthorize`, `@PostAuthorize`, etc.
* `@PreAuthorize`: Checks a SpEL expression before entering a method.
* `@PostAuthorize`: Checks a SpEL expression after a method has executed.
* `@Secured`: Simple role-based access control (e.g., `@Secured("ROLE_ADMIN")`).
* `@RolesAllowed`: JSR-250 compatible role-based security.
* `@AuthenticationPrincipal`: Injects the currently authenticated user into a method argument.

---

## 6. Spring AOP (Aspect Oriented Programming)
Used for cross-cutting concerns like logging and auditing.

* `@Aspect`: Declares a class as an aspect.
* `@Before`: Advice that runs before a join point.
* `@After`: Advice that runs after a join point, regardless of outcome.
* `@AfterReturning`: Advice that runs only if the method returns successfully.
* `@AfterThrowing`: Advice that runs only if the method throws an exception.
* `@Around`: Advice that surrounds a join point (most powerful).
* `@Pointcut`: Defines where the advice should be applied.

---

## 7. Spring WebFlux (Reactive)
For non-blocking, asynchronous applications.

* `@EnableWebFlux`: Enables reactive web support.
* `@EventListener`: Marks a method as a listener for application events.

---

## 8. Spring Testing
Validating your application logic.

* `@SpringBootTest`: Loads the full application context for integration tests.
* `@WebMvcTest`: Focuses only on Spring MVC components (MockMvc).
* `@DataJpaTest`: Configures an in-memory database for testing the JPA layer.
* `@MockBean`: Adds a mock object to the Spring ApplicationContext.
* `@SpyBean`: Wraps an existing bean in a Mockito spy.
* `@TestPropertySource`: Configures property file locations for test classes.
* `@Sql`: Executes SQL scripts before or after test execution.

---

## 9. Bean Validation (JSR 303/380)
Often used with `@Valid` or `@Validated` in Spring.

* `@NotNull`: The field must not be null.
* `@NotEmpty`: The field must not be null and size > 0.
* `@NotBlank`: The string must not be null and must contain at least one non-whitespace character.
* `@Min` / `@Max`: Numeric value constraints.
* `@Email`: Validates email format.
* `@Size`: Checks the size of strings, collections, or arrays.