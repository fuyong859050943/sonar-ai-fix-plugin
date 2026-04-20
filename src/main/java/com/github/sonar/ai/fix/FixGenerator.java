/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.fix;

import com.github.sonar.ai.config.AiFixConfiguration;
import com.github.sonar.ai.llm.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 修复建议生成器
 * 
 * 协调问题收集、代码获取和 LLM 调用，
 * 生成完整的修复建议。
 */
public class FixGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(FixGenerator.class);

    private final LlmClient llmClient;
    private final AiFixConfiguration configuration;

    /**
     * 构造函数
     * 
     * @param llmClient LLM 客户端
     * @param configuration 配置
     */
    public FixGenerator(LlmClient llmClient, AiFixConfiguration configuration) {
        this.llmClient = llmClient;
        this.configuration = configuration;
    }

    /**
     * 为指定问题生成修复建议
     * 
     * @param issueKey SonarQube 问题 Key
     * @return 修复建议
     */
    public FixSuggestion generate(String issueKey) {
        LOG.info("Generating fix for issue: {}", issueKey);

        // TODO: 从 SonarQube 获取问题详情
        // TODO: 获取源代码上下文
        // TODO: 检测编程语言

        // 临时实现：直接调用 LLM
        return llmClient.generateFix(
            "// Sample code snippet",
            "java:S1068",
            "Unused private field should be removed",
            "java",
            "src/main/java/Example.java"
        );
    }

    /**
     * 批量生成修复建议
     * 
     * @param issueKeys 问题 Key 列表
     * @return 修复建议列表
     */
    public List<FixSuggestion> generateBatch(List<String> issueKeys) {
        LOG.info("Generating fixes for {} issues", issueKeys.size());

        List<FixSuggestion> suggestions = new ArrayList<>();
        for (String issueKey : issueKeys) {
            try {
                suggestions.add(generate(issueKey));
            } catch (Exception e) {
                LOG.error("Failed to generate fix for issue: {}", issueKey, e);
            }
        }

        return suggestions;
    }
}
