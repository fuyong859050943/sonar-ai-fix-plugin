/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.lang;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 多语言支持管理器
 * 
 * 为不同编程语言提供特定的提示词模板和分析规则。
 */
public class LanguageSupport {

    private static final Logger LOG = Loggers.get(LanguageSupport.class);

    private final Map<String, LanguageHandler> handlers = new HashMap<>();

    public LanguageSupport() {
        registerDefaultHandlers();
    }

    private void registerDefaultHandlers() {
        registerHandler(new JavaHandler());
        registerHandler(new JavaScriptHandler());
        registerHandler(new PythonHandler());
        registerHandler(new TypeScriptHandler());
        registerHandler(new GoHandler());
        registerHandler(new CSharpHandler());
    }

    public void registerHandler(LanguageHandler handler) {
        handlers.put(handler.getLanguageKey(), handler);
        LOG.debug("Registered language handler for: {}", handler.getLanguageKey());
    }

    public LanguageHandler getHandler(String languageKey) {
        return handlers.get(languageKey);
    }

    public Set<String> getSupportedLanguages() {
        return handlers.keySet();
    }

    public boolean isLanguageSupported(String languageKey) {
        return handlers.containsKey(languageKey);
    }

    /**
     * 语言处理器接口
     */
    public interface LanguageHandler {
        String getLanguageKey();
        String getLanguageName();
        String getFileExtension();
        String getSystemPrompt();
        String getFixTemplate();
        String getCommentPrefix();
        String getImportPattern(String importPath);
        String formatCode(String code);
    }

    /**
     * Java 语言处理器
     */
    public static class JavaHandler implements LanguageHandler {
        @Override
        public String getLanguageKey() { return "java"; }

        @Override
        public String getLanguageName() { return "Java"; }

        @Override
        public String getFileExtension() { return ".java"; }

        @Override
        public String getSystemPrompt() {
            return """
                You are an expert Java code analyzer. Analyze the code for:
                - Best practices violations
                - Potential bugs
                - Security vulnerabilities
                - Performance issues
                - Code smell patterns
                
                Follow Java conventions (Oracle, Google, or project-specific).
                Consider Java 17+ features where applicable.
                """;
        }

        @Override
        public String getFixTemplate() {
            return """
                Fix the Java code issue following these guidelines:
                1. Use SLF4J for logging instead of System.out
                2. Use try-with-resources for Closeable resources
                3. Prefer immutable objects and final fields
                4. Use Optional for nullable return values
                5. Follow Java naming conventions
                
                Original code:
                ```java
                {original_code}
                ```
                
                Issue: {issue_description}
                
                Provide the fixed code in JSON format:
                """;
        }

        @Override
        public String getCommentPrefix() { return "//"; }

        @Override
        public String getImportPattern(String importPath) {
            return "import " + importPath + ";";
        }

        @Override
        public String formatCode(String code) {
            return code; // Ideally use a Java formatter
        }
    }

    /**
     * JavaScript 语言处理器
     */
    public static class JavaScriptHandler implements LanguageHandler {
        @Override
        public String getLanguageKey() { return "js"; }

        @Override
        public String getLanguageName() { return "JavaScript"; }

        @Override
        public String getFileExtension() { return ".js"; }

        @Override
        public String getSystemPrompt() {
            return """
                You are an expert JavaScript code analyzer. Analyze the code for:
                - Common JavaScript pitfalls
                - ES6+ best practices
                - Security vulnerabilities (XSS, injection)
                - Performance issues
                - Code organization patterns
                
                Consider modern JavaScript (ES2020+) features.
                """;
        }

        @Override
        public String getFixTemplate() {
            return """
                Fix the JavaScript code issue following these guidelines:
                1. Use const/let instead of var
                2. Use arrow functions for callbacks
                3. Use template literals for string interpolation
                4. Use async/await instead of raw Promises
                5. Handle errors properly with try-catch
                
                Original code:
                ```javascript
                {original_code}
                ```
                
                Issue: {issue_description}
                
                Provide the fixed code in JSON format:
                """;
        }

        @Override
        public String getCommentPrefix() { return "//"; }

        @Override
        public String getImportPattern(String importPath) {
            return "import '" + importPath + "';";
        }

        @Override
        public String formatCode(String code) {
            return code;
        }
    }

    /**
     * Python 语言处理器
     */
    public static class PythonHandler implements LanguageHandler {
        @Override
        public String getLanguageKey() { return "py"; }

        @Override
        public String getLanguageName() { return "Python"; }

        @Override
        public String getFileExtension() { return ".py"; }

        @Override
        public String getSystemPrompt() {
            return """
                You are an expert Python code analyzer. Analyze the code for:
                - PEP 8 compliance
                - Pythonic idioms
                - Security vulnerabilities
                - Performance issues
                - Type safety (consider type hints)
                
                Follow PEP 8 style guide and Python best practices.
                Consider Python 3.10+ features where applicable.
                """;
        }

