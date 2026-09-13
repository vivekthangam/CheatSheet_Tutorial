# 23. Visitor Pattern: Decoupling Algorithms from Object Structures

[![Pattern: Behavioral](https://img.shields.io/badge/Pattern-Behavioral-f59e0b.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![Double Dispatch](https://img.shields.io/badge/Mechanism-Double%20Dispatch-purple.svg?style=for-the-badge)]()

> **Intent**: Represent an operation to be performed on the elements of an object structure. Visitor lets you define a new operation without changing the classes of the elements on which it operates.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine a **Tax Auditor visiting a Company**:
- The company has different types of assets: **Office Buildings**, **Company Cars**, and **Employee Laptop Computers**.
- You do **not** teach the buildings, cars, and laptops how to calculate state taxes, property depreciation, or carbon emission penalties. That would pollute their classes with complex tax code!
- Instead, the **Auditor (The Visitor)** walks through the office:
  - When the auditor visits a **Building**, the auditor calculates real-estate property tax.
  - When the auditor visits a **Car**, the auditor calculates road fuel tax.
  - When the auditor visits a **Laptop**, the auditor calculates electronics asset depreciation.

The asset classes simply provide an `accept(visitor)` method. If next year Congress passes a new "Green Carbon Audit Tax", you create a new `CarbonAuditVisitor` without modifying a single line of the Building, Car, or Laptop classes!

---

## 🖼️ Architectural Diagram

![Behavioral Design Patterns: Visitor Architecture](../../assets/images/design_patterns/behavioral_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

```python
"""
Visitor Pattern with Double Dispatch in Python 3.12+.
Performs financial auditing and serialization over complex asset object hierarchies.
"""

from __future__ import annotations
from dataclasses import dataclass
from decimal import Decimal
from typing import List, Protocol, runtime_checkable


# ---------------------------------------------------------------------
# Visitor Protocol: Declares visit methods for each concrete element
# ---------------------------------------------------------------------
@runtime_checkable
class AssetVisitor(Protocol):
    def visit_real_estate(self, building: RealEstateAsset) -> None: ...
    def visit_vehicle(self, vehicle: VehicleAsset) -> None: ...
    def visit_server_cluster(self, cluster: ServerClusterAsset) -> None: ...


# ---------------------------------------------------------------------
# Element Protocol: Declares the accept() double dispatch hook
# ---------------------------------------------------------------------
@runtime_checkable
class CorporateAsset(Protocol):
    def accept(self, visitor: AssetVisitor) -> None: ...


# ---------------------------------------------------------------------
# Concrete Elements
# ---------------------------------------------------------------------
@dataclass(frozen=True, slots=True)
class RealEstateAsset:
    property_id: str
    square_meters: float
    market_valuation: Decimal

    def accept(self, visitor: AssetVisitor) -> None:
        visitor.visit_real_estate(self)


@dataclass(frozen=True, slots=True)
class VehicleAsset:
    vin: str
    mileage_km: float
    original_cost: Decimal

    def accept(self, visitor: AssetVisitor) -> None:
        visitor.visit_vehicle(self)


@dataclass(frozen=True, slots=True)
class ServerClusterAsset:
    cluster_id: str
    gpu_count: int
    purchase_price: Decimal

    def accept(self, visitor: AssetVisitor) -> None:
        visitor.visit_server_cluster(self)


# ---------------------------------------------------------------------
# Concrete Visitors: Operations decoupled from Element classes
# ---------------------------------------------------------------------
class AnnualTaxAuditVisitor:
    """Visitor 1: Calculates annual jurisdiction taxes across assets."""

    def __init__(self) -> None:
        self.total_tax_due: Decimal = Decimal(0)

    def visit_real_estate(self, building: RealEstateAsset) -> None:
        # Commercial property tax: 1.5% of market value
        tax = building.market_valuation * Decimal("0.015")
        self.total_tax_due += tax
        print(f"[Tax Audit] Building {building.property_id}: Tax Due = ${tax:,.2f}")

    def visit_vehicle(self, vehicle: VehicleAsset) -> None:
        # Vehicle excise duty: 2.0% of original cost + mileage surcharge
        tax = vehicle.original_cost * Decimal("0.020")
        self.total_tax_due += tax
        print(f"[Tax Audit] Fleet Vehicle {vehicle.vin}: Tax Due = ${tax:,.2f}")

    def visit_server_cluster(self, cluster: ServerClusterAsset) -> None:
        # Tech asset equipment tax: 1.0% flat
        tax = cluster.purchase_price * Decimal("0.010")
        self.total_tax_due += tax
        print(f"[Tax Audit] AI Cluster {cluster.cluster_id}: Tax Due = ${tax:,.2f}")


class JsonExportVisitor:
    """Visitor 2: Exports heterogeneous assets to a unified JSON report."""

    def __init__(self) -> None:
        self.serialized_records: List[dict] = []

    def visit_real_estate(self, building: RealEstateAsset) -> None:
        self.serialized_records.append({
            "type": "REAL_ESTATE",
            "id": building.property_id,
            "sqm": building.square_meters,
            "valuation": str(building.market_valuation)
        })

    def visit_vehicle(self, vehicle: VehicleAsset) -> None:
        self.serialized_records.append({
            "type": "VEHICLE",
            "id": vehicle.vin,
            "mileage": vehicle.mileage_km
        })

    def visit_server_cluster(self, cluster: ServerClusterAsset) -> None:
        self.serialized_records.append({
            "type": "AI_HARDWARE",
            "id": cluster.cluster_id,
            "h100_gpus": cluster.gpu_count
        })


# =====================================================================
# 🧪 Execution & Demonstration
# =====================================================================
if __name__ == "__main__":
    assets: List[CorporateAsset] = [
        RealEstateAsset("BLD-HQ-SILICON", square_meters=5500.0, market_valuation=Decimal("15000000.00")),
        VehicleAsset("VIN-TESLA-SEMI-001", mileage_km=45000.0, original_cost=Decimal("180000.00")),
        ServerClusterAsset("CLUSTER-H100-NODE1", gpu_count=64, purchase_price=Decimal("2500000.00")),
    ]

    print("--- Operation 1: Annual Tax Assessment ---")
    tax_auditor = AnnualTaxAuditVisitor()
    for asset in assets:
        asset.accept(tax_auditor) # Double dispatch
    print(f"Total Corporate Tax Assessed: ${tax_auditor.total_tax_due:,.2f}\n")

    print("--- Operation 2: Unified JSON Catalog Export ---")
    json_exporter = JsonExportVisitor()
    for asset in assets:
        asset.accept(json_exporter)
    print(f"Exported {len(json_exporter.serialized_records)} records to JSON schema:")
    for record in json_exporter.serialized_records:
        print(f"  {record}")
```

---

## 🔍 Step-by-Step Code Tutorial

1. **The Double Dispatch Mechanism**:
   When you call `asset.accept(visitor)`, Python dispatches on the runtime type of `asset`. Inside `RealEstateAsset.accept`, it calls `visitor.visit_real_estate(self)`, which dispatches on the runtime type of `visitor`. This allows executing the right method without dynamic `isinstance` cascades.

2. **Extending Operations without Modifying Elements**:
   Notice that `RealEstateAsset`, `VehicleAsset`, and `ServerClusterAsset` never change. You can add 20 new visitors (`InsuranceValuationVisitor`, `CarbonFootprintVisitor`, `DepreciationVisitor`) and none of the core asset classes need recompilation.
