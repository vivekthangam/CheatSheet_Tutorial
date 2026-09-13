# 01. Singleton Pattern: The Sovereign Single Instance

[![Pattern: Creational](https://img.shields.io/badge/Pattern-Creational-10b981.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![Thread Safe](https://img.shields.io/badge/Thread--Safe-Double--Checked%20Locking-blue.svg?style=for-the-badge)]()

> **Intent**: Ensure a class has only one instance, and provide a global point of access to it.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine the **President of a Country**. At any given moment, there is only **one** person occupying the Office of the President. 
- If citizen Alice asks to speak with the President, she is directed to the current occupant.
- If citizen Bob across the nation asks to speak with the President, he is directed to that exact same person.
- Nobody can create a second "new President" simply by calling an inauguration function; all government branches refer back to the exact same executive authority.

In software, your application needs a single sovereign coordinator for things like **configuration managers**, **database connection pools**, and **hardware telemetry drivers**. If 50 background threads create 50 separate config managers, they burn memory, desynchronize settings, and create catastrophic data corruption.

---

## 🖼️ Architectural Diagram

![Creational Design Patterns: Singleton Architecture](../../assets/images/design_patterns/creational_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

Here is the thread-safe, double-checked locked Metaclass implementation in Python 3.12+:

```python
"""
Thread-Safe Singleton Metaclass & Production Configuration Manager.
Compatible with Python 3.12+ and Python 3.13 free-threaded (no-GIL) runtimes.
"""

from __future__ import annotations
import threading
from typing import Any, ClassVar, Dict, Optional


class SingletonMeta(type):
    """
    A thread-safe implementation of Singleton using double-checked locking
    on a custom metaclass.
    """
    _instances: ClassVar[Dict[type, Any]] = {}
    _lock: ClassVar[threading.Lock] = threading.Lock()

    def __call__(cls, *args: Any, **kwargs: Any) -> Any:
        # First check (unlocked) for optimal read throughput
        if cls not in cls._instances:
            with cls._lock:
                # Second check (locked) to prevent race condition during cold init
                if cls not in cls._instances:
                    instance = super().__call__(*args, **kwargs)
                    cls._instances[cls] = instance
        return cls._instances[cls]


class AppConfig(metaclass=SingletonMeta):
    """
    Global Enterprise Configuration Manager.
    Guaranteed to have exactly one instance across all threads.
    """

    def __init__(self) -> None:
        # Prevent re-initialization if __init__ is called repeatedly
        if hasattr(self, "_initialized") and self._initialized:
            return
        
        self._settings: Dict[str, str] = {
            "database.url": "postgresql://admin:secret@db.internal:5432/primary",
            "redis.cluster": "redis-node-1.cache.internal:6379",
            "rate_limit.rpm": "1000",
            "log.level": "INFO",
            "timeout.seconds": "30",
        }
        self._rw_lock = threading.RLock()
        self._initialized: bool = True

    def get(self, key: str, default: Optional[str] = None) -> Optional[str]:
        """Thread-safe retrieval of configuration parameter."""
        with self._rw_lock:
            return self._settings.get(key, default)

    def set(self, key: str, value: str) -> None:
        """Thread-safe update of configuration parameter."""
        with self._rw_lock:
            self._settings[key] = value

    def get_all(self) -> Dict[str, str]:
        """Returns an immutable snapshot of all settings."""
        with self._rw_lock:
            return dict(self._settings)


# =====================================================================
# 🧪 Verification & Concurrent Execution Test
# =====================================================================
if __name__ == "__main__":
    def worker_task(thread_id: int) -> None:
        config = AppConfig()
        config.set(f"thread_{thread_id}.status", "active")
        print(f"[Thread-{thread_id}] Config Instance ID: {id(config)}")

    threads = [threading.Thread(target=worker_task, args=(i,)) for i in range(5)]
    for t in threads:
        t.start()
    for t in threads:
        t.join()

    # Verify both references point to the exact same memory address
    instance_a = AppConfig()
    instance_b = AppConfig()
    
    print("\n--- Singleton Verification ---")
    print(f"Instance A Memory Address : {hex(id(instance_a))}")
    print(f"Instance B Memory Address : {hex(id(instance_b))}")
    print(f"Are instances identical?  : {instance_a is instance_b}")
    assert instance_a is instance_b, "Singleton contract violated!"
```

---

## 🔍 Step-by-Step Code Tutorial

1. **Why a Metaclass?**:
   In Python, classes are themselves instances of metaclasses (`type`). When you write `AppConfig()`, Python invokes `SingletonMeta.__call__`. By intercepting this call, we can decide whether to construct a new object via `super().__call__` or return an existing cached instance from `_instances`.

2. **Double-Checked Locking**:
   - **First Check (`if cls not in cls._instances`)**: 99.99% of requests happen *after* initialization. Bypassing the lock on already-instantiated singletons avoids massive thread contention and latency spikes.
   - **The Lock (`with cls._lock`)**: Synchronizes concurrent worker threads if two threads attempt to create the instance simultaneously at boot.
   - **Second Check**: Confirms whether another thread already created the instance while the current thread was waiting to acquire the lock.

3. **Re-Initialization Trap (`__init__`)**:
   In standard Python, `__init__` is called *every time* `AppConfig()` is called, even if `__new__` returns the same instance! We guard against state overwrites with `if hasattr(self, "_initialized")`.

---

## 🐍 Python-Specifics & CPython Internals

### The "Pythonic" Alternative: Module-Level Singleton
In Python, **modules are singletons by design**. When you `import my_config`, Python executes the file once and caches it in `sys.modules`.
```python
# config.py
class _Config:
    def __init__(self):
        self.db_url = "postgres://..."

config = _Config() # Evaluated once on first import!
```
**When to use Metaclass vs Module**:
- Use **Module Singleton** for simple flat configurations.
- Use **Metaclass Singleton** when you need inheritance, dynamic runtime parameters, dependency injection mocking, or lazy deferred construction.

---

## ⚠️ Anti-Patterns & Testing Pitfalls

1. **Global State Mutation**: Avoid using singletons as an uncontrolled global bag of mutable variables. This introduces hidden dependencies across unrelated modules.
2. **Unit Testing Woes**: Because singletons persist across test cases, tests can bleed state into one another. Provide a test teardown method:
   ```python
   @classmethod
   def _reset(cls):
       with cls._lock:
           cls._instances.clear()
   ```
