# ☸️ Kubernetes Mastery: Part 1 - Architecture & The API Machinery

This section covers the "Brain" and "Nervous System" of the cluster. Understanding the internals is key to troubleshooting production issues.

---

## 🏗️ 1. The Control Plane (Master) Internals
The Control Plane is responsible for maintaining the desired state of your cluster.

* **kube-apiserver**: The "Front Door." All communication (from `kubectl` or nodes) goes here. It is the only component that talks to the database.
* **etcd**: The "Memory." A distributed key-value store that holds the entire state of the cluster. **Tip:** Never lose your etcd backups!
* **kube-scheduler**: The "Matcher." It looks at new Pods and assigns them to the best Node based on resource availability and constraints.
* **kube-controller-manager**: The "Regulator." It runs loops that handle node failures, replicate pods, and manage endpoints.



---

## 🛡️ 2. Admission Controllers (The Gatekeepers)
Often skipped in tutorials, these are plugins that intercept requests to the API server **after** you are authenticated but **before** the object is saved.

* **Mutating Admission**: Can modify the YAML (e.g., injecting a sidecar container like Istio or Jaeger).
* **Validating Admission**: Can reject the YAML (e.g., "You cannot run a container as root").

---

## 🏷️ 3. Namespaces & Labels
* **Namespaces**: Virtual isolation. Use these to separate `dev`, `staging`, and `prod`.
* **Labels**: Key-value pairs used for organizing. 
* **Selectors**: How you query labels. A Service uses a selector to find which Pods to send traffic to.

---

# ☸️ Kubernetes Mastery: Part 2 - Workload & Lifecycle Management

Kubernetes categorizes applications based on how they behave.

## 📦 1. Stateless vs. Stateful Workloads

### **A. Deployments (Stateless)**
Perfect for your Spring Boot APIs or React frontends. They manage ReplicaSets to ensure pods are replaced if they crash.
* **Strategy**: Supports `RollingUpdate` (zero downtime) or `Recreate`.



### **B. StatefulSets (Stateful)**
Essential for databases like PostgreSQL or MongoDB.
* **Ordinal Index**: Pods are named `db-0`, `db-1`, etc.
* **Stable Storage**: When `db-0` restarts, it automatically reconnects to its exact same virtual disk.

---

## 🧹 2. Jobs & CronJobs (Task-Based)
* **Job**: Runs a container until it successfully finishes its task (e.g., a DB migration).
* **CronJob**: Runs a Job on a specific time schedule using standard Crontab syntax.

---

## 🛠️ Part 1 & 2: Pro-Level Commands

| Scenario | Command |
| :--- | :--- |
| **Check API status** | `kubectl get componentstatuses` |
| **See raw API resources** | `kubectl api-resources` |
| **Explain a specific field** | `kubectl explain pod.spec.containers.imagePullPolicy` |
| **List pods with labels** | `kubectl get pods --show-labels` |
| **Filter by label** | `kubectl get pods -l app=nginx,env=prod` |

---

# ☸️ Kubernetes Mastery: Part 3 - Networking & Service Discovery

Kubernetes networking ensures that every Pod has its own IP and can communicate across the cluster without NAT.

---

## 🕸️ 1. Service Types (The Entry Points)
Pods are ephemeral (they die and get new IPs). A **Service** provides a stable network abstraction.

* **ClusterIP (Default)**: Exposes the service on a cluster-internal IP. Use this for internal microservice communication (e.g., API to DB).
* **NodePort**: Exposes the service on each Node’s IP at a static port (30000-32767). Useful for simple external access.
* **LoadBalancer**: Provisions an external Load Balancer in supported cloud environments (AWS, GCP, Azure). 
* **Headless Service**: Set `clusterIP: None`. Instead of a single IP, a DNS lookup returns the IPs of all underlying Pods. Critical for **StatefulSets** (Databases).



---

## 🛣️ 2. Ingress & Ingress Controllers
Ingress acts as a smart router or reverse proxy (like Nginx) sitting at the edge of your cluster.

* **Path-Based Routing**: `domain.com/api` goes to the backend; `domain.com/` goes to the frontend.
* **Host-Based Routing**: `api.domain.com` and `web.domain.com` handled by different services.
* **TLS Termination**: Handles SSL certificates in one place rather than in every app.

---

## 🛡️ 3. NetworkPolicies (The Firewall)
By default, Kubernetes has a "Flat Network" (any pod can talk to any pod). **NetworkPolicies** allow you to implement a "Zero Trust" model.

* **Ingress Rules**: Control who can talk TO the pod.
* **Egress Rules**: Control who the pod can talk TO.



---

# ☸️ Kubernetes Mastery: Part 4 - Storage & Persistence

Since container filesystems are wiped on restart, we need a way to store data permanently.

## 💾 1. The Storage Abstraction Layer
Kubernetes separates "Requesting Storage" from "Providing Storage."

1. **StorageClass (SC)**: The "Menu." Defines tiers of storage (e.g., "fast-ssd" or "standard-hdd").
2. **PersistentVolume (PV)**: The "Actual Disk." A piece of storage in the cluster provisioned by an admin or a cloud.
3. **PersistentVolumeClaim (PVC)**: The "Request." A user's claim for a specific size and access mode.



---

## 🔄 2. Access Modes
When defining storage, you must choose how many nodes can connect:
* **ReadWriteOnce (RWO)**: Mounted by a single node (Best for Databases).
* **ReadOnlyMany (ROX)**: Many nodes can read (Best for static assets).
* **ReadWriteMany (RWX)**: Many nodes can read and write (Requires NFS or specialized cloud storage).

---

## 📂 3. Projected Volumes (Deep Dive)
A specialized volume type that maps existing data sources into the same directory:
* **Secrets**: Passwords/Certs.
* **ConfigMaps**: Configuration files.
* **DownwardAPI**: Injecting Pod metadata (like its own IP or Namespace) as a file.

---

## 🛠️ Part 3 & 4: Essential Commands

| Scenario | Command |
| :--- | :--- |
| **Check Services** | `kubectl get svc -A` |
| **Test Internal DNS** | `kubectl run test --rm -it --image=alpine -- nslookup <service-name>` |
| **View Ingress Rules** | `kubectl get ingress` |
| **List PV/PVC Status** | `kubectl get pv,pvc` |
| **Check Storage Classes** | `kubectl get sc` |

---

# ☸️ Kubernetes Mastery: Part 5 - Security, RBAC & Secrets

Security in Kubernetes follows the principle of "Least Privilege." We control who can do what via Role-Based Access Control (RBAC).

---

## 🛡️ 1. RBAC (Role-Based Access Control)
RBAC determines permissions by linking a **Subject** (User/Group/ServiceAccount) to a **Role**.

* **Role**: Defines permissions (verbs like `get`, `list`, `create`) within a **specific namespace**.
* **ClusterRole**: Defines permissions across the **entire cluster** (useful for nodes or global resources).
* **RoleBinding**: The bridge that assigns a Role to a User or ServiceAccount.
* **ServiceAccount**: An identity created for a Pod. For example, if your Spring Boot app needs to talk to the K8s API to list other pods, it needs a ServiceAccount.



---

## 🔑 2. Secrets & ConfigMaps
* **ConfigMap**: Stores non-sensitive configuration (e.g., `application.properties` or environment names).
* **Secret**: Stores sensitive data (e.g., DB passwords, API keys, SSL certs). 
  * *Note*: Secrets are Base64 encoded, not encrypted by default. You should use a tool like "Sealed Secrets" or "HashiCorp Vault" for true security.



---

# ☸️ Kubernetes Mastery: Part 6 - Observability & Resource Management

How Kubernetes ensures your application stays healthy and doesn't crash the server.

## 🏥 1. Probes (The Health Checks)
Kubernetes doesn't just check if a container is "running"; it checks if it is "working."

1. **Liveness Probe**: "Are you alive?" If this fails, K8s kills the container and restarts it.
2. **Readiness Probe**: "Are you ready for traffic?" If this fails, K8s removes the Pod from the Service's Load Balancer so users don't see 500 errors.
3. **Startup Probe**: "Are you still booting up?" Used for heavy apps (like legacy Java) to prevent Liveness probes from killing them before they finish starting.

---

## ⚖️ 2. Resource Quotas & Limits
To prevent a "Noisy Neighbor" (one pod taking all the RAM), we define resource constraints.

* **Requests**: The minimum guaranteed amount of CPU/RAM. The Scheduler uses this to find a Node.
* **Limits**: The maximum amount a container can use. If a container hits its RAM limit, it is **OOM Killed** (Out of Memory).



---

## 📉 3. QoS (Quality of Service) Classes
Kubernetes assigns a priority to your pod based on your resource definitions:
* **Guaranteed**: Requests == Limits (Highest priority).
* **Burstable**: Requests < Limits.
* **Best-Effort**: No requests/limits defined (First to be killed during node pressure).

---

## 🛠️ Part 5 & 6: Security & Health Commands

| Scenario | Command |
| :--- | :--- |
| **Check RBAC permissions** | `kubectl auth can-i create pods --as=system:serviceaccount:ns:sa` |
| **View secret data (decoded)** | `kubectl get secret <name> -o jsonpath='{.data.password}' | base64 --decode` |
| **Monitor pod health events** | `kubectl get events --field-selector involvedObject.name=<pod-name>` |
| **Check resource usage** | `kubectl top pod` (Requires Metrics Server) |
| **View Pod QoS Class** | `kubectl get pod <name> -o jsonpath='{.status.qosClass}'` |

---

# ☸️ Kubernetes Mastery: Part 7 - Advanced Scheduling & Node Control

This section explains how Kubernetes decides exactly "where" a pod should live and how to handle specialized hardware or maintenance.

---

## 🏗️ 1. Taints and Tolerations
Think of a **Taint** as a "No Entry" sign on a Node. Only Pods with a matching **Toleration** can enter.
* **Scenario**: You have a Node with an expensive GPU. You Taint it so that only "GPU-heavy" jobs land there, preventing regular Nginx pods from wasting that resource.



---

## 🔗 2. Node Affinity & Anti-Affinity
While Taints repel, **Affinity** attracts.
* **Node Affinity**: "I only want to run on nodes with SSDs."
* **Pod Anti-Affinity**: "Do not run two copies of this API on the same physical server." (Essential for High Availability).

---

