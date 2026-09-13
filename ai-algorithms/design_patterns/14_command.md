# 14. Command Pattern: Transactional Sagas & Reversible Actions

[![Pattern: Behavioral](https://img.shields.io/badge/Pattern-Behavioral-f59e0b.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![Saga Transactions](https://img.shields.io/badge/Distributed-Saga%20Orchestrator-blue.svg?style=for-the-badge)]()

> **Intent**: Encapsulate a request as an object, thereby letting you parameterize clients with different requests, queue or log requests, and support undoable operations.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine eating at a **Diner Restaurant**:
- You tell the waiter: `"I want a cheeseburger, fries, and a vanilla milkshake."`
- The waiter does **not** sprint into the kitchen, yell at 3 different line cooks, and hold your hand while they fry beef.
- Instead, the waiter writes your request onto a **Physical Paper Order Ticket (The Command Object)**.
- The ticket contains all instructions, customer table number, and timestamps.
- The waiter pins the ticket to the kitchen order carousel (the **Queue**).
- If you suddenly realize you forgot your wallet, the manager takes the ticket, writes **VOID (Compensate/Undo)** across it, and tells the cooks to put the ingredients back.

In software, turning an action into an object allows us to **queue requests**, **delay execution**, **retry on failure**, and most importantly, **execute compensating transactions (Undo / Saga Pattern)** if something fails in a multi-step distributed operation.

---

## 🖼️ Architectural Blueprint

![Behavioral Design Patterns: Command Architecture](../../assets/images/design_patterns/behavioral_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

```python
"""
Command Pattern with Compensating Transactions (Distributed Saga Foundation) in Python 3.12+.
Provides reversible execution with an explicit LIFO undo stack.
"""

from __future__ import annotations
from dataclasses import dataclass
from decimal import Decimal
from typing import Deque, List, Protocol, runtime_checkable
from collections import deque


@runtime_checkable
class TransactionCommand(Protocol):
    """Command Protocol with forward execution and backward compensation."""

    def execute(self) -> bool:
        """Executes forward state mutation. Returns True if successful."""
        ...

    def compensate(self) -> None:
        """Reverses mutation if downstream operations fail."""
        ...


# ---------------------------------------------------------------------
# Concrete Command 1: Inventory Reservation
# ---------------------------------------------------------------------
@dataclass
class ReserveInventoryCommand:
    sku: str
    quantity: int

    def execute(self) -> bool:
        print(f"[Inventory] Reserved {self.quantity} units of SKU '{self.sku}'.")
        return True

    def compensate(self) -> None:
        print(f"[Inventory COMPENSATE] Restocked {self.quantity} units of SKU '{self.sku}'.")


# ---------------------------------------------------------------------
# Concrete Command 2: Financial Account Debit
# ---------------------------------------------------------------------
@dataclass
class DebitAccountCommand:
    account_id: str
    amount: Decimal

    def execute(self) -> bool:
        print(f"[Ledger] Debited ${self.amount:,.2f} from account '{self.account_id}'.")
        return True

    def compensate(self) -> None:
        print(f"[Ledger COMPENSATE] Refunded ${self.amount:,.2f} to account '{self.account_id}'.")


# ---------------------------------------------------------------------
# Concrete Command 3: External Shipping Courier Booking (Fails)
# ---------------------------------------------------------------------
@dataclass
class BookCourierCommand:
    pickup_address: str
    fail_simulation: bool = True

    def execute(self) -> bool:
        print(f"[Courier] Attempting dispatch to '{self.pickup_address}'...")
        if self.fail_simulation:
            print("[Courier ERROR] Courier API timed out! Out of drivers.")
            return False
        return True

    def compensate(self) -> None:
        print("[Courier COMPENSATE] Cancelled courier booking ticket.")


# ---------------------------------------------------------------------
# Invoker / Orchestrator: The Saga Engine
# ---------------------------------------------------------------------
class SagaTransactionOrchestrator:
    """
    Invoker managing sequential execution and automatic backwards rollback.
    """

    def __init__(self) -> None:
        self._history: Deque[TransactionCommand] = deque()

    def run_saga(self, *commands: TransactionCommand) -> bool:
        print("\n--- Starting Distributed Transaction Saga ---")
        self._history.clear()

        for idx, cmd in enumerate(commands, 1):
            success = cmd.execute()
            if success:
                self._history.append(cmd)
            else:
                print(f"\n🚨 Step {idx} failed! Initiating Compensating Rollback...")
                self._rollback()
                return False

        print("✅ All Saga steps executed successfully!")
        return True

    def _rollback(self) -> None:
        """Pops commands LIFO (Last In, First Out) and executes undo actions."""
        while self._history:
            cmd = self._history.pop()
            try:
                cmd.compensate()
            except Exception as err:
                print(f"[CRITICAL] Compensation failed on {type(cmd).__name__}: {err}")


# =====================================================================
# 🧪 Execution & Demonstration
# =====================================================================
if __name__ == "__main__":
    orchestrator = SagaTransactionOrchestrator()

    cmd_inventory = ReserveInventoryCommand(sku="SKU-MACBOOK-PRO", quantity=1)
    cmd_payment = DebitAccountCommand(account_id="ACC-CORP-4401", amount=Decimal("2499.00"))
    cmd_courier = BookCourierCommand(pickup_address="450 Silicon Way", fail_simulation=True)

    # Run saga with simulated failure on step 3
    is_completed = orchestrator.run_saga(cmd_inventory, cmd_payment, cmd_courier)
    assert is_completed is False, "Saga should have aborted!"
    print("\nVerified: All mutations compensated cleanly. No orphan charges or lost stock.")
```

---

## 🔍 Step-by-Step Code Tutorial

1. **First-Class Actions**:
   Notice that each action (`ReserveInventoryCommand`, `DebitAccountCommand`) is a fully parameterized object with its own state. It can be serialized to JSON, stored in Redis, or placed into a Celery task queue.

2. **LIFO Compensation Stack**:
   By pushing succeeded commands onto a `collections.deque` stack, rolling back simply requires popping each command in reverse order and calling `.compensate()`. This is the fundamental building block of **Saga Orchestrators in Microservices**.
