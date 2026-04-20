/*
 * SonarQube AI Fix Plugin
 * Copyright (C) 2024
 */
package com.github.sonar.ai.page;

import org.sonar.api.web.page.Context;
import org.sonar.api.web.page.Page;
import org.sonar.api.web.page.Page.Scope;
import org.sonar.api.web.page.PageDefinition;

/**
 * AI 修复页面定义
 * 
 * 在 SonarQube 中注册自定义页面。
 */
public class AiFixPageDefinition implements PageDefinition {

    @Override
    public void define(Context context) {
        // AI 修复概览页面
        context
            .addPage(
                Page.builder("sonar-ai-fix/overview")
                    .setName("AI 修复概览")
                    .setScope(Scope.GLOBAL)
                    .setDescription("查看所有 AI 生成的修复建议")
                    .build()
            )
            // 项目级别的 AI 修复页面
            .addPage(
                Page.builder("sonar-ai-fix/project")
                    .setName("AI 修复")
                    .setScope(Scope.COMPONENT)
                    .setComponentQualifier("TRK")
                    .setDescription("查看项目的 AI 修复建议")
                    .build()
            );
    }
}