## 🧹 3. Node Maintenance (Cordon & Drain)
When you need to update the Linux kernel on a server, you don't just turn it off.
1. **Cordon**: Marks the node as "Unschedulable" (no new pods).
2. **Drain**: Gracefully evicts all existing pods and moves them to other nodes.

---

# ☸️ Kubernetes Mastery: Part 8 - The Full-Stack Java Deployment Scenario

This is the "Grand Finale" where we deploy your **Expense Manager** (React + Spring Boot + Postgres) using all the concepts learned.

## 🏗️ 1. Architecture Map
* **Persistence Layer**: PostgreSQL via `StatefulSet` + `PVC`.
* **Business Logic**: Spring Boot via `Deployment` + `ConfigMap` + `Secret`.
* **Web Tier**: React via `Deployment` + `Service` (LoadBalancer).



---

## 📄 2. Production-Grade Java Manifest
This YAML includes the "Deep-Dive" features like non-root users, resource limits, and specific probes.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: expense-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: expense-api
  template:
    metadata:
      labels:
        app: expense-api
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
      containers:
      - name: spring-boot-app
        image: your-repo/expense-api:v1.0
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: app-config
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 40 # Java needs time to boot
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080

# ☸️ The Kubernetes Concept Map: 100+ Deep-Dive Topics

This list covers the architectural, operational, and security concepts required to master Kubernetes.

---

## 🏗️ Group 1: The Control Plane (The Brain)
These concepts explain how the cluster "thinks" and stays healthy.

1.  **Desired State**: The core philosophy of K8s. You define what you want; K8s makes it happen.
2.  **Reconciliation Loop**: The continuous check K8s performs to match "Actual State" to "Desired State."
3.  **Declarative vs. Imperative**: Using YAML (Declarative) vs. Commands (Imperative).
4.  **Control Plane Isolation**: Why we don't run application pods on Master nodes.
5.  **etcd Quorum**: Understanding the majority-vote system for cluster data consistency.
6.  **API Versioning**: Alpha, Beta, and GA (Stable) versions of resources.
7.  **Admission Controllers**: Mutating vs. Validating webhooks that filter every request.
8.  **CRDs (Custom Resource Definitions)**: Extending K8s to create your own types (e.g., `kind: Kafka`).
9.  **The Scheduler (kube-scheduler)**: The logic used to place Pods on Nodes.
10. **Controller Manager**: The bundle of loops (Node, Route, ServiceAccount controllers).

---

## 📦 Group 2: Workload Management (The Muscles)
How we run code and manage its lifecycle.

11. **Pod**: The smallest deployable unit.
12. **Sidecar Pattern**: Helper containers (logs/proxy) running next to the main app.
13. **Init Containers**: Containers that run and finish before the app starts.
14. **Ephemeral Containers**: Temporary containers used for debugging live pods.
15. **Static Pods**: Pods managed by a Node's Kubelet directly (bypass API server).
16. **ReplicaSet**: Ensures a specific number of pod copies.
17. **Deployment Strategies**: RollingUpdate vs. Recreate.
18. **MaxSurge / MaxUnavailable**: Controlling update speed and availability.
19. **StatefulSet Ordinality**: Why Pods are named `0, 1, 2` instead of random hashes.
20. **Headless Services**: Direct Pod-to-Pod communication for DB clusters.
21. **DaemonSets**: Ensuring a pod runs on every single node (for logs/monitoring).
22. **Jobs**: One-off tasks (e.g., database migrations).
23. **CronJobs**: Scheduled tasks (e.g., nightly backups).
24. **Garbage Collection**: How K8s cleans up orphaned resources.
25. **Horizontal Pod Autoscaler (HPA)**: Scaling pods based on CPU/RAM metrics.
26. **Vertical Pod Autoscaler (VPA)**: Recommending or resizing pod resource limits.

---

## 🕸️ Group 3: Networking & Discovery (The Nervous System)
How parts of the system talk to each other.

27. **ClusterIP**: Internal-only service address.
28. **NodePort**: Opening a port on every node's physical IP.
29. **LoadBalancer**: Integrating with Cloud Providers (AWS/GCP/Azure).
30. **ExternalName**: CNAME mapping to external databases (e.g., RDS).
31. **Ingress**: Layer 7 (HTTP) routing and SSL termination.
32. **Ingress Controller**: The engine (Nginx/Traefik) that processes Ingress rules.
33. **Service Discovery**: Internal DNS (e.g., `my-service.my-namespace.svc.cluster.local`).
34. **EndpointSlice**: High-performance way to track network endpoints.
35. **NetworkPolicies**: The internal firewall (Ingress/Egress rules).
36. **CNI (Container Network Interface)**: The networking plugin (Calico, Flannel, Cilium).
37. **Service Mesh**: Advanced traffic management (Istio, Linkerd).
38. **mTLS (Mutual TLS)**: Automatic encryption between microservices.

---

## 💾 Group 4: Storage & Persistence (The Memory)
How data survives when a container dies.

39. **Volumes**: Temporary or permanent directories in a pod.
40. **PersistentVolume (PV)**: The physical storage resource.
41. **PersistentVolumeClaim (PVC)**: The developer's request for storage.
42. **StorageClass**: Automating the creation of disks (Dynamic Provisioning).
43. **Access Modes**: ReadWriteOnce (RWO), ReadWriteMany (RWX), etc.
44. **Reclaim Policy**: What happens to data when a PVC is deleted (Retain vs. Delete).
45. **CSI (Container Storage Interface)**: How K8s talks to external storage vendors.
46. **ConfigMap**: Injecting configuration files or env vars.
47. **Secrets**: Injecting sensitive data (Base64 encoded).
48. **Projected Volumes**: Bundling Secrets, ConfigMaps, and Metadata into one path.

---

## 🛡️ Group 5: Scheduling & Node Logic (The Environment)
How K8s manages the relationship between Pods and Servers.

49. **Nodes**: Physical or Virtual machines.
50. **Kubelet**: The node-level agent.
51. **Taints**: Nodes repelling pods ("Don't come here").
52. **Tolerations**: Pods allowed on tainted nodes ("I'm okay with that").
53. **Node Affinity**: Pods preferring certain nodes ("I want to run on SSD nodes").
54. **Pod Anti-Affinity**: "Don't put two copies of this on the same server" (High Availability).
55. **Cordoning**: Marking a node as unschedulable.
56. **Draining**: Gracefully evicting all pods from a node for maintenance.
57. **Eviction Policy**: How K8s decides which pod to kill when RAM is low.
58. **QoS Classes**: Best-Effort, Burstable, Guaranteed.
59. **Resource Requests**: Minimum resources guaranteed to a pod.
60. **Resource Limits**: The "Ceiling" resources a pod can use (OOM Kill trigger).
61. **Pod Disruption Budget (PDB)**: Ensuring a minimum % of pods stay up during maintenance.

---

## 📈 Group 6: Observability & Maintenance
Keeping an eye on the cluster.

62. **Liveness Probe**: "Are you still running?" (Restarts if fail).
63. **Readiness Probe**: "Are you ready for traffic?" (Removes from Load Balancer if fail).
64. **Startup Probe**: "Are you still booting?" (Disables other probes until done).
65. **Termination Grace Period**: The time given to a pod to shut down (default 30s).
66. **Events**: The cluster log showing what happened to resources.
67. **Metrics Server**: Provides data for `kubectl top`.
68. **Namespaces**: Logical isolation for teams/environments.

---

## 🔒 Group 7: RBAC & Governance (The Security)
Who can do what?

69. **Role**: Permissions within a namespace.
70. **ClusterRole**: Permissions across the whole cluster.
71. **RoleBinding**: Connecting a User/ServiceAccount to a Role.
72. **ServiceAccount**: Identity for a Pod to talk to the K8s API.
73. **Contexts**: kubeconfig settings for switching between clusters.
74. **Impersonation**: Testing permissions as if you were another user.
75. **Pod Security Standards**: Privileged, Baseline, and Restricted profiles.



# ☸️ Kubernetes Deep Dive: Part 1 & Part 2

This guide explores the internal mechanics of the Kubernetes Control Plane (The Brain) and the Workload Resources (The Muscles).

---

## 🧠 Part 1: The Brain (Control Plane)

The Control Plane makes global decisions about the cluster (e.g., scheduling) and detects/responds to cluster events.

### 1.1 kube-apiserver (The Hub)
* **Deep Dive**: It is the central management entity. It is the only component that communicates with `etcd`. It serves as the gateway for all REST commands from users (via `kubectl`) and external/internal components.
* **Key Lifecycle**: Authentication -> Authorization -> Admission Control.
* **Real-World Example**: Like an **Air Traffic Controller**. Every pilot (component) must talk to the tower (API Server) before taking off or landing. No one moves without its approval.



### 1.2 etcd (The Source of Truth)
* **Deep Dive**: A consistent, highly-available key-value store used as Kubernetes' backing store for all cluster data. It uses the **Raft Consensus Algorithm** to ensure that data is replicated across multiple nodes without corruption.
* **Real-World Example**: Like a **Bank Ledger**. It doesn't matter how many branches (nodes) the bank has; every branch must agree on exactly how much money is in your account (the state of the cluster).

### 1.3 kube-scheduler (The Matchmaker)
* **Deep Dive**: It watches for newly created Pods that have no assigned node. It filters nodes based on resource requirements (CPU/RAM), data locality, and policy constraints (Taints/Affinity), then ranks them to find the "best" fit.
* **Real-World Example**: Like a **Hotel Receptionist**. When a guest (Pod) arrives, the receptionist checks which rooms (Nodes) are clean, have enough beds, and match the guest's preferences before handing over the key.

### 1.4 kube-controller-manager (The Regulator)
* **Deep Dive**: It runs "Control Loops." It is a non-terminating loop that regulates the state of the system. For example, if a node goes down, the **Node Controller** notices and moves the pods to another node.
* **Real-World Example**: Like a **Cruise Control** in a car. You set the speed (Desired State). If the car slows down on a hill, the controller adds gas (Reconciliation) to get back to the set speed.

---

## 📦 Part 2: The Muscles (Workloads)

Workloads are the resources that actually run your containers and handle your application's logic.

### 2.1 Pods (The Atomic Unit)
* **Deep Dive**: A Pod is a wrapper around one or more containers. Containers in a Pod share the same **Network Namespace** (IP Address) and **Storage Volumes**. They are ephemeral—they are designed to be replaced, not repaired.
* **Concept - The Pause Container**: Every Pod has a hidden container that holds the network namespace open, even if your app container crashes and restarts.
* **Real-World Example**: Like a **Pod of Peas**. The peas (containers) are distinct, but they live inside the same shell (Pod) and share the same nutrients (resources).



