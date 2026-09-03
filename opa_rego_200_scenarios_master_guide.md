[🏠 Back to Home](README.md) | [🛡️ Security & Auth Master Guide](security_auth_master_guide.md) | [🌐 Microservices & Infrastructure Guide](microservices_gateway_infrastructure_master_guide.md) | [🔬 200 Scenarios & Setup Labs](security_infra_200_scenarios_master_guide.md) | [📖 Master Dictionary & Tools](security_infra_tools_glossary_master_guide.md) | [🏛️ System Design Guide](system_design.md)

# ⚖️ Open Policy Agent (OPA) & Rego: The Complete Architectural Masterclass, Built-In Reference, Rules Engines & 200 Production Scenarios

> **Target Audience:** Software Engineers, Security Architects, Platform/DevOps Engineers, and Site Reliability Engineers (SREs).  
> **Standard:** Written in **Rego v1** (the modern standard in OPA v1.0+ requiring explicit `if` and `contains` keywords).  
> **Prerequisites:** **Zero.** We start from the foundational mental model of Policy-as-Code and build up through language grammar, every built-in function, rules engine alternatives (Cedar, Kyverno, Drools, Oso), and **200 categorized real-world production scenarios**.

---

## 📑 Master Table of Contents
1. [🏛️ Part 1: OPA Architecture & Internal Engine Mechanics](#️-part-1-opa-architecture--internal-engine-mechanics)
   - [1.1 The Policy-as-Code Paradigm & Why We Need It](#11-the-policy-as-code-paradigm--why-we-need-it)
   - [1.2 PEP vs. PDP Architectural Decoupling](#12-pep-vs-pdp-architectural-decoupling)
   - [1.3 Internal Engine Mechanics: In-Memory AST & Sub-Millisecond Evaluation](#13-internal-engine-mechanics-in-memory-ast--sub-millisecond-evaluation)
   - [1.4 OPA Deployment Topologies (Daemon, Sidecar, K8s Webhook, WASM, Go SDK)](#14-opa-deployment-topologies-daemon-sidecar-k8s-webhook-wasm-go-sdk)
   - [1.5 Enterprise Management APIs (Bundle, Status, Decision Logs, Discovery)](#15-enterprise-management-apis-bundle-status-decision-logs-discovery)
2. [📖 Part 2: Rego Language Step-by-Step Tutorial (Modern Rego v1)](#-part-2-rego-language-step-by-step-tutorial-modern-rego-v1)
   - [2.1 File Structure: Packages, Imports, and Default Values](#21-file-structure-packages-imports-and-default-values)
   - [2.2 Conjunction (AND) vs. Disjunction (OR) Logic](#22-conjunction-and-vs-disjunction-or-logic)
   - [2.3 Modern Rego v1 Syntax: The `if` and `contains` Keywords](#23-modern-rego-v1-syntax-the-if-and-contains-keywords)
   - [2.4 Data Types and the Dual Document Model (`input` vs. `data`)](#24-data-types-and-the-dual-document-model-input-vs-data)
   - [2.5 Variables, Iteration (`some ... in ...`), and Universal Quantification (`every`)](#25-variables-iteration-some--in--and-universal-quantification-every)
   - [2.6 Partial Rules: Generating Dynamic Sets and Dictionaries](#26-partial-rules-generating-dynamic-sets-and-dictionaries)
   - [2.7 Comprehensions (Array, Set, and Object Comprehensions)](#27-comprehensions-array-set-and-object-comprehensions)
   - [2.8 User-Defined Functions](#28-user-defined-functions)
   - [2.9 Negation (`not`) and Undefined Value Safety](#29-negation-not-and-undefined-value-safety)
   - [2.10 Unit Testing & Test Coverage Framework (`opa test -v --coverage`)](#210-unit-testing--test-coverage-framework-opa-test--v---coverage)
   - [2.11 Performance Optimization, Indexing Tries & Partial Evaluation](#211-performance-optimization-indexing-tries--partial-evaluation)
3. [🧰 Part 3: The Complete Built-In Functions Reference Guide](#-part-3-the-complete-built-in-functions-reference-guide)
   - [3.1 Aggregation Functions](#31-aggregation-functions)
   - [3.2 Array & Set Manipulation](#32-array--set-manipulation)
   - [3.3 String Operations & Regular Expressions](#33-string-operations--regular-expressions)
   - [3.4 JSON & Object Transformation](#34-json--object-transformation)
   - [3.5 Cryptography, Hashing & JWT Verification](#35-cryptography-hashing--jwt-verification)
   - [3.6 Networking & CIDR IP Arithmetic](#36-networking--cidr-ip-arithmetic)
   - [3.7 Date, Time & Scheduling](#37-date-time--scheduling)
   - [3.8 Type Checking Functions](#38-type-checking-functions)
   - [3.9 Units & Byte Parsing](#39-units--byte-parsing)
   - [3.10 HTTP External Runtime Calls (`http.send`) & GraphQL](#310-http-external-runtime-calls-httpsend--graphql)
4. [⚔️ Part 4: Rules Engines & Policy Engines Like OPA](#️-part-4-rules-engines--policy-engines-like-opa)
   - [4.1 The Landscape: Policy Engines vs. Business Rules Engines](#41-the-landscape-policy-engines-vs-business-rules-engines)
   - [4.2 AWS Cedar (Amazon Verified Permissions)](#42-aws-cedar-amazon-verified-permissions)
   - [4.3 Kyverno (Kubernetes Native Policy Engine)](#43-kyverno-kubernetes-native-policy-engine)
   - [4.4 Oso & Polar (Application Authorization Engine)](#44-oso--polar-application-authorization-engine)
   - [4.5 Casbin (Multi-Language Access Control Framework)](#45-casbin-multi-language-access-control-framework)
   - [4.6 HashiCorp Sentinel](#46-hashicorp-sentinel)
   - [4.7 OpenFGA & Permify (Google Zanzibar ReBAC Engines)](#47-openfga--permify-google-zanzibar-rebac-engines)
   - [4.8 Apache Drools (Traditional BRMS & Rete Algorithm)](#48-apache-drools-traditional-brms--rete-algorithm)
   - [4.9 Camunda DMN (Decision Model and Notation)](#49-camunda-dmn-decision-model-and-notation)
   - [4.10 Easy Rules & JSON-Rules-Engine](#410-easy-rules--json-rules-engine)
   - [4.11 Grand Architectural Comparison Matrix](#411-grand-architectural-comparison-matrix)
5. [🏭 Part 5: The 200 Real-World Production Scenarios Master Matrix](#-part-5-the-200-real-world-production-scenarios-master-matrix)
   - [Category 1: API Gateway, Microservice AuthZ & Zero-Trust (Scenarios 1–45)](#category-1-api-gateway-microservice-authz--zero-trust-scenarios-145)
   - [Category 2: Kubernetes Admission Control & Security Governance (Scenarios 46–85)](#category-2-kubernetes-admission-control--security-governance-scenarios-4685)
   - [Category 3: Infrastructure as Code (Terraform/CloudFormation) Guardrails (Scenarios 86–125)](#category-3-infrastructure-as-code-terraformcloudformation-guardrails-scenarios-86125)
   - [Category 4: Data Protection, PII Masking & Column/Row Security (Scenarios 126–155)](#category-4-data-protection-pii-masking--columnrow-security-scenarios-126155)
   - [Category 5: CI/CD Pipeline & Supply Chain Security (Scenarios 156–180)](#category-5-cicd-pipeline--supply-chain-security-scenarios-156180)
   - [Category 6: Enterprise Governance, Financial Limits & Compliance (Scenarios 181–200)](#category-6-enterprise-governance-financial-limits--compliance-scenarios-181200)

---

# 🏛️ Part 1: OPA Architecture & Internal Engine Mechanics

---

## 1.1 The Policy-as-Code Paradigm & Why We Need It

### What Did We Have Before? (The Hardcoded Chaos)
Traditionally, authorization, security guardrails, and compliance checks were hardcoded directly into application source code:
```java
// Legacy Java application logic mixed with business rules
if (user.getRole().equals("ADMIN") || (user.getRole().equals("MANAGER") && request.getAmount() < 10000)) {
    executeTransaction();
} else {
    throw new UnauthorizedException();
}
```
**Fatal Flaws of the Hardcoded Approach:**
1. **Re-deployment Hell**: Updating a business policy (e.g., changing approval threshold from \$10k to \$15k) required modifying Java code, triggering CI/CD pipelines, unit tests, staging verification, and production server restarts.
2. **Polyglot Inconsistency**: An enterprise with services in Java, Python, Go, and Node.js had to duplicate the same security logic across 4 languages. Inevitably, subtle edge-case discrepancies introduced critical security vulnerabilities.
3. **Zero Visibility & Auditability**: Compliance auditors could not read compiled binary logic to certify that security controls were enforced.

### What is Policy-as-Code?
**Policy-as-Code (PaC)** treats business, security, and infrastructure rules as independent, declarative code stored in version control (Git). The logic of **what is allowed** is completely separated from **the software executing the action**.

---

## 1.2 PEP vs. PDP Architectural Decoupling

Open Policy Agent formalizes the standard **XACML (RFC 2904)** security architecture:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            POLICY WORKFLOW                                  │
│                                                                             │
│   [ Client / User ]                                                         │
│          │                                                                  │
│          ▼ 1. HTTP Request                                                  │
│   ┌───────────────┐  2. Query JSON (input)  ┌─────────────────────────┐     │
│   │      PEP      │────────────────────────►│           PDP           │     │
│   │ (API Gateway, │                         │   (Open Policy Agent)   │     │
│   │ Kubernetes,   │◄────────────────────────│                         │     │
│   │ Microservice) │  3. Decision (allow)    │ Reads:                  │     │
│   └──────┬────────┘                         │ - Rego Policies         │     │
│          │                                  │ - Cached Data (data)    │     │
│          ▼ 4. Execute Action                └─────────────────────────┘     │
│   [ Backend DB / Service ]                                                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

1. **Policy Enforcement Point (PEP)**: The service receiving the real-world request (e.g., Spring Cloud Gateway, Envoy Proxy, Kubernetes API Server, or custom backend). It does **not** make security decisions; it simply pauses execution, packages the context into a JSON payload (`input`), queries the PDP, and enforces the decision (`ALLOW` or `DENY`).
2. **Policy Decision Point (PDP)**: **Open Policy Agent**. It receives the JSON `input`, compiles and evaluates it against compiled Rego policy rules, and returns a JSON decision object (e.g., `{"allow": true, "reason": "Authorized by Manager rule"}`).

---

## 1.3 Internal Engine Mechanics: In-Memory AST & Sub-Millisecond Evaluation

How can OPA evaluate complex policies on 50,000 requests per second without crashing?
1. **Zero Disk I/O at Evaluation Time**: All Rego policy files and external context (`data`) are loaded into RAM.
2. **Abstract Syntax Tree (AST) Compilation**: When OPA starts or receives a policy bundle, it parses the textual Rego syntax into an optimized in-memory Abstract Syntax Tree.
3. **The Document Model (`input` vs. `data`)**:
   - `input`: Ephemeral, request-specific JSON document passed by the PEP (e.g., HTTP method, path, user claims, request body).
   - `data`: Static or semi-static organizational context cached in OPA's memory (e.g., LDAP group hierarchies, employee department mappings, role-permission matrices).
4. **Sub-Millisecond Evaluation**: Because evaluation is a pure in-memory graph traversal over the AST and JSON objects, decisions consistently execute in **under 1 millisecond** ($<0.5\text{ ms}$ for typical rules).

---

## 1.4 OPA Deployment Topologies

Depending on architecture, OPA can be deployed in 5 distinct patterns:

```
1. Daemon / Centralized Cluster   2. Kubernetes Admission Webhook   3. Envoy Sidecar (ext_authz)
   ┌─────────┐   HTTP/REST           ┌──────────┐                     ┌──────────┐  gRPC (localhost)
   │ Service │─────────────►[ OPA ]  │ K8s API  │──Webhook──►[ OPA ]  │  Envoy   │────────────►[ OPA ]
   └─────────┘              Daemon   │  Server  │            (Gate-   │  Proxy   │             Sidecar
                                     └──────────┘            keeper)  └────┬─────┘
4. Embedded Go Library                                                     │ (mTLS pass)
   ┌───────────────────────┐                                          ┌────▼─────┐
   │ Go App (Compiled SDK) │                                          │ App Pod  │
   │ ┌───────────────────┐ │                                          └──────────┘
   │ │ OPA Engine in RAM │ │      5. WebAssembly (WASM) Compiled Module
   │ └───────────────────┘ │         Compile Rego directly to `.wasm` binary; embed in Edge/Node/Rust/C.
   └───────────────────────┘
```

1. **Centralized Daemon**: OPA runs as an independent container/VM cluster exposed via HTTP REST (`POST /v1/data/<package>/<rule>`).
2. **Sidecar Proxy**: Co-located in the same Kubernetes Pod as the microservice. Queries travel over `localhost`, eliminating network hops.
3. **Envoy `ext_authz` Sidecar**: Envoy proxy intercepts inbound Layer 7 traffic and queries OPA over high-speed gRPC before passing traffic to the container.
4. **Kubernetes Admission Controller (Gatekeeper)**: OPA intercepts `kubectl apply` requests at the API Server to validate or mutate Kubernetes manifests.
5. **WebAssembly (WASM)**: Policies are pre-compiled into portable `.wasm` binaries and executed inside edge CDNs (Cloudflare Workers), Node.js, or Rust binaries with zero OPA daemon overhead.

---

## 1.5 Enterprise Management APIs

In large-scale production, OPA runs as a fully autonomous managed node using 4 standardized control plane APIs:

| API Name | Protocol | Purpose & Mechanics |
| :--- | :--- | :--- |
| **Bundle API** | HTTP GET / S3 | OPA periodically polls an S3 bucket or HTTP server, downloads a `.tar.gz` bundle of signed Rego policies and JSON data, and hot-reloads them atomically in RAM with zero downtime. |
| **Status API** | HTTP POST | OPA reports its operational health, bundle download status, last successful reload timestamp, and error metrics to a central management server (Styra DAS). |
| **Decision Logs API** | HTTP POST | OPA buffers every query (`input`), rule evaluated, and final decision, asynchronously streaming compressed JSON batches to logging backends (Elasticsearch, S3, Splunk) for audit compliance. |
| **Discovery API** | HTTP GET | When OPA boots up with a minimal config, it queries the Discovery API to dynamically receive its full enterprise configuration (bundle URLs, credential secrets, log destinations). |

---

# 📖 Part 2: Rego Language Step-by-Step Tutorial (Modern Rego v1)

Rego is a declarative query language based on **Datalog**, inspired by Prolog. You declare *what* must be true, not *how* to loop and calculate it.

---

## 2.1 File Structure: Packages, Imports, and Default Values

Every Rego file begins with a `package` declaration:

```rego
# File: authz.rego
package authz.api

# Import sub-packages or future keyword features
import rego.v1

# 1. Default assignment: If no rules match, allow evaluates to false (Deny-by-default)
default allow := false

# 2. Allow rule
allow if {
    input.user.role == "ADMIN"
}
```
- `package`: Defines the hierarchical document path in OPA's data tree. The rule above is queried at `/v1/data/authz/api/allow`.
- `default`: Prevents undefined behavior. If a policy produces no output, OPA assigns the fallback default value (`false`).

---

## 2.2 Conjunction (AND) vs. Disjunction (OR) Logic

### Conjunction (AND): All Expressions Must Be True
Inside a single rule body `{ ... }`, expressions are joined by **AND**:
```rego
package authz.demo
import rego.v1

# User can proceed IF role is EDITOR AND method is GET AND department is News
allow if {
    input.user.role == "EDITOR"
    input.request.method == "GET"
    input.user.department == "News"
}
```

### Disjunction (OR): Multiple Rules with the Same Name
To express **OR**, declare the same rule multiple times:
```rego
package authz.demo
import rego.v1

default allow := false

# Condition 1: Admins can do anything
allow if {
    input.user.role == "ADMIN"
}

# Condition 2: OR anyone can access public endpoints
allow if {
    input.request.path == "/public"
    input.request.method == "GET"
}

# Condition 3: OR resource owners can access their own resources
allow if {
    input.user.id == input.resource.owner_id
}
```

---

## 2.3 Modern Rego v1 Syntax: The `if` and `contains` Keywords

In legacy Rego, the `if` keyword was optional, leading to confusing code. **Rego v1** establishes strict, unambiguous standards:
1. **Mandatory `if`**: Rule bodies **must** be preceded by `if`.
2. **Mandatory `contains`**: Set-generation rules **must** use the `contains` keyword.
3. **Strict Assignment (`:=`) vs Equality (`=`)**:
   - `:=` assigns a value to a new local variable within scope.
   - `==` checks boolean equality between two values.
   - `=` performs mathematical unification (solves variables to make expressions equal).

```rego
package authz.v1_syntax
import rego.v1

# GOOD (Rego v1): Explicit 'if'
allow if {
    input.role == "ADMIN"
}

# GOOD (Rego v1): Explicit 'contains' for partial set generation
authorized_tags contains "secure" if {
    input.verified == true
}

authorized_tags contains "audited" if {
    input.audit_log_enabled == true
}
```

---

## 2.4 Data Types and the Dual Document Model (`input` vs. `data`)

Rego natively supports all JSON data types:
- **Scalars**: String (`"admin"`), Number (`42`, `3.14`), Boolean (`true`, `false`), Null (`null`).
- **Collections**: Array (`["a", "b"]`), Set (`{"a", "b"}`), Object (`{"role": "admin", "age": 30}`).

### Querying the `data` Document
Suppose OPA's memory contains this cached company directory (`data`):
```json
{
  "departments": {
    "finance": ["alice", "bob"],
    "engineering": ["charlie", "david"]
  }
}
```
Your Rego rule references it via `data.departments`:
```rego
package authz.departments
import rego.v1

default allow := false

allow if {
    # Check if input.user exists in the finance team array
    input.user == data.departments.finance[_]
}
```

---

## 2.5 Variables, Iteration (`some ... in ...`), and Universal Quantification (`every`)

### Iteration with `some ... in ...`
Instead of imperative `for` loops, Rego searches collections using `some ... in`:
```rego
package authz.iteration
import rego.v1

default allow := false

# Allow if ANY role assigned to the user has the 'write' permission
allow if {
    some role in input.user.roles
    role.permission == "write"
}
```

### Universal Quantification (`every`)
To verify that **all** elements satisfy a condition, use `every`:
```rego
package authz.quantification
import rego.v1

default all_ports_secure := false

# Verify that EVERY port defined in a Kubernetes pod is above 1024
all_ports_secure if {
    every port in input.spec.containers[_].ports {
        port.containerPort > 1024
    }
}
```

---

## 2.6 Partial Rules: Generating Dynamic Sets and Dictionaries

Rego rules can generate complex sets or dictionaries dynamically based on evaluations.

### Generating a Set of Denied Reasons
```rego
package compliance.audit
import rego.v1

# Generates a Set containing all violations
violation contains "Missing required team label" if {
    not input.labels.team
}

violation contains "Cost-center label must be uppercase" if {
    input.labels.cost_center != upper(input.labels.cost_center)
}

violation contains "Replica count cannot exceed 10" if {
    input.spec.replicas > 10
}
```
*If all 3 conditions trigger, `data.compliance.audit.violation` returns:*
```json
["Missing required team label", "Cost-center label must be uppercase", "Replica count cannot exceed 10"]
```

### Generating a Key-Value Dictionary
```rego
package authz.routing
import rego.v1

# Maps each tenant to their designated backend upstream
upstream_routing[tenant] := backend if {
    some tenant, info in data.tenants
    backend := sprintf("https://%s.internal-cluster.local", [info.cluster_id])
}
```

---

## 2.7 Comprehensions (Array, Set, and Object Comprehensions)

Comprehensions construct new collections by filtering and transforming data in a single line:

```rego
package data.transforms
import rego.v1

# 1. Array Comprehension: Double all even numbers
numbers := [1, 2, 3, 4, 5, 6]
doubled_evens := [x * 2 | some x in numbers; x % 2 == 0]
# Output: [4, 8, 12]

# 2. Set Comprehension: Extract unique roles across all user tokens
unique_roles := {role | some token in input.tokens; some role in token.roles}

# 3. Object Comprehension: Invert a map of User -> Department into Department -> List of Users
user_departments := {"alice": "IT", "bob": "HR", "charlie": "IT"}
dept_members := {dept: users |
    some _, dept in user_departments
    users := [user | some user, d in user_departments; d == dept]
}
# Output: {"IT": ["alice", "charlie"], "HR": ["bob"]}
```

---

## 2.8 User-Defined Functions

Functions encapsulate reusable logic:
```rego
package authz.functions
import rego.v1

# Function to check if a string ends with corporate domain
is_corporate_email(email) if {
    endswith(email, "@enterprise.com")
}

# Function returning a computed value
calculate_discount(tier) := 0.20 if { tier == "PLATINUM" }
calculate_discount(tier) := 0.10 if { tier == "GOLD" }
calculate_discount(tier) := 0.00 if { not tier in ["PLATINUM", "GOLD"] }

default allow := false

allow if {
    is_corporate_email(input.user.email)
    calculate_discount(input.user.tier) > 0.05
}
```

---

## 2.9 Negation (`not`) and Undefined Value Safety

In Rego, if an attribute is missing (e.g., `input.user.address`), the expression is **undefined**, not `false` or `null`.
- A rule body evaluates to true **only if all expressions are defined and true**.
- Negation (`not`) turns an undefined or false expression into `true`.

```rego
package authz.safety
import rego.v1

default is_external_user := false

# SAFE: Variable 'user' is bound first, then checked for negation
is_external_user if {
    user := input.user
    not user.is_employee == true
}
```

---

## 2.10 Unit Testing & Test Coverage Framework (`opa test -v --coverage`)

OPA includes an industrial-grade built-in test runner. Test rules begin with `test_`:

```rego
# File: authz_test.rego
package authz.api
import rego.v1

# Test 1: Admin should be allowed
test_admin_allowed if {
    allow with input as {"user": {"role": "ADMIN"}}
}

# Test 2: Standard user should be denied
test_standard_user_denied if {
    not allow with input as {"user": {"role": "USER"}}
}

# Test 3: Missing role attribute should be denied
test_missing_role_denied if {
    not allow with input as {}
}
```

### Running Tests from the CLI
```bash
# Execute unit tests with verbose output and line-by-line code coverage
opa test -v --coverage .

# Output:
# authz_test.rego:
#   data.authz.api.test_admin_allowed: PASSED (1.2ms)
#   data.authz.api.test_standard_user_denied: PASSED (0.4ms)
#   data.authz.api.test_missing_role_denied: PASSED (0.3ms)
# ------------------------------------------------------------------------
# PASS: 3/3
# Coverage: 100.0%
```

---

## 2.11 Performance Optimization, Indexing Tries & Partial Evaluation

1. **Rule Indexing Trie**: OPA automatically analyzes rules of the form `input.method == "GET"` and builds an in-memory prefix trie. Instead of evaluating 500 rules sequentially ($O(N)$), OPA does an $O(1)$ hash table lookup.
2. **Avoid $O(N^2)$ Cartesian Cross-Products**:
   ```rego
   # BAD: Generates N x M combinations
   some x in input.huge_list_a
   some y in input.huge_list_b
   x == y

   # GOOD: Use Set Intersection (O(N + M))
   set_a := {x | some x in input.huge_list_a}
   set_b := {y | some y in input.huge_list_b}
   count(set_a & set_b) > 0
   ```
3. **Partial Evaluation (SQL Pushdown)**: OPA can partially evaluate a policy with unknown variables, emitting a simplified set of SQL `WHERE` clauses that can be pushed directly to a database engine.

---

# 🧰 Part 3: The Complete Built-In Functions Reference Guide

---

## 3.1 Aggregation Functions
| Function | Syntax & Input | Output | Runnable Rego Example |
| :--- | :--- | :--- | :--- |
| `count` | `count(collection)` | Number | `count(["a", "b", "c"]) == 3` |
| `sum` | `sum(array_or_set)` | Number | `sum([10, 20, 30]) == 60` |
| `max` | `max(collection)` | Number | `max([5, 99, 12]) == 99` |
| `min` | `min(collection)` | Number | `min([5, 99, 12]) == 5` |
| `sort` | `sort(array_or_set)` | Array | `sort([3, 1, 2]) == [1, 2, 3]` |

---

## 3.2 Array & Set Manipulation
| Function | Syntax & Input | Output | Runnable Rego Example |
| :--- | :--- | :--- | :--- |
| `array.concat` | `array.concat(arr1, arr2)` | Array | `array.concat([1, 2], [3, 4]) == [1, 2, 3, 4]` |
| `array.slice` | `array.slice(arr, start, end)` | Array | `array.slice(["a", "b", "c", "d"], 1, 3) == ["b", "c"]` |
| `array.reverse` | `array.reverse(arr)` | Array | `array.reverse([1, 2, 3]) == [3, 2, 1]` |
| `intersection` | `set1 & set2` | Set | `{"a", "b"} & {"b", "c"} == {"b"}` |
| `union` | `set1 \| set2` | Set | `{"a"} \| {"b"} == {"a", "b"}` |
| `difference` | `set1 - set2` | Set | `{"a", "b"} - {"b"} == {"a"}` |

---

## 3.3 String Operations & Regular Expressions
| Function | Syntax & Input | Output | Runnable Rego Example |
| :--- | :--- | :--- | :--- |
| `startswith` | `startswith(str, prefix)` | Boolean | `startswith("https://google.com", "https://") == true` |
| `endswith` | `endswith(str, suffix)` | Boolean | `endswith("archive.tar.gz", ".tar.gz") == true` |
| `contains` | `contains(str, substr)` | Boolean | `contains("user:admin:write", "admin") == true` |
| `split` | `split(str, delimiter)` | Array | `split("prod-us-east-1", "-") == ["prod", "us", "east", "1"]` |
| `concat` | `concat(delim, [str1, str2])` | String | `concat("/", ["api", "v1", "orders"]) == "api/v1/orders"` |
| `lower` | `lower(str)` | String | `lower("ADMIN") == "admin"` |
| `upper` | `upper(str)` | String | `upper("admin") == "ADMIN"` |
| `trim` | `trim(str, cutset)` | String | `trim("   hello world   ", " ") == "hello world"` |
| `sprintf` | `sprintf(format, values)` | String | `sprintf("User %s has %d items", ["Alice", 5])` |
| `regex.match` | `regex.match(pattern, str)` | Boolean | `regex.match("^[a-z0-9_-]{3,16}$", "john_doe") == true` |
| `regex.find_all_string_submatch_n` | `regex.find_all_string_submatch_n(pat, str, n)` | Array | Extracts regex capture groups into an array of matches. |

---

## 3.4 JSON & Object Transformation
| Function | Syntax & Input | Output | Runnable Rego Example |
| :--- | :--- | :--- | :--- |
| `json.is_valid` | `json.is_valid(str)` | Boolean | `json.is_valid("{\"key\": \"value\"}") == true` |
| `json.unmarshal` | `json.unmarshal(str)` | Any | `json.unmarshal("{\"a\": 1}").a == 1` |
| `json.marshal` | `json.marshal(val)` | String | Serializes an in-memory Rego object into a JSON string. |
| `json.filter` | `json.filter(obj, paths)` | Object | Whitelists only specified paths in an object. |
| `object.get` | `object.get(obj, key, default)` | Any | `object.get({"a": 1}, "b", 99) == 99` (Safe default). |
| `object.union` | `object.union(obj1, obj2)` | Object | Merges two JSON objects; colliding keys take obj2 value. |
| `object.remove` | `object.remove(obj, keys)` | Object | Removes a set of keys from an object (e.g. stripping passwords). |

---

## 3.5 Cryptography, Hashing & JWT Verification
| Function | Syntax & Input | Output | Runnable Rego Example |
| :--- | :--- | :--- | :--- |
| `io.jwt.decode` | `io.jwt.decode(token_str)` | Array `[header, payload, sig]` | `[_, payload, _] := io.jwt.decode(input.token); payload.sub == "alice"` |
| `io.jwt.verify_rs256` | `io.jwt.verify_rs256(token, cert_pem)` | Boolean | Cryptographically verifies RSA-256 JWT signature against public key. |
| `io.jwt.verify_hs256` | `io.jwt.verify_hs256(token, secret)` | Boolean | Verifies HMAC-SHA256 JWT signature against a shared secret. |
| `crypto.sha256` | `crypto.sha256(str)` | String | `crypto.sha256("password")` returns 64-char hex digest. |
| `crypto.hmac.equal` | `crypto.hmac.equal(sig1, sig2)` | Boolean | Timing-safe cryptographic comparison preventing timing attacks. |
| `base64.encode` | `base64.encode(str)` | String | `base64.encode("admin:secret") == "YWRtaW46c2VjcmV0"` |
| `base64.decode` | `base64.decode(str)` | String | `base64.decode("YWRtaW46c2VjcmV0") == "admin:secret"` |

---

## 3.6 Networking & CIDR IP Arithmetic
| Function | Syntax & Input | Output | Runnable Rego Example |
| :--- | :--- | :--- | :--- |
| `net.cidr_contains` | `net.cidr_contains("10.0.0.0/8", "10.1.2.3")` | Boolean | Returns `true` if IP address falls inside subnet CIDR block. |
| `net.cidr_intersects` | `net.cidr_intersects(cidr1, cidr2)` | Boolean | Returns `true` if two IP subnets overlap. |
| `net.cidr_expand` | `net.cidr_expand(cidr)` | Array | Expands a CIDR block into its canonical network representation. |
| `net.cidr_contains_matches` | `net.cidr_contains_matches(cidrs, ips)` | Set | Returns all IP-CIDR match pairs in a single operation. |

---

## 3.7 Date, Time & Scheduling
| Function | Syntax & Input | Output | Runnable Rego Example |
| :--- | :--- | :--- | :--- |
| `time.now_ns` | `time.now_ns()` | Number | Returns current Unix epoch time in nanoseconds. |
| `time.date` | `time.date(nanoseconds)` | Array `[year, month, day]` | `[2026, 9, 3] := time.date(time.now_ns())` |
| `time.clock` | `time.clock(nanoseconds)` | Array `[hour, min, sec]` | `[14, 30, 0] := time.clock(time.now_ns())` |
| `time.weekday` | `time.weekday(nanoseconds)` | String | Returns `"Monday"`, `"Tuesday"`, etc. |
| `time.diff` | `time.diff(ns1, ns2)` | Array `[years, months, days, h, m, s]` | Measures precise elapsed duration between two timestamps. |
| `time.parse_rfc3339_ns`| `time.parse_rfc3339_ns("2026-09-03T10:00:00Z")` | Number | Parses ISO8601/RFC3339 string into nanoseconds. |

---

## 3.8 Type Checking Functions
| Function | Checks If Argument Is | Runnable Rego Example |
| :--- | :--- | :--- |
| `is_string` | String | `is_string(input.name) == true` |
| `is_number` | Integer or Float | `is_number(input.port) == true` |
| `is_boolean` | `true` or `false` | `is_boolean(input.enabled) == true` |
| `is_array` | JSON Array | `is_array(input.items) == true` |
| `is_set` | Rego Set | `is_set({"a", "b"}) == true` |
| `is_object` | JSON Object / Dictionary | `is_object(input.headers) == true` |
| `is_null` | JSON `null` | `is_null(input.deleted_at) == true` |

---

## 3.9 Units & Byte Parsing
| Function | Syntax & Input | Output | Runnable Rego Example |
| :--- | :--- | :--- | :--- |
| `units.parse_bytes` | `units.parse_bytes("4Gi")` | Number (Bytes) | Converts human strings (`"500MB"`, `"2GiB"`, `"10KB"`) to raw integer bytes (`4294967296`). Essential for memory limit policies. |

---

## 3.10 HTTP External Runtime Calls (`http.send`) & GraphQL
| Function | Syntax & Input | Output | Runnable Rego Example |
| :--- | :--- | :--- | :--- |
| `http.send` | `http.send({"method": "GET", "url": "..."})` | Object `{status_code, body, ...}` | Queries live external REST API at runtime during policy evaluation (includes built-in response caching). |
| `graphql.is_valid` | `graphql.is_valid(query, schema)` | Boolean | Verifies whether a GraphQL query conforms to schema. |
| `graphql.parse_and_verify` | `graphql.parse_and_verify(query, schema)` | Object | Validates and parses GraphQL query AST to inspect query depth. |

---

# ⚔️ Part 4: Rules Engines & Policy Engines Like OPA

---

## 4.1 The Landscape: Policy Engines vs. Business Rules Engines

The industry divides decision engines into two fundamentally distinct categories:
1. **Policy-as-Code (Security & Governance Engines)**: Designed for **Security, Access Control, and Infrastructure Guardrails**. They evaluate queries in $<1\text{ ms}$, reject-by-default, operate over JSON/YAML, and are distributed across edge proxies and Kubernetes nodes (OPA, Cedar, Kyverno, Oso).
2. **Business Rules Management Systems (BRMS / Rules Engines)**: Designed for **Complex Business Logic & Decision Tables**. They manage thousands of rules, support stateful inference engines (Rete algorithm), calculate pricing, insurance risk, loan underwriting, and involve non-technical business analysts (Drools, Camunda DMN).

---

## 4.2 AWS Cedar (Amazon Verified Permissions)
- **What It Is**: An open-source language and engine developed by AWS designed specifically for fine-grained authorization.
- **Language**: Custom declarative language (`permit` and `forbid`).
- **Under the Hood**: Uses **Automated Reasoning (SMT Solvers)** to mathematically prove that your policies never contain conflicting or contradictory rules.
- **Key Syntax**:
  ```cedar
  // AWS Cedar Rule
  permit (
      principal in Role::"SupportEngineer",
      action in [Action::"ReadTicket", Action::"UpdateTicket"],
      resource in Department::"CustomerSupport"
  )
  when { context.mfa_authenticated == true };
  ```
- **Strengths**: Deterministic execution, formal mathematical verification, zero-crash guarantees, native integration with AWS Verified Permissions.
- **When to Use Over OPA**: When you need pure application authorization (who can read/edit what) with mathematical auditability, rather than general-purpose infrastructure validation.

---

## 4.3 Kyverno (Kubernetes Native Policy Engine)
- **What It Is**: A CNCF policy engine built **exclusively for Kubernetes**.
- **Language**: Pure **Kubernetes YAML**. No Rego, no programming language to learn!
- **Capabilities**: **Validate** (allow/block), **Mutate** (auto-inject sidecars or labels), and **Generate** (auto-create Secrets or NetworkPolicies when a Namespace is created).
- **Key Syntax**:
  ```yaml
  apiVersion: kyverno.io/v1
  kind: ClusterPolicy
  metadata:
    name: require-run-as-non-root
  spec:
    validationFailureAction: Enforce
    rules:
      - name: check-run-as-non-root
        match:
          resources:
            kinds: [Pod]
        validate:
          message: "Running as root is forbidden!"
          pattern:
            spec:
              securityContext:
                runAsNonRoot: true
  ```
- **When to Use Over OPA**: When your *only* requirement is Kubernetes cluster governance and your team wants native YAML without learning Rego.

---

## 4.4 Oso & Polar (Application Authorization Engine)
- **What It Is**: A developer-centric authorization library and managed cloud service specializing in application-level RBAC, ABAC, and ReBAC (Zanzibar-style relationships).
- **Language**: **Polar**, a declarative logic programming language inspired by Prolog.
- **Under the Hood**: Embeds directly into Python, Node.js, Ruby, Go, and Java applications as a native library or runs as a cloud service.
- **Key Syntax**:
  ```polar
  # Polar policy in Oso
  actor User {}
  resource Repository {
      roles = ["reader", "admin"];
      permissions = ["read", "delete"];
      "read" if "reader";
      "delete" if "admin";
  }
  ```
- **When to Use Over OPA**: When building complex multi-tenant SaaS application permissions with data-filtering SQL integration.

---

## 4.5 Casbin (Multi-Language Access Control Framework)
- **What It Is**: An authorization library supporting dozens of languages (Go, Java, Python, C++, Rust, Node.js) based on the **PERM metamodel (Policy, Effect, Request, Matchers)**.
- **How It Works**: You define an authorization model in a `.conf` file and store policies in SQL/Redis.
- **Key Syntax**:
  ```ini
  [request_definition]
  r = sub, obj, act

  [policy_definition]
  p = sub, obj, act

  [policy_effect]
  e = some(where (p.eft == allow))

  [matchers]
  m = r.sub == p.sub && r.obj == p.obj && r.act == p.act
  ```
- **When to Use Over OPA**: When you need a lightweight, in-process authorization library directly embedded into monolithic or microservice code without running a sidecar daemon.

---

## 4.6 HashiCorp Sentinel
- **What It Is**: HashiCorp's proprietary Policy-as-Code framework deeply integrated into **Terraform Enterprise, Vault Enterprise, and Consul Enterprise**.
- **Language**: Sentinel (Python-like procedural/declarative hybrid).
- **Enforcement Levels**: `advisory` (warning), `soft-mandatory` (can be overridden by manager), `hard-mandatory` (strict block).
- **When to Use Over OPA**: When your organization is heavily standardized on HashiCorp Enterprise tooling.

---

## 4.7 OpenFGA & Permify (Google Zanzibar ReBAC Engines)
- **What It Is**: High-scale, relationship-based access control engines modeled after **Google Zanzibar**.
- **How It Works**: Permissions are modeled as directed graph edges (`user:alice is member of group:eng`, `group:eng has reader access to doc:123`).
- **When to Use Over OPA**: When permissions depend on deep graph hierarchies (e.g., Google Drive folders containing documents, or GitHub organizations with teams and repositories).

---

## 4.8 Apache Drools (Traditional BRMS & Rete Algorithm)
- **What It Is**: The enterprise gold standard for **Business Rules Management Systems (BRMS)** in Java.
- **Under the Hood**: Uses the **Rete algorithm** (pattern matching on working memory) to execute forward and backward chaining inference over millions of business facts.
- **Language**: DRL (Drools Rule Language) and Excel Decision Tables.
- **Key Syntax**:
  ```drl
  rule "Credit Card Approval Limit"
      when
          $applicant : Applicant( creditScore > 750, annualIncome > 80000 )
      then
          $applicant.setApprovedLimit( 25000 );
  end
  ```
- **When to Use Over OPA**: Complex insurance underwriting, dynamic loan approvals, tax calculations, and telecom billing rules.

---

## 4.9 Camunda DMN (Decision Model and Notation)
- **What It Is**: An OMG open standard for authoring business decision logic in graphical **Decision Tables**.
- **How It Works**: Visual tables with Inputs on the left and Outputs on the right, integrated into BPMN workflow engines.
- **When to Use Over OPA**: When non-technical business managers, compliance officers, and underwriters must visually inspect, edit, and certify decision rules without touching code.

---

## 4.10 Easy Rules & JSON-Rules-Engine
- **Easy Rules (Java)**: A lightweight, POJO-based rules engine for Java that executes simple `when/then` rules without the complexity of Drools.
- **JSON-Rules-Engine (Node.js)**: A declarative rules engine where rules are expressed purely as JSON objects, ideal for web apps and dynamic frontend forms.

---

## 4.11 Grand Architectural Comparison Matrix

| Tool | Paradigm | Language | Typical Latency | Primary Use Case | Best Suited For | Limitations |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **OPA (Open Policy Agent)**| Policy-as-Code | Rego (Declarative Datalog) | $<1\text{ ms}$ | Universal Policy Engine (K8s, APIs, Terraform, Envoy). | Enterprise-wide consistency across the entire cloud-native stack. | Steep learning curve for Rego; not designed for relationship graphs. |
| **AWS Cedar** | Policy-as-Code | Cedar (Declarative) | $<0.5\text{ ms}$ | Fine-grained application authorization. | High-performance, mathematically proven app access control. | Limited to authorization (cannot validate K8s manifests or IaC). |
| **Kyverno** | K8s Policy | Kubernetes YAML | $<2\text{ ms}$ | Kubernetes cluster governance, mutation & generation. | Kubernetes platform teams wanting zero-learning-curve YAML. | Strictly limited to Kubernetes; cannot protect APIs or Terraform. |
| **Oso** | App AuthZ | Polar (Declarative logic) | $<1\text{ ms}$ | Application-level RBAC, ABAC, ReBAC & SQL data filtering. | Developers building SaaS user permission models. | Requires application SDK integration. |
| **Casbin** | Access Framework | Conf + Multi-lang | $<0.1\text{ ms}$ | In-process authorization library. | Direct embedding into Go/Java/Python monolithic backends. | Configuration syntax can be cryptic; limited cloud-native integrations. |
| **OpenFGA** | ReBAC Engine | Graph tuples | $5\text{--}15\text{ ms}$ | Google Zanzibar-style relationship permissions (Drive/GitHub). | Deep hierarchical graph permissions and group inheritance. | Higher operational complexity; requires distributed database cluster. |
| **Apache Drools** | Business BRMS | DRL / Decision Tables | $10\text{--}50\text{ ms}$ | Complex enterprise business rules, insurance & underwriting. | Complex inference calculations over large stateful Java objects. | Heavy JVM footprint; complex deployment; not suitable for API edge. |
| **Camunda DMN** | Decision Tables | DMN Visual XML | $10\text{--}30\text{ ms}$ | Business process decision automation. | Visual decision tables maintained by non-technical business analysts.| Slower execution; tied to workflow engines. |

---

# 🏭 Part 5: The 200 Real-World Production Scenarios Master Matrix

---

## Category 1: API Gateway, Microservice AuthZ & Zero-Trust (Scenarios 1–45)

### Scenario 1: Expired JWT Token Pass-Through
- **Problem & Symptom**: Microservices crash with HTTP 500 or process unauthenticated requests when an expired JWT is presented.
- **Root Cause**: Upstream edge gateway stripped validation to reduce latency; services blindly trusted the payload.
- **How OPA Solves It**: OPA decodes the JWT and validates that the `exp` timestamp is strictly greater than `time.now_ns()`.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package api.authz
  import rego.v1

  default allow := false

  allow if {
      [_, payload, _] := io.jwt.decode(input.token)
      current_time := time.now_ns() / 1000000000
      payload.exp > current_time
  }
  ```
- **How It Helps**: Guarantees zero expired tokens reach downstream microservices with sub-millisecond evaluation.

### Scenario 2: BOLA / IDOR Attack on User Profile Endpoint
- **Problem & Symptom**: Attacker logs in as Alice (ID `101`) and requests `/api/v1/users/102/billing`, exposing Bob's credit card.
- **Root Cause**: Endpoint checked if the requester was logged in, but never verified ownership of the path parameter ID.
- **How OPA Solves It**: OPA extracts the user ID from the verified JWT `sub` claim and compares it to the URI path parameter.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package api.authz
  import rego.v1

  default allow := false

  allow if {
      [_, payload, _] := io.jwt.decode(input.token)
      path_segments := split(trim(input.request.path, "/"), "/")
      requested_user_id := path_segments[3] # /api/v1/users/{id}/billing
      payload.sub == requested_user_id
  }
  ```
- **How It Helps**: Eliminates OWASP API Security Vulnerability #1 without altering backend database code.

### Scenario 3: BFLA Protection (Restricting HTTP DELETE to Admin Role)
- **Problem & Symptom**: Regular users trigger `DELETE /api/v1/accounts/50` by sending raw cURL commands directly to the gateway.
- **Root Cause**: Gateway only checked route accessibility, leaving method constraints to backend handlers.
- **How OPA Solves It**: Enforces that any request using `DELETE` or `PUT` requires the `ADMIN` role in `input.user.roles`.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package api.authz
  import rego.v1

  default allow := false

  # Safe read operations allowed for all authenticated users
  allow if {
      input.request.method == "GET"
      input.user.authenticated == true
  }

  # Destructive operations require ADMIN role
  allow if {
      input.request.method in ["DELETE", "PUT"]
      "ADMIN" in input.user.roles
  }
  ```
- **How It Helps**: Centralizes HTTP verb authorization at the gateway; prevents accidental destructive endpoint exposure.

### Scenario 4: Scope Validation for Third-Party OAuth2 Clients
- **Problem & Symptom**: A third-party mobile app granted `read:profile` scope invokes `POST /api/v1/payments`, charging users without consent.
- **Root Cause**: Backend verified that the token was valid, but never inspected the OAuth2 `scope` claim.
- **How OPA Solves It**: Checks that the space-separated `scope` claim contains the specific required permission.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package api.authz
  import rego.v1

  default allow := false

  allow if {
      [_, payload, _] := io.jwt.decode(input.token)
      scopes := split(payload.scope, " ")
      "write:payments" in scopes
      input.request.path == "/api/v1/payments"
  }
  ```
- **How It Helps**: Enforces OAuth2 scope boundaries before requests reach internal financial APIs.

### Scenario 5: External IP Whitelisting for Admin Portal
- **Problem & Symptom**: Admin credentials leaked; attacker logs into `/admin/system` from an external residential IP address.
- **Root Cause**: Portal was internet-facing without IP subnet restrictions.
- **How OPA Solves It**: Uses `net.cidr_contains` to ensure admin routes are only accessible from corporate VPN subnets.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package api.authz
  import rego.v1

  default allow := false

  corporate_subnets := ["10.0.0.0/8", "192.168.100.0/24"]

  allow if {
      input.request.path == "/admin/system"
      some cidr in corporate_subnets
      net.cidr_contains(cidr, input.request.client_ip)
  }
  ```
- **How It Helps**: Blocks credential stuffing and hijacked sessions originating outside trusted corporate CIDRs.

### Scenario 6: Enforcing Weekend Maintenance Freeze on Production Deployments
- **Problem & Symptom**: Junior developer triggers deployment pipeline on Saturday night, breaking production with no SRE on call.
- **Root Cause**: CI/CD pipeline lacked calendar awareness.
- **How OPA Solves It**: Evaluates `time.weekday(time.now_ns())` and rejects production change requests on Saturday and Sunday.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package cicd.deploy
  import rego.v1

  default allow := false

  allow if {
      day := time.weekday(time.now_ns())
      not day in ["Saturday", "Sunday"]
      input.environment == "production"
  }
  ```
- **How It Helps**: Prevents unmonitored weekend outages automatically across all deployment pipelines.

### Scenario 7: Time-of-Day Working Hours ABAC Policy
- **Problem & Symptom**: Customer support agent queries sensitive medical records at 3:00 AM from a remote laptop.
- **Root Cause**: Role-Based access allowed access anytime the role was present.
- **How OPA Solves It**: Extracts hour via `time.clock` and blocks queries outside 8:00 AM to 6:00 PM.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package healthcare.authz
  import rego.v1

  default allow := false

  allow if {
      input.user.role == "SUPPORT_AGENT"
      [hour, _, _] := time.clock(time.now_ns())
      hour >= 8
      hour < 18
  }
  ```
- **How It Helps**: Prevents insider data exfiltration outside monitored working hours.

### Scenario 8: Tenant Partitioning in Multi-Tenant SaaS
- **Problem & Symptom**: Tenant A injects Tenant B's UUID into an HTTP header, querying cross-tenant database rows.
- **Root Cause**: Gateway routed requests without validating that the tenant header matched the authenticated user's organization.
- **How OPA Solves It**: Asserts that `input.headers["x-tenant-id"]` matches `payload.organization_id`.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package multitenant.authz
  import rego.v1

  default allow := false

  allow if {
      [_, payload, _] := io.jwt.decode(input.token)
      payload.organization_id == input.headers["x-tenant-id"]
  }
  ```
- **How It Helps**: Eliminates cross-tenant data leakage vulnerabilities across the entire multi-tenant fleet.

### Scenario 9: Restricting Payload Size to Prevent Buffer Overflow & Memory Exhaustion
- **Problem & Symptom**: Attacker sends a 500MB JSON payload to `/api/v1/search`, causing Node.js gateway out-of-memory crashes.
- **Root Cause**: API server buffered the entire request body before checking size.
- **How OPA Solves It**: Inspects `Content-Length` header and enforces a maximum threshold of 10MB (`10485760` bytes).
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package gateway.security
  import rego.v1

  default allow := false

  allow if {
      content_length := to_number(input.headers["content-length"])
      content_length <= 10485760
  }
  ```
- **How It Helps**: Drops oversized malicious payloads at the edge before backend memory allocation occurs.

### Scenario 10: Mutual TLS Client Certificate Header Verification
- **Problem & Symptom**: Service A impersonates Service B because inter-service calls relied on plain HTTP headers without mTLS verification.
- **Root Cause**: Sidecar passed `X-Client-Cert-SAN` without verifying that Envoy verified the client certificate.
- **How OPA Solves It**: Verifies both `input.tls.verified == true` and that the SPIFFE ID belongs to an authorized client.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package mesh.authz
  import rego.v1

  default allow := false

  allow if {
      input.tls.verified == true
      input.tls.client_san == "spiffe://cluster.local/ns/default/sa/order-service"
  }
  ```
- **How It Helps**: Guarantees zero-trust identity verification between microservices.

*(Scenarios 11–45 continue across API Gateway rate limiting, CORS headers, bot detection, and edge routing...)*

---

## Category 2: Kubernetes Admission Control & Security Governance (Scenarios 46–85)

### Scenario 46: Blocking Containers Running as Root
- **Problem & Symptom**: Container breakout attack gains root privileges on the underlying host node.
- **Root Cause**: Developers deployed containers without setting `runAsNonRoot: true` in `securityContext`.
- **How OPA Solves It**: Inspects Pod manifests in the Kubernetes `AdmissionReview` request and rejects any pod running as root.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package k8s.admission
  import rego.v1

  default allow := false

  violation contains msg if {
      some container in input.request.object.spec.containers
      not container.securityContext.runAsNonRoot == true
      msg := sprintf("Container '%s' must set securityContext.runAsNonRoot to true", [container.name])
  }

  allow if count(violation) == 0
  ```
- **How It Helps**: Prevents container privilege escalation across all Kubernetes clusters.

### Scenario 47: Enforcing Container Resource Requests and Limits (Preventing Noisy Neighbors)
- **Problem & Symptom**: A rogue memory leak in one pod triggers Linux kernel OOM killer, killing critical neighboring pods on the node.
- **Root Cause**: Manifest lacked CPU and memory limits.
- **How OPA Solves It**: Mandates that both `resources.requests` and `resources.limits` are defined for every container.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package k8s.admission
  import rego.v1

  default allow := false

  violation contains msg if {
      some container in input.request.object.spec.containers
      not container.resources.limits.memory
      msg := sprintf("Container '%s' must define a memory limit", [container.name])
  }

  allow if count(violation) == 0
  ```
- **How It Helps**: Prevents cluster node instability and enforces predictable capacity planning.

### Scenario 48: Banning Untrusted Public Docker Registries
- **Problem & Symptom**: Developer pulls an unvetted image from Docker Hub (`docker.io/malicious-crypto-miner:latest`).
- **Root Cause**: Kubernetes cluster allowed pulling from any public registry.
- **How OPA Solves It**: Verifies that every container image starts with the corporate private registry prefix (`registry.enterprise.com/`).
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package k8s.admission
  import rego.v1

  default allow := false

  trusted_registry := "registry.enterprise.com/"

  violation contains msg if {
      some container in input.request.object.spec.containers
      not startswith(container.image, trusted_registry)
      msg := sprintf("Image '%s' is not from the trusted registry '%s'", [container.image, trusted_registry])
  }

  allow if count(violation) == 0
  ```
- **How It Helps**: Eliminates software supply chain malware from entering Kubernetes production clusters.

### Scenario 49: Banning the `:latest` Image Tag in Production
- **Problem & Symptom**: Production pod reboots, pulls an untracked `:latest` image build, breaking production with non-reproducible changes.
- **Root Cause**: Deployment used mutable tags instead of immutable semantic versions or SHA256 digests.
- **How OPA Solves It**: Denies pods whose image strings end with `:latest` or lack a version tag.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package k8s.admission
  import rego.v1

  default allow := false

  violation contains msg if {
      some container in input.request.object.spec.containers
      endswith(container.image, ":latest")
      msg := sprintf("Container '%s' uses forbidden ':latest' tag", [container.name])
  }

  allow if count(violation) == 0
  ```
- **How It Helps**: Enforces immutable, reproducible production deployments and simplifies incident rollbacks.

### Scenario 50: Disallowing `hostPath` Volume Mounts
- **Problem & Symptom**: Compromised pod mounts `/var/run/docker.sock` from the host node, seizing total control of the cluster.
- **Root Cause**: Pod specification mounted host directories directly.
- **How OPA Solves It**: Denies any Pod specification containing a volume of type `hostPath`.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package k8s.admission
  import rego.v1

  default allow := false

  violation contains msg if {
      some volume in input.request.object.spec.volumes
      volume.hostPath
      msg := sprintf("Volume '%s' uses forbidden hostPath mounting", [volume.name])
  }

  allow if count(violation) == 0
  ```
- **How It Helps**: Hardens nodes against host filesystem traversal and container breakouts.

*(Scenarios 51–85 continue across Ingress collision prevention, privileged pods, read-only root filesystems, drop capabilities, and namespace isolation...)*

---

## Category 3: Infrastructure as Code (Terraform/CloudFormation) Guardrails (Scenarios 86–125)

### Scenario 86: Blocking Public S3 Buckets in Terraform Plans
- **Problem & Symptom**: Company database backups exposed to the public internet because a Terraform module defaulted to public read.
- **Root Cause**: Infrastructure PR lacked automated policy scanning.
- **How OPA Solves It**: Parses `terraform show -json` execution plan and blocks any `aws_s3_bucket_public_access_block` where public blocks are `false`.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package terraform.aws
  import rego.v1

  default allow := false

  violation contains msg if {
      some resource in input.resource_changes
      resource.type == "aws_s3_bucket_public_access_block"
      resource.change.after.block_public_acls != true
      msg := sprintf("S3 Bucket '%s' must enable block_public_acls", [resource.address])
  }

  allow if count(violation) == 0
  ```
- **How It Helps**: Eliminates cloud data leaks before infrastructure is ever provisioned in AWS.

### Scenario 87: Preventing Open Ingress (`0.0.0.0/0`) on Sensitive Ports (SSH/RDP)
- **Problem & Symptom**: Security group opens port 22 to the world, resulting in immediate brute-force SSH attacks on cloud servers.
- **Root Cause**: Developer opened port 22 temporarily to debug and committed to git.
- **How OPA Solves It**: Scans `aws_security_group` resources and denies ingress rules with CIDR `0.0.0.0/0` on port 22 or 3389.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package terraform.aws
  import rego.v1

  default allow := false

  forbidden_ports := [22, 3389]

  violation contains msg if {
      some resource in input.resource_changes
      resource.type == "aws_security_group"
      some rule in resource.change.after.ingress
      "0.0.0.0/0" in rule.cidr_blocks
      some port in forbidden_ports
      rule.from_port <= port
      rule.to_port >= port
      msg := sprintf("Security Group '%s' exposes port %d to 0.0.0.0/0", [resource.address, port])
  }

  allow if count(violation) == 0
  ```
- **How It Helps**: Blocks open management ports automatically in pull request CI pipelines.

### Scenario 88: Enforcing Encryption-at-Rest for EBS Volumes
- **Problem & Symptom**: Unencrypted disk containing customer data is cloned without enterprise encryption compliance.
- **Root Cause**: `encrypted = true` was omitted from the Terraform `aws_ebs_volume` block.
- **How OPA Solves It**: Evaluates that `resource.change.after.encrypted == true`.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package terraform.aws
  import rego.v1

  default allow := false

  violation contains msg if {
      some resource in input.resource_changes
      resource.type == "aws_ebs_volume"
      resource.change.after.encrypted != true
      msg := sprintf("EBS volume '%s' must have encryption enabled", [resource.address])
  }

  allow if count(violation) == 0
  ```
- **How It Helps**: Guarantees SOC2 and HIPAA encryption-at-rest compliance across all cloud disks.

### Scenario 89: Mandating Standardized Cost-Center Tags on Cloud Resources
- **Problem & Symptom**: FinOps cannot allocate a \$50,000 monthly cloud bill because 30% of VMs lack ownership tags.
- **Root Cause**: Engineers launched VMs without standardized tagging.
- **How OPA Solves It**: Checks that required tags (`Environment`, `CostCenter`, `Owner`) exist and match permitted patterns.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package terraform.governance
  import rego.v1

  default allow := false

  required_tags := ["Environment", "CostCenter", "Owner"]

  violation contains msg if {
      some resource in input.resource_changes
      resource.type in ["aws_instance", "aws_rds_cluster"]
      some tag in required_tags
      not resource.change.after.tags[tag]
      msg := sprintf("Resource '%s' is missing mandatory tag: '%s'", [resource.address, tag])
  }

  allow if count(violation) == 0
  ```
- **How It Helps**: Ensures 100% cost allocation accuracy across all business units.

### Scenario 90: Banning Wildcard IAM Policies (`Action: "*"`)
- **Problem & Symptom**: Service account given `AdministratorAccess` policy allows an attacker to seize full control of the cloud account.
- **Root Cause**: Developer used wildcard to avoid figuring out minimal permissions.
- **How OPA Solves It**: Parses IAM policy documents and rejects statements where `Action` or `Resource` equals `*`.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package terraform.iam
  import rego.v1

  default allow := false

  violation contains msg if {
      some resource in input.resource_changes
      resource.type == "aws_iam_policy"
      policy_doc := json.unmarshal(resource.change.after.policy)
      some statement in policy_doc.Statement
      statement.Effect == "Allow"
      statement.Action == "*"
      msg := sprintf("IAM Policy '%s' contains forbidden wildcard Action: '*'", [resource.address])
  }

  allow if count(violation) == 0
  ```
- **How It Helps**: Enforces Principle of Least Privilege across all IAM policies before provisioning.

*(Scenarios 91–125 continue across RDS deletion protection, multi-AZ requirements, TLS 1.2 minimums, DynamoDB point-in-time recovery, and CloudTrail logging...)*

---

## Category 4: Data Protection, PII Masking & Column/Row Security (Scenarios 126–155)

### Scenario 126: Dynamic PII Field Redaction Based on Role
- **Problem & Symptom**: Customer service agents see full unmasked Social Security Numbers and Credit Card numbers.
- **Root Cause**: API returned the entire database record without contextual masking.
- **How OPA Solves It**: Generates an array of fields that the gateway must strip/mask before returning JSON to the client.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package data.masking
  import rego.v1

  # Returns set of fields to redact from response
  redacted_fields contains "ssn" if {
      not "COMPLIANCE_OFFICER" in input.user.roles
  }

  redacted_fields contains "credit_card_cvv" if {
      true # Never expose CVV to any user role
  }
  ```
- **How It Helps**: Enforces field-level redaction at the API gateway layer dynamically.

### Scenario 127: Row-Level Tenancy Filter Generation
- **Problem & Symptom**: Multi-tenant database query could return rows belonging to another organization if raw SQL was compromised.
- **Root Cause**: Backend relied on developers manually adding `WHERE tenant_id = ?` to every SQL query.
- **How OPA Solves It**: OPA returns a query filter constraint that the ORM automatically appends to the database execution plan.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package db.authz
  import rego.v1

  tenant_filter := sprintf("tenant_id = '%s'", [input.user.tenant_id]) if {
      not "SUPER_ADMIN" in input.user.roles
  }

  tenant_filter := "1=1" if {
      "SUPER_ADMIN" in input.user.roles
  }
  ```
- **How It Helps**: Guarantees database row isolation across all multi-tenant queries.

### Scenario 128: Blocking Cross-Border GDPR Data Transfers
- **Problem & Symptom**: EU citizen personal data downloaded to a US analytics cluster, violating GDPR cross-border transfer laws.
- **Root Cause**: Data pipeline lacked geographic residency checks.
- **How OPA Solves It**: Validates that destination country equals the source residency jurisdiction.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package data.governance
  import rego.v1

  default allow := false

  allow if {
      input.data.classification != "GDPR_RESTRICTED"
  }

  allow if {
      input.data.classification == "GDPR_RESTRICTED"
      input.destination_region in ["eu-west-1", "eu-central-1"]
  }
  ```
- **How It Helps**: Prevents multi-million dollar regulatory fines by enforcing data sovereignty at runtime.

*(Scenarios 129–155 continue across export size limits, audit triggers, VIP account masking, and medical HIPAA access restrictions...)*

---

## Category 5: CI/CD Pipeline & Supply Chain Security (Scenarios 156–180)

### Scenario 156: Enforcing Cryptographically Signed Container Images (Cosign)
- **Problem & Symptom**: CI/CD pipeline deploys an image that was tampered with in transit by a man-in-the-middle attacker.
- **Root Cause**: Cluster pulled images without checking cryptographic digital signatures.
- **How OPA Solves It**: Checks that the image manifest has a verified Cosign cryptographic signature matching the corporate public key.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package cicd.security
  import rego.v1

  default allow := false

  allow if {
      input.image_signature.verified == true
      input.image_signature.issuer == "https://token.actions.githubusercontent.com"
      input.image_signature.repository == "enterprise/core-banking"
  }
  ```
- **How It Helps**: Enforces SLSA Level 3 software supply chain verification.

### Scenario 157: Blocking Deployments with Critical CVE Vulnerabilities
- **Problem & Symptom**: High-severity remote code execution vulnerability (Log4Shell) deployed to production.
- **Root Cause**: Developer bypassed security scan alerts.
- **How OPA Solves It**: Inspects vulnerability scan report (Trivy/Snyk) and blocks deployment if any CVE with CVSS $\ge 9.0$ exists.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package cicd.vulnerability
  import rego.v1

  default allow := false

  critical_cves := [cve |
      some cve in input.scan_report.vulnerabilities
      cve.severity == "CRITICAL"
  ]

  allow if count(critical_cves) == 0
  ```
- **How It Helps**: Automates zero-tolerance vulnerability gates in release pipelines.

*(Scenarios 158–180 continue across license compliance (blocking AGPL), branch protection, commit message standards, and provenance verification...)*

---

## Category 6: Enterprise Governance, Financial Limits & Compliance (Scenarios 181–200)

### Scenario 181: Dual-Control / Four-Eyes Principle for High-Value Wire Transfers
- **Problem & Symptom**: Rogue employee authorizes a \$5,000,000 transfer to an offshore account alone.
- **Root Cause**: System permitted single-user authorization for all transaction amounts.
- **How OPA Solves It**: Enforces that any transaction over \$100,000 must have approval signatures from two distinct senior executives.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package finance.transfers
  import rego.v1

  default allow := false

  # Small transfers allow single authorization
  allow if {
      input.amount <= 100000
      input.requester.role == "FINANCE_OFFICER"
  }

  # High-value transfers require two distinct managers
  allow if {
      input.amount > 100000
      count(input.approvers) >= 2
      every approver in input.approvers {
          approver.role == "MANAGING_DIRECTOR"
      }
      # Requester cannot approve their own transfer
      not input.requester.id in [a.id | some a in input.approvers]
  }
  ```
- **How It Helps**: Prevents executive fraud and satisfies strict financial regulatory mandates.

### Scenario 182: Maximum Daily Expense Approval Ceiling
- **Problem & Symptom**: Manager approves 50 separate \$9,999 expenses in a single day, evading the \$10,000 single-transaction review limit.
- **Root Cause**: Rules evaluated individual transactions instead of daily aggregate sums.
- **How OPA Solves It**: Queries cached data to compute cumulative daily approved amounts and blocks once the daily ceiling is breached.
- **Concrete Solution & Rego v1 Config**:
  ```rego
  package finance.expenses
  import rego.v1

  default allow := false

  max_daily_ceiling := 50000

  allow if {
      daily_total := sum([tx.amount | some tx in data.daily_approved[input.manager_id]])
      daily_total + input.amount <= max_daily_ceiling
  }
  ```
- **How It Helps**: Prevents structuring attacks and enforces cumulative organizational budget limits.

*(Scenarios 183–200 continue across travel policy constraints, stock trading blackout periods, automated SLA penalty clauses, and SOC2 audit log preservation...)*

---
[🏠 Back to Central Home Documentation Hub](README.md)
