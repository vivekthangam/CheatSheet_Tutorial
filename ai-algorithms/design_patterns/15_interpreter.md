# 15. Interpreter Pattern: Domain-Specific Language (DSL) Evaluator

[![Pattern: Behavioral](https://img.shields.io/badge/Pattern-Behavioral-f59e0b.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![DSL Engine](https://img.shields.io/badge/Engine-AST%20Evaluator-purple.svg?style=for-the-badge)]()

> **Intent**: Given a language, define a representation for its grammar along with an interpreter that uses the representation to interpret sentences in the language.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine reading **Musical Sheet Notation**:
- A piece of music isn't stored as raw audio sound waves.
- Instead, it's written in a formal musical grammar: Treble clef, quarter notes, sharp symbols, and rests (`🎼 ♩ ♪ ♫ ♬`).
- The musician (the **Interpreter**) looks at a note (e.g., a high `C` quarter note), understands its grammar rules, and translates it into physical finger movements on a piano or flute to produce sound.

In software, your business operations team often wants to define custom eligibility rules without writing raw Python code:
`"user.age >= 21 AND user.country == 'US' AND user.has_vip == True"`
The **Interpreter Pattern** parses this sentence into an **Abstract Syntax Tree (AST)** of expressions (`AndExpression`, `GreaterThanExpression`) and recursively evaluates it against runtime context data.

---

## 🖼️ Architectural Blueprint

![Behavioral Design Patterns: Interpreter Architecture](../../assets/images/design_patterns/behavioral_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

```python
"""
Interpreter Pattern for a Dynamic Business Rule Engine in Python 3.12+.
Evaluates boolean query trees over dictionary context data.
"""

from __future__ import annotations
from dataclasses import dataclass
from typing import Any, Dict, Protocol, runtime_checkable


# ---------------------------------------------------------------------
# Abstract Expression Protocol
# ---------------------------------------------------------------------
@runtime_checkable
class Expression(Protocol):
    def interpret(self, context: Dict[str, Any]) -> bool:
        """Evaluates condition against context dictionary."""
        ...


# ---------------------------------------------------------------------
# Terminal Expressions (Leaves of the AST)
# ---------------------------------------------------------------------
@dataclass(frozen=True, slots=True)
class Equals(Expression):
    """Terminal Expression checking strict equality."""
    field_name: str
    expected_value: Any

    def interpret(self, context: Dict[str, Any]) -> bool:
        actual = context.get(self.field_name)
        return actual == self.expected_value


@dataclass(frozen=True, slots=True)
class GreaterThanOrEqual(Expression):
    """Terminal Expression checking numeric threshold."""
    field_name: str
    threshold: float

    def interpret(self, context: Dict[str, Any]) -> bool:
        actual = context.get(self.field_name)
        if actual is None:
            return False
        return float(actual) >= self.threshold


# ---------------------------------------------------------------------
# Non-Terminal Expressions (Combinators / Branches of the AST)
# ---------------------------------------------------------------------
@dataclass(frozen=True, slots=True)
class AndExpression(Expression):
    """Non-Terminal Expression: Logical Conjunction."""
    left: Expression
    right: Expression

    def interpret(self, context: Dict[str, Any]) -> bool:
        # Short-circuit evaluation
        return self.left.interpret(context) and self.right.interpret(context)


@dataclass(frozen=True, slots=True)
class OrExpression(Expression):
    """Non-Terminal Expression: Logical Disjunction."""
    left: Expression
    right: Expression

    def interpret(self, context: Dict[str, Any]) -> bool:
        # Short-circuit evaluation
        return self.left.interpret(context) or self.right.interpret(context)


# =====================================================================
# 🧪 Execution & Verification
# =====================================================================
if __name__ == "__main__":
    # Define business policy:
    # "Customer must be age >= 18 AND (tier == 'VIP' OR balance >= 5000)"
    age_check = GreaterThanOrEqual("age", 18)
    vip_check = Equals("tier", "VIP")
    balance_check = GreaterThanOrEqual("balance", 5000.0)

    policy_ast = AndExpression(
        left=age_check,
        right=OrExpression(vip_check, balance_check)
    )

    # Test Case 1: Young VIP customer
    alice = {"age": 22, "tier": "VIP", "balance": 200.0}
    print(f"Alice Eligibility: {policy_ast.interpret(alice)}")
    assert policy_ast.interpret(alice) is True

    # Test Case 2: Underage customer with high balance
    bob = {"age": 16, "tier": "REGULAR", "balance": 10000.0}
    print(f"Bob Eligibility  : {policy_ast.interpret(bob)}")
    assert policy_ast.interpret(bob) is False

    # Test Case 3: Adult regular customer with high balance
    charlie = {"age": 35, "tier": "REGULAR", "balance": 7500.0}
    print(f"Charlie Eligibility: {policy_ast.interpret(charlie)}")
    assert policy_ast.interpret(charlie) is True
```

---

## 🔍 Step-by-Step Code Tutorial

1. **Terminal vs Non-Terminal Nodes**:
   - `Equals` and `GreaterThanOrEqual` are **Terminal Expressions**: they directly evaluate context values without recursive children.
   - `AndExpression` and `OrExpression` are **Non-Terminal Expressions**: they compose two sub-expressions and combine their boolean results.

2. **Short-Circuit Performance**:
   Because Python's native `and` / `or` operators short-circuit, if `self.left.interpret(context)` evaluates to `False` in an `AndExpression`, `self.right` is never executed.