### 2.2 Deployments (Stateless Scaling)
* **Deep Dive**: Manages a **ReplicaSet**. It allows you to define a "Desired State" for your application (e.g., "I want 5 copies of Nginx"). It handles **Rolling Updates**—slowly replacing old pods with new ones to ensure zero downtime.
* **Real-World Example**: Like a **Starbucks Manager**. If the manager says there must be 3 baristas on shift, and one gets sick (Pod crashes), the manager automatically calls in a replacement to maintain the count.

### 2.3 StatefulSets (Stable Identity)
* **Deep Dive**: Unlike Deployments, Pods here have a **stable identity**. They are named `app-0`, `app-1`, etc. If a pod dies, it is recreated with the same name and reattached to its specific persistent disk.
* **Real-World Example**: Like **Assigned Seating in a Classroom**. Student 0 must always sit in Desk 0 because that is where their specific books and notes (Persistent Data) are stored.

### 2.4 DaemonSets (The Infrastructure)
* **Deep Dive**: Ensures that a copy of a specific Pod runs on **all** (or some) Nodes. When a new Node is added to the cluster, the DaemonSet automatically adds the Pod to it.
* **Real-World Example**: Like **Security Guards in a Building**. Every floor (Node) must have exactly one guard (Pod) to monitor the floor. You don't need 10 guards on one floor and none on the other.



---

## 🛠️ Summary Command Reference

| Action | Command |
| :--- | :--- |
| **Check Master Components** | `kubectl get componentstatuses` |
| **List All Workloads** | `kubectl get all -A` |
| **Inspect Controller Events** | `kubectl get events --sort-by='.lastTimestamp'` |
| **Force Restart a Deployment** | `kubectl rollout restart deployment <name>` |
| **Describe Pod Internals** | `kubectl describe pod <pod-name>` |

---

# ☸️ Kubernetes Deep Dive: Part 3 & Part 4

This section covers how Kubernetes components talk to each other (The Nervous System) and how data survives when containers die (The Memory).

---

## 🕸️ Part 3: Networking (The Nervous System)

Kubernetes networking is built on a "Flat Network" model where every Pod has a unique IP and can talk to any other Pod without NAT.

### 3.1 Services (The Stable Front Door)
* **Deep Dive**: Pods are ephemeral; their IPs change. A **Service** is a virtual object that provides a stable IP and DNS name. It uses **Selectors** to identify which Pods to send traffic to.
* **Service Types**:
    * **ClusterIP**: Internal-only IP. Used for communication between services (e.g., API to DB).
    * **NodePort**: Opens a port (30000-32767) on every Node's physical IP.
    * **LoadBalancer**: Integrates with Cloud Providers to give you a real external IP.
* **Real-World Example**: Like a **Company Switchboard**. You don't call the employee's personal cell phone (Pod IP); you call the office extension (Service IP), and the switchboard routes you to whoever is at their desk.



### 3.2 Ingress (The Traffic Cop)
* **Deep Dive**: While a Service works at the Layer 4 (TCP), Ingress works at **Layer 7 (HTTP/S)**. It manages external access to services, typically via HTTP, providing load balancing, SSL termination, and name-based virtual hosting.
* **Real-World Example**: Like a **Mall Directory**. You enter through one main door (Ingress), and based on the path you want (/shoes or /food), the directory tells you exactly which store (Service) to go to.



### 3.3 CoreDNS & Service Discovery
* **Deep Dive**: Kubernetes runs a built-in DNS service. When a Pod wants to talk to a service named `user-db`, it simply pings `user-db`. CoreDNS translates that name into the Service's ClusterIP.
* **Real-World Example**: Like your **Phone's Contacts List**. You don't memorize numbers (IPs); you just click on "Mom" (Service Name), and the phone knows which number to dial.

---

## 💾 Part 4: Storage (The Memory)

In Kubernetes, storage is decoupled from the Pod's lifecycle so that data persists even if a container restarts or moves.

### 4.1 PersistentVolumes (PV) vs. Claims (PVC)
* **Deep Dive**:
    * **PersistentVolume (PV)**: A piece of storage in the cluster that has been provisioned by an administrator or dynamically provisioned using Storage Classes.
    * **PersistentVolumeClaim (PVC)**: A request for storage by a user. It is similar to a Pod; Pods consume node resources, and PVCs consume PV resources.
* **Real-World Example**: Like **Renting an Apartment**. The building owner has many empty apartments (PVs). You submit an application stating you need 2 bedrooms (PVC). The owner matches your application to a specific unit and gives you the key (Binding).



### 4.2 StorageClasses (Dynamic Provisioning)
* **Deep Dive**: Instead of an admin creating 100 disks manually, they create a **StorageClass**. When a user creates a PVC, the StorageClass tells the Cloud Provider (AWS/GCP/Azure) to create a new disk automatically.
* **Real-World Example**: Like a **Vending Machine**. You don't wait for an employee to hand you a soda; you put your money in (PVC), and the machine dispenses the product (PV) instantly.

### 4.3 ConfigMaps & Secrets
* **Deep Dive**: 
    * **ConfigMap**: Used for non-sensitive data (e.g., `application-dev.properties`). 
    * **Secret**: Used for sensitive data (e.g., DB passwords). Both can be injected into Pods as environment variables or mounted as files.
* **Real-World Example**: Like a **Safe vs. a Bookshelf**. You keep your public manuals on the bookshelf (ConfigMap) where anyone can read them, but you keep your jewelry and passport in the safe (Secret).



---

## 🛠️ Networking & Storage Command Reference

| Goal | Command |
| :--- | :--- |
| **Check Services** | `kubectl get svc -A` |
| **Debug DNS** | `kubectl run curl --image=radial/busyboxplus:curl -i --tty` |
| **View Storage Usage** | `kubectl get pv,pvc` |
| **Decode a Secret** | `kubectl get secret <name> -o jsonpath='{.data.key}' | base64 --decode` |
| **Check Ingress Rules** | `kubectl describe ingress <name>` |

---

# ☸️ Kubernetes Deep Dive: Part 5 & Part 6

This final section focuses on the "Shield" (Security and Permissions) and the "Strategy" (How Pods are placed on specific servers).

---

## 🛡️ Part 5: Security & RBAC (The Shield)

Kubernetes uses **Role-Based Access Control (RBAC)** to regulate access to computer or network resources based on the roles of individual users within an enterprise.

### 5.1 Roles and ClusterRoles
* **Deep Dive**: 
    * **Role**: Sets permissions within a specific **Namespace** (e.g., "User can only read pods in 'dev'").
    * **ClusterRole**: Sets permissions cluster-wide (e.g., "Admin can view nodes across the whole cluster").
* **Verbs**: RBAC uses standard verbs like `get`, `list`, `watch`, `create`, `update`, `patch`, and `delete`.
* **Real-World Example**: Like **Building Security Badges**. A regular employee's badge only opens the front door and their office (Role). A maintenance worker's badge opens every mechanical room in the entire building (ClusterRole).



### 5.2 ServiceAccounts
* **Deep Dive**: While Users are for humans, **ServiceAccounts** are for processes. When your Spring Boot app needs to talk to the K8s API (perhaps to discover other pods), it uses a ServiceAccount identity.
* **Real-World Example**: Like an **App Integration Token**. You don't give a third-party app your personal password; you give it a specific API Key (ServiceAccount) that only allows it to do certain tasks.

### 5.3 NetworkPolicies
* **Deep Dive**: By default, K8s is "Allow All." A **NetworkPolicy** is a pod-level firewall. It defines which pods can talk to which pods using labels and port numbers.
* **Real-World Example**: Like a **VIP Lounge**. Even if you are in the same club (Cluster), you can't enter the VIP area (Secure Pod) unless you are on the list (NetworkPolicy).



---

## 📅 Part 6: Advanced Scheduling (The Strategy)

Scheduling is the process of deciding which Node should run a Pod. While the Scheduler does this automatically, you can influence its decision.

### 6.1 Taints and Tolerations
* **Deep Dive**: 
    * **Taint**: Applied to a **Node**. It tells the cluster: "Don't put pods here unless they have a specific exception."
    * **Toleration**: Applied to a **Pod**. It tells the cluster: "I am allowed to run on nodes with this specific taint."
* **Real-World Example**: Like **Specialized Hospital Wards**. A "Contagious Ward" (Node) has a Taint. Only doctors with "Protective Gear" (Toleration) are allowed to enter.

### 6.2 Node Affinity & Anti-Affinity
* **Deep Dive**: 
    * **Node Affinity**: Forces or prefers a Pod to run on nodes with specific labels (e.g., "Only run on nodes with SSDs").
    * **Pod Anti-Affinity**: Prevents pods from running on the same node (e.g., "Don't put two replicas of the same DB on the same server" to avoid a single point of failure).
* **Real-World Example**: Like **Roommates**. Affinity is "I want to live in a house with a pool." Anti-Affinity is "I don't want to live in the same house as my twin" (so if the house burns down, one of us survives).



### 6.3 Resource Quotas & Limits
* **Deep Dive**: 
    * **Requests**: The minimum resources a pod needs to start. 
    * **Limits**: The absolute maximum resources a pod is allowed to consume. If a pod exceeds its RAM limit, it is **OOMKilled** (Out of Memory).
* **Real-World Example**: Like a **Fixed-Price Buffet**. The "Request" is your reserved table (guaranteed space). The "Limit" is the maximum amount of food you are allowed to take before the manager asks you to leave.

---

## 🛠️ Security & Scheduling Command Reference

| Goal | Command |
| :--- | :--- |
| **Check RBAC Permissions** | `kubectl auth can-i create pods --as system:serviceaccount:ns:sa` |
| **Taint a Node** | `kubectl taint nodes <node-name> key=value:NoSchedule` |
| **View Node Labels** | `kubectl get nodes --show-labels` |
| **Check Resource Usage** | `kubectl top pods` |
| **View Pod Security Context** | `kubectl get pod <name> -o jsonpath='{.spec.securityContext}'` |

---

