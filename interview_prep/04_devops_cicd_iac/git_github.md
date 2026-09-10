# Git, GitHub, GitHub Actions & Enterprise Version Control Interview Guide

> **Scope**: Low-Level Git Internals (Object Store, DAG, Packfiles, Reflog), Branching Strategies (Trunk-Based vs GitFlow), Advanced Git Plumbing & Porcelain, Merge vs Rebase vs Cherry-Pick, GitHub Actions CI/CD Architecture (Self-Hosted Runners, OIDC, Cache), Security & Secrets Management, and Production War-Room Post-Mortems.

---

## Guide Architecture Overview

```
========================================================================================================================
                                     GIT, GITHUB & GITHUB ACTIONS ARCHITECTURE
========================================================================================================================
 [Layer 1: Git Core Internals & Object Storage]     --> Content-Addressable Store, Blobs, Trees, Commits, Packfiles, DAG
 [Layer 2: Advanced Branching, Rebasing & Recovery]--> Fast-Forward, Three-Way Merge, Rebase Onto, Reflog, Bisect
 [Layer 3: GitHub Actions Enterprise CI/CD]         --> Workflow Triggers, Matrix Builds, Self-Hosted Runners, OIDC, Caching
 [Layer 4: Enterprise Repository Governance & Sec]  --> Branch Protections, CODEOWNERS, GPG Signing, CodeQL, Secret Scanning
 [Layer 5: Ultra-Deep Real-World War-Room Cases]    --> 10 Production Disasters (Force Push Overwrite, Detached HEAD Loss)
 [Layer 6: Beginner Mistakes & Anti-Patterns]       --> 8 Fatal Engineering Traps (Merge Commits, Large Binary Tracking)
 [Layer 7: Globally Reported Production Incidents]  --> Real Outages (GitHub Actions Global Outage, Codecov Supply Chain)
 [Layer 8: Rapid-Fire Cheat Sheet & Summary Matrix] --> High-Speed Plumbing Commands, Git Architecture Complexity Table
========================================================================================================================
```

---

# Layer 1: Git Core Internals & Object Storage

---

### Scenario 1: Git Object Store Architecture: Blobs, Trees, Commits & Annotated Tags
**Interviewer Evaluation:** Assesses mechanical sympathy for Git's content-addressable storage engine, SHA-1/SHA-256 cryptographic hashing, and immutable object representations in `.git/objects`.

#### Technical Deep Dive
Git is not a delta-based version control system; it is a **content-addressable filesystem**:
1. **The Object Store (`.git/objects`)**:
   Every object is identified by a 40-character hexadecimal SHA-1 hash (or 64-char SHA-256). The first 2 characters form the subfolder name; the remaining 38 characters form the filename (e.g., `.git/objects/4b/825dc...`).
   - Object Header: `[type] [size]\0[content]` compressed with zlib.
2. **The 4 Fundamental Git Object Types**:
   - **Blob**: Stores pure file content bytes with **zero filename or permissions metadata**.
   - **Tree**: Represents a directory. Maps file modes, object types, SHA-1 hashes, and filenames.
   - **Commit**: Points to a root Tree SHA, parent commit SHA(s), author metadata, committer metadata, and commit message.
   - **Annotated Tag**: An immutable point in history pointing to a specific commit SHA with its own tagger metadata and GPG signature.

```
Git Object Relationship Graph:
+-------------------------------------------------------------+
| Commit Object (SHA: a1b2c3d)                                |
| tree: 9f82ab (Points to Root Tree)                          |
| parent: 0e74f1 (Previous Commit SHA)                        |
| author: Linus <torvalds@kernel.org>                         |
+-------------------------------------------------------------+
                            |
                            v
+-------------------------------------------------------------+
| Tree Object: Root (SHA: 9f82ab)                             |
| 100644 blob 3e829a   README.md                              |
| 040000 tree 5c71be   src/                                   |
+-------------------------------------------------------------+
                            |
                            v
+-------------------------------------------------------------+
| Tree Object: src/ (SHA: 5c71be)                             |
| 100644 blob 7d99fa   main.go                                |
+-------------------------------------------------------------+
```

