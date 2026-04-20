/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.llm;

import com.github.sonar.ai.config.AiFixConfiguration;
import com.github.sonar.ai.config.LlmProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.config.Configuration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * LlmClientFactory 单元测试
 */
class LlmClientFactoryTest {

    private Configuration configuration;
    private AiFixConfiguration aiConfig;
    private LlmClientFactory factory;

    @BeforeEach
    void setUp() {
        configuration = mock(Configuration.class);
        aiConfig = mock(AiFixConfiguration.class);
        factory = new LlmClientFactory(configuration, aiConfig);
    }

    @Test
    void testCreateClient_OpenAI() {
        when(aiConfig.getProvider()).thenReturn(LlmProvider.OPENAI);
        when(configuration.get("sonar.ai.openai.api-key")).thenReturn(Optional.of("sk-test-key"));
        when(configuration.get("sonar.ai.openai.base-url")).thenReturn(Optional.empty());
        when(configuration.get("sonar.ai.base-url")).thenReturn(Optional.empty());
        when(aiConfig.getModel()).thenReturn("gpt-4");
        when(aiConfig.getMaxTokens()).thenReturn(2000);
        when(aiConfig.getTemperature()).thenReturn(0.3);

        LlmClient client = factory.createClient(LlmProvider.OPENAI);

        assertNotNull(client);
        assertTrue(client instanceof OpenAiClient);
    }

    @Test
    void testCreateClient_AzureOpenAI() {
        when(aiConfig.getProvider()).thenReturn(LlmProvider.AZURE_OPENAI);
        when(configuration.get("sonar.ai.azure.api-key")).thenReturn(Optional.of("azure-key"));
        when(configuration.get("sonar.ai.azure.endpoint")).thenReturn(Optional.of("https://test.openai.azure.com"));
        when(configuration.get("sonar.ai.azure.deployment")).thenReturn(Optional.of("gpt-4-deployment"));
        when(aiConfig.getModel()).thenReturn("gpt-4");
        when(aiConfig.getMaxTokens()).thenReturn(2000);

        LlmClient client = factory.createClient(LlmProvider.AZURE_OPENAI);

        assertNotNull(client);
        assertTrue(client instanceof AzureOpenAiClient);
    }

    @Test
    void testCreateClient_Local() {
        when(aiConfig.getProvider()).thenReturn(LlmProvider.LOCAL);
        when(configuration.get("sonar.ai.local.endpoint")).thenReturn(Optional.of("http://localhost:11434"));
        when(aiConfig.getModel()).thenReturn("llama2");
        when(aiConfig.getMaxTokens()).thenReturn(2000);

        LlmClient client = factory.createClient(LlmProvider.LOCAL);

        assertNotNull(client);
        assertTrue(client instanceof LocalLlmClient);
    }

    @Test
    void testGetClient_Cached() {
        when(aiConfig.getProvider()).thenReturn(LlmProvider.OPENAI);
        when(configuration.get("sonar.ai.openai.api-key")).thenReturn(Optional.of("sk-test-key"));
        when(aiConfig.getModel()).thenReturn("gpt-4");
        when(aiConfig.getMaxTokens()).thenReturn(2000);
        when(aiConfig.getTemperature()).thenReturn(0.3);

        LlmClient client1 = factory.getClient();
        LlmClient client2 = factory.getClient();

        assertSame(client1, client2);  // 应该返回同一个缓存实例
    }

    @Test
    void testGetDefaultClient() {
        when(aiConfig.getProvider()).thenReturn(LlmProvider.OPENAI);
        when(configuration.get("sonar.ai.openai.api-key")).thenReturn(Optional.of("sk-test-key"));
        when(aiConfig.getModel()).thenReturn("gpt-4");
        when(aiConfig.getMaxTokens()).thenReturn(2000);
        when(aiConfig.getTemperature()).thenReturn(0.3);

        LlmClient client = factory.getDefaultClient();

        assertNotNull(client);
    }

    @Test
    void testIsClientAvailable_WithApiKey() {
        when(aiConfig.getProvider()).thenReturn(LlmProvider.OPENAI);
        when(configuration.get("sonar.ai.openai.api-key")).thenReturn(Optional.of("sk-test-key"));
        when(aiConfig.getModel()).thenReturn("gpt-4");
        when(aiConfig.getMaxTokens()).thenReturn(2000);
        when(aiConfig.getTemperature()).thenReturn(0.3);

        LlmClient client = factory.getClient();

        assertTrue(client.isAvailable());
    }

    @Test
    void testIsClientAvailable_WithoutApiKey() {
        when(aiConfig.getProvider()).thenReturn(LlmProvider.OPENAI);
        when(configuration.get("sonar.ai.openai.api-key")).thenReturn(Optional.empty());
        when(aiConfig.getModel()).thenReturn("gpt-4");
        when(aiConfig.getMaxTokens()).thenReturn(2000);
        when(aiConfig.getTemperature()).thenReturn(0.3);

        LlmClient client = factory.getClient();

        assertFalse(client.isAvailable());
    }

    @Test
    void testCreateClient_NullProvider() {
        assertThrows(IllegalArgumentException.class, () -> {
            factory.createClient(null);
        });
    }

    @Test
    void testResetClient() {
        when(aiConfig.getProvider()).thenReturn(LlmProvider.OPENAI);
        when(configuration.get("sonar.ai.openai.api-key")).thenReturn(Optional.of("sk-test-key"));
        when(aiConfig.getModel()).thenReturn("gpt-4");
        when(aiConfig.getMaxTokens()).thenReturn(2000);
        when(aiConfig.getTemperature()).thenReturn(0.3);

        LlmClient client1 = factory.getClient();
        factory.resetClient();
        LlmClient client2 = factory.getClient();

        assertNotSame(client1, client2);  // 重置后应该是新实例
    }
}