## 🎓 Final Mastery Wrap-Up
You now have a complete README reference for:
1. **The Brain** (Control Plane Internals)
2. **The Muscles** (Workloads & Pods)
3. **The Nervous System** (Networking & Services)
4. **The Memory** (Storage & Persistence)
5. **The Shield** (RBAC & Security)
6. **The Strategy** (Scheduling & Affinity)


# ☸️ The Kubernetes Advanced Ops Manual: 100+ Production Scenarios

This document acts as a "Cookbook" for managing, troubleshooting, and optimizing Kubernetes clusters in real-world environments.

---

## 🛠️ 1. Troubleshooting & Incident Response (25 Scenarios)

### **Scenario 1: The "Invisible" Crash (CrashLoopBackOff)**
* **Context:** Container crashes before logs can be streamed.
* **Action:** `kubectl logs <pod-name> --previous` to see the exit code of the dead container.

### **Scenario 2: Debugging a Distroless/Minimal Image**
* **Context:** You need to check the network but the image has no `curl` or `sh`.
* **Action:** `kubectl debug -it <pod-name> --image=nicolaka/netshoot --target=<container-name>`

### **Scenario 3: "Terminating" Pod is Stuck**
* **Context:** A Pod won't die due to finalizers or a disconnected Kubelet.
* **Action:** `kubectl delete pod <name> --grace-period=0 --force`

### **Scenario 4: Service is up but returning 404**
* **Context:** Service exists but has no Endpoints.
* **Action:** `kubectl get endpoints <service-name>` — usually a label selector mismatch.

### **Scenario 5: Node Pressure Eviction**
* **Context:** A Node is running out of disk or RAM and killing your pods.
* **Action:** `kubectl describe node <name> | grep -A 5 Conditions` to check for `DiskPressure`.



---

## 🚀 2. Resource Management & Performance (20 Scenarios)

### **Scenario 6: Identifying Resource "Slack" (Over-provisioning)**
* **Context:** You are paying for big nodes but pods use 10% of their requests.
* **Action:** `kubectl top pods -A --sort-by=memory` vs. the requests defined in YAML.

### **Scenario 7: Preventing "OOM Killer" Loops**
* **Context:** Java heap is higher than K8s memory limit.
* **Action:** Ensure `Xmx` is set to ~75% of the K8s limit.
* **Command:** `kubectl describe pod <name> | grep -i "Reason: OOMKilled"`

### **Scenario 8: Pod Disruption Budgets (PDBs)**
* **Context:** You need to ensure a minimum of 2 replicas stay up during node maintenance.
* **Action:** Create a `kind: PodDisruptionBudget` with `minAvailable: 2`.

---

## 🕸️ 3. Networking & Traffic Control (20 Scenarios)

### **Scenario 9: Testing Internal DNS Resolution**
* **Context:** Microservice A cannot find Microservice B.
* **Action:** `kubectl run dns-test -it --rm --image=busybox:1.28 -- nslookup <service-name>.<namespace>.svc.cluster.local`

### **Scenario 10: Mapping External Services (RDS/S3) as K8s Services**
* **Context:** You want your app to connect to `db-prod` without hardcoding AWS URLs.
* **Action:** Create a Service `kind: Service` with `type: ExternalName`.

### **Scenario 11: SSL/TLS Termination at Ingress**
* **Context:** Move HTTPS logic out of Spring Boot to the edge.
* **Action:** Add `tls` block to Ingress and use `cert-manager` for automated Let's Encrypt certs.



---

## 🛡️ 4. Security & Compliance (20 Scenarios)

### **Scenario 12: Auditing "Privileged" Containers**
* **Context:** Finding pods that could potentially escape to the host node.
* **Action:** `kubectl get pods -A -o jsonpath='{.items[?(@.spec.containers[*].securityContext.privileged==true)].metadata.name}'`

### **Scenario 13: Rotating Compromised Secrets**
* **Context:** A developer accidentally pushed a K8s secret to GitHub.
* **Action:** Update the Secret and run `kubectl rollout restart deployment <name>` to force pods to reload.

### **Scenario 14: Network Isolation between Environments**
* **Context:** Stop "Dev" pods from talking to "Prod" pods.
* **Action:** Apply a `NetworkPolicy` with `podSelector: {}` and `policyTypes: ["Ingress"]` to deny all by default.

---

## 📉 5. Cluster Maintenance & Scaling (15 Scenarios)

### **Scenario 15: Safe Node Upgrades**
* **Context:** You need to patch the OS on `node-01`.
* **Action:** `kubectl cordon node-01` followed by `kubectl drain node-01 --ignore-daemonsets`.

### **Scenario 16: Scaling based on Custom Metrics (SQS Queue Size)**
* **Context:** Scale pods based on how many messages are in a queue, not CPU.
* **Action:** Use **KEDA (Kubernetes Event-driven Autoscaling)**.

---

## 📋 The "Super" CLI Reference Table

| Objective | Advanced Command |
| :--- | :--- |
| **Hot Fix** | `kubectl patch deployment <name> -p '{"spec": {"replicas": 10}}'` |
| **View Node Usage** | `kubectl get nodes -o custom-columns=NAME:.metadata.name,CPU_ALC:.status.allocatable.cpu,MEM_ALC:.status.allocatable.memory` |
| **Port Forward** | `kubectl port-forward svc/<svc-name> 8080:80` |
| **Copy File to Pod** | `kubectl cp ./config.xml <pod-name>:/app/config.xml` |
| **Check API Access** | `kubectl auth can-i create deployment --namespace project-x` |

---

## 📈 Group 6: Advanced "Under-the-Hood" Scenarios

1.  **Orphaned Pods:** Deleting a Deployment but keeping Pods via `--cascade=orphan`.
2.  **Taint-Toleration Mismatch:** Pod is `Pending` because it doesn't tolerate the `node-role.kubernetes.io/master` taint.
3.  **Image Pull Secret Failure:** App fails because it can't authenticate with a private Docker Registry.
4.  **HPA Thrashing:** Pods keep scaling up and down (Flapping). Fix: Increase `stabilizationWindowSeconds`.
5.  **Service Type "LoadBalancer" Pending:** Cloud provider ran out of Static IPs.

---

# ☸️ The Kubernetes Advanced Ops Manual: Part 2 (Scenarios 101-200)

This section focuses on the intersection of High-Performance Java (Spring Boot) and Hardened Security.

---

## ☕ 6. Java & Spring Boot Optimization (50 Scenarios)

### **Scenario 101: The "Invisible" JVM OOM**
* **Context:** The container stays running, but Java throws `java.lang.OutOfMemoryError`. 
* **Action:** K8s won't restart the pod because the process hasn't died. 
* **Solution:** Configure Liveness Probes to hit an Actuator endpoint that checks heap health.

### **Scenario 102: Tuning Java for CGroups v2**
* **Context:** Java 8u131 or older doesn't "see" K8s memory limits and tries to use the whole Node's RAM.
* **Action:** Upgrade to Java 11/17+ or use `-XX:+UseContainerSupport`.

### **Scenario 103: Slow Startup causing Liveness Failure**
* **Context:** Spring Boot takes 45s to start; K8s kills it after 20s because the Liveness probe failed.
* **Solution:** Use **Startup Probes** to give the app a "grace period" before Liveness kicks in.



### **Scenario 104: Database Connection Pool Exhaustion**
* **Context:** 10 pods each having a `max-active` of 50 connections hit a DB that only allows 200.
* **Action:** Scale down `replicas` or reduce `HikariCP` pool size in `ConfigMap`.

### **Scenario 105: Remote Debugging a Java Pod**
* **Context:** You need to attach IntelliJ/Eclipse to a pod running in Dev.
* **Action:** `kubectl port-forward <pod-name> 5005:5005` after starting JVM with `-agentlib:jdwp=...address=*:5005`.

### **Scenario 106: JVM DNS Caching Issues**
* **Context:** You scaled a service, but the Java client keeps hitting the old Pod IPs.
* **Solution:** Set `networkaddress.cache.ttl` to a low value (e.g., 60s) in the JVM security policy.

---

## 🛡️ 7. Security Hardening & Compliance (50 Scenarios)

### **Scenario 151: Preventing HostPath Mounts**
* **Context:** A malicious pod tries to mount `/etc/shadow` from the host server.
* **Solution:** Apply a **Pod Security Admission** (PSA) policy to "Restricted" mode for the namespace.

### **Scenario 152: Restricting "Automount" ServiceAccount Tokens**
* **Context:** Most pods don't need to talk to the K8s API. Leaving tokens mounted is a risk.
* **Action:** Set `automountServiceAccountToken: false` in the Pod spec.

