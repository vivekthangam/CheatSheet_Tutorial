[🏠 Back to Home](README.md) | [🧪 Test Automation Master Guide: Cucumber, Selenium 4 & Playwright](test_automation_master_guide.md)

# 🥒 Cucumber BDD: Complete Architecture, Syntax & Enterprise Test Automation

A comprehensive, production-ready guide to Behavior Driven Development (BDD) with Cucumber in Java. Covers Gherkin grammar, Cucumber Expressions, Step Definitions, Data Tables, Hook Lifecycles, PicoContainer Dependency Injection, and real-world API/Web testing scenarios.

---

## 📑 Table of Contents

1. [🧠 The BDD Mental Model: Business Requirements to Automated Code](#-the-bdd-mental-model)
2. [📦 Track 1: The 5 Core Building Blocks of Cucumber BDD](#2-the-5-core-building-blocks-of-cucumber-bdd)
3. [📝 Beginner Code Walkthrough: Clean Step Definition & State Sharing](#3-beginner-code-walkthrough-clean-step-definition--state-sharing)
4. [💥 What Happens When Things Break? (Top 3 Disasters)](#4-what-happens-when-things-break-top-3-disasters)
5. [⚠️ Top 5 Beginner Mistakes in Production](#5-top-5-beginner-mistakes-in-production)
6. [🎓 Top 10 Junior Interview Questions (ELI5 Answers)](#6-top-10-junior-interview-questions-with-explain-like-im-5-answers)
7. [📜 1. Core Gherkin Syntax & Grammar](#-1-core-gherkin-syntax--grammar)
8. [🎯 2. Cucumber Expressions vs. Regular Expressions](#-2-cucumber-expressions-vs-regular-expressions)
9. [☕ 3. Java Step Definitions & State Management](#-3-java-step-definitions--state-management)
10. [🪝 4. Execution Lifecycle & Hooks Architecture](#-4-execution-lifecycle--hooks-architecture)
11. [📊 5. Advanced Test Data: Data Tables & Custom Type Registry](#-5-advanced-test-data-data-tables--custom-type-registry)
12. [🧪 6. 5+ Enterprise Testing Scenarios with Full Code](#-6-5-enterprise-testing-scenarios-with-full-code)
13. [🏃 7. Test Runners & Parallel Execution (JUnit 5 / TestNG)](#-7-test-runners--parallel-execution-junit-5--testng)
14. [⚖️ 8. Gherkin & Cucumber Cheat Sheet](#️-8-gherkin--cucumber-cheat-sheet)

---

## 🧠 The BDD Mental Model

Behavior Driven Development (BDD) bridges the communication gap between Business Stakeholders (Product Owners), Developers, and Quality Assurance (QA) engineers using a shared, human-readable Domain Specific Language (Gherkin).

```mermaid
flowchart LR
    A[Business Requirement] --> B[Gherkin Feature File\n.feature]
    B --> C[Cucumber Engine]
    C --> D[Java Step Definitions\n@Given, @When, @Then]
    D --> E[Application Under Test\nREST API / UI Web / Service]
    E --> F[Test Report: Allure / HTML]
```

---

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model (The 3 Amigos & The Restaurant Menu)

### Why BDD Instead of Plain JUnit?
Imagine building a high-end restaurant:
- **Without BDD (The Chaos):** The French Chef (Developer) speaks French. The Business Investor (Product Owner) speaks English. The Food Inspector (QA) speaks German.
  - The Investor asks for *"a light morning snack"*.
  - The Chef bakes a heavy 4-course duck confit because that's what made sense in code.
  - The Inspector rejects the meal because it violated calorie regulations.
- **With BDD (The Shared Printed Menu):** All three sit down together (The **"Three Amigos"**). Before a single vegetable is chopped, they agree on a plain-English menu:
  - *"Given the kitchen has fresh eggs and sourdough bread"*
  - *"When the customer orders avocado toast"*
  - *"Then serve it with lemon within 10 minutes."*
- **Cucumber's Role:** Cucumber is the **Translator Engine**. It reads the plain-English menu (`.feature` file) and matches each sentence to Java automation code (`@Given`, `@When`, `@Then`). If the menu changes, the test fails!

---

## 2. The 5 Core Building Blocks of Cucumber BDD

| Term | What It Is | Real-World Analogy | Purpose in Testing |
| :--- | :--- | :--- | :--- |
| **`Feature`** | High-level business capability or epic. | The title of a restaurant menu section (e.g. "Breakfast Specials"). | Groups related user stories into a single `.feature` file. |
| **`Scenario`** | A concrete user journey or business rule example. | A customer ordering a specific dish ("Ordering Avocado Toast with Extra Cheese"). | A single automated test execution. |
| **`Scenario Outline` + `Examples:`** | A parameterized test template that loops over data rows. | A combo meal formula with pick-your-own beverage and sides. | Running the exact same test with 10 different usernames, roles, or prices. |
| **Step Definition** | Java method annotated with `@Given`, `@When`, or `@Then`. | The chef's recipe instructions executing behind the scenes. | Translates plain Gherkin text into Selenium or REST Assured calls. |
| **PicoContainer DI** | Lightweight Dependency Injection container for Cucumber. | A shared service cart pushed between kitchen stations. | Passes state (tokens, user IDs) between step definition classes safely without static variables. |

---

## 3. Beginner Code Walkthrough: Clean Step Definition & State Sharing

### Step 1: Feature File (`login.feature`)
```gherkin
Feature: Customer Authentication
  As an online shopper
  I want to log in with valid credentials
  So that I can view my personal orders

  Scenario Outline: Successful login with valid roles
    Given the user navigates to the login page
    When the user enters username "<username>" and password "<password>"
    Then the user should see the welcome dashboard for "<role>"

    Examples:
      | username          | password   | role     |
      | alice@shop.com    | Secret123! | CUSTOMER |
      | bob_admin@shop.com| Admin999!  | ADMIN    |
```

### Step 2: Java Step Definition (`LoginSteps.java`)
```java
package com.example.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;

public class LoginSteps {

    // 🌟 Best Practice: Shared TestContext injected via PicoContainer constructor!
    private final TestContext context;

    public LoginSteps(TestContext context) {
        this.context = context;
    }

    // Modern Cucumber Expression: {string} automatically extracts quoted text!
    @Given("the user navigates to the login page")
    public void navigateToLoginPage() {
        context.driver.get("https://shop.example.com/login");
    }

    @When("the user enters username {string} and password {string}")
    public void enterCredentials(String username, String password) {
        context.driver.findElement(By.id("user-input")).sendKeys(username);
        context.driver.findElement(By.id("pass-input")).sendKeys(password);
        context.driver.findElement(By.id("login-btn")).click();
        context.currentUser = username; // Store in shared context
    }

    @Then("the user should see the welcome dashboard for {string}")
    public void verifyDashboard(String expectedRole) {
        String welcomeText = context.driver.findElement(By.id("welcome-msg")).getText();
        Assertions.assertTrue(welcomeText.contains(expectedRole));
    }
}
```

---

## 4. What Happens When Things Break? (Top 3 Disasters)

1. **`DuplicateStepDefinitionException`:**
   Two different Java classes define a method matching the exact same Gherkin phrase (e.g. `@Given("the user is on home page")`). Cucumber refuses to guess which one to run and immediately aborts the test suite! **Fix:** Consolidate common navigation steps into a shared `NavigationSteps` class.
2. **`UndefinedStepException` (Snippet Warnings):**
   A typo exists in the `.feature` file (e.g., `"user logs in"` vs `"user logs into account"`). Cucumber cannot find a matching Java method and marks the test as undefined or skipped! **Fix:** Run with `dryRun = true` in `@CucumberOptions` to validate syntax in 1 second before booting browsers.
3. **The Static Variable Parallel Corruption Disaster:**
   Developers store the `WebDriver` or `authToken` in a `public static WebDriver driver;` field. When tests run across 4 parallel threads, **Thread B overwrites Thread A's browser handle**, causing massive random flakiness and crashes! **Fix:** Use Cucumber's PicoContainer DI or `ThreadLocal<WebDriver>`.

---

## 5. Top 5 Beginner Mistakes in Production

1. **Writing Technical Code in Gherkin:** Writing steps like `When user clicks on div with xpath "//button[@id='submit']"`. Gherkin must express **business behavior**, not technical HTML locators!
2. **Using Static Variables to Share State:** Using static fields across step classes breaks parallel test execution immediately.
3. **Putting Assertions Inside `@When` Steps:** `@When` represents an action; `@Then` represents the verification. Putting assertions in `@When` violates BDD semantics.
4. **Massive Monolithic Feature Files:** Putting 50 unrelated scenarios into one `.feature` file. Feature files should be small, focused, and taggable (e.g., `@Smoke`, `@Regression`, `@Payment`).
5. **Ignoring `Background:` for Common Preconditions:** Repeating `"Given user is logged in"` across 20 individual scenarios instead of declaring it once in `Background:`.

---

## 6. Top 10 Junior Interview Questions (With "Explain Like I'm 5" Answers)

### Q1: What is the difference between BDD and TDD?

- **ELI5 Answer:** *"`TDD` is an engineer testing their own engine bolts with a wrench (written in code for developers). `BDD` is the driver, owner, and mechanic reading a user manual together to agree on how the car should drive on the highway."*
- **Technical Answer:** *"`TDD` (Test Driven Development) focuses on unit-level implementation tests written in code (`JUnit`) by developers before writing production code. `BDD` (Behavior Driven Development) extends TDD to high-level system behavior using a shared, human-readable DSL (Gherkin) so product managers, QA, and developers collaborate."*

### Q2: What is the difference between `Scenario` and `Scenario Outline`?

- **ELI5 Answer:** *"`Scenario` is ordering 1 pizza for yourself. `Scenario Outline` is a pizza order form template with an `Examples:` table listing 5 different toppings and crust sizes to bake 5 pizzas."*
- **Technical Answer:** *"`Scenario` runs a single test flow with hardcoded values. `Scenario Outline` is a parameterized test template that replaces `<placeholders>` with values from an `Examples:` table, executing once for every row in the table."*

### Q3: What is the purpose of `Background:` in a feature file?

- **ELI5 Answer:** *"Washing your hands before every meal: instead of reminding you on every page of the recipe, the rule is at the top of the kitchen wall."*
- **Technical Answer:** *"`Background:` defines steps that run automatically before **every single scenario** in that feature file. It eliminates boilerplate repetition of common preconditions (like logging in or setting up database records)."*

### Q4: What is the difference between Cucumber Expressions and Regular Expressions?

- **ELI5 Answer:** *"`Cucumber Expressions` use simple words like `{int}` and `{string}` that anyone can read. `Regex` uses cryptic symbols like `^([0-9]+)$` that look like a cat walked on the keyboard."*
- **Technical Answer:** *"Cucumber Expressions (v3+) provide readable, type-safe parameter matching: `{int}` converts to `Integer`, `{string}` extracts quoted text, and custom parameter types can parse directly to domain objects (e.g., `{money}`). Regex (`^...$`) uses raw pattern groups."*

### Q5: How do you share data between multiple step definition classes?

- **ELI5 Answer:** *"Using a shared backpack: instead of pinning notes to a public bulletin board (static variables), you hand a backpack (`PicoContainer`) to every worker entering the room."*
- **Technical Answer:** *"Using Cucumber's built-in **PicoContainer** dependency injection. By declaring a shared `TestContext` class as a constructor argument in your step classes, PicoContainer automatically instantiates and injects a single per-scenario context instance, guaranteeing isolation in parallel runs."*

### Q6: What are Hooks in Cucumber and how do they differ from `Background:`?

- **ELI5 Answer:** *"`Background:` is visible on the printed menu for everyone to read. `Hooks` (`@Before`, `@After`) are invisible kitchen operations (like turning on the ovens before opening) that business customers don't need to see."*
- **Technical Answer:** *"`Background:` consists of Gherkin steps visible in feature files and reports. `Hooks` (`@Before`, `@After`, `@BeforeStep`) are invisible Java methods executed by Cucumber to handle technical setup/teardown (starting browser drivers, clearing caches, capturing failure screenshots)."*

### Q7: How do you attach a screenshot to the Cucumber report upon scenario failure?

- **ELI5 Answer:** *"Setting an automatic camera trap: if an error happens, snap a photo and staple it directly to the incident report."*
- **Technical Answer:** *"Inside an `@After` hook, inspect `scenario.isFailed()`. If true, cast the driver to `TakesScreenshot`, capture bytes via `getScreenshotAs(OutputType.BYTES)`, and call `scenario.attach(bytes, "image/png", "Failure Screenshot")`."*

### Q8: What does `dryRun = true` in `@CucumberOptions` do?

- **ELI5 Answer:** *"A spell-check scan that reads your homework in 1 second without submitting it, telling you if you missed any answers."*
- **Technical Answer:** *"`dryRun = true` parses feature files and validates that every Gherkin step has a corresponding Java step definition without actually executing any test code or launching browsers. It runs in milliseconds."*

### Q9: What is the difference between Data Tables and Scenario Outline Examples?

- **ELI5 Answer:** *"`Examples:` runs the whole scenario 5 times (1 time per row). `Data Table` passes a list of 5 items into a single step all at once (like handing someone a shopping list)."*
- **Technical Answer:** *"`Examples:` drives test iteration—each row executes the entire scenario as a separate test run. A `DataTable` is passed as an argument to a **single step** (e.g. `Given the following users exist:`), mapping directly to `List<User>` or `Map<String, String>`."*

### Q10: How do you execute Cucumber tests in parallel safely?

- **ELI5 Answer:** *"Giving each cook their own cutting board and knife so nobody grabs the same onion at the same time."*
- **Technical Answer:** *"Configure parallel execution in JUnit 5 (`junit-platform.properties` with `cucumber.execution.parallel.enabled=true`). To ensure thread safety, each thread must use an isolated `WebDriver` instance managed via `ThreadLocal` or PicoContainer scoped per scenario."*

---

## 📜 1. Core Gherkin Syntax & Grammar

Gherkin uses plain-text formatting with structured keywords:

| Keyword | Description | Real-World Purpose |
| :--- | :--- | :--- |
| `Feature:` | High-level business feature or user story | Provides context (e.g., "User Authentication") |
| `Background:` | Steps executed before **every** scenario in the file | Sets up common state (e.g., "Given the user is on login page") |
| `Scenario:` | A concrete business rule example | Tests a single flow (e.g., "Successful login with valid credentials") |
| `Scenario Outline:` | A parameterized test template | Runs the exact same scenario with multiple data rows |
| `Examples:` | The data table for a `Scenario Outline` | Supplies parameters mapped to `<variable>` placeholders |
| `Given` | Preconditions or initial context | "Given user has $500 balance" |
| `When` | The action or event triggered | "When user transfers $200 to Alice" |
| `Then` | Expected outcome or assertion | "Then balance should be $300" |
| `And` / `But` | Syntactic sugar to chain multiple steps | "And a confirmation SMS is sent" |
| `DocStrings` | Triple quotes `"""` for multiline text / JSON | Passing raw JSON request bodies |
| `Data Tables` | Tabular data passed directly into a step | Passing lists or maps of test entities |

### Example Feature File: `shopping_cart.feature`
```gherkin
@Regression @Cart
Feature: Shopping Cart & Discount Processing
  As a customer
  I want to add items to my cart and apply coupon codes
  So that I can purchase items with discounts

  Background:
    Given the product catalog contains the following items:
      | sku      | name             | price  |
      | LAPTOP01 | MacBook Pro 16   | 2499.0 |
      | MOUSE01  | Magic Mouse      | 99.0   |
      | HEADSET  | AirPods Max      | 549.0  |

  @Smoke
  Scenario: Add single item to cart
    Given the user is logged in as "john.doe@example.com"
    When the user adds "LAPTOP01" with quantity 1 to the cart
    Then the cart item count should be 1
    And the total cart amount should be 2499.0

  Scenario Outline: Applying promotional discount coupons
    Given the user has added "<sku>" to their shopping cart
    When the user applies coupon code "<code>"
    Then the final price should be <discountedPrice>
    And the coupon status should be "<status>"

    Examples:
      | sku      | code        | discountedPrice | status  |
      | LAPTOP01 | SAVE10      | 2249.10         | APPLIED |
      | MOUSE01  | FLAT20      | 79.0            | APPLIED |
      | HEADSET  | INVALID_EXP | 549.0           | EXPIRED |
```

---

## 🎯 2. Cucumber Expressions vs. Regular Expressions

Modern Cucumber (v3+) uses **Cucumber Expressions** by default because they are readable and type-safe.

| Type | Cucumber Expression | Regex Equivalent | Matches Example |
| :--- | :--- | :--- | :--- |
| Integer | `{int}` | `(-?\\d+)` | `42`, `-5` |
| Floating Point | `{float}` | `(-?\\d+(?:\\.\\d+)?)` | `19.99`, `0.5` |
| String (Quoted) | `{string}` | `"([^"\\]*(\\.[^"\\]*)*)"` | `"MacBook Pro"` |
| Word (No space) | `{word}` | `(\\S+)` | `active`, `pending` |
| Anonymous | `{}` | `(.*)` | Matches any text |
| Custom Objects | `{product}` | Custom Registered Type | Converts JSON/String to POJO |

### Example Comparison
```java
// ✅ Modern: Cucumber Expression (Clean, Readable)
@Given("user has {int} items in their cart with total {float}")
public void userHasItemsInCart(int count, float total) {
    cartService.initCart(count, total);
}

// ⚠️ Legacy: Regex (Complex, harder to maintain)
@Given("^user has (\\d+) items in their cart with total (\\d+\\.\\d+)$")
public void userHasItemsInCartRegex(int count, double total) {
    cartService.initCart(count, total);
}
```

---

## ☕ 3. Java Step Definitions & State Management

### 3.1 Step Definition Implementation
```java
package com.example.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.junit.jupiter.api.Assertions;

public class CartStepDefinitions {
    private final TestContext context; // Injected via PicoContainer

    public CartStepDefinitions(TestContext context) {
        this.context = context;
    }

    @Given("the user is logged in as {string}")
    public void userIsLoggedIn(String email) {
        context.setCurrentUser(userService.authenticateByEmail(email));
    }

    @When("the user adds {string} with quantity {int} to the cart")
    public void userAddsItem(String sku, int quantity) {
        context.getCart().addItem(sku, quantity);
    }

    @Then("the cart item count should be {int}")
    public void verifyCartCount(int expectedCount) {
        Assertions.assertEquals(expectedCount, context.getCart().getTotalItems());
    }

    @And("the total cart amount should be {float}")
    public void verifyCartAmount(float expectedAmount) {
        Assertions.assertEquals(expectedAmount, context.getCart().getTotalPrice(), 0.01);
    }
}
```

### 3.2 State Sharing with PicoContainer (No Static Variables!)
Never use `static` fields to pass state between step definition classes—it causes race conditions during parallel test runs. Use `cucumber-picocontainer`:

```java
// TestContext.java (Shared across step definitions)
public class TestContext {
    private User currentUser;
    private ShoppingCart cart = new ShoppingCart();
    private Response apiResponse;

    // Getters and Setters
}
```

---

## 🪝 4. Execution Lifecycle & Hooks Architecture

Hooks run automatically before and after scenarios or steps to manage test setup and teardown.

```mermaid
sequenceDiagram
    participant Runner as Test Runner
    participant Hook as @Before / @After Hooks
    participant Step as Step Definitions

    Runner->>Hook: @Before (Order = 1: DB Clean)
    Runner->>Hook: @Before (Order = 2: Browser Init)
    loop Every Step
        Runner->>Hook: @BeforeStep
        Runner->>Step: Execute Gherkin Step
        Runner->>Hook: @AfterStep
    end
    Runner->>Hook: @After (Capture Screenshot on Failure)
    Runner->>Hook: @After (Teardown Browser)
```

### Hook Implementation Code
```java
package com.example.bdd.hooks;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class TestHooks {
    private final TestContext context;

    public TestHooks(TestContext context) {
        this.context = context;
    }

    // 1. Tagged Hook with Order (Runs only for @Database tagged scenarios)
    @Before(value = "@Database", order = 1)
    public void resetDatabaseState() {
        databaseService.truncateAllTables();
        databaseService.seedReferenceData();
    }

    // 2. Global Setup Hook
    @Before(order = 10)
    public void initializeDriver(Scenario scenario) {
        System.out.println("Starting scenario: " + scenario.getName());
    }

    // 3. Teardown Hook with Screenshot Capture on Failure
    @After(order = 1)
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            WebDriver driver = context.getDriver();
            if (driver != null) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Failure_Screenshot");
            }
        }
        if (context.getDriver() != null) {
            context.getDriver().quit();
        }
    }
}
```

---

## 📊 5. Advanced Test Data: Data Tables & Custom Type Registry

### 5.1 Mapping Data Tables to Java POJOs
```gherkin
Given the following users exist in the system:
  | email              | role       | active |
  | admin@fintech.com  | SUPER_ADMIN| true   |
  | dev@fintech.com    | DEVELOPER  | true   |
  | user@fintech.com   | GUEST      | false  |
```

```java
public record UserDTO(String email, String role, boolean active) {}

// Step Definition: Cucumber automatically transforms the table into List<UserDTO>
@Given("the following users exist in the system:")
public void createUsers(List<UserDTO> users) {
    users.forEach(user -> userService.createUser(user.email(), user.role(), user.active()));
}
```

---

## 🧪 6. 5+ Enterprise Testing Scenarios with Full Code

### 🧩 Scenario 1: REST API Testing with RestAssured & Cucumber
**Problem:** Test an Order Creation REST API endpoint including status code, JSON path validation, and auth header.

```gherkin
@API @Orders
Scenario: Create a new customer order via REST API
  Given user has valid bearer authentication token
  When the user sends a POST request to "/api/v1/orders" with payload:
    """
    {
      "productId": "PROD-998",
      "quantity": 2,
      "currency": "USD"
    }
    """
  Then the response status code should be 201
  And the response body field "status" should be "CREATED"
  And the response body should contain a valid UUID "orderId"
```

```java
public class OrderApiSteps {
    private Response response;
    private String authToken;

    @Given("user has valid bearer authentication token")
    public void authenticate() {
        this.authToken = AuthUtil.generateTestToken();
    }

    @When("the user sends a POST request to {string} with payload:")
    public void sendPostRequest(String endpoint, String jsonPayload) {
        this.response = RestAssured.given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(jsonPayload)
            .post(endpoint);
    }

    @Then("the response status code should be {int}")
    public void verifyStatusCode(int expectedCode) {
        response.then().statusCode(expectedCode);
    }

    @And("the response body field {string} should be {string}")
    public void verifyField(String jsonPath, String expectedValue) {
        response.then().body(jsonPath, org.hamcrest.Matchers.equalTo(expectedValue));
    }
}
```

---

### 🧩 Scenario 2: Dynamic Data Table with Calculations & Summaries
**Problem:** Verify that a multi-currency tax calculation engine computes tax for different US states accurately.

```gherkin
Scenario: Multi-state sales tax calculation
  When the invoice is calculated for the following line items:
    | item         | state | netAmount | taxRate |
    | Cloud Server | CA    | 1000.00   | 0.0725  |
    | Domain Name  | NY    | 50.00     | 0.08875 |
    | SSL Cert     | TX    | 120.00    | 0.0625  |
  Then the computed total tax should be 84.44
```

---

### 🧩 Scenario 3: Database Verification After Async Event (Polling Assertion)
**Problem:** After placing an order, Kafka fires an event. The notification worker asynchronously writes a record to `notification_audit` table. We need a polling assertion.

```java
@Then("a notification record for user {string} should appear in database within {int} seconds")
public void verifyDatabaseRecordWithPolling(String userEmail, int timeoutSeconds) {
    org.awaitility.Awaitility.await()
        .atMost(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
        .pollInterval(500, java.util.concurrent.TimeUnit.MILLISECONDS)
        .until(() -> auditRepository.existsByUserEmail(userEmail));
}
```

---

## 🏃 7. Test Runners & Parallel Execution

### 7.1 Modern JUnit 5 Platform Suite Runner
```java
package com.example.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.example.bdd.steps,com.example.bdd.hooks")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-reports/report.html, json:target/cucumber-reports/cucumber.json")
public class RunCucumberTest {
}
```

### 7.2 Parallel Execution in `pom.xml`
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <parallel>methods</parallel>
        <threadCount>4</threadCount>
        <properties>
            <configurationParameters>
                cucumber.execution.parallel.enabled=true
                cucumber.execution.parallel.config.strategy=fixed
                cucumber.execution.parallel.config.fixed.parallelism=4
            </configurationParameters>
        </properties>
    </configuration>
</plugin>
```

---

## ⚖️ 8. Gherkin & Cucumber Cheat Sheet

| Command / Annotation | Description | Best Practice |
| :--- | :--- | :--- |
| `@Before(order = n)` | Runs before each scenario | Use low numbers for DB/Env init, high for browser setup |
| `@After` | Runs after each scenario | Always check `scenario.isFailed()` to attach failure screenshots |
| `@BeforeStep` | Runs before every step | Useful for network tracing or UI highlighting |
| `Scenario.attach(byte[], mime, name)` | Embeds media into HTML report | Attach screenshots, API payloads, or logs |
| `Scenario Outline` + `Examples` | Data-driven testing | Keep tables clean; do not exceed 10 columns |
| `cucumber.execution.parallel.enabled` | Multi-threaded test execution | Use PicoContainer for thread-safe context |
| `tags = "not @Ignored and (@Smoke or @Regression)"` | Tag expressions | Filter tests in CI/CD pipeline |

---

## 🎓 9. Senior Cucumber BDD Interview Preparation & Scenario Q&A

### 📌 Core Conceptual Interview Questions

#### Q1: How does PicoContainer manage state sharing across step definitions during parallel execution?
> **Answer & Explanation:**
> - In modular BDD frameworks, step definitions are split across multiple classes (e.g., `UserSteps.java`, `CartSteps.java`, `PaymentSteps.java`).
> - **Anti-Pattern:** Using `public static TestContext context` causes severe race conditions and data corruption when running scenarios concurrently.
> - **Solution (PicoContainer DI):** PicoContainer creates a new instance of `TestContext` **per scenario** and constructor-injects the exact same scenario-scoped instance into all step classes participating in that scenario.
> - When the scenario ends, PicoContainer automatically disposes of the context, guaranteeing complete thread isolation without manual cleanup.

#### Q2: How do you handle Custom Objects in Data Tables using `@DataTableType`?
> **Answer & Explanation:**
> - Rather than manually parsing `Map<String, String>` inside step definitions, define a custom transformer:
> ```java
> @DataTableType
> public UserAccount userAccountEntry(Map<String, String> entry) {
>     return new UserAccount(
>         entry.get("Username"),
>         entry.get("Role"),
>         Double.parseDouble(entry.get("Balance"))
>     );
> }
> ```
> - Cucumber will automatically deserialize rows directly into `List<UserAccount> users` as a strongly typed step argument.

#### Q3: What is the difference between `Background:` and `@Before` hooks?
> **Answer & Explanation:**
> - `Background:` is visible in the `.feature` file and read by business stakeholders. It should only contain **business-meaningful steps** (e.g., `Given the user has an active premium subscription`).
> - `@Before` is invisible technical code in Java. It should be used exclusively for **infrastructure setup** (e.g., opening a browser instance, creating a DB transaction, initializing wiremock stubs).

---

### 🚨 Real-World Scenario-Based Interview Questions

#### Scenario Q1: Flaky Asynchronous Order Processing in BDD
> **Interviewer Question:** *"In our e-commerce BDD suite, the step `Then the order status should be 'COMPLETED'` fails randomly because the backend processes payments asynchronously via Kafka. Adding `Thread.sleep(5000)` makes our CI pipeline take 2 hours. How do you design robust asynchronous verification?"*
>
> **Senior Architect Answer:**
> - Never use static `Thread.sleep()`.
> - Use **Awaitility** with polling and exponential backoff:
> ```java
> @Then("the order status should be {string}")
> public void verifyOrderStatus(String expectedStatus) {
>     org.awaitility.Awaitility.await()
>         .atMost(Duration.ofSeconds(10))
>         .pollInterval(Duration.ofMillis(300))
>         .untilAsserted(() -> {
>             OrderDTO order = orderApiClient.getOrder(testContext.getOrderId());
>             org.junit.jupiter.api.Assertions.assertEquals(expectedStatus, order.getStatus());
>         });
> }
> ```

---

## 🔄 10. Architectural Transferability: Where & How to Apply Elsewhere

1. **Enterprise Living Documentation (Specification by Example):** Generating automated HTML/PDF compliance reports for regulated industries (Healthcare FDA CFR 21, Financial PCI-DSS audit trails).
2. **Contract Testing & API Acceptance Suites:** Validating OpenAPI / gRPC microservice contracts against human-readable business rules before production deployment.
3. **Cross-Platform Test Automation:** Sharing identical Gherkin feature files between Web (Selenium), Mobile (Appium), and Backend (REST Assured) test automation engines.

---

[⬆️ Back to Top](#-cucumber-bdd-complete-architecture-syntax--enterprise-test-automation)

