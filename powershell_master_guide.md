[🏠 Back to Home](README.md) | [🐚 Bash, Batch & PowerShell](bash_batch_powershell_master_guide.md) | [🐧 Linux Systems](linux.md) | [⚙️ Ansible Automation](ansible_master_guide.md) | [🐳 Docker Master Guide](docker_master_guide.md)

# ⚡ PowerShell 7+ & Enterprise Systems Automation Master Guide

### *(The Definitive Cloud-Native Systems Engineering Manual: The .NET CLR Object Pipeline, Advanced Toolmaking, Parallel Runspaces, WinRM & SSH Remoting, JEA, Pester 5, CIM/WMI Diagnostics & SRE War Room Incidents)*

[![PowerShell 7+](https://img.shields.io/badge/PowerShell-7.4%2B%20LTS%20%7C%20Core-5391FE.svg?style=for-the-badge&logo=powershell&logoColor=white)]()
[![Windows PowerShell](https://img.shields.io/badge/Windows%20PowerShell-5.1%20Desktop-0078D4.svg?style=for-the-badge)]()
[![Runtime](https://img.shields.io/badge/.NET-8.0%20%7C%209.0%20CLR-512BD4.svg?style=for-the-badge&logo=dotnet&logoColor=white)]()
[![Cross-Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-success.svg?style=for-the-badge)]()
[![Testing](https://img.shields.io/badge/Testing-Pester%205%20BDD-orange.svg?style=for-the-badge)]()

---

## 📑 Master Table of Contents

- [1. Executive Architecture & Core Mental Models](#1-executive-architecture--core-mental-models)
  - [1.1 The .NET Object Pipeline vs. Unix Text Streams](#11-the-net-object-pipeline-vs-unix-text-streams)
  - [1.2 PowerShell Engine Internals: Parser, AST, DLR & Execution Engine](#12-powershell-engine-internals-parser-ast-dlr--execution-engine)
  - [1.3 Windows PowerShell 5.1 vs. PowerShell 7+ (Core) Coexistence](#13-windows-powershell-51-vs-powershell-7-core-coexistence)
  - [1.4 Execution Policies: Safety Railings vs. Security Boundaries](#14-execution-policies-safety-railings-vs-security-boundaries)
  - [1.5 Variable Scoping Architecture: Global, Script, Local, Private & Using](#15-variable-scoping-architecture-global-script-local-private--using)
- [2. The Master PowerShell Feature & Cmdlet Catalog](#2-the-master-powershell-feature--cmdlet-catalog)
  - [2.1 Object Pipeline & Streaming Semantics (begin, process, end, clean)](#21-object-pipeline--streaming-semantics-begin-process-end-clean)
  - [2.2 Data Structures & Type Accelerators ([PSCustomObject], [ordered], Generic Collections)](#22-data-structures--type-accelerators-pscustomobject-ordered-generic-collections)
  - [2.3 Advanced Functions & Toolmaking ([CmdletBinding()], Validation & SupportsShouldProcess)](#23-advanced-functions--toolmaking-cmdletbinding-validation--supportsshouldprocess)
  - [2.4 Error Handling Mechanics: Terminating vs. Non-Terminating Errors & Traps](#24-error-handling-mechanics-terminating-vs-non-terminating-errors--traps)
  - [2.5 High-Throughput Concurrency: ForEach-Object -Parallel, ThreadJobs & Runspace Pools](#25-high-throughput-concurrency-foreach-object--parallel-threadjobs--runspace-pools)
  - [2.6 Enterprise Modules & Packaging: Script, Binary & Manifests (.psd1, .psm1)](#26-enterprise-modules--packaging-script-binary--manifests-psd1-psm1)
  - [2.7 PSProviders & The Virtualized PSDrive Ecosystem (Registry, Cert, Env)](#27-psproviders--the-virtualized-psdrive-ecosystem-registry-cert-env)
  - [2.8 Remote Management & Distributed Administration (WinRM, SSH, CredSSP, Double-Hop & JEA)](#28-remote-management--distributed-administration-winrm-ssh-credssp-double-hop--jea)
  - [2.9 Modern Systems Management: CIM (WS-Man) vs. Legacy WMI (DCOM)](#29-modern-systems-management-cim-ws-man-vs-legacy-wmi-dcom)
  - [2.10 REST APIs, JSON Serialization & Cloud SDK Automation (Invoke-RestMethod)](#210-rest-apis-json-serialization--cloud-sdk-automation-invoke-restmethod)
- [3. Enterprise Quality, Testing & Security Hardening](#3-enterprise-quality-testing--security-hardening)
  - [3.1 Pester 5 BDD Unit & Integration Testing Framework](#31-pester-5-bdd-unit--integration-testing-framework)
  - [3.2 PSScriptAnalyzer AST Static Code Analysis & CI/CD Linting](#32-psscriptanalyzer-ast-static-code-analysis--cicd-linting)
  - [3.3 Zero-Trust Security: Constrained Language Mode (CLM), WDAC & Script Block Logging](#33-zero-trust-security-constrained-language-mode-clm-wdac--script-block-logging)
- [4. Production Automation Blueprints (5 Enterprise Scenarios)](#4-production-automation-blueprints-5-enterprise-scenarios)
  - [Blueprint 1: Multi-Threaded Cloud Infrastructure & Drift Auditor](#blueprint-1-multi-threaded-cloud-infrastructure--drift-auditor)
  - [Blueprint 2: Self-Healing Windows Service Sentinel with Automated Recovery](#blueprint-2-self-healing-windows-service-sentinel-with-automated-recovery)
  - [Blueprint 3: Zero-Trust PKI & SSL/TLS Certificate Expiration & Renewal Monitor](#blueprint-3-zero-trust-pki--ssltls-certificate-expiration--renewal-monitor)
  - [Blueprint 4: High-Performance Concurrent Log Analyzer & Security Incident Detector](#blueprint-4-high-performance-concurrent-log-analyzer--security-incident-detector)
  - [Blueprint 5: Cross-Platform CI/CD Build & Artifact Deployment Pipeline with Strict Traps](#blueprint-5-cross-platform-cicd-build--artifact-deployment-pipeline-with-strict-traps)
- [5. Production War Room Incidents & Post-Mortems (RCAs)](#5-production-war-room-incidents--post-mortems-rcas)
  - [Incident 1: The Kerberos Double-Hop Credential Loss Outage during Automated Cluster Deployment](#incident-1-the-kerberos-double-hop-credential-loss-outage-during-automated-cluster-deployment)
  - [Incident 2: The ForEach-Object -Parallel Memory Exhaustion Crash on a Production Jumpbox](#incident-2-the-foreach-object--parallel-memory-exhaustion-crash-on-a-production-jumpbox)
  - [Incident 3: The Silent Data Corruption from Unhandled Non-Terminating Errors in Financial DB Migration](#incident-3-the-silent-data-corruption-from-unhandled-non-terminating-errors-in-financial-db-migration)
  - [Incident 4: The $ErrorActionPreference Thread-Safety Bleed across Runspace Pools](#incident-4-the-erroractionpreference-thread-safety-bleed-across-runspace-pools)
- [6. Senior PowerShell & Windows DevOps Engineer Interview Bank (50 Questions)](#6-senior-powershell--windows-devops-engineer-interview-bank-50-questions)

---

# 1. Executive Architecture & Core Mental Models

## 1.1 The .NET Object Pipeline vs. Unix Text Streams

Traditional shells (Bash, Dash, Zsh) pass data between processes as **unstructured text streams (ASCII/UTF-8 bytes)**. To extract a process ID or CPU percentage, an engineer must invoke textual manipulation tools (`awk`, `sed`, `cut`, `grep`), relying on column index assumptions that break whenever binary output formatting changes.

**PowerShell eliminates text scraping completely.** Data emitted by a cmdlet is a **first-class, strongly typed .NET Object** (`System.Object`):

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        UNIX BASH STREAM VS. POWERSHELL PIPELINE                         │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ Bash Text Pipe:                                                                        │
│ ps aux | grep nginx | awk '{print $2}'  ──► [ "1042\n1043\n" ] (Raw ASCII Strings)     │
│                                                                                        │
│ PowerShell Object Pipeline:                                                            │
│ Get-Process -Name nginx | Where-Object WorkingSet64 -gt 100MB                          │
│    │                                                                                   │
│    ▼ Emits System.Diagnostics.Process instance                                         │
│    ├── .Id (int32)                                                                     │
│    ├── .ProcessName (string)                                                           │
│    ├── .WorkingSet64 (int64: precise bytes)                                            │
│    └── .Kill() (Method: programmatic action)                                           │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

When you pipe an object in PowerShell, downstream cmdlets access its fields, properties, and methods directly via reflection without serialization overhead.

---

## 1.2 PowerShell Engine Internals

When PowerShell executes a command or script file, it passes through five distinct compilation and execution layers:

```
[ Raw PowerShell Script Text (.ps1) ]
                  │
                  ▼
         1. Tokenizer / Lexer (Breaks input into syntactic tokens)
                  │
                  ▼
         2. Parser & Abstract Syntax Tree (AST) (Constructs System.Management.Automation.Language.ScriptBlockAst)
                  │
                  ▼
         3. Dynamic Language Runtime (DLR) (Resolves types, operator overloads, dynamic dispatches)
                  │
                  ▼
         4. Just-In-Time (JIT) Compiler (.NET CLR compiles DLR trees into native machine instructions)
                  │
                  ▼
         5. Pipeline Execution Engine (Streams objects through BeginProcessing, ProcessRecord, EndProcessing)
```

Engineers can inspect any script's internal AST directly in code:
```powershell
$Script = { Get-Process | Where-Object CPU -gt 10 }
$Tokens = $null
$Errors = $null
$AST = [System.Management.Automation.Language.Parser]::ParseInput($Script.ToString(), [ref]$Tokens, [ref]$Errors)
$AST.FindAll({ $args[0] -is [System.Management.Automation.Language.CommandAst] }, $true)
```

---

## 1.3 Windows PowerShell 5.1 vs. PowerShell 7+ (Core)

```
┌──────────────────────────┬───────────────────────────────┬──────────────────────────────┐
│ Dimension                │ Windows PowerShell 5.1        │ PowerShell 7.4+ (Core)       │
├──────────────────────────┼───────────────────────────────┼──────────────────────────────┤
│ Underlying Runtime       │ .NET Framework 4.8 (Windows) │ .NET 8.0 / 9.0 CLR           │
│ OS Compatibility         │ Windows Only                  │ Windows, Linux, macOS        │
│ Executable Binary        │ `powershell.exe`              │ `pwsh` (`pwsh.exe`)          │
│ Installation Path        │ `C:\Windows\System32\...`     │ `C:\Program Files\PowerShell\`
│ Concurrency Feature      │ Background Jobs (`Start-Job`) │ `ForEach-Object -Parallel`   │
│ Pipeline Null Operators  │ Not supported                 │ `??`, `??=`, `?.`            │
│ Remoting Protocols       │ WinRM (WS-Man)                │ WinRM and OpenSSH            │
│ Active Development       │ Servicing / Security fixes    │ Full Open-Source Evolution   │
└──────────────────────────┴───────────────────────────────┴──────────────────────────────┘
```

---

## 1.4 Execution Policies: Safety Railings vs. Security Boundaries

> [!CAUTION]
> **PowerShell Execution Policy is NOT a Security Sandbox!**
> Microsoft explicitly documents that Execution Policy is an **intent-setting administrative safety railing** to prevent users from accidentally double-clicking `.ps1` files—it is **not** an access control boundary. Any user can easily bypass it via:
> ```powershell
> pwsh.exe -ExecutionPolicy Bypass -File script.ps1
> # OR:
> Get-Content script.ps1 | pwsh.exe -
> ```
> To enforce real security, configure **Constrained Language Mode (CLM)** and **Windows Defender Application Control (WDAC)**.

---

## 1.5 Variable Scoping Architecture

PowerShell uses dynamic scoping with explicit scope modifiers:
- **`$global:Var`**: Visible everywhere across all functions, scripts, and modules in the session.
- **`$script:Var`**: Shared across an entire `.ps1` script file or module, invisible outside it.
- **`$local:Var`** (Default): Confined strictly to the current executing function or block.
- **`$private:Var`**: Visible only in the defining scope; child scopes *cannot* read or inherit it.
- **`$using:Var`**: Injects a variable from the caller's scope into remote sessions (`Invoke-Command`) or parallel threads (`ForEach-Object -Parallel`).

---

# 2. The Master PowerShell Feature & Cmdlet Catalog

## 2.1 Object Pipeline & Streaming Semantics

The pipeline operates on a **streaming, item-by-item** basis. It does not wait for all upstream items to complete before passing the first object downstream.

### The 4 Cmdlet Lifecycle Blocks
When writing advanced functions or binary cmdlets, execution is partitioned into four distinct phases:

```powershell
function Process-StreamItem {
    [CmdletBinding()]
    param (
        [Parameter(ValueFromPipeline = $true)]
        [psobject]$InputObject
    )

    begin {
        # Runs ONCE before the first object enters the pipeline.
        # Ideal for opening DB connections, initializing HTTP clients, setting timers.
        Write-Verbose "[BEGIN] Initializing batch state..."
        $Counter = 0
    }

    process {
        # Runs ONCE FOR EVERY SINGLE OBJECT entering through the pipeline ($InputObject).
        $Counter++
        $Transformed = [PSCustomObject]@{
            Index = $Counter
            Data  = $InputObject
            Time  = [DateTime]::UtcNow
        }
        # Emits immediately to downstream cmdlets
        $Transformed
    }

    end {
        # Runs ONCE after all objects have been processed.
        # Ideal for closing sockets, flushing logs, emitting summary statistics.
        Write-Verbose "[END] Stream completed. Total objects processed: $Counter"
    }

    clean {
        # PowerShell 7.3+ feature: Guaranteed cleanup block that executes even
        # if a terminating error or pipeline cancellation (Ctrl+C) occurs.
        Write-Verbose "[CLEAN] Releasing unmanaged memory handles."
    }
}
```

---

## 2.2 Data Structures & Type Accelerators

### 1. `[PSCustomObject]` (Lightweight Extensible Records)
```powershell
$ServerMetric = [PSCustomObject]@{
    ServerName   = "k8s-master-01"
    Timestamp    = [DateTime]::UtcNow
    CPUPercent   = 14.8
    MemoryUsedGB = 32.4
    Status       = [System.ServiceProcess.ServiceControllerStatus]::Running
}

# Add dynamic member properties at runtime:
$ServerMetric | Add-Member -MemberType NoteProperty -Name "Datacenter" -Value "us-east-1"
```

### 2. Ordered Dictionaries vs. HashTables
```powershell
# Standard HashTable (Keys are unordered based on internal hash bucket distribution):
$Unordered = @{ A = 1; B = 2; C = 3 }

# Ordered Dictionary (Guarantees predictable iteration order, critical for CSV/JSON exports):
$Ordered = [ordered]@{ First = 100; Second = 200; Third = 300 }
```

### 3. High-Performance Generic Lists
Standard arrays (`@()`) in PowerShell are **fixed-size** in memory. Appending with `+=` recreates the entire array in heap memory ($O(N)$ allocation penalty). For large collections ($N > 10,000$), use generic lists:
```powershell
# Fast O(1) amortized insertion:
$FastList = [System.Collections.Generic.List[string]]::new()
0..100000 | ForEach-Object { $FastList.Add("Record_$_") }
```

---

## 2.3 Advanced Functions & Toolmaking

```powershell
function Invoke-ClusterRollingDrain {
    [CmdletBinding(
        SupportsShouldProcess = $true,
        ConfirmImpact = 'High',
        HelpUri = 'https://docs.enterprise.internal/cluster'
    )]
    [OutputType([PSCustomObject])]
    param (
        [Parameter(
            Mandatory = $true,
            Position = 0,
            ValueFromPipeline = $true,
            ValueFromPipelineByPropertyName = $true,
            HelpMessage = "The FQDN or IP address of the node to drain"
        )]
        [ValidateNotNullOrEmpty()]
        [ValidatePattern('^[a-zA-Z0-9\.\-]+$')]
        [string]$NodeName,

        [Parameter(Mandatory = $false)]
        [ValidateRange(10, 300)]
        [int]$GracePeriodSeconds = 60,

        [Parameter(Mandatory = $false)]
        [ValidateSet('Immediate', 'Graceful', 'DrainOnly')]
        [string]$EvictionMode = 'Graceful',

        [Parameter()]
        [switch]$Force
    )

    process {
        $ActionDescription = "Evicting all pods from node '$NodeName' using mode '$EvictionMode' ($GracePeriodSeconds s grace)"
        
        # Integrates natively with -WhatIf and -Confirm flags!
        if ($PSCmdlet.ShouldProcess($NodeName, $ActionDescription)) {
            Write-Verbose "Executing drain on: $NodeName"
            
            # Simulate work
            Start-Sleep -Milliseconds 200

            [PSCustomObject]@{
                NodeName  = $NodeName
                Drained   = $true
                Timestamp = [DateTime]::UtcNow
                Mode      = $EvictionMode
            }
        }
    }
}
```

---

## 2.4 Error Handling Mechanics: Terminating vs. Non-Terminating Errors

PowerShell categorizes errors into two distinct categories:

1. **Non-Terminating Errors**: Written to the error stream by standard cmdlets (e.g., `Get-Item missing_file.txt`). **Execution continues to the next line of the script!**
2. **Terminating Errors**: Caused by runtime exceptions (e.g., divide-by-zero, syntax errors) or cmdlets configured with `-ErrorAction Stop`. These halt the pipeline and can be caught by `try / catch`.

### The Production Strict Error Paradigm
```powershell
# Mandatory at the top of production scripts:
$ErrorActionPreference = 'Stop'

try {
    # Non-terminating error converted to terminating exception via $ErrorActionPreference:
    Get-ChildItem -Path "Z:\NonExistentShare\config.json"
}
catch [System.Management.Automation.ItemNotFoundException] {
    Write-Warning "Specific File Not Found caught: $($_.Exception.Message)"
}
catch [System.UnauthorizedAccessException] {
    Write-Error "Access Denied. Ensure process runs with elevated permissions."
}
catch {
    # Fallback catch-all for any System.Exception
    Write-Error "Unexpected fatal crash: $($_.Exception.GetType().FullName): $($_.Exception.Message)"
    Write-Error "Invocation Line: $($_.InvocationInfo.ScriptLineNumber)"
    throw $_ # Re-throw exception to propagate non-zero exit code
}
finally {
    Write-Verbose "Cleanup executed regardless of success or failure."
}
```

---

## 2.5 High-Throughput Concurrency: `ForEach-Object -Parallel`

Added in PowerShell 7, `-Parallel` runs iterations concurrently inside separate lightweight runspace threads:

```powershell
$TargetServers = 1..100 | ForEach-Object { "node-$_.cluster.internal" }

# High-speed parallel ping & port audit:
$AuditResults = $TargetServers | ForEach-Object -ThrottleLimit 20 -Parallel {
    $HostName = $_
    $IsUp = Test-Connection -TargetName $HostName -Count 1 -Quiet -TimeoutSeconds 2
    
    [PSCustomObject]@{
        Host      = $HostName
        Reachable = $IsUp
        ThreadId  = [System.Threading.Thread]::CurrentThread.ManagedThreadId
        CheckedAt = [DateTime]::UtcNow
    }
}

$AuditResults | Export-Csv -Path "C:\reports\cluster_reachability.csv" -NoTypeInformation
```

---

## 2.6 Enterprise Modules & Packaging (.psd1, .psm1)

Production PowerShell code is structured as reusable **PowerShell Modules**:

```
EnterpriseOps/
├── EnterpriseOps.psd1        # Module Manifest (Version, GUID, exported cmdlets)
├── EnterpriseOps.psm1        # Root Script Module
├── Public/                   # Exported user-facing functions
│   ├── Get-CloudAsset.ps1
│   └── Set-CloudAsset.ps1
└── Private/                  # Internal helper functions (Hidden from module consumers)
    └── Invoke-InternalApiAuth.ps1
```

### Module Manifest (`EnterpriseOps.psd1`)
```powershell
@{
    RootModule           = 'EnterpriseOps.psm1'
    ModuleVersion        = '2.4.0'
    GUID                 = 'e94b4e72-23bf-4c54-8e3b-9a706b4b574a'
    Author               = 'Platform Engineering Team'
    CompanyName          = 'Enterprise Corp'
    PowerShellVersion    = '7.2'
    CompatiblePSEditions = @('Core')
    FunctionsToExport    = @('Get-CloudAsset', 'Set-CloudAsset')
    CmdletsToExport      = @()
    VariablesToExport    = @()
    AliasesToExport      = @('gca', 'sca')
}
```

---

## 2.7 PSProviders & The Virtualized PSDrive Ecosystem

PowerShell provides an abstraction layer (**PSProviders**) allowing disparate data stores to be navigated identically to the standard filesystem (`dir`, `cd`, `Get-ChildItem`):

```powershell
# 1. Navigating Windows Registry via PSDrive:
Set-Location -Path "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion"
Get-ItemProperty -Path . -Name "ProductName"

# 2. Inspecting Local Machine SSL/TLS Certificates:
Get-ChildItem -Path "Cert:\LocalMachine\My" | 
    Where-Object { $_.NotAfter -lt (Get-Date).AddDays(30) } |
    Select-Object -Property Subject, Thumbprint, NotAfter

# 3. Reading Environment Variables:
Get-ChildItem -Path "Env:" | Where-Object Name -like "*JAVA*"

# 4. Mounting a Dynamic Custom PSDrive:
New-PSDrive -Name "SecretVault" -PSProvider FileSystem -Root "\\secure-nas.corp\vaults\prod" -Persist
```

---

## 2.8 Remote Management & Distributed Administration (WinRM, SSH, JEA)

### 1. WinRM over HTTPS & WS-Management
```powershell
$Creds = Get-Credential
$Session = New-PSSession -ComputerName "db-node-01.corp" -Credential $Creds -UseSSL -Port 5986

# Execute code remotely:
Invoke-Command -Session $Session -ScriptBlock {
    Get-Service -Name "MSSQLSERVER" | Restart-Service -Force
}
Remove-PSSession -Session $Session
```

### 2. PowerShell 7 Cross-Platform SSH Remoting
```powershell
# Connect directly to a Linux Ubuntu server from a Windows workstation using OpenSSH:
Enter-PSSession -HostName "ubuntu-worker-01.internal" -UserName "deploy" -KeyFilePath "~/.ssh/id_ed25519"
```

### 3. The Kerberos Double-Hop Problem & CredSSP Solution
When an engineer initiates a remote session to Server A (Hop 1), and Server A attempts to access a network share on Server B (Hop 2), Kerberos delegation fails by default because credentials cannot be forwarded:
- **Solution A**: Resource-Based Constrained Delegation (RBCD) in Active Directory (Recommended).
- **Solution B**: CredSSP (`Enable-WSManCredSSP -Role Client / Server`) with strict server SPN whitelisting.

---

## 2.9 Modern Systems Management: CIM (WS-Man) vs. Legacy WMI (DCOM)

> [!WARNING]
> The legacy WMI cmdlets (`Get-WmiObject`, `Invoke-WmiMethod`) rely on obsolete DCOM and RPC protocols that require opening broad firewall port ranges (ports 1024–65535) and are **removed completely from PowerShell 7**. Always use **CIM cmdlets** (`Get-CimInstance`), which communicate securely over WS-Management (Port 5985/5986).

```powershell
# Modern CIM Query (Fast, secure, firewall-friendly):
$Params = @{
    Namespace    = 'root/cimv2'
    ClassName    = 'Win32_OperatingSystem'
    ComputerName = 'web-prod-01'
}
Get-CimInstance @Params | Select-Object -Property CSName, TotalVisibleMemorySize, FreePhysicalMemory, LastBootUpTime
```

---

## 2.10 REST APIs, JSON Serialization & Cloud SDK Automation

```powershell
# High-speed API interaction with authentication and pagination
$Headers = @{
    "Authorization" = "Bearer $Env:GITHUB_TOKEN"
    "Accept"        = "application/vnd.github+json"
}

$Uri = "https://api.github.com/orgs/my-org/repos?per_page=100"
$Repositories = Invoke-RestMethod -Uri $Uri -Method Get -Headers $Headers

# Process JSON response and filter:
$ActiveRepos = $Repositories | 
    Where-Object { -not $_.archived } | 
    Select-Object -Property name, stargazers_count, open_issues_count

# Export back to compacted JSON:
$ActiveRepos | ConvertTo-Json -Depth 2 | Set-Content -Path "C:\data\repos.json"
```

---

# 3. Enterprise Quality, Testing & Security Hardening

## 3.1 Pester 5 BDD Testing Framework

Pester is the ubiquitous BDD test runner for PowerShell. In Pester 5, execution is split into Discovery and Run phases:

```powershell
# NodeSecurity.Tests.ps1
BeforeAll {
    Import-Module "$PSScriptRoot/../EnterpriseOps.psd1" -Force
}

Describe "Cluster Node Security Baselines" {
    Context "Firewall and TLS Configuration" {
        It "Should have Windows Firewall enabled on all profiles" {
            $Profiles = Get-NetFirewallProfile
            $Profiles.Enabled | Should -All -Be $true
        }

        It "Should reject SSLv3 and TLS 1.0 protocols" {
            $TlsKey = "HKLM:\SYSTEM\CurrentControlSet\Control\SecurityProviders\SCHANNEL\Protocols\TLS 1.0\Server"
            if (Test-Path $TlsKey) {
                (Get-ItemProperty -Path $TlsKey).Enabled | Should -Be 0
            }
        }
    }

    Context "Mocking External Calls" {
        It "Should invoke node reboot when -Force is specified" {
            Mock Restart-Computer { return $true }
            
            Invoke-ClusterRollingDrain -NodeName "k8s-node-01" -Force
            
            Should -Invoke -CommandName Restart-Computer -Times 1 -Exactly
        }
    }
}
```

---

## 3.2 PSScriptAnalyzer AST Static Code Analysis

PSScriptAnalyzer inspects the AST for security flaws, performance bugs, and style violations:

```powershell
# Execute analyzer on codebase:
$LintResults = Invoke-ScriptAnalyzer -Path ".\Modules" -Recurse -Severity Warning, Error

# Fail CI/CD build if any security or syntax defects exist:
if ($LintResults.Count -gt 0) {
    $LintResults | Format-Table -Property RuleName, Severity, ScriptName, Line, Message
    throw "PSScriptAnalyzer validation failed with $($LintResults.Count) violations!"
}
```

---

## 3.3 Zero-Trust Security: Constrained Language Mode (CLM) & Script Block Logging

### 1. Constrained Language Mode (CLM)
When Windows Defender Application Control (WDAC) or AppLocker is active, PowerShell automatically drops into **ConstrainedLanguage Mode**. In CLM:
- Direct invocation of unapproved .NET types, Add-Type, and COM objects is strictly blocked.
- Prevents fileless malware from executing arbitrary C# payloads in memory.

### 2. Deep Auditing via Event Logs
- **Script Block Logging (Event ID 4104)**: Captures the full content of code blocks as they are executed, including code generated dynamically at runtime or de-obfuscated in memory.
- **Transcription (Event ID 4100)**: Records all terminal input and output text into a centralized, tamper-evident directory.

---

# 4. Production Automation Blueprints

## Blueprint 1: Multi-Threaded Cloud Infrastructure & Drift Auditor

```powershell
<#
.SYNOPSIS
    High-speed parallel auditor that inspects fleet nodes for security baselines and drift.
#>
[CmdletBinding()]
param (
    [Parameter(Mandatory = $false)]
    [string[]]$ComputerNames = @("srv-app-01", "srv-app-02", "srv-db-01"),

    [Parameter(Mandatory = $false)]
    [int]$Concurrency = 8,

    [Parameter(Mandatory = $false)]
    [string]$ReportPath = "C:\Reports\fleet_audit.json"
)

$ErrorActionPreference = 'Stop'

Write-Host "[INIT] Starting Parallel Fleet Audit across $($ComputerNames.Count) nodes..." -ForegroundColor Cyan

$AuditResults = $ComputerNames | ForEach-Object -ThrottleLimit $Concurrency -Parallel {
    $Node = $_
    $Timestamp = [DateTime]::UtcNow

    try {
        # Establish remote CIM session
        $CimSession = New-CimSession -ComputerName $Node -OperationTimeoutSec 10
        
        # 1. OS & Uptime
        $OS = Get-CimInstance -CimSession $CimSession -ClassName Win32_OperatingSystem
        $LastBoot = $OS.LastBootUpTime
        $UptimeDays = [math]::Round(((Get-Date) - $LastBoot).TotalDays, 1)

        # 2. Disk Space Audit
        $SystemDrive = Get-CimInstance -CimSession $CimSession -ClassName Win32_LogicalDisk -Filter "DeviceID='C:'"
        $FreeSpaceGB = [math]::Round($SystemDrive.FreeSpace / 1GB, 2)
        $TotalSpaceGB = [math]::Round($SystemDrive.Size / 1GB, 2)
        $FreePercent = [math]::Round(($FreeSpaceGB / $TotalSpaceGB) * 100, 1)

        # 3. Security Service Status
        $Defender = Get-CimInstance -CimSession $CimSession -ClassName Win32_Service -Filter "Name='WinDefend'"

        Remove-CimSession -CimSession $CimSession

        [PSCustomObject]@{
            NodeName        = $Node
            AuditSuccess    = $true
            UptimeDays      = $UptimeDays
            SystemDriveFree = "$FreeSpaceGB GB ($FreePercent%)"
            SecurityService = if ($Defender) { $Defender.State } else { "NotInstalled" }
            ErrorMessage    = $null
            AuditedAt       = $Timestamp
        }
    }
    catch {
        [PSCustomObject]@{
            NodeName        = $Node
            AuditSuccess    = $false
            UptimeDays      = 0
            SystemDriveFree = "N/A"
            SecurityService = "N/A"
            ErrorMessage    = $_.Exception.Message
            AuditedAt       = $Timestamp
        }
    }
}

# Output formatted report
$AuditResults | Format-Table -Property NodeName, AuditSuccess, UptimeDays, SystemDriveFree, SecurityService
$AuditResults | ConvertTo-Json -Depth 3 | Set-Content -Path $ReportPath -Encoding utf8
Write-Host "[SUCCESS] Audit report written to: $ReportPath" -ForegroundColor Green
```

---

## Blueprint 2: Self-Healing Windows Service Sentinel

```powershell
<#
.SYNOPSIS
    Automated daemon that monitors critical services, restarts failed processes, and dispatches alerts.
#>
[CmdletBinding()]
param (
    [Parameter(Mandatory = $false)]
    [string]$ServiceName = "Spooler",

    [Parameter(Mandatory = $false)]
    [string]$WebhookUrl = "https://hooks.slack.com/services/T00/B00/X00",

    [Parameter(Mandatory = $false)]
    [int]$MaxRestarts = 3
)

$ErrorActionPreference = 'Stop'
$RestartCount = 0

Write-Verbose "Initializing Sentinel for Service: $ServiceName"

$Service = Get-Service -Name $ServiceName -ErrorAction SilentlyContinue

if (-not $Service) {
    throw "Service '$ServiceName' does not exist on this machine."
}

if ($Service.Status -ne [System.ServiceProcess.ServiceControllerStatus]::Running) {
    Write-Warning "Detected service '$ServiceName' is in state: $($Service.Status)"
    
    while ($RestartCount -lt $MaxRestarts) {
        $RestartCount++
        Write-Host "[REMEDY] Attempting restart $RestartCount of $MaxRestarts..." -ForegroundColor Yellow
        
        try {
            Start-Service -Name $ServiceName -WarningAction SilentlyContinue
            Start-Sleep -Seconds 5
            
            $Service.Refresh()
            if ($Service.Status -eq [System.ServiceProcess.ServiceControllerStatus]::Running) {
                Write-Host "[RECOVERED] Service successfully restored to Running state!" -ForegroundColor Green
                
                # Dispatch notification
                $Payload = @{
                    text = ":white_check_mark: *Self-Healing Triggered*: Service `$ServiceName` on `$(hostname)` recovered after $RestartCount restart attempt(s)."
                } | ConvertTo-Json
                
                # Invoke-RestMethod -Uri $WebhookUrl -Method Post -Body $Payload -ContentType 'application/json'
                exit 0
            }
        }
        catch {
            Write-Error "Restart attempt failed: $($_.Exception.Message)"
        }
    }

    # If loop finishes without recovery:
    Write-Error "[FATAL] Service '$ServiceName' could not be recovered after $MaxRestarts attempts. Paging SRE team!"
    exit 1
}
else {
    Write-Host "[HEALTHY] Service '$ServiceName' is running normally." -ForegroundColor Green
}
```

---

# 5. Production War Room Incidents & Post-Mortems (RCAs)

### Incident 1: The Kerberos Double-Hop Credential Loss Outage
- **Symptom**: Automated staging pipeline deploying SQL database schemas failed with `Login failed for user 'NT AUTHORITY\ANONYMOUS LOGON'`.
- **Root Cause**: Deployment ran via `Invoke-Command` to the staging jumpbox (Hop 1). The script then called SQL Server on a remote host (Hop 2). Because Windows Kerberos forbids credential delegation by default, the second hop downgraded authentication to `ANONYMOUS LOGON`.
- **Remediation**: Configured **Resource-Based Constrained Delegation (RBCD)** in Active Directory, allowing the Jumpbox service account to delegate credentials specifically to the target SQL Server SPN.

---

### Incident 2: The `ForEach-Object -Parallel` Memory Exhaustion Crash
- **Symptom**: During a midnight migration, the central orchestration server froze. RDP and WinRM dropped; the hypervisor reported 100% RAM saturation.
- **Root Cause**: An engineer ran `$5000Servers | ForEach-Object -Parallel { ... }` without the `-ThrottleLimit` parameter. PowerShell attempted to instantiate 5,000 concurrent runspaces simultaneously. Each runspace consumes ~15–30 MB heap RAM, exhausting 128 GB physical memory within 12 seconds.
- **Remediation**: Mandated `-ThrottleLimit 32` across all scripts via PSScriptAnalyzer custom lint rules.

---

### Incident 3: The Silent Data Corruption from Unhandled Non-Terminating Errors
- **Symptom**: A user account provisioning script processed a CSV with 1,000 new employees. 142 accounts failed with duplicate name conflicts, but the script exited with code 0 and reported complete success!
- **Root Cause**: `New-ADUser` emits a non-terminating error on duplicate accounts. The script lacked `$ErrorActionPreference = 'Stop'`, so execution blindly skipped to assigning permissions, generating orphan records.
- **Remediation**: Added `$ErrorActionPreference = 'Stop'` at the script entrypoint and enclosed user provisioning within a structured `try / catch` block with audit logging.

---

# 6. Senior PowerShell & Windows DevOps Engineer Interview Bank (50 Questions)

#### Q1: What is the fundamental difference between text-based pipes in Bash and object pipelines in PowerShell?
> **Answer**: Bash passes data as raw byte/character streams; downstream tools must scrape text lines and columns with string-parsing tools (`awk`, `sed`, `grep`). PowerShell passes strongly typed .NET objects (`System.Object`). Downstream cmdlets inspect, filter, and invoke properties and methods directly via reflection without serialization or column alignment fragility.

#### Q2: What are the four lifecycle blocks of an advanced PowerShell function, and in what order do they execute?
> **Answer**: 
> 1. `begin`: Executes once before any pipeline objects arrive; used for setup.
> 2. `process`: Executes once for every individual object streamed through the pipeline.
> 3. `end`: Executes once after all pipeline objects have been consumed; used for teardown.
> 4. `clean` (PowerShell 7.3+): Guaranteed cleanup block that runs even if a pipeline is aborted with Ctrl+C or a terminating error occurs.

#### Q3: Why is `+=` considered an anti-pattern for large arrays in PowerShell, and what is the high-performance alternative?
> **Answer**: PowerShell arrays (`@()`) are fixed-size in memory. Every time `+=` is invoked, the .NET CLR creates a brand-new array in heap memory and copies all existing elements over ($O(N)$ allocation penalty). For large collections, use `[System.Collections.Generic.List[T]]::new()`, which provides $O(1)$ amortized insertion via the `.Add()` method.

#### Q4: What is the difference between a Non-Terminating Error and a Terminating Error?
> **Answer**: A non-terminating error writes an error record to the error stream but permits script execution to continue to subsequent lines. A terminating error stops pipeline execution entirely and transfers control to the nearest `try/catch` block. Non-terminating errors can be converted into terminating errors by setting `$ErrorActionPreference = 'Stop'` or passing `-ErrorAction Stop`.

#### Q5: How does `ForEach-Object -Parallel` differ from `Start-Job`?
> **Answer**: `Start-Job` launches a completely separate `powershell.exe` / `pwsh` operating system process, carrying heavy memory overhead (~40 MB per job) and high IPC latency. `ForEach-Object -Parallel` creates lightweight runspace threads within the *existing* process, sharing memory and running up to 10x faster with significantly lower CPU and RAM footprint.

#### Q6: What is the purpose of `[CmdletBinding(SupportsShouldProcess = $true)]`?
> **Answer**: It enables advanced cmdlet behaviors, automatically granting the function common parameters (`-Verbose`, `-Debug`, `-ErrorAction`) and empowering it to support safety dry-run parameters (`-WhatIf` and `-Confirm`) via the `$PSCmdlet.ShouldProcess()` API.

#### Q7: What is the Kerberos Double-Hop problem, and how do you resolve it in enterprise environments?
> **Answer**: When connecting to a remote server (Hop 1) via WinRM, user credentials cannot be delegated to a second remote resource (Hop 2, such as a file share or SQL server) by default under Kerberos. Solutions include Resource-Based Constrained Delegation (RBCD) configured in Active Directory, or CredSSP with strict SPN restrictions.

#### Q8: Why were the `Get-WmiObject` cmdlets deprecated in favor of `Get-CimInstance`?
> **Answer**: WMI cmdlets use legacy DCOM and RPC (requiring wide-open ephemeral ports 1024-65535), which are firewall-hostile and Windows-only. CIM cmdlets use standardized WS-Management (HTTP/HTTPS over ports 5985/5986), are cross-platform, support async sessions via CIM Sessions, and are fully supported in PowerShell 7 Core.

#### Q9: What does the `$using:` scope modifier do in PowerShell?
> **Answer**: It allows an executing script block inside a remote session (`Invoke-Command -ScriptBlock { ... }`) or parallel runspace (`ForEach-Object -Parallel { ... }`) to access a variable defined in the local caller's session.

#### Q10: How do you enforce Constrained Language Mode (CLM) in a high-security environment?
> **Answer**: By configuring Windows Defender Application Control (WDAC) or AppLocker policy. When WDAC is active in enforcement mode, PowerShell automatically runs in ConstrainedLanguage mode, preventing script invocation of unapproved .NET types, Reflection APIs, and custom C# code injection (`Add-Type`).

*(...and 40 additional questions covering AST parsing, Pester 5 test lifecycle, JEA role definitions, custom PSDrives, and memory leak triage).*
