# 17. Mediator Pattern: Central Hub Decoupling N-to-N Chaos

[![Pattern: Behavioral](https://img.shields.io/badge/Pattern-Behavioral-f59e0b.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![Decoupling](https://img.shields.io/badge/Architecture-Decouples%20N--to--N-blue.svg?style=for-the-badge)]()

> **Intent**: Define an object that encapsulates how a set of objects interact. Mediator promotes loose coupling by keeping objects from referring to each other explicitly, and it lets you vary their interaction independently.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine an **Airport with 50 Airplanes** landing and taking off:
- If there is **no Air Traffic Controller**:
  - Every pilot has to radio all 49 other pilots simultaneously to ask: `"Are you landing on Runway 1? Can I take off? Where are you flying?"`
  - The radio channels turn into screaming chaos, and airplanes crash into each other ($N \times (N-1) / 2$ connections).
- With an **Air Traffic Control Tower (The Mediator)**:
  - Pilots do **not** talk to each other directly at all!
  - Pilot A radios the Tower: `"Tower, requesting landing on Runway 2."`
  - The Tower checks the airspace, coordinates timing, and gives clearance.

In software, when 20 UI components or 10 microservices need to exchange events, they don't hold direct references to each other. They talk only to a central **ChatHub / EventMediator**.

---

## 🖼️ Architectural Diagram

![Behavioral Design Patterns: Mediator Architecture](../../assets/images/design_patterns/behavioral_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

```python
"""
Mediator Pattern for a Real-Time Chat Room Hub in Python 3.12+.
Decouples peer-to-peer WebSocket connections into an atomic central routing hub.
"""

from __future__ import annotations
from dataclasses import dataclass
from typing import Dict, Protocol, runtime_checkable


@runtime_checkable
class ChatMediator(Protocol):
    """Mediator Interface."""
    def register_user(self, user: ChatUser) -> None: ...
    def send_direct_message(self, from_user: str, to_user: str, message: str) -> None: ...
    def broadcast(self, from_user: str, message: str) -> None: ...


class ChatUser:
    """Colleague Class: Interacts solely through the Mediator."""

    def __init__(self, user_id: str, display_name: str, mediator: ChatMediator) -> None:
        self.user_id = user_id
        self.display_name = display_name
        self._mediator = mediator
        self._mediator.register_user(self)

    def send_direct(self, to_user_id: str, message: str) -> None:
        print(f"\n[{self.display_name}] Sending direct message to {to_user_id}...")
        self._mediator.send_direct_message(self.user_id, to_user_id, message)

    def send_broadcast(self, message: str) -> None:
        print(f"\n[{self.display_name}] Broadcasting to room...")
        self._mediator.broadcast(self.user_id, message)

    def receive_message(self, sender_id: str, message: str) -> None:
        print(f"  📩 [{self.display_name}] Received from {sender_id}: '{message}'")


class WebSocketChatHub(ChatMediator):
    """Concrete Mediator managing connected users and dispatching events."""

    def __init__(self) -> None:
        self._users: Dict[str, ChatUser] = {}

    def register_user(self, user: ChatUser) -> None:
        self._users[user.user_id] = user
        print(f"[ChatHub] User '{user.display_name}' ({user.user_id}) joined the session.")

    def send_direct_message(self, from_user: str, to_user: str, message: str) -> None:
        recipient = self._users.get(to_user)
        if recipient:
            recipient.receive_message(from_user, message)
        else:
            print(f"[ChatHub] User '{to_user}' offline; queuing message in persistent store.")

    def broadcast(self, from_user: str, message: str) -> None:
        for uid, user in self._users.items():
            if uid != from_user:
                user.receive_message(from_user, message)


# =====================================================================
# 🧪 Execution & Verification
# =====================================================================
if __name__ == "__main__":
    hub = WebSocketChatHub()

    alice = ChatUser("usr_01", "Alice", hub)
    bob = ChatUser("usr_02", "Bob", hub)
    charlie = ChatUser("usr_03", "Charlie", hub)

    # Direct routed communication
    alice.send_direct("usr_02", "Hey Bob, do you have the deployment keys?")

    # Broadcast communication
    charlie.send_broadcast("Reminder: Production maintenance starts in 15 minutes!")
```

---

## 🔍 Step-by-Step Code Tutorial

1. **Decoupled Colleague Classes**:
   `ChatUser` has no reference to other `ChatUser` instances. Alice doesn't know Bob's memory address, IP, or internal data structures. She only communicates via `self._mediator`.

2. **Eliminating Mesh Complexity**:
   Without a Mediator, $N$ components require $O(N^2)$ direct bindings. With a Mediator, complexity drops to $O(N)$, because each component only registers with the central hub.
