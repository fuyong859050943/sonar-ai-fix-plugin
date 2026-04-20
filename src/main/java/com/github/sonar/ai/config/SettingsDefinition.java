/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.config;

import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import java.util.Arrays;
import java.util.List;

/**
 * SonarQube AI Fix 插件配置定义
 * 
 * 定义插件在 SonarQube 管理界面中的配置项。
 */
public class SettingsDefinition {

    // ============ 配置键常量 ============

    /** 是否启用 AI Fix 功能 */
    public static final String AI_FIX_ENABLED = "sonar.ai-fix.enabled";
    
    /** LLM 提供者类型 */
    public static final String LLM_PROVIDER = "sonar.ai-fix.llm.provider";
    
    /** OpenAI API Key */
    public static final String OPENAI_API_KEY = "sonar.ai-fix.openai.api-key";
    
    /** OpenAI 模型 */
    public static final String OPENAI_MODEL = "sonar.ai-fix.openai.model";
    
    /** OpenAI Base URL */
    public static final String OPENAI_BASE_URL = "sonar.ai-fix.openai.base-url";
    
    /** Azure OpenAI API Key */
    public static final String AZURE_API_KEY = "sonar.ai-fix.azure.api-key";
    
    /** Azure OpenAI Endpoint */
    public static final String AZURE_ENDPOINT = "sonar.ai-fix.azure.endpoint";
    
    /** Azure OpenAI Deployment */
    public static final String AZURE_DEPLOYMENT = "sonar.ai-fix.azure.deployment";
    
    /** 本地 LLM Base URL */
    public static final String LOCAL_LLM_BASE_URL = "sonar.ai-fix.local.base-url";
    
    /** 本地 LLM 模型 */
    public static final String LOCAL_LLM_MODEL = "sonar.ai-fix.local.model";
    
    /** 请求超时（秒） */
    public static final String REQUEST_TIMEOUT = "sonar.ai-fix.request-timeout";
    
    /** 每分钟最大请求数 */
    public static final String RATE_LIMIT_REQUESTS = "sonar.ai-fix.rate-limit.requests-per-minute";
    
    /** 每天最大 Token 数 */
    public static final String RATE_LIMIT_TOKENS = "sonar.ai-fix.rate-limit.tokens-per-day";

    /**
     * 获取所有配置定义
     * 
     * @return 配置定义列表
     */
    public static List<PropertyDefinition> getProperties() {
        return Arrays.asList(
            // ============ 通用配置 ============
            PropertyDefinition.builder(AI_FIX_ENABLED)
                .name("Enable AI Fix")
                .description("Enable or disable AI code fix suggestions")
                .defaultValue("true")
                .category("AI Fix")
                .onQualifiers(Qualifiers.PROJECT, Qualifiers.APP)
                .build(),

            PropertyDefinition.builder(LLM_PROVIDER)
                .name("LLM Provider")
                .description("Select the LLM provider: openai, azure, local")
                .defaultValue("openai")
                .category("AI Fix")
                .onQualifiers(Qualifiers.PROJECT, Qualifiers.APP)
                .build(),

            // ============ OpenAI 配置 ============
            PropertyDefinition.builder(OPENAI_API_KEY)
                .name("OpenAI API Key")
                .description("OpenAI API key for GPT models")
                .category("AI Fix")
                .type(org.sonar.api.config.PropertyDefinition.Type.PASSWORD)
                .onQualifiers(Qualifiers.PROJECT, Qualifiers.APP)
                .build(),

            PropertyDefinition.builder(OPENAI_MODEL)
                .name("OpenAI Model")
                .description("OpenAI model to use (e.g., gpt-4, gpt-4o, gpt-3.5-turbo)")
                .defaultValue("gpt-4")
                .category("AI Fix")
                .onQualifiers(Qualifiers.PROJECT, Qualifiers.APP)
                .build(),

            PropertyDefinition.builder(OPENAI_BASE_URL)
                .name("OpenAI Base URL")
                .description("Custom OpenAI API base URL (optional, for proxy or custom endpoint)")
                .defaultValue("https://api.openai.com/v1")
                .category("AI Fix")
                .onQualifiers(Qualifiers.PROJECT, Qualifiers.APP)
                .build(),

            // ============ Azure OpenAI 配置 ============
            PropertyDefinition.builder(AZURE_API_KEY)
                .name("Azure OpenAI API Key")
                .description("Azure OpenAI API key")
                .category("AI Fix")
                .type(org.sonar.api.config.PropertyDefinition.Type.PASSWORD)
                .onQualifiers(Qualifiers.PROJECT, Qualifiers.APP)
                .build(),

            PropertyDefinition.builder(AZURE_ENDPOINT)
                .name("Azure OpenAI Endpoint")
                .description("Azure OpenAI endpoint URL")
                .category("AI Fix")
                .onQualifiers(Qualifiers.PROJECT, Qualifiers.APP)
                .build(),

            PropertyDefinition.builder(AZURE_DEPLOYMENT)
                .name("Azure OpenAI Deployment")
                .description("Azure OpenAI deployment name")
                .category("AI Fix")
                .onQualifiers(Qualifiers.PROJECT, Qualifiers.APP)
                .build(),

            // ============ 本地 LLM 配置 ============
            PropertyDefinition.builder(LOCAL_LLM_BASE_URL)
                .name("Local LLM Base URL")
                .description("Local LLM API base URL (e.g., http://localhost:11434/v1)")
                .defaultValue("http://localhost:11434/v1")
                .category("AI Fix")
                .onQualifiers(Qualifiers.PROJECT, Qualifiers.APP)
                .build(),

            PropertyDefinition.builder(LOCAL_LLM_MODEL)
                .name("Local LLM Model")
                .description("Local LLM model name (e.g., deepseek-coder, codellama)")
                .defaultValue("deepseek-coder")
                .category("AI Fix")
                .onQualifiers(Qualifiers.PROJECT, Qualifiers.APP)
                .build(),

            // ============ 高级配置 ============
            PropertyDefinition.builder(REQUEST_TIMEOUT)
                .name("Request Timeout (seconds)")
                .description("LLM API request timeout in seconds")
                .defaultValue("30")
                .category("AI Fix")
                .type(org.sonar.api.config.PropertyDefinition.Type.INTEGER)
                .onQualifiers(Qualifiers.PROJECT, Qualifiers.APP)
                .build(),

            PropertyDefinition.builder(RATE_LIMIT_REQUESTS)
                .name("Rate Limit: Requests per Minute")
                .description("Maximum LLM API requests per minute")
                .defaultValue("30")
                .category("AI Fix")
                .type(org.sonar.api.config.PropertyDefinition.Type.INTEGER)
                .onQualifiers(Qualifiers.PROJECT, Qualifiers.APP)
                .build(),

            PropertyDefinition.builder(RATE_LIMIT_TOKENS)
                .name("Rate Limit: Tokens per Day")
                .description("Maximum LLM tokens per day")
                .defaultValue("100000")
                .category("AI Fix")
                .type(org.sonar.api.config.PropertyDefinition.Type.INTEGER)
                .onQualifiers(Qualifiers.PROJECT, Qualifiers.APP)
                .build()
        );
    }
}
