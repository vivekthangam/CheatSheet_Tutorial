$ErrorActionPreference = "SilentlyContinue"

$mapping = @{
    "java-core" = @(
        "java_interview_master_guide.md",
        "java_thread.md",
        "completable_future.md",
        "java_io.md",
        "java_collection.md",
        "java%20collection.md",
        "java collection.md",
        "java_collection_stream.md",
        "jvm_gc_profiling_master_guide.md",
        "jvm_jit_compiler_master_guide.md",
        "jackson_master_guide.md",
        "java_spring_cryptography_master_guide.md",
        "maven_gradle_master_guide.md",
        "enterprise_java_technical_terms_master_guide.md"
    )
    "spring-framework" = @(
        "spring_master_guide.md",
        "spring_boot.md",
        "spring_aop_master_guide.md",
        "spring_data_jpa.md",
        "spring_security.md",
        "spring_sql.md",
        "spring_redis.md",
        "spring_kafka.md",
        "spring_cloud_microservices.md",
        "spring_webflux_reactive.md",
        "spring_batch.md",
        "spring_camel.md",
        "spring_testing.md"
    )
    "scenarios" = @(
        "spring_200_scenarios_master_guide.md",
        "spring_aop_scenarios_master_guide.md",
        "spring_batch_scenarios_master_guide.md",
        "spring_camel_scenarios_master_guide.md",
        "spring_security_scenarios_master_guide.md",
        "spring_data_jpa_scenarios_master_guide.md",
        "spring_cloud_scenarios_master_guide.md",
        "spring_redis_scenarios_master_guide.md",
        "spring_kafka_scenarios_master_guide.md",
        "spring_webflux_scenarios_master_guide.md",
        "spring_testing_scenarios_master_guide.md",
        "spring_sql_scenarios_master_guide.md",
        "jackson_scenarios_master_guide.md",
        "java_spring_cryptography_scenarios_master_guide.md",
        "mongodb_scenarios_master_guide.md",
        "frontend_scenarios_master_guide.md",
        "rust_scenarios_master_guide.md",
        "golang_scenarios_master_guide.md",
        "rust_golang_scenarios_master_guide.md",
        "devops_iac_scenarios_master_guide.md",
        "java_threads_concurrency_200_scenarios_master_guide.md",
        "java_collections_streams_200_scenarios_master_guide.md",
        "completable_future_200_scenarios_master_guide.md",
        "java_io_nio_200_scenarios_master_guide.md",
        "message_queues_200_scenarios_master_guide.md",
        "security_infra_200_scenarios_master_guide.md",
        "opa_rego_200_scenarios_master_guide.md"
    )
    "databases-persistence" = @(
        "sql.md",
        "sql_normalization_acid_master_guide.md",
        "postgresql_master_guide.md",
        "mongodb_master_guide.md"
    )
    "messaging-distributed" = @(
        "message_queues_beginner_guide.md",
        "message_queues_master_guide.md",
        "kafka_internals_master_guide.md",
        "microservices_gateway_infrastructure_master_guide.md"
    )
    "cloud-infrastructure" = @(
        "aws_master_guide.md",
        "azure_master_guide.md",
        "google_cloud_master_guide.md",
        "kubernetes_master_guide.md",
        "kubernetes.md",
        "docker_master_guide.md",
        "linux.md",
        "vi_vim_nano_master_guide.md",
        "bash_batch_powershell_master_guide.md",
        "powershell_master_guide.md",
        "nginx_master_guide.md",
        "apache_httpd_lamp_master_guide.md",
        "apache_tomcat_master_guide.md",
        "envoy_proxy_master_guide.md",
        "istio_service_mesh_master_guide.md",
        "istio_envoy_nginx_apache_tomcat_lamp_master_guide.md"
    )
    "devops-cicd-iac" = @(
        "jenkins_master_guide.md",
        "argocd_master_guide.md",
        "git_master_guide.md",
        "github_master_guide.md",
        "github_actions_master_guide.md",
        "github_pages_master_guide.md",
        "vagrant_master_guide.md",
        "ansible_master_guide.md",
        "terraform_master_guide.md",
        "chef_master_guide.md",
        "devops_iac_technical_terms_master_guide.md"
    )
    "security-identity" = @(
        "cryptography_algorithms_master_guide.md",
        "security_auth_master_guide.md",
        "security_infra_tools_glossary_master_guide.md",
        "vault_secrets_master_guide.md"
    )
    "frontend-web" = @(
        "frontend_polyglot_technical_terms_master_guide.md",
        "react_master_guide.md",
        "angular_master_guide.md",
        "nextjs_rsc_master_guide.md",
        "graphql_polyglot_master_guide.md",
        "grpc_polyglot_master_guide.md",
        "tauri_rust_desktop_master_guide.md"
    )
    "systems-languages" = @(
        "rust_master_guide.md",
        "rust_technical_terms_master_guide.md",
        "golang_master_guide.md",
        "golang_technical_terms_master_guide.md",
        "rust_golang_technical_terms_master_guide.md",
        "python_master_guide.md",
        "c_cpp_master_guide.md"
    )
    "testing-qa" = @(
        "test_automation_master_guide.md",
        "cucumber.md",
        "cucmber.md",
        "selenium.md",
        "selinum.md"
    )
    "observability-sre" = @(
        "lgtm_master_guide.md",
        "opentelemetry_master_guide.md"
    )
    "documentation-engines" = @(
        "doc_generation_master_guide.md",
        "vitepress_master_guide.md",
        "mkdocs_material_master_guide.md",
        "hugo_master_guide.md",
        "starlight_astro_master_guide.md",
        "docusaurus_master_guide.md",
        "docsify_master_guide.md"
    )
    "communication-english" = @(
        "spoken_english_tamil_to_global_master_guide.md",
        "spoken_english_500_scenarios_master_guide.md",
        "english_root_words_master_guide.md",
        "english_phrases_master_guide.md",
        "english_idioms_master_guide.md",
        "english_phrases_idioms_root_words_master_guide.md",
        "ielts_toefl_advanced_vocabulary_master_guide.md",
        "ielts_500_words_master_guide.md",
        "business_english_corporate_words_master_guide.md",
        "it_tech_words_master_guide.md"
    )
    "ai-algorithms" = @(
        "ai_genai_master_guide.md",
        "rag_vector_search_master_guide.md",
        "system_design.md",
        "original_system_design.md",
        "dsa_master_guide.md",
        "leetcode_patterns.md",
        "regx.md"
    )
}

$targetFiles = @(
    "README.md",
    "LEARNING_PATH.md",
    "all_markdown_files_categorized.md"
)

foreach ($tf in $targetFiles) {
    if (Test-Path -Path $tf) {
        $content = [System.IO.File]::ReadAllText((Resolve-Path $tf), [System.Text.Encoding]::UTF8)
        foreach ($dir in $mapping.Keys) {
            foreach ($doc in $mapping[$dir]) {
                # Replace links like (doc) or (doc#anchor) with (dir/doc) or (dir/doc#anchor)
                # But ensure we do not double-prefix if already prefixed like (dir/doc)
                $escapedDoc = [regex]::Escape($doc)
                $pattern = "(?<=\()(?!(?:[a-zA-Z0-9_\-]+/|http|#|\.\./))$escapedDoc"
                $replacement = "$dir/$doc"
                $content = [regex]::Replace($content, $pattern, $replacement)
            }
        }
        [System.IO.File]::WriteAllText((Resolve-Path $tf), $content, [System.Text.Encoding]::UTF8)
        Write-Host "Updated links in $tf"
    }
}
Write-Host "All links updated successfully."
