# AWS Master Certification & Staff Cloud Architect Interview Guide (100 Comprehensive Scenarios)

> **Certification & Architecture Alignment**: 
> - **AWS Certified Solutions Architect – Professional (SAP-C02)**
> - **AWS Certified DevOps Engineer – Professional (DOP-C02)**
> - **AWS Certified Security – Specialty (SCS-C02)**
> - **AWS Certified Advanced Networking – Specialty (ANS-C01)**
> - **Staff / Principal Cloud Infrastructure & Distributed Systems Architect**

---

## Guide Architecture Overview

```
========================================================================================================================
                          AWS MASTER 100-SCENARIO CERTIFICATION & INTERVIEW BLUEPRINT
========================================================================================================================
 Part 1: Advanced VPC Networking, Transit Gateway, Direct Connect & Hybrid (Q1 – Q15)
 Part 2: High-Performance Compute, Auto Scaling, EC2 Nitro & Placement Groups (Q16 – Q28)
 Part 3: Serverless Architecture: Lambda, SnapStart, EventBridge & Step Functions (Q29 – Q42)
 Part 4: Container Orchestration: ECS Fargate, EKS, Karpenter & App Runner (Q43 – Q54)
 Part 5: Storage Architectures: S3 Consistency, Lifecycle, Object Lock & EBS/EFS/FSx (Q55 – Q67)
 Part 6: Distributed Databases: Aurora Global, DynamoDB Single-Table & ElastiCache (Q68 – Q80)
 Part 7: Enterprise Security, IAM Governance, Organizations, SCPs & KMS (Q81 – Q92)
 Part 8: Edge Networking, CloudFront, Route 53, WAF & Disaster Recovery (Q93 – Q100)
 Layer 6: Fatal Anti-Patterns & Certification Traps
 Layer 7: Globally Reported Production Incidents & Post-Mortems
 Layer 8: Rapid-Fire Formula & Sizing Matrix
========================================================================================================================
```

---

# Part 1: Advanced VPC Networking, Transit Gateway, Direct Connect & Hybrid

---

### Scenario 1: Multi-Account Transit Gateway (TGW) Hub-and-Spoke vs VPC Peering
**Question:** An enterprise has 250 AWS accounts across 4 regions with requirements for centralized inspection, non-transitive isolation between Dev and Prod, and shared access to on-premises datacenters. Why does VPC Peering fail, and how do you design this with AWS Transit Gateway route tables?
- **Interviewer Evaluation:** Assesses network scaling limits ($O(N^2)$ peering complexity), Transit Gateway route domains, Resource Access Manager (RAM), and routing separation.
- **Standout Technical Answer:**
  - *VPC Peering Limitation*: Full-mesh peering for 250 VPCs requires $\frac{N(N-1)}{2} = \frac{250 \times 249}{2} = 31,125$ peerings. VPC Peering is strictly **non-transitive** (VPC A cannot talk to VPC C through VPC B).
  - *Transit Gateway Design*:
    1. Deploy a central Transit Gateway in a dedicated `Network-Hub` AWS account and share it across the AWS Organization using **AWS RAM (Resource Access Manager)**.
    2. Create separate **TGW Route Tables** to establish isolated route domains:
       - `Dev_TGW_RouteTable`: Propagates Dev VPC attachments and on-prem Direct Connect. Has NO route to Prod.
       - `Prod_TGW_RouteTable`: Propagates Prod VPC attachments and on-prem. Has NO route to Dev.
       - `Inspection_TGW_RouteTable`: Directs all egress/east-west traffic through a centralized inspection VPC hosting an AWS Network Firewall or auto-scaled third-party firewall appliances.
- **Follow-Up Trap & Winning Answer:**
  - *Trap:* "Can two VPCs with overlapping CIDR blocks communicate across a Transit Gateway?"
  - *Answer:* No, TGW routes packets based on destination IP; overlapping CIDRs cause non-deterministic routing. To connect overlapping CIDRs without redesigning subnets, deploy **AWS PrivateLink** (endpoint service) or insert **Transit VPC with Bidirectional NAT (NAT Gateway / Private NAT Gateway)**.

---

### Scenario 2: Direct Connect (DX) Dedicated vs Hosted Connections & Resiliency Architectures
**Question:** How do you architect a maximum resiliency (99.99% SLA) hybrid connection between two on-premises datacenters and AWS, and what is the difference between a Private VIF, Public VIF, and Transit VIF?
- **Interviewer Evaluation:** Evaluates hybrid cloud connectivity, BGP failover, Direct Connect Gateway (DXGW), and SLA requirements.
- **Standout Technical Answer:**
  - *99.99% Maximum Resiliency Architecture*:
    - Requires **two separate Direct Connect locations** (different colocation providers).
    - Requires **two physical connections per location** across independent customer routers, terminating on diverse AWS Direct Connect routers (total 4 circuits).
    - Dual BGP sessions configured with BFD (Bidirectional Forwarding Detection) for sub-second failure detection.
  - *Virtual Interface (VIF) Types*:
    - **Private VIF**: Connects directly to a specific VPC via a Virtual Private Gateway (VGW) or to multiple VPCs in any region via a Direct Connect Gateway.
    - **Public VIF**: Connects to public AWS services (S3, DynamoDB) without traversing the public internet, using public BGP ASN.
    - **Transit VIF**: Connects to an AWS Transit Gateway via a Direct Connect Gateway. Supports up to 3 Transit Gateways across multiple regions.
- **Follow-Up Trap & Winning Answer:**
  - *Trap:* "Can you attach a Transit VIF directly to a Virtual Private Gateway (VGW)?"
  - *Answer:* No. A Transit VIF can only be associated with a **Direct Connect Gateway** that attaches to an AWS Transit Gateway.

---

### Scenario 3: AWS PrivateLink vs VPC Peering Security Isolation
**Question:** A SaaS provider hosts an API in VPC A and wants to offer it securely to 500 enterprise customers in VPC B, C, D... without CIDR conflicts and without exposing their network topology. Which pattern must be used?
- **Interviewer Evaluation:** PrivateLink (VPC Endpoint Services), NLB requirement, zero-route table exposure, unidirectional access.
- **Standout Technical Answer:**
  - Deploy **AWS PrivateLink (VPC Endpoint Service)**:
    1. Provider places API behind a **Network Load Balancer (NLB)** in VPC A.
    2. Provider creates an *Endpoint Service* referencing the NLB and whitelists customer AWS Account IDs.
    3. Customer creates an *Interface VPC Endpoint* (ENI) in their private subnet.
  - *Why this is superior to Peering*:
    - **No CIDR Collisions**: Both provider and consumer can use `10.0.0.0/16`.
    - **Strict Unidirectional Access**: Consumer can initiate TCP sessions to the provider; provider *cannot* initiate connections back into consumer subnets.
    - **Zero Routing Table Pollution**: No VPC route table updates are required.
- **Follow-Up Trap & Winning Answer:**
  - *Trap:* "Can an Application Load Balancer (ALB) be directly configured as the target of an AWS PrivateLink Endpoint Service?"
  - *Answer:* No, an Endpoint Service requires a **Network Load Balancer (NLB)**. However, the NLB can use an ALB as an **ALB-type target group**, allowing Layer 7 routing behind a Layer 4 PrivateLink endpoint.

---

### Scenario 4: Eliminating NAT Gateway Data Processing Costs via Gateway Endpoints
**Question:** Private subnets are pushing 100 TB of analytics data to S3 and DynamoDB monthly. The NAT Gateway bill is astronomical. How do you reduce this cost to $0 with zero application downtime?
- **Standout Technical Answer:**
  - Provision **VPC Gateway Endpoints** for Amazon S3 and DynamoDB:
    - Gateway Endpoints are **100% free** (no hourly charge, no per-GB data processing fee).
    - Attach the Gateway Endpoint to the private subnet Route Tables.
    - AWS automatically injects a prefix-list route (e.g., `pl-63a5400a (com.amazonaws.us-east-1.s3) -> vpce-xxxx`).
    - Outbound traffic matching the S3 prefix list immediately detours away from the NAT Gateway directly over the AWS internal software-defined network.
- **Follow-Up Trap:** "Why isn't there a Gateway Endpoint for SQS or Secrets Manager?"
  - *Answer:* Gateway endpoints were historically implemented via Route Table prefix-lists and are available strictly for **S3** and **DynamoDB**. All other AWS services use **Interface Endpoints (PrivateLink)** which allocate ENIs and incur standard hourly and data processing fees.

---

### Scenario 5: Route 53 Routing Policies: Latency, Geolocation, Geoproximity & Failover
**Question:** Differentiate between Geolocation and Geoproximity routing, and explain how Route 53 Application Recovery Controller (ARC) orchestrates multi-region failover.
- **Standout Technical Answer:**
  - **Geolocation Routing**: Routes traffic based on the geographic location of the DNS query origin (continent, country, or US state). Used for licensing, localization, and compliance (e.g., GDPR).
  - **Geoproximity Routing**: Routes traffic based on the physical distance between the user and AWS resources, with an adjustable **Bias** ($-99$ to $+99$) to expand or shrink the catchment area of a specific AWS region. Requires Route 53 Traffic Flow.
  - **Route 53 ARC (Application Recovery Controller)**: Uses **Routing Controls** (failover flags evaluated in milliseconds across 5 independent regional control plane cells) to shift traffic away from an impaired region without relying on standard DNS TTL propagation delays.

---

