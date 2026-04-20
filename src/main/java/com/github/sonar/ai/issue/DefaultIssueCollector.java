/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.issue;

import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.Components;

import java.util.Optional;

/**
 * 默认问题收集器实现
 * 
 * 使用 SonarQube Web Service API 获取问题详情。
 * 
 * 注意：此类依赖 SonarQube Server API，需要在 SonarQube 环境中运行。
 */
@ServerSide
public class DefaultIssueCollector implements IssueCollector {

    private static final Logger LOG = Loggers.get(DefaultIssueCollector.class);

    // 默认上下文行数（问题前后各取多少行）
    private static final int CONTEXT_LINES = 10;

    @Override
    public Optional<IssueContext> getIssueContext(String issueKey) {
        LOG.debug("Fetching issue context for: {}", issueKey);

        try {
            // TODO: 实现通过 SonarQube API 获取问题详情
            // 这里需要使用 SonarQube 内部的 DbClient 或 Web Service API
            
            // 临时返回模拟数据，实际实现需要：
            // 1. 调用 issues/show API 或使用内部 DbClient
            // 2. 获取问题的详细信息（规则、消息、位置等）
            // 3. 获取源代码内容
            // 4. 构建 IssueContext 对象

            LOG.warn("IssueCollector.getIssueContext() not fully implemented yet");
            return Optional.empty();

        } catch (Exception e) {
            LOG.error("Failed to fetch issue context: " + issueKey, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getSourceCodeSnippet(String componentKey, int lineStart, int lineEnd) {
        LOG.debug("Fetching source code snippet: {} [{}-{}]", componentKey, lineStart, lineEnd);

        try {
            // TODO: 实现源代码获取
            // 可以使用 SonarQube 的源代码 API：
            // GET /api/sources/lines?key=<componentKey>&from=<lineStart>&to=<lineEnd>
            
            LOG.warn("IssueCollector.getSourceCodeSnippet() not fully implemented yet");
            return Optional.empty();

        } catch (Exception e) {
            LOG.error("Failed to fetch source code: " + componentKey, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getFullFileContent(String componentKey) {
        LOG.debug("Fetching full file content: {}", componentKey);

        try {
            // TODO: 实现完整文件内容获取
            // GET /api/sources/raw?key=<componentKey>
            
            LOG.warn("IssueCollector.getFullFileContent() not fully implemented yet");
            return Optional.empty();

        } catch (Exception e) {
            LOG.error("Failed to fetch file content: " + componentKey, e);
            return Optional.empty();
        }
    }

    @Override
    public String detectLanguage(String componentKey) {
        // 从 componentKey 推断语言
        // 例如：my-project:src/main/java/com/example/Foo.java -> java
        
        if (componentKey == null) {
            return null;
        }

        // 简单的文件扩展名检测
        if (componentKey.endsWith(".java")) {
            return "java";
        } else if (componentKey.endsWith(".js")) {
            return "javascript";
        } else if (componentKey.endsWith(".ts")) {
            return "typescript";
        } else if (componentKey.endsWith(".py")) {
            return "python";
        } else if (componentKey.endsWith(".go")) {
            return "go";
        } else if (componentKey.endsWith(".kt") || componentKey.endsWith(".kts")) {
            return "kotlin";
        } else if (componentKey.endsWith(".cs")) {
            return "csharp";
        } else if (componentKey.endsWith(".php")) {
            return "php";
        } else if (componentKey.endsWith(".rb")) {
            return "ruby";
        } else if (componentKey.endsWith(".rs")) {
            return "rust";
        } else if (componentKey.endsWith(".swift")) {
            return "swift";
        }

        return null;
    }

    /**
     * 计算代码片段的行范围（包含上下文）
     * 
     * @param issueLine 问题所在行
     * @param totalLines 文件总行数
     * @return [startLine, endLine]
     */
    protected int[] calculateContextRange(int issueLine, int totalLines) {
        int start = Math.max(1, issueLine - CONTEXT_LINES);
        int end = Math.min(totalLines, issueLine + CONTEXT_LINES);
        return new int[]{start, end};
    }
}
