[🏠 Back to Home](README.md) | [🐧 Linux Mastery Guide](linux.md) | [📜 Bash & Shell Scripting](linux.md#-linux-mastery-part-7---bash-scripting-and-automation) | [💻 IT Tech Words](it_tech_words_master_guide.md)

# 📝 Vi, Vim & Nano Master Guide: Terminal Text Editing & DevOps Production Surgery

### *(The Complete Guide to Terminal Text Editors: Modal Navigation, Regex Surgery, Visual Block Editing, Production Configuration, Sudo Tricks & War Room Rescues)*

[![Terminal Editors](https://img.shields.io/badge/Terminal-Vi%20%7C%20Vim%20%7C%20Nano-blue.svg?style=for-the-badge)]()
[![DevOps Operations](https://img.shields.io/badge/DevOps-Sysadmin%20%26%20SRE-brightgreen.svg?style=for-the-badge)]()
[![Modal Editing](https://img.shields.io/badge/Vim-Modal%20Grammar-orange.svg?style=for-the-badge)]()
[![Production Ready](https://img.shields.io/badge/War%20Room-Zero%20GUI%20Rescues-red.svg?style=for-the-badge)]()

---

## 📑 Master Table of Contents

- [1. Executive Overview & The Editor Comparison Matrix](#1-executive-overview--the-editor-comparison-matrix)
- [2. The "I Am Trapped" Emergency Exit Sheet](#2-the-i-am-trapped-emergency-exit-sheet)
- [3. Nano In-Depth: The Human-Friendly Modeless Terminal Editor](#3-nano-in-depth-the-human-friendly-modeless-terminal-editor)
  - [3.1 The Nano Mental Model & Key Notation](#31-the-nano-mental-model--key-notation)
  - [3.2 Essential Commands & Daily Shortcuts](#32-essential-commands--daily-shortcuts)
  - [3.3 Navigation, Searching & Text Surgery](#33-navigation-searching--text-surgery)
  - [3.4 Production Configuration (~/.nanorc)](#34-production-configuration-nanorc)
  - [3.5 Nano Production Pitfalls (The Silent Auto-Wrap Danger)](#35-nano-production-pitfalls-the-silent-auto-wrap-danger)
- [4. Vi & Vim In-Depth: The Modal Powerhouse](#4-vi--vim-in-depth-the-modal-powerhouse)
  - [4.1 The Modal Editing Philosophy & State Machine](#41-the-modal-editing-philosophy--state-machine)
  - [4.2 The Grammar of Vim: [Verb] + [Count] + [Noun / Motion]](#42-the-grammar-of-vim-verb--count--noun--motion)
  - [4.3 High-Velocity Navigation Without Arrow Keys](#43-high-velocity-navigation-without-arrow-keys)
  - [4.4 Text Surgery: Deletion, Yanking (Copy), Pasting & Registers](#44-text-surgery-deletion-yanking-copy-pasting--registers)
  - [4.5 Powerhouse Text Objects (ciw, di", yap, da{)](#45-powerhouse-text-objects-ciw-di-yap-da)
  - [4.6 Search & Global Search-and-Replace (Regex Surgery)](#46-search--global-search-and-replace-regex-surgery)
  - [4.7 Visual Mode & Multi-Line Column Editing (Ctrl+v)](#47-visual-mode--multi-line-column-editing-ctrlv)
  - [4.8 Splits, Tabs, and Buffers](#48-splits-tabs-and-buffers)
  - [4.9 Production SRE & DevOps Tricks (sudo tee, paste mode, unix line endings)](#49-production-sre--devops-tricks-sudo-tee-paste-mode-unix-line-endings)
  - [4.10 Production Hardening Configuration (~/.vimrc)](#410-production-hardening-configuration-vimrc)
- [5. Production War Room Incidents & RCAs](#5-production-war-room-incidents--rcas)
  - [Incident 1: The Nano Auto-Wrap SSH Key Disaster](#incident-1-the-nano-auto-wrap-ssh-key-disaster)
  - [Incident 2: The Dreaded Vim Swap File Lockout (E325: ATTENTION)](#incident-2-the-dreaded-vim-swap-file-lockout-e325-attention)
  - [Incident 3: The Staircase Cascade Paste Bug in Kubernetes YAML](#incident-3-the-staircase-cascade-paste-bug-in-kubernetes-yaml)
- [6. Senior DevOps & SRE Interview Bank (25 Questions & Explanations)](#6-senior-devops--sre-interview-bank-25-questions--explanations)
- [7. Bonus Engineering Note: Resolving Rust ring v0.17.14 Windows GCC Build Failures](#7-bonus-engineering-note-resolving-rust-ring-v01714-windows-gcc-build-failures)

---

# 1. Executive Overview & The Editor Comparison Matrix

In cloud infrastructure, container environments, bare-metal servers, and live production incidents, graphical user interfaces (GUIs) like VS Code or IntelliJ do not exist. When an SSH session drops you into a broken server at 3:00 AM with high latency and a degraded network, your survival depends entirely on **terminal text editors**.

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                      THE TERMINAL TEXT EDITOR LANDSCAPE                          │
├──────────────────┬───────────────────────────────┬───────────────────────────────┤
│ Editor           │ Primary Mental Model          │ Default Availability          │
├──────────────────┼───────────────────────────────┼───────────────────────────────┤
│ nano             │ Modeless (Type = Insert text) │ Ubuntu, Debian, Alpine (extra)│
│ vi               │ Modal (Command vs Insert)     │ POSIX Standard (Everywhere!)  │
│ vim              │ Modal (Enhanced vi + plugins) │ Enterprise Linux (RHEL/Debian)│
│ nvim (Neovim)    │ Modal (Async Lua ecosystem)   │ Modern Workstations & Servers │
└──────────────────┴───────────────────────────────┴───────────────────────────────┘
```

### Comprehensive Technical Matrix

| Dimension | `nano` | `vi` (Original / BusyBox) | `vim` (Vi IMproved) |
| :--- | :--- | :--- | :--- |
| **Mental Model** | Modeless (WYSIWYG) | Modal (Command, Insert, Ex) | Modal (Command, Insert, Visual, Ex) |
| **Learning Curve** | 5 minutes | Moderate | Steep (Lifelong mastery) |
| **Footprint / Binary Size** | ~200 KB - 500 KB | ~30 KB - 100 KB (Tiny) | ~2 MB - 30 MB |
| **Presence in Minimal Containers (Alpine / Scratch / Distroless)** | ❌ Rarely installed | ✅ Almost always (`/bin/vi` via BusyBox) | ⚠️ Often needs `apt install vim` |
| **Syntax Highlighting** | Basic (via `.nanorc`) | ❌ None | ✅ Rich, language-aware |
| **Visual Column / Block Editing** | ❌ No | ❌ No | ✅ Yes (`Ctrl+v`) |
| **Extensibility & Plugins** | ❌ Minimal | ❌ None | ✅ Vimscript, Python, Lua |
| **Regex & Global Substitution** | Basic Regex | Standard ed/ex Regex | Powerful POSIX/Perl-like Regex |
| **Primary Use Case** | Quick config edit by beginners | Emergency single-user mode / Alpine pods | Full software engineering & SRE surgery |

---

# 2. The "I Am Trapped" Emergency Exit Sheet

The single most famous meme in computer science is: *"I have been using Vim for 3 years, mostly because I don't know how to exit."*

Here is your foolproof emergency cheat card:

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          EMERGENCY EXIT CHEAT SHEET                             │
├─────────┬───────────────────────────┬───────────────────────────────────────────┤
│ Editor  │ Action                    │ Exact Key Sequence                        │
├─────────┼───────────────────────────┼───────────────────────────────────────────┤
│ Vim/Vi  │ Quit WITHOUT saving       │ Esc  Esc  Esc  :q!  Enter                 │
│ Vim/Vi  │ Save and Quit             │ Esc  Esc  Esc  :wq  Enter  (or  ZZ )      │
│ Vim/Vi  │ Save with sudo permission │ Esc  :w !sudo tee % > /dev/null  Enter    │
│ Vim/Vi  │ Unfreeze frozen terminal  │ Ctrl+q (undoes accidental Ctrl+s freeze)  │
├─────────┼───────────────────────────┼───────────────────────────────────────────┤
│ Nano    │ Exit                      │ Ctrl+X                                    │
│ Nano    │ Save (WriteOut)           │ Ctrl+O  then press  Enter                 │
│ Nano    │ Discard changes on exit   │ Ctrl+X  then press  N                     │
│ Nano    │ Cancel current prompt     │ Ctrl+C                                    │
└─────────┴───────────────────────────┴───────────────────────────────────────────┘
```

---

# 3. Nano In-Depth: The Human-Friendly Modeless Terminal Editor

`nano` is a clone of the classic `pico` editor from the Pine email client. It was created to provide a simple, modeless, intuitive text editor that requires zero prior training.

## 3.1 The Nano Mental Model & Key Notation

In `nano`, whatever you type immediately appears on the screen as text (just like Notepad, Word, or Google Docs). Control operations are triggered via modifier keys displayed at the bottom two lines of the terminal:

- **`^` (Caret)**: Represents the **`Ctrl`** key.
  - Example: `^X` means `Ctrl + X`.
  - Example: `^O` means `Ctrl + O`.
- **`M-` (Meta)**: Represents the **`Alt`** key (or `Option` key on macOS, or pressing `Esc` followed by the letter).
  - Example: `M-U` means `Alt + U` (Undo).
  - Example: `M-E` means `Alt + E` (Redo).

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│  GNU nano 7.2                 /etc/nginx/nginx.conf                             │
│                                                                                 │
│  events {                                                                       │
│      worker_connections 1024;                                                   │
│  }                                                                              │
│                                                                                 │
│[ Read 3 lines ]                                                                 │
│^G Get Help   ^O WriteOut   ^W Where Is    ^K Cut Text    ^T Execute   ^C Location   │
│^X Exit       ^R Read File  ^\ Replace     ^U Paste Text  ^J Justify   ^_ Go To Line │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## 3.2 Essential Commands & Daily Shortcuts

### File Operations
- **`Ctrl + O` (WriteOut)**: Save the file without exiting. Prompts for filename; press `Enter` to confirm.
- **`Ctrl + X` (Exit)**: Closes the editor. If changes are unsaved, it prompts `Save modified buffer? (Y/N)`. Press `Y` then `Enter` to save, or `N` to discard.
- **`Ctrl + R` (Read File)**: Inserts the contents of another file at the current cursor position.

### Cut, Copy & Paste
- **`Ctrl + K` (Cut Line)**: Cuts the entire current line into the cutbuffer. Pressing it multiple times in succession cuts multiple lines into a single buffer.
- **`Alt + 6` (or `M-6` - Copy Line / Mark)**: Copies the current line or selected text block into the buffer without deleting it.
- **`Ctrl + U` (Uncut / Paste)**: Pastes the contents of the cutbuffer at the current cursor position.
- **`Ctrl + ^` (or `Alt + A` - Mark Text)**: Begins text selection mark. Move the cursor with arrow keys to highlight text, then press `Ctrl + K` (cut) or `Alt + 6` (copy).

## 3.3 Navigation, Searching & Text Surgery

### Searching & Replacing
- **`Ctrl + W` (Where Is)**: Search for a string. Type the term and press `Enter`.
- **`Alt + W` (Find Next)**: Repeat the last search forward.
- **`Ctrl + \` (Search and Replace)**: Prompts for search term, then replacement string. Then prompts for confirmation: `(Y)es`, `(N)o`, `(A)ll`, `^C` (Cancel).

### Precision Navigation
- **`Ctrl + _` (or `Ctrl + G` - Go to Line)**: Jump directly to a line number and column (`line,column`).
- **`Ctrl + C` (CurPos)**: Displays current line number, column, and total character count.
- **`Alt + \` (First Line)**: Jump to the very beginning of the file.
- **`Alt + /` (Last Line)**: Jump to the very end of the file.
- **`Ctrl + Y` (Page Up)**: Scroll up one full screen.
- **`Ctrl + V` (Page Down)**: Scroll down one full screen.

## 3.4 Production Configuration (`~/.nanorc`)

By default, stock `nano` does not show line numbers, uses physical tabs, and lacks mouse support. You can configure it via `/etc/nanorc` (global) or `~/.nanorc` (user-level):

```ini
# ~/.nanorc - Production Sysadmin Configuration

# Display line numbers on the left margin
set linenumbers

# Automatically convert typed Tab key into spaces
set tabstospaces

# Set tab width to 2 spaces (standard for YAML, Kubernetes, JSON)
set tabsize 2

# Enable smooth scrolling (scroll line-by-line rather than half-screen jumps)
set smooth

# DO NOT wrap long lines automatically (Prevents breaking SSH keys & tokens!)
set nowrap

# Enable mouse support (click to position cursor)
set mouse

# Constant cursor position display on the bottom bar
set constantshow

# Auto-indent new lines to the previous line's indentation level
set autoindent

# Syntax highlighting includes (Ubuntu/Debian paths)
include "/usr/share/nano/*.nanorc"
```

## 3.5 Nano Production Pitfalls (The Silent Auto-Wrap Danger)

> [!WARNING]
> **The #1 Nano Incident in Production: Hard Wrapping (`-w` flag requirement)**
> By default on older Linux distributions, `nano` enables **automatic line wrapping**. If you paste a long, unbroken string—such as an RSA private key, a JWT bearer token, or a base64 encoded secret in a Kubernetes ConfigMap—nano will silently insert a hard carriage return / newline character every 80 characters!
> 
> **How to prevent this:**
> Always launch nano with the `-w` (no wrap) flag when editing production configuration files:
> ```bash
> nano -w /etc/kubernetes/manifests/kube-apiserver.yaml
> ```

---

# 4. Vi & Vim In-Depth: The Modal Powerhouse

`vi` was created in 1976 by Bill Joy (co-founder of Sun Microsystems). `Vim` (Vi IMproved) was released in 1991 by Bram Moolenaar.

Unlike conventional editors, Vim is **modal**. The keys on your keyboard do not directly correspond to typing letters; instead, they act as an expressive **command language**.

## 4.1 The Modal Editing Philosophy & State Machine

```
                              ┌─────────────────────────┐
                              │       NORMAL MODE       │◄─────────────┐
                              │ (Navigation & Commands) │              │
                              └──────┬───────────▲──────┘              │
                                     │           │                     │
                        Press 'i','a','o'     Press 'Esc'          Press 'Esc'
                                     │           │                     │
                                     ▼           │                     │
                              ┌──────────────────┴──────┐              │
                              │       INSERT MODE       │              │
                              │     (Direct Typing)     │              │
                              └─────────────────────────┘              │
                                     │                                 │
                                     │ Press ':'                       │
                                     ▼                                 │
                              ┌─────────────────────────┐              │
                              │   COMMAND-LINE / EX     │──────────────┤
                              │ (:w, :q, :%s/old/new/g) │  Press Enter │
                              └─────────────────────────┘              │
                                     │                                 │
                                     │ Press 'v', 'V', 'Ctrl+v'        │
                                     ▼                                 │
                              ┌─────────────────────────┐              │
                              │       VISUAL MODE       │──────────────┘
                              │(Selection & Column Edit)│
                              └─────────────────────────┘
```

### The 5 Primary Modes:
1. **Normal Mode (`Esc`)**: The default home base. Every key executes an operational verb or motion (e.g., `dd` deletes a line, `u` undoes).
2. **Insert Mode (`i`, `a`, `o`, `I`, `A`, `O`)**: Direct text entry mode.
   - `i`: Insert before cursor.
   - `a`: Append after cursor.
   - `I`: Insert at beginning of line (first non-whitespace).
   - `A`: Append at end of line.
   - `o`: Open new line below current line and enter Insert mode.
   - `O`: Open new line above current line and enter Insert mode.
3. **Visual Mode (`v`, `V`, `Ctrl + v`)**:
   - `v`: Character-wise visual selection.
   - `V`: Line-wise visual selection.
   - `Ctrl + v`: Column-wise **block** visual selection (multi-cursor editing).
4. **Command-Line / Ex Mode (`:`, `/`, `?`)**: Used for writing files (`:w`), quitting (`:q`), external shell commands (`:!`), and regex substitutions (`:%s`).
5. **Replace Mode (`R`)**: Overwrites existing text character-by-character without shifting subsequent text.

## 4.2 The Grammar of Vim: `[Verb] + [Count] + [Noun / Motion]`

In Vim, you speak in structured grammar sentences:

$$\text{Command} = [\text{Operator (Verb)}] + [\text{Count}] + [\text{Motion / Text Object (Noun)}]$$

### The Core Verbs (Operators):
- `d`: Delete (cut)
- `c`: Change (delete text and instantly drop into Insert mode)
- `y`: Yank (copy)
- `v`: Visually select
- `>`: Indent right
- `<`: Indent left

### The Core Nouns (Motions & Text Objects):
- `w`: Next word
- `b`: Back one word
- `e`: End of word
- `$`: End of line
- `0`: Beginning of line
- `G`: End of file
- `iw`: "Inner Word"
- `i"`: "Inner Quotes" (everything inside `"..."`)
- `i(` or `ib`: "Inner Parentheses"
- `i{` or `iB`: "Inner Braces"
- `it`: "Inner XML/HTML Tag"

### Example "Sentences" in Vim:
- `d w` $\to$ Delete word.
- `d 3 w` $\to$ Delete next 3 words.
- `c i "` $\to$ Change text **inside** double quotes (e.g., `"DATABASE_URL"` becomes `""` with cursor inside in Insert mode).
- `d a (` $\to$ Delete **around** parentheses (including the parentheses themselves).
- `y 5 j` $\to$ Yank (copy) current line and next 5 lines downward.
- `c $` (or `C`) $\to$ Change text from cursor to end of line.

## 4.3 High-Velocity Navigation Without Arrow Keys

Professional engineers never take their fingers off the home row (`ASDF JKL;`):

```
             k (Up)
             ▲
             │
   h (Left) ◄─┼─► l (Right)
             │
             ▼
             j (Down)
```

### Motion Cheat Table

| Key | Movement Direction | Description |
| :--- | :--- | :--- |
| `h` / `j` / `k` / `l` | Left / Down / Up / Right | Character and line navigation |
| `w` | Forward | Jump forward to the start of the next word |
| `b` | Backward | Jump backward to the start of the previous word |
| `e` | End | Jump forward to the end of the current word |
| `0` | Hard start | Jump to the very first character of the line (column 0) |
| `^` | Soft start | Jump to the first non-blank character of the line |
| `$` | End | Jump to the last character of the line |
| `gg` | Top | Jump to line 1 (start of file) |
| `G` | Bottom | Jump to the last line of the file |
| `42G` or `:42` | Line Jump | Jump directly to line 42 |
| `H` / `M` / `L` | High / Middle / Low | Jump to top, middle, or bottom of currently visible screen |
| `Ctrl + u` | Up | Scroll up half a screen |
| `Ctrl + d` | Down | Scroll down half a screen |
| `Ctrl + b` | Back | Scroll up full screen |
| `Ctrl + f` | Forward | Scroll down full screen |
| `%` | Match Bracket | Jump to matching `( )`, `{ }`, or `[ ]` |

## 4.4 Text Surgery: Deletion, Yanking (Copy), Pasting & Registers

### Deletion & Cutting:
- `x`: Delete single character under cursor.
- `dd`: Delete (cut) current entire line.
- `5dd`: Delete 5 lines starting from current line.
- `dw`: Delete word from cursor forward.
- `d$`: Delete from cursor to end of line (synonym: `D`).
- `d0`: Delete from cursor to beginning of line.
- `dG`: Delete from cursor all the way to the end of the file.
- `1,$d`: Ex command to delete the entire file contents.

### Yanking & Putting (Copy & Paste):
- `yy`: Yank (copy) current entire line.
- `3yy`: Yank 3 lines.
- `yw`: Yank word.
- `p`: Put (paste) clipboard buffer **after** current cursor or line below.
- `P`: Put (paste) clipboard buffer **before** current cursor or line above.

### Undo & Redo:
- `u`: Undo last operation.
- `Ctrl + r`: Redo last undone operation.

### Registers (Vim's Multi-Clipboard System):
Vim does not have just one clipboard; it has 26 named registers (`"a` to `"z`) plus special registers:
- `""`: The default unnamed register.
- `"0`: The yank register (always holds the last copied text, never overwritten by `d` delete!).
- `"+`: The system OS clipboard (requires Vim compiled with clipboard support).
  - Example: `"+y` yanks text directly into your Mac/Windows clipboard.
  - Example: `"+p` pastes text directly from your Mac/Windows clipboard.
- `"a5yy`: Copies 5 lines specifically into register `a`.
- `"ap`: Pastes the contents of register `a`.

## 4.5 Powerhouse Text Objects (`ciw`, `di"`, `yap`, `da{`)

Text objects allow surgical modifications regardless of where your cursor is positioned inside the target:

```
Pattern: [Action: c, d, y, v] + [Scope: i (inside) or a (around)] + [Target: w, ", ', (, {, t]
```

```python
# Cursor is on the letter 'u' in "production_cluster"
database_host = "production_cluster.internal.net"
```
- Typing **`ci"`** yields: `database_host = ""` (with cursor inside quotes in Insert mode).
- Typing **`ca"`** yields: `database_host = ` (removes the quotes entirely).
- Typing **`ciw`** on `production_cluster` replaces just that word inside the snake_case identifier.

## 4.6 Search & Global Search-and-Replace (Regex Surgery)

### Searching:
- `/pattern`: Search forward for `pattern`. Press `Enter`.
- `?pattern`: Search backward for `pattern`.
- `n`: Jump to next search match in same direction.
- `N`: Jump to previous search match in reverse direction.
- `*`: Search forward for the exact word currently under the cursor.
- `#`: Search backward for the exact word currently under the cursor.
- `:noh` (or `:nohlsearch`): Turn off bright search highlighting after searching.

### Global Search and Replace (`:s` Command):

$$\text{Syntax: } :[\text{range}]\text{s}/[\text{pattern}]/[\text{replacement}]/[\text{flags}]$$

| Command | Scope & Meaning |
| :--- | :--- |
| `:s/foo/bar/` | Replace first occurrence of `foo` with `bar` on **current line only** |
| `:s/foo/bar/g` | Replace all occurrences of `foo` with `bar` on **current line only** |
| `:%s/foo/bar/g` | Replace all occurrences of `foo` with `bar` throughout the **entire file** (`%`) |
| `:%s/foo/bar/gc` | Replace all throughout file, but **prompt for confirmation** (`c`) on each match |
| `:%s/\<foo\>/bar/g` | Replace only exact full word `foo` (matches `foo`, ignores `foobar` or `myfoo`) |
| `:10,30s/foo/bar/g` | Replace all occurrences only between lines 10 and 30 |
| `:%s/http:\/\/old/https:\/\/new/g` | Using alternate delimiter to avoid escaping slashes: `:%s#http://old#https://new#g` |

## 4.7 Visual Mode & Multi-Line Column Editing (`Ctrl + v`)

One of the most powerful features in Vim is **Visual Block Mode** (`Ctrl + v`). It allows you to edit 100 lines simultaneously in parallel columns.

### Killer Scenario: Commenting Out 50 Lines in YAML / Nginx:
1. Place cursor on the first character of the first line to comment.
2. Press **`Ctrl + v`** (bottom left shows `-- VISUAL BLOCK --`).
3. Press **`j`** (or `50j`) to move down and highlight the first column across all 50 lines.
4. Press **`Shift + i`** (Capital `I` for Insert at start of block).
5. Type **`# `** (hash and space). *Note: It will only appear on the first line while typing; this is normal!*
6. Press **`Esc`**.
7. **BAM!** The `# ` is instantly stamped onto all 50 lines simultaneously.

### Killer Scenario: Deleting Indentation / Comments on 50 Lines:
1. Move to column 1.
2. Press **`Ctrl + v`**.
3. Move `j` down 50 lines, and `l` right 2 columns (to select `# `).
4. Press **`d`** or **`x`**. All 50 comments disappear instantly.

## 4.8 Splits, Tabs, and Buffers

Vim allows you to work across multiple files in a single terminal session:

### Viewport Splits:
- `:split` (or `:sp`): Split window horizontally.
- `:vsplit` (or `:vsp`): Split window vertically.
- `:vsplit filename.txt`: Split vertically and open `filename.txt` in the new window.
- `Ctrl + w then h / j / k / l`: Navigate between split windows (Left / Down / Up / Right).
- `Ctrl + w then =`: Equalize dimensions of all open split windows.
- `Ctrl + w then q`: Close current split window.

### Buffers (The Internal Tab Engine):
- `:e filename.txt`: Open `filename.txt` into a buffer.
- `:ls` (or `:buffers`): List all loaded buffers in memory.
- `:bnext` (or `:bn`): Switch to next buffer.
- `:bprev` (or `:bp`): Switch to previous buffer.
- `:b 3` or `:b config`: Jump directly to buffer number 3 or buffer matching "config".
- `:bd`: Delete current buffer (close file without quitting Vim).

## 4.9 Production SRE & DevOps Tricks

### 1. Sudo Save Trick (Opened file without `sudo`):
Have you ever edited `/etc/nginx/nginx.conf` for 20 minutes, only to see `:w` output: `E212: Can't open file for writing`?

Instead of discarding your work:
```vim
:w !sudo tee % > /dev/null
```
- `:w !`: Pipe buffer contents to an external shell command.
- `sudo tee %`: Run `tee` with superuser privileges, writing to `%` (which represents current filename).
- Vim will prompt you: `File has been modified, press L to reload`. Press `L` or `Enter`. You're done!

### 2. The Auto-Indent Paste Staircase Disaster:
When pasting code into Vim over an SSH connection, Vim interprets indentation as keyboard strokes, creating a diagonal staircase of tabs:

```
function test() {
    var x = 1;
        var y = 2;
            var z = 3;
```
**The Solution:**
```vim
:set paste
```
Paste your code, then turn it off:
```vim
:set nopaste
```
*(Or configure `set pastetoggle=<F2>` in your `.vimrc` to toggle with one key).*

### 3. Stripping Windows `^M` Carriage Returns:
When scripts written on Windows are brought into Linux, they fail with `: bad interpreter: No such file or directory` due to CRLF `\r\n` line breaks:
```vim
:%s/\r$//g
```
Or simply set the file format:
```vim
:set ff=unix
:wq
```

### 4. Reading Shell Command Output Directly into Vim:
Need to paste current server IP, timestamp, or directory tree into your notes?
```vim
:r !date
:r !curl -s https://api.ipify.org
:r !kubectl get pods -n kube-system
```
The output of the command is instantly inserted below your current line!

## 4.10 Production Hardening Configuration (`~/.vimrc`)

Place this `.vimrc` on every production bastion host and developer box:

```vim
" ~/.vimrc - Production DevOps & SRE Configuration

" Disable legacy Vi compatibility mode (Enables modern Vim features)
set nocompatible

" Show hybrid line numbers (Current line absolute, others relative for fast jumping)
set number
set relativenumber

" Enable syntax highlighting
syntax on

" Tab & Space Discipline (2 spaces for YAML, Kubernetes, DevOps files)
set tabstop=2
set shiftwidth=2
set softtabstop=2
set expandtab
set autoindent
set smartindent

" Search Configuration
set incsearch        " Highlight matches as you type
set hlsearch         " Highlight all search matches
set ignorecase       " Case-insensitive search...
set smartcase        " ...unless an uppercase letter is typed!

" Visual Feedback & Formatting
set showcmd          " Display incomplete commands in bottom right
set showmode         " Display current mode (-- INSERT --)
set ruler            " Show cursor row and column position
set wildmenu         " Visual autocomplete menu for Ex commands
set cursorline       " Highlight current line horizontally

" Paste toggle shortcut (Press F2 to toggle paste mode cleanly)
set pastetoggle=<F2>

" Backspace behavior across lines, indentations, and insert start
set backspace=indent,eol,start

" Buffer safety
set hidden           " Allow switching buffers without saving first
set nobackup         " Do not write backup files (prevents cluttering git repos)
set noswapfile       " Avoid .swp files in ephemeral cloud containers
```

---

# 5. Production War Room Incidents & RCAs

### Incident 1: The Nano Auto-Wrap SSH Key Disaster

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ TIME: 14:22 UTC | SEVERITY: SEV-1 | OUTAGE DURATION: 184 MINUTES            │
│ SYSTEM: CI/CD Deployment Bastion & Kubernetes Production Cluster            │
├─────────────────────────────────────────────────────────────────────────────┤
│ ROOT CAUSE:                                                                 │
│ A junior DevOps engineer was updating the deployer private SSH key in       │
│ /root/.ssh/id_ed25519 using stock `nano`. Nano had default auto-wrap        │
│ enabled (80-character limit). When the base64-encoded key was pasted,       │
│ nano silently wrapped the 68-character block across multiple lines,         │
│ injecting hard newlines. The cryptographic parser failed with:              │
│ `Load key "/root/.ssh/id_ed25519": invalid format`.                         │
│ Every deployment pipeline globally was halted.                              │
├─────────────────────────────────────────────────────────────────────────────┤
│ LESSON LEARNED:                                                             │
│ 1. Never use nano on cryptographic keys without `nano -w` (no wrap).        │
│ 2. Enforce `set nowrap` in global `/etc/nanorc`.                            │
│ 3. Validate keys using `ssh-keygen -y -f <key>` prior to leaving server.   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### Incident 2: The Dreaded Vim Swap File Lockout (`E325: ATTENTION`)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ SCENARIO:                                                                   │
│ An engineer is editing `/etc/hosts` over SSH. The VPN drops unexpectedly.   │
│ The SSH session disconnects, leaving Vim running in background with an open │
│ `.hosts.swp` file lock. When reconnecting and opening the file again:       │
│                                                                             │
│   E325: ATTENTION                                                           │
│   Found a swap file by the name "/etc/.hosts.swp"                           │
│   owned by: root   dated: Wed Sep  6 13:00:00 2026                          │
│   [O]pen Read-Only, (E)dit anyway, (R)ecover, (D)elete it, (Q)uit, (A)bort: │
├─────────────────────────────────────────────────────────────────────────────┤
│ PRODUCTION RESOLUTION PROCEDURE:                                            │
│ 1. Do NOT blindly press (D)elete immediately!                               │
│ 2. Press (R) to RECOVER unsaved changes from the swap buffer.               │
│ 3. Save the recovered buffer to a temporary file: `:w /tmp/hosts.recovered` │
│ 4. Quit Vim: `:q`                                                           │
│ 5. Diff the recovered file against the original:                            │
│    `diff -u /etc/hosts /tmp/hosts.recovered`                                │
│ 6. Once confirmed, safely remove the hidden swap lock:                      │
│    `rm -f /etc/.hosts.swp`                                                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### Incident 3: The Staircase Cascade Paste Bug in Kubernetes YAML

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ SCENARIO:                                                                   │
│ During a hotfix for an ingress controller, an engineer pasted a 40-line     │
│ Kubernetes Deployment manifest into Vim over a high-latency satellite SSH   │
│ connection without `:set paste`.                                            │
│                                                                             │
│ Because `autoindent` was active, each line compounded the indent of the     │
│ previous line. By line 30, the indentation was 120 spaces deep.             │
│ The engineer saved (`:wq`) and ran `kubectl apply -f deploy.yaml`.          │
│ YAML parser error: `error: error parsing deploy.yaml: error converting YAML │
│ to JSON: yaml: line 18: mapping values are not allowed in this context`.    │
├─────────────────────────────────────────────────────────────────────────────┤
│ PREVENTION:                                                                 │
│ Always activate `:set paste` before pasting external buffers, or use       │
│ command-line cat redirection: `cat << 'EOF' > deploy.yaml ... EOF`          │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

# 6. Senior DevOps & SRE Interview Bank (25 Questions & Explanations)

#### Q1: Why is `vi` present on almost every minimal Linux container (including BusyBox/Alpine), while `vim` and `nano` are frequently missing?
> **Answer**: `vi` is codified in the IEEE POSIX 1003.1 standard specification for Unix-like operating systems. Minimal container base images (like Alpine Linux) bundle BusyBox, which includes a stripped-down, tiny C implementation of POSIX `vi` (~30KB binary). `vim` and `nano` are external optional packages with dynamic shared library dependencies (like `libncurses` and `libtinfo`) that increase attack surface and image size.

#### Q2: What is the difference between `i`, `a`, `I`, `A`, `o`, and `O` in Vim?
> **Answer**: All 6 enter Insert Mode from Normal Mode, but position the insertion point differently:
> - `i`: Before cursor.
> - `a`: After cursor.
> - `I`: At the first non-blank character of the current line.
> - `A`: At the very end of the current line.
> - `o`: Opens a new line **below** the current line.
> - `O`: Opens a new line **above** the current line.

#### Q3: How do you save a file in Vim when you forgot to open it with `sudo`?
> **Answer**: `:w !sudo tee % > /dev/null`. This writes the current buffer into the stdin of the external `sudo tee` utility, which writes to `%` (the path of the active file), bypassing the read-only file descriptor of the unprivileged Vim process.

#### Q4: Explain the difference between `yy` and `"0p` versus `p`.
> **Answer**: In Vim, `p` puts the contents of the default unnamed register (`""`). However, whenever you delete text with `dd`, `dw`, or `x`, Vim also puts the deleted text into `""`. If you yank a line with `yy`, then delete a word with `dw`, pressing `p` will paste the deleted word, not the yanked line! Register `0` (`"0`) always retains the *most recently yanked text*. Therefore, `"0p` guarantees you paste what you copied, ignoring subsequent deletions.

#### Q5: How do you perform a search-and-replace across the entire file for an exact word without affecting substrings?
> **Answer**: `:%s/\<target\>/replacement/g`. The `\<` and `\>` escape sequences are Vim regex word boundaries (equivalent to `\b` in PCRE), ensuring `target` matches but `target_cluster` or `pretarget` are ignored.

#### Q6: How does Visual Block Mode (`Ctrl + v`) differ from regular Visual Mode (`v`) and Visual Line Mode (`V`)?
> **Answer**:
> - `v` selects text character-by-character across lines.
> - `V` selects whole lines vertically.
> - `Ctrl + v` selects a 2-dimensional rectangular column block of characters. It allows simultaneous column insertions (`Shift + i`), column deletions (`d` or `x`), and column text replacements.

#### Q7: In Nano, what is the danger of editing configuration files without the `-w` flag?
> **Answer**: Without `-w` (`--nowrap`), Nano automatically wraps long lines at the screen boundary by inserting hard carriage return / newline characters. This breaks long single-line tokens like SSH private keys, certificates, base64 blobs, and long environment variable definitions.

#### Q8: What does `:set paste` do in Vim, and why is it essential during SSH operations?
> **Answer**: When typing interactively, Vim features like `autoindent`, `smartindent`, and abbreviation expansion make editing faster. However, when pasting code over SSH, the terminal sends characters as a stream of keypresses. `autoindent` triggers on every newline, multiplying the indentation of each subsequent line into a "staircase effect." `:set paste` disables all indentation intelligence, allowing raw text to be inserted exactly as formatted.

#### Q9: What happens when an engineer accidentally presses `Ctrl + s` in Vim or Nano inside an SSH session?
> **Answer**: `Ctrl + s` triggers legacy software flow control (XOFF), which instructs the terminal emulator to stop receiving input. The terminal appears completely frozen. To unfreeze it, the engineer must press **`Ctrl + q`** (XON), which resumes transmission.

#### Q10: How do you format or indent an entire file automatically in Vim?
> **Answer**: `gg=G`.
> - `gg`: Go to line 1.
> - `=`: Format/indent operator.
> - `G`: Motion to the end of the file.

#### Q11: Explain how to comment out 100 lines of code in Vim using Visual Block Mode.
> **Answer**:
> 1. Move to line 1 of the block.
> 2. Press `Ctrl + v`.
> 3. Press `99j` to select the first column of all 100 lines.
> 4. Press `Shift + i`.
> 5. Type `# ` (or `// `).
> 6. Press `Esc`.

#### Q12: How do you view line numbers in Nano temporarily if they are not enabled in `.nanorc`?
> **Answer**: Press `Alt + N` (or `M-N` on Meta). This toggles line numbers on and off dynamically.

#### Q13: What is the meaning of `ci(` in Vim?
> **Answer**: "Change Inside Parentheses". It deletes all text between the surrounding `(` and `)` brackets and places the cursor in Insert Mode between them.

#### Q14: How do you search for the word currently under the cursor in Vim without retyping it?
> **Answer**: Press `*` to search forward for the word under the cursor, or `#` to search backward.

#### Q15: In Vim, what is the difference between `:wq` and `:x` and `ZZ`?
> **Answer**: `:wq` writes the file to disk unconditionally and exits. `:x` and `ZZ` only write to disk if the buffer was actually modified, preserving the file's original last-modified timestamp (`mtime`) if no changes were made.

#### Q16: How do you delete all empty lines in a file in Vim using Ex commands?
> **Answer**: `:g/^$/d`.
> - `:g`: Global command.
> - `/^$/`: Pattern matching lines with beginning of line `^` immediately followed by end of line `$`.
> - `d`: Delete matching lines.

#### Q17: How can you insert the output of an external command like `kubectl get nodes` directly into the current Vim buffer?
> **Answer**: `:r !kubectl get nodes`.

#### Q18: What is the function of the swap file (`.filename.swp`) in Vim?
> **Answer**: Vim writes uncommitted buffer edits to a hidden `.swp` file periodically. If the system crashes or the SSH session drops, the swap file preserves unsaved changes and prevents two users from opening the same file concurrently for writing.

#### Q19: How do you split a window vertically in Vim and switch to the new window?
> **Answer**: `:vsp <filename>` to split vertically and load the file, followed by `Ctrl + w then l` (or `Ctrl + w w`) to switch focus to the right-hand split.

#### Q20: How do you convert all tabs to spaces in an existing file in Vim?
> **Answer**:
> ```vim
> :set expandtab
> :retab
> ```

#### Q21: How do you open a file directly at line 150 in Vim from the Linux bash terminal?
> **Answer**: `vim +150 filename.py` (or `nano +150 filename.py`).

#### Q22: What is the purpose of `set relativenumber` in `.vimrc`?
> **Answer**: It displays line numbers relative to the current cursor line. This allows you to immediately see how many lines away a target is (e.g., if target is 14 lines below, you immediately type `14j` or `d14j` without doing mental arithmetic).

#### Q23: How do you record and execute a macro in Vim?
> **Answer**:
> - Press `qa` to start recording into register `a`.
> - Perform your editing steps.
> - Press `q` to stop recording.
> - Press `@a` to replay the macro once, or `100@a` to replay it 100 times.

#### Q24: How do you copy text from Vim into the operating system clipboard?
> **Answer**: Visually select the text (`v`), then type `"+y` (using the `+` clipboard register).

#### Q25: How do you discard all changes and reload the original file from disk in Vim?
> **Answer**: `:e!`.

---

# 7. Bonus Engineering Note: Resolving Rust `ring v0.17.14` Windows GCC Build Failures

### The Problem
During automated CI/CD builds (e.g., GitHub Actions Windows runners `actions-runner\_work\...`), compiling crates that depend on `ring v0.17.14` fails with:

```text
error: failed to run custom build command for `ring v0.17.14`
Caused by:
process didn't exit successfully: `...\ring-d72078008ea44400\build-script-build` (exit code: 1)
--- stdout
cargo:warning=Compiler family detection failed due to error: ToolExecError: command did not execute successfully (status code exit code: 1): "gcc.exe" "-E" "...detect_compiler_family.c"
--- stderr
error occurred in cc-rs: command did not execute successfully (status code exit code: 1): "gcc.exe" "-Os" ... "curve25519.c"
```

### Root Cause
1. `ring` compiles low-level C and assembly crypto code (`curve25519.c`, `aes`, etc.) using `cc-rs`.
2. On Windows runners targeting the GNU ABI (`x86_64-pc-windows-gnu`), `cc-rs` looks for `gcc.exe`. If `gcc.exe` (MinGW / Rtools) is missing, not in `PATH`, or has an ABI version mismatch with Cargo, compilation aborts.
3. On Windows runners where MSVC (`x86_64-pc-windows-msvc`) is the default toolchain, having a stray `gcc.exe` in `PATH` (e.g., from Git Bash or Strawberry Perl) causes Cargo to erroneously detect GNU rather than MSVC.

### The Fixes

#### Fix 1: Switch Target to MSVC (Recommended for Windows CI)
Ensure your GitHub Actions workflow or local environment targets the native MSVC toolchain:
```yaml
# In GitHub Actions Workflow (.github/workflows/build.yml)
- name: Install Rust toolchain
  uses: dtolnay/rust-toolchain@stable
  with:
    targets: x86_64-pc-windows-msvc

- name: Build
  run: cargo build --release --target x86_64-pc-windows-msvc
```

#### Fix 2: If GNU ABI is Mandatory, Install MinGW Properly
If your project strictly requires `x86_64-pc-windows-gnu`:
```yaml
- name: Setup MinGW
  uses: egor-tensin/setup-mingw@v2
  with:
    platform: x64

- name: Build with GNU
  run: cargo build --release --target x86_64-pc-windows-gnu
```

#### Fix 3: Clean Stale GCC Environment Variables
In your runner step or PowerShell before calling `cargo`:
```powershell
$env:CC = ""
$env:CC_x86_64_pc_windows_gnu = ""
$env:CFLAGS = ""
```
