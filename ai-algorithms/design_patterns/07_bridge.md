# 07. Bridge Pattern: Decoupling Abstraction from Implementation

[![Pattern: Structural](https://img.shields.io/badge/Pattern-Structural-6366f1.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![Class Explosion Prevention](https://img.shields.io/badge/Architecture-Prevents%20M%20x%20N-orange.svg?style=for-the-badge)]()

> **Intent**: Decouple an abstraction from its implementation so that the two can vary independently.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine you want to buy a **Universal TV Remote Control**:
- You have two types of remotes: **Basic Remote** and **Advanced Voice Remote**.
- You have three brands of TVs: **Sony**, **Samsung**, and **LG**.
- If you don't use the Bridge pattern, you would need to manufacture **6 separate remote models**:
  `BasicSonyRemote`, `BasicSamsungRemote`, `BasicLGRemote`, `VoiceSonyRemote`, `VoiceSamsungRemote`, `VoiceLGRemote`.
  If you add 5 more TV brands, your classes explode to **16**! (Cartesian product: $M \times N$).
- Instead, with the **Bridge Pattern**:
  - The **Remote Control** is the **Abstraction** (`BasicRemote`, `VoiceRemote`).
  - The **TV Hardware** is the **Implementation** (`SonyDevice`, `SamsungDevice`, `LgDevice`).
  - The Remote holds a reference (the **Bridge**) to an `EntertainmentDevice` interface.

Now you have only $2 + 3 = 5$ classes instead of $2 \times 3 = 6$. Adding 10 new TV brands requires only 10 new implementations without touching the Remote classes!

---

## 🖼️ Architectural Diagram

![Structural Design Patterns: Bridge Architecture](../../assets/images/design_patterns/structural_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

```python
"""
Bridge Pattern in Python 3.12+.
Decouples Notification business priority from transport communication channels.
"""

from __future__ import annotations
from abc import ABC, abstractmethod
from typing import Protocol, runtime_checkable


# ---------------------------------------------------------------------
# Implementor: Communication Channel Protocol
# ---------------------------------------------------------------------
@runtime_checkable
class MessageChannel(Protocol):
    def send_raw_message(self, recipient: str, message_payload: str) -> None:
        """Physical transport implementation."""
        ...


# Concrete Implementors
class TwilioSmsChannel:
    def send_raw_message(self, recipient: str, message_payload: str) -> None:
        print(f"[Twilio SMS API] To: {recipient} | Payload: {message_payload}")


class SendGridEmailChannel:
    def send_raw_message(self, recipient: str, message_payload: str) -> None:
        print(f"[SendGrid Email API] Dispatching HTML email to <{recipient}>: {message_payload}")


class SlackAlertChannel:
    def send_raw_message(self, recipient: str, message_payload: str) -> None:
        print(f"[Slack Incoming Webhook] Posting to channel #{recipient}: {message_payload}")


# ---------------------------------------------------------------------
# Abstraction: Notification Hierarchy
# ---------------------------------------------------------------------
class Notification(ABC):
    """
    Base Abstraction. Holds a reference to the Bridge implementor.
    """

    def __init__(self, channel: MessageChannel) -> None:
        self._channel = channel

    @abstractmethod
    def notify(self, target: str, subject: str, message: str) -> None:
        pass


class StandardNotification(Notification):
    """Refined Abstraction 1: Standard low-priority alert."""

    def notify(self, target: str, subject: str, message: str) -> None:
        body = f"[INFO] {subject} - {message}"
        self._channel.send_raw_message(target, body)


class UrgentNotification(Notification):
    """Refined Abstraction 2: High-priority SEV-1 alert with escalation."""

    def notify(self, target: str, subject: str, message: str) -> None:
        # Business logic: Prepends emergency headers and triggers audit log
        body = f"🚨 [CRITICAL ALERT] 🚨 {subject.upper()} - {message} (ACK REQUIRED)"
        print("[Security Audit] Urgent alert dispatched; escalating to on-call paging.")
        self._channel.send_raw_message(target, body)


# =====================================================================
# 🧪 Execution & Demonstration
# =====================================================================
if __name__ == "__main__":
    sms_bridge = TwilioSmsChannel()
    email_bridge = SendGridEmailChannel()
    slack_bridge = SlackAlertChannel()

    # Mix and match abstractions with channels dynamically at runtime!
    daily_summary = StandardNotification(email_bridge)
    daily_summary.notify("finance-team@enterprise.io", "Daily Revenue", "MRR reached $120,000 today.")

    server_crash = UrgentNotification(sms_bridge)
    server_crash.notify("+1-555-0199", "DB Partition Fail", "Postgres Primary unreachable in us-east-1.")

    war_room_alert = UrgentNotification(slack_bridge)
    war_room_alert.notify("war-room-outages", "Throttling Threshold Hit", "Redis cluster CPU at 98%.")
```

---

## 🔍 Step-by-Step Code Tutorial

1. **The Bridge Composition**:
   `Notification` contains `self._channel = channel`. This is the "Bridge". Instead of creating `UrgentSmsNotification` or `StandardEmailNotification`, the priority logic and channel logic are completely orthogonal.

2. **Linear vs Combinatorial Growth**:
   - Without Bridge: $N$ priorities $\times$ $M$ channels $= N \times M$ classes.
   - With Bridge: $N$ priorities $+ M$ channels $= N + M$ classes.
   If you have 5 notification levels and 8 channels, Bridge saves you from writing 40 classes, reducing it to just 13.
