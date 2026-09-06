[🏠 Back to Home](README.md) | [💼 Business English Guide](business_english_corporate_words_master_guide.md) | [📖 IELTS 500 Lexicon](ielts_500_words_master_guide.md) | [💬 500 Spoken English Scenarios](spoken_english_500_scenarios_master_guide.md) | [🎭 Idioms Guide](english_idioms_master_guide.md) | [💬 Phrases Guide](english_phrases_master_guide.md)

# 💻 IT & Software Engineering Technical Words Master Guide (Global Tech, Silicon Valley & Cloud Architecture Fluency)
### *(75 Core Technical Terms, Architectural Concepts & Engineering Vocabulary with Tamil Intuition, Real PR Reviews, War Room Dialogues & System Design Scenarios)*

[![IT Technical Terms](https://img.shields.io/badge/Engineering%20Vocabulary-Global%20IT%20Fluency-blue.svg?style=for-the-badge)]()
[![System Design](https://img.shields.io/badge/Architecture-Distributed%20Systems-brightgreen.svg?style=for-the-badge)]()
[![Tamil Context](https://img.shields.io/badge/Language-Tamil%20Meaning%20%26%20Intuition-purple.svg?style=for-the-badge)]()
[![War Room Dialogues](https://img.shields.io/badge/Context-PR%20Reviews%20%2B%20War%20Rooms-orange.svg?style=for-the-badge)]()

---

## 🏛️ What's Inside the IT Tech Words Master Guide

Every single term in this guide contains:
1. **Phonetic Pronunciation** (`[eye-duhm-POH-tuhnt]`, `[SHAR-ding]`, `[kash stam-PEED]`)
2. **Tamil Meaning & Intuition** (*Tamil-la idhu enna, eppadi feel panni use pannanum*)
3. **Precise Technical & Architectural Definition** (RFC / Industry Standard)
4. **Natural Engineering Collocations** (*"enforce idempotency"*, *"mitigate cache stampede"*, *"cascading failure"*)
5. **3 Real-World Multi-Context Sentences**:
   - 💻 **Code Review & Pull Request (PR) Discussion**
   - 🏗️ **System Design & Architecture Review Meeting**
   - 🚨 **Live Production Outage / Sev-1 War Room Context**
6. 🎯 **Staff Engineer Insider Tip** (Subtle gotchas, concurrency bugs, and interview edge cases)

---

## 📑 Master Catalog Overview (75 High-Impact IT Terms)

| Category | Words Covered | Core Technical Concepts |
| :--- | :---: | :--- |
| **1. Distributed Systems & Architecture** | **Words 1–15** | *Idempotency, Decoupling / Loosely Coupled, Circuit Breaker, Rate Limiting / Throttling, High Availability (HA), Sharding / Partitioning, Backpressure, Failover, CQRS, Eventual Consistency, Reverse Proxy, Sidecar Pattern, Ingress & Egress, Service Mesh, Monolith vs. Microservices* |
| **2. Data Engineering & Databases** | **Words 16–30** | *Cache Stampede / Avalanche, Deadlock, Connection Pool Exhaustion, Replication Lag, CAP Theorem, ACID vs. BASE, Cold Start, Write-Through vs. Cache-Aside, Change Data Capture (CDC), Database Indexing (B-Tree vs. LSM-Tree), Optimistic vs. Pessimistic Locking, Hotspotting, Polling vs. WebSockets vs. SSE, Dirty Read vs. Phantom Read, Write-Ahead Logging (WAL)* |
| **3. Concurrency, Performance & Memory** | **Words 31–45** | *Race Condition, Memory Leak, Garbage Collection Pause / Stop-The-World, P99 / P95 Latency, Throughput vs. Latency, Concurrency vs. Parallelism, Thread Pool Starvation, Mutex vs. Semaphore, Non-Blocking I/O (NIO), Context Switching, CPU Throttling, Thread Contention, Graceful Degradation, Telemetry, Heap vs. Stack Memory* |
| **4. Cloud Infrastructure & DevOps** | **Words 46–60** | *Blue-Green vs. Canary Deployment, Infrastructure as Code (IaC), Immutable Infrastructure, Observability, Zero Trust Architecture, Rolling Update, Liveness vs. Readiness Probes, Horizontal vs. Vertical Scaling, Secret Management / Key Vault, Cascading Failure, Autoscaling (HPA), Chaos Engineering, SLA vs. SLO vs. SLI, DaemonSet vs. StatefulSet, Container Orchestration* |
| **5. Web Protocols, APIs & Security** | **Words 61–75** | *Payload, Serialization vs. Deserialization, CORS, CSRF, XSS, mTLS (Mutual TLS), Webhook, Stateless vs. Stateful, Man-in-the-Middle (MITM), JWT (JSON Web Token), API Gateway, Multiplexing, DNS Propagation, DDoS, Envelope Encryption* |

---

## ⚡ Additional Exclusive Toolkits Inside the Guide

1. **The Top 25 IT Engineering Acronyms Decoded Table**:
   - Complete engineering translations for: `API`, `CI/CD`, `SRE`, `JWT`, `CORS`, `mTLS`, `TTL`, `OOM`, `RPC`, `CRUD`, `ORM`, `VPC`, `IAM`, `DNS`, `SPOF`, `ACID`, `BASE`, `RPO/RTO`, `NIO`, `GC`, `HPA`, `NAT`, `SSL/TLS`, `WAF`, `SDK`.
2. **Real-World Engineering Dialogues**:
   - **Scenario A: PR Architectural Debate**: Staff Architect vs. Senior Engineer on why calling an external payment gateway inside `@Transactional` causes connection pool exhaustion, cascading failures, and breaks idempotency.
   - **Scenario B: Sev-1 Production Outage War Room**: Incident Commander, SRE Lead, and Tech Lead resolving a cache stampede that spiked P99 latency to 14,000ms and triggered a cascading reboot loop through emergency throttling and distributed Mutex locks.

---

## 🔬 Sample High-Impact Entries (Quick Showcase & Summary Table)

### 📊 Quick Reference Table: High-Frequency Production Terms

| Term & Pronunciation | Architecture Domain | Tamil Meaning & Intuition | Production Incident / War Room Context | Staff Engineer Rule |
| :--- | :--- | :--- | :--- | :--- |
| **Idempotency / Idempotent** (`[eye-duhm-POH-tuhnt]`) | Distributed Systems | ஒரு செயலை ஒருமுறை செய்தாலும், தவறுதலாக பத்து முறை செய்தாலும், முடிவில் இரட்டை மாற்றமோ பக்கவிளைவோ இன்றி ஒரே நிலையாக இருத்தல். | *The customer was billed three times because the bank webhook processor was not idempotent under message retry storms.* | Enforce unique `Idempotency-Key` headers stored with TTL in Redis for all financial/mutating API endpoints. |
| **Cache Stampede / Avalanche** (`[kash stam-PEED]`) | Data Engineering & Caching | ஒரு பிரபலமான கேச் கீ (Cache Key) ஒரே விநாடியில் expire ஆகும்போது, பல்லாயிரக்கணக்கான கோரிக்கைகள் நேரடியாக DB-ஐ தாக்கி முடக்குதல். | *When the World Cup live score cache expired, 200,000 requests bypassed Redis in one second, crushing our Postgres database instantly.* | Mitigate with randomized TTL jitter (`TTL + rand(δ)`) and distributed Mutex locks so only 1 worker queries DB on cache miss. |
| **Garbage Collection (Stop-The-World)** (`[STAH-the-world]`) | Concurrency & Memory | பயன்பாட்டில் இல்லாத குப்பையான மெமரியை சுத்தம் செய்ய, அப்ளிகேஷனின் அனைத்து த்ரெட்களையும் சில நொடிகள் மொத்தமாக உறைய வைத்தல். | *Our 32GB JVM heap triggered a full garbage collection cycle, freezing all user requests for 6 full seconds and causing load balancers to mark nodes dead.* | Tune heap headroom, eliminate unnecessary object allocations, and adopt ultra-low pause collectors (Java 21 ZGC / Shenandoah). |

---

### Detailed High-Impact Sample Breakdowns

#### Entry #1: **Idempotency / Idempotent** `[eye-duhm-POH-tuhnt]` *(Noun / Adjective)*

- **Tamil Meaning & Intuition**: *ஒரு செயலை ஒருமுறை செய்தாலும் சரி, தவறுதலாக பத்து முறை செய்தாலும் சரி, முடிவில் எந்தவித பக்கவிளைவோ இரட்டை மாற்றமோ ஏற்படாமல் ஒரே நிலையாக இருக்கும் தன்மை.*
- **Technical Definition**: An operation that can be applied multiple times without changing the result beyond the initial application ($f(f(x)) = f(x)$).
- **Natural Collocations**: *Idempotent endpoint, enforce idempotency, idempotency key, naturally idempotent.*
- 💻 **Code Review / PR**: *"Please add a unique `Idempotency-Key` header check in Redis before deducting wallet balances to protect against client network retries."*
- 🏗️ **System Design**: *"HTTP `GET`, `PUT`, and `DELETE` methods must remain strictly **idempotent**, whereas `POST` typically is not."*
- 🚨 **War Room Incident**: *"The customer was billed three times because the bank webhook processor was not **idempotent** under message retry storms."*
- 🎯 **Staff Engineer Tip**: In distributed systems, networks fail constantly. If an API is not idempotent, automatic retries will corrupt database and financial state.

---

#### Entry #16: **Cache Stampede / Cache Avalanche** `[kash stam-PEED / AV-uh-lanch]` *(Noun)*

- **Tamil Meaning & Intuition**: *ஒரு பிரபலமான கேச் கீ (Cache Key) ஒரே விநாடியில் எக்ஸ்பைர் (expire) ஆகும்போது, ஒரே நேரத்தில் பல்லாயிரக்கணக்கான கோரிக்கைகள் நேரடியாக டேட்டாபேஸை தாக்கி அதை செயலிழக்கச் செய்யும் பேராபத்து.*
- **Technical Definition**: A phenomenon where many concurrent requests miss the cache simultaneously upon TTL expiration, all querying the underlying database at the exact same moment.
- **Natural Collocations**: *Prevent cache stampede, cache stampede mitigation, probabilistic early expiration, mutex locking.*
- 💻 **Code Review / PR**: *"Add randomized TTL jitter (e.g., `300s + rand(30s)`) to our Redis cache keys to prevent a midnight **cache avalanche**."*
- 🏗️ **System Design**: *"We mitigated **cache stampedes** using distributed Redis Mutex locks, ensuring only one worker queries the database while others wait."*
- 🚨 **War Room Incident**: *"When the World Cup live score cache expired, 200,000 requests bypassed Redis in one second, crushing our Postgres database instantly."*
- 🎯 **Staff Engineer Tip**: Never let hot cache keys expire with deterministic timestamps. Always apply jitter and consider probabilistic early background refreshes (XFetch algorithm).

---

#### Entry #33: **Garbage Collection Pause / Stop-The-World** `[STAH-the-world]` *(Noun)*

- **Tamil Meaning & Intuition**: *பயன்பாட்டில் இல்லாத குப்பையான மெமரியை சுத்தம் செய்வதற்காக, அப்ளிகேஷனின் அனைத்து த்ரெட்களையும் சில நொடிகள் மொத்தமாக உறைய வைக்கும் JVM செயல்முறை.*
- **Technical Definition**: An execution pause in managed runtimes (like the JVM) where all application threads are halted while the garbage collector collects memory and compacts the heap.
- **Natural Collocations**: *Stop-The-World pause, GC pause latency, tune garbage collector, G1GC / ZGC.*
- 💻 **Code Review / PR**: *"Migrating our financial order-matching engine to Java 21 with the **ZGC** collector eliminated our 800ms **Stop-The-World** latency spikes."*
- 🏗️ **System Design**: *"For ultra-low-latency microservices, configure heap allocations carefully and eliminate object churn to prevent concurrent mark-sweep pauses from violating our 10ms SLA."*
- 🚨 **War Room Incident**: *"Our 32GB JVM heap triggered a full garbage collection cycle, freezing all user requests for 6 full seconds and causing load balancers to mark the servers dead."*
- 🎯 **Staff Engineer Tip**: Huge heaps (e.g., >32GB) with older garbage collectors (like Parallel GC or CMS) can produce catastrophic multi-second Stop-The-World pauses. For modern Java microservices, prioritize Generational ZGC or Shenandoah.

---

## 📑 Master Table of Contents

- [🏛️ What's Inside the IT Tech Words Master Guide](#️-whats-inside-the-it-tech-words-master-guide)
- [📑 Master Catalog Overview (75 High-Impact IT Terms)](#-master-catalog-overview-75-high-impact-it-terms)
- [⚡ Additional Exclusive Toolkits Inside the Guide](#-additional-exclusive-toolkits-inside-the-guide)
- [🔬 Sample High-Impact Entries (Quick Showcase & Summary Table)](#-sample-high-impact-entries-quick-showcase--summary-table)
- [1. The Philosophy of Technical Fluency: Talking Like a Senior/Staff Engineer](#1-the-philosophy-of-technical-fluency-talking-like-a-seniorstaff-engineer)
- [2. Category 1: Distributed Systems & High-Level Architecture (Words 1–15)](#2-category-1-distributed-systems--high-level-architecture-words-115)
- [3. Category 2: Data Engineering, Databases & Caching (Words 16–30)](#3-category-2-data-engineering-databases--caching-words-1630)
- [4. Category 3: Concurrency, Performance & Memory Mechanics (Words 31–45)](#4-category-3-concurrency-performance--memory-mechanics-words-3145)
- [5. Category 4: Cloud Infrastructure, DevOps & Reliability (Words 46–60)](#5-category-4-cloud-infrastructure-devops--reliability-words-4660)
- [6. Category 5: Web Protocols, APIs & Network Security (Words 61–75)](#6-category-5-web-protocols-apis--network-security-words-6175)
- [7. The Top 25 IT Engineering Acronyms Decoded](#7-the-top-25-it-engineering-acronyms-decoded)
- [8. Real-World Engineering Dialogues (PR Review, System Design & Sev-1 War Room)](#8-real-world-engineering-dialogues-pr-review-system-design--sev-1-war-room)

---

# 1. The Philosophy of Technical Fluency: Talking Like a Senior/Staff Engineer

In global engineering teams (Google, Uber, Meta, Amazon, Microsoft, Netflix), seniority is judged not just by lines of code written, but by **precise architectural vocabulary**.

When an incident occurs or an architecture review happens:
- A junior engineer says: *"The database is slow because too many people are using it at once, and it crashed."*
- A staff engineer says: *"We are experiencing a **cache stampede** that saturated our database **connection pool**, triggering **cascading timeouts** and thread pool **starvation**."*

```
┌────────────────────────────────────────────────────────────────────────┐
│               THE 3 LEVELS OF TECHNICAL COMMUNICATION                  │
├────────────────────────────────────────────────────────────────────────┤
│ LEVEL 1: SYMPTOMATIC (Junior)                                          │
│ "The server got stuck and gave 500 errors."                            │
│                                                                        │
│ LEVEL 2: DESCRIPTIVE (Mid-Level)                                       │
│ "The payment API was called multiple times, so the user got charged   │
│ twice."                                                                │
│                                                                        │
│ LEVEL 3: ARCHITECTURAL & PRECISE (Senior / Staff)                     │
│ "The payment endpoint lacks an **idempotency key**; network retries    │
│ caused duplicate order settlement."                                    │
└────────────────────────────────────────────────────────────────────────┘
```

This guide decodes the **75 most critical IT technical words** that every software developer, cloud engineer, architect, and tech lead must command with effortless fluency.

---

# 2. Category 1: Distributed Systems & High-Level Architecture (Words 1–15)

---

### 1. **Idempotency / Idempotent** `[eye-duhm-POH-tuhnt]` *(Noun / Adjective)*
- **Tamil Meaning**: ஒரு செயலை ஒருமுறை செய்தாலும் சரி, தவறுதலாக பத்து முறை செய்தாலும் சரி, முடிவில் எந்தவித பக்கவிளைவோ இரட்டை மாற்றமோ ஏற்படாமல் ஒரே நிலையாக இருக்கும் தன்மை.
- **Technical Definition**: An operation that can be applied multiple times without changing the result beyond the initial application ($f(f(x)) = f(x)$).
- **Natural Collocations**: *Idempotent endpoint, enforce idempotency, idempotency key, naturally idempotent*.
- 💻 *Code Review / PR*: *"Please add a unique `Idempotency-Key` header check in Redis before deducting wallet balances to protect against client network retries."*
- 🏗️ *System Design*: *"HTTP `GET`, `PUT`, and `DELETE` methods must remain strictly **idempotent**, whereas `POST` typically is not."*
- 🚨 *War Room Incident*: *"The customer was billed three times because the bank webhook processor was not **idempotent** under message retry storms."*
- 🎯 *Staff Engineer Tip*: In distributed systems, networks fail constantly. If an API is not idempotent, automatic retries will corrupt financial and database state.

---

### 2. **Decoupling / Loosely Coupled** `[dee-KUHP-ling / LOOS-lee KUHP-uhld]` *(Noun / Adjective)*
- **Tamil Meaning**: இரு அமைப்புகள் ஒன்றையொன்று நேரடியாக சார்ந்து இருக்காமல், ஒன்று உடைந்தாலும் மற்றொன்று சுதந்திரமாக இயங்கும் வகையில் பிரித்து அமைத்தல்.
- **Technical Definition**: Designing software components so that each has little or no direct knowledge of or dependency on the internal definitions of others.
- **Natural Collocations**: *Decouple services, loosely coupled architecture, tight coupling*.
- 💻 *Code Review / PR*: *"Instead of having the order service call the notification service synchronously, let's **decouple** them via an Apache Kafka topic."*
- 🏗️ *System Design*: *"A **loosely coupled** event-driven architecture allows our logistics squad to deploy updates without coordinating with the checkout squad."*
- 🚨 *War Room Incident*: *"Because our inventory and payment services were tightly coupled, an inventory crash took down our entire checkout portal."*

---

### 3. **Circuit Breaker** `[SUR-kit BREY-ker]` *(Noun / Pattern)*
- **Tamil Meaning**: வீட்டில் உள்ள மின் இணைப்பில் அதிக மின்னழுத்தம் வந்தால் மின் இணைப்பை உடனே துண்டித்து தீப்பிடிக்காமல் காக்கும் பியூஸ்/சுவிட்ச் போல், இயங்காத சர்வரை தொடர்ந்து அழைத்து ஒட்டுமொத்த அமைப்பும் அழியாமல் காக்கும் மென்பொருள் உத்தி.
- **Technical Definition**: A design pattern used to detect failures and encapsulate the logic of preventing a failure from constantly recurring during maintenance or temporary outages.
- **Natural Collocations**: *Circuit breaker tripped, open state, half-open state, Resilience4j circuit breaker*.
- 💻 *Code Review / PR*: *"Wrap this external credit rating HTTP call in a Resilience4j **circuit breaker** with a 3-second fallback response."*
- 🏗️ *System Design*: *"When the fraud detection vendor goes down, the **circuit breaker** trips to 'OPEN', instantly serving fallback cached decisions."*
- 🚨 *War Room Incident*: *"Without a **circuit breaker**, our frontend threads waited 30 seconds for dead upstream APIs, causing total thread pool starvation."*

---

### 4. **Rate Limiting / Throttling** `[REYT LIM-i-ting / THROHT-ling]` *(Noun)*
- **Tamil Meaning**: ஒரு பயனர் அல்லது போட் ஒரு வினாடிக்கு அனுமதிக்கப்பட்ட அளவுக்கு மேல் அதிக கோரிக்கைகளை (requests) அனுப்பி சர்வரை திணறடிக்காமல் கட்டுப்படுத்தும் முறை.
- **Technical Definition**: Controlling the rate of traffic sent or received by an endpoint to prevent resource starvation, denial-of-service, and abuse.
- **Natural Collocations**: *Enforce rate limiting, token bucket algorithm, throttle incoming traffic, return 429 Too Many Requests*.
- 💻 *Code Review / PR*: *"Configure a Redis Token Bucket filter on our public login API to **rate limit** requests to 5 attempts per IP per minute."*
- 🏗️ *System Design*: *"Our API Gateway uses distributed **rate limiting** to ensure our premium enterprise tenants receive guaranteed quota."*
- 🚨 *War Room Incident*: *"A rogue scraping bot bombarded our product search endpoint with 40,000 req/sec; we had to enable emergency ingress **throttling**."*

---

### 5. **High Availability (HA)** `[hahy uh-vey-luh-BIL-i-tee]` *(Noun)*
- **Tamil Meaning**: எந்த ஒரு குறிப்பிட்ட சர்வர் அல்லது பாகம் பழுதானாலும், பயனருக்கு எவ்வித தடையுமின்றி 24/7 தொடர்ந்து இயங்கிக்கொண்டே இருக்கும் உயர் நம்பகத்தன்மை.
- **Technical Definition**: A characteristic of a system which aims to ensure an agreed level of operational performance, usually uptime (e.g., 99.999% "five nines"), for a higher than normal period.
- **Natural Collocations**: *Achieve high availability, HA cluster, designed for HA, zero-downtime HA*.
- 💻 *Code Review / PR*: *"Ensure the PostgreSQL connection string specifies multiple cluster hosts to support automatic **high availability** failover."*
- 🏗️ *System Design*: *"We achieved multi-region **high availability** by deploying identical Kubernetes pods across US-East and US-West active-active."*
- 🚨 *War Room Incident*: *"Our lack of database **HA** meant that a single disk failure in Frankfurt took our European banking portal down for four hours."*

---

### 6. **Sharding / Partitioning** `[SHAR-ding / pahr-TISH-uh-ning]` *(Noun)*
- **Tamil Meaning**: ஒரு பிரம்மாண்டமான டேட்டாபேஸை ஒரே சர்வரில் அடக்க முடியாமல், பல சர்வர்களில் துண்டு துண்டாகப் பிரித்து சேமிக்கும் உத்தி.
- **Technical Definition**: Horizontally partitioning data across multiple physical database instances or servers, each containing a subset of the total dataset.
- **Natural Collocations**: *Shard key, horizontal sharding, partition the database, cross-shard query*.
- 💻 *Code Review / PR*: *"Avoid cross-shard queries by selecting `tenant_id` as our primary MongoDB **shard key**."*
- 🏗️ *System Design*: *"When our user table crossed 500 million records, relational performance tanked, forcing us to adopt range-based database **sharding**."*
- 🚨 *War Room Incident*: *"An uneven **shard key** caused 80% of write traffic to hit a single database node, causing catastrophic hotspot CPU saturation."*

---

### 7. **Backpressure** `[BAK-presh-er]` *(Noun)*
- **Tamil Meaning**: டேட்டாவை அனுப்பும் வேகத்திற்கு ஈடாக பெறக்கூடிய சர்வரால் ஜீரணிக்க முடியாதபோது, 'கொஞ்சம் மெதுவா அனுப்பு' என்று பின்னோக்கி கொடுக்கும் பாதுகாப்பு சமிக்ஞை.
- **Technical Definition**: A resistance or force opposing the desired flow of data in software, signaling the producer to slow down when the consumer cannot keep up.
- **Natural Collocations**: *Apply backpressure, handle backpressure, reactive streams backpressure, buffer overflow*.
- 💻 *Code Review / PR*: *"Using Project Reactor's `onBackpressureBuffer`, our subscriber safely queues incoming telemetry bursts without blowing JVM heap memory."*
- 🏗️ *System Design*: *"Kafka consumers leverage pull-based polling rather than push-based sockets to naturally enforce consumer-controlled **backpressure**."*
- 🚨 *War Room Incident*: *"The message broker lacked **backpressure** mechanisms, flooding downstream billing workers until they crashed with OutOfMemory errors."*

---

### 8. **Failover** `[FEYL-oh-ver]` *(Noun / Verb)*
- **Tamil Meaning**: முதன்மை சர்வர் பழுதடைந்த அடுத்த விநாடியே, காத்திருப்பில் உள்ள துணை சர்வர் தானாகவே பொறுப்பை ஏற்றுக்கொண்டு தடையின்றி இயங்கும் செயல்முறை.
- **Technical Definition**: An automatic switching to a redundant or standby computer server, system, hardware component, or network upon the failure of the primary.
- **Natural Collocations**: *Automatic failover, graceful failover, initiate failover, failover delay*.
- 💻 *Code Review / PR*: *"Verify that the Redis Sentinel client detects master node heartbeats and executes **failover** within 1.5 seconds."*
- 🏗️ *System Design*: *"Our multi-AZ AWS architecture triggers automated DNS **failover** via Route 53 if health checks fail three consecutive times."*
- 🚨 *War Room Incident*: *"The primary database crashed, but due to a split-brain bug, automatic **failover** to the replica failed to promote a new leader."*

---

### 9. **CQRS (Command Query Responsibility Segregation)** `[see-kyoo-ahr-es]` *(Noun / Pattern)*
- **Tamil Meaning**: தகவல்களை மாற்றும் வேலைகளையும் (Create/Update/Delete) தகவல்களை வாசிக்கும் வேலைகளையும் (Read/Search) தனித்தனி மாடல்களாகவும் டேட்டாபேஸ்களாகவும் பிரிக்கும் உத்தி.
- **Technical Definition**: An architectural pattern that separates read and write operations for a data store into distinct models.
- **Natural Collocations**: *Implement CQRS, CQRS architecture, separate command and query buses*.
- 💻 *Code Review / PR*: *"Let's adopt **CQRS** here: write orders to PostgreSQL for ACID guarantees, and stream events to Elasticsearch for lightning-fast read queries."*
- 🏗️ *System Design*: *"High-throughput e-commerce architectures use **CQRS** because read traffic typically exceeds write traffic by a 50:1 ratio."*

---

### 10. **Eventual Consistency** `[ih-VEN-choo-uhl kuhn-SIS-tuhn-see]` *(Noun)*
- **Tamil Meaning**: டேட்டா மாற்றப்பட்ட உடனேயே அனைத்து சர்வர்களிலும் உடனே தெரியாமல் போகலாம்; ஆனால் சிறிது நேரத்தில் தானாகவே அனைத்து இடங்களிலும் ஒத்திசைந்து சரியாகிவிடும் நிலை.
- **Technical Definition**: A consistency model used in distributed systems where, given that no new updates are made, all replicas will eventually return the last updated value.
- **Natural Collocations**: *Tolerate eventual consistency, eventual consistency delay, BASE model*.
- 💻 *Code Review / PR*: *"Since social media follower counts can tolerate **eventual consistency**, we can update the count asynchronously via Kafka."*
- 🏗️ *System Design*: *"Amazon DynamoDB trades strong immediate consistency for ultra-low latency write speeds using **eventual consistency**."*
- 🚨 *War Room Incident*: *"A customer complained their new profile picture was missing because our read replica suffered a 45-second replication lag under **eventual consistency**."*

---

### 11. **Reverse Proxy** `[ri-VURS PROK-see]` *(Noun)*
- **Tamil Meaning**: இணையத்திற்கும் பின்னால் இருக்கும் பல சர்வர்களுக்கும் இடையே நின்று, உள்வரும் போக்குவரத்தை ஒழுங்குபடுத்தி பாதுகாக்கும் வாயில் காவலன் (Nginx, Envoy, HAProxy).
- **Technical Definition**: A proxy server that appears to clients as an ordinary server, directing incoming client requests to one or more internal backend servers.
- **Natural Collocations**: *Nginx reverse proxy, deploy behind a reverse proxy, reverse proxy routing*.
- 💻 *Code Review / PR*: *"Configure the **reverse proxy** to terminate SSL certificates and gzip-compress JSON responses before returning them to clients."*
- 🏗️ *System Design*: *"Our architecture places Envoy as a **reverse proxy** at the edge to handle SSL termination, authentication, and HTTP/2 multiplexing."*

---

### 12. **Sidecar Pattern** `[SAHYD-kahr PAT-ern]` *(Noun / Pattern)*
- **Tamil Meaning**: மோட்டார் சைக்கிளுடன் இணைக்கப்பட்ட துணை வண்டி போல், முக்கிய அப்ளிகேஷன் கன்டெய்னருடன் கூடவே ஒட்டிக்கொண்டு இயங்கும் துணை கன்டெய்னர் (Logging, Metrics, Proxy).
- **Technical Definition**: A design pattern where a secondary container is attached to a primary application container within a single pod to extend or enhance functionality.
- **Natural Collocations**: *Sidecar container, Envoy sidecar, inject sidecars, sidecar architecture*.
- 💻 *Code Review / PR*: *"Instead of coding OpenTelemetry tracing inside our Java code, let's inject an Envoy **sidecar** to capture network metrics transparently."*
- 🏗️ *System Design*: *"Istio service mesh operates entirely via the **sidecar pattern**, intercepting all inter-service ingress and egress traffic."*

---

### 13. **Ingress & Egress** `[IN-gres / EE-gres]` *(Noun)*
- **Tamil Meaning**:
  - *Ingress*: நெட்வொர்க் அல்லது கிளஸ்டருக்குள் வெளியே இருந்து உள்ளே நுழையும் உள்வரும் போக்குவரத்து.
  - *Egress*: நெட்வொர்க்கிலிருந்து வெளியே செல்லும் வெளிச்செல்லும் போக்குவரத்து.
- **Technical Definition**:
  - *Ingress*: Network traffic entering a system, cluster, or cloud VPC from outside.
  - *Egress*: Network traffic exiting a network perimeter or cloud provider.
- 💻 *Code Review / PR*: *"Define a Kubernetes **Ingress** rule to route `/api/v2/orders` to our newly deployed microservice pod."*
- 🏗️ *System Design*: *"Cloud providers charge zero dollars for data **ingress**, but charge hefty bandwidth fees for inter-region data **egress**."*
- 🚨 *War Room Incident*: *"A misconfigured backup cron job streamed 40TB of raw logs to an external cloud, triggering an unexpected $12,000 **egress** cost spike."*

---

### 14. **Service Mesh** `[SUR-vis mesh]` *(Noun)*
- **Tamil Meaning**: நூற்றுக்கணக்கான மைக்ரோசர்வீஸ்களுக்கு இடையே நடக்கும் அனைத்து உரையாடல்களையும் கண்காணித்து, பாதுகாத்து, வழிநடத்தும் ஒரு அர்ப்பணிக்கப்பட்ட உள்கட்டமைப்பு வலை (Istio, Linkerd).
- **Technical Definition**: A dedicated infrastructure layer for facilitating service-to-service communications between microservices using proxies, typically handling mTLS, telemetry, and retries.
- **Natural Collocations**: *Deploy a service mesh, Istio service mesh, service mesh telemetry*.
- 🏗️ *System Design*: *"Implementing an enterprise **service mesh** provided zero-trust mutual TLS (mTLS) encryption across our 400 microservices without touching application code."*

---

### 15. **Monolith vs. Microservices** `[MON-uh-lith / MAHY-kroh-sur-vis-iz]` *(Noun)*
- **Tamil Meaning**:
  - *Monolith*: அனைத்து அம்சங்களும் ஒரே பெரிய ஒற்றைக் குறியீட்டுக் கோப்பாக இயங்கும் பிரம்மாண்ட அப்ளிகேஷன்.
  - *Microservices*: சிறிய தனித்தனி சுதந்திரமான அப்ளிகேஷன்களாகப் பிரித்து ஒவ்வொன்றும் தனித்தனி டேட்டாபேஸுடன் இயங்கும் கட்டமைப்பு.
- 💻 *Code Review / PR*: *"Let's extract the PDF generation logic from our core **monolith** into a lightweight Go **microservice**."*
- 🏗️ *System Design*: *"Prematurely decomposing a simple system into 30 distributed **microservices** introduces severe network latency and debugging hell."*

---

# 3. Category 2: Data Engineering, Databases & Caching (Words 16–30)

---

### 16. **Cache Stampede / Cache Avalanche** `[kash stam-PEED / AV-uh-lanch]` *(Noun)*
- **Tamil Meaning**: ஒரு பிரபலமான கேச் கீ (Cache Key) ஒரே விநாடியில் எக்ஸ்பைர் (expire) ஆகும்போது, ஒரே நேரத்தில் பல்லாயிரக்கணக்கான கோரிக்கைகள் நேரடியாக டேட்டாபேஸை தாக்கி அதை செயலிழக்கச் செய்யும் பேராபத்து.
- **Technical Definition**: A phenomenon where many concurrent requests miss the cache simultaneously upon TTL expiration, all querying the underlying database at the exact same moment.
- **Natural Collocations**: *Prevent cache stampede, cache stampede mitigation, probabilistic early expiration, mutex locking*.
- 💻 *Code Review / PR*: *"Add randomized TTL jitter (e.g., `300s + rand(30s)`) to our Redis cache keys to prevent a midnight **cache avalanche**."*
- 🏗️ *System Design*: *"We mitigated **cache stampedes** using distributed Redis Mutex locks, ensuring only one worker queries the database while others wait."*
- 🚨 *War Room Incident*: *"When the World Cup live score cache expired, 200,000 requests bypassed Redis in one second, crushing our Postgres database instantly."*

---

### 17. **Deadlock** `[DED-lok]` *(Noun)*
- **Tamil Meaning**: இரண்டு த்ரெட்கள் அல்லது பரிவர்த்தனைகள் (Transactions) ஒன்றுக்கொன்று தேவைப்படும் வளங்களை லாக் செய்து கொண்டு, எவரும் விட்டுக்கொடுக்காமல் நிரந்தரமாக உறைந்து நிற்கும் முட்டுக்கட்டை.
- **Technical Definition**: A situation in concurrent computing where two or more processes are unable to proceed because each is waiting for the other to release a lock.
- **Natural Collocations**: *Database deadlock, thread deadlock, deadlock detection, encounter a deadlock*.
- 💻 *Code Review / PR*: *"Always acquire entity locks in a consistent alphabetical order across all services to eliminate **deadlocks**."*
- 🚨 *War Room Incident*: *"Transaction A locked User 1 and waited for User 2, while Transaction B locked User 2 and waited for User 1, triggering a Postgres **deadlock** error."*

---

### 18. **Connection Pool Exhaustion** `[kuh-NEK-shuhn pool ig-ZAWS-chuhn]` *(Noun)*
- **Tamil Meaning**: டேட்டாபேஸுடன் பேச அனுமதிக்கப்பட்ட இணைப்புகள் அனைத்தும் பிஸியாகி, புதிய கோரிக்கைகள் இணைப்பு கிடைக்காமல் காத்துக் கிடந்து காலாவதியாகும் நிலை.
- **Technical Definition**: A failure state where all available database connections in a pre-allocated pool (e.g., HikariCP) are in use, causing incoming requests to block and time out.
- **Natural Collocations**: *Pool exhaustion, HikariCP connection timeout, leak detection*.
- 💻 *Code Review / PR*: *"Never execute slow external HTTP calls inside a `@Transactional` block; it holds the database connection open and causes **pool exhaustion**."*
- 🚨 *War Room Incident*: *"A slow unindexed query held 50 HikariCP connections for 15 seconds, causing complete **connection pool exhaustion** for all users."*

---

### 19. **Replication Lag** `[rep-li-KEY-shuhn lag]` *(Noun)*
- **Tamil Meaning**: முதன்மை டேட்டாபேஸில் (Primary) செய்யப்பட்ட மாற்றங்கள், படிக்கும் துணை டேட்டாபேஸ்களுக்கு (Read Replicas) சென்று சேர எடுக்கும் கால தாமதம்.
- **Technical Definition**: The time delay between an operation being committed on a primary database instance and that change being reflected on its read replicas.
- **Natural Collocations**: *High replication lag, monitor replication lag, lag spike, read-your-own-writes consistency*.
- 💻 *Code Review / PR*: *"Immediately after a user updates their profile, route the next read to the Primary database to prevent stale data due to **replication lag**."*
- 🚨 *War Room Incident*: *"Under a 10,000 writes/sec burst, MySQL replica lag climbed to 35 seconds, causing customers to see empty shopping carts after payment."*

---

### 20. **CAP Theorem** `[kap THEER-uhm]` *(Noun)*
- **Tamil Meaning**: ஒரு விநியோகிக்கப்பட்ட கணினி அமைப்பில் (Distributed System) ஒரே நேரத்தில் Consistency (துல்லியம்), Availability (கிடைக்கும் தன்மை), Partition Tolerance (நெட்வொர்க் பிளவு தாங்குதல்) ஆகிய மூன்றையும் பெற முடியாது; ஏதேனும் இரண்டை மட்டுமே தேர்ந்தெடுக்க முடியும் என்ற அடிப்படை விதி.
- **Technical Definition**: A theorem in theoretical computer science stating that any distributed data store can simultaneously provide at most two out of three guarantees: Consistency, Availability, and Partition Tolerance.
- **Natural Collocations**: *CAP theorem trade-offs, CP system vs AP system*.
- 🏗️ *System Design*: *"Because network partitions are inevitable across cloud data centers (P), system designers must choose between **CP** (e.g., HBase, Spanner) and **AP** (e.g., Cassandra, DynamoDB)."*

---

### 21. **ACID vs. BASE** `[AS-id / beys]` *(Noun - Acronyms)*
- **Tamil Meaning**:
  - *ACID (Atomicity, Consistency, Isolation, Durability)*: பாரம்பரிய வங்கிகளுக்கு தேவையான 100% சமரசமற்ற உடனடி துல்லியம்.
  - *BASE (Basically Available, Soft state, Eventual consistency)*: உலகளாவிய சமூக வலைத்தளங்களுக்கு தேவையான அதிவேக, நெகிழ்வான அணுகுமுறை.
- 🏗️ *System Design*: *"Relational SQL databases prioritize strict **ACID** transaction semantics, whereas NoSQL architectures embrace **BASE** for massive global horizontal scale."*

---

### 22. **Cold Start** `[kohld stahrt]` *(Noun)*
- **Tamil Meaning**: தூங்கிக்கொண்டிருக்கும் ஒரு சர்வர்லெஸ் பங்ஷன் (AWS Lambda) அல்லது புதிதாகத் தொடங்கும் JVM அப்ளிகேஷன், முதல் கோரிக்கைக்கு பதில் அளிக்க எடுக்கும் ஆரம்பக்கட்ட தொடக்க தாமதம்.
- **Technical Definition**: The latency penalty experienced when a serverless function or container must be initialized from scratch to serve its first invocation.
- **Natural Collocations**: *Suffer from cold starts, minimize cold start latency, provisioned concurrency*.
- 💻 *Code Review / PR*: *"Migrating from a heavyweight Spring Boot JVM image to a GraalVM native binary reduced our AWS Lambda **cold start** from 4.2 seconds to 80 milliseconds."*
- 🚨 *War Room Incident*: *"Traffic spiked after a marketing push; AWS spun up 200 new Lambda containers whose collective **cold starts** caused widespread HTTP 504 gateway timeouts."*

---

### 23. **Write-Through vs. Cache-Aside** `[rahyt-throo / kash uh-SAHYD]` *(Noun / Pattern)*
- **Tamil Meaning**:
  - *Cache-Aside*: அப்ளிகேஷன் முதலில் கேச்சில் பார்க்கும்; இல்லை என்றால் டேட்டாபேஸில் எடுத்து கேச்சில் போடும்.
  - *Write-Through*: அப்ளிகேஷன் கேச்சில் மட்டுமே எழுதும்; கேச்சே பின்னணியில் டேட்டாபேஸில் எழுதிக்கொள்ளும்.
- 💻 *Code Review / PR*: *"Our microservice implements the **Cache-Aside** pattern using Spring's `@Cacheable` annotation over our Redis cluster."*

---

### 24. **Change Data Capture (CDC)** `[cheynj DEY-tuh KAP-cher]` *(Noun)*
- **Tamil Meaning**: டேட்டாபேஸில் நடக்கும் ஒவ்வொரு Insert, Update, Delete மாற்றங்களையும் உடனடியாகப் பிடித்து ஸ்ட்ரீமிங் நிகழ்வாக (Events) மாற்றும் தொழில்நுட்பம் (Debezium).
- **Technical Definition**: A set of software design patterns used to determine and track the data that has changed so that action can be taken using the changed data.
- **Natural Collocations**: *Deploy CDC pipeline, Debezium CDC, stream CDC events to Kafka*.
- 🏗️ *System Design*: *"We use Debezium for **Change Data Capture (CDC)** on PostgreSQL to replicate updated product inventories directly into Elasticsearch in real time."*

---

### 25. **Database Indexing (B-Tree vs. LSM-Tree)** `[BEE-tree / el-es-em tree]` *(Noun)*
- **Tamil Meaning**: புத்தகத்தின் பின்னால் இருக்கும் அகரவரிசைப் பட்டியல் போல், டேட்டாவை மிக வேகமாக தேடிக் கண்டுபிடிக்க உதவும் சிறப்பு தரவுக் கட்டமைப்பு.
- **Technical Definition**: Data structures used to quickly locate and access data in a database without having to search every row. B-Trees optimize for random reads; Log-Structured Merge (LSM) Trees optimize for heavy sequential write throughput.
- 💻 *Code Review / PR*: *"Add a composite B-Tree **index** on `(tenant_id, created_at)` to accelerate our dashboard analytics query from 4,000ms to 6ms."*
- 🏗️ *System Design*: *"Cassandra and RocksDB use **LSM-Trees** to achieve phenomenal write throughput for time-series logging data."*

---

### 26. **Optimistic vs. Pessimistic Locking** `[op-tuh-MIS-tik / pes-uh-MIS-tik LOK-ing]` *(Noun)*
- **Tamil Meaning**:
  - *Optimistic Locking*: முரண்பாடுகள் வராது என நம்பி லாக் செய்யாமல், பதிவின் வெர்ஷன் எண்ணை (`@Version`) வைத்து சேமிக்கும் போது சரிபார்த்தல்.
  - *Pessimistic Locking*: முரண்பாடுகள் நிச்சயம் வரும் என பயந்து, வரிசையை வாசிக்கும் போதே முழுமையாக லாக் செய்தல் (`SELECT FOR UPDATE`).
- 💻 *Code Review / PR*: *"For high-concurrency ticket reservations, use **Pessimistic Locking** to ensure no two users can book the exact same airline seat."*

---

### 27. **Hotspotting** `[HOT-spot-ing]` *(Noun)*
- **Tamil Meaning**: கிளஸ்டரில் பல சர்வருகள் இருந்தாலும், ஒரே ஒரு நோடில் மட்டும் அளவுக்கு அதிகமான டிராஃபிக் குவிந்து அது மட்டும் வெப்பமடைந்து திணறும் நிலை.
- **Technical Definition**: A condition in distributed databases where an uneven distribution of data or access patterns causes disproportionate load on a single partition or node.
- **Natural Collocations**: *Partition hotspotting, avoid hotspotting, hotspot key*.
- 🚨 *War Room Incident*: *"Using today's date as the partition key caused acute **hotspotting**; all current writes slammed partition #4 while the other 31 partitions sat idle."*

---

### 28. **Polling vs. WebSockets vs. SSE** `[POHL-ing / WEB-sok-its / es-es-ee]` *(Noun)*
- **Tamil Meaning**:
  - *Polling*: 'பதில் வந்தாச்சா?' என்று வாடிக்கையாளர் மீண்டும் மீண்டும் சர்வரைக் கேட்டுக்கொண்டே இருப்பது.
  - *WebSockets*: இருபுறமும் முழுமையாகத் திறந்திருக்கும் இருவழித் தொலைபேசி இணைப்பு (Two-way full duplex).
  - *SSE (Server-Sent Events)*: சர்வரில் இருந்து வாடிக்கையாளருக்கு மட்டுமே செய்திகள் வந்து விழும் ஒருவழி அறிவிப்பு இணைப்பு.
- 🏗️ *System Design*: *"For real-time stock ticker prices, **Server-Sent Events (SSE)** are far simpler and more lightweight than full **WebSockets**."*

---

### 29. **Dirty Read vs. Phantom Read** `[DUR-tee reed / FAN-tuhm reed]` *(Noun)*
- **Tamil Meaning**:
  - *Dirty Read*: மற்றொரு பரிவர்த்தனை இன்னும் Commit செய்யாத தற்காலிக தவறான டேட்டாவை வாசிப்பது.
  - *Phantom Read*: நாம் ஒருமுறை வாசித்த பிறகு, மற்றொருவர் புதிய வரிகளை சேர்த்துவிட்டு Commit செய்ததால் இரண்டாவது முறை வாசிக்கும் போது புதிய வரிகள் தெரிவது.
- 💻 *Code Review / PR*: *"Set our isolation level to `READ_COMMITTED` to prevent catastrophic financial **dirty reads**."*

---

### 30. **WAL (Write-Ahead Logging)** `[rahyt uh-HED LOG-ing]` *(Noun)*
- **Tamil Meaning**: டேட்டாபேஸ் டேபிளில் எழுதும் முன், விபத்து நடந்தால் மீட்டெடுக்க ஏதுவாக மாற்றங்களை முதலில் வரிசையாக ஒரு லாக் பைலில் குறித்து வைக்கும் உத்தி.
- **Technical Definition**: A family of techniques for providing atomicity and durability in database systems, writing all modifications to a sequential log before applying them to data pages.
- 🏗️ *System Design*: *"PostgreSQL relies on **Write-Ahead Logging (WAL)** to guarantee that uncommitted in-memory pages can be reconstructed following an abrupt server reboot."*

---

# 4. Category 3: Concurrency, Performance & Memory Mechanics (Words 31–45)

---

### 31. **Race Condition** `[REYS kuhn-DISH-uhn]` *(Noun)*
- **Tamil Meaning**: இரண்டு த்ரெட்கள் ஒரே டேட்டாவை ஒரே நேரத்தில் மாற்ற முயன்று, யார் முதலில் முடித்தார்கள் என்பதைப் பொறுத்து எதிர்பாராத விசித்திரமான தவறான முடிவுகள் உண்டாகும் நிலை.
- **Technical Definition**: An undesirable situation that occurs when a device or system attempts to perform two or more operations at the same time, but because of the nature of the device or system, the operations must be done in the proper sequence to be done correctly.
- **Natural Collocations**: *Encounter a race condition, prevent race conditions, subtle race condition, atomic operation*.
- 💻 *Code Review / PR*: *"This non-atomic `count++` operation has a severe **race condition**; replace it with `AtomicInteger.incrementAndGet()`."*
- 🚨 *War Room Incident*: *"A **race condition** in our promo voucher redemption logic allowed 400 users to exploit a one-time coupon simultaneously."*

---

### 32. **Memory Leak** `[MEM-uh-ree leek]` *(Noun)*
- **Tamil Meaning**: பயன்பாடு முடிந்த பிறகும் மெமரியை விடுவிக்காமல் பிடித்து வைத்திருப்பதால், ரேம் (RAM) மெதுவாக நிரம்பி இறுதியில் அப்ளிகேஷன் முழுமையாக கிராஷ் ஆகும் நிலை.
- **Technical Definition**: A failure in a program to release discarded memory, causing impaired performance or failure due to an eventual OutOfMemoryError.
- **Natural Collocations**: *Diagnose a memory leak, severe memory leak, heap dump analysis, leak unclosed resources*.
- 💻 *Code Review / PR*: *"Always close database `ResultSet` and `Statement` objects in a `try-with-resources` block to avoid slow JVM **memory leaks**."*
- 🚨 *War Room Incident*: *"A static HashMap holding unevicted user sessions created a **memory leak** that crashed our payment microservices every 72 hours."*

---

### 33. **Garbage Collection (GC) Pause / Stop-The-World** `[STAH-the-world]` *(Noun)*
- **Tamil Meaning**: பயன்பாட்டில் இல்லாத குப்பையான மெமரியை சுத்தம் செய்வதற்காக, அப்ளிகேஷனின் அனைத்து த்ரெட்களையும் சில நொடிகள் மொத்தமாக உறைய வைக்கும் JVM செயல்முறை.
- **Technical Definition**: An execution pause in managed runtimes (like the JVM) where all application threads are halted while the garbage collector collects memory and compacts the heap.
- **Natural Collocations**: *Stop-The-World pause, GC pause latency, tune garbage collector, G1GC / ZGC*.
- 💻 *Code Review / PR*: *"Migrating our financial order-matching engine to Java 21 with the **ZGC** collector eliminated our 800ms **Stop-The-World** latency spikes."*
- 🚨 *War Room Incident*: *"Our 32GB JVM heap triggered a full garbage collection cycle, freezing all user requests for 6 full seconds and causing load balancers to mark the servers dead."*

---

### 34. **P99 / P95 Latency** `[PEE-ninety-nine LEY-tuhn-see]` *(Noun)*
- **Tamil Meaning**: சராசரி வேகத்தை (Average) நம்பாமல், மிகவும் தாமதமான கடைசி 1% பயனர்கள் சந்தித்த மிக மோசமான தாமத நேரம்.
- **Technical Definition**: The 99th percentile of response time, indicating that 99% of requests are faster than this number, while the slowest 1% experience this latency or worse.
- **Natural Collocations**: *P99 latency SLA, reduce P99 latency, tail latency, P50 vs P99*.
- 💻 *Code Review / PR*: *"While our average latency is a respectable 25ms, our **P99 latency** spikes to 3,400ms due to unindexed database queries."*
- 🏗️ *System Design*: *"Our enterprise SLA contractually guarantees sub-100ms **P99 latency** across all authenticated REST endpoints."*

---

### 35. **Throughput vs. Latency** `[THROO-poot vs LEY-tuhn-see]` *(Noun)*
- **Tamil Meaning**:
  - *Latency*: ஒரு தனி கோரிக்கை சென்று பதில் வர ஆகும் கால தாமதம் (Time per request).
  - *Throughput*: ஒரு குறிப்பிட்ட விநாடியில் அந்த சிஸ்டம் ஒட்டுமொத்தமாக எத்தனை ஆயிரம் கோரிக்கைகளை கையாள்கிறது என்ற உற்பத்தி திறன் (Requests per second).
- 🏗️ *System Design*: *"A bullet train may have high **latency** (takes 2 hours to arrive), but phenomenal **throughput** (transports 1,500 people per trip)."*

---

### 36. **Concurrency vs. Parallelism** `[kuhn-KUR-uhn-see vs PAIR-uh-lel-iz-uhm]` *(Noun)*
- **Tamil Meaning**:
  - *Concurrency*: ஒரே நேரத்தில் பல வேலைகளை நிர்வகித்தல் (Dealing with lots of things at once - e.g., single CPU juggling tasks).
  - *Parallelism*: ஒரே நேரத்தில் பல வேலைகளை நிஜமாகவே வெவ்வேறு கோர்களில் பிரித்து ஒரே கணத்தில் செய்தல் (Doing lots of things at once).
- 💻 *Code Review / PR*: *"Go channels provide elegant **concurrency** primitives, allowing thousands of goroutines to run concurrently on a handful of OS threads."*

---

### 37. **Thread Pool Starvation** `[THRED pool stahr-VEY-shuhn]` *(Noun)*
- **Tamil Meaning**: த்ரெட் பூலில் உள்ள அனைத்து த்ரெட்களும் நீண்ட நேரம் பதிலளிக்காத பிற சர்வர்களுக்காக காத்துக் கிடப்பதால், புதிய சாதாரண கோரிக்கைகளைக் கூட இயக்க த்ரெட் இல்லாமல் சிஸ்டம் செயலிழக்கும் நிலை.
- **Technical Definition**: A condition where all threads in a pool are blocked waiting for slow I/O or downstream dependencies, leaving no threads available to process incoming tasks.
- 🚨 *War Room Incident*: *"A hung payment gateway blocked all 200 Tomcat worker threads, causing total **thread pool starvation** across the entire web application."*

---

### 38. **Mutex / Semaphore** `[MYOO-teks / SEM-uh-fawr]` *(Noun)*
- **Tamil Meaning**:
  - *Mutex (Mutual Exclusion)*: ஒரே ஒரு த்ரெட் மட்டுமே ஒரே நேரத்தில் அணுகக்கூடிய பிரத்தியேக சாவி.
  - *Semaphore*: ஒரே நேரத்தில் அனுமதிக்கப்பட்ட குறிப்பிட்ட எண்ணிக்கையிலான (எ.கா. 5) த்ரெட்கள் மட்டுமே அணுக அனுமதிக்கும் டோக்கன் பாஸ்.
- 💻 *Code Review / PR*: *"Use a **Mutex** to protect the counter variable from simultaneous concurrent writes."*

---

### 39. **Non-Blocking I/O (NIO)** `[non-BLOK-ing eye-oh]` *(Noun)*
- **Tamil Meaning**: டேட்டா நெட்வொர்க்கில் இருந்து வரும் வரை த்ரெட்டை சும்மா உட்கார வைக்காமல், டேட்டா வரும் வரை வேறு வேலைகளை செய்ய அனுமதிக்கும் அதிநவீன உள்ளீட்டு வெளியீட்டு முறை (Netty, Node.js).
- **Technical Definition**: A form of input/output processing that permits other processing to continue before the transmission has finished, utilizing event loops and selectors.
- 🏗️ *System Design*: *"Node.js and Spring WebFlux achieve massive concurrency on minimal RAM by relying on **Non-Blocking I/O** event loops."*

---

### 40. **Context Switching** `[KON-tekst SWICH-ing]` *(Noun)*
- **Tamil Meaning**: சிபியூ (CPU) ஒரு த்ரெட்டில் இருந்து மற்றொரு த்ரெட்டிற்கு மாறும் போது, முந்தைய த்ரெட்டின் நிலையை சேமித்து புதியதை ஏற்றுவதற்கு எடுக்கும் மறைமுக சுமை.
- **Technical Definition**: The process of storing the state of a process or thread so that it can be restored and resume execution at a later point, incurring CPU overhead.
- 💻 *Code Review / PR*: *"Spawning 5,000 native OS threads caused devastating CPU **context switching** overhead; switch to Java 21 Virtual Threads."*

---

### 41. **CPU Throttling** `[see-pyoo THROHT-ling]` *(Noun)*
- **Tamil Meaning**: ஒரு கன்டெய்னர் (Docker/K8s) தனக்கு நிர்ணயிக்கப்பட்ட சிபியூ வரம்பைத் தாண்டும் போது, ஆப்பரேட்டிங் சிஸ்டம் அதன் வேகத்தை செயற்கையாக குறைத்து நசுக்கும் செயல்முறை.
- **Technical Definition**: The intentional slowing down of CPU execution by the OS kernel or Kubernetes cgroups when a process exceeds its assigned CPU limits.
- 🚨 *War Room Incident*: *"Our pod latency doubled during the sale because Kubernetes enforced severe **CPU throttling** whenever our pod hit its 1.0 CPU limit."*

---

### 42. **Thread Contention** `[THRED kuhn-TEN-shuhn]` *(Noun)*
- **Tamil Meaning**: ஒரே ஒரு பொதுவான லாக்-ஐ பெறுவதற்காக நூற்றுக்கணக்கான த்ரெட்கள் ஒன்றோடொன்று மோதிக்கொண்டு வரிசையில் காத்துக்கிடக்கும் நிலை.
- **Technical Definition**: A condition that occurs when one thread is waiting for a lock held by another thread, degrading parallel performance.
- 💻 *Code Review / PR*: *"Replace the heavily contested `synchronized` block with `ConcurrentHashMap` stripes to slash **thread contention**."*

---

### 43. **Graceful Degradation** `[GREYS-fuhl deg-ruh-DEY-shuhn]` *(Noun)*
- **Tamil Meaning**: கடுமையான சர்வர் சுமை அல்லது சில பாகங்கள் பழுதாகும் போது, ஒட்டுமொத்தமாக கிராஷ் ஆகாமல், அத்தியாவசியமற்ற சில அம்சங்களை மட்டும் முடக்கிவிட்டு முக்கிய அம்சங்களை தொடர்ந்து இயங்க வைக்கும் நளினமான அணுகுமுறை.
- **Technical Definition**: The ability of a computer, machine, or system to maintain limited functionality even when a large portion of it has been destroyed or rendered inoperative.
- 🏗️ *System Design*: *"Under extreme Black Friday traffic, our app enters **graceful degradation**: personalized recommendations turn off, but checkout remains 100% operational."*

---

### 44. **Telemetry (Metrics, Logs, Traces)** `[tuh-LEM-i-tree]` *(Noun)*
- **Tamil Meaning**: அப்ளிகேஷன் உள்ளே என்ன நடக்கிறது என்பதை தூரத்திலிருந்தே துல்லியமாக கண்காணிப்பதற்காக தானாக சேகரிக்கப்படும் அளவீடுகள், பதிவுகள் மற்றும் பாதை விவரங்கள்.
- **Technical Definition**: The automated recording and transmission of data from remote systems to an observability backend for monitoring and diagnostics.
- 💻 *Code Review / PR*: *"Ensure our OpenTelemetry agent propagates W3C trace context headers across all outbound Feign client calls to maintain continuous distributed **telemetry**."*

---

### 45. **Heap vs. Stack Memory** `[heep vs stak MEM-uh-ree]` *(Noun)*
- **Tamil Meaning**:
  - *Stack*: மிக வேகமானது; ஒவ்வொரு த்ரெட்டின் மெத்தட் கால்கள் (Method calls) மற்றும் லோக்கல் மாறிகளை சேமிக்க பயன்படுவது.
  - *Heap*: அனைத்து ஆப்ஜெக்டுகளும் (Objects) உருவாக்கப்பட்டு, கர்பேஜ் கலெக்டரால் நிர்வகிக்கப்படும் பொதுவான பெரிய நினைவகப் பகுதி.
- 💻 *Code Review / PR*: *"Primitive variables and references live on the **Stack**, whereas the actual instantiated objects reside on the JVM **Heap**."*

---

# 5. Category 4: Cloud Infrastructure, DevOps & Reliability (Words 46–60)

---

### 46. **Blue-Green vs. Canary Deployment** `[bloo-green / kuh-NAIR-ee dih-PLOY-muhnt]` *(Noun)*
- **Tamil Meaning**:
  - *Blue-Green*: இரண்டு முழுமையான சர்வர் சூழல்களை வைத்துக்கொண்டு (Blue-பழையது, Green-புதியது), சுவிட்ச் போடுவது போல் டிராஃபிக்கை நொடியில் மாற்றுதல்.
  - *Canary*: புதிய கோடை முதலில் வெறும் 2% பயனர்களுக்கு மட்டும் கொடுத்து சோதித்து, பிழைகள் இல்லை என்பதை உறுதி செய்த பின் அனைவருக்கும் விரிவுபடுத்துதல்.
- 🏗️ *System Design*: *"We use **Canary deployments** via Argo Rollouts, routing 5% of production traffic to the new release for 30 minutes before full promotion."*
- 🚨 *War Room Incident*: *"Thanks to our **Canary deployment** strategy, a fatal NullPointerException only affected 1% of users before automated rollback triggered."*

---

### 47. **Infrastructure as Code (IaC)** `[IN-fruh-struhk-cher az kohd]` *(Noun)*
- **Tamil Meaning**: கிளவுட் சர்வர்கள் மற்றும் நெட்வொர்க்குகளை மேனுவலாக மவுஸ் கிளிக் செய்து உருவாக்காமல், டெராபார்ம் (Terraform) போன்ற கோடுகளாக எழுதி தானியக்கமாக்குதல்.
- **Technical Definition**: The managing and provisioning of computer data centers through machine-readable definition files, rather than physical hardware configuration or interactive configuration tools.
- **Natural Collocations**: *Terraform IaC, define infrastructure as code, IaC drift*.
- 💻 *Code Review / PR*: *"Never create AWS S3 buckets manually via the web console; declare them in our Terraform **IaC** repository."*

---

### 48. **Immutable Infrastructure** `[ih-MYOO-tuh-buhl IN-fruh-struhk-cher]` *(Noun)*
- **Tamil Meaning**: இயங்கிக்கொண்டிருக்கும் லைவ் சர்வரில் புகுந்து மேனுவலாக எதையும் திருத்தக்கூடாது; ஏதேனும் மாற்றம் வேண்டுமென்றால் புதிய இமேஜை பில்ட் செய்து பழையதை அழித்துவிட்டு புதிதாகத்தான் போட வேண்டும் என்ற கொள்கை.
- **Technical Definition**: An infrastructure paradigm in which servers or virtual machines are never modified after they are deployed; updates require building and deploying an entirely new instance.
- 🏗️ *System Design*: *"Docker containers enforce **immutable infrastructure**; we never SSH into running production containers to patch software."*

---

### 49. **Observability** `[uhb-zur-vuh-BIL-i-tee]` *(Noun)*
- **Tamil Meaning**: ஒரு சிஸ்டத்தின் வெளித்தோற்ற அறிகுறிகளையும் அவுட்புட்டையும் மட்டுமே பார்த்து, அதன் உள்ளே என்ன பிரச்சனை நடக்கிறது என்பதை முழுமையாக உய்த்துணரும் திறன் (Logs, Metrics, Traces).
- **Technical Definition**: A measure of how well internal states of a system can be inferred from knowledge of its external outputs.
- **Natural Collocations**: *Full-stack observability, observability pipeline, Datadog observability*.
- 🏗️ *System Design*: *"Without distributed tracing, diagnosing microservice network latency across 80 asynchronous hops is impossible; **observability** is paramount."*

---

### 50. **Zero Trust Architecture** `[ZEER-oh truhst AHR-ki-tek-cher]` *(Noun)*
- **Tamil Meaning**: 'அலுவலக நெட்வொர்க்கிற்குள் வந்துவிட்டான் என்பதால் யாரையும் தானாக நம்பாதே; ஒவ்வொரு கோரிக்கையையும் மீண்டும் மீண்டும் சரிபார்த்து உறுதி செய்' என்ற அதிரடி பாதுகாப்பு தத்துவம்.
- **Technical Definition**: A security framework requiring all users and devices, whether in or outside the organization's network, to be continuously authenticated, authorized, and validated.
- 🏗️ *System Design*: *"Under our **Zero Trust Architecture**, internal microservice-to-microservice traffic must present cryptographic mTLS certificates and JWT tokens."*

---

### 51. **Rolling Update** `[ROH-ling UHP-deyt]` *(Noun)*
- **Tamil Meaning**: அனைத்து சர்வர்களையும் ஒரே நேரத்தில் நிறுத்தாமல், ஒவ்வொரு சர்வராக படிப்படியாக புதிய பதிப்பிற்கு மாற்றி பூஜ்ஜிய டவுன்டைமில் அப்ளிகேஷனை அப்டேட் செய்யும் முறை.
- **Technical Definition**: A deployment strategy that replaces previous versions of an application with a new version without zero downtime by iteratively updating pod instances.
- 💻 *Code Review / PR*: *"Set `maxSurge: 25%` and `maxUnavailable: 0` in our Kubernetes deployment spec to ensure safe zero-downtime **rolling updates**."*

---

### 52. **Liveness vs. Readiness Probes** `[LAHYV-nis vs RED-ee-nis prohbz]` *(Noun)*
- **Tamil Meaning**:
  - *Liveness Probe*: 'சர்வர் உயிரோடு இருக்கிறதா?' (இறந்துவிட்டால் உடனே கொன்றுவிட்டு புதியதை ரீஸ்டார்ட் செய்).
  - *Readiness Probe*: 'சர்வர் டிராஃபிக்கை ஏற்க தயாராக உள்ளதா?' (டேட்டாபேஸ் லோட் ஆகும் வரை டிராஃபிக்கை உள்ளே அனுப்பாதே).
- 💻 *Code Review / PR*: *"Don't check database connections inside a **Liveness Probe**; if the database blips, Kubernetes will mistakenly slaughter all healthy app pods!"*

---

### 53. **Horizontal vs. Vertical Scaling** `[hawr-uh-ZON-tl vs VUR-ti-kuhl SKEY-ling]` *(Noun)*
- **Tamil Meaning**:
  - *Vertical Scaling (Scale Up)*: இருக்கும் ஒரே சர்வருக்கு கூடுதல் ரேம் மற்றும் சிபியூ-வை திணித்து பெரிதாக்குதல்.
  - *Horizontal Scaling (Scale Out)*: அதே போல் பல சிறிய சர்வர்களை அடுத்தடுத்து சேர்த்து பட்டாளமாக வேலைகளை பகிர்ந்தளித்தல்.
- 🏗️ *System Design*: *"Vertical scaling has a hard hardware ceiling and creates a single point of failure; **horizontal scaling** via Kubernetes pods provides infinite elasticity."*

---

### 54. **Secret Management / Key Vault** `[SEE-krit MAN-ij-muhnt]` *(Noun)*
- **Tamil Meaning**: பாஸ்வேர்டுகள், ஏபிஐ சாவிகள் மற்றும் என்கிரிப்ஷன் சான்றிதழ்களை கோடில் ஹார்ட்கோட் செய்யாமல், பாதுகாப்பான பிரத்தியேக பெட்டகத்தில் மறைத்து நிர்வகிக்கும் முறை (HashiCorp Vault, AWS Secrets Manager).
- **Natural Collocations**: *HashiCorp Vault, automated secret rotation, inject secrets at runtime*.
- 💻 *Code Review / PR*: *"Never commit database passwords to Git; fetch credentials dynamically at startup from AWS Secrets Manager."*

---

### 55. **Cascading Failure** `[kas-KEY-ding FEYL-yer]` *(Noun)*
- **Tamil Meaning**: சீட்டுக்கட்டு சரிவது போல், ஒரு சிறிய சர்வர் பழுதானதால் ஏற்பட்ட சுமை அடுத்தடுத்த சர்வர்கள் மீது விழுந்து வரிசையாக மொத்த அமைப்பும் நொறுங்கி வீழும் பேரழிவு.
- **Technical Definition**: A failure in a system of interconnected parts in which the failure of a part can trigger the failure of successive parts.
- 🚨 *War Room Incident*: *"When service C slowed down, service B exhausted its thread pool, which subsequently crashed the API gateway, triggering an enterprise-wide **cascading failure**."*

---

### 56. **Autoscaling (HPA)** `[AW-toh-skey-ling]` *(Noun)*
- **Tamil Meaning**: டிராஃபிக் கூடும்போது தானாகவே புதிய சர்வர்களை உருவாக்கி, டிராஃபிக் குறைந்ததும் தானாகவே அவற்றை அழித்து செலவை குறைக்கும் தானியங்கி உத்தி.
- **Technical Definition**: A cloud computing feature that automatically adjusts the number of active computing resources in a server farm according to current workload.
- 🏗️ *System Design*: *"Our Kubernetes Horizontal Pod Autoscaler (**HPA**) scales our checkout squad pods from 10 to 80 instances whenever CPU utilization exceeds 70%."*

---

### 57. **Chaos Engineering** `[KEY-os en-juh-NEER-ing]` *(Noun)*
- **Tamil Meaning**: தயாரிப்பு சூழலில் எதிர்பாராத விபத்துகள் ஏற்படும் போது சிஸ்டம் தாக்குப்பிடிக்குமா என்பதை முன்கூட்டியே சோதிப்பதற்காக, திட்டமிட்டு வேண்டுமென்றே சர்வர்களை கழட்டிவிட்டு சேதம் உண்டாக்கி சோதிக்கும் நடைமுறை (Chaos Monkey).
- **Technical Definition**: The discipline of experimenting on a distributed software system to build confidence in the system's capability to withstand turbulent conditions in production.
- 🏗️ *System Design*: *"Netflix runs Chaos Monkey in live production to randomly terminate AWS EC2 instances, proving that their automated failover architecture actually works."*

---

### 58. **SLA vs. SLO vs. SLI** `[es-el-ey / es-el-oh / es-el-eye]` *(Noun - Acronyms)*
- **Tamil Meaning**:
  - *SLI (Indicator)*: நாம் தற்போது நிஜமாக அளவிடும் தற்போதைய செயல்திறன் எண் (எ.கா. 99.92% uptime).
  - *SLO (Objective)*: நமது இன்ஜினியரிங் டீம் அடைய நினைக்கும் உள்கட்டமைப்பு இலக்கு (எ.கா. 99.95%).
  - *SLA (Agreement)*: வாடிக்கையாளரிடம் ஒப்புக்கொண்டு கையெழுத்திட்ட சட்டப்பூர்வ ஒப்பந்தம்; தவறினால் அபராதம் கட்ட வேண்டும்.
- 👔 *Executive / Management*: *"Our SLI shows 99.98% availability, which comfortably satisfies our internal SLO of 99.95% and prevents violating our client **SLA**."*

---

### 59. **DaemonSet / StatefulSet** `[DEE-muhn set / STEYT-fuhl set]` *(Noun)*
- **Tamil Meaning**:
  - *DaemonSet*: கிளஸ்டரில் உள்ள ஒவ்வொரு சர்வரிலும் கட்டாயமாக ஒரு பிரதி இயங்க வேண்டும் என்ற கட்டளை (Log collectors, Monitoring agents).
  - *StatefulSet*: தரவுகளை சேமிக்கும் டேட்டாபேஸ் போன்ற நிலையான ஐடி மற்றும் டிஸ்க் தேவைப்படும் சர்வர்களுக்கான பணி அமைப்பு.
- 💻 *Code Review / PR*: *"Deploy the Datadog monitoring agent as a Kubernetes **DaemonSet** so every physical worker node automatically runs one monitoring pod."*

---

### 60. **Container Orchestration** `[kuhn-TEY-ner awr-kuh-STREY-shuhn]` *(Noun)*
- **Tamil Meaning**: ஆயிரக்கணக்கான டாக்கர் கன்டெய்னர்களை எங்கே ஏற்றுவது, எப்படி இணைப்பது, எப்போது ரீஸ்டார்ட் செய்வது என்பதை மேஸ்ட்ரோ போல் ஒருங்கிணைத்து இயக்கும் மென்பொருள் (Kubernetes).
- **Technical Definition**: The automated management, coordination, scaling, and networking of computer containers.

---

# 6. Category 5: Web Protocols, APIs & Network Security (Words 61–75)

---

### 61. **Payload** `[PEY-lohd]` *(Noun)*
- **Tamil Meaning**: ஹெட்டர்கள், ஐபி முகவரிகள் போன்ற நெட்வொர்க் வழித்தட விபரங்களை நீக்கிவிட்டு, உண்மையில் நாம் அனுப்ப நினைத்த அசல் சரக்கு / டேட்டா (Body content).
- **Technical Definition**: The actual cargo or essential data carried in an HTTP request or response body, excluding surrounding metadata or headers.
- **Natural Collocations**: *JSON payload, payload size, encrypt the payload, validate payload schema*.
- 💻 *Code Review / PR*: *"Reject requests with HTTP 413 Payload Too Large if the uploaded image **payload** exceeds 10 megabytes."*
- 🚨 *War Room Incident*: *"A client sent an uncompressed 200MB JSON **payload** that consumed 1.5GB of memory during Jackson deserialization, triggering an OOM crash."*

---

### 62. **Serialization vs. Deserialization** `[seer-ee-uh-luh-ZEY-shuhn]` *(Noun)*
- **Tamil Meaning**:
  - *Serialization*: மெமரியில் இருக்கும் ஆப்ஜெக்ட்டை நெட்வொர்க்கில் அனுப்பக்கூடிய பைட் ஸ்ட்ரீம் அல்லது JSON ஸ்ட்ரிங்காக மாற்றுதல்.
  - *Deserialization*: நெட்வொர்க்கில் இருந்து வந்த JSON ஸ்ட்ரிங்கை மீண்டும் மெமரி ஆப்ஜெக்ட்டாக உருமாற்றுதல்.
- 💻 *Code Review / PR*: *"Avoid Java native `ObjectInputStream` **deserialization** due to well-known Remote Code Execution (RCE) security gadget vulnerabilities."*

---

### 63. **CORS (Cross-Origin Resource Sharing)** `[kawrz]` *(Noun)*
- **Tamil Meaning**: பிரவுசர் பாதுகாப்பிற்காக, ஒரு டொமைனில் உள்ள ஜாவாஸ்கிரிப்ட் மற்றொரு டொமைனில் இருக்கும் ஏபிஐ-யை தன்னிச்சையாக திருட்டுத்தனமாக அழைப்பதை தடுக்கும் பாதுகாப்பு விதி.
- **Technical Definition**: An HTTP-header based mechanism that allows a server to indicate any origins (domain, scheme, or port) other than its own from which a browser should permit loading resources.
- **Natural Collocations**: *CORS error, configure CORS headers, Access-Control-Allow-Origin, preflight OPTIONS request*.
- 💻 *Code Review / PR*: *"Never set `Access-Control-Allow-Origin: *` in production when credentials like cookies are enabled; specify explicit authorized domains."*
- 🚨 *War Room Incident*: *"Frontend developers woke up to broken staging builds because our new gateway deployment omitted the `Access-Control-Allow-Headers` **CORS** configuration."*

---

### 64. **CSRF (Cross-Site Request Forgery)** `[see-es-ahr-ef]` *(Noun)*
- **Tamil Meaning**: பயனர் அறியாமல், அவர் லாக்-இன் செய்துள்ள வங்கியின் அதே அமர்வை (Session/Cookies) பயன்படுத்தி போலி இணையதளம் ஒன்று திருட்டுத்தனமாக பணத்தை அனுப்ப வைக்கும் இணையத் தாக்குதல்.
- **Technical Definition**: An attack that forces an authenticated end user to execute unwanted actions on a web application in which they are currently authenticated.
- **Natural Collocations**: *CSRF token, prevent CSRF, SameSite cookie attribute, CSRF vulnerability*.
- 💻 *Code Review / PR*: *"Since our mobile and React clients use stateless JWT bearer tokens in the `Authorization` header rather than ambient cookies, we can safely disable **CSRF** protection."*

---

### 65. **XSS (Cross-Site Scripting)** `[ex-es-es]` *(Noun)*
- **Tamil Meaning**: இணையதளத்தில் உள்ள உள்ளீட்டுப் பெட்டியில் (Input box) ஒரு விஷமி தீங்கிழைக்கும் ஜாவாஸ்கிரிப்ட் கோடை தட்டச்சு செய்து, மற்ற பயனர்களின் செஷன் குக்கீகளை திருடும் தாக்குதல்.
- **Technical Definition**: A security vulnerability that enables attackers to inject malicious client-side scripts into web pages viewed by other users.
- **Natural Collocations**: *Stored XSS, Reflected XSS, sanitize user input, HTML escaping*.
- 💻 *Code Review / PR*: *"Always sanitize and HTML-escape user forum comments before rendering them in the DOM to prevent **XSS** injection."*

---

### 66. **mTLS (Mutual TLS)** `[em-tee-el-es]` *(Noun)*
- **Tamil Meaning**: கிளைன்ட் மட்டுமே சர்வரை சரிபார்க்காமல், சர்வரே திரும்பி கிளைன்ட்டின் டிஜிட்டல் சான்றிதழையும் சரிபார்த்து இருதரப்பும் ஒன்றுக்கொன்று 100% பாதுகாப்பை உறுதி செய்யும் முறை.
- **Technical Definition**: A process where both the client and the server authenticate each other at the same time using cryptographic X.509 digital certificates before an encrypted TLS connection is established.
- 🏗️ *System Design*: *"Our Kubernetes Istio service mesh enforces strict **mTLS** across all inter-service pods to eliminate inside-network snooping."*

---

### 67. **Webhook** `[WEB-hook]` *(Noun)*
- **Tamil Meaning**: நாமாக அடிக்கடி சென்று கேட்காமல், ஒரு நிகழ்வு (எ.கா. பணம் செலுத்தப்பட்டது) நடந்த அடுத்த விநாடியே சர்வர் நமது ஏபிஐ-யை கூப்பிட்டு தகவலை வழங்கும் தானியங்கி அழைப்பு.
- **Technical Definition**: User-defined HTTP callbacks triggered by a specific event in a web application, sending real-time data to a third-party destination URL.
- **Natural Collocations**: *Stripe webhook, verify webhook signature, webhook handler, webhook retries*.
- 💻 *Code Review / PR*: *"Always verify the cryptographic HMAC signature on incoming Stripe **webhooks** to ensure the payload was not forged by an attacker."*

---

### 68. **Stateless vs. Stateful** `[STEYT-lis vs STEYT-fuhl]` *(Noun / Adjective)*
- **Tamil Meaning**:
  - *Stateless*: முந்தைய கோரிக்கையின் நினைவை மனதில் வைத்துக்கொள்ளாமல், ஒவ்வொரு கோரிக்கையையும் ஒரு புதிய சுயமாக இயங்கும் நிகழ்வாகக் கருதும் முறை (JWT, REST).
  - *Stateful*: பயனரின் நிலையை (Login session) தனது உள்ளூர் நினைவகத்தில் குறித்து வைத்துக்கொண்டு இயங்கும் முறை.
- 🏗️ *System Design*: *"Building strictly **stateless** backend microservices allows us to instantly scale our pods from 2 to 200 without synchronizing sticky session memory."*

---

### 69. **Man-in-the-Middle (MITM)** `[man in thuh MID-l]` *(Noun)*
- **Tamil Meaning**: கிளைன்ட்டிற்கும் சர்வருக்கும் இடையே உள்ள நெட்வொர்க்கில் ஒளிந்திருந்து, செல்லும் ரகசிய தகவல்களை ஒட்டுக்கேட்டு அல்லது மாற்றி அனுப்பும் தாக்குதல்.
- **Technical Definition**: An attack where the attacker secretly relays and possibly alters the communications between two parties who believe that they are directly communicating with each other.
- 💻 *Code Review / PR*: *"Enforce HTTPS with HSTS (HTTP Strict Transport Security) to shield users against **Man-in-the-Middle** Wi-Fi eavesdropping."*

---

### 70. **JSON Web Token (JWT)** `[JOT / jay-wuh-t]` *(Noun)*
- **Tamil Meaning**: பயனரின் அடையாளம் மற்றும் உரிமைகளை டிஜிட்டல் கையொப்பத்துடன் என்கிரிப்ட் செய்யாமல் அல்லது என்கிரிப்ட் செய்து சுருக்கமாக கொண்டு செல்லும் பாதுகாப்பான இணைய டோக்கன்.
- **Natural Collocations**: *JWT claims, sign the JWT, JWT expiration, refresh token vs access token*.
- 💻 *Code Review / PR*: *"Store the **JWT** expiration in the `exp` claim and keep access token lifespans short (e.g., 15 minutes) backed by long-lived refresh tokens."*

---

### 71. **API Gateway** `[ay-pee-eye GEYT-wey]` *(Noun)*
- **Tamil Meaning**: வெளிப்புற வாடிக்கையாளர்களின் அனைத்து அழைப்புகளையும் முதலில் பெற்று, அங்கீகாரம் (Auth), ரேட் லிமிட் செய்து உள்நாட்டு மைக்ரோசர்வீஸ்களுக்கு சரியான முறையில் பிரித்து அனுப்பும் பிரதான நுழைவு வாயில்.
- **Technical Definition**: An API management tool that sits between a client and a collection of backend services, acting as a reverse proxy to route requests, enforce security policies, and aggregate telemetry.
- 🏗️ *System Design*: *"Spring Cloud Gateway serves as our enterprise **API Gateway**, handling authentication, JWT validation, and canary traffic routing."*

---

### 72. **Multiplexing** `[MUHL-tuh-pleks-ing]` *(Noun)*
- **Tamil Meaning**: ஒரே ஒரு ஒற்றை நெட்வொர்க் இணைப்பின் வழியாக, பல கோரிக்கைகளையும் பதில்களையும் வரிசையில் காக்க வைக்காமல் ஒரே நேரத்தில் பின்னிப் பிணைத்து அனுப்பும் உத்தி (HTTP/2).
- **Technical Definition**: The process of sending multiple signals or streams of information across a single carrier simultaneously.
- 🏗️ *System Design*: *"HTTP/2 **multiplexing** eliminated browser head-of-line blocking by allowing 100 assets to download over a single TCP connection concurrently."*

---

### 73. **DNS Propagation** `[dee-en-es prop-uh-GEY-shuhn]` *(Noun)*
- **Tamil Meaning**: நாம் மாற்றிய புதிய டொமைன் ஐபி முகவரி உலகம் முழுவதும் உள்ள அனைத்து இணைய சர்வர்களுக்கும் சென்று சேர எடுக்கும் கால அவகாசம்.
- **Technical Definition**: The time period it takes for domain name system records to be updated across all global recursive DNS servers.
- 🚨 *War Room Incident*: *"Our DNS TTL was set to 86,400 seconds; consequently, **DNS propagation** delayed our disaster recovery traffic switch by an entire day."*

---

### 74. **DDOS (Distributed Denial of Service)** `[DEE-dos]` *(Noun)*
- **Tamil Meaning**: பல்லாயிரக்கணக்கான பாதிக்கப்பட்ட கணினிகளில் (Botnet) இருந்து ஒரே நேரத்தில் போலியான டிராஃபிக்கை ஏவி சர்வரை மூச்சுத் திணற வைத்து முடக்கும் தாக்குதல்.
- **Technical Definition**: A malicious attempt to disrupt the normal traffic of a targeted server, service, or network by overwhelming the target or its surrounding infrastructure with a flood of Internet traffic.
- 🏗️ *System Design*: *"We place Cloudflare Magic Transit in front of our origins to absorb multi-terabit volumetric **DDoS** floods before they reach our VPC."*

---

### 75. **Envelope Encryption** `[EN-vuh-lohp en-KRIP-shuhn]` *(Noun / Security Pattern)*
- **Tamil Meaning**: கடிதத்தை உறையில் போட்டு பூட்டுவது போல், அசல் டேட்டாவை ஒரு டேட்டா கீயை வைத்து பூட்டிவிட்டு, அந்த டேட்டா கீயை ஒரு மாஸ்டர் கீயை வைத்து பூட்டி பாதுகாக்கும் இரண்டு அடுக்கு பாதுகாப்பு முறை.
- **Technical Definition**: The practice of encrypting plaintext data with a unique Data Encryption Key (DEK), and then encrypting the DEK with a Key Encryption Key (KEK) managed by a KMS (Key Management Service).
- 💻 *Code Review / PR*: *"For PCI-DSS credit card compliance, use AWS KMS **Envelope Encryption** to ensure raw plaintext cryptographic keys never touch local persistent disks."*

---

# 7. The Top 25 IT Engineering Acronyms Decoded

| Acronym | Full Form | Tamil Meaning & Real Tech Context |
| :--- | :--- | :--- |
| **API** | Application Programming Interface | இரண்டு மென்பொருட்கள் ஒன்றுடன் ஒன்று பேசிக்கொள்ளும் இணைப்பு பாலம். |
| **CI / CD** | Continuous Integration / Continuous Deployment | கோடை தானாக டெஸ்ட் செய்து தானாகவே சர்வரில் வெளியிடும் பைப்லைன். |
| **SRE** | Site Reliability Engineering | கூகுள் உருவாக்கிய சாப்ட்வேர் சார்ந்த உள்கட்டமைப்பு நம்பகத்தன்மை துறை. |
| **JWT** | JSON Web Token | பயனரின் அடையாளத்தை டிஜிட்டல் கையொப்பத்துடன் சுமந்து செல்லும் டோக்கன். |
| **CORS** | Cross-Origin Resource Sharing | மற்றொரு டொமைனில் இருந்து ஏபிஐ அழைப்புகளை கட்டுப்படுத்தும் பிரவுசர் விதி. |
| **mTLS** | Mutual Transport Layer Security | கிளைன்ட், சர்வர் இருவருமே பரஸ்பரம் சான்றிதழை சரிபார்க்கும் நெறிமுறை. |
| **TTL** | Time To Live | கேச் அல்லது டிஎன்எஸ் பதிவு எத்தனை நொடிகள் உயிரோடு இருக்க வேண்டும் என்ற கால வரம்பு. |
| **OOM** | Out Of Memory | ரேம் மெமரி முழுவதும் தீர்ந்துபோய் ஜாவா அல்லது கன்டெய்னர் சாகும் நிலை. |
| **RPC** | Remote Procedure Call | மற்றொரு சர்வரில் இருக்கும் மெத்தடை உள்ளூர் மெத்தட் போல் அழைக்கும் முறை (gRPC). |
| **CRUD** | Create, Read, Update, Delete | டேட்டாபேஸில் செய்யப்படும் நான்கு அடிப்படை செயல்பாடுகள். |
| **ORM** | Object-Relational Mapping | டேட்டாபேஸ் அட்டவணைகளை ஜாவா ஆப்ஜெக்ட்டாக இணைக்கும் பாலம் (Hibernate). |
| **VPC** | Virtual Private Cloud | கிளவுடில் நமது நிறுவனத்திற்காக ஒதுக்கப்படும் தனிப்பட்ட பிரத்தியேக நெட்வொர்க். |
| **IAM** | Identity and Access Management | யார் யாருக்கு எந்த கிளவுட் சர்வரில் என்னென்ன அதிகாரம் உண்டு என்பதை நிர்ணயிக்கும் அமைப்பு. |
| **DNS** | Domain Name System | மனிதர்கள் படிக்கும் இணைய முகவரியை (google.com) ஐபி முகவரியாக மாற்றும் போன்புக். |
| **SPOF** | Single Point of Failure | பழுதானால் மொத்த அமைப்பையும் கவிழ்த்துவிடும் ஒற்றை பலவீன அங்கம். |
| **ACID** | Atomicity, Consistency, Isolation, Durability | வங்கி டேட்டாபேஸிற்கு தேவையான நான்கு புனித நற்பண்புகள். |
| **BASE** | Basically Available, Soft State, Eventual Consistency | NoSQL டேட்டாபேஸ்கள் பின்பற்றும் நெகிழ்வான அணுகுமுறை. |
| **RPO / RTO** | Recovery Point Objective / Recovery Time Objective | பேக்கப் இழப்பு வரம்பு / சர்வர் முடங்கினால் மீண்டும் உயிர்ப்பிக்க எடுக்கும் அதிகபட்ச நேரம். |
| **NIO** | Non-Blocking Input/Output | த்ரெட்டை முடக்காமல் இயங்கும் அதிவேக நெட்வொர்க் பரிமாற்ற முறை. |
| **GC** | Garbage Collection | ஜாவா மெமரியில் உள்ள குப்பைகளை தானாக துடைக்கும் தூய்மைப் பணியாளர். |
| **HPA** | Horizontal Pod Autoscaler | கியூபர்னெட்டீஸில் சுமை கூடும்போது தானாக போட்களை அதிகரிக்கும் கருவி. |
| **NAT** | Network Address Translation | பிரைவேட் ஐபி முகவரிகளை பப்ளிக் ஐபி முகவரியாக மாற்றி இணையத்திற்கு அனுப்பும் தொழில்நுட்பம். |
| **SSL / TLS**| Secure Sockets Layer / Transport Layer Security | இணையத்தில் செல்லும் தகவல்களை என்கிரிப்ட் செய்து பாதுகாக்கும் நெறிமுறை. |
| **WAF** | Web Application Firewall | SQL Injection, XSS போன்ற இணையத் தாக்குதல்களை வடிகட்டும் மென்பொருள் சுவர். |
| **SDK** | Software Development Kit | ஒரு குறிப்பிட்ட தளத்தில் அப்ளிகேஷன் எழுத வழங்கப்படும் கருவிகள் மற்றும் லைப்ரரிகளின் தொகுப்பு. |

---

# 8. Real-World Engineering Dialogues (PR Review, System Design & Sev-1 War Room)

### 🎭 Scenario A: Pull Request (PR) Architectural Debate

> **Staff Architect (Suresh)**:
> *"Looking at your PR on line 142, you are calling the external payment gateway inside the `@Transactional` method.
> 
> If the third-party payment gateway takes 8 seconds to respond under heavy load, your thread will hold open the HikariCP connection for that entire duration. Under peak traffic, this will cause **connection pool exhaustion** and trigger **cascading failures** across our unrelated user login endpoints.
> 
> Furthermore, if the client experiences a network timeout and retries the request, your endpoint is not **idempotent**, meaning the customer could be charged twice. Please extract the HTTP call outside the database transaction, enforce an **idempotency key** backed by Redis, and wrap the client in a **circuit breaker**."*
> 
> **Senior Engineer (Vignesh)**:
> *"Brilliant catch, Suresh. I will move the network I/O outside the transaction boundaries, use an `Idempotency-Key` header with atomic Redis locking, and configure Resilience4j to fast-fail if the gateway latency breaches our 2-second timeout SLA."*

---

### 🎭 Scenario B: Sev-1 Production Outage War Room

> **Incident Commander**: *"Status check! Checkout service is throwing 504 Gateway Timeouts across US-East. What are the metrics showing?"*
> 
> **SRE Lead (Priya)**:
> *"Our Datadog APM **telemetry** indicates that our database **P99 latency** skyrocketed from 12ms to 14,000ms.
> 
> What happened is that our top product inventory key expired from Redis at 12:00 PM without any TTL jitter. This triggered a massive **cache stampede** where 40,000 concurrent requests bypassed Redis and hammered Postgres simultaneously.
> 
> The database CPU hit 100%, causing severe **thread pool starvation** in our backend pods. Kubernetes readiness probes started failing, and pods are getting killed in an endless **cascading reboot loop**."*
> 
> **Incident Commander**: *"What is our mitigation plan?"*
> 
> **SRE Lead**:
> *"We are enabling emergency rate-limiting at our Cloudflare edge to **throttle** ingress traffic by 30%. Next, we are populating the Redis cache with randomized TTL jitter and applying a distributed Mutex lock on the cache loader. That will shield the database and allow HikariCP connections to stabilize."*
> 
> **Incident Commander**: *"Execute mitigation immediately. Log all actions for our post-mortem."*

---

👉 **Next Steps & Essential Resources**:
- 💼 **[Business English & Corporate Words Master Guide](business_english_corporate_words_master_guide.md)**
- 📖 **[IELTS 500 Advanced Academic Lexicon Master Guide](ielts_500_words_master_guide.md)**
- 🎓 **[IELTS Band 8–9 & TOEFL Advanced Vocabulary Master Guide](ielts_toefl_advanced_vocabulary_master_guide.md)**
- 🎭 **[English Idioms, Origins & Corporate Metaphors Master Guide](english_idioms_master_guide.md)**
- 💬 **[English Phrases, Phrasal Verbs & Expressions Master Guide](english_phrases_master_guide.md)**
- 💬 **[500 Real-World Spoken English Scenarios & Workplace Scripts](spoken_english_500_scenarios_master_guide.md)**
- 🗣️ **[Spoken English, Grammar & IT Communication Master Guide](spoken_english_tamil_to_global_master_guide.md)**
- 📑 **[README.md Main Hub](README.md#15-professional-communication-spoken-english--global-it-fluency)**
