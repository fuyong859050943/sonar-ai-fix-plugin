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
import com.github.sonar.ai.llm.LlmClientFactory;
import com.github.sonar.ai.fix.FixGenerator;
import com.github.sonar.ai.issue.DefaultIssueCollector;
import com.github.sonar.ai.cache.FixCache;
import com.github.sonar.ai.sensor.AiAnalysisSensor;
import com.github.sonar.ai.rules.AiRulesDefinition;
import com.github.sonar.ai.rules.AiRuleSensor;
import com.github.sonar.ai.page.AiFixPageDefinition;
import com.github.sonar.ai.ws.AiFixRestService;

/**
 * SonarQube AI Fix Plugin 入口类
 * 
 * 该插件为 SonarQube 社区版提供类似企业版 AI CodeFix 的功能，
 * 支持多种 LLM 后端（OpenAI、Azure OpenAI、本地模型）。
 * 
 * 功能模块:
 * - AI 修复建议生成
 * - 多 LLM 提供商支持
 * - 智能缓存机制
 * - REST API 端点
 * - 自定义规则
 * - 前端 UI 组件
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
     * 插件版本
     */
    public static final String VERSION = "1.0.0";

    /**
     * 定义插件扩展点
     * 
     * 注册顺序很重要:
     * 1. 配置类 - 最先注册
     * 2. 核心组件 - 依赖配置
     * 3. 传感器和分析器 - 依赖核心组件
     * 4. Web 服务和页面 - 最后注册
     * 
     * @param context SonarQube 插件上下文
     */
    @Override
    public void define(Context context) {
        // ========== 第 1 层：配置和设置 ==========
        context.addExtensions(
            SettingsDefinition.class,      // 设置页面定义
            AiFixConfiguration.class       // 配置管理
        );

        // ========== 第 2 层：核心组件 ==========
        context.addExtensions(
            FixCache.class,                // 修复建议缓存
            DefaultIssueCollector.class    // 问题收集器
        );

        // ========== 第 3 层：传感器和分析器 ==========
        context.addExtensions(
            AiAnalysisSensor.class,        // AI 分析传感器
            AiRulesDefinition.class,       // 自定义规则定义
            AiRuleSensor.class             // 规则传感器
        );

        // ========== 第 4 层：Web 服务和页面 ==========
        context.addExtensions(
            AiFixRestService.class,        // REST API
            AiFixPageDefinition.class      // 页面定义
        );

        // 日志输出
        System.out.println("[AI Fix Plugin] v" + VERSION + " loaded successfully");
    }
}
