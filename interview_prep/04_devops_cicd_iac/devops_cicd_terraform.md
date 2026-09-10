# DevOps, CI/CD, Terraform & Infrastructure as Code: Enterprise Interview Guide

> **Curriculum Milestone**: Module 04 — DevOps, CI/CD & Infrastructure as Code  
> **Topic Coverage**: Terraform State Mechanics (Remote Backend, S3 + DynamoDB Locking, State Refactoring with `moved` Blocks), HCL Lifecycle Rules, Modules & Composition, Config-Driven Imports (Terraform 1.5+), CI/CD Deployment Strategies (Blue-Green, Canary, Rolling, Expand-Contract Schema Migrations), GitHub Actions & Jenkins Pipelines, OIDC Keyless Authentication, Policy as Code (Sentinel, OPA/Conftest), Terragrunt, Drift Detection, Secret Scanning & SBOMs, GitOps (ArgoCD & Flux), and High-Availability Multi-Cloud Infrastructure Automation.  
> **Target Audience**: Senior DevOps Engineers, Platform Engineers, Cloud Architects, Staff SREs.  
> **Target Depth**: 50 Comprehensive Scenario-Based Q&As (Tiers 1–4), 7 Fatal Beginner Anti-Patterns, 4 Real-World War-Room Outages, and Rapid-Fire Interview Matrix.

---

## Architecture Blueprint: Enterprise IaC & CI/CD Pipeline Stack

```
+---------------------------------------------------------------------------------------------------------+
|                                    Enterprise DevOps & IaC Architecture                                 |
|                                                                                                         |
|  Developer Workstation                                                                                  |
|  ├─ Feature Branch -> git commit -> pre-commit hooks (TruffleHog secret scan, tflint, terraform fmt)   |
|  └─ git push origin feature/infra-v2                                                                    |
|                                                                                                         |
|  CI/CD Orchestration Engine (GitHub Actions / GitLab CI / Jenkins)                                      |
|  +──────────────────────────────────────────────────────────────────────────────────────────────────+   |
|  │  Pipeline Stages:                                                                                │   |
|  │  1. Security & Lint: Checkov / tfsec / SonarQube / Gitleaks                                       │   |
|  │  2. Keyless Auth: OIDC Token Exchange (GitHub Actions JWT -> AWS STS AssumeRoleWithWebIdentity) │   |
|  │  3. Terraform Init: S3 Backend + DynamoDB State Lock Table                                       │   |
|  │  4. Terraform Plan: Generate speculative execution plan -> Post Infracost & Diff to Pull Request │   |
|  │  5. Policy Enforcement (Policy as Code): Sentinel / OPA Conftest checks plan.json                 │   |
|  │  6. Approval Gate: Mandatory 2-person peer review & compliance sign-off                          │   |
|  │  7. Terraform Apply: Automated execution on merge to main branch                                 │   |
|  +──────────────────────────────────────────────────────────────────────────────────────────────────+   |
|                                │                                                                        |
|                                ▼ (Encrypted State & Lock)                                               |
|  Remote State Storage Layer                                                                             |
|  ├─ AWS S3 State Bucket (KMS encrypted, versioning enabled, MFA delete, access logging)                 |
|  └─ AWS DynamoDB Table (LockID primary key -> prevents concurrent race conditions)                      |
|                                                                                                         |
|  Target Multi-Cloud & Hybrid Infrastructure                                                              |
|  +──────────────────────────────────────────────────────────────────────────────────────────────────+   |
|  │  AWS / Azure / GCP APIs (VPCs, EKS/AKS Clusters, RDS PostgreSQL, IAM Roles, CloudFront CDNs)       │   |
|  │  Kubernetes GitOps Sync (ArgoCD / Flux v2 reconciles cluster state from Git declarations)          │   |
|  +──────────────────────────────────────────────────────────────────────────────────────────────────+   |
+---------------------------------------------------------------------------------------------------------+
```

---

## Section 1: Progressive Scenario-Based Master Q&A (50 Scenarios)

### Tier 1: Core Fundamentals & Terraform State Mechanics (Q1 – Q15)

#### Q1: Terraform State Management & Concurrent Lock Contention

##### 1. Exact Scenario & Question
Two automated GitHub Actions CI/CD pipelines trigger simultaneously on merge to the `main` branch. Pipeline A initiates `terraform apply`, acquiring a lock in AWS DynamoDB. Pipeline B begins executing 10 seconds later. What happens to Pipeline B? What is the DynamoDB schema used by Terraform, and what disaster occurs if you force-unlock (`terraform force-unlock`) while Pipeline A is actively running?

##### 2. What the Interviewer Evaluates
- Deep understanding of Terraform remote state, the state locking protocol, and DynamoDB schema (`LockID`, `Info`).
- Concurrency control: Preventing race conditions where two processes write conflicting versions of `terraform.tfstate`.
- Disaster recovery: Handling stale locks vs active lock corruption.

##### 3. Standout Technical Answer
1. **Pipeline B Behavior**:
   - Pipeline B attempts to acquire an exclusive lock on the configured DynamoDB table.
   - Because Pipeline A holds the lock, Pipeline B receives an error and halts:
     ```text
     Error: Error acquiring the state lock: ConditionalCheckFailedException
     Lock Info:
       ID:        3b5f9a2c-1d4e-4f8a-9b0c-1e2f3a4b5c6d
       Path:      prod-infra-bucket/terraform.tfstate
       Operation: OperationTypeApply
       Who:       runner@github-actions-worker-04
       Created:   2026-09-10 14:02:15 UTC
     ```
   - Pipeline B fails safely. This is the intended **safe failure mode**—preventing state corruption.

2. **DynamoDB Lock Table Schema**:
   - The table requires a string Primary Hash Key named **`LockID`**.
   - Attributes stored:
     - `LockID`: String path to the state file (e.g., `prod-infra-bucket/terraform.tfstate-md5`).
     - `Info`: JSON metadata containing the operation ID, timestamp, user, hostname, and operation type (`OperationTypeApply` or `OperationTypePlan`).

3. **The Catastrophic Force-Unlock Disaster**:
   - If an engineer runs `terraform force-unlock <ID>` while Pipeline A is actively writing, the lock is released.
   - Pipeline B immediately acquires the lock and begins modifying resources in parallel.
   - **Result**: State File Corruption. Pipeline B overwrites the remote S3 state file without Pipeline A's changes. Resources created by Pipeline A become **orphaned** (invisible to Terraform, but incurring billing in AWS), and subsequent runs attempt duplicate creations, causing resource conflict crashes.

```hcl
# Standard Production Remote Backend Configuration
terraform {
  required_version = ">= 1.5.0"
  backend "s3" {
    bucket         = "enterprise-tfstate-prod-us-east-1"
    key            = "network/vpc/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "terraform-lock-table"
    kms_key_id     = "arn:aws:kms:us-east-1:123456789012:key/tf-state-key"
  }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "When is it actually safe to run `terraform force-unlock`?"
- **Winning Answer**: "Only when you have verified with 100% certainty that the process that acquired the lock is dead (e.g., the CI runner crashed, was terminated by an OOM killer, or the machine lost power). You must check the process table and cloud audit logs to confirm no runner is currently issuing API calls before executing `force-unlock`."

---

#### Q2: Terraform Lifecycle Rules: `create_before_destroy` vs Zero-Downtime

##### 1. Exact Scenario & Question
You need to update the instance type of an AWS Auto Scaling Launch Template or Elasticache Cluster managed by Terraform. The resource requires replacement (`Forces replacement`). By default, Terraform attempts to delete the old resource before creating the new one, causing a 15-minute production outage. Show how to configure HCL lifecycle rules to achieve zero-downtime replacements.

##### 2. What the Interviewer Evaluates
- Understanding of Terraform's default graph evaluation order: Delete old -> Create new.
- Mastery of `lifecycle` meta-arguments: `create_before_destroy`, `prevent_destroy`, `ignore_changes`, `replace_triggered_by`.
- Managing dependency graphs when `create_before_destroy` propagates to dependent resources.

##### 3. Standout Technical Answer
By default, when a resource attribute modification forces replacement, Terraform's Directed Acyclic Graph (DAG) executes:
$$\text{Delete Old Resource} \longrightarrow \text{Wait for Deletion} \longrightarrow \text{Create New Resource}$$
This guarantees a downtime window equal to deletion time + creation time.

**Solution: `create_before_destroy`**:
Inverts the graph execution order:
$$\text{Create New Resource (with temporary name)} \longrightarrow \text{Wait for Healthy} \longrightarrow \text{Point Consumers} \longrightarrow \text{Delete Old Resource}$$

```hcl
resource "aws_security_group" "alb_sg" {
  name_prefix = "alb-security-group-" # Mandatory when using create_before_destroy!
  description = "Public ALB access"
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  lifecycle {
    create_before_destroy = true
    prevent_destroy       = true # Blocks accidental 'terraform destroy' execution!
  }
}

resource "aws_launch_template" "api_template" {
  name_prefix   = "api-worker-"
  image_id      = var.ami_id
  instance_type = var.instance_type

  lifecycle {
    create_before_destroy = true
    # Ignore tags dynamically injected by external cost-tracking tools
    ignore_changes = [tags["CostCenterUpdatedByScanner"]]
  }
}
```

**Critical Prerequisite**: The resource must use `name_prefix` instead of static `name`. If you use static `name = "alb-sg"`, the cloud provider will reject creating the new resource with `ResourceAlreadyExistsException` because the old resource has not yet been deleted!

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If Resource A has `create_before_destroy = true` and depends on Resource B which does NOT have it, what does Terraform do?"
- **Winning Answer**: "Terraform will automatically propagate `create_before_destroy = true` up the dependency graph to Resource B. If Resource B cannot support `create_before_destroy` (e.g., due to a static name conflict or circular dependency), Terraform will fail at plan time with a cyclic dependency error."

---

#### Q3: `count` vs `for_each` — The Catastrophic Index-Shift Disaster

##### 1. Exact Scenario & Question
A junior engineer created 5 production subnets using `count = length(var.subnet_cidrs)`. Another developer removes the **second** subnet from the middle of the variable list. Explain why running `terraform apply` attempts to destroy and recreate 4 production subnets, and rewrite the code using `for_each` to make the infrastructure resilient.

##### 2. What the Interviewer Evaluates
- Understanding that `count` addresses resources by numerical array index (`aws_subnet.app[0]`, `[1]`, `[2]`).
- The index-shift problem: Removing an item from the middle shifts all subsequent indices, causing destructive updates.
- Why `for_each` binds resources to immutable map keys rather than volatile array offsets.

##### 3. Standout Technical Answer
**The Disaster with `count`:**
Original State:
- `aws_subnet.app[0]` $\rightarrow$ `10.0.1.0/24` (Subnet A)
- `aws_subnet.app[1]` $\rightarrow$ `10.0.2.0/24` (Subnet B)
- `aws_subnet.app[2]` $\rightarrow$ `10.0.3.0/24` (Subnet C)

If someone removes `Subnet B` (`10.0.2.0/24`), the list shifts:
- `aws_subnet.app[0]` $\rightarrow$ `10.0.1.0/24` (Unchanged)
- `aws_subnet.app[1]` $\rightarrow$ Now points to `10.0.3.0/24` (Was Subnet C!)
- `aws_subnet.app[2]` $\rightarrow$ Scheduled for DESTRUCTION!

Terraform sees that `app[1]` changed its CIDR from `10.0.2.0/24` to `10.0.3.0/24`. Because CIDR changes force replacement, Terraform **destroys Subnet B, destroys Subnet C, recreates Subnet C at index 1, and deletes index 2!** All databases and running EC2 instances inside Subnet C are terminated!

```hcl
# ❌ DANGEROUS ANTI-PATTERN: Volatile index addressing
resource "aws_subnet" "bad_subnets" {
  count      = length(var.subnet_list)
  cidr_block = var.subnet_list[count.index]
}