### Scenario 6: Stateful Security Groups vs Stateless Network ACLs (NACLs)
**Question:** A junior engineer adds an inbound rule to a NACL allowing port 443, but client HTTPS requests timeout. Why?
- **Standout Technical Answer:**
  - NACLs are **stateless**. Allowing inbound port 443 does not automatically allow the response traffic.
  - When an external client connects from an ephemeral port (e.g., port 49152) to port 443, the response packet travels from port 443 to port 49152.
  - The NACL outbound table must have an explicit allow rule for **Ephemeral Ports (TCP 1024–65535)**.
  - Security Groups are **stateful**; return traffic is automatically tracked at the connection level regardless of outbound rules.

---

### Scenario 7: DHCP Option Sets & Hybrid On-Premises DNS Resolution
**Question:** EC2 instances in an AWS VPC must resolve both internal on-premises domains (`corp.local`) and AWS private hosted zones. How do you configure Route 53 Resolver?
- **Standout Technical Answer:**
  - Deploy **Route 53 Resolver Endpoints**:
    1. **Inbound Endpoint**: Allows on-premises DNS servers to forward DNS queries for AWS Private Hosted Zones (`*.aws.internal`) into the VPC.
    2. **Outbound Endpoint**: Associated with a **Resolver Rule** (Forwarding rule) that intercepts queries for `corp.local` and forwards them across Direct Connect / VPN to on-premises DNS resolvers (`192.168.1.10`).

---

### Scenario 8: AWS Network Firewall vs Security Groups & WAF
**Question:** When do you need AWS Network Firewall if you already have AWS WAF and Security Groups?
- **Standout Technical Answer:**
  - **Security Groups**: Layer 4 stateful host firewalls (IP/port only, no deep packet inspection).
  - **AWS WAF**: Layer 7 application firewall inspecting HTTP/HTTPS payloads for SQLi, XSS, rate-limiting, and bot control.
  - **AWS Network Firewall**: Stateful Layer 3-7 deep packet inspection (Suricata-compatible engine) that inspects **non-HTTP traffic** (SSH, FTP, DNS), enforces domain name filtering with SNI on outbound TLS, and provides network-wide intrusion detection/prevention (IDS/IPS).

---

### Scenario 9: VPC Flow Logs Analysis with Athena & CloudWatch
**Question:** How do you detect port scanning or rejected traffic across an entire AWS Organization without installing agents on EC2 instances?
- **Standout Technical Answer:**
  - Enable **VPC Flow Logs** at the VPC or Transit Gateway level and publish in Parquet format directly to an **Amazon S3 centralized bucket**.
  - Query using **Amazon Athena**:
    ```sql
    SELECT srcaddr, dstport, count(*) as rejections
    FROM vpc_flow_logs
    WHERE action = 'REJECT'
    GROUP BY srcaddr, dstport
    ORDER BY rejections DESC LIMIT 20;
    ```

---

### Scenario 10: Private NAT Gateway & Overlapping CIDR Integration
**Question:** What is an AWS Private NAT Gateway, and how does it differ from a Public NAT Gateway?
- **Standout Technical Answer:**
  - A **Public NAT Gateway** translates private subnet IPs to an Elastic Public IPv4 address for internet egress.
  - A **Private NAT Gateway** performs NAT using a private IP address within its subnet. It allows communication between VPCs and on-premises networks that have overlapping CIDRs or where strict address translation is required before entering a central hub.

---

### Scenario 11: Elastic IP Transfer & Bring Your Own IP (BYOIP)
**Question:** How does BYOIP work in AWS, and why is it critical for email deliverability and whitelisting?
- **Standout Technical Answer:**
  - AWS BYOIP allows enterprises to import publicly routable IPv4/IPv6 address blocks into their AWS account.
  - AWS advertises the prefix globally via BGP from AWS edge routers.
  - Eliminates the need to re-whitelist IP addresses with external business partners and preserves IP reputation for email sending services.

---

### Scenario 12: Egress-Only Internet Gateway vs NAT Gateway for IPv6
**Question:** Can an IPv6 EC2 instance use a NAT Gateway to reach the internet?
- **Standout Technical Answer:**
  - No. IPv6 addresses are globally unique and publicly routable; NAT is unnecessary and violates the IPv6 architecture.
  - AWS provides an **Egress-Only Internet Gateway (EIGW)** for IPv6.
  - It allows outbound IPv6 traffic from the VPC to the internet, while preventing external internet entities from initiating inbound connections to the instance.

---

### Scenario 13: Gateway Load Balancer (GWLB) & Inline Virtual Appliance Fleet
**Question:** How does Gateway Load Balancer route traffic through third-party firewalls without altering packet headers?
- **Standout Technical Answer:**
  - GWLB operates at Layer 3/4.
  - It encapsulates original IP packets inside **GENEVE (Generic Network Virtualization Encapsulation)** packets (UDP port 6081) preserving all original source/destination IPs, ports, and metadata.
  - Dispatches packets to a fleet of firewall appliances across multiple AZs and unwraps them upon return.

---

### Scenario 14: AWS Global Accelerator vs Amazon CloudFront
**Question:** Both utilize the AWS global edge network. When should you choose Global Accelerator over CloudFront?
- **Standout Technical Answer:**
  - **CloudFront**: Content Delivery Network (CDN) caching HTTP/HTTPS web content at edge locations.
  - **Global Accelerator**: Network Layer (Layer 4) service that provides two static anycast public IP addresses. Routes TCP/UDP traffic (e.g., VoIP, gaming, IoT, non-HTTP APIs) over the AWS dedicated global fiber backbone directly to the closest healthy regional endpoint.

---

