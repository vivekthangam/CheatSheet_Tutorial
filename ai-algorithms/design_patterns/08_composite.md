# 08. Composite Pattern: Recursive Tree Hierarchies

[![Pattern: Structural](https://img.shields.io/badge/Pattern-Structural-6366f1.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![Tree Traversal](https://img.shields.io/badge/Data%20Structure-Recursive%20Tree-emerald.svg?style=for-the-badge)]()

> **Intent**: Compose objects into tree structures to represent part-whole hierarchies. Composite lets clients treat individual objects and compositions of objects uniformly.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine your computer's **File System**:
- A folder called `Documents` contains a single file: `resume.pdf` (2 Megabytes).
- It also contains a nested sub-folder called `Photos`.
- The `Photos` folder contains `vacation1.jpg` (5 MB) and `vacation2.jpg` (3 MB).
- When you right-click on `resume.pdf` and check **"Get Size"**, it returns `2 MB`.
- When you right-click on `Documents` and check **"Get Size"**, the computer doesn't crash or require a completely different `GetFolderSize()` function! It treats the file and the folder **identically**:
  - A file returns its own byte size.
  - A folder recursively asks all its children (files AND subfolders) for their size, sums them up, and returns the total.

The client code doesn't care whether an item is a single leaf or a massive nested tree—both respond to the exact same `.get_size_bytes()` method!

---

## 🖼️ Architectural Blueprint

![Structural Design Patterns: Composite Architecture](../../assets/images/design_patterns/structural_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

```python
"""
Composite Pattern with Recursive Tree Aggregation in Python 3.12+.
Treats FileLeaf and DirectoryComposite uniformly with structural typing.
"""

from __future__ import annotations
from dataclasses import dataclass, field
from typing import List, Protocol, runtime_checkable


@runtime_checkable
class FileSystemNode(Protocol):
    """Component Interface: Uniform contract for leaves and branches."""
    name: str

    def get_size_bytes(self) -> int:
        """Calculates storage size in bytes recursively."""
        ...

    def display(self, indent: int = 0) -> str:
        """Visual tree renderer."""
        ...


@dataclass(frozen=True, slots=True)
class FileLeaf:
    """Leaf Object: Contains raw payload bytes and no child elements."""
    name: str
    size_bytes: int

    def get_size_bytes(self) -> int:
        return self.size_bytes

    def display(self, indent: int = 0) -> str:
        prefix = "  " * indent
        return f"{prefix}📄 {self.name} ({self.size_bytes:,.0f} bytes)\n"


@dataclass(slots=True)
class DirectoryComposite:
    """Composite Object: Manages children and delegates operations recursively."""
    name: str
    _children: List[FileSystemNode] = field(default_factory=list)

    def add(self, node: FileSystemNode) -> None:
        self._children.append(node)

    def remove(self, node: FileSystemNode) -> None:
        self._children.remove(node)

    def get_size_bytes(self) -> int:
        # Uniform recursive calculation
        return sum(child.get_size_bytes() for child in self._children)

    def display(self, indent: int = 0) -> str:
        prefix = "  " * indent
        output = f"{prefix}📁 [{self.name}] - Total: {self.get_size_bytes():,.0f} bytes\n"
        for child in self._children:
            output += child.display(indent + 1)
        return output


# =====================================================================
# 🧪 Execution & Demonstration
# =====================================================================
if __name__ == "__main__":
    # Construct leaf files
    resume = FileLeaf("resume_2026.pdf", 2_450_000)
    avatar = FileLeaf("avatar_profile.png", 1_120_000)
    config = FileLeaf("settings.yaml", 4_500)
    docker = FileLeaf("Dockerfile", 1_200)

    # Build sub-directory
    src_dir = DirectoryComposite("src")
    src_dir.add(FileLeaf("main.py", 18_400))
    src_dir.add(FileLeaf("utils.py", 9_200))

    # Build root directory
    root_project = DirectoryComposite("ProjectRoot")
    root_project.add(resume)
    root_project.add(avatar)
    root_project.add(config)
    root_project.add(docker)
    root_project.add(src_dir) # Nesting directory inside directory

    # Client treats leaves and root identically
    print("--- Directory Tree Rendering ---")
    print(root_project.display())

    total_bytes = root_project.get_size_bytes()
    print(f"Overall Calculated Size: {total_bytes / (1024 * 1024):.2f} MB")
    assert total_bytes == 2_450_000 + 1_120_000 + 4_500 + 1_200 + 18_400 + 9_200
```

---

## 🔍 Step-by-Step Code Tutorial

1. **Uniform Polymorphism**:
   Notice that `DirectoryComposite.get_size_bytes()` executes:
   `sum(child.get_size_bytes() for child in self._children)`
   It does **not** check `isinstance(child, FileLeaf)` or `isinstance(child, DirectoryComposite)`. Because both implement `FileSystemNode`, recursion happens automatically.

2. **Infinite Depth**:
   Directories can contain directories, which in turn contain directories. The call stack handles recursion cleanly down to the leaf files.
