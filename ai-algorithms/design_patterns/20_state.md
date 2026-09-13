# 20. State Pattern: Finite State Machine Order Lifecycles

[![Pattern: Behavioral](https://img.shields.io/badge/Pattern-Behavioral-f59e0b.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![FSM](https://img.shields.io/badge/Architecture-Finite%20State%20Machine-blue.svg?style=for-the-badge)]()

> **Intent**: Allow an object to alter its behavior when its internal state changes. The object will appear to change its class.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine a **Traffic Light**:
- When the light is **Green**, stepping on the gas pedal means: `"Drive forward safely"`.
- When the light is **Yellow**, stepping on the gas pedal means: `"Prepare to stop or proceed with caution"`.
- When the light is **Red**, stepping on the gas pedal means: `"ILLEGAL ACTION - CRASH / TICKET!"`.
- The car pedal didn't physically change; the **State of the Traffic Intersection** changed how actions are interpreted!

In software, an E-commerce Order moves through states: **Pending Payment → Processing → Shipped → Delivered → Cancelled**.
If a customer clicks **"Cancel Order"** when the state is `Pending Payment`, the order is safely cancelled.
If they click **"Cancel Order"** when the state is already `Delivered`, the system must **throw an IllegalTransition error**! The **State Pattern** eliminates 50 messy nested `if/elif/else` statements by encapsulating each state into its own class.

---

## 🖼️ Architectural Diagram

![Behavioral Design Patterns: State Architecture](../../assets/images/design_patterns/behavioral_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

```python
"""
State Pattern for Distributed Order Fulfillment Lifecycle in Python 3.12+.
Enforces strict finite state transitions and prevents illegal business mutations.
"""

from __future__ import annotations
from abc import ABC, abstractmethod
from typing import Protocol, runtime_checkable


class OrderContext:
    """
    Context Object holding the current state reference and delegating actions.
    """

    def __init__(self, order_id: str) -> None:
        self.order_id = order_id
        # Initial starting state
        self._state: OrderState = PendingPaymentState()

    def transition_to(self, state: OrderState) -> None:
        print(f"[State Machine] Order '{self.order_id}': {self._state.name} ──► {state.name}")
        self._state = state

    def proceed(self) -> None:
        self._state.proceed(self)

    def cancel(self) -> None:
        self._state.cancel(self)

    @property
    def status(self) -> str:
        return self._state.name


class OrderState(ABC):
    """Abstract Base State defining valid transition triggers."""
    name: str

    @abstractmethod
    def proceed(self, context: OrderContext) -> None: ...

    @abstractmethod
    def cancel(self, context: OrderContext) -> None: ...


# ---------------------------------------------------------------------
# Concrete States
# ---------------------------------------------------------------------
class PendingPaymentState(OrderState):
    name = "PENDING_PAYMENT"

    def proceed(self, context: OrderContext) -> None:
        print("  -> Payment confirmed! Moving order to warehouse processing.")
        context.transition_to(ProcessingState())

    def cancel(self, context: OrderContext) -> None:
        print("  -> Order cancelled before payment was captured.")
        context.transition_to(CancelledState())


class ProcessingState(OrderState):
    name = "PROCESSING"

    def proceed(self, context: OrderContext) -> None:
        print("  -> Items packed & scanned by FedEx. Package dispatched!")
        context.transition_to(ShippedState())

    def cancel(self, context: OrderContext) -> None:
        print("  -> Order cancelled during packing; restocking items to warehouse.")
        context.transition_to(CancelledState())


class ShippedState(OrderState):
    name = "SHIPPED"

    def proceed(self, context: OrderContext) -> None:
        print("  -> Delivery confirmed at recipient doorstep!")
        context.transition_to(DeliveredState())

    def cancel(self, context: OrderContext) -> None:
        # Business rule: Cannot cancel an order in transit
        raise RuntimeError("Illegal Operation: Cannot cancel order while in transit with courier. Must initiate Return.")


class DeliveredState(OrderState):
    name = "DELIVERED"

    def proceed(self, context: OrderContext) -> None:
        print("  -> Order already delivered. Archiving order record.")

    def cancel(self, context: OrderContext) -> None:
        raise RuntimeError("Illegal Operation: Cannot cancel already delivered order.")


class CancelledState(OrderState):
    name = "CANCELLED"

    def proceed(self, context: OrderContext) -> None:
        raise RuntimeError("Illegal Operation: Cannot advance an order that has been cancelled.")

    def cancel(self, context: OrderContext) -> None:
        print("  -> Order is already in cancelled state.")


# =====================================================================
# 🧪 Execution & Verification
# =====================================================================
if __name__ == "__main__":
    order = OrderContext("ORD-99104")
    print(f"Initial Status: {order.status}\n")

    # Legal progression
    order.proceed() # PENDING_PAYMENT -> PROCESSING
    order.proceed() # PROCESSING -> SHIPPED
    print(f"Current Status: {order.status}\n")

    # Attempt illegal business transition
    print(">>> Attempting illegal cancellation on SHIPPED order:")
    try:
        order.cancel()
    except RuntimeError as err:
        print(f"Caught Expected Error: {err}\n")

    # Complete legal delivery
    order.proceed() # SHIPPED -> DELIVERED
    print(f"Final Status: {order.status}")
```

---

## 🔍 Step-by-Step Code Tutorial

1. **Eliminating Cyclomatic Complexity**:
   Without the State pattern, every method looks like:
   ```python
   if self.state == "PENDING": ...
   elif self.state == "PROCESSING": ...
   elif self.state == "SHIPPED": ...
   elif self.state == "DELIVERED": ...
   ```
   Adding a new state (e.g., `REFUNDED`) requires hunting down and modifying 10 different methods. With the State pattern, each state is an independent class.

2. **Guaranteed Invariant Safety**:
   Illegal transitions raise immediate runtime exceptions, preventing corrupt database writes.
