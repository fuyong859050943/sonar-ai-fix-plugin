/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.ws;

import org.sonar.api.server.ws.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Fix Web Service
 * 
 * 提供 REST API 供前端调用。
 */
public class AiFixWs implements WebService {

    private static final Logger LOG = LoggerFactory.getLogger(AiFixWs.class);

    @Override
    public void define(Context context) {
        LOG.info("Defining AI Fix Web Service");

        NewController controller = context.createController("api/ai-fix");
        controller.setDescription("AI-powered code fix suggestions");

        // 生成修复建议 API
        controller.createAction("generate")
            .setDescription("Generate AI fix suggestion for an issue")
            .setHandler(this::handleGenerate)
            .createParam("issueKey")
                .setDescription("The key of the SonarQube issue")
                .setRequired(true);

        // 获取配置 API
        controller.createAction("config")
            .setDescription("Get AI Fix plugin configuration")
            .setHandler(this::handleGetConfig);

        // 健康检查 API
        controller.createAction("health")
            .setDescription("Check AI Fix plugin health status")
            .setHandler(this::handleHealth);

        controller.done();
        LOG.info("AI Fix Web Service defined");
    }

    /**
     * 处理生成修复请求
     */
    private void handleGenerate(Request request, Response response) {
        String issueKey = request.mandatoryParam("issueKey");
        LOG.info("Generating fix for issue: {}", issueKey);

        // TODO: 实现修复生成逻辑

        response.stream().output(stream -> {
            String json = String.format(
                "{\"status\":\"ok\",\"issueKey\":\"%s\",\"message\":\"Fix generation not implemented yet\"}",
                issueKey
            );
            stream.write(json.getBytes());
        });
    }

    /**
     * 处理获取配置请求
     */
    private void handleGetConfig(Request request, Response response) {
        LOG.info("Getting AI Fix configuration");

        response.stream().output(stream -> {
            String json = "{\"status\":\"ok\",\"message\":\"Configuration endpoint\"}";
            stream.write(json.getBytes());
        });
    }

    /**
     * 处理健康检查请求
     */
    private void handleHealth(Request request, Response response) {
        response.stream().output(stream -> {
            String json = "{\"status\":\"healthy\",\"version\":\"1.0.0\"}";
            stream.write(json.getBytes());
        });
    }
}
