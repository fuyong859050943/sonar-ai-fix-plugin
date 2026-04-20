# SonarQube AI Fix Plugin

> 为 SonarQube 社区版提供 AI 代码修复建议功能

## 功能特性

- **AI CodeFix** - 自动生成代码修复建议
- **多 LLM 支持** - 支持 OpenAI、Azure OpenAI、本地模型
- **问题分析** - 智能分析问题根因
- **修复建议** - 提供详细的修复步骤和最佳实践

## 快速开始

### 环境要求

- Java 11+
- Maven 3.6+
- SonarQube 10.x

### 构建

```bash
mvn clean package
```

### 安装

1. 将 `target/sonar-ai-fix-plugin-1.0.0-SNAPSHOT.jar` 复制到 SonarQube 的 `extensions/plugins/` 目录
2. 重启 SonarQube
3. 在 SonarQube 管理界面配置 API Key

### 配置

在 SonarQube 管理界面 → 配置 → AI Fix 中设置：

| 配置项 | 说明 |
|--------|------|
| `sonar.ai-fix.enabled` | 是否启用 AI Fix |
| `sonar.ai-fix.llm.provider` | LLM 提供者 (openai/azure/local) |
| `sonar.ai-fix.openai.api-key` | OpenAI API Key |
| `sonar.ai-fix.openai.model` | OpenAI 模型 (gpt-4, gpt-4o) |

## 使用方法

### API 调用

```bash
# 生成修复建议
curl -X POST "http://localhost:9000/api/ai-fix/generate?issueKey=AY123456"

# 健康检查
curl "http://localhost:9000/api/ai-fix/health"
```

## 项目结构

```
sonar-ai-fix-plugin/
├── src/main/java/com/github/sonar/ai/
│   ├── SonarAiFixPlugin.java      # 插件入口
│   ├── config/                    # 配置模块
│   ├── llm/                       # LLM 客户端
│   ├── fix/                       # 修复生成
│   └── ws/                        # Web Service API
├── pom.xml
└── README.md
```

## 开发计划

- [x] 插件骨架
- [x] OpenAI 客户端
- [ ] Azure OpenAI 客户端
- [ ] 本地模型支持
- [ ] 前端 UI 集成
- [ ] 一键应用修复

## 许可证

MIT License