```bash
# Inspecting Git objects using low-level plumbing commands:
git cat-file -t a1b2c3d   # Returns: commit
git cat-file -p a1b2c3d   # Pretty-prints raw commit headers & root tree SHA
git rev-parse HEAD        # Returns exact 40-character commit hash
```

**Follow-Up Trap & Winning Answer:**
*Trap:* "If two identical 10MB files exist in different directories with different names, how many blobs does Git store?"
*Answer:* Exactly **one** blob. Because Git is content-addressable, the SHA-1 hash depends solely on the file content bytes (`blob [size]\0[content]`). The file names and paths are stored in their respective Tree objects, which both point to the same single blob hash, saving massive disk space.

---

### Scenario 2: Git References: Branches, Tags, HEAD & Symbolic References
**Interviewer Evaluation:** Evaluates knowledge of `.git/refs`, symbolic references (`HEAD`), detached HEAD state mechanics, and lightweight vs annotated tags.

#### Technical Deep Dive
- **Branches (`.git/refs/heads/`)**:
  A branch in Git is simply a **41-byte text file** containing the 40-character hexadecimal commit SHA of its latest commit, plus a newline character!
- **HEAD (`.git/HEAD`)**:
  A symbolic reference pointing to the currently checked-out branch:
  `ref: refs/heads/main`
- **Detached HEAD State**:
  Occurs when `HEAD` points directly to a commit SHA rather than a branch reference (e.g., `git checkout <commit-sha>`). Any commits created in detached HEAD state do not belong to any branch; if you switch branches without creating a reference, those commits become unreachable "dangling commits" eligible for garbage collection by `git gc`.

```bash
# Creating a branch is just writing a 41-byte file:
echo "a1b2c3d4e5f60718293a4b5c6d7e8f9012345678" > .git/refs/heads/feature-x
# Is identical to:
git branch feature-x
```

**Follow-Up Trap & Winning Answer:**
*Trap:* "What is the difference between a Lightweight Tag and an Annotated Tag?"
*Answer:* A Lightweight Tag is merely a static pointer file in `.git/refs/tags/` containing a commit SHA. An Annotated Tag is a full, immutable first-class Git object in `.git/objects` with its own author, date, message, and optional cryptographic GPG signature. Production releases must always use annotated tags (`git tag -a v1.0.0 -m "Release 1.0.0"`).

---

### Scenario 3: Git Packfiles (`.pack`) & Delta Compression
**Interviewer Evaluation:** Assesses understanding of Git storage optimization, sliding-window delta compression, and the `.pack` / `.idx` file architecture.

#### Technical Deep Dive
Storing every file version as an individual compressed loose object in `.git/objects` wastes disk space and exhausts OS inodes.
- **Git Packfile (`git gc` / `git repack`)**:
  1. Identifies objects with similar names, sizes, and content within a sliding window.
  2. Computes **directed delta compression**: stores one full version (usually the *newest* version to prioritize checkout speed) and records older versions as byte-level deltas against it.
  3. Bundles thousands of loose objects into a single binary **`.pack`** file.
  4. Generates an accompanying **`.idx` (Index)** file containing fan-out tables and binary search offsets to locate any object within the `.pack` in $O(\log N)$ time.

**Follow-Up Trap & Winning Answer:**
*Trap:* "Why does Git delta compression store the NEWEST version of a file whole and compute deltas backwards for OLDER versions?"
*Answer:* Developers checkout, build, and run the *latest* commit far more frequently than historical commits. Storing the newest file whole allows immediate decompression without traversing delta chains, optimizing checkout performance for daily work.

---

### Scenario 4: The Three Trees: Working Directory, Index (Staging), and HEAD
**Interviewer Evaluation:** Evaluates comprehension of Git's three-state architecture, index binary structure (`.git/index`), and file state transitions.

#### Technical Deep Dive
```
The Three Trees Architecture:
+--------------------+      git add      +--------------------+     git commit     +--------------------+
| Working Directory  | ----------------> |  Index (Staging)   | -----------------> |    HEAD (Commit)   |
| (Sandbox on disk)  | <---------------- | (.git/index binary)| <----------------- | (Committed Repo)   |
+--------------------+    git restore    +--------------------+    git reset --soft+--------------------+
```

