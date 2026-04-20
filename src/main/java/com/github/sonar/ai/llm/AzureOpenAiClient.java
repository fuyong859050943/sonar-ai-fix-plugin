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
 * Azure OpenAI LLM 客户端实现
 * 
 * 支持 Azure OpenAI Service 部署的模型。
 */
public class AzureOpenAiClient implements LlmClient {

    private static final Logger LOG = LoggerFactory.getLogger(AzureOpenAiClient.class);
    private static final MediaType JSON = MediaType.parse("application/json");

    private final String apiKey;
    private final String endpoint;
    private final String deployment;
    private final String apiVersion;
    private final int timeout;
    private final OkHttpClient httpClient;
    private final Gson gson;

    /**
     * 构造函数
     * 
     * @param config AI Fix 配置
     */
    public AzureOpenAiClient(AiFixConfiguration config) {
        this.apiKey = config.getAzureApiKey()
            .orElseThrow(() -> new IllegalArgumentException("Azure OpenAI API Key is required"));
        this.endpoint = config.getAzureEndpoint()
            .orElseThrow(() -> new IllegalArgumentException("Azure OpenAI Endpoint is required"));
        this.deployment = config.getAzureDeployment()
            .orElseThrow(() -> new IllegalArgumentException("Azure OpenAI Deployment is required"));
        this.apiVersion = "2024-02-15-preview"; // Azure OpenAI API 版本
        this.timeout = config.getRequestTimeout();

        // 移除 endpoint 末尾的斜杠
        String normalizedEndpoint = endpoint.endsWith("/") 
            ? endpoint.substring(0, endpoint.length() - 1) 
            : endpoint;

        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .build();

        this.gson = new GsonBuilder().setPrettyPrinting().create();

        LOG.info("Azure OpenAI client initialized: endpoint={}, deployment={}", normalizedEndpoint, deployment);
    }

    @Override
    public FixSuggestion generateFix(
        String codeSnippet,
        String issueType,
        String issueMessage,
        String language,
        String filePath
    ) {
        LOG.info("Generating fix using Azure OpenAI for issue {} in file {}", issueType, filePath);

        String systemPrompt = buildSystemPrompt(language);
        String userPrompt = buildUserPrompt(codeSnippet, issueType, issueMessage, language, filePath);

        JsonObject requestBody = new JsonObject();
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

        requestBody.addProperty("temperature", 0.3);
        requestBody.addProperty("max_tokens", 2000);

        // Azure OpenAI URL 格式：
        // {endpoint}/openai/deployments/{deployment}/chat/completions?api-version={api-version}
        String url = String.format("%s/openai/deployments/%s/chat/completions?api-version=%s",
            endpoint, deployment, apiVersion);

        Request request = new Request.Builder()
            .url(url)
            .header("api-key", apiKey) // Azure 使用 api-key header
            .header("Content-Type", "application/json")
            .post(RequestBody.create(gson.toJson(requestBody), JSON))
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                LOG.error("Azure OpenAI API error: {} - {}", response.code(), errorBody);
                throw new LlmException("Azure OpenAI API error: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            return parseResponse(responseBody, issueType, issueMessage);

        } catch (IOException e) {
            LOG.error("Failed to call Azure OpenAI API", e);
            throw new LlmException("Failed to call Azure OpenAI API", e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            // Azure OpenAI 不提供 /models 端点，使用一个简单的请求测试
            String url = String.format("%s/openai/deployments/%s/chat/completions?api-version=%s",
                endpoint, deployment, apiVersion);

            JsonObject testBody = new JsonObject();
            testBody.add("messages", new JsonArray());
            testBody.getAsJsonArray("messages").add(new JsonObject());
            testBody.getAsJsonArray("messages").get(0).getAsJsonObject().addProperty("role", "user");
            testBody.getAsJsonArray("messages").get(0).getAsJsonObject().addProperty("content", "test");
            testBody.addProperty("max_tokens", 1);

            Request request = new Request.Builder()
                .url(url)
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(gson.toJson(testBody), JSON))
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful() || response.code() == 400; // 400 表示 API 可达但请求参数问题
            }
        } catch (Exception e) {
            LOG.warn("Azure OpenAI API is not available", e);
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "Azure OpenAI";
    }

    @Override
    public String getModelName() {
        return deployment;
    }

    /**
     * 构建系统提示词（与 OpenAI 相同）
     */
    private String buildSystemPrompt(String language) {
        return String.format(
            "You are an expert code reviewer and bug fixer specializing in %s.\n\n" +
            "Your task is to analyze code issues and provide fix suggestions.\n\n" +
            "Output format (JSON):\n" +
            "{\n" +
            "  \"explanation\": \"Brief explanation of the issue and why it's a problem\",\n" +
            "  \"root_cause\": \"The root cause of the issue\",\n" +
            "  \"fixed_code\": \"The corrected code snippet (complete, not partial)\",\n" +
            "  \"changes\": [\"List of specific changes made\"],\n" +
            "  \"best_practices\": [\"Related best practices to follow\"],\n" +
            "  \"confidence\": \"high|medium|low\"\n" +
            "}\n\n" +
            "Rules:\n" +
            "1. Keep the fixed code minimal and focused on the specific issue\n" +
            "2. Preserve the original code style and formatting\n" +
            "3. Add comments explaining key changes\n" +
            "4. Suggest only necessary changes\n" +
            "5. Be precise and specific in your explanations\n" +
            "6. The fixed_code must be complete and compilable",
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
            "## Issue Information\n" +
            "- **Rule**: %s\n" +
            "- **Message**: %s\n" +
            "- **File**: %s\n" +
            "- **Language**: %s\n\n" +
            "## Code Context\n" +
            "```%s\n%s\n```\n\n" +
            "## Task\n" +
            "Analyze this issue and provide a fix suggestion in the specified JSON format.",
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
            String content = response.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

            String jsonContent = extractJson(content);
            JsonObject fixData = JsonParser.parseString(jsonContent).getAsJsonObject();

            List<String> changes = new ArrayList<>();
            JsonArray changesArray = fixData.getAsJsonArray("changes");
            if (changesArray != null) {
                for (JsonElement element : changesArray) {
                    changes.add(element.getAsString());
                }
            }

            List<String> bestPractices = new ArrayList<>();
            JsonArray practicesArray = fixData.getAsJsonArray("best_practices");
            if (practicesArray != null) {
                for (JsonElement element : practicesArray) {
                    bestPractices.add(element.getAsString());
                }
            }

            String confidence = fixData.has("confidence")
                ? fixData.get("confidence").getAsString()
                : "medium";

            return new FixSuggestion(
                issueType,
                issueMessage,
                fixData.get("explanation").getAsString(),
                fixData.get("root_cause").getAsString(),
                fixData.get("fixed_code").getAsString(),
                changes,
                bestPractices,
                confidence,
                getProviderName(),
                getModelName()
            );

        } catch (Exception e) {
            LOG.error("Failed to parse Azure OpenAI response", e);
            throw new LlmException("Failed to parse Azure OpenAI response: " + e.getMessage(), e);
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
