# 11. Flyweight Pattern: Massive Memory Optimization

[![Pattern: Structural](https://img.shields.io/badge/Pattern-Structural-6366f1.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![RAM Optimization](https://img.shields.io/badge/Memory-90%25%20RAM%20Reduction-brightgreen.svg?style=for-the-badge)]()

> **Intent**: Use sharing to support large numbers of fine-grained objects efficiently.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine you are developing a video game like **Minecraft** or an open-world forest simulator:
- Your virtual forest contains **1,000,000 Pine Trees**.
- Every single tree has:
  1. A complex 3D mesh polygon wireframe (50 Kilobytes).
  2. A bark and needle high-resolution texture map (200 Kilobytes).
  3. A 3D coordinate on the map: `(X, Y, Z)` (24 bytes).
- **The Naive Approach**: If you instantiate 1,000,000 separate `Tree` objects holding their own 3D mesh and texture, you need **250 Gigabytes of RAM**! Your game crashes immediately with `OutOfMemoryError`.
- **The Flyweight Solution**:
  - **Intrinsic State (Shared & Immutable)**: The 3D mesh and texture are identical for all pine trees. You store **ONE single instance** of `PineTreeModel` in RAM (250 KB).
  - **Extrinsic State (Unique & Mutable)**: The individual coordinates `(X, Y, Z)` change for each tree.
  - Each of your 1,000,000 trees simply stores `(X, Y, Z)` plus a **lightweight pointer** to the single shared `PineTreeModel`.

Your memory drops from **250 Gigabytes** down to **40 Megabytes**!

---

## 🖼️ Architectural Blueprint

![Structural Design Patterns: Flyweight Architecture](../../assets/images/design_patterns/structural_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

```python
"""
Flyweight Pattern in Python 3.12+.
Simulates rendering 500,000 particles with __slots__ memory optimization.
"""

from __future__ import annotations
from dataclasses import dataclass
import sys
import time
from typing import ClassVar, Dict


# ---------------------------------------------------------------------
# Intrinsic State (Immutable, Heavy, Shared)
# ---------------------------------------------------------------------
@dataclass(frozen=True, slots=True)
class ParticleType:
    """
    Stores heavy texture metadata, color definitions, and shader strings.
    Shared across thousands or millions of lightweight particles.
    """
    name: str
    color_hex: str
    texture_atlas: str
    shader_bytecode: bytes

    def render(self, x: float, y: float) -> None:
        # High-performance draw call referencing extrinsic coordinates
        pass


# ---------------------------------------------------------------------
# Flyweight Factory (Cache Manager)
# ---------------------------------------------------------------------
class ParticleTypeFactory:
    """Ensures intrinsic types are created once and reused globally."""
    _cache: ClassVar[Dict[str, ParticleType]] = {}

    @classmethod
    def get_type(cls, name: str, color_hex: str, texture: str) -> ParticleType:
        key = f"{name}:{color_hex}:{texture}"
        if key not in cls._cache:
            # Simulate loading a 20KB shader/mesh
            simulated_bytecode = b"\x7fELF" + (b"\x00" * 20_000)
            cls._cache[key] = ParticleType(
                name=name,
                color_hex=color_hex,
                texture_atlas=texture,
                shader_bytecode=simulated_bytecode,
            )
        return cls._cache[key]

    @classmethod
    def cache_size(cls) -> int:
        return len(cls._cache)


# ---------------------------------------------------------------------
# Extrinsic Context (Lightweight, Mutable, Unique per Entity)
# ---------------------------------------------------------------------
class Particle:
    """
    Extrinsic Context. Uses __slots__ to bypass Python's dynamic __dict__,
    reducing per-instance memory footprint from 152 bytes down to 48 bytes.
    """
    __slots__ = ("x", "y", "vx", "vy", "type_ref")

    def __init__(self, x: float, y: float, vx: float, vy: float, type_ref: ParticleType) -> None:
        self.x = x
        self.y = y
        self.vx = vx
        self.vy = vy
        self.type_ref = type_ref # Pointer to the shared Flyweight instance

    def step(self, delta_seconds: float) -> None:
        self.x += self.vx * delta_seconds
        self.y += self.vy * delta_seconds


# =====================================================================
# 🧪 Execution & Benchmark Demonstration
# =====================================================================
if __name__ == "__main__":
    NUM_PARTICLES = 250_000

    print(f"Spawning {NUM_PARTICLES:,} particles using Flyweight Pattern...")
    t0 = time.perf_counter()

    # Pre-fetch 3 shared intrinsic flyweights
    fire_type = ParticleTypeFactory.get_type("Fire", "#FF4500", "flame_atlas.png")
    smoke_type = ParticleTypeFactory.get_type("Smoke", "#708090", "smoke_atlas.png")
    spark_type = ParticleTypeFactory.get_type("Spark", "#FFFF00", "spark_atlas.png")

    particles = []
    for i in range(NUM_PARTICLES):
        chosen_type = fire_type if i % 3 == 0 else (smoke_type if i % 3 == 1 else spark_type)
        p = Particle(
            x=float(i % 1920),
            y=float(i % 1080),
            vx=0.5,
            vy=-1.2,
            type_ref=chosen_type # Reuses identical pointer!
        )
        particles.append(p)

    t1 = time.perf_counter()
    print(f"Created {NUM_PARTICLES:,} particles in {(t1 - t0):.3f} seconds.")
    print(f"Unique Intrinsic Flyweight Types in Memory: {ParticleTypeFactory.cache_size()}")

    # Memory comparison
    particle_sample_size = sys.getsizeof(particles[0])
    type_sample_size = sys.getsizeof(fire_type)
    print(f"Single Particle RAM Size (__slots__): {particle_sample_size} bytes")
    print(f"Single ParticleType RAM Size (Shared): {type_sample_size} bytes")

    # Step simulation
    for p in particles[:5]:
        p.step(0.016)
    print(f"Particle 0 updated position: x={particles[0].x:.2f}, y={particles[0].y:.2f}")
```

---

## 🔍 Step-by-Step Code Tutorial

1. **`__slots__` Mechanism in Python**:
   Standard Python objects store attributes in a dynamic dictionary (`__dict__`), which consumes ~152 bytes minimum per object. By declaring `__slots__ = ('x', 'y', 'vx', 'vy', 'type_ref')`, Python allocates a flat C-array of pointers, shrinking each instance to 48 bytes!

2. **Pointer Sharing**:
   Even though there are 250,000 particles, there are only **3 instances** of `ParticleType` in the entire operating system process. Every particle simply points to one of those three memory addresses.
