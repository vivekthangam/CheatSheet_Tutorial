# 06. Adapter Pattern: Interface Translation Bridge

[![Pattern: Structural](https://img.shields.io/badge/Pattern-Structural-6366f1.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![Compatibility](https://img.shields.io/badge/Legacy-Compatibility-blue.svg?style=for-the-badge)]()

> **Intent**: Convert the interface of a class into another interface clients expect. Adapter lets classes work together that couldn't otherwise because of incompatible interfaces.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine you travel from London (UK) to New York (USA) carrying your laptop:
- Your laptop charger has a **UK 3-prong triangular plug**.
- The American hotel wall outlet only accepts a **US 2-prong flat parallel plug**.
- You do **not** take wire cutters, dismantle your laptop charger cord, and solder new copper prongs to it!
- Instead, you plug your UK charger into a cheap **Travel Plug Adapter**, and plug the adapter into the US wall.

In software, your modern Python microservice expects clean, JSON-based asynchronous objects, but your enterprise bank requires communicating with a 20-year-old legacy SOAP XML SDK. The **Adapter Pattern** wraps the legacy client, translating modern requests into legacy function calls behind the scenes.

---

## 🖼️ Architectural Diagram

![Structural Design Patterns: Adapter Architecture](../../assets/images/design_patterns/structural_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

```python
"""
Adapter Pattern in Python 3.12+.
Bridges an incompatible legacy 3rd-party banking client to a modern typed gateway.
"""

from __future__ import annotations
from dataclasses import dataclass
from decimal import Decimal
import time
from typing import Optional, Protocol, runtime_checkable


# ---------------------------------------------------------------------
# Target Modern Domain Interface
# ---------------------------------------------------------------------
@dataclass(frozen=True, slots=True)
class PaymentRequest:
    transaction_id: str
    amount: Decimal
    currency: str
    customer_token: str


@dataclass(frozen=True, slots=True)
class PaymentReceipt:
    success: bool
    reference_id: Optional[str]
    error_message: Optional[str] = None


@runtime_checkable
class PaymentGateway(Protocol):
    def process_charge(self, request: PaymentRequest) -> PaymentReceipt:
        """Standardized enterprise charge method."""
        ...


# ---------------------------------------------------------------------
# Adaptee: Incompatible Legacy 3rd-Party Vendor SDK
# ---------------------------------------------------------------------
class LegacyVendorBankingSDK:
    """
    Simulates a legacy library that cannot be modified.
    Uses integer cents, positional args, and throws raw exceptions.
    """

    def make_charge(self, amount_in_cents: int, customer_auth: str, curr_iso: str) -> str:
        print(f"[Legacy SDK] Executing XML-RPC transaction: {amount_in_cents} cents ({curr_iso})...")
        if amount_in_cents <= 0:
            raise ValueError("ERR_INVALID_SUM: Amount must be greater than zero.")
        # Generates vendor reference string
        return f"VND-REF-{int(time.time() * 1000)}"


# ---------------------------------------------------------------------
# The Adapter: Conforms to Modern Interface while delegating to Legacy
# ---------------------------------------------------------------------
class VendorPaymentAdapter:
    """
    Object Adapter: Wraps LegacyVendorBankingSDK and exposes PaymentGateway.
    """

    def __init__(self, legacy_sdk: LegacyVendorBankingSDK) -> None:
        self._sdk = legacy_sdk

    def process_charge(self, request: PaymentRequest) -> PaymentReceipt:
        try:
            # Convert modern Decimal dollars to legacy integer cents
            cents = int(request.amount * Decimal(100))
            
            # Delegate to legacy method with required signature
            raw_ref = self._sdk.make_charge(
                amount_in_cents=cents,
                customer_auth=request.customer_token,
                curr_iso=request.currency
            )
            return PaymentReceipt(success=True, reference_id=raw_ref)
        except Exception as err:
            return PaymentReceipt(success=False, reference_id=None, error_message=str(err))


# ---------------------------------------------------------------------
# Client Consumer
# ---------------------------------------------------------------------
class CheckoutOrchestrator:
    def __init__(self, gateway: PaymentGateway) -> None:
        self._gateway = gateway

    def checkout(self, customer: str, total: Decimal) -> None:
        req = PaymentRequest(
            transaction_id="TXN-991204",
            amount=total,
            currency="USD",
            customer_token=f"tok_{customer}"
        )
        receipt = self._gateway.process_charge(req)
        if receipt.success:
            print(f"[Checkout Success] Charged ${total} | Gateway Ref: {receipt.reference_id}")
        else:
            print(f"[Checkout Failed] Reason: {receipt.error_message}")


# =====================================================================
# 🧪 Execution & Verification
# =====================================================================
if __name__ == "__main__":
    # Wrap legacy client in adapter
    legacy_client = LegacyVendorBankingSDK()
    adapted_gateway = VendorPaymentAdapter(legacy_client)

    # Client runs cleanly without knowing about legacy XML or integer cents
    orchestrator = CheckoutOrchestrator(adapted_gateway)
    orchestrator.checkout("alice_wonderland", Decimal("149.99"))
```

---

## 🔍 Step-by-Step Code Tutorial

1. **Object Adapter vs Class Adapter**:
   - **Object Adapter (Used here)**: Uses composition (`self._sdk = legacy_sdk`). This is preferred in 99% of Python applications because it adheres to the principle of composition over inheritance and can adapt any subclass of the adaptee.
   - **Class Adapter**: Uses multiple inheritance (`class Adapter(PaymentGateway, LegacySDK)`). Rarely needed in modern Python.

2. **Error Translation**:
   The adapter catches raw vendor-specific exceptions (`ERR_INVALID_SUM`) and maps them into the standardized domain model (`PaymentReceipt(success=False, error_message=...)`).
