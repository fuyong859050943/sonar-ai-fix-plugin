/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.config;

/**
 * LLM 提供者枚举
 * 
 * 定义支持的 LLM 后端类型。
 */
public enum LlmProvider {

    /** OpenAI (GPT-4, GPT-4o, GPT-3.5-turbo) */
    OPENAI("openai"),

    /** Azure OpenAI */
    AZURE("azure"),

    /** 本地模型 (Ollama, LM Studio, vLLM 等) */
    LOCAL("local");

    private final String value;

    LlmProvider(String value) {
        this.value = value;
    }

    /**
     * 获取配置值
     * 
     * @return 配置字符串
     */
    public String getValue() {
        return value;
    }

    /**
     * 从字符串解析 LLM 提供者
     * 
     * @param value 配置字符串
     * @return LLM 提供者
     * @throws IllegalArgumentException 如果值无效
     */
    public static LlmProvider fromString(String value) {
        if (value == null) {
            return OPENAI; // 默认使用 OpenAI
        }

        for (LlmProvider provider : values()) {
            if (provider.value.equalsIgnoreCase(value)) {
                return provider;
            }
        }

        throw new IllegalArgumentException("Unknown LLM provider: " + value);
    }
}
