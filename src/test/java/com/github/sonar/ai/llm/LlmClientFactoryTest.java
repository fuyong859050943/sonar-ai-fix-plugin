/*
 * SonarQube AI Fix Plugin - Tests
 * Copyright (C) 2024
 */
package com.github.sonar.ai.llm;

import com.github.sonar.ai.config.AiFixConfiguration;
import com.github.sonar.ai.config.LlmProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LLM 客户端工厂测试
 */
class LlmClientFactoryTest {

    private MapSettings settings;

    @BeforeEach
    void setUp() {
        settings = new MapSettings();
    }

    @Test
    @DisplayName("Should create OpenAI client when provider is OPENAI")
    void shouldCreateOpenAIClient() {
        // Given
        settings.setProperty("sonar.ai.enabled", true);
        settings.setProperty("sonar.ai.provider", "OPENAI");
        settings.setProperty("sonar.ai.openai.api-key", "test-api-key");
        
        Configuration config = settings.asConfig();
        AiFixConfiguration aiConfig = new AiFixConfiguration(config);

        // When
        LlmClient client = LlmClientFactory.createClient(config, aiConfig);

        // Then
        assertNotNull(client);
        assertTrue(client instanceof OpenAiClient);
    }

    @Test
    @DisplayName("Should create Azure OpenAI client when provider is AZURE")
    void shouldCreateAzureClient() {
        // Given
        settings.setProperty("sonar.ai.enabled", true);
        settings.setProperty("sonar.ai.provider", "AZURE");
        settings.setProperty("sonar.ai.azure.api-key", "test-api-key");
        settings.setProperty("sonar.ai.azure.endpoint", "https://test.openai.azure.com");
        
        Configuration config = settings.asConfig();
        AiFixConfiguration aiConfig = new AiFixConfiguration(config);

        // When
        LlmClient client = LlmClientFactory.createClient(config, aiConfig);

        // Then
        assertNotNull(client);
        assertTrue(client instanceof AzureOpenAiClient);
    }

    @Test
    @DisplayName("Should create local LLM client when provider is LOCAL")
    void shouldCreateLocalClient() {
        // Given
        settings.setProperty("sonar.ai.enabled", true);
        settings.setProperty("sonar.ai.provider", "LOCAL");
        settings.setProperty("sonar.ai.local.base-url", "http://localhost:11434");
        
        Configuration config = settings.asConfig();
        AiFixConfiguration aiConfig = new AiFixConfiguration(config);

        // When
        LlmClient client = LlmClientFactory.createClient(config, aiConfig);

        // Then
        assertNotNull(client);
        assertTrue(client instanceof LocalLlmClient);
    }

    @Test
    @DisplayName("Should default to OpenAI when provider not specified")
    void shouldDefaultToOpenAI() {
        // Given
        settings.setProperty("sonar.ai.enabled", true);
        // No provider set, should default to OPENAI
        
        Configuration config = settings.asConfig();
        AiFixConfiguration aiConfig = new AiFixConfiguration(config);

        // When
        LlmClient client = LlmClientFactory.createClient(config, aiConfig);

        // Then
        assertNotNull(client);
        assertTrue(client instanceof OpenAiClient);
    }

    @Test
    @DisplayName("Should throw exception for unknown provider")
    void shouldThrowForUnknownProvider() {
        // Given
        settings.setProperty("sonar.ai.enabled", true);
        settings.setProperty("sonar.ai.provider", "UNKNOWN_PROVIDER");
        
        Configuration config = settings.asConfig();
        AiFixConfiguration aiConfig = new AiFixConfiguration(config);

        // When/Then
        assertThrows(LlmException.class, () -> {
            LlmClientFactory.createClient(config, aiConfig);
        });
    }

    @Test
    @DisplayName("Should return unavailable client when disabled")
    void shouldReturnUnavailableClientWhenDisabled() {
        // Given
        settings.setProperty("sonar.ai.enabled", false);
        
        Configuration config = settings.asConfig();
        AiFixConfiguration aiConfig = new AiFixConfiguration(config);

        // When
        LlmClient client = LlmClientFactory.createClient(config, aiConfig);

        // Then
        assertNotNull(client);
        assertFalse(client.isAvailable());
    }
}
