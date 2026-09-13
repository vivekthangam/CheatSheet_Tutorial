# 05. Prototype Pattern: Deep State Cloning

[![Pattern: Creational](https://img.shields.io/badge/Pattern-Creational-10b981.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![In-Memory Cloning](https://img.shields.io/badge/Cloning-Zero%20IO%20Cost-blue.svg?style=for-the-badge)]()

> **Intent**: Specify the kinds of objects to create using a prototypical instance, and create new objects by copying this prototype without coupling to concrete classes.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine you work in a 3D animation studio designing an army of **10,000 Medieval Knights**:
- Each knight has a detailed 3D skeleton, armor texture, weapon inventory, facial geometry, and physics colliders.
- Calculating and initializing a single knight from raw files takes **3 full seconds** of CPU time.
- If you run the initialization 10,000 times from scratch, your computer will freeze for **8.3 hours**!
- Instead, you load and initialize **ONE "Gold Master" Prototype Knight** into memory once.
- Then, you perform an instantaneous in-memory **Deep Clone (Cell Mitosis)** in 2 milliseconds, simply overriding the position, armor color, and character name.

The **Prototype Pattern** allows you to clone complex, pre-computed object trees in RAM without touching databases or repeating expensive cold-boot initializations.

---

## 🖼️ Architectural Diagram

![Creational Design Patterns: Prototype Architecture](../../assets/images/design_patterns/creational_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

```python
"""
Prototype Pattern with Custom Deep-Cloning Mechanics in Python 3.12+.
Supports safe mutation overrides and avoids expensive cold initialization.
"""

from __future__ import annotations
import copy
from dataclasses import dataclass, field
from datetime import datetime, timezone
import time
from typing import Any, Dict, List, Optional, Protocol, Self


class Cloneable(Protocol):
    def clone(self, **overrides: Any) -> Self: ...


@dataclass
class NetworkNode:
    node_id: str
    ip_address: str
    allocated_ram_mb: int


@dataclass
class VirtualMachineSnapshot:
    """
    Complex enterprise object tree with nested mutable structures.
    Initializes slowly due to simulated OS boot & disk indexing.
    """
    snapshot_id: str
    os_kernel: str
    nodes: List[NetworkNode]
    env_vars: Dict[str, str]
    created_at: datetime = field(default_factory=lambda: datetime.now(timezone.utc))

    @classmethod
    def cold_boot(cls, kernel: str) -> VirtualMachineSnapshot:
        """Simulates expensive 2-second setup process."""
        print(f"[Cold Boot] Initializing OS kernel '{kernel}', indexing virtual disks...")
        time.sleep(0.5) # Simulated latency
        return cls(
            snapshot_id="gold-image-base",
            os_kernel=kernel,
            nodes=[
                NetworkNode("node-01", "10.0.0.1", 16384),
                NetworkNode("node-02", "10.0.0.2", 16384),
            ],
            env_vars={"ENV": "STAGING", "SECURITY_POLICY": "STRICT_SELINUX"},
        )

    def clone(self, **overrides: Any) -> Self:
        """
        Deeply clones the instance and applies requested property overrides.
        Guarantees that child objects (nodes, env_vars) are not shared.
        """
        cloned_instance = copy.deepcopy(self)
        for key, value in overrides.items():
            if not hasattr(cloned_instance, key):
                raise AttributeError(f"Invalid override attribute: {key}")
            setattr(cloned_instance, key, value)
        return cloned_instance


class PrototypeRegistry:
    """
    Centralized store of pre-initialized gold prototypes.
    """

    def __init__(self) -> None:
        self._prototypes: Dict[str, VirtualMachineSnapshot] = {}

    def register(self, key: str, prototype: VirtualMachineSnapshot) -> None:
        self._prototypes[key] = prototype

    def get_clone(self, key: str, **overrides: Any) -> VirtualMachineSnapshot:
        if key not in self._prototypes:
            raise KeyError(f"No prototype registered under key: {key}")
        return self._prototypes[key].clone(**overrides)


# =====================================================================
# 🧪 Execution & Verification
# =====================================================================
if __name__ == "__main__":
    registry = PrototypeRegistry()

    # 1. Perform ONE expensive cold boot
    gold_ubuntu = VirtualMachineSnapshot.cold_boot("Linux 6.8.0-enterprise")
    registry.register("ubuntu_prod", gold_ubuntu)

    # 2. Instantaneous in-memory cloning
    t0 = time.perf_counter()
    vm_client_a = registry.get_clone(
        "ubuntu_prod",
        snapshot_id="vm-tenant-alpha-001",
        env_vars={"TENANT": "ALPHA", "ENV": "PRODUCTION"}
    )
    t1 = time.perf_counter()

    vm_client_b = registry.get_clone(
        "ubuntu_prod",
        snapshot_id="vm-tenant-beta-002",
        env_vars={"TENANT": "BETA", "ENV": "TEST"}
    )

    print(f"\n[Clone Speed] VM Cloned in {(t1 - t0) * 1000:.3f} ms (vs 500ms cold boot)!")
    
    # 3. Verify deep independence
    vm_client_a.nodes[0].ip_address = "192.168.100.5"
    
    print("\n--- Deep Independence Test ---")
    print(f"VM Client A (Node 0 IP): {vm_client_a.nodes[0].ip_address}")
    print(f"VM Client B (Node 0 IP): {vm_client_b.nodes[0].ip_address}")
    print(f"Gold Master (Node 0 IP): {gold_ubuntu.nodes[0].ip_address}")

    assert vm_client_a.nodes[0].ip_address != vm_client_b.nodes[0].ip_address, "Deep copy failed: Memory leaked between clones!"
    print("Assertion Passed: Clones are 100% memory-isolated.")
```

---

## 🔍 Step-by-Step Code Tutorial

1. **`copy.deepcopy` vs Shallow `copy.copy`**:
   If we used `copy.copy(self)`, the `nodes` list would be shallow-copied. If `vm_client_a` modified its first node's IP address, `vm_client_b`'s IP address would also mutate! `copy.deepcopy` recursively duplicates every nested pointer in memory.

2. **Custom `__deepcopy__` Optimization**:
   For objects containing gigabytes of read-only lookup matrices, a naive `copy.deepcopy` can be slow. You can override `__deepcopy__(self, memo)` to deep-copy mutable fields while shallow-copying frozen immutable arrays.
