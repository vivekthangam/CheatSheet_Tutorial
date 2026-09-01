[Back to Home](../README.md) | [Interview Prep Guide](interview_prep.md) | [Tech Glossary](glossary.md)

# 🐳 Docker Mastery: The Architect's Infrastructure Hub

Master the art of containerization. From immutable image design and layer optimization to complex multi-container orchestration and production-grade storage drivers.

---

## 📑 Table of Contents
1. [🗺️ The 5-Phase Docker Roadmap](#the-5-phase-docker-roadmap)
2. [🏗️ Dockerfile Vocabulary: Good vs. Bad](#dockerfile-vocabulary-good-vs-bad)
3. [💾 Storage Deep Dive (Volumes & Bind Mounts)](#storage-deep-dive)
4. [🌐 Networking Deep Dive (Bridge, Host, Overlay)](#networking-deep-dive)
5. [🐙 Docker Compose V2 Mastery](#docker-compose-v2-mastery)
6. [🛠️ The Ultimate Docker CLI Cheat Sheet](#the-ultimate-docker-cli-cheat-sheet)

---

## 🗺️ The 5-Phase Docker Roadmap

| Phase | Level | Focus Area | Goal |
| :--- | :--- | :--- | :--- |
| **Phase 1** | **Foundations** | Architecture, Images, Containers. | Solve the "It works on my machine" problem. |
| **Phase 2** | **Data & Networking** | Volumes, Bind Mounts, Bridge Network. | Stop losing data; let containers "talk." |
| **Phase 3** | **Orchestration** | Docker Compose V2, YAML Syntax. | Manage 10 containers with 1 command. |
| **Phase 4** | **Optimization** | Multi-stage Builds, Layer Caching. | Reduce image size from 1GB to 50MB. |
| **Phase 5** | **Production** | Swarm/K8s, Secrets, Non-root Users. | Secure and scale for the Chennai Fintech market. |

---

## 🏗️ Dockerfile Vocabulary: Good vs. Bad

Mastering the **Dockerfile Instructions** is the difference between a Junior and a Senior Architect.

| Instruction | ❌ The "Bad" Newbie Way | ✅ The "Good" Professional Way | The "Hidden" Feature |
| :--- | :--- | :--- | :--- |
| **FROM** | `FROM node:latest` (Unpredictable) | `FROM node:18-alpine` (Small & Fixed) | Supports **Multi-stage builds** to slim down images. |
| **WORKDIR** | `RUN cd /app && npm install` (Messy) | `WORKDIR /app` (Clean/Predictable) | Automatically **creates the folder** if it doesn't exist. |
| **COPY** | `COPY . .` at the top (Breaks cache) | `COPY package.json ./` first (Cached) | Can change file ownership using `--chown`. |
| **RUN** | `RUN apt-get update` (Multiple layers) | `RUN apt-get update && apt install...` | Use `--mount=type=cache` for ultra-fast builds. |
| **USER** | (Left as Root) | `USER 1000` (Security) | Prevents "Container Breakout" hacker attacks. |
| **HEALTHCHECK** | (Missing it) | `HEALTHCHECK CMD curl -f ...` | Automatically **restarts** a "frozen" container. |
| **ENTRYPOINT** | `CMD node app.js` (No signals) | `ENTRYPOINT ["node", "app.js"]` | Makes the container behave like a **fixed tool**. |

---

## 💾 Storage Deep Dive

### 1. The 4 Types of Storage
1.  **Named Volumes:** Managed by Docker. **Best for Databases.** Survives container deletion.
2.  **Bind Mounts:** Maps a local folder to a container. **Best for Development.** Changes on your PC show instantly inside the container.
3.  **tmpfs Mounts:** Stores data only in **RAM**. **Best for Secrets.** Wiped instantly when the container stops.
4.  **Anonymous Volumes:** No name. Used to "shield" folders (like `node_modules`) from being overwritten by a bind mount.

### 2. Volume Drivers (Cloud Connectors)
*   **Local:** Stores data on the server's hard drive.
*   **NFS:** Connects to a shared Network File System. Best for clusters of servers.
*   **REX-Ray / Cloud Drivers:** Connects directly to **AWS EBS** or **Azure Disk**, allowing your data to "follow" your container if it moves servers.

---

## 🌐 Networking Deep Dive

*   **Bridge (Default):** A private "office network." Containers talk via **Name** (DNS) instead of IP.
*   **Host:** Removes isolation. Container uses the host's IP directly. **Fastest performance.**
*   **Overlay:** Connects multiple servers together. Containers on "Server 1" talk to "Server 5" as if they were on the same desk.
*   **None:** Total isolation. High-security batch processing.

---

## 🐙 Docker Compose V2 Mastery

Compose is your "Project Manager." It orchestrates the **App**, the **Database**, and the **Network** in one YAML.

### The "Pro" Compose Template
```yaml
services:
  api:
    build: .                 # Uses your Dockerfile
    ports: ["3000:3000"]
    volumes: ["./src:/app/src:ro"] # 💻 Bind Mount (Source Code Sync)
    depends_on: ["db"]       # ⌛ Startup Order
    networks: ["fintech-net"]

  db:
    image: postgres:15-alpine
    volumes: ["db_data:/var/lib/postgresql/data"] # ☁️ Managed Volume
    environment:
      - POSTGRES_PASSWORD_FILE=/run/secrets/db_pass
    networks: ["fintech-net"]

volumes:
  db_data:                   # Define managed storage

networks:
  fintech-net:               # Define private network
```

---

## 🛠️ The Ultimate Docker CLI Cheat Sheet

### Construction & Management
*   **The Deep Clean:** `docker system prune` (Wipe all unused images/vols).
*   **The X-Ray:** `docker inspect <id>` (See IP, Env vars, and Volumes).
*   **The CCTV:** `docker logs -f <id>` (Tail the live app output).
*   **The SSH:** `docker exec -it <id> sh` (Login inside the container).

### Optimization Checklist
> [!TIP]
> **Production Best Practices:**
> 1. Use **Multi-Stage Builds** to keep images small.
> 2. Always create a **.dockerignore** to skip `node_modules` or `.git`.
> 3. Never store passwords in **ENV**. Use **Docker Secrets**.
> 4. Set `requests` and `limits` when moving to Kubernetes.

---

[⬆️ Back to Top](#🐳-docker-mastery-the-architects-infrastructure-hub)
