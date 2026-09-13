# 21. Strategy Pattern: Interchangeable Algorithmic Policies

[![Pattern: Behavioral](https://img.shields.io/badge/Pattern-Behavioral-f59e0b.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![Dynamic Routing](https://img.shields.io/badge/AI%20Gateway-Multi--LLM%20Router-purple.svg?style=for-the-badge)]()

> **Intent**: Define a family of algorithms, encapsulate each one, and make them interchangeable. Strategy lets the algorithm vary independently from clients that use it.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine navigating with **Google Maps**:
- You type your destination: `"Airport Terminal 2"`.
- Google Maps doesn't hardcode a single fixed path. It offers **Interchangeable Strategies**:
  1. **Drive (Car Strategy)**: Fastest route taking highways, paying tolls.
  2. **Bicycle Strategy**: Uses dedicated green bike lanes and avoids steep hills.
  3. **Walking Strategy**: Takes pedestrian paths and staircases cars cannot enter.
  4. **Public Transit Strategy**: Calculates bus and subway schedules.
- You swap strategies with a single tap. The map UI and the destination remain identical; only the routing algorithm changes under the hood!

In software, whether calculating **taxes across 50 international jurisdictions** or **routing AI prompts across OpenAI, Anthropic, and Gemini LLMs**, the **Strategy Pattern** lets you swap algorithms polymorphically at runtime.

---

## 🖼️ Architectural Diagram

![Behavioral Design Patterns: Strategy Architecture](../../assets/images/design_patterns/behavioral_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

```python
"""
Strategy Pattern for a Dynamic Multi-LLM Routing Gateway in Python 3.12+.
Swaps between AI model providers dynamically based on cost, latency, or token limits.
"""

from __future__ import annotations
from dataclasses import dataclass
from typing import Protocol, runtime_checkable


@dataclass(frozen=True, slots=True)
class LLMResponse:
    provider: str
    text: str
    tokens_used: int
    cost_usd: float


@runtime_checkable
class LLMStrategy(Protocol):
    """Strategy Protocol: Standardized model invocation interface."""
    def generate_completion(self, prompt: str) -> LLMResponse: ...


# ---------------------------------------------------------------------
# Concrete Strategies
# ---------------------------------------------------------------------
class OpenAIGpt4Strategy:
    """Strategy 1: High intelligence, premium pricing."""

    def generate_completion(self, prompt: str) -> LLMResponse:
        print(f"[OpenAI GPT-4o] Invoking inference API with prompt length: {len(prompt)}...")
        return LLMResponse(
            provider="OpenAI-GPT4o",
            text="Simulated high-reasoning GPT-4 response.",
            tokens_used=450,
            cost_usd=0.012
        )


class AnthropicClaudeStrategy:
    """Strategy 2: Long-context specialized analysis."""

    def generate_completion(self, prompt: str) -> LLMResponse:
        print(f"[Anthropic Claude 3.5 Sonnet] Processing long-context window...")
        return LLMResponse(
            provider="Claude-3.5-Sonnet",
            text="Simulated nuanced Claude architectural analysis.",
            tokens_used=500,
            cost_usd=0.008
        )


class LocalOllamaStrategy:
    """Strategy 3: Zero-cost, privacy-first on-premises local model."""

    def generate_completion(self, prompt: str) -> LLMResponse:
        print("[Local Ollama / Llama-3] Running on local GPU (RTX 4090)...")
        return LLMResponse(
            provider="Local-Llama3",
            text="Simulated local open-source inference.",
            tokens_used=420,
            cost_usd=0.000
        )


# ---------------------------------------------------------------------
# The Context: AI Router Gateway
# ---------------------------------------------------------------------
class LLMGateway:
    """
    Context Object that delegates generation to the currently active Strategy.
    Allows runtime policy switching without changing caller code.
    """

    def __init__(self, initial_strategy: LLMStrategy) -> None:
        self._strategy = initial_strategy

    def set_strategy(self, strategy: LLMStrategy) -> None:
        print(f"\n[Gateway Router] Swapping AI strategy to: {type(strategy).__name__}")
        self._strategy = strategy

    def ask(self, prompt: str) -> LLMResponse:
        return self._strategy.generate_completion(prompt)


# =====================================================================
# 🧪 Execution & Demonstration
# =====================================================================
if __name__ == "__main__":
    gateway = LLMGateway(OpenAIGpt4Strategy())

    # Request 1: Complex reasoning using default GPT-4
    res1 = gateway.ask("Analyze architectural risk in Kubernetes cluster.")
    print(f"Result: Provider={res1.provider}, Cost=${res1.cost_usd:.4f}\n")

    # Strategy Switch: Cost optimization policy swaps to local model
    gateway.set_strategy(LocalOllamaStrategy())
    res2 = gateway.ask("Summarize log files for batch job #882.")
    print(f"Result: Provider={res2.provider}, Cost=${res2.cost_usd:.4f}\n")

    # Strategy Switch: Deep contextual analysis with Claude
    gateway.set_strategy(AnthropicClaudeStrategy())
    res3 = gateway.ask("Review 100-page enterprise compliance audit.")
    print(f"Result: Provider={res3.provider}, Cost=${res3.cost_usd:.4f}")
```

---

## 🔍 Step-by-Step Code Tutorial

1. **Pluggable Architecture**:
   The `LLMGateway` doesn't know *how* OpenAI or Claude works. It only knows that whatever strategy is plugged in complies with `LLMStrategy`.

2. **Pythonic Function Alternative**:
   In Python, you can implement lightweight strategies using plain functions:
   ```python
   from typing import Callable
   TaxStrategy = Callable[[float], float]
   
   def california_tax(subtotal: float) -> float: return subtotal * 0.0925
   def exempt_tax(subtotal: float) -> float: return 0.0
   ```
