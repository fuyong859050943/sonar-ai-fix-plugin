/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.sensor;

import com.github.sonar.ai.config.AiFixConfiguration;
import com.github.sonar.ai.fix.FixGenerator;
import com.github.sonar.ai.fix.FixSuggestion;
import com.github.sonar.ai.issue.DefaultIssueCollector;
import com.github.sonar.ai.issue.IssueContext;
import com.github.sonar.ai.llm.LlmClient;
import com.github.sonar.ai.llm.LlmClientFactory;
import com.github.sonar.ai.cache.FixCache;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AI 分析传感器
 * 
 * 在代码扫描完成后，对检测到的问题进行 AI 分析，
 * 生成修复建议并存储到问题上下文中。
 */
public class AiAnalysisSensor implements Sensor {

    private static final Logger LOG = Loggers.get(AiAnalysisSensor.class);

    private final Configuration configuration;
    private final AiFixConfiguration aiConfig;
    private final DefaultIssueCollector issueCollector;
    private final FixCache fixCache;

    public AiAnalysisSensor(Configuration configuration, 
                           AiFixConfiguration aiConfig,
                           DefaultIssueCollector issueCollector,
                           FixCache fixCache) {
        this.configuration = configuration;
        this.aiConfig = aiConfig;
        this.issueCollector = issueCollector;
        this.fixCache = fixCache;
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
            .name("AI Analysis Sensor")
            .onlyOnLanguage("java")  // 可扩展支持更多语言
            .onlyWhenConfiguration(config -> aiConfig.isEnabled());
    }

    @Override
    public void execute(SensorContext context) {
        if (!aiConfig.isEnabled()) {
            LOG.info("AI Fix Plugin is disabled");
            return;
        }

        LOG.info("Starting AI analysis...");

        // 创建 LLM 客户端
        LlmClient llmClient = LlmClientFactory.createClient(configuration, aiConfig);
        if (!llmClient.isAvailable()) {
            LOG.warn("LLM client is not available. Please configure API key.");
            return;
        }

        // 创建修复生成器
        FixGenerator fixGenerator = new FixGenerator(llmClient, aiConfig);

        // 收集所有问题
        List<IssueContext> issues = issueCollector.collectIssues(context);
        LOG.info("Collected {} issues for AI analysis", issues.size());

        int processedCount = 0;
        int cachedCount = 0;
        int errorCount = 0;

        // 对每个问题生成修复建议
        for (IssueContext issue : issues) {
            try {
                // 检查缓存
                String cacheKey = fixCache.generateKey(issue.getRuleKey(), issue.getCodeSnippet());
                Optional<FixSuggestion> cached = fixCache.get(cacheKey);
                
                if (cached.isPresent()) {
                    cachedCount++;
                    issue.setFixSuggestion(cached.get());
                    LOG.debug("Using cached fix for issue: {}", issue.getRuleKey());
                } else {
                    // 生成新的修复建议
                    Optional<FixSuggestion> suggestion = fixGenerator.generateFix(
                        issue.getCodeSnippet(),
                        issue.getRuleKey(),
                        issue.getMessage()
                    );

                    if (suggestion.isPresent()) {
                        issue.setFixSuggestion(suggestion.get());
                        fixCache.put(cacheKey, suggestion.get());
                        processedCount++;
                    }
                }
            } catch (Exception e) {
                LOG.error("Error generating fix for issue {}: {}", 
                    issue.getRuleKey(), e.getMessage());
                errorCount++;
            }
        }

        // 输出统计信息
        LOG.info("AI analysis completed. Generated: {}, Cached: {}, Errors: {}", 
            processedCount, cachedCount, errorCount);
        LOG.info("Cache stats: {}", fixCache.getStats());

        // 保存结果（通过扩展属性）
        saveResults(context, issues);
    }

    /**
     * 保存分析结果到 SonarQube
     */
    private void saveResults(SensorContext context, List<IssueContext> issues) {
        // 将修复建议保存为问题扩展属性
        for (IssueContext issue : issues) {
            if (issue.hasFixSuggestion()) {
                // SonarQube 允许通过 addFlow 添加额外的信息
                // 这里简化处理，实际可以创建自定义的问题类型
                LOG.debug("Fix suggestion available for issue: {}", issue.getRuleKey());
            }
        }
    }
}
