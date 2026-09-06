# 📚 Master Directory: All Markdown Guides Categorized
### The Complete Architectural Inventory of All 150+ Engineering Documents across the Repository

> **Repository Master Index**: This directory catalogs every single Markdown (`.md`) guide in this repository. All documents are organized by domain, technical discipline, and engineering tier with direct links, scope descriptions, and target audience.

---

## 🧭 High-Level Category Map

| # | Category Domain | Total Files | Key Highlights |
| :-: | :--- | :-: | :--- |
| **1** | [Core Java, Concurrency & JVM Internals](#1-core-java-concurrency--jvm-internals) | 16 | Threads, JMM, Memory, JIT, I/O, Collections, Streams, GC |
| **2** | [Spring Boot & Spring Framework Ecosystem](#2-spring-boot--spring-framework-ecosystem) | 15 | IoC, AOP, JPA, Security 6, WebFlux, Batch, Camel, Kafka |
| **3** | [200+ & 50+ Production Scenarios Deep Dives](#3-200--50-production-scenarios-deep-dives) | 22 | Strict 4-Part & 5-Part War Room Scenario Interview Banks |
| **4** | [Technical Terms & Zero-Jargon Encyclopedias](#4-technical-terms--zero-jargon-encyclopedias) | 6 | Plain-English definitions, mental models & trap breakdowns |
| **5** | [Databases, Persistence & SQL Normalization](#5-databases-persistence--sql-normalization) | 6 | 1NF-6NF, ACID, MVCC, PostgreSQL, MongoDB, Spring SQL |
| **6** | [Message Queues, Event Streaming & Distributed Systems](#6-message-queues-event-streaming--distributed-systems) | 6 | Kafka, RabbitMQ, Pulsar, Outbox Pattern, KRaft, EOS |
| **7** | [Cloud Native, Containers & Infrastructure](#7-cloud-native-containers--infrastructure) | 16 | AWS, Azure, GCP, Kubernetes, Docker, Envoy, NGINX, Istio |
| **8** | [DevOps, CI/CD, GitOps & Infrastructure-as-Code](#8-devops-cicd-gitops--infrastructure-as-code) | 11 | GitHub Actions, Jenkins, ArgoCD, Terraform, Ansible, Chef |
| **9** | [Enterprise Security, Identity & Cryptography](#9-enterprise-security-identity--cryptography) | 7 | AES-GCM, RSA, Ed25519, OAuth2, OIDC, Vault, OPA Rego |
| **10** | [Modern Web, Frontend & Cross-Platform Desktop](#10-modern-web-frontend--cross-platform-desktop) | 6 | React 19, Next.js RSC, Angular Signals, Tauri 2.0, GraphQL, gRPC |
| **11** | [Systems Programming (Rust, Golang & C/C++)](#11-systems-programming-rust-golang--cc) | 6 | Ownership, Tokio, GMP Scheduler, brk/mmap, Cache Lines |
| **12** | [Testing, QA & Test Automation](#12-testing-qa--test-automation) | 5 | Cucumber BDD, Selenium 4, Playwright, Testcontainers |
| **13** | [Observability, Telemetry & SRE Troubleshooting](#13-observability-telemetry--sre-troubleshooting) | 7 | OpenTelemetry, LGTM Stack, Splunk, Chaos, Forensics |
| **14** | [Artificial Intelligence, Generative AI & Python](#14-artificial-intelligence-generative-ai--python) | 3 | LLM Transformers, RAG Vector Search, Python & PyTorch |
| **15** | [Static Site Generators & Documentation Engines](#15-static-site-generators--documentation-engines) | 7 | VitePress, MkDocs, Hugo, Starlight, Docusaurus, Docsify |
| **16** | [Professional Communication & Spoken English](#16-professional-communication--spoken-english) | 8 | 500 Scenarios, IELTS AWL, Roots, Phrasal Verbs, Business English |
| **17** | [System Design & Technical Interview Preparation](#17-system-design--technical-interview-preparation) | 4 | HLD, LLD, FAANG Behavioral STAR, Architecture Blueprints |
| **18** | [Algorithms, Data Structures & LeetCode Patterns](#18-algorithms-data-structures--leetcode-patterns) | 2 | LeetCode Patterns, Big-O Data Structures & Visual Trees |
| **19** | [Specialized Topic Mastery Labs (`topics/`)](#19-specialized-topic-mastery-labs-topics) | 11 | Hands-on labs for GC, Profiling, Kubernetes, Docker, Splunk |
| **20** | [Staged Roadmaps & Extended Modules (`new/`)](#20-staged-roadmaps--extended-modules-new) | 7 | Java Mastery Hub, Kubernetes Zero-to-Hero, Docker Roadmap |

---

## ☕ 1. Core Java, Concurrency & JVM Internals

| Document Title | File Link | Focus Areas & Technical Depth | Target Level |
| :--- | :--- | :--- | :--- |
| **Java Interview Master Guide** | [`java_interview_master_guide.md`](java-core/java_interview_master_guide.md) | Collections Internals, JMM, Singleton Masterclass, Generics, Reflection in Spring, AOP, Thread Safety, Flagship Programs. | Intermediate to Senior |
| **Java Concurrency & Threads** | [`java_thread.md`](java-core/java_thread.md) | Synchronized blocks, Locks, Reentrancy, ExecutorService, Virtual Threads, Thread Dumps. | Senior Engineer |
| **CompletableFuture & Async I/O** | [`completable_future.md`](java-core/completable_future.md) | Non-blocking pipelines, Thread pool isolation, Exception handling, Timeout guards. | Intermediate to Senior |
| **Java I/O, NIO & File Channels** | [`java_io.md`](java-core/java_io.md) | Standard Streams, Readers, Non-blocking NIO Channels, Off-heap buffers, Zero-Copy. | Intermediate to Senior |
| **Java Collections Architecture** | [`java_collection.md`](java-core/java_collection.md) | Big-O time/space matrix, Hierarchy, Hash tables, Thread-safety tradeoffs. | Foundational to Senior |
| **Java Collections Reference (Alt)** | [`java collection.md`](java-core/java%20collection.md) | Auxiliary reference notes on core Java collection interfaces. | Quick Reference |
| **Java Streams Real-World Recipes** | [`java_collection_stream.md`](java-core/java_collection_stream.md) | Grouping, Partitioning, Map/Reduce, FlatMap, Parallel streams performance. | Intermediate to Senior |
| **JVM GC & Profiling Master Guide** | [`jvm_gc_profiling_master_guide.md`](java-core/jvm_gc_profiling_master_guide.md) | HotSpot Memory, Serial/Parallel/G1/ZGC/Shenandoah, JFR, Async-profiler, `jcmd`/`jstat`. | Staff / Principal |
| **JVM JIT Compiler Master Guide** | [`jvm_jit_compiler_master_guide.md`](java-core/jvm_jit_compiler_master_guide.md) | C1/C2 Compilers, Tiered Compilation Levels 0-4, Escape Analysis, Inlining, OSR, hsdis. | Staff / Principal |
| **Jackson JSON Engine Master Guide** | [`jackson_master_guide.md`](java-core/jackson_master_guide.md) | Streaming `JsonParser`, Tree Model, Polymorphism, `@JsonView`, Java 17/21 Records, RCE Defense. | Senior / Staff |
| **Java & Spring Cryptography Guide** | [`java_spring_cryptography_master_guide.md`](java-core/java_spring_cryptography_master_guide.md) | JCA/JCE, AES-256-GCM, RSA, Ed25519, Argon2id, KeyStores, KMS Envelope Encryption. | Staff / Security Lead |
| **Maven & Gradle Master Guide** | [`maven_gradle_master_guide.md`](java-core/maven_gradle_master_guide.md) | Maven Lifecycles, Gradle Task Graphs, Configuration Cache, Workers, Jib containers. | Senior / DevOps |
| **Java Collections Mastery Lab** | [`topics/java_collections_mastery.md`](topics/java_collections_mastery.md) | Deep algorithmic mechanics, Hash collision handling, LinkedHashMap LRU. | Lab / Practice |
| **Java GC Mastery Lab** | [`topics/java_gc_mastery.md`](topics/java_gc_mastery.md) | Generational heap, card tables, write barriers, pause time tuning. | Lab / Practice |
| **Java Profiling Mastery Lab** | [`topics/java_profiling_mastery.md`](topics/java_profiling_mastery.md) | JProfiler, VisualVM, Async-profiler, Flame graphs, CPU hotspot identification. | Lab / Practice |
| **Maven Mastery Lab** | [`topics/maven_mastery.md`](topics/maven_mastery.md) | POM lifecycle, Plugin bindings, Dependency conflict trees, CLI shortcuts. | Lab / Practice |

---

## 🍃 2. Spring Boot & Spring Framework Ecosystem

| Document Title | File Link | Focus Areas & Technical Depth | Target Level |
| :--- | :--- | :--- | :--- |
| **Spring Framework 6 & Boot 3 Master Guide** | [`spring_master_guide.md`](spring-framework/spring_master_guide.md) | Core IoC Container, Security 6, Data JPA & Hibernate 6, WebFlux, Kafka, GraalVM AOT. | Staff / Architect |
| **Spring Boot 3+ Enterprise Guide** | [`spring_boot.md`](spring-framework/spring_boot.md) | IoC/DI, RFC 7807 Exception Handling, JPA, Security 6, Actuator Observability. | Intermediate to Senior |
| **Spring AOP & Proxy Architecture** | [`spring_aop_master_guide.md`](spring-framework/spring_aop_master_guide.md) | JDK Dynamic Proxies, CGLIB Byte Buddy, AspectJ LTW/CTW, Idempotency, SLA Auditing. | Senior / Staff |
| **Spring Data JPA & Hibernate 6** | [`spring_data_jpa.md`](spring-framework/spring_data_jpa.md) | Pooled-lo sequences, N+1 solutions (`JOIN FETCH`, `@EntityGraph`), Locks, Specs, HikariCP. | Senior / Staff |
| **Spring Security 6 & OAuth2** | [`spring_security.md`](spring-framework/spring_security.md) | SecurityFilterChain, Stateless JWT filters, RBAC, Method Security, CORS/CSRF. | Senior / Staff |
| **Spring SQL & JDBC Architecture** | [`spring_sql.md`](spring-framework/spring_sql.md) | `NamedParameterJdbcTemplate`, HikariCP pool formula, batching, O(1) memory streaming. | Senior Engineer |
| **Spring Data Redis & Caching** | [`spring_redis.md`](spring-framework/spring_redis.md) | Lettuce Netty pipeline, `@Cacheable` SpEL, Stampede defense, Redisson locks, Streams. | Senior / Architect |
| **Spring for Apache Kafka** | [`spring_kafka.md`](spring-framework/spring_kafka.md) | Idempotent producer, concurrency, manual ack, DLT error handler, EOS transactions. | Senior / Architect |
| **Spring Cloud & Microservices** | [`spring_cloud_microservices.md`](spring-framework/spring_cloud_microservices.md) | Spring Cloud Gateway, OpenFeign, Resilience4j Circuit Breakers, Discovery, Micrometer. | Senior / Architect |
| **Spring WebFlux & Reactive Systems** | [`spring_webflux_reactive.md`](spring-framework/spring_webflux_reactive.md) | Netty event loops, Project Reactor (`Mono`/`Flux`), WebClient, R2DBC reactive SQL. | Senior / Architect |
| **Spring Batch 5+ Processing Hub** | [`spring_batch.md`](spring-framework/spring_batch.md) | Chunk processing, Tasklets, Skip/Retry policies, ExecutionContext checkpoints. | Senior Engineer |
| **Apache Camel 4 Integration** | [`spring_camel.md`](spring-framework/spring_camel.md) | EIPs (Content Router, Splitter, Aggregator), Kafka/REST connectors, Dead Letter Channels. | Senior / Architect |
| **Spring Boot Testing Master Guide** | [`spring_testing.md`](spring-framework/spring_testing.md) | Sliced tests (`@WebMvcTest`, `@DataJpaTest`), Testcontainers `@ServiceConnection`, WireMock. | Senior / QA Lead |
| **MongoDB Polyglot Guide (Java & Node.js)**| [`mongodb_master_guide.md`](databases-persistence/mongodb_master_guide.md) | BSON, WiredTiger Internals, Aggregations, Spring Data Mongo, Mongoose, Multi-Doc ACID. | Senior / Staff |
| **Java Mastery Hub (Roadmap)** | [`new/Java_Mastery_Hub.md`](new/Java_Mastery_Hub.md) | Comprehensive high-level curriculum outline for Java backend mastery. | Learning Roadmap |

---

## 🎯 3. 200+ & 50+ Production Scenarios Deep Dives

| Document Title | File Link | Scenario Count | Domains Covered |
| :--- | :--- | :--- | :--- |
| **Java Concurrency: 200 Scenarios** | [`java_threads_concurrency_200_scenarios_master_guide.md`](scenarios/java_threads_concurrency_200_scenarios_master_guide.md) | **200 Scenarios** | Virtual Threads, JMM, AQS, Lock-Free CAS, Executor Tuning, Deadlocks. |
| **Java Collections & Streams: 200 Scenarios** | [`java_collections_streams_200_scenarios_master_guide.md`](scenarios/java_collections_streams_200_scenarios_master_guide.md) | **200 Scenarios** | HashMap Math, ConcurrentSkipList, Spliterators, Stream GC Optimization. |
| **CompletableFuture: 200 Scenarios** | [`completable_future_200_scenarios_master_guide.md`](scenarios/completable_future_200_scenarios_master_guide.md) | **200 Scenarios** | Non-Blocking Pipelines, ForkJoinPool Traps, Distributed Aggregators, Async Stack Traces. |
| **Java I/O, NIO & Channels: 200 Scenarios** | [`java_io_nio_200_scenarios_master_guide.md`](scenarios/java_io_nio_200_scenarios_master_guide.md) | **200 Scenarios** | DirectByteBuffer Off-Heap, Zero-Copy sendfile, MappedByteBuffer, Netty Selectors. |
| **Message Queues: 200 Scenarios** | [`message_queues_200_scenarios_master_guide.md`](scenarios/message_queues_200_scenarios_master_guide.md) | **200 Scenarios** | Commit Logs, EOS, Consumer Rebalances, RabbitMQ, Pulsar, Redis Streams, SQS. |
| **Security & Infrastructure: 200 Scenarios** | [`security_infra_200_scenarios_master_guide.md`](scenarios/security_infra_200_scenarios_master_guide.md) | **200 Scenarios** | AuthN, AuthZ, Microservices, API Gateways, F5 LTM/GTM, Avi Networks, Istio. |
| **OPA & Rego: 200 Scenarios** | [`opa_rego_200_scenarios_master_guide.md`](scenarios/opa_rego_200_scenarios_master_guide.md) | **200 Scenarios** | Gateway AuthZ, K8s Admission Control, Terraform IaC Guardrails, CI/CD Compliance. |
| **Spring Enterprise: 100+ Scenarios** | [`spring_200_scenarios_master_guide.md`](scenarios/spring_200_scenarios_master_guide.md) | **100+ Scenarios** | Core IoC, AOP, Batch, Camel, JPA, Security, Cloud, Redis, Kafka, WebFlux. |
| **Spring AOP: 50+ Scenarios** | [`spring_aop_scenarios_master_guide.md`](scenarios/spring_aop_scenarios_master_guide.md) | **50+ Scenarios** | CGLIB Byte Buddy, AspectJ LTW/CTW, Advice Precedence, Self-Invocation. |
| **Spring Batch: 50+ Scenarios** | [`spring_batch_scenarios_master_guide.md`](scenarios/spring_batch_scenarios_master_guide.md) | **50+ Scenarios** | Chunk Demarcation, Paging vs Cursors, Skip/Retry DLQ, Partitioning. |
| **Apache Camel: 50+ Scenarios** | [`spring_camel_scenarios_master_guide.md`](scenarios/spring_camel_scenarios_master_guide.md) | **50+ Scenarios** | Exchange Anatomy, Streaming Splitters, Aggregators, Dead Letter Channels. |
| **Spring Security: 50+ Scenarios** | [`spring_security_scenarios_master_guide.md`](scenarios/spring_security_scenarios_master_guide.md) | **50+ Scenarios** | Filter Chains, Stateless JWTs, Method Security, CORS Preflight, Multi-Tenant JWKS. |
| **Spring Data JPA: 50+ Scenarios** | [`spring_data_jpa_scenarios_master_guide.md`](scenarios/spring_data_jpa_scenarios_master_guide.md) | **50+ Scenarios** | Pooled-lo Sequences, N+1 Elimination, MultipleBagFetch, Pessimistic Locks. |
| **Spring Cloud: 50+ Scenarios** | [`spring_cloud_scenarios_master_guide.md`](scenarios/spring_cloud_scenarios_master_guide.md) | **50+ Scenarios** | Netty Gateway Filters, Feign Timeouts, Resilience4j Sliding Windows, Micrometer. |
| **Spring Redis: 50+ Scenarios** | [`spring_redis_scenarios_master_guide.md`](scenarios/spring_redis_scenarios_master_guide.md) | **50+ Scenarios** | Lettuce Pipelining, Redisson Watchdog Locks, Cache Avalanche Jitter, Streams. |
| **Spring Kafka: 50+ Scenarios** | [`spring_kafka_scenarios_master_guide.md`](scenarios/spring_kafka_scenarios_master_guide.md) | **50+ Scenarios** | Rebalance Storms, Exactly-Once Semantics, Poison Pill DLTs, Transactional Outbox. |
| **Spring WebFlux: 50+ Scenarios** | [`spring_webflux_scenarios_master_guide.md`](scenarios/spring_webflux_scenarios_master_guide.md) | **50+ Scenarios** | Netty EventLoops, boundedElastic, Backpressure, BlockHound, R2DBC. |
| **Spring Testing: 50+ Scenarios** | [`spring_testing_scenarios_master_guide.md`](scenarios/spring_testing_scenarios_master_guide.md) | **50+ Scenarios** | Context Cache Explosion, Sliced WebMvc, Testcontainers, Awaitility. |
| **Spring SQL & JDBC: 50+ Scenarios** | [`spring_sql_scenarios_master_guide.md`](scenarios/spring_sql_scenarios_master_guide.md) | **50+ Scenarios** | HikariCP Pool Sizing Formula, Leak Detection, TransactionTemplate, O(1) Stream. |
| **Jackson JSON: 50+ Scenarios** | [`jackson_scenarios_master_guide.md`](scenarios/jackson_scenarios_master_guide.md) | **50+ Scenarios** | Streaming O(1) Memory, RCE Gadget Allowlisting, Records, `@JsonView` PII. |
| **Java Cryptography: 50+ Scenarios** | [`java_spring_cryptography_scenarios_master_guide.md`](scenarios/java_spring_cryptography_scenarios_master_guide.md) | **50+ Scenarios** | AES-GCM Nonce Reuse, Ed25519, Argon2id, KMS Envelope Encryption, Timing Attacks. |
| **MongoDB Polyglot: 50+ Scenarios** | [`mongodb_scenarios_master_guide.md`](scenarios/mongodb_scenarios_master_guide.md) | **50+ Scenarios** | 16MB BSON Limit, ESR Rule, 100MB Aggregation Spilling, ACID Transactions. |
| **Frontend Polyglot: 50+ Scenarios** | [`frontend_scenarios_master_guide.md`](scenarios/frontend_scenarios_master_guide.md) | **50+ Scenarios** | Fiber useTransition, TypeScript Emitters, Zoneless Signals, Microtask Starvation. |
| **Rust Concurrency: 50+ Scenarios** | [`rust_scenarios_master_guide.md`](scenarios/rust_scenarios_master_guide.md) | **50+ Scenarios** | Borrow Checker Battles, Tokio Worker Starvation, Mutex Poisoning, Serde Zero-Copy. |
| **Golang Concurrency: 50+ Scenarios** | [`golang_scenarios_master_guide.md`](scenarios/golang_scenarios_master_guide.md) | **50+ Scenarios** | Goroutine Leaks, 3-Index Slices, Interface Nil Gotchas, Preemption, Fast Buffers. |
| **Rust & Golang Dual Scenarios** | [`rust_golang_scenarios_master_guide.md`](scenarios/rust_golang_scenarios_master_guide.md) | **50+ Scenarios** | Direct comparative scenario breakdown between Rust memory and Go concurrency. |
| **DevOps & IaC: 50+ Scenarios** | [`devops_iac_scenarios_master_guide.md`](scenarios/devops_iac_scenarios_master_guide.md) | **50+ Scenarios** | Terraform State Drift, Ansible Drains, Docker Union Mounts, K8s Pod Failures. |

---

## 📖 4. Technical Terms & Zero-Jargon Encyclopedias

| Document Title | File Link | Focus Areas & Terms Decoded | Target Level |
| :--- | :--- | :--- | :--- |
| **Enterprise Java Technical Terms** | [`enterprise_java_technical_terms_master_guide.md`](java-core/enterprise_java_technical_terms_master_guide.md) | 60+ Terms: CGLIB, AspectJ Weaving, Dirty Checking, Pooled-Lo, HikariCP, AEAD, Netty. | All Levels |
| **Frontend Polyglot Technical Terms** | [`frontend_polyglot_technical_terms_master_guide.md`](frontend-web/frontend_polyglot_technical_terms_master_guide.md) | V8 Event Loop, Microtasks, Closures, Discriminated Unions, Fiber, Signals, RSC. | All Levels |
| **Rust Systems Technical Terms** | [`rust_technical_terms_master_guide.md`](systems-languages/rust_technical_terms_master_guide.md) | Ownership, Move Semantics, Borrow Checker, Lifetimes `'a`, RAII `Drop`, `Pin<P>`, Tokio. | All Levels |
| **Golang Systems Technical Terms** | [`golang_technical_terms_master_guide.md`](systems-languages/golang_technical_terms_master_guide.md) | GMP Scheduler, Work-Stealing, Escape Analysis, Tri-Color GC, `hchan` Ring Buffer. | All Levels |
| **Rust & Golang Terms (Dual Guide)** | [`rust_golang_technical_terms_master_guide.md`](systems-languages/rust_golang_technical_terms_master_guide.md) | Comparative terminology Rosetta Stone between Rust affine types and Go channels. | All Levels |
| **DevOps & IaC Technical Terms** | [`devops_iac_technical_terms_master_guide.md`](devops-cicd-iac/devops_iac_technical_terms_master_guide.md) | IaC Idempotency, DAG Compilers, cgroups v2, OverlayFS, JCasC, GitOps Sync Waves. | All Levels |
| **Distributed Systems Glossary** | [`topics/glossary.md`](topics/glossary.md) | Distributed Systems, Cloud Native, Networking and Storage Terms. | Reference |

---

## 🗄️ 5. Databases, Persistence & SQL Normalization

| Document Title | File Link | Focus Areas & Technical Depth | Target Level |
| :--- | :--- | :--- | :--- |
| **SQL Normalization & ACID Master Guide** | [`sql_normalization_acid_master_guide.md`](databases-persistence/sql_normalization_acid_master_guide.md) | 1NF-6NF, BCNF, DKNF, Anomalies, WAL, ARIES Recovery, fsync, 6 Concurrency Phenomena, 2PL, MVCC, SSI, Blueprints, 50 Q&As. | Senior to Principal |
| **PostgreSQL Internals Master Guide** | [`postgresql_master_guide.md`](databases-persistence/postgresql_master_guide.md) | Heap Tuples, MVCC `xmin`/`xmax`, WAL & Checkpoints, Autovacuum Freeze Outages, B-Tree/GIN, PgBouncer. | Staff / Principal |
| **MongoDB Polyglot Master Guide** | [`mongodb_master_guide.md`](databases-persistence/mongodb_master_guide.md) | BSON Spec, WiredTiger Engine, ESR Index Rule, Aggregation Pipelines, Spring Data, Mongoose, ACID. | Senior / Staff |
| **SQL & PL/SQL Master Reference** | [`sql.md`](databases-persistence/sql.md) | DDL, DML, Window functions, CTEs, Indexing, Execution plans, 50+ Real-World Query Recipes. | Intermediate to Senior |
| **Spring Data JPA & Hibernate 6** | [`spring_data_jpa.md`](spring-framework/spring_data_jpa.md) | Entity lifecycle, N+1 solutions, Keyset paging, Optimistic/Pessimistic locking, Batching. | Senior / Staff |
| **Spring SQL & JDBC Architecture** | [`spring_sql.md`](spring-framework/spring_sql.md) | `NamedParameterJdbcTemplate`, HikariCP pool sizing, batch inserts, streaming cursors. | Senior Engineer |

---

## 📬 6. Message Queues, Event Streaming & Distributed Systems

| Document Title | File Link | Focus Areas & Technical Depth | Target Level |
| :--- | :--- | :--- | :--- |
| **Message Queues Master Guide** | [`message_queues_master_guide.md`](messaging-distributed/message_queues_master_guide.md) | RabbitMQ vs Kafka vs Pulsar, Zero-Copy sendfile, Outbox pattern, Quorum queues, KRaft. | Senior to Principal |
| **Apache Kafka Internals Master Guide**| [`kafka_internals_master_guide.md`](messaging-distributed/kafka_internals_master_guide.md) | Commit Log Segments, KRaft Quorum, Zero-Copy sendfile, ISR Replication, EOS Idempotency. | Staff / Principal |
| **Message Queues for Beginners** | [`message_queues_beginner_guide.md`](messaging-distributed/message_queues_beginner_guide.md) | Visual fast-food kitchen analogy, 5 core components, Queue vs Topic, DLQ, Junior Q&A. | Beginner to Intermediate |
| **Spring for Apache Kafka** | [`spring_kafka.md`](spring-framework/spring_kafka.md) | Spring Kafka producers, concurrency, manual acknowledgments, DLT poison pills. | Senior Engineer |
| **Message Queues: 200 Scenarios** | [`message_queues_200_scenarios_master_guide.md`](scenarios/message_queues_200_scenarios_master_guide.md) | 200 In-depth production scenarios across Kafka, RabbitMQ, Pulsar, Redis Streams. | Staff Engineer |
| **Microservices Infrastructure Guide**| [`microservices_gateway_infrastructure_master_guide.md`](messaging-distributed/microservices_gateway_infrastructure_master_guide.md) | Saga, Outbox, Reverse Proxies, Service Mesh, L4/L7 Load Balancing, F5 BIG-IP, Avi. | Staff / Architect |

---

## ☁️ 7. Cloud Native, Containers & Infrastructure

| Document Title | File Link | Focus Areas & Technical Depth | Target Level |
| :--- | :--- | :--- | :--- |
| **Amazon Web Services (AWS) Master Guide** | [`aws_master_guide.md`](cloud-infrastructure/aws_master_guide.md) | Nitro Hypervisors, Global Dark Fiber, SigV4, Multi-Account, S3 Erasure, KMS, Master Catalog. | Senior / Architect |
| **Microsoft Azure Master Guide** | [`azure_master_guide.md`](cloud-infrastructure/azure_master_guide.md) | Entra ID Zero-Trust, Accelerated Networking, CAF Groups, Cosmos DB, Master Catalog. | Senior / Architect |
| **Google Cloud Platform (GCP) Guide** | [`google_cloud_master_guide.md`](cloud-infrastructure/google_cloud_master_guide.md) | Global VPC, Andromeda SDN, Maglev VIP, Borg, Spanner TrueTime, BigQuery, Master Catalog. | Senior / Architect |
| **Kubernetes Architecture Guide** | [`kubernetes_master_guide.md`](cloud-infrastructure/kubernetes_master_guide.md) | etcd Raft, kube-apiserver reconciliation, CRI/CNI/CSI, iptables/IPVS, Cilium eBPF, Catalog. | Senior / Staff |
| **Docker Internals Master Guide** | [`docker_master_guide.md`](cloud-infrastructure/docker_master_guide.md) | Namespaces, cgroups v2, OverlayFS, OCI runc/containerd, PID 1 Zombie Reaping, Catalog. | Senior / Staff |
| **Linux Systems & Administration** | [`linux.md`](cloud-infrastructure/linux.md) | Navigation, File permissions, Process signals, Network sockets, 100+ Shell Scenarios. | Intermediate to Senior |
| **NGINX Edge Proxy Master Guide** | [`nginx_master_guide.md`](cloud-infrastructure/nginx_master_guide.md) | epoll EventLoops, Reverse Proxy Buffering, Load Balancing, Rate Limiting, HTTP/2/3. | Senior / SRE |
| **Apache HTTPD & LAMP Stack** | [`apache_httpd_lamp_master_guide.md`](cloud-infrastructure/apache_httpd_lamp_master_guide.md) | MPM (Prefork, Worker, Event), mod_rewrite flags, VirtualHosts, PHP-FPM Unix sockets. | Senior Engineer |
| **Apache Tomcat Master Guide** | [`apache_tomcat_master_guide.md`](cloud-infrastructure/apache_tomcat_master_guide.md) | Catalina Containment, Valves, Coyote NIO, Thread Pool Queuing, JNDI Pool Hardening. | Senior Engineer |
| **Envoy Proxy Master Guide** | [`envoy_proxy_master_guide.md`](cloud-infrastructure/envoy_proxy_master_guide.md) | Thread-per-Core EventLoops, 5 Primitives Chain, Dynamic xDS, Circuit Breakers. | Staff / Architect |
| **Istio Service Mesh Master Guide** | [`istio_service_mesh_master_guide.md`](cloud-infrastructure/istio_service_mesh_master_guide.md) | istiod Control Plane, Sidecars vs Ambient (ztunnel/Waypoint), SPIFFE mTLS, Canary. | Staff / Architect |
| **Web Servers & Mesh Comparison** | [`istio_envoy_nginx_apache_tomcat_lamp_master_guide.md`](cloud-infrastructure/istio_envoy_nginx_apache_tomcat_lamp_master_guide.md) | Unified Comparative Matrix across NGINX, Apache, Tomcat, Envoy, Istio, LAMP. | Staff / Architect |
| **Vi, Vim & Nano Master Guide** | [`vi_vim_nano_master_guide.md`](cloud-infrastructure/vi_vim_nano_master_guide.md) | Modal Editing, Key Grammars, Regex Search/Replace, Visual Blocks, Sudo Tricks. | All Levels |
| **Bash, Batch & PowerShell Guide** | [`bash_batch_powershell_master_guide.md`](cloud-infrastructure/bash_batch_powershell_master_guide.md) | Cross-Platform Rosetta Stone, Strict Modes, Parameter Expansion, .NET Object Pipeline. | All Levels |
| **PowerShell 7+ Automation Guide** | [`powershell_master_guide.md`](cloud-infrastructure/powershell_master_guide.md) | .NET CLR Pipeline, Advanced Toolmaking, Parallel Runspaces, WinRM, Pester 5 BDD. | Senior / SRE |
| **Vagrant & Virtualization Guide** | [`vagrant_master_guide.md`](devops-cicd-iac/vagrant_master_guide.md) | VirtualBox/KVM, NFS/vboxsf Synced Folders, Multi-Node K8s Lab, Packer Golden Boxes. | Senior / DevOps |

---

## 🛠️ 8. DevOps, CI/CD, GitOps & Infrastructure-as-Code

| Document Title | File Link | Focus Areas & Technical Depth | Target Level |
| :--- | :--- | :--- | :--- |
| **GitHub Actions CI/CD Master Guide** | [`github_actions_master_guide.md`](devops-cicd-iac/github_actions_master_guide.md) | ARC on K8s, OIDC Cloud STS Federation, Reusable Workflows, Cache Poisoning Defense. | Senior / Platform |
| **Jenkins CI/CD Pipeline Guide** | [`jenkins_master_guide.md`](devops-cicd-iac/jenkins_master_guide.md) | Controller-Agent Remoting, Ephemeral K8s Pods, JCasC, Groovy CPS Serialization. | Senior / Platform |
| **ArgoCD & GitOps Master Guide** | [`argocd_master_guide.md`](devops-cicd-iac/argocd_master_guide.md) | 3-Way Merge Diff, Sync Waves, ApplicationSets, Hub-and-Spoke Fleet, Argo Rollouts. | Senior / Platform |
| **Git & Version Control Guide** | [`git_master_guide.md`](devops-cicd-iac/git_master_guide.md) | Content-Addressable DAG, Packfiles, 3-Way Merge (ORT), Worktrees, Sparse-Checkout. | Intermediate to Senior |
| **GitHub Enterprise Governance** | [`github_master_guide.md`](devops-cicd-iac/github_master_guide.md) | EMU SAML/SCIM, GitHub Apps JWT Auth, Repository Rulesets, Webhook Event Buses. | Staff / Platform Lead |
| **GitHub Pages & Edge CDN Guide** | [`github_pages_master_guide.md`](devops-cicd-iac/github_pages_master_guide.md) | Fastly Edge Anycast CDN, Automated Let's Encrypt ACME, Custom Apex CNAME, SPA Routing. | Intermediate to Senior |
| **HashiCorp Terraform & IaC Guide** | [`terraform_master_guide.md`](devops-cicd-iac/terraform_master_guide.md) | DAG Dependency Compiler, Remote State Locks, OpenTofu, EKS IRSA, OPA Guardrails. | Senior / DevOps |
| **Ansible Automation Master Guide** | [`ansible_master_guide.md`](devops-cicd-iac/ansible_master_guide.md) | Ansiballz Payloads, OpenSSH Multiplexing, Dynamic Cloud EC2, 22-Level Variable Scoping. | Senior / DevOps |
| **Chef Infra Master Guide** | [`chef_master_guide.md`](devops-cicd-iac/chef_master_guide.md) | Compile vs Converge Engine, Policyfiles, Ohai Kernel Profiling, InSpec Compliance. | Senior / DevOps |
| **Docker Mastery Lab** | [`topics/docker_mastery.md`](topics/docker_mastery.md) | Multi-stage builds, Layer caching, Bridge/Overlay networks, 335+ Troubleshooting points. | Lab / Practice |
| **Kubernetes Mastery Lab** | [`topics/kubernetes_mastery.md`](topics/kubernetes_mastery.md) | CrashLoopBackOff, OOMKilled, Ingress Controller debugging, 50+ Scenario Grid. | Lab / Practice |

---

## 🛡️ 9. Enterprise Security, Identity & Cryptography

| Document Title | File Link | Focus Areas & Technical Depth | Target Level |
| :--- | :--- | :--- | :--- |
| **Cryptography Algorithms Encyclopedia** | [`cryptography_algorithms_master_guide.md`](security-identity/cryptography_algorithms_master_guide.md) | AES-GCM, ChaCha20, RSA-OAEP, ECC, SHA-3, Argon2id, Post-Quantum (ML-KEM/Kyber), ZKP. | Staff / Security Lead |
| **Enterprise Security & Auth Masterclass** | [`security_auth_master_guide.md`](security-identity/security_auth_master_guide.md) | AuthN vs AuthZ, mTLS, FIDO2/WebAuthn, RBAC/ABAC/ReBAC, SAML 2.0, Active Directory, OAuth2. | Staff / Architect |
| **HashiCorp Vault Master Guide** | [`vault_secrets_master_guide.md`](security-identity/vault_secrets_master_guide.md) | Shamir's Secret Sharing, Transit Encryption-as-a-Service, Dynamic DB Credentials, Auto-Auth. | Senior / Security Lead |
| **OPA & Rego Architectural Masterclass** | [`opa_rego_200_scenarios_master_guide.md`](scenarios/opa_rego_200_scenarios_master_guide.md) | OPA AST Engine, PEP vs PDP, Modern Rego v1, 200 Real-World Production Scenarios. | Senior / Security Lead |
| **Security & Infrastructure 200 Scenarios** | [`security_infra_200_scenarios_master_guide.md`](scenarios/security_infra_200_scenarios_master_guide.md) | 9 Hands-on Zero-to-Hero Labs + 200 Production Scenarios across Auth, Proxies, Mesh. | Senior / Staff |
| **Master Dictionary, Tools & Labs** | [`security_infra_tools_glossary_master_guide.md`](security-identity/security_infra_tools_glossary_master_guide.md) | 120+ A-to-Z Security Terms + 5 Flagship Setup Labs (Istio, OAuth2, NGINX, Vault, OPA). | All Levels |
| **Java & Spring Cryptography Guide** | [`java_spring_cryptography_master_guide.md`](java-core/java_spring_cryptography_master_guide.md) | JCA/JCE, AES-256-GCM, RSA, Ed25519, Argon2id, KeyStore PKCS12, KMS Envelopes. | Staff / Security Lead |

---

## ⚛️ 10. Modern Web, Frontend & Cross-Platform Desktop

| Document Title | File Link | Focus Areas & Technical Depth | Target Level |
| :--- | :--- | :--- | :--- |
| **React & Modern Frontend Architecture** | [`react_master_guide.md`](frontend-web/react_master_guide.md) | Fiber Tree, Double-Buffering Swap, 31-Lane Scheduler, Hooks, RSC, Suspense, Error Boundaries. | Senior to Principal |
| **Next.js App Router & RSC Master Guide** | [`nextjs_rsc_master_guide.md`](frontend-web/nextjs_rsc_master_guide.md) | RSC Flight Protocol, Streaming SSR, Server Actions, 4-Tier Caching, Partial Prerendering. | Senior to Principal |
| **Angular Architecture Master Guide** | [`angular_master_guide.md`](frontend-web/angular_master_guide.md) | Ivy Incremental DOM, Signal Reactive Graph, Hierarchical DI, Zoneless CD, Standalone. | Senior to Principal |
| **Tauri 2.0 & Rust Desktop Guide** | [`tauri_rust_desktop_master_guide.md`](frontend-web/tauri_rust_desktop_master_guide.md) | IPC Protocol Mechanics, WRY Webview, Rust Invocations, CSP Sandboxing, Multi-Window. | Senior Engineer |
| **GraphQL Polyglot Masterclass** | [`graphql_polyglot_master_guide.md`](frontend-web/graphql_polyglot_master_guide.md) | AST Execution, SDL, N+1 DataLoader, Node.js (Apollo 4), Go (`gqlgen`), Java (Spring). | Senior / Architect |
| **gRPC Polyglot Masterclass** | [`grpc_polyglot_master_guide.md`](frontend-web/grpc_polyglot_master_guide.md) | Proto3 Varint Encoding, HTTP/2 Multiplexing, 4 RPC Patterns (Node, Go, Java), Deadlines. | Senior / Architect |

---

## ⚙️ 11. Systems Programming (Rust, Golang & C/C++)

| Document Title | File Link | Focus Areas & Technical Depth | Target Level |
| :--- | :--- | :--- | :--- |
| **Rust Systems Architecture & Concurrency** | [`rust_master_guide.md`](systems-languages/rust_master_guide.md) | Ownership, Borrow Checker, Affine Types, Tokio Work-Stealing, Dynamic Dispatch vtables. | Senior to Principal |
| **Golang Systems Architecture** | [`golang_master_guide.md`](systems-languages/golang_master_guide.md) | GMP Scheduler, M:N Work-Stealing, Stack Growth/Copying, Tri-Color GC, Channel `hchan`. | Senior to Principal |
| **C & C++ Architecture & Memory Guide** | [`c_cpp_master_guide.md`](systems-languages/c_cpp_master_guide.md) | Virtual Memory, Kernel Allocators (brk/mmap), Cache Bouncing, C++20 Move Semantics. | Senior to Principal |
| **Rust Technical Terms Encyclopedia** | [`rust_technical_terms_master_guide.md`](systems-languages/rust_technical_terms_master_guide.md) | Zero-Jargon Definitions & Deep Internals for 50+ Core Rust & Tokio concepts. | All Levels |
| **Golang Technical Terms Encyclopedia** | [`golang_technical_terms_master_guide.md`](systems-languages/golang_technical_terms_master_guide.md) | Zero-Jargon Definitions & Deep Internals for 50+ Core Go, Gin & Fiber concepts. | All Levels |
| **Rust & Go Dual Technical Terms** | [`rust_golang_technical_terms_master_guide.md`](systems-languages/rust_golang_technical_terms_master_guide.md) | Comparative terminology Rosetta Stone between Rust affine types and Go channels. | All Levels |

---

## 🧪 12. Testing, QA & Test Automation

| Document Title | File Link | Focus Areas & Technical Depth | Target Level |
| :--- | :--- | :--- | :--- |
| **Test Automation Master Guide** | [`test_automation_master_guide.md`](testing-qa/test_automation_master_guide.md) | Gherkin BDD, Selenium 4 W3C/CDP/BiDi, Playwright Node/Java, Auto-Waiting, Grid Sharding. | Senior / QA Architect |
| **Selenium 4 WebDriver Masterclass** | [`selenium.md`](testing-qa/selenium.md) | W3C Standard, POM Architecture, CDP Interception, Dynamic Waits, Parallel Execution. | Intermediate to Senior |
| **Selenium Reference Notes (Alt)** | [`selinum.md`](testing-qa/selinum.md) | Auxiliary setup notes on Selenium WebDriver. | Quick Reference |
| **Cucumber BDD Masterclass** | [`cucumber.md`](testing-qa/cucumber.md) | Gherkin, Cucumber Expressions, DataTables, PicoContainer DI, Step Definitions. | Intermediate to Senior |
| **Cucumber Reference Notes (Alt)** | [`cucmber.md`](testing-qa/cucmber.md) | Auxiliary reference notes on Cucumber BDD. | Quick Reference |

---

## 📊 13. Observability, Telemetry & SRE Troubleshooting

| Document Title | File Link | Focus Areas & Technical Depth | Target Level |
| :--- | :--- | :--- | :--- |
| **OpenTelemetry (OTel) Master Guide** | [`opentelemetry_master_guide.md`](observability-sre/opentelemetry_master_guide.md) | W3C Trace Context, OTel Collector DAG Engine, Tail-Based Sampling, High-Cardinality Metrics. | Staff / SRE Lead |
| **LGTM Stack Master Guide** | [`lgtm_master_guide.md`](observability-sre/lgtm_master_guide.md) | Loki LogQL, Tempo TraceQL, Mimir PromQL, OTel Tail-Sampling, S3 Object Storage, Exemplars. | Senior / SRE Lead |
| **Splunk & Observability Hub** | [`topics/observability_splunk_mastery.md`](topics/observability_splunk_mastery.md) | SPL Cheat Sheet, OpenTelemetry Traces, RED/USE Metrics, High-Throughput Aggregations. | Senior Engineer |
| **Universal Troubleshooting Guide** | [`topics/troubleshooting_mastery.md`](topics/troubleshooting_mastery.md) | Linux exit codes, K8s Pod CrashLoops, JVM OOMs, DB Deadlocks, Forensic Runbooks. | All Levels |
| **Chaos & Performance Testing** | [`topics/chaos_perf_microservices.md`](topics/chaos_perf_microservices.md) | k6 load scripts, Chaos Mesh latency/pod-kill, Flame graphs, Latency SLA degradation. | Senior / SRE |
| **Java Profiling Tools & Workflow** | [`new/_Java Profiling_ Tools and Workflow .md`](new/_Java%20Profiling_%20Tools%20and%20Workflow%20.md) | Detailed diagnostics workflow for CPU, heap, and thread profiling in Java. | Practical Guide |
| **Java Garbage Collection Step-by-Step** | [`new/_Java Garbage Collection Explained Step-by-Step .md`](new/_Java%20Garbage%20Collection%20Explained%20Step-by-Step%20.md) | Step-by-step visual tutorial on JVM memory management and GC collectors. | Tutorial |

---

## 🤖 14. Artificial Intelligence, Generative AI & Python

| Document Title | File Link | Focus Areas & Technical Depth | Target Level |
| :--- | :--- | :--- | :--- |
| **AI, GenAI & Prompt Master Guide** | [`ai_genai_master_guide.md`](ai-algorithms/ai_genai_master_guide.md) | Transformers ($Q, K, V$), Self-Attention, BPE, Training (SFT/RLHF/DPO), Vector DBs (HNSW), MCP. | Staff / AI Engineer |
| **Enterprise RAG & Vector Search Guide** | [`rag_vector_search_master_guide.md`](ai-algorithms/rag_vector_search_master_guide.md) | HNSW Graph Traversal, Hybrid Dense-Sparse (BM25 + pgvector), Cross-Encoder Rerankers. | Staff / AI Architect |
| **Python Engineering, Data & AI Guide** | [`python_master_guide.md`](systems-languages/python_master_guide.md) | CPython Internals, GIL, Metaclasses, NumPy, OpenCV, PyTorch, FastAPI, Django ORM. | Senior / Staff |

---

## 📚 15. Static Site Generators & Documentation Engines

| Document Title | File Link | Focus Areas & Technical Depth | Target Level |
| :--- | :--- | :--- | :--- |
| **Static Doc Engines Master Guide** | [`doc_generation_master_guide.md`](documentation-engines/doc_generation_master_guide.md) | Architectural Taxonomy, Search Mechanics, SSG Comparison Matrix, CI/CD Hosting. | Senior / Tech Lead |
| **VitePress Master Guide** | [`vitepress_master_guide.md`](documentation-engines/vitepress_master_guide.md) | Vite & Vue 3 SPA Hydration, Shiki Highlighting, Multi-Section Sidebar, MiniSearch. | Intermediate to Senior |
| **Material for MkDocs Master Guide** | [`mkdocs_material_master_guide.md`](documentation-engines/mkdocs_material_master_guide.md) | Zero-Node Python Engine, Horizontal Tabs, Lunr Search in Web Workers, Admonitions. | Intermediate to Senior |
| **Hugo & Hextra Master Guide** | [`hugo_master_guide.md`](documentation-engines/hugo_master_guide.md) | Go Single Binary Engine, Sub-100ms Builds across 1,000+ Pages, FlexSearch Tokenizer. | Intermediate to Senior |
| **Starlight (Astro) Master Guide** | [`starlight_astro_master_guide.md`](documentation-engines/starlight_astro_master_guide.md) | Component Islands, Zero Client JS, Pagefind 50KB Segmented Byte-Chunk Search. | Intermediate to Senior |
| **Docusaurus Master Guide** | [`docusaurus_master_guide.md`](documentation-engines/docusaurus_master_guide.md) | Meta's React Platform, MDX Interactive Components, Multi-Versioning Releases. | Intermediate to Senior |
| **Docsify Master Guide** | [`docsify_master_guide.md`](documentation-engines/docsify_master_guide.md) | Zero-Build Single `index.html` Runtime SPA, Client-Side `marked.js` Parsing. | Foundational |

---

## 🗣️ 16. Professional Communication & Spoken English

| Document Title | File Link | Focus Areas & Practical Scripts | Target Audience |
| :--- | :--- | :--- | :--- |
| **Spoken English & Communication Guide** | [`spoken_english_tamil_to_global_master_guide.md`](communication-english/spoken_english_tamil_to_global_master_guide.md) | Tamil-to-English Shift, 35 Tanglish Traps, Accent Neutralization, Essential Grammar. | Indian / Tamil Engineers |
| **500 Spoken English Scenarios** | [`spoken_english_500_scenarios_master_guide.md`](communication-english/spoken_english_500_scenarios_master_guide.md) | **500 Scenarios**: Hotels, Restaurants, Hospitals, Flights, Standups, Outages, Appraisals. | Global Workplace |
| **English Root Words & Etymology** | [`english_root_words_master_guide.md`](communication-english/english_root_words_master_guide.md) | 40 Core Greek & Latin Roots, Word Family Trees, Tech Etymology Matrix. | Vocabulary Builders |
| **Phrases & Phrasal Verbs Guide** | [`english_phrases_master_guide.md`](communication-english/english_phrases_master_guide.md) | 60 High-Impact Phrasal Verbs, Separable vs Inseparable, Corporate Set Phrases. | Fluency Enhancement |
| **Idioms & Corporate Metaphors** | [`english_idioms_master_guide.md`](communication-english/english_idioms_master_guide.md) | 70 Workplace Metaphors, Historical Naval/War Origins, Real Meaning, Tamil Parallels. | Business Conversational |
| **Advanced Vocabulary (IELTS & TOEFL)** | [`ielts_toefl_advanced_vocabulary_master_guide.md`](communication-english/ielts_toefl_advanced_vocabulary_master_guide.md) | 122 Band 8-9 Academic Words, 12 Master Themes, Collocations, US vs UK Matrix. | Exam & GCC Aspirants |
| **IELTS 500 Academic Lexicon** | [`ielts_500_words_master_guide.md`](communication-english/ielts_500_words_master_guide.md) | Complete 500 Academic Word List (AWL) A-to-Z with Tamil Intuition and Sentences. | IELTS Band 9 Target |
| **Business English & Corporate Words** | [`business_english_corporate_words_master_guide.md`](communication-english/business_english_corporate_words_master_guide.md) | 75 Corporate Strategy Terms, Top 25 Acronyms, 15 Diplomatic Email Rewrites. | Corporate Professionals |
| **IT Technical Words Master Guide** | [`it_tech_words_master_guide.md`](communication-english/it_tech_words_master_guide.md) | 75 Core Architectural Terms, PR Review Debates, Sev-1 Outage Communication Scripts. | Software Engineers |
| **English Root Words & Idioms (Combo)** | [`english_phrases_idioms_root_words_master_guide.md`](communication-english/english_phrases_idioms_root_words_master_guide.md) | Comprehensive combined compendium of roots, idioms, and workplace phrases. | Comprehensive Reference |

---

## 🧠 17. System Design & Technical Interview Preparation

| Document Title | File Link | Focus Areas & Technical Depth | Target Level |
| :--- | :--- | :--- | :--- |
| **System Design Masterclass** | [`system_design.md`](ai-algorithms/system_design.md) | DSA in Distributed Systems, GoF (LLD), HLD (TinyURL, Chat, Netflix, Uber, Rate Limiter). | Senior to Staff |
| **Original System Design Reference** | [`original_system_design.md`](ai-algorithms/original_system_design.md) | Archive of foundational system design architectures and trade-off analyses. | Reference |
| **Senior Architect Interview Guide** | [`topics/interview_prep.md`](topics/interview_prep.md) | System design rounds, Behavioral STAR methodology, Staff+ bar-raiser expectations. | Staff / Principal |
| **Regular Expressions Engineering** | [`regx.md`](ai-algorithms/regx.md) | Deterministic Finite Automata, Lookarounds, Captures, ReDoS protection, 110+ Recipes. | Intermediate to Senior |

---

## 🧩 18. Algorithms, Data Structures & LeetCode Patterns

| Document Title | File Link | Focus Areas & Algorithmic Patterns | Target Level |
| :--- | :--- | :--- | :--- |
| **LeetCode Patterns Comprehensive** | [`leetcode_patterns.md`](ai-algorithms/leetcode_patterns.md) | Two Pointers, Sliding Window, Fast/Slow, Monotonic Stack, Top K, Graph BFS/DFS, DP. | FAANG Interview Prep |
| **DSA Master Guide** | [`dsa_master_guide.md`](ai-algorithms/dsa_master_guide.md) | Core Data Structures, Trees, Graphs, Sorting, Hash Tables, Big-O Analysis. | Foundational to Advanced |

---

## 🔬 19. Specialized Topic Mastery Labs (`topics/`)

| Document Title | File Link | Domain & Lab Focus |
| :--- | :--- | :--- |
| **Chaos & Performance in Microservices** | [`topics/chaos_perf_microservices.md`](topics/chaos_perf_microservices.md) | Microservices resilience under simulated network latency and pod terminations. |
| **Docker Mastery: War Room Manual** | [`topics/docker_mastery.md`](topics/docker_mastery.md) | 335+ Troubleshooting points, multi-stage layer caching, network bridges. |
| **Technical Glossary** | [`topics/glossary.md`](topics/glossary.md) | Distributed systems, cloud native, networking and storage terminology dictionary. |
| **Interview Preparation** | [`topics/interview_prep.md`](topics/interview_prep.md) | High-stakes architecture interviews, whiteboarding techniques, STAR responses. |
| **Java Collections Mastery** | [`topics/java_collections_mastery.md`](topics/java_collections_mastery.md) | Algorithmic deep dive into JVM collection structures and hash collisions. |
| **Java Garbage Collection Mastery** | [`topics/java_gc_mastery.md`](topics/java_gc_mastery.md) | In-depth GC log analysis, card table scanning, generational allocation tuning. |
| **Java Profiling Mastery** | [`topics/java_profiling_mastery.md`](topics/java_profiling_mastery.md) | Async-profiler flame graphs, native memory tracking (NMT), off-heap leaks. |
| **Kubernetes Mastery** | [`topics/kubernetes_mastery.md`](topics/kubernetes_mastery.md) | CrashLoopBackOff, OOMKilled, Ingress Controller debugging, 50+ Scenario Grid. |
| **Maven Mastery** | [`topics/maven_mastery.md`](topics/maven_mastery.md) | Multi-module build optimization, dependency trees, conflict resolution. |
| **Splunk & Observability Mastery** | [`topics/observability_splunk_mastery.md`](topics/observability_splunk_mastery.md) | SPL aggregation queries, high-throughput log ingestion, dashboarding. |
| **Troubleshooting Mastery** | [`topics/troubleshooting_mastery.md`](topics/troubleshooting_mastery.md) | Universal forensic runbooks for Linux signals, K8s pod freezes, and JVM crashes. |

---

## 📦 20. Staged Roadmaps & Extended Modules (`new/`)

| Document Title | File Link | Staged Content Description |
| :--- | :--- | :--- |
| **Java Mastery Hub** | [`new/Java_Mastery_Hub.md`](new/Java_Mastery_Hub.md) | Full-stack Java backend curriculum map and topic roadmap. |
| **Docker Roadmap: Scratch to Advanced** | [`new/_Docker Roadmap_ From Scratch to Advanced .md`](new/_Docker%20Roadmap_%20From%20Scratch%20to%20Advanced%20.md) | Extended roadmap guide covering Docker containerization from ground zero. |
| **Kubernetes Zero to Hero Guide** | [`new/_Kubernetes Zero to Hero Guide .md`](new/_Kubernetes%20Zero%20to%20Hero%20Guide%20.md) | Comprehensive step-by-step introduction to container orchestration. |
| **Java Garbage Collection Step-by-Step** | [`new/_Java Garbage Collection Explained Step-by-Step .md`](new/_Java%20Garbage%20Collection%20Explained%20Step-by-Step%20.md) | Visual explanation of HotSpot heap partitions and collector lifecycles. |
| **Java Profiling: Tools and Workflow** | [`new/_Java Profiling_ Tools and Workflow .md`](new/_Java%20Profiling_%20Tools%20and%20Workflow%20.md) | Detailed diagnostics workflow for profiling memory, CPU, and thread lock contention. |
| **Maven Explained: Concepts & Workflow** | [`new/_Maven Explained_ Concepts and Workflow .md`](new/_Maven%20Explained_%20Concepts%20and%20Workflow%20.md) | Foundations of project object model, dependency management, and build plugins. |
| **Chat Export History Utility** | [`new/_Export Chat History to Markdown .md`](new/_Export%20Chat%20History%20to%20Markdown%20.md) | Technical utility guide for archiving and formatting conversation threads. |
