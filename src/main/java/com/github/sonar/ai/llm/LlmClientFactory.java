/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.llm;

import com.github.sonar.ai.config.AiFixConfiguration;
import com.github.sonar.ai.config.LlmProvider;
import org.sonar.api.server.ServerSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LLM 客户端工厂
 * 
 * 根据配置动态创建对应的 LLM 客户端实例。
 */
@ServerSide
public class LlmClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(LlmClientFactory.class);

    private final AiFixConfiguration configuration;

    /**
     * 构造函数
     * 
     * @param configuration AI Fix 配置
     */
    public LlmClientFactory(AiFixConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * 创建 LLM 客户端
     * 
     * @return LLM 客户端实例
     * @throws IllegalArgumentException 如果配置无效
     */
    public LlmClient createClient() {
        if (!configuration.isEnabled()) {
            throw new IllegalStateException("AI Fix is disabled");
        }

        LlmProvider provider = configuration.getLlmProvider();
        LOG.info("Creating LLM client for provider: {}", provider);

        switch (provider) {
            case OPENAI:
                return createOpenAiClient();
            case AZURE:
                return createAzureClient();
            case LOCAL:
                return createLocalClient();
            default:
                throw new IllegalArgumentException("Unknown LLM provider: " + provider);
        }
    }

    /**
     * 创建 OpenAI 客户端
     */
    private LlmClient createOpenAiClient() {
        if (!configuration.getOpenAiApiKey().isPresent()) {
            throw new IllegalArgumentException("OpenAI API Key is required");
        }

        LOG.info("Creating OpenAI client with model: {}", configuration.getOpenAiModel());
        return new OpenAiClient(configuration);
    }

    /**
     * 创建 Azure OpenAI 客户端
     */
    private LlmClient createAzureClient() {
        if (!configuration.getAzureApiKey().isPresent()) {
            throw new IllegalArgumentException("Azure OpenAI API Key is required");
        }
        if (!configuration.getAzureEndpoint().isPresent()) {
            throw new IllegalArgumentException("Azure OpenAI Endpoint is required");
        }
        if (!configuration.getAzureDeployment().isPresent()) {
            throw new IllegalArgumentException("Azure OpenAI Deployment is required");
        }

        LOG.info("Creating Azure OpenAI client for deployment: {}", configuration.getAzureDeployment().get());
        return new AzureOpenAiClient(configuration);
    }

    /**
     * 创建本地 LLM 客户端
     */
    private LlmClient createLocalClient() {
        LOG.info("Creating Local LLM client: {} at {}", 
            configuration.getLocalLlmModel(), 
            configuration.getLocalLlmBaseUrl());
        return new LocalLlmClient(configuration);
    }

    /**
     * 验证配置是否有效
     * 
     * @return 如果配置有效返回 null，否则返回错误消息
     */
    public String validateConfiguration() {
        if (!configuration.isEnabled()) {
            return "AI Fix is disabled";
        }

        LlmProvider provider = configuration.getLlmProvider();

        switch (provider) {
            case OPENAI:
                if (!configuration.getOpenAiApiKey().isPresent()) {
                    return "OpenAI API Key is required for OpenAI provider";
                }
                break;

            case AZURE:
                if (!configuration.getAzureApiKey().isPresent()) {
                    return "Azure OpenAI API Key is required for Azure provider";
                }
                if (!configuration.getAzureEndpoint().isPresent()) {
                    return "Azure OpenAI Endpoint is required for Azure provider";
                }
                if (!configuration.getAzureDeployment().isPresent()) {
                    return "Azure OpenAI Deployment is required for Azure provider";
                }
                break;

            case LOCAL:
                // 本地 LLM 不需要额外验证
                break;
        }

        return null; // 配置有效
    }

    /**
     * 检查 LLM 服务是否可用
     * 
     * @return true 如果服务可用
     */
    public boolean isServiceAvailable() {
        try {
            LlmClient client = createClient();
            return client.isAvailable();
        } catch (Exception e) {
            LOG.warn("LLM service is not available: {}", e.getMessage());
            return false;
        }
    }
}
