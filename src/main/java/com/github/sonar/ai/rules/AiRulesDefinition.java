/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.rules;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.SonarRuntime;
import org.sonar.api.utils.Version;

/**
 * AI 规则定义
 * 
 * 定义插件提供的自定义规则，用于检测 AI 相关的代码问题。
 */
public class AiRulesDefinition implements RulesDefinition {

    public static final String REPOSITORY_KEY = "sonar-ai-fix";
    public static final String REPOSITORY_NAME = "AI Code Quality Rules";

    // 规则键
    public static final String RULE_AI_CODE_SMELL = "ai-code-smell";
    public static final String RULE_AI_SECURITY_ISSUE = "ai-security-issue";
    public static final String RULE_AI_PERFORMANCE = "ai-performance-issue";
    public static final String RULE_AI_BEST_PRACTICE = "ai-best-practice";

    private final SonarRuntime runtime;

    public AiRulesDefinition(SonarRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public void define(Context context) {
        NewRepository repository = context
            .createRepository(REPOSITORY_KEY, "java")
            .setName(REPOSITORY_NAME);

        // 代码异味规则
        createCodeSmellRule(repository);

        // 安全问题规则
        createSecurityRule(repository);

        // 性能问题规则
        createPerformanceRule(repository);

        // 最佳实践规则
        createBestPracticeRule(repository);

        repository.done();
    }

    private void createCodeSmellRule(NewRepository repository) {
        NewRule rule = repository.createRule(RULE_AI_CODE_SMELL)
            .setName("AI-Detected Code Smell")
            .setHtmlDescription(
                "<p>Code smell detected by AI analysis. This indicates code that may be " +
                "hard to maintain, understand, or extend.</p>" +
                "<h2>Common Issues</h2>" +
                "<ul>" +
                "<li>Long methods (> 30 lines)</li>" +
                "<li>Complex conditionals</li>" +
                "<li>Duplicate code patterns</li>" +
                "<li>Poor naming conventions</li>" +
                "</ul>" +
                "<h2>Recommendation</h2>" +
                "<p>Review the AI-generated fix suggestion for refactoring options.</p>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL);

        rule.setDebtSubCharacteristic("MAINTAINABILITY");
        rule.setDebtRemediationFunction(
            rule.debtRemediationFunctions().constantPerIssue("5min")
        );

        // 添加标签
        rule.addTags("ai-detected", "maintainability", "code-smell");

        // 参数：最小代码行数
        rule.createParam("minLines")
            .setDescription("Minimum lines of code to consider as a long method")
            .setType(RuleParamType.INTEGER)
            .setDefaultValue("30");
    }

    private void createSecurityRule(NewRepository repository) {
        NewRule rule = repository.createRule(RULE_AI_SECURITY_ISSUE)
            .setName("AI-Detected Security Vulnerability")
            .setHtmlDescription(
                "<p>Security vulnerability detected by AI analysis. This may expose " +
                "the application to attacks or data breaches.</p>" +
                "<h2>Common Issues</h2>" +
                "<ul>" +
                "<li>SQL injection vulnerabilities</li>" +
                "<li>XSS (Cross-Site Scripting) risks</li>" +
                "<li>Insecure data handling</li>" +
                "<li>Hardcoded credentials</li>" +
                "</ul>" +
                "<h2>Recommendation</h2>" +
                "<p>Address immediately. Review the AI-generated fix suggestion.</p>"
            )
            .setSeverity("CRITICAL")
            .setType(org.sonar.api.rules.RuleType.VULNERABILITY);

        rule.setDebtSubCharacteristic("SECURITY");
        rule.setDebtRemediationFunction(
            rule.debtRemediationFunctions().constantPerIssue("15min")
        );

        rule.addTags("ai-detected", "security", "vulnerability", "owasp");
    }

    private void createPerformanceRule(NewRepository repository) {
        NewRule rule = repository.createRule(RULE_AI_PERFORMANCE)
            .setName("AI-Detected Performance Issue")
            .setHtmlDescription(
                "<p>Performance issue detected by AI analysis. This code may cause " +
                "slowdowns or resource exhaustion under load.</p>" +
                "<h2>Common Issues</h2>" +
                "<ul>" +
                "<li>Inefficient loops</li>" +
                "<li>Unnecessary object creation</li>" +
                "<li>Poor database query patterns</li>" +
                "<li>Memory leaks</li>" +
                "</ul>" +
                "<h2>Recommendation</h2>" +
                "<p>Optimize based on the AI-generated suggestion.</p>"
            )
            .setSeverity("MAJOR")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL);

        rule.setDebtSubCharacteristic("EFFICIENCY");
        rule.setDebtRemediationFunction(
            rule.debtRemediationFunctions().constantPerIssue("10min")
        );

        rule.addTags("ai-detected", "performance", "optimization");
    }

    private void createBestPracticeRule(NewRepository repository) {
        NewRule rule = repository.createRule(RULE_AI_BEST_PRACTICE)
            .setName("AI-Suggested Best Practice")
            .setHtmlDescription(
                "<p>Best practice suggestion from AI analysis. Following this recommendation " +
                "will improve code quality and maintainability.</p>" +
                "<h2>Common Suggestions</h2>" +
                "<ul>" +
                "<li>Use immutable objects</li>" +
                "<li>Apply design patterns</li>" +
                "<li>Improve error handling</li>" +
                "<li>Add documentation</li>" +
                "</ul>" +
                "<h2>Recommendation</h2>" +
                "<p>Consider applying the suggested improvement.</p>"
            )
            .setSeverity("INFO")
            .setType(org.sonar.api.rules.RuleType.CODE_SMELL);

        rule.setDebtSubCharacteristic("MAINTAINABILITY");
        rule.setDebtRemediationFunction(
            rule.debtRemediationFunctions().constantPerIssue("3min")
        );

        rule.addTags("ai-detected", "best-practice", "clean-code");
    }
}
