# 13. Chain of Responsibility: Sequential Filter Pipeline

[![Pattern: Behavioral](https://img.shields.io/badge/Pattern-Behavioral-f59e0b.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![Zero Trust](https://img.shields.io/badge/Security-Zero%20Trust%20Pipeline-red.svg?style=for-the-badge)]()

> **Intent**: Avoid coupling the sender of a request to its receiver by giving more than one object a chance to handle the request. Chain the receiving objects and pass the request along the chain until an object handles it or rejects it.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine going through **Airport Security**:
- You don't get stamped and cleared by a single person doing 20 things simultaneously.
- Instead, you walk through a **Sequential Chain of Stations**:
  1. **Station 1 (Boarding Pass Verifier)**: Confirms your ticket is valid. If fake, you are turned away immediately.
  2. **Station 2 (Baggage X-Ray Scanner)**: Scans for prohibited liquids or metals. If detected, security stops you.
  3. **Station 3 (Metal Detector Body Scanner)**: Verifies you have nothing concealed.
  4. **Station 4 (Passport Control Gate)**: Scans biometrics and lets you through to the terminal.

Each security officer only knows their specific job, and points you to the **next** officer in line if you pass. If any check fails, the chain **halts immediately**.

In software, when an HTTP request arrives, we pass it down a chain: **JWT Authentication → IP Rate Limiter → XSS Payload Sanitizer → Business Logic**.

---

## 🖼️ Architectural Blueprint

![Behavioral Design Patterns: Chain of Responsibility Architecture](../../assets/images/design_patterns/behavioral_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

```python
"""
Chain of Responsibility Pattern for a Zero-Trust Security Pipeline in Python 3.12+.
Passes request objects down a chain of specialized security gate handlers.
"""

from __future__ import annotations
from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Optional, Self


@dataclass(frozen=True, slots=True)
class InboundRequest:
    token: Optional[str]
    source_ip: str
    body_json: str


@dataclass(frozen=True, slots=True)
class PipelineResult:
    allowed: bool
    status_code: int
    rejection_reason: Optional[str] = None


class SecurityHandler(ABC):
    """
    Abstract Handler defining the set_next() chaining method
    and the recursive process() execution flow.
    """

    def __init__(self) -> None:
        self._next_handler: Optional[SecurityHandler] = None

    def set_next(self, handler: SecurityHandler) -> SecurityHandler:
        """Chains handlers fluently and returns the newly added handler."""
        self._next_handler = handler
        return handler

    def process(self, request: InboundRequest) -> PipelineResult:
        # Evaluate current handler's specific check
        result = self.handle_check(request)
        if not result.allowed:
            # Short-circuit on failure
            return result
        
        # If there's a next handler, delegate down the chain
        if self._next_handler is not None:
            return self._next_handler.process(request)

        # Reached end of chain successfully
        return PipelineResult(allowed=True, status_code=200)

    @abstractmethod
    def handle_check(self, request: InboundRequest) -> PipelineResult:
        """Specific security check implemented by subclasses."""
        pass


class BearerTokenAuthHandler(SecurityHandler):
    """Station 1: Validates cryptographic bearer token."""

    def handle_check(self, request: InboundRequest) -> PipelineResult:
        print("[Security Stage 1] Evaluating Bearer Token...")
        if not request.token or not request.token.startswith("Bearer sec_live_"):
            return PipelineResult(allowed=False, status_code=401, rejection_reason="MISSING_OR_INVALID_TOKEN")
        return PipelineResult(allowed=True, status_code=200)


class IpRateLimitHandler(SecurityHandler):
    """Station 2: Evaluates IP against malicious blacklist subnets."""

    def handle_check(self, request: InboundRequest) -> PipelineResult:
        print(f"[Security Stage 2] Evaluating IP reputation for {request.source_ip}...")
        # Reject simulated malicious subnet
        if request.source_ip.startswith("198.51.100."):
            return PipelineResult(allowed=False, status_code=403, rejection_reason="IP_BLACKLISTED_BOTNET")
        return PipelineResult(allowed=True, status_code=200)


class PayloadSanitizationHandler(SecurityHandler):
    """Station 3: Scans for Cross-Site Scripting (XSS) / SQL Injection attack vectors."""

    def handle_check(self, request: InboundRequest) -> PipelineResult:
        print("[Security Stage 3] Inspecting payload body for script injections...")
        dangerous_tags = ["<script>", "javascript:", "DROP TABLE"]
        for tag in dangerous_tags:
            if tag.lower() in request.body_json.lower():
                return PipelineResult(allowed=False, status_code=400, rejection_reason="XSS_INJECTION_DETECTED")
        return PipelineResult(allowed=True, status_code=200)


# =====================================================================
# 🧪 Execution & Verification
# =====================================================================
if __name__ == "__main__":
    # Assemble the pipeline chain
    auth_handler = BearerTokenAuthHandler()
    rate_handler = IpRateLimitHandler()
    sanitize_handler = PayloadSanitizationHandler()

    # Fluent chain wiring: Auth -> Rate -> Sanitize
    auth_handler.set_next(rate_handler).set_next(sanitize_handler)

    print("--- Test Case 1: Valid Clean Request ---")
    valid_req = InboundRequest(
        token="Bearer sec_live_998124",
        source_ip="10.0.0.15",
        body_json='{"action": "update_email", "email": "alice@corp.io"}'
    )
    res1 = auth_handler.process(valid_req)
    print(f"Result: Allowed={res1.allowed}, Status={res1.status_code}\n")
    assert res1.allowed is True

    print("--- Test Case 2: XSS Injection Attack ---")
    attack_req = InboundRequest(
        token="Bearer sec_live_998124",
        source_ip="10.0.0.15",
        body_json='{"comment": "Hello <script>stealCookies()</script>"}'
    )
    res2 = auth_handler.process(attack_req)
    print(f"Result: Allowed={res2.allowed}, Status={res2.status_code}, Reason={res2.rejection_reason}\n")
    assert res2.allowed is False and res2.status_code == 400
```

---

## 🔍 Step-by-Step Code Tutorial

1. **Fluent Chaining with `set_next()`**:
   By having `set_next` return the argument handler, you can chain entire pipelines on one line:
   `chain = auth.set_next(rate_limiter).set_next(sanitizer).set_next(metrics)`

2. **Early Short-Circuiting**:
   If `auth_handler` fails, the pipeline returns immediately. It doesn't waste CPU evaluating IP addresses or scanning for XSS tags on an unauthenticated request.
