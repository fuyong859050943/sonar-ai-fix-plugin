/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License as published by
 * the Open Source Initiative.
 */
package com.github.sonar.ai;

import org.sonar.api.Plugin;
import com.github.sonar.ai.config.AiFixConfiguration;
import com.github.sonar.ai.config.SettingsDefinition;
import com.github.sonar.ai.ws.AiFixWs;

/**
 * SonarQube AI Fix Plugin 入口类
 * 
 * 该插件为 SonarQube 社区版提供类似企业版 AI CodeFix 的功能，
 * 支持多种 LLM 后端（OpenAI、Azure OpenAI、本地模型）。
 * 
 * @author AI Fix Plugin Team
 * @version 1.0.0
 */
public class SonarAiFixPlugin implements Plugin {

    /**
     * 插件唯一标识
     */
    public static final String PLUGIN_KEY = "aifix";

    /**
     * 定义插件扩展点
     * 
     * @param context SonarQube 插件上下文
     */
    @Override
    public void define(Context context) {
        // 注册配置定义
        context.addExtensions(
            SettingsDefinition.class,
            AiFixConfiguration.class
        );

        // 注册 Web Service API
        context.addExtension(AiFixWs.class);

        // 日志输出
        System.out.println("[AI Fix Plugin] Plugin loaded successfully");
    }
}
