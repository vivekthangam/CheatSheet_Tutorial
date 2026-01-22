# 🥒 Cucumber BDD: Advanced Concepts & Internals

This guide covers over 100 concepts related to Behavior Driven Development (BDD) using Cucumber, focusing on syntax, execution lifecycle, and framework integration.

---

## 1. Core Gherkin & Syntax Concepts
Gherkin is the domain-specific language (DSL) used to define behaviors.

1.  **Feature**: The high-level description of the software requirement.
2.  **Scenario**: A concrete example of a business rule.
3.  **Given**: Sets the initial context or preconditions.
4.  **When**: Describes the key action or event.
5.  **Then**: Asserts the expected outcome or post-condition.
6.  **And**: Syntactic sugar to repeat the previous keyword.
7.  **But**: Syntactic sugar for a negative assertion in a sequence.
8.  **Background**: Common steps that run before *every* scenario in a file.
9.  **Scenario Outline**: A template for running the same scenario with multiple data rows.
10. **Examples**: The table used to provide data for a Scenario Outline.
11. **DocStrings**: Triple quotes (`"""`) used to pass large blocks of text to a step.
12. **Data Tables**: Inline tables within a step for passing complex data structures.
13. **Step Parameters**: Using `{string}`, `{int}`, or `{float}` to capture variables.
14. **Cucumber Expressions**: The modern, readable way to map text to code.
15. **Regular Expressions (Regex)**: The classic way to map text (e.g., `^I have (\\d+) items$`).
16. **Tags**: Metadata (e.g., `@Smoke`, `@Regression`) to filter test execution.
17. **Comments**: Lines starting with `#`.
18. **Gherkin Dialects**: Support for 70+ languages (e.g., `# language: fr`).
19. **Feature Description**: The text block immediately following the `Feature:` header.
20. **Scenario Description**: The text block immediately following the `Scenario:` header.

---

## 2. Execution Lifecycle & Hooks
Understanding how Cucumber handles the "World" of a test execution.

21. **Test Runner**: The engine (JUnit/TestNG) that triggers execution.
22. **Glue Path**: The configuration that tells Cucumber where to find Step Definitions.
23. **Step Definition**: The actual method code that executes Gherkin steps.
24. **Hooks**: Code blocks that run at specific points in the lifecycle.
25. **@Before**: Runs before every scenario.
26. **@After**: Runs after every scenario.
27. **@BeforeStep**: Runs before every single step.
28. **@AfterStep**: Runs after every single step.
29. **Tagged Hooks**: Hooks that only run for specific tags (e.g., `@Before("@Database")`).
30. **Hook Order**: Using the `order` attribute to sequence multiple hooks.
31. **Scenario Object**: Injected into hooks to check status (`scenario.isFailed()`).
32. **World Object**: The shared state/context for a single scenario execution.
33. **Dry Run**: Checking if every step has a matching definition without running code.
34. **Monochrome**: Formatting console output for better readability.
35. **Strict Mode**: Failing the execution if undefined or pending steps exist.
36. **Snippets**: Auto-generated code templates for missing steps.
37. **Object Factory**: The internal engine that instantiates Step Definition classes.
38. **Pickle**: The internal representation of a Gherkin scenario during execution.
39. **Pickle Step**: An individual step within a Pickle.
40. **Execution Order**: Scenarios usually run in the order they appear in the file.

---

## 3. Dependency Injection & State Management
Managing data across different step definition classes.

41. **PicoContainer**: The default lightweight DI container for Cucumber.
42. **Spring Integration**: Using `@ContextConfiguration` to share Spring beans.
43. **Guice Integration**: Using Google Guice for dependency management.
44. **State Sharing**: Passing objects between steps using constructors.
45. **Context Inversion**: How Cucumber manages the lifecycle of shared objects.
46. **Scenario Scoping**: Beans are created at the start of a scenario and destroyed at the end.
47. **Thread Safety**: Managing parallel execution state.
48. **Parameter Type Registry**: Defining custom types (e.g., transforming a string to a `User` object).
49. **DataTable Type**: Mapping a table to a list of custom POJOs.
50. **Transformer**: The logic that converts Gherkin text into Java objects.

