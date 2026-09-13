# 04. Builder Pattern: Fluent Complex Object Assembly

[![Pattern: Creational](https://img.shields.io/badge/Pattern-Creational-10b981.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![Fluent API](https://img.shields.io/badge/API-Fluent%20Chaining-orange.svg?style=for-the-badge)]()

> **Intent**: Separate the construction of a complex object from its representation so that the same construction process can create different representations.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine ordering a sandwich at **Subway**:
- The sandwich artist doesn't throw 25 ingredients onto the counter all at once in random order.
- Instead, you build it **step-by-step fluently**:
  1. `.choose_bread("Italian Herbs & Cheese")`
  2. `.add_protein("Smoked Turkey")`
  3. `.add_cheese("Provolone")`
  4. `.toast(True)`
  5. `.add_veggies(["Lettuce", "Tomatoes", "Olives"])`
  6. `.add_sauce("Chipotle Southwest")`
  7. `.build()`
- If you don't want sauce, you simply skip step 6. You don't pass `sauce=None, sauce2=None, sauce3=None, cheese2=None` into a monster 30-parameter constructor!

In enterprise engineering, constructing complex objects (like HTTP Requests, SQL queries, or ML pipeline manifests) through a telescoping constructor with 15 arguments is error-prone. The **Builder Pattern** gives you readable, fluent method chaining and validates the entire object atomically upon `.build()`.

---

## 🖼️ Architectural Diagram

![Creational Design Patterns: Builder Architecture](../../assets/images/design_patterns/creational_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

```python
"""
Fluent Builder Pattern with Immutable Target Product in Python 3.12+.
Uses typing.Self, dataclasses(frozen=True), and atomic validation on build.
"""

from __future__ import annotations
from dataclasses import dataclass, field
from typing import Dict, Optional, Self
from urllib.parse import urlparse


@dataclass(frozen=True, slots=True)
class HttpRequest:
    """
    Immutable Target Product.
    Once constructed by the Builder, fields cannot be modified.
    """
    uri: str
    method: str
    headers: Dict[str, str] = field(default_factory=dict)
    body: str = ""
    timeout_seconds: float = 30.0

    @classmethod
    def builder(cls, uri: str) -> _HttpRequestBuilder:
        """Entry point to the fluent builder."""
        return _HttpRequestBuilder(uri)


class _HttpRequestBuilder:
    """
    Mutable staging builder with fluent chaining.
    """

    def __init__(self, uri: str) -> None:
        if not uri or not uri.strip():
            raise ValueError("URI must not be empty.")
        self._uri = uri
        self._method: str = "GET"
        self._headers: Dict[str, str] = {}
        self._body: str = ""
        self._timeout_seconds: float = 10.0

    def method(self, method: str) -> Self:
        """Sets HTTP verb (GET, POST, PUT, DELETE, PATCH)."""
        valid_methods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD"}
        norm = method.upper().strip()
        if norm not in valid_methods:
            raise ValueError(f"Invalid HTTP Method: {method}. Must be one of {valid_methods}")
        self._method = norm
        return self

    def header(self, name: str, value: str) -> Self:
        """Fluent setter for HTTP header."""
        self._headers[name.strip()] = value.strip()
        return self

    def headers(self, headers_dict: Dict[str, str]) -> Self:
        """Bulk setter for headers."""
        for k, v in headers_dict.items():
            self.header(k, v)
        return self

    def body(self, body_payload: str) -> Self:
        """Sets request payload body."""
        self._body = body_payload
        return self

    def timeout(self, seconds: float) -> Self:
        """Sets network socket timeout in seconds."""
        if seconds <= 0:
            raise ValueError("Timeout must be strictly positive.")
        self._timeout_seconds = seconds
        return self

    def build(self) -> HttpRequest:
        """
        Validates all accumulated parameters and instantiates
        the final immutable HttpRequest object.
        """
        parsed = urlparse(self._uri)
        if not parsed.scheme or not parsed.netloc:
            raise ValueError(f"Malformed URI: '{self._uri}'. Scheme and domain required.")

        # Business rule: Body forbidden on GET / HEAD requests
        if self._method in {"GET", "HEAD"} and self._body:
            raise ValueError(f"HTTP {self._method} requests cannot contain a message body.")

        return HttpRequest(
            uri=self._uri,
            method=self._method,
            headers=dict(self._headers), # Defensively shallow copied
            body=self._body,
            timeout_seconds=self._timeout_seconds,
        )


# =====================================================================
# 🧪 Execution & Demonstration
# =====================================================================
if __name__ == "__main__":
    # Fluent construction
    request = (
        HttpRequest.builder("https://api.enterprise.io/v2/settlement")
        .method("POST")
        .header("Authorization", "Bearer eyJhbGciOi...")
        .header("Content-Type", "application/json")
        .header("X-Idempotency-Key", "8931-4123-uuid")
        .body('{"transfer_amount_cents": 500000, "currency": "USD"}')
        .timeout(5.0)
        .build()
    )

    print("--- Successfully Built HttpRequest ---")
    print(f"URI     : {request.uri}")
    print(f"Method  : {request.method}")
    print(f"Headers : {request.headers}")
    print(f"Body    : {request.body}")
    print(f"Timeout : {request.timeout_seconds}s")

    # Verify Immutability
    try:
        request.method = "PUT" # type: ignore
    except Exception as e:
        print(f"\n[Immutability Test Passed] Caught expected mutation error: {type(e).__name__}")
```

---

## 🔍 Step-by-Step Code Tutorial

1. **`typing.Self` in Python 3.11+**:
   By typing return values as `Self`, modern IDEs and `mypy` understand that every method chain (`.method().header().body()`) returns the exact instance of `_HttpRequestBuilder`, preserving autocompletion.

2. **Decoupling Validation from Construction**:
   The validation logic (such as checking if a `GET` request illegally contains a body) is encapsulated in `.build()`. Clients cannot construct an invalid or half-initialized `HttpRequest`.

3. **Immutability via `dataclasses(frozen=True)`**:
   Once built, the `HttpRequest` is read-only. Multiple asynchronous coroutines or threads can read its headers without race conditions or memory corruption.
