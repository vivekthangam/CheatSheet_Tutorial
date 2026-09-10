# HashiCorp Terraform & Infrastructure as Code (IaC) Architecture Interview Guide

> **Scope**: Terraform Core Architecture, State File Internals & Distributed Locking, Dependency Directed Acyclic Graph (DAG), Remote Backends (S3, DynamoDB), Module Engineering, Drift Detection, Zero-Downtime Lifecycle Rules, Terragrunt, Secret Handling, and Production War-Room Post-Mortems.

---

## Guide Architecture Overview

```
========================================================================================================================
                                     TERRAFORM & INFRASTRUCTURE AS CODE (IAC)
========================================================================================================================
 [Layer 1: Core Architecture & State Management]     --> State File Internals, Distributed Locking (DynamoDB), Providers
 [Layer 2: Execution Engine, Plan & Graph Traversal] --> Directed Acyclic Graph (DAG), Refresh, Plan, Apply Phase AST
 [Layer 3: Enterprise Modular Design & Terragrunt]   --> Reusable Modules, Terragrunt DRY Backends, Workspaces
 [Layer 4: Zero-Downtime Lifecycle & Drift Detection]--> create_before_destroy, prevent_destroy, Drift Remediation
 [Layer 5: Ultra-Deep Real-World War-Room Cases]     --> 10 Production Disasters (State File Corruption, Concurrent Apply)
 [Layer 6: Beginner Mistakes & Anti-Patterns]        --> 8 Fatal Engineering Traps (Local State, Unpinned Provider Versions)
 [Layer 7: Globally Reported Production Incidents]   --> Real Outages (Global VPC Deletion via Misconfigured Terraform Apply)
 [Layer 8: Rapid-Fire Cheat Sheet & Summary Matrix]  --> High-Speed CLI Commands, Lifecycle Rules, State Commands
========================================================================================================================
```

---

# Layer 1: Core Architecture & State Management

---

### Scenario 1: Terraform State Architecture (`terraform.tfstate`) & Distributed Locking
**Interviewer Evaluation:** Assesses mechanical understanding of Terraform state mapping, real-world resource binding, metadata caching, and distributed locking mechanisms.

#### Technical Deep Dive
Terraform is not a direct API wrapper; it is a **state-driven declarator**:
1. **The State File (`terraform.tfstate`)**:
   - Maps declared HCL configuration blocks to real-world cloud resource IDs (`aws_instance.web` $\to$ `i-09f82a123`).
   - Caches resource attributes to avoid thousands of slow API calls on every run.
   - Tracks dependency relationships and resource metadata (`schema_version`, `serial`).
2. **Distributed State Locking**:
   - When two engineers or CI pipelines run `terraform apply` concurrently, they race to modify the state file.
   - Without locking, the state file suffers **concurrent write corruption**, resulting in lost resource tracking and orphaned cloud resources.
   - **Mechanism (AWS S3 + DynamoDB)**:
     - Before executing any operation, Terraform acquires a lock record in DynamoDB containing an MD5 lock info hash, client hostname, and timestamp.
     - Any subsequent process encountering the lock immediately fails with `Error: Error acquiring the state lock`.
     - Upon completion, the lock record is released.

```
Distributed State Locking Architecture:
[ Engineer A (terraform apply) ] ---> [ DynamoDB Table ] (Acquires Lock: lock_id = 9f8a)
                                              |
[ Engineer B (terraform apply) ] ---> [ DynamoDB Table ] (LOCKED! Error 423 Locked)
                                              |
Engineer A completes ---> Writes state to S3 Bucket ---> Releases DynamoDB Lock
```

```hcl
# Enterprise S3 Remote Backend Configuration:
terraform {
  required_version = ">= 1.7.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.40"
    }
  }
  backend "s3" {
    bucket         = "corp-terraform-state-prod"
    key            = "networking/vpc/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "corp-terraform-locks" # Enforces atomic distributed locking
  }
}
```

**Follow-Up Trap & Winning Answer:**
*Trap:* "What should you do if an engineer's CI build crashes midway and leaves a persistent DynamoDB state lock, blocking all future deployments?"
*Answer:* First verify that no pipeline or process is actively modifying the infrastructure. Then execute `terraform force-unlock <LOCK-ID>`. Never manually delete records directly from the DynamoDB table, as doing so can leave corrupted lock metadata.

