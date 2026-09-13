# 03. Abstract Factory Pattern: Cohesive Families of Objects

[![Pattern: Creational](https://img.shields.io/badge/Pattern-Creational-10b981.svg?style=for-the-badge)]()
[![Python 3.12+](https://img.shields.io/badge/Python-3.12%2B-3776AB.svg?style=for-the-badge&logo=python&logoColor=white)]()
[![Family Consistency](https://img.shields.io/badge/Architecture-Family%20Consistency-purple.svg?style=for-the-badge)]()

> **Intent**: Provide an interface for creating families of related or dependent objects without specifying their concrete classes.

---

## 🧠 The Real-World Mental Model (Explain Like I'm 5)

Imagine you are furnishing an entire living room at **IKEA**.
- You decide on a **Victorian Gothic Style**. You need a Victorian Chair, a Victorian Coffee Table, and a Victorian Sofa.
- If you accidentally mix a modern neon plastic chair with an antique Victorian mahogany table, your living room looks bizarre and mismatched!
- The **Abstract Factory** is like buying a **Curated Room Suite**:
  - If you pick the `VictorianFurnitureFactory`, you are guaranteed to get matching Victorian Chairs, Victorian Sofas, and Victorian Tables.
  - If you pick the `ModernMinimalistFactory`, you are guaranteed to get matching Modern Chairs, Modern Sofas, and Modern Tables.

The client program only interacts with the generic concepts (`Chair`, `Table`, `Sofa`) and lets the chosen Factory enforce that all instantiated items harmonize perfectly.

---

## 🖼️ Architectural Diagram

![Creational Design Patterns: Abstract Factory Architecture](../../assets/images/design_patterns/creational_patterns_architecture.jpg)

---

## 💻 Production Python 3.12+ Blueprint

Here is a multi-cloud enterprise infrastructure suite (AWS vs. GCP) in Python 3.12+:

```python
"""
Abstract Factory Pattern for Cloud Infrastructure Resource Suites.
Demonstrating family consistency across Object Storage and Message Queuing.
"""

from __future__ import annotations
from dataclasses import dataclass
from typing import Protocol, runtime_checkable


# ---------------------------------------------------------------------
# Abstract Products (Protocols)
# ---------------------------------------------------------------------
@runtime_checkable
class BlobStorage(Protocol):
    def upload(self, bucket: str, key: str, payload: bytes) -> str:
        """Upload raw binary payload and return access URI."""
        ...


@runtime_checkable
class MessageQueue(Protocol):
    def publish(self, channel: str, message: str) -> None:
        """Publish payload message to distributed queue."""
        ...


# ---------------------------------------------------------------------
# Concrete Products: AWS Family
# ---------------------------------------------------------------------
@dataclass(frozen=True, slots=True)
class S3BlobStorage:
    region: str = "us-east-1"

    def upload(self, bucket: str, key: str, payload: bytes) -> str:
        uri = f"s3://{bucket}.s3.{self.region}.amazonaws.com/{key}"
        print(f"[AWS S3] Stored {len(payload)} bytes to {uri}")
        return uri


@dataclass(frozen=True, slots=True)
class SqsMessageQueue:
    queue_url: str = "https://sqs.us-east-1.amazonaws.com/12345/tasks"

    def publish(self, channel: str, message: str) -> None:
        print(f"[AWS SQS] Enqueued message to {self.queue_url} (channel={channel}): {message}")


# ---------------------------------------------------------------------
# Concrete Products: GCP Family
# ---------------------------------------------------------------------
@dataclass(frozen=True, slots=True)
class GcsBlobStorage:
    project_id: str = "prod-corp-cloud"

    def upload(self, bucket: str, key: str, payload: bytes) -> str:
        uri = f"gs://{bucket}/{key}"
        print(f"[Google Cloud Storage] Uploaded {len(payload)} bytes to {uri} under project {self.project_id}")
        return uri


@dataclass(frozen=True, slots=True)
class PubSubMessageQueue:
    topic_path: str = "projects/prod-corp-cloud/topics/events"

    def publish(self, channel: str, message: str) -> None:
        print(f"[Google Cloud Pub/Sub] Published message to {self.topic_path} [{channel}]: {message}")


# ---------------------------------------------------------------------
# The Abstract Factory Interface
# ---------------------------------------------------------------------
class CloudResourceFactory(Protocol):
    def create_blob_storage(self) -> BlobStorage: ...
    def create_message_queue(self) -> MessageQueue: ...


# ---------------------------------------------------------------------
# Concrete Factories
# ---------------------------------------------------------------------
class AwsResourceFactory:
    def create_blob_storage(self) -> BlobStorage:
        return S3BlobStorage(region="us-west-2")

    def create_message_queue(self) -> MessageQueue:
        return SqsMessageQueue()


class GcpResourceFactory:
    def create_blob_storage(self) -> BlobStorage:
        return GcsBlobStorage(project_id="enterprise-prod-99")

    def create_message_queue(self) -> MessageQueue:
        return PubSubMessageQueue()


# ---------------------------------------------------------------------
# Client Service Orchestrator
# ---------------------------------------------------------------------
class DocumentIngestionPipeline:
    """
    Client orchestrator decoupled from underlying cloud vendor implementations.
    Guaranteed never to cross-mix S3 with Google Pub/Sub.
    """

    def __init__(self, factory: CloudResourceFactory) -> None:
        self.storage = factory.create_blob_storage()
        self.queue = factory.create_message_queue()

    def ingest_document(self, filename: str, content: bytes) -> None:
        print(f"\n--- Ingesting Document: {filename} ---")
        uri = self.storage.upload(bucket="finance-invoices", key=filename, payload=content)
        self.queue.publish(channel="invoice-events", message=f"NEW_INGESTION:{uri}")


# =====================================================================
# 🧪 Execution Demo
# =====================================================================
if __name__ == "__main__":
    test_pdf = b"%PDF-1.4 Mock Financial Statement"

    # Deploy on AWS
    aws_pipeline = DocumentIngestionPipeline(AwsResourceFactory())
    aws_pipeline.ingest_document("q3_report.pdf", test_pdf)

    # Deploy on GCP with zero client code modification
    gcp_pipeline = DocumentIngestionPipeline(GcpResourceFactory())
    gcp_pipeline.ingest_document("q3_report.pdf", test_pdf)
```

---

## 🔍 Step-by-Step Code Tutorial

1. **The Family Guarantee**:
   Notice that `DocumentIngestionPipeline` accepts only a single factory instance. It is mathematically impossible for a developer to accidentally connect an AWS S3 bucket to a GCP Pub/Sub topic because the products are created in coordinated sets by the factory.

2. **Decoupled Client Code**:
   The `DocumentIngestionPipeline` relies completely on `BlobStorage` and `MessageQueue` protocols. If you need to add an `AzureResourceFactory` (Blob Storage + Service Bus), you simply write the Azure implementations and pass the factory into the client.

3. **Runtime Factory Resolution**:
   In enterprise frameworks, factories are often resolved via environment variables:
   ```python
   def get_cloud_factory(env: str) -> CloudResourceFactory:
       match env.upper():
           case "AWS": return AwsResourceFactory()
           case "GCP": return GcpResourceFactory()
           case _: raise ValueError(f"Unsupported cloud provider: {env}")
   ```
