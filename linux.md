[? Back to Home](README.md)

# 🐧 Linux Mastery: Part 1 - The Core Foundation

This section covers the essential commands for navigating the filesystem, managing files, and viewing data. These are the most frequently used commands in any Linux environment.

---

## 📂 1. Navigation & Pathfinding

### **`pwd` (Print Working Directory)**
Shows the full path of the directory you are currently in.
* **Options:**
    * `-L`: Display the logical current working directory (includes symlinks).
    * `-P`: Display the physical current working directory (avoids symlinks).
* **Example:**
    ```bash
    pwd
    # Result: /home/username/projects/api-client
    ```

### **`ls` (List Contents)**
Lists files and directories within a specific path.
* **Options:**
    * `-a`: Show hidden files (those starting with `.`).
    * `-l`: Long listing format (shows permissions, owner, size, and date).
    * `-h`: Human-readable (converts bytes to KB, MB, GB).
    * `-S`: Sort by file size (largest first).
    * `-t`: Sort by modification time (newest first).
    * `-R`: Recursive (lists all subdirectories too).
* **Example:**
    ```bash
    ls -lahS  # List all files, human-readable, sorted by size
    ```

### **`cd` (Change Directory)**
Moves you between folders.
* **Shortcuts:**
    * `cd ~`: Jump to your Home directory.
    * `cd ..`: Move up one level.
    * `cd -`: Jump back to the last directory you were in.
    * `cd /`: Go to the Root directory.
* **Example:**
    ```bash
    cd ~/Documents/work
    ```

---

## 📄 2. File & Directory Creation

### **`mkdir` (Make Directory)**
Creates one or more new folders.
* **Options:**
    * `-p`: Create parent directories (no error if existing, make intermediate folders).
    * `-v`: Verbose (display a message for every directory created).
    * `-m`: Set file mode (permissions) at the time of creation.
* **Example:**
    ```bash
    mkdir -pv project/src/components
    ```

### **`touch` (Create/Update File)**
Creates an empty file or updates the access/modification time of an existing file.
* **Options:**
    * `-a`: Change only the access time.
    * `-m`: Change only the modification time.
    * `-c`: Do not create any files if they don't exist.
* **Example:**
    ```bash
    touch README.md app.js .env
    ```

---

## ✂️ 3. Moving, Copying, and Deletion

### **`cp` (Copy)**
Copies files or directories from source to destination.
* **Options:**
    * `-r`: Recursive (Required to copy directories).
    * `-i`: Interactive (Prompt before overwriting).
    * `-u`: Update (Copy only when source is newer than destination).
    * `-v`: Verbose (Show what is being copied).
    * `-p`: Preserve (Keep file attributes like timestamps and permissions).
* **Example:**
    ```bash
    cp -rv ~/Downloads/logo.png ./assets/
    ```

### **`mv` (Move / Rename)**
Moves files or renames them if the path remains the same.
* **Options:**
    * `-i`: Interactive (Prompt before overwriting).
    * `-f`: Force (Overwrite without asking).
    * `-v`: Verbose.
* **Example:**
    ```bash
    mv old_name.txt new_name.txt  # Renaming
    mv script.py ./scripts/       # Moving
    ```

### **`rm` (Remove)**
Deletes files or directories permanently. **Note: No Undo!**
* **Options:**
    * `-r`: Recursive (Required to delete folders).
    * `-f`: Force (Ignore non-existent files, never prompt).
    * `-i`: Interactive (Prompt before every removal).
    * `-v`: Verbose.
* **Example:**
    ```bash
    rm -rf ./node_modules  # The "Nuclear" delete for folders
    ```

---

## 👁️ 4. Viewing & Inspecting Files

### **`cat` (Concatenate)**
Prints the entire content of a file to the screen.
* **Options:**
    * `-n`: Number all output lines.
    * `-b`: Number non-blank lines only.
    * `-s`: Squeeze multiple blank lines into one.
* **Example:**
    ```bash
    cat -n package.json
    ```

### **`head` & `tail` (Peek at files)**
View the top or bottom portion of a file.
* **Options:**
    * `-n [number]`: Specify the number of lines (default is 10).
    * `-f` (Tail only): Follow the file (live updates as the file grows).
* **Example:**
    ```bash
    tail -f /var/log/syslog  # Monitor system logs in real-time
    head -n 20 index.html    # See the first 20 lines
    ```

### **`less` (Interactive Viewer)**
Opens a file for reading with navigation. Better than `cat` for large files.
* **Controls:**
    * `Space`: Page down.
    * `b`: Page up.
    * `/pattern`: Search for a word.
    * `q`: Quit.
* **Example:**
    ```bash
    less long_documentation.txt
    ```

---
# 🐧 Linux Mastery: Part 2 - Permissions & Ownership

This section covers how Linux handles security, who can access files, and how to change those rules.

---

## 1. Understanding Permissions (The Basics)
In Linux, every file and directory has three types of permissions for three types of users:
- **Users:** Owner (u), Group (g), Others (o)
- **Permissions:** Read (r=4), Write (w=2), Execute (x=1)



---

## 2. Permission Commands

### **`chmod` (Change Mode)**
Changes the access permissions of a file or folder.
* **Options:**
    * `-R`: Recursive (apply to all files inside a folder).
* **Examples:**
    * **Symbolic:** `chmod u+x script.sh` (Give the owner **execute** permission).
    * **Numeric:** `chmod 755 folder_name` (Owner: rwx, Group: r-x, Others: r-x).
    * **Full Access:** `chmod 777 file.txt` (Everyone can do everything—use with caution!).

### **`chown` (Change Owner)**
Changes the user and/or group ownership of a file.
* **Options:**
    * `-R`: Recursive.
    * `-v`: Verbose (tells you what it changed).
* **Examples:**
    * `sudo chown john:developers file.txt` (Changes owner to 'john' and group to 'developers').
    * `sudo chown root script.sh` (Changes only the owner to root).

### **`chgrp` (Change Group)**
Specifically changes the group ownership only.
* **Example:**
    * `chgrp admin data.log` (Changes the group of the log file to 'admin').

---

## 3. User & Group Management

### **`useradd` / `adduser`**
Creates a new user on the system.
* **Options:**
    * `-m`: Create a home directory for the new user.
    * `-s`: Specify the default shell (e.g., `/bin/bash`).
* **Example:**
    * `sudo useradd -m dev_user`

### **`usermod` (User Modify)**
Modifies an existing user account.
* **Options:**
    * `-aG`: Append the user to a specific group (Commonly used for `sudo` or `docker` groups).
* **Example:**
    * `sudo usermod -aG docker john` (Adds user 'john' to the docker group).

### **`userdel` (User Delete)**
Removes a user from the system.
* **Options:**
    * `-r`: Remove the home directory and mail spool along with the user.
* **Example:**
    * `sudo userdel -r old_user`

