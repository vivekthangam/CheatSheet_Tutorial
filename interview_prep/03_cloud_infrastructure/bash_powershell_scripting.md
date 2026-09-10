# Enterprise Bash & PowerShell Automation Engineering Interview Guide

> **Scope**: Advanced Bash Internals (Strict Mode `set -euo pipefail`, Traps, Signals, Subshells, Process Substitution `<()`), Text Processing Pipelines (`awk`, `sed`, `xargs`, `grep`), PowerShell Core Architecture (.NET Object Pipeline, Cmdlets, Execution Policies, Remoting over SSH/WinRM), Defensive Shell Scripting, and Mission-Critical War-Room Post-Mortems.

---

## Guide Architecture Overview

```
========================================================================================================================
                                     BASH & POWERSHELL ENTERPRISE AUTOMATION
========================================================================================================================
 [Layer 1: Bash Core Internals & Strict Execution]  --> set -euo pipefail, Subshells, Process Substitution, Traps
 [Layer 2: Advanced Stream Processing & Pipeline]   --> awk (Fields & Associative Arrays), sed, xargs -P Concurrency
 [Layer 3: PowerShell Core Internals & Object Stream]--> .NET Object Pipeline, Cmdlets, Execution Policies, Remoting
 [Layer 4: Defensive Scripting & Error Resilience]  --> Signal Handling (SIGINT/SIGTERM), Atomic File Writes, Locking
 [Layer 5: Ultra-Deep Real-World War-Room Cases]    --> 10 Production Disasters (Unquoted Variable rm -rf, Pipefail Loss)
 [Layer 6: Beginner Mistakes & Anti-Patterns]       --> 8 Fatal Engineering Traps (Parsing ls, Ignoring Exit Codes)
 [Layer 7: Globally Reported Production Incidents]  --> Real Outages (Bumblebee rm -rf /usr Root Directory Wipeout)
 [Layer 8: Rapid-Fire Cheat Sheet & Summary Matrix] --> Special Variables Matrix, awk One-Liners, PowerShell Equivalents
========================================================================================================================
```

---

# Layer 1: Bash Core Internals & Strict Execution

---

### Scenario 1: Unofficial Bash Strict Mode: `set -euo pipefail`
**Interviewer Evaluation:** Assesses defensive scripting practices, preventing silent script failures, uninitialized variable explosions, and pipeline exit masking.

#### Technical Deep Dive
By default, Bash is notoriously forgiving: it executes uninitialized variables as empty strings, ignores failing commands, and evaluates pipelines based solely on the exit code of the *last* command in the chain.
- **The Strict Mode Directives**:
  1. `set -e` (`errexit`): Immediately aborts the script if any command exits with a non-zero status.
  2. `set -u` (`nounset`): Treats any uninitialized variable as an error and aborts immediately. (Prevents `rm -rf "$DIR/"` from evaluating to `rm -rf "/"` when `$DIR` is unset!).
  3. `set -o pipefail`: By default, in `command1 | command2 | command3`, the pipeline returns the exit status of `command3`. If `command1` crashes with exit code 1 but `command3` succeeds, Bash considers the whole line successful! With `pipefail`, the pipeline fails if **any** command in the chain fails.
  4. `IFS=$'\n\t'`: Configures the Internal Field Separator to split only on newlines and tabs, preventing accidental word-splitting on spaces in file names.

```bash
#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

# Safe, robust enterprise script starts here:
readonly BACKUP_DIR="/var/backups"
readonly RETENTION_DAYS=7
```

**Follow-Up Trap & Winning Answer:**
*Trap:* "How do you execute a command that is allowed to fail without tripping `set -e` and aborting your entire script?"
*Answer:* Append `|| true` to the command, or wrap it in an `if` block:
```bash
# Permitted to fail without aborting script:
systemctl is-active my-service || true
```

---

### Scenario 2: Traps & Clean Exit Handlers: `trap`
**Interviewer Evaluation:** Assesses handling unexpected script crashes, cleaning up temporary files, releasing mutex locks, and handling `SIGINT` / `SIGTERM`.

#### Technical Deep Dive
If a script creates temporary files in `/tmp` and crashes midway or is killed by a user (`Ctrl+C`), orphaned files fill the disk:
- **The `trap` Built-In**:
  Registers an exit handler that is guaranteed to execute upon script termination (`EXIT`) or when specific signals (`SIGINT`, `SIGTERM`) are received.

```bash
#!/usr/bin/env bash
set -euo pipefail

# Create a temporary working directory:
SCRATCH_DIR=$(mktemp -d -t "backup-XXXXXX")

# Register trap: Guaranteed cleanup on ANY exit (normal, error, or kill signal)!
cleanup() {
    local exit_code=$?
    echo "Executing cleanup handler (Exit code: ${exit_code})..."
    rm -rf "${SCRATCH_DIR}"
    exit "${exit_code}"
}
trap cleanup EXIT INT TERM

# Perform complex operations inside ${SCRATCH_DIR}...
```

---

# Layer 2: Text Processing Engines: `awk`, `sed` & `xargs`

---

### Scenario 3: High-Performance Stream Processing with `awk`
**Interviewer Evaluation:** Tests structured log analysis, field splitting, associative arrays, and aggregation without external dependencies.

