/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.config.Configuration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AiFixConfiguration 单元测试
 */
class AiFixConfigurationTest {

    private Configuration configuration;
    private AiFixConfiguration aiConfig;

    @BeforeEach
    void setUp() {
        configuration = mock(Configuration.class);
        aiConfig = new AiFixConfiguration(configuration);
    }

    @Test
    void testIsEnabled() {
        when(configuration.getBoolean("sonar.ai.enabled")).thenReturn(Optional.of(true));

        assertTrue(aiConfig.isEnabled());
    }

    @Test
    void testIsEnabled_Default() {
        when(configuration.getBoolean("sonar.ai.enabled")).thenReturn(Optional.empty());

        assertTrue(aiConfig.isEnabled());  // 默认启用
    }

    @Test
    void testIsDisabled() {
        when(configuration.getBoolean("sonar.ai.enabled")).thenReturn(Optional.of(false));

        assertFalse(aiConfig.isEnabled());
    }

    @Test
    void testGetProvider() {
        when(configuration.get("sonar.ai.provider")).thenReturn(Optional.of("OPENAI"));

        assertEquals(LlmProvider.OPENAI, aiConfig.getProvider());
    }

    @Test
    void testGetProvider_Default() {
        when(configuration.get("sonar.ai.provider")).thenReturn(Optional.empty());

        assertEquals(LlmProvider.OPENAI, aiConfig.getProvider());  // 默认 OpenAI
    }

    @Test
    void testGetProvider_Azure() {
        when(configuration.get("sonar.ai.provider")).thenReturn(Optional.of("AZURE_OPENAI"));

        assertEquals(LlmProvider.AZURE_OPENAI, aiConfig.getProvider());
    }

    @Test
    void testGetProvider_Local() {
        when(configuration.get("sonar.ai.provider")).thenReturn(Optional.of("LOCAL"));

        assertEquals(LlmProvider.LOCAL, aiConfig.getProvider());
    }

    @Test
    void testGetModel() {
        when(configuration.get("sonar.ai.model")).thenReturn(Optional.of("gpt-4-turbo"));

        assertEquals("gpt-4-turbo", aiConfig.getModel());
    }

    @Test
    void testGetModel_Default() {
        when(configuration.get("sonar.ai.model")).thenReturn(Optional.empty());

        assertEquals("gpt-4", aiConfig.getModel());  // 默认 gpt-4
    }

    @Test
    void testGetMaxTokens() {
        when(configuration.getInt("sonar.ai.max-tokens")).thenReturn(Optional.of(4000));

        assertEquals(4000, aiConfig.getMaxTokens());
    }

    @Test
    void testGetMaxTokens_Default() {
        when(configuration.getInt("sonar.ai.max-tokens")).thenReturn(Optional.empty());

        assertEquals(2000, aiConfig.getMaxTokens());  // 默认 2000
    }

    @Test
    void testGetTemperature() {
        when(configuration.getDouble("sonar.ai.temperature")).thenReturn(Optional.of(0.5));

        assertEquals(0.5, aiConfig.getTemperature(), 0.001);
    }

    @Test
    void testGetTemperature_Default() {
        when(configuration.getDouble("sonar.ai.temperature")).thenReturn(Optional.empty());

        assertEquals(0.3, aiConfig.getTemperature(), 0.001);  // 默认 0.3
    }

    @Test
    void testGetCacheExpireMinutes() {
        when(configuration.getInt("sonar.ai.cache.expire-minutes")).thenReturn(Optional.of(120));

        assertEquals(120, aiConfig.getCacheExpireMinutes());
    }

    @Test
    void testGetCacheExpireMinutes_Default() {
        when(configuration.getInt("sonar.ai.cache.expire-minutes")).thenReturn(Optional.empty());

        assertEquals(60, aiConfig.getCacheExpireMinutes());  // 默认 60 分钟
    }

    @Test
    void testIsCacheEnabled() {
        when(configuration.getBoolean("sonar.ai.cache.enabled")).thenReturn(Optional.of(true));

        assertTrue(aiConfig.isCacheEnabled());
    }

    @Test
    void testIsCacheEnabled_Default() {
        when(configuration.getBoolean("sonar.ai.cache.enabled")).thenReturn(Optional.empty());

        assertTrue(aiConfig.isCacheEnabled());  // 默认启用缓存
    }

    @Test
    void testGetTimeout() {
        when(configuration.getInt("sonar.ai.timeout")).thenReturn(Optional.of(30));

        assertEquals(30, aiConfig.getTimeout());
    }

    @Test
    void testGetTimeout_Default() {
        when(configuration.getInt("sonar.ai.timeout")).thenReturn(Optional.empty());

        assertEquals(60, aiConfig.getTimeout());  // 默认 60 秒
    }

    @Test
    void testValidate_Valid() {
        when(configuration.getBoolean("sonar.ai.enabled")).thenReturn(Optional.of(true));
        when(configuration.get("sonar.ai.provider")).thenReturn(Optional.of("OPENAI"));
        when(configuration.get("sonar.ai.model")).thenReturn(Optional.of("gpt-4"));
        when(configuration.get("sonar.ai.openai.api-key")).thenReturn(Optional.of("sk-test"));

        assertTrue(aiConfig.validate().isValid());
    }

    @Test
    void testValidate_MissingApiKey() {
        when(configuration.getBoolean("sonar.ai.enabled")).thenReturn(Optional.of(true));
        when(configuration.get("sonar.ai.provider")).thenReturn(Optional.of("OPENAI"));
        when(configuration.get("sonar.ai.openai.api-key")).thenReturn(Optional.empty());

        var result = aiConfig.validate();
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(e -> e.contains("API key")));
    }
}
