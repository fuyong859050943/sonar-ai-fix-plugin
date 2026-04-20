/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.llm;

import com.github.sonar.ai.fix.FixSuggestion;

/**
 * LLM 客户端接口
 * 
 * 定义与 LLM 后端交互的标准接口，
 * 支持多种 LLM 提供者实现。
 */
public interface LlmClient {

    /**
     * 生成代码修复建议
     * 
     * @param codeSnippet 问题代码片段
     * @param issueType 问题类型（规则 ID）
     * @param issueMessage 问题描述
     * @param language 编程语言
     * @param filePath 文件路径（用于上下文）
     * @return 修复建议
     * @throws LlmException 如果调用 LLM 失败
     */
    FixSuggestion generateFix(
        String codeSnippet,
        String issueType,
        String issueMessage,
        String language,
        String filePath
    );

    /**
     * 检查 LLM 服务是否可用
     * 
     * @return true 如果服务可用
     */
    boolean isAvailable();

    /**
     * 获取 LLM 提供者名称
     * 
     * @return 提供者名称
     */
    String getProviderName();

    /**
     * 获取当前使用的模型名称
     * 
     * @return 模型名称
     */
    String getModelName();
}