- **Working Directory**: Physical files on the filesystem.
- **Index (`.git/index`)**: A binary cache mapping file paths, timestamps, file sizes, permissions, and SHA-1 blob hashes. Acts as the exact proposed blueprint for the next commit.
- **HEAD**: The tree representation of the current active commit in Git history.

```bash
# Moving changes between the three trees:
git reset --soft HEAD~1   # Moves HEAD back; leaves Index and Working Dir untouched
git reset --mixed HEAD~1  # Moves HEAD back and resets Index; leaves Working Dir untouched (Default)
git reset --hard HEAD~1   # Resets HEAD, Index, AND Working Dir (DESTRUCTIVE!)
```

**Follow-Up Trap & Winning Answer:**
*Trap:* "What command safely unstages a file from the Index without touching the working directory in modern Git?"
*Answer:* In Git 2.23+, use `git restore --staged <file>`. In legacy Git, `git reset HEAD <file>`.

---

# Layer 2: Advanced Branching, Rebasing & Recovery

---

### Scenario 5: Merge Strategies: Fast-Forward vs Three-Way Merge vs Squash
**Interviewer Evaluation:** Tests understanding of DAG branch topology, commit history readability, merge conflict resolution, and enterprise pull request policies.

#### Technical Deep Dive
1. **Fast-Forward Merge (`git merge --ff`)**:
   - Occurs when the target branch has had no new commits since the feature branch diverged.
   - Simply moves the target branch pointer forward to point to the feature branch head. Creates **no merge commit**.
2. **Three-Way Merge (`git merge --no-ff`)**:
   - Used when both branches have diverged.
   - Finds the **Common Ancestor** (Base), compares Base against Branch A and Branch B, and creates a new **Merge Commit** with two parent pointers (`parent1`, `parent2`).
3. **Squash Merge (`git merge --squash`)**:
   - Takes all commits from the feature branch, combines their net changes into a single working tree state, and commits it onto the target branch as a single new commit. Destroys individual intermediate feature commit history.

```
Three-Way Merge Topology:
      B1 --- B2 (feature)
     /         \
--- A1 --- A2 - M (main - 3-way merge commit with 2 parents)
```

**Follow-Up Trap & Winning Answer:**
*Trap:* "Why do high-velocity engineering organizations mandate Squash Merge or Rebase Merge on GitHub Pull Requests?"
*Answer:* To maintain a clean, linear, bisectable commit history. When developers commit messy checkpoints (`WIP`, `fix typo`, `fix test`), merging them directly pollutes `main` with hundreds of noise commits, complicating `git bisect` automated regression tracking.

---

### Scenario 6: Advanced Rebasing: `git rebase -i` and `--onto`
**Interviewer Evaluation:** Assesses manipulating DAG history, rewriting commit trees, and surgical branch relocation.

#### Technical Deep Dive
- **Standard Rebase**:
  Replays commits from the current branch on top of another base tip by generating new commits with new SHA hashes:
  $$\text{Old: } A \to B \to C \quad \xrightarrow{\text{rebase onto } D} \quad D \to B' \to C'$$
- **Surgical Relocation with `--onto`**:
  ```bash
  # Syntax: git rebase --onto <new-base> <old-base> <branch>
  git rebase --onto main feature-v1 feature-v2
  ```
  Extracts all commits in `feature-v2` that occurred *after* `feature-v1` and transplants them directly onto `main`, dropping `feature-v1` commits from the lineage.

```
git rebase --onto main feature-v1 feature-v2:
Before:
main:       M1 --- M2
               \
feature-v1:     F1 --- F2
                         \
feature-v2:               V1 --- V2

After:
main:       M1 --- M2 --- V1' --- V2' (Transplanted cleanly!)
```

**Follow-Up Trap & Winning Answer:**
*Trap:* "What is the Golden Rule of Rebasing?"
*Answer:* **Never rebase commits that exist outside your local repository and have been shared publicly.** Rebasing changes commit SHAs. If other engineers based work on the old commit hashes, pushing a rebased branch forces upstream diverged histories, requiring complex manual reconciliations for the entire team.

---

### Scenario 7: Disaster Recovery with `git reflog` & Dangling Objects
**Interviewer Evaluation:** Evaluates ability to recover accidentally deleted branches, lost commits, and undone hard resets.

