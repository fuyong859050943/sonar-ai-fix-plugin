/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.fix;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FixSuggestion 单元测试
 */
class FixSuggestionTest {

    private FixGenerator fixGenerator;

    @BeforeEach
    void setUp() {
        fixGenerator = new FixGenerator();
    }

    @Test
    void testFixSuggestion_Creation() {
        FixSuggestion suggestion = new FixSuggestion(
            "test-id",
            "org.sonar.java.rules:S1068",
            "Unused private field",
            "private String name;",
            "private String name; // TODO: use this field",
            "Add @SuppressWarnings or use the field"
        );

        assertEquals("test-id", suggestion.getId());
        assertEquals("org.sonar.java.rules:S1068", suggestion.getRuleKey());
        assertEquals("Unused private field", suggestion.getIssueType());
        assertNotNull(suggestion.getTimestamp());
    }

    @Test
    void testFixSuggestion_Equality() {
        FixSuggestion s1 = new FixSuggestion(
            "id-1",
            "rule-1",
            "type-1",
            "original",
            "fixed",
            "explanation"
        );

        FixSuggestion s2 = new FixSuggestion(
            "id-1",
            "rule-1",
            "type-1",
            "original",
            "fixed",
            "explanation"
        );

        assertEquals(s1.getId(), s2.getId());
    }

    @Test
    void testFixSuggestion_Builder() {
        FixSuggestion suggestion = FixSuggestion.builder()
            .id("builder-test")
            .ruleKey("test:rule")
            .issueType("Bug")
            .originalCode("int x = 1;")
            .fixedCode("final int x = 1;")
            .explanation("Variable should be final")
            .severity("MAJOR")
            .build();

        assertEquals("builder-test", suggestion.getId());
        assertEquals("test:rule", suggestion.getRuleKey());
        assertEquals("MAJOR", suggestion.getSeverity());
    }

    @Test
    void testFixSuggestion_Severity() {
        FixSuggestion suggestion = new FixSuggestion();
        suggestion.setSeverity("CRITICAL");
        
        assertEquals("CRITICAL", suggestion.getSeverity());
    }

    @Test
    void testFixSuggestion_Language() {
        FixSuggestion suggestion = new FixSuggestion();
        suggestion.setLanguage("java");
        
        assertEquals("java", suggestion.getLanguage());
    }

    @Test
    void testFixSuggestion_FilePath() {
        FixSuggestion suggestion = new FixSuggestion();
        suggestion.setFilePath("/path/to/File.java");
        
        assertEquals("/path/to/File.java", suggestion.getFilePath());
    }

    @Test
    void testFixSuggestion_LineNumber() {
        FixSuggestion suggestion = new FixSuggestion();
        suggestion.setStartLine(10);
        suggestion.setEndLine(15);
        
        assertEquals(10, suggestion.getStartLine());
        assertEquals(15, suggestion.getEndLine());
    }

    @Test
    void testFixSuggestion_Steps() {
        FixSuggestion suggestion = new FixSuggestion();
        suggestion.addStep("Step 1: Read the code");
        suggestion.addStep("Step 2: Apply the fix");
        
        assertEquals(2, suggestion.getSteps().size());
        assertTrue(suggestion.getSteps().contains("Step 1: Read the code"));
    }

    @Test
    void testFixSuggestion_Metadata() {
        FixSuggestion suggestion = new FixSuggestion();
        suggestion.setMetadata("key", "value");
        suggestion.setMetadata("confidence", "0.95");
        
        assertEquals("value", suggestion.getMetadata("key"));
        assertEquals("0.95", suggestion.getMetadata("confidence"));
    }

    @Test
    void testFixSuggestion_isExpired() throws InterruptedException {
        FixSuggestion suggestion = new FixSuggestion();
        suggestion.setTtlMinutes(1); // 1 minute TTL
        
        assertFalse(suggestion.isExpired());
        
        // 等待超过 TTL
        TimeUnit.MILLISECONDS.sleep(100);
        // 仍然不应该过期（1分钟很短）
        assertFalse(suggestion.isExpired());
    }
}