---

## 4. Advanced Patterns & Architecture
Designing maintainable BDD frameworks.

51. **Page Object Model (POM)**: Separating UI logic from step definitions.
52. **Screenplay Pattern**: An actor-based pattern for complex BDD.
53. **Declarative Style**: Focus on "What" the user does (Better for BDD).
54. **Imperative Style**: Focus on "How" (e.g., click this, wait that) - (Anti-pattern).
55. **Step Library**: Reusable step definitions across multiple features.
56. **Feature Mapping**: Linking features to JIRA or requirement IDs.
57. **Parallel Execution**: Running scenarios in multiple threads via TestNG/JUnit 5.
58. **Rerun Plugin**: Capturing failed scenarios to a text file for immediate re-execution.
59. **Custom Formatters**: Creating your own logic for processing test results.
60. **JSON Reporter**: Generating machine-readable results for CI/CD.
61. **HTML Reporter**: Visualizing test execution for stakeholders.
62. **JUnit Reporter**: XML output for Jenkins/Azure DevOps integration.
63. **Cucumber Reports Plugin**: Third-party plugins for rich graphical reports.
64. **Step Categorization**: Grouping steps into "Setup", "Action", and "Assertion".
65. **Soft Assertions**: Allowing a scenario to continue after a minor failure.
66. **Hard Assertions**: Stopping a scenario immediately on failure.
67. **Exception Translation**: How Cucumber handles and reports Java exceptions.
68. **Wait Strategies**: Implementing explicit/implicit waits inside steps.
69. **Parameter Capture Groups**: `(.*)` or `(\\d+)` in Regex definitions.
70. **Non-Capturing Groups**: `(?:...)` for optional text in Gherkin.

---

## 5. Integration & Tooling
Connecting Cucumber to the wider ecosystem.

71. **Maven/Gradle**: Dependency management for Cucumber libraries.
72. **Cucumber JVM**: The Java implementation of Cucumber.
73. **Cucumber JS**: The JavaScript implementation.
74. **Behave**: The Python equivalent of Cucumber.
75. **SpecFlow**: The .NET equivalent.
76. **Karate DSL**: A tool built on Cucumber for API testing.
77. **RestAssured**: Frequently used *inside* Cucumber steps for API BDD.
78. **Selenium WebDriver**: Frequently used for UI BDD.
79. **Appium**: Used for Mobile BDD scenarios.
80. **Dockerization**: Running Cucumber tests inside containers.
81. **Jenkins Pipeline**: Orchestrating Cucumber tests in CI.
82. **CLI Runner**: Executing Cucumber from the command line.
83. **Plugin System**: The `--plugin` flag for attaching listeners.
84. **Event Publisher**: The internal bus that broadcasts test events (TestStarted, StepFinished).
85. **Type Registry**: The internal map of all custom parameter types.

---

## 6. BDD Principles & Philosophy
The "Why" behind the tool.

86. **The Three Amigos**: Business, Dev, and QA collaborating on features.
87. **Living Documentation**: The idea that feature files stay updated with the code.
88. **Ubiquitous Language**: Using terms that everyone (business and tech) understands.
89. **Discovery Phase**: Brainstorming scenarios before coding.
90. **Formulation Phase**: Turning brain-stormed ideas into Gherkin.
91. **Automation Phase**: Coding the step definitions.
92. **Refinement**: Polishing Gherkin for clarity.
93. **Shared Understanding**: The primary goal of Cucumber.
94. **Outside-In Development**: Coding starts from the user behavior.
95. **Specification by Example (SbE)**: Using concrete examples to define requirements.
96. **Regression Testing**: Using Cucumber to ensure old features don't break.
97. **Acceptance Criteria**: The source material for Feature files.
98. **Given-When-Then (GWT)**: The standard structure for behavioral specifications.
99. **Anti-Patterns**: Avoid "Laptop Testing" (Tests that only work on one machine).
100. **Cucumber Expressions vs Regex**: Preference for readability over complexity.