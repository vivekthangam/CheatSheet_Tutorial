# 22. Template Method Pattern: Invariant Workflow Skeletons

[![Pattern: Behavioral](https://img.shields.io/badge/Pattern-Behavioral-f59e0b.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![Hollywood Principle](https://img.shields.io/badge/Principle-Don't%20Call%20Us-blue.svg?style=for-the-badge)]()

> **Intent**: Define the skeleton of an algorithm in an operation, deferring some steps to subclasses. Template Method lets subclasses redefine certain steps of an algorithm without changing the algorithm's structure.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine baking a **Layer Cake**:
- The master baking recipe has a **Fixed Invariant Sequence**:
  1. Preheat oven to 350°F (Mandatory invariant).
  2. Mix dry flour and sugar (Mandatory invariant).
  3. **Add Custom Flavoring Hook** (Subclass decides: Chocolate vs. Vanilla vs. Red Velvet).
  4. Bake for 30 minutes (Mandatory invariant).
  5. **Apply Custom Frosting Hook** (Subclass decides: Buttercream vs. Cream Cheese).
- You are not allowed to reverse the order (e.g. frosting the batter before baking!). The overall structure is locked, but key milestones are customizable.

This is the **"Hollywood Principle": *Don't call us, we'll call you***. The parent base class controls the execution order and calls the child hooks when appropriate.

In software, **Data Science / Machine Learning Pipelines** (Load Data → Preprocess → Train Model → Validate Metrics → Deploy) use the **Template Method** to enforce data hygiene while letting data scientists experiment with different model architectures.

---

## 🖼️ Architectural Diagram

![Behavioral Design Patterns: Template Method Architecture](../../assets/images/design_patterns/behavioral_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

```python
"""
Template Method Pattern for Machine Learning Model Training Pipelines in Python 3.12+.
Locks the end-to-end training sequence while allowing customizable data cleaning and modeling hooks.
"""

from __future__ import annotations
from abc import ABC, abstractmethod
from typing import Any, Dict


class MachineLearningPipeline(ABC):
    """
    Abstract Class defining the Template Method (run_pipeline).
    The template method is final and should not be overridden.
    """

    def run_pipeline(self, dataset_uri: str) -> None:
        """The Template Method: Defines the rigid invariant execution flow."""
        print(f"\n=======================================================")
        print(f"🚀 Launching ML Pipeline for: {dataset_uri}")
        print(f"=======================================================")

        # Step 1: Mandatory ingestion
        raw_data = self._ingest_data(dataset_uri)

        # Step 2: Customizable cleaning hook
        clean_data = self.clean_and_preprocess(raw_data)

        # Step 3: Mandatory feature validation
        self._validate_schema(clean_data)

        # Step 4: Customizable model training
        model = self.train_model(clean_data)

        # Step 5: Optional audit hook
        if self.should_perform_compliance_audit():
            self._compliance_audit(model)

        # Step 6: Mandatory artifact export
        self._export_model(model)
        print("✅ Pipeline Completed Successfully!")

    # Invariant Steps (Fixed across all teams)
    def _ingest_data(self, uri: str) -> Dict[str, Any]:
        print(f"[Step 1] Ingesting Parquet blocks from {uri}...")
        return {"records": 100_000, "features": 45}

    def _validate_schema(self, data: Dict[str, Any]) -> None:
        print("[Step 3] Validating data schema against Protobuf definitions (Null checks passed).")

    def _compliance_audit(self, model: str) -> None:
        print("[Step 5] Running FairLearn bias audit and model explainability scan.")

    def _export_model(self, model: str) -> None:
        print(f"[Step 6] Serializing '{model}' to S3 model registry bucket.")

    # Customizable Hooks (Subclasses implement these)
    @abstractmethod
    def clean_and_preprocess(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """Subclasses clean outliers, impute missing values, or vectorize."""
        pass

    @abstractmethod
    def train_model(self, data: Dict[str, Any]) -> str:
        """Subclasses train neural networks, gradient boosters, etc."""
        pass

    # Hook with default implementation (Can be optionally overridden)
    def should_perform_compliance_audit(self) -> bool:
        return True


# ---------------------------------------------------------------------
# Concrete Pipeline 1: Computer Vision ResNet Pipeline
# ---------------------------------------------------------------------
class VisionClassificationPipeline(MachineLearningPipeline):
    def clean_and_preprocess(self, data: Dict[str, Any]) -> Dict[str, Any]:
        print("[Step 2 - Vision] Normalizing image RGB tensors and applying random rotation augmentations.")
        return data

    def train_model(self, data: Dict[str, Any]) -> str:
        print("[Step 4 - Vision] Training PyTorch ResNet-50 on 8x NVIDIA H100 GPUs.")
        return "ResNet50_ImageClassifier_v1.pth"


# ---------------------------------------------------------------------
# Concrete Pipeline 2: Quick Tabular Fraud Detection Pipeline
# ---------------------------------------------------------------------
class TabularFraudPipeline(MachineLearningPipeline):
    def clean_and_preprocess(self, data: Dict[str, Any]) -> Dict[str, Any]:
        print("[Step 2 - Tabular] Standardizing numerical distributions and one-hot encoding categorical tags.")
        return data

    def train_model(self, data: Dict[str, Any]) -> str:
        print("[Step 4 - Tabular] Training XGBoost gradient boosted tree ensemble.")
        return "XGBoost_FraudDetector_v4.json"

    # Override hook to skip slow compliance audit during fast local testing
    def should_perform_compliance_audit(self) -> bool:
        print("[Step 5 Hook] Skipping compliance audit for rapid test iteration.")
        return False


# =====================================================================
# 🧪 Execution & Demonstration
# =====================================================================
if __name__ == "__main__":
    vision_pipeline = VisionClassificationPipeline()
    vision_pipeline.run_pipeline("s3://datasets/imagenet-2026/")

    fraud_pipeline = TabularFraudPipeline()
    fraud_pipeline.run_pipeline("s3://datasets/financial-transactions-q3/")
```

---

## 🔍 Step-by-Step Code Tutorial

1. **Template Method vs Strategy**:
   - **Template Method**: Uses **Inheritance**. The algorithm skeleton is fixed in the parent class; subclasses fill in individual steps.
   - **Strategy**: Uses **Composition**. The entire algorithm is encapsulated in a swappable strategy object.

2. **Hook Methods**:
   `should_perform_compliance_audit` is an optional hook with a sensible default (`return True`). Subclasses can override it to tweak behavior without rewriting the pipeline.
