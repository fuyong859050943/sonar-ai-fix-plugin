/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.fix;

import java.util.ArrayList;
import java.util.List;

/**
 * 修复建议模型
 * 
 * 表示 AI 生成的代码修复建议。
 */
public class FixSuggestion {

    private final String issueType;
    private final String issueMessage;
    private final String explanation;
    private final String rootCause;
    private final String fixedCode;
    private final List<String> changes;
    private final List<String> bestPractices;
    private final String confidence;
    private final String providerName;
    private final String modelName;

    /**
     * 构造函数
     */
    public FixSuggestion(
        String issueType,
        String issueMessage,
        String explanation,
        String rootCause,
        String fixedCode,
        List<String> changes,
        List<String> bestPractices,
        String confidence,
        String providerName,
        String modelName
    ) {
        this.issueType = issueType;
        this.issueMessage = issueMessage;
        this.explanation = explanation;
        this.rootCause = rootCause;
        this.fixedCode = fixedCode;
        this.changes = changes != null ? changes : new ArrayList<>();
        this.bestPractices = bestPractices != null ? bestPractices : new ArrayList<>();
        this.confidence = confidence;
        this.providerName = providerName;
        this.modelName = modelName;
    }

    // Getters

    public String getIssueType() {
        return issueType;
    }

    public String getIssueMessage() {
        return issueMessage;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getRootCause() {
        return rootCause;
    }

    public String getFixedCode() {
        return fixedCode;
    }

    public List<String> getChanges() {
        return changes;
    }

    public List<String> getBestPractices() {
        return bestPractices;
    }

    public String getConfidence() {
        return confidence;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getModelName() {
        return modelName;
    }

    @Override
    public String toString() {
        return "FixSuggestion{" +
            "issueType='" + issueType + '\'' +
            ", confidence='" + confidence + '\'' +
            ", providerName='" + providerName + '\'' +
            '}';
    }
}