#### Technical Deep Dive
Git almost never deletes commit objects immediately. Even after `git reset --hard HEAD~10` or `git branch -D feature`:
- **The Reflog (`.git/logs/`)**:
  Records every single movement of `HEAD` and local branch tips (checkouts, commits, rebases, resets) for a retention window (default 90 days).
- **Recovery Workflow**:
  1. Run `git reflog` to identify the commit SHA right before the catastrophic action:
     ```bash
     git reflog
     # Output:
     # 8f92a1c HEAD@{0}: reset: moving to HEAD~10
     # 4b71d9e HEAD@{1}: commit: Completed payment gateway implementation
     ```
  2. Restore the lost state into a new branch immediately:
     ```bash
     git branch recovered-feature 4b71d9e
     ```
- **Dangling Objects (`git fsck`)**:
  If reflog entries expire, orphaned commits can still be retrieved via `git fsck --lost-found`.

**Follow-Up Trap & Winning Answer:**
*Trap:* "Does `git reflog` record commits made on remote repositories or branches on other machines?"
*Answer:* **No.** `reflog` is purely local to your specific machine. It tracks local pointer movements. If an engineer performs `git push --force` and overwrites a remote branch, other engineers must use their own local reflogs or remote audit logs to restore the overwritten commits.

---

### Scenario 8: Binary Search Debugging with `git bisect`
**Interviewer Evaluation:** Assesses automated regression isolation across thousands of commits using logarithmic search ($O(\log N)$).

#### Technical Deep Dive
When a bug appears in production and the codebase has 2,000 commits between the last good release and HEAD:
1. `git bisect start`
2. `git bisect bad` (current HEAD is broken)
3. `git bisect good v1.4.0` (last known working version)
4. Git checks out the midpoint commit ($1000^{\text{th}}$ commit).
5. Developer tests the build, enters `git bisect good` or `bad`.
6. Repeats $\log_2(2000) \approx 11$ steps to identify the exact breaking commit.

```bash
# Fully Automated Git Bisect with Test Script:
git bisect start HEAD v1.4.0
git bisect run ./run-integration-tests.sh
# Git automatically runs the script at every step and reports the culprit commit!
```

---

# Layer 3: GitHub Actions Enterprise CI/CD

---

### Scenario 9: GitHub Actions Workflow Architecture & Execution Lifecycle
**Interviewer Evaluation:** Assesses workflow syntax, event triggers (`pull_request`, `workflow_dispatch`), job execution dependencies, and runner isolation.

#### Technical Deep Dive
GitHub Actions orchestrates automation through YAML workflows in `.github/workflows/`:
- **Workflow**: Automated process triggered by events (`push`, `pull_request_target`, `schedule`).
- **Job**: Set of steps executed on the same runner VM/container. Jobs execute in **parallel** by default unless chained with `needs: [job_a]`.
- **Step**: Individual task (either a shell `run` command or an `action`).
- **Contexts & Expressions**: `${{ github.event_name }}`, `${{ secrets.API_TOKEN }}`.

```yaml
name: Enterprise Production Pipeline
on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  lint-and-test:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0 # Full history for SonarQube analysis

      - name: Setup Go Runtime
        uses: actions/setup-go@v5
        with:
          go-version: '1.22'
          cache: true # Built-in dependency caching

      - name: Execute Tests
        run: go test -v -race ./...

  deploy:
    needs: [ lint-and-test ] # Enforces dependency sequence
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'
    runs-on: ubuntu-latest
    steps:
      - run: echo "Deploying to production..."
```

**Follow-Up Trap & Winning Answer:**
*Trap:* "What is the critical security difference between `on: pull_request` and `on: pull_request_target`?"
*Answer:* `pull_request` runs in the context of the untrusted fork branch with read-only repository permissions and zero access to secrets. `pull_request_target` runs in the context of the base target branch and has access to repository secrets. If a workflow with `pull_request_target` checks out and executes code from the fork PR (`actions/checkout` with `ref: ${{ github.event.pull_request.head.sha }}`), it exposes the repository to malicious secret exfiltration.

---

### Scenario 10: OIDC (OpenID Connect) Cloud Authentication in GitHub Actions
**Interviewer Evaluation:** Tests modern passwordless cloud authentication (AWS, Azure, GCP) eliminating long-lived static cloud access keys.

