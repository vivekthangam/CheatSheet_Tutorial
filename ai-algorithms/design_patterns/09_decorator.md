# 09. Decorator Pattern: Dynamic Layered Wrapping

[![Pattern: Structural](https://img.shields.io/badge/Pattern-Structural-6366f1.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![Dynamic Behavior](https://img.shields.io/badge/Behavior-Dynamic%20Wrapping-cyan.svg?style=for-the-badge)]()

> **Intent**: Attach additional responsibilities to an object dynamically. Decorators provide a flexible alternative to subclassing for extending functionality.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine you are dressing for cold winter weather:
- You put on your base **Cotton T-Shirt**.
- If it starts getting chilly, you put on a warm **Fleece Sweater** over your t-shirt.
- If it starts raining, you put on a **Waterproof Raincoat** over your sweater.
- If it's night, you clip a **Reflective LED Badge** onto your raincoat.

Each layer wraps the inner layer. You are still the same person, but you have dynamically acquired new capabilities (warmth, waterproof protection, high visibility). You did **not** surgically fuse a raincoat to your skin or create an impossible genetic hybrid subclass like `WaterproofReflectiveHuman`!

In software, instead of creating rigid subclasses like `EncryptedCompressedFileStream`, you dynamically wrap an input stream with a `CompressionDecorator` and then an `EncryptionDecorator`.

---

## 🖼️ Architectural Blueprint

![Structural Design Patterns: Decorator Architecture](../../assets/images/design_patterns/structural_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

### Part 1: The Classic GoF Object Decorator (Composable Streams)

```python
"""
GoF Object Decorator Pattern in Python 3.12+.
Composable cryptographic, compression, and encoding pipeline layers.
"""

from __future__ import annotations
import base64
import hashlib
import hmac
from typing import Protocol, runtime_checkable


@runtime_checkable
class DataStream(Protocol):
    """Component Interface defining stream IO."""
    def write(self, data: str) -> None: ...
    def read(self) -> str: ...


class MemoryDataStream:
    """Concrete Component: Raw in-memory storage buffer."""

    def __init__(self) -> None:
        self._buffer: str = ""

    def write(self, data: str) -> None:
        self._buffer = data

    def read(self) -> str:
        return self._buffer


class StreamDecorator(DataStream):
    """Base Decorator delegating all operations to wrapped stream."""

    def __init__(self, wrappee: DataStream) -> None:
        self._wrappee = wrappee

    def write(self, data: str) -> None:
        self._wrappee.write(data)

    def read(self) -> str:
        return self._wrappee.read()


class Base64EncodingDecorator(StreamDecorator):
    """Concrete Decorator 1: Encodes and decodes Base64 payloads."""

    def write(self, data: str) -> None:
        encoded = base64.b64encode(data.encode("utf-8")).decode("ascii")
        super().write(encoded)

    def read(self) -> str:
        raw_encoded = super().read()
        return base64.b64decode(raw_encoded.encode("ascii")).decode("utf-8")


class HmacSigningDecorator(StreamDecorator):
    """Concrete Decorator 2: Prepends and validates cryptographic HMAC signatures."""

    def __init__(self, wrappee: DataStream, secret_key: str) -> None:
        super().__init__(wrappee)
        self._key = secret_key.encode("utf-8")

    def _compute_hmac(self, content: str) -> str:
        return hmac.new(self._key, content.encode("utf-8"), hashlib.sha256).hexdigest()

    def write(self, data: str) -> None:
        signature = self._compute_hmac(data)
        signed_payload = f"SIG:{signature}::{data}"
        super().write(signed_payload)

    def read(self) -> str:
        signed_payload = super().read()
        if not signed_payload.startswith("SIG:"):
            raise ValueError("Integrity Violation: Payload lacks cryptographic signature header.")
        
        parts = signed_payload.split("::", 1)
        sig_received = parts[0].replace("SIG:", "")
        content = parts[1]
        
        # Constant-time comparison preventing timing attacks
        expected_sig = self._compute_hmac(content)
        if not hmac.compare_digest(sig_received, expected_sig):
            raise PermissionError("Security Alert: Data tampering detected! Invalid HMAC signature.")
        return content


# =====================================================================
# 🧪 Execution & Verification
# =====================================================================
if __name__ == "__main__":
    raw_storage = MemoryDataStream()

    # Composing 2 dynamic layers: Base64 -> HMAC Signature
    pipeline = HmacSigningDecorator(
        wrappee=Base64EncodingDecorator(raw_storage),
        secret_key="enterprise_secret_vault_key"
    )

    sensitive_payload = '{"account": "4491-0021-9988", "transfer": 50000.00}'
    print(f"Original Payload: {sensitive_payload}")

    # Write through pipeline
    pipeline.write(sensitive_payload)
    print(f"\nRaw Storage In-Memory Buffer: {raw_storage.read()}")

    # Read and verify through pipeline
    recovered = pipeline.read()
    print(f"\nRecovered Verified Payload: {recovered}")
    assert recovered == sensitive_payload, "Payload corrupted during round-trip!"
```

---

## 🐍 Pythonic Functional `@decorator` Syntax

In Python, the word "decorator" also refers to language syntax using `@`. Here is the modern typed production pattern for function decorators using `ParamSpec` and `TypeVar`:

```python
from functools import wraps
import time
from typing import Callable, ParamSpec, TypeVar

P = ParamSpec("P")
R = TypeVar("R")

def audit_timer(metric_name: str) -> Callable[[Callable[P, R]], Callable[P, R]]:
    """Production latency timing decorator preserving exact function signatures."""
    def decorator(func: Callable[P, R]) -> Callable[P, R]:
        @wraps(func)
        def wrapper(*args: P.args, **kwargs: P.kwargs) -> R:
            t0 = time.perf_counter()
            try:
                return func(*args, **kwargs)
            finally:
                elapsed_ms = (time.perf_counter() - t0) * 1000
                print(f"[METRICS] {metric_name} executed in {elapsed_ms:.2f} ms")
        return wrapper
    return decorator

@audit_timer("user_checkout_latency")
def process_order(order_id: str, amount: float) -> bool:
    time.sleep(0.05)
    return True
```