### **Scenario 153: Finding Vulnerable Images (Log4Shell style)**
* **Context:** You need to find all pods running an old version of an image.
* **Command:** ```bash
  kubectl get pods -A -o custom-columns=NAME:.metadata.name,IMAGE:.spec.containers[*].image | grep "v1.2.0"










# ☸️ The Ultimate Kubernetes Deep-Dive & 200+ Scenario Manual

This documentation covers the complete Kubernetes ecosystem, from architectural internals to advanced production troubleshooting.

---

## 🏗️ PART 1: The Control Plane (The Brain)
Kubernetes operates on a "Desired State" model via a continuous reconciliation loop.

* **kube-apiserver**: The central hub. All communication (kubectl, nodes) goes here. Only component talking to `etcd`.
* **etcd**: Distributed Key-Value store. The "Source of Truth." Uses Raft consensus.
* **kube-scheduler**: Matches Pods to Nodes based on resource availability, Taints, and Affinity.
* **kube-controller-manager**: Runs loops to handle node failures, replication, and endpoints.
* **Admission Controllers**: 
    * *Mutating*: Modifies YAML (e.g., sidecar injection).
    * *Validating*: Rejects YAML (e.g., security policy violations).



---

## 📦 PART 2: Workload Resources (The Muscles)

### 2.1 Atomic & Stateless Units
* **Pods**: Smallest unit. Containers share IP (via Pause container) and Storage.
* **Deployments**: Manages ReplicaSets. Best for stateless apps (Java APIs). Supports RollingUpdates.
* **DaemonSets**: Runs one pod on every node (Logging/Monitoring agents).

### 2.2 Stateful & Task Units
* **StatefulSets**: Stable network IDs (`db-0`, `db-1`) and persistent storage. Best for Databases.
* **Jobs/CronJobs**: Run-to-completion tasks or scheduled scripts.



---

## 🕸️ PART 3: Networking & Service Discovery (The Nervous System)

* **Services**: Stable IP/DNS for ephemeral pods.
    * *ClusterIP*: Internal only.
    * *NodePort*: Port on every Node IP.
    * *LoadBalancer*: Cloud-provider external IP.
* **Ingress**: Layer 7 (HTTP) routing. Handles Hostnames, Paths, and SSL Termination.
* **NetworkPolicies**: Pod-level firewall. Implements "Zero Trust" by blocking all traffic by default.



---

## 💾 PART 4: Storage & Persistence (The Memory)

* **PersistentVolume (PV)**: The physical disk.
* **PersistentVolumeClaim (PVC)**: The developer's request for space.
* **StorageClass**: Dynamic provisioning (creates disks automatically when a PVC is created).
* **ConfigMaps/Secrets**: Injecting configuration and sensitive data as env vars or files.



---

## 🛠️ PART 5: 200+ Advanced Scenarios & Commands

### 🛡️ Security & Hardening (Scenarios 1-50)
1.  **Read-Only Root FS**: `securityContext.readOnlyRootFilesystem: true` to prevent script execution.
2.  **Drop Capabilities**: `capabilities.drop: ["ALL"]` to minimize attack surface.
3.  **Find Vulnerabilities**: `kubectl get pods -A -o custom-columns=IMAGE:.spec.containers[*].image | grep "v1.2.0"`
4.  **No-Root**: `runAsNonRoot: true` to prevent container breakout.

### ☕ Java/Spring Boot Optimization (Scenarios 51-100)
1.  **JVM Awareness**: Use `-XX:+UseContainerSupport` for proper RAM usage.
2.  **Startup Probes**: Give Java 45s to boot before Liveness probes start killing it.
3.  **Heap Management**: Set Xmx to 75% of K8s memory limit to avoid OOMKills.
4.  **Actuator Health**: Link `readinessProbe` to `/actuator/health/readiness`.

### 🚨 Troubleshooting & Ops (Scenarios 101-200)
1.  **Debug Distroless**: `kubectl debug -it <pod> --image=nicolaka/netshoot`.
2.  **Stuck Namespace**: Patch the resource to remove `finalizers: []`.
3.  **Cordon & Drain**: `kubectl drain <node>` for safe maintenance.
4.  **Audit Permissions**: `kubectl auth can-i delete secrets --as=system:serviceaccount:ns:sa`.
5.  **Previous Logs**: `kubectl logs <pod> --previous` to see why it crashed.
6.  **Taint Nodes**: `kubectl taint nodes <node> key=value:NoSchedule` to reserve hardware.
7.  **Resource Audit**: `kubectl top pods -A --sort-by=memory`.
8.  **Port Forward**: `kubectl port-forward svc/db 5432:5432` for local DB access.
9.  **Scale by Label**: `kubectl scale deployment -l tier=frontend --replicas=0`.
10. **Hot Copy**: `kubectl cp <pod>:/app/logs ./local-logs`.



---

## 📊 Master Cheat Sheet

| Command | Action |
| :--- | :--- |
| `kubectl get events -w` | Watch cluster events in real-time |
| `kubectl explain pod.spec` | Deep-dive into YAML field documentation |
| `kubectl rollout undo deploy/app` | Rollback to the previous stable version |
| `kubectl get pods --field-selector=status.phase!=Running` | Find all failing pods |

---


# ☸️ Kubernetes Deep-Dive: Scenarios 1 - 50

This manual covers production-level scenarios from cluster internals to workload orchestration.

---

## 🏗️ 1. Cluster Architecture & Control Plane (1-10)

1.  **API Server Failure**: You cannot run any `kubectl` commands. 
    * *Fix:* Check the `kube-apiserver` static pod on the master node.
2.  **etcd Database Corruption**: The cluster loses all its "memory" (metadata).
    * *Fix:* Restore from the last snapshot using `etcdutl snapshot restore`.
3.  **Scheduler Bottleneck**: New pods are stuck in `Pending` even though nodes have space.
    * *Action:* Check scheduler logs; ensure no `NodeSelector` is blocking all nodes.
4.  **Controller Manager Loop**: A deleted pod keeps coming back immediately.
    * *Concept:* The Reconciliation Loop is working as intended to maintain the "Desired State."
5.  **Kubelet Connection Loss**: A Node shows status `NotReady`.
    * *Fix:* SSH into the node and run `systemctl restart kubelet`.
6.  **Kube-Proxy Mode Mismatch**: Networking is slow.
    * *Action:* Check if proxy is using `iptables` or `IPVS` mode.
7.  **Admission Controller Rejection**: A pod won't create due to "Policy violation."
    * *Concept:* A Validating Admission Webhook is blocking the request.
8.  **etcd Quorum Loss**: 2 out of 3 etcd nodes are down.
    * *Action:* The cluster becomes read-only; you must recover the majority.
9.  **API Latency**: `kubectl` commands take 10 seconds to respond.
    * *Action:* Check API Server CPU usage and etcd disk I/O wait times.
10. **Custom Resource Extension**: You need to manage a non-native object (like a Database).
    * *Action:* Use a **CRD** (Custom Resource Definition) and a custom Controller.



---

## 📦 2. Pod Lifecycle & Design Patterns (11-20)

11. **Pod Shared Network**: Two containers in one Pod need to talk.
    * *Action:* They use `localhost` because they share the same Network Namespace.
12. **The Pause Container**: You restart your app container but the Pod IP remains the same.
    * *Concept:* The `pause` container holds the network namespace open.
13. **Init Container Dependency**: Your Java app starts before the DB is ready and crashes.
    * *Fix:* Add an `initContainer` to ping the DB port before the main app starts.
14. **Sidecar Log Shipping**: You need to send logs to Splunk/ELK.
    * *Pattern:* Run a Fluentd sidecar that reads the log file from a shared `emptyDir`.
15. **Ephemeral Debugging**: A "Distroless" image has no shell to debug.
    * *Command:* `kubectl debug -it <pod> --image=busybox --target=<app>`.
16. **Static Pods**: You need a pod to run even if the API server is down.
    * *Action:* Place the YAML in `/etc/kubernetes/manifests` on the node.
17. **Pod Termination Grace Period**: Your app needs 60s to finish requests before dying.
    * *Action:* Set `terminationGracePeriodSeconds: 60` in the Pod spec.
18. **PostStart Hook**: You need to run a script immediately after the container starts.
    * *Action:* Use a `lifecycle.postStart` exec hook.
19. **PreStop Hook**: You need to gracefully shut down a legacy app that ignores SIGTERM.
    * *Action:* Use a `lifecycle.preStop` hook to trigger a shutdown script.
20. **ImagePullBackOff**: Pod cannot pull image from a private registry.
    * *Fix:* Create a `docker-registry` secret and add it to `imagePullSecrets`.

---

## 🚀 3. Deployments & Scaling (21-35)

21. **Zero-Downtime Update**: You need to update v1 to v2.
    * *Strategy:* Use `RollingUpdate` with `maxSurge: 1`.
22. **Instant Rollback**: v2 has a critical bug.
    * *Command:* `kubectl rollout undo deployment/<name>`.
23. **Manual Scaling**: Traffic spiked 500% on your Expense Manager.
    * *Command:* `kubectl scale deployment <name> --replicas=20`.
24. **Horizontal Pod Autoscaler (HPA)**: You want to scale automatically based on CPU.
    * *Action:* Create an HPA object targeting the deployment.
25. **Revision History Limit**: You have too many old ReplicaSets cluttering the cluster.
    * *Action:* Set `revisionHistoryLimit: 5` in the Deployment spec.
26. **Blue-Green Deployment**: You want to switch all traffic at once.
    * *Strategy:* Create two deployments and update the Service selector to point to the new one.
27. **Canary Deployment**: You want to test v2 on only 5% of users.
    * *Strategy:* Create a second deployment with 1 replica and the same labels as the Service.
28. **Deployment Pausing**: You need to make multiple changes without triggering multiple rollouts.
    * *Command:* `kubectl rollout pause deployment/<name>`.
29. **Recreate Strategy**: Your app doesn't support two versions running at once.
    * *Action:* Set `strategy.type: Recreate`.
30. **Stuck Deployment**: A rollout is taking too long.
    * *Action:* Check `progressDeadlineSeconds`; the rollout may have failed.
31. **Label Mismatch**: Your deployment created pods, but the Service can't find them.
    * *Action:* Match the `spec.selector` of the Service to the `spec.template.metadata.labels` of the Deployment.
32. **Self-Healing Check**: You manually delete a pod in a deployment.
    * *Result:* The Deployment controller automatically creates a replacement pod.
33. **Scaling to Zero**: You want to save costs on a dev environment at night.
    * *Command:* `kubectl scale deployment <name> --replicas=0`.
34. **Container Port Mapping**: Your app runs on 8080 but you want K8s to see it as 80.
    * *Action:* Map `containerPort: 8080` in the pod and `targetPort: 8080` in the service.
35. **Namespace Resource Quota**: You want to prevent one team from using all the cluster RAM.
    * *Action:* Create a `ResourceQuota` object in that team's namespace.

---

## 🕸️ 4. Networking & Service Discovery (36-50)

36. **Internal Service Discovery**: Microservice A needs to talk to Microservice B.
    * *Action:* Use the DNS name `service-b.namespace.svc.cluster.local`.
37. **Headless Service**: You need the IPs of all pods, not a single LoadBalancer IP.
    * *Action:* Set `clusterIP: None` in the Service spec.
38. **NodePort Access**: You want to access a pod from outside the cluster without a Cloud LB.
    * *Action:* Set `type: NodePort` (Ports 30000-32767).
39. **ExternalName**: You want to map a K8s service to an external AWS RDS URL.
    * *Action:* Use `type: ExternalName` with the `externalName` field set to the URL.
40. **CoreDNS Failure**: Pods cannot resolve any names.
    * *Action:* Check the `coredns` pods and service in the `kube-system` namespace.
41. **Service Session Affinity**: You want a user to stay on the same pod for their session.
    * *Action:* Set `sessionAffinity: ClientIP`.
42. **LoadBalancer Pending**: You are on-premise and `type: LoadBalancer` is stuck.
    * *Fix:* Install **MetalLB** to provide IP addresses.
43. **Ingress Path Routing**: `/api` should go to Backend, `/` should go to Frontend.
    * *Action:* Define paths in an **Ingress** resource.
44. **SSL Termination**: You want to handle HTTPS at the Ingress level.
    * *Action:* Add a `tls` section to the Ingress and reference a K8s Secret.
45. **Network Policy - Deny All**: You want to lock down a namespace for security.
    * *Action:* Create a `NetworkPolicy` with `podSelector: {}` and no ingress rules.
46. **Network Policy - Allow DB only**: Frontend should only talk to Backend.
    * *Action:* Create a Policy allowing ingress only from pods with label `app: frontend`.
47. **Multiple Ingress Controllers**: You have Nginx and Traefik in one cluster.
    * *Action:* Use `ingressClassName` to specify which controller should handle the rule.
48. **Port Forwarding**: You want to access a private DB pod from your laptop.
    * *Command:* `kubectl port-forward <pod-name> 5432:5432`.
49. **ExternalTrafficPolicy**: You want to preserve the Client Source IP.
    * *Action:* Set `externalTrafficPolicy: Local`.
50. **EndpointSlice Limit**: You have a service with 2000 pods.
    * *Concept:* K8s uses `EndpointSlices` to handle large-scale service discovery efficiently.
# ☸️ Kubernetes Deep-Dive: Scenarios 51 - 100

This section covers data persistence, identity management, and advanced security configurations.

---

## 💾 5. Storage & Persistence (51-65)

51. **Volume Lifespan**: Your container crashed and restarted; you lost your files.
    * *Fix:* Move files to a `Volume` (like `emptyDir`) so they survive container restarts within the same Pod.
52. **PersistentVolume (PV) vs. PVC**: You need a disk that survives even if the Pod is deleted.
    * *Concept:* PV is the physical disk; PVC is the "request" or "ticket" for that disk.
53. **StorageClass Automation**: You don't want to create 50 AWS EBS volumes manually.
    * *Action:* Use a `StorageClass` for **Dynamic Provisioning** so disks are created on-demand.
54. **AccessMode Conflict**: You want two different nodes to write to the same disk.
    * *Action:* You must use `ReadWriteMany` (RWX), usually backed by NFS or EFS, not standard block storage.
55. **Reclaim Policy**: You deleted a PVC and the data disappeared.
    * *Action:* Set `persistentVolumeReclaimPolicy: Retain` if you want to manually recover data later.
56. **ConfigMap Injection**: You want to change a Spring Boot property without rebuilding the image.
    * *Action:* Mount a `ConfigMap` as a file at `/app/config/application.properties`.
57. **Secret Security**: You stored a password in a ConfigMap and anyone can see it.
    * *Action:* Use a `Secret` resource. While Base64, it is the standard for sensitive data in K8s.
58. **Immutable Configs**: You want to prevent any accidental changes to a configuration.
    * *Action:* Set `immutable: true` in your ConfigMap/Secret.
59. **Projected Volumes**: You need a Secret, a ConfigMap, and Downward API data in one folder.
    * *Action:* Use a `projected` volume type to bundle multiple sources into one directory.
60. **Local Persistent Volumes**: You need extremely low latency for a database.
    * *Action:* Use `Local` PVs tied to specific SSDs on the physical host.
61. **Volume Expansion**: Your database disk is 90% full.
    * *Action:* Edit the PVC `storage` field (if the StorageClass supports `allowVolumeExpansion`).
62. **Database Snapshotting**: You need a backup before a major migration.
    * *Action:* Use the `VolumeSnapshot` resource.
63. **Mount Propagation**: A sidecar needs to see mounts created by the main container.
    * *Action:* Set `mountPropagation: Bidirectional`.
64. **SubPath Mounting**: You want to mount a single file from a ConfigMap without overwriting the whole folder.
    * *Action:* Use the `subPath` field in `volumeMounts`.
65. **Storage Capacity Tracking**: A pod is stuck `Pending` because the node's local disk is full.
    * *Action:* The scheduler uses CSI storage capacity tracking to avoid placing pods on "full" nodes.



---

## 🛡️ 6. RBAC, Identity & Access (66-80)

66. **ServiceAccount Identity**: Your Java pod needs to list other pods in the namespace.
    * *Action:* Create a `ServiceAccount` and assign it to the pod.
67. **Role vs. ClusterRole**: You want a user to only manage pods in the `dev` namespace.
    * *Action:* Create a `Role` (Namespaced) instead of a `ClusterRole` (Global).
68. **RoleBinding**: You created a Role but the user still gets "Access Denied."
    * *Action:* You must create a `RoleBinding` to link the User/Group to the Role.
69. **Least Privilege**: A developer has "Admin" access in production.
    * *Action:* Audit permissions and switch to a Role that only allows `get`, `list`, and `watch`.
70. **Automount Token Security**: Your pod doesn't need to talk to the API, but a token is mounted.
    * *Fix:* Set `automountServiceAccountToken: false` in the pod spec.
71. **Impersonation Testing**: You want to see what a specific developer can see.
    * *Command:* `kubectl auth can-i list pods --as=developer-name`.
72. **Default ServiceAccount**: You didn't specify a ServiceAccount.
    * *Concept:* K8s assigns the `default` SA, which usually has zero permissions.
73. **User Identity**: You want to add a new human user to the cluster.
    * *Action:* Kubernetes does not manage "User" objects; use X.509 certs or an OIDC provider (like Google/Okta).
74. **Aggregated ClusterRoles**: You want to combine multiple permissions into one "SuperRole."
    * *Action:* Use `aggregationRule` in a ClusterRole.
75. **Node Authorizer**: A hijacked node is trying to modify pods on another node.
    * *Concept:* The Node Authorizer restricts Kubelets so they can only modify their own resources.
76. **ServiceAccount Token Rotation**: You suspect a token was leaked.
    * *Action:* Delete the Secret associated with the SA; K8s will generate a new one.
77. **RBAC for Namespaces**: You want a user to have access to all namespaces starting with `team-`.
    * *Action:* Use a `ClusterRole` but bind it via a `RoleBinding` in *each* specific namespace.
78. **View-Only Access**: You want to give a manager a dashboard without delete power.
    * *Action:* Bind the built-in `view` ClusterRole.
79. **API Group Permissions**: You can manage Pods but not Deployments.
    * *Fix:* Add `apps` to the `apiGroups` list in your Role.
80. **Certificate Rotation**: The cluster stopped working because the internal certs expired.
    * *Action:* Run `kubeadm certs renew all`.



---

## ⚖️ 7. Advanced Scheduling & Governance (81-100)

81. **Node Affinity**: You want your "Expense Manager" to only run on nodes with SSDs.
    * *Action:* Label nodes `disk=ssd` and use `nodeAffinity` in the deployment.
82. **Taints & Tolerations**: You have a node reserved for GPU tasks only.
    * *Action:* **Taint** the node with `dedicated=gpu:NoSchedule`. Only pods with a **Toleration** can land there.
83. **Pod Anti-Affinity**: You don't want two replicas of the same DB on the same physical server.
    * *Action:* Use `podAntiAffinity` with `topologyKey: kubernetes.io/hostname`.
84. **Pod Priority & Preemption**: A high-priority "Production" pod needs to start, but the cluster is full.
    * *Action:* K8s will kill a low-priority "Test" pod to make room.
85. **Resource Quota Over-commitment**: A team requested 100 CPUs but the cluster only has 50.
    * *Action:* The `ResourceQuota` will reject any pod creation that exceeds the limit.
86. **LimitRange**: A developer forgot to set `limits` and their pod crashed the node.
    * *Fix:* Create a `LimitRange` to set mandatory default limits for all pods in that namespace.
87. **Taint-Based Eviction**: A node started having network issues.
    * *Concept:* K8s automatically taints the node `unreachable` and begins moving pods after a timeout.
88. **Pod Disruption Budget (PDB)**: You are upgrading the cluster and don't want all replicas to go down at once.
    * *Action:* Set `minAvailable: 2` in a PDB.
89. **Cordoning a Node**: You need to perform hardware maintenance.
    * *Command:* `kubectl cordon <node>` (Stops new pods from landing).
90. **Draining a Node**: You are decommissioning a server.
    * *Command:* `kubectl drain <node>` (Evicts all current pods gracefully).
91. **Pod Topology Spread Constraints**: You want pods distributed evenly across AWS Availability Zones.
    * *Action:* Use `topologySpreadConstraints` with `topologyKey: topology.kubernetes.io/zone`.
92. **Preemption Policy**: You want a high-priority pod to wait for space rather than killing others.
    * *Action:* Set `preemptionPolicy: Never`.
93. **Scheduler Profiles**: You want a different scheduling logic for Batch Jobs vs. Web APIs.
    * *Action:* Configure multiple profiles in the `kube-scheduler` config.
94. **Node Selector**: The simplest way to pin a pod to a node.
    * *Action:* Use `nodeSelector` with a specific node label.
95. **Overhead**: You are running Katacoda/VMs inside pods and need extra RAM for the host.
    * *Action:* Use the `RuntimeClass` overhead field.
96. **Descheduler**: Your cluster has become "unbalanced" over time.
    * *Action:* Run the **Descheduler** tool to re-balance pods based on current load.
97. **Vertical Pod Autoscaler (VPA)**: You don't know what resource limits to set for a new app.
    * *Action:* Deploy VPA in `Recommender` mode to see real-world usage data.
98. **Horizontal Pod Autoscaler (HPA) Cooldown**: Your app scales up and down too fast (Thrashing).
    * *Fix:* Tune the `stabilizationWindowSeconds`.
99. **Cluster Autoscaler (CA)**: All nodes are full and 10 pods are `Pending`.
    * *Action:* The CA will automatically request a new VM from your cloud provider.
100. **PriorityClass for System Pods**: You want to ensure CoreDNS is never killed.
    * *Action:* Assign it the `system-cluster-critical` PriorityClass.

# ☸️ Kubernetes Advanced Scenarios: 101 - 150

This part focuses on real-world debugging and the deep optimization of Java/Spring Boot applications in a production cluster.

---

## 🔍 8. Troubleshooting & Debugging (101-115)

101. **Pod stuck in `Pending`**
    * **Context:** You deployed your app, but no pods are running.
    * **Fix:** Run `kubectl describe pod <name>`. Check for `Events`. Usually caused by insufficient CPU/RAM or a `Taint` that isn't tolerated.

102. **Pod stuck in `CrashLoopBackOff`**
    * **Context:** The container starts and immediately exits.
    * **Fix:** Run `kubectl logs <name> --previous`. This is vital for seeing the stack trace of the JVM crash *before* the restart.

103. **`ImagePullBackOff` on Private Registry**
    * **Context:** K8s cannot find your Docker image.
    * **Fix:** Verify the `imagePullSecrets` in your Deployment and ensure the Secret exists in that specific namespace.

104. **Service returning 404/503 but Pod is running**
    * **Context:** Your LoadBalancer is up, but the app isn't reachable.
    * **Action:** Check `kubectl get endpoints`. If empty, your Service `selector` labels do not match your Pod labels.

105. **Node status `NotReady`**
    * **Context:** A worker node stops accepting pods.
    * **Action:** Check the Kubelet service on the host (`systemctl status kubelet`) and check for disk exhaustion (`df -h`).



106. **Debugging "Distroless" Containers**
    * **Context:** You need a shell, but the image has no `/bin/sh`.
    * **Command:** `kubectl debug -it <pod-name> --image=busybox --target=<app-container>`.

107. **Network Sniffing between Microservices**
    * **Context:** You suspect an API handshake is failing.
    * **Action:** Use an ephemeral debug container with `netshoot` to run `tcpdump` on the pod’s network interface.

108. **Stuck `Terminating` Pod**
    * **Context:** A pod won't delete even after a delete command.
    * **Fix:** `kubectl delete pod <name> --grace-period=0 --force`.

109. **DNS Resolution Failure**
    * **Context:** Java app can't find `db-service`.
    * **Action:** Check `coredns` logs and verify if the service is in a different namespace (use `<svc>.<ns>.svc.cluster.local`).

110. **CPU Throttling despite Low Usage**
    * **Context:** App is slow but CPU isn't at 100%.
    * **Fix:** Check `container_cpu_cfs_throttled_seconds_total`. If high, your `limit` is too low for the app's burst requirements.

111. **OOMKill (Exit Code 137)**
    * **Context:** Pod suddenly restarts without logs.
    * **Action:** `kubectl describe pod` will show `Reason: OOMKilled`. You must increase the memory `limit` or tune JVM heap.

112. **Inbound Traffic Blocked**
    * **Context:** Service is fine, but connection times out.
    * **Fix:** Check `NetworkPolicy` ingress rules and the Service `targetPort`.

113. **Local IDE Debugging**
    * **Context:** You want to step through code running in K8s.
    * **Command:** `kubectl port-forward <pod-name> 5005:5005`.

114. **Viewing Historic Cluster Errors**
    * **Context:** Something broke 10 minutes ago, and the pod is gone.
    * **Command:** `kubectl get events --sort-by='.lastTimestamp' -A`.

115. **Pre-flight YAML Check**
    * **Context:** You want to see what a change will do before applying it.
    * **Command:** `kubectl diff -f deployment.yaml`.

---

## ☕ 9. Java & Spring Boot Production Tuning (116-135)

116. **JVM Container Awareness**
    * **Context:** Old Java 8 apps trying to use the Node's total RAM.
    * **Fix:** Upgrade to Java 11/17+ or use `-XX:+UseContainerSupport`.

117. **Spring Boot "Slow Start" Failure**
    * **Context:** Bean initialization takes 60s; K8s kills it at 30s.
    * **Fix:** Use a `startupProbe` to give the app a grace period before `liveness` starts.

118. **Liveness vs. Readiness for Spring Actuator**
    * **Action:** Map `readinessProbe` to `/actuator/health/readiness` and `livenessProbe` to `/actuator/health/liveness`.

119. **Graceful Shutdown (SIGTERM)**
    * **Context:** Active requests are dropped during a rollout.
    * **Fix:** Set `server.shutdown=graceful` in Spring and `terminationGracePeriodSeconds: 60` in K8s.

120. **HikariCP Connection Pool Tuning**
    * **Context:** 10 replicas exhausting a DB with a 100-connection limit.
    * **Action:** Reduce `maximum-pool-size` per pod to avoid DB "Too many connections" errors.

121. **JVM Heap vs. Container Limit**
    * **Rule:** Never set `-Xmx` equal to the K8s limit. 
    * **Action:** Set `Xmx` to ~75% of the limit to leave room for Metaspace, Threads, and Stack.

122. **Logging to Standard Out**
    * **Problem:** `kubectl logs` is empty because app writes to a file.
    * **Fix:** Redirect Logback/Log4j2 to `STDOUT`.

123. **Trusting Internal CA Certs**
    * **Context:** Java app calling an internal HTTPS service.
    * **Fix:** Mount the CA cert as a Secret and use an `initContainer` to import it into the Java TrustStore.

124. **Remote Profiling**
    * **Context:** You need to debug a memory leak in K8s.
    * **Action:** Use `kubectl cp` to move an async-profiler agent into the pod and attach it.

125. **DNS Caching in JVM**
    * **Problem:** Java caches DNS results forever by default.
    * **Fix:** Set `networkaddress.cache.ttl=60` in `java.security` to honor K8s service changes.



---

## 📈 10. Observability & Monitoring (136-150)

136. **Prometheus Scraping**
    * **Action:** Add `micrometer-registry-prometheus` to Spring and annotate the Pod for scraping.

137. **Grafana Dashboards**
    * **Action:** Import the "JVM (Micrometer)" dashboard to monitor heap, GC, and threads.

138. **Distributed Tracing (Tracing)**
    * **Action:** Use **Spring Cloud Sleuth** (or Micrometer Tracing) with **Jaeger** to visualize request spans.

139. **Searching Logs across Replicas**
    * **Action:** Install the **Loki** stack to query logs from multiple pods using labels.

140. **Metrics Server Installation**
    * **Action:** Install the `metrics-server` to enable `kubectl top pods` and HPA.

141. **Alertmanager for Pod Restarts**
    * **Action:** Set a Prometheus alert for `kube_pod_container_status_restarts_total > 5`.

142. **Blackbox Monitoring**
    * **Action:** Use Prometheus Blackbox Exporter to probe your external Ingress URLs.

143. **Kube-state-metrics**
    * **Action:** Install to monitor cluster-level state like "Number of Pending PVCs."

144. **Audit Logging**
    * **Action:** Enable K8s API Audit Logs to track who deleted a resource.

145. **Prometheus Adapter for HPA**
    * **Action:** Use custom metrics (like "Requests Per Second") to scale your Java pods.



---

# ☸️ Kubernetes Deep-Dive: Scenarios 151 - 200

This final section covers **Security Hardening (The Shield)**, **Automation/GitOps**, and **Disaster Recovery**.

---

## 🛡️ 11. Security Hardening & Zero Trust (151-170)

151. **Non-Root Execution**: A container is compromised; the hacker gains root access to the host.
    * *Fix:* Set `securityContext.runAsNonRoot: true` to prevent the container from starting as UID 0.
152. **Dropping Linux Capabilities**: Your Java app doesn't need to change the system clock or sniff network packets.
    * *Action:* Use `capabilities: { drop: ["ALL"] }` in your Pod spec.
153. **Read-Only Root Filesystem**: Preventing hackers from downloading and executing malicious scripts.
    * *Action:* Set `readOnlyRootFilesystem: true` and mount an `emptyDir` for `/tmp`.
154. **Privileged Escalation**: A pod tries to gain more permissions than its parent process.
    * *Fix:* Set `allowPrivilegeEscalation: false`.
155. **Scanning Images for CVEs**: You are running an image with a known Log4Shell vulnerability.
    * *Action:* Integrate **Trivy** or **Grype** into your CI/CD pipeline to block "High/Critical" images.
156. **Secrets Encryption at Rest**: If someone steals the `etcd` backups, they see all passwords.
    * *Fix:* Enable **EncryptionConfiguration** on the API server to encrypt secrets at the storage layer.
157. **Pod Security Admission (PSA)**: You want to enforce security standards across a whole namespace.
    * *Action:* Label the namespace with `pod-security.kubernetes.io/enforce: restricted`.
158. **Network Isolation (Zero Trust)**: You want to ensure "Dev" pods cannot talk to "Prod" pods.
    * *Action:* Use **NetworkPolicies** to implement a "Default Deny" and only allow specific "Allow" rules.
159. **Image Guardrails**: A developer accidentally pulls an image from a random public registry.
    * *Fix:* Use an Admission Controller (like Kyverno) to only allow images from your internal JFrog/Nexus.
160. **ServiceAccount Token Protection**: Pods are being hijacked to call the K8s API.
    * *Action:* Set `automountServiceAccountToken: false` unless the pod specifically needs it.
161. **Seccomp Profiles**: Restricting the system calls (syscalls) a container can make to the Linux kernel.
    * *Action:* Apply the `RuntimeDefault` seccomp profile in the `securityContext`.
162. **AppArmor/SELinux**: Adding an extra layer of mandatory access control.
    * *Action:* Assign AppArmor profiles to pods to restrict file/network access.
163. **Runtime Security Monitoring**: Detecting a shell being opened in a production pod.
    * *Action:* Install **Falco** to alert on suspicious runtime behavior.
164. **Hardening the Kubelet**: Preventing unauthorized access to the node-level agent.
    * *Action:* Disable anonymous authentication and enable `NodeRestriction`.
165. **CIS Benchmark Auditing**: You need to ensure the cluster meets industry security standards.
    * *Action:* Run **kube-bench** against your master and worker nodes.
166. **Tainting the Master Node**: Ensuring no user workloads run on the control plane.
    * *Action:* Taint the master with `node-role.kubernetes.io/master:NoSchedule`.
167. **Rotating K8s Certificates**: Your cluster is about to expire in 30 days.
    * *Command:* `kubeadm certs renew all`.
168. **SSH Hardening**: Preventing direct access to worker nodes.
    * *Action:* Use a **Bastion Host** or a tool like **Teleport** for secure node access.
169. **Audit Log Retention**: You need to see who modified a ConfigMap 3 months ago.
    * *Action:* Enable K8s Audit Logging and send logs to an external long-term storage (S3/GCS).
170. **Kubernetes Dashboard Security**: The default dashboard is often an entry point for hackers.
    * *Action:* Avoid installing the Dashboard; use `kubectl` or a secure IDE like Lens with RBAC.



---

## 🤖 12. GitOps, CI/CD & Automation (171-185)

171. **Declarative Management (GitOps)**: Your cluster state is drifting from your Git repo.
    * *Action:* Install **ArgoCD** or **Flux** to automatically sync Git changes to the cluster.
172. **Blue/Green Deployment with ArgoCD**: You want to test the new version before switching users.
    * *Action:* Use Argo Rollouts to manage the traffic switch.
173. **Canary Analysis with Kayenta**: Automatically rolling back v2 if the error rate increases by 5%.
    * *Action:* Integrate Spinnaker with Kayenta for automated canary analysis.
174. **Helm Chart Versioning**: You want to package your Expense Manager app for different environments.
    * *Action:* Use **Helm** to template your YAML and manage releases.
175. **Helm Rollback**: A Helm upgrade failed and broke the site.
    * *Command:* `helm rollback <release-name> <revision-number>`.
176. **Kustomize Overlays**: You need small changes for `dev` vs `prod` without huge Helm charts.
    * *Action:* Use **Kustomize** to overlay specific fields (like replica counts).
177. **CI/CD ServiceAccount**: Your Jenkins/GitHub Actions needs to deploy to the cluster.
    * *Action:* Create a dedicated ServiceAccount with a scoped `RoleBinding`, not `cluster-admin`.
178. **Automated Image Updates**: You want the cluster to pull the latest image automatically when Jenkins pushes it.
    * *Action:* Use **Image Updater** for ArgoCD.
179. **Configuring Terraform for K8s**: You want to manage the cluster infrastructure and the apps in one place.
    * *Action:* Use the **Kubernetes Provider** for Terraform.
180. **Secret Management with Vault**: You don't want to store K8s Secrets in Git.
    * *Action:* Use the **HashiCorp Vault Agent** to inject secrets directly into your Java pod as files.
181. **GitHub Actions Self-Hosted Runners**: Running your build jobs inside your own K8s cluster.
    * *Action:* Deploy the **Actions Runner Controller (ARC)**.
182. **Handling Manifest Drift**: Someone manually changed the replica count via `kubectl`.
    * *Action:* ArgoCD will detect the "Out of Sync" status and revert it to the Git state.
183. **Tekton Pipelines**: Running your CI/CD pipelines as native K8s resources.
    * *Action:* Use Tekton for a Kubernetes-native build experience.
184. **Kaniko for Container Builds**: Building Docker images inside K8s without needing the Docker Daemon (rootless).
    * *Action:* Use Kaniko in your CI jobs.
185. **Skaffold for Local Dev**: You want your code changes to instantly reflect in a local K8s cluster.
    * *Action:* Run `skaffold dev` in your Spring Boot project.



---

## 🌪️ 13. Advanced Ops & Disaster Recovery (186-200)

186. **Multi-Cluster Management**: You have a cluster in AWS and one in Azure.
    * *Action:* Use **Cluster API** or a tool like **Rancher** to manage them from one pane.
187. **Cross-Region Failover**: AWS US-East-1 is down.
    * *Action:* Use **Global Server Load Balancing (GSLB)** to route traffic to US-West-2.
188. **Velero for Backups**: You need to back up both your YAMLs and your Persistent Volumes.
    * *Action:* Install **Velero** and schedule nightly backups to S3.
189. **Restoring a Deleted Namespace**: You accidentally deleted the `prod` namespace.
    * *Action:* Use Velero to restore the entire namespace from the last snapshot.
190. **Chaos Engineering with Chaos Mesh**: Intentionally killing pods to see if your app is truly resilient.
    * *Action:* Run **Chaos Mesh** to inject network latency and pod failures.
191. **Kubernetes API Upgrades**: Moving from v1.24 to v1.25 where APIs are deprecated.
    * *Action:* Use **pluto** to find deprecated API versions in your Helm charts.
192. **Handling Large-Scale Clusters**: Managing 5,000+ nodes.
    * *Action:* Tune `kube-apiserver` flags and use `Paging` for list requests.
193. **Node Feature Discovery (NFD)**: You need to schedule pods only on nodes with specific CPU instructions (AVX-512).
    * *Action:* Use NFD to label nodes based on hardware capabilities.
194. **GPU Scheduling**: Your AI/ML microservice needs a GPU.
    * *Action:* Install the **NVIDIA Device Plugin** and request `nvidia.com/gpu: 1`.
195. **Service Mesh (Istio) Traffic Splitting**: Sending 10% of traffic to v2 for testing.
    * *Action:* Use a **VirtualService** in Istio.
196. **Egress Gateway**: You want all outgoing traffic from your cluster to come from one static IP.
    * *Action:* Configure an Istio Egress Gateway.
197. **Cost Allocation (Kubecost)**: You need to know exactly how much each team is costing in cloud spend.
    * *Action:* Install **Kubecost**.
198. **Ephemeral Storage Limits**: A pod is filling up the Node's local disk with logs.
    * *Fix:* Set `requests.ephemeral-storage` and `limits.ephemeral-storage`.
199. **Orphaned PVCs**: You deleted a StatefulSet but the disks are still costing money.
    * *Action:* Use a cleanup script to find PVCs with no associated Pods.
200. **Final Cluster Audit**: You are ready for the big production launch.
    * *Action:* Run `kubectl cluster-info dump`, check all PDBs, HPA limits, and ensure all images are pinned to SHA hashes, not `latest`.

---

## 🏆 Graduation
You have now covered **200 real-world Kubernetes scenarios**. You are prepared for everything from high-performance Java tuning to deep-security hardening.

# ☸️ Kubernetes Mastery: The Complete Deep-Dive & Scenario Manual

A comprehensive technical guide and operational playbook for mastering Kubernetes, specifically tailored for Java/Spring Boot developers and DevOps engineers.

---

## 📑 Table of Contents

1.  [🏗️ Control Plane: The Brain](#1-control-plane-the-brain)
2.  [📦 Workloads: The Muscles](#2-workloads-the-muscles)
3.  [🕸️ Networking: The Nervous System](#3-networking-the-nervous-system)
4.  [💾 Storage: The Memory](#4-storage-the-memory)
5.  [🛡️ Security: The Shield](#5-security-the-shield)
6.  [📅 Scheduling: The Strategy](#6-scheduling-the-strategy)
7.  [☕ Java & Spring Boot Optimization](#7-java--spring-boot-optimization)
8.  [🛠️ Operational Scenarios (200+)](#8-operational-scenarios-200)
9.  [📜 Daily Use Cheat Sheet](#9-daily-use-cheat-sheet)

---

## 🏗️ 1. Control Plane: The Brain
The Control Plane is the decision-making layer. It operates on a **Desired State** model via a continuous reconciliation loop.

* **kube-apiserver**: The only component talking to `etcd`. It handles Authentication, Authorization, and Admission Control.
* **etcd**: Distributed Key-Value store. The cluster's "Source of Truth."
* **kube-scheduler**: Assigns Pods to Nodes based on resources, Taints, and Affinity.
* **kube-controller-manager**: Runs loops to regulate the state (Node, Replica, Endpoints).



---

## 📦 2. Workloads: The Muscles
Resources that execute your application code.

* **Pods**: Smallest unit. Containers share the same Network/IP (via Pause container).
* **Deployments**: Manages stateless replicas. Supports zero-downtime **RollingUpdates**.
* **StatefulSets**: For stateful apps (DBs). Provides stable identities (`db-0`, `db-1`) and storage.
* **DaemonSets**: Ensures a Pod runs on **every** node (Logs/Monitoring).



---

## 🕸️ 3. Networking: The Nervous System
* **Services**: Stable front-door for Pods. Types include `ClusterIP` (Internal), `NodePort` (Static port on Node), and `LoadBalancer` (Cloud IP).
* **Ingress**: Layer 7 routing (HTTP/S). Manages paths, domains, and SSL termination.
* **CoreDNS**: Internal service discovery (e.g., `my-app.namespace.svc.cluster.local`).

---

## 💾 4. Storage: The Memory
* **PersistentVolume (PV)**: Physical storage resource.
* **PersistentVolumeClaim (PVC)**: The developer's request for space.
* **StorageClass**: Automates disk creation (Dynamic Provisioning).
* **ConfigMaps/Secrets**: Injecting environment variables or configuration files.

---

## 🛡️ 5. Security: The Shield
* **RBAC**: Role-Based Access Control using `Roles`, `ClusterRoles`, and `RoleBindings`.
* **NetworkPolicies**: Internal firewalls to implement **Zero Trust**.
* **ServiceAccounts**: Identity for Pods to interact with the K8s API.

---

## 📅 6. Scheduling: The Strategy
* **Taints & Tolerations**: Nodes repelling Pods (e.g., "GPU-only node").
* **Affinity/Anti-Affinity**: Rules for placing Pods together or apart for High Availability.
* **Resource Quotas**: Limiting the total CPU/RAM a namespace can consume.

---

## ☕ 7. Java & Spring Boot Optimization
* **Container Awareness**: Use `-XX:+UseContainerSupport` for proper RAM detection.
* **Probes**: Use `startupProbe` for heavy boot times; map `liveness` and `readiness` to Spring Actuator endpoints.
* **Heap Management**: Set `-Xmx` to ~75% of the container limit.



---

## 🛠️ 8. Operational Scenarios (200+)

### **Troubleshooting & Debugging**
* **Scenario 101: Pending Pods**: Check `kubectl describe pod` for resource shortages or taints.
* **Scenario 102: CrashLoopBackOff**: Use `kubectl logs --previous` to see the JVM crash log.
* **Scenario 106: Debugging Distroless**: Use `kubectl debug -it <pod> --image=nicolaka/netshoot`.

### **Security Hardening**
* **Scenario 151: Non-Root**: Set `runAsNonRoot: true` in the `securityContext`.
* **Scenario 153: CVE Auditing**: Use `kubectl get pods -A -o custom-columns=...` to find vulnerable image tags.
* **Scenario 156: Read-Only Root FS**: Prevent malware downloads by locking the filesystem.

---

## 📜 9. Daily Use Cheat Sheet

| Task | Command |
| :--- | :--- |
| **Follow Logs** | `kubectl logs -f <pod-name>` |
| **Remote Debug** | `kubectl port-forward <pod> 5005:5005` |
| **Check Events** | `kubectl get events --sort-by='.lastTimestamp'` |
| **Restart App** | `kubectl rollout restart deployment <name>` |
| **Resource Top** | `kubectl top pod <pod-name> --containers` |

---

## 🚀 Getting Started
To apply the foundational templates for a Java-based Expense Manager:
1. `kubectl apply -f postgres-statefulset.yaml`
2. `kubectl apply -f expense-api-deployment.yaml`
3. `kubectl apply -f ingress.yaml`