#### Technical Deep Dive
- **Legacy Risk**: Storing static `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` in GitHub Secrets. If compromised, attackers gain permanent cloud access.
- **OIDC Passwordless Authentication**:
  1. GitHub Actions runner requests a short-lived OIDC JSON Web Token (JWT) signed by GitHub's private key.
  2. Runner sends JWT to AWS Security Token Service (STS) via `AssumeRoleWithWebIdentity`.
  3. AWS validates the cryptographic signature against GitHub's public OIDC keys (`token.actions.githubusercontent.com`).
  4. AWS verifies claims (repository, branch, environment). If valid, AWS returns a temporary 1-hour IAM session token.

```yaml
jobs:
  deploy-to-aws:
    runs-on: ubuntu-latest
    permissions:
      id-token: write # Mandatory to request GitHub OIDC JWT
      contents: read
    steps:
      - uses: actions/checkout@v4
      - name: Configure AWS Credentials via OIDC
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: arn:aws:iam::123456789012:role/GitHubActionsDeploymentRole
          aws-region: us-east-1
      - run: aws s3 ls # Authenticated without static keys!
```

---

# Layer 4: Enterprise Repository Governance & Security

---

### Scenario 11: Branch Protection Rules, Merge Queues & CODEOWNERS
**Interviewer Evaluation:** Evaluates enforcing code review policies, branch protection rules, linear history, and merge queue synchronization.

#### Technical Deep Dive
1. **Branch Protection Rules**:
   - Require pull request reviews before merging (minimum 2 approvals).
   - Require status checks to pass (CI tests, SonarQube, security scans).
   - Require signed commits (GPG/SSH).
   - Enforce linear history (blocks merge commits).
2. **Merge Queues (GitHub Merge Queue)**:
   - Eliminates "Merge Skew": When PR 1 and PR 2 pass CI independently, but break when merged together.
   - The Merge Queue builds and tests speculative merge combinations (`main + PR1 + PR2`) in temporary branches before committing to `main`.
3. **CODEOWNERS (`.github/CODEOWNERS`)**:
   - Defines team ownership per directory path:
     ```
     *                   @org/core-platform
     /src/auth/          @org/security-team
     /infra/terraform/   @org/devops-leads
     ```
   - Automatically requests reviews and blocks PR merges without designated team sign-off.

---

# Layer 5: Ultra-Deep Real-World War-Room Incidents

---

### Scenario 12: War Room: The `git push --force` Production Branch Destruction
**Interviewer Evaluation:** Assesses emergency recovery when a developer accidentally force-pushes and overwrites 3 months of commits on `main`.

#### Incident Scenario
A developer intended to force-push their feature branch, but had their upstream set to `origin/main`. `git push --force origin main` executed, overwriting 3 months of committed code with their local branch. All CI/CD pipelines failed, and `main` was rewound by 450 commits.

#### Root Cause Analysis
1. The repository lacked branch protection rules on `main`.
2. The remote Git server updated `refs/heads/main` to the developer's commit SHA.
3. However, the original 450 commits still existed as unreferenced objects in the remote server's object store.

#### Remediation & Prevention
1. **Emergency Remediation**:
   - Identified another engineer whose local repository had synced `origin/main` that morning.
   - Inspected their local reflog: `git reflog show origin/main`.
   - Identified the pre-incident commit SHA (`7d9a12c`).
   - Pushed the recovered commit SHA back: `git push --force-with-lease origin 7d9a12c:main`.
2. **Permanent Prevention**:
   - Enabled GitHub Branch Protection on `main`: Checked **"Do not allow force pushes"** and **"Do not allow deletions"**.

---

### Scenario 13: War Room: The 50GB Repository Git Clone Pipeline Freeze
**Interviewer Evaluation:** Tests diagnosing repository bloat caused by accidentally committing large binary models and cleaning Git history with `git-filter-repo`.

#### Incident Scenario
Every CI build in a machine learning company began taking 45 minutes just on the `actions/checkout` step. Developers were unable to clone the repository locally due to network timeouts.

#### Root Cause Analysis
1. A developer accidentally committed a 4GB `.tar.gz` model weight file 6 months prior.
2. Even though the file was deleted in a subsequent commit, **every clone downloaded the full 4GB object** because it remained anchored in historical commits.
3. With 10 feature branches branching off that point, the packfile ballooned to 48 GB.