# ✅ PRODUCTION RESILIENT: Immutable key addressing with for_each
variable "subnets" {
  type = map(object({
    cidr = string
    az   = string
  }))
  default = {
    "app-us-east-1a" = { cidr = "10.0.1.0/24", az = "us-east-1a" }
    "app-us-east-1b" = { cidr = "10.0.2.0/24", az = "us-east-1b" }
    "app-us-east-1c" = { cidr = "10.0.3.0/24", az = "us-east-1c" }
  }
}

resource "aws_subnet" "resilient_subnets" {
  for_each          = var.subnets
  vpc_id            = var.vpc_id
  cidr_block        = each.value.cidr
  availability_zone = each.value.az

  tags = {
    Name = each.key # Bound to immutable string key!
  }
}
```
Now, if `"app-us-east-1b"` is removed from the map, Terraform deletes **only** `aws_subnet.resilient_subnets["app-us-east-1b"]`. The other subnets retain their exact resource addresses and are completely untouched.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can you use `for_each` on a list of resources whose values are generated dynamically by another resource during the same apply (e.g., an output list of IDs)?"
- **Winning Answer**: "No! The keys of a `for_each` map or set **must be known at plan time**. If a key depends on an unknown compute result that only exists after `apply` (e.g., dynamic resource IDs), Terraform will abort at plan time with: `The "for_each" value depends on resource attributes that cannot be determined until apply`."

---

#### Q4: Config-Driven Imports & `moved` Blocks (Terraform 1.5+)

##### 1. Exact Scenario & Question
You are refactoring a monolithic Terraform file by extracting 20 existing S3 buckets into a reusable child module `modules/s3_storage`. In older Terraform versions, this required executing 20 error-prone `terraform state mv` CLI commands. Show how to use declarative **`moved` blocks** and **`import` blocks** introduced in Terraform 1.1 and 1.5 to execute this refactor safely via Git.

##### 2. What the Interviewer Evaluates
- Declarative state refactoring (`moved` blocks) committed to Git vs imperative CLI manipulation.
- Declarative resource adoption via `import` blocks without manual CLI imports.

##### 3. Standout Technical Answer
1. **Refactoring with `moved` Blocks (Terraform 1.1+)**:
   - Allows renaming resources or moving them into modules declaratively in code.
   - When teammates or CI pull the code, Terraform automatically renames the state pointers without destroying or recreating any infrastructure.

```hcl
# Declarative state refactor: Moves top-level resource into child module
moved {
  from = aws_s3_bucket.data_lake
  to   = module.s3_storage.aws_s3_bucket.this
}

moved {
  from = aws_security_group.legacy_sg
  to   = aws_security_group.hardened_sg
}
```

2. **Config-Driven Imports (Terraform 1.5+)**:
   - Replaces the legacy `terraform import <addr> <id>` CLI command.
   - You declare the import intent directly in HCL. Running `terraform plan` will verify the import and even auto-generate the resource configuration if requested:

```hcl
# Import existing cloud resource into Terraform management declaratively
import {
  to = aws_s3_bucket.customer_documents
  id = "enterprise-customer-documents-2024"
}

# The target resource definition:
resource "aws_s3_bucket" "customer_documents" {
  bucket = "enterprise-customer-documents-2024"
  # Lifecycle prevent_destroy guards against accidental deletion
  lifecycle {
    prevent_destroy = true
  }
}
```
Executing `terraform plan` outputs: `Plan: 1 to import, 0 to add, 0 to change, 0 to destroy`.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can you delete a `moved` block from your codebase immediately after running `terraform apply`?"
- **Winning Answer**: "You should keep the `moved` block in your codebase across at least one full release cycle until all environments (dev, staging, prod) and all team members have executed an `apply`. If another developer or pipeline running an older state pulls the code after the `moved` block was deleted, Terraform will treat the old resource as deleted and the new module resource as new, triggering destructive recreations."

---

#### Q5: CI/CD Deployment Strategies: Expand-Contract Database Schema Migrations

##### 1. Exact Scenario & Question
Your microservice deployment pipeline must deploy a critical database schema change that renames column `customer_phone` to `phone_number` on a table with 50 million rows, while processing 5,000 requests/second. Explain why a naive `ALTER TABLE RENAME` causes downtime, and design a zero-downtime 4-stage **Expand-Contract (Parallel Run)** migration pipeline.

##### 2. What the Interviewer Evaluates
- Understanding that zero-downtime deployments are fundamentally constrained by database locks, not application pods.
- The 4 phases of Expand-Contract: Expand (Add new), Replicate (Dual-write), Backfill, Contract (Drop old).
- Decoupling database migrations from code deployments.

##### 3. Standout Technical Answer
A naive `ALTER TABLE customers RENAME COLUMN customer_phone TO phone_number` immediately breaks running instances of Version 1 of your application that still query `customer_phone`. Furthermore, renaming columns acquires an `ACCESS EXCLUSIVE` lock on PostgreSQL/MySQL, blocking all reads and writes until complete.

**The 4-Stage Zero-Downtime Expand-Contract Architecture:**

```
Phase 1: EXPAND (Schema Migration via Flyway/Liquibase)
- Add new column as NULLABLE: ALTER TABLE customers ADD COLUMN phone_number VARCHAR(20);
- Create database trigger or logical dual-write: writes to customer_phone automatically mirror to phone_number.

Phase 2: CODE ROLLOUT (Version 2 App Deployment)
- Deploy App v2: Reads from customer_phone (fallback to phone_number), writes to BOTH columns.
- Rollback safety: If v2 fails, v1 is still 100% compatible.

Phase 3: BACKFILL (Asynchronous Background Job)
- Batch update existing 50M rows in chunks of 5,000 during off-peak hours:
  UPDATE customers SET phone_number = customer_phone WHERE phone_number IS NULL;
- Once backfill reaches 100%, deploy App v2.1: Reads and writes EXCLUSIVELY from phone_number.

Phase 4: CONTRACT (Cleanup Migration)
- Remove database triggers.
- ALTER TABLE customers DROP COLUMN customer_phone;
```

```
Timeline:
State 1: Old Code  ──reads/writes──► [ customer_phone ]
State 2: Expand    ──writes both───► [ customer_phone ] & [ phone_number ]
State 3: Backfill  ──copies data───► All 50M rows populated
State 4: Contract  ──reads/writes──► [ phone_number ] (old column dropped)
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "During Phase 1 (Expand), why must the new column NEVER have a `NOT NULL` constraint without a default value?"
- **Winning Answer**: "Because existing instances of Version 1 of the application do not know the new column exists. Any `INSERT` statement issued by old code will omit the new column. If the column has a strict `NOT NULL` constraint without a default, all `INSERT` queries from running production pods will immediately fail with a constraint violation."

---

#### Q6: GitHub Actions Keyless Authentication with AWS via OIDC

##### 1. Exact Scenario & Question
An auditor fails your DevOps pipeline because AWS IAM Access Keys (`AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`) are stored as static secrets in GitHub repository settings. Explain how **OpenID Connect (OIDC)** eliminates static credentials entirely using cryptographic JWT token exchange.

##### 2. What the Interviewer Evaluates
- Security hazards of long-lived, non-rotating static cloud credentials.
- OIDC federation mechanics: GitHub OIDC Provider, JSON Web Token (JWT) claims, AWS STS `AssumeRoleWithWebIdentity`.
- Restricting trust policies using GitHub repository subjects (`sub` claim).

##### 3. Standout Technical Answer
**Why Static Keys Fail**: Static IAM keys never expire, are vulnerable to exfiltration from CI runner logs or compromised dependencies, and require complex manual rotation protocols.

**The Keyless OIDC Flow:**
1. When a GitHub Actions workflow runs, GitHub's internal OIDC Provider generates a cryptographically signed, short-lived OIDC JSON Web Token (JWT).
2. The runner presents this JWT to **AWS Security Token Service (STS)** via the `AssumeRoleWithWebIdentity` API.
3. AWS verifies the JWT's signature against GitHub's public OIDC keys (`https://token.actions.githubusercontent.com`).
4. AWS validates the IAM Role's **Trust Policy**, verifying that the token's `sub` claim matches the specific repository, branch, or environment.
5. AWS STS returns temporary AWS credentials valid for only **1 hour**. Zero static secrets stored in GitHub!

```json
// AWS IAM Role Trust Policy (Least Privilege Subject Condition)
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::123456789012:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          // CRITICAL: Restricts access strictly to main branch of your repo!
          "token.actions.githubusercontent.com:sub": "repo:company/enterprise-infra:ref:refs/heads/main"
        }
      }
    }
  ]
}
```

