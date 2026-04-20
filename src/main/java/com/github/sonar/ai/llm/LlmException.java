/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.llm;

/**
 * LLM 调用异常
 * 
 * 当 LLM API 调用失败时抛出。
 */
public class LlmException extends RuntimeException {

    /**
     * 构造函数
     * 
     * @param message 错误消息
     */
    public LlmException(String message) {
        super(message);
    }

    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param cause 原因
     */
    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }
}