---

### Scenario 2: Dependency Directed Acyclic Graph (DAG) & Parallelism
**Interviewer Evaluation:** Evaluates how Terraform resolves resource dependencies, detects circular loops, and optimizes concurrent API operations.

#### Technical Deep Dive
Before applying changes, Terraform builds an internal **Directed Acyclic Graph (DAG)**:
1. **Implicit Dependencies**: Inferred when an attribute of Resource A references an attribute of Resource B (`vpc_id = aws_vpc.main.id`).
2. **Explicit Dependencies**: Declared via `depends_on = [aws_iam_role_policy_attachment.worker]`.
3. **Graph Traversal & Parallelism**:
   - Terraform traverses the graph in topological order.
   - Independent branches of the DAG execute **in parallel** (default concurrency is controlled by `-parallelism=10`).
   - If a circular dependency exists ($A \to B \to A$), graph compilation immediately fails with `CycleException`.

```
Terraform Dependency DAG:
           [ aws_vpc.main ]
             /          \
            v            v
[ aws_subnet.a ]     [ aws_subnet.b ]  (Created in Parallel!)
            \            /
             v          v
       [ aws_security_group.web ]
```

---

# Layer 2: Zero-Downtime Lifecycle Rules & Drift Detection

---

### Scenario 3: Zero-Downtime Updates: `create_before_destroy` vs Default Behavior
**Interviewer Evaluation:** Assesses preventing service outages when mutating immutable cloud attributes that force resource re-creation.

#### Technical Deep Dive
In cloud infrastructure, modifying immutable properties (e.g., changing an EC2 instance AMI or security group name) forces resource recreation:
- **Default Behavior (Destroy-then-Create)**:
  1. Terminate old live instance $A$. (Traffic drops $\to$ **DOWNTIME!**)
  2. Provision new instance $B$.
  3. Register $B$ with Load Balancer.
- **Zero-Downtime Rule (`create_before_destroy = true`)**:
  1. Provision new instance $B$ first.
  2. Wait for $B$ to become healthy and attach to Load Balancer.
  3. Terminate old instance $A$. **Zero downtime achieved!**

```hcl
resource "aws_instance" "app_server" {
  ami           = var.latest_ami_id
  instance_type = "c5.xlarge"

  lifecycle {
    create_before_destroy = true # Guarantees zero-downtime rolling replacement
    prevent_destroy       = true # Blocks accidental deletion via terraform destroy
    ignore_changes        = [tags["LastScanned"]] # Ignores external metadata updates
  }
}
```

---

### Scenario 4: Drift Detection & Reconciliation Mechanics
**Interviewer Evaluation:** Tests diagnosing and remediating manual cloud console modifications ("click-ops") using Terraform.

#### Technical Deep Dive
When someone manually modifies a security group in the AWS Console:
1. **Refresh Phase (`terraform plan`)**:
   - Queries the AWS API to fetch the current live state of all managed resources.
   - Compares Live Cloud State against `terraform.tfstate`.
   - Compares the result against the declared `.tf` configuration.
2. **Reconciliation**:
   - `terraform plan` flags the drift: `~ security_group_rule will be removed`.
   - Running `terraform apply` overwrites the manual changes, restoring the declared Git state.
3. **Automated Drift Detection Pipelines**:
   - Run scheduled read-only `terraform plan -detailed-exitcode` cron jobs in CI.
   - Exit codes:
     - `0`: Success, zero changes.
     - `1`: Error.
     - `2`: Success, **infrastructure drift detected** (triggers Slack/PagerDuty alert).

---

# Layer 3: Enterprise Terragrunt Architecture

---

### Scenario 5: Terragrunt DRY Architecture for Multi-Environment Fleets
**Interviewer Evaluation:** Evaluates managing dev/stage/prod environments without duplicating backend and provider code across directories.

