[? Back to Home](README.md)

# 🏎️ Selenium WebDriver: Architecture & Concepts

Start automating with the W3C standard for browser automation.

## 1. Selenium Architecture
The core foundation of how Selenium communicates with browsers.

1.  **WebDriver Protocol**: The W3C standard for browser communication.
2.  **Browser Drivers**: Executables like `chromedriver` or `geckodriver` that bridge your code and the browser.
3.  **Client Libraries**: Language bindings (Java, Python, C#, Ruby, JavaScript).
4.  **JSON Wire Protocol**: The legacy communication method (pre-W3C).
5.  **Session ID**: The unique ID generated for every browser instance to track the session.
6.  **RemoteWebDriver**: The base class for all browser driver implementations.
7.  **SearchContext**: The top-level interface for `findElement` and `findElements`.
8.  **Local vs Remote Execution**: Running on a local machine vs. a remote Selenium Grid.
9.  **Command Executor**: The internal module that sends HTTP requests to the driver.
10. **W3C Compliance**: Modern drivers use a standardized handshake for better stability.

## 2. Browser Management
Controlling the browser window and navigation.

16. **WebDriver Interface**: The main control interface.
17. **ChromeDriver Implementation**: Specific implementation for Google Chrome.
18. **ChromeOptions**: Class used to define browser settings (arguments, extensions).
19. **Headless Mode**: Running the browser without a GUI (useful for CI/CD).
20. **Incognito Mode**: Running without saving history or cookies.
21. **Navigation**: `driver.get("url")` (waits for load) vs `driver.navigate().to("url")` (history).
22. **Back/Forward**: `driver.navigate().back()` and `forward()`.
23. **Refresh**: `driver.navigate().refresh()`.
24. **Window Handles**: Managing multiple tabs/windows via unique IDs (`getWindowHandle()`).
25. **Window Switching**: `driver.switchTo().window(id)`.

## 3. The 8 Basic Locators
Strategies to identify elements on the page.

31. **ID**: Targeted via `By.id("value")` (Fastest).
32. **Name**: Targeted via `By.name("value")`.
33. **Class Name**: Targeted via `By.className("value")`.
34. **Tag Name**: Targeted via `By.tagName("value")`.
35. **Link Text**: Exact match for anchor tags (`<a>`).
36. **Partial Link Text**: Partial match for anchor tags.
37. **CSS Selector**: Uses CSS patterns (e.g., `#id`, `.class`, `tag[attr='val']`).
38. **XPath**: XML Path language for traversing the HTML DOM.
39. **Absolute XPath**: Path starting from the root node (fragile).
40. **Relative XPath**: Path starting from a specific node using `//` (robust).
41. **Contains()**: XPath function for partial attribute matching (e.g., `//div[contains(@class,'btn')]`).
42. **Text()**: XPath function for matching inner text (`//span[text()='Login']`).
43. **Sibling Axes**: `following-sibling` and `preceding-sibling`.
44. **Parent Axes**: Navigating up to the parent element (`/..`).
45. **Descendant Axes**: Navigating deep into child elements.

## 4. Synchronization
Handling dynamic content and page loads.

51. **Wait Interface**: The base interface for all waits.
52. **Implicit Wait**: A global, default poll for elements before throwing `NoSuchElementException`.
53. **Explicit Wait**: Waiting for a specific condition using `WebDriverWait`.
54. **ExpectedConditions**: Predefined rules like `visibilityOfElementLocated`, `elementToBeClickable`.
55. **Fluent Wait**: Custom wait with polling intervals and ignored exceptions.
56. **Polling Interval**: How often Selenium checks for the element (default 500ms).
57. **TimeoutException**: Triggered when the wait period expires.

## 5. User Interactions & Elements
Simulating user actions beyond simple clicks.

66. **Actions Class**: For complex mouse and keyboard sequences (Builder pattern).
67. **MoveToElement**: Performing a mouse hover action.
68. **DragAndDrop**: Moving an element from source to target.
69. **ContextClick**: Performing a right-click.
70. **DoubleClick**: Performing a double mouse click.
71. **Select Class**: Specialized class for HTML `<select>` (dropdowns).
72. **Deselect methods**: Removing selections in multi-select boxes.
73. **Alert Interface**: Handling browser pop-ups (`accept()`, `dismiss()`).
74. **Prompt Handling**: Entering text into an alert prompt (`sendKeys()`).
75. **KeyBoard Keys**: Using `Keys.ENTER`, `Keys.TAB`, etc.

## 6. JavaScript & Frames
Advanced control and handling special structures.

86. **JavascriptExecutor**: The interface to run arbitrary JS in the browser context.
87. **ExecuteScript**: Running synchronous JavaScript.
88. **ExecuteAsyncScript**: Running asynchronous JavaScript (waits for callback).
89. **ScrollIntoView**: Scrolling the page until an element is visible via JS.
90. **SwitchTo Frame**: Navigating inside an `<iframe>`.
91. **DefaultContent**: Returning to the main HTML from a frame.
92. **ParentFrame**: Moving one level up in nested frames.
93. **Shadow DOM**: Handling elements inside shadow roots (requires special locators).
94. **Hidden Elements**: Locating or interacting with elements having `display: none`.
95. **StaleElementReferenceException**: Handling elements that have been refreshed/detached in the DOM.

## 7. Page Object Model & Design (POM)
Design patterns for maintainable test code.

101. **POM Pattern**: Separating Locators (Model) from Test Logic.
102. **PageFactory**: Built-in initialization engine for Page Objects (deprecated in later versions but common).
103. **@FindBy**: Annotation to define locators in class fields.
104. **@FindBys / @FindAll**: Combining multiple criteria for stricter or looser matches.
105. **@CacheLookup**: Performance optimization to cache elements (use for static elements).
106. **InitElements**: Static method to initialize the proxy elements.

## 8. Framework Components
Building a robust test automation framework.

116. **TakesScreenshot**: Interface for capturing the browser view.
117. **OutputType.FILE**: Saving screenshots to the disk.
118. **Cookie Management**: Adding, getting, and deleting browser cookies (`manage().cookies()`).
119. **Selenium Grid**: Running tests on a distributed hub-and-node network.
120. **DesiredCapabilities**: Legacy way of configuring browser/OS for Grid testing (replaced by Options).
121. **Event Listeners**: Implementing `WebDriverListener` for logging and reporting.
122. **Headless Chrome/Firefox**: Running tests without UI for speed and CI/CD.
123. **Profiles**: Loading custom user profiles to persist settings.
124. **Service Objects**: Using `ChromeDriverService` for lower-level driver process control.
125. **File Upload**: Handling `<input type='file'>` via simple `sendKeys("path/to/file")`.
