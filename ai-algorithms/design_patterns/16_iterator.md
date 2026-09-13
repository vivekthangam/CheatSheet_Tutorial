# 16. Iterator Pattern: Lazy Traversal & Memory-Efficient Streaming

[![Pattern: Behavioral](https://img.shields.io/badge/Pattern-Behavioral-f59e0b.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![Memory Efficient](https://img.shields.io/badge/Memory-O(1)%20RAM%20Streaming-brightgreen.svg?style=for-the-badge)]()

> **Intent**: Provide a way to access the elements of an aggregate object sequentially without exposing its underlying representation.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine you are eating from a **Pez Candy Dispenser**:
- Inside the dispenser, 12 candies are stacked on a spring.
- You don't rip open the plastic casing with a hammer to look at all 12 candies at once.
- Instead, you pull back the dispenser head, and **exactly ONE candy pops out**.
- You eat it, pull the head back again, and the next candy pops out.
- When the spring reaches the top and has no more candies, it stops dispensing (`StopIteration`).

In software, if your database has **10,000,000 user records**, doing `records = db.query_all()` loads a 15 Gigabyte JSON array into memory, immediately crashing your server. The **Iterator Pattern** lets you traverse records **one page or one row at a time lazily**, maintaining a constant **O(1) memory footprint**.

---

## 🖼️ Architectural Blueprint

![Behavioral Design Patterns: Iterator Architecture](../../assets/images/design_patterns/behavioral_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

```python
"""
Iterator Pattern in Python 3.12+.
Implements native Python Iterator Protocol (__iter__, __next__, StopIteration)
to traverse paginated social timeline feeds with O(1) memory overhead.
"""

from __future__ import annotations
from dataclasses import dataclass
from typing import Iterator, List, Optional


@dataclass(frozen=True, slots=True)
class SocialPost:
    post_id: str
    author: str
    content: str


class MockDatabaseFeedProvider:
    """Simulates a remote SQL/NoSQL database supporting cursor pagination."""

    def __init__(self, total_records: int = 5) -> None:
        self._records = [
            SocialPost(f"p_{i}", f"user_{i}", f"Hello World post #{i}!")
            for i in range(total_records)
        ]

    def fetch_page(self, page_number: int, page_size: int) -> List[SocialPost]:
        start = page_number * page_size
        end = start + page_size
        if start >= len(self._records):
            return []
        print(f"[Database Query] SELECT * FROM posts LIMIT {page_size} OFFSET {start}...")
        return self._records[start:end]


class PaginatedFeedIterator(Iterator[SocialPost]):
    """
    Concrete Iterator implementing Python's Iterator Protocol.
    Fetches chunks lazily from DB only when the local buffer is exhausted.
    """

    def __init__(self, provider: MockDatabaseFeedProvider, page_size: int = 2) -> None:
        self._provider = provider
        self._page_size = page_size
        self._current_page = 0
        self._buffer: List[SocialPost] = []
        self._buffer_index = 0
        self._is_exhausted = False

    def __iter__(self) -> PaginatedFeedIterator:
        return self

    def __next__(self) -> SocialPost:
        # If we have items in our local pre-fetched buffer, return next
        if self._buffer_index < len(self._buffer):
            post = self._buffer[self._buffer_index]
            self._buffer_index += 1
            return post

        # If data is completely exhausted, stop iteration
        if self._is_exhausted:
            raise StopIteration

        # Fetch next chunk from database
        self._buffer = self._provider.fetch_page(self._current_page, self._page_size)
        self._current_page += 1
        self._buffer_index = 0

        if not self._buffer:
            self._is_exhausted = True
            raise StopIteration

        post = self._buffer[self._buffer_index]
        self._buffer_index += 1
        return post


# =====================================================================
# 🧪 Execution & Demonstration
# =====================================================================
if __name__ == "__main__":
    db = MockDatabaseFeedProvider(total_records=5)
    feed_stream = PaginatedFeedIterator(db, page_size=2)

    print("--- Streaming Feed via Standard Python For-Loop ---")
    for post in feed_stream:
        print(f"  -> Displaying: [{post.post_id}] {post.author}: '{post.content}'")

    print("\nIteration cleanly completed with zero memory overflow.")
```

---

## 🐍 The Modern Pythonic Generator Alternative (`yield`)

In Python, you can often implement an iterator in just 5 lines using `yield`:

```python
def stream_timeline_posts(db: MockDatabaseFeedProvider, page_size: int = 100) -> Iterator[SocialPost]:
    """Memory-efficient generator function."""
    page = 0
    while True:
        chunk = db.fetch_page(page, page_size)
        if not chunk:
            break
        yield from chunk
        page += 1
```
Use the full class-based Iterator pattern when you need state inspection, serialization, bidirectional traversal (`__reversed__`), or reset capabilities.
