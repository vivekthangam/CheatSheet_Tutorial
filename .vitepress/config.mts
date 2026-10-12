import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'
import { bundledLanguages } from 'shiki'

export default withMermaid(
  defineConfig({
    title: "Developer & Architect Hub",
    description: "Production-grade engineering documentation, interview master guides, and scenario handbook.",
    base: '/vivekthangam/CheatSheet_Tutorial/',
    srcDir: '.',
    rewrites: {
      'README.md': 'index.md'
    },
    ignoreDeadLinks: true,
    mermaid: {
      // Mermaid configuration options
      securityLevel: 'loose',
      startOnLoad: false
    },
    markdown: {
      theme: {
        light: 'github-light',
        dark: 'github-dark'
      },
      languages: Object.keys(bundledLanguages),
      config: (md) => {
        // Normalize ascii, ditaa and diagram code blocks to text for clean rendering
        const defaultFence = md.renderer.rules.fence!
        md.renderer.rules.fence = (tokens, idx, options, env, self) => {
          const token = tokens[idx]
          const info = (token.info || '').trim()
          if (info === 'ascii' || info === 'ditaa' || info === 'diagram') {
            token.info = 'text'
          }
          return defaultFence(tokens, idx, options, env, self)
        }
      }
    },
    themeConfig: {
      search: {
        provider: 'local'
      },
      nav: [
        { text: 'Home', link: '/' },
        { text: '🎯 Interview Master Guides', link: '/interview_prep/README' },
        { text: '🔥 Scenario Banks', link: '/scenarios/java_threads_concurrency_200_scenarios_master_guide' },
        { text: '🗺️ Learning Path', link: '/LEARNING_PATH' },
        { text: '📚 Master Index', link: '/all_markdown_files_categorized' }
      ],

      // 📑 Comprehensive Sidebar Matching All Repository Folders & New Docs
      sidebar: [
        {
          text: 'Getting Started',
          items: [
            { text: '🚀 Hub ReadMe', link: '/README' },
            { text: '🎯 Interview Master Curriculum', link: '/interview_prep/README' },
            { text: '🗺️ Learning Path', link: '/LEARNING_PATH' },
            { text: '📚 Categorized Index', link: '/all_markdown_files_categorized' }
          ]
        },
        {
          text: '🎯 Interview Prep: Core Java (J01–J31)',
          collapsed: false,
          items: [
            { text: 'J01: Java Interview Master Guide', link: '/interview_prep/01_java_core/java_interview_master_guide' },
            { text: 'J02: Multithreading & Concurrency', link: '/interview_prep/01_java_core/java_multithreading_concurrency_mastery' },
            { text: 'J03: Collections Framework Internals', link: '/interview_prep/01_java_core/java_collections_internals_mastery' },
            { text: 'J04: CompletableFuture & Async Engine', link: '/interview_prep/01_java_core/completable_future_async' },
            { text: 'J05: I/O, NIO Channels & Zero-Copy', link: '/interview_prep/01_java_core/java_io_nio_zero_copy_mastery' },
            { text: 'J06: JVM Architecture & ClassLoaders', link: '/interview_prep/01_java_core/java_internals_deep_dive' },
            { text: 'J07: Garbage Collection & Profiling', link: '/interview_prep/01_java_core/jvm_gc_profiling' },
            { text: 'J08: JIT Compiler (C1/C2/Graal)', link: '/interview_prep/01_java_core/jvm_jit_compiler_deep_dive' },
            { text: 'J09: Memory Model (JMM) & Hardware', link: '/interview_prep/01_java_core/java_memory_model_jmm_hardware' },
            { text: 'J10: Memory Leaks & OOM Diagnostics', link: '/interview_prep/01_java_core/java_memory_leaks_oom_diagnostics' },
            { text: 'J11: Reflection, Proxies & ByteBuddy', link: '/interview_prep/01_java_core/java_reflection_proxies_bytebuddy' },
            { text: 'J12: Functional & Streams Mastery', link: '/interview_prep/01_java_core/java_functional_interfaces_lambdas_streams' },
            { text: 'J13: Modern Java 21-25 & Loom', link: '/interview_prep/01_java_core/java_modern_features_loom_concurrency' },
            { text: 'J14: JPMS Modules & ClassLoaders', link: '/interview_prep/01_java_core/jpms_modules_classloaders' },
            { text: 'J15: Enterprise Design Patterns', link: '/interview_prep/01_java_core/java_design_patterns_enterprise' },
            { text: 'J16: SOLID & Architectural Principles', link: '/interview_prep/01_java_core/design_principles_solid' },
            { text: 'J17: High-Level System Design (HLD)', link: '/interview_prep/01_java_core/java_system_design_hld' },
            { text: 'J18: Low-Level System Design (LLD)', link: '/interview_prep/01_java_core/java_system_design_lld' },
            { text: 'J19: JUnit 5, Mockito & ArchUnit', link: '/interview_prep/01_java_core/java_testing_junit5_mockito_archunit' },
            { text: 'J20: JMH Performance Benchmarking', link: '/interview_prep/01_java_core/java_jmh_performance_engineering' },
            { text: 'J21: Jackson Serialization Engine', link: '/interview_prep/01_java_core/jackson_json_mastery' },
            { text: 'J22: Maven Build Automation', link: '/interview_prep/01_java_core/maven_build_automation_mastery' },
            { text: 'J23: Gradle Build Automation', link: '/interview_prep/01_java_core/gradle_build_automation_mastery' },
            { text: 'J24: Kotlin for Java Architects', link: '/interview_prep/01_java_core/kotlin_for_java_architects' },
            { text: 'J25: Generics & Type System Mastery', link: '/interview_prep/01_java_core/java_generics_type_system_mastery' },
            { text: 'J26: Cryptography & Security Architecture', link: '/interview_prep/01_java_core/java_cryptography_security_mastery' },
            { text: 'J27: JDBC, HikariCP & Connection Pooling', link: '/interview_prep/01_java_core/java_jdbc_hikaricp_mastery' },
            { text: 'J28: Java 8 to 25 Roadmap & Evolution', link: '/interview_prep/01_java_core/java_version_evolution_8_to_25' },
            { text: 'J29: Exception & Error Architecture', link: '/interview_prep/01_java_core/java_exceptions_error_architecture' },
            { text: 'J30: OOP Core Fundamentals', link: '/interview_prep/01_java_core/java_oop_core_fundamentals' },
            { text: 'J31: JDBC Stored Procedures & Callables', link: '/interview_prep/01_java_core/java_jdbc_stored_procedures_callable_statements' }
          ]
        },
        {
          text: '🎯 Interview Prep: Spring Framework (S01–S23)',
          collapsed: false,
          items: [
            { text: 'S01: Spring Core, IoC & Boot AutoConfig', link: '/interview_prep/02_spring_framework/spring_core_ioc_boot' },
            { text: 'S02: Spring Data JPA & Hibernate 6', link: '/interview_prep/02_spring_framework/spring_data_jpa_hibernate' },
            { text: 'S03: Spring Security 6 & OAuth2 / OIDC', link: '/interview_prep/02_spring_framework/spring_security_oauth2' },
            { text: 'S04: Spring WebFlux & Reactive Systems', link: '/interview_prep/02_spring_framework/spring_webflux_reactive' },
            { text: 'S05: Spring for Apache Kafka', link: '/interview_prep/02_spring_framework/spring_kafka_events' },
            { text: 'S06: Spring AOP & Dynamic Proxies', link: '/interview_prep/02_spring_framework/spring_aop_proxies' },
            { text: 'S07: Spring Boot Internals & Starters', link: '/interview_prep/02_spring_framework/spring_boot_internals_autoconfiguration' },
            { text: 'S08: Spring Cloud Microservices & Resilience', link: '/interview_prep/02_spring_framework/spring_cloud_microservices_resilience' },
            { text: 'S09: Spring Batch High-Throughput ETL', link: '/interview_prep/02_spring_framework/spring_batch_high_throughput' },
            { text: 'S10: Spring Integration & Apache Camel EIP', link: '/interview_prep/02_spring_framework/spring_integration_apache_camel' },
            { text: 'S11: Spring Testing & Testcontainers', link: '/interview_prep/02_spring_framework/spring_testing_testcontainers' },
            { text: 'S12: Spring Cache & Redis Clustering', link: '/interview_prep/02_spring_framework/spring_cache_redis_distributed' },
            { text: 'S13: Spring Modulith Modular Monoliths', link: '/interview_prep/02_spring_framework/spring_modulith_modular_monoliths' },
            { text: 'S14: GraalVM Native Image & AOT Engine', link: '/interview_prep/02_spring_framework/spring_boot_graalvm_native_aot' },
            { text: 'S15: Spring Actuator & Micrometer OTel', link: '/interview_prep/02_spring_framework/spring_actuator_observability_micrometer' },
            { text: 'S16: WebSocket, STOMP & RSocket', link: '/interview_prep/02_spring_framework/spring_websocket_stomp_rsocket' },
            { text: 'S17: Web MVC & REST Internals', link: '/interview_prep/02_spring_framework/spring_web_mvc_rest_internals' },
            { text: 'S18: Spring SQL, JDBC & Transactions', link: '/interview_prep/02_spring_framework/spring_sql_jdbc_transactions' },
            { text: 'S19: Spring AI & Enterprise GenAI RAG', link: '/interview_prep/02_spring_framework/spring_ai_genai_architecture' },
            { text: 'S20: Spring Session & State Clustering', link: '/interview_prep/02_spring_framework/spring_session_clustering' },
            { text: 'S21: Spring Data JPA Stored Procedures', link: '/interview_prep/02_spring_framework/spring_jpa_jdbc_stored_procedures' },
            { text: 'S22: Spring AMQP & RabbitMQ Architecture', link: '/interview_prep/02_spring_framework/spring_amqp_rabbitmq' },
            { text: 'S23: Spring JMS, ActiveMQ & IBM MQ', link: '/interview_prep/02_spring_framework/spring_jms_activemq_ibmmq' }
          ]
        },
        {
          text: '🎯 Interview Prep: Cloud, DevOps & Distributed',
          collapsed: true,
          items: [
            { text: 'AWS 100-Scenario Master Guide', link: '/interview_prep/03_cloud_infrastructure/aws_architecture' },
            { text: 'Azure 100-Scenario Master Guide', link: '/interview_prep/03_cloud_infrastructure/azure_architecture' },
            { text: 'GCP 100-Scenario Master Guide', link: '/interview_prep/03_cloud_infrastructure/gcp_architecture' },
            { text: 'Kubernetes CKA/CKAD/CKS Architecture', link: '/interview_prep/03_cloud_infrastructure/kubernetes_orchestration' },
            { text: 'Kubernetes Production Operations', link: '/interview_prep/04_cloud_kubernetes/kubernetes_production_operations' },
            { text: 'Helm v3 100-Scenario GitOps Guide', link: '/interview_prep/03_cloud_infrastructure/helm_cloud_native' },
            { text: 'Docker & Container Runtimes (DCA)', link: '/interview_prep/03_cloud_infrastructure/docker_containers' },
            { text: 'Service Mesh & Envoy Proxy', link: '/interview_prep/03_cloud_infrastructure/envoy_istio_service_mesh' },
            { text: 'NGINX Architecture & Edge Proxy', link: '/interview_prep/03_cloud_infrastructure/nginx_reverse_proxy' },
            { text: 'Apache HTTPD & Tomcat Internals', link: '/interview_prep/03_cloud_infrastructure/apache_httpd_tomcat' },
            { text: 'Linux Systems & OS Kernel', link: '/interview_prep/03_cloud_infrastructure/linux_systems' },
            { text: 'Bash & PowerShell Enterprise Scripting', link: '/interview_prep/03_cloud_infrastructure/bash_powershell_scripting' },
            { text: 'DevOps Pipelines & Terraform', link: '/interview_prep/04_devops_cicd_iac/devops_cicd_terraform' },
            { text: 'Terraform Declarative IaC Engine', link: '/interview_prep/04_devops_cicd_iac/terraform_iac' },
            { text: 'Jenkins Enterprise CI/CD Pipelines', link: '/interview_prep/04_devops_cicd_iac/jenkins_cicd' },
            { text: 'Git Internals & GitHub Workflows', link: '/interview_prep/04_devops_cicd_iac/git_github' },
            { text: 'ArgoCD Declarative GitOps', link: '/interview_prep/04_devops_cicd_iac/argocd_gitops' },
            { text: 'Ansible Agentless Automation', link: '/interview_prep/04_devops_cicd_iac/ansible_automation' },
            { text: 'Chef Convergent Systems', link: '/interview_prep/04_devops_cicd_iac/chef_configuration' },
            { text: 'Vagrant Virtualization Architecture', link: '/interview_prep/04_devops_cicd_iac/vagrant_virtualization' }
          ]
        },
        {
          text: '🎯 Interview Prep: DB, Security, AI & Systems',
          collapsed: true,
          items: [
            { text: 'PostgreSQL Architecture & SQL Deep Dive', link: '/interview_prep/05_databases_persistence/sql_postgresql_internals' },
            { text: 'PostgreSQL Engine Internals & MVCC', link: '/interview_prep/05_databases_persistence/postgresql_internals' },
            { text: 'SQL Normalization & ACID Isolation', link: '/interview_prep/05_databases_persistence/sql_normalization_acid' },
            { text: 'MongoDB Polyglot Architecture', link: '/interview_prep/05_databases_persistence/mongodb_polyglot' },
            { text: 'Distributed Messaging & Event Queues', link: '/interview_prep/06_messaging_distributed/messaging_distributed_systems' },
            { text: 'Apache Kafka Storage & KRaft Internals', link: '/interview_prep/06_messaging_distributed/kafka_internals' },
            { text: 'Kafka Event-Driven Streaming', link: '/interview_prep/03_distributed_systems/kafka_event_driven_streaming' },
            { text: 'RabbitMQ & AMQP Protocol Engineering', link: '/interview_prep/06_messaging_distributed/rabbitmq_amqp' },
            { text: 'Redis In-Memory Architecture', link: '/interview_prep/06_messaging_distributed/redis_caching' },
            { text: 'ActiveMQ & JMS Enterprise Messaging', link: '/interview_prep/06_messaging_distributed/activemq_enterprise' },
            { text: 'Keycloak IAM & OAuth2 Architecture', link: '/interview_prep/07_security_identity/keycloak_iam' },
            { text: 'Security, Identity & OWASP Top 10', link: '/interview_prep/07_security_identity/security_identity_owasp' },
            { text: 'LGTM Stack & OpenTelemetry SRE', link: '/interview_prep/08_observability_sre/lgtm_opentelemetry' },
            { text: 'GraphQL & gRPC API Protocols', link: '/interview_prep/09_frontend_web/graphql_grpc_api' },
            { text: 'React & Next.js App Router', link: '/interview_prep/09_frontend_web/react_nextjs' },
            { text: 'Golang Systems Architecture', link: '/interview_prep/10_systems_languages/golang_systems' },
            { text: 'Rust Systems Architecture', link: '/interview_prep/10_systems_languages/rust_systems' },
            { text: 'AI, GenAI & RAG Engineering', link: '/interview_prep/11_ai_algorithms/ai_genai_rag_engineering' },
            { text: 'System Design & Applied DSA', link: '/interview_prep/11_ai_algorithms/system_design_dsa' },
            { text: 'Enterprise QA & Test Automation', link: '/interview_prep/12_testing_qa/test_automation_qa' },
            { text: 'Static Site Generators Masterclass', link: '/interview_prep/13_documentation_engines/static_site_generators' }
          ]
        },
        {
          text: '☕ Core Java & JVM Internals (Classic Guides)',
          collapsed: true,
          items: [
            { text: 'Java Master Guide', link: '/java-core/java_master_guide' },
            { text: '500 Coding Scenarios Guide', link: '/java-core/java_500_master_scenarios_coding_interview_guide' },
            { text: 'Project Panama & FFM Native Memory', link: '/java-core/panama_ffm_native_memory_master_guide' },
            { text: 'JPMS Java Modules Deep-Dive', link: '/java-core/jpms_java_modules_master_guide' },
            { text: 'Java Serialization & Security', link: '/java-core/java_serialization_security_master_guide' },
            { text: 'Exceptions & Error Architecture', link: '/java-core/java_exceptions_error_architecture_master_guide' },
            { text: 'Generics & Advanced Type System', link: '/java-core/java_generics_type_system_master_guide' },
            { text: 'Java Memory Model (JMM)', link: '/java-core/java_memory_model_jmm_master_guide' },
            { text: 'Reflection, Annotations & Proxies', link: '/java-core/java_reflection_annotations_proxies_master_guide' },
            { text: 'JMH Microbenchmarking Mastery', link: '/java-core/java_jmh_microbenchmarking_master_guide' },
            { text: 'JVM Garbage Collection & Profiling', link: '/java-core/jvm_gc_profiling_master_guide' },
            { text: 'JIT Compiler Internals', link: '/java-core/jvm_jit_compiler_master_guide' },
            { text: 'Technical Terms Encyclopedia', link: '/java-core/enterprise_java_technical_terms_master_guide' },
            { text: 'Java Versions & Evolution', link: '/java-core/java_version_features_master_guide' },
            { text: 'Collections & Memory', link: '/java-core/java_collection' },
            { text: 'Streams Recipes', link: '/java-core/java_collection_stream' },
            { text: 'Functional Interfaces & Streams', link: '/java-core/java_functional_interfaces_and_streams_master_guide' },
            { text: 'CompletableFuture & Async', link: '/java-core/completable_future' },
            { text: 'Multithreading & Concurrency', link: '/java-core/java_thread' },
            { text: 'Java I/O & NIO Channels', link: '/java-core/java_io' },
            { text: 'Jackson JSON Serialization', link: '/java-core/jackson_master_guide' },
            { text: 'Java & Spring Cryptography', link: '/java-core/java_spring_cryptography_master_guide' },
            { text: 'Maven & Gradle Combined Guide', link: '/java-core/maven_gradle_master_guide' },
            { text: 'Maven Master Guide', link: '/java-core/maven_master_guide' },
            { text: 'Gradle Master Guide', link: '/java-core/gradle_master_guide' },
            { text: 'Design Principles & SOLID', link: '/java-core/design_principles_solid_master_guide' },
            { text: 'Design Patterns Master Guide', link: '/java-core/java_design_patterns_master_guide' },
            { text: '100 Design Pattern Scenarios', link: '/java-core/java_design_patterns_100_interview_scenarios' }
          ]
        },
        {
          text: '🍃 Spring Framework (Classic Guides)',
          collapsed: true,
          items: [
            { text: 'Spring Master Guide', link: '/spring-framework/spring_master_guide' },
            { text: 'Spring AOP & Proxies', link: '/spring-framework/spring_aop_master_guide' },
            { text: 'Spring Data JPA & Hibernate', link: '/spring-framework/spring_data_jpa' },
            { text: 'Spring Boot Enterprise', link: '/spring-framework/spring_boot' },
            { text: 'Apache Camel 4 Integration', link: '/spring-framework/spring_camel' },
            { text: 'Spring Batch 5+ Processing', link: '/spring-framework/spring_batch' },
            { text: 'Spring SQL & JDBC', link: '/spring-framework/spring_sql' },
            { text: 'Spring Data Redis', link: '/spring-framework/spring_redis' },
            { text: 'Spring Security 6 & JWT', link: '/spring-framework/spring_security' },
            { text: 'Spring for Apache Kafka', link: '/spring-framework/spring_kafka' },
            { text: 'Spring Cloud Microservices', link: '/spring-framework/spring_cloud_microservices' },
            { text: 'Spring WebFlux & Reactive', link: '/spring-framework/spring_webflux_reactive' },
            { text: 'Spring Testing & Testcontainers', link: '/spring-framework/spring_testing' },
            { text: 'Sockets to Spring Boot MVC', link: '/spring-framework/java_sockets_tomcat_spring_boot_mvc_internals_master_guide' },
            { text: 'IoC, Security & JPA Internals', link: '/spring-framework/spring_ioc_security_jpa_internals_master_guide' }
          ]
        },
        {
          text: '☁️ Cloud & Infrastructure',
          collapsed: true,
          items: [
            { text: 'AWS Architecture Guide', link: '/cloud-infrastructure/aws_master_guide' },
            { text: 'Azure Architecture Guide', link: '/cloud-infrastructure/azure_master_guide' },
            { text: 'GCP Architecture Guide', link: '/cloud-infrastructure/google_cloud_master_guide' },
            { text: 'Kubernetes Master Guide', link: '/cloud-infrastructure/kubernetes_master_guide' },
            { text: 'Kubernetes Architecture', link: '/cloud-infrastructure/kubernetes' },
            { text: 'Docker & Container Internals', link: '/cloud-infrastructure/docker_master_guide' },
            { text: 'Java & Spring Boot on K8s', link: '/cloud-infrastructure/java_spring_boot_docker_kubernetes_master_guide' },
            { text: 'Linux Systems & Admin', link: '/cloud-infrastructure/linux' },
            { text: 'Vi, Vim & Nano Editors', link: '/cloud-infrastructure/vi_vim_nano_master_guide' },
            { text: 'Bash, Batch & PowerShell', link: '/cloud-infrastructure/bash_batch_powershell_master_guide' },
            { text: 'PowerShell 7+ Automation', link: '/cloud-infrastructure/powershell_master_guide' },
            { text: 'NGINX Edge Proxy', link: '/cloud-infrastructure/nginx_master_guide' },
            { text: 'Apache HTTPD & LAMP Stack', link: '/cloud-infrastructure/apache_httpd_lamp_master_guide' },
            { text: 'Apache Tomcat Container', link: '/cloud-infrastructure/apache_tomcat_master_guide' },
            { text: 'Envoy Proxy L4/L7', link: '/cloud-infrastructure/envoy_proxy_master_guide' },
            { text: 'Istio Service Mesh', link: '/cloud-infrastructure/istio_service_mesh_master_guide' },
            { text: 'Web Servers & Mesh Comparison', link: '/cloud-infrastructure/istio_envoy_nginx_apache_tomcat_lamp_master_guide' }
          ]
        },
        {
          text: '🔄 DevOps, CI/CD & IaC',
          collapsed: true,
          items: [
            { text: 'Jenkins CI/CD Pipeline', link: '/devops-cicd-iac/jenkins_master_guide' },
            { text: 'ArgoCD & GitOps', link: '/devops-cicd-iac/argocd_master_guide' },
            { text: 'Zero-Downtime Deployments', link: '/devops-cicd-iac/deployment_strategies_master_guide' },
            { text: 'Git Version Control', link: '/devops-cicd-iac/git_master_guide' },
            { text: 'GitHub Enterprise Governance', link: '/devops-cicd-iac/github_master_guide' },
            { text: 'GitHub Actions CI/CD', link: '/devops-cicd-iac/github_actions_master_guide' },
            { text: 'GitHub Pages & CDN', link: '/devops-cicd-iac/github_pages_master_guide' },
            { text: 'Vagrant Virtualization', link: '/devops-cicd-iac/vagrant_master_guide' },
            { text: 'Ansible Automation', link: '/devops-cicd-iac/ansible_master_guide' },
            { text: 'Terraform & IaC', link: '/devops-cicd-iac/terraform_master_guide' },
            { text: 'Chef Infra Configuration', link: '/devops-cicd-iac/chef_master_guide' },
            { text: 'DevOps Technical Terms', link: '/devops-cicd-iac/devops_iac_technical_terms_master_guide' }
          ]
        },
        {
          text: '🗄️ Databases & Persistence',
          collapsed: true,
          items: [
            { text: 'SQL Master Reference', link: '/databases-persistence/sql' },
            { text: 'SQL Normalization & ACID', link: '/databases-persistence/sql_normalization_acid_master_guide' },
            { text: 'MongoDB Polyglot Guide', link: '/databases-persistence/mongodb_master_guide' },
            { text: 'PostgreSQL Internals', link: '/databases-persistence/postgresql_master_guide' }
          ]
        },
        {
          text: '📨 Messaging & Distributed Systems',
          collapsed: true,
          items: [
            { text: 'Message Queues Master Guide', link: '/messaging-distributed/message_queues_master_guide' },
            { text: 'Message Queues Beginner Guide', link: '/messaging-distributed/message_queues_beginner_guide' },
            { text: 'Kafka Storage & Internals', link: '/messaging-distributed/kafka_internals_master_guide' },
            { text: 'Microservices & Infrastructure', link: '/messaging-distributed/microservices_gateway_infrastructure_master_guide' }
          ]
        },
        {
          text: '🔍 Observability & SRE',
          collapsed: true,
          items: [
            { text: 'LGTM Stack & OTel Guide', link: '/observability-sre/lgtm_master_guide' },
            { text: 'OpenTelemetry Tracing', link: '/observability-sre/opentelemetry_master_guide' }
          ]
        },
        {
          text: '🛡️ Security & Identity',
          collapsed: true,
          items: [
            { text: 'Cryptography Encyclopedia', link: '/security-identity/cryptography_algorithms_master_guide' },
            { text: 'Enterprise Security & Auth', link: '/security-identity/security_auth_master_guide' },
            { text: 'Security Tools & Glossary', link: '/security-identity/security_infra_tools_glossary_master_guide' },
            { text: 'HashiCorp Vault & Secrets', link: '/security-identity/vault_secrets_master_guide' }
          ]
        },
        {
          text: '🌐 Frontend & API Protocols',
          collapsed: true,
          items: [
            { text: 'GraphQL Polyglot Guide', link: '/frontend-web/graphql_polyglot_master_guide' },
            { text: 'gRPC Polyglot Guide', link: '/frontend-web/grpc_polyglot_master_guide' },
            { text: 'REST API, OpenAPI & Swagger', link: '/frontend-web/rest_api_openapi_swagger_master_guide' },
            { text: 'GraphQL Design & DataLoader', link: '/frontend-web/graphql_design_query_generation_master_guide' },
            { text: 'gRPC & Protobuf v3 Design', link: '/frontend-web/grpc_protobuf_design_generation_master_guide' },
            { text: 'Real-Time WebSockets & SSE', link: '/frontend-web/realtime_websockets_sse_socketio_master_guide' },
            { text: 'WebRTC P2P Real-Time', link: '/frontend-web/webrtc_peer_to_peer_streaming_master_guide' },
            { text: 'WebSocket & Socket.IO', link: '/frontend-web/websocket_rfc6455_socketio_master_guide' },
            { text: 'Server-Sent Events (SSE)', link: '/frontend-web/server_sent_events_sse_master_guide' },
            { text: 'WebTransport (QUIC & HTTP/3)', link: '/frontend-web/webtransport_quic_http3_master_guide' },
            { text: 'Network & Transport Protocols', link: '/frontend-web/network_transport_protocols_tcp_udp_tls_http1_2_3_master_guide' },
            { text: 'V8 JIT, React Fiber & TS Internals', link: '/frontend-web/v8_react_ts_core_internals_interview_master_guide' },
            { text: 'Modern JavaScript & V8', link: '/frontend-web/javascript_master_guide' },
            { text: 'TypeScript Compiler Architecture', link: '/frontend-web/typescript_master_guide' },
            { text: 'CSS, Sass & Rendering Engine', link: '/frontend-web/css_sass_master_guide' },
            { text: 'Frontend Polyglot Terms', link: '/frontend-web/frontend_polyglot_technical_terms_master_guide' },
            { text: 'React Architecture Guide', link: '/frontend-web/react_master_guide' },
            { text: 'Angular Architecture Guide', link: '/frontend-web/angular_master_guide' },
            { text: 'Next.js App Router & RSC', link: '/frontend-web/nextjs_rsc_master_guide' },
            { text: 'Tauri 2.0 & Rust Desktop', link: '/frontend-web/tauri_rust_desktop_master_guide' }
          ]
        },
        {
          text: '🦀 Systems Languages',
          collapsed: true,
          items: [
            { text: 'Rust Systems Architecture', link: '/systems-languages/rust_master_guide' },
            { text: 'Rust Technical Terms', link: '/systems-languages/rust_technical_terms_master_guide' },
            { text: 'Golang Systems Architecture', link: '/systems-languages/golang_master_guide' },
            { text: 'Golang Technical Terms', link: '/systems-languages/golang_technical_terms_master_guide' },
            { text: 'Python Engineering & AI', link: '/systems-languages/python_master_guide' },
            { text: 'C & C++ Memory & DSA', link: '/systems-languages/c_cpp_master_guide' }
          ]
        },
        {
          text: '🔥 Scenario Master Banks (49 Banks)',
          collapsed: true,
          items: [
            { text: 'Java Threads & Concurrency (200)', link: '/scenarios/java_threads_concurrency_200_scenarios_master_guide' },
            { text: 'Java Collections & Streams (200)', link: '/scenarios/java_collections_streams_200_scenarios_master_guide' },
            { text: 'Java OOP Core Fundamentals (200)', link: '/scenarios/java_oop_core_fundamentals_200_scenarios_master_guide' },
            { text: 'JVM GC & Memory Leaks (200)', link: '/scenarios/jvm_gc_memory_leaks_200_scenarios_master_guide' },
            { text: 'Java Modern Features 21-25 (200)', link: '/scenarios/java_modern_features_21_25_scenarios_master_guide' },
            { text: 'Java Design Patterns (200)', link: '/scenarios/java_design_patterns_200_scenarios_master_guide' },
            { text: 'Java System Design LLD (200)', link: '/scenarios/java_system_design_lld_200_scenarios_master_guide' },
            { text: 'CompletableFuture & Async (200)', link: '/scenarios/completable_future_200_scenarios_master_guide' },
            { text: 'Java I/O & NIO Channels (200)', link: '/scenarios/java_io_nio_200_scenarios_master_guide' },
            { text: 'Spring Enterprise (100+)', link: '/scenarios/spring_200_scenarios_master_guide' },
            { text: 'Spring AOP Proxies (50+)', link: '/scenarios/spring_aop_scenarios_master_guide' },
            { text: 'Spring Batch High-Throughput (50+)', link: '/scenarios/spring_batch_scenarios_master_guide' },
            { text: 'Apache Camel 4 Integration (50+)', link: '/scenarios/spring_camel_scenarios_master_guide' },
            { text: 'Spring Security 6 (50+)', link: '/scenarios/spring_security_scenarios_master_guide' },
            { text: 'Spring Data JPA & Hibernate (50+)', link: '/scenarios/spring_data_jpa_scenarios_master_guide' },
            { text: 'Spring Cloud Microservices (50+)', link: '/scenarios/spring_cloud_scenarios_master_guide' },
            { text: 'Spring Data Redis Distributed (50+)', link: '/scenarios/spring_redis_scenarios_master_guide' },
            { text: 'Spring Kafka Events (50+)', link: '/scenarios/spring_kafka_scenarios_master_guide' },
            { text: 'Spring WebFlux Reactive (50+)', link: '/scenarios/spring_webflux_scenarios_master_guide' },
            { text: 'Spring Testing & Testcontainers (50+)', link: '/scenarios/spring_testing_scenarios_master_guide' },
            { text: 'Spring SQL & JDBC Transactions (50+)', link: '/scenarios/spring_sql_scenarios_master_guide' },
            { text: 'SQL & Relational DB Internals (200+)', link: '/scenarios/sql_scenarios_master_guide' },
            { text: 'Enterprise Design Patterns (200+)', link: '/scenarios/design_patterns_scenarios_master_guide' },
            { text: 'Applied DSA & Algorithms (200+)', link: '/scenarios/dsa_scenarios_master_guide' },
            { text: 'MongoDB Polyglot Architecture (50+)', link: '/scenarios/mongodb_scenarios_master_guide' },
            { text: 'Jackson JSON Serialization (50+)', link: '/scenarios/jackson_scenarios_master_guide' },
            { text: 'Java & Spring Cryptography (50+)', link: '/scenarios/java_spring_cryptography_scenarios_master_guide' },
            { text: 'Deployment Strategies (50)', link: '/scenarios/deployment_strategies_50_scenarios_master_guide' },
            { text: 'Cloud & K8s Observability (200+)', link: '/scenarios/cloud_kubernetes_observability_scenarios_master_guide' },
            { text: 'Message Queues & Brokers (200+)', link: '/scenarios/message_queues_200_scenarios_master_guide' },
            { text: 'Microservices & Distributed Systems (200+)', link: '/scenarios/microservices_distributed_systems_scenarios_master_guide' },
            { text: 'PostgreSQL Database Internals (200+)', link: '/scenarios/postgresql_database_internals_scenarios_master_guide' },
            { text: 'OAuth2, OIDC & Zero Trust (50+)', link: '/scenarios/oauth2_oidc_zero_trust_scenarios_master_guide' },
            { text: 'Security, AuthN & AuthZ (50+)', link: '/scenarios/security_auth_50_scenarios_master_guide' },
            { text: 'Security & Infrastructure (200+)', link: '/scenarios/security_infra_200_scenarios_master_guide' },
            { text: 'OPA & Rego Policy Engines (200+)', link: '/scenarios/opa_rego_200_scenarios_master_guide' },
            { text: 'API Protocols & Real-Time (200+)', link: '/scenarios/api_protocols_realtime_scenarios_master_guide' },
            { text: 'DevOps & IaC Scenarios', link: '/scenarios/devops_iac_scenarios_master_guide' },
            { text: 'JavaScript Internals (200+)', link: '/scenarios/javascript_scenarios_master_guide' },
            { text: 'TypeScript Internals (200+)', link: '/scenarios/typescript_scenarios_master_guide' },
            { text: 'Node.js Enterprise Scenarios', link: '/scenarios/nodejs_scenarios_master_guide' },
            { text: 'Next.js App Router Scenarios', link: '/scenarios/nextjs_scenarios_master_guide' },
            { text: 'CSS & Sass Layout Engine (200+)', link: '/scenarios/css_sass_scenarios_master_guide' },
            { text: 'React Fiber & Hooks (200+)', link: '/scenarios/react_scenarios_master_guide' },
            { text: 'Angular Signals & RxJS (200+)', link: '/scenarios/angular_scenarios_master_guide' },
            { text: 'Frontend Polyglot (50+)', link: '/scenarios/frontend_scenarios_master_guide' },
            { text: 'Rust Systems Scenarios (50+)', link: '/scenarios/rust_scenarios_master_guide' },
            { text: 'Golang Concurrency Scenarios (50+)', link: '/scenarios/golang_scenarios_master_guide' },
            { text: 'Rust & Golang Dual Scenarios', link: '/scenarios/rust_golang_scenarios_master_guide' }
          ]
        },
        {
          text: '🧠 Deep Dives & Master Topics',
          collapsed: true,
          items: [
            { text: 'Cognitive Master Playbook', link: '/topics/engineers_cognitive_master_playbook' },
            { text: 'Java Profiling Mastery', link: '/topics/java_profiling_mastery' },
            { text: 'Troubleshooting Mastery', link: '/topics/troubleshooting_mastery' },
            { text: 'Splunk Observability Mastery', link: '/topics/observability_splunk_mastery' },
            { text: 'Architectural Decisions & Trade-offs', link: '/topics/architectural_decisions_and_tradeoffs_mastery' },
            { text: 'Enterprise Architecture Glossary', link: '/topics/glossary' },
            { text: 'Microservices Design Patterns', link: '/topics/microservices_design_patterns_mastery' },
            { text: 'Distributed Systems Patterns', link: '/topics/distributed_systems_patterns_mastery' },
            { text: 'DevOps & SRE Playbook', link: '/topics/devops_sre_playbook' },
            { text: 'Full-Stack Performance Optimization', link: '/topics/fullstack_performance_optimization_playbook' },
            { text: 'Software Engineering Best Practices', link: '/topics/software_engineering_best_practices' },
            { text: 'Star Wars Chronological Watch Guide', link: '/topics/star_wars_chronological_watch_guide' }
          ]
        },
        {
          text: '🗣️ Communication & Corporate English',
          collapsed: true,
          items: [
            { text: 'Spoken English & IT Communication', link: '/communication-english/spoken_english_tamil_to_global_master_guide' },
            { text: '500 Spoken English Scenarios', link: '/communication-english/spoken_english_500_scenarios_master_guide' },
            { text: 'English Root Words & Etymology', link: '/communication-english/english_root_words_master_guide' },
            { text: 'Phrasal Verbs & Expressions', link: '/communication-english/english_phrases_master_guide' },
            { text: 'Idioms & Corporate Metaphors', link: '/communication-english/english_idioms_master_guide' },
            { text: 'Advanced Vocabulary (IELTS/TOEFL)', link: '/communication-english/ielts_toefl_advanced_vocabulary_master_guide' },
            { text: 'IELTS 500 Academic Lexicon', link: '/communication-english/ielts_500_words_master_guide' },
            { text: 'Business English & Corporate', link: '/communication-english/business_english_corporate_words_master_guide' },
            { text: 'IT Technical Vocabulary', link: '/communication-english/it_tech_words_master_guide' },
            { text: 'Developer English Vocabulary', link: '/communication-english/developer_english_vocabulary_master_guide' }
          ]
        },
        {
          text: '📚 Documentation, AI & Testing',
          collapsed: true,
          items: [
            { text: 'Static Doc Engines Hub', link: '/documentation-engines/doc_generation_master_guide' },
            { text: 'VitePress Master Guide', link: '/documentation-engines/vitepress_master_guide' },
            { text: 'MkDocs Material Guide', link: '/documentation-engines/mkdocs_material_master_guide' },
            { text: 'Hugo & Hextra Guide', link: '/documentation-engines/hugo_master_guide' },
            { text: 'Starlight (Astro) Guide', link: '/documentation-engines/starlight_astro_master_guide' },
            { text: 'Docusaurus Master Guide', link: '/documentation-engines/docusaurus_master_guide' },
            { text: 'Docsify Master Guide', link: '/documentation-engines/docsify_master_guide' },
            { text: 'AI, GenAI & Prompt Master Guide', link: '/ai-algorithms/ai_genai_master_guide' },
            { text: 'Enterprise RAG & Vector Search', link: '/ai-algorithms/rag_vector_search_master_guide' },
            { text: 'System Design Masterclass', link: '/ai-algorithms/system_design' },
            { text: 'Problem-to-Pattern Blueprints', link: '/ai-algorithms/system_design_problem_to_pattern_blueprint_master_guide' },
            { text: 'Testing & QA Automation', link: '/testing-qa/test_automation_master_guide' },
            { text: 'Cucumber BDD Masterclass', link: '/testing-qa/cucumber' },
            { text: 'Selenium 4 Masterclass', link: '/testing-qa/selenium' }
          ]
        }
      ],

      socialLinks: [
        { icon: 'github', link: 'https://github.com/vivekthangam/CheatSheet_Tutorial' }
      ]
    }
  })
)