### Scenario 15: MTU Sizing: Jumbo Frames (9001 MTU) vs Standard Frames (1500 MTU)
**Question:** When can you use Jumbo Frames in AWS, and what happens when traffic leaves the VPC?
- **Standout Technical Answer:**
  - Jumbo frames (MTU 9001) are supported for traffic **within a VPC** between instances that support enhanced networking.
  - Traffic traversing an Internet Gateway, VPC Peering outside the region, or Direct Connect without jumbo frame support is fragmented or dropped if the DF (Don't Fragment) bit is set. Path MTU Discovery (PMTUD) must be permitted via ICMP type 3 code 4.

---

# Part 2: High-Performance Compute, Auto Scaling, EC2 Nitro & Placement Groups

---

### Scenario 16: AWS Nitro System Architecture: Security & Offloaded Hypervisor
**Question:** Explain how the AWS Nitro architecture eliminates the performance overhead and security vulnerability of traditional Xen/KVM software hypervisors.
- **Standout Technical Answer:**
  - In traditional virtualization, 15–30% of host CPU and memory is consumed by the hypervisor managing disk I/O, network virtualization, and hardware security.
  - **Nitro Architecture**: Offloads all virtualization tasks to dedicated hardware **Nitro PCI Cards**:
    - *Nitro Card for VPC*: Encapsulates network packets, security groups, and enforces up to 100 Gbps network bandwidth.
    - *Nitro Card for EBS*: Manages NVMe storage encryption and IOPS.
    - *Nitro Security Chip*: Cryptographically validates hardware firmware integrity on power-up.
    - *Nitro Hypervisor*: Ultra-lightweight hypervisor providing memory and CPU allocation with bare-metal performance.

---

### Scenario 17: EC2 Placement Groups: Cluster vs Spread vs Partition
**Question:** For which workloads would you choose Cluster, Spread, or Partition placement groups?
- **Standout Technical Answer:**
  - **Cluster**: Packs instances close together inside a single Availability Zone within the same network spine. Delivers sub-millisecond latency and up to 100 Gbps bandwidth. *Best for*: HPC, distributed ML training (PyTorch/DistributedDataParallel).
  - **Spread**: Places each instance on distinct physical hardware racks with independent power and network supplies. Maximum 7 instances per AZ. *Best for*: Small critical quorum nodes (Kafka Controller, ZooKeeper, etcd).
  - **Partition**: Divides the placement group into partitions (logical racks). Instances in one partition do not share hardware with other partitions. *Best for*: Large distributed data stores (HDFS, Cassandra, Kafka).

---

### Scenario 18: Auto Scaling: Target Tracking vs Step Scaling vs Predictive Scaling
**Question:** An e-commerce service experiences sharp 5-minute flash sales. Why does Target Tracking scaling fail, and how do you configure Auto Scaling for this spike?
- **Standout Technical Answer:**
  - *Why Target Tracking fails*: CloudWatch metrics take 1-3 minutes to alarm, and instances take 2-4 minutes to initialize; the flash sale is over before new capacity arrives.
  - *Remediation*:
    1. **Predictive Scaling**: Analyzes historical traffic patterns using ML to schedule EC2 capacity *in advance* of the recurring peak.
    2. **Warm Pools**: Pre-initializes EC2 instances (stopped or running state) with dependencies loaded, bringing scale-out time down to $< 30$ seconds.
    3. **Step Scaling**: Triggers aggressive capacity additions based on metric breach tiers without cooldown delays.

---

### Scenario 19: Auto Scaling Lifecycle Hooks & Graceful Draining
**Question:** How do you ensure an EC2 instance in an Auto Scaling Group finishes in-flight jobs and flushes logs to S3 before termination?
- **Standout Technical Answer:**
  - Configure an **EC2 Auto Scaling Termination Lifecycle Hook** (`autoscaling:EC2_INSTANCE_TERMINATING`).
  - When ASG decides to terminate the instance, it moves it to `Terminating:Wait` state.
  - EventBridge captures the event and invokes an SSM Run Command or Lambda script on the instance to:
    1. Stop accepting new jobs and finish active processing.
    2. Upload application logs to S3.
  - The script completes by sending `CompleteLifecycleAction` with `CONTINUE`, allowing the instance to terminate cleanly.

---

### Scenario 20: Spot Instances & Spot Fleet Allocation Strategies
**Question:** How do you run cost-effective fault-tolerant batch processing on Spot instances without catastrophic interruptions?
- **Standout Technical Answer:**
  - Use an **EC2 Fleet / Spot Fleet** with the **Capacity-Optimized** allocation strategy.
  - Deep diversifies across multiple instance types and Availability Zones (e.g., `m5.large`, `m5a.large`, `c5.large` across 3 AZs).
  - Enables **Spot Instance Interruption Notice**: Listens to the 2-minute warning via EventBridge or metadata service (`http://169.254.169.254/latest/meta-data/spot/instance-action`) to checkpoint processing state.

---

### Scenario 21: Enhanced Networking: ENA (Elastic Network Adapter) vs EFA (Elastic Fabric Adapter)
**Question:** When is ENA insufficient, and why is EFA required for distributed AI training and HPC?
- **Standout Technical Answer:**
  - **ENA**: Provides standard high-throughput, low-latency TCP/IP networking up to 100 Gbps.
  - **EFA**: Bypasses the Linux kernel networking stack (**Kernel Bypass**) using the OS-bypass **SRD (Scalable Reliable Datagram)** protocol developed by Annapurna Labs. Achieves ultra-low microsecond inter-node MPI and NCCL communication across GPU instances without packet drops or head-of-line blocking.

---

### Scenario 22: EC2 Instance Metadata Service: IMDSv1 vs IMDSv2
**Question:** Explain how IMDSv2 mitigates SSRF (Server-Side Request Forgery) attacks that leaked millions of Capital One records in 2019.
- **Standout Technical Answer:**
  - *IMDSv1 Vulnerability*: Simple HTTP GET request (`GET http://169.254.169.254/latest/meta-data/iam/security-credentials/`) requires no headers; an SSRF vulnerability allows an attacker to fetch IAM role credentials directly.
  - *IMDSv2 Defense*:
    1. Requires a **Session Token** created via HTTP `PUT` with a `X-aws-ec2-metadata-token-ttl-seconds` header:
       `TOKEN=$(curl -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600")`.
    2. Subsequent requests must pass the token: `curl -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/meta-data/`.
    3. WAF and reverse proxies block HTTP `PUT` requests, and the TTL header prevents SSRF reflection.
    4. By default, IMDSv2 sets IP packet hop limit to 1, preventing external containers from hopping through host metadata.

---

### Scenario 23: Nitro Enclaves for Confidential Computing
**Question:** How do Nitro Enclaves protect cryptographic private keys from even the root user on an EC2 instance?
- **Standout Technical Answer:**
  - Nitro Enclaves create an isolated, hardened compute environment with **no persistent storage, no interactive access, and no external networking**.
  - CPU and memory are carved out exclusively by the Nitro Hypervisor.
  - Communicates with the parent EC2 instance solely over a local **virtio socket (vsock)**.
  - Features cryptographic **Attestation**: Generates an attestation document signed by the Nitro Hypervisor that AWS KMS validates before decrypting sensitive keys.

---

### Scenario 24: Graviton Processors (ARM64) Migration Architectural Considerations
**Question:** What technical hurdles must be addressed when migrating x86_64 workloads to AWS Graviton (ARM64)?
- **Standout Technical Answer:**
  - Recompiling native binaries for `aarch64`.
  - Updating Docker images to multi-arch builds (`docker buildx` with `linux/arm64`).
  - Checking third-party dependencies and proprietary C-libraries for ARM support.
  - Graviton delivers up to 40% better price-performance due to high core counts without hyperthreading interference.

---

### Scenario 25: EC2 Hibernation Mechanics
**Question:** What prerequisites must be satisfied to enable EC2 Hibernation, and where is the RAM stored?
- **Standout Technical Answer:**
  - Root volume must be an encrypted EBS volume with sufficient free space to store RAM contents.
  - Instance RAM must be $\le 150$ GB.
  - The OS saves the in-memory RAM state directly into an encrypted file on the EBS root volume and shuts down the instance.
  - While hibernated, you pay only for EBS storage, not for EC2 compute hours.

---

### Scenario 26: Mixed Instances Policy in Auto Scaling Groups
**Question:** How do you configure an ASG to run 30% On-Demand (base capacity) and 70% Spot instances across diverse instance types?
- **Standout Technical Answer:**
  - Use an **ASG Mixed Instances Policy**:
    - `OnDemandBaseCapacity = 3` (guarantees minimum 3 instances remain On-Demand).
    - `OnDemandPercentageAboveBaseCapacity = 30`.
    - `SpotAllocationStrategy = "capacity-optimized"`.
    - Instance overrides list: `c5.large`, `c5a.large`, `m5.large`.

---

### Scenario 27: Dedicated Instances vs Dedicated Hosts
**Question:** What is the technical and licensing difference between Dedicated Instances and Dedicated Hosts?
- **Standout Technical Answer:**
  - **Dedicated Instances**: Run on single-tenant hardware dedicated to your AWS account. You cannot control which physical socket or core an instance lands on.
  - **Dedicated Hosts**: Gives full visibility and control over the physical server (sockets, physical cores, host ID). Allows bringing existing per-socket or per-core enterprise software licenses (BYOL - Microsoft Windows Server, SQL Server).

---

### Scenario 28: Elastic IP Cold Migration Architecture
**Question:** How do you perform a zero-downtime cutover between two standalone EC2 instances using Elastic IPs?
- **Standout Technical Answer:**
  - Associate an Elastic IP with Instance A.
  - When ready to cut over, issue an API call to reassociate the Elastic IP to Instance B with `AllowReassociation = true`.
  - ARP tables in AWS software-defined networking update in $< 1$ second globally without waiting for DNS propagation.

---

# Part 3: Serverless Architecture: Lambda, SnapStart, EventBridge & Step Functions

---

### Scenario 29: AWS Lambda MicroVM Execution Environment & Cold Start Lifecycle
**Question:** Walk through the exact internal execution lifecycle of an AWS Lambda function from invocation to teardown.
- **Standout Technical Answer:**
  1. **Download Code**: Downloads code zip/container from internal S3 repository.
  2. **MicroVM Boot**: Boots a dedicated lightweight microVM using **Firecracker** (Linux KVM-based).
  3. **Runtime Init**: Starts runtime engine (Node.js, Python, JVM).
  4. **Static Initialization**: Executes code outside the handler method (imports, DB connection pools, KMS client init).
  5. **Handler Execution**: Invokes handler with event payload.
  6. **Freeze/Thaw**: After execution, microVM is frozen in RAM for 5–15 minutes waiting for subsequent warm invocations.

```
Lambda Cold Start Breakdown:
[ Invocation ] -> [ Download Code ] -> [ Firecracker MicroVM Boot ] -> [ Init Runtime ] -> [ Static Code Init ] -> [ Handler ]
                   |<----------------- Cold Start Latency (100ms - 5000ms) -------------->| (Warm: <10ms)
```

---

### Scenario 30: Lambda SnapStart for Java Functions
**Question:** How does AWS Lambda SnapStart eliminate Java cold starts from 6 seconds to under 200ms?
- **Standout Technical Answer:**
  - During function deployment, SnapStart boots the Lambda function, runs the complete static initialization phase, and executes runtime optimizations.
  - Takes an encrypted snapshot of the microVM memory and disk state and caches it in a multi-tier cache.
  - On subsequent invocations, Lambda resumes execution from the cached snapshot in $< 200$ ms.
  - *Warning*: Any random seed or unique connection token initialized during static init must be refreshed via `CRaC` (Coordinated Restore at Checkpoint) hooks to avoid duplicate state.

---

### Scenario 31: Lambda Provisioned Concurrency vs Reserved Concurrency
**Question:** Explain the difference between Provisioned Concurrency and Reserved Concurrency.
- **Standout Technical Answer:**
  - **Reserved Concurrency**: Sets the *maximum* number of concurrent executions a function can consume. Acts as an upper-bound cap to protect downstream databases from being overwhelmed, and guarantees that other functions in the account cannot starve this function.
  - **Provisioned Concurrency**: Pre-warms a designated number of execution environments ready to respond instantly with zero cold start. Incurs continuous hourly pricing.

---

### Scenario 32: Lambda Database Connection Exhaustion & RDS Proxy
**Question:** 1,000 concurrent Lambda instances connect directly to an Amazon RDS PostgreSQL instance with `max_connections = 200`. What happens, and how is it resolved?
- **Standout Technical Answer:**
  - Each Lambda instance opens its own TCP connection. 1,000 Lambdas immediately exhaust the 200 RDS connection limit, causing connection rejection errors (`FATAL: remaining connection slots are reserved`).
  - *Fix*: Deploy **Amazon RDS Proxy**:
    - Sits between Lambda and RDS.
    - Pools and multiplexes thousands of incoming Lambda connections over a small, persistent pool of database connections.
    - Automatically handles database failovers 66% faster while maintaining client connection state.

---

### Scenario 33: Asynchronous Lambda Invocations, Retries & Dead Letter Queues (DLQ)
**Question:** How does AWS Lambda handle retries for asynchronous invocations (e.g., from S3 or EventBridge), and how do you prevent poison pills?
- **Standout Technical Answer:**
  - For asynchronous invocations, Lambda places the event in an internal queue and returns HTTP 202 Accepted.
  - If the function fails, Lambda retries **2 additional times** with exponential backoff.
  - If all retries fail, the event is either dropped or sent to a configured **Lambda Destination** or **Dead Letter Queue (SQS/SNS)**.
  - *Best Practice*: Use **Lambda Destinations** with SQS rather than standard DLQs because Destinations retain function execution metadata, stack traces, and invocation parameters.

---

### Scenario 34: Lambda Event Source Mapping (ESM) with SQS vs Kinesis
**Question:** How does Lambda Event Source Mapping differ when consuming from SQS vs Kinesis Data Streams?
- **Standout Technical Answer:**
  - **SQS (Queue Model)**:
    - Long-polls SQS queue. Scales concurrency up to maximum based on queue depth.
    - If a message fails, only that specific message becomes visible again after the visibility timeout expires.
  - **Kinesis (Stream Model)**:
    - Reads sequentially from shards.
    - Default concurrency: **1 Lambda instance per Kinesis shard**.
    - If a record fails, the entire shard processing is blocked until the record succeeds or expires (Head-of-Line Blocking), unless configured with `BisectBatchOnFunctionError` and `MaximumRetryAttempts`.

---

### Scenario 35: EventBridge Event Bus: Content-Based Filtering & Schema Discovery
**Question:** How does EventBridge replace legacy SNS/SQS pub-sub topologies in microservices?
- **Standout Technical Answer:**
  - Centralizes event distribution using an **Event Bus**.
  - Supports **Content-Based Filtering**: Rules inspect the JSON payload of events (not just metadata headers) to route messages to specific targets (e.g., route only if `$.detail.status == "CRITICAL"`).
  - Integrates with **Schema Registry**: Automatically discovers event schemas and generates strongly-typed code bindings (Java, Python, TypeScript).

---

### Scenario 36: Step Functions: Standard vs Express Workflows
**Question:** When must you choose an Express Workflow over a Standard Workflow in AWS Step Functions?
- **Standout Technical Answer:**
  - **Standard Workflows**:
    - Long-running (up to 1 year).
    - Exactly-Once execution model.
    - Full execution history stored and visualizable in console.
    - Priced per state transition ($0.025 per 1,000 transitions).
  - **Express Workflows**:
    - High-volume, short-duration (up to 5 minutes).
    - At-Least-Once execution model.
    - Handles $> 100,000$ executions per second.
    - Priced based on memory and execution duration (cost-effective for streaming and IoT data ingestion).

---

### Scenario 37: Step Functions Saga Pattern & Distributed Transactions
**Question:** How do you implement the Saga Pattern in Step Functions for a booking service across Flights, Hotels, and Car rentals?
- **Standout Technical Answer:**
  - Each forward task has a designated **Compensating Task** configured via `Catch` blocks:
    ```
    BookFlight -> BookHotel -> BookCarRental
    ```
  - If `BookCarRental` fails:
    1. Catch block catches error and routes to `CancelHotel` state.
    2. Then routes to `CancelFlight` state.
    3. Emits `BookingFailed` event.
  - Guarantees eventual consistency without distributed database locks.

---

### Scenario 38: Lambda Ephemeral Storage (`/tmp`) Expansion
**Question:** A machine learning Lambda function needs to process a 5GB video file. Lambda memory is set to 2GB. How can it process the file locally?
- **Standout Technical Answer:**
  - AWS Lambda supports configuring ephemeral storage (`/tmp`) from **512 MB up to 10,240 MB (10 GB)**.
  - The function downloads the 5GB file into `/tmp` and streams chunks through memory without exceeding the 2GB memory allocation.

---

### Scenario 39: Lambda Function URLs vs API Gateway
**Question:** When should you use a Lambda Function URL instead of provisioning an Amazon API Gateway?
- **Standout Technical Answer:**
  - **Function URLs**: Free, built-in HTTPS endpoints directly attached to a Lambda function. Supports IAM auth or Public (CORS). Supports up to 15-minute response timeouts. Ideal for Webhooks and single-purpose microservices.
  - **API Gateway**: Provides Layer 7 features: custom domain routing, request validation, API keys, usage plans, throttling tiers, WAF integration, and WebSocket connections.

---

### Scenario 40: API Gateway Throttling: Token Bucket Algorithm
**Question:** How does API Gateway enforce rate limits across clients using the Token Bucket algorithm?
- **Standout Technical Answer:**
  - Configured via **Rate** (tokens added per second) and **Burst** (maximum bucket capacity).
  - If a client sends a spike of requests, tokens are consumed from the burst capacity. Once the bucket is empty, API Gateway drops requests immediately with **HTTP 429 Too Many Requests** without hitting the backend Lambda.

---

### Scenario 41: API Gateway Caching & Invalidation
**Question:** How do you configure API Gateway caching, and how can authorized clients bypass the cache?
- **Standout Technical Answer:**
  - Enable caching on a dedicated stage with a defined TTL (default 300s).
  - Cache keys are generated based on method and configured query parameters or headers.
  - Clients with `Cache-Control: max-age=0` can bypass the cache if the client has the `execute-api:InvalidateCache` IAM permission.

---

### Scenario 42: WebSocket APIs in API Gateway
**Question:** How does API Gateway manage stateful WebSocket connections while keeping backends serverless?
- **Standout Technical Answer:**
  - API Gateway maintains the persistent TCP connection with the client.
  - Translates WebSocket frames into standard HTTP POST event invocations to backend Lambda functions (`$connect`, `$disconnect`, `$default`).
  - Backends send asynchronous messages back to connected clients via the **API Gateway Management API** using a persistent `connectionId`.

---

# Part 4: Container Orchestration: ECS Fargate, EKS, Karpenter & App Runner

---

### Scenario 43: ECS Fargate vs EKS: Architectural Selection Criteria
**Question:** You are designing a greenfield microservices architecture. What criteria dictate choosing Amazon ECS Fargate vs Amazon EKS?
- **Standout Technical Answer:**
  - **Choose ECS Fargate**:
    - Low operational overhead; native AWS integration (IAM task roles, CloudWatch, ALB).
    - No Kubernetes control plane to manage, upgrade, or patch.
    - Ideal for teams without dedicated Kubernetes SRE expertise.
  - **Choose EKS**:
    - Multicloud or on-premises hybrid deployments requiring consistent Kubernetes tooling.
    - Complex service mesh (Istio/Linkerd), custom operators, and fine-grained scheduling (CRDs).
    - Extreme scale requiring Karpenter-based node provisioning and heterogeneous GPU clusters.

---

### Scenario 44: EKS Node Autoscaling: Karpenter vs Cluster Autoscaler
**Question:** Why has Karpenter replaced Kubernetes Cluster Autoscaler as the gold standard for EKS node provisioning?
- **Standout Technical Answer:**
  - **Cluster Autoscaler**: Relies on EC2 Auto Scaling Groups. When a pod is unschedulable, it scales up pre-defined ASG node groups, taking 3–5 minutes to negotiate with EC2 APIs.
  - **Karpenter (Group-less Autoscaling)**:
    - Bypasses Auto Scaling Groups entirely.
    - Directly calls EC2 Fleet APIs.
    - Inspects pending Pod resource requests, tolerations, and affinities, and calculates the **exact optimal instance type** (e.g., launching a single `c6g.2xlarge` to fit 15 pods).
    - Provisions nodes in **under 45 seconds** and automatically performs **Node Consolidation** (bin-packing and terminating underutilized nodes).

---

### Scenario 45: ECS Task Definition: Task Role vs Execution Role
**Question:** Explain the security distinction between an ECS `TaskRoleArn` and `ExecutionRoleArn`.
- **Standout Technical Answer:**
  - **Task Execution Role (`executionRoleArn`)**: Used by the **Amazon ECS container agent** and infrastructure to pull private images from ECR, send container logs to CloudWatch, and retrieve secrets from Secrets Manager.
  - **Task Role (`taskRoleArn`)**: Assigned directly to the **application running inside the container**. Grants application business logic permissions to query DynamoDB, publish to S3, or send SQS messages.

---

### Scenario 46: EKS IAM Roles for Service Accounts (IRSA) vs Pod Identity
**Question:** How does EKS IRSA work under the hood using OIDC, and how does EKS Pod Identity simplify it?
- **Standout Technical Answer:**
  - **IRSA (IAM Roles for Service Accounts)**:
    - EKS cluster hosts an **OIDC Identity Provider**.
    - When a pod starts, the EKS mutating webhook injects an OIDC token and AWS credentials environment variables.
    - Application calls AWS STS `AssumeRoleWithWebIdentity`, exchanging the K8s service account token for temporary AWS credentials.
  - **EKS Pod Identity (2023+)**:
    - Replaces complex OIDC trust policy configurations.
    - EKS Pod Identity agent on nodes directly handles IAM credential vending via node agent, eliminating OIDC IAM trust policy sprawl.

---

### Scenario 47: ECS Networking Modes: `awsvpc` vs `bridge` vs `host`
**Question:** Why is `awsvpc` networking mode mandatory for modern secure ECS microservices?
- **Standout Technical Answer:**
  - `bridge`: Docker internal bridge network with host port mapping; port collision risks.
  - `host`: Bypasses Docker networking, binding container directly to EC2 host NIC; zero isolation.
  - **`awsvpc`**:
    - Every ECS task receives its **own dedicated Elastic Network Interface (ENI)** and private VPC IP address.
    - Tasks can be secured with their own dedicated **Security Groups**.
    - Mandatory for ECS Fargate.

---

### Scenario 48: EKS Networking: VPC CNI & Prefix Delegation
**Question:** An EKS cluster on `m5.large` nodes exhausts IP addresses when running only 30 pods per node. How do you fix this?
- **Standout Technical Answer:**
  - By default, EC2 instances are limited in the number of secondary IPs each ENI can hold (`m5.large` supports max 30 IPs).
  - Enable **AWS VPC CNI Prefix Delegation**:
    - Instead of allocating individual `/32` IP addresses to the ENI, the VPC CNI allocates entire **/28 IPv4 prefixes** (16 IP addresses per slot).
    - Increases pod density per node up to the Linux kernel limit (110–250 pods per node) without subnet exhaustion.

---

### Scenario 49: Container Security: Amazon ECR Image Scanning & Immutable Tags
**Question:** How do you prevent attackers from overwriting the `latest` Docker tag in ECR with a malicious image?
- **Standout Technical Answer:**
  - Enable **Tag Immutability** on the ECR repository: prevents any push from overwriting an existing tag.
  - Enable **Enhanced Scanning with Amazon Inspector**: automatically scans container images for CVEs, operating system package vulnerabilities, and programming language dependencies.

---

### Scenario 50: ECS Rolling Deployments vs Blue/Green with AWS CodeDeploy
**Question:** How does CodeDeploy orchestrate a Blue/Green canary deployment for an ECS service?
- **Standout Technical Answer:**
  - ALB is configured with two target groups: **Production** (port 80/443) and **Test** (port 8443).
  - CodeDeploy provisions the new task set (Green).
  - Test traffic is routed to the Green task set via the test listener.
  - Shifts production traffic using configured routing strategies (e.g., `Linear10PercentEvery1Minute` or `Canary10Percent5Minutes`).
  - If CloudWatch alarms trigger, CodeDeploy automatically rolls back production traffic to the Blue task set instantly.

---

### Scenario 51: App Runner: Container Execution for Web Applications
**Question:** What is AWS App Runner, and when is it preferred over ECS Fargate?
- **Standout Technical Answer:**
  - Fully managed container application service that automatically builds from source code (GitHub) or container images (ECR).
  - Manages ALBs, SSL certificates, auto-scaling, and health checks automatically.
  - Ideal for simple web apps and APIs that don't require complex multi-container networking or custom VPC routing.

---

### Scenario 52: EKS Bottlerocket OS vs Amazon Linux 2
**Question:** Why should production EKS worker nodes run Bottlerocket OS?
- **Standout Technical Answer:**
  - Bottlerocket is an open-source, Linux-based OS purpose-built by AWS for running containers.
  - Has **no package manager, no Python runtime, and no SSH server**.
  - Read-only root filesystem enforced via `dm-verity`.
  - Dramatically smaller attack surface and automated zero-downtime transactional OS updates.

---

### Scenario 53: Container Storage: EFS CSI Driver in EKS
**Question:** How do multiple pods across different Availability Zones share persistent read-write storage in Kubernetes?
- **Standout Technical Answer:**
  - EBS volumes are bound to a single AZ (`ReadWriteOnce`).
  - Deploy the **Amazon EFS CSI Driver** (`ReadWriteMany` - RWX).
  - Pods across multiple AZs mount the shared EFS filesystem over NFS v4.1 with POSIX compliance.

---

### Scenario 54: Distroless Containers & Read-Only Root Filesystems in ECS
**Question:** How do you enforce security hardening in ECS task definitions for zero-trust environments?
- **Standout Technical Answer:**
  - Set `readonlyRootFilesystem: true` in the task definition (forces applications to write temporary files only to attached tmpfs volumes).
  - Use Google Distroless base images (contains only the application and runtime, eliminating shells, curl, and package managers).
  - Drop all default Linux capabilities (`drop: ["ALL"]`).

---

# Part 5: Storage Architectures: S3 Consistency, Lifecycle, Object Lock & EBS/EFS/FSx

---

### Scenario 55: Amazon S3 Strong Consistency Model Internals
**Question:** Describe Amazon S3's consistency model. Can a `GET` request immediately following a `PUT` ever return stale data?
- **Standout Technical Answer:**
  - In December 2020, AWS upgraded Amazon S3 to **Strong Read-After-Write Consistency** for `PUT` and `DELETE` operations on all objects across all AWS regions globally at zero extra cost.
  - Immediately after a successful `PUT` of a new object or an overwrite of an existing object, any subsequent `GET` or `LIST` request is **guaranteed to return the most recently committed version**.
  - S3 achieves this without compromising high availability or single-digit millisecond latency via internal consensus tracking.

---

### Scenario 56: S3 Object Lock: Governance Mode vs Compliance Mode
**Question:** How does S3 Object Lock enforce WORM (Write Once, Read Many) compliance for SEC Rule 17a-4?
- **Standout Technical Answer:**
  - **Governance Mode**: Users with specific IAM permissions (`s3:BypassGovernanceRetention`) can overwrite or delete the locked object version or alter retention periods. Protects against accidental deletion.
  - **Compliance Mode**: **NO ONE**, including the AWS Root Account owner, can delete or overwrite the object version or decrease the retention period until the retention period has expired! Enforces immutable legal and regulatory compliance.

---

### Scenario 57: S3 Multipart Upload Architecture & Abort Incomplete Uploads Rule
**Question:** S3 bucket storage usage is growing mysteriously, but `aws s3 ls` shows only a few gigabytes of data. What is happening?
- **Standout Technical Answer:**
  - Applications are uploading large files via **S3 Multipart Upload**.
  - When multipart uploads fail or are abandoned, the uploaded parts remain stored in S3 indefinitely, consuming storage and generating storage bills while remaining invisible to standard `s3 ls` commands.
  - *Fix*: Configure an S3 Lifecycle Rule with **`AbortIncompleteMultipartUpload`** set to 7 days to purge orphaned parts automatically.

---

### Scenario 58: S3 Cross-Region Replication (CRR) & KMS Key Re-Encryption
**Question:** Objects in S3 Bucket A encrypted with KMS Key A fail to replicate to S3 Bucket B in another region. Why?
- **Standout Technical Answer:**
  - S3 replication configuration requires explicit permissions to use KMS:
    1. The S3 replication IAM role must have `kms:Decrypt` on KMS Key A in the source region.
    2. The replication IAM role must have `kms:Encrypt` on KMS Key B in the destination region.
    3. The replication configuration must explicitly enable **KMS Re-encryption**:
       `SourceSelectionCriteria: { SseKmsEncryptedObjects: { Status: "Enabled" } }`.

---

### Scenario 59: EBS Volume Types: gp3 vs io2 Block Express
**Question:** What are the performance and architectural differences between EBS gp3 and io2 Block Express?
- **Standout Technical Answer:**
  - **gp3**: General purpose SSD. Decouples IOPS and throughput from volume size. Delivers baseline 3,000 IOPS and 125 MB/s throughput; scale up to 16,000 IOPS and 1,000 MB/s.
  - **io2 Block Express**: Mission-critical SAN in the cloud. Sub-millisecond latency; delivers up to **256,000 IOPS and 4,000 MB/s throughput** per volume with 99.999% durability. Supports **EBS Multi-Attach**.

---

### Scenario 60: EBS Multi-Attach Architecture
**Question:** When can you attach an EBS volume to multiple EC2 instances simultaneously, and what filesystem is required?
- **Standout Technical Answer:**
  - Supported on `io1` and `io2` volumes within the **same Availability Zone**.
  - Can attach to up to 16 Nitro-based EC2 instances simultaneously.
  - *Critical Constraint*: Standard filesystems (ext4, XFS) are non-clustered and will corrupt data immediately. You must use a **Cluster-Aware Shared Filesystem** (e.g., GFS2, OCFS2, or Oracle RAC) to coordinate multi-host write operations.

---

### Scenario 61: Amazon EFS Storage Classes & Throughput Modes
**Question:** How do you configure Amazon EFS for a workload that has massive burst write activity but sits idle for hours?
- **Standout Technical Answer:**
  - Use **Elastic Throughput Mode**: Automatically scales throughput up and down based on workload demand; pay only for data read and written (eliminates managing provisioned throughput or monitoring burst credit pools).
  - Use **EFS Lifecycle Management**: Automatically transitions files unaccessed for 30 days to the **EFS Infrequent Access (IA)** tier to reduce storage costs by 92%.

---

### Scenario 62: FSx for Lustre for High-Performance Machine Learning
**Question:** Why is FSx for Lustre used for SageMaker training instead of Amazon S3 directly?
- **Standout Technical Answer:**
  - FSx for Lustre is a POSIX-compliant, massively parallel filesystem delivering hundreds of gigabytes per second throughput and millions of IOPS.
  - Links directly to an S3 bucket: lazily hydrates data from S3 on first access, presents it as a local filesystem to GPU clusters, and writes output checkpoints back to S3 asynchronously.

---

### Scenario 63: S3 Batch Operations
**Question:** You must encrypt 500 million existing unencrypted objects in an S3 bucket with a new KMS key. How do you accomplish this without writing a custom script?
- **Standout Technical Answer:**
  - Use **S3 Batch Operations**:
    1. Generate an S3 Inventory report (CSV/Parquet manifest of all bucket objects).
    2. Create an S3 Batch Operations Job specifying the manifest and operation: **PUT Copy**.
    3. Configure destination encryption with the KMS key.
    4. S3 executes the copy operation across all 500 million objects in parallel with automated retry tracking and audit logging.

---

### Scenario 64: S3 Access Points & Multi-Region Access Points (MRAP)
**Question:** How do S3 Multi-Region Access Points route global client requests to the lowest-latency bucket?
- **Standout Technical Answer:**
  - S3 MRAP provides a single global DNS endpoint backed by AWS Global Accelerator.
  - Client requests enter the nearest AWS edge point of presence and travel over the AWS global backbone.
  - Automatically routes requests to the active S3 bucket in the closest healthy region, supporting active-passive or active-active multi-region architectures.

---

### Scenario 65: AWS Backup vs EBS Snapshots
**Question:** Why should enterprise backup policies be managed with AWS Backup rather than cron scripts executing EBS snapshots?
- **Standout Technical Answer:**
  - **AWS Backup**: Centralized, policy-driven backup service supporting cross-account and cross-region backup copying.
  - Enforces **AWS Backup Vault Lock** (WORM compliance; prevents ransomware from deleting backups even with root account compromise).
  - Supports automated lifecycle transitions to cold storage and audits backup compliance via AWS Backup Audit Manager.

---

### Scenario 66: S3 Glacier Instant Retrieval vs Flexible vs Deep Archive
**Question:** Differentiate retrieval times and economics across the three S3 Glacier tiers.
- **Standout Technical Answer:**
  - **Glacier Instant Retrieval**: Millisecond retrieval; designed for medical archives and media news footage accessed once a quarter.
  - **Glacier Flexible Retrieval**: Minutes to hours (Expedited: 1-5 mins; Standard: 3-5 hours; Bulk: 5-12 hours). Free bulk retrieval.
  - **Glacier Deep Archive**: Lowest cost storage in the cloud ($0.00099/GB/month); 12–48 hour retrieval. Designed for multi-year regulatory retention.

---

### Scenario 67: S3 Intelligent-Tiering Automation
**Question:** How does S3 Intelligent-Tiering save costs on datasets with unpredictable access patterns without retrieval fees?
- **Standout Technical Answer:**
  - Monitors access patterns and automatically moves objects between tiers:
    - Frequent Access $\to$ Infrequent Access (after 30 days of no access).
    - Archive Instant Access (after 90 days).
    - Optional asynchronous Archive / Deep Archive tiers (after 180 days).
  - Charges a small monthly automation fee per 10,000 objects, but charges **zero retrieval fees** when data is accessed.

---

# Part 6: Distributed Databases: Aurora Global, DynamoDB Single-Table & ElastiCache

---

### Scenario 68: Amazon Aurora Storage Engine: The Log is the Database
**Question:** Explain how Amazon Aurora decouples compute from storage, and why it writes 6 copies of data across 3 AZs.
- **Standout Technical Answer:**
  - Traditional databases write full dirty 8KB pages back to disk, causing heavy I/O churn.
  - **Aurora Architecture**:
    - Compute nodes write **only redo log records** to the distributed storage fleet; the storage fleet itself materializes database pages on demand.
    - Data is partitioned into 10GB protection groups replicated **6 ways across 3 Availability Zones**.
    - **Quorum Consensus**:
      - Write Quorum: $4/6$ nodes must acknowledge write (survives loss of an entire AZ plus one additional node without write downtime).
      - Read Quorum: $3/6$ nodes.
    - Aurora continuous peer-to-peer storage repair fixes lagging storage nodes in the background.

```
Aurora 6-Way Storage Fleet:
[ Aurora Compute Writer ]
             |
             +---(Redo Log Streams)---+
             |                        |
             v                        v
[ Storage Node 1a ]  [ Node 1b ]   [ Storage Node 2a ]  [ Node 2b ]   [ Storage Node 3a ]  [ Node 3b ]
   (AZ 1)               (AZ 1)        (AZ 2)               (AZ 2)        (AZ 3)               (AZ 3)
```

---

### Scenario 69: Aurora Serverless v2 Scaling Mechanics
**Question:** How does Aurora Serverless v2 achieve instantaneous scaling in fractions of a second without dropping connections?
- **Standout Technical Answer:**
  - Aurora Serverless v1 scaled by replacing entire compute instances, causing failovers and connection drops.
  - **Aurora Serverless v2**: Adjusts CPU and memory **in-place** within the running instance in fine increments of 0.5 ACUs (Aurora Capacity Units).
  - Cooperates with the underlying hypervisor to dynamically expand/shrink the PostgreSQL/MySQL buffer pool and thread allocations in milliseconds without restarting the database engine.

---

### Scenario 70: Aurora Global Database & Storage-Level Replication
**Question:** How does Aurora Global Database replicate data across regions with $< 1$ second replication lag without impacting primary database performance?
- **Standout Technical Answer:**
  - Replication happens at the **storage engine layer**, not the compute layer.
  - Dedicated storage nodes in the primary region stream redo logs directly to storage nodes in the secondary regions over the AWS dedicated global fiber backbone.
  - Primary compute instance experiences zero performance degradation.
  - Secondary regions provide read-only replicas with cross-region failover (RTO $< 1$ minute, RPO $\approx 1$ second).

---

### Scenario 71: DynamoDB Single-Table Design & Partition Key Cardinality
**Question:** Why do NoSQL architects advocate for Single-Table Design in DynamoDB, and what is the ESR equivalent?
- **Standout Technical Answer:**
  - Relational databases normalize data and execute expensive multi-table SQL `JOIN`s at runtime.
  - **DynamoDB Single-Table Design**:
    - Stores multiple entity types (Users, Orders, OrderItems) in one table using generic partition keys (`PK`) and sort keys (`SK`).
    - Pre-joins related entities in adjacent sort key slots at write time.
    - Fetching a User and their 10 most recent orders requires a **single atomic `Query` operation**:
      `Query(PK = "USER#101", SK begins_with "ORDER#")`.
    - Guarantees single-digit millisecond latency regardless of whether the table has 10,000 or 10 billion items.

---

### Scenario 72: DynamoDB Global Tables & Conflict Resolution
**Question:** How do DynamoDB Global Tables handle concurrent writes to the same item in two different regions?
- **Standout Technical Answer:**
  - DynamoDB Global Tables provide fully managed active-active multi-region replication.
  - **Conflict Resolution Strategy**: **Last-Writer-Wins (LWW)** based on timestamp metadata.
  - If concurrent updates occur in `us-east-1` and `eu-west-1`, DynamoDB evaluates the internal timestamps and the last write overwrites earlier writes.
  - For financial invariants requiring strict serialization, use **DynamoDB Transactions (`TransactWriteItems`)** scoped to a single region.

---

### Scenario 73: DynamoDB Streams & Change Data Capture (CDC) Patterns
**Question:** How do you reliably update an Elasticsearch cluster and invalidate a Redis cache whenever a DynamoDB table changes?
- **Standout Technical Answer:**
  - Enable **DynamoDB Streams** (`NEW_AND_OLD_IMAGES`).
  - Configure an AWS Lambda function triggered by the stream via Event Source Mapping.
  - Stream events contain exact before-and-after snapshots of mutated items in strict chronological order per partition key.
  - Lambda updates Elasticsearch and invalidates Redis asynchronously without adding latency to the primary write path.

---

### Scenario 74: DynamoDB Accelerator (DAX) vs ElastiCache
**Question:** When should you place DAX in front of DynamoDB instead of Redis ElastiCache?
- **Standout Technical Answer:**
  - **DAX (DynamoDB Accelerator)**: Fully managed in-memory write-through cache designed specifically for DynamoDB. Seamlessly drops into applications using the standard DynamoDB SDK; requires zero application code changes for cache invalidation. Reduces read latency from milliseconds to microseconds.
  - **ElastiCache (Redis)**: General-purpose cache supporting complex data structures (sorted sets, hashes, bitmaps, pub-sub). Requires custom application caching and invalidation logic.

---

### Scenario 75: ElastiCache Redis Cluster: Slot Resharding & MOVED Redirection
**Question:** How does Redis Cluster scale writes horizontally across 16,384 hash slots?
- **Standout Technical Answer:**
  - Keys are assigned to one of **16,384 hash slots** via: `HASH_SLOT = CRC16(key) mod 16384`.
  - Slots are distributed across master nodes in the cluster.
  - When a client sends a command for a key residing on another node, the contacted node returns a **`MOVED <slot> <ip>:<port>`** error.
  - Smart Redis clients cache the slot-to-node map to route subsequent commands directly to the correct shard without redirection hops.

---

### Scenario 76: Amazon DocumentDB vs MongoDB
**Question:** What is the architectural difference between Amazon DocumentDB and a native MongoDB replica set?
- **Standout Technical Answer:**
  - DocumentDB emulates the MongoDB 3.6/4.0/5.0 API, but its storage architecture is built on the **Aurora distributed storage subsystem**.
  - Compute is decoupled from storage; storage is replicated 6 ways across 3 AZs.
  - Does not support native MongoDB WiredTiger engine features or arbitrary MongoDB plugins.

---

### Scenario 77: Amazon Keyspaces (Apache Cassandra) Serverless Architecture
**Question:** How does Amazon Keyspaces eliminate Cassandra compaction and tombstone performance degradation?
- **Standout Technical Answer:**
  - Native Apache Cassandra requires manual nodetool maintenance, partition sizing, and tombstone management.
  - Amazon Keyspaces is a serverless Cassandra-compatible database where storage is decoupled from compute and backed by AWS's internal distributed storage engine, eliminating compaction stalls and node repairs.

---

### Scenario 78: RDS Read Replicas vs Multi-AZ Deployments
**Question:** Differentiate the operational purpose of RDS Multi-AZ vs RDS Read Replicas.
- **Standout Technical Answer:**
  - **Multi-AZ**: Synchronous physical replication to a standby instance in another AZ for **High Availability and Disaster Recovery**. Standby instance cannot accept read traffic; DNS automatically fails over in $< 30$ seconds on primary crash.
  - **Read Replicas**: Asynchronous logical replication used to **scale read-heavy workloads** horizontally. Can be promoted to standalone master.

---

### Scenario 79: RDS Zero-Downtime Blue/Green Deployments
**Question:** How does Amazon RDS Blue/Green Deployments eliminate downtime during major PostgreSQL engine upgrades?
- **Standout Technical Answer:**
  - Provisions a complete copy of the production database environment (Green).
  - Establishes logical replication to keep Green in continuous synchronization with Blue.
  - You execute database engine upgrades and schema migrations on the Green database in isolation.
  - Once validated, triggers switchover: RDS blocks writes to Blue, waits for logical replication to catch up, switches DNS endpoints to Green in $< 60$ seconds, and terminates Blue.

---

### Scenario 80: Amazon Timestream for Time-Series Analytics
**Question:** Why is Amazon Timestream optimized for IoT and telemetry over relational databases?
- **Standout Technical Answer:**
  - Features dual-tier storage: in-memory store for high-throughput recent data writes and cost-effective magnetic store for historical data.
  - Built-in time-series SQL functions (interpolation, smoothing, rolling aggregates).
  - Automatically manages retention and data tiering without manual partitioning jobs.

---

# Part 7: Enterprise Security, IAM Governance, Organizations, SCPs & KMS

---

### Scenario 81: IAM Policy Evaluation Logic: The Complete Algorithm
**Question:** Walk through the exact decision-tree algorithm AWS IAM uses to evaluate authorization requests.
- **Standout Technical Answer:**
  1. Default decision is **Implicit Deny**.
  2. Evaluate all applicable policies: **SCPs**, **Permissions Boundaries**, **Identity-Based Policies**, **Resource-Based Policies**, and **Session Policies**.
  3. If ANY policy evaluates to an **Explicit Deny**, the evaluation immediately halts and the final decision is **DENIED**.
  4. In the presence of an SCP: SCP must allow the action.
  5. In the presence of a Permissions Boundary: Boundary must allow the action.
  6. If an **Explicit Allow** is found and no Deny exists, the decision is **ALLOWED**.

```
IAM Evaluation Logic:
[ Request Received ] ---> [ Explicit Deny Anywhere? ] --(YES)--> [ DENIED ]
                                      | (NO)
                         [ SCP Permits Action? ] --------(NO)--> [ DENIED ]
                                      | (YES)
                    [ Permissions Boundary Permits? ] --(NO)--> [ DENIED ]
                                      | (YES)
                         [ Explicit Allow Exists? ] -----(YES)-> [ ALLOWED ]
                                      | (NO)
                                  [ DENIED ] (Implicit Deny)
```

---

### Scenario 82: Service Control Policies (SCPs) & Maximum Permission Boundaries
**Question:** Can an SCP grant permissions to a developer in a member account who does not have an IAM policy?
- **Standout Technical Answer:**
  - **NO**. SCPs are **guardrails (filters)**, not grantors.
  - An SCP defines the *maximum available permissions* for an account.
  - A user must have both an explicit allow in their local account IAM policy AND must not be blocked by any parent SCP in the AWS Organizations tree.
  - Even the AWS Root User in a member account is strictly bound by SCP restrictions.

---

### Scenario 83: AWS KMS Envelope Encryption & Data Encryption Keys (DEKs)
**Question:** Why does AWS KMS use Envelope Encryption instead of directly encrypting 50 GB database files?
- **Standout Technical Answer:**
  - Sending a 50GB file over the network to the KMS API would saturate network bandwidth, introduce massive latency, and breach KMS request payload limits (max 4KB).
  - **Envelope Encryption**:
    1. Application calls KMS `GenerateDataKey(KeyId = "alias/master")`.
    2. KMS returns a **Plaintext DEK** and a **Ciphertext DEK** (encrypted under the KMS Master Key).
    3. Application encrypts the 50GB file locally using AES-256 GCM with the Plaintext DEK.
    4. Application erases the Plaintext DEK from RAM.
    5. Stores the Ciphertext DEK alongside the encrypted 50GB file on disk.

```
Envelope Encryption Architecture:
KMS API ---> GenerateDataKey ---> [ Plaintext DEK ]  +  [ Ciphertext DEK ]
                                          |                     |
                              Encrypts File Locally             |
                                          |                     v
                                          +-----------> [ Encrypted File + Ciphertext DEK ]
```

---

### Scenario 84: KMS Multi-Region Keys vs Cross-Region Replicas
**Question:** How do KMS Multi-Region Keys allow decrypting encrypted data in another region without transferring raw keys over the internet?
- **Standout Technical Answer:**
  - Standard KMS keys are strictly regional and cannot leave their region.
  - **KMS Multi-Region Keys**: Consist of a Primary Key in one region and Replica Keys in other regions sharing the **same key ID and key material**.
  - Allows an application in `us-east-1` to encrypt data, replicate the ciphertext to `eu-west-1`, and decrypt it locally using the `eu-west-1` replica key without cross-region KMS API calls.

---

### Scenario 85: IAM Roles Anywhere for On-Premises Workloads
**Question:** How do on-premises physical servers obtain temporary AWS IAM credentials without static access keys?
- **Standout Technical Answer:**
  - **AWS IAM Roles Anywhere**:
    - Establishes trust between your on-premises Public Key Infrastructure (X.509 Certificate Authority) and AWS IAM.
    - On-premises workload presents a certificate signed by the trusted corporate CA.
    - Workload calls the Roles Anywhere daemon to obtain temporary, rotating 1-hour AWS STS credentials via `CreateSession`.

---

### Scenario 86: AWS Secrets Manager vs SSM Parameter Store
**Question:** When must you use AWS Secrets Manager instead of SSM Parameter Store Advanced Tier?
- **Standout Technical Answer:**
  - **SSM Parameter Store**: Key-value configuration store. Supports SecureString with KMS encryption. Free standard tier; low cost. No automated password rotation.
  - **Secrets Manager**: Purpose-built for secrets. Features **Automated Secrets Rotation** via native integration with Lambda (rotates RDS, DocumentDB, and Redshift passwords automatically without application downtime). Generates CloudTrail audit logs for compliance.

---

### Scenario 87: AWS WAF Web ACL Rules & Rate-Based Shielding
**Question:** How do you configure AWS WAF to block brute-force login attacks on `/login` without impacting standard users?
- **Standout Technical Answer:**
  - Configure a **Rate-Based Rule** in AWS WAF:
    - Scope down statement: `URI Path equals /login` and `HTTP Method equals POST`.
    - Evaluation window: 5 minutes.
    - Rate limit: 100 requests per IP.
    - Action: **Block** (with HTTP 429 status code) or **CAPTCHA**.

---

### Scenario 88: GuardDuty Threat Detection & Machine Learning
**Question:** How does Amazon GuardDuty detect compromised EC2 instances communicating with Tor exit nodes without running host agents?
- **Standout Technical Answer:**
  - GuardDuty operates entirely out-of-band by analyzing foundational AWS data streams: **VPC Flow Logs, DNS Query Logs, AWS CloudTrail Event Logs, and EKS Audit Logs**.
  - Employs machine learning, anomaly detection, and integrated threat intelligence feeds (Proofpoint, CrowdStrike) to identify cryptocurrency mining, credential exfiltration, and unusual API activity.

---

### Scenario 89: AWS Security Hub & Automated Remediation
**Question:** How do you automatically quarantine an S3 bucket that becomes publicly accessible across an AWS Organization?
- **Standout Technical Answer:**
  - **AWS Security Hub** continuously evaluates compliance against CIS AWS Foundations Benchmark.
  - When an S3 bucket violation is detected, Security Hub raises a finding.
  - **EventBridge** captures the finding and triggers an **AWS Systems Manager (SSM) Automation Runbook** to invoke `PutPublicAccessBlock` on the violating bucket, enforcing instant remediation.

---

### Scenario 90: AWS CloudTrail: Management Events vs Data Events
**Question:** Why are S3 object-level operations (`GetObject`, `PutObject`) not logged in CloudTrail by default?
- **Standout Technical Answer:**
  - CloudTrail distinguishes between:
    - **Management Events**: Control plane operations (e.g., `CreateBucket`, `RunInstances`, `AttachPolicy`). Logged by default.
    - **Data Events**: High-volume resource-level operations (e.g., `S3:GetObject`, `Lambda:Invoke`). Disabled by default due to high log volume and CloudTrail per-event processing charges. Must be explicitly enabled.

---

### Scenario 91: AWS Macie: Sensitive Data Discovery with ML
**Question:** How do you discover unencrypted PII (Social Security numbers, credit cards) stored in S3 buckets across 50 accounts?
- **Standout Technical Answer:**
  - Enable **Amazon Macie** at the AWS Organizations management account level.
  - Macie utilizes machine learning and pattern matching to scan objects in S3 buckets across member accounts, generating findings when sensitive PII, PHI, or credential keys are detected.

---

### Scenario 92: AWS Control Tower & Account Factory
**Question:** How does AWS Control Tower enforce multi-account landing zone compliance?
- **Standout Technical Answer:**
  - Automates provisioning of new AWS accounts via **Account Factory** (pre-configured with VPCs, logging, and IAM SSO).
  - Enforces **Guardrails**:
    - *Preventative Guardrails*: Implemented via SCPs (e.g., disallow deleting CloudTrail).
    - *Detective Guardrails*: Implemented via AWS Config rules (e.g., detect unencrypted EBS volumes).

---

# Part 8: Edge Networking, CloudFront, Route 53, WAF & Disaster Recovery

---

### Scenario 93: CloudFront Origin Access Control (OAC) vs Legacy OAI
**Question:** Why has AWS deprecated Origin Access Identity (OAI) in favor of Origin Access Control (OAC) for Amazon S3?
- **Standout Technical Answer:**
  - **OAI**: Legacy mechanism that did not support AWS KMS SSE-KMS encrypted S3 objects, did not support all AWS regions, and lacked HTTP `PUT`/`DELETE` support.
  - **OAC (Origin Access Control)**:
    - Uses AWS Signature Version 4 (SigV4) authentication.
    - Fully supports SSE-KMS encrypted S3 buckets with proper KMS key policies.
    - Supports dynamic S3 multi-region replication access points and granular resource-based policies.

---

### Scenario 94: CloudFront Functions vs Lambda@Edge
**Question:** Differentiate execution models, scale, and performance between CloudFront Functions and Lambda@Edge.
- **Standout Technical Answer:**
  - **CloudFront Functions**:
    - Ultra-lightweight JavaScript runtime running directly in **all 450+ CloudFront Edge Points of Presence (PoPs)**.
    - Sub-millisecond execution; scales to millions of QPS.
    - Evaluated on Viewer Request / Viewer Response. Cannot access request bodies or external networks. *Best for*: URL rewrites, header manipulation, JWT validation.
  - **Lambda@Edge**:
    - Full Node.js and Python runtimes executing in **Regional Edge Caches (approx 13 locations)**.
    - Can make network calls to external databases, access request bodies, and execute complex business logic (execution timeouts up to 30s).

---

### Scenario 95: CloudFront Signed URLs vs Signed Cookies
**Question:** When would you choose CloudFront Signed Cookies over Signed URLs for protecting video streaming content?
- **Standout Technical Answer:**
  - **Signed URLs**: Provide access to individual files (e.g., a single downloadable software installer).
  - **Signed Cookies**: Provide access to **multiple restricted files** without altering URL paths (e.g., HLS or DASH video streams consisting of thousands of `.ts` segment files referenced by a single `.m3u8` master playlist).

---

### Scenario 96: Disaster Recovery Strategies: RPO and RTO Continuum
**Question:** Contrast the 4 classic AWS Disaster Recovery strategies across RPO, RTO, and cost.
- **Standout Technical Answer:**
  1. **Backup and Restore**: High RPO (hours), High RTO (24+ hours), Lowest cost. Recreates infrastructure from S3/AWS Backup upon disaster.
  2. **Pilot Light**: Low RPO (minutes), RTO (tens of minutes). Core data is continuously replicated to secondary region (e.g., Aurora Global DB); compute resources are provisioned from AMIs/IaC upon failover.
  3. **Warm Standby**: Low RPO (seconds), Low RTO (minutes). Scaled-down version of full environment runs 24/7 in secondary region; auto-scales up during disaster.
  4. **Multi-Site Active-Active**: Zero/near-zero RPO, RTO (seconds). Full production capacity operates concurrently across multiple regions via Route 53 latency/weighted routing. Highest cost.

```
Disaster Recovery Trade-Off Spectrum:
[ Backup & Restore ] ---> [ Pilot Light ] ---> [ Warm Standby ] ---> [ Multi-Site Active-Active ]
Highest RTO / Lowest Cost                                           Near-Zero RTO / Highest Cost
```

---

### Scenario 97: Route 53 Split-View (Split-Horizon) DNS
**Question:** How do you configure Route 53 so that `api.corp.com` resolves to a private IP inside the VPC and a public IP for internet clients?
- **Standout Technical Answer:**
  - Create **two Route 53 Hosted Zones** with the exact same domain name `api.corp.com`:
    1. **Private Hosted Zone**: Associated with the VPC; contains an `A` record pointing to the internal ALB private IP (`10.0.1.50`).
    2. **Public Hosted Zone**: Publicly accessible on the internet; contains an `A` record pointing to the external public ALB.
  - Queries originating within the VPC hit the private zone; external queries hit the public zone.

---

### Scenario 98: AWS Elastic Disaster Recovery (AWS DRS)
**Question:** How does AWS DRS achieve low-cost RPO/RTO for on-premises physical servers replicating to AWS?
- **Standout Technical Answer:**
  - Installs a lightweight agent on source servers.
  - Continuously replicates storage blocks at the **block level** into an inexpensive staging area in your AWS account (low-cost EBS volumes).
  - Compute instances are **not launched during normal replication**!
  - Upon disaster declaration, AWS DRS automatically launches target EC2 instances and attaches materialized EBS volumes in minutes.

---

### Scenario 99: CloudFront Cache-Control: `s-maxage` vs `max-age`
**Question:** An API response returns `Cache-Control: max-age=60, s-maxage=3600`. How do the browser and CloudFront CDN cache this?
- **Standout Technical Answer:**
  - **`s-maxage=3600`**: Directs shared public caches (CloudFront CDN) to cache the object for **3,600 seconds (1 hour)**.
  - **`max-age=60`**: Directs private client caches (user's web browser) to cache the object for only **60 seconds**.
  - Allows edge caching while ensuring client browsers refresh state frequently.

---

### Scenario 100: AWS Fault Injection Service (FIS) & Chaos Engineering
**Question:** How do you validate that an Aurora Multi-AZ database failover completes without crashing upstream microservices?
- **Standout Technical Answer:**
  - Execute a controlled chaos experiment using **AWS Fault Injection Service (FIS)**.
  - Target the Aurora DB cluster with the `aws:rds:reboot-db-instance` action with `failover = true`.
  - Monitor upstream synthetic HTTP traffic, error rates, and connection retry mechanisms in CloudWatch to ensure Resilience4j circuit breakers and HikariCP connection pools handle the 20-second failover seamlessly.

---

# Layer 6: Fatal Anti-Patterns & Certification Traps

---

### Anti-Pattern 1: Relying on Public NAT Gateways for High-Volume S3/DynamoDB Ingestion
- ❌ **The Anti-Pattern**: Pushing terabytes of analytical data from private subnets to S3 through a NAT Gateway.
- 💥 **Production Impact**: Massive NAT Gateway data processing charges ($0.045/GB), easily leading to five-figure monthly billing surprises.
- ✅ **The Fix**: Deploy **VPC Gateway Endpoints** for S3 and DynamoDB (free of charge, instant prefix-list routing).

---

### Anti-Pattern 2: Hardcoding Static AWS Access Keys in EC2/Lambda
- ❌ **The Anti-Pattern**: Baking `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` into code or container environment variables.
- 💥 **Production Impact**: Secret leakage on GitHub leading to compromised infrastructure and cryptojacking within minutes.
- ✅ **The Fix**: Use **IAM Roles for EC2 / Task Roles for ECS / Execution Roles for Lambda** with temporary, rotating STS credentials.

---

### Anti-Pattern 3: Single Availability Zone Deployment for Production Workloads
- ❌ **The Anti-Pattern**: Deploying EC2 instances, RDS databases, or ALB targets in a single AZ.
- 💥 **Production Impact**: A localized datacenter power failure or fiber cut takes down entire production operations.
- ✅ **The Fix**: Always architect across a minimum of **3 Availability Zones** with Multi-AZ RDS and Auto Scaling Groups.

---

# Layer 7: Globally Reported Production Incidents & Post-Mortems

---

### Incident 1: AWS US-East-1 Kinesis & CloudWatch Dependency Cascade (November 2020)
- 🚨 **The Incident**: On November 25, 2020, Amazon Kinesis in `us-east-1` experienced severe performance degradation, cascading into outages across Cognitio, EventBridge, CloudWatch, and thousands of customer applications.
- 🔍 **Root Cause**: An operational procedure added capacity to the Kinesis front-end fleet. As new servers joined, each existing server exceeded the maximum operating system thread limit, causing front-end servers to enter an unrecoverable crash loop.
- 🛠️ **Remediation**: AWS moved to dedicated front-end clusters, modified the operating system thread limits, and isolated internal telemetry reporting from customer data planes.
- 🛡️ **Architectural Guardrail**: Always isolate critical internal control planes from external telemetry subsystems.

---

# Layer 8: Rapid-Fire Formula & Sizing Matrix

---

### Critical AWS Limits, Sizing & Formulas

| Service / Parameter | Limit / Formula | Architectural Rule |
| :--- | :--- | :--- |
| **S3 Gateway Endpoint** | Prefix-list route table entry | 100% Free; mandatory for all VPCs |
| **Aurora Quorum** | $4/6$ writes, $3/6$ reads across 3 AZs | Tolerates loss of 1 full AZ + 1 node |
| **Lambda Concurrency** | Default 1,000 per region | Use RDS Proxy to prevent database exhaustion |
| **DynamoDB RCU Sizing** | 1 RCU = 1 Strongly Consistent read of 4KB/s | Eventual consistency reads consume 0.5 RCU |
| **DynamoDB WCU Sizing** | 1 WCU = 1 write of 1KB/s | Transactions consume 2x WCUs |
| **EBS gp3 Baseline** | 3,000 IOPS & 125 MB/s | Independent of volume size |
| **IMDSv2 Protection** | Session token via HTTP `PUT` | Prevents SSRF credential exfiltration |