#### Technical Deep Dive
Standard Terraform forces duplicating `backend.tf` and provider configs across every environment folder. **Terragrunt** eliminates repetition (DRY):
1. **Root `terragrunt.hcl`**:
   Declares remote state backend and AWS provider configurations once at the root:
   ```hcl
   remote_state {
     backend = "s3"
     config = {
       bucket         = "corp-state-${get_aws_account_id()}"
       key            = "${path_relative_to_include()}/terraform.tfstate"
       region         = "us-east-1"
       dynamodb_table = "terraform-locks"
     }
   }
   ```
2. **Leaf Environments (`env/prod/vpc/terragrunt.hcl`)**:
   Inherits the root configuration and passes input parameters only:
   ```hcl
   include {
     path = find_in_parent_folders()
   }
   terraform {
     source = "git::git@github.com:org/tf-modules.git//vpc?ref=v2.1.0"
   }
   inputs = {
     cidr_block = "10.0.0.0/16"
   }
   ```

---

# Layer 4: Ultra-Deep Real-World War-Room Incidents

---

### Scenario 6: War Room: The Accidental Production RDS Database Drop
**Interviewer Evaluation:** Evaluates emergency disaster recovery and preventative engineering guards for stateful resources.

#### Incident Scenario
A junior engineer updated a database module. The database name parameter had an unnoticed character typo. Running `terraform apply` resulted in Terraform destroying the production PostgreSQL RDS instance containing 4TB of transactional data.

#### Root Cause Analysis
1. Terraform identified the name change as an immutable attribute modification requiring resource replacement.
2. The resource lacked the `prevent_destroy = true` lifecycle guard.
3. The engineer approved `terraform apply` without carefully inspecting the destruction warning in the plan output.

#### Remediation & Prevention
- **Preventative Guards**:
  ```hcl
  resource "aws_db_instance" "production" {
    identifier = "corp-prod-db"
    deletion_protection = true # AWS API-level protection
    skip_final_snapshot = false
    final_snapshot_identifier = "prod-db-final-snapshot"

    lifecycle {
      prevent_destroy = true # Terraform compiler-level abort!
    }
  }
  ```
- Any future `apply` attempting to destroy this resource immediately halts with a fatal compiler error before touching the cloud API.

---

# Layer 5: Beginner Mistakes & Anti-Patterns

---

### Anti-Pattern 1: Committing State Files to Git Repositories
- ❌ **The Anti-Pattern**: Leaving state stored locally and checking `terraform.tfstate` into Git.
- 💥 **Production Impact**: State files store database passwords, private keys, and cloud tokens in **unencrypted plaintext**. Furthermore, Git cannot provide distributed locking, leading to state file merge conflicts and corruption.
- ✅ **The Fix**: Always configure encrypted remote backends (S3, GCS, Terraform Cloud) with distributed locking.
- 🧠 **Architectural Principle**: State files must be centralized, encrypted at rest, and accessed exclusively via atomic lock managers.

---

# Layer 6: Rapid-Fire Cheat Sheet & Interview Summary Matrix

---

### Critical Terraform CLI Commands & Sizing

| Command | Function | Production Best Practice |
| :--- | :--- | :--- |
| `terraform plan -out=tfplan` | Computes state diff and locks plan | Always save plan output in CI/CD pipelines |
| `terraform apply tfplan` | Applies exact saved plan | Guarantees changes match reviewed plan |
| `terraform state rm <resource>` | Stops managing resource without destroying it | Used during module refactorings |
| `terraform import <res> <id>` | Adopts existing cloud resource into state | Migrating click-ops to IaC |
| `terraform force-unlock <id>` | Breaks stuck distributed lock | Verify no active process is running first |

---

### The Golden Terraform Interview Rules
1. **Always use remote backends with locking**: S3 + DynamoDB to prevent concurrent write corruption.
2. **Protect stateful resources with `prevent_destroy`**: Guard databases, KMS keys, and VPCs against accidental destruction.
3. **Pin provider versions strictly**: Avoid breaking upstream changes by specifying exact minor/patch versions.
4. **Enforce `create_before_destroy` on immutable compute**: Ensure zero-downtime rolling updates.
5. **Always plan to a file in CI/CD**: Run `terraform plan -out=tfplan` and apply the exact artifact.