### **`passwd`**
Changes the password for a user.
* **Example:**
    * `passwd` (Change your own).
    * `sudo passwd username` (Change someone else's).

---

## 4. Administrative Power (Sudo)

### **`sudo` (SuperUser Do)**
Run a command with administrative/root privileges.
* **Options:**
    * `-i`: Start an interactive login shell as root.
    * `-u`: Run a command as a specific user.
* **Example:**
    * `sudo -u www-data php script.php` (Run a script as the web server user).

### **`su` (Substitute User)**
Switch to another user account.
* **Example:**
    * `su - admin` (Switch to the admin user and load their environment).

---

## 5. Security Check Commands

### **`umask`**
Sets the default permissions for newly created files.
* **Example:**
    * `umask 022` (Ensures new files are not writable by others).

### **`groups`**
Shows which groups a user belongs to.
* **Example:**
    * `groups john`

---

## 💡 Quick Reference: Numerical Permission Table
| Number | Permission | Description |
| :--- | :--- | :--- |
| **7** | `rwx` | Read, Write, and Execute |
| **6** | `rw-` | Read and Write |
| **5** | `r-x` | Read and Execute |
| **4** | `r--` | Read Only |
| **0** | `---` | No Permissions |

---

# 🐧 Linux Mastery: Part 2 - Search, Filter & Pipes

This section covers how to find files, search through massive amounts of text, and connect commands to create powerful workflows.

---

## 🔍 1. Finding Files and Text

### **`grep` (Global Regular Expression Print)**
The most powerful tool for searching text within files.
* **Options:**
    * `-i`: Ignore case (search for "Error" and "error").
    * `-r` or `-R`: Recursive (search through all files in a directory).
    * `-v`: Invert match (show lines that do **not** contain the pattern).
    * `-n`: Show line numbers.
    * `-l`: Show only names of files with matching lines.
    * `-c`: Count the number of matches.
* **Example:**
    ```bash
    grep -rn "TODO" ./src  # Find all TODOs in your source code
    ```

### **`find` (Search for Files)**
Searches the filesystem for files and directories based on attributes.
* **Options:**
    * `-name`: Search by filename (supports wildcards like `*.js`).
    * `-type`: `f` for files, `d` for directories.
    * `-size`: Search by size (e.g., `+100M` for over 100MB).
    * `-mtime`: Search by modification time (e.g., `-7` for last 7 days).
    * `-exec`: Run a command on every file found.
* **Example:**
    ```bash
    find . -type f -name "*.log" -delete  # Find and delete all .log files
    ```

### **`locate` (Fast File Search)**
Uses a pre-built database to find files instantly.
* **Note:** Run `sudo updatedb` first to refresh the database.
* **Example:**
    ```bash
    locate config.json
    ```

---

## ⛓️ 2. Redirection & Piping (The Core of Linux)

Linux treats everything as a "stream" of data. You can redirect these streams.



### **Redirection Operators**
* **`>`**: Redirect output to a file (Overwrites existing content).
* **`>>`**: Append output to the end of a file.
* **`<`**: Take input from a file.
* **`2>`**: Redirect only errors (Standard Error).

### **`|` (The Pipe)**
The pipe takes the output of the command on the left and feeds it as input to the command on the right.

* **Examples:**
    ```bash
    # List files and search for 'secret' in the list
    ls -la | grep "secret"

    # Count how many files are in a folder
    ls | wc -l

    # See the top 5 largest files
    du -ah | sort -rh | head -n 5
    ```

---

## 📝 3. Text Manipulation

### **`sort`**
Sorts lines of text alphabetically or numerically.
* **Options:**
    * `-n`: Numeric sort.
    * `-r`: Reverse sort.
    * `-u`: Unique (remove duplicates).
* **Example:**
    ```bash
    sort users.txt
    ```

### **`uniq`**
Filters out adjacent matching lines from a file.
* **Options:**
    * `-c`: Count occurrences.
    * `-d`: Only show duplicate lines.
* **Example:**
    ```bash
    sort log.txt | uniq -c  # Count how many times each log entry appears
    ```

### **`wc` (Word Count)**
Counts lines, words, and bytes.
* **Options:**
    * `-l`: Count lines.
    * `-w`: Count words.
    * `-m`: Count characters.
* **Example:**
    ```bash
    wc -l file.txt
    ```

---

## 🛠️ 4. Advanced Stream Editors

### **`sed` (Stream Editor)**
Used for finding and replacing text in a stream or file.
* **Syntax:** `sed 's/old/new/g' file`
* **Example:**
    ```bash
    # Replace 'localhost' with '127.0.0.1' in a config file
    sed -i 's/localhost/127.0.0.1/g' config.env
    ```

### **`awk` (Pattern Scanning)**
A full scripting language for processing data in columns/fields.
* **Example:**
    ```bash
    # Print only the first column of a space-separated file
    awk '{print $1}' data.txt
    ```

---

# 🐧 Linux Mastery: Part 3 - Processes, Archiving & Networking

This section focuses on how to manage running programs, compress files for storage/transfer, and interact with the network.

---

## 🚀 1. Process Management
Every running program in Linux is a "Process" with a unique ID (PID).

### **`ps` (Process Status)**
Snapshots the currently active processes.
* **Options:**
    * `aux`: Shows every process running on the system by every user.
    * `-ef`: Standard syntax to see full command details.
* **Example:**
    ```bash
    ps aux | grep "node"  # Find all running Node.js processes
    ```

### **`top` / `htop` (Resource Monitor)**
Interactive, real-time view of system resources (CPU, RAM).
* **Shortcut:** Use `htop` if available for a color-coded, easier-to-read interface.
* **Example:**
    ```bash
    htop
    ```

### **`kill` (Terminate Process)**
Sends a signal to a process to stop it.
* **Options:**
    * `-9`: Forced kill (Immediate termination).
    * `-15`: Terminate (Graceful shutdown).
* **Example:**
    ```bash
    kill -9 1234  # Replace 1234 with the actual PID
    ```

### **`bg` & `fg` (Background/Foreground)**
* `bg`: Sends a suspended process to the background.
* `fg`: Brings a background process to the foreground.
* **Shortcut:** `Ctrl + Z` pauses a running process so you can move it.

---

## 📦 2. Archiving & Compression

### **`tar` (Tape Archive)**
The standard tool for "zipping" folders.
* **Options:**
    * `-c`: Create an archive.
    * `-x`: Extract an archive.
    * `-v`: Verbose (show progress).
    * `-f`: Filename (must be the last option before the name).
    * `-z`: Use Gzip compression (`.tar.gz`).
* **Examples:**
    ```bash
    tar -cvzf backup.tar.gz ./my_folder   # Compress
    tar -xvzf backup.tar.gz               # Extract
    ```

### **`zip` / `unzip`**
Standard ZIP utility compatible with Windows.
* **Example:**
    ```bash
    zip -r archive.zip folder/
    unzip archive.zip
    ```

---

## 🌐 3. Networking

### **`ip addr` (IP Address)**
Shows your network interfaces and IP addresses (Replaces the old `ifconfig`).
* **Example:**
    ```bash
    ip addr show
    ```

### **`ping`**
Checks connectivity to a host.
* **Example:**
    ```bash
    ping google.com
    ```

### **`curl` / `wget` (Data Transfer)**
Download files or interact with APIs from the terminal.
* **Options:**
    * `curl -O`: Download file with its original name.
    * `curl -I`: Show only the HTTP headers.
    * `wget -c`: Resume a broken download.
* **Example:**
    ```bash
    curl -X POST [http://api.example.com/data](http://api.example.com/data) -d '{"id": 1}'
    ```

### **`ssh` (Secure Shell)**
Connect to a remote Linux machine securely.
* **Example:**
    ```bash
    ssh username@192.168.1.50
    ```

### **`scp` (Secure Copy)**
Copies files between local and remote machines over SSH.
* **Example:**
    ```bash
    scp local_file.txt user@remote_host:/path/to/destination
    ```

---

## 🛠️ 4. System Information

### **`uptime`**
Shows how long the system has been running and the load average.

### **`history`**
Shows a list of all commands you have typed recently.
* **Pro-Tip:** Use `history | grep "command_name"` to find a specific old command.

### **`alias`**
Creates a shortcut for long commands.
* **Example:**
    ```bash
    alias ll='ls -lah'  # Now typing 'll' runs 'ls -lah'
    ```

---

# 🐧 Linux Mastery: Part 4 - Package Management, Logs & Disks

This section covers how to install software, monitor system health through logs, and manage physical storage.

---

## 📦 1. Package Management (Debian/Ubuntu)
Most modern Linux distributions use a package manager to handle software installation and updates.

### **`apt` (Advanced Package Tool)**
The standard tool for Debian-based systems (like Ubuntu).
* **Commands:**
    * `update`: Refresh the list of available packages.
    * `upgrade`: Install available updates for all packages.
    * `install [package]`: Download and install a new program.
    * `remove`: Uninstall a package.
    * `search`: Look for a package in the repository.
* **Example:**
    ```bash
    sudo apt update && sudo apt install git htop curl
    ```

### **`dpkg` (Debian Package)**
A lower-level tool to install `.deb` files manually.
* **Example:**
    ```bash
    sudo dpkg -i discord.deb
    ```

---

## 📜 2. System Logs & Troubleshooting
When something goes wrong, the logs tell you why.

### **`journalctl`**
The modern way to view logs from the systemd journal.
* **Options:**
    * `-u [service]`: See logs for a specific service (e.g., nginx or docker).
    * `-f`: Follow logs in real-time.
    * `-xe`: Open the end of the log with help text for errors.
* **Example:**
    ```bash
    journalctl -u docker.service -f
    ```

### **`dmesg`**
Prints the message buffer of the kernel. Used for debugging hardware or driver issues (like USB or WiFi failing).
* **Example:**
    ```bash
    dmesg | tail -n 20
    ```

---

## 💾 3. Disk & Filesystem Utilities

### **`df` (Disk Free)**
Shows how much space is left on your hard drives.
* **Options:**
    * `-h`: Human-readable format (GB/MB).
    * `-T`: Show the filesystem type (ext4, xfs, etc.).
* **Example:**
    ```bash
    df -h
    ```

### **`du` (Disk Usage)**
Estimates how much space specific files or folders are taking up.
* **Options:**
    * `-s`: Summary (total size of a folder).
    * `-h`: Human-readable.
    * `--max-depth=1`: Show sizes of folders in the current directory only.
* **Example:**
    ```bash
    du -sh ./node_modules
    ```

### **`mount` / `umount`**
Attaches or detaches a physical drive (like a USB stick) to the filesystem.
* **Example:**
    ```bash
    sudo mount /dev/sdb1 /mnt/usb
    sudo umount /mnt/usb
    ```

### **`lsblk` (List Block Devices)**
Shows all disks and their partitions in a "tree" format.
* **Example:**
    ```bash
    lsblk
    ```



---

## 🛠️ 4. Useful System Maintenance

### **`shutdown` / `reboot`**
Safely power off or restart the machine.
* **Options:**
    * `now`: Execute immediately.
    * `+10`: Execute in 10 minutes.
* **Example:**
    ```bash
    sudo shutdown -h now  # Halt (Turn off)
    sudo reboot           # Restart
    ```

### **`free`**
Displays the amount of free and used memory (RAM) in the system.
* **Example:**
    ```bash
    free -h
    ```

### **`crontab`**
Schedules tasks to run automatically at specific times.
* **Options:**
    * `-e`: Edit your cron jobs.
    * `-l`: List your scheduled jobs.
* **Example:**
    ```bash
    # (Inside the editor) Run a backup every day at midnight:
    0 0 * * * /home/user/scripts/backup.sh
    ```

# 🐧 Linux Mastery: Part 5 - Environment & Scripting

This section covers how to customize your shell environment and automate repetitive tasks using Bash scripts.

---

## 🏗️ 1. Environment Variables
Environment variables are dynamic values that affect the behavior of processes on your system.

### **`export`**
Sets an environment variable for the current session and child processes.
* **Example:**
    ```bash
    export API_KEY="your_secret_key_here"
    echo $API_KEY
    ```

### **`env` / `printenv`**
Lists all currently set environment variables.
* **Example:**
    ```bash
    printenv PATH  # See the directories where Linux looks for executable programs
    ```

### **`source`**
Executes a file in the current shell context. Commonly used to refresh your configuration after editing `.bashrc` or `.zshrc`.
* **Example:**
    ```bash
    source ~/.bashrc
    ```

---

## 📜 2. Shell Scripting Basics
A script is simply a text file containing a list of commands.

### **The Shebang (`#!`)**
Every script should start with a "shebang" to tell Linux which interpreter to use.
* **Example:** `#!/bin/bash`

### **Variables & User Input**
* **`read`**: Pauses the script to get input from the user.
* **Example Script (`hello.sh`):**
    ```bash
    #!/bin/bash
    echo "What is your name?"
    read name
    echo "Hello $name, today is $(date)"
    ```

### **Conditional Logic (If/Else)**
Allows your script to make decisions.
* **Example:**
    ```bash
    if [ -f "config.json" ]; then
        echo "Configuration file exists."
    else
        echo "File missing!"
    fi
    ```



---

## 🔄 3. Loops
Automation often requires doing the same thing to multiple files.

### **`for` Loop**
Iterates over a list of items.
* **Example:**
    ```bash
    # Rename all .txt files to .bak
    for file in *.txt; do
        mv "$file" "${file%.txt}.bak"
    done
    ```

### **`while` Loop**
Runs as long as a condition is true.
* **Example:**
    ```bash
    counter=1
    while [ $counter -le 5 ]; do
        echo "Iteration $counter"
        ((counter++))
    done
    ```

---

## 🔀 4. Advanced I/O Redirection
Beyond simple `>` and `|`, you can control where data flows more precisely.

### **`tee`**
Reads from standard input and writes to both standard output and files (it's like a "T" junction in a pipe).
* **Example:**
    ```bash
    ls -la | tee file_list.txt  # See the list on screen AND save to file
    ```

### **`xargs`**
Builds and executes command lines from standard input.
* **Example:**
    ```bash
    # Find all .log files and delete them using xargs
    find . -name "*.log" | xargs rm
    ```

### **`/dev/null`**
The "Black Hole" of Linux. Send output here to discard it.
* **Example:**
    ```bash
    # Run a command but hide all error messages
    command_name 2> /dev/null
    ```

---

## 🛠️ 5. Helpful Scripting Utilities

### **`sleep`**
Pauses the script for a specified amount of time.
* **Example:** `sleep 5` (Wait 5 seconds).

### **`exit`**
Terminates a script and returns a status code (0 for success, non-zero for error).
* **Example:** `exit 1`

### **`basename` / `dirname`**
Extracts the filename or the directory path from a full string.
* **Example:**
    ```bash
    basename /home/user/test.txt  # Result: test.txt
    dirname /home/user/test.txt   # Result: /home/user
    ```

# 🐧 Linux Mastery: Part 6 - Hardware, Kernel & Security

This section focuses on interacting with the physical hardware, managing the Linux Kernel, and securing your system against unauthorized access.

---

## 💻 1. Hardware Inspection
These commands help you identify exactly what hardware is inside your machine.

### **`lscpu`**
Displays detailed information about the CPU architecture.
* **Details shown:** Number of cores, threads, virtualization support, and cache sizes.
* **Example:**
    ```bash
    lscpu
    ```

### **`lsusb`**
Lists all USB controllers and the devices connected to them.
* **Example:**
    ```bash
    lsusb
    ```

### **`lspci`**
Lists all PCI devices (Graphics cards, Network cards, NVMe drives).
* **Options:**
    - `-v`: Verbose (shows driver information).
* **Example:**
    ```bash
    lspci -nnk | grep -i vga -A3  # See details about your GPU
    ```

### **`lshw` (List Hardware)**
A comprehensive tool that provides a detailed report of all hardware.
* **Options:**
    - `-short`: Display a summary list.
    - `-html`: Generate a hardware report in HTML format.
* **Example:**
    ```bash
    sudo lshw -short
    ```

---

## 🧠 2. Kernel & Modules
The Kernel is the heart of Linux. Modules are "drivers" that can be loaded or unloaded on the fly.

### **`uname`**
Prints system information.
* **Options:**
    - `-a`: All information (Kernel version, hostname, OS).
    - `-r`: Just the kernel release version.
* **Example:**
    ```bash
    uname -a
    ```

### **`lsmod`**
Shows which kernel modules (drivers) are currently loaded.
* **Example:**
    ```bash
    lsmod | grep "nvidia"  # Check if Nvidia drivers are active
    ```

### **`modprobe`**
Intelligently adds or removes modules from the Linux Kernel.
* **Example:**
    ```bash
    sudo modprobe bluetooth  # Load the bluetooth module
    sudo modprobe -r bluetooth  # Remove/Unload it
    ```

---

## 🛡️ 3. Security & Firewalls

### **`ufw` (Uncomplicated Firewall)**
The standard firewall for Ubuntu/Debian.
* **Commands:**
    - `enable / disable`: Turn the firewall on/off.
    - `allow [port]`: Open a specific port.
    - `status`: Check current rules.
* **Example:**
    ```bash
    sudo ufw allow 22/tcp    # Allow SSH
    sudo ufw allow 80/tcp    # Allow HTTP
    sudo ufw enable
    ```

### **`iptables`**
The powerful, lower-level tool that `ufw` sits on top of.
* **Example:**
    ```bash
    sudo iptables -L -n -v  # List all active firewall rules
    ```



### **`fail2ban`**
A service that protects against "brute force" attacks by banning IPs that have too many failed login attempts.
* **Example:**
    ```bash
    sudo fail2ban-client status sshd
    ```

---

## 🔑 4. SSH Hardening (Secure Shell)
The file `/etc/ssh/sshd_config` controls how people can log into your server.

### **`ssh-keygen`**
Generates a pair of public/private keys for password-less (and more secure) login.
* **Example:**
    ```bash
    ssh-keygen -t ed25519 -C "your_email@example.com"
    ```

### **`ssh-copy-id`**
Copies your public key to a remote server.
* **Example:**
    ```bash
    ssh-copy-id username@remote_host
    ```

---

## 🏁 5. System Limits & Quotas

### **`ulimit`**
Provides control over the resources available to the shell and processes started by it.
* **Options:**
    - `-a`: View all current limits.
    - `-n`: Max number of open files (critical for high-performance servers).
* **Example:**
    ```bash
    ulimit -n 4096  # Increase open file limit for the session
    ```

### **`last`**
Shows a list of the last logged-in users. Useful for auditing who has accessed the machine.
* **Example:**
    ```bash
    last -n 10  # See the last 10 logins
    ```

# 🐧 Linux Mastery: Part 7 - Editors, Transfers & Performance

This section focuses on manipulating files directly in the terminal, syncing data across machines, and identifying why a system is running slowly.

---

## ✍️ 1. Terminal Text Editors
When you are on a remote server, you must edit config files via the command line.

### **`nano` (The Beginner Friendly Editor)**
Easy to use with shortcuts listed at the bottom of the screen.
* **Shortcuts:**
    - `Ctrl + O`: Save (Write Out).
    - `Ctrl + X`: Exit.
    - `Ctrl + W`: Search (Where is).
* **Example:**
    ```bash
    nano /etc/hosts
    ```

### **`vim` (The Professional Powerhouse)**
Extremely fast once you learn the "modes."
* **Modes:**
    - `i`: Insert mode (to type).
    - `Esc`: Normal mode (to run commands).
    - `:w`: Save.
    - `:q!`: Quit without saving.
* **Example:**
    ```bash
    vim configuration.conf
    ```



---

## 📡 2. Advanced File Transfers

### **`rsync` (Remote Sync)**
The gold standard for backups and transfers. It only copies the *differences* between files, making it incredibly fast.
* **Options:**
    - `-a`: Archive mode (preserves permissions and symlinks).
    - `-v`: Verbose.
    - `-z`: Compress data during transfer.
    - `-P`: Show progress bar.
    - `--delete`: Delete files in destination that no longer exist in source.
* **Example:**
    ```bash
    rsync -avzP ./local_folder/ user@remote_host:/backup/
    ```

### **`wget` (The Non-Interactive Downloader)**
Great for downloading files from the web via scripts.
* **Options:**
    - `-r`: Recursive download (download a whole website).
    - `-c`: Resume a partially downloaded file.
    - `-b`: Run in the background.
* **Example:**
    ```bash
    wget -c [https://example.com/large-dataset.zip](https://example.com/large-dataset.zip)
    ```

---

## 🛠️ 3. Performance Troubleshooting
Use these when your application or server feels "laggy."

### **`iostat` (I/O Statistics)**
Shows if your Hard Drive/SSD is the bottleneck.
* **Example:**
    ```bash
    iostat -xz 1  # Show extended disk stats every 1 second
    ```

### **`vmstat` (Virtual Memory Statistics)**
Reports information about processes, memory, paging, block IO, traps, and cpu activity.
* **Example:**
    ```bash
    vmstat 2 5  # Report every 2 seconds, 5 times
    ```

### **`strace` (System Trace)**
The "ultimate" debugger. It shows every system call a program makes to the Linux kernel.
* **Example:**
    ```bash
    strace -p 1234  # See what process 1234 is doing in real-time
    ```

### **`lsof` (List Open Files)**
In Linux, "Everything is a file." This shows which files/ports are being used by which programs.
* **Example:**
    ```bash
    lsof -i :8080  # See what is running on port 8080
    ```



---

## 🔍 4. System Integrity & Comparison

### **`diff` (Difference)**
Compares two files line by line.
* **Options:**
    - `-u`: Unified format (easier to read).
    - `-y`: Side-by-side comparison.
* **Example:**
    ```bash
    diff -u old_config.conf new_config.conf
    ```

### **`sha256sum`**
Calculates a unique "fingerprint" for a file. Use this to verify that a file wasn't corrupted or tampered with.
* **Example:**
    ```bash
    sha256sum ubuntu-iso.dmg
    ```

### **`watch`**
Runs any command repeatedly at a set interval and highlights the changes.
* **Example:**
    ```bash
    watch -n 1 "df -h"  # Monitor disk space changes every second
    ```
# 🐧 Linux Mastery: Part 8 - Storage Architecture & Shell Styling

This section covers how Linux manages disks at an enterprise level and how to customize your terminal to be more productive.

---

## 🏗️ 1. Logical Volume Management (LVM)
LVM allows you to resize partitions on the fly without rebooting. It treats physical disks as a "pool" of storage.

### **`pvdisplay` / `pvcreate`**
Manages **Physical Volumes** (the actual hard drives).
* **Example:**
    ```bash
    sudo pvcreate /dev/sdb  # Initialize a new disk for LVM
    ```

### **`vgdisplay` / `vgextend`**
Manages **Volume Groups** (combining multiple disks into one big pool).
* **Example:**
    ```bash
    sudo vgextend main_vg /dev/sdb  # Add a new disk to your existing pool
    ```

### **`lvdisplay` / `lvextend`**
Manages **Logical Volumes** (the "virtual" partitions you actually format and use).
* **Example:**
    ```bash
    sudo lvextend -L +10G /dev/main_vg/root_lv  # Add 10GB to your root partition
    ```

[Image of LVM architecture layers: PV, VG, LV]

---

## 🗜️ 2. High-Ratio Compression
While `tar` and `gzip` are common, these tools provide much higher compression for huge backups.

### **`bzip2` / `bunzip2`**
Slower than gzip, but creates smaller files.
* **Example:**
    ```bash
    bzip2 large_log.txt  # Creates large_log.txt.bz2
    ```

### **`xz` / `unxz`**
The current industry standard for the highest compression ratio (used for Linux Kernel source code).
* **Example:**
    ```bash
    xz -9 file.tar       # Compresses with maximum effort
    xz -d file.tar.xz    # Decompress
    ```

---

## 🎨 3. Shell Customization (Zsh & Bash)
Your shell is your home. You can make it faster and more informative.

### **`chsh` (Change Shell)**
Changes the default shell for your user account.
* **Example:**
    ```bash
    chsh -s /usr/bin/zsh  # Switch from Bash to Zsh
    ```

### **`alias` (Permanent Shortcuts)**
To make aliases permanent, add them to your `~/.bashrc` or `~/.zshrc`.
* **Pro-Tip Aliases:**
    ```bash
    alias update='sudo apt update && sudo apt upgrade'
    alias ..='cd ..'
    alias mkdir='mkdir -pv'
    ```

### **`echo $SHELL`**
Identify which shell you are currently using.

---

## 🖥️ 4. Terminal Multiplexers
These allow you to keep sessions running even if your internet disconnects.

### **`tmux`**
Allows you to split your terminal into multiple windows and panes.
* **Commands:**
    - `tmux new -s session_name`: Create a new session.
    - `tmux attach -t session_name`: Reconnect to a session.
    - `Ctrl+b` then `%`: Split screen vertically.
* **Example:**
    ```bash
    tmux
    ```

### **`screen`**
An older but simpler version of tmux.
* **Example:**
    ```bash
    screen -S backup_run  # Start a session named backup_run
    # Press Ctrl+A then D to detach
    ```

---

## 🛠️ 5. Advanced File Manipulation

### **`split`**
Breaks a large file into smaller pieces (useful for transferring files over size limits).
* **Options:**
    - `-b`: Split by size (e.g., 100M).
* **Example:**
    ```bash
    split -b 500M large_iso.iso part_
    ```

### **`truncate`**
Shrink or extend the size of a file to a specified size. Very useful for clearing logs without deleting the file.
* **Example:**
    ```bash
    truncate -s 0 access.log  # Instantlly wipes the file content to 0 bytes
    ```

### **`comm`**
Compares two sorted files and shows lines unique to each.
* **Example:**
    ```bash
    comm file1.txt file2.txt
    ```
# 🐧 Linux Mastery: Part 9 - Performance Tuning & Kernel Control

This section focuses on managing how the system allocates resources to specific tasks and how to tweak the kernel without rebooting.

---

## ⚖️ 1. Process Priorities (Nice & Renice)
Linux uses a "Niceness" scale from **-20** (Highest priority) to **19** (Lowest priority). Default is 0.

### **`nice`**
Starts a new process with a specific priority.
* **Example:**
    ```bash
    nice -n 10 python3 heavy_script.py  # Run with lower priority to keep system responsive
    ```

### **`renice`**
Changes the priority of an already running process.
* **Options:**
    * `-p`: Process ID (PID).
    * `-u`: User name (changes all processes for that user).
* **Example:**
    ```bash
    sudo renice -n -5 -p 1234  # Give process 1234 a high priority boost
    ```

### **`ionice`**
Sets the I/O scheduling class and priority for a program (tells the hard drive which program is most important).
* **Example:**
    ```bash
    sudo ionice -c 3 -p 1234  # Set process 1234 to "Idle" (only uses disk when nothing else needs it)
    ```

---

## 🛠️ 2. Kernel Tuning with `sysctl`
The `/proc/sys` directory contains files that control kernel behavior. `sysctl` allows you to modify these "on the fly."

### **`sysctl`**
Configures kernel parameters at runtime.
* **Options:**
    * `-a`: Display all available kernel variables.
    * `-w`: Write a new value.
    * `-p`: Load settings from `/etc/sysctl.conf`.
* **Examples:**
    ```bash
    sudo sysctl -w net.ipv4.ip_forward=1        # Enable IP forwarding (routing)
    sudo sysctl -w vm.swappiness=10             # Make the system use RAM more and Swap less
    ```

---

## 📊 3. Resource Monitoring Tools

### **`sar` (System Activity Reporter)**
The "Black Box" for Linux. It collects and reports system activity over time.
* **Example:**
    ```bash
    sar -u 1 5  # Report CPU usage every 1 second, 5 times
    ```

### **`nload` / `nethogs`**
Visualizes network traffic. `nethogs` specifically shows which *process* is using the bandwidth.
* **Example:**
    ```bash
    sudo nethogs eth0
    ```



---

## 🧩 4. Memory & Swap Management

### **`swapon` / `swapoff`**
Enable or disable swap devices and files.
* **Example:**
    ```bash
    sudo swapon --show          # See active swap space
    sudo swapoff -a             # Disable all swap (use with caution!)
    ```

### **`sync`**
Flushes file system buffers. It forces any data "waiting" to be written to the disk to be saved immediately.
* **Example:**
    ```bash
    sync; sudo reboot           # Ensure all data is saved before restarting
    ```

---

## 🚀 5. Power & Execution Shortcuts

### **`nohup`**
Runs a command that will keep running even after you log out of the terminal.
* **Example:**
    ```bash
    nohup ./long_running_script.sh &
    ```

### **`disown`**
Removes a background job from the shell's job list so it doesn't get killed when the shell closes.
* **Example:**
    ```bash
    ./script.sh &
    disown
    ```

### **`time`**
Measures how long a command takes to execute (Real, User, and System time).
* **Example:**
    ```bash
    time grep "search_term" large_file.txt
    ```

---

## 🧪 6. Special File Systems

### **`/dev/shm` (Shared Memory)**
This is a virtual folder that stores files directly in your RAM. It is incredibly fast.
* **Example:**
    ```bash
    cp large_database.db /dev/shm/  # Access the DB at RAM speeds
    ```
# 🐧 Linux Mastery: Part 10 - Advanced Networking & Security

This section covers diagnostic tools for the web, managing encryption certificates, and using regular expressions for complex text manipulation.

---

## 🌐 1. Advanced Network Diagnostics

### **`dig` (Domain Information Groper)**
The professional tool for DNS lookups.
* **Options:**
    * `+short`: Provides just the IP address.
    * `ANY`: Shows all DNS records (MX, TXT, A, etc.).
* **Example:**
    ```bash
    dig google.com MX +short  # Find the mail servers for Google
    ```

### **`nslookup`**
An older but common tool for querying Internet name servers.
* **Example:**
    ```bash
    nslookup 8.8.8.8  # Reverse DNS lookup (find domain from IP)
    ```

### **`netstat` / `ss`**
Displays network connections, routing tables, and interface statistics. `ss` is the modern, faster replacement for `netstat`.
* **Options:**
    * `-t`: Show TCP sockets.
    * `-u`: Show UDP sockets.
    * `-l`: Show listening sockets.
    * `-p`: Show the process using the socket.
    * `-n`: Show numerical addresses (no DNS lookup).
* **Example:**
    ```bash
    sudo ss -tulpn  # See every program listening on a port
    ```

### **`traceroute` / `mtr`**
Shows the path packets take to reach a network host. `mtr` (My Traceroute) combines `ping` and `traceroute` into a live report.
* **Example:**
    ```bash
    mtr google.com
    ```



---

## 🔐 2. Security & Certificates

### **`openssl`**
A toolkit for Transport Layer Security (TLS) and Secure Sockets Layer (SSL).
* **Common Tasks:**
    * **Check a remote certificate:**
      ```bash
      openssl s_client -connect google.com:443
      ```
    * **Generate a private key:**
      ```bash
      openssl genrsa -out private.key 2048
      ```
    * **Check the expiration date of a local file:**
      ```bash
      openssl x509 -enddate -noout -in cert.pem
      ```

### **`gpg` (GNU Privacy Guard)**
Used for encrypting files and signing communications.
* **Example:**
    ```bash
    gpg -c secret_file.txt  # Encrypt a file with a password
    gpg -d secret_file.txt.gpg  # Decrypt the file
    ```

---

## 🏗️ 3. Power-User Text Processing (Regex)

### **`egrep` (Extended Grep)**
Same as `grep -E`. Allows for complex patterns using `|` (OR), `+`, and `?`.
* **Example:**
    ```bash
    # Find lines containing either 'error' or 'warning'
    egrep "error|warning" /var/log/syslog
    ```

### **`sed` (Advanced Search & Replace)**
Use back-references to swap parts of a string.
* **Example:**
    ```bash
    # Swap two words (Word1 Word2 -> Word2 Word1)
    echo "Hello World" | sed -r 's/([^ ]+) ([^ ]+)/\2 \1/'
    ```

### **`awk` (Column Processing)**
Treats text like a database table.
* **Example:**
    ```bash
    # Print the second column ($2) if the first column contains 'Alice'
    awk '$1=="Alice" {print $2}' names.txt
    ```



---

## 🛠️ 4. Miscellaneous Power Tools

### **`nc` (Netcat)**
The "Swiss Army Knife" of networking. Used for reading/writing data across network connections.
* **Example:**
    ```bash
    # Check if a specific port is open on a remote server
    nc -zv 192.168.1.10 80
    ```

### **`nmap` (Network Mapper)**
The industry standard for security auditing and network discovery.
* **Example:**
    ```bash
    sudo nmap -sV 192.168.1.1  # Scan a device for open ports and service versions
    ```

### **`tcpdump`**
Captures raw network traffic (packets) for analysis.
* **Example:**
    ```bash
    sudo tcpdump -i eth0 port 80  # Sniff HTTP traffic on interface eth0
    ```

---

# 🐧 Linux Mastery: Part 11 - Immutable Files & System Recovery

This section covers how to lock files so even root cannot delete them, how to fix a broken system, and how to automate complex terminal interactions.

---

## 🔒 1. Advanced File Attributes (Beyond Permissions)
Standard permissions (`chmod`) aren't always enough. Attributes provide a deeper level of security.

### **`chattr` (Change Attribute)**
Changes file attributes on a Linux file system.
* **The "Immutable" Flag (`+i`):** Makes a file impossible to delete, rename, or modify—even by the **root** user.
* **The "Append-only" Flag (`+a`):** Allows data to be added to a file (like a log), but never deleted or overwritten.
* **Example:**
    ```bash
    sudo chattr +i /etc/resolv.conf  # Lock your DNS settings so they can't be changed
    sudo chattr -i /etc/resolv.conf  # Unlock it
    ```

### **`lsattr`**
Lists the attributes of files in a directory.
* **Example:**
    ```bash
    lsattr /etc/passwd
    ```

---

## 🚑 2. System Recovery & Hardware Health

### **`fsck` (File System Consistency Check)**
Used to check and repair a Linux file system. 
* **Note:** Usually run on unmounted partitions or from a live USB.
* **Example:**
    ```bash
    sudo fsck /dev/sda1
    ```

### **`badblocks`**
Scans a disk drive for bad sectors (physical damage).
* **Example:**
    ```bash
    sudo badblocks -v /dev/sdb
    ```

### **`smartctl`**
Controls the Self-Monitoring, Analysis, and Reporting Technology (SMART) system built into most hard drives and SSDs.
* **Example:**
    ```bash
    sudo smartctl -a /dev/nvme0n1  # Get a full health report of your SSD
    ```

### **`memtester`**
A userspace utility for testing the memory subsystem (RAM) for faults.
* **Example:**
    ```bash
    sudo memtester 1024M 5  # Test 1GB of RAM for 5 cycles
    ```



---

## ⚡ 3. Automation & Efficiency

### **`expect`**
A tool for automating interactive applications such as telnet, ftp, or passwd. It "expects" a specific string and "sends" a response.
* **Example (Script):**
    ```bash
    # This script automatically logs into an SSH server
    spawn ssh user@host
    expect "password:"
    send "mypassword\r"
    interact
    ```

### **`watch`**
Executes a program periodically, showing output in full screen.
* **Options:**
    * `-d`: Highlight the differences between updates.
* **Example:**
    ```bash
    watch -d -n 1 "cat /proc/interrupts"  # Watch CPU interrupts change in real-time
    ```

### **`xargs` (Advanced)**
Converts standard input into arguments for other commands.
* **Example:**
    ```bash
    # Find all empty files and move them to a 'trash' folder
    find . -type f -empty | xargs -I {} mv {} ./trash/
    ```

---

## 💾 4. Disk Imaging & Low-Level Writing

### **`dd` (Data Duplicator)**
Often called "Disk Destroyer" because it is very powerful. It copies data at a bit-by-bit level.
* **Common Use:** Creating a bootable USB drive from an ISO.
* **Example:**
    ```bash
    sudo dd if=ubuntu.iso of=/dev/sdX bs=4M status=progress
    ```
    *(Note: Replace /dev/sdX with your actual USB drive identifier)*

---

## 📅 5. Scheduling (Beyond Cron)

### **`at`**
Runs a command exactly **once** at a specific future time (unlike Cron which is repeating).
* **Example:**
    ```bash
    echo "sh backup.sh" | at 11:00 PM
    atq  # List pending 'at' jobs
    ```

### **`batch`**
Executes commands only when the system load levels permit (when the system is not busy).
* **Example:**
    ```bash
    batch < heavy_processing_script.sh
    ```

# 🐧 Linux Mastery: Part 12 - Privacy, Deep Logs & Kernel Tuning

This section covers how to permanently destroy data, analyze system journals like a pro, and manage kernel parameters.

---

## 🗑️ 1. Secure Data Destruction
When you delete a file with `rm`, the data stays on the disk until overwritten. These commands ensure the data is unrecoverable.

### **`shred`**
Overwrites a file multiple times with random data to make it nearly impossible to recover.
* **Options:**
    * `-u`: Deletes the file after overwriting it.
    * `-n [number]`: Specify how many times to overwrite (default is 3).
    * `-z`: Add a final overwrite with zeros to hide shredding.
* **Example:**
    ```bash
    shred -uz -n 5 secret_passwords.txt
    ```

### **`srm` (Secure Remove)**
Part of the `secure-delete` package. It wipes files using a more advanced algorithm than shred.
* **Example:**
    ```bash
    srm -v project_data.zip
    ```

---

## 📜 2. Advanced Log Parsing (`journalctl`)
Modern Linux systems use `systemd`, which stores logs in a binary format. `journalctl` is the tool used to query them.

### **`journalctl`**
* **Filtering by Time:**
    ```bash
    journalctl --since "2026-01-15" --until "2026-01-18 10:00:00"
    journalctl --since "1 hour ago"
    ```
* **Filtering by Priority (Errors only):**
    ```bash
    journalctl -p err -b  # -p err (errors), -b (since current boot)
    ```
* **Filtering by Service:**
    ```bash
    journalctl -u nginx.service
    ```
* **Output Formats:**
    ```bash
    journalctl -u ssh -o json-pretty  # View logs as structured JSON
    ```



---

## 🧠 3. Kernel & Runtime Management

### **`sysctl` (Deep Dive)**
Used to modify kernel parameters at runtime. This is how you "tune" a server.
* **Common Tuning Examples:**
    ```bash
    # Increase the maximum number of open files system-wide
    sudo sysctl -w fs.file-max=100000

    # Disable IPv6
    sudo sysctl -w net.ipv6.conf.all.disable_ipv6=1

    # Apply changes from the config file
    sudo sysctl -p /etc/sysctl.conf
    ```

### **`sysfs` & `/proc`**
These aren't commands, but "virtual filesystems" where you can "talk" to the kernel using `cat` and `echo`.
* **Example:**
    ```bash
    # Check battery percentage via sysfs
    cat /sys/class/power_supply/BAT0/capacity

    # Drop Linux filesystem caches (free up RAM)
    echo 3 | sudo tee /proc/sys/vm/drop_caches
    ```

---

## 🛠️ 4. Process & Binary Inspection

### **`ldd` (List Dynamic Dependencies)**
Shows which libraries a program needs to run. Essential for fixing "library not found" errors.
* **Example:**
    ```bash
    ldd /usr/bin/python3
    ```

### **`nm`**
Lists symbols from object files (functions, variables). Used by developers to debug compiled code.
* **Example:**
    ```bash
    nm -D /lib/x86_64-linux-gnu/libc.so.6 | head -n 20
    ```

### **`strings`**
Finds and prints printable strings in a binary file. Great for finding hidden text in executable files.
* **Example:**
    ```bash
    strings /usr/bin/ls | grep "Copyright"
    ```

---

## ⚙️ 5. Automation: The `alias` & `function` combo
Take your `.bashrc` or `.zshrc` to the next level.

### **Shell Functions**
Functions are more powerful than aliases because they can take arguments.
* **Example (Add to ~/.bashrc):**
    ```bash
    # A function to create a directory and enter it immediately
    mkcd() {
      mkdir -p "$1"
      cd "$1"
    }
    ```
    *Usage:* `mkcd new_project`



---

# 🐧 Linux Mastery: Part 13 - Auditing, Containers & Resource Limits

This section focuses on tracking every action on the system and understanding how Linux isolates processes for containers like Docker.

---

## 🕵️ 1. System Auditing (`auditd`)
The audit system allows you to track who changed a file, who logged in, and which commands were run by whom.

### **`auditctl`**
Controls the kernel's audit system.
* **Options:**
    * `-w [path]`: Watch a specific file or directory.
    * `-p [rwxa]`: Set permissions to watch (read, write, execute, or attribute change).
    * `-k [key]`: Add a search key to the log entry for easy filtering.
* **Example:**
    ```bash
    sudo auditctl -w /etc/shadow -p wa -k password_changes
    # This logs every time the password file is written to or its attributes change.
    ```

### **`ausearch`**
Searches the audit logs for specific events.
* **Example:**
    ```bash
    sudo ausearch -k password_changes
    ```

### **`aureport`**
Generates summary reports of audit system activity.
* **Example:**
    ```bash
    sudo aureport -au  # Show a report of all authentication attempts
    ```

---

## 📦 2. Namespaces & Cgroups (The Tech Behind Docker)
Before using Docker, Linux used these two features to isolate processes.

### **`lsns` (List Namespaces)**
Lists all currently active namespaces (Isolation layers for networking, mounting, users, etc.).
* **Example:**
    ```bash
    lsns -t net  # Show all network namespaces
    ```

### **`unshare`**
Runs a program with some namespaces unshared from the parent (starts a process in its own "container").
* **Example:**
    ```bash
    sudo unshare --fork --pid --mount-proc bash
    # Starts a bash shell that cannot see other processes on the system.
    ```

### **`cgcreate` / `cgset`**
Manages **Control Groups (Cgroups)**, which limit how much CPU or RAM a process can use.
* **Example:**
    ```bash
    # Limit a group to 512MB of RAM
    sudo cgcreate -g memory:mygroup
    sudo cgset -r memory.limit_in_bytes=536870912 mygroup
    ```



---

## 📊 3. Advanced Performance Profiling

### **`perf`**
The official Linux profiler. It can profile CPU cycles, cache misses, and even specific lines of code in a kernel module.
* **Example:**
    ```bash
    sudo perf top  # Real-time view of which functions are consuming the most CPU
    ```

### **`pidstat`**
Reports statistics for Linux tasks (processes).
* **Example:**
    ```bash
    pidstat -d 1  # Monitor I/O usage for every process every second
    ```

---

## 🛡️ 4. Mandatory Access Control (MAC)

### **`getenforce` / `setenforce`**
Checks or sets the state of **SELinux** (Security-Enhanced Linux).
* **Example:**
    ```bash
    getenforce          # Check if 'Enforcing', 'Permissive', or 'Disabled'
    sudo setenforce 0   # Put SELinux in Permissive mode (logging only, no blocking)
    ```

### **`aa-status` (AppArmor)**
Checks the status of AppArmor profiles, which restrict what applications (like Firefox or Snap) can do.
* **Example:**
    ```bash
    sudo aa-status
    ```



---

## 💾 5. Advanced Disk Quotas

### **`quota` / `edquota`**
Sets limits on how much disk space a specific user or group can use.
* **Example:**
    ```bash
    sudo edquota -u john  # Open an editor to set soft/hard limits for user 'john'
    ```

### **`repquota`**
Summarizes quotas for a filesystem.
* **Example:**
    ```bash
    sudo repquota -a
    ```
# 🐧 Linux Mastery: Part 14 - Persistent Sessions & Secure Tunneling

This section covers how to keep your work running after you disconnect and how to use SSH as a powerful networking tool.

---

## 📺 1. Terminal Multiplexers (tmux)
When you lose your Wi-Fi connection, a standard SSH session dies. `tmux` keeps your processes alive on the server.

### **Core `tmux` Commands**
* **`tmux new -s <name>`**: Start a new named session.
* **`tmux ls`**: List all running sessions.
* **`tmux a -t <name>`**: Attach (reconnect) to a session.
* **`tmux kill-session -t <name>`**: Permanently close a session.

### **Inside `tmux` (The Prefix: `Ctrl+b`)**
* **`Ctrl+b` then `d`**: Detach (leave the session running in the background).
* **`Ctrl+b` then `%`**: Split screen vertically.
* **`Ctrl+b` then `"`**: Split screen horizontally.
* **`Ctrl+b` then `o`**: Switch between panes.



---

## 🚇 2. SSH Tunneling & Port Forwarding
SSH can act as a "secure pipe" to move traffic from your local machine to a remote server or vice versa.

### **Local Port Forwarding (`-L`)**
Access a remote database (e.g., on port 5432) as if it were running on your own laptop.
* **Example:**
    ```bash
    ssh -L 8000:localhost:5432 user@remote-server
    # Now, connecting to localhost:8000 on your laptop reaches port 5432 on the server.
    ```

### **Remote Port Forwarding (`-R`)**
Let someone on the internet access a web server running on your local machine.
* **Example:**
    ```bash
    ssh -R 9000:localhost:3000 user@remote-server
    # People hitting port 9000 on the server are routed to port 3000 on your laptop.
    ```

### **Dynamic Port Forwarding (`-D`)**
Turns your SSH connection into a SOCKS proxy.
* **Example:**
    ```bash
    ssh -D 1080 user@remote-server
    # Configure your browser to use SOCKS proxy localhost:1080 to browse through the server.
    ```



---

## 🛠️ 3. Essential TUI Tools (Terminal User Interfaces)
These tools provide a visual experience inside the text-only terminal.

### **`ncdu` (NCurses Disk Usage)**
A visual replacement for `du`. It lets you browse folders and see what is eating your disk space.
* **Example:**
    ```bash
    sudo ncdu /
    ```

### **`lazygit`**
A simple terminal UI for git commands. Great for staging files and managing branches without typing long commands.
* **Example:**
    ```bash
    lazygit
    ```

### **`nmtui` (Network Manager TUI)**
The easiest way to configure Wi-Fi or Ethernet on a server without a desktop.
* **Example:**
    ```bash
    sudo nmtui
    ```

### **`glances`**
An "all-in-one" monitoring tool that shows CPU, Load, Memory, Network, and Disk I/O on one screen.
* **Example:**
    ```bash
    glances
    ```

---

## ⚡ 4. Advanced Bash Expansion & Magic

### **Brace Expansion `{}`**
Create multiple files or strings instantly.
* **Example:**
    ```bash
    touch image_{1..10}.jpg      # Creates image_1.jpg through image_10.jpg
    mkdir -p project/{src,bin,lib} # Creates 3 subfolders at once
    ```

### **Command Substitution `$()`**
Use the output of one command as an argument for another.
* **Example:**
    ```bash
    kill -9 $(pgrep firefox)    # Find Firefox's ID and kill it in one line
    ```

### **The "Safe" Move/Copy**
Use the `-b` flag to create backups of files before they are overwritten.
* **Example:**
    ```bash
    cp -b new_config.conf old_config.conf # Creates old_config.conf~ as a backup
    ```

---

## 📂 5. File System Metadata

### **`stat`**
Displays detailed status of a file or directory (Inodes, Links, Access/Modify/Change times).
* **Example:**
    ```bash
    stat README.md
    ```

### **`file`**
Determines the file type (even if the file has no extension).
* **Example:**
    ```bash
    file mystery_data
    # Output: mystery_data: JPEG image data, JFIF standard 1.01
    ```

# 🐧 Linux Mastery: Part 15 - Log Rotation & Environment Deep-Dive

This section covers how to prevent log files from crashing your system and how to inspect the deep environmental settings of your shell.

---

## 🔄 1. Log Rotation (`logrotate`)
If logs are never deleted, they will eventually fill up your entire hard drive. `logrotate` automates the process of compressing, renaming, or deleting old logs.

### **`logrotate`**
Usually runs as a background cron job, but you can trigger it manually.
* **Configuration Path:** `/etc/logrotate.conf` and `/etc/logrotate.d/`
* **Example:**
    ```bash
    sudo logrotate -f /etc/logrotate.d/nginx  # Force a rotation of Nginx logs immediately
    ```

### **`logger`**
A shell command interface to the `syslog` system module. Use it to add your own custom messages to system logs (great for scripts).
* **Example:**
    ```bash
    logger "Backup process started by user $USER"
    # View it in the logs:
    tail -n 5 /var/log/syslog
    ```

---

## 🌍 2. Environmental Deep-Dive

### **`set` / `declare`**
While `env` shows exported variables, `set` shows **everything**—including local variables, shell functions, and read-only variables.
* **Example:**
    ```bash
    set | grep "BASH_VERSION"
    ```

### **`export -f`**
Exporting isn't just for variables; you can export entire functions to sub-shells.
* **Example:**
    ```bash
    my_func() { echo "Hello!"; }
    export -f my_func
    bash -c my_func  # Function works even inside a new bash instance
    ```

### **`whereis`**
Locates the binary, source, and manual page files for a command.
* **Example:**
    ```bash
    whereis python3
    # Output: /usr/bin/python3 /usr/lib/python3.10 /usr/share/man/man1/python3.1.gz
    ```

### **`which`**
Shows the full path of (shell) commands.
* **Example:**
    ```bash
    which ls
    # Output: /usr/bin/ls
    ```

---

## 📟 3. Terminal & Console Control

### **`stty` (Set Teletype)**
Changes and prints terminal line settings. This is how you change things like "backspace" behavior or hide password input.
* **Example:**
    ```bash
    stty -echo    # Disable terminal echo (typing becomes invisible)
    stty echo     # Re-enable terminal echo
    ```

### **`tput`**
Used to initialize a terminal or query the terminfo database. Great for adding colors or moving the cursor in scripts.
* **Example:**
    ```bash
    tput cols    # Shows how many columns wide your terminal is
    tput setaf 2; echo "This is green text"; tput sgr0
    ```

---

## 🧠 4. Kernel Messaging & Symbols

### **`dmesg` (Advanced)**
Read the kernel ring buffer. Essential for debugging hardware failures or "Out of Memory" (OOM) kills.
* **Options:**
    * `-T`: Show human-readable timestamps.
    * `-w`: Wait for new messages (follow mode).
    * `-l [level]`: Filter by level (err, crit, warn, info).
* **Example:**
    ```bash
    dmesg -T --level=err,crit  # See only critical kernel errors with timestamps
    ```



---

## ⚙️ 5. Shared Libraries & Binaries

### **`ldconfig`**
Configures dynamic linker run-time bindings. Run this after installing new libraries to ensure the system "sees" them.
* **Example:**
    ```bash
    sudo ldconfig -v | grep "libssl"
    ```

### **`readelf`**
Displays information about ELF (Executable and Linkable Format) files.
* **Example:**
    ```bash
    readelf -h /bin/bash  # See the header info for the bash binary
    ```

### **`objdump`**
Displays information from object files; often used to "disassemble" a binary to see the assembly code.
* **Example:**
    ```bash
    objdump -d /bin/ls | head -n 20
    ```

# 🐧 Linux Mastery: Part 16 - Privilege Control & System Boot

This section covers the security of administrative access, faster data compression, and the evolution of how Linux starts.

---

## 🔑 1. Advanced User Privileges (`sudoers`)
The `/etc/sudoers` file defines who can run what as root. You should **never** edit this file with a normal editor; always use `visudo`.

### **`visudo`**
Safely edits the sudoers file by checking for syntax errors before saving.
* **Example Entry:**
    ```text
    # Allow user 'dev' to run only nginx restart without a password
    dev ALL=(ALL) NOPASSWD: /usr/sbin/service nginx restart
    ```

### **`sudo -v`**
Update the user's cached credentials (restart the sudo timer) without running a command.
* **Example:**
    ```bash
    sudo -v
    ```

### **`sudo -l`**
List the allowed (and forbidden) commands for the invoking user. Great for checking what permissions you actually have.
* **Example:**
    ```bash
    sudo -l
    ```

---

## 🗜️ 2. Parallel & Advanced Compression
Standard `gzip` only uses one CPU core. For large servers, we use parallel tools to speed up compression by 10x.

### **`pigz` (Parallel Gzip)**
A fully functional replacement for `gzip` that exploits multiple processors and cores.
* **Example:**
    ```bash
    tar -cvf - folder/ | pigz > folder.tar.gz
    ```

### **`pbzip2`**
A parallel implementation of `bzip2`.
* **Example:**
    ```bash
    pbzip2 -d large_file.bz2  # Decompress using all available CPU cores
    ```

### **`zcat` / `zgrep` / `zless`**
Tools to view or search compressed files **without** decompressing them first.
* **Example:**
    ```bash
    zgrep "Error 404" access.log.gz
    ```

---

## 🏗️ 3. System Initialization (Init vs. Systemd)
Linux has transitioned from the old "System V Init" to the modern "Systemd".



### **`systemctl` (The Systemd Boss)**
* **`systemctl list-units --type=service`**: See all active services.
* **`systemctl enable --now nginx`**: Set Nginx to start on boot and start it immediately.
* **`systemctl mask apache2`**: Completely disable a service so it cannot be started even by other services.

### **`runlevel` / `who -r`**
Shows the current "state" of the system (e.g., Multi-user mode, Graphical mode, or Maintenance mode).
* **Example:**
    ```bash
    runlevel
    # Output: N 5 (N means no previous level, 5 means Graphical mode)
    ```

### **`telinit`**
Change the system runlevel.
* **Example:**
    ```bash
    sudo telinit 1  # Switch to Single-User (Maintenance) mode
    ```

---

## 🛡️ 4. File Integrity & Comparison

### **`md5sum` / `sha512sum`**
Generate or check message digests. Useful for verifying that a file hasn't been corrupted during download.
* **Example:**
    ```bash
    sha512sum ubuntu.iso > ubuntu.iso.sha512
    sha512sum -c ubuntu.iso.sha512  # Verifies the file
    ```

### **`cksum`**
Print CRC (Cyclic Redundancy Check) checksum and byte counts.
* **Example:**
    ```bash
    cksum script.sh
    ```

---

## 🔦 5. The "Where is my stuff?" Commands

### **`findmnt`**
Finds a filesystem. It shows exactly where disks are mounted in a tree-like format.
* **Example:**
    ```bash
    findmnt --real  # Only show physical disks, ignore virtual ones
    ```

### **`blkid`**
Locates/prints block device attributes (UUIDs and Filesystem types). This is what you need to edit `/etc/fstab`.
* **Example:**
    ```bash
    sudo blkid
    ```

### **`mount -a`**
Mounts all filesystems mentioned in `/etc/fstab`. Run this after editing your disk config to test for errors.
* **Example:**
    ```bash
    sudo mount -a
    ```

# 🐧 Linux Mastery: Part 18 - Data Merging & Resource Limits

This section covers how to join data from multiple files, set limits on running processes, and visualize the relationship between parent and child tasks.

---

## 🛠️ 1. Advanced Text Filtering & Merging
While `cat` and `grep` are common, these tools are designed for structural manipulation of data files.

### **`join`**
Joins the lines of two files on a common field (similar to a SQL JOIN).
* **Example:**
    ```bash
    # file1: 1 Apple, file2: 1 Red
    join file1.txt file2.txt
    # Output: 1 Apple Red
    ```

### **`paste`**
Merges lines of files side-by-side, separated by tabs.
* **Options:**
    * `-d`: Specify a custom delimiter (e.g., a comma).
    * `-s`: Paste all lines from one file into a single line.
* **Example:**
    ```bash
    paste -d "," names.txt ages.txt > combined.csv
    ```

### **`column`**
Formats its input into multiple columns. Extremely useful for making raw data readable.
* **Example:**
    ```bash
    mount | column -t  # Formats the mount output into a clean table
    ```

---

## 🛑 2. System Resource Limits

### **`prlimit`**
Get and set process resource limits. Unlike `ulimit` (which is for the shell), `prlimit` can be used on any running process.
* **Options:**
    * `--nofile`: Limit number of open files.
    * `--nproc`: Limit number of processes.
    * `-p`: Specify the PID.
* **Example:**
    ```bash
    # See the limits for process 1234
    prlimit -p 1234
    # Set a max of 2000 open files for process 1234
    sudo prlimit --pid 1234 --nofile=2000
    ```

### **`timeout`**
Runs a command with a time limit. If the command takes too long, it is automatically killed.
* **Example:**
    ```bash
    timeout 10s ping google.com  # Run ping for exactly 10 seconds then stop
    ```

---

## 🌳 3. Process Relationships

### **`pstree`**
Shows running processes as a tree. This makes it easy to see which "Parent" process started which "Child" process.
* **Options:**
    * `-p`: Show PIDs.
    * `-u`: Show user transitions (if a process changed user).
* **Example:**
    ```bash
    pstree -p | less
    ```



### **`pgrep` / `pkill`**
Finds or signals processes based on name and other attributes.
* **Example:**
    ```bash
    pgrep -u root sshd  # Find the PIDs of all SSH sessions owned by root
    pkill -t pts/0      # Kill all processes running on a specific terminal
    ```

---

## 📡 4. Network Services & Ports

### **`lsof` (Advanced)**
"List Open Files." In Linux, a network socket is a file. Use this to find what is blocking a port.
* **Example:**
    ```bash
    sudo lsof -i :8080      # See what process is using port 8080
    sudo lsof -u username   # See all files opened by a specific user
    ```

### **`ss` (Socket Statistics)**
The modern replacement for `netstat`.
* **Example:**
    ```bash
    ss -tlpn  # t (tcp), l (listening), p (process), n (numeric port)
    ```

---

## 🏁 5. Command Execution Logic

### **`xargs` (Parallel Mode)**
`xargs` can run multiple commands at the same time using your CPU cores.
* **Example:**
    ```bash
    # Download 5 files at the same time (parallel)
    cat urls.txt | xargs -n 1 -P 5 wget
    ```

### **`env -i`**
Runs a command with an empty environment. Useful for testing if your application depends on hidden environmental variables.
* **Example:**
    ```bash
    env -i ./my_script.sh
    ```

# 🐧 Linux Mastery: Part 19 - Virtualization, ISOs & Screen

This section covers how to check if your hardware supports Virtual Machines, how to manipulate disk images, and how to use the classic `screen` utility.

---

## 💻 1. Virtualization & CPU Features
Before running Docker, KVM, or VirtualBox, you need to know if your hardware allows it.

### **`virt-host-validate`**
Checks if the host is properly configured to run virtual machines.
* **Example:**
    ```bash
    sudo virt-host-validate
    ```

### **`kvm-ok`**
Specifically checks if your CPU supports KVM (Kernel-based Virtual Machine) acceleration.
* **Example:**
    ```bash
    kvm-ok
    # Output: INFO: /dev/kvm exists, KVM acceleration can be used
    ```

### **`lscpu` (Virtualization check)**
* **Example:**
    ```bash
    lscpu | grep -i "virtualization"
    # Look for VT-x (Intel) or AMD-V (AMD)
    ```



---

## 📀 2. ISO & Disk Image Management

### **`isoinfo`**
Lists information about ISO9660 images (CD/DVD images).
* **Options:**
    * `-i`: Path to the ISO file.
    * `-l`: List all files in the ISO.
* **Example:**
    ```bash
    isoinfo -i ubuntu-22.04.iso -l
    ```

### **`genisoimage`**
Creates an ISO image from a folder on your computer.
* **Example:**
    ```bash
    genisoimage -o backup.iso ./my_files/
    ```

### **`mount` (ISO Mounting)**
You can "open" an ISO file as if it were a folder.
* **Example:**
    ```bash
    sudo mount -o loop image.iso /mnt/iso_contents
    ```

---

## 📺 3. The `screen` Utility
While `tmux` is modern, `screen` is available on almost every Linux system by default. It is the original "persistent terminal."

### **Core `screen` Commands**
* **`screen`**: Start a new session.
* **`screen -S <name>`**: Start a named session.
* **`screen -ls`**: List active sessions.
* **`screen -r <name>`**: Reattach to a running session.

### **Inside `screen` (The Prefix: `Ctrl+a`)**
* **`Ctrl+a` then `d`**: Detach (Keep it running in background).
* **`Ctrl+a` then `k`**: Kill the current window.
* **`Ctrl+a` then `c`**: Create a new window within the session.
* **`Ctrl+a` then `"`**: List all windows in the session.

---

## 🔧 4. Advanced Hardware Control

### **`hdparm`**
Gets/sets SATA/IDE device parameters. Used to test hard drive speeds or set sleep timers for disks.
* **Options:**
    * `-tT`: Perform a read speed test.
    * `-I`: Detailed information about the drive.
* **Example:**
    ```bash
    sudo hdparm -tT /dev/sda  # Benchmark your Hard Drive speed
    ```

### **`sdparm`**
The SCSI/SAS version of `hdparm`.
* **Example:**
    ```bash
    sudo sdparm --get=WCE /dev/sdb  # Check if Write Cache is Enabled
    ```

### **`nvme`**
The tool for managing modern NVMe SSDs.
* **Example:**
    ```bash
    sudo nvme list
    sudo nvme smart-log /dev/nvme0  # View health of the NVMe drive
    ```



---

## 🏁 5. System Execution Context

### **`runuser`**
Runs a shell with substitute user and group IDs. Unlike `su`, it is designed for use in init scripts.
* **Example:**
    ```bash
    sudo runuser -u postgres -- psql
    ```

### **`setpriv`**
Allows you to run a command with a very specific set of Linux "capabilities" or privileges (fine-grained control).
* **Example:**
    ```bash
    setpriv --reuid=1000 --regid=1000 --clear-groups --inplace my_app
    ```
# 🐧 Linux Mastery: Part 20 - Advanced Networking & System Auditing

This section focuses on low-level network encapsulation, auditing user login databases, and optimizing hardware for high-performance applications.

---

## 🌐 1. Advanced Network Tunneling

### **`iptunnel`**
Used to create, change, or delete network tunnels (encapsulating one protocol inside another).
* **Common Use:** Creating a GRE tunnel to connect two private networks over the internet.
* **Example:**
    ```bash
    sudo iptunnel add tun0 mode gre remote 203.0.113.1 local 198.51.100.1 ttl 255
    sudo ifconfig tun0 10.0.0.1 netmask 255.255.255.0 up
    ```

### **`bridge`**
Used to manage network bridges, which connect two different network segments as if they were a single physical wire.
* **Example:**
    ```bash
    bridge link show  # Show the status of all bridged interfaces
    ```



---

## 🕵️ 2. User Session & Login Auditing

### **`utmpdump`**
The `utmp` and `wtmp` files store who is currently logged in and the history of all logins. They are binary files; `utmpdump` converts them to text.
* **Example:**
    ```bash
    utmpdump /var/log/wtmp | tail -n 10
    ```

### **`lastlog`**
Reports the most recent login of all users or a specific user. It pulls data from the `/var/log/lastlog` file.
* **Example:**
    ```bash
    lastlog -u username  # See exactly when a specific user last accessed the system
    ```

### **`who` / `w`**
`who` shows who is logged in. `w` provides a more detailed view, including what process they are currently running and system load.
* **Example:**
    ```bash
    w
    ```

---

## 🧠 3. High-Performance Hardware Tuning (NUMA)
Modern servers have multiple CPUs, and each CPU has its "local" RAM. Accessing RAM attached to a *different* CPU is slower. This is called **NUMA** (Non-Uniform Memory Access).

### **`numactl`**
Controls NUMA policy for processes or shared memory. Use it to "bind" a process to a specific CPU and its local RAM for maximum speed.
* **Example:**
    ```bash
    # Run a database only on CPU node 0 with local memory
    sudo numactl --cpunodebind=0 --membind=0 /usr/bin/my_db_engine
    ```

### **`numastat`**
Shows statistics about memory allocation across different NUMA nodes. Use this to see if your system is suffering from "memory latency."
* **Example:**
    ```bash
    numastat -p <PID>
    ```



---

## 🛠️ 4. Low-Level System Inspection

### **`getconf`**
Queries system configuration variables, such as the maximum number of arguments a command can take or the cache line size of the CPU.
* **Example:**
    ```bash
    getconf ARG_MAX      # Find the character limit for a single command line
    getconf PAGE_SIZE    # Find the memory page size (usually 4096 bytes)
    ```

### **`getcap` / `setcap`**
Linux "Capabilities" allow you to give specific "root-like" powers to a file without making it a full `setuid` root file.
* **Example:**
    ```bash
    # Allow 'ping' to open raw sockets without being root
    sudo setcap cap_net_raw+ep /usr/bin/ping
    getcap /usr/bin/ping
    ```

---

## 🏁 5. Terminal "Hidden" Gems

### **`script`**
Records everything you do in the terminal session into a text file. Perfect for creating tutorials or proof of work.
* **Example:**
    ```bash
    script session_record.txt
    # ... do your work ...
    exit
    # Now check session_record.txt to see the full replay.
    ```

### **`yes`**
Outputs a string (defaulting to 'y') repeatedly until killed. Useful for piping into commands that ask for many confirmations.
* **Example:**
    ```bash
    yes | rm -i *.txt  # Automatically says 'y' to every delete prompt
    ```

# 🐧 Linux Mastery: Part 21 - Data, Performance & Speed-Search

This section covers interacting with databases via the terminal, stress-testing web servers, and using modern, high-speed alternatives to `grep`.

---

## 🗄️ 1. Database CLI Clients
Managing databases directly from the terminal is often faster than using a GUI like pgAdmin or DBeaver.

### **`psql` (PostgreSQL Client)**
The interactive terminal for managing PostgreSQL databases.
* **Commands:**
    * `-h`: Hostname.
    * `-U`: Username.
    * `-d`: Database name.
* **Example:**
    ```bash
    psql -h localhost -U postgres -d my_app_db
    # Inside psql: \dt (list tables), \q (quit)
    ```

### **`mysql` / `mariadb`**
The command-line shell for MySQL/MariaDB.
* **Example:**
    ```bash
    mysql -u root -p -e "SHOW DATABASES;"
    ```

### **`redis-cli`**
The command-line interface for Redis.
* **Example:**
    ```bash
    redis-cli SET user:1 "John"
    redis-cli GET user:1
    ```

---

## 🚀 2. Web Server Benchmarking
When you build an API, you need to know how many requests per second (RPS) it can handle before it crashes.

### **`ab` (Apache Benchmark)**
A simple but powerful tool for benchmarking your HTTP server.
* **Options:**
    * `-n`: Total number of requests to perform.
    * `-c`: Number of multiple requests to perform at a time (concurrency).
* **Example:**
    ```bash
    ab -n 1000 -c 10 [http://127.0.0.1:8080/api/v1/data](http://127.0.0.1:8080/api/v1/data)
    ```

### **`wrk`**
A more modern, multithreaded HTTP benchmarking tool capable of generating massive loads.
* **Example:**
    ```bash
    wrk -t12 -c400 -d30s [http://127.0.0.1:8080/](http://127.0.0.1:8080/)
    # Uses 12 threads, 400 connections, for 30 seconds.
    ```



---

## ⚡ 3. The "New Wave" Search Tools
While `grep` is a classic, these modern tools are written in Rust or C and are 10x to 100x faster for searching large codebases.

### **`rg` (ripgrep)**
The fastest search tool available today. It respects `.gitignore` by default.
* **Example:**
    ```bash
    rg "handleRequest" ./src  # Searches for text while ignoring node_modules
    ```

### **`ag` (The Silver Searcher)**
Similar to `rg`, designed for code searching.
* **Example:**
    ```bash
    ag --js "const"  # Search only within JavaScript files
    ```

### **`fd`**
A fast and user-friendly alternative to the `find` command.
* **Example:**
    ```bash
    fd -e png  # Instantly find all files with .png extension
    ```

---

## 🛠️ 4. System-Wide Tracing & Debugging

### **`ltrace` (Library Trace)**
Similar to `strace`, but it intercepts and records the **dynamic library calls** which are called by the executed process.
* **Example:**
    ```bash
    ltrace ./my_program
    ```

### **`gdb` (GNU Debugger)**
The industry-standard debugger for C/C++/Go/Rust programs.
* **Example:**
    ```bash
    gdb ./my_binary
    # Inside GDB: run, backtrace, quit
    ```

### **`valgrind`**
An instrumentation framework for building dynamic analysis tools. It is primarily used to detect **memory leaks**.
* **Example:**
    ```bash
    valgrind --leak-check=full ./my_program
    ```



---

## 📂 5. Advanced File Transfer (Part 2)

### **`rclone`**
The "rsync for cloud storage." It supports Google Drive, AWS S3, Dropbox, and 40+ others.
* **Example:**
    ```bash
    rclone copy ./backups remote:my-s3-bucket
    ```

### **`sftp` (Secure File Transfer Protocol)**
Interactive file transfer program, similar to ftp, but it performs all operations over an encrypted ssh transport.
* **Example:**
    ```bash
    sftp user@remote-host
    # Inside sftp: get filename, put filename
    ```

# 🐧 Linux Mastery: Part 22 - Media, PDFs & Terminal Browsing

This section focuses on manipulating images, videos, and documents directly from the command line, plus browsing the web without a GUI.

---

## 🎥 1. Video & Audio Mastery (`ffmpeg`)
`ffmpeg` is the "Swiss Army Knife" of media. It can convert, stream, and filter almost any video or audio format.

### **`ffmpeg`**
* **Convert Video Format:**
    ```bash
    ffmpeg -i input.mp4 output.webm
    ```
* **Extract Audio from Video:**
    ```bash
    ffmpeg -i video.mp4 -vn -acodec libmp3lame audio.mp3
    ```
* **Resize/Scale Video:**
    ```bash
    ffmpeg -i input.mp4 -vf scale=1280:720 output_720p.mp4
    ```
* **Compress Video (Reduce File Size):**
    ```bash
    ffmpeg -i input.mp4 -vcodec libx265 -crf 28 output_compressed.mp4
    ```

### **`ffprobe`**
Used to display information (metadata, resolution, bitrate) about a media file.
* **Example:**
    ```bash
    ffprobe -v error -show_format -show_streams video.mp4
    ```



---

## 🖼️ 2. Image Manipulation (`ImageMagick`)
The `magick` suite (formerly `convert`) allows you to edit images in bulk.

### **`convert` / `magick`**
* **Resize an Image:**
    ```bash
    convert photo.jpg -resize 50% photo_small.jpg
    ```
* **Convert Format (PNG to JPG):**
    ```bash
    convert image.png image.jpg
    ```
* **Create a PDF from Multiple Images:**
    ```bash
    convert page1.jpg page2.jpg page3.jpg document.pdf
    ```

### **`mogrify`**
Similar to convert, but it overwrites the original file. Use with caution!
* **Example (Batch resize all JPGs in a folder):**
    ```bash
    mogrify -resize 800x600 *.jpg
    ```

### **`identify`**
Describes the format and characteristics of one or more image files.
* **Example:**
    ```bash
    identify -verbose logo.png
    ```

---

## 📄 3. PDF Manipulation

### **`qpdf`**
A powerful tool for structural transformation of PDF files (merging, splitting, encrypting).
* **Merge PDFs:**
    ```bash
    qpdf --empty --pages doc1.pdf doc2.pdf -- merged.pdf
    ```
* **Decrypt a Password-Protected PDF:**
    ```bash
    qpdf --password=mypass --decrypt protected.pdf decrypted.pdf
    ```

### **`pdftotext`**
Converts PDF documents to plain text files.
* **Example:**
    ```bash
    pdftotext resume.pdf resume.txt
    ```

---

## 🌐 4. Terminal Web Browsers
Sometimes you need to browse the web or check an internal URL on a server with no desktop.

### **`lynx`**
The oldest and most famous text-based web browser.
* **Example:**
    ```bash
    lynx [https://www.google.com](https://www.google.com)
    ```

### **`links` / `elinks`**
More modern versions of lynx that support frames and tables.
* **Example:**
    ```bash
    elinks [https://en.wikipedia.org](https://en.wikipedia.org)
    ```

### **`googler`**
Search Google from the terminal. It gives you a numbered list of results you can open.
* **Example:**
    ```bash
    googler "linux kernel mailing list"
    ```

---

## 🛠️ 5. Utility & Information

### **`exiftool`**
Read and write Meta information (EXIF data) in files. Useful for removing GPS data from photos for privacy.
* **Example:**
    ```bash
    exiftool -all= photo.jpg  # Delete all metadata from the image
    ```

### **`units`**
A library for unit conversion (meters to feet, Celsius to Fahrenheit, etc.).
* **Example:**
    ```bash
    units "100 meters" "feet"
    ```

### **`cal` / `ncal`**
Displays a simple calendar in your terminal.
* **Example:**
    ```bash
    cal 2026
    ```

# 🐧 Linux Mastery: Part 23 - Structured Data & Clipboard Magic

This section focuses on parsing and transforming the data formats that power modern APIs and configurations, plus controlling your clipboard from the command line.

---

## 🏗️ 1. JSON Processing (`jq`)
`jq` is like `sed` for JSON data. It is essential for interacting with REST APIs via the terminal.

### **`jq`**
* **Prettify JSON:**
    ```bash
    cat data.json | jq .
    ```
* **Extract a Specific Field:**
    ```bash
    curl -s [https://api.github.com/repos/stedolan/jq](https://api.github.com/repos/stedolan/jq) | jq '.description'
    ```
* **Filter an Array:**
    ```bash
    # Find all items where price > 100
    cat products.json | jq '.[] | select(.price > 100)'
    ```
* **Map and Transform:**
    ```bash
    # Extract only names and emails into a new object
    cat users.json | jq '[.[] | {full_name: .name, contact: .email}]'
    ```



---

## 📝 2. YAML & XML Processing

### **`yq`**
The YAML equivalent of `jq`. Vital for Kubernetes (K8s) and Docker Compose files.
* **Example (Change a value in a YAML file):**
    ```bash
    yq -i '.replicaCount = 3' values.yaml
    ```

### **`xmlstarlet`**
A powerful tool to query, transform, and validate XML files.
* **Example (Extract value of an attribute):**
    ```bash
    xmlstarlet sel -t -v "/root/node/@attribute" file.xml
    ```

---

## 📋 3. Clipboard Management
These tools allow you to pipe command output directly into your system's "Copy" buffer.

### **`xclip` / `xsel` (For X11/Linux Desktop)**
* **Copy Output to Clipboard:**
    ```bash
    pwd | xclip -selection clipboard
    ```
* **Paste Content from Clipboard to a File:**
    ```bash
    xclip -selection clipboard -o > new_file.txt
    ```

### **`pbcopy` / `pbpaste` (For macOS)**
* **Copying:** `ls | pbcopy`
* **Pasting:** `pbpaste > list.txt`

### **`wl-copy` / `wl-paste` (For Wayland)**
The modern replacement for xclip on newer Linux distributions (like Ubuntu 22.04+).
* **Example:**
    ```bash
    cat logs.txt | wl-copy
    ```

---

## 🛠️ 4. Advanced System Information

### **`inxi`**
A full-featured system information tool. It shows hardware, CPU, drivers, desktop environment, and even battery health in one command.
* **Example:**
    ```bash
    inxi -F  # Full system report
    ```

### **`neofetch` / `fastfetch`**
A visual tool that displays system info alongside an ASCII logo of your Linux distribution.
* **Example:**
    ```bash
    fastfetch
    ```



---

## 📦 5. File Integrity & Diffing (Part 2)

### **`vimdiff`**
Opens two, three, or four files in Vim and shows the differences between them. It highlights changed lines and allows you to "merge" them manually.
* **Example:**
    ```bash
    vimdiff config_v1.js config_v2.js
    ```

### **`patch`**
Takes a "diff" file and applies the changes to an original file. This is how software updates were distributed before Git.
* **Example:**
    ```bash
    diff -u old.js new.js > changes.patch
    patch old.js < changes.patch
    ```

### **`comm`**
Compares two sorted files line by line and shows which lines are unique to file 1, file 2, or common to both.
* **Example:**
    ```bash
    comm -12 file1.txt file2.txt  # Show only lines found in BOTH files
    ```
# 🐧 Linux Mastery: Part 24 - Security Auditing & Hardening

This section focuses on scanning for vulnerabilities, detecting rootkits, and managing active defense systems.

---

## 🛡️ 1. System Auditing (`lynis`)
`lynis` is a battle-tested security tool for systems running Linux. It performs a comprehensive health scan to harden the system.

### **`lynis`**
* **Perform a System Audit:**
    ```bash
    sudo lynis audit system
    ```
* **What it checks:**
    * Boot loader integrity.
    * Outdated software packages.
    * Weak file permissions.
    * Firewall configuration.
* **Pro-Tip:** After the scan, look at the "Suggestions" section to see exactly how to improve your security score.

---

## 🕵️ 2. Malware & Rootkit Detection
Rootkits are malicious programs designed to hide their presence on a system.

### **`chkrootkit`**
A tool to locally check for signs of a rootkit.
* **Example:**
    ```bash
    sudo chkrootkit
    ```

### **`rkhunter` (Rootkit Hunter)**
Scans for rootkits, backdoors, and local exploits by comparing SHA-1 hashes of important files with known good ones in online databases.
* **Example:**
    ```bash
    sudo rkhunter --check
    ```

### **`clamscan` (ClamAV)**
The standard open-source antivirus engine for detecting trojans, viruses, and malware.
* **Example:**
    ```bash
    sudo clamscan -r /home  # Scan the home directory recursively
    ```



---

## 🚪 3. Intrusion Prevention (`fail2ban`)
`fail2ban` scans log files and bans IPs that show malicious signs (like too many password failures).

### **`fail2ban-client`**
* **Check Status:**
    ```bash
    sudo fail2ban-client status sshd
    ```
* **Unban an IP:**
    ```bash
    sudo fail2ban-client set sshd unbanip 1.2.3.4
    ```

---

## ⛓️ 4. Advanced Network Security

### **`nmap` (Advanced Scripting)**
Nmap is more than just a port scanner; it can use the Nmap Scripting Engine (NSE) to find specific vulnerabilities.
* **Example:**
    ```bash
    # Scan for common vulnerabilities on a web server
    nmap --script vuln 192.168.1.10
    ```

### **`sslyze`**
A fast and powerful library to analyze the SSL configuration of a server.
* **Example:**
    ```bash
    sslyze --regular [www.google.com](https://www.google.com)
    ```

### **`nikto`**
A web server scanner that tests for over 6700 potentially dangerous files/programs and outdated server versions.
* **Example:**
    ```bash
    nikto -h http://localhost:8080
    ```



---

## 🔒 5. File & Data Encryption

### **`cryptsetup` (LUKS)**
Used to set up transparent encryption of block devices (hard drives). This is how you encrypt your entire laptop disk.
* **Example:**
    ```bash
    sudo cryptsetup luksFormat /dev/sdb1
    sudo cryptsetup luksOpen /dev/sdb1 my_encrypted_disk
    ```

### **`gpg` (Key Management)**
* **List your keys:**
    ```bash
    gpg --list-keys
    ```
* **Export a Public Key:**
    ```bash
    gpg --export -a "User Name" > public.key
    ```

### **`steghide`**
A steganography program that is able to hide data in various kinds of image and audio files.
* **Example:**
    ```bash
    steghide embed -cf picture.jpg -ef secret.txt
    ```

# 🐧 Linux Mastery: Part 25 - Benchmarking & Kernel Tracing

This section focuses on pushing your hardware to its limits to test stability and using eBPF to watch the kernel's "internal organs" while it works.

---

## 🏎️ 1. System Benchmarking (`sysbench`)
`sysbench` is a modular, cross-platform tool for evaluating CPU, memory, file I/O, and database performance.

### **`sysbench`**
* **CPU Benchmark (Prime numbers):**
    ```bash
    sysbench cpu --cpu-max-prime=20000 run
    ```
* **Memory Benchmark (Read/Write speed):**
    ```bash
    sysbench memory --memory-block-size=1K --memory-total-size=10G run
    ```
* **File I/O Benchmark:**
    ```bash
    # Prepare: Create 2GB of test files
    sysbench fileio --file-total-size=2G prepare
    # Run: Perform random read/write tests
    sysbench fileio --file-total-size=2G --file-test-mode=rndrw run
    # Cleanup: Delete the test files
    sysbench fileio --file-total-size=2G cleanup
    ```

---

## 🌋 2. Stress Testing (`stress-ng`)
Unlike benchmarking (which measures speed), stress testing checks for **stability**. It tries to "break" the system by making it work as hard as possible.

### **`stress-ng`**
* **Stress the CPU:**
    ```bash
    stress-ng --cpu 4 --timeout 60s  # Use 4 cores for 60 seconds
    ```
* **Stress the Memory (RAM):**
    ```bash
    stress-ng --vm 2 --vm-bytes 1G --timeout 60s  # Spin up 2 workers using 1GB RAM each
    ```
* **Stress the Disk (I/O):**
    ```bash
    stress-ng --io 4 --timeout 30s
    ```
* **Matrix Math (CPU Floating Point):**
    ```bash
    stress-ng --matrix 1 --timeout 1m
    ```



---

## 🔎 3. Modern Kernel Tracing (`bpftrace`)
`bpftrace` uses eBPF technology to let you write "one-liner" scripts that peek inside the kernel without slowing it down.

### **`bpftrace`**
* **Trace Program Execution:**
    ```bash
    # Prints the name of every program as it starts
    sudo bpftrace -e 'tracepoint:syscalls:sys_enter_execve { printf("Exec: %s\n", str(args->filename)); }'
    ```
* **Count Syscalls by Process:**
    ```bash
    # Shows which programs are making the most system calls
    sudo bpftrace -e 'tracepoint:raw_syscalls:sys_enter { @[comm] = count(); }'
    ```
* **Disk I/O Latency (Histogram):**
    ```bash
    # Visualizes how long disk reads take in nanoseconds
    sudo bpftrace -e 'kprobe:vfs_read { @start[tid] = nsecs; } kretprobe:vfs_read /@start[tid]/ { @ns = hist(nsecs - @start[tid]); delete(@start[tid]); }'
    ```



---

## 📊 4. Advanced Performance Events (`perf`)
`perf` is the official Linux profiler. It can count hardware events like CPU cache misses.

### **`perf`**
* **System-wide CPU Profiling:**
    ```bash
    sudo perf top  # See which kernel/user functions are using the most CPU right now
    ```
* **Record and Report:**
    ```bash
    # Record data for 10 seconds
    sudo perf record -a sleep 10
    # Generate a report from that data
    sudo perf report
    ```
* **List All Supported Events:**
    ```bash
    perf list
    ```

---

## 🛠️ 5. Low-Level I/O Testing (`fio`)
If `sysbench` is for general testing, `fio` (Flexible I/O Tester) is for extreme, detailed disk performance analysis.

### **`fio`**
* **Example (Sequential Read Test):**
    ```bash
    fio --name=test --filename=/tmp/testfile --size=1G --rw=read --bs=4k --direct=1 --numjobs=4 --runtime=30 --group_reporting
    ```
    * `bs=4k`: Block size of 4KB.
    * `direct=1`: Bypass the Linux OS cache (test the hardware directly).

---

# 🐧 Linux Mastery: Part 26 - Automation & Infrastructure at Scale

This section covers the command-line tools that turn a System Administrator into a DevOps Engineer. We focus on automation, cloud management, and virtual machine control.

---

## 🤖 1. Automation & Configuration (`ansible`)
Ansible allows you to manage 100+ servers by typing a single command. It uses SSH and requires no "agent" on the remote machines.

### **`ansible`**
* **Ad-hoc Command (Ping all servers):**
    ```bash
    ansible all -m ping -i inventory.ini
    ```
* **Run a Command as Root on all servers:**
    ```bash
    ansible webservers -a "uptime" -u username --become
    ```

### **`ansible-playbook`**
Runs a "Playbook" (a YAML file containing the desired state of your servers).
* **Example:**
    ```bash
    ansible-playbook -i hosts.ini site.yml
    ```



---

## 🏗️ 2. Infrastructure as Code (`terraform`)
Terraform is used to "code" your hardware (VPCs, EC2 instances, S3 buckets) across any cloud provider.

### **`terraform`**
* **Initialize a project:**
    ```bash
    terraform init  # Downloads the necessary cloud drivers
    ```
* **Preview changes:**
    ```bash
    terraform plan  # Shows what will be created/deleted
    ```
* **Apply changes:**
    ```bash
    terraform apply -auto-approve
    ```

---

## ☁️ 3. Cloud Provider CLIs
Modern Linux administration often happens inside AWS, Azure, or GCP.

### **`aws` (Amazon Web Services CLI)**
* **List S3 Buckets:**
    ```bash
    aws s3 ls
    ```
* **List EC2 Instances:**
    ```bash
    aws ec2 describe-instances --query 'Reservations[*].Instances[*].[InstanceId,State.Name]'
    ```

### **`gcloud` (Google Cloud SDK)**
* **List Projects:**
    ```bash
    gcloud projects list
    ```

### **`az` (Azure CLI)**
* **List Resource Groups:**
    ```bash
    az group list --output table
    ```

---

## 🖥️ 4. Virtualization Management (`libvirt` & `virsh`)
`virsh` is the command-line interface for managing guest virtual machines (VMs) via the KVM/QEMU hypervisor.

### **`virsh`**
* **List all VMs:**
    ```bash
    sudo virsh list --all
    ```
* **Start/Stop a VM:**
    ```bash
    sudo virsh start my_ubuntu_vm
    sudo virsh shutdown my_ubuntu_vm
    ```
* **Edit VM Hardware Config:**
    ```bash
    sudo virsh edit my_ubuntu_vm  # Opens the XML config in Vim
    ```

### **`virt-top`**
A `top`-like utility specifically for showing the CPU/RAM usage of running Virtual Machines.
* **Example:**
    ```bash
    sudo virt-top
    ```



---

## 🛡️ 5. Secret Management (`vault`)
HashiCorp Vault is the standard for storing API keys, passwords, and certificates securely.

### **`vault`**
* **Read a Secret:**
    ```bash
    vault kv get secret/my-app/config
    ```
* **Login to Vault:**
    ```bash
    vault login -method=github
    ```

---

## 🏁 6. Miscellaneous Scale Tools

### **`parallel` (GNU Parallel)**
A shell tool for executing jobs in parallel using one or more computers.
* **Example:**
    ```bash
    # Gzip all log files using all available CPU cores simultaneously
    ls *.log | parallel gzip
    ```

### **`serf`**
A decentralized solution for cluster membership, failure detection, and orchestration.
* **Example:**
    ```bash
    serf members  # See all other nodes in your server cluster
    ```

# 🐧 Linux Mastery: Part 27 - Kubernetes, Service Mesh & Observability

This section focuses on managing containerized applications at scale and the "telemetry" tools used to monitor them.

---

## ☸️ 1. Container Orchestration (`kubectl`)
`kubectl` is the command-line tool for communicating with a Kubernetes cluster's control plane.

### **`kubectl`**
* **Get Cluster Info:**
    ```bash
    kubectl cluster-info
    ```
* **List All Running Pods:**
    ```bash
    kubectl get pods -A  # -A stands for all namespaces
    ```
* **View Live Logs of a Container:**
    ```bash
    kubectl logs -f <pod-name>
    ```
* **Execute a Command inside a Pod:**
    ```bash
    kubectl exec -it <pod-name> -- /bin/bash
    ```
* **Describe a Resource (Deep Troubleshooting):**
    ```bash
    kubectl describe node <node-name>  # See CPU/RAM pressure on a specific server
    ```



---

## 🕸️ 2. Service Mesh (`istioctl`)
When you have hundreds of microservices, you use a Service Mesh like Istio to manage security and traffic between them.

### **`istioctl`**
* **Analyze the Cluster for Config Errors:**
    ```bash
    istioctl analyze
    ```
* **Check Proxy Status:**
    ```bash
    istioctl proxy-status  # See if the "sidecar" containers are synced
    ```
* **View Dashboard (Kiali):**
    ```bash
    istioctl dashboard kiali
    ```

---

## 🚀 3. Helm (The Kubernetes Package Manager)
Just as `apt` is for Linux, `helm` is for Kubernetes.

### **`helm`**
* **Search for a Chart (Application):**
    ```bash
    helm search hub grafana
    ```
* **Install an Application:**
    ```bash
    helm install my-release bitnami/mysql
    ```
* **List Installed Releases:**
    ```bash
    helm list
    ```

---

## 📊 4. Observability & Log Shipping
In a distributed system, you don't `ssh` into every server to read logs; you ship them to a central location.

### **`vector`**
A high-performance observability data pipeline. It can collect, transform, and route logs and metrics.
* **Example:**
    ```bash
    vector --config /etc/vector/vector.yaml
    ```

### **`fluentd` / `td-agent`**
An open-source data collector for a unified logging layer.
* **Example:**
    ```bash
    fluentd -c my-config.conf
    ```

### **`promtail`**
The agent that ships local logs to **Loki** (the "Grafana" of logs).
* **Example:**
    ```bash
    promtail -config.file=config.yaml
    ```



---

## 🛠️ 5. Advanced Network Debugging (Part 3)

### **`socat` (Socket Cat)**
The successor to `netcat`. It is used for bidirectional data transfer between two independent data channels.
* **Example (Relay port 80 to 8080):**
    ```bash
    socat TCP-LISTEN:80,fork TCP:localhost:8080
    ```

### **`conntrack`**
Allows you to search, list, and delete entries in the kernel's network connection tracking table.
* **Example:**
    ```bash
    sudo conntrack -L  # See all currently tracked network connections
    ```

### **`ethtool`**
Used to query and control network device driver and hardware settings (e.g., speed, duplex).
* **Example:**
    ```bash
    sudo ethtool eth0  # Check if your network card is running at 1Gbps or 10Gbps
    ```

# 🐧 Linux Mastery: Part 28 - Cloud-Native Security & Persistence

This section bridges the gap between raw Linux commands and the "Service Reliability" layer of the cloud.

---

## 🛡️ 1. Container Vulnerability Scanning (`trivy`)
Before you deploy a container, you must ensure it doesn't contain known security holes (CVEs).

### **`trivy`**
* **Scan a Docker Image for Vulnerabilities:**
    ```bash
    trivy image alpine:3.15
    ```
* **Scan a Filesystem or Repository:**
    ```bash
    trivy fs /path/to/project
    ```
* **Scan a Kubernetes Cluster:**
    ```bash
    trivy k8s --report summary cluster
    ```



---

## 💾 2. Cloud-Native Storage Management
In Kubernetes, containers are ephemeral (they die and restart). You need specialized tools to manage "Persistent Volumes" (PVs).

### **`longctl` (Longhorn CLI)**
Used to manage Longhorn, a distributed block storage system for Kubernetes.
* **Example:**
    ```bash
    longctl volume list
    ```

### **`rook` / `ceph`**
Ceph is the "giant" of distributed storage. It provides file, block, and object storage in one cluster.
* **Commands:**
    ```bash
    ceph status       # Check health of the storage cluster
    ceph osd tree     # See the status of physical disks in the cluster
    ```



---

## 🕵️ 3. Distributed Tracing Concepts
When a user clicks "Buy," that request might hit a Gateway, an Auth service, a Billing service, and a Shipping service. Tracing helps you find which one is slow.

### **`jaeger` / `otel-collector` (OpenTelemetry)**
While these are mostly APIs, the CLI tools help you validate that data is flowing.
* **`otelcol`**: The OpenTelemetry Collector binary.
* **Example:**
    ```bash
    otelcol --config config.yaml  # Starts the telemetry pipeline
    ```

---

## 🛠️ 4. Advanced Linux Capability Auditing

### **`capsh` (Capability Shell)**
A tool to explore and constrain Linux capabilities (root powers broken into small pieces).
* **Example:**
    ```bash
    capsh --print  # See which "root powers" your current shell has
    ```

### **`getpcaps`**
Displays the capabilities of a specific running process.
* **Example:**
    ```bash
    getpcaps 1234  # See what process 1234 is allowed to do (e.g., CAP_NET_BIND_SERVICE)
    ```

---

## 📡 5. Modern Network Performance (`iperf3`)
The standard tool for measuring maximum TCP and UDP bandwidth performance between two Linux machines.

### **`iperf3`**
* **On Server A (Receiver):**
    ```bash
    iperf3 -s
    ```
* **On Server B (Sender):**
    ```bash
    iperf3 -c <Server_A_IP> -t 10  # Run a 10-second speed test
    ```

---

## 📊 6. Resource Usage Benchmarking

### **`mc` (MinIO Client)**
If you use S3-compatible storage (like MinIO or AWS S3), `mc` is the best tool to manage it via the terminal.
* **Example:**
    ```bash
    mc cp my_local_backup.tar.gz my_s3/backups/
    mc du my_s3/backups  # Check disk usage on the remote cloud storage
    ```

### **`ncdu` (Remote Check)**
You can use `ncdu` over SSH to scan remote servers visually.
* **Example:**
    ```bash
    ssh user@remote-host -t ncdu /
    ```

# 🐧 Linux Mastery: Part 29 - Reliability, Monitoring & Chaos

This section focuses on observing system health, tuning high-traffic gateways, and intentionally breaking things to test resilience.

---

## 📈 1. Infrastructure Monitoring (`Prometheus`)
Prometheus is the industry standard for time-series monitoring. It "scrapes" metrics from your Linux servers and applications.

### **`node_exporter`**
A helper binary that runs on every Linux server to expose hardware and OS metrics.
* **Example:**
    ```bash
    ./node_exporter --collector.meminfo --collector.cpu
    # Metrics are now available at http://localhost:9100/metrics
    ```

### **`promtool`**
The Swiss Army knife for Prometheus developers to validate configurations and query data.
* **Example:**
    ```bash
    promtool check config prometheus.yml  # Verify syntax before restarting
    promtool query instant http://localhost:9090 "up" # Check which targets are alive
    ```

### **`amtool`**
The CLI tool for managing alerts in **Alertmanager**.
* **Example:**
    ```bash
    amtool silence add alertname=NodeDown  # Silence an alert during maintenance
    ```



---

## 🏎️ 2. High-Performance Load Balancing

### **`nginx` (Tuning & Testing)**
Beyond just serving files, Nginx acts as a high-speed reverse proxy.
* **Check Configuration:**
    ```bash
    sudo nginx -t
    ```
* **Hot Reload (No downtime):**
    ```bash
    sudo nginx -s reload
    ```

### **`haproxy`**
A specialized load balancer used for extreme traffic (Layer 4 and Layer 7).
* **Check stats via CLI:**
    ```bash
    echo "show stat" | sudo socat stdio /var/run/haproxy.sock
    ```



---

## 🌪️ 3. Chaos Engineering (`Chaos Mesh`)
Chaos Engineering is the practice of injecting "failure" into a system to see if it survives.

### **`chaosctl`**
The CLI tool for **Chaos Mesh** to manage experiments in a Kubernetes/Linux environment.
* **Example (Inject Network Latency):**
    ```bash
    # Artificially add 100ms lag to all network traffic for a specific pod
    chaosctl inject network-latency --latency 100ms --duration 5m
    ```

### **`stress-ng` (Chaos mode)**
As seen in Part 25, but used here to simulate "noisy neighbors" in a shared server environment.
* **Example:**
    ```bash
    stress-ng --cyclic 1 --timeout 1m # Test for real-time scheduling jitter
    ```

---

## 🛡️ 4. Advanced System Recovery (Part 3)

### **`magic sysrq` keys**
A set of key combinations in the Linux kernel that allow you to perform low-level tasks even if the system is "frozen."
* **Example (Command Line version):**
    ```bash
    # Force a safe reboot when the keyboard/mouse are unresponsive
    echo "b" | sudo tee /proc/sysrq-trigger
    ```

### **`kdump` / `crash`**
Used to capture and analyze "Kernel Panics" (Blue Screen of Death for Linux).
* **Example:**
    ```bash
    sudo crash /var/crash/2026-01-18/vmcore /usr/lib/debug/boot/vmlinux
    ```

---

## 📡 5. DNS Performance & Troubleshooting

### **`dog`**
A modern, colorful alternative to `dig`.
* **Example:**
    ```bash
    dog google.com MX @1.1.1.1
    ```

### **`dnstop`**
Displays statistics about DNS traffic on your network interface.
* **Example:**
    ```bash
    sudo dnstop -l 3 eth0  # See top 3 domains being requested from this server
    ```

---

## 🛠️ 6. Process Priority & Real-time Tuning

### **`chrt`**
Manipulates the real-time attributes of a process. Useful for audio processing or high-frequency trading apps.
* **Example:**
    ```bash
    sudo chrt -f -p 99 1234  # Set process 1234 to Maximum Real-time Priority (FIFO)
    ```

### **`taskset`**
Binds a process to a specific CPU core (CPU Affinity).
* **Example:**
    ```bash
    taskset -cp 0,1 1234  # Force process 1234 to only run on CPU cores 0 and 1
    ```