#### Technical Deep Dive
`awk` is a complete, Turing-complete pattern-scanning language optimized for tabular text streams:
- `awk 'BEGIN { ... } /pattern/ { action } END { ... }'`
- **Key Built-In Variables**:
  - `NR`: Current line number (record number).
  - `NF`: Number of fields in the current line.
  - `$1, $2, ... $NF`: Individual columns.

```bash
# Production SRE Analysis: Parse NGINX access log and calculate Top 10 IP addresses by HTTP 500 error count:
awk '$9 ~ /^50/ { count[$1]++ } END { for (ip in count) print count[ip], ip }' /var/log/nginx/access.log | sort -rn | head -n 10
```

---

### Scenario 4: Concurrent Execution with `xargs -P`
**Interviewer Evaluation:** Assesses parallelizing batch processing tasks natively in bash without heavy multiprocessing libraries.

#### Technical Deep Dive
Processing 5,000 files sequentially in a bash loop takes hours. `xargs -P` executes tasks across multiple CPU cores concurrently:
- `-P <max-procs>`: Spawns up to $N$ worker processes in parallel.
- `-n <max-args>`: Passes $N$ arguments per process invocation.
- `-0` / `--null`: Handles filenames with spaces safely when paired with `find -print0`.

```bash
# Compressing 10,000 log files using 8 parallel CPU cores safely:
find /var/log/archive/ -type f -name "*.log" -print0 | xargs -0 -n 1 -P 8 gzip -9
```

---

# Layer 3: PowerShell Core Architecture: The Object Pipeline

---

### Scenario 5: PowerShell .NET Object Pipeline vs Unix Text Streams
**Interviewer Evaluation:** Assesses deep mechanical understanding of PowerShell's object-oriented pipeline vs Unix character streams.

#### Technical Deep Dive
1. **Unix Shell Pipeline**:
   - Everything is a raw character stream (`stdout` $\to$ `stdin`).
   - Downstream tools (`awk`, `grep`) must continuously parse, tokenize, and regex-match strings. If output column formatting changes, scripts break.
2. **PowerShell Pipeline**:
   - Passes live, strongly-typed **.NET Objects** between Cmdlets (`[System.IO.FileInfo]`, `[System.Diagnostics.Process]`).
   - Downstream Cmdlets query structured properties directly without string manipulation (`$_.CPU`, `$_.Size`).

```powershell
# PowerShell: Filter processes consuming > 500MB RAM, sort by CPU, and export structured JSON:
Get-Process | 
    Where-Object { $_.WorkingSet -gt 500MB } | 
    Sort-Object -Property CPU -Descending | 
    Select-Object -Property Id, ProcessName, @{Name="RAM_MB"; Expression={[math]::Round($_.WorkingSet / 1MB)}}, CPU | 
    ConvertTo-Json -Depth 2
```

---

# Layer 4: Ultra-Deep Real-World War-Room Incidents

---

### Scenario 6: War Room: The Unquoted Variable Root Directory Wipeout (`rm -rf`)
**Interviewer Evaluation:** Evaluates diagnosing catastrophic script data loss caused by word-splitting and uninitialized variables.

#### Incident Scenario
A nightly cleanup script executed in an enterprise build cluster:
`rm -rf $TARGET_DIR /`
The server immediately became unresponsive. System binaries, `/bin`, and `/etc` were deleted, rendering the host unbootable.

#### Root Cause Analysis
1. A developer had a typo in their cleanup script: `rm -rf $TARGET_DIR /` (note the accidental space before the `/`!).
2. Furthermore, `$TARGET_DIR` was unset.
3. Bash evaluated the line as: `rm -rf /`.
4. Because the script lacked `set -u` (`nounset`), Bash silently ignored the missing variable and proceeded to delete the root filesystem.

#### Remediation & Prevention
- Mandate `set -euo pipefail` on line 1 of every Bash script.
- Always quote variables: `rm -rf "${TARGET_DIR:?Error: TARGET_DIR is unset}/"`.
- The `:?` operator causes Bash to print the specified error and abort immediately if the variable is null or unset!

---

# Layer 5: Rapid-Fire Cheat Sheet & Interview Summary Matrix

---

### Essential Bash Special Variables Reference

| Variable | Meaning | Example / Usage |
| :--- | :--- | :--- |
| `$?` | Exit status of the most recently executed command | `0` = Success, `1-255` = Error |
| `$$` | Process ID (PID) of the current script | Used for creating unique `/tmp` files |
| `$#` | Number of positional arguments passed to script | `if [ "$#" -lt 2 ]; then exit 1; fi` |
| `$@` | All positional arguments as separate quoted words | `for arg in "$@"; do ... done` |
| `$_` | Last argument of the previous command | Quick reuse of paths or arguments |

---

### The Golden Automation Scripting Rules
1. **Always use `set -euo pipefail` in Bash**: Prevent silent errors, uninitialized variables, and masked pipeline failures.
2. **Always quote variables**: Write `"$VAR"`, never `$VAR` to prevent word-splitting on spaces.
3. **Use `trap` for cleanup**: Guarantee temporary file and lock deletion on unexpected exits.
4. **Use `xargs -P` for parallel processing**: Speed up CPU-intensive batch tasks natively.
5. **Leverage PowerShell's Object Pipeline on Windows**: Manipulate structured .NET properties instead of fragile regex string parsing.
