/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.lang;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LanguageSupport 单元测试
 */
class LanguageSupportTest {

    private LanguageSupport languageSupport;

    @BeforeEach
    void setUp() {
        languageSupport = new LanguageSupport();
    }

    @Test
    void testGetSupportedLanguages() {
        var languages = languageSupport.getSupportedLanguages();

        assertTrue(languages.contains("java"));
        assertTrue(languages.contains("js"));
        assertTrue(languages.contains("ts"));
        assertTrue(languages.contains("py"));
        assertTrue(languages.contains("go"));
        assertTrue(languages.contains("cs"));
    }

    @Test
    void testIsLanguageSupported() {
        assertTrue(languageSupport.isLanguageSupported("java"));
        assertTrue(languageSupport.isLanguageSupported("py"));
        assertFalse(languageSupport.isLanguageSupported("ruby"));
    }

    @Test
    void testGetHandler_Java() {
        LanguageSupport.LanguageHandler handler = languageSupport.getHandler("java");

        assertNotNull(handler);
        assertEquals("java", handler.getLanguageKey());
        assertEquals("Java", handler.getLanguageName());
        assertEquals(".java", handler.getFileExtension());
        assertEquals("//", handler.getCommentPrefix());
    }

    @Test
    void testGetHandler_JavaScript() {
        LanguageSupport.LanguageHandler handler = languageSupport.getHandler("js");

        assertNotNull(handler);
        assertEquals("js", handler.getLanguageKey());
        assertEquals("JavaScript", handler.getLanguageName());
        assertEquals(".js", handler.getFileExtension());
        assertTrue(handler.getSystemPrompt().contains("JavaScript"));
    }

    @Test
    void testGetHandler_Python() {
        LanguageSupport.LanguageHandler handler = languageSupport.getHandler("py");

        assertNotNull(handler);
        assertEquals("py", handler.getLanguageKey());
        assertEquals("Python", handler.getLanguageName());
        assertEquals("#", handler.getCommentPrefix());
        assertTrue(handler.getSystemPrompt().contains("PEP 8"));
    }

    @Test
    void testGetHandler_TypeScript() {
        LanguageSupport.LanguageHandler handler = languageSupport.getHandler("ts");

        assertNotNull(handler);
        assertEquals("ts", handler.getLanguageKey());
        assertEquals("TypeScript", handler.getLanguageName());
        assertTrue(handler.getSystemPrompt().contains("Type"));
    }

    @Test
    void testGetHandler_Go() {
        LanguageSupport.LanguageHandler handler = languageSupport.getHandler("go");

        assertNotNull(handler);
        assertEquals("go", handler.getLanguageKey());
        assertEquals("Go", handler.getLanguageName());
        assertTrue(handler.getFixTemplate().contains("defer"));
    }

    @Test
    void testGetHandler_CSharp() {
        LanguageSupport.LanguageHandler handler = languageSupport.getHandler("cs");

        assertNotNull(handler);
        assertEquals("cs", handler.getLanguageKey());
        assertEquals("C#", handler.getLanguageName());
        assertTrue(handler.getSystemPrompt().contains(".NET"));
    }

    @Test
    void testGetHandler_Unsupported() {
        LanguageSupport.LanguageHandler handler = languageSupport.getHandler("ruby");
        assertNull(handler);
    }

    @Test
    void testJavaHandler_SystemPrompt() {
        LanguageSupport.LanguageHandler handler = languageSupport.getHandler("java");
        String prompt = handler.getSystemPrompt();

        assertTrue(prompt.contains("best practices"));
        assertTrue(prompt.contains("security"));
        assertTrue(prompt.contains("performance"));
    }

    @Test
    void testJavaHandler_FixTemplate() {
        LanguageSupport.LanguageHandler handler = languageSupport.getHandler("java");
        String template = handler.getFixTemplate();

        assertTrue(template.contains("{original_code}"));
        assertTrue(template.contains("{issue_description}"));
        assertTrue(template.contains("SLF4J"));
    }

    @Test
    void testJavaHandler_ImportPattern() {
        LanguageSupport.LanguageHandler handler = languageSupport.getHandler("java");
        String importPattern = handler.getImportPattern("java.util.List");

        assertEquals("import java.util.List;", importPattern);
    }

    @Test
    void testPythonHandler_ImportPattern() {
        LanguageSupport.LanguageHandler handler = languageSupport.getHandler("py");
        String importPattern = handler.getImportPattern("os.path");

        assertEquals("import os.path", importPattern);
    }

    @Test
    void testJavaScriptHandler_ImportPattern() {
        LanguageSupport.LanguageHandler handler = languageSupport.getHandler("js");
        String importPattern = handler.getImportPattern("lodash");

        assertEquals("import 'lodash';", importPattern);
    }

    @Test
    void testGoHandler_ImportPattern() {
        LanguageSupport.LanguageHandler handler = languageSupport.getHandler("go");
        String importPattern = handler.getImportPattern("fmt");

        assertEquals("import \"fmt\"", importPattern);
    }

    @Test
    void testCSharpHandler_ImportPattern() {
        LanguageSupport.LanguageHandler handler = languageSupport.getHandler("cs");
        String importPattern = handler.getImportPattern("System.Collections");

        assertEquals("using System.Collections;", importPattern);
    }

    @Test
    void testPythonHandler_FixTemplate() {
        LanguageSupport.LanguageHandler handler = languageSupport.getHandler("py");
        String template = handler.getFixTemplate();

        assertTrue(template.contains("PEP 8"));
        assertTrue(template.contains("type hints"));
        assertTrue(template.contains("f-strings"));
    }

    @Test
    void testTypeScriptHandler_FixTemplate() {
        LanguageSupport.LanguageHandler handler = languageSupport.getHandler("ts");
        String template = handler.getFixTemplate();

        assertTrue(template.contains("explicit types"));
        assertTrue(template.contains("interfaces"));
        assertTrue(template.contains("optional chaining"));
    }
}
