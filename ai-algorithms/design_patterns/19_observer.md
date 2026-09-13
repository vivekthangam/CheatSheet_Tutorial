# 19. Observer Pattern: Event Pub/Sub & High-Velocity Broadcasting

[![Pattern: Behavioral](https://img.shields.io/badge/Pattern-Behavioral-f59e0b.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![Event-Driven](https://img.shields.io/badge/Architecture-Event--Driven%20Pub%2FSub-brightgreen.svg?style=for-the-badge)]()
[![Memory Safe](https://img.shields.io/badge/Memory-Zero--Leak%20WeakRef-blue.svg?style=for-the-badge)]()
[![Async Ready](https://img.shields.io/badge/Concurrency-AsyncIO%20%26%20Thread--Safe-purple.svg?style=for-the-badge)]()

> **Intent**: Define a one-to-many dependency between objects so that when one object changes state, all its dependents are notified and updated automatically without coupling the publisher to concrete subscriber classes.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine clicking the **YouTube Notification Bell** on a channel with 10 million subscribers:
- **The Creator (Subject / Publisher)** does not maintain 10 million private phone numbers and call each fan individually: *"Hey, are you awake? I just uploaded a video!"*
- Instead, fans **Subscribe** to the channel, expressing interest in future events.
- When the creator uploads a new video, the central broadcast system fans out an alert event to every active subscriber device simultaneously.
- If a user loses interest, they click **Unsubscribe**, and the system immediately removes them from future broadcasts without affecting any other subscribers.

In enterprise software engineering, the **Observer Pattern** decouples the core domain entity that generates business events (e.g., Stock Exchange Price Ticker, Payment Gateway, Sensor Telemetry) from the multitude of independent downstream consumers (Algorithmic Trading Bots, Audit Ledgers, Push Notification Engines, Webhook Dispatchers).

---

## 🖼️ Architectural Blueprint

![Behavioral Design Patterns: Observer Architecture](../../assets/images/design_patterns/behavioral_patterns_architecture.jpg)

### Push vs. Pull Model Comparison

| Dimension | Push Model (Event-Driven) | Pull Model (Polling / Reactive) |
| :--- | :--- | :--- |
| **Data Flow** | Subject sends the complete state payload inside the event object. | Subject sends a minimal notification signal; observer calls getters on the subject. |
| **Coupling** | Low coupling. Observers only depend on an immutable Event DTO. | Higher coupling. Observers hold a reference to the Subject to inspect its state. |
| **Network & CPU Efficiency** | High efficiency. Observers execute only when a state change genuinely occurs. | Can be inefficient if observers pull redundant or oversized state structures. |
| **Best Used For** | Real-time financial tickers, user activity feeds, distributed message brokers. | Complex UI frameworks where observers only need a subset of 50 different fields. |

---

## 📐 Algorithmic & Complexity Analysis

| Operation | Time Complexity | Space Complexity | Description |
| :--- | :--- | :--- | :--- |
| **Subscribe** | $\mathcal{O}(1)$ | $\mathcal{O}(1)$ | Appending or hashing subscriber handle in an internal registry. |
| **Unsubscribe** | $\mathcal{O}(1)$ amortized | $\mathcal{O}(1)$ | Hash-lookup removal from observer set or dictionary. |
| **Broadcast (Fan-Out)** | $\mathcal{O}(N)$ | $\mathcal{O}(N)$ | Iterating over $N$ registered observers and invoking callbacks. |
| **Memory Footprint** | — | $\mathcal{O}(N)$ | Proportional to active listeners; minimized using `weakref` proxies. |

---

## 💻 Production Python 3.12+ Blueprint 1: Synchronous & Thread-Safe Market Data Feed

This blueprint implements a high-throughput financial exchange price feed. It uses **Double-Checked Synchronization**, **Deadlock-Free Snapshotting**, and **Weak References (`weakref`)** to solve the notorious *Lapsed Listener Problem* (preventing memory leaks when observers are deleted).

```python
"""
Thread-Safe Synchronous Observer Engine in Python 3.12+.
Features zero-leak weak referencing and reentrant lock snapshotting.
"""

from __future__ import annotations
from dataclasses import dataclass
from decimal import Decimal
import threading
import time
from typing import Callable, Protocol, runtime_checkable
import weakref


@dataclass(frozen=True, slots=True)
class PriceTickEvent:
    """Immutable Event Data Transfer Object (DTO)."""
    symbol: str
    price: Decimal
    volume: int
    exchange_timestamp_ns: int


@runtime_checkable
class MarketDataSubscriber(Protocol):
    """Observer Protocol defining the contract for event reception."""

    def on_price_tick(self, event: PriceTickEvent) -> None:
        """Invoked synchronously when a new price tick arrives."""
        ...


class MarketDataFeed:
    """
    Subject / Publisher Class.
    Broadcasts real-time ticks to hundreds of concurrent observers.
    """

    def __init__(self, exchange_id: str) -> None:
        self.exchange_id = exchange_id
        # WeakSet automatically discards subscribers when no strong references remain
        self._weak_subscribers: weakref.WeakSet[MarketDataSubscriber] = weakref.WeakSet()
        # Strong set backup for callers that explicitly request strong lifecycle ownership
        self._strong_subscribers: set[MarketDataSubscriber] = set()
        self._lock = threading.RLock()
        self._tick_counter: int = 0

    def subscribe(self, subscriber: MarketDataSubscriber, hold_strong_ref: bool = False) -> None:
        """Registers a new observer with thread safety."""
        with self._lock:
            if hold_strong_ref:
                self._strong_subscribers.add(subscriber)
            self._weak_subscribers.add(subscriber)
            print(f"[{self.exchange_id}] Registered subscriber: {type(subscriber).__name__} (Strong: {hold_strong_ref})")

    def unsubscribe(self, subscriber: MarketDataSubscriber) -> None:
        """Removes an observer from both weak and strong registries."""
        with self._lock:
            self._strong_subscribers.discard(subscriber)
            self._weak_subscribers.discard(subscriber)
            print(f"[{self.exchange_id}] Unregistered subscriber: {type(subscriber).__name__}")

    def publish_tick(self, symbol: str, price: Decimal, volume: int) -> None:
        """
        Broadcasts an event to all subscribers.
        
        CRITICAL ARCHITECTURAL RULE:
        We take a shallow snapshot of active observers under the lock, then release
        the lock BEFORE invoking user callbacks. This completely prevents deadlocks
        if an observer's callback attempts to unsubscribe or register a new listener.
        """
        event = PriceTickEvent(
            symbol=symbol,
            price=price,
            volume=volume,
            exchange_timestamp_ns=time.time_ns(),
        )

        # 1. Snapshot active subscribers under lock
        with self._lock:
            self._tick_counter += 1
            observers_snapshot = list(self._weak_subscribers)

        # 2. Fan-out notification OUTSIDE lock to eliminate contention
        for observer in observers_snapshot:
            try:
                observer.on_price_tick(event)
            except Exception as exc:
                # Isolate faulty observers so one failing subscriber does not crash the pipeline
                print(f"⚠️ [Publisher Error] Failed notifying {type(observer).__name__}: {exc}")

    @property
    def active_subscriber_count(self) -> int:
        with self._lock:
            return len(self._weak_subscribers)


# =====================================================================
# Concrete Observers
# =====================================================================

class AlgorithmicTradingBot:
    """High-frequency automated trading observer."""

    def __init__(self, bot_id: str, buy_limit: Decimal) -> None:
        self.bot_id = bot_id
        self.buy_limit = buy_limit

    def on_price_tick(self, event: PriceTickEvent) -> None:
        if event.price <= self.buy_limit:
            print(
                f"🤖 [{self.bot_id}] BUY ORDER FIRED! {event.symbol} at ${event.price:,.2f} "
                f"<= limit ${self.buy_limit:,.2f} (Vol: {event.volume})"
            )


class RegulatoryAuditLogger:
    """Compliance observer writing audit log entries."""

    def __init__(self) -> None:
        self._audit_records: list[str] = []

    def on_price_tick(self, event: PriceTickEvent) -> None:
        record = f"AUDIT: {event.symbol} traded at {event.price} (Qty: {event.volume}) at {event.exchange_timestamp_ns}"
        self._audit_records.append(record)
        print(f"📋 [Compliance] Logged trade verification for {event.symbol}.")


# =====================================================================
# 🧪 Execution & Verification
# =====================================================================

if __name__ == "__main__":
    feed = MarketDataFeed(exchange_id="NASDAQ-EQUITIES")

    bot = AlgorithmicTradingBot(bot_id="AlphaBot-01", buy_limit=Decimal("150.00"))
    auditor = RegulatoryAuditLogger()

    feed.subscribe(bot)
    feed.subscribe(auditor, hold_strong_ref=True)

    print(f"\n--- Active Listeners: {feed.active_subscriber_count} ---")
    feed.publish_tick("AAPL", Decimal("152.50"), 1000)
    feed.publish_tick("AAPL", Decimal("149.25"), 2500)

    # Demonstrate Weak Reference Memory Cleanup
    print("\n--- Deleting AlgorithmicTradingBot Instance ---")
    del bot  # No strong references left in caller scope

    import gc
    gc.collect()

    print(f"Active Listeners after GC: {feed.active_subscriber_count}")
    assert feed.active_subscriber_count == 1, "Memory leak! Weak subscriber was not garbage collected."

    feed.publish_tick("AAPL", Decimal("148.00"), 500)
```

---

## ⚡ Production Python 3.12+ Blueprint 2: High-Throughput Asyncio Event Bus

In modern asynchronous architectures (such as FastAPI, microservice event gateways, and WebSocket streamers), notifications must not block the main event loop. This blueprint demonstrates an **Asynchronous Topic-Based Event Bus** with **Wildcard Subscriptions**, **Slow-Consumer Backpressure**, and **Isolated Task Queues**.

```python
"""
Asynchronous Non-Blocking Event Bus in Python 3.12+.
Features wildcard topic matching, concurrent fan-out, and timeout isolation.
"""

from __future__ import annotations
import asyncio
from dataclasses import dataclass
from typing import Any, Callable, Coroutine, Dict, List, Set
import fnmatch


@dataclass(slots=True, frozen=True)
class ApplicationEvent:
    topic: str
    payload: dict[str, Any]
    correlation_id: str


AsyncListener = Callable[[ApplicationEvent], Coroutine[Any, Any, None]]


class AsyncEventBus:
    """
    Central Asynchronous Pub/Sub Event Bus.
    Supports topic wildcards (e.g., 'order.*', 'order.payment.completed').
    """

    def __init__(self, dispatch_timeout_seconds: float = 2.0) -> None:
        self._listeners: Dict[str, Set[AsyncListener]] = {}
        self._dispatch_timeout = dispatch_timeout_seconds
        self._lock = asyncio.Lock()

    async def subscribe(self, topic_pattern: str, listener: AsyncListener) -> None:
        """Subscribes an async coroutine to a specific topic or wildcard pattern."""
        async with self._lock:
            if topic_pattern not in self._listeners:
                self._listeners[topic_pattern] = set()
            self._listeners[topic_pattern].add(listener)
            print(f"[AsyncBus] Subscribed '{listener.__name__}' to pattern '{topic_pattern}'")

    async def unsubscribe(self, topic_pattern: str, listener: AsyncListener) -> None:
        """Removes an async listener from a topic pattern."""
        async with self._lock:
            if topic_pattern in self._listeners:
                self._listeners[topic_pattern].discard(listener)
                if not self._listeners[topic_pattern]:
                    del self._listeners[topic_pattern]

    async def publish(self, topic: str, payload: dict[str, Any], correlation_id: str) -> None:
        """
        Publishes an event to all matching topic subscribers concurrently.
        Uses asyncio.gather with timeout isolation so one slow consumer does
        not stall the entire event dispatching system.
        """
        event = ApplicationEvent(topic=topic, payload=payload, correlation_id=correlation_id)

        # 1. Match active listeners based on pattern
        matched_listeners: list[AsyncListener] = []
        async with self._lock:
            for pattern, listeners in self._listeners.items():
                if fnmatch.fnmatch(topic, pattern):
                    matched_listeners.extend(listeners)

        if not matched_listeners:
            return

        # 2. Create isolated concurrent tasks with timeout guards
        async def _safe_dispatch(listener: AsyncListener) -> None:
            try:
                async with asyncio.timeout(self._dispatch_timeout):
                    await listener(event)
            except TimeoutError:
                print(f"⚠️ [Slow Consumer Warning] Listener '{listener.__name__}' timed out on topic '{topic}'!")
            except Exception as err:
                print(f"❌ [Consumer Exception] Listener '{listener.__name__}' raised error: {err}")

        # Fan-out all matched handlers concurrently
        await asyncio.gather(*(_safe_dispatch(l) for l in matched_listeners))


# =====================================================================
# Concrete Async Consumers
# =====================================================================

async def email_notification_consumer(event: ApplicationEvent) -> None:
    await asyncio.sleep(0.05)  # Simulate network latency to SMTP server
    print(f"📧 [Email Service] Confirmation sent to {event.payload.get('email')} for {event.topic}")


async def warehouse_inventory_consumer(event: ApplicationEvent) -> None:
    await asyncio.sleep(0.02)
    sku = event.payload.get("sku")
    qty = event.payload.get("quantity")
    print(f"📦 [Warehouse Service] Reserved {qty}x SKU '{sku}' [CorrID: {event.correlation_id}]")


async def slow_analytics_consumer(event: ApplicationEvent) -> None:
    # Deliberately exceeds timeout to demonstrate consumer isolation
    await asyncio.sleep(3.0)
    print("📊 [Analytics] Successfully processed big data event.")


# =====================================================================
# 🧪 Async Demonstration
# =====================================================================

async def main() -> None:
    bus = AsyncEventBus(dispatch_timeout_seconds=0.5)

    await bus.subscribe("order.*", email_notification_consumer)
    await bus.subscribe("order.created", warehouse_inventory_consumer)
    await bus.subscribe("order.*", slow_analytics_consumer)

    print("\n--- Publishing: order.created ---")
    await bus.publish(
        topic="order.created",
        payload={"order_id": "ORD-9912", "email": "customer@faang.com", "sku": "IPHONE-16", "quantity": 1},
        correlation_id="CORR-7701",
    )


if __name__ == "__main__":
    asyncio.run(main())
```

---

## 🔍 Deep-Dive Step-by-Step Architectural Tutorial

### 1. The Lapsed Listener Problem (Hidden Memory Leaks)
In CPython, an object is deallocated when its reference count drops to zero (`ob_refcnt == 0`).
- If a short-lived component (e.g., a modal dialog or dynamic microservice connection) registers itself with a long-lived publisher (e.g., `GlobalAppEventBus`):
  ```python
  self.bus.register(self) # The bus now holds a STRONG reference to self!
  ```
- When the caller deletes the short-lived component (`del component`), the object **cannot be garbage-collected** because the publisher still holds a pointer to it in `self._subscribers`.
- Over days or weeks in production, this leads to fatal out-of-memory crashes (`OOMKilled`).
- **The Solution**: Always use `weakref.WeakSet` or store `weakref.ref(observer)`. When all user references disappear, CPython's reference counter cleanly drops to 0 and reclaims memory.

### 2. Deadlock-Free Snapshotting Under Concurrency
A severe anti-pattern in naive Observer implementations is holding the publisher's lock while notifying listeners:
```python
# ❌ DEADLOCK DANGER
def publish(self, event):
    with self._lock:
        for sub in self._subscribers:
            sub.on_event(event) # If sub calls self.unsubscribe(), it attempts to re-acquire self._lock -> DEADLOCK!
```
- If an observer needs to unregister itself upon receiving a specific terminal message, or trigger a secondary event, holding the lock creates an instant deadlock.
- **The Solution**: Copy the collection under lock in $\mathcal{O}(N)$ time: `snapshot = list(self._subscribers)`. Release the lock immediately, and then iterate through `snapshot`.

---

## 🐍 CPython Internals, GIL & Memory Architecture

| Memory Attribute | Standard Strong Reference (`list[Observer]`) | Zero-Leak Weak Reference (`weakref.WeakSet`) |
| :--- | :--- | :--- |
| **`ob_refcnt` Increment** | Increments caller `ob_refcnt` by 1. | Does NOT increment `ob_refcnt` (allocates adjacent `PyWeakReference`). |
| **Garbage Collection Trigger** | Blocked forever until publisher explicitly calls `remove()`. | Immediate reclamation as soon as caller scope drops the variable. |
| **Production Failure Mode** | High risk of `OOMKilled` (Lapsed Listener memory leak). | Zero memory leaks; automatic sweep upon garbage collection. |
| **Internal Data Structure** | Dynamic array of C pointers (`PyObject**`). | Hash set of weakref proxies with automatic dead-reference eviction. |

1. **Weak Reference Representation (`PyWeakReference`)**:
   Under the hood in CPython, `weakref.ref` allocates an auxiliary `PyWeakReference` struct that sits adjacent to the target object. It holds a pointer to the object without incrementing `ob_refcnt`. When the target object's reference count drops to 0, CPython's `type->tp_dealloc` sweeps the weak reference pointers and notifies any registered cleanup callbacks.

2. **GIL Impact on Synchronous Fan-Out**:
   In standard CPython (prior to Python 3.13 free-threaded mode), multi-threaded broadcasting does not achieve CPU parallelism for pure Python bytecode execution. If 50 observers perform heavy CPU computations inside `on_tick()`, running them across 50 OS threads will cause severe GIL lock contention.
   - **Remedy**: For CPU-bound tasks, the observer should push the event into a multiprocessing queue (`multiprocessing.Queue`) or offload to a Celery/Redis worker cluster.

---

## 🌐 Real-World Enterprise Production Scenarios

### Scenario 1: Financial Order Execution & Dynamic Risk Limits
When a trader submits an order:
- The `OrderExecutionService` publishes `OrderSubmittedEvent`.
- **Observer 1 (`MarginAccountant`)**: Deducts buying power.
- **Observer 2 (`RiskEngine`)**: Evaluates real-time VaR (Value at Risk) against portfolio limits.
- **Observer 3 (`AuditTrailService`)**: Writes tamper-proof records to an append-only ledger.

### Scenario 2: E-Commerce Post-Purchase Orchestration
Following a successful checkout transaction:
- The checkout microservice publishes `OrderPaidEvent`.
- **Subscribers**:
  - `InventoryService`: Decrements warehouse physical stock.
  - `NotificationService`: Sends SMS and email receipts to the customer.
  - `FraudDetectionService`: Updates the user's velocity scoring metrics.
  - `RecommendationEngine`: Recalculates personalized product affinities.

---

## 🔥 SEV-1 War Room Post-Mortem: The Slow Consumer Event Loop Collapse

### Incident Overview
- **Incident Severity**: **SEV-1 (Global Payment Gateway Ingress Failure)**
- **Mean Time to Detection (MTTD)**: 4 minutes
- **Mean Time to Resolution (MTTR)**: 38 minutes
- **Impact**: 140,000 checkout payment transactions failed globally with HTTP 504 Gateway Timeout errors.

### Root Cause Analysis (RCA)
1. The engineering team deployed a new `SlackWebhookAuditObserver` that sent real-time payment alerts to an internal Slack channel whenever high-value transactions occurred.
2. The observer was registered synchronously on the main payment transaction pipeline.
3. During peak traffic, the third-party Slack incoming webhook API experienced a 2,500ms latency degradation.
4. Because the publisher iterated over observers sequentially without concurrency or timeouts, every incoming payment thread was blocked waiting for Slack's HTTP socket to respond.
5. Within 90 seconds, all 500 Gunicorn worker threads were exhausted, blocking the primary HTTP connection pool and causing total gateway collapse.

### Permanent Engineering Remediation
1. **Strict Asynchronous Decoupling**: All audit, logging, and notification observers were migrated to the asynchronous event bus (`AsyncEventBus`) powered by non-blocking background workers.
2. **Timeout Circuit Breaker**: Introduced `asyncio.timeout(0.5)` on all subscriber callbacks. Any consumer exceeding 500ms is immediately aborted without affecting core payment execution.
3. **Queue-Based Backpressure**: Observers consume events via bounded queues (`asyncio.Queue(maxsize=1000)`). If a downstream consumer slows down, messages drop into a dead-letter queue (DLQ) rather than choking the publisher.

---

## 📊 Staff Engineer Best Practices & Anti-Patterns Checklist

| Anti-Pattern (What NOT to do) | Staff Engineer Best Practice |
| :--- | :--- |
| **Direct Strong References**: Storing observers in a standard `list` without lifecycle cleanup, causing memory leaks. | **Use `weakref.WeakSet`**: Ensure observers can be garbage collected as soon as their owning scope terminates. |
| **Holding Locks During Callback Execution**: Invoking observer methods inside a `with self._lock:` block. | **Take an Immutable Snapshot**: Copy the observer list under lock, release the lock, and iterate safely. |
| **Synchronous External I/O**: Calling remote HTTP webhooks or disk writes directly inside an observer callback. | **Asynchronous Worker Handoff**: Push the event to an async queue or background thread pool executor. |
| **Unprotected Exceptions**: Allowing one buggy observer to raise an unhandled exception and abort notifications to all subsequent observers. | **Wrap Dispatch in `try/except`**: Log subscriber failures independently and continue the notification loop. |
| **Unbounded Event Payloads**: Passing massive mutable data structures inside event objects. | **Immutable Event DTOs**: Use frozen dataclasses (`@dataclass(frozen=True, slots=True)`) to ensure integrity. |
