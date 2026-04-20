/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.config;

import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;
import java.util.Optional;

/**
 * AI Fix 插件配置管理类
 * 
 * 封装配置读取逻辑，提供类型安全的配置访问方法。
 */
@ServerSide
@ScannerSide
public class AiFixConfiguration {

    private final Configuration configuration;

    /**
     * 构造函数
     * 
     * @param configuration SonarQube 配置对象
     */
    public AiFixConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    // ============ 通用配置 ============

    /**
     * 检查 AI Fix 功能是否启用
     * 
     * @return true 如果启用
     */
    public boolean isEnabled() {
        return configuration.getBoolean(SettingsDefinition.AI_FIX_ENABLED).orElse(true);
    }

    /**
     * 获取 LLM 提供者类型
     * 
     * @return LLM 提供者
     */
    public LlmProvider getLlmProvider() {
        String provider = configuration.get(SettingsDefinition.LLM_PROVIDER).orElse("openai");
        return LlmProvider.fromString(provider);
    }

    // ============ OpenAI 配置 ============

    /**
     * 获取 OpenAI API Key
     * 
     * @return API Key
     */
    public Optional<String> getOpenAiApiKey() {
        return configuration.get(SettingsDefinition.OPENAI_API_KEY);
    }

    /**
     * 获取 OpenAI 模型名称
     * 
     * @return 模型名称
     */
    public String getOpenAiModel() {
        return configuration.get(SettingsDefinition.OPENAI_MODEL).orElse("gpt-4");
    }

    /**
     * 获取 OpenAI API Base URL
     * 
     * @return Base URL
     */
    public String getOpenAiBaseUrl() {
        return configuration.get(SettingsDefinition.OPENAI_BASE_URL)
            .orElse("https://api.openai.com/v1");
    }

    // ============ Azure OpenAI 配置 ============

    /**
     * 获取 Azure OpenAI API Key
     * 
     * @return API Key
     */
    public Optional<String> getAzureApiKey() {
        return configuration.get(SettingsDefinition.AZURE_API_KEY);
    }

    /**
     * 获取 Azure OpenAI Endpoint
     * 
     * @return Endpoint URL
     */
    public Optional<String> getAzureEndpoint() {
        return configuration.get(SettingsDefinition.AZURE_ENDPOINT);
    }

    /**
     * 获取 Azure OpenAI Deployment 名称
     * 
     * @return Deployment 名称
     */
    public Optional<String> getAzureDeployment() {
        return configuration.get(SettingsDefinition.AZURE_DEPLOYMENT);
    }

    // ============ 本地 LLM 配置 ============

    /**
     * 获取本地 LLM Base URL
     * 
     * @return Base URL
     */
    public String getLocalLlmBaseUrl() {
        return configuration.get(SettingsDefinition.LOCAL_LLM_BASE_URL)
            .orElse("http://localhost:11434/v1");
    }

    /**
     * 获取本地 LLM 模型名称
     * 
     * @return 模型名称
     */
    public String getLocalLlmModel() {
        return configuration.get(SettingsDefinition.LOCAL_LLM_MODEL)
            .orElse("deepseek-coder");
    }

    // ============ 高级配置 ============

    /**
     * 获取请求超时时间（秒）
     * 
     * @return 超时时间
     */
    public int getRequestTimeout() {
        return configuration.getInt(SettingsDefinition.REQUEST_TIMEOUT).orElse(30);
    }

    /**
     * 获取每分钟最大请求数
     * 
     * @return 最大请求数
     */
    public int getRateLimitRequestsPerMinute() {
        return configuration.getInt(SettingsDefinition.RATE_LIMIT_REQUESTS).orElse(30);
    }

    /**
     * 获取每天最大 Token 数
     * 
     * @return 最大 Token 数
     */
    public int getRateLimitTokensPerDay() {
        return configuration.getInt(SettingsDefinition.RATE_LIMIT_TOKENS).orElse(100000);
    }

    // ============ 验证方法 ============

    /**
     * 验证配置是否有效
     * 
     * @return true 如果配置有效
     */
    public boolean isValid() {
        if (!isEnabled()) {
            return true; // 未启用时跳过验证
        }

        LlmProvider provider = getLlmProvider();
        switch (provider) {
            case OPENAI:
                return getOpenAiApiKey().isPresent();
            case AZURE:
                return getAzureApiKey().isPresent() 
                    && getAzureEndpoint().isPresent()
                    && getAzureDeployment().isPresent();
            case LOCAL:
                return true; // 本地模型不需要验证
            default:
                return false;
        }
    }

    /**
     * 获取配置错误信息
     * 
     * @return 错误信息，配置有效时返回空
     */
    public Optional<String> getConfigurationError() {
        if (!isEnabled()) {
            return Optional.empty();
        }

        LlmProvider provider = getLlmProvider();
        switch (provider) {
            case OPENAI:
                if (!getOpenAiApiKey().isPresent()) {
                    return Optional.of("OpenAI API Key is required when using OpenAI provider");
                }
                break;
            case AZURE:
                if (!getAzureApiKey().isPresent()) {
                    return Optional.of("Azure OpenAI API Key is required when using Azure provider");
                }
                if (!getAzureEndpoint().isPresent()) {
                    return Optional.of("Azure OpenAI Endpoint is required when using Azure provider");
                }
                if (!getAzureDeployment().isPresent()) {
                    return Optional.of("Azure OpenAI Deployment is required when using Azure provider");
                }
                break;
            case LOCAL:
                // 本地模型配置可选
                break;
        }

        return Optional.empty();
    }
}