        @Override
        public String getFixTemplate() {
            return """
                Fix the Python code issue following these guidelines:
                1. Follow PEP 8 naming conventions
                2. Use type hints for function signatures
                3. Use context managers for resource handling
                4. Use f-strings for string formatting
                5. Use list/dict comprehensions where appropriate
                
                Original code:
                ```python
                {original_code}
                ```
                
                Issue: {issue_description}
                
                Provide the fixed code in JSON format:
                """;
        }

        @Override
        public String getCommentPrefix() { return "#"; }

        @Override
        public String getImportPattern(String importPath) {
            return "import " + importPath;
        }

        @Override
        public String formatCode(String code) {
            return code;
        }
    }

    /**
     * TypeScript 语言处理器
     */
    public static class TypeScriptHandler implements LanguageHandler {
        @Override
        public String getLanguageKey() { return "ts"; }

        @Override
        public String getLanguageName() { return "TypeScript"; }

        @Override
        public String getFileExtension() { return ".ts"; }

        @Override
        public String getSystemPrompt() {
            return """
                You are an expert TypeScript code analyzer. Analyze the code for:
                - Type safety issues
                - Interface design patterns
                - Generic type usage
                - ES6+ features with TypeScript enhancements
                - Angular/React patterns (if applicable)
                
                Follow TypeScript best practices and strict mode guidelines.
                """;
        }

        @Override
        public String getFixTemplate() {
            return """
                Fix the TypeScript code issue following these guidelines:
                1. Use explicit types instead of 'any'
                2. Use interfaces for object shapes
                3. Use readonly for immutable properties
                4. Use optional chaining (?.) for null safety
                5. Use nullish coalescing (??) for defaults
                
                Original code:
                ```typescript
                {original_code}
                ```
                
                Issue: {issue_description}
                
                Provide the fixed code in JSON format:
                """;
        }

        @Override
        public String getCommentPrefix() { return "//"; }

        @Override
        public String getImportPattern(String importPath) {
            return "import { } from '" + importPath + "';";
        }

        @Override
        public String formatCode(String code) {
            return code;
        }
    }

    /**
     * Go 语言处理器
     */
    public static class GoHandler implements LanguageHandler {
        @Override
        public String getLanguageKey() { return "go"; }

        @Override
        public String getLanguageName() { return "Go"; }

        @Override
        public String getFileExtension() { return ".go"; }

        @Override
        public String getSystemPrompt() {
            return """
                You are an expert Go code analyzer. Analyze the code for:
                - Go idioms and conventions
                - Error handling patterns
                - Concurrency safety
                - Performance considerations
                - Code organization
                
                Follow Go best practices and effective Go guidelines.
                """;
        }

        @Override
        public String getFixTemplate() {
            return """
                Fix the Go code issue following these guidelines:
                1. Handle errors explicitly
                2. Use defer for cleanup
                3. Use context for cancellation
                4. Avoid global variables
                5. Use interfaces for abstraction
                
                Original code:
                ```go
                {original_code}
                ```
                
                Issue: {issue_description}
                
                Provide the fixed code in JSON format:
                """;
        }

        @Override
        public String getCommentPrefix() { return "//"; }

        @Override
        public String getImportPattern(String importPath) {
            return "import \"" + importPath + "\"";
        }

        @Override
        public String formatCode(String code) {
            return code;
        }
    }

    /**
     * C# 语言处理器
     */
    public static class CSharpHandler implements LanguageHandler {
        @Override
        public String getLanguageKey() { return "cs"; }

        @Override
        public String getLanguageName() { return "C#"; }

        @Override
        public String getFileExtension() { return ".cs"; }

        @Override
        public String getSystemPrompt() {
            return """
                You are an expert C# code analyzer. Analyze the code for:
                - .NET best practices
                - C# language features
                - Async/await patterns
                - LINQ usage
                - Memory management
                
                Follow .NET coding conventions and C# best practices.
                Consider C# 10+ features where applicable.
                """;
        }

        @Override
        public String getFixTemplate() {
            return """
                Fix the C# code issue following these guidelines:
                1. Use nullable reference types
                2. Use async/await for I/O operations
                3. Use LINQ for collection operations
                4. Use dependency injection
                5. Use record types for immutable data
                
                Original code:
                ```csharp
                {original_code}
                ```
                
                Issue: {issue_description}
                
                Provide the fixed code in JSON format:
                """;
        }

        @Override
        public String getCommentPrefix() { return "//"; }

        @Override
        public String getImportPattern(String importPath) {
            return "using " + importPath + ";";
        }

        @Override
        public String formatCode(String code) {
            return code;
        }
    }
}
