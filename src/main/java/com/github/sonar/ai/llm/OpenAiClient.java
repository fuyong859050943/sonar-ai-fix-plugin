/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.llm;

import com.github.sonar.ai.config.AiFixConfiguration;
import com.github.sonar.ai.config.LlmProvider;
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
 * OpenAI LLM 客户端实现
 * 
 * 支持 OpenAI GPT-4、GPT-4o、GPT-3.5-turbo 等模型。
 */
public class OpenAiClient implements LlmClient {

    private static final Logger LOG = LoggerFactory.getLogger(OpenAiClient.class);
    private static final MediaType JSON = MediaType.parse("application/json");

    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final int timeout;
    private final OkHttpClient httpClient;
    private final Gson gson;

    /**
     * 构造函数
     * 
     * @param config AI Fix 配置
     */
    public OpenAiClient(AiFixConfiguration config) {
        this.apiKey = config.getOpenAiApiKey()
            .orElseThrow(() -> new IllegalArgumentException("OpenAI API Key is required"));
        this.model = config.getOpenAiModel();
        this.baseUrl = config.getOpenAiBaseUrl();
        this.timeout = config.getRequestTimeout();

        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .build();

        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    }

    @Override
    public FixSuggestion generateFix(
        String codeSnippet,
        String issueType,
        String issueMessage,
        String language,
        String filePath
    ) {
        LOG.info("Generating fix for issue {} in file {}", issueType, filePath);

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

        requestBody.addProperty("temperature", 0.3);
        requestBody.addProperty("max_tokens", 2000);

        String url = baseUrl + "/chat/completions";

        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .post(RequestBody.create(gson.toJson(requestBody), JSON))
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                LOG.error("OpenAI API error: {} - {}", response.code(), errorBody);
                throw new LlmException("OpenAI API error: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            return parseResponse(responseBody, issueType, issueMessage);

        } catch (IOException e) {
            LOG.error("Failed to call OpenAI API", e);
            throw new LlmException("Failed to call OpenAI API", e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            Request request = new Request.Builder()
                .url(baseUrl + "/models")
                .header("Authorization", "Bearer " + apiKey)
                .get()
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            LOG.warn("OpenAI API is not available", e);
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "OpenAI";
    }

    @Override
    public String getModelName() {
        return model;
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
     * 解析 LLM 响应
     */
    private FixSuggestion parseResponse(String responseBody, String issueType, String issueMessage) {
        try {
            JsonObject response = JsonParser.parseString(responseBody).getAsJsonObject();
            String content = response.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

            // 提取 JSON 内容
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
            LOG.error("Failed to parse OpenAI response", e);
            throw new LlmException("Failed to parse OpenAI response: " + e.getMessage(), e);
        }
    }

    /**
     * 从文本中提取 JSON
     */
    private String extractJson(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        throw new LlmException("No valid JSON found in response");
    }
}
