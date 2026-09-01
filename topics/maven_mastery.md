[Back to Home](../README.md) | [Interview Prep Guide](interview_prep.md) | [Tech Glossary](glossary.md)

# 📦 Maven & Build Mastery: The Architect's Assembly Line

Master the logic, dependency trees, and automation lifecycles of modern Java build systems. From Maven's strict convention-over-configuration to Gradle's high-performance DSL flexibility and Jenkins' orchestration power.

---

## 📑 Table of Contents
1. [👨‍🍳 The Maven Chef: POM, Repositories, Artifacts](#the-maven-chef-pom-repositories-artifacts)
2. [⚙️ The Assembly Line: 50 Maven Scenarios by Phase](#the-assembly-line-50-maven-scenarios-by-phase)
3. [🌳 The Dependency Family Tree (BOM, Scopes, Conflicts)](#the-dependency-family-tree)
4. [🐘 Gradle Mastery: The High-Performance Graph (50 Scenarios)](#gradle-mastery-the-high-performance-graph)
5. [🏗️ Jenkins Pipelines: The Automation Flow (50 Scenarios)](#jenkins-pipelines-the-automation-flow)
6. [🔬 Technical Deep Dives (The "Under the Hood" Logic)](#technical-deep-dives)
7. [🛠️ The Ultimate Maven One-Liner Cheat Sheet (100+ Commands)](#the-ultimate-maven-one-liner-cheat-sheet)

---

## 👨‍🍳 The Maven Chef: POM, Repositories, Artifacts

Think of Maven as a **Robot Chef**. Instead of you manually hunting for ingredients (libraries), you give it a **Recipe (the POM file)**.

### Core Terminologies
*   **POM (Project Object Model):** The `pom.xml`. The "Brain" of your project. If it’s not here, it doesn't exist.
*   **GAV (G-roupId, A-rtifactId, V-ersion):** The "ID Card" for every library.
    *   `GroupId`: The company (e.g., `com.fintech`).
    *   `ArtifactId`: The product (e.g., `auth-service`).
    *   `Version`: The specific release (`1.0.0-SNAPSHOT`).
*   **The Pantry (Repositories):**
    1.  **Local Repository:** Your computer's cache (found in `~/.m2`).
    2.  **Central Repository:** The public online supermarket.
    3.  **Remote Repository:** Your company's private warehouse for internal code (Nexus/Artifactory).

---

## ⚙️ The Assembly Line: 50 Maven Scenarios by Phase

Maven follows a strict **Conveyor Belt** logic. Here is the definitive roadmap of real-world scenarios.

### Phase 1: Validate & Initialize (Foundations)
| # | Scenario | Diagnosis & Fix |
| :--- | :--- | :--- |
| 1 | **Wrong Java Version** | Use `maven-enforcer-plugin` to kill the build if the JDK is not version 17. |
| 2 | **Missing Env Variable** | Use Enforcer's `requireEnvironmentVariable` rule for DB credentials. |
| 3 | **Broken POM Schema** | Catch syntax errors (missing tags) before starting the build. |
| 4 | **Checksum Failure** | Verify that downloaded JARs haven't been tampered with. |
| 5 | **Banned Licenses** | Scan for legal risks (GPL/LGPL) before compiling. |

> [!TIP]
> **Pro Level:** Run `mvn help:effective-pom` to see the "Final Truth"—the merged XML after all parents and profiles are applied.

### Phase 2: Compile (Turning Text to Code)
| # | Scenario | Diagnosis & Fix |
| :--- | :--- | :--- |
| 11 | **Slow Compilation** | Enable `fork` and `parallel` in the `maven-compiler-plugin`. |
| 12 | **Mixed Languages** | Use `gmavenplus` or `kotlin-maven-plugin` to compile Java + Groovy/Kotlin. |
| 13 | **Resource Filtering** | Swap `${db.url}` in `application.properties` with profile-specific values. |
| 14 | **Lombok Logic** | Annotation processing happens here to generate getters/setters. |
| 15 | **Incremental Build** | Maven only recompiles modified files. Use `-U` to force a check. |

### Phase 3: Test & Verify (Quality Control)
| # | Scenario | Diagnosis & Fix |
| :--- | :--- | :--- |
| 21 | **Run Single Test** | `mvn test -Dtest=LoginTest`. |
| 22 | **Flaky Tests** | Use `rerunFailingTestsCount` in Surefire to retry failed tests twice. |
| 23 | **Security Scanning** | Run **OWASP Dependency-Check** in the `verify` phase to find CVEs. |
| 24 | **Code Coverage** | Use **JaCoCo**; fail the build if coverage is below 80%. |
| 25 | **Integration Tests** | Use `Failsafe` (not Surefire) so cleanup runs even if tests fail. |

> [!CAUTION]
> **Production Safety:** Always run `mvn verify` in Jenkins. It ensures the JAR is not just "created," but is actually "valid" and "secure."

### Phase 4: Install & Deploy (Shipping)
| # | Scenario | Diagnosis & Fix |
| :--- | :--- | :--- |
| 41 | **Local Sharing** | Run `mvn install` so other local team projects can use your library. |
| 42 | **Remote Publishing** | Run `mvn deploy` to upload to the Bank's central Nexus repo. |
| 43 | **Auth Failure** | Put Nexus credentials in `settings.xml` (never in `pom.xml`). |
| 44 | **Release Immutability** | A `1.0.0` release can never be changed. Use `SNAPSHOT` while coding. |
| 45 | **Version Bumping** | Use `mvn versions:set -DnewVersion=1.1.0` to sync 50 microservices. |

---

## 🌳 The Dependency Family Tree

Managing "Friend of a Friend" libraries is critical for security in Fintech.

### 1. The "Nearest Win" Strategy
*   **The Conflict:** Library A needs `Jackson 2.12`, Library B needs `Jackson 2.15`.
*   **The Logic:** Maven picks the one with the shortest path to your POM.
*   **The Fix:** Use `<dependencyManagement>` in a Parent POM to "force" a specific version globally.

### 2. Dependency Scopes (The Access Pass)
| Scope | When is it used? | Analogy | Real World Example |
| :--- | :--- | :--- | :--- |
| **Compile** | Everywhere. | All-Access Pass. | Standard web libraries. |
| **Provided** | Coding only. | DIY Pass. | `tomcat-embed` (Server has its own). |
| **Runtime** | Running only. | After-Party Pass. | JDBC Database Driver. |
| **Test** | Testing only. | Rehearsal Pass. | JUnit / Mockito. |

---

## 🐘 Gradle Mastery: The High-Performance Graph

Gradle doesn't use linear phases; it uses a **Task Graph**. It is the "Powerhouse" for large multi-module projects.

### 50 Gradle Scenarios by Goal
#### Performance & Speed
1.  **Incremental Build:** Gradle takes a hash (snapshot) of inputs and outputs. If they match, it skips the task (`UP-TO-DATE`).
2.  **Build Cache:** Enable `org.gradle.caching=true` to reuse results from other developers' machines.
3.  **The Daemon:** A background "warm" process that keeps the JVM ready, reducing startup time by 80%.

#### Custom Logic (Groovy/Kotlin DSL)
| Scenario | Tool/Logic | Why? |
| :--- | :--- | :--- |
| **Clean Build Script** | `buildSrc` Folder | Move complex logic out of `build.gradle` into Java/Kotlin code. |
| **Live Link Testing** | `includeBuild` | Test a local library and app together without publishing to Maven Local. |
| **Shared Auth Keys** | `gradle.properties` | Load secrets from `~/.gradle/` to keep them out of Git. |

---

## 🏗️ Jenkins Pipelines: The Automation Flow

Jenkins is the "Orchestrator" that connects Git, Build, and Deploy.

### 50 Jenkins Scenarios: The Resilient Pipeline
#### 1. "Stash" vs. "Archive"
*   **Stash:** Temporary files moved between stages (e.g., Build -> Test). Deleted when the job finishes.
*   **Archive:** Final results (the `.jar`) kept permanently in the UI for auditors to download.

#### 2. Declarative vs. Scripted
*   **Declarative (`pipeline {}`):** Strict, readable form. Use for 95% of Fintech projects.
*   **Scripted (`node {}`):** Raw Groovy logic. Use only for extremely complex deployments.

#### 3. Security Hardening
*   **Credentials Binding:** Use `withCredentials` to mask passwords in logs (they show as `****`).
*   **Shared Libraries:** Move standard "Security Scans" into a central Git repo. Every project calls one line: `securityScan()`.

---

## 🔬 Technical Deep Dives (The "Under the Hood" Logic)

### 1. The BOM (Bill of Materials) vs. Parent POM
*   **The Conflict:** Java only allows **one** Parent.
*   **The Solution:** Use a **BOM** to "import" version rules from many sources (Spring, Hibernate, Cloud) without losing your Parent POM.

### 2. Configuration vs. Execution (Gradle)
*   **The Trap:** Putting `println("Hello")` directly in the script runs it during the "Graph Building" phase.
*   **The Fix:** Always put work inside `doLast { ... }` so it only runs when the task is actually called.

---

## 🛠️ The Ultimate Maven One-Liner Cheat Sheet

### Troubleshooting & Monitoring
*   `mvn dependency:tree` - The classic hierarchy view.
*   `mvn dependency:tree -Dverbose` - Shows **precisely** why a version was omitted (Conflicts).
*   `mvn dependency:analyze` - Find "Unused declared" vs "Used undeclared" dependencies.
*   `mvn help:effective-settings` - Debug Proxy/Auth issues in your `settings.xml`.

### Speed & Performance
*   `mvn install -T 1C` - Parallel build (1 thread per core).
*   `mvn install -pl :auth-service -am` - Build one module AND its parents.
*   `mvn clean install -U` - Force download of latest SNAPSHOTs.

### Deployment & Release
*   `mvn versions:display-dependency-updates` - See every library that is out of date.
*   `mvn release:prepare release:perform` - The automated "Audit-Ready" deployment flow.

---

[⬆️ Back to Top](#📦-maven--build-mastery-the-architects-assembly-line)
