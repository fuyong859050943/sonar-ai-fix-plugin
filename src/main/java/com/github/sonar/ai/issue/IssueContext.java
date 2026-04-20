/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.issue;

/**
 * 问题上下文模型
 * 
 * 封装 SonarQube 问题的完整上下文信息，
 * 包括代码片段、位置、规则等。
 */
public class IssueContext {

    private final String issueKey;
    private final String ruleKey;
    private final String ruleName;
    private final String severity;
    private final String message;
    private final String filePath;
    private final String language;
    private final int line;
    private final int lineStart;
    private final int lineEnd;
    private final String codeSnippet;
    private final String fullFileContent;
    private final String componentKey;

    /**
     * 构造函数
     */
    public IssueContext(Builder builder) {
        this.issueKey = builder.issueKey;
        this.ruleKey = builder.ruleKey;
        this.ruleName = builder.ruleName;
        this.severity = builder.severity;
        this.message = builder.message;
        this.filePath = builder.filePath;
        this.language = builder.language;
        this.line = builder.line;
        this.lineStart = builder.lineStart;
        this.lineEnd = builder.lineEnd;
        this.codeSnippet = builder.codeSnippet;
        this.fullFileContent = builder.fullFileContent;
        this.componentKey = builder.componentKey;
    }

    // Getters

    public String getIssueKey() {
        return issueKey;
    }

    public String getRuleKey() {
        return ruleKey;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getLanguage() {
        return language;
    }

    public int getLine() {
        return line;
    }

    public int getLineStart() {
        return lineStart;
    }

    public int getLineEnd() {
        return lineEnd;
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public String getFullFileContent() {
        return fullFileContent;
    }

    public String getComponentKey() {
        return componentKey;
    }

    /**
     * 获取编程语言（简化格式）
     */
    public String getLanguageSimple() {
        if (language == null) {
            return "unknown";
        }
        // 简化语言名称
        switch (language.toLowerCase()) {
            case "java":
                return "java";
            case "javascript":
            case "js":
                return "javascript";
            case "typescript":
            case "ts":
                return "typescript";
            case "python":
            case "py":
                return "python";
            case "go":
                return "go";
            case "kotlin":
                return "kotlin";
            case "csharp":
            case "c#":
                return "csharp";
            case "cpp":
            case "c++":
                return "cpp";
            case "php":
                return "php";
            case "ruby":
                return "ruby";
            case "rust":
                return "rust";
            case "swift":
                return "swift";
            default:
                return language.toLowerCase();
        }
    }

    @Override
    public String toString() {
        return "IssueContext{" +
            "issueKey='" + issueKey + '\'' +
            ", ruleKey='" + ruleKey + '\'' +
            ", filePath='" + filePath + '\'' +
            ", line=" + line +
            '}';
    }

    /**
     * Builder 模式
     */
    public static class Builder {
        private String issueKey;
        private String ruleKey;
        private String ruleName;
        private String severity;
        private String message;
        private String filePath;
        private String language;
        private int line;
        private int lineStart;
        private int lineEnd;
        private String codeSnippet;
        private String fullFileContent;
        private String componentKey;

        public Builder issueKey(String issueKey) {
            this.issueKey = issueKey;
            return this;
        }

        public Builder ruleKey(String ruleKey) {
            this.ruleKey = ruleKey;
            return this;
        }

        public Builder ruleName(String ruleName) {
            this.ruleName = ruleName;
            return this;
        }

        public Builder severity(String severity) {
            this.severity = severity;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder line(int line) {
            this.line = line;
            return this;
        }

        public Builder lineStart(int lineStart) {
            this.lineStart = lineStart;
            return this;
        }

        public Builder lineEnd(int lineEnd) {
            this.lineEnd = lineEnd;
            return this;
        }

        public Builder codeSnippet(String codeSnippet) {
            this.codeSnippet = codeSnippet;
            return this;
        }

        public Builder fullFileContent(String fullFileContent) {
            this.fullFileContent = fullFileContent;
            return this;
        }

        public Builder componentKey(String componentKey) {
            this.componentKey = componentKey;
            return this;
        }

        public IssueContext build() {
            return new IssueContext(this);
        }
    }
}