#### Remediation & Prevention
- **History Purge with `git-filter-repo`**:
  ```bash
  # Rewrite all commits to completely erase the file from Git history:
  git-filter-repo --path models/weights.tar.gz --invert-paths
  git gc --prune=now --aggressive
  ```
- Repository size dropped from 48GB to 120MB.
- Configured **Git LFS (Large File Storage)** for legitimate large assets.
- Added pre-commit hooks blocking any file $> 50\text{ MB}$.

---

# Layer 6: Beginner Mistakes & Anti-Patterns

---

### Anti-Pattern 1: Blind `git push --force` Instead of `--force-with-lease`
- ❌ **The Anti-Pattern**: Using `git push -f` to update a remote branch after rebasing.
- 💥 **Production Impact**: If a teammate pushed commits to that branch in the meantime, `git push -f` silently obliterates their work without warning.
- ✅ **The Fix**: Use `git push --force-with-lease`. It checks that the remote ref matches your local remote-tracking ref before overwriting.
- 🧠 **Architectural Principle**: Never force-overwrite a remote pointer without verifying that no unseen concurrent updates occurred.

---

### Anti-Pattern 2: Committing API Secrets and Private Keys
- ❌ **The Anti-Pattern**: Committing `.env` or `id_rsa` into Git, then running `git rm` in the next commit.
- 💥 **Production Impact**: The secret remains permanently readable in Git history forever. Automated bots scrape public GitHub commits within seconds.
- ✅ **The Fix**: Immediately revoke and rotate the secret. Remove the commit using `git-filter-repo` or BFG Repo-Cleaner.
- 🧠 **Architectural Principle**: A committed secret must always be assumed fully compromised. Deleting it in a subsequent commit does not secure it.

---

# Layer 7: Globally Reported Production Incidents & Post-Mortems

---

### Incident 1: Codecov Bash Uploader Supply Chain Breach (2021)
- 🚨 **The Incident**: In April 2021, attackers modified Codecov's Bash uploader script used inside thousands of GitHub Actions CI pipelines worldwide.
- 🔍 **Root Cause**: Attackers gained access to a compromised Docker credential, modified the hosted bash script to siphon CI environment variables (AWS keys, database passwords, tokens) to an external server.
- 🛠️ **Remediation**: Codecov rotated all infrastructure keys, deprecated bash scripts, and introduced cryptographic integrity verification.
- 🛡️ **Architectural Guardrail**: Never execute unpinned external bash scripts (`curl | bash`) in CI; always pin GitHub Actions to full **commit SHAs** rather than mutable tags (`uses: actions/checkout@b4ffde65f46336ab88eb53be808477a3936bae11`).

---

# Layer 8: Rapid-Fire Cheat Sheet & Interview Summary Matrix

---

### Essential Git Plumbing vs Porcelain Matrix

| Command | Type | Function |
| :--- | :--- | :--- |
| `git cat-file -p <sha>` | Plumbing | Pretty-prints object content (blob, tree, commit) |
| `git rev-parse HEAD` | Plumbing | Returns exact 40-character SHA of current commit |
| `git reflog` | Porcelain | Shows chronological history of local HEAD pointer updates |
| `git checkout -b <name>` | Porcelain | Creates and switches to branch (updates `.git/HEAD`) |
| `git reset --soft HEAD~1`| Porcelain | Moves HEAD backward, keeping Index and Working Directory intact |
| `git fsck --lost-found` | Plumbing | Scans object database for orphaned dangling commits |

---

### The Golden Git & CI/CD Interview Rules
1. **Git is content-addressable**: Files are stored as immutable SHA-hashed blobs; directories are trees mapping filenames to hashes.
2. **Always use `--force-with-lease`**: Never use raw `git push -f` on shared or feature branches.
3. **Pin GitHub Actions to commit SHAs**: Avoid tag pinning (`@v4`) in security-critical enterprise pipelines to prevent supply chain poisoning.
4. **Use OIDC for cloud deployments**: Eliminate permanent AWS/GCP static secrets from GitHub repository settings.
5. **Never commit secrets**: Deleting a secret in commit $N+1$ leaves it visible in commit $N$ forever.
