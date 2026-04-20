/*
 * SonarQube AI Fix Plugin - Tests
 * Copyright (C) 2024
 */
package com.github.sonar.ai.fix;

import com.github.sonar.ai.config.AiFixConfiguration;
import com.github.sonar.ai.llm.LlmClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 修复生成器测试
 */
class FixGeneratorTest {

    private FixGenerator fixGenerator;
    private MockLlmClient mockClient;
    private AiFixConfiguration aiConfig;

    @BeforeEach
    void setUp() {
        MapSettings settings = new MapSettings();
        settings.setProperty("sonar.ai.enabled", true);
        settings.setProperty("sonar.ai.max-tokens", 2000);
        
        Configuration config = settings.asConfig();
        aiConfig = new AiFixConfiguration(config);
        
        mockClient = new MockLlmClient();
        fixGenerator = new FixGenerator(mockClient, aiConfig);
    }

    @Test
    @DisplayName("Should generate fix suggestion for valid input")
    void shouldGenerateFixForValidInput() {
        // Given
        String code = "public void test() { System.out.println(\"test\"); }";
        String ruleKey = "java:S106";
        String message = "Use logger instead of System.out";

        // When
        Optional<FixSuggestion> result = fixGenerator.generateFix(code, ruleKey, message);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Fixed code placeholder", result.get().getFixedCode());
        assertNotNull(result.get().getExplanation());
    }

    @Test
    @DisplayName("Should return empty when LLM returns empty")
    void shouldReturnEmptyWhenLlmReturnsEmpty() {
        // Given
        mockClient.setReturnEmpty(true);
        String code = "public void test() {}";
        String ruleKey = "java:S1186";
        String message = "Empty method";

        // When
        Optional<FixSuggestion> result = fixGenerator.generateFix(code, ruleKey, message);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should handle complex code")
    void shouldHandleComplexCode() {
        // Given
        String code = """
            public class Example {
                private String name;
                
                public void process() {
                    if (name != null) {
                        System.out.println(name);
                    }
                }
            }
            """;
        String ruleKey = "java:S106";
        String message = "Use logger instead of System.out";

        // When
        Optional<FixSuggestion> result = fixGenerator.generateFix(code, ruleKey, message);

        // Then
        assertTrue(result.isPresent());
    }

    /**
     * Mock LLM 客户端用于测试
     */
    private static class MockLlmClient implements LlmClient {
        private boolean returnEmpty = false;

        public void setReturnEmpty(boolean returnEmpty) {
            this.returnEmpty = returnEmpty;
        }

        @Override
        public String generateFixSuggestion(String code, String issueType, String context) {
            if (returnEmpty) return "";
            return """
                {
                    "fixedCode": "Fixed code placeholder",
                    "explanation": "This is a test explanation",
                    "steps": ["Step 1", "Step 2", "Step 3"],
                    "severity": "MAJOR"
                }
                """;
        }

        @Override
        public String explainIssue(String issueDescription, String codeSnippet) {
            if (returnEmpty) return "";
            return "This is a test explanation for the issue.";
        }

        @Override
        public String assessCodeQuality(String code, String language) {
            return "Code quality assessment placeholder";
        }

        @Override
        public String chat(String userMessage, Map<String, String> context) {
            return "Chat response placeholder";
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }
}
