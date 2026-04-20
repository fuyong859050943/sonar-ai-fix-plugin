/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.issue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IssueContext 单元测试
 */
class IssueContextTest {

    private IssueContext.Builder builder;

    @BeforeEach
    void setUp() {
        builder = IssueContext.builder();
    }

    @Test
    void testBuilder_FullContext() {
        IssueContext context = builder
            .projectKey("my-project")
            .filePath("src/main/java/com/example/Service.java")
            .language("java")
            .ruleKey("java:S1068")
            .issueType("CODE_SMELL")
            .message("Unused private field")
            .severity("MAJOR")
            .startLine(15)
            .endLine(15)
            .codeSnippet("private String name;")
            .build();

        assertEquals("my-project", context.getProjectKey());
        assertEquals("src/main/java/com/example/Service.java", context.getFilePath());
        assertEquals("java", context.getLanguage());
        assertEquals("java:S1068", context.getRuleKey());
        assertEquals("CODE_SMELL", context.getIssueType());
        assertEquals("Unused private field", context.getMessage());
        assertEquals("MAJOR", context.getSeverity());
        assertEquals(15, context.getStartLine());
        assertEquals(15, context.getEndLine());
        assertEquals("private String name;", context.getCodeSnippet());
    }

    @Test
    void testBuilder_MinimalContext() {
        IssueContext context = builder
            .ruleKey("test:rule")
            .message("Test issue")
            .build();

        assertEquals("test:rule", context.getRuleKey());
        assertEquals("Test issue", context.getMessage());
    }

    @Test
    void testBuilder_RequiredFields() {
        assertThrows(IllegalStateException.class, () -> {
            builder.build();  // 缺少必需字段
        });
    }

    @Test
    void testGetFileExtension() {
        IssueContext context = builder
            .ruleKey("test:rule")
            .message("Test")
            .filePath("src/main/java/Test.java")
            .build();

        assertEquals(".java", context.getFileExtension());
    }

    @Test
    void testGetFileExtension_NoPath() {
        IssueContext context = builder
            .ruleKey("test:rule")
            .message("Test")
            .build();

        assertNull(context.getFileExtension());
    }

    @Test
    void testGetFileName() {
        IssueContext context = builder
            .ruleKey("test:rule")
            .message("Test")
            .filePath("src/main/java/com/example/Service.java")
            .build();

        assertEquals("Service.java", context.getFileName());
    }

    @Test
    void testIsSecurityIssue() {
        IssueContext context1 = builder
            .ruleKey("test:rule")
            .message("Test")
            .issueType("VULNERABILITY")
            .build();
        assertTrue(context1.isSecurityIssue());

        IssueContext context2 = builder
            .ruleKey("test:rule")
            .message("Test")
            .issueType("CODE_SMELL")
            .build();
        assertFalse(context2.isSecurityIssue());
    }

    @Test
    void testIsBug() {
        IssueContext context = builder
            .ruleKey("test:rule")
            .message("Test")
            .issueType("BUG")
            .build();

        assertTrue(context.isBug());
    }

    @Test
    void testIsCodeSmell() {
        IssueContext context = builder
            .ruleKey("test:rule")
            .message("Test")
            .issueType("CODE_SMELL")
            .build();

        assertTrue(context.isCodeSmell());
    }

    @Test
    void testToMap() {
        IssueContext context = builder
            .projectKey("test-project")
            .ruleKey("test:rule")
            .message("Test issue")
            .severity("MAJOR")
            .build();

        var map = context.toMap();

        assertEquals("test-project", map.get("projectKey"));
        assertEquals("test:rule", map.get("ruleKey"));
        assertEquals("Test issue", map.get("message"));
        assertEquals("MAJOR", map.get("severity"));
    }

    @Test
    void testToJson() {
        IssueContext context = builder
            .ruleKey("test:rule")
            .message("Test issue")
            .build();

        String json = context.toJson();

        assertTrue(json.contains("test:rule"));
        assertTrue(json.contains("Test issue"));
    }
}
