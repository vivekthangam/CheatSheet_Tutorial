import { defineConfig } from 'vitepress'

export default defineConfig({
  title: "Developer & Architect Hub",
  description: "Production-grade engineering documentation and scenario handbook.",
  
  // 🎨 Safe markdown configuration without invalid shiki bindings
  markdown: {
    theme: {
      light: 'github-light',
      dark: 'github-dark'
    }
  },

  themeConfig: {
    // 🔍 Enable instant local full-text search
    search: {
      provider: 'local'
    },

    // 🧭 Top Navigation Links
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Learning Path', link: '/LEARNING_PATH' },
      { text: 'Master Index', link: '/all_markdown_files_categorized' }
    ],

    // 📑 Comprehensive Sidebar Matching Your Repo Folders
    sidebar: [
      {
        text: 'Getting Started',
        items: [
          { text: '🚀 Hub ReadMe', link: '/README' },
          { text: '🗺️ Learning Path', link: '/LEARNING_PATH' },
          { text: '📚 Categorized Index', link: '/all_markdown_files_categorized' }
        ]
      },
      {
        text: '☕ Core Java & Concurrency',
        collapsed: false,
        items: [
          { text: 'Java Master Guide', link: '/java-core/java_master_guide' },
          { text: 'Technical Terms Encyclopedia', link: '/java-core/enterprise_java_technical_terms_master_guide' },
          { text: 'JIT Compiler Internals', link: '/java-core/jvm_jit_compiler_master_guide' },
          { text: 'Java Versions & Evolution', link: '/java-core/java_version_features_master_guide' },
          { text: 'Interview Master Guide', link: '/java-core/java_interview_master_guide' },
          { text: 'Collections & Memory', link: '/java-core/java_collection' },
          { text: 'Streams Recipes', link: '/java-core/java_collection_stream' },
          { text: 'CompletableFuture & Async', link: '/java-core/completable_future' },
          { text: 'Multithreading & Concurrency', link: '/java-core/java_thread' },
          { text: 'Java I/O & NIO Channels', link: '/java-core/java_io' },
          { text: 'Jackson JSON Serialization', link: '/java-core/jackson_master_guide' },
          { text: 'Java & Spring Cryptography', link: '/java-core/java_spring_cryptography_master_guide' }
        ]
      },
      {
        text: '🍃 Spring & Frameworks',
        collapsed: false,
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
        collapsed: false,
        items: [
          { text: 'AWS Architecture Guide', link: '/cloud-infrastructure/aws_master_guide' },
          { text: 'Azure Architecture Guide', link: '/cloud-infrastructure/azure_master_guide' },
          { text: 'GCP Architecture Guide', link: '/cloud-infrastructure/google_cloud_master_guide' },
          { text: 'Kubernetes Master Guide', link: '/cloud-infrastructure/kubernetes_master_guide' },
          { text: 'Linux Systems & Admin', link: '/cloud-infrastructure/linux' },
          { text: 'Vi, Vim & Nano Editors', link: '/cloud-infrastructure/vi_vim_nano_master_guide' },
          { text: 'Bash, Batch & PowerShell', link: '/cloud-infrastructure/bash_batch_powershell_master_guide' },
          { text: 'PowerShell 7+ Automation', link: '/cloud-infrastructure/powershell_master_guide' },
          { text: 'NGINX Edge Proxy', link: '/cloud-infrastructure/nginx_master_guide' },
          { text: 'Apache HTTPD & LAMP Stack', link: '/cloud-infrastructure/apache_httpd_lamp_master_guide' },
          { text: 'Apache Tomcat Container', link: '/cloud-infrastructure/apache_tomcat_master_guide' },
          { text: 'Envoy Proxy L4/L7', link: '/cloud-infrastructure/envoy_proxy_master_guide' },
          { text: 'Istio Service Mesh', link: '/cloud-infrastructure/istio_service_mesh_master_guide' },
          { text: 'Web Servers & Mesh Comparison', link: '/cloud-infrastructure/istio_envoy_nginx_apache_tomcat_lamp_master_guide' },
          { text: 'Kubernetes Architecture', link: '/cloud-infrastructure/kubernetes' },
          { text: 'Docker & Container Internals', link: '/cloud-infrastructure/docker_master_guide' },
          { text: 'Java & Spring Boot on K8s', link: '/cloud-infrastructure/java_spring_boot_docker_kubernetes_master_guide' }
        ]
      },
      {
        text: '🔄 DevOps, CI/CD & IaC',
        collapsed: false,
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
        collapsed: false,
        items: [
          { text: 'SQL Master Reference', link: '/databases-persistence/sql' },
          { text: 'SQL Normalization & ACID', link: '/databases-persistence/sql_normalization_acid_master_guide' },
          { text: 'MongoDB Polyglot Guide', link: '/databases-persistence/mongodb_master_guide' },
          { text: 'PostgreSQL Internals', link: '/databases-persistence/postgresql_master_guide' }
        ]
      },
      {
        text: '📨 Messaging & Distributed Systems',
        collapsed: false,
        items: [
          { text: 'Message Queues Master Guide', link: '/messaging-distributed/message_queues_master_guide' },
          { text: 'Message Queues Beginner Guide', link: '/messaging-distributed/message_queues_beginner_guide' },
          { text: 'Kafka Storage & Internals', link: '/messaging-distributed/kafka_internals_master_guide' },
          { text: 'Microservices & Infrastructure', link: '/messaging-distributed/microservices_gateway_infrastructure_master_guide' }
        ]
      },
      {
        text: '🔍 Observability & SRE',
        collapsed: false,
        items: [
          { text: 'LGTM Stack & OTel Guide', link: '/observability-sre/lgtm_master_guide' },
          { text: 'OpenTelemetry Tracing', link: '/observability-sre/opentelemetry_master_guide' }
        ]
      },
      {
        text: '🛡️ Security & Identity',
        collapsed: false,
        items: [
          { text: 'Cryptography Encyclopedia', link: '/security-identity/cryptography_algorithms_master_guide' },
          { text: 'Enterprise Security & Auth', link: '/security-identity/security_auth_master_guide' },
          { text: 'Security Tools & Glossary', link: '/security-identity/security_infra_tools_glossary_master_guide' },
          { text: 'HashiCorp Vault & Secrets', link: '/security-identity/vault_secrets_master_guide' }
        ]
      },
      {
        text: '🌐 Frontend & API Protocols',
        collapsed: false,
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
        text: '🦀 Systems Languages (Rust, Go, Python, C/C++)',
        collapsed: false,
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
        text: '🔥 Scenario Master Banks',
        collapsed: false,
        items: [
          { text: 'Java Threads & Concurrency (200)', link: '/scenarios/java_threads_concurrency_200_scenarios_master_guide' },
          { text: 'Java Collections & Streams (200)', link: '/scenarios/java_collections_streams_200_scenarios_master_guide' },
          { text: 'CompletableFuture (200)', link: '/scenarios/completable_future_200_scenarios_master_guide' },
          { text: 'Java I/O & NIO (200)', link: '/scenarios/java_io_nio_200_scenarios_master_guide' },
          { text: 'Spring Enterprise (100+)', link: '/scenarios/spring_200_scenarios_master_guide' },
          { text: 'Spring AOP (50+)', link: '/scenarios/spring_aop_scenarios_master_guide' },
          { text: 'Spring Batch (50+)', link: '/scenarios/spring_batch_scenarios_master_guide' },
          { text: 'Apache Camel 4 (50+)', link: '/scenarios/spring_camel_scenarios_master_guide' },
          { text: 'Spring Security 6 (50+)', link: '/scenarios/spring_security_scenarios_master_guide' },
          { text: 'Spring Data JPA (50+)', link: '/scenarios/spring_data_jpa_scenarios_master_guide' },
          { text: 'Spring Cloud (50+)', link: '/scenarios/spring_cloud_scenarios_master_guide' },
          { text: 'Spring Data Redis (50+)', link: '/scenarios/spring_redis_scenarios_master_guide' },
          { text: 'Spring Kafka (50+)', link: '/scenarios/spring_kafka_scenarios_master_guide' },
          { text: 'Spring WebFlux (50+)', link: '/scenarios/spring_webflux_scenarios_master_guide' },
          { text: 'Spring Testing (50+)', link: '/scenarios/spring_testing_scenarios_master_guide' },
          { text: 'Spring SQL & JDBC (50+)', link: '/scenarios/spring_sql_scenarios_master_guide' },
          { text: 'SQL & Relational DB (200+)', link: '/scenarios/sql_scenarios_master_guide' },
          { text: 'Enterprise Design Patterns (200+)', link: '/scenarios/design_patterns_scenarios_master_guide' },
          { text: 'Applied DSA (200+)', link: '/scenarios/dsa_scenarios_master_guide' },
          { text: 'MongoDB Polyglot (50+)', link: '/scenarios/mongodb_scenarios_master_guide' },
          { text: 'Jackson JSON (50+)', link: '/scenarios/jackson_scenarios_master_guide' },
          { text: 'Java & Spring Crypto (50+)', link: '/scenarios/java_spring_cryptography_scenarios_master_guide' },
          { text: 'Deployment Strategies (50)', link: '/scenarios/deployment_strategies_50_scenarios_master_guide' },
          { text: 'Cloud & K8s Observability (200+)', link: '/scenarios/cloud_kubernetes_observability_scenarios_master_guide' },
          { text: 'Message Queues (200+)', link: '/scenarios/message_queues_200_scenarios_master_guide' },
          { text: 'Microservices & Distributed Systems (200+)', link: '/scenarios/microservices_distributed_systems_scenarios_master_guide' },
          { text: 'PostgreSQL Database Internals (200+)', link: '/scenarios/postgresql_database_internals_scenarios_master_guide' },
          { text: 'Security, AuthN & AuthZ (50+)', link: '/scenarios/security_auth_50_scenarios_master_guide' },
          { text: 'Security & Infrastructure (200+)', link: '/scenarios/security_infra_200_scenarios_master_guide' },
          { text: 'OPA & Rego (200+)', link: '/scenarios/opa_rego_200_scenarios_master_guide' },
          { text: 'API Protocols & Real-Time (200+)', link: '/scenarios/api_protocols_realtime_scenarios_master_guide' },
          { text: 'JavaScript (200+)', link: '/scenarios/javascript_scenarios_master_guide' },
          { text: 'TypeScript (200+)', link: '/scenarios/typescript_scenarios_master_guide' },
          { text: 'CSS & Sass (200+)', link: '/scenarios/css_sass_scenarios_master_guide' },
          { text: 'React (200+)', link: '/scenarios/react_scenarios_master_guide' },
          { text: 'Angular (200+)', link: '/scenarios/angular_scenarios_master_guide' },
          { text: 'Frontend Polyglot (50+)', link: '/scenarios/frontend_scenarios_master_guide' },
          { text: 'Rust (50+)', link: '/scenarios/rust_scenarios_master_guide' },
          { text: 'Golang (50+)', link: '/scenarios/golang_scenarios_master_guide' }
        ]
      },
      {
        text: '🗣️ Communication & English',
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
        text: '📚 Documentation & Misc',
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
      { icon: 'github', link: 'https://github.com' }
    ]
  }
})