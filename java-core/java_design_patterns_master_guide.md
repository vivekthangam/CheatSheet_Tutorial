[🏠 Back to Home](../README.md) | [🎯 100 Interview Scenarios (Tier-1 Flagship)](java_design_patterns_100_interview_scenarios.md) | [☕ 500 Production Scenarios](java_500_master_scenarios_coding_interview_guide.md) | [🏛️ SOLID Principles](design_principles_solid_master_guide.md) | [🧵 Concurrency](java_thread.md)

# 🎨 Java Design Patterns: The Zero-Jargon Master Guide

[![Java](https://img.shields.io/badge/Java-17%20%2F%2021%20LTS-orange.svg?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![GoF Patterns](https://img.shields.io/badge/GoF-23%20Patterns-blue.svg?style=for-the-badge)](https://en.wikipedia.org/wiki/Design_Patterns)
[![140+ Patterns](https://img.shields.io/badge/Encyclopedia-140%2B%20Patterns-purple.svg?style=for-the-badge)](https://java-design-patterns.com/patterns/)
[![100 Scenarios](https://img.shields.io/badge/Interview-100%20Scenario%20Q%26As-success.svg?style=for-the-badge)](java_design_patterns_100_interview_scenarios.md)
[![Style](https://img.shields.io/badge/Style-Zero%20Jargon%20Format-brightgreen.svg?style=for-the-badge)](https://github.com/)

> 🚀 **Looking for In-Depth Staff/Principal Engineer Interview Scenarios?**
> Check out the companion flagship guide: **[🎯 100 Tier-1 Design Pattern Scenario-Based Interview Questions & Production Code](java_design_patterns_100_interview_scenarios.md)** covering battle-tested production failures, JMM concurrency traps, microservice dual-write mitigations, and resilient distributed architectures!

---

## 💡 What Are Design Patterns? (Zero-Jargon Introduction)

Imagine you are building a house. You don't reinvent how door hinges work, how water pipes connect, or how roof frames bear weight. Architects and plumbers have figured out the most reliable, leak-free ways to do those things over hundreds of years.

**Design patterns are the standard, time-tested blueprints for solving common software architecture problems.** 
- They are **NOT** ready-made code libraries you download from Maven.
- They are **conceptual templates** showing you how to organize your classes and methods so your code doesn't become a tangled, fragile mess when your project grows.

Design patterns are grouped into **three main families**:
1. **Creational Patterns (5)**: *How do you create objects without hard-coding chaos?*
2. **Structural Patterns (7)**: *How do you assemble classes and objects together like LEGO blocks?*
3. **Behavioral Patterns (11)**: *How do objects communicate, delegate tasks, and react to changes?*

---

## 📑 Master Navigation & Quick-Reference Matrix

| # | Pattern | Family | Real-World Everyday Analogy | When to Use It |
| :- | :--- | :--- | :--- | :--- |
| **01** | [Singleton](#1-singleton-pattern) | Creational | The President of a Country (Only 1 in office) | Global logger, DB connection pool, config cache |
| **02** | [Factory Method](#2-factory-method-pattern) | Creational | Logistics dispatch (Truck vs Ship vs Airplane) | When you don't know the exact class needed until runtime |
| **03** | [Abstract Factory](#3-abstract-factory-pattern) | Creational | Furniture Store Suites (Modern set vs Victorian set) | Creating families of matching products together |
| **04** | [Builder](#4-builder-pattern) | Creational | Subway Sandwich Artist (Ingredient by ingredient) | Constructing complex objects with many optional fields |
| **05** | [Prototype](#5-prototype-pattern) | Creational | Photocopier / Mitosis cell division | Cloning an expensive object instead of re-fetching |
| **06** | [Adapter](#6-adapter-pattern) | Structural | International Travel Power Plug Adapter | Making two incompatible interfaces work together |
| **07** | [Bridge](#7-bridge-pattern) | Structural | Universal TV Remote vs TV Hardware Brands | Letting abstraction and implementation grow independently |
| **08** | [Composite](#8-composite-pattern) | Structural | File System Folders containing files & subfolders | Tree structures where items and groups are treated the same |
| **09** | [Decorator](#9-decorator-pattern) | Structural | Adding toppings, whipped cream & syrups to coffee | Adding new behavior to an object dynamically at runtime |
| **10** | [Facade](#10-facade-pattern) | Structural | Hotel Concierge booking dinner, taxi, and tickets | Giving a simple "one-button" interface to a messy subsystem |
| **11** | [Flyweight](#11-flyweight-pattern) | Structural | Video Game Forest (1,000,000 trees sharing 1 3D model) | Sharing identical memory-heavy data across millions of items |
| **12** | [Proxy](#12-proxy-pattern) | Structural | Security Guard / Credit Card acting for cash | Controlling, delaying, or logging access to an object |
| **13** | [Chain of Responsibility](#13-chain-of-responsibility-pattern) | Behavioral | Customer Support Escalation (Level 1 → 2 → 3) | Passing a request through a pipeline until handled |
| **14** | [Command](#14-command-pattern) | Behavioral | Restaurant Waiter's Order Slip (Undo / Queuing) | Turning an action into a standalone object |
| **15** | [Interpreter](#15-interpreter-pattern) | Behavioral | Musical sheet reader / Math formula evaluator | Evaluating sentences or grammar in a mini-language || **16** | [Iterator](#16-iterator-pattern) | Behavioral | TV Remote Channel Up/Down button | Stepping through items in a list without exposing internal array |
| **17** | [Mediator](#17-mediator-pattern) | Behavioral | Airport Air Traffic Control Tower | Stopping 10 planes from talking directly to each other |
| **18** | [Memento](#18-memento-pattern) | Behavioral | Video Game Checkpoint / Save Game | Saving and restoring object state without breaking privacy |
| **19** | [Observer](#19-observer-pattern) | Behavioral | YouTube Channel Subscriber Notifications | Alerting multiple watchers automatically when state changes |
| **20** | [State](#20-state-pattern) | Behavioral | Vending Machine / Traffic Light | Changing object behavior completely when internal state shifts |
| **21** | [Strategy](#21-strategy-pattern) | Behavioral | Google Maps route (Car vs Walking vs Bike vs Train) | Swapping different algorithms or payment methods at runtime |
| **22** | [Template Method](#22-template-method-pattern) | Behavioral | Standard Recipe (Bake cake: Prep → Mix → Bake → Frost) | Defining fixed steps while letting subclasses customize details |
| **23** | [Visitor](#23-visitor-pattern) | Behavioral | Tax Inspector visiting different business departments | Adding new operations to existing classes without editing them |

---

# 📦 PART 1: CREATIONAL PATTERNS

---

### 1. Singleton Pattern

#### 💡 The Plain-English Analogy
Think of the **President of a Country** or the **Central Bank**. At any given moment, there can only ever be **one** in charge. If every citizen or department created their own President, you'd have total anarchy.

#### ❓ The Problem It Solves
Sometimes your entire application needs **exactly one shared instance** of something—such as a database connection pool, a hardware print spooler, or an application configuration manager. If 20 different threads create 20 different configuration managers, you waste huge amounts of memory and risk out-of-sync settings.

#### ⚙️ How It Works
1. Make the constructor `private` so nobody outside can write `new MyClass()`.
2. Keep a `private static` variable holding the one-and-only instance.
3. Provide a `public static getInstance()` method that returns this single instance.

#### 🏗️ Key Components & Architectural Roles
- **Singleton Class**: Encapsulates its own single instance creation and lifetime.
- **Private Constructor**: Prevents client code from directly instantiating the class using the `new` operator.
- **Static Instance Holder**: Holds the unique instance reference (lazily or eagerly initialized).
- **Static Accessor (`getInstance`)**: Provides the global point of access, returning the single cached instance.

#### 🔄 Execution Flow
1. Client requests an instance by invoking `DatabaseConnectionManager.getInstance()`.
2. The runtime checks if the nested static `InstanceHolder` class is loaded; if not, the JVM class loader initializes it atomically.
3. The single static instance is returned to the caller.
4. Subsequent callers across any thread receive the exact same reference with zero synchronization overhead.

#### ⚖️ Pros and Cons
- **Pros**:
  - Guaranteed single instance saves memory and system resources.
  - Global, controlled access point for shared state.
  - Lazy initialization: Object is only created when first requested.
- **Cons**:
  - Can make unit testing harder (global state makes tests tightly coupled and order-dependent).
  - Can mask bad design if used as a dumping ground for global variables.
  - Vulnerable to reflection, serialization, and classloader duplication if not guarded.

#### ⚠️ Common Pitfalls & Antipatterns
- **The "God Object" Trap**: Turning a Singleton into a global junk drawer of unrelated utility methods and state.
- **Broken Double-Checked Locking**: Forgetting the `volatile` keyword on the instance field, causing other threads to observe half-initialized objects due to CPU instruction reordering.
- **Mocking Nightmares**: Tightly coupling business services directly to `MySingleton.getInstance()` prevents mocking in unit tests (prefer constructor dependency injection).

#### 🏢 Where You See It in Real Java
- `java.lang.Runtime.getRuntime()`
- `java.awt.Desktop.getDesktop()`
- Spring Beans (Default bean scope is Singleton).

#### 💻 Complete Java Code (Thread-Safe Bill Pugh Holder & Volatile DCL)
```java
// Option A: The Bill Pugh Singleton (The cleanest, fastest, lock-free way in Java)
public class DatabaseConnectionManager {
    // Step 1: Private constructor prevents 'new DatabaseConnectionManager()'
    private DatabaseConnectionManager() {
        System.out.println("Connecting to Database Cluster... [EXPENSIVE OPERATION]");
    }

    // Step 2: Static nested helper class (Only loaded into memory when getInstance() is called)
    private static class InstanceHolder {
        private static final DatabaseConnectionManager INSTANCE = new DatabaseConnectionManager();
    }

    // Step 3: Public global access point
    public static DatabaseConnectionManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void executeQuery(String sql) {
        System.out.println("Executing SQL: " + sql + " via Singleton Instance #" + hashCode());
    }

    public static void main(String[] args) {
        // Both references point to the exact same memory address!
        DatabaseConnectionManager clientA = DatabaseConnectionManager.getInstance();
        DatabaseConnectionManager clientB = DatabaseConnectionManager.getInstance();

        clientA.executeQuery("SELECT * FROM users");
        clientB.executeQuery("SELECT * FROM orders");

        System.out.println("Are both instances identical? " + (clientA == clientB)); // prints true
    }
}
```

---

### 2. Factory Method Pattern

#### 💡 The Plain-English Analogy
Imagine ordering a delivery online. You click "Standard Shipping", and the logistics company's dispatch center automatically decides whether to send a **Van**, a **Delivery Truck**, or a **Bicycle Courier**. You don't build the truck yourself; you just ask the logistics company for a delivery transport.

#### ❓ The Problem It Solves
If your code has `new Van()` scattered across 50 different files, and tomorrow your company replaces Vans with Electric Scooters, you would have to hunt down and change all 50 files. Factory Method centralizes and decouples object creation.

#### ⚙️ How It Works
- Create an interface for the product (`Notification`).
- Create an abstract creator class with a factory method `createNotification()`.
- Subclasses override this method to instantiate the specific concrete product.

#### 🏗️ Key Components & Architectural Roles
- **Product Interface**: Defines the contract for objects the factory method creates.
- **Concrete Products**: Different implementations of the product interface.
- **Creator (Abstract Base)**: Declares the factory method and implements core business workflows relying on the product.
- **Concrete Creators**: Override the factory method to return specific concrete product instances.

#### 🔄 Execution Flow
1. Client instantiates a specific `ConcreteCreator` (e.g., `EmailNotificationFactory`) or obtains it via dependency injection.
2. Client calls the creator's high-level workflow method (`notifyUser(...)`).
3. The workflow method calls `createNotification()` internally (late binding via polymorphism).
4. The concrete creator instantiates and returns the concrete product.
5. The workflow invokes product methods through the uniform `Notification` interface.

#### ⚖️ Pros and Cons
- **Pros**: 
  - Follows Open/Closed Principle: You can add new product types without breaking existing client code.
  - Eliminates tight coupling between client code and concrete product classes (Single Responsibility for object creation).
- **Cons**: 
  - Introduces more classes into your codebase (creator subclass hierarchy).

#### ⚠️ Common Pitfalls & Antipatterns
- **Conflating Simple Factory with Factory Method**: Simple Factory is a single static method containing a `switch` statement; Factory Method uses polymorphic inheritance with subclasses.
- **Subclass Proliferation**: Creating factory subclasses for tiny, trivial variations instead of parameterizing or using modern lambda suppliers.

#### 🏢 Where You See It in Real Java
- `java.util.Calendar.getInstance()`
- `java.nio.charset.Charset.forName("UTF-8")`
- `java.text.NumberFormat.getInstance()`
- Spring's `FactoryBean<T>` interface

#### 💻 Complete Java Code
```java
// 1. The Product Interface
interface Notification {
    void send(String recipient, String message);
}

// 2. Concrete Products
class EmailNotification implements Notification {
    public void send(String recipient, String message) {
        System.out.println("📧 Sending Email to " + recipient + ": " + message);
    }
}

class SmsNotification implements Notification {
    public void send(String recipient, String message) {
        System.out.println("📱 Sending SMS to " + recipient + ": " + message);
    }
}

// 3. Creator with Factory Method
abstract class NotificationFactory {
    // The Factory Method
    public abstract Notification createNotification();

    // Client operation using the created product
    public void notifyUser(String user, String message) {
        Notification notification = createNotification();
        notification.send(user, message);
    }
}

// 4. Concrete Factories
class EmailNotificationFactory extends NotificationFactory {
    public Notification createNotification() {
        return new EmailNotification();
    }
}

class SmsNotificationFactory extends NotificationFactory {
    public Notification createNotification() {
        return new SmsNotification();
    }
}

// Demo
public class FactoryMethodDemo {
    public static void main(String[] args) {
        NotificationFactory emailFactory = new EmailNotificationFactory();
        emailFactory.notifyUser("john@example.com", "Your order has shipped!");

        NotificationFactory smsFactory = new SmsNotificationFactory();
        smsFactory.notifyUser("+1-555-0199", "Your OTP is 492019");
    }
}
```

---

### 3. Abstract Factory Pattern

#### 💡 The Plain-English Analogy
Go to **IKEA**. You want to furnish your living room. You can buy the **Modern Minimalist Furniture Suite** (Modern Chair + Modern Sofa + Modern Coffee Table) or the **Victorian Vintage Suite** (Victorian Chair + Victorian Sofa + Victorian Coffee Table). You don't want to accidentally mix an ornate 18th-century Victorian armchair with an ultra-futuristic glass neon sofa!

#### ❓ The Problem It Solves
When your application needs to create **entire families of matching products** (e.g., Mac UI buttons, checkboxes, and windows vs. Windows OS buttons, checkboxes, and windows), you need a guarantee that components from family A are never accidentally mixed with components from family B.

#### ⚖️ Pros and Cons
- **Pros**:
  - Guarantees 100% compatibility among products in a family.
  - Isolates client code from concrete operating systems or themes.
- **Cons**:
  - Adding a brand-new product type (e.g., adding "Lamp") requires modifying the root factory interface and all its implementations.

#### 🏢 Where You See It in Real Java
- `javax.xml.parsers.DocumentBuilderFactory.newInstance()`
- `javax.xml.transform.TransformerFactory.newInstance()`

#### 💻 Complete Java Code
```java
// Product Families: Button & Checkbox
interface Button { void render(); }
interface Checkbox { void render(); }

// Windows Family
class WindowsButton implements Button {
    public void render() { System.out.println("Rendering fluent-style Windows Button [OK]"); }
}
class WindowsCheckbox implements Checkbox {
    public void render() { System.out.println("Rendering fluent-style Windows Checkbox [X]"); }
}

// Mac Family
class MacButton implements Button {
    public void render() { System.out.println("Rendering sleek Cupertino-style Mac Button (OK)"); }
}
class MacCheckbox implements Checkbox {
    public void render() { System.out.println("Rendering sleek Cupertino-style Mac Checkbox (✓)"); }
}

// The Abstract Factory
interface UIFactory {
    Button createButton();
    Checkbox createCheckbox();
}

// Concrete Factories
class WindowsUIFactory implements UIFactory {
    public Button createButton() { return new WindowsButton(); }
    public Checkbox createCheckbox() { return new WindowsCheckbox(); }
}

class MacUIFactory implements UIFactory {
    public Button createButton() { return new MacButton(); }
    public Checkbox createCheckbox() { return new MacCheckbox(); }
}

public class AbstractFactoryDemo {
    public static void main(String[] args) {
        String currentOS = "Mac"; // or "Windows"
        UIFactory factory = currentOS.equalsIgnoreCase("Mac") ? new MacUIFactory() : new WindowsUIFactory();

        // Client builds an entire consistent screen without knowing concrete types!
        Button btn = factory.createButton();
        Checkbox chk = factory.createCheckbox();
        btn.render();
        chk.render();
    }
}
```

---

### 4. Builder Pattern

#### 💡 The Plain-English Analogy
Think of ordering a **custom sandwich at Subway**. You tell the sandwich artist: 
"9-grain honey oat bread, add turkey, add cheddar cheese, toast it, add lettuce and olives, but **no** onions, and drizzle chipotle southwest sauce." 
You don't want a single giant constructor where you have to pass 20 parameters like `new Sandwich("Oat", "Turkey", "Cheddar", true, true, false, true, false, false, "Chipotle")`!

#### ❓ The Problem It Solves
When a class has 6, 10, or 20 attributes (some mandatory, many optional), using constructors leads to the **Telescoping Constructor Anti-Pattern** (constructors with 10 parameters where you pass `null, null, false, 0` for optional fields). Builder lets you construct objects step-by-step with clean, readable, fluent method chains.

#### ⚖️ Pros and Cons
- **Pros**:
  - Crystal-clear readable code (`.bread("Wheat").addCheese()`).
  - Produces immutable objects (no setters needed on the finished object).
  - Easy to validate invariants before calling `.build()`.
- **Cons**:
  - Requires writing an extra Builder class (or using Project Lombok `@Builder`).

#### 🏢 Where You See It in Real Java
- `java.lang.StringBuilder`
- `java.net.http.HttpRequest.newBuilder()`
- `java.util.Locale.Builder`
- Guava `CacheBuilder`

#### 💻 Complete Java Code
```java
public class Computer {
    // Required parameters
    private final String cpu;
    private final int ramGB;

    // Optional parameters
    private final int storageGB;
    private final boolean hasDedicatedGpu;
    private final boolean isLiquidCooled;

    // Private constructor: Only the Builder can call this
    private Computer(Builder builder) {
        this.cpu = builder.cpu;
        this.ramGB = builder.ramGB;
        this.storageGB = builder.storageGB;
        this.hasDedicatedGpu = builder.hasDedicatedGpu;
        this.isLiquidCooled = builder.isLiquidCooled;
    }

    public String toString() {
        return "Computer [CPU=" + cpu + ", RAM=" + ramGB + "GB, Storage=" + storageGB 
             + "GB, Dedicated GPU=" + hasDedicatedGpu + ", Liquid Cooled=" + isLiquidCooled + "]";
    }

    // Static nested Builder class
    public static class Builder {
        private final String cpu;
        private final int ramGB;
        private int storageGB = 256; // sensible defaults
        private boolean hasDedicatedGpu = false;
        private boolean isLiquidCooled = false;

        public Builder(String cpu, int ramGB) {
            this.cpu = cpu;
            this.ramGB = ramGB;
        }

        public Builder storage(int storageGB) {
            this.storageGB = storageGB;
            return this; // Return 'this' to allow chaining
        }

        public Builder dedicatedGpu(boolean hasGpu) {
            this.hasDedicatedGpu = hasGpu;
            return this;
        }

        public Builder liquidCooled(boolean liquidCooled) {
            this.isLiquidCooled = liquidCooled;
            return this;
        }

        public Computer build() {
            // Validation step
            if (isLiquidCooled && !hasDedicatedGpu) {
                throw new IllegalStateException("Liquid cooling is only allowed with dedicated GPU!");
            }
            return new Computer(this);
        }
    }

    public static void main(String[] args) {
        Computer officePC = new Computer.Builder("Intel i5", 16)
                                .storage(512)
                                .build();

        Computer gamingRig = new Computer.Builder("AMD Ryzen 9", 64)
                                .storage(2048)
                                .dedicatedGpu(true)
                                .liquidCooled(true)
                                .build();

        System.out.println(officePC);
        System.out.println(gamingRig);
    }
}
```

---

### 5. Prototype Pattern

#### 💡 The Plain-English Analogy
Think of a **Photocopier** or biological **cell division (mitosis)**. If you already have a 200-page complex legal contract filled out, and you need a second copy to make just two small revisions, you don't type all 200 pages from scratch. You photocopy the existing document and adjust the few lines you need.

#### ❓ The Problem It Solves
Creating a brand new object can be **very expensive** (e.g. requires 5 database queries, network calls, or parsing large 3D models). If an object with identical or nearly identical state already exists, cloning it is dozens of times faster than running a full initialization from scratch.

#### ⚖️ Pros and Cons
- **Pros**:
  - Drastically speeds up object creation for heavy objects.
  - Hides the complex creation details from clients.
- **Cons**:
  - Deep cloning can be tricky when objects have nested, circular references.

#### 🏢 Where You See It in Real Java
- `java.lang.Object.clone()`
- `java.lang.Cloneable`
- Copy constructors in Java collections (`new ArrayList<>(existingList)`).

#### 💻 Complete Java Code (Clean Copy Constructor Prototype)
```java
import java.util.ArrayList;
import java.util.List;

public class GameCharacter implements Cloneable {
    private String name;
    private int health;
    private List<String> inventory;

    public GameCharacter(String name, int health) {
        this.name = name;
        this.health = health;
        // Pretend this simulates an expensive database query
        this.inventory = new ArrayList<>(List.of("Wooden Shield", "Iron Sword", "Healing Potion"));
    }

    // Prototype Copy Constructor (Deep Copy)
    public GameCharacter(GameCharacter source) {
        this.name = source.name;
        this.health = source.health;
        // Deep copy the list so clones don't mutate each other's items!
        this.inventory = new ArrayList<>(source.inventory);
    }

    public GameCharacter cloneCharacter() {
        return new GameCharacter(this);
    }

    public void addItem(String item) { inventory.add(item); }
    public void takeDamage(int dmg) { health -= dmg; }

    public String toString() {
        return name + " [HP=" + health + ", Inventory=" + inventory + "]";
    }

    public static void main(String[] args) {
        // Base prototype created once
        GameCharacter warriorBase = new GameCharacter("Base Warrior", 100);

        // Instant cloning for army generation!
        GameCharacter warrior1 = warriorBase.cloneCharacter();
        warrior1.addItem("Dragon Helmet");

        GameCharacter warrior2 = warriorBase.cloneCharacter();
        warrior2.takeDamage(35);

        System.out.println("Base:   " + warriorBase);
        System.out.println("Clone1: " + warrior1);
        System.out.println("Clone2: " + warrior2);
    }
}
```

---

# 🏗️ PART 2: STRUCTURAL PATTERNS

---

### 6. Adapter Pattern

#### 💡 The Plain-English Analogy
When you travel from the **United States to the United Kingdom**, your laptop charger has a 2-prong flat plug, but the UK wall socket has 3 rectangular prongs. You don't rewire your laptop or tear down the hotel wall—you simply plug in a **UK Travel Adapter**.

#### ❓ The Problem It Solves
You have an existing class or a 3rd-party library with great functionality, but its method signatures or interfaces do not match what your client system expects. The Adapter sits between them and translates calls back and forth.

#### ⚖️ Pros and Cons
- **Pros**:
  - Reuses existing legacy or 3rd-party classes without touching their source code.
  - Single Responsibility: Interface translation logic is isolated in one place.
- **Cons**:
  - Increases overall code complexity by introducing adapter wrapper classes.

#### 🏢 Where You See It in Real Java
- `java.util.Arrays.asList()` (Adapts an array to a `List`)
- `java.io.InputStreamReader(InputStream)` (Adapts raw byte stream to character stream)

#### 💻 Complete Java Code
```java
// 1. Target interface expected by modern system
interface PaymentGateway {
    void processPayment(String customerId, double amountInDollars);
}

// 2. Legacy 3rd-party system with incompatible interface
class LegacyPaypalEngine {
    public void makeCharge(long cents, String email) {
        System.out.println("💳 Charged " + cents + " cents to PayPal user: " + email);
    }
}

// 3. Adapter that wraps the legacy engine
class PaypalAdapter implements PaymentGateway {
    private final LegacyPaypalEngine paypalEngine;

    public PaypalAdapter(LegacyPaypalEngine paypalEngine) {
        this.paypalEngine = paypalEngine;
    }

    @Override
    public void processPayment(String customerId, double amountInDollars) {
        // Translate dollars to cents and customer ID to email
        long cents = (long) (amountInDollars * 100);
        String email = customerId + "@customer-email.com";
        paypalEngine.makeCharge(cents, email);
    }
}

public class AdapterDemo {
    public static void main(String[] args) {
        // Modern client code only knows PaymentGateway
        PaymentGateway gateway = new PaypalAdapter(new LegacyPaypalEngine());
        gateway.processPayment("user_4291", 49.99);
    }
}
```

---

### 7. Bridge Pattern

#### 💡 The Plain-English Analogy
Think of a **Universal TV Remote Control** and **TV Brands (Sony, Samsung, LG)**.
- Without a bridge: You'd need a "Sony Basic Remote", "Sony Touch Remote", "Samsung Basic Remote", "Samsung Touch Remote", "LG Basic Remote", etc. That's $2 \times 3 = 6$ classes. If you have 5 remote styles and 10 TV brands, you'd need 50 classes!
- With a bridge: You keep the **Remote Controls** (Abstraction) on one side, and the **TV Hardware APIs** (Implementation) on the other side, connected by a bridge. Now you only have $5 + 10 = 15$ classes!

#### ❓ The Problem It Solves
When a class varies along **two independent dimensions** (e.g. Shapes: Circle, Square AND Colors: Red, Blue; or Remotes AND Device Brands), normal inheritance causes a combinatorial explosion of subclasses (`RedCircle`, `BlueCircle`, `RedSquare`, `BlueSquare`...). Bridge separates them into two distinct class hierarchies.

#### ⚖️ Pros and Cons
- **Pros**:
  - Prevents subclass explosion.
  - Lets you update remotes or add new TV brands without touching each other.
- **Cons**:
  - Adds architectural indirection and requires understanding two parallel hierarchies.

#### 🏢 Where You See It in Real Java
- `java.sql.DriverManager` and `java.sql.Driver` (JDBC bridge connecting Java apps to MySQL, PostgreSQL, Oracle drivers).

#### 💻 Complete Java Code
```java
// 1. Implementation Hierarchy (The Device)
interface Device {
    boolean isEnabled();
    void enable();
    void disable();
    int getVolume();
    void setVolume(int percent);
}

class SonyTV implements Device {
    private boolean on = false;
    private int volume = 20;

    public boolean isEnabled() { return on; }
    public void enable() { on = true; System.out.println("📺 Sony TV powered ON."); }
    public void disable() { on = false; System.out.println("📺 Sony TV powered OFF."); }
    public int getVolume() { return volume; }
    public void setVolume(int percent) { this.volume = percent; System.out.println("Sony TV volume set to " + volume); }
}

// 2. Abstraction Hierarchy (The Remote Control)
class RemoteControl {
    protected final Device device; // The "Bridge" reference

    public RemoteControl(Device device) {
        this.device = device;
    }

    public void togglePower() {
        if (device.isEnabled()) device.disable();
        else device.enable();
    }

    public void volumeUp() {
        device.setVolume(device.getVolume() + 5);
    }
}

// 3. Refined Abstraction
class AdvancedSmartRemote extends RemoteControl {
    public AdvancedSmartRemote(Device device) {
        super(device);
    }

    public void mute() {
        System.out.println("🔇 Muting device instantly!");
        device.setVolume(0);
    }
}

public class BridgeDemo {
    public static void main(String[] args) {
        Device sony = new SonyTV();
        AdvancedSmartRemote smartRemote = new AdvancedSmartRemote(sony);

        smartRemote.togglePower();
        smartRemote.volumeUp();
        smartRemote.mute();
    }
}
```

---

### 8. Composite Pattern

#### 💡 The Plain-English Analogy
Think of a **Computer File System**. 
A directory can contain **individual files** (e.g. `resume.pdf`, `photo.jpg`) AND **other directories** (which themselves contain files and more folders). 
When you right-click on a folder and ask "What is the total size?", the computer calculates the size seamlessly, treating an individual file and a folder full of 1,000 files with the **exact same interface**.

#### ❓ The Problem It Solves
When you have tree structures or hierarchies of objects (like UI component trees, corporate hierarchies, or file systems), client code shouldn't need messy `if (node instanceof Folder)` checks to treat leaves and containers differently.

#### ⚖️ Pros and Cons
- **Pros**:
  - Makes client code simple: Treats complex trees and single objects uniformly.
  - Easy to add new element types into the tree.
- **Cons**:
  - Harder to restrict what types can be added to a composite (e.g., if you only want certain files in certain folders).

#### 🏢 Where You See It in Real Java
- `java.awt.Container` and `java.awt.Component`
- DOM XML tree nodes (`org.w3c.dom.Node`)

#### 💻 Complete Java Code
```java
import java.util.ArrayList;
import java.util.List;

// 1. Common Component Interface
interface FileSystemItem {
    void print(String indent);
    long getSizeInBytes();
}

// 2. Leaf (Individual item, cannot contain children)
class FileItem implements FileSystemItem {
    private final String name;
    private final long size;

    public FileItem(String name, long size) {
        this.name = name;
        this.size = size;
    }

    public void print(String indent) {
        System.out.println(indent + "📄 " + name + " (" + size + " bytes)");
    }

    public long getSizeInBytes() { return size; }
}

// 3. Composite (Container that holds other items)
class FolderItem implements FileSystemItem {
    private final String name;
    private final List<FileSystemItem> children = new ArrayList<>();

    public FolderItem(String name) { this.name = name; }

    public void add(FileSystemItem item) { children.add(item); }

    public void print(String indent) {
        System.out.println(indent + "📁 [" + name + "]");
        for (FileSystemItem child : children) {
            child.print(indent + "   ");
        }
    }

    public long getSizeInBytes() {
        return children.stream().mapToLong(FileSystemItem::getSizeInBytes).sum();
    }
}

public class CompositeDemo {
    public static void main(String[] args) {
        FolderItem root = new FolderItem("Project");
        FolderItem src = new FolderItem("src");
        FolderItem docs = new FolderItem("docs");

        src.add(new FileItem("Main.java", 4500));
        src.add(new FileItem("Utils.java", 1800));

        docs.add(new FileItem("README.md", 1200));

        root.add(src);
        root.add(docs);
        root.add(new FileItem(".gitignore", 150));

        // Uniform execution over entire tree!
        root.print("");
        System.out.println("Total Project Size: " + root.getSizeInBytes() + " bytes");
    }
}
```

---

### 9. Decorator Pattern

#### 💡 The Plain-English Analogy
Think of ordering a **Coffee** at Starbucks.
You start with a plain **Black Coffee**. Then you decorate it:
- Add **Whipped Cream** (+ \$0.50)
- Add **Caramel Drizzle** (+ \$0.75)
- Add **Extra Espresso Shot** (+ \$1.00)
Each layer wraps around the previous drink, adding new flavor and increasing the total price, but to the customer at the checkout counter, the final cup is still just a `Coffee`!

#### ❓ The Problem It Solves
If you use standard inheritance to handle every possible combination of features, you end up with hundreds of ridiculous subclasses (`CoffeeWithCream`, `CoffeeWithCreamAndCaramel`, `CoffeeWithCreamAndCaramelAndShot`...). Decorator lets you wrap behaviors dynamically like Russian nesting dolls.

#### ⚖️ Pros and Cons
- **Pros**:
  - Dynamically add or remove responsibilities at runtime.
  - Follows Single Responsibility: Every decorator focuses on one single enhancement.
- **Cons**:
  - Creates lots of small wrapping objects that can be confusing to debug in stack traces.

#### 🏢 Where You See It in Real Java
- Java I/O Streams! 
  `new BufferedReader(new InputStreamReader(new FileInputStream("file.txt")))`
  (`BufferedReader` decorates `InputStreamReader`, which decorates `FileInputStream`).
- `java.util.Collections.unmodifiableList(list)`

#### 💻 Complete Java Code
```java
// 1. Base Component Interface
interface Beverage {
    String getDescription();
    double getCost();
}

// 2. Concrete Base Component
class SimpleCoffee implements Beverage {
    public String getDescription() { return "Dark Roast Coffee"; }
    public double getCost() { return 2.50; }
}

// 3. Abstract Decorator
abstract class BeverageDecorator implements Beverage {
    protected final Beverage decoratedBeverage;

    public BeverageDecorator(Beverage beverage) {
        this.decoratedBeverage = beverage;
    }

    public String getDescription() { return decoratedBeverage.getDescription(); }
    public double getCost() { return decoratedBeverage.getCost(); }
}

// 4. Concrete Decorators
class MilkDecorator extends BeverageDecorator {
    public MilkDecorator(Beverage beverage) { super(beverage); }
    public String getDescription() { return super.getDescription() + ", Steamed Milk"; }
    public double getCost() { return super.getCost() + 0.60; }
}

class CaramelDecorator extends BeverageDecorator {
    public CaramelDecorator(Beverage beverage) { super(beverage); }
    public String getDescription() { return super.getDescription() + ", Caramel Drizzle"; }
    public double getCost() { return super.getCost() + 0.85; }
}

public class DecoratorDemo {
    public static void main(String[] args) {
        // Order: Coffee + Milk + Caramel
        Beverage myDrink = new SimpleCoffee();
        myDrink = new MilkDecorator(myDrink);
        myDrink = new CaramelDecorator(myDrink);

        System.out.println("Your Order: " + myDrink.getDescription());
        System.out.printf("Total Cost: $%.2f%n", myDrink.getCost());
    }
}
```

---

### 10. Facade Pattern

#### 💡 The Plain-English Analogy
Think of a **Hotel Concierge**. 
When you stay at a luxury resort and say, "I want to arrange a romantic evening," the concierge books a dinner table, reserves theater tickets, calls a limousine, and orders champagne to your room. You don't have to dial 4 different businesses, negotiate 4 bills, and coordinate the schedule yourself—the concierge provides a **single, simple front-desk desk button**.

#### ❓ The Problem It Solves
Complex enterprise systems consist of dozens of intricate subsystems (audio codecs, video decoders, streaming buffers, authentication, billing, notification). Exposing all those subsystems directly to client code makes your application brittle and impossible to maintain. A Facade wraps that mess behind one clean method.

#### ⚖️ Pros and Cons
- **Pros**:
  - Isolates client code from subsystem complexity and version changes.
  - Drastically lowers learning curve for developers consuming the module.
- **Cons**:
  - A facade can accidentally become a "God Object" if it tries to do too much.

#### 🏢 Where You See It in Real Java
- `org.slf4j.Logger` (A facade over Logback, Log4j2, or java.util.logging).
- Spring's `JdbcTemplate` (A facade hiding raw JDBC Connection, Statement, and ResultSet boilerplate).

#### 💻 Complete Java Code
```java
// Complex Subsystems
class Lights {
    public void dim() { System.out.println("💡 Lights dimmed to 10%"); }
}
class Projector {
    public void on() { System.out.println("📽️ Projector turned on in 4K HDR mode."); }
}
class SoundSystem {
    public void setSurroundSound() { System.out.println("🔊 Dolby Atmos Surround Sound enabled."); }
}
class StreamingPlayer {
    public void playMovie(String movie) { System.out.println("🍿 Streaming '" + movie + "'"); }
}

// The Facade
class HomeTheaterFacade {
    private final Lights lights = new Lights();
    private final Projector projector = new Projector();
    private final SoundSystem sound = new SoundSystem();
    private final StreamingPlayer player = new StreamingPlayer();

    public void watchMovie(String movie) {
        System.out.println("\n🎬 PREPARING HOME THEATER EXPERIENCE...");
        lights.dim();
        projector.on();
        sound.setSurroundSound();
        player.playMovie(movie);
        System.out.println("✨ Enjoy the show!\n");
    }
}

public class FacadeDemo {
    public static void main(String[] args) {
        // Client code is delightfully simple: ONE line!
        HomeTheaterFacade homeTheater = new HomeTheaterFacade();
        homeTheater.watchMovie("Interstellar");
    }
}
```

---

### 11. Flyweight Pattern

#### 💡 The Plain-English Analogy
Think of a **Massive Video Game Forest** (like in *Grand Theft Auto* or *Elden Ring*) with **1,000,000 pine trees**.
If each individual tree stored its own 50MB 3D mesh polygon model and 4K textures in memory, the game would require **50 Terabytes of RAM** and immediately crash your computer!
Instead, the game loads the 3D model and textures **once** into memory (Intrinsic State). Each of the 1,000,000 trees only stores its $(X, Y, Z)$ coordinates and tree size (Extrinsic State), requiring just a few megabytes.

#### ❓ The Problem It Solves
When an application needs to create hundreds of thousands or millions of similar objects, RAM gets exhausted. Flyweight splits object data into:
1. **Intrinsic State**: Immutable, shared data that never changes across objects.
2. **Extrinsic State**: Context-specific data passed into the object from the outside.

#### ⚖️ Pros and Cons
- **Pros**:
  - Drastically cuts heap memory consumption.
- **Cons**:
  - Code becomes slightly more complex by splitting object state into two parts.

#### 🏢 Where You See It in Real Java
- `java.lang.String.intern()` (The Java String Pool).
- `java.lang.Integer.valueOf(int)` (The JVM caches numbers between -128 and 127).

#### 💻 Complete Java Code
```java
import java.util.HashMap;
import java.util.Map;

// 1. Flyweight: The shared, heavy intrinsic state (loaded once)
class TreeType {
    private final String name;
    private final String color;
    private final String texture3D; // heavy payload

    public TreeType(String name, String color, String texture3D) {
        this.name = name;
        this.color = color;
        this.texture3D = texture3D;
        System.out.println("🌲 [HEAVY ALLOCATION] Loading 3D mesh for: " + name);
    }

    public void render(int x, int y) {
        System.out.println("Rendering " + name + " (" + color + ") at coordinates (" + x + "," + y + ")");
    }
}

// 2. Flyweight Factory (Ensures heavy instances are reused)
class TreeTypeFactory {
    private static final Map<String, TreeType> treeTypes = new HashMap<>();

    public static TreeType getTreeType(String name, String color, String texture) {
        String key = name + "_" + color;
        return treeTypes.computeIfAbsent(key, k -> new TreeType(name, color, texture));
    }
}

// 3. Context: The lightweight extrinsic state (holds reference + coordinates)
class Tree {
    private final int x;
    private final int y;
    private final TreeType type; // Reference to shared flyweight

    public Tree(int x, int y, TreeType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void draw() { type.render(x, y); }
}

public class FlyweightDemo {
    public static void main(String[] args) {
        TreeType oak = TreeTypeFactory.getTreeType("Oak", "DarkGreen", "OakTexture_4K.dat");
        TreeType pine = TreeTypeFactory.getTreeType("Pine", "ForestGreen", "PineTexture_4K.dat");

        // Planting 5 trees, but only 2 heavy 3D textures ever exist in memory!
        Tree[] forest = {
            new Tree(10, 20, oak),
            new Tree(15, 25, oak),
            new Tree(100, 50, pine),
            new Tree(105, 52, pine),
            new Tree(110, 58, oak)
        };

        for (Tree tree : forest) { tree.draw(); }
    }
}
```

---

### 12. Proxy Pattern

#### 💡 The Plain-English Analogy
Think of a **Credit Card** or a **Security Guard at a VIP Lounge**.
- You don't walk around with \$50,000 cash in your pocket. A credit card is a **proxy** for your cash.
- A security guard checks your credentials before letting you into the building. The guard acts as a **protection proxy** for the secure area.

#### ❓ The Problem It Solves
You want to control, secure, cache, or delay access to an expensive or sensitive object without changing that object's core code. The proxy implements the exact same interface as the real object, intercepts incoming calls, does its check or caching, and then forwards the call to the real object.

#### ⚖️ Pros and Cons
- **Pros**:
  - Security, caching, and lazy loading without modifying the underlying class.
  - Transparent to the client (client thinks it's talking directly to the real service).
- **Cons**:
  - Can introduce slight latency if the proxy pipeline is long.

#### 🏢 Where You See It in Real Java
- Spring `@Transactional`, `@Cacheable`, and `@Async` (All implemented using Spring Dynamic Proxies).
- `java.lang.reflect.Proxy` (JDK Dynamic Proxy).
- Hibernate lazy-loaded entity proxies.

#### 💻 Complete Java Code
```java
// 1. Service Interface
interface VideoDownloader {
    void playVideo(String videoId);
}

// 2. Real Expensive Service
class RealYouTubeService implements VideoDownloader {
    public void playVideo(String videoId) {
        System.out.println("🌐 [NETWORK CALL] Downloading and streaming video: " + videoId);
    }
}

// 3. Proxy with In-Memory Caching & Access Control
import java.util.HashSet;
import java.util.Set;

class CachedProxyYouTubeService implements VideoDownloader {
    private final RealYouTubeService realService = new RealYouTubeService();
    private final Set<String> cache = new HashSet<>();

    public void playVideo(String videoId) {
        if (cache.contains(videoId)) {
            System.out.println("⚡ [CACHE HIT] Playing video instantly from local disk cache: " + videoId);
        } else {
            System.out.println("💾 [CACHE MISS] Fetching from network first...");
            realService.playVideo(videoId);
            cache.add(videoId);
        }
    }
}

public class ProxyDemo {
    public static void main(String[] args) {
        VideoDownloader player = new CachedProxyYouTubeService();

        // First play: Downloads over network
        player.playVideo("DesignPatterns_101.mp4");

        // Second play: Instant playback from cache!
        player.playVideo("DesignPatterns_101.mp4");
    }
}
```

---

# ⚡ PART 3: BEHAVIORAL PATTERNS

---

### 13. Chain of Responsibility Pattern

#### 💡 The Plain-English Analogy
Think of **Customer Support Escalation**.
You call your internet provider because your connection dropped:
1. **Tier 1 Bot**: Asks you to reboot your router. If that doesn't fix it, it passes you to...
2. **Tier 2 Helpdesk Technician**: Checks line signal strength. If physical fiber is cut, passes to...
3. **Tier 3 Field Engineering Team**: Dispatches a truck to repair the line.
Each level either solves the problem or passes it down the line.

#### ❓ The Problem It Solves
Avoids tying the sender of a request to one concrete receiver. You can string together multiple handlers (auth checks, rate limiters, logging, input sanitization) in a sequential pipeline.

#### ⚖️ Pros and Cons
- **Pros**:
  - Flexible pipeline: Add, remove, or reorder validation steps without touching other handlers.
  - Follows Single Responsibility: Each handler does one job.
- **Cons**:
  - A request might fall off the end of the chain unhandled if no handler claims it.

#### 🏢 Where You See It in Real Java
- `javax.servlet.Filter` & `FilterChain` in Java Web APIs.
- Spring Security filter chains (`SecurityFilterChain`).
- `java.util.logging.Logger.log()` parent handler escalation.

#### 💻 Complete Java Code
```java
// The Request
record UserRequest(String username, String role, boolean hasValidToken) {}

// Handler Base Class
abstract class RequestHandler {
    protected RequestHandler nextHandler;

    public RequestHandler setNext(RequestHandler next) {
        this.nextHandler = next;
        return next; // returns next handler for easy fluent chaining
    }

    public abstract boolean handle(UserRequest req);

    protected boolean checkNext(UserRequest req) {
        if (nextHandler == null) return true; // Reached end of pipeline safely!
        return nextHandler.handle(req);
    }
}

// Concrete Handlers
class AuthenticationHandler extends RequestHandler {
    public boolean handle(UserRequest req) {
        if (!req.hasValidToken()) {
            System.out.println("❌ Auth Failed: Invalid or missing token!");
            return false;
        }
        System.out.println("✅ Auth Passed for user: " + req.username());
        return checkNext(req);
    }
}

class RoleAuthorizationHandler extends RequestHandler {
    public boolean handle(UserRequest req) {
        if (!"ADMIN".equalsIgnoreCase(req.role())) {
            System.out.println("❌ Forbidden: Admin role required, but got: " + req.role());
            return false;
        }
        System.out.println("✅ Authorization Passed: User is ADMIN.");
        return checkNext(req);
    }
}

public class ChainOfResponsibilityDemo {
    public static void main(String[] args) {
        // Build the pipeline: Auth -> Role Authorization
        RequestHandler pipeline = new AuthenticationHandler();
        pipeline.setNext(new RoleAuthorizationHandler());

        System.out.println("--- Test 1: Invalid Token ---");
        pipeline.handle(new UserRequest("hacker", "USER", false));

        System.out.println("\n--- Test 2: Valid Token, Wrong Role ---");
        pipeline.handle(new UserRequest("alice", "USER", true));

        System.out.println("\n--- Test 3: Fully Valid Admin ---");
        pipeline.handle(new UserRequest("bob_admin", "ADMIN", true));
    }
}
```

---

### 14. Command Pattern

#### 💡 The Plain-English Analogy
Think of a **Restaurant Waiter and Order Ticket**.
When you order food, the waiter doesn't drag the chef out to your table to start cooking immediately. The waiter writes your order onto a paper **Order Slip (Command)** and pins it to the kitchen queue board. 
The chef cooks the orders in sequence. If you change your mind immediately, the waiter can grab the paper and tear it up (**Undo**)!

#### ❓ The Problem It Solves
Turns a request or action into a self-contained object. This allows you to parameterize clients with different actions, queue tasks for execution, schedule them on background threads, and implement robust **Undo / Redo** operations.

#### ⚖️ Pros and Cons
- **Pros**:
  - Completely decouples the invoker (UI button) from the receiver (business engine).
  - Makes Undo/Redo and macro recording straightforward.
- **Cons**:
  - Can generate many small command classes.

#### 🏢 Where You See It in Real Java
- `java.lang.Runnable` and `java.util.concurrent.Callable`
- UI frameworks like JavaFX `EventHandler`

#### 💻 Complete Java Code (With Undo Functionality)
```java
import java.util.Stack;

// 1. Command Interface
interface Command {
    void execute();
    void undo();
}

// 2. Receiver (The business object that does real work)
class TextEditor {
    private final StringBuilder text = new StringBuilder();

    public void append(String str) { text.append(str); }
    public void deleteLast(int length) {
        int start = text.length() - length;
        if (start >= 0) text.delete(start, text.length());
    }
    public String getText() { return text.toString(); }
}

// 3. Concrete Command
class WriteCommand implements Command {
    private final TextEditor editor;
    private final String textToWrite;

    public WriteCommand(TextEditor editor, String textToWrite) {
        this.editor = editor;
        this.textToWrite = textToWrite;
    }

    public void execute() { editor.append(textToWrite); }
    public void undo() { editor.deleteLast(textToWrite.length()); }
}

// 4. Invoker (Manages command execution and undo stack)
class EditorInvoker {
    private final Stack<Command> history = new Stack<>();

    public void executeCommand(Command cmd) {
        cmd.execute();
        history.push(cmd);
    }

    public void undo() {
        if (!history.isEmpty()) {
            Command lastCmd = history.pop();
            lastCmd.undo();
        } else {
            System.out.println("Nothing to undo!");
        }
    }
}

public class CommandDemo {
    public static void main(String[] args) {
        TextEditor editor = new TextEditor();
        EditorInvoker invoker = new EditorInvoker();

        invoker.executeCommand(new WriteCommand(editor, "Hello "));
        invoker.executeCommand(new WriteCommand(editor, "World!"));
        System.out.println("Current: " + editor.getText()); // "Hello World!"

        // Undo last step
        invoker.undo();
        System.out.println("After 1 Undo: " + editor.getText()); // "Hello "

        invoker.undo();
        System.out.println("After 2 Undos: '" + editor.getText() + "'"); // ""
    }
}
```

---

### 15. Interpreter Pattern

#### 💡 The Plain-English Analogy
Think of a **Musical Sheet Reader** or a **Math Calculator**.
When you give a calculator `5 + 10 - 2`, it doesn't view it as arbitrary letters. It breaks the sentence down into grammar rules: `Number(5)`, `Operator(+)`, `Number(10)`, and interprets the sentence according to arithmetic grammar rules.

#### ❓ The Problem It Solves
When a problem occurs repeatedly in a well-defined domain, you can express it as a simple mini-language or grammar (e.g. basic SQL queries, arithmetic formulas, or custom business promotion rules).

#### ⚖️ Pros and Cons
- **Pros**:
  - Easy to change and extend grammar rules by adding new expression classes.
- **Cons**:
  - Complex grammars become very hard to maintain (use ANTLR or parser generators for real programming languages).

#### 🏢 Where You See It in Real Java
- `java.util.regex.Pattern`
- `java.text.SimpleDateFormat`
- Spring Expression Language (SpEL).

#### 💻 Complete Java Code (Simple Math Rule Evaluator)
```java
// 1. Expression Interface
interface Expression {
    int interpret();
}

// 2. Terminal Expression (Numbers)
class NumberExpression implements Expression {
    private final int number;
    public NumberExpression(int number) { this.number = number; }
    public int interpret() { return number; }
}

// 3. Non-Terminal Expressions (Operators)
class AddExpression implements Expression {
    private final Expression left;
    private final Expression right;

    public AddExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }
    public int interpret() { return left.interpret() + right.interpret(); }
}

class SubtractExpression implements Expression {
    private final Expression left;
    private final Expression right;

    public SubtractExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }
    public int interpret() { return left.interpret() - right.interpret(); }
}

public class InterpreterDemo {
    public static void main(String[] args) {
        // Represents: (20 + 5) - 8
        Expression expression = new SubtractExpression(
            new AddExpression(new NumberExpression(20), new NumberExpression(5)),
            new NumberExpression(8)
        );

        System.out.println("Interpreted Result of (20 + 5) - 8 = " + expression.interpret()); // 17
    }
}
```

---

### 16. Iterator Pattern

#### 💡 The Plain-English Analogy
Think of your **TV Remote Channel Up / Down Button** or a music playlist **Next Track** button.
You don't care whether the songs are stored in an array, a linked list, a binary tree, or a cloud server. You just hit `next()`, and the remote delivers the next song without forcing you to understand internal audio memory storage.

#### ❓ The Problem It Solves
Allows you to traverse elements of a complex data structure (trees, graphs, lists) sequentially without exposing the underlying data structure (array indices, node pointers, hash buckets).

#### ⚖️ Pros and Cons
- **Pros**:
  - Single Responsibility: Traversal logic is separated from the collection storage.
  - Multiple iterators can traverse the same collection at the same time independently.
- **Cons**:
  - Overkill for simple flat arrays where a basic loop is faster.

#### 🏢 Where You See It in Real Java
- `java.util.Iterator`
- `java.lang.Iterable` (Powers the Java `for (Item x : collection)` enhanced for-loop).

#### 💻 Complete Java Code
```java
import java.util.Iterator;
import java.util.NoSuchElementException;

// Custom collection storing notifications
class NotificationCollection implements Iterable<String> {
    private final String[] items = new String[5];
    private int count = 0;

    public void add(String message) {
        if (count < items.length) {
            items[count++] = message;
        }
    }

    @Override
    public Iterator<String> iterator() {
        return new NotificationIterator();
    }

    // Inner Iterator class
    private class NotificationIterator implements Iterator<String> {
        private int currentIndex = 0;

        public boolean hasNext() {
            return currentIndex < count && items[currentIndex] != null;
        }

        public String next() {
            if (!hasNext()) throw new NoSuchElementException();
            return items[currentIndex++];
        }
    }
}

public class IteratorDemo {
    public static void main(String[] args) {
        NotificationCollection notifications = new NotificationCollection();
        notifications.add("System reboot scheduled at 2 AM");
        notifications.add("Invoice #1024 paid");
        notifications.add("New login from IP: 192.168.1.1");

        // Uses clean for-each syntax thanks to Iterable/Iterator!
        for (String notification : notifications) {
            System.out.println("📢 Notice: " + notification);
        }
    }
}
```

---

### 17. Mediator Pattern

#### 💡 The Plain-English Analogy
Think of an **Airport Air Traffic Control (ATC) Tower**.
If you have 20 airplanes in the sky near an airport, you don't let every pilot radio the other 19 pilots directly to negotiate who lands first. That would be chaotic and guarantee a mid-air collision. Instead, all airplanes communicate **only with the central ATC tower**, and the tower coordinates safe landings.

#### ❓ The Problem It Solves
When classes communicate directly with each other in an $M:N$ web of dependencies, the code becomes tightly coupled spaghetti. Mediator turns this into a clean $1:N$ hub-and-spoke model.

#### ⚖️ Pros and Cons
- **Pros**:
  - Centralizes control and reduces chaotic inter-dependencies between objects.
  - Individual components can be reused without dragging in other components.
- **Cons**:
  - The mediator itself can become a monolithic, overly complex class over time.

#### 🏢 Where You See It in Real Java
- `java.util.concurrent.ExecutorService` (Mediates between tasks and worker threads).
- Spring `ApplicationEventPublisher`.

#### 💻 Complete Java Code
```java
import java.util.ArrayList;
import java.util.List;

// 1. Mediator Interface
interface ChatRoomMediator {
    void sendMessage(String message, User sender);
    void registerUser(User user);
}

// 2. Colleague
abstract class User {
    protected final ChatRoomMediator mediator;
    protected final String name;

    public User(ChatRoomMediator mediator, String name) {
        this.mediator = mediator;
        this.name = name;
    }

    public abstract void receive(String message, String senderName);
    public void send(String message) {
        mediator.sendMessage(message, this);
    }
}

// 3. Concrete Colleague
class ChatUser extends User {
    public ChatUser(ChatRoomMediator mediator, String name) { super(mediator, name); }

    public void receive(String message, String senderName) {
        System.out.println("[" + name + "'s screen] " + senderName + " says: " + message);
    }
}

// 4. Concrete Mediator
class EngineeringChatRoom implements ChatRoomMediator {
    private final List<User> users = new ArrayList<>();

    public void registerUser(User user) { users.add(user); }

    public void sendMessage(String message, User sender) {
        for (User u : users) {
            // Don't echo the message back to the sender!
            if (u != sender) {
                u.receive(message, sender.name);
            }
        }
    }
}

public class MediatorDemo {
    public static void main(String[] args) {
        ChatRoomMediator slackChannel = new EngineeringChatRoom();

        User alice = new ChatUser(slackChannel, "Alice");
        User bob = new ChatUser(slackChannel, "Bob");
        User charlie = new ChatUser(slackChannel, "Charlie");

        slackChannel.registerUser(alice);
        slackChannel.registerUser(bob);
        slackChannel.registerUser(charlie);

        alice.send("Production deployment is starting now!");
    }
}
```

---

### 18. Memento Pattern

#### 💡 The Plain-English Analogy
Think of a **Video Game Save Checkpoint** or **Ctrl+Z in Photoshop**.
Before fighting a brutal video game boss, you hit "Save Game". The game takes a snapshot of your health, weapons, and coordinates. If you die 30 seconds later, the game restores that saved state without breaking or exposing the internal variables of your character.

#### ❓ The Problem It Solves
You want to capture and restore an object's previous state (for undo, checkpointing, or transaction rollbacks) **without** violating encapsulation (i.e. without making private variables public).

#### ⚖️ Pros and Cons
- **Pros**:
  - Preserves encapsulation: Nobody outside gets direct access to the object's private fields.
  - Simplifies the originator object (it doesn't have to keep track of its own history).
- **Cons**:
  - Can consume a lot of RAM if snapshots are large and saved frequently.

#### 🏢 Where You See It in Real Java
- `java.io.Serializable` snapshots.
- Spring Web Flow state management.

#### 💻 Complete Java Code
```java
// 1. Memento (Holds the immutable snapshot)
class DocumentMemento {
    private final String content;
    public DocumentMemento(String content) { this.content = content; }
    public String getSavedContent() { return content; }
}

// 2. Originator (The object whose state needs saving)
class DocumentEditor {
    private String text = "";

    public void type(String words) { this.text += words; }
    public String getText() { return text; }

    // Creates snapshot
    public DocumentMemento save() { return new DocumentMemento(text); }

    // Restores snapshot
    public void restore(DocumentMemento memento) {
        this.text = memento.getSavedContent();
    }
}

// 3. Caretaker (Stores the memento without inspecting its contents)
import java.util.Stack;

class HistoryCaretaker {
    private final Stack<DocumentMemento> snapshots = new Stack<>();

    public void backup(DocumentMemento memento) { snapshots.push(memento); }

    public DocumentMemento undo() {
        if (!snapshots.isEmpty()) return snapshots.pop();
        return null;
    }
}

public class MementoDemo {
    public static void main(String[] args) {
        DocumentEditor editor = new DocumentEditor();
        HistoryCaretaker history = new HistoryCaretaker();

        editor.type("Chapter 1: The Beginning. ");
        history.backup(editor.save()); // Checkpoint 1

        editor.type("Chapter 2: The Journey Continues. ");
        history.backup(editor.save()); // Checkpoint 2

        editor.type("Chapter 3: An Accidental Disaster occurred!");
        System.out.println("Before Undo:\n" + editor.getText());

        // Rollback to Checkpoint 2
        editor.restore(history.undo());
        System.out.println("\nAfter 1st Rollback:\n" + editor.getText());

        // Rollback to Checkpoint 1
        editor.restore(history.undo());
        System.out.println("\nAfter 2nd Rollback:\n" + editor.getText());
    }
}
```

---

### 19. Observer Pattern

#### 💡 The Plain-English Analogy
Think of **Subscribing to a YouTube Channel** (or hitting the notification bell 🔔).
You don't sit there reloading the creator's page every 10 seconds asking "Did you upload a video yet?". That would waste your time and bandwidth. Instead, you click **Subscribe**. When the creator posts a new video, YouTube automatically broadcasts a notification to all 500,000 subscribers simultaneously.

#### ❓ The Problem It Solves
When a change to one object requires changing others, and you don't know ahead of time how many other objects need to change. It decouples the publisher (Subject) from the subscribers (Observers).

#### ⚖️ Pros and Cons
- **Pros**:
  - Open/Closed Principle: You can add new subscribers without modifying the publisher.
  - Broadcast communication: Automatically distributes state changes.
- **Cons**:
  - Memory leak danger: If subscribers are not unregistered when done, the publisher retains references to them indefinitely ("Lapsed Listener Problem").

#### 🏢 Where You See It in Real Java
- `java.beans.PropertyChangeListener`
- Java 9+ Reactive Streams (`java.util.concurrent.Flow.Publisher` & `Subscriber`)
- Spring Application Events (`@EventListener`).

#### 💻 Complete Java Code
```java
import java.util.ArrayList;
import java.util.List;

// 1. Observer Interface
interface StockSubscriber {
    void onPriceChanged(String stockSymbol, double newPrice);
}

// 2. Subject / Publisher
class StockMarket {
    private final String symbol;
    private double price;
    private final List<StockSubscriber> subscribers = new ArrayList<>();

    public StockMarket(String symbol, double initialPrice) {
        this.symbol = symbol;
        this.price = initialPrice;
    }

    public void subscribe(StockSubscriber subscriber) { subscribers.add(subscriber); }
    public void unsubscribe(StockSubscriber subscriber) { subscribers.remove(subscriber); }

    public void updatePrice(double newPrice) {
        this.price = newPrice;
        notifySubscribers();
    }

    private void notifySubscribers() {
        for (StockSubscriber sub : subscribers) {
            sub.onPriceChanged(symbol, price);
        }
    }
}

// 3. Concrete Observers
class MobileAppTrader implements StockSubscriber {
    public void onPriceChanged(String stockSymbol, double newPrice) {
        System.out.println("📱 Mobile Alert: " + stockSymbol + " is now $" + newPrice);
    }
}

class AutoTradingBot implements StockSubscriber {
    public void onPriceChanged(String stockSymbol, double newPrice) {
        if (newPrice < 150.00) {
            System.out.println("🤖 Bot Trigger: Buying 100 shares of " + stockSymbol + " at $" + newPrice);
        }
    }
}

public class ObserverDemo {
    public static void main(String[] args) {
        StockMarket appleStock = new StockMarket("AAPL", 160.00);

        MobileAppTrader mobile = new MobileAppTrader();
        AutoTradingBot bot = new AutoTradingBot();

        appleStock.subscribe(mobile);
        appleStock.subscribe(bot);

        System.out.println("--- Price drop to $155.00 ---");
        appleStock.updatePrice(155.00);

        System.out.println("\n--- Price drop to $145.00 ---");
        appleStock.updatePrice(145.00);
    }
}
```

---

### 20. State Pattern

#### 💡 The Plain-English Analogy
Think of a **Vending Machine** or a **Smart Phone Power Button**.
- If your phone is in the **Unlocked State**, pressing the power button turns off the screen.
- If your phone is in the **Screen Off State**, pressing the same power button wakes the lock screen.
- If your phone is in the **Ringing Call State**, pressing the power button silences the ringtone.
The exact same button does completely different things depending on the phone's **current state**.

#### ❓ The Problem It Solves
Eliminates massive, fragile, 50-line `switch (state)` or `if-else` blocks that check status flags across every method in your class. State turns each condition into its own class.

#### ⚖️ Pros and Cons
- **Pros**:
  - Single Responsibility: Each state's behaviors and transitions are cleanly isolated.
  - Eliminates brittle nested conditional spaghetti.
- **Cons**:
  - Can be overkill if a state machine only ever has 2 simple states.

#### 🏢 Where You See It in Real Java
- Order lifecycle workflows (e.g. `NEW -> PAID -> SHIPPED -> DELIVERED -> CANCELLED`).
- Thread lifecycle in the JVM (`NEW, RUNNABLE, BLOCKED, WAITING, TERMINATED`).

#### 💻 Complete Java Code
```java
// 1. State Interface
interface PackageState {
    void next(DeliveryContext ctx);
    void printStatus();
}

// 2. Concrete States
class OrderedState implements PackageState {
    public void next(DeliveryContext ctx) {
        ctx.setState(new ShippedState());
    }
    public void printStatus() {
        System.out.println("📦 Package ordered. Preparing for warehouse dispatch.");
    }
}

class ShippedState implements PackageState {
    public void next(DeliveryContext ctx) {
        ctx.setState(new DeliveredState());
    }
    public void printStatus() {
        System.out.println("🚚 Package in transit with carrier. On delivery van.");
    }
}

class DeliveredState implements PackageState {
    public void next(DeliveryContext ctx) {
        System.out.println("✅ Package has already been delivered to the customer door.");
    }
    public void printStatus() {
        System.out.println("🏠 Package successfully delivered!");
    }
}

// 3. Context
class DeliveryContext {
    private PackageState currentState = new OrderedState();

    public void setState(PackageState state) { this.currentState = state; }
    public void advance() { currentState.next(this); }
    public void printCurrentStatus() { currentState.printStatus(); }
}

public class StateDemo {
    public static void main(String[] args) {
        DeliveryContext order = new DeliveryContext();

        order.printCurrentStatus(); // Ordered
        order.advance();

        order.printCurrentStatus(); // Shipped
        order.advance();

        order.printCurrentStatus(); // Delivered
        order.advance();            // Already delivered
    }
}
```

---

### 21. Strategy Pattern

#### 💡 The Plain-English Analogy
Think of **Google Maps** or getting to the airport.
You input your destination. Google Maps offers you different **Strategies**:
- **Drive by Car** (Fast, but tolls & traffic).
- **Take Public Transit** (Cheaper, fixed schedule).
- **Bicycle** (Zero fuel, workout, takes longer).
The destination is the same; you simply choose and swap the **routing algorithm** at runtime based on your preference.

#### ❓ The Problem It Solves
When you have multiple ways of performing the same operation (e.g. calculating tax, sorting a list, compressing a file, or processing payment), Strategy lets you swap the algorithm on the fly without ugly `if (paymentType.equals("PAYPAL"))` blocks.

#### ⚖️ Pros and Cons
- **Pros**:
  - Open/Closed Principle: Introduce new algorithms without editing existing classes.
  - In modern Java, strategies can be passed as simple **Lambda expressions**!
- **Cons**:
  - Clients must be aware of the differences between strategies to pick the right one.

#### 🏢 Where You See It in Real Java
- `java.util.Comparator.comparing(...)` (Passes comparison strategy to sort).
- Spring `ResourceLoader` strategies.

#### 💻 Complete Java Code
```java
// 1. Strategy Interface
@FunctionalInterface
interface PaymentStrategy {
    void pay(double amount);
}

// 2. Concrete Strategies
class CreditCardStrategy implements PaymentStrategy {
    private final String cardNumber;
    public CreditCardStrategy(String cardNumber) { this.cardNumber = cardNumber; }

    public void pay(double amount) {
        System.out.printf("💳 Paid $%.2f using Credit Card ending in %s%n", amount, cardNumber.substring(cardNumber.length() - 4));
    }
}

class CryptoStrategy implements PaymentStrategy {
    private final String walletAddress;
    public CryptoStrategy(String walletAddress) { this.walletAddress = walletAddress; }

    public void pay(double amount) {
        System.out.printf("₿ Paid $%.2f using Bitcoin Wallet: %s%n", amount, walletAddress);
    }
}

// 3. Context
class ShoppingCart {
    private double total = 0;

    public void addItem(double price) { total += price; }

    public void checkout(PaymentStrategy strategy) {
        strategy.pay(total);
    }
}

public class StrategyDemo {
    public static void main(String[] args) {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(45.50);
        cart.addItem(19.99);

        // Swap payment algorithm effortlessly!
        cart.checkout(new CreditCardStrategy("1234-5678-9876-5432"));
        cart.checkout(new CryptoStrategy("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"));

        // Modern Java 8+ Lambda Strategy (Zero extra class needed!):
        cart.checkout(amount -> System.out.printf("🎁 Paid $%.2f using Gift Card!%n", amount));
    }
}
```

---

### 22. Template Method Pattern

#### 💡 The Plain-English Analogy
Think of a **Standard Culinary Recipe for Baking Bread**.
The master recipe template defines the strict sequence:
1. Mix flour and water.
2. Knead the dough.
3. Let it rise for 2 hours.
4. **[Custom Step] Add optional flavor ingredients** (Walnuts, Raisins, or Olives).
5. Bake at 400°F for 35 minutes.
The overall baking structure is fixed in stone by the master baker, but apprentice bakers can override specific optional steps.

#### ❓ The Problem It Solves
When multiple algorithms have almost the exact same steps in the exact same order, but vary only in a couple of specific sub-steps. It prevents code duplication by enforcing the overall skeleton in a base class and delegating the specific steps to subclasses (**Hollywood Principle: "Don't call us, we'll call you"**).

#### ⚖️ Pros and Cons
- **Pros**:
  - Reuses common boilerplate code across dozens of subclasses.
  - Controls the exact sequence of execution strictly.
- **Cons**:
  - Can violate Liskov Substitution if a subclass changes the expected behavior of a step.

#### 🏢 Where You See It in Real Java
- `java.io.InputStream.read(byte[], int, int)` (Calls abstract `read()` in a loop).
- `java.util.AbstractList` and `AbstractMap`.
- Spring `JdbcTemplate`.

#### 💻 Complete Java Code
```java
// Abstract Base Template
abstract class DataMiner {
    // The Template Method: Marked final so subclasses cannot break the step sequence!
    public final void mineData(String path) {
        openFile(path);
        extractRawData();
        parseData();
        generateReport();
        closeFile();
    }

    protected void openFile(String path) { System.out.println("📂 Opening file: " + path); }
    protected void closeFile() { System.out.println("🔒 Closing file safely."); }
    protected void generateReport() { System.out.println("📊 Standard analytics report generated."); }

    // Steps that subclasses MUST customize
    protected abstract void extractRawData();
    protected abstract void parseData();
}

// Concrete Implementations
class PdfDataMiner extends DataMiner {
    protected void extractRawData() { System.out.println("📄 Extracting binary byte stream from PDF font streams..."); }
    protected void parseData() { System.out.println("🔡 Parsing PDF layout text blocks and tables."); }
}

class CsvDataMiner extends DataMiner {
    protected void extractRawData() { System.out.println("📑 Reading CSV comma-separated lines into buffer..."); }
    protected void parseData() { System.out.println("🔢 Parsing CSV rows into numeric columns."); }
}

public class TemplateMethodDemo {
    public static void main(String[] args) {
        System.out.println("--- Mining PDF ---");
        DataMiner pdfMiner = new PdfDataMiner();
        pdfMiner.mineData("financial_statement.pdf");

        System.out.println("\n--- Mining CSV ---");
        DataMiner csvMiner = new CsvDataMiner();
        csvMiner.mineData("daily_metrics.csv");
    }
}
```

---

### 23. Visitor Pattern

#### 💡 The Plain-English Analogy
Think of a **Tax Auditor visiting different companies**.
The auditor visits a **Bakery**, a **Hospital**, and an **Auto Repair Shop**.
Instead of forcing the Bakery, Hospital, and Auto Shop to each write their own 500-page complex tax calculation logic, they simply let the Auditor in (`accept(Auditor)`), and the Auditor examines their records and calculates the tax based on the specific type of business.

#### ❓ The Problem It Solves
You have a stable hierarchy of classes (e.g. geometric shapes: Circle, Dot, Rectangle; or Document nodes: Paragraph, Table, Image). You frequently need to add **brand new operations** (Export to JSON, Export to XML, Render 3D, Calculate Weight) across all those classes. Without Visitor, you'd have to edit 20 classes every time you add an operation! With Visitor, you write one single visitor class.

#### ⚖️ Pros and Cons
- **Pros**:
  - Open/Closed Principle: Add completely new operations without modifying existing classes.
  - Groups related operation logic in one place instead of scattering it across 20 entity classes.
- **Cons**:
  - Hard to use if the element hierarchy changes frequently (adding a new shape requires updating all visitors).

#### 🏢 Where You See It in Real Java
- `java.nio.file.FileVisitor` & `Files.walkFileTree()`
- Compilers & AST traversal (Java compiler `com.sun.source.util.TreeVisitor`).

#### 💻 Complete Java Code (Double-Dispatch Architecture)
```java
// 1. Element Interface
interface Shape {
    void accept(ShapeVisitor visitor); // The Double Dispatch hook
}

// 2. Concrete Elements
class Circle implements Shape {
    private final double radius;
    public Circle(double radius) { this.radius = radius; }
    public double getRadius() { return radius; }

    public void accept(ShapeVisitor visitor) {
        visitor.visitCircle(this); // Calls specific method for Circle
    }
}

class Rectangle implements Shape {
    private final double width;
    private final double height;
    public Rectangle(double width, double height) { this.width = width; this.height = height; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }

    public void accept(ShapeVisitor visitor) {
        visitor.visitRectangle(this); // Calls specific method for Rectangle
    }
}

// 3. Visitor Interface
interface ShapeVisitor {
    void visitCircle(Circle circle);
    void visitRectangle(Rectangle rectangle);
}

// 4. Concrete Visitor: Area Calculator
class AreaCalculatorVisitor implements ShapeVisitor {
    private double totalArea = 0;

    public void visitCircle(Circle c) {
        double area = Math.PI * c.getRadius() * c.getRadius();
        totalArea += area;
        System.out.printf("Circle Area: %.2f%n", area);
    }

    public void visitRectangle(Rectangle r) {
        double area = r.getWidth() * r.getHeight();
        totalArea += area;
        System.out.printf("Rectangle Area: %.2f%n", area);
    }

    public double getTotalArea() { return totalArea; }
}

// 5. Concrete Visitor: Export to XML
class XmlExportVisitor implements ShapeVisitor {
    public void visitCircle(Circle c) {
        System.out.println("<circle radius=\"" + c.getRadius() + "\"/>");
    }

    public void visitRectangle(Rectangle r) {
        System.out.println("<rectangle width=\"" + r.getWidth() + "\" height=\"" + r.getHeight() + "\"/>");
    }
}

public class VisitorDemo {
    public static void main(String[] args) {
        Shape[] shapes = { new Circle(5.0), new Rectangle(4.0, 6.0) };

        System.out.println("--- 1. Running Area Calculation Visitor ---");
        AreaCalculatorVisitor areaVisitor = new AreaCalculatorVisitor();
        for (Shape s : shapes) { s.accept(areaVisitor); }
        System.out.printf("Total Combined Area: %.2f%n", areaVisitor.getTotalArea());

        System.out.println("\n--- 2. Running XML Export Visitor ---");
        XmlExportVisitor xmlVisitor = new XmlExportVisitor();
        for (Shape s : shapes) { s.accept(xmlVisitor); }
    }
}
```

---

# 🚀 PART 4: BONUS ENTERPRISE PATTERNS

---

### 24. Null Object Pattern

#### 💡 The Plain-English Analogy
Instead of handing someone an empty box that blows up when they open it (`NullPointerException`), you hand them a harmless dummy box that safely does nothing when pressed.

#### 💻 Complete Java Code
```java
interface Customer {
    boolean isNull();
    String getName();
}

class RealCustomer implements Customer {
    private final String name;
    public RealCustomer(String name) { this.name = name; }
    public boolean isNull() { return false; }
    public String getName() { return name; }
}

class NullCustomer implements Customer {
    public boolean isNull() { return true; }
    public String getName() { return "Guest / Anonymous User"; }
}

public class NullObjectDemo {
    public static Customer findCustomer(String id) {
        if ("42".equals(id)) return new RealCustomer("Sarah Connor");
        return new NullCustomer(); // Never return null!
    }

    public static void main(String[] args) {
        Customer c1 = findCustomer("42");
        Customer c2 = findCustomer("999");

        // Zero null checks needed! Never throws NullPointerException!
        System.out.println("Welcome, " + c1.getName());
        System.out.println("Welcome, " + c2.getName());
    }
}
```

---

## 🎯 Master Decision Tree: Which Pattern Should You Use?

```
Need to solve an architectural problem in Java?
│
├── ❓ OBJECT CREATION PROBLEMS
│   ├── Only 1 instance must exist globally? ─────────────► SINGLETON
│   ├── Complex object with 10+ parameters/options? ─────► BUILDER
│   ├── Creating an object is too expensive/slow? ────────► PROTOTYPE
│   ├── Don't know which class to instantiate until run? ─► FACTORY METHOD
│   └── Need a whole matching suite of products? ─────────► ABSTRACT FACTORY
│
├── ❓ CONNECTING & STRUCTURING CLASSES
│   ├── Two interfaces don't fit together? ───────────────► ADAPTER
│   ├── Class varies in two independent ways? ────────────► BRIDGE
│   ├── Tree hierarchy of folders & files? ───────────────► COMPOSITE
│   ├── Want to add features to an object on the fly? ────► DECORATOR
│   ├── Subsystem is too complex with 20 classes? ────────► FACADE
│   ├── Millions of tiny objects eating all your RAM? ────► FLYWEIGHT
│   └── Need security, lazy loading, or caching? ─────────► PROXY
│
└── ❓ OBJECT BEHAVIOR & COMMUNICATION
    ├── Pass a request through validation steps? ────────► CHAIN OF RESPONSIBILITY
    ├── Need Undo/Redo or task queuing? ─────────────────► COMMAND
    ├── Mini-language or grammar parsing? ───────────────► INTERPRETER
    ├── Loop through a collection cleanly? ──────────────► ITERATOR
    ├── Too many classes talking directly to each other? ─► MEDIATOR
    ├── Save game checkpoint / restore state? ───────────► MEMENTO
    ├── Broadcast changes to multiple listeners? ─────────► OBSERVER
    ├── Object changes behavior based on its mood/state? ─► STATE
    ├── Swap different algorithms at runtime? ───────────► STRATEGY
    ├── Fixed algorithm steps with custom sub-steps? ────► TEMPLATE METHOD
    └── Add new operations without editing 20 classes? ──► VISITOR
```

---

## 🏁 Summary Checklist

1. **Don't over-engineer**: Don't force a design pattern if a simple 5-line method does the job!
2. **Design patterns prevent tech debt**: When systems grow large, patterns keep code modular, testable, and maintainable.
3. **Modern Java Simplifies Patterns**:
   - `Strategy` $\to$ Modern Java Lambdas (`Runnable`, `Comparator`, `Function`).
   - `Singleton` $\to$ Bill Pugh Holder or Spring Beans.
   - `Builder` $\to$ Project Lombok or Java Records with fluent builders.
   - `Observer` $\to$ Reactive Streams (`Flow.Publisher`).

---

# 📚 PART 4: THE COMPREHENSIVE 140+ DESIGN PATTERNS ENCYCLOPEDIA

This section provides an exhaustive, categorized directory of all modern architectural, enterprise, cloud, concurrency, and behavioral patterns recognized across the Java ecosystem and [java-design-patterns.com](https://java-design-patterns.com/patterns/).

---

## 🏗️ 1. Creational & Object Lifecycle Patterns

| Pattern | Definition (Zero Jargon) | Problem Solved | When to Use | Java / Framework Example |
| :--- | :--- | :--- | :--- | :--- |
| [Abstract Document](https://java-design-patterns.com/patterns/abstract-document) | Uses dynamic hierarchical key-value maps to define schema-less objects with typed view interfaces. | Eliminates rigid class inheritance when domain models require dynamic, polymorphic properties. | CMS systems, document databases, dynamic product catalogs. | Jackson `JsonNode`, MongoDB BSON documents |
| [Abstract Factory](https://java-design-patterns.com/patterns/abstract-factory) | A factory of factories that creates families of related or dependent objects without specifying concrete classes. | Prevents mixing incompatible components (e.g., Mac button with Windows scrollbar). | Multi-cloud SDKs (AWS vs Azure), cross-platform UI toolkits. | `javax.xml.parsers.DocumentBuilderFactory` |
| [Builder](https://java-design-patterns.com/patterns/builder) | Separates complex object construction from its representation, assembling fields step-by-step. | Eliminates the "Telescoping Constructor" anti-pattern with 15 confusing constructor parameters. | Objects with more than 4-5 optional configuration parameters. | `java.lang.StringBuilder`, Lombok `@Builder` |
| [Step Builder](https://java-design-patterns.com/patterns/step-builder) | A fluent builder variation that guides the developer through mandatory fields sequentially using compile-time interfaces. | Prevents runtime invariant failures by enforcing exact creation sequences at compile-time. | Complex wizard configurations, multi-step transaction initialization. | Stream pipelines, AWS Client Builders |
| [Factory](https://java-design-patterns.com/patterns/factory) | A simple factory class that creates objects based on given inputs without exposing creation logic. | Centralizes object instantiation so callers don't hardcode `new ConcreteClass()`. | Standard object creation based on string type flags or enums. | `java.util.Calendar.getInstance()` |
| [Factory Kit](https://java-design-patterns.com/patterns/factory-kit) | A stateless, immutable builder that maps string/enum keys to constructor method references or lambdas. | Eliminates long, fragile `switch-case` statements in traditional factory classes. | Dynamic plugin registration, extensible toolkits. | Map of constructor references: `Map<Type, Supplier<T>>` |
| [Factory Method](https://java-design-patterns.com/patterns/factory-method) | Defines an interface for creating an object, but lets subclasses decide which exact class to instantiate. | Defers object instantiation decisions to child classes at runtime. | Framework extension points, plugin hooks. | `java.util.Collection.iterator()` |
| [Monostate](https://java-design-patterns.com/patterns/monostate) | All instances of a class share the same underlying static state while looking like regular instances. | Provides transparent singleton-like behavior without exposing a static `getInstance()`. | Transparent configuration caching, telemetry counters. | Logging bridges, Spring shared scoped beans |
| [Multiton](https://java-design-patterns.com/patterns/multiton) | A registry of singletons where each unique key maps to exactly one instance. | Manages a controlled pool of named singletons (e.g. per-tenant database connections). | Multi-tenant applications, localized resource caches. | `java.util.Currency.getInstance(String code)` |
| [Object Pool](https://java-design-patterns.com/patterns/object-pool) | Recycles expensive, reusable objects from a pre-allocated pool instead of constantly creating and destroying them. | Eliminates the massive CPU/memory overhead of instantiating heavy resources. | Database connections, network sockets, thread pools. | HikariCP, Apache Commons Pool |
| [Prototype](https://java-design-patterns.com/patterns/prototype) | Creates new objects by cloning an existing prototype instance rather than constructing from scratch. | Avoids costly database queries or heavy calculations required to initialize a new object. | Game character cloning, template-based documents. | `java.lang.Object.clone()`, Copy constructors |
| [Singleton](https://java-design-patterns.com/patterns/singleton) | Guarantees that a class has only one instance and provides a global access point to it. | Prevents resource contention and state inconsistency for shared global resources. | Configuration managers, hardware spoolers, metrics registries. | `java.lang.Runtime.getRuntime()` |
| [Twin](https://java-design-patterns.com/patterns/twin) | Models multiple inheritance in languages that do not support it by linking two cooperating objects with mutually reciprocal references. | Overcomes single-inheritance limits without creating messy, bloated diamond hierarchies. | Game development (Player + PhysicsBody), legacy API bridges. | Swing peer components |
| [Type Object](https://java-design-patterns.com/patterns/type-object) | Represents a new "type" or classification of an entity as an instance of a class rather than as a new Java subclass. | Prevents class explosion when domain types are user-defined or change frequently. | Game monster types, insurance policy definitions. | Dynamic metadata engines |
| [Value Object](https://java-design-patterns.com/patterns/value-object) | An immutable object whose equality is determined solely by its attribute values, not by an identity ID. | Eliminates floating-point inaccuracies, primitive obsession, and illegal domain states. | Monetary amounts, geo-coordinates, dates, email addresses. | Java 17+ `record`, `java.time.LocalDate` |

---

## 🔌 2. Structural & Interface Adaptation Patterns

| Pattern | Definition (Zero Jargon) | Problem Solved | When to Use | Java / Framework Example |
| :--- | :--- | :--- | :--- | :--- |
| [Adapter](https://java-design-patterns.com/patterns/adapter) | Converts the interface of a class into another interface that clients expect, acting like a power plug converter. | Allows classes with incompatible interfaces to collaborate seamlessly without altering their source code. | Integrating third-party SDKs, legacy system modernization. | `java.util.Arrays.asList()` |
| [Ambassador](https://java-design-patterns.com/patterns/ambassador) | An out-of-process helper service or sidecar that handles network routing, logging, mTLS, and retries on behalf of a client. | Offloads common network resilience features from application code to a dedicated co-located proxy. | Kubernetes microservice sidecars, service mesh integration. | Envoy Proxy, Istio Sidecar |
| [Anti-Corruption Layer](https://java-design-patterns.com/patterns/anti-corruption-layer) | A translation layer that isolates a modern domain model from a legacy system's archaic schema and foreign semantics. | Prevents legacy, proprietary, or poorly designed external data models from polluting modern microservices. | Mainframe integrations, monolith-to-microservice migrations. | Spring Integration Gateways, DDD ACL Facades |
| [Bridge](https://java-design-patterns.com/patterns/bridge) | Decouples an abstraction from its implementation so that both can vary independently. | Prevents Cartesian product class explosions when a class varies across two dimensions (e.g. shapes and colors). | Hardware abstraction layers, multi-platform graphics engines. | JDBC `DriverManager` and database drivers |
| [Component](https://java-design-patterns.com/patterns/component) | Allows an entity to span multiple distinct domains without coupling them by delegating specific capabilities to pluggable components. | Eliminates massive "God Objects" by splitting entity logic into focused, reusable parts. | Video game entity-component systems (ECS), GUI widgets. | Unity GameObjects, Spring Component composition |
| [Composite](https://java-design-patterns.com/patterns/composite) | Composes objects into tree structures to represent part-whole hierarchies, treating single items and collections uniformly. | Eliminates repetitive `if (isFolder) { loop(); } else { act(); }` checks throughout the codebase. | File systems, hierarchical organization charts, nested UI layouts. | `java.awt.Container`, DOM elements |
| [Composite Entity](https://java-design-patterns.com/patterns/composite-entity) | Models a set of closely related, fine-grained persistent objects as a single coarse-grained coarse entity bean. | Eliminates chattiness and excessive remote network calls in legacy EJB systems. | Bundling master-detail database records into a single network unit. | JPA Aggregate Roots with cascade fetch |
| [Decorator](https://java-design-patterns.com/patterns/decorator) | Attaches additional responsibilities to an object dynamically at runtime by wrapping it. | Provides a flexible alternative to subclassing for extending functionality without class explosion. | Adding logging, encryption, or compression layers dynamically. | `java.io.BufferedReader(new FileReader())` |
| [Extension Objects](https://java-design-patterns.com/patterns/extension-objects) | Allows an object's interface to be extended dynamically at runtime without modifying its class or subclassing. | Extends core classes with plugin capabilities while maintaining strict Open/Closed compliance. | Modular IDE plugins, extensible CAD systems. | Eclipse `IAdaptable.getAdapter()` |
| [Facade](https://java-design-patterns.com/patterns/facade) | Provides a simplified, high-level interface to a complex subsystem of multiple interacting classes. | Protects clients from the intricate details and cognitive overload of complex library subsystems. | Simplifying complex internal workflows into a clean, 1-click method. | Spring `JdbcTemplate`, SLF4J `LoggerFactory` |
| [Flyweight](https://java-design-patterns.com/patterns/flyweight) | Shares common intrinsic state across thousands of fine-grained objects to drastically reduce memory consumption. | Prevents JVM `OutOfMemoryError` when dealing with millions of similar objects. | Text editor character rendering, game rendering (trees, particles). | `java.lang.Integer.valueOf()` cache (-128 to 127) |
| [Marker Interface](https://java-design-patterns.com/patterns/marker-interface) | An interface with zero methods used to tag a class with metadata or runtime permissions. | Conveys structural type semantics to the compiler and JVM runtime. | Tagging objects for special serialization or cloning capabilities. | `java.io.Serializable`, `java.lang.Cloneable` |
| [Private Class Data](https://java-design-patterns.com/patterns/private-class-data) | Encapsulates all data attributes of a class into a separate private data class, exposing zero write methods. | Prevents unwanted mutation of class data after initialization without making all fields public. | Immutability protection in complex domain entities. | Value object encapsulation |
| [Proxy](https://java-design-patterns.com/patterns/proxy) | Provides a surrogate or placeholder for another object to control, delay, or log access to it. | Controls access to expensive, remote, or sensitive objects without changing client code. | Lazy loading, remote RPC stubs, access control. | Spring `@Transactional` proxies, CGLIB |
| [Dynamic Proxy](https://java-design-patterns.com/patterns/dynamic-proxy) | Generates proxy classes at runtime using reflection or bytecode manipulation without writing boilerplate classes. | Eliminates manual proxy boilerplate for cross-cutting concerns across hundreds of interfaces. | AOP instrumentation, security interceptors, mock frameworks. | `java.lang.reflect.Proxy`, ByteBuddy |
| [Virtual Proxy](https://java-design-patterns.com/patterns/virtual-proxy) | A proxy that defers the creation of an expensive object until a method on that object is actually invoked. | Eliminates slow startup times and memory bloat from loading unused heavy assets. | ORM lazy-loaded entity relationships, high-res image viewers. | Hibernate Entity Proxies |
| [Role Object](https://java-design-patterns.com/patterns/role-object) | Allows an object to dynamically take on and shed multiple roles (e.g., Customer, Employee, Supplier) at runtime. | Avoids rigid inheritance hierarchies where a Person cannot simultaneously be an Employee and Customer. | Enterprise identity systems, CRM platforms. | Security principal role context mapping |
| [Separated Interface](https://java-design-patterns.com/patterns/separated-interface) | Defines an interface in one package and places its implementation in a completely separate package. | Decouples domain logic from concrete infrastructure dependencies (Dependency Inversion). | Clean Architecture boundaries, modular microservice libraries. | SPI (Service Provider Interface) |
| [Tolerant Reader](https://java-design-patterns.com/patterns/tolerant-reader) | A message parser that extracts only the fields it cares about, safely ignoring unknown attributes and schema additions. | Prevents distributed microservice consumers from breaking whenever an upstream service updates its payload. | Evolving JSON/XML contracts in distributed event streams. | Jackson `FAIL_ON_UNKNOWN_PROPERTIES = false` |

---

## ⚡ 3. Behavioral, Workflow & Rule Patterns

| Pattern | Definition (Zero Jargon) | Problem Solved | When to Use | Java / Framework Example |
| :--- | :--- | :--- | :--- | :--- |
| [Active Object](https://java-design-patterns.com/patterns/active-object) | Decouples method execution from method invocation, executing tasks asynchronously on its own private thread. | Prevents slow operations from blocking caller threads while maintaining single-threaded safety. | Stock exchange order matching engines, hardware drivers. | Akka Actors, Single-threaded Executor loops |
| [Acyclic Visitor](https://java-design-patterns.com/patterns/acyclic-visitor) | A Visitor pattern variant that breaks circular compile-time dependencies between visitors and element hierarchies. | Allows adding new element types without forcing all existing visitors to recompile. | Extensible AST compilers, dynamic document engines. | Pluggable syntax trees |
| [Balking](https://java-design-patterns.com/patterns/balking) | Executes an action only if the object is in an appropriate state; if not, it "balks" (returns immediately without waiting). | Prevents redundant or illegal concurrent operations when a task is already running or completed. | Auto-saving documents, smart washing machine start buttons. | `AtomicBoolean.compareAndSet(false, true)` |
| [Chain of Responsibility](https://java-design-patterns.com/patterns/chain-of-responsibility) | Passes a request along a chain of potential handlers until one handles it or the chain ends. | Decouples the sender of a request from its receivers, avoiding hardcoded `if-else` routing logic. | Request validation pipelines, middleware, exception handling chains. | `jakarta.servlet.FilterChain`, Spring Security |
| [Command](https://java-design-patterns.com/patterns/command) | Encapsulates a request or action as a standalone object, complete with all parameters needed to execute it. | Enables undo/redo, task scheduling, logging, and decoupling invocations from receivers. | GUI buttons, transactional job queues, transactional rollback logs. | `java.lang.Runnable`, Spring Batch jobs |
| [Combinator](https://java-design-patterns.com/patterns/combinator) | Combines simple functional primitives into complex composite functions using fluent combinator methods. | Eliminates brittle validation logic by composing reusable functional predicates. | Validation rules, functional query builders. | `java.util.function.Predicate.and().or()` |
| [Converter](https://java-design-patterns.com/patterns/converter) | A bidirectional mapping utility that converts domain entities to DTOs and vice versa. | Isolates transformation boilerplate so business services remain clean and focused. | REST API boundary transformations, database entity mappings. | MapStruct, ModelMapper |
| [Currying](https://java-design-patterns.com/patterns/currying) | Transforms a function that takes multiple arguments into a chain of functions that each take a single argument. | Allows partial function application and reusing pre-configured functions with fixed parameters. | Tax calculation functions with fixed rates, currency conversion. | Java `Function<A, Function<B, C>>` |
| [Double Dispatch](https://java-design-patterns.com/patterns/double-dispatch) | Dispatches a method call dynamically based on the runtime types of **both** the receiver and the argument object. | Overcomes Java's single-dispatch limitation where method overloading is resolved statically at compile-time. | Collision detection in games (Asteroid vs Ship), financial asset valuation. | Visitor pattern `accept(visitor)` implementation |
| [Execute Around](https://java-design-patterns.com/patterns/execute-around) | Sandwiches custom business logic between standard, mandatory preamble and postamble operations. | Guarantees that resources are always initialized and cleaned up without duplicating `try-finally` blocks. | Database transactions, performance profiling timers, lock handling. | Spring `TransactionTemplate`, Java try-with-resources |
| [Filterer](https://java-design-patterns.com/patterns/filterer) | Allows a container or tree structure to filter its internal elements while returning the exact same structural subtype. | Solves type-erasure and loss of specialized container types when filtering collections. | Domain collections, hierarchical trees. | Specialized collection filtering |
| [Fluent Interface](https://java-design-patterns.com/patterns/fluent-interface) | Chains method calls together by returning `this` to create code that reads like a natural English sentence. | Drastically improves code readability and discoverability in IDEs. | Builders, query DSLs, assertion libraries. | AssertJ `assertThat()`, Mockito `when().thenReturn()` |
| [Function Composition](https://java-design-patterns.com/patterns/function-composition) | Combines two or more mathematical functions to produce a new function ($f(g(x))$). | Creates complex data transformation pipelines by snapping together small, testable pure functions. | Data enrichment streams, ETL processing. | `java.util.function.Function.compose()` |
| [Guarded Suspension](https://java-design-patterns.com/patterns/guarded-suspension) | Suspends execution of a method until a specific precondition is met (e.g. queue is not empty). | Eliminates busy-waiting loops that burn 100% CPU waiting for state changes. | Thread coordination, bounded buffer consumer queues. | `Object.wait()` / `notifyAll()`, `Condition.await()` |
| [Interpreter](https://java-design-patterns.com/patterns/interpreter) | Defines a grammatical representation for a mini-language and an interpreter to evaluate sentences in the language. | Enables evaluating dynamic mathematical expressions, search queries, or domain-specific rules. | SQL query parsers, regular expressions, calculation engines. | `java.util.regex.Pattern`, Spring SpEL |
| [Iterator](https://java-design-patterns.com/patterns/iterator) | Provides a way to access elements of an aggregate object sequentially without exposing its underlying representation. | Decouples traversal algorithms from collection data structures (arrays, linked lists, trees). | Looping through collections without breaking encapsulation. | `java.util.Iterator`, `java.lang.Iterable` |
| [Mediator](https://java-design-patterns.com/patterns/mediator) | Restricts direct communication between objects, forcing them to collaborate solely through a central mediator object. | Eliminates an unmaintainable $O(N^2)$ web of tight cross-references between peer components. | Chat rooms, Air Traffic Control, complex GUI dialog boxes. | Java `Timer`, Spring Integration MessageBus |
| [Memento](https://java-design-patterns.com/patterns/memento) | Captures and externalizes an object's internal state without violating encapsulation, allowing restoration later. | Implements undo/redo mechanisms, rollback snapshots, and save-game checkpoints safely. | Text editor history, financial state rollbacks. | Java serialization, Git commit snapshots |
| [Mute Idiom](https://java-design-patterns.com/patterns/mute-idiom) | Explicitly suppresses expected, non-actionable exceptions (like socket close errors) cleanly without empty catch blocks. | Cleans up codebases and avoids lint warnings while preserving intent. | Resource teardowns, non-critical background socket cleanup. | Apache Commons `DbUtils.closeQuietly()` |
| [Notification](https://java-design-patterns.com/patterns/notification) | Collects multiple business validation errors in a single container object rather than throwing on the first failure. | Allows presenting all validation errors to the user at once instead of failing repeatedly. | Web form validation, batch data import sanitization. | Spring `BindingResult`, Domain Notification bags |
| [Null Object](https://java-design-patterns.com/patterns/null-object) | Provides a do-nothing surrogate object instead of returning `null`. | Eliminates defensive `if (x != null)` checks scattered across the entire codebase. | Default user profiles, no-op loggers, dummy listeners. | `java.util.Optional`, Empty collections (`Collections.emptyList()`) |
| [Observer](https://java-design-patterns.com/patterns/observer) | Defines a one-to-many subscription dependency so that when one object changes state, all dependents are notified automatically. | Decouples state-holding broadcasters from dependent listeners. | UI event handling, real-time dashboards, distributed events. | `java.util.concurrent.Flow`, Spring `@EventListener` |
| [Pipeline](https://java-design-patterns.com/patterns/pipeline) | Sequences a series of discrete data processing stages where the output of each stage feeds as input to the next. | Eliminates monolithic transformation functions by breaking workflows into modular, reusable steps. | Image processing filters, ETL pipelines, order processing. | Java Stream API, Netty `ChannelPipeline` |
| [Rule Engine](https://java-design-patterns.com/patterns/rule-engine) | Externalizes complex nested business policy rules into independent, priority-ordered condition-action units. | Eliminates 3,000-line procedural `if-else` blocks and allows non-technical users to manage rules. | Loan underwriting, insurance claims, pricing and discounts. | Drools, Easy Rules |
| [Servant](https://java-design-patterns.com/patterns/servant) | Defines a helper class that provides a common set of operations to a group of classes without forcing them to inherit from it. | Adds functionality to classes that already have an inheritance hierarchy without bloating them. | Geometric shape movers, drawing helpers. | Utility services acting on interface groups |
| [Special Case](https://java-design-patterns.com/patterns/special-case) | A specialized subclass providing default behavior for unusual or boundary domain conditions (generalization of Null Object). | Replaces repetitive condition checks for anomalous states with clean polymorphism. | Inactive accounts, unknown customers, guest checkout users. | Default pricing models |
| [Specification](https://java-design-patterns.com/patterns/specification) | Encapsulates a business rule or predicate into a standalone class with composable boolean logic (`and`, `or`, `not`). | Reusable validation rules that can be evaluated in memory or translated directly to SQL queries. | Complex search criteria, credit check validations. | Spring Data JPA `Specification<T>` |
| [State](https://java-design-patterns.com/patterns/state) | Allows an object to alter its behavior when its internal state changes, appearing to change its class. | Eliminates massive, error-prone `switch (state)` statements across dozens of methods. | Vending machines, order fulfillment workflows, TCP connection lifecycles. | Spring State Machine |
| [Strategy](https://java-design-patterns.com/patterns/strategy) | Defines a family of interchangeable algorithms, encapsulates each one, and makes them swappable at runtime. | Eliminates conditional branching when choosing between different algorithms or business policies. | Payment methods (PayPal vs Stripe), compression algorithms, sorting strategies. | `java.util.Comparator`, Spring Strategies |
| [Subclass Sandbox](https://java-design-patterns.com/patterns/subclass-sandbox) | Provides operations in a base class that subclasses can combine in an abstract method to define behavior safely. | Prevents subclasses from directly coupling to external systems by giving them a safe, protected API sandbox. | Video game super-powers, particle effects. | Game entity action systems |
| [Template Method](https://java-design-patterns.com/patterns/template-method) | Defines the skeleton of an algorithm in a superclass method, deferring specific steps to subclasses without changing structure. | Prevents code duplication by standardizing fixed workflows while allowing customized sub-steps. | Framework lifecycle hooks, report generators, ETL extractors. | `AbstractList`, Spring `JdbcTemplate` |
| [Trampoline](https://java-design-patterns.com/patterns/trampoline) | Transforms deep recursive function calls into an iterative loop on the heap to prevent stack overflow. | Overcomes the JVM's lack of native Tail-Call Optimization (TCO). | Deep recursive algorithms, compiler AST walks, functional parsers. | Functional Java libraries (Vavr) |
| [Update Method](https://java-design-patterns.com/patterns/update-method) | Simulates a collection of independent objects by advancing each entity one step in time per frame. | Decouples multiple autonomous game entities from a centralized tick scheduler. | Real-time simulations, video game entities, IoT sensor emulators. | Game loop actor updating |
| [Visitor](https://java-design-patterns.com/patterns/visitor) | Represents an operation to be performed on the elements of an object structure, letting you define new operations without changing element classes. | Allows adding new reporting, export, or audit capabilities to heterogeneous trees without editing 20 classes. | Compiler syntax tree evaluation, financial portfolio analysis. | `java.nio.file.FileVisitor` |

---

## 🧵 4. Concurrency, Threading & Synchronization Patterns

| Pattern | Definition (Zero Jargon) | Problem Solved | When to Use | Java / Framework Example |
| :--- | :--- | :--- | :--- | :--- |
| [Async Method Invocation](https://java-design-patterns.com/patterns/async-method-invocation) | Starts an operation in the background and immediately returns a handle (Future) so the caller doesn't block. | Prevents slow I/O or computations from stalling the main application thread. | Long-running background jobs, email dispatching, remote calls. | `CompletableFuture.supplyAsync()`, Spring `@Async` |
| [Double-Checked Locking](https://java-design-patterns.com/patterns/double-checked-locking) | Reduces locking overhead by checking a condition before and after acquiring a lock. | Avoids acquiring a synchronized lock on every single read after an object has already been initialized. | Lazy-loaded singletons, resource managers. | Correct implementation with `volatile` field |
| [Event-Based Asynchronous](https://java-design-patterns.com/patterns/event-based-asynchronous) | Manages asynchronous tasks using events and callbacks rather than polling threads. | Frees threads from waiting on slow operations, scaling to thousands of concurrent connections. | Non-blocking network I/O, GUI background workers. | SwingWorker, Node.js event loops |
| [Half-Sync/Half-Async](https://java-design-patterns.com/patterns/half-sync-half-async) | Deconstructs a concurrent system into an asynchronous I/O layer and a synchronous processing layer bridged by a queue. | Combines the high-throughput scaling of non-blocking I/O with the simple programming model of synchronous services. | High-performance network servers handling slow databases. | Netty EventLoop to JDBC worker pool |
| [Leader Election](https://java-design-patterns.com/patterns/leader-election) | Coordinates multiple cluster nodes to elect a single master node responsible for scheduling and coordination. | Prevents conflicting actions (Split-Brain) and single points of failure in distributed clusters. | Distributed job schedulers, cluster controllers, database primaries. | Apache ZooKeeper, Raft, Kubernetes Lease |
| [Leader-Followers](https://java-design-patterns.com/patterns/leader-followers) | Thread pool pattern where one thread (the leader) waits for incoming events; upon an event, it promotes a follower to leader before processing. | Minimizes context switching, dynamic thread allocations, and locking overhead in high-throughput network servers. | Low-latency socket servers, telecom switches. | Real-time event dispatchers |
| [Lockable Object](https://java-design-patterns.com/patterns/lockable-object) | Allows an object to be locked explicitly, rejecting modifications or concurrent access from other threads. | Provides fine-grained concurrency control over shared domain entities. | Concurrency protection in collaborative editors. | `ReentrantLock`, Explicit read locks |
| [Master-Worker](https://java-design-patterns.com/patterns/master-worker) | A master thread splits a large computational task into smaller chunks, dispatches them to worker threads, and aggregates results. | Accelerates CPU-heavy computations by parallelizing work across all available hardware cores. | Image rendering, matrix multiplication, batch processing. | `ForkJoinPool`, MapReduce frameworks |
| [Monitor](https://java-design-patterns.com/patterns/monitor) | Synchronizes access to an object's methods, ensuring only one thread can execute within the monitor at any given time. | Eliminates race conditions and protects mutable internal state across concurrent threads. | Thread-safe queues, concurrent collections. | Java `synchronized` keyword, `ReentrantLock` |
| [Poison Pill](https://java-design-patterns.com/patterns/poison-pill) | A special tombstone message placed into a queue that instructs consumer threads to shut down cleanly. | Eliminates awkward polling checks or abrupt thread interruptions during graceful service shutdown. | Multi-threaded worker shutdown, message queue draining. | `BlockingQueue` graceful shutdown token |
| [Producer-Consumer](https://java-design-patterns.com/patterns/producer-consumer) | Decouples threads generating data from threads processing data using a shared, synchronized buffer. | Balances workloads when generation and processing speeds differ wildly. | Logging buffers, task processing systems, event ingestion. | `ArrayBlockingQueue`, LMAX Disruptor |
| [Promise](https://java-design-patterns.com/patterns/promise) | An object that acts as a proxy for a value that will become available in the future. | Avoids callback hell by allowing chaining of asynchronous operations. | Asynchronous workflows, non-blocking REST calls. | `java.util.concurrent.CompletableFuture` |
| [Reactor](https://java-design-patterns.com/patterns/reactor) | Handles service requests that arrive concurrently from multiple clients using a single-threaded demultiplexer and dispatchers. | Scales to 100,000+ concurrent connections without incurring the memory overhead of a thread-per-connection model. | High-concurrency network servers, web gateways. | Netty, Node.js, Project Reactor |
| [Resource Acquisition Is Initialization (RAII)](https://java-design-patterns.com/patterns/resource-acquisition-is-initialization) | Binds the lifecycle of a resource (memory, file, socket) to the lifetime of an object, guaranteeing automatic release. | Prevents resource leaks caused by forgotten `close()` calls or uncaught exceptions. | File I/O, database connections, mutex locks. | Java `AutoCloseable` with try-with-resources |
| [Thread-Pool Executor](https://java-design-patterns.com/patterns/thread-pool-executor) | Reuses a bounded pool of pre-allocated worker threads to execute tasks from a queue. | Prevents server crashes caused by spawning thousands of unconstrained OS threads. | Web servers handling incoming HTTP requests. | `java.util.concurrent.ThreadPoolExecutor` |
| [Thread-Specific Storage](https://java-design-patterns.com/patterns/thread-specific-storage) | Allocates dedicated, isolated memory storage for each individual thread, avoiding locks entirely. | Eliminates lock contention when threads need their own independent workspace. | Request context tracking, transaction managers, database connections. | `java.lang.ThreadLocal` |

---

## 🌐 5. Enterprise, Microservices & Resiliency Patterns

| Pattern | Definition (Zero Jargon) | Problem Solved | When to Use | Java / Framework Example |
| :--- | :--- | :--- | :--- | :--- |
| [Backends For Frontends (BFF)](https://java-design-patterns.com/patterns/backends-for-frontends) | Creates separate, specialized backend services tailored specifically for different client interfaces (Mobile, Web, IoT). | Prevents bloated, one-size-fits-all API gateways that slow down team deployment velocity. | Systems supporting drastically different client form factors and network speeds. | Spring Cloud Gateway, GraphQL BFF layers |
| [Circuit Breaker](https://java-design-patterns.com/patterns/circuit-breaker) | Automatically stops sending requests to a failing remote service, failing fast and giving the downstream service time to recover. | Prevents cascading microservice failures and thread pool starvation across the entire cluster. | All synchronous microservice-to-microservice REST/gRPC calls. | Resilience4j, Netflix Hystrix |
| [Commander](https://java-design-patterns.com/patterns/commander) | Coordinates distributed transactions across multiple independent microservices using asynchronous messaging and retry ledgers. | Resolves partial distributed failures when two-phase commit ($2\text{PC}$) is not viable. | Distributed order-to-payment processing. | Distributed coordinator services |
| [Fallback](https://java-design-patterns.com/patterns/fallback) | Provides an alternative path, cached response, or default value when a service call fails. | Preserves user experience during outages instead of displaying an ugly crash screen. | Recommender systems, pricing engines, weather widgets. | Resilience4j fallback handlers |
| [Fan-Out/Fan-In](https://java-design-patterns.com/patterns/fanout-fanin) | Broadcasts a request to multiple services concurrently (Fan-Out) and aggregates their responses into one payload (Fan-In). | Reduces total user latency from $O(N \times \text{delay})$ to $\max(\text{delay})$. | Travel aggregators (flight search), e-commerce product detail pages. | `CompletableFuture.allOf()` |
| [Feature Toggle](https://java-design-patterns.com/patterns/feature-toggle) | Modifies system behavior dynamically at runtime without deploying new code by toggling configuration flags. | Enables canary releases, A/B testing, and continuous delivery without risky code merges. | Rolling out experimental features to 5% of users. | LaunchDarkly, Togglz, Unleash |
| [Gateway](https://java-design-patterns.com/patterns/gateway) | A single entry-point server that encapsulates the internal microservice topology, handling routing, SSL, and auth. | Prevents external clients from needing to know the URLs of 100 internal microservices. | Exposing microservice APIs to web, mobile, and third parties. | Spring Cloud Gateway, Kong, Apigee |
| [Health Check](https://java-design-patterns.com/patterns/health-check) | Exposes standardized endpoints (`/health/liveness`, `/health/readiness`) reporting the internal health of a service. | Allows orchestrators (Kubernetes) to automatically reboot crashed pods and route traffic only to ready pods. | All containerized microservices. | Spring Boot Actuator |
| [Microservices Aggregator](https://java-design-patterns.com/patterns/microservices-aggregrator) | Consolidates data from multiple backend microservices and returns a single unified JSON payload to the client. | Eliminates network chattiness and reduces cellular battery drain on client devices. | Mobile dashboards, shopping cart checkout pages. | Aggregator Spring controllers, GraphQL |
| [Microservices API Gateway](https://java-design-patterns.com/patterns/microservices-api-gateway) | Acts as the security, traffic management, and routing facade for all incoming client traffic. | Centralizes cross-cutting concerns like rate limiting, JWT validation, and CORS. | Modern distributed microservice architectures. | AWS API Gateway, Spring Cloud Gateway |
| [Microservices Client-Side UI Composition](https://java-design-patterns.com/patterns/microservices-client-side-ui-composition) | Assembles independent UI micro-frontends in the user's browser, where each microservice renders its own DOM component. | Decouples large frontend teams, enabling independent deployment of UI widgets. | Enterprise portals, e-commerce storefronts. | Micro-frontends (Single-SPA, Module Federation) |
| [Microservices Distributed Tracing](https://java-design-patterns.com/patterns/microservices-distributed-tracing) | Injects unique trace and span IDs into HTTP/message headers to track a request across 50 microservices. | Solves the impossible mystery of finding which microservice failed in a 500-service cluster. | All distributed microservice meshes. | OpenTelemetry, Jaeger, Zipkin |
| [Microservices Idempotent Consumer](https://java-design-patterns.com/patterns/microservices-idempotent-consumer) | Ensures that processing the exact same message multiple times produces the exact same side-effect as processing it once. | Prevents double-billing or duplicate order creation caused by network retries in Kafka/RabbitMQ. | Financial ledgers, payment processing microservices. | Database idempotency tables, Redis locks |
| [Microservices Log Aggregation](https://java-design-patterns.com/patterns/microservices-log-aggregation) | Collects, indexes, and centralizes log lines from hundreds of ephemeral container instances into a single searchable store. | Eliminates SSHing into 40 individual servers to hunt down production error stack traces. | Centralized DevOps monitoring. | ELK Stack (Elasticsearch, Logstash, Kibana), Loki |
| [Microservices Messaging](https://java-design-patterns.com/patterns/microservices-messaging) | Enables asynchronous, decoupled communication between microservices using message queues and publish-subscribe topics. | Eliminates cascading failures and temporal coupling between microservices. | Distributed event-driven architectures. | Apache Kafka, RabbitMQ, AWS SQS |
| [Microservices Pattern - Self-Registration](https://java-design-patterns.com/patterns/microservices-self-registration) | A microservice instance registers its own IP and port with a Service Registry upon boot and deregisters on shutdown. | Enables dynamic service discovery without hardcoding IP addresses in configuration files. | Ephemeral containerized clusters. | Netflix Eureka, HashiCorp Consul |
| [Monolithic Architecture](https://java-design-patterns.com/patterns/monolithic-architecture) | Packages the entire application (UI, business logic, data access) as a single executable artifact. | Maximizes developer velocity, debugging ease, and deployment simplicity in early-stage startups. | MVP development, applications with small teams and low complexity. | Traditional Spring Boot JAR, Ruby on Rails |
| [Queue-Based Load Leveling](https://java-design-patterns.com/patterns/queue-based-load-leveling) | Buffers bursty incoming traffic in a queue so rate-constrained downstream databases can process work at a steady pace. | Prevents database connection pool exhaustion and crashes during flash sales. | Black Friday sales, telemetry ingestion, email campaigns. | Kafka, Amazon SQS, RabbitMQ |
| [Rate Limiting](https://java-design-patterns.com/patterns/rate-limiting-pattern) | Restricts the number of requests a user or client can make within a specified time window. | Protects APIs from DDoS attacks, brute-force hacking, and noisy-neighbor resource starvation. | Public APIs, login endpoints, payment gateways. | Token Bucket (Bucket4j), Leaky Bucket |
| [Retry](https://java-design-patterns.com/patterns/retry) | Automatically retries a failed operation with exponential backoff and randomized jitter. | Overcomes transient network glitches, socket timeouts, and temporary service blips transparently. | Cloud network calls, database reconnection logic. | Spring Retry, Resilience4j Retry |
| [Saga](https://java-design-patterns.com/patterns/saga) | Coordinates distributed transactions across microservices using a sequence of local transactions and compensating rollbacks. | Maintains eventual data consistency across microservices without blocking database locks ($2\text{PC}$). | Multi-service checkout flows (Order -> Payment -> Shipping). | Temporal, Axon, Eventuate Tram |
| [Sharding](https://java-design-patterns.com/patterns/sharding) | Horizontally partitions database records across multiple physical database instances using a shard key. | Overcomes single-server disk, CPU, and memory limits when data reaches terabytes or billions of rows. | Massive social networks, multi-tenant SaaS platforms. | Consistent Hashing routers, Vitess, Citus |
| [Strangler](https://java-design-patterns.com/patterns/strangler) | Incrementally replaces legacy monolithic features with modern microservices until the monolith is completely replaced. | Eliminates the massive risk of "Big Bang" complete system rewrites. | Legacy modernization, migrating off mainframes. | Cloudflare routing rules, API Gateway proxies |
| [Throttling](https://java-design-patterns.com/patterns/throttling) | Dynamically constrains system consumption (CPU, bandwidth, memory) to prevent catastrophic resource saturation. | Protects systems from cascading brownouts under extreme load by shedding non-critical tasks. | Cloud multi-tenant services. | Semaphore limits, rate limiters |
| [Transactional Outbox](https://java-design-patterns.com/patterns/transactional-outbox) | Writes both business entity changes and domain events to the same relational database in a single atomic transaction before publishing to Kafka. | Solves the "Dual-Write Problem": prevents losing events if Kafka is down or publishing ghost events if the DB transaction rolls back. | All event-driven microservices with database persistence. | Debezium CDC reading Postgres WAL |
| [Write-Ahead Log](https://java-design-patterns.com/patterns/write-ahead-log) | Appends all state mutations to an append-only disk log before applying changes to in-memory data structures. | Guarantees zero data loss (durability) if the server crashes before dirty pages are flushed to disk. | Storage engines, distributed consensus algorithms, distributed databases. | Kafka commit log, PostgreSQL WAL, Raft |

---

## 📡 6. Messaging, Event-Driven & Reactive Patterns

| Pattern | Definition (Zero Jargon) | Problem Solved | When to Use | Java / Framework Example |
| :--- | :--- | :--- | :--- | :--- |
| [Actor Model](https://java-design-patterns.com/patterns/actor-model) | Represents computational entities as "Actors" that retain private state and communicate exclusively via asynchronous messages. | Eliminates shared-memory concurrency bugs, deadlocks, and lock contention entirely. | Real-time gaming servers, telecom switching, chat platforms. | Apache Pekko, Akka |
| [Backpressure](https://java-design-patterns.com/patterns/backpressure) | Allows a slow consumer to communicate its current processing capacity to a fast producer, preventing buffer overflow. | Prevents JVM `OutOfMemoryError` and GC thrashing when producers generate data faster than consumers can process. | Reactive data streams, IoT telemetry ingestion. | Reactive Streams `Subscription.request(n)` |
| [Bloc](https://java-design-patterns.com/patterns/bloc) | Business Logic Component: separates presentation UI from business logic using reactive sink and stream pipes. | Prevents mixing business calculations and network calls inside UI view classes. | Mobile and desktop reactive UI architectures. | Flutter BLoC, RxJava ViewModel |
| [Data Bus](https://java-design-patterns.com/patterns/data-bus) | An application-wide communication channel that routes strongly-typed messages between decoupled components. | Eliminates tightly coupled point-to-point cross-references between dozens of subsystems. | Large desktop applications, trading terminals, modular plugins. | Guava `EventBus`, Custom type-safe buses |
| [Event Aggregator](https://java-design-patterns.com/patterns/event-aggregator) | Consolidates event subscriptions from multiple distinct sources into a single central hub for subscribers. | Reduces an $O(N \times M)$ web of listeners down to an organized $O(N)$ channel. | Complex UI dashboards with 50 widgets listening to events. | Centralized event managers |
| [Event-Driven Architecture](https://java-design-patterns.com/patterns/event-driven-architecture) | An architectural paradigm where decoupled services produce, detect, and react to asynchronous state-change events. | Maximizes loose coupling, elastic scalability, and autonomous service deployment. | Modern enterprise cloud architectures. | Kafka event meshes, AWS EventBridge |
| [Event Queue](https://java-design-patterns.com/patterns/event-queue) | Buffers asynchronous events sequentially, allowing consumers to pull and process events at their own pace. | Decouples event generation timing from execution timing. | User input event handling, audio sound effects in games. | Java AWT EventQueue, Disruptor |
| [Event Sourcing](https://java-design-patterns.com/patterns/event-sourcing) | Persists the state of a business entity as an immutable sequence of append-only state-transition events. | Provides a 100% complete, tamper-proof audit log and enables time-travel debugging and state rehydration. | Banking core ledgers, shipping tracking, version control systems. | Axon Framework, EventStoreDB |
| [Flux](https://java-design-patterns.com/patterns/flux) | Represents an asynchronous sequence of 0 to $N$ reactive items with non-blocking backpressure. | Enables handling infinite streams of data over tiny thread pools without blocking CPU threads. | Real-time financial tickers, streaming HTTP responses, WebFlux. | Project Reactor `Flux`, RxJava `Observable` |
| [MapReduce](https://java-design-patterns.com/patterns/map-reduce) | Processes and generates large data sets by applying a `map` function to partition chunks and a `reduce` function to aggregate results. | Enables parallel processing of petabytes of distributed data across thousands of commodity machines. | Big Data analytics, batch log parsing. | Apache Hadoop, Spark, Java parallel streams |
| [Polling Pub/Sub](https://java-design-patterns.com/patterns/polling-publisher) | Periodically queries a persistent store or external feed to discover new records and publish them to an event stream. | Integrates uncooperative legacy databases or third parties that lack native webhook capabilities. | Legacy database audit table publishing. | Spring Integration poller, Kafka Connect JDBC |
| [Publish-Subscribe](https://java-design-patterns.com/patterns/publish-subscribe) | Senders (publishers) categorize messages into topics without knowing who (subscribers) will receive them. | Provides dynamic, one-to-many broadcast messaging with zero coupling between publishers and subscribers. | Real-time notifications, market data updates, decoupled microservices. | JMS Topics, Redis Pub/Sub, Kafka |

---

## 🏛️ 7. Enterprise Architecture & Domain-Driven Design (DDD) Patterns

| Pattern | Definition (Zero Jargon) | Problem Solved | When to Use | Java / Framework Example |
| :--- | :--- | :--- | :--- | :--- |
| [Clean Architecture](https://java-design-patterns.com/patterns/clean-architecture) | Organizes software into concentric rings where dependencies point strictly inward toward pure business logic. | Guarantees that business rules are 100% testable and completely independent of databases, web frameworks, and UI. | Long-lived enterprise applications, mission-critical systems. | Uncle Bob's Clean Architecture |
| [Hexagonal Architecture](https://java-design-patterns.com/patterns/hexagonal-architecture) | Isolates core domain logic inside a hexagon, connecting to external systems (DB, Web, Kafka) through Inbound/Outbound Ports and Adapters. | Allows swapping databases (Postgres $\to$ Mongo) or delivery mechanisms (REST $\to$ gRPC) without changing business code. | Domain-Driven Design, microservice architectures. | Ports and Adapters pattern |
| [Onion Architecture](https://java-design-patterns.com/patterns/onion-architecture) | A layered architectural model where the domain core is at the center, surrounded by domain services, application services, and infrastructure. | Enforces strict Dependency Inversion: infrastructure depends on domain, never vice versa. | Complex domain-heavy enterprise software. | Jeffrey Palermo's Onion Architecture |
| [Layered Architecture](https://java-design-patterns.com/patterns/layered-architecture) | Organizes code into standard horizontal layers (Presentation, Business, Persistence, Database) where each layer only communicates with the layer below it. | Provides simple, familiar separation of concerns for development teams. | Standard CRUD applications, enterprise web applications. | Classic Spring Controller-Service-Repository |
| [Domain Model](https://java-design-patterns.com/patterns/domain-model) | An object model of the domain that incorporates both state and behavior together, strictly encapsulating business invariants. | Prevents the "Anemic Domain Model" anti-pattern where services become unmaintainable procedural scripts. | Complex business domains with intricate rules, state transitions, and calculations. | Rich DDD Aggregate Roots |
| [Transaction Script](https://java-design-patterns.com/patterns/transaction-script) | Organizes business logic as a single procedural method that executes all steps (database query, calculation, update) directly. | Avoids unnecessary OOP abstraction overhead for simple, straightforward business operations. | Simple CRUD operations, data migration scripts, reporting utilities. | Procedural Spring `@Service` methods |
| [Money](https://java-design-patterns.com/patterns/money) | A specialized Value Object that couples a precise `BigDecimal` amount with a `java.util.Currency` code. | Completely eliminates floating-point precision errors and accidental cross-currency math. | All financial, banking, and e-commerce systems. | `javax.money` (JSR 354), Moneta |
| [Naked Objects](https://java-design-patterns.com/patterns/naked-objects) | An architectural pattern where the user interface is completely and automatically auto-generated from domain entity classes. | Eliminates 100% of the manual boilerplate required to write forms, tables, and CRUD UI screens. | Internal administrative tools, rapid prototyping, back-office dashboards. | Apache Causeway (formerly Isis) |
| [Unit of Work](https://java-design-patterns.com/patterns/unit-of-work) | Tracks all newly created, modified, and deleted entities during a business transaction, flushing changes in a single atomic SQL batch. | Eliminates redundant database round-trips and guarantees transactional integrity across multiple repository calls. | High-performance ORMs, enterprise transaction coordination. | Hibernate `Session`, JPA `EntityManager` |
| [Identity Map](https://java-design-patterns.com/patterns/identity-map) | Ensures that each database record is loaded into memory exactly once per transaction session, storing them in a lookup map. | Prevents concurrent in-memory divergence (lost updates) and eliminates duplicate SQL `SELECT` queries. | ORMs, transactional caches. | Hibernate First-Level Cache |
| [Data Mapper](https://java-design-patterns.com/patterns/data-mapper) | A layer of mappers that moves data between objects and a database while keeping both completely independent of each other. | Insulates domain models from relational database schema quirks and snake_case column names. | Enterprise domain models where database tables don't match OOP models. | MyBatis, Hibernate ORM |

---

## 💾 8. Data Access, Caching & Persistence Patterns

| Pattern | Definition (Zero Jargon) | Problem Solved | When to Use | Java / Framework Example |
| :--- | :--- | :--- | :--- | :--- |
| [Caching](https://java-design-patterns.com/patterns/caching) | Stores high-cost query results in fast in-memory storage (Redis/Caffeine) to serve subsequent requests rapidly. | Eliminates database bottlenecks and reduces API latency from 200ms to 2ms. | High-read, low-write data (product catalogs, configurations). | Spring `@Cacheable`, Caffeine, Redis |
| [DAO Factory](https://java-design-patterns.com/patterns/dao-factory) | An abstract factory that instantiates vendor-specific Data Access Objects (Oracle, Postgres, MongoDB) dynamically. | Enables switching underlying database vendors or supporting multi-database deployments without code changes. | Multi-database enterprise software products. | Abstract DAO Factory hierarchies |
| [Data Access Object (DAO)](https://java-design-patterns.com/patterns/data-access-object) | An object that encapsulates all direct database CRUD access logic, isolating SQL queries from business services. | Prevents raw SQL, JDBC connection logic, and table queries from polluting domain code. | Relational database table access. | Spring `JdbcTemplate` DAO classes |
| [Data Locality](https://java-design-patterns.com/patterns/data-locality) | Organizes data sequentially in contiguous memory to maximize CPU L1/L2 cache line utilization. | Eliminates RAM memory latency and CPU cache misses caused by Java heap pointer chasing. | High-frequency trading engines, game physics simulations. | Primitive arrays (`long[]`), Off-heap memory |
| [Data Transfer Object (DTO)](https://java-design-patterns.com/patterns/data-transfer-object) | A dumb, serializable data holder object used solely to transfer data between remote processes or application layers. | Reduces network round-trips by bundling multiple attributes into a single payload; hides internal entity structure. | REST API request/response models, remote RPC calls. | Java 17+ `record`, Jackson DTOs |
| [Dirty Flag](https://java-design-patterns.com/patterns/dirty-flag) | A boolean flag that marks an object as modified, deferring expensive calculations or SQL updates until necessary. | Prevents redundant calculations and skips unnecessary SQL `UPDATE` statements for unchanged entities. | 3D rendering engines, spreadsheet calculation trees, ORM dirty checking. | Hibernate Bytecode Dirty Tracking |
| [Metadata Mapping](https://java-design-patterns.com/patterns/metadata-mapping) | Maps database columns to class properties using external metadata (annotations, XML) rather than hardcoded SQL. | Decouples object-relational mapping rules from compiled Java code, enabling dynamic ORM behavior. | ORM frameworks, serialization engines. | JPA `@Column`, Jackson `@JsonProperty` |
| [Optimistic Offline Lock](https://java-design-patterns.com/patterns/optimistic-offline-lock) | Detects concurrent conflicting edits across disconnected web requests by verifying a version number before updating. | Prevents lost updates without holding database locks open during human think time. | Long-running web editing sessions, REST API updates. | JPA `@Version` column |
| [Partial Response](https://java-design-patterns.com/patterns/partial-response) | Allows the client to specify exactly which fields it needs in the response (`?fields=id,name`), pruning all other attributes. | Drastically cuts cellular data usage and speeds up mobile rendering times. | High-traffic mobile APIs, microservice data exchange. | Jackson `@JsonFilter`, GraphQL Field Selection |
| [Repository](https://java-design-patterns.com/patterns/repository) | Simulates an in-memory collection of domain Aggregate Roots, completely hiding persistence mechanics. | Provides clean, collection-like domain access (`add`, `remove`, `getById`) without exposing SQL concepts. | Domain-Driven Design aggregate persistence. | Spring Data `JpaRepository<T, ID>` |
| [Serialized Entity](https://java-design-patterns.com/patterns/serialized-entity) | Persists an object graph as a serialized binary or text blob (JSON/Protobuf) directly in a database column. | Eliminates complex multi-table joins for deep, immutable object trees that are always loaded together. | Audit trails, user preference graphs, shopping cart snapshots. | PostgreSQL `JSONB`, Jackson serialization |
| [Serialized LOB](https://java-design-patterns.com/patterns/serialized-lob) | Stores unstructured or polymorphic domain graphs inside large database objects (`BLOB` / `CLOB`). | Avoids creating 20 normalized database tables for rarely queried, heterogeneous objects. | Storing dynamic document attachments, CAD models. | Oracle `CLOB`, Postgres `BYTEA` |
| [Single Table Inheritance](https://java-design-patterns.com/patterns/single-table-inheritance) | Maps an entire class inheritance hierarchy into a single relational table using a discriminator column (`DTYPE`). | Delivers maximum polymorphic query performance by completely eliminating SQL `JOIN`s. | Shallow inheritance hierarchies with shared columns. | JPA `@Inheritance(strategy = SINGLE_TABLE)` |
| [Table Inheritance](https://java-design-patterns.com/patterns/table-inheritance) | Maps an inheritance hierarchy into relational tables using joined subclass tables or separate tables per class. | Preserves database 3NF normalization and allows declaring `NOT NULL` constraints on subclass fields. | Deep inheritance hierarchies with distinct attributes. | JPA `@Inheritance(strategy = JOINED)` |
| [Table Module](https://java-design-patterns.com/patterns/table-module) | A single singleton class that organizes all business logic for an entire database table, operating on tabular record sets. | Consolidates table operations in simple, data-driven applications without full OOP domain models. | Legacy tabular data applications, reporting systems. | ADO.NET DataSets, Tabular query engines |
| [Version Number](https://java-design-patterns.com/patterns/version-number) | Maintains a monotonically incrementing integer on an entity that is bumped on every mutation. | Enables conflict detection in optimistic concurrency and version watermarking in distributed caches. | Concurrency control, distributed cache invalidation. | JPA `@Version` integer |

---

## 🖥️ 9. Web, Presentation & UI Patterns

| Pattern | Definition (Zero Jargon) | Problem Solved | When to Use | Java / Framework Example |
| :--- | :--- | :--- | :--- | :--- |
| [Business Delegate](https://java-design-patterns.com/patterns/business-delegate) | A local client-side class that hides the network lookup, transport details, and remote exceptions of remote business services. | Shields web controllers from distributed network complexity (`NamingException`, `RemoteException`). | Multi-tier distributed applications, legacy EJB bridges. | Remote service wrappers |
| [Client Session](https://java-design-patterns.com/patterns/client-session) | Stores session state (such as user identity and permissions) directly in the client's browser using cryptographically signed tokens. | Enables true stateless horizontal server scaling without needing centralized Redis session stores. | High-scale modern cloud web applications. | JSON Web Tokens (JWT), Encrypted Cookies |
| [Server Session](https://java-design-patterns.com/patterns/server-session) | Stores session state centrally on the server or in a distributed cache, giving the client an opaque session ID pointer. | Enables instant session revocation, prevents stale user permissions, and keeps network payloads small. | High-security banking applications, enterprise portals. | Spring Session with Redis, Tomcat `HttpSession` |
| [Composite View](https://java-design-patterns.com/patterns/composite-view) | Builds a complex web page by assembling multiple independent sub-views or template tiles together. | Eliminates copy-pasting shared page components (headers, footers, sidebars) across 50 templates. | Web application portals, dashboard layouts. | Apache Tiles, Thymeleaf Fragments |
| [Context Object](https://java-design-patterns.com/patterns/context-object) | Encapsulates protocol-specific request state (headers, user identity, IP address, tenant) into a clean domain object. | Prevents polluting dozens of business method signatures with 10 infrastructure parameters. | Cross-cutting security, tenant routing, distributed tracing. | Spring `SecurityContextHolder`, MDC |
| [Front Controller](https://java-design-patterns.com/patterns/front-controller) | A centralized master servlet that handles all incoming HTTP requests, managing routing, security, and view resolution. | Eliminates duplicating security checks, character encoding, and error handling across 60 separate servlets. | Core architectural backbone of all modern web frameworks. | Spring MVC `DispatcherServlet` |
| [Intercepting Filter](https://java-design-patterns.com/patterns/intercepting-filter) | A composable chain of filters that pre-processes requests and post-processes responses transparently. | Decouples cross-cutting concerns (authentication, compression, CORS, rate limiting) from core controllers. | HTTP request sanitation, audit logging, security inspection. | `jakarta.servlet.Filter`, Spring Interceptors |
| [Model-View-Controller (MVC)](https://java-design-patterns.com/patterns/model-view-controller) | Separates an application into three interconnected parts: Model (data), View (UI display), and Controller (input logic). | Decouples business logic from presentation, allowing changing UI skins without altering business rules. | Server-rendered web applications, classic desktop applications. | Spring MVC, Struts |
| [Model-View-Intent (MVI)](https://java-design-patterns.com/patterns/model-view-intent) | A reactive UI pattern where user actions create immutable Intents that feed into a reducer to produce a new single ViewState. | Eliminates race conditions and UI synchronization bugs through strict Unidirectional Data Flow (UDF). | Modern reactive web and mobile applications. | Redux, Jetpack Compose, Flutter |
| [Model-View-Presenter (MVP)](https://java-design-patterns.com/patterns/model-view-presenter) | A derivative of MVC where the Presenter mediates all communication between Model and View, with the View being passive. | Drastically improves UI unit-testability by isolating all UI logic into a pure Java presenter class. | Desktop GUI applications, legacy Android apps. | Vaadin, Android MVP |
| [Model-View-ViewModel (MVVM)](https://java-design-patterns.com/patterns/model-view-viewmodel) | Binds the View to the ViewModel using two-way data binding, so UI changes automatically update the ViewModel and vice versa. | Eliminates tedious glue code that manually copies data between UI text inputs and domain models. | Rich UI desktop and mobile applications. | Android Jetpack ViewModel, Vue.js |
| [Page Controller](https://java-design-patterns.com/patterns/page-controller) | Assigns an individual controller object to handle requests for a specific web page or action. | Simple, intuitive request handling for websites with few common cross-cutting requirements. | Basic web applications, legacy PHP/JSP scripts. | Spring `@Controller` per page |
| [Presentation Model](https://java-design-patterns.com/patterns/presentation-model) | Represents the state and behavior of the presentation independently of the GUI components used to render it. | Enables testing complex screen interactions and validation logic without launching a GUI window. | Rich client applications, complex desktop forms. | Martin Fowler Presentation Model |
| [Service Layer](https://java-design-patterns.com/patterns/service-layer) | Defines an application's boundary with a layer of services that coordinates response operations and encapsulates business logic. | Provides a clear, reusable API facade for web controllers, batch jobs, and remote APIs. | Enterprise architectures. | Spring `@Service` layer |
| [Service Locator](https://java-design-patterns.com/patterns/service-locator) | A central registry that decouples clients from concrete implementations by looking up services on demand. | Enables dynamic service resolution when dependencies cannot be injected at compile-time. | Dynamic plugin systems, legacy JNDI lookups. | `java.util.ServiceLoader`, JNDI `InitialContext` |
| [Service Stub](https://java-design-patterns.com/patterns/service-stub) | A lightweight, in-memory surrogate that simulates a real remote service during development or testing. | Eliminates dependencies on slow, unreliable, or costly third-party APIs during test suites. | Unit testing, local development environments. | Mockito Stubs, WireMock |
| [Service to Worker](https://java-design-patterns.com/patterns/service-to-worker) | Combines a Front Controller with Action Workers that execute business logic *before* dynamically selecting the target view. | Manages complex transactional workflows where business logic determines which screen to show next. | Transactional web applications. | Spring MVC Controller to View |
| [Session Facade](https://java-design-patterns.com/patterns/session-facade) | A coarse-grained remote facade that aggregates fine-grained domain calls into a single remote transactional call. | Eliminates network chattiness and prevents partial client-side transaction failures. | Distributed multi-service backends. | Coarse-grained REST endpoints |
| [Template View](https://java-design-patterns.com/patterns/templateview) | Renders dynamic HTML by embedding markers or evaluation expressions inside static HTML template files. | Allows web designers to edit HTML templates without breaking Java backend logic. | Server-side rendered web pages. | Thymeleaf, JSP, FreeMarker |
| [View Helper](https://java-design-patterns.com/patterns/view-helper) | A helper class that isolates data formatting and view manipulation logic outside of HTML templates. | Prevents messy Java scriptlet code from cluttering HTML templates. | Formatting dates, currencies, and localized strings in UI views. | Thymeleaf Dialects, JSP Custom Tags |

---

## 🧪 10. Testing, Idiomatic & Low-Level Patterns

| Pattern | Definition (Zero Jargon) | Problem Solved | When to Use | Java / Framework Example |
| :--- | :--- | :--- | :--- | :--- |
| [Arrange/Act/Assert (AAA)](https://java-design-patterns.com/patterns/arrange-act-assert) | Structures unit tests into three clean, separate phases: Arrange inputs, Act on behavior, Assert results. | Eliminates confusing, multi-action tests and guarantees crystal-clear defect localization. | All unit tests across all frameworks. | JUnit 5, TestNG tests |
| [Bytecode](https://java-design-patterns.com/patterns/bytecode) | Encodes domain rules or game instructions as a compact array of byte opcodes executed by a virtual machine loop. | Delivers blazing-fast execution speeds without the risk and overhead of dynamic class-loading. | Fraud detection rules, game scripting engines, math DSLs. | JVM bytecode, Lua VMs, Custom rule VMs |
| [Collecting Parameter](https://java-design-patterns.com/patterns/collecting-parameter) | Passes a collection into multiple methods so that each method can append its results to the single accumulator. | Prevents allocating and merging dozens of intermediate collection objects across recursive calls. | Tree traversal, recursive document exports, multi-validator runs. | Passing `List<String> errors` to validators |
| [Collection Pipeline](https://java-design-patterns.com/patterns/collection-pipeline) | Programs collection transformations as a linear pipeline of operations (map, filter, reduce). | Eliminates messy nested `for` loops and temporary mutable accumulator variables. | Data processing, stream manipulation. | Java 8+ Stream API (`stream().filter().map()`) |
| [Curiously Recurring Template Pattern (CRTP)](https://java-design-patterns.com/patterns/curiously-recurring-template-pattern) | A class `Foo` extends a generic base class parameterized by `Foo` itself (`class Foo extends Base<Foo>`). | Achieves compile-time polymorphism and enables fluent builders with correct subclass return types. | Method chaining in class hierarchies, compile-time comparisons. | `Comparable<T>`, Fluent Builder inheritance |
| [Double Buffer](https://java-design-patterns.com/patterns/double-buffer) | Maintains two buffers (Front and Back); reads occur on the front while writes occur on the back, swapping atomically. | Completely eliminates visual tearing, state inconsistency, and lock contention between readers and writers. | High-frequency order books, graphics rendering, sensor arrays. | `java.awt.image.BufferStrategy`, Display GPUs |
| [Game Loop](https://java-design-patterns.com/patterns/game-loop) | Runs a continuous loop with fixed simulation time steps and variable rendering interpolation. | Decouples game physics and simulation logic from unpredictable hardware rendering frame rates. | Real-time game engines, robotics simulations. | LibGDX, Physics engines |
| [Object Mother](https://java-design-patterns.com/patterns/object-mother) | A dedicated test fixture factory class that produces standardized, named domain objects for integration tests. | Eliminates brittle copy-pasted mock data and constructor churn across thousands of tests. | Large automated test suites. | Test fixture factories (`Users.validAdmin()`) |
| [Page Object](https://java-design-patterns.com/patterns/page-object) | Encapsulates an HTML web page's DOM elements and user actions behind a high-level domain class. | Prevents frontend CSS/XPath selector changes from breaking hundreds of end-to-end UI tests. | End-to-end browser automation testing. | Selenium WebDriver, Playwright tests |
| [Parameter Object](https://java-design-patterns.com/patterns/parameter-object) | Groups related method parameters that frequently travel together into a single cohesive object. | Fixes long parameter lists (code smell) and makes method signatures resistant to change. | Methods with more than 3-4 related arguments. | Java 17+ `record`, SearchFilter request objects |
| [Property](https://java-design-patterns.com/patterns/property) | Allows dynamic addition of typed properties to an object at runtime without altering class structure. | Models objects with completely unpredictable or user-defined attributes. | Game item attributes, customizable CRM fields. | Dynamic attribute maps with typed keys |
| [Spatial Partition](https://java-design-patterns.com/patterns/spatial-partition) | Organizes objects into spatial data structures (Grids, Quadtrees, Octrees) based on their physical coordinates. | Reduces spatial search complexity from an impossible $O(N^2)$ comparison down to $O(N \log N)$ or $O(1)$. | Proximity searches (e.g. "Find nearby Uber drivers"), collision detection. | Geo-hashing, Uber H3, Quadtrees |

