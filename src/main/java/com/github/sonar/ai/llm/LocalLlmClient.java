/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.llm;

import com.github.sonar.ai.config.AiFixConfiguration;
import com.github.sonar.ai.fix.FixSuggestion;
import com.google.gson.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 本地 LLM 客户端实现
 * 
 * 支持本地部署的 LLM 服务，如：
 * - Ollama (http://localhost:11434/v1)
 * - LM Studio (http://localhost:1234/v1)
 * - vLLM (http://localhost:8000/v1)
 * - 其他兼容 OpenAI API 格式的本地服务
 */
public class LocalLlmClient implements LlmClient {

    private static final Logger LOG = LoggerFactory.getLogger(LocalLlmClient.class);
    private static final MediaType JSON = MediaType.parse("application/json");

    private final String baseUrl;
    private final String model;
    private final int timeout;
    private final OkHttpClient httpClient;
    private final Gson gson;

    /**
     * 构造函数
     * 
     * @param config AI Fix 配置
     */
    public LocalLlmClient(AiFixConfiguration config) {
        this.baseUrl = config.getLocalLlmBaseUrl();
        this.model = config.getLocalLlmModel();
        this.timeout = config.getRequestTimeout();

        // 移除 baseUrl 末尾的斜杠
        String normalizedBaseUrl = baseUrl.endsWith("/")
            ? baseUrl.substring(0, baseUrl.length() - 1)
            : baseUrl;

        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout * 2, TimeUnit.SECONDS) // 本地模型可能较慢
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .build();

        this.gson = new GsonBuilder().setPrettyPrinting().create();

        LOG.info("Local LLM client initialized: baseUrl={}, model={}", normalizedBaseUrl, model);
    }

    @Override
    public FixSuggestion generateFix(
        String codeSnippet,
        String issueType,
        String issueMessage,
        String language,
        String filePath
    ) {
        LOG.info("Generating fix using local LLM for issue {} in file {}", issueType, filePath);

        String systemPrompt = buildSystemPrompt(language);
        String userPrompt = buildUserPrompt(codeSnippet, issueType, issueMessage, language, filePath);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.add("messages", new JsonArray());

        JsonArray messages = requestBody.getAsJsonArray("messages");

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", systemPrompt);
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", userPrompt);
        messages.add(userMessage);

        // 本地模型可能不支持所有参数，使用保守的设置
        requestBody.addProperty("temperature", 0.3);

        // 根据本地模型类型调整 max_tokens
        if (supportsMaxTokens()) {
            requestBody.addProperty("max_tokens", 2000);
        }

        String url = baseUrl + "/chat/completions";

        Request request = new Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .post(RequestBody.create(gson.toJson(requestBody), JSON))
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                LOG.error("Local LLM API error: {} - {}", response.code(), errorBody);
                throw new LlmException("Local LLM API error: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            return parseResponse(responseBody, issueType, issueMessage);

        } catch (IOException e) {
            LOG.error("Failed to call local LLM API", e);
            throw new LlmException("Failed to call local LLM API", e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            // 尝试获取模型列表（Ollama 使用 /api/tags，其他使用 /models）
            String modelsUrl = baseUrl + "/models";

            Request request = new Request.Builder()
                .url(modelsUrl)
                .get()
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return true;
                }
            }

            // 如果 /models 失败，尝试 Ollama 的 /api/tags
            String ollamaUrl = baseUrl.replace("/v1", "/api/tags");
            Request ollamaRequest = new Request.Builder()
                .url(ollamaUrl)
                .get()
                .build();

            try (Response response = httpClient.newCall(ollamaRequest).execute()) {
                return response.isSuccessful();
            }

        } catch (Exception e) {
            LOG.warn("Local LLM API is not available: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "Local LLM";
    }

    @Override
    public String getModelName() {
        return model;
    }

    /**
     * 检查是否支持 max_tokens 参数
     */
    private boolean supportsMaxTokens() {
        // Ollama 支持 num_predict 但不支持 max_tokens
        // LM Studio 和 vLLM 支持 max_tokens
        return !baseUrl.contains("11434"); // 简单检测，Ollama 默认使用 11434 端口
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(String language) {
        return String.format(
            "You are an expert code reviewer and bug fixer specializing in %s.\n\n" +
            "Your task is to analyze code issues and provide fix suggestions.\n\n" +
            "Output format (JSON):\n" +
            "{\n" +
            "  \"explanation\": \"Brief explanation of the issue\",\n" +
            "  \"root_cause\": \"The root cause of the issue\",\n" +
            "  \"fixed_code\": \"The corrected code snippet\",\n" +
            "  \"changes\": [\"List of changes\"],\n" +
            "  \"best_practices\": [\"Best practices\"],\n" +
            "  \"confidence\": \"high|medium|low\"\n" +
            "}\n\n" +
            "Rules:\n" +
            "1. Keep fixes minimal and focused\n" +
            "2. Preserve original code style\n" +
            "3. The fixed_code must be complete",
            language
        );
    }

    /**
     * 构建用户提示词
     */
    private String buildUserPrompt(
        String codeSnippet,
        String issueType,
        String issueMessage,
        String language,
        String filePath
    ) {
        return String.format(
            "## Issue\n" +
            "- Rule: %s\n" +
            "- Message: %s\n" +
            "- File: %s\n" +
            "- Language: %s\n\n" +
            "## Code\n" +
            "```%s\n%s\n```\n\n" +
            "## Task\n" +
            "Analyze and fix this issue. Output JSON format.",
            issueType,
            issueMessage,
            filePath,
            language,
            language,
            codeSnippet
        );
    }

    /**
     * 解析响应
     */
    private FixSuggestion parseResponse(String responseBody, String issueType, String issueMessage) {
        try {
            JsonObject response = JsonParser.parseString(responseBody).getAsJsonObject();
            
            // 兼容不同响应格式
            String content;
            if (response.has("choices")) {
                // OpenAI 格式
                content = response.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
            } else if (response.has("message")) {
                // Ollama 格式
                content = response.getAsJsonObject("message")
                    .get("content").getAsString();
            } else {
                throw new LlmException("Unknown response format");
            }

            String jsonContent = extractJson(content);
            JsonObject fixData = JsonParser.parseString(jsonContent).getAsJsonObject();

            List<String> changes = new ArrayList<>();
            if (fixData.has("changes")) {
                for (JsonElement element : fixData.getAsJsonArray("changes")) {
                    changes.add(element.getAsString());
                }
            }

            List<String> bestPractices = new ArrayList<>();
            if (fixData.has("best_practices")) {
                for (JsonElement element : fixData.getAsJsonArray("best_practices")) {
                    bestPractices.add(element.getAsString());
                }
            }

            String confidence = fixData.has("confidence")
                ? fixData.get("confidence").getAsString()
                : "medium";

            return new FixSuggestion(
                issueType,
                issueMessage,
                fixData.has("explanation") ? fixData.get("explanation").getAsString() : "No explanation",
                fixData.has("root_cause") ? fixData.get("root_cause").getAsString() : "Unknown",
                fixData.has("fixed_code") ? fixData.get("fixed_code").getAsString() : "",
                changes,
                bestPractices,
                confidence,
                getProviderName(),
                getModelName()
            );

        } catch (Exception e) {
            LOG.error("Failed to parse local LLM response", e);
            throw new LlmException("Failed to parse local LLM response: " + e.getMessage(), e);
        }
    }

    private String extractJson(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        throw new LlmException("No valid JSON found in response");
    }
}
