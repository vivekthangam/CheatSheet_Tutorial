[🏠 Back to Home](README.md) | [⚡ Dedicated PowerShell Guide](powershell_master_guide.md) | [🐧 Linux Mastery Guide](linux.md) | [📝 Vi, Vim & Nano](vi_vim_nano_master_guide.md) | [💻 IT Tech Words](it_tech_words_master_guide.md)

# 🐚 Bash, Windows Batch & PowerShell Master Guide: Enterprise Terminal Automation & Systems Administration

### *(The Comprehensive Cross-Platform Engineering Handbook: Shell Internals, Syntax Rosetta Stone, Strict Modes, Object Pipelines, Delayed Expansion, SRE Toolkits & War Room Forensics)*

[![Bash 5+](https://img.shields.io/badge/Bash-5.2%20POSIX-green.svg?style=for-the-badge)]()
[![Batch CMD](https://img.shields.io/badge/Windows-Batch%20%7C%20CMD-blue.svg?style=for-the-badge)]()
[![PowerShell 7+](https://img.shields.io/badge/PowerShell-7%2B%20Core%20%7C%20WinPS-purple.svg?style=for-the-badge)]()
[![Cross-Platform Automation](https://img.shields.io/badge/DevOps-Cross--Platform%20SRE-orange.svg?style=for-the-badge)]()

---

## 📑 Master Table of Contents

- [1. The Fundamental Mental Models & Architecture](#1-the-fundamental-mental-models--architecture)
  - [1.1 Text Streams vs. String Characters vs. The .NET Object Pipeline](#11-text-streams-vs-string-characters-vs-the-net-object-pipeline)
  - [1.2 Process Architecture, Subshells & Variable Inheritance](#12-process-architecture-subshells--variable-inheritance)
  - [1.3 Standard Streams & Exit Codes ($?, %ERRORLEVEL%, $LASTEXITCODE)](#13-standard-streams--exit-codes--errorlevel-lastexitcode)
- [2. The Cross-Platform Rosetta Stone (Bash vs. Batch vs. PowerShell)](#2-the-cross-platform-rosetta-stone-bash-vs-batch-vs-powershell)
- [3. Track A: Bash Shell Scripting & Linux Terminal Mastery](#3-track-a-bash-shell-scripting--linux-terminal-mastery)
  - [3.1 The Unforgiving Production Shebang & Strict Mode](#31-the-unforgiving-production-shebang--strict-mode)
  - [3.2 Variables, Arrays & Advanced Parameter Expansion](#32-variables-arrays--advanced-parameter-expansion)
  - [3.3 Conditionals ([[ ... ]]), Pattern Matching & Arithmetic](#33-conditionals----pattern-matching--arithmetic)
  - [3.4 Loops & High-Throughput Stream Reading](#34-loops--high-throughput-stream-reading)
  - [3.5 File Descriptors, Redirection, Process Substitution & HereDocs](#35-file-descriptors-redirection-process-substitution--heredocs)
  - [3.6 Signal Trapping & Graceful Cleanup (trap)](#36-signal-trapping--graceful-cleanup-trap)
  - [3.7 Text Surgery Trio: grep, sed, awk, xargs & jq](#37-text-surgery-trio-grep-sed-awk-xargs--jq)
  - [3.8 Production Bash Blueprint: Robust Service Health Monitor with Backoff](#38-production-bash-blueprint-robust-service-health-monitor-with-backoff)
- [4. Track B: Windows Batch Scripting (.bat / .cmd)](#4-track-b-windows-batch-scripting-bat--cmd)
  - [4.1 cmd.exe Engine & @echo off](#41-cmdexe-engine--echo-off)
  - [4.2 Delayed Variable Expansion (!VAR! vs %VAR%)](#42-delayed-variable-expansion-var-vs-var)
  - [4.3 Command-Line Arguments & Directory Resolution (%~dp0)](#43-command-line-arguments--directory-resolution-dp0)
  - [4.4 Conditionals & ERRORLEVEL Branching](#44-conditionals--errorlevel-branching)
  - [4.5 Powerful Loops: for, for /r, for /f CSV Parsing](#45-powerful-loops-for-for-r-for-f-csv-parsing)
  - [4.6 Subroutines, Functions & Local Variable Isolation](#46-subroutines-functions--local-variable-isolation)
  - [4.7 Production Batch Blueprint: Automated Backup & Service Maintenance](#47-production-batch-blueprint-automated-backup--service-maintenance)
- [5. Track C: PowerShell 7+ & Windows PowerShell Mastery](#5-track-c-powershell-7--windows-powershell-mastery)
  - [5.1 The .NET Object Pipeline & Cmdlet Architecture](#51-the-net-object-pipeline--cmdlet-architecture)
  - [5.2 Collections, HashTables & PSCustomObject](#52-collections-hashtables--pscustomobject)
  - [5.3 Advanced Scripting: [CmdletBinding()], Param & SupportsShouldProcess](#53-advanced-scripting-cmdletbinding-param--supportsshouldprocess)
  - [5.4 Bulletproof Error Handling: $ErrorActionPreference, try/catch/finally](#54-bulletproof-error-handling-erroractionpreference-trycatchfinally)
  - [5.5 REST APIs, JSON Parsing & System Monitoring (Invoke-RestMethod)](#55-rest-apis-json-parsing--system-monitoring-invoke-restmethod)
  - [5.6 Remote Management & Security (WinRM, SSH, Execution Policies)](#56-remote-management--security-winrm-ssh-execution-policies)
  - [5.7 Production PowerShell Blueprint: Multi-Target System Health & Security Auditor](#57-production-powershell-blueprint-multi-target-system-health--security-auditor)
- [6. Production War Room Incidents & RCAs](#6-production-war-room-incidents--rcas)
  - [Incident 1: The rm -rf $TARGET/ Unset Variable Disaster (Bash)](#incident-1-the-rm--rf-target-unset-variable-disaster-bash)
  - [Incident 2: The Batch Delayed Expansion Loop Overwrite Catastrophe (CMD)](#incident-2-the-batch-delayed-expansion-loop-overwrite-catastrophe-cmd)
  - [Incident 3: The Silent Error Continuation Cascade in Database Migrations (PowerShell)](#incident-3-the-silent-error-continuation-cascade-in-database-migrations-powershell)
- [7. Senior DevOps, SRE & Sysadmin Interview Bank (35 Questions)](#7-senior-devops-sre--sysadmin-interview-bank-35-questions)

---

# 1. The Fundamental Mental Models & Architecture

Every systems administrator and cloud engineer must navigate three distinct terminal environments across Linux, macOS, and Windows. Understanding their underlying design philosophies is crucial to writing robust, portable, and bug-free automation.

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                     THE THREE TERMINAL PARADIGMS COMPARED                        │
├──────────────────┬───────────────────────────┬───────────────────────────────────┤
│ Shell Environment│ Underlying Engine         │ Inter-Process Data Contract       │
├──────────────────┼───────────────────────────┼───────────────────────────────────┤
│ Bash (sh / zsh)  │ Linux/Unix C System Calls │ Unstructured Stream of ASCII/UTF8 │
│ Batch (cmd.exe)  │ Legacy MS-DOS / NT Engine │ Raw Line-by-Line Text Characters  │
│ PowerShell (Core)│ Microsoft .NET CLR Runtime│ Strongly Typed First-Class Objects│
└──────────────────┴───────────────────────────┴───────────────────────────────────┘
```

## 1.1 Text Streams vs. String Characters vs. The .NET Object Pipeline

### The Bash Mental Model: Text is Universal
Bash treats everything passed through a pipe (`|`) as a stream of raw bytes or text lines. To extract information, you run the text through specialized textual filtering filters (`grep`, `awk`, `cut`, `sed`).
- **Advantage**: Extreme composability. Works with any executable written in C, Go, Python, or Rust.
- **Disadvantage**: Fragile parsing. If a tool changes its spacing, column alignment, or date format, downstream scripts silently break.

### The Batch Mental Model: Line-Oriented Command Interpretation
`cmd.exe` reads scripts line-by-line, performs lexical macro substitution on the entire line before executing it, and relies on rudimentary control flow.
- **Advantage**: Zero dependencies. Guaranteed to run on any Windows NT system dating back to Windows 2000.
- **Disadvantage**: Cryptic syntax, primitive string handling, and dangerous variable expansion timing.

### The PowerShell Mental Model: Object Pipelines
In PowerShell, a pipe does **not** send raw text. It sends rich .NET objects with properties and methods:
```powershell
# In PowerShell: No text parsing needed!
Get-Service | Where-Object Status -eq 'Running' | Select-Object -Property Name, StartType
```
Here, `Get-Service` outputs an array of `System.ServiceProcess.ServiceController` objects. The next cmdlet accesses properties (`$_.Status`) directly without regex or column counting.

---

## 1.2 Process Architecture, Subshells & Variable Inheritance

```
            ┌──────────────────────────────────────────────┐
            │          Parent Shell Process (PID: 1000)    │
            │           Exported: APP_ENV="production"     │
            │           Local:    TEMP_COUNTER=42          │
            └──────────────────────┬───────────────────────┘
                                   │
                     Forks Child Process / Subshell
                                   │
                                   ▼
            ┌──────────────────────────────────────────────┐
            │           Child Process (PID: 1001)          │
            │           Inherits: APP_ENV="production"     │
            │           CANNOT see: TEMP_COUNTER           │
            │  Modifying APP_ENV DOES NOT affect Parent!   │
            └──────────────────────────────────────────────┘
```

- **In Bash**:
  - Regular variables (`VAR="val"`) are private to the current process.
  - To pass a variable to child processes, you must `export VAR="val"`.
  - Parent processes **never** see variable modifications made inside child processes or subshells `( ... )` or commands piped into `while read`.
- **In Batch**:
  - By default, all variables in a `.bat` file are global to the entire command prompt session!
  - You must use `setlocal` and `endlocal` to isolate variable scope.
- **In PowerShell**:
  - PowerShell uses explicit scoping: `$global:VAR`, `$script:VAR`, `$env:VAR` (environment), and local `$VAR`.

---

## 1.3 Standard Streams & Exit Codes ($?, %ERRORLEVEL%, $LASTEXITCODE)

Every operating system process communicates execution status through standard file descriptors and return codes:

```
                  ┌──────────────────────┐
   [0: STDIN]  ──►│                      │──► [1: STDOUT] (Normal Output)
                  │   Running Process    │
                  │                      │──► [2: STDERR] (Error Messages)
                  └──────────┬───────────┘
                             │
                      Exit Code Return
                     (0 = Success, >0 = Error)
```

| Metric / Stream | Bash (Linux/macOS) | Windows Batch (`cmd.exe`) | PowerShell |
| :--- | :--- | :--- | :--- |
| **Standard In (0)** | `< input.txt` | `< input.txt` | `Get-Content \| ...` |
| **Standard Out (1)**| `> out.txt` or `1> out.txt` | `> out.txt` | `> out.txt` or `Out-File` |
| **Standard Err (2)**| `2> err.txt` | `2> err.txt` | `2> err.txt` |
| **Merge Err to Out**| `> file.txt 2>&1` or `&> file.txt` | `> file.txt 2>&1` | `*> file.txt` (All streams) |
| **Null Device (Drop)** | `> /dev/null 2>&1` | `> nul 2>&1` | `> $null` or `Out-Null` |
| **Exit Code Variable** | **`$?`** | **`%ERRORLEVEL%`** | **`$LASTEXITCODE`** (Native apps) / **`$?`** (True/False) |
| **Success Convention** | `0` = Success, non-zero = Fail | `0` = Success, non-zero = Fail | `0` / `$true` = Success |

---

# 2. The Cross-Platform Rosetta Stone (Bash vs. Batch vs. PowerShell)

This master translation table maps common operational commands across all three environments:

| Administrative Task | Linux Bash | Windows Batch (`.bat`) | PowerShell (7+ / Windows) |
| :--- | :--- | :--- | :--- |
| **Print Message** | `echo "Hello World"` | `echo Hello World` | `Write-Host "Hello World"` |
| **Current Working Dir** | `pwd` | `cd` | `Get-Location` (or `pwd`) |
| **List Files & Folders** | `ls -la` | `dir /a` | `Get-ChildItem -Force` (or `dir` / `ls`) |
| **Change Directory** | `cd /var/log` | `cd /d C:\Windows` | `Set-Location C:\Windows` |
| **Create Directory** | `mkdir -p /opt/app/logs` | `mkdir C:\app\logs` | `New-Item -ItemType Directory -Path C:\app\logs -Force` |
| **Remove File** | `rm -f file.txt` | `del /f /q file.txt` | `Remove-Item -Path file.txt -Force` |
| **Remove Directory Tree** | `rm -rf /opt/app` | `rmdir /s /q C:\app` | `Remove-Item -Path C:\app -Recurse -Force` |
| **Copy File / Directory** | `cp -r src/ dst/` | `xcopy /s /e /y src dst\` | `Copy-Item -Path src -Destination dst -Recurse -Force` |
| **Move / Rename** | `mv old.txt new.txt` | `move /y old.txt new.txt` | `Move-Item -Path old.txt -Destination new.txt -Force` |
| **View File Content** | `cat file.txt` | `type file.txt` | `Get-Content file.txt` |
| **Follow Log in Realtime** | `tail -f /var/log/syslog` | *(Requires PowerShell or tail.exe)* | `Get-Content app.log -Wait -Tail 20` |
| **Set Local Variable** | `NAME="Alice"` | `set "NAME=Alice"` | `$Name = "Alice"` |
| **Access Variable** | `echo "$NAME"` | `echo %NAME%` (or `!NAME!`) | `Write-Host "$Name"` |
| **Set Environment Variable**| `export ENV_NAME="prod"` | `setx ENV_NAME "prod"` (machine) | `$env:ENV_NAME = "prod"` |
| **Read Environment Variable**| `echo "$PATH"` | `echo %PATH%` | `Write-Host $env:PATH` |
| **String Search (grep)** | `grep -rn "ERROR" /var/log` | `findstr /s /i "ERROR" *.*` | `Select-String -Path *.* -Pattern "ERROR"` |
| **Find Files by Name** | `find . -name "*.log"` | `dir /s /b *.log` | `Get-ChildItem -Filter *.log -Recurse` |
| **List Running Processes** | `ps aux` | `tasklist` | `Get-Process` |
| **Kill Process by Name** | `pkill -9 nginx` | `taskkill /f /im nginx.exe` | `Stop-Process -Name nginx -Force` |
| **Kill Process by PID** | `kill -9 1234` | `taskkill /f /pid 1234` | `Stop-Process -Id 1234 -Force` |
| **HTTP GET Request** | `curl -s https://api.ipify.org`| `curl.exe -s https://api.ipify.org` | `(Invoke-RestMethod https://api.ipify.org)` |
| **Check Network Port** | `ss -tlpn` or `netstat -tlpn` | `netstat -ano \| findstr 8080` | `Test-NetConnection -ComputerName localhost -Port 8080` |
| **Conditional: If file exists**| `if [[ -f "$FILE" ]]; then` | `if exist "%FILE%" (` | `if (Test-Path $File) {` |
| **Conditional: String equals**| `if [[ "$A" == "$B" ]]; then` | `if "%A%"=="%B%" (` | `if ($A -eq $B) {` |
| **Loop Over Files** | `for f in *.txt; do ... done` | `for %%f in (*.txt) do (...)` | `Get-ChildItem *.txt \| ForEach-Object { ... }` |
| **Chained Execution (AND)** | `cmd1 && cmd2` | `cmd1 && cmd2` | `cmd1; if ($?) { cmd2 }` (or `cmd1 && cmd2` in PS7) |
| **Fallback Execution (OR)** | `cmd1 \|\| cmd2` | `cmd1 \|\| cmd2` | `cmd1; if (-not $?) { cmd2 }` (or `cmd1 \|\| cmd2` in PS7)|

---

# 3. Track A: Bash Shell Scripting & Linux Terminal Mastery

Bash is the lingua franca of Linux servers, Kubernetes containers, and cloud infrastructure pipelines.

## 3.1 The Unforgiving Production Shebang & Strict Mode

Never write a production shell script that begins with just `#!/bin/bash`. If a command fails or an environment variable is unset, standard Bash will blindly continue executing, often destroying files.

### The "Unofficial Bash Strict Mode"
```bash
#!/usr/bin/env bash
# ==============================================================================
# SCRIPT: deploy.sh
# AUTHOR: SRE Infrastructure Team
# ==============================================================================

# Stop script immediately on any command error
set -o errexit     # Same as: set -e

# Treat unset/unbound variables as a fatal error
set -o nounset     # Same as: set -u

# Ensure a pipeline returns the exit code of the LAST FAILING command, not the last command
set -o pipefail

# Set Internal Field Separator to newline and tab only (prevents word splitting on spaces)
IFS=$'\n\t'
```

### Why `pipefail` is Critical:
```bash
# WITHOUT pipefail:
grep "CRITICAL" /nonexistent/file.log | awk '{print $1}'
echo $?  # OUTPUT: 0! Because awk succeeded, masking grep's catastrophic exit code 2!

# WITH set -o pipefail:
echo $?  # OUTPUT: 2! The pipeline properly registers grep's failure.
```

---

## 3.2 Variables, Arrays & Advanced Parameter Expansion

### Variable Assignment & Quoting Rules
```bash
SERVER_NAME="prod-api-01"
PORT=8080

# ALWAYS quote variable expansions to prevent globbing and word splitting!
echo "Connecting to ${SERVER_NAME} on port ${PORT}..."
```

### Advanced Parameter Expansion (No External Tools Needed!)
Bash parameter expansion runs inside the shell engine with zero subshell overhead:

| Expansion Syntax | Meaning | Example (`FILE="app.bundle.tar.gz"`) | Output |
| :--- | :--- | :--- | :--- |
| `${VAR:-default}` | If `VAR` unset or null, return `default` | `${DEPLOY_ENV:-staging}` | `staging` (if unset) |
| `${VAR:=default}` | If unset, set `VAR` to `default` and return it | `${TIMEOUT:=30}` | Sets `TIMEOUT=30` |
| `${VAR:?error}` | If unset, abort script and print `error` | `${DB_PASSWORD:?Password required!}`| Aborts if unset |
| `${#VAR}` | Length of string | `${#SERVER_NAME}` | `11` |
| `${VAR#pattern}` | Remove shortest match from start | `${FILE#*.}` | `bundle.tar.gz` |
| `${VAR##pattern}` | Remove longest match from start | `${FILE##*.}` | `gz` (Extract extension) |
| `${VAR%pattern}` | Remove shortest match from end | `${FILE%.*}` | `app.bundle.tar` |
| `${VAR%%pattern}` | Remove longest match from end | `${FILE%%.*}` | `app` (Extract base name) |
| `${VAR/find/replace}`| Replace first occurrence | `${SERVER_NAME/prod/stage}` | `stage-api-01` |
| `${VAR//find/replace}`| Replace all occurrences | `${SERVER_NAME//-/._}` | `prod._api._01` |
| `${VAR^^}` | Convert to UPPERCASE (Bash 4+) | `${SERVER_NAME^^}` | `PROD-API-01` |
| `${VAR,,}` | Convert to lowercase (Bash 4+) | `STR="DEV"; echo "${STR,,}"`| `dev` |

### Indexed and Associative Arrays
```bash
# Indexed Array
SERVICES=("nginx" "postgres" "redis")
echo "${SERVICES[0]}"      # "nginx"
echo "${SERVICES[@]}"      # All elements: "nginx postgres redis"
echo "${#SERVICES[@]}"     # Array length: 3
SERVICES+=("rabbitmq")     # Append element

# Loop over indexed array
for svc in "${SERVICES[@]}"; do
    echo "Restarting service: ${svc}"
done

# Associative Array (Hash Map - Requires Bash 4+)
declare -A PORT_MAP
PORT_MAP["http"]=80
PORT_MAP["https"]=443
PORT_MAP["postgres"]=5432

echo "Postgres port is: ${PORT_MAP["postgres"]}"
```

---

## 3.3 Conditionals (`[[ ... ]]`), Pattern Matching & Arithmetic

### Always Prefer `[[ ... ]]` over `[ ... ]`
`[[ ... ]]` is a Bash keyword that handles empty strings safely, supports regex matching (`=~`), and prevents accidental word splitting.

```bash
# Numeric Comparisons: -eq, -ne, -lt, -le, -gt, -ge
COUNT=10
if [[ "${COUNT}" -ge 5 ]]; then
    echo "Count is high enough."
fi

# String Comparisons: ==, !=, -z (is empty), -n (is not empty)
if [[ -z "${API_KEY:-}" ]]; then
    echo "Error: API_KEY is empty!" >&2
    exit 1
fi

# Regex Matching with =~
EMAIL="ops-lead@company.internal"
if [[ "${EMAIL}" =~ ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$ ]]; then
    echo "Valid email address format."
fi

# File Checks: -f (file), -d (dir), -e (exists), -s (non-empty), -r (readable), -w (writable)
if [[ -f "/etc/nginx/nginx.conf" && -r "/etc/nginx/nginx.conf" ]]; then
    echo "Nginx config exists and is readable."
fi

# Arithmetic Evaluations using (( ... ))
TOTAL=$(( 5 + 10 * 2 ))      # TOTAL=25
(( COUNT++ ))                # Increment COUNT in-place
if (( COUNT > 10 )); then
    echo "Count exceeds threshold."
fi
```

---

## 3.4 Loops & High-Throughput Stream Reading

### The Bulletproof `while read` Loop
Never parse lines with `for line in $(cat file.txt)`. It breaks on spaces and chokes on huge files.

```bash
# Read a file line-by-line safely
CONFIG_FILE="/etc/hosts"

while IFS= read -r line || [[ -n "${line}" ]]; do
    # Skip empty lines and comments
    [[ -z "${line}" || "${line}" =~ ^[[:space:]]*# ]] && continue
    echo "Active host entry: ${line}"
done < "${CONFIG_FILE}"
```

---

## 3.5 File Descriptors, Redirection, Process Substitution & HereDocs

### Deep File Descriptor Architecture
```bash
# Discard stdout, keep stderr visible
command > /dev/null

# Discard both stdout and stderr completely
command > /dev/null 2>&1
# Or in modern Bash:
command &> /dev/null

# Redirect stderr to a log file, stdout to console
command 2> error.log

# Custom File Descriptor (FD 3 for audit logging)
exec 3> /var/log/script_audit.log
echo "Audit checkpoint reached at $(date)" >&3
exec 3>&-  # Close FD 3
```

### Process Substitution `<( ... )`
Avoid creating temporary files on disk when comparing outputs of two commands:
```bash
# Diff active running processes between two remote servers
diff -u <(ssh server1 "ps aux | awk '{print \$11}' | sort") \
        <(ssh server2 "ps aux | awk '{print \$11}' | sort")
```

### HereDoc with Variable Expansion vs Literal HereDoc
```bash
# Expand variables inside HereDoc:
cat << EOF > /etc/motd
Welcome to ${HOSTNAME}!
Deployed on: $(date)
EOF

# LITERAL HereDoc (Quotes around delimiter prevent ANY variable expansion):
cat << 'EOF' > /tmp/installer.sh
#!/usr/bin/env bash
# $USER will NOT be evaluated now; it will stay as literal text!
echo "Current user is: ${USER}"
EOF
```

---

## 3.6 Signal Trapping & Graceful Cleanup (`trap`)

In production automation, scripts often generate lock files (`/tmp/deploy.lock`) or temporary staging directories. If someone presses `Ctrl + C` (SIGINT) or the process is terminated (SIGTERM), the cleanup must run.

```bash
#!/usr/bin/env bash
set -euo pipefail

LOCK_FILE="/tmp/nightly_sync.lock"
TEMP_DIR=$(mktemp -d /tmp/sync_job.XXXXXX)

cleanup() {
    local exit_code=$?
    echo "[CLEANUP] Cleaning temp files and releasing locks..."
    rm -rf "${TEMP_DIR}"
    rm -f "${LOCK_FILE}"
    echo "[CLEANUP] Done. Exiting with code: ${exit_code}"
    exit "${exit_code}"
}

# Trap EXIT (runs on normal exit, error exit, and signals)
trap cleanup EXIT
trap 'echo "Terminated by user!"; exit 130' INT
trap 'echo "Killed by system!"; exit 143' TERM

# Acquire Lock
if [[ -f "${LOCK_FILE}" ]]; then
    echo "Another instance is already running! PID: $(cat "${LOCK_FILE}")" >&2
    exit 1
fi
echo "$$" > "${LOCK_FILE}"

echo "Working inside ${TEMP_DIR}..."
sleep 2
```

---

## 3.7 Text Surgery Trio: `grep`, `sed`, `awk`, `xargs` & `jq`

```bash
# 1. GREP: Fast filtering with context
grep -rnE "NullPointer|OutOfMemory" /var/log/app/ --color=auto

# 2. SED: Stream Editor (In-place replacement)
# Replace 'staging.db' with 'prod.db' in config.json
sed -i.bak 's/staging\.db/prod\.db/g' /opt/app/config.json

# 3. AWK: Column-oriented text programming
# Print username and shell for users with UID >= 1000 from /etc/passwd
awk -F: '$3 >= 1000 { printf "User: %-15s Shell: %s\n", $1, $7 }' /etc/passwd

# 4. XARGS: Parallel execution
# Delete 10,000 old log files safely without "argument list too long" error
find /var/log/archive -name "*.log.gz" -mtime +30 -print0 | xargs -0 -P 4 rm -f

# 5. JQ: Surgical JSON manipulation
# Extract all IP addresses from AWS EC2 describe-instances JSON payload
curl -s http://api.internal/servers | jq -r '.servers[] | select(.status == "RUNNING") | .ip'
```

---

## 3.8 Production Bash Blueprint: Robust Service Health Monitor with Backoff

```bash
#!/usr/bin/env bash
# ==============================================================================
# SCRIPT: check_service.sh
# PURPOSE: Production HTTP Health Poller with Exponential Backoff & Slack Alert
# ==============================================================================
set -euo pipefail
IFS=$'\n\t'

readonly TARGET_URL="${1:-https://api.company.internal/health}"
readonly MAX_RETRIES=5
readonly INITIAL_DELAY=2
readonly TIMEOUT_SECONDS=5

log() {
    local level="$1"
    shift
    printf "[%s] [%-5s] %s\n" "$(date -u +'%Y-%m-%dT%H:%M:%SZ')" "${level}" "$*"
}

poll_endpoint() {
    local url="$1"
    local attempt=1
    local delay="${INITIAL_DELAY}"

    while (( attempt <= MAX_RETRIES )); do
        log "INFO" "Probing ${url} (Attempt ${attempt}/${MAX_RETRIES})..."
        
        # -s: Silent, -o /dev/null: Drop body, -w: Format HTTP status code
        http_code=$(curl -s -o /dev/null -w "%{http_code}" --max-time "${TIMEOUT_SECONDS}" "${url}" || echo "000")

        if [[ "${http_code}" == "200" ]]; then
            log "INFO" "Endpoint is healthy! HTTP ${http_code}"
            return 0
        fi

        log "WARN" "Probe failed with HTTP status: ${http_code}. Retrying in ${delay}s..."
        sleep "${delay}"
        
        # Exponential backoff: delay = delay * 2
        delay=$(( delay * 2 ))
        (( attempt++ ))
    done

    log "ERROR" "CRITICAL: Endpoint ${url} failed after ${MAX_RETRIES} attempts!"
    return 1
}

main() {
    log "INFO" "Starting Health Prober for: ${TARGET_URL}"
    if ! poll_endpoint "${TARGET_URL}"; then
        # In a real environment, trigger PagerDuty / Slack webhook
        exit 1
    fi
    log "INFO" "All health checks passed successfully."
}

main "$@"
```

---

# 4. Track B: Windows Batch Scripting (`.bat` / `.cmd`)

Despite the dominance of PowerShell, Windows Command Prompt (`cmd.exe`) batch files remain foundational across Windows Server boot scripts, legacy enterprise CI runners, and fast local automation where launching the PowerShell runtime introduces latency.

## 4.1 cmd.exe Engine & `@echo off`

By default, `cmd.exe` echoes every command to the terminal before executing it.
- **`@` prefix**: Suppresses echoing for that single command line.
- **`@echo off`**: Suppresses echoing for all subsequent commands in the script.

```bat
@echo off
rem This is a comment in Batch
:: This double-colon syntax is also commonly used as a comment
echo Initializing Enterprise Windows Backup Task...
```

---

## 4.2 Delayed Variable Expansion (`!VAR!` vs `%VAR%`)

> [!CAUTION]
> **The #1 Bug in Windows Batch Scripting: Early Parse-Time Expansion**
> `cmd.exe` reads an entire block of code (e.g., inside parentheses `( ... )` of an `if` or `for` loop) **all at once** before running any command inside it!
> Any variable enclosed in `%VAR%` is replaced with its value **before the loop starts**.

### The Bug Demonstrated:
```bat
@echo off
set COUNT=0
for /l %%i in (1,1,3) do (
    set /a COUNT+=1
    echo In Loop: %COUNT%
)
echo Final: %COUNT%

rem OUTPUT:
rem In Loop: 0
rem In Loop: 0
rem In Loop: 0
rem Final: 3
```
*Why did it print 0? Because `%COUNT%` was evaluated at line parse time when COUNT was 0!*

### The Fix: `setlocal enabledelayedexpansion` and `!VAR!`
```bat
@echo off
setlocal enabledelayedexpansion

set COUNT=0
for /l %%i in (1,1,3) do (
    set /a COUNT+=1
    echo In Loop: !COUNT!
)
echo Final: !COUNT!

rem OUTPUT:
rem In Loop: 1
rem In Loop: 2
rem In Loop: 3
rem Final: 3
```

---

## 4.3 Command-Line Arguments & Directory Resolution (`%~dp0`)

When a batch script is invoked, positional parameters `%1` through `%9` contain arguments:

| Modifier Syntax | Meaning & Resolution |
| :--- | :--- |
| `%0` | The full invocation path of the batch script itself |
| `%~dp0` | **The exact directory where the batch script lives** (with trailing `\`) |
| `%~nx0` | The filename and extension of the batch script (e.g., `backup.bat`) |
| `%~f1` | Resolves argument `%1` to a fully qualified absolute path |
| `%~s1` | Resolves argument `%1` to legacy 8.3 short filename (no spaces) |
| `%*` | All command-line arguments combined into a single string |

### Directory Self-Resolution (Bulletproof Pathing):
```bat
@echo off
rem Never assume the user is running the script from its own folder!
set "SCRIPT_DIR=%~dp0"
echo Script is executing from: %SCRIPT_DIR%

rem Safely reference adjacent config file
set "CONFIG_FILE=%SCRIPT_DIR%config\app.properties"
if not exist "%CONFIG_FILE%" (
    echo Error: Config file missing at %CONFIG_FILE%
    exit /b 1
)
```

---

## 4.4 Conditionals & ERRORLEVEL Branching

```bat
@echo off
set "ENVIRONMENT=%1"

if "%ENVIRONMENT%"=="" (
    echo Error: Environment argument required [dev^|prod]
    exit /b 1
)

if /i "%ENVIRONMENT%"=="prod" (
    echo Running with Production safeguards.
) else (
    echo Running with Non-Prod safeguards.
)

rem Testing command execution success using ERRORLEVEL
ping -n 1 127.0.0.1 > nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Network ping failed!
    exit /b %ERRORLEVEL%
)
```

---

## 4.5 Powerful Loops: `for`, `for /r`, `for /f` CSV Parsing

Batch `for` loops are unexpectedly powerful:

### 1. Loop Over Files (`for`)
```bat
@echo off
for %%F in (C:\logs\*.log) do (
    echo Archiving: %%~nxF (Size: %%~zF bytes)
)
```

### 2. Recursive Search (`for /r`)
```bat
@echo off
for /r "C:\projects" %%F in (*.tmp) do (
    del /f /q "%%F"
)
```

### 3. Parsing Command Output & Text Files (`for /f`)
```bat
@echo off
rem Parse CSV: tokens 1 and 3 separated by comma
for /f "tokens=1,3 delims=," %%A in (C:\data\servers.csv) do (
    echo Host: %%A - IP: %%B
)

rem Parse output of another command (enclosed in single quotes)
for /f "tokens=*" %%I in ('hostname') do (
    set "LOCAL_HOST=%%I"
)
echo Captured host: %LOCAL_HOST%
```

---

## 4.6 Subroutines, Functions & Local Variable Isolation

Batch allows subroutines using `:Label` and `call :Label`:

```bat
@echo off
setlocal

set "GLOBAL_COUNTER=100"

call :CalculateSum 25 75 RESULT
echo Returned sum is: %RESULT%

rem Must exit script to prevent falling through into the function below!
exit /b 0

:: -----------------------------------------------------------------------------
:: FUNCTION: CalculateSum(Num1, Num2, OutputVar)
:: -----------------------------------------------------------------------------
:CalculateSum
setlocal
set /a _sum=%1 + %2

rem Pass the result back across the endlocal barrier
endlocal & set "%3=%_sum%"
goto :eof
```

---

## 4.7 Production Batch Blueprint: Automated Backup & Service Maintenance

```bat
@echo off
setlocal enabledelayedexpansion

:: =============================================================================
:: SCRIPT: maintenance_backup.bat
:: PURPOSE: Stops Windows Service, Archives Data Directory, Restarts Service
:: =============================================================================

set "SERVICE_NAME=W32Time"
set "SOURCE_DIR=C:\App\Data"
set "BACKUP_ROOT=C:\Backups"

:: Generate Timestamp formatted as YYYY-MM-DD_HHMMSS
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set "DT=%%I"
set "TIMESTAMP=!DT:~0,4!-!DT:~4,2!-!DT:~6,2!_!DT:~8,2!!DT:~10,2!!DT:~12,2!"
set "TARGET_DIR=%BACKUP_ROOT%\backup_%TIMESTAMP%"

echo [%TIMESTAMP%] Starting Automated Maintenance Pipeline...

:: Verify Admin Privileges
net session > nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] This script requires Elevated Administrator privileges!
    exit /b 1
)

:: Step 1: Stop Service
echo [STEP 1] Stopping %SERVICE_NAME%...
net stop "%SERVICE_NAME%" > nul 2>&1

:: Step 2: Create Backup
echo [STEP 2] Creating backup directory: %TARGET_DIR%
mkdir "%TARGET_DIR%" > nul 2>&1

robocopy "%SOURCE_DIR%" "%TARGET_DIR%" /MIR /R:2 /W:3 /NP /LOG+:"%BACKUP_ROOT%\backup.log"
if %ERRORLEVEL% GEQ 8 (
    echo [ERROR] Robocopy encountered fatal errors during copy!
    goto :RESTART
)
echo [STEP 2] Files backed up successfully.

:RESTART
:: Step 3: Always Restart Service
echo [STEP 3] Restarting %SERVICE_NAME%...
net start "%SERVICE_NAME%" > nul 2>&1

echo [COMPLETE] Maintenance process finished.
exit /b 0
```

---

# 5. Track C: PowerShell 7+ & Windows PowerShell Mastery

PowerShell is built directly on Microsoft's .NET Common Language Runtime (CLR). It exists in two flavors:
1. **Windows PowerShell (5.1)**: Preinstalled on Windows, tightly coupled to the full .NET Framework.
2. **PowerShell 7+ (PowerShell Core)**: Cross-platform (Windows, Linux, macOS), open-source, built on modern .NET 8/9.

> [!TIP]
> **Looking for the Dedicated Deep-Dive?**
> For an exhaustive, standalone guide covering parallel runspaces, Pester 5 BDD testing, CIM/WMI diagnostics, JEA, WinRM/SSH remoting, and 50 senior interview scenarios, consult the dedicated [⚡ PowerShell 7+ & Enterprise Systems Automation Master Guide](powershell_master_guide.md).

## 5.1 The .NET Object Pipeline & Cmdlet Architecture

Cmdlets adhere strictly to an approved **`Verb-Noun`** naming standard (e.g., `Get-Process`, `Set-Content`, `Invoke-WebRequest`).

```powershell
# Inspecting an object's properties and methods:
Get-Process -Name chrome | Get-Member

# Filtering and Selecting:
Get-Process | 
    Where-Object { $_.CPU -gt 50 -and $_.WorkingSet64 -gt 500MB } | 
    Select-Object -Property Id, ProcessName, @{Name="MemoryMB"; Expression={[math]::Round($_.WorkingSet64 / 1MB, 2)}} | 
    Sort-Object -Property MemoryMB -Descending
```

---

## 5.2 Collections, HashTables & PSCustomObject

```powershell
# 1. Array
$Servers = @("web-01", "web-02", "db-01")
$Servers += "redis-01"

# 2. HashTable (Dictionary)
$PortConfig = @{
    HTTP     = 80
    HTTPS    = 443
    Database = 5432
}
Write-Host "Database port: $($PortConfig['Database'])"

# 3. PSCustomObject (First-Class Structured Data)
$ClusterNode = [PSCustomObject]@{
    NodeName   = "k8s-worker-01"
    IPAddress  = "10.0.1.15"
    Roles      = @("worker", "ingress")
    CPUCores   = 16
    MemoryGB   = 64
    IsActive   = $true
}

# Export directly to JSON or CSV!
$ClusterNode | ConvertTo-Json -Depth 3
```

---

## 5.3 Advanced Scripting: `[CmdletBinding()]`, `Param` & `SupportsShouldProcess`

Turning a simple script into an enterprise-grade PowerShell tool with built-in `-Verbose`, `-Debug`, `-WhatIf`, and parameter validation:

```powershell
function Restart-ClusterNode {
    [CmdletBinding(SupportsShouldProcess = $true, ConfirmImpact = 'High')]
    param (
        [Parameter(Mandatory = $true, Position = 0, ValueFromPipeline = $true)]
        [ValidateNotNullOrEmpty()]
        [string]$ComputerName,

        [Parameter(Mandatory = $false)]
        [ValidateRange(1, 60)]
        [int]$TimeoutSeconds = 30,

        [Parameter()]
        [switch]$Force
    )

    process {
        if ($PSCmdlet.ShouldProcess($ComputerName, "Rebooting Cluster Node and draining workloads")) {
            Write-Verbose "Initiating reboot on node: $ComputerName with timeout $TimeoutSeconds s"
            if ($Force) {
                Write-Warning "Force parameter detected. Bypassing node drain!"
            }
            # Execution logic here...
            Write-Host "Node $ComputerName reboot command dispatched." -ForegroundColor Green
        }
    }
}
```

---

## 5.4 Bulletproof Error Handling: `$ErrorActionPreference`, `try/catch/finally`

> [!IMPORTANT]
> **PowerShell Non-Terminating Errors vs Terminating Errors**
> By default, standard cmdlets that encounter errors (like `Get-Item missing.txt`) emit a **Non-Terminating Error**. They write red text to the console, **but the script keeps running!**
> A `try/catch` block will NOT intercept non-terminating errors unless you tell PowerShell to treat them as terminating.

### The Production Strict Setting:
```powershell
# Force all cmdlet errors to throw terminating exceptions
$ErrorActionPreference = 'Stop'

try {
    Write-Host "Attempting file download..."
    $Response = Invoke-RestMethod -Uri "https://api.internal/v1/config" -TimeoutSec 5
    $Response.Content | Set-Content -Path "C:\app\config.json"
}
catch [System.Net.WebException] {
    Write-Error "Network connection failed: $($_.Exception.Message)"
    exit 1
}
catch {
    Write-Error "Unexpected fatal error: $($_.Exception.GetType().FullName)"
    Write-Error "Details: $($_.Exception.Message)"
    exit 2
}
finally {
    Write-Host "Releasing resources and ending session."
}
```

---

## 5.5 REST APIs, JSON Parsing & System Monitoring (`Invoke-RestMethod`)

`Invoke-RestMethod` automatically deserializes JSON responses into native PowerShell custom objects:

```powershell
$Headers = @{
    "Authorization" = "Bearer eyJhbGciOi..."
    "Content-Type"  = "application/json"
}

# Sending a JSON POST Request
$Payload = @{
    clusterId = "prod-us-east-1"
    desiredReplicas = 5
} | ConvertTo-Json

$ApiResponse = Invoke-RestMethod `
    -Uri "https://orchestrator.internal/api/scale" `
    -Method Post `
    -Headers $Headers `
    -Body $Payload

Write-Host "Operation ID: $($ApiResponse.operationId)"
```

---

## 5.6 Remote Management & Security (WinRM, SSH, Execution Policies)

### Execution Policies (Not a Security Sandbox!)
Execution policies prevent users from accidentally double-clicking untrusted scripts:
- `Restricted`: Default on Windows clients. No scripts allowed.
- `RemoteSigned`: Scripts written locally run; scripts downloaded from internet must be signed by a trusted certificate.
- `Bypass`: Nothing is blocked; no warnings or prompts.

```powershell
# Check current policy:
Get-ExecutionPolicy -List

# Set policy for current process only (Requires NO Administrator rights):
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process -Force
```

### Remote Execution over WinRM / SSH (`Invoke-Command`)
```powershell
# Execute script block across 10 servers simultaneously in parallel
$ServerList = @("srv-01", "srv-02", "srv-03")

Invoke-Command -ComputerName $ServerList -ScriptBlock {
    Get-Service -Name Spooler | Stop-Service -PassThru
}
```

---

## 5.7 Production PowerShell Blueprint: Multi-Target System Health & Security Auditor

```powershell
<#
.SYNOPSIS
    Production System Health and Security Auditor for Windows Servers.
.DESCRIPTION
    Scans disk volumes, critical services, and pending reboot status.
    Generates a structured JSON report and enforces strict error policies.
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $false)]
    [string]$ReportPath = "C:\AuditReports\SystemAudit_$(Get-Date -Format 'yyyyMMdd_HHmmss').json",

    [Parameter(Mandatory = $false)]
    [int]$DiskThresholdPercent = 15
)

$ErrorActionPreference = 'Stop'

function Get-PendingRebootStatus {
    $CBSReboot = Test-Path "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Component Based Servicing\RebootPending"
    $WUReboot  = Test-Path "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\WindowsUpdate\Auto Update\RebootRequired"
    return ($CBSReboot -or $WUReboot)
}

function Audit-SystemHealth {
    Write-Verbose "Collecting System Operating Information..."
    $OS = Get-CimInstance -ClassName Win32_OperatingSystem
    
    Write-Verbose "Analyzing Disk Capacity..."
    $Disks = Get-CimInstance -ClassName Win32_LogicalDisk -Filter "DriveType = 3" | ForEach-Object {
        $FreePct = [math]::Round(($_.FreeSpace / $_.Size) * 100, 2)
        [PSCustomObject]@{
            DriveLetter = $_.DeviceID
            TotalSizeGB = [math]::Round($_.Size / 1GB, 2)
            FreeSpaceGB = [math]::Round($_.FreeSpace / 1GB, 2)
            FreePercent = $FreePct
            IsAlert     = ($FreePct -lt $DiskThresholdPercent)
        }
    }

    Write-Verbose "Checking Critical Infrastructure Services..."
    $CriticalServices = @("WinRM", "W32Time", "EventLog")
    $ServiceStates = Get-Service -Name $CriticalServices | Select-Object Name, Status, StartType

    $AuditReport = [PSCustomObject]@{
        ReportGeneratedUtc = (Get-Date).ToUniversalTime().ToString("o")
        HostName           = $env:COMPUTERNAME
        OperatingSystem    = $OS.Caption
        LastBootTimeUtc    = $OS.LastBootUpTime.ToUniversalTime().ToString("o")
        PendingReboot      = (Get-PendingRebootStatus)
        Disks              = $Disks
        Services           = $ServiceStates
    }

    return $AuditReport
}

try {
    $Report = Audit-SystemHealth
    
    $ReportDirectory = Split-Path -Parent $ReportPath
    if (-not (Test-Path $ReportDirectory)) {
        New-Item -ItemType Directory -Path $ReportDirectory -Force | Out-Null
    }

    $Report | ConvertTo-Json -Depth 4 | Set-Content -Path $ReportPath -Encoding utf8
    Write-Host "[SUCCESS] Audit report written to: $ReportPath" -ForegroundColor Green
}
catch {
    Write-Error "[FATAL] System Audit Failed: $($_.Exception.Message)"
    exit 1
}
```

---

# 6. Production War Room Incidents & RCAs

### Incident 1: The `rm -rf $TARGET/` Unset Variable Disaster (Bash)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ TIME: 03:15 UTC | SEVERITY: SEV-1 | OUTAGE: DATA LOSS & POD CRASH LOOP      │
│ SYSTEM: Automated Nightly Log Cleanup Pod                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│ THE CRITICAL SCRIPT LINE:                                                   │
│   rm -rf $LOG_DIR/                                                          │
│                                                                             │
│ ROOT CAUSE:                                                                 │
│ In a new Kubernetes deployment, the secret providing `LOG_DIR` was renamed  │
│ to `APP_LOG_DIRECTORY`. The script did not have `set -u` (nounset) enabled. │
│ When `$LOG_DIR` evaluated to an empty string, the command expanded to:      │
│   rm -rf /                                                                  │
│ The container immediately purged all root libraries and binary directories  │
│ until the Linux kernel suffered a kernel panic.                             │
├─────────────────────────────────────────────────────────────────────────────┤
│ REMEDIATION:                                                                │
│ 1. Always enforce `set -o nounset` (`set -u`) at the script shebang.        │
│ 2. Use parameter verification:                                              │
│    ${LOG_DIR:?Fatal error: LOG_DIR environment variable is not defined!}   │
│ 3. Never put raw slashes on bare variables without path anchors:            │
│    [[ -n "${LOG_DIR}" && -d "${LOG_DIR}" ]] && rm -rf "${LOG_DIR:?}"/*     │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### Incident 2: The Batch Delayed Expansion Loop Overwrite Catastrophe (CMD)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ TIME: 18:40 UTC | SEVERITY: SEV-2 | OUTAGE: PRODUCTION CONFIG CORRUPTION    │
│ SYSTEM: Windows Application Server Cluster Deployment Script                │
├─────────────────────────────────────────────────────────────────────────────┤
│ THE CRITICAL CODE:                                                          │
│   for %%F in (C:\Configs\*.xml) do (                                        │
│       set CURRENT_APP=%%~nF                                                 │
│       copy "%%F" "C:\Deploy\%CURRENT_APP%\app.xml"                          │
│   )                                                                         │
│                                                                             │
│ ROOT CAUSE:                                                                 │
│ Because `setlocal enabledelayedexpansion` was omitted, `%CURRENT_APP%` was  │
│ resolved once before the `for` loop started (when it was null).             │
│ Every single configuration file in the folder was sequentially copied over  │
│ the same invalid target path `C:\Deploy\\app.xml`, completely overwriting   │
│ every distinct microservice config with the last alphabetical file!        │
├─────────────────────────────────────────────────────────────────────────────┤
│ REMEDIATION:                                                                │
│ 1. Add `setlocal enabledelayedexpansion`.                                   │
│ 2. Use `!CURRENT_APP!` inside the loop body:                                │
│    copy "%%F" "C:\Deploy\!CURRENT_APP!\app.xml"                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### Incident 3: The Silent Error Continuation Cascade in Database Migrations (PowerShell)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ TIME: 09:12 UTC | SEVERITY: SEV-1 | OUTAGE: SILENT DATA INCONSISTENCY       │
│ SYSTEM: Automated CI/CD Database Migration Pipeline                         │
├─────────────────────────────────────────────────────────────────────────────┤
│ THE CRITICAL SCRIPT:                                                        │
│   Invoke-Sqlcmd -Query "ALTER TABLE orders ADD COLUMN loyalty_pts INT;"    │
│   Invoke-Sqlcmd -Query "UPDATE orders SET loyalty_pts = 0;"                 │
│                                                                             │
│ ROOT CAUSE:                                                                 │
│ The first DDL query failed due to a database metadata lock timeout.         │
│ Because default PowerShell `$ErrorActionPreference` is 'Continue', the      │
│ failure was logged as non-terminating red text, and PowerShell continued!   │
│ The second command ran, failed on missing column, and the deployment step   │
│ reported success (exit code 0), allowing broken code into production.       │
├─────────────────────────────────────────────────────────────────────────────┤
│ REMEDIATION:                                                                │
│ 1. Always declare `$ErrorActionPreference = 'Stop'` at the script entry.    │
│ 2. Wrap all database operations inside strict `try { ... } catch { exit 1 }`│
└─────────────────────────────────────────────────────────────────────────────┘
```

---

# 7. Senior DevOps, SRE & Sysadmin Interview Bank (35 Questions)

#### Q1: What is the exact difference between `set -e`, `set -u`, and `set -o pipefail` in Bash?
> **Answer**:
> - `set -e` (errexit): Causes Bash to exit immediately if any pipeline returns a non-zero exit status (with exceptions for conditionals like `if` or `while`).
> - `set -u` (nounset): Causes Bash to treat any expansion of an unset/unbound variable as an error and exit immediately.
> - `set -o pipefail`: In a pipeline (`cmd1 | cmd2 | cmd3`), Bash normally returns the exit status of `cmd3`. With `pipefail`, the pipeline returns the exit code of the last command to fail (exit with non-zero), or zero if all commands succeed.

#### Q2: In PowerShell, why does `try { Get-Item "C:\missing.txt" } catch { "Caught" }` NOT catch the error?
> **Answer**: `Get-Item` generates a **Non-Terminating Error**. In PowerShell, `try/catch` only catches **Terminating Errors**. To force `Get-Item` to throw a terminating exception that `try/catch` can intercept, you must either set `$ErrorActionPreference = 'Stop'` globally, or append `-ErrorAction Stop` to the cmdlet: `Get-Item "C:\missing.txt" -ErrorAction Stop`.

#### Q3: How does delayed variable expansion work in Windows Batch, and what happens if you forget to enable it?
> **Answer**: By default, `cmd.exe` parses an entire parenthesized block (such as an `if` statement or `for` loop) in one pass, expanding all `%VAR%` variables to their values *before* executing any command inside the block. If you modify a variable inside the loop, `%VAR%` will still reflect the old pre-loop value. Enabling `setlocal enabledelayedexpansion` allows the use of `!VAR!`, which tells `cmd.exe` to evaluate the variable dynamically at execution time for every iteration.

#### Q4: What is the difference between `exec > file.txt` and `> file.txt` in a Bash script?
> **Answer**: Running `command > file.txt` redirects standard output for that specific command only. Running `exec > file.txt` replaces the standard output file descriptor (FD 1) of the current shell process itself with `file.txt`. Every subsequent command in the entire script will have its standard output redirected to `file.txt` without needing manual redirection on every line.

#### Q5: What is the purpose of `%~dp0` in a Windows batch script?
> **Answer**: `%~dp0` is a special variable modifier. `%0` represents the script's invocation path. The `~d` modifier extracts the drive letter (e.g., `C:`), and `~p` extracts the directory path (e.g., `\Scripts\`). Together, `%~dp0` returns the drive and directory where the batch script resides, ensuring the script can locate adjacent files regardless of what the user's current working directory was when they invoked it.

#### Q6: How does PowerShell's object pipeline fundamentally differ from Unix pipelines?
> **Answer**: Unix pipelines stream raw byte streams (unstructured ASCII/UTF-8 text lines). Consuming programs must parse strings, count columns, and apply regular expressions. PowerShell pipelines stream strongly typed .NET objects. Downstream cmdlets receive objects with strongly typed properties and methods, eliminating the need for text parsing and making pipelines immune to whitespace or formatting changes.

#### Q7: In Bash, what is the difference between `$*` and `$@`, and why should you almost always use `"$@"`?
> **Answer**:
> - Unquoted `$*` and `$@` both split arguments on whitespace according to `IFS`.
> - `"$*"` combines all arguments into a single string separated by the first character of `IFS` (`"$1 $2 $3"`).
> - `"$@"` expands each positional argument as an individually quoted string (`"$1" "$2" "$3"`). This preserves arguments containing internal spaces (e.g., `"file with spaces.txt"`).

#### Q8: How do you capture both standard output and standard error into a single variable in Bash?
> **Answer**: Using command substitution with `2>&1`:
> ```bash
> OUTPUT=$(my_command 2>&1)
> ```

#### Q9: What is the function of `trap` in Bash, and which signals CANNOT be trapped?
> **Answer**: `trap` registers handlers (functions or commands) to be executed when the shell receives specific operating system signals (e.g., `SIGINT`, `SIGTERM`, `EXIT`). Signals **`SIGKILL` (signal 9)** and **`SIGSTOP` (signal 19)** cannot be caught, blocked, or trapped by design in POSIX operating systems; the kernel immediately terminates or freezes the process.

#### Q10: How do you run background tasks in parallel in Bash and wait for all of them to finish?
> **Answer**: Append `&` to each command to spawn it in the background, and invoke `wait`:
> ```bash
> task1 &
> task2 &
> wait
> echo "Both background tasks completed."
> ```

#### Q11: In Windows Batch, what is the difference between `exit` and `exit /b`?
> **Answer**: `exit` terminates the entire `cmd.exe` process (closing the command prompt window). `exit /b` only exits the current batch script or the current subroutine (`goto :eof`), leaving the parent command prompt session open and optionally returning an exit code: `exit /b 1`.

#### Q12: How do you verify if a command or executable exists in Bash without executing it?
> **Answer**: `command -v <toolname> > /dev/null 2>&1`. Do not use `which`, as `which` is an external binary, behaves inconsistently across Linux distributions, and does not recognize shell built-ins or aliases.

#### Q13: In PowerShell, what is the difference between `ForEach-Object` and the `foreach` statement?
> **Answer**:
> - `ForEach-Object` is a cmdlet designed for pipeline processing. It streams objects one by one with minimal memory overhead ($O(1)$ memory).
> - The `foreach ($item in $collection)` statement is a language keyword. It loads the entire collection into memory upfront before starting the loop ($O(N)$ memory), which makes it faster for CPU-bound iterations but dangerous for massive datasets.

#### Q14: What is Process Substitution in Bash, and when is it preferred over pipes?
> **Answer**: Syntax: `<(command)` or `>(command)`. The shell runs the command and connects its output to a temporary Unix named pipe or `/dev/fd/<n>`. It is preferred when a utility requires a file path argument rather than standard input (e.g., `diff -u <(cmd1) <(cmd2)`), or when you want to avoid subshell variable isolation caused by standard pipes.

#### Q15: How do you handle command-line flags (e.g., `-f`, `--verbose`) robustly in Bash?
> **Answer**: Use the POSIX built-in `getopts` for short options, or a custom `while [[ $# -gt 0 ]]` loop with a `case "$1"` statement to handle both short and long options cleanly.

#### Q16: In PowerShell, what does `[CmdletBinding()]` enable?
> **Answer**: It transforms a standard PowerShell function into an Advanced Function. It automatically adds common parameters like `-Verbose`, `-Debug`, `-ErrorAction`, `-WarningAction`, and `-OutVariable`, and integrates with PowerShell's pipeline bindings and parameter validation attributes.

#### Q17: In Batch, what does `2>&1` mean, and why must it appear at the end of the command?
> **Answer**: `2>&1` redirects File Descriptor 2 (Standard Error) to the current destination of File Descriptor 1 (Standard Output). It must appear after `> file.txt` because `cmd.exe` parses redirections from left to right; if placed first (`2>&1 > file.txt`), FD 2 is redirected to the original console stdout before stdout is redirected to the file.

#### Q18: What is the difference between single quotes `'...'` and double quotes `"..."` in Bash?
> **Answer**:
> - Single quotes preserve the literal value of every character inside; no variable expansion (`$VAR`), command substitution (`$(...)`), or escape sequences (`\n`) occur.
> - Double quotes preserve literal text but allow variable expansion (`$VAR`), command substitution (`$(...)`), arithmetic expansion (`$((...))`), and backslash escapes.

#### Q19: In PowerShell, how do you convert an array of objects directly into a CSV file?
> **Answer**: `Get-Process | Export-Csv -Path "C:\processes.csv" -NoTypeInformation -Encoding utf8`.

#### Q20: What is the Bash internal field separator (`IFS`), and what happens when it is unset?
> **Answer**: `IFS` defines the characters used for word splitting after expansion and line splitting in `read`. Default is space, tab, and newline (`$' \t\n'`). If unset, words split on any whitespace. In production scripts, setting `IFS=$'\n\t'` prevents filenames with spaces from being split into multiple tokens.

#### Q21: How do you test if a string contains a substring in PowerShell without using regex?
> **Answer**: Using the `-like` operator with wildcards: `if ($Str -like "*error*")` or using the string method: `if ($Str.Contains("error"))`.

#### Q22: In Batch, how do you perform integer arithmetic?
> **Answer**: Using the `set /a` command (e.g., `set /a RESULT=5 * 10 + 2`). Batch only supports 32-bit signed integers; floating-point math is not supported natively in `cmd.exe`.

#### Q23: How do you create an atomic lock directory in Bash to prevent duplicate script execution?
> **Answer**: Use `mkdir /var/lock/my_job.lock`. In POSIX filesystems, `mkdir` is an atomic system call (`sys_mkdir`). If the directory exists, `mkdir` fails immediately, avoiding the race conditions inherent in checking `[[ -f file ]]`.

#### Q24: What is the purpose of `$LASTEXITCODE` vs `$?` in PowerShell?
> **Answer**:
> - `$LASTEXITCODE` captures the integer return code of the last external native executable invoked (e.g., `git.exe`, `curl.exe`).
> - `$?` is a boolean (`$true`/`$false`) that indicates whether the last PowerShell operation or cmdlet executed successfully without error.

#### Q25: How do you export a variable in Bash so it is accessible in a subshell or child script?
> **Answer**: `export VAR_NAME="value"`. Without `export`, the variable is local to the current shell process and is not inherited by child processes.

#### Q26: How do you read a specific column from a delimiter-separated file in Bash without `awk`?
> **Answer**: Using `while IFS="," read -r col1 col2 col3; do ... done < file.csv`.

#### Q27: In PowerShell, how do you run a script block under another user credential?
> **Answer**: `Invoke-Command -ComputerName localhost -Credential (Get-Credential) -ScriptBlock { ... }`.

#### Q28: In Windows Batch, what does `goto :eof` do?
> **Answer**: It exits the current subroutine that was invoked via `call :Label`, returning control back to the line immediately following the `call` statement.

#### Q29: How do you measure the execution time of a command in Linux terminal?
> **Answer**: Prefix the command with `time` (e.g., `time ./deploy.sh`). It outputs `real` (wall clock), `user` (CPU time in user mode), and `sys` (CPU time in kernel mode).

#### Q30: How do you execute PowerShell commands with bypass policy from a Windows Command Prompt or shortcut?
> **Answer**: `powershell.exe -NoProfile -ExecutionPolicy Bypass -File "C:\Scripts\deploy.ps1"`.

#### Q31: In Bash, what is the difference between `hash -r` and clearing cache?
> **Answer**: Bash maintains an internal hash table of paths to previously executed binaries to avoid scanning `$PATH` every time. `hash -r` empties this table, forcing Bash to search `$PATH` again for newly installed binaries.

#### Q32: How do you suppress all output from a PowerShell cmdlet without writing to disk?
> **Answer**: Pipe to `Out-Null` or redirect to `$null`: `Get-Process | Out-Null` or `$null = Get-Process`.

#### Q33: What is the danger of using `eval` in Bash?
> **Answer**: `eval` forces the shell to parse and execute its argument as code twice. If any part of the argument contains untrusted user input, it allows arbitrary command injection and remote code execution (RCE).

#### Q34: In Batch, how do you pause script execution for 5 seconds without user interaction?
> **Answer**: `timeout /t 5 /nobreak > nul` (or the legacy trick: `ping -n 6 127.0.0.1 > nul`).

#### Q35: In PowerShell, how do you dynamically construct custom objects using the pipeline?
> **Answer**:
> ```powershell
> Get-Service | Select-Object -Property Name, Status, @{Name="UptimeHours"; Expression={(New-TimeSpan -Start $_.StartTime).TotalHours}}
> ```