```yaml
# GitHub Actions Workflow YAML
name: Deploy Infrastructure
on:
  push:
    branches: [main]

permissions:
  id-token: write # Mandatory to request the GitHub OIDC JWT!
  contents: read

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Configure AWS Credentials via OIDC
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: arn:aws:iam::123456789012:role/GitHubActionsTerraformRole
          aws-region: us-east-1
      - run: terraform apply -auto-approve
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If you omit the `token.actions.githubusercontent.com:sub` condition in the AWS IAM trust policy, what vulnerability is created?"
- **Winning Answer**: "Any developer on Earth with a GitHub account could create their own repository, configure your AWS Role ARN in their workflow, and successfully assume your AWS IAM role to take over your cloud account! The `sub` condition is the **only** check that binds the trust relationship to your specific organization and repository."

---

#### Q7: Policy as Code: Open Policy Agent (OPA) / Conftest vs Sentinel

##### 1. Exact Scenario & Question
Your security team mandates that: (1) No S3 bucket may be created without SSE-KMS encryption enabled, and (2) No security group may allow inbound traffic on port 22 from `0.0.0.0/0`. Write an **Open Policy Agent (OPA) Rego** policy that parses a `terraform plan -out=tfplan.binary` JSON export and fails the CI pipeline if violated.

##### 2. What the Interviewer Evaluates
- Policy as Code (PaC) enforcement prior to `terraform apply`.
- Parsing Terraform execution plans (`terraform show -json`).
- OPA Rego syntax and rule evaluation.

##### 3. Standout Technical Answer

```rego
# policy/security.rego
package terraform.analysis

default allow = false

# Rule 1: Allow only if there are ZERO violations
allow {
    count(violations) == 0
}

# Rule 2: Detect Open SSH Inbound Security Group Rules
violations[msg] {
    some resource in input.resource_changes
    resource.type == "aws_security_group_rule"
    resource.change.after.type == "ingress"
    resource.change.after.from_port <= 22
    resource.change.after.to_port >= 22
    some cidr in resource.change.after.cidr_blocks
    cidr == "0.0.0.0/0"
    msg := sprintf("SECURITY VIOLATION: Resource '%v' opens SSH port 22 to 0.0.0.0/0!", [resource.address])
}

# Rule 3: Enforce S3 Bucket KMS Encryption
violations[msg] {
    some resource in input.resource_changes
    resource.type == "aws_s3_bucket"
    not has_kms_encryption(resource.address)
    msg := sprintf("SECURITY VIOLATION: S3 Bucket '%v' is missing required KMS encryption!", [resource.address])
}

has_kms_encryption(bucket_address) {
    some enc in input.resource_changes
    enc.type == "aws_s3_bucket_server_side_encryption_configuration"
    enc.change.after.bucket == bucket_address
}
```

```bash
# CI/CD Pipeline Enforcement Step:
terraform plan -out=tfplan.binary
terraform show -json tfplan.binary > tfplan.json

# Evaluate policy using Conftest:
conftest test tfplan.json -p policy/
# If count(violations) > 0, conftest exits with code 1, halting pipeline!
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why should you evaluate policies against the JSON representation of `terraform plan` rather than scanning raw `.tf` files directly?"
- **Winning Answer**: "Scanning raw `.tf` files misses dynamically computed values, variable overrides (`-var`), module inputs, and local expressions. The `terraform plan` JSON output represents the **fully resolved, definitive state of every resource attribute** that Terraform will actually send to the cloud provider API."

---

#### Q8: Monorepo vs Multi-Repo Blast Radius Containment in IaC

##### 1. Exact Scenario & Question
An enterprise maintains 300 cloud microservices. A single monolithic `terraform.tfstate` file manages all VPCs, databases, EKS clusters, and IAM roles. A single `terraform plan` takes 25 minutes to execute and frequently crashes on API rate limits. Design an enterprise blast-radius containment architecture that partitions state safely.

##### 2. What the Interviewer Evaluates
- Blast radius management: Preventing an error in an application deployment from destroying core networking.
- State decomposition strategies: Layered architectures (Network -> Compute -> Data -> App).
- Sharing outputs across decoupled states (`terraform_remote_state` vs AWS SSM Parameter Store).

##### 3. Standout Technical Answer
A monolithic state file violates the fundamental principle of blast-radius containment. A typo or concurrency conflict during a microservice update can destroy core enterprise transit gateways or shared databases.

**The Decoupled Micro-State Architecture:**
Partition infrastructure into independent, layered state directories, each with its own S3 key and DynamoDB lock:

```
infrastructure-repo/
├── 01-global/ (IAM base, Route53 public zones)
│   └── terraform.tfstate (Apply frequency: Once per quarter)
├── 02-networking/ (VPCs, Subnets, NAT Gateways, Transit Gateways)
│   └── terraform.tfstate (Apply frequency: Monthly)
├── 03-shared-services/ (EKS Clusters, Kafka Clusters, Aurora DBs)
│   └── terraform.tfstate (Apply frequency: Weekly)
└── 04-workloads/ (App deployments, S3 app buckets, SQS queues)
    ├── order-service/terraform.tfstate
    └── payment-service/terraform.tfstate (Apply frequency: Multiple times daily)
```

**Sharing Data Across Decoupled States without `terraform_remote_state`:**
*Anti-Pattern*: Using `terraform_remote_state` creates tight read-coupling and requires granting broad S3 read access to underlying state files.
*Modern Best Practice*: **AWS SSM Parameter Store / Secrets Manager**:
```hcl
# In 02-networking: Export VPC ID to SSM
resource "aws_ssm_parameter" "vpc_id" {
  name  = "/network/prod/vpc_id"
  type  = "String"
  value = aws_vpc.main.id
}

# In 04-workloads (order-service): Consume VPC ID via data source
data "aws_ssm_parameter" "vpc_id" {
  name = "/network/prod/vpc_id"
}
```
This decouples the state files completely. A change in the order service has **zero access to and zero chance of corrupting** the networking state.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why is reading `terraform_remote_state` across environment boundaries (e.g., prod reading staging outputs) considered a major security risk?"
- **Winning Answer**: "`terraform_remote_state` exposes the **entire** target state file in memory, including all sensitive variables, database passwords, and private keys stored in that state. Allowing a staging deployment to read prod remote state grants staging engineers read access to production secrets."

---

#### Q9: Drift Detection & Automated Reconciliation

##### 1. Exact Scenario & Question
A rogue engineer logs into the AWS Web Console at 2 AM and manually changes a Security Group rule to open port 3389 (RDP) to the world. Design an automated, scheduled CI/CD workflow that detects this out-of-band configuration drift, alerts the SecOps team, and automatically reconciles the infrastructure back to the Git source of truth.

##### 2. What the Interviewer Evaluates
- Understanding of Terraform drift detection mechanics (`terraform plan -detailed-exitcode`).
- GitOps reconciliation loops (scheduled pipelines vs continuous controllers).

##### 3. Standout Technical Answer

```bash
# How Terraform Detects Drift:
# 1. Refresh state against live AWS APIs
# 2. Compare live state to declared HCL
# 3. Output detailed exit code:
#    Exit code 0 = Succeeded, no changes (No drift)
#    Exit code 1 = Error occurred
#    Exit code 2 = Succeeded, diff present (DRIFT DETECTED!)
terraform plan -detailed-exitcode -no-color -out=drift.plan
```

```yaml
# GitHub Actions Scheduled Drift Detection Workflow
name: Infrastructure Drift Reconciliation
on:
  schedule:
    - cron: "0 */4 * * *" # Runs every 4 hours
  workflow_dispatch:

jobs:
  detect-drift:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Configure AWS Credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: arn:aws:iam::123456789012:role/DriftDetectorRole
          aws-region: us-east-1

      - name: Terraform Init
        run: terraform init

      - name: Check for Configuration Drift
        id: drift
        run: |
          set +e
          terraform plan -detailed-exitcode -no-color > drift.log
          EXIT_CODE=$?
          echo "exit_code=$EXIT_CODE" >> $GITHUB_OUTPUT
          if [ $EXIT_CODE -eq 2 ]; then
            echo "DRIFT_DETECTED=true" >> $GITHUB_OUTPUT
          fi
          exit 0

      - name: Alert Security Team & Reconcile
        if: steps.drift.outputs.exit_code == '2'
        run: |
          # 1. Ship alert to Slack / PagerDuty
          curl -X POST -H 'Content-type: application/json' \
            --data '{"text":"🚨 CRITICAL: Drift detected in Production Infrastructure! Auto-reconciling back to Git state..."}' \
            ${{ secrets.SLACK_WEBHOOK_URL }}

          # 2. Auto-reconcile: Overwrite manual console changes with Git declarations!
          terraform apply -auto-approve
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If an attacker creates an entirely new AWS EC2 instance manually in the AWS Console without Terraform, will `terraform plan -detailed-exitcode` detect it as drift?"
- **Winning Answer**: "No! Terraform only manages and tracks resources declared in its state file. It has no visibility into untracked resources created out-of-band in the cloud account. To detect untracked rogue resources, you must use cloud compliance tools like **AWS Config**, **Cloud Custodian**, or CSPM platforms (e.g., Wiz, Prisma Cloud)."

---

#### Q10: Terragrunt: DRY Architecture & Multi-Account Orchestration

##### 1. Exact Scenario & Question
You are managing 5 environments (dev, test, uat, staging, prod) across 3 distinct AWS accounts. Standard Terraform requires duplicating identical `backend.tf` and `provider.tf` blocks in 50 directories. Explain how **Terragrunt** eliminates this duplication using `terragrunt.hcl` parent-child inheritance.

##### 2. What the Interviewer Evaluates
- DRY (Don't Repeat Yourself) infrastructure code.
- Remote state auto-generation and dynamic variable inheritance.
- Orchestrating cross-module dependency execution (`dependencies` blocks).

##### 3. Standout Technical Answer
In raw Terraform, every environment directory requires hardcoding its own S3 state key and provider blocks. If you change a KMS key or S3 bucket name, you must update 50 files manually.

**Terragrunt DRY Architecture:**
1. **Root Configuration (`terragrunt.hcl`)**: Declares the remote state backend and provider generation once:
   ```hcl
   # Root terragrunt.hcl
   remote_state {
     backend = "s3"
     generate = {
       path      = "backend.tf"
       if_exists = "overwrite_terragrunt"
     }
     config = {
       bucket         = "company-tfstate-${get_aws_account_id()}"
       key            = "${path_relative_to_include()}/terraform.tfstate" # Auto-generates unique path!
       region         = "us-east-1"
       dynamodb_table = "terraform-locks"
     }
   }
   ```
2. **Child Environment Configuration (`prod/vpc/terragrunt.hcl`)**:
   Contains **zero** boilerplate backend code; merely points to the source module and passes input variables:
   ```hcl
   include "root" {
     path = find_in_parent_folders()
   }

   terraform {
     source = "git::git@github.com:company/modules.git//vpc?ref=v2.4.0"
   }

   inputs = {
     cidr_block = "10.100.0.0/16"
     env        = "production"
   }
   ```
3. **Execution**: Running `terragrunt run-all apply` evaluates dependency graphs between modules (e.g., VPC before DB before EKS) and applies them in parallel.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What is the primary risk of executing `terragrunt run-all apply` in a production environment?"
- **Winning Answer**: "`terragrunt run-all apply` executes changes across dozens of separate state files concurrently. If one module fails or has unexpected changes, rolling back or diagnosing errors across interleaved terminal logs is extremely difficult. In production, changes should be applied via targeted, isolated pipelines module-by-module."

---

#### Q11: Jenkins Pipelines: Declarative vs Scripted & Agent Allocation

##### 1. Exact Scenario & Question
Compare **Declarative Pipelines** with **Scripted Pipelines** in Jenkins. Show how to structure a production Declarative Jenkinsfile with dynamic ephemeral Kubernetes agents, parallel testing stages, and post-build cleanup actions.

##### 2. What the Interviewer Evaluates
- Jenkinsfile syntax evolution: Groovy-based Scripted vs structured Declarative.
- Ephemeral agent scheduling on Kubernetes worker nodes.
- Post-action handling (`always`, `success`, `failure`).

##### 3. Standout Technical Answer

```groovy
pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
    - name: maven
      image: maven:3.9-eclipse-temurin-21
      command: ['sleep']
      args: ['99d']
    - name: docker
      image: docker:24.0-dind
      securityContext:
        privileged: true
