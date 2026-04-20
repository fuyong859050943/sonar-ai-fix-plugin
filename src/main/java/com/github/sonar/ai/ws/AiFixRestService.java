/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.ws;

import com.github.sonar.ai.config.AiFixConfiguration;
import com.github.sonar.ai.fix.FixGenerator;
import com.github.sonar.ai.fix.FixSuggestion;
import com.github.sonar.ai.llm.LlmClient;
import com.github.sonar.ai.llm.LlmClientFactory;
import com.github.sonar.ai.cache.FixCache;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * AI 修复 REST API 控制器
 * 
 * 提供获取和生成 AI 修复建议的 API 端点。
 */
public class AiFixRestService implements WebService {

    private static final Logger LOG = Loggers.get(AiFixRestService.class);

    private final Configuration configuration;
    private final AiFixConfiguration aiConfig;
    private final FixCache fixCache;

    public AiFixRestService(Configuration configuration, 
                           AiFixConfiguration aiConfig,
                           FixCache fixCache) {
        this.configuration = configuration;
        this.aiConfig = aiConfig;
        this.fixCache = fixCache;
    }

    @Override
    public void define(Context context) {
        // 创建 API 控制器
        NewController controller = context.createController("api/ai-fix")
            .setSince("1.0")
            .setDescription("AI Fix Plugin API");

        // GET /api/ai-fix/suggestions/{issueKey}
        // 获取问题的 AI 修复建议
        controller.createAction("suggestions")
            .setSince("1.0")
            .setDescription("Get AI fix suggestion for an issue")
            .setHandler(new GetSuggestionHandler())
            .createParam("issueKey")
                .setDescription("Issue key")
                .setRequired(true);

        // POST /api/ai-fix/generate/{issueKey}
        // 生成新的 AI 修复建议
        controller.createAction("generate")
            .setSince("1.0")
            .setDescription("Generate AI fix suggestion for an issue")
            .setPost(true)
            .setHandler(new GenerateSuggestionHandler())
            .createParam("issueKey")
                .setDescription("Issue key")
                .setRequired(true);

        // GET /api/ai-fix/status
        // 获取 AI 插件状态
        controller.createAction("status")
            .setSince("1.0")
            .setDescription("Get AI fix plugin status")
            .setHandler(new GetStatusHandler());

        // POST /api/ai-fix/apply
        // 应用修复建议
        controller.createAction("apply")
            .setSince("1.0")
            .setDescription("Apply AI fix suggestion")
            .setPost(true)
            .setHandler(new ApplyFixHandler())
            .createParam("suggestionId")
                .setDescription("Suggestion ID")
                .setRequired(true);

        // GET /api/ai-fix/cache/stats
        // 获取缓存统计信息
        controller.createAction("cache/stats")
            .setSince("1.0")
            .setDescription("Get cache statistics")
            .setHandler(new GetCacheStatsHandler());

        controller.done();
    }

    /**
     * 获取修复建议处理器
     */
    private class GetSuggestionHandler implements RequestHandler {
        @Override
        public void handle(Request request, Response response) {
            String issueKey = request.mandatoryParam("issueKey");
            
            try {
                // 从缓存获取
                Optional<FixSuggestion> cached = fixCache.get(
                    fixCache.generateKey(issueKey, null)
                );

                Map<String, Object> result = new HashMap<>();
                if (cached.isPresent()) {
                    result.put("suggestion", cached.get());
                    result.put("cached", true);
                } else {
                    result.put("suggestion", null);
                    result.put("cached", false);
                }

                response.stream().output(getJsonResponse(result));
            } catch (Exception e) {
                LOG.error("Error getting suggestion for issue {}", issueKey, e);
                response.stream().output(getErrorResponse(e.getMessage()));
            }
        }
    }

    /**
     * 生成修复建议处理器
     */
    private class GenerateSuggestionHandler implements RequestHandler {
        @Override
        public void handle(Request request, Response response) {
            String issueKey = request.mandatoryParam("issueKey");
            
            try {
                LlmClient llmClient = LlmClientFactory.createClient(configuration, aiConfig);
                if (!llmClient.isAvailable()) {
                    response.stream().output(getErrorResponse("LLM client not available"));
                    return;
                }

                FixGenerator fixGenerator = new FixGenerator(llmClient, aiConfig);
                
                // TODO: 从 SonarQube 获取问题详情
                String codeSnippet = "// Code snippet placeholder";
                String ruleKey = "java:S1234";
                String message = "Issue message placeholder";

                Optional<FixSuggestion> suggestion = fixGenerator.generateFix(
                    codeSnippet, ruleKey, message
                );

                Map<String, Object> result = new HashMap<>();
                if (suggestion.isPresent()) {
                    String cacheKey = fixCache.generateKey(issueKey, codeSnippet);
                    fixCache.put(cacheKey, suggestion.get());
                    result.put("suggestion", suggestion.get());
                    result.put("generated", true);
                } else {
                    result.put("suggestion", null);
                    result.put("generated", false);
                }

                response.stream().output(getJsonResponse(result));
            } catch (Exception e) {
                LOG.error("Error generating suggestion for issue {}", issueKey, e);
                response.stream().output(getErrorResponse(e.getMessage()));
            }
        }
    }

    /**
     * 获取状态处理器
     */
    private class GetStatusHandler implements RequestHandler {
        @Override
        public void handle(Request request, Response response) {
            Map<String, Object> status = new HashMap<>();
            status.put("enabled", aiConfig.isEnabled());
            status.put("provider", aiConfig.getProvider());
            status.put("model", aiConfig.getModel());
            status.put("cacheSize", fixCache.size());
            status.put("cacheHitRate", fixCache.hitRate());

            response.stream().output(getJsonResponse(status));
        }
    }

    /**
     * 应用修复处理器
     */
    private class ApplyFixHandler implements RequestHandler {
        @Override
        public void handle(Request request, Response response) {
            String suggestionId = request.mandatoryParam("suggestionId");
            
            // TODO: 实现应用修复逻辑
            Map<String, Object> result = new HashMap<>();
            result.put("applied", false);
            result.put("message", "Apply fix feature coming soon");

            response.stream().output(getJsonResponse(result));
        }
    }

    /**
     * 获取缓存统计处理器
     */
    private class GetCacheStatsHandler implements RequestHandler {
        @Override
        public void handle(Request request, Response response) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("stats", fixCache.getStats());
            stats.put("size", fixCache.size());
            stats.put("hitRate", fixCache.hitRate());

            response.stream().output(getJsonResponse(stats));
        }
    }

    // 辅助方法
    private String getJsonResponse(Map<String, Object> data) {
        // 简化的 JSON 序列化
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");
            json.append(objectToJson(entry.getValue()));
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    private String objectToJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escapeJson((String) obj) + "\"";
        if (obj instanceof Boolean || obj instanceof Number) return obj.toString();
        // 简化处理，实际应使用 Gson
        return "\"" + escapeJson(obj.toString()) + "\"";
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String getErrorResponse(String message) {
        return "{\"error\": \"" + escapeJson(message) + "\"}";
    }
}
