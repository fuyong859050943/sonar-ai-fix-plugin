/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.rules;

import com.github.sonar.ai.config.AiFixConfiguration;
import com.github.sonar.ai.llm.LlmClient;
import com.github.sonar.ai.llm.LlmClientFactory;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI 规则传感器
 * 
 * 扫描代码文件，使用 AI 分析潜在问题并报告。
 */
public class AiRuleSensor implements Sensor {

    private static final Logger LOG = Loggers.get(AiRuleSensor.class);

    private final Configuration configuration;
    private final AiFixConfiguration aiConfig;

    public AiRuleSensor(Configuration configuration, AiFixConfiguration aiConfig) {
        this.configuration = configuration;
        this.aiConfig = aiConfig;
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
            .name("AI Rule Sensor")
            .onlyOnLanguage("java")
            .onlyWhenConfiguration(config -> aiConfig.isEnabled());
    }

    @Override
    public void execute(SensorContext context) {
        if (!aiConfig.isEnabled()) {
            return;
        }

        LlmClient llmClient = LlmClientFactory.createClient(configuration, aiConfig);
        if (!llmClient.isAvailable()) {
            LOG.debug("LLM client not available, skipping AI rule analysis");
            return;
        }

        FileSystem fs = context.fileSystem();
        Iterable<InputFile> files = fs.inputFiles(fs.predicates().hasLanguage("java"));

        AtomicInteger analyzedFiles = new AtomicInteger(0);
        AtomicInteger issuesFound = new AtomicInteger(0);

        for (InputFile file : files) {
            try {
                String content = file.contents();
                String fileName = file.filename();

                // 分析代码质量
                String analysis = llmClient.assessCodeQuality(content, "java");

                // 解析分析结果并创建问题
                parseAndCreateIssues(context, file, analysis);

                analyzedFiles.incrementAndGet();
            } catch (IOException e) {
                LOG.error("Error reading file {}: {}", file.filename(), e.getMessage());
            } catch (Exception e) {
                LOG.error("Error analyzing file {}: {}", file.filename(), e.getMessage());
            }
        }

        LOG.info("AI Rule Sensor completed. Analyzed {} files, found {} potential issues",
            analyzedFiles.get(), issuesFound.get());
    }

    private void parseAndCreateIssues(SensorContext context, InputFile file, String analysis) {
        // 简化实现：直接创建信息级别的问题
        // 实际实现需要解析 LLM 返回的结构化数据
        
        if (analysis != null && !analysis.isEmpty()) {
            // 这里可以根据分析结果创建具体的问题
            // 示例：创建一个最佳实践建议
            if (analysis.contains("improve") || analysis.contains("suggestion")) {
                NewIssue issue = context.newIssue()
                    .forRule(RuleKey.of(AiRulesDefinition.REPOSITORY_KEY, 
                        AiRulesDefinition.RULE_AI_BEST_PRACTICE));

                NewIssueLocation location = issue.newLocation()
                    .on(file)
                    .at(file.selectLine(1))
                    .message("AI suggests improvements for this file. See analysis for details.");

                issue.at(location).save();
            }
        }
    }
}
