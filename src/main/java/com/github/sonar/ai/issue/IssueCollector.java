/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.issue;

import org.sonar.api.server.ServerSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import java.util.Optional;

/**
 * 问题收集器
 * 
 * 从 SonarQube 获取问题详情和源代码上下文。
 * 
 * 注意：这是一个接口设计，实际实现需要依赖 SonarQube 内部 API。
 */
@ServerSide
public interface IssueCollector {

    /**
     * 根据 issueKey 获取问题上下文
     * 
     * @param issueKey SonarQube 问题 Key
     * @return 问题上下文，如果不存在返回 empty
     */
    Optional<IssueContext> getIssueContext(String issueKey);

    /**
     * 获取源代码片段
     * 
     * @param componentKey 组件 Key（文件）
     * @param lineStart 开始行
     * @param lineEnd 结束行
     * @return 代码片段
     */
    Optional<String> getSourceCodeSnippet(String componentKey, int lineStart, int lineEnd);

    /**
     * 获取完整文件内容
     * 
     * @param componentKey 组件 Key（文件）
     * @return 完整文件内容
     */
    Optional<String> getFullFileContent(String componentKey);

    /**
     * 检测文件的语言
     * 
     * @param componentKey 组件 Key
     * @return 语言名称
     */
    @CheckForNull
    String detectLanguage(String componentKey);
}
