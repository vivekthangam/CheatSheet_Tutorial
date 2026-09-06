[🏠 Back to Home](README.md) | [⚛️ React Master Guide](react_master_guide.md) | [🅰️ Angular Master Guide](angular_master_guide.md)

# 🌐 Frontend Polyglot: JavaScript, TypeScript, React & Angular Technical Terms Encyclopedia

[![JavaScript](https://img.shields.io/badge/JavaScript-ES2024%2B-yellow.svg?style=for-the-badge&logo=javascript)](https://developer.mozilla.org/en-US/docs/Web/JavaScript)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.5%2B-blue.svg?style=for-the-badge&logo=typescript)](https://www.typescriptlang.org/)
[![React](https://img.shields.io/badge/React-18%20%2F%2019-cyan.svg?style=for-the-badge&logo=react)](https://react.dev/)
[![Angular](https://img.shields.io/badge/Angular-18%2B-red.svg?style=for-the-badge&logo=angular)](https://angular.dev/)
[![Level](https://img.shields.io/badge/Level-Zero%20Jargon%20to%20Staff%2B-brightgreen.svg?style=for-the-badge)](https://github.com/)

An exhaustive, zero-jargon technical encyclopedia breaking down every core term, mental model, memory structure, and runtime mechanism across **JavaScript, TypeScript, React 18/19, and Angular 18+**.

Every single term in this guide strictly follows the **6-Part Zero-Ambiguity Breakdown**:
1. **Plain-English Definition & Real-World Analogy** (Zero circular jargon)
2. **Why It Exists & The Exact Problem It Solves** (What broke before this existed?)
3. **Under-the-Hood Mechanics** (How it works in V8, the Event Loop, Browser DOM, or JS Memory Heap)
4. **How To Use It** (Clean, minimal, copy-pasteable production code blueprint)
5. **Common Issues, Traps & "Gotchas"** (What catches developers off-guard?)
6. **Comparison Matrix & Key Takeaway** (How it compares to alternatives)

---

## 📑 Master Table of Contents

- [Section 1: Modern JavaScript Runtime & Memory Architecture](#section-1-modern-javascript-runtime--memory-architecture)
  - [1.1 The V8 Event Loop: Microtask Queue vs Macrotask Queue](#11-the-v8-event-loop-microtask-queue-vs-macrotask-queue)
  - [1.2 Closures, Lexical Scope & The Scope Chain](#12-closures-lexical-scope--the-scope-chain)
  - [1.3 The Prototype Chain (`__proto__` vs `prototype`)](#13-the-prototype-chain-__proto__-vs-prototype)
  - [1.4 Garbage Collection: Mark-and-Sweep, WeakMap & Memory Leaks](#14-garbage-collection-mark-and-sweep-weakmap--memory-leaks)
- [Section 2: TypeScript Deep Dive & Type System Internals](#section-2-typescript-deep-dive--type-system-internals)
  - [2.1 Structural vs Nominal Typing](#21-structural-vs-nominal-typing)
  - [2.2 Type Erasure & The Emit Phase](#22-type-erasure--the-emit-phase)
  - [2.3 Type Narrowing & Discriminated Unions](#23-type-narrowing--discriminated-unions)
  - [2.4 `unknown` vs `any` vs `never`](#24-unknown-vs-any-vs-never)
  - [2.5 Generics, Mapped Types & Conditional Types (`infer`)](#25-generics-mapped-types--conditional-types-infer)
- [Section 3: React 18/19 Internals & Fiber Architecture](#section-3-react-1819-internals--fiber-architecture)
  - [3.1 The Virtual DOM vs Incremental DOM](#31-the-virtual-dom-vs-incremental-dom)
  - [3.2 The React Fiber Tree & Reconciler Architecture](#32-the-react-fiber-tree--reconciler-architecture)
  - [3.3 Concurrent Mode, Interruptible Rendering & `useTransition`](#33-concurrent-mode-interruptible-rendering--usetransition)
  - [3.4 React Server Components (RSC) vs Client Components](#34-react-server-components-rsc-vs-client-components)
  - [3.5 SSR, Streaming HTML & Hydration Mismatch](#35-ssr-streaming-html--hydration-mismatch)
  - [3.6 React Hooks Under the Hood: The Fiber Linked List](#36-react-hooks-under-the-hood-the-fiber-linked-list)
- [Section 4: Angular 18+ Architecture & Next-Gen Reactivity](#section-4-angular-18-architecture--next-gen-reactivity)
  - [4.1 Change Detection: Default vs `OnPush`](#41-change-detection-default-vs-onpush)
  - [4.2 Zone.js vs Angular Signals (Zoneless Angular)](#42-zonejs-vs-angular-signals-zoneless-angular)
  - [4.3 Hierarchical Dependency Injection (Injector Tree)](#43-hierarchical-dependency-injection-injector-tree)
  - [4.4 RxJS Reactive Streams: Observables vs Promises vs Signals](#44-rxjs-reactive-streams-observables-vs-promises-vs-signals)
- [Section 5: State Management & Modern Build Tooling](#section-5-state-management--modern-build-tooling)
  - [5.1 Client State vs Server Cache: Zustand vs TanStack Query](#51-client-state-vs-server-cache-zustand-vs-tanstack-query)
  - [5.2 Bundlers: Webpack vs Vite (ESM Native Dev Server)](#52-bundlers-webpack-vs-vite-esm-native-dev-server)
  - [5.3 Micro-Frontends & Module Federation](#53-micro-frontends--module-federation)

---

# Section 1: Modern JavaScript Runtime & Memory Architecture

---

### 1.1 The V8 Event Loop: Microtask Queue vs Macrotask Queue
- **Plain-English Definition & Real-World Analogy:**
  JavaScript is **single-threaded**: it can only execute one line of code at any given millisecond. The **Event Loop** is a continuously running orchestrator that coordinates when synchronous code, microtasks, and macrotasks are pushed onto the Call Stack.
  *Real-World Analogy:* Think of a bank teller (the Call Stack). 
  - **Synchronous code:** The customer currently standing at the counter.
  - **Microtasks (`Promise.then`, `queueMicrotask`):** VIP customers standing in an express line. The teller MUST serve *every single VIP customer* in line before calling anyone from the general waiting lobby!
  - **Macrotasks (`setTimeout`, `setInterval`, I/O events):** Regular ticket holders waiting in the lobby.
- **Why It Exists & What It Solves:**
  If JavaScript blocked while waiting for a 2-second HTTP network response or disk read, the entire web browser would completely freeze—buttons couldn't be clicked, and CSS animations would halt. The Event Loop allows asynchronous non-blocking I/O while keeping JavaScript execution single-threaded and thread-safe.
- **Under-the-Hood Mechanics (Execution Order):**
```
1. Run ALL Synchronous Code on Call Stack until empty.
2. Check Microtask Queue: Run ALL pending microtasks until EMPTY!
   (If a microtask schedules another microtask, it runs in the SAME tick!)
3. Browser performs UI Rendering / Repaint if needed (60fps / 16.6ms cycle).
4. Pull exactly ONE task from the Macrotask Queue and push to Call Stack.
5. Repeat from Step 2!
```
- **How To Verify It in Code:**
```javascript
console.log('1. Synchronous Main'); // Step 1: Call stack executes immediately

setTimeout(() => {
  console.log('4. Macrotask (setTimeout)'); // Step 4: Scheduled in Macrotask queue
}, 0);

Promise.resolve().then(() => {
  console.log('2. Microtask (Promise)'); // Step 2: Microtask queue runs first!
}).then(() => {
  console.log('3. Nested Microtask'); // Step 2 continued: Microtask drain!
});

// Output Order:
// 1. Synchronous Main
// 2. Microtask (Promise)
// 3. Nested Microtask
// 4. Macrotask (setTimeout)
```
- **Common Issues & Traps:**
  - **Microtask Starvation:** If you trigger recursive `Promise.resolve().then(...)` microtasks in an infinite loop, the Event Loop will **never** advance to Macrotasks or UI Rendering, freezing the browser tab completely!

---

### 1.2 Closures, Lexical Scope & The Scope Chain
- **Plain-English Definition & Real-World Analogy:**
  A **Closure** is a function bundled together with references to its surrounding lexical environment. In simple terms: **A function remembers the variables from where it was created, even when executed in a completely different part of the program!**
  *Real-World Analogy:* A backpack. When a student leaves home (the outer function finishes executing), they put a notebook in their backpack. Wherever they travel in the world (the inner function called elsewhere), they can still open their backpack and read that notebook.
- **Why It Exists & What It Solves:**
  Before ES6 classes, closures were the *only* way in JavaScript to create **private variables and state encapsulation** that outside code could not directly tamper with.
- **Under-the-Hood Mechanics:**
  - When an outer function executes, the V8 engine creates an **Execution Context** containing an **Environment Record**.
  - When the outer function returns an inner function, V8 detects that the inner function references outer variables.
  - Instead of discarding the outer scope on stack pop, V8 moves those referenced variables to the **Heap Memory** and attaches a hidden `[[Scopes]]` property to the inner function instance!
- **How To Use It (Encapsulated Factory Blueprint):**
```javascript
function createSecureWallet(initialBalance) {
  // Private variable: unreachable from outside!
  let balance = initialBalance;

  return {
    deposit(amount) {
      if (amount <= 0) throw new Error("Invalid amount");
      balance += amount;
      return balance;
    },
    getBalance() {
      return balance; // Retains access to 'balance' via Closure!
    }
  };
}

const wallet = createSecureWallet(100);
wallet.deposit(50);
console.log(wallet.getBalance()); // 150
console.log(wallet.balance); // undefined (Completely private!)
```
- **Common Issues & Traps:**
  - **Accidental Memory Leaks:** If a closure inside an event listener references a large DOM node or giant array, that large object can never be garbage collected as long as the event listener exists!

---

### 1.3 The Prototype Chain (`__proto__` vs `prototype`)
- **Plain-English Definition & Real-World Analogy:**
  JavaScript does not have traditional class-based inheritance; it uses **Prototypal Inheritance**. Every object has a hidden link to another object called its **Prototype**. When you ask for a property that doesn't exist on an object, JavaScript walks up the chain until it finds it or hits `null`.
  *Real-World Analogy:* A child asking for money. If the child doesn't have it, they ask their parent. If the parent doesn't have it, they ask the grandparent. If the grandparent doesn't have it, the answer is `undefined`.
- **The Core Difference:**
  - `prototype`: A property that exists **ONLY on constructor functions / classes**. It defines the blueprint of properties that will be shared by instances created with `new`.
  - `__proto__` (or `Object.getPrototypeOf()`): A property that exists on **EVERY object instance**. It points directly to the prototype object it inherited from!
- **Under-the-Hood Traversal:**
```javascript
const user = { name: "Alice" };
user.toString(); 

// 1. Does 'user' have 'toString'? No.
// 2. Look at user.__proto__ (points to Object.prototype).
// 3. Does Object.prototype have 'toString'? YES! Execute it.
// 4. Object.prototype.__proto__ === null (End of chain).
```

---

### 1.4 Garbage Collection: Mark-and-Sweep, WeakMap & Memory Leaks
- **V8 Garbage Collection (Mark-and-Sweep):**
  - V8 starts at the **Roots** (global `window` object, current call stack local variables).
  - It traverses all object references, "marking" every object it can reach as alive.
  - In the "sweep" phase, any object in memory that was **not marked** is reclaimed.
- **The WeakMap Solution:**
  - A standard `Map` holds a **strong reference** to its keys. If you store a DOM element as a key in a standard `Map`, that DOM node will **NEVER be garbage collected**, even if removed from the HTML document!
  - A **`WeakMap`** holds only **weak references** to its keys. If no other part of the application references that DOM node, V8 will garbage collect it automatically, preventing memory leaks!
```javascript
// Memory Leak Prevention with WeakMap
const clickMetadata = new WeakMap();

const button = document.createElement('button');
clickMetadata.set(button, { clickCount: 42 });

// When button is removed from DOM:
button.remove(); 
// The { clickCount: 42 } entry is AUTOMATICALLY garbage collected from memory!
```

---

# Section 2: TypeScript Deep Dive & Type System Internals

---

### 2.1 Structural vs Nominal Typing
- **Nominal Typing (Java, C++, C#):**
  - Type compatibility is based on **explicit names and declarations**.
  - Even if `ClassA` and `ClassB` have the exact same fields, they are NOT compatible unless one inherits from the other.
- **Structural Typing / "Duck Typing" (TypeScript):**
  - Type compatibility is based **strictly on the shape of the data**, not its name!
  - *"If it walks like a duck and quacks like a duck, it is a duck."*
```typescript
type User = { id: string; name: string };
type Customer = { id: string; name: string; age: number };

let user: User;
const customer: Customer = { id: "101", name: "Bob", age: 30 };

// Perfectly VALID in TypeScript! Customer has all properties required by User.
user = customer; 
```

---

### 2.2 Type Erasure & The Emit Phase
- **Plain-English Definition:**
  TypeScript exists **ONLY during development and compilation**. When the TypeScript compiler (`tsc`) runs, it strips away 100% of types, interfaces, and type assertions, emitting pure, raw JavaScript.
- **The Critical Consequence:**
  - Types do **NOT exist at runtime**!
  - You cannot write `if (payload instanceof UserInterface)` because `UserInterface` does not exist in JavaScript bytecode!
  - Runtime validation of incoming API data must be performed using libraries like **Zod** or **Valibot**!

---

### 2.3 Type Narrowing & Discriminated Unions
- **Plain-English Definition & Real-World Analogy:**
  A **Discriminated Union** (or Tagged Union) is a union of object types that all share a single common literal field (the "discriminant tag").
  *Real-World Analogy:* Packages with different colored labels: Red = fragile glassware, Blue = heavy books. Just looking at the label tells you exactly how to handle the box safely.
- **Production Pattern Blueprint:**
```typescript
interface LoadingState {
  status: 'loading'; // Discriminant tag
}

interface SuccessState {
  status: 'success'; // Discriminant tag
  data: string[];
}

interface ErrorState {
  status: 'error';   // Discriminant tag
  error: Error;
}

type NetworkState = LoadingState | SuccessState | ErrorState;

function renderUI(state: NetworkState) {
  switch (state.status) {
    case 'loading':
      return "Spinner...";
    case 'success':
      // TypeScript automatically narrows 'state' to SuccessState!
      return `Loaded ${state.data.length} items`;
    case 'error':
      // TypeScript automatically knows 'state.error' exists!
      return `Failed: ${state.error.message}`;
  }
}
```

---

### 2.4 `unknown` vs `any` vs `never`
| Type | Mental Model | Safety Level | Operations Allowed |
| :--- | :--- | :--- | :--- |
| **`any`** | *"I don't care, turn off the type checker."* | ❌ Unsafe (Disables TS) | Anything (Can call `x.foo()`, crashes at runtime if missing) |
| **`unknown`** | *"I don't know what this is yet, force me to verify it first."* | ✅ 100% Safe | **ZERO operations** allowed until narrowed via `typeof` or `instanceof` |
| **`never`** | *"This value can mathematically never happen."* | 🛡️ Exhaustive check | Used in exhaustive `switch` statements to guarantee all cases are handled |

---

### 2.5 Generics, Mapped Types & Conditional Types (`infer`)
- **Generics (`<T>`):** Type variables that allow writing reusable, type-safe functions and classes without losing type information.
- **Mapped Types:** Transforming an existing type into a new type by iterating over its keys (`[K in keyof T]`).
- **Conditional Types (`T extends U ? X : Y`):** Selecting types based on relationships:
```typescript
// Unwrapping a Promise return type using 'infer'
type Await<T> = T extends Promise<infer U> ? U : T;

type UserPromise = Promise<{ id: string }>;
type ResolvedUser = Await<UserPromise>; // Resolves to { id: string }!
```

---

# Section 3: React 18/19 Internals & Fiber Architecture

---

### 3.1 The Virtual DOM vs Incremental DOM
- **The Problem with Real Browser DOM:**
  The browser DOM tree is massive. Reading and writing to it (`document.createElement`, modifying styles) triggers browser **Reflow (Layout calculation)** and **Repaint**, which are the slowest operations in modern browsers.
- **Virtual DOM (React):**
  - An in-memory lightweight JavaScript object representation of the real DOM.
  - When state changes, React creates a new Virtual DOM tree, computes the mathematical difference (**Diffing Algorithm**) against the previous snapshot, and batches the minimal set of changes to apply to the real DOM in one browser paint cycle.
- **Incremental DOM (Angular):**
  - Does NOT build an in-memory Virtual DOM tree!
  - Compiles templates directly into bytecode instructions that mutate the real DOM in place when dirty values change, reducing heap memory overhead on mobile devices.

---

### 3.2 The React Fiber Tree & Reconciler Architecture
- **Why React 16+ Rebuilt the Reconciler (Fiber):**
  In older React (v15), reconciliation was synchronous and recursive ("Stack Reconciler"). If a complex page had 10,000 components, updating state locked the main browser thread for 100ms+, causing dropped animation frames and stuttering typing inputs (**Jank**).
- **The Fiber Mental Model:**
  - A **Fiber** is a plain JavaScript object that represents a unit of work.
  - It converts the component tree into a **Singly-Linked List** with 3 pointers:
    - `child`: Points to its first child component.
    - `sibling`: Points to its next sibling.
    - `return`: Points to its parent component.
- **The Magic of Fiber:**
  Because it is a linked list rather than a recursive call stack, **React can pause execution in the middle of rendering**, yield control back to the browser to handle an urgent mouse click or keystroke, and then resume rendering where it left off!

---

### 3.3 Concurrent Mode, Interruptible Rendering & `useTransition`
- **Urgent vs Non-Urgent Updates:**
  - **Urgent:** Typing into a search input, clicking a tab button (expects immediate 16ms visual feedback).
  - **Non-Urgent / Transition:** Filtering a list of 5,000 search results based on that text input.
- **`useTransition` in Action:**
```jsx
function SearchPage() {
  const [query, setQuery] = useState("");
  const [filteredList, setFilteredList] = useState([]);
  const [isPending, startTransition] = useTransition();

  function handleInputChange(e) {
    // 1. URGENT update: Updates text input immediately with zero lag!
    setQuery(e.target.value);

    // 2. NON-URGENT update: React computes this in background without freezing UI!
    startTransition(() => {
      setFilteredList(heavyFilterAlgorithm(e.target.value));
    });
  }

  return (
    <div>
      <input value={query} onChange={handleInputChange} />
      {isPending && <p>Updating list...</p>}
      <Results list={filteredList} />
    </div>
  );
}
```

---

### 3.4 React Server Components (RSC) vs Client Components
- **Client Components (`'use client'`):**
  - Run in the browser (and during SSR).
  - Can use `useState`, `useEffect`, event listeners (`onClick`), and browser APIs (`window`, `localStorage`).
  - Their JavaScript code is bundled and shipped over the network to the browser.
- **React Server Components (RSC - Default in Next.js App Router):**
  - Execute **ONLY on the Node.js server**.
  - **Zero JavaScript shipped to the client!** Eliminates massive dependencies (like Markdown parsers or date formatters) from the client bundle.
  - Can directly query databases via SQL or read files from the server filesystem safely:
```jsx
// React Server Component: Runs ONLY on server! Zero client bundle weight!
export default async function ProductPage({ params }) {
  const product = await db.products.findUnique({ where: { id: params.id } });

  return (
    <div>
      <h1>{product.name}</h1>
      <p>${product.price}</p>
      {/* Interactive client component imported inside server component */}
      <AddToCartButton productId={product.id} />
    </div>
  );
}
```

---

### 3.5 SSR, Streaming HTML & Hydration Mismatch
- **Hydration:**
  When Server-Side Rendering (SSR) generates raw HTML on the server and sends it to the browser, the page is visible immediately, but non-interactive (clicking buttons does nothing). **Hydration** is the process where client-side React loads, walks the existing HTML DOM, and attaches JavaScript event listeners to make it interactive.
- **Hydration Mismatch Catastrophe:**
  If the HTML generated on the server does not match the HTML React renders on the client (e.g. rendering `new Date().toLocaleTimeString()` or checking `window.innerWidth`), React throws a **Hydration Error** and is forced to destroy the DOM and re-render from scratch!

---

### 3.6 React Hooks Under the Hood: The Fiber Linked List
- **Why Can Hooks Never Be Called Inside `if` Statements or Loops?**
  React does **NOT** store component state by variable name.
  Inside each component's Fiber node, there is a **singly-linked list of Hook nodes**:
  `Fiber.memoizedState -> Hook1 -> Hook2 -> Hook3`
  - On the first render, React initializes Hook1 (`useState`), Hook2 (`useEffect`), Hook3 (`useState`).
  - On the next render, React assumes Hook1 matches the first call, Hook2 matches the second call, etc.
  - If you wrap Hook2 in an `if (condition)`:
    When the condition is false, Hook3 is invoked second! React tries to read Hook2's state for Hook3, **corrupting component state completely!**

---

# Section 4: Angular 18+ Architecture & Next-Gen Reactivity

---

### 4.1 Change Detection: Default vs `OnPush`
- **Default Change Detection (Zone.js):**
  - Whenever ANY event happens (click, `setTimeout`, HTTP response), Angular traverses the **ENTIRE component tree** from the root down, checking every single binding in the application.
  - In large applications with thousands of components, this causes massive CPU usage.
- **`ChangeDetectionStrategy.OnPush`:**
  - Instructs Angular to skip this component and its children **UNLESS**:
    1. An `@Input()` reference physically changes (`prev !== curr` by object identity).
    2. An event handler inside this component fires (`(click)`).
    3. An Observable bound via `| async` pipe emits a new value.
    4. A Signal read by the template updates!

---

### 4.2 Zone.js vs Angular Signals (Zoneless Angular)
- **Zone.js (Legacy Monkey-Patching):**
  - Zone.js monkey-patches browser APIs (`addEventListener`, `setTimeout`, `fetch`).
  - When a patched API completes, Zone.js triggers a top-to-bottom change detection cycle.
  - *Downside:* Zone.js cannot tell *which specific component* changed; it just knows *something* happened.
- **Angular Signals (Angular 16+, Standard in 18+):**
  - **Fine-Grained Reactivity (Zoneless):**
  - A Signal is a reactive value wrapper (`signal(10)`).
  - When a component template reads a Signal, Angular creates a **direct dependency subscription**.
  - When `mySignal.set(20)` is called, Angular updates **ONLY that specific DOM text node directly**, without needing Zone.js or checking any other component in the tree!
```typescript
@Component({
  selector: 'app-counter',
  standalone: true,
  template: `
    <p>Count: {{ count() }}</p>
    <p>Double: {{ doubleCount() }}</p>
    <button (click)="increment()">+1</button>
  `
})
export class CounterComponent {
  count = signal(0); // Writable Signal
  doubleCount = computed(() => this.count() * 2); // Derived Signal

  increment() {
    this.count.update(c => c + 1); // Fine-grained DOM update!
  }
}
```

---

### 4.3 Hierarchical Dependency Injection (Injector Tree)
- **Angular's Injector Hierarchy:**
  - Unlike Spring Boot (which defaults to a flat global application context), Angular has a **tree of injectors**:
    1. **`EnvironmentInjector` (Root):** Singleton services declared with `@Injectable({ providedIn: 'root' })`. Shared across the entire app.
    2. **Element / Component Injector:** Declared in `@Component({ providers: [LocalService] })`. Every instance of that component receives its own private instance of `LocalService`, destroyed when the component is unmounted!

---

### 4.4 RxJS Reactive Streams: Observables vs Promises vs Signals
| Feature | Promise | RxJS Observable | Angular Signal |
| :--- | :--- | :--- | :--- |
| **Emission Count** | Single value | Multiple values over time (stream) | Synchronous current value |
| **Execution** | Eager (starts immediately) | Cold (starts only on `.subscribe()`) | Glitch-free, synchronous pull |
| **Cancellation**| ❌ Cannot cancel | ✅ Can cancel (`unsubscribe()`) | N/A (reactive value) |
| **Best For** | One-off `fetch()` calls | WebSocket events, debounced search inputs | UI Component state & template rendering |

---

# Section 5: State Management & Modern Build Tooling

---

### 5.1 Client State vs Server Cache: Zustand vs TanStack Query
- **The Modern Paradigm Shift:**
  In the past, developers stored all backend API responses in global Redux stores.
  Today, state is strictly bifurcated:
  1. **Server Cache (TanStack Query / React Query):**
     - Asynchronous, remote, owned by the database.
     - Handles caching, background re-fetching, deduplication, polling, and cache invalidation.
  2. **Client State (Zustand / Redux Toolkit):**
     - Synchronous, local, owned by the browser UI (e.g. dark mode toggle, modal open/close, multi-step wizard step).
     - Lightweight; no API request logic.

---

### 5.2 Bundlers: Webpack vs Vite (ESM Native Dev Server)
- **Webpack (Bundle-Based Dev Server):**
  - Crawls your entire application and bundles all JavaScript into disk/memory bundles *before* the development server can start.
  - On a project with 5,000 modules, cold startup takes 40 seconds, and Hot Module Replacement (HMR) takes 3–5 seconds.
- **Vite (Native ES Modules):**
  - Starts instantly (sub-200ms) because it does **NOT bundle code in development**!
  - Serves source files directly over native browser ES Modules (`import`). The browser requests only the exact file currently being viewed!
  - Uses ultra-fast **esbuild** (written in Go) for pre-bundling dependencies.

---

### 5.3 Micro-Frontends & Module Federation
- **What is Module Federation (Webpack 5)?**
  Allows a JavaScript application to dynamically import and execute code from another independently deployed JavaScript application at runtime.
- **How It Works in Production:**
  - Team A deploys the **Host Shell** at `app.company.com`.
  - Team B deploys the **Checkout Micro-Frontend** at `checkout.company.com/remoteEntry.js`.
  - The Host loads Team B's component dynamically over the network. Team B can deploy bug fixes in 30 seconds without Team A needing to recompile or redeploy the Host Shell!

---

## 🧭 Frontend Terminology Quick Reference Cheat Sheet

| Domain | Key Term | One-Sentence Summary |
| :--- | :--- | :--- |
| **JS** | **Event Loop** | Single-threaded orchestrator draining microtasks before rendering and macrotasks. |
| **JS** | **Closure** | Function retaining access to its lexical scope variables even when executed elsewhere. |
| **TS** | **Structural Typing** | Types are compatible if their shapes match, regardless of declared class names. |
| **TS** | **Discriminated Union** | Union of object types sharing a common literal tag for type narrowing. |
| **React** | **Fiber** | Linked-list unit of work enabling interruptible, concurrent rendering. |
| **React** | **RSC** | Server Components that run only on Node.js and ship 0KB JavaScript to the client. |
| **Angular**| **Signals** | Fine-grained reactive values updating exact DOM nodes directly without Zone.js. |
| **Angular**| **OnPush** | Change detection strategy skipping component verification unless inputs change. |
| **Build** | **Vite** | Dev server serving unbundled native ES Modules for sub-second startup. |

---
[🏠 Back to Home](README.md) | [⚛️ React Master Guide](react_master_guide.md) | [🅰️ Angular Master Guide](angular_master_guide.md)