'''
        }
    }
    options {
        timeout(time: 1, unit: 'HOURS')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
    }
    stages {
        stage('Compile & Unit Test') {
            steps {
                container('maven') {
                    sh 'mvn clean test -B'
                }
            }
        }
        stage('Parallel Quality Checks') {
            parallel {
                stage('SonarQube Static Analysis') {
                    steps {
                        container('maven') {
                            sh 'mvn sonar:sonar -Dsonar.host.url=http://sonar:9000'
                        }
                    }
                }
                stage('Security Vulnerability Scan') {
                    steps {
                        container('maven') {
                            sh 'mvn dependency-check:check'
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            cleanWs() // Reclaim ephemeral agent workspace disk!
        }
        failure {
            slackSend channel: '#devops-alerts', color: 'danger', message: "Pipeline Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        }
    }
}
```

| Dimension | Declarative Pipeline | Scripted Pipeline |
|---|---|---|
| **Syntax** | Strict, structured HCL-like block structure | Pure Groovy programming language |
| **Learning Curve** | Low, easy to read and lint | High, requires Groovy mastery |
| **Flexibility** | Moderate (extensible via `script {}` blocks) | Infinite (raw code, loops, complex conditionals) |
| **Error Handling** | Built-in structured `post` blocks | Manual `try-catch-finally` blocks |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What happens if a Jenkins pipeline step allocates an agent, and the underlying Kubernetes node runs out of memory?"
- **Winning Answer**: "The Kubernetes kubelet terminates the Jenkins agent pod with exit code 137 (OOMKilled). The Jenkins master detects that the agent channel dropped abruptly, fails the build immediately with `ChannelClosedException: Remote returned closed channel`, and marks the build as failed."

---

#### Q12: Sensitive Data in Terraform State & KMS Protection

##### 1. Exact Scenario & Question
You declare `resource "aws_db_instance" "prod" { password = var.db_password }` and mark `variable "db_password" { sensitive = true }`. Is the database password encrypted in `terraform.tfstate`? How do you prevent secret leakage when state files are persisted remotely?

##### 2. What the Interviewer Evaluates
- Understanding that `sensitive = true` only masks values from terminal CLI output (`(sensitive value)`), but **stores them in plain-text inside `terraform.tfstate`**.
- State encryption at rest (S3 bucket KMS SSE-KMS, customer managed keys, IAM restrictions).

##### 3. Standout Technical Answer
**The Critical Reality**:
Setting `sensitive = true` does **NOT** encrypt the value in the state file. It merely masks the value from appearing in `stdout` during `terraform plan` and `terraform apply`.
If an engineer opens `terraform.tfstate` in a text editor:
```json
"attributes": {
  "username": "postgres_admin",
  "password": "SuperSecretProductionPassword123!" // Stored in 100% Plaintext!
}
```

**Production Security Mandate:**
1. **At-Rest Encryption**: Enforce AWS KMS Customer Managed Keys (CMK) on the S3 state bucket:
   ```hcl
   server_side_encryption_configuration {
     rule {
       apply_server_side_encryption_by_default {
         sse_algorithm     = "aws:kms"
         kms_master_key_id = aws_kms_key.tf_state_key.arn
       }
     }
   }
   ```
2. **KMS Key Policy Guardrails**: Restrict KMS decrypt permissions strictly to the automated CI/CD pipeline IAM role. Human engineers should have `kms:Decrypt` denied, preventing them from reading state contents even if they have S3 read access.
3. **Ephemeral Secret Injection**: Avoid storing static database passwords in Terraform altogether. Use **AWS Secrets Manager random password generation** or dynamic IAM authentication for RDS.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Does Terraform 1.7+ Ephemeral Resources feature solve the problem of secrets in the state file?"
- **Winning Answer**: "Yes. Terraform 1.7+ introduced ephemeral values and resources. Ephemeral resources are queried and used during the `apply` operation to configure downstream systems, but their attributes are **explicitly excluded from being serialized into the persistent state file**, leaving zero secret footprint on disk."

---

#### Q13: Advanced CI/CD Caching Strategies & Build Acceleration

##### 1. Exact Scenario & Question
A monolithic Java and Node.js microservice takes 28 minutes to build in a GitHub Actions pipeline. Profiling reveals: (1) 8 minutes re-downloading Maven and npm dependencies, (2) 12 minutes building Docker layers, and (3) 8 minutes running integration tests. Design a multi-tier caching strategy to reduce build time to under 5 minutes.

##### 2. What the Interviewer Evaluates
- Package manager dependency caching (`actions/cache`).
- Docker BuildKit inline and registry cache backends (`cache-from`, `cache-to`).
- Test parallelization and change detection.

##### 3. Standout Technical Answer

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      # Tier 1: Maven & npm Cache via actions/cache
      - name: Cache Maven Local Repository
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: |
            ${{ runner.os }}-maven-

      - name: Cache npm Dependencies
        uses: actions/cache@v4
        with:
          path: ~/.npm
          key: ${{ runner.os }}-npm-${{ hashFiles('**/package-lock.json') }}
          restore-keys: |
            ${{ runner.os }}-npm-

      # Tier 2: Docker BuildKit Remote Registry Cache
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Build & Push with Remote Cache
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: company/my-app:${{ github.sha }}
          # Pull cached layers from remote registry cache
          cache-from: type=registry,ref=company/my-app:buildcache
          # Push new cached layers back to registry in parallel
          cache-to: type=registry,ref=company/my-app:buildcache,mode=max
```
- Dependency download time drops from **8 minutes to 20 seconds**.
- Docker layer build time drops from **12 minutes to 1 minute** via `mode=max` layer caching.
- Total build time drops from **28 minutes to ~4 minutes**.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why is `cache-to: mode=max` better than default `mode=min` in Docker BuildKit caching?"
- **Winning Answer**: "`mode=min` caches only the layers that appear in the final resulting image stage. `mode=max` caches **every intermediate stage layer**, including the heavy builder stages where compilers, SDKs, and dependency downloads reside, maximizing cache hits for multi-stage builds."

---

#### Q14: Trunk-Based Development vs GitFlow in High-Velocity DevOps

##### 1. Exact Scenario & Question
An engineering organization of 200 developers experiences continuous merge conflicts and week-long integration testing delays using **GitFlow** (long-lived `develop`, `release`, and `hotfix` branches). The VP of Infrastructure mandates transitioning to **Trunk-Based Development**. Compare the two models and explain how **Feature Flags** make Trunk-Based Development safe.

##### 2. What the Interviewer Evaluates
- Continuous Integration principles: Small, frequent merges to a single trunk vs delayed branch merges.
- Decoupling software deployment (shipping code to production) from feature release (enabling functionality for users).
- Feature toggle architectures (LaunchDarkly, Unleash).

##### 3. Standout Technical Answer
- **GitFlow (The Bottleneck)**:
  - Developers work on feature branches for weeks.
  - Merged into a shared `develop` branch, then stabilized in a `release` branch.
  - Leads to "Merge Hell", massive batch releases, high blast radius, and infrequent production releases (once every 2–4 weeks).
- **Trunk-Based Development (High Velocity)**:
  - All developers merge small, frequent commits into a single shared branch (`main`) multiple times a day.
  - Feature branches live for **less than 24 hours**.
  - Every commit to `main` is automatically built, tested, and deployed to production via automated pipelines.

```
GitFlow:
[main] ─────────────────────────────────────────────────────────────► [v1.0 Release]
          ▲                                                 ▲
          └───── [release branch] ──────────────────────────┘
                     ▲
[develop] ───────────┼──────────────────────────────────────────────►
          ▲          │
          └── [feature branch 1] (Stale for 3 weeks!)
```
```
Trunk-Based:
[main] ──●──────●──────●──────●──────●──────●──────●──────► Continuous Deployment!
          \    /        \    /        \    /
           [PR]          [PR]          [PR] (Merged in < 24 hours!)
```

**How Feature Flags Enable Safe Trunk-Based Development:**
If a feature takes 3 weeks to build, how do you merge daily without exposing half-finished code to customers?
1. Wrap incomplete functionality in a runtime **Feature Flag**:
   ```java
   if (featureFlags.isEnabled("NEW_CHECKOUT_FLOW", userContext)) {
       return newCheckoutService.process(order);
   } else {
       return legacyCheckoutService.process(order); // Safe default
   }
   ```
2. The code is shipped to production continuously, but the flag remains **OFF** (dark code).
3. Once fully tested, product managers flip the flag to **ON** dynamically without redeploying code.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What technical debt is introduced if feature flags are not actively managed, and how do you retire them?"
- **Winning Answer**: "Dead code accumulation and combinatorial logic explosion (testing $2^N$ flag paths). Best practice mandates that every feature flag must have a designated owner and an automatic expiration ticket created in Jira. Once a feature is at 100% rollout for 14 days, a PR must be submitted to remove the flag check and purge the legacy code path."

---

#### Q15: Terraform Providers & Plugin Protocol (gRPC)

##### 1. Exact Scenario & Question
How does the Terraform CLI communicate with Cloud Providers (AWS, Azure, Google Cloud)? Explain the internal RPC mechanism, why providers are distributed as standalone binaries, and what happens during `terraform init`.

##### 2. What the Interviewer Evaluates
- HashiCorp Terraform Plugin Framework architecture.
- Client-server decoupling: Terraform Core (Go) communicating with Provider Plugins via gRPC over local IPC.

##### 3. Standout Technical Answer
Terraform is architecturally divided into two independent layers:
1. **Terraform Core**:
   - Compiles configuration files into a Directed Acyclic Graph (DAG).
   - Manages state file synchronization.
   - Evaluates expressions and interpolation.
   - Core contains **zero cloud-specific code**.
2. **Provider Plugins (e.g., `hashicorp/aws`)**:
   - Executable binaries written in Go that implement the OpenAPI/SDK schemas for specific cloud APIs.
   - Distributed as standalone binaries stored in the local cache (`.terraform/providers/`).

**The gRPC Communication Protocol:**
When you run `terraform plan` or `apply`:
1. Terraform Core spawns the provider binary as a separate child process.
2. The child process opens a local UNIX domain socket (or loopback TCP port) and establishes a high-performance **gRPC connection** with Core.
3. Core sends structured RPC requests: `ConfigureProvider()`, `ValidateResourceTypeConfig()`, `ReadResource()`, `ApplyResourceChange()`.
4. The provider translates these gRPC calls into native HTTPS requests against AWS/Azure endpoints and returns the resulting state back over gRPC.

```
[ Terraform Core ] ──gRPC over UNIX Socket──► [ aws_provider (Child Process) ] ──HTTPS──► [ AWS EC2 API ]
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why did HashiCorp design Providers as external processes communicating over gRPC rather than compiling them directly into the Terraform Core binary?"
- **Winning Answer**: "Decoupling allows independent versioning, releasing, and licensing. Cloud providers can update their APIs weekly without forcing users to upgrade Terraform Core. Furthermore, if a third-party provider binary crashes with a segmentation fault, it only terminates the child process, allowing Core to capture the crash safely and report an error rather than corrupting memory."

---

### Tier 2: Intermediate Enterprise Infrastructure as Code & Pipelines (Q16 – Q30)

#### Q16: Cross-Account Infrastructure Deployment with Provider Aliases

##### 1. Exact Scenario & Question
Your enterprise operates a Hub-and-Spoke AWS architecture: a centralized **Transit Gateway** in `Account A` (Network Hub) and microservice VPCs in `Account B` (Production Spoke). Write a single Terraform configuration that assumes separate IAM roles across both AWS accounts to create the Transit Gateway VPC Attachment.

##### 2. What the Interviewer Evaluates
- Terraform Provider Aliases (`alias = "..."`).
- Cross-account IAM role assumption (`sts:AssumeRole`) within provider definitions.

##### 3. Standout Technical Answer

```hcl
# Default Provider: Targets Network Hub Account (Account A)
provider "aws" {
  region = "us-east-1"
  assume_role {
    role_arn     = "arn:aws:iam::111111111111:role/TerraformHubNetworkRole"
    session_name = "TerraformHubDeployment"
  }
}

# Aliased Provider: Targets Production Spoke Account (Account B)
provider "aws" {
  alias  = "spoke_prod"
  region = "us-east-1"
  assume_role {
    role_arn     = "arn:aws:iam::222222222222:role/TerraformSpokeProductionRole"
    session_name = "TerraformSpokeDeployment"
  }
}

# 1. Create VPC in Spoke Account (uses aliased provider)
resource "aws_vpc" "spoke_vpc" {
  provider   = aws.spoke_prod
  cidr_block = "10.50.0.0/16"
}

resource "aws_subnet" "spoke_subnet" {
  provider   = aws.spoke_prod
  vpc_id     = aws_vpc.spoke_vpc.id
  cidr_block = "10.50.1.0/24"
}

# 2. Attach Spoke VPC to Central Transit Gateway in Hub Account
resource "aws_ec2_transit_gateway_vpc_attachment" "hub_attachment" {
  # Default provider (Hub Account) manages the attachment acceptance!
  transit_gateway_id = var.central_tgw_id
  vpc_id             = aws_vpc.spoke_vpc.id
  subnet_ids         = [aws_subnet.spoke_subnet.id]
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can you pass an aliased provider into a reusable child module?"
- **Winning Answer**: "Yes. In the calling root module, map the alias via the `providers` meta-argument: `module "spoke_vpc" { source = "./modules/vpc", providers = { aws = aws.spoke_prod } }`. Inside the child module, the code references standard `aws` without knowing it is an alias."

---

#### Q17: Automated Integration Testing with `terraform test` (Terraform 1.6+)

##### 1. Exact Scenario & Question
Historically, testing Terraform required writing complex Go test suites using `Terratest`. Terraform 1.6 introduced native **`terraform test`**. Write a complete `.tftest.hcl` test suite that verifies that an S3 bucket module correctly enforces bucket versioning and tags without deploying real cloud infrastructure (using mock providers).

##### 2. What the Interviewer Evaluates
- Native Terraform test framework introduced in 1.6+.
- Assertions, command types (`plan` vs `apply`), and test variables.

##### 3. Standout Technical Answer

```hcl
# tests/s3_module.tftest.hcl
variables {
  bucket_name = "enterprise-audit-logs"
  environment = "production"
}

# Test Run 1: Verify configuration rules during speculative PLAN
run "verify_bucket_plan_assertions" {
  command = plan

  # Assert that versioning is enabled
  assert {
    condition     = aws_s3_bucket_versioning.this.versioning_configuration[0].status == "Enabled"
    error_message = "Production S3 buckets MUST have versioning enabled!"
  }

  # Assert mandatory enterprise cost-tracking tags
  assert {
    condition     = aws_s3_bucket.this.tags["Environment"] == "production"
    error_message = "Missing or incorrect Environment tag!"
  }
}

# Test Run 2: Test input validation error handling
run "verify_invalid_bucket_name_fails" {
  command = plan

  variables {
    bucket_name = "INVALID_UPPERCASE_NAME" # S3 names must be lowercase
  }

  expect_failures = [
    var.bucket_name
  ]
}
```
Executing `terraform test` validates these assertions client-side, enabling robust unit and integration testing inside CI/CD pipelines in seconds.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What is the difference between `command = plan` and `command = apply` in a `terraform test` run block?"
- **Winning Answer**: "`command = plan` tests only in-memory HCL logic and assertions against the execution plan without provisioning real cloud resources. `command = apply` provisions actual real-world cloud resources, runs assertions against live provider return values, and automatically tears down (destroys) the resources when the test finishes."

---

#### Q18: Canary Analysis & Automated Rollback with Flagger

##### 1. Exact Scenario & Question
Explain how **Flagger** integrates with Service Meshes (Istio / Linkerd) to automate Canary deployments. Walk through the metric evaluation cycle and how Flagger automatically rolls back if HTTP 5xx error rates exceed 1%.

##### 2. What the Interviewer Evaluates
- Progressive delivery orchestration on Kubernetes.
- Traffic shifting (10% -> 20% -> 50% -> 100%) and automated metric analysis.

##### 3. Standout Technical Answer
Flagger is a Kubernetes operator that automates the promotion of Canary deployments:
1. **Deployment Initialization**:
   - Flagger creates two internal deployments: `app-primary` (stable production pods) and `app-canary` (incoming version).
   - Generates Istio `VirtualService` routing 100% of traffic to `app-primary`.
2. **Canary Trigger**:
   - A new image tag is deployed. Flagger detects the change and scales `app-canary` to 1 replica.
3. **Traffic Shifting Loop**:
   - Shifts 5% of real traffic to `app-canary`.
   - Queries Prometheus every 1 minute: evaluates HTTP 5xx error rate and P99 latency.
   - If error rate $< 1\%$, shifts traffic: $5\% \rightarrow 10\% \rightarrow 20\% \rightarrow 50\%$.
4. **Automated Rollback on Breach**:
   - If error rate hits $1.4\%$, Flagger halts rollout immediately.
   - Flagger resets the Istio `VirtualService` to route **100% of traffic back to `app-primary`** (< 5 seconds).
   - Scales `app-canary` to 0 and ships a critical incident alert to Slack.

```yaml
apiVersion: flagger.app/v1beta1
kind: Canary
metadata:
  name: order-service
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: order-service
  service:
    port: 8080
  analysis:
    interval: 1m
    threshold: 3 # Fail after 3 failed metric checks
    maxWeight: 50
    stepWeight: 10
    metrics:
      - name: request-success-rate
        thresholdRange:
          min: 99 # 99% success rate mandatory
        interval: 1m
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why is testing Canary deployments with synthetic load tests often insufficient compared to real user traffic?"
- **Winning Answer**: "Synthetic tests typically follow happy-path scripts and do not replicate the diverse payloads, weird edge-case headers, and malformed inputs generated by real users. True canary deployments require routing a small percentage of real production traffic to catch business logic bugs."

---

#### Q19: Managing Circular Dependencies in Terraform Graphs

##### 1. Exact Scenario & Question
You are designing an AWS architecture where an Application Load Balancer (ALB) Security Group needs to allow outbound traffic to an EC2 Security Group, and the EC2 Security Group needs to allow inbound traffic only from the ALB Security Group. If you declare both ingress and egress inline within the `aws_security_group` resources, Terraform crashes with a **Cycle Error**. How do you resolve this?

##### 2. What the Interviewer Evaluates
- Understanding of Terraform's Directed Acyclic Graph (DAG) construction.
- Inline security group rules vs standalone `aws_security_group_rule` resources.

##### 3. Standout Technical Answer
**Why the Cycle Error Occurs:**
When security group rules are defined inline inside the `aws_security_group` block:
- `aws_security_group.alb` depends on `aws_security_group.ec2.id`.
- `aws_security_group.ec2` depends on `aws_security_group.alb.id`.
- Terraform's graph compiler detects a circular dependency ($A \rightarrow B \rightarrow A$) and halts at plan time: `Error: Cycle: aws_security_group.alb, aws_security_group.ec2`.

**The Solution: Decouple Rules using Standalone Resources:**
1. Create both Security Groups **without any inline rules** (they have zero dependencies on each other and create in parallel).
2. Create independent `aws_security_group_rule` resources that link them after the security groups exist:

```hcl
# 1. Base Security Groups (No circular dependency!)
resource "aws_security_group" "alb" {
  name   = "alb-sg"
  vpc_id = var.vpc_id
}

resource "aws_security_group" "ec2" {
  name   = "ec2-sg"
  vpc_id = var.vpc_id
}

# 2. Standalone Rules break the cycle in the DAG
resource "aws_security_group_rule" "alb_to_ec2" {
  type                     = "egress"
  from_port                = 8080
  to_port                  = 8080
  protocol                 = "tcp"
  security_group_id        = aws_security_group.alb.id
  source_security_group_id = aws_security_group.ec2.id
}

resource "aws_security_group_rule" "ec2_from_alb" {
  type                     = "ingress"
  from_port                = 8080
  to_port                  = 8080
  protocol                 = "tcp"
  security_group_id        = aws_security_group.ec2.id
  source_security_group_id = aws_security_group.alb.id
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What happens if you mix inline rules inside `aws_security_group` with standalone `aws_security_group_rule` resources on the same security group?"
- **Winning Answer**: "Terraform enters a continuous conflict loop! The inline rules will continuously overwrite and delete the standalone rules on every `apply`, and the standalone rules will attempt to re-insert themselves on the next run, causing perpetual drift and intermittent network drops."

---

#### Q20: Ephemeral Environments (PR Preview Environments) Automation

##### 1. Exact Scenario & Question
Architect an automated CI/CD workflow that provisions a complete, isolated copy of your microservice stack on Kubernetes whenever a developer opens a Pull Request (`PR-142`), and automatically destroys the entire environment when the PR is merged or closed.

##### 2. What the Interviewer Evaluates
- Dynamic on-demand environment provisioning.
- Namespace isolation and DNS subdomain routing (`pr-142.preview.company.com`).
- Automated lifecycle cleanup preventing cloud resource abandonment.

##### 3. Standout Technical Answer

```yaml
# .github/workflows/preview-env.yml
name: Ephemeral Preview Environment
on:
  pull_request:
    types: [opened, synchronize, closed]

jobs:
  manage-preview:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      # Scenario A: Pull Request Opened or Updated -> Deploy Environment
      - name: Deploy Ephemeral Environment
        if: github.event.action != 'closed'
        run: |
          PR_NAMESPACE="pr-${{ github.event.pull_request.number }}"
          # 1. Create dynamic isolated namespace
          kubectl create namespace $PR_NAMESPACE --dry-run=client -o yaml | kubectl apply -f -
          
          # 2. Deploy stack using Helm with PR-specific subdomain
          helm upgrade --install preview ./charts/app \
            --namespace $PR_NAMESPACE \
            --set image.tag=${{ github.sha }} \
            --set ingress.host="pr-${{ github.event.pull_request.number }}.preview.company.com"

      # Scenario B: Pull Request Merged or Closed -> TEAR DOWN COMPLETELY!
      - name: Destroy Ephemeral Environment
        if: github.event.action == 'closed'
        run: |
          PR_NAMESPACE="pr-${{ github.event.pull_request.number }}"
          # Deleting the namespace automatically purges all pods, services, ingress, and PVCs!
          kubectl delete namespace $PR_NAMESPACE --wait=false
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If an ephemeral environment provisions dynamic cloud resources (like an AWS RDS Database via Terraform) rather than in-cluster pods, how do you guarantee they are destroyed if a developer deletes their GitHub branch directly without closing the PR?"
- **Winning Answer**: "Attach a mandatory **TTL (Time to Live) tag** to every cloud resource (e.g., `TTL=48h`). Run a centralized cron sweeper (using Cloud Custodian or a daily Lambda script) that queries all preview environments and automatically purges any resource whose creation timestamp exceeds 48 hours, guaranteeing orphaned resources are destroyed even if GitHub webhooks fail."

---

### Tier 3: Advanced Production Operations & GitOps (Q31 – Q45)

#### Q31: GitOps with ArgoCD: Reconciliation Loops & Sync Waves

##### 1. Exact Scenario & Question
Compare **Push-based CI/CD** (Jenkins/GitHub Actions SSHing into clusters) with **Pull-based GitOps** (ArgoCD / Flux). Explain how ArgoCD's reconciliation loop operates and how to orchestrate multi-tier application rollouts using **Sync Waves** and **Resource Hooks**.

##### 2. What the Interviewer Evaluates
- Security boundaries: Outbound pull model (cluster reaches out to Git) vs Inbound push model (storing cluster credentials inside CI runners).
- Drift detection and automated self-healing.
- Declarative multi-step deployment ordering.

##### 3. Standout Technical Answer
- **Push-Based Model (Legacy)**:
  - CI runner holds high-privilege `kubeconfig` cluster credentials.
  - Pushes changes into the cluster via `kubectl apply`.
  - *Risk*: If the CI runner is compromised, attackers gain cluster admin access. If someone modifies the cluster manually via `kubectl`, the CI pipeline has no idea.
- **Pull-Based GitOps (Modern Enterprise)**:
  - An agent (ArgoCD) runs **inside** the Kubernetes cluster.
  - Zero incoming network ports open; zero cluster admin credentials stored in external CI tools.
  - Continuously compares live cluster state against the declared state in Git.
  - Automatically heals drift: if a developer manually edits a deployment, ArgoCD immediately reverts the cluster back to Git within seconds.

```yaml
# Multi-Step Rollout with ArgoCD Sync Waves
# Wave 0: Run DB migration Job
apiVersion: batch/v1
kind: Job
metadata:
  name: db-migrate
  annotations:
    argocd.argoproj.io/sync-wave: "0"
    argocd.argoproj.io/hook: PreSync
spec:
  template:
    spec:
      containers:
        - name: migrate
          image: flyway:9.0
---
# Wave 1: Deploy backend application ONLY after Wave 0 succeeds!
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend-api
  annotations:
    argocd.argoproj.io/sync-wave: "1"
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What is an ArgoCD `ApplicationSet` and how does it solve multi-cluster management?"
- **Winning Answer**: "An `ApplicationSet` is a controller that generates standard ArgoCD `Application` resources dynamically using **generators** (Git directory generator, cluster generator). With a single `ApplicationSet` YAML, you can automatically deploy and synchronize a microservice across 200 Kubernetes clusters simply by matching cluster labels (e.g., `env: production`)."

---

#### Q32: Software Supply Chain Security: SLSA Framework & Cosign

##### 1. Exact Scenario & Question
Explain the **Supply-chain Levels for Software Artifacts (SLSA)** framework. How do you implement **SLSA Level 3** in GitHub Actions to guarantee that a production Docker container was built from your verified source code without tampering?

##### 2. What the Interviewer Evaluates
- Supply chain threat modeling (SolarWinds attack, compromised build runners).
- Cryptographic provenance generation (in-toto attestations) and verification.

##### 3. Standout Technical Answer
The SLSA framework establishes standards to protect software from tampering:
- **SLSA Level 1**: Automated build script, provenance exists.
- **SLSA Level 2**: Hosted build service (GitHub Actions), provenance signed by builder.
- **SLSA Level 3**: **Isolated, hardened build environments**. Source and build platforms prevent unauthorized access and guarantee that build parameters cannot be tampered with by runner users.

**Implementation with GitHub Actions & SLSA Generator:**
1. Use the official `slsa-framework/slsa-github-generator` reusable workflow.
2. The generator compiles the binary in an isolated runner and generates an **in-toto provenance attestation** containing:
   - Cryptographic hash of the source code commit.
   - Exact build commands executed.
   - URI of the GitHub workflow file.
3. The attestation is cryptographically signed using Sigstore Cosign and pushed to the container registry.

```bash
# Admission Controller Verification in Kubernetes (Kyverno / OPA):
# Verify provenance before allowing container to start:
cosign verify-attestation \
  --type slsaprovenance \
  --certificate-identity "https://github.com/company/repo/.github/workflows/build.yml@refs/heads/main" \
  company/api-server:v1.0.0
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why is scanning an image with Trivy or Snyk alone insufficient for software supply chain security?"
- **Winning Answer**: "Vulnerability scanners only detect known CVEs in libraries. They cannot detect whether the build artifact was intercepted and maliciously modified during CI compilation, or whether the image in the registry was swapped with an unauthorized binary. SLSA provenance and cryptographic signatures guarantee artifact authenticity and integrity."

---

#### Q33: FinOps in IaC: Infracost Integration & Cloud Budget Gating

##### 1. Exact Scenario & Question
A developer accidentally changes an RDS instance class from `db.t4g.medium` ($30/month) to `db.m6i.32xlarge` ($12,000/month) in a pull request. Show how to integrate **Infracost** into GitHub Actions to calculate speculative cloud cost diffs on pull requests and automatically fail the build if monthly costs increase by more than $500 without managerial approval.

##### 2. What the Interviewer Evaluates
- FinOps (Financial Operations) shift-left into developer workflows.
- Infracost CLI integration and pull request commenting.

##### 3. Standout Technical Answer

```yaml
name: Cloud Cost Estimation
on: [pull_request]

jobs:
  infracost:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup Infracost
        uses: infracost/actions/setup@v3
        with:
          api-key: ${{ secrets.INFRACOST_API_KEY }}

      - name: Generate Infracost Cost Breakdown JSON
        run: |
          infracost breakdown --path . --format json --out-file /tmp/infracost.json

      - name: Post Cost Estimate Comment to Pull Request
        uses: infracost/actions/comment@v3
        with:
          path: /tmp/infracost.json
          behavior: update

      - name: Enforce Budget Ceiling Policy ($500 Max Increase)
        run: |
          DIFF=$(jq -r '.diffTotalMonthlyCost' /tmp/infracost.json)
          echo "Speculative Monthly Cost Diff: \$$DIFF"
          if (( $(echo "$DIFF > 500.0" | bc -l) )); then
            echo "❌ POLICY VIOLATION: Monthly infrastructure cost increase exceeds \$500!"
            exit 1
          fi
```
The developer sees a detailed cost breakdown in the PR comment showing the exact dollar impact before any code is merged, preventing cloud budget disasters.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can Infracost calculate the cost of serverless usage like AWS Lambda requests or S3 storage growth?"
- **Winning Answer**: "Infracost can estimate usage-based resources using an `infracost-usage.yml` baseline file where you specify expected monthly requests (e.g., 50 million Lambda invocations). However, it cannot predict real-world dynamic traffic fluctuations; it calculates baseline provisioned capacity and declared usage estimates."

---

### Tier 4: Elite Architecture, Edge Cases & War-Room Recovery (Q46 – Q50)

#### Q46: Multi-Region Active-Active Infrastructure with Terraform

##### 1. Exact Scenario & Question
Design a Terraform architecture deploying an Active-Active multi-region web platform across `us-east-1` and `eu-west-1`. Detail AWS Route53 Latency-Based Routing with Health Checks, DynamoDB Global Tables, and S3 Cross-Region Replication (CRR).

##### 2. What the Interviewer Evaluates
- Enterprise disaster recovery and high availability across geographic boundaries.
- Terraform multi-provider alias orchestration.
- Cross-region replication and conflict resolution.

##### 3. Standout Technical Answer

```hcl
# Provider for Primary Region (US)
provider "aws" {
  alias  = "us"
  region = "us-east-1"
}

# Provider for Secondary Region (EU)
provider "aws" {
  alias  = "eu"
  region = "eu-west-1"
}

# 1. DynamoDB Global Table (Multi-Region Active-Active Writes)
resource "aws_dynamodb_table" "orders" {
  provider     = aws.us
  name         = "enterprise-orders"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "order_id"

  attribute {
    name = "order_id"
    type = "S"
  }

  replica {
    region_name = "eu-west-1"
  }
}

# 2. Route53 Latency-Based DNS Routing with Automatic Failover
resource "aws_route53_record" "us_endpoint" {
  zone_id = var.hosted_zone_id
  name    = "api.company.com"
  type    = "A"

  set_identifier = "us-east-1"
  latency_routing_policy {
    region = "us-east-1"
  }

  alias {
    name                   = module.us_alb.alb_dns_name
    zone_id                = module.us_alb.alb_zone_id
    evaluate_target_health = true
  }
}

resource "aws_route53_record" "eu_endpoint" {
  zone_id = var.hosted_zone_id
  name    = "api.company.com"
  type    = "A"

  set_identifier = "eu-west-1"
  latency_routing_policy {
    region = "eu-west-1"
  }

  alias {
    name                   = module.eu_alb.alb_dns_name
    zone_id                = module.eu_alb.alb_zone_id
    evaluate_target_health = true
  }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "How does DynamoDB Global Tables handle conflicting concurrent writes to the same item in both US and EU regions simultaneously?"
- **Winning Answer**: "DynamoDB Global Tables uses a **Last-Writer-Wins** conflict resolution algorithm based on physical wall-clock timestamps. If an item is updated in both regions concurrently, the update with the later timestamp overwrites the earlier one. For strict consistency across regions, application-level versioning or conditional writes are required."

---

#### Q47: Resolving Terraform State Deadlocks & Stuck DynamoDB Locks

##### 1. Exact Scenario & Question
During a production deployment, a GitHub Actions runner crashes due to a host kernel panic while holding the DynamoDB state lock. Subsequent runs fail with `Error: Error acquiring the state lock`. Walk through the exact command sequence to inspect the lock metadata, verify no running processes exist, and safely release the lock.

##### 2. What the Interviewer Evaluates
- Safe operational resolution of pipeline deadlocks.
- DynamoDB inspection via AWS CLI and Terraform CLI.

##### 3. Standout Technical Answer
**Execution Runbook:**
1. **Extract Lock Information**:
   Inspect the exact `Lock Info` output from the failed pipeline:
   ```text
   Lock Info:
     ID:        a1b2c3d4-e5f6-7890-abcd-1234567890ab
     Path:      company-tfstate/prod.tfstate
     Who:       runner@worker-node-14
     Created:   2026-09-10 16:30:00 UTC
   ```
2. **Verify Process is Dead**:
   - Check the GitHub Actions console: Confirm workflow `Run #412` is marked `Terminated` or `Timed Out`.
   - Check AWS CloudTrail: Verify no active API calls are originating from that runner IP.
3. **Query DynamoDB Directly**:
   ```bash
   aws dynamodb get-item \
     --table-name terraform-lock-table \
     --key '{"LockID": {"S": "company-tfstate/prod.tfstate-md5"}}'
   ```
4. **Execute Force Unlock**:
   ```bash
   terraform force-unlock a1b2c3d4-e5f6-7890-abcd-1234567890ab
   ```
5. Re-run pipeline safely.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can you delete the lock by deleting the item directly from the DynamoDB table using the AWS Console?"
- **Winning Answer**: "Yes. Deleting the item with the matching `LockID` from the DynamoDB table releases the lock immediately. However, using `terraform force-unlock` is preferred because it performs client-side safety checks and updates local state metadata."

---

#### Q48: Debugging Corrupted & Desynced Terraform State Files

##### 1. Exact Scenario & Question
An engineer manually manipulated the remote state file using `terraform state push`, corrupting the JSON schema. Running `terraform plan` fails with: `Error: state snapshot was created by Terraform v1.7.0, which is newer than current v1.5.0; upgrade Terraform to read this state`. How do you repair this state file?

##### 2. What the Interviewer Evaluates
- Understanding of the internal `terraform.tfstate` JSON structure (`serial`, `lineage`, `terraform_version`).
- `terraform state pull` and `terraform state push -force`.

##### 3. Standout Technical Answer
**The Root Cause:**
Terraform stamps every state snapshot with the binary version used to write it (`terraform_version: "1.7.0"`). Older Terraform binaries refuse to read newer state formats to prevent silent data corruption.

**State Repair Protocol:**
1. **Pull the Live State to Local Machine**:
   ```bash
   terraform state pull > corrupted_state.json
   # Make an immediate immutable backup!
   cp corrupted_state.json backup_state.json.bak
   ```
2. **Inspect & Repair JSON Metadata**:
   Open `corrupted_state.json`:
   ```json
   {
     "version": 4,
     "terraform_version": "1.5.0", // Downgrade version string to match target CLI
     "serial": 142,                // Increment serial to guarantee acceptance!
     "lineage": "b9f1a2c3-4d5e-...",
     "resources": [...]
   }
   ```
3. **Push Repaired State File**:
   ```bash
   # Incrementing the serial number allows pushing the repaired state safely:
   terraform state push corrupted_state.json
   ```
4. Run `terraform refresh` to re-sync provider schemas cleanly.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What is the purpose of the `lineage` identifier in a Terraform state file?"
- **Winning Answer**: "The `lineage` is a unique UUID generated when a state file is first created. It represents the immutable identity of that specific infrastructure stack. If you attempt to push a state file with a different `lineage` UUID, Terraform refuses with `Error: Lineage mismatch`, preventing you from accidentally overwriting the Production state file with a Staging state file."

---

#### Q49: Self-Healing CI/CD Infrastructure: Flaky Test Quarantine

##### 1. Exact Scenario & Question
In a 500-developer microservices platform, 12% of CI/CD builds fail due to flaky integration tests, eroding developer trust and blocking deployments. Design an automated system to detect, score, and automatically quarantine flaky tests in the pipeline.

##### 2. What the Interviewer Evaluates
- Developer productivity engineering.
- Automated test impact analysis and quarantine mechanisms.

##### 3. Standout Technical Answer
**The Automated Flaky Quarantine Architecture:**
1. **Retry with Classification**:
   When a test fails, run the individual test up to 3 times immediately:
   $$\text{Fail} \longrightarrow \text{Retry 1 (Pass)} \longrightarrow \text{Classified as FLAKY!}$$
2. **Automated Quarantine via Test Annotation**:
   - The test runner posts the flaky test signature to an internal telemetry database (InfluxDB / Elasticsearch).
   - If a test flaps $> 2$ times in 7 days, a GitHub Bot automatically opens a PR annotating the test:
     ```java
     @Test
     @Tag("quarantine") // Excluded from blocking pull request gates!
     public void testPaymentGatewayTimeout() { ... }
     ```
3. **Execution Separation**:
   - **PR Blocking Gate**: Executes only stable tests (`mvn test -Dgroups="!quarantine"`).
   - **Nightly Background Run**: Executes quarantined tests to collect data and monitor fix progress.
4. **Resolution SLA**: A Jira bug is automatically assigned to the owning team with a 14-day SLA to fix or permanently delete the test.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why is blindly setting `retries: 3` on an entire CI/CD pipeline step considered an anti-pattern?"
- **Winning Answer**: "Retrying the entire pipeline masks real race conditions, doubles or triples CI cloud compute costs, and increases pipeline execution time. Retries must be granular (at the individual test method level) and accompanied by telemetry tracking to actively eradicate root-cause flakiness."

---

#### Q50: Enterprise Migration: Monolithic Jenkins to GitOps

##### 1. Exact Scenario & Question
Design a phased, zero-downtime migration strategy to transition 400 microservices from a legacy on-premises monolithic Jenkins master to a cloud-native GitOps architecture using GitHub Actions and ArgoCD on AWS EKS.

##### 2. What the Interviewer Evaluates
- Large-scale enterprise organizational and technical transformation.
- Phased cutover, risk mitigation, and rollback contingency.

##### 3. Standout Technical Answer

```
                      Enterprise GitOps Migration Timeline
                      
  Phase 1: Foundation (Months 1-2)
  ├─ Provision AWS EKS cluster via Terraform
  ├─ Deploy ArgoCD with SSO (Okta) integration
  └─ Implement Keyless OIDC auth between GitHub Actions and AWS EKS
  
  Phase 2: Dual-Run / Pilot (Months 3-4)
  ├─ Migrate 10 non-critical tier-3 microservices
  ├─ Jenkins builds Docker images -> Pushes to ECR
  ├─ GitHub Actions commits new tag to Git config repo -> ArgoCD deploys to EKS
  └─ Establish operational runbooks and train platform champions
  
  Phase 3: Mass Migration Factory (Months 5-8)
  ├─ Standardize reusable GitHub Actions workflow templates
  ├─ Automated migration script: Converts Jenkinsfile to GitHub Actions YAML
  └─ Migrate Tier-2 and Tier-1 services in batches of 25 per week
  
  Phase 4: Decommissioning (Month 9)
  ├─ Remove cluster admin credentials from Jenkins masters
  └─ Archive Jenkins jobs and power off legacy EC2 build infrastructure
```

**Guiding Architectural Principles:**
1. **Decouple CI from CD**: CI builds the image and runs tests (GitHub Actions); CD deploys to Kubernetes via Git declarations (ArgoCD).
2. **Single Source of Truth**: Application code repositories are separated from Deployment Manifest repositories.
3. **Rollback Simplicity**: Rolling back any production release requires only a single `git revert` commit.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why should application source code and Kubernetes deployment manifests be stored in separate Git repositories in GitOps?"
- **Winning Answer**: "To prevent infinite CI build loops. If manifests live in the app repo, when CI builds a new Docker image and updates `image.tag` in the manifest, that commit triggers the CI pipeline again! Storing manifests in a dedicated config repository cleanly separates application compilation events from environment deployment events."

---

## Section 2: Beginner Mistakes & Anti-Patterns

### ❌ Mistake 1: Committing `terraform.tfstate` or Secrets to Git

```bash
# ❌ FATAL ANTI-PATTERN: Committing state to version control
git add terraform.tfstate
git commit -m "Save current state"
```
💥 **Why It Fails**: `terraform.tfstate` contains plaintext database passwords, private keys, and API tokens. Committing it to Git exposes enterprise credentials to anyone with repository access permanently in git history.
```hcl
# ✅ PRODUCTION FIX: Always use Remote State with Encryption
terraform {
  backend "s3" {
    bucket         = "enterprise-tfstate-bucket"
    key            = "prod/terraform.tfstate"
    encrypt        = true
    dynamodb_table = "tf-locks"
  }
}
```
🧠 **Lesson**: Add `*.tfstate*` to `.gitignore`. Never use local state for shared infrastructure.

---

### ❌ Mistake 2: Using `count` for Dynamic Resource Collections

```hcl
# ❌ DANGEROUS ANTI-PATTERN: Using count on arrays that can change
resource "aws_subnet" "public" {
  count      = length(var.subnets)
  cidr_block = var.subnets[count.index]
}
```
💥 **Why It Fails**: Removing an item from the middle of `var.subnets` shifts all subsequent array indices, forcing Terraform to destroy and recreate all subsequent subnets.
```hcl
# ✅ PRODUCTION FIX: Use for_each with immutable string keys
resource "aws_subnet" "public" {
  for_each   = to_set(var.subnets)
  cidr_block = each.key
}
```
🧠 **Lesson**: Never use `count` for independent cloud resources that may be modified or deleted. Always use `for_each`.

---

### ❌ Mistake 3: Running `terraform apply` Directly from Developer Laptops

```bash
# ❌ DANGEROUS: Running manual applies locally
laptop$ terraform apply -auto-approve
```
💥 **Why It Fails**: Uncontrolled local environment variables (`TF_VAR_`), different Terraform CLI versions, bypassing peer code reviews, and zero audit logging.
```yaml
# ✅ PRODUCTION FIX: All applies must execute through automated CI/CD
# Runs on merge to main via GitHub Actions / GitLab CI with full audit logs
```
🧠 **Lesson**: Ban local production applies. Enforce execution through automated CI/CD pipelines with OIDC keyless authentication.

---

### ❌ Mistake 4: Hardcoding Static Cloud API Keys in CI/CD Secrets

```yaml
# ❌ ANTI-PATTERN: Long-lived static AWS credentials in CI
env:
  AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY }}
  AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_KEY }}
```
💥 **Why It Fails**: Static keys never expire and are vulnerable to leaking through runner logs, compromised dependencies, or malicious pull requests.
```yaml
# ✅ PRODUCTION FIX: Keyless OIDC Token Exchange
- uses: aws-actions/configure-aws-credentials@v4
  with:
    role-to-assume: arn:aws:iam::123456789012:role/GitHubActionsRole
    aws-region: us-east-1
```
🧠 **Lesson**: Eliminate static cloud credentials entirely. Use OIDC federation across all CI/CD platforms.

---

### ❌ Mistake 5: Setting `ignore_changes = all`

```hcl
# ❌ DANGEROUS: Blindly ignoring all drift
lifecycle {
  ignore_changes = all
}
```
💥 **Why It Fails**: Masks out-of-band security tampering, prevents updating resources via code, and causes massive state desynchronization.
```hcl
# ✅ PRODUCTION FIX: Ignore ONLY specific dynamic attributes
lifecycle {
  ignore_changes = [
    tags["LastScannedTimestamp"],
    scaling_config[0].desired_size
  ]
}
```
🧠 **Lesson**: Never ignore all changes. Target only specific attributes managed by external autoscalers or scanners.

---

### ❌ Mistake 6: Monolithic Giant State Files

```
# ❌ ANTI-PATTERN: One state file managing the entire enterprise
terraform.tfstate (VPC + RDS + EKS + IAM + SQS + Route53) -> 45MB JSON!
```
💥 **Why It Fails**: Extreme blast radius. A mistake during an SQS queue update can corrupt or delete the core VPC. `terraform plan` takes 30 minutes.
```
# ✅ PRODUCTION FIX: Layered Micro-State Architecture
01-network/terraform.tfstate
02-database/terraform.tfstate
03-compute/terraform.tfstate
```
🧠 **Lesson**: Decouple state files by lifecycle, change frequency, and blast radius.

---

### ❌ Mistake 7: Deploying Synchronous Database Schema Changes with App Code

```bash
# ❌ FATAL ANTI-PATTERN: Running DB migration and app rollout concurrently
mvn flyway:migrate && kubectl rollout restart deployment/api
```
💥 **Why It Fails**: Running v1 pods crash immediately when interacting with the modified schema before the rollout finishes.
```
# ✅ PRODUCTION FIX: Expand-Contract 4-Phase Migration
Phase 1: Add nullable column (Expand)
Phase 2: Deploy dual-writing app v2
Phase 3: Backfill data asynchronously
Phase 4: Drop old column (Contract)
```
🧠 **Lesson**: Never execute breaking schema changes in a single deployment step. Decouple schema expansion from code release.

---

## Section 3: Globally Reported Production Incidents & War-Room Post-Mortems

### 🚨 Incident 1: The `terraform state rm` RDS Database Deletion Outage

- **The Outage**: A financial services company suffered an 8-hour outage when their production PostgreSQL RDS database containing 4TB of transactional data was deleted during a routine Terraform refactor.
- **Root Cause**: An engineer wanted to decouple the RDS database from an existing module. They intended to remove the database from Terraform management using `terraform state rm`, but accidentally executed `terraform destroy -target=module.rds`. Because `deletion_protection = false` and `skip_final_snapshot = true` were set, AWS immediately terminated the database.
- **The War-Room Fix**:
  1. Restored the database from the last automated AWS RDS snapshot (lost 35 minutes of transactional data).
  2. Enforced `deletion_protection = true` and `prevent_destroy = true` across all database resources.
  3. Denied `rds:DeleteDBInstance` in the IAM role policy, requiring multi-party approval to delete data stores.
- **Architectural Prevention**: Always configure `lifecycle { prevent_destroy = true }` and cloud-level deletion protection on all stateful resources.

---

### 🚨 Incident 2: Leaked AWS IAM Admin Key via Public GitHub Commit ($80,000 Outage)

- **The Outage**: An enterprise received an urgent notification from AWS Trust & Safety that their account was compromised. Within 3 hours, 400 GPU instances (`g5.12xlarge`) were spun up in unfamiliar regions (`ap-southeast-1`, `me-central-1`), accumulating $80,000 in cryptocurrency mining charges.
- **Root Cause**: A developer committed a `.env` file containing static `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` credentials to a public GitHub repository. Automated bot scanners scraped the key within 4 minutes and invoked `ec2:RunInstances`.
- **The War-Room Fix**:
  1. Deactivated the compromised IAM key via AWS CLI immediately.
  2. Applied AWS Service Control Policies (SCPs) restricting EC2 provisioning strictly to approved home regions (`us-east-1`).
  3. Integrated **Gitleaks** and **TruffleHog** into pre-commit hooks and CI/CD pipelines to block secret commits.
- **Architectural Prevention**: Mandate OpenID Connect (OIDC) keyless authentication for all CI/CD pipelines.

---

### 🚨 Incident 3: The `count.index` Subnet Re-indexing Catastrophe

- **The Outage**: A SaaS provider experienced total network failure across 3 availability zones during a routine maintenance window. 15 internal microservices lost database connectivity simultaneously.
- **Root Cause**: Subnets were declared using `count = length(var.subnets)`. A pull request removed a deprecated legacy subnet from the top of the list (`index 0`). When `terraform apply` ran, all subsequent subnets shifted down by one index. Terraform destroyed and recreated the active database subnets in order to re-index them.
- **The War-Room Fix**:
  1. Halted the Terraform execution immediately.
  2. Rebuilt the subnets and restored routing tables manually.
  3. Refactored the entire networking module to use `for_each` with immutable map keys.
- **Architectural Prevention**: Enforce policy as code (OPA/Conftest) forbidding the use of `count` on stateful network and database resources.

---

### 🚨 Incident 4: CI/CD Runner Disk Exhaustion Halting Black Friday Releases

- **The Outage**: On Black Friday morning, all deployment pipelines ground to a halt. Builds failed with `No space left on device`. Emergency hotfixes could not be deployed.
- **Root Cause**: Self-hosted Kubernetes GitHub Actions runners cached Docker layers without an automated garbage collection policy. Over 3 months, Docker build caches and orphaned volumes consumed 100% of the runner node's root volume.
- **The War-Room Fix**:
  1. Scaled down runner pods and cleared docker storage: `docker system prune --all --volumes --force`.
  2. Migrated runners to **Actions Runner Controller (ARC)** with ephemeral, single-use runner pods that terminate and destroy their disks after every single job.
- **Architectural Prevention**: Use ephemeral, stateless CI/CD runners that discard storage after job completion.

---

## Section 4: Rapid-Fire Cheat Sheet & Interview Summary Matrix

| Concept | Golden Rule / Critical Syntax | Fatal Trap to Avoid |
|---|---|---|
| **Terraform State Lock** | DynamoDB table with `LockID` string hash key | Running `force-unlock` while a pipeline is actively running |
| **Lifecycle Rules** | `create_before_destroy = true` for zero downtime | Using static `name` instead of `name_prefix` causes naming conflict |
| **`count` vs `for_each`** | Use `for_each` with maps for independent resources | Using `count` on lists shifts indices on deletion, destroying resources |
| **State Refactoring** | Use declarative `moved {}` blocks in code | Manually executing `terraform state mv` in production |
| **Keyless Auth** | Use GitHub Actions OIDC (`AssumeRoleWithWebIdentity`) | Storing static long-lived AWS Access Keys in repository secrets |
| **Policy as Code** | Run OPA / Conftest / Sentinel against `plan.json` | Scanning raw `.tf` files misses dynamically computed values |
| **Blast Radius** | Partition state by layer (Network -> DB -> App) | Monolithic giant state files covering the entire enterprise |
| **Drift Detection** | Run `terraform plan -detailed-exitcode` (exit 2 = drift) | Assuming Terraform detects out-of-band untracked resources |
| **Sensitive Data** | `sensitive = true` masks CLI output ONLY | Plaintext secrets still exist in `terraform.tfstate` |
| **Database Migrations** | Use Expand-Contract 4-stage pattern | Renaming columns synchronously with app code causes 500 errors |
| **GitOps Reconcile** | ArgoCD pulls from Git; auto-heals cluster drift | Storing cluster admin credentials in push-based CI runners |
| **Build Caching** | Use BuildKit `cache-to: type=registry,mode=max` | Default `mode=min` misses multi-stage builder caches |
| **Provider Aliases** | `provider "aws" { alias = "west" }` | Forgetting to pass alias into child module `providers` map |
| **Importing Infra** | Use Terraform 1.5+ declarative `import {}` blocks | Imperative `terraform import` CLI without version control |
