# SonarQube AI Fix Plugin - 项目规划

## 项目概述

### 目标
开发一个 SonarQube 插件，为社区版提供类似企业版 AI CodeFix 和 AI Code Assurance 的功能。

### 核心功能

| 功能 | 说明 | 优先级 |
|------|------|--------|
| **AI CodeFix** | 自动生成代码修复建议 | P0 |
| **多 LLM 支持** | 支持 OpenAI、Azure OpenAI、本地模型 | P0 |
| **问题分析** | 智能分析问题根因 | P1 |
| **一键应用** | 在 SonarQube UI 中直接应用修复 | P2 |
| **修复历史** | 记录 AI 修复历史 | P2 |

### 技术栈

| 类别 | 技术选型 |
|------|---------|
| 语言 | Java 11+ |
| 构建工具 | Maven |
| 框架 | SonarQube Plugin API 10.x |
| HTTP 客户端 | OkHttp 4.x |
| JSON 处理 | Gson |
| 缓存 | Caffeine |
| 前端 | TypeScript + React |

---

## 项目结构

```
sonar-ai-fix-plugin/
├── pom.xml                              # Maven 配置
├── README.md                            # 项目文档
├── LICENSE                              # 许可证
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/github/sonar/ai/
│   │   │       ├── SonarAiFixPlugin.java        # 插件入口
│   │   │       │
│   │   │       ├── config/                      # 配置模块
│   │   │       │   ├── AiFixConfiguration.java
│   │   │       │   ├── LlmProvider.java
│   │   │       │   └── SettingsDefinition.java
│   │   │       │
│   │   │       ├── issue/                       # 问题处理模块
│   │   │       │   ├── IssueCollector.java
│   │   │       │   ├── IssueContext.java
│   │   │       │   └── SourceCodeProvider.java
│   │   │       │
│   │   │       ├── llm/                         # LLM 客户端模块
│   │   │       │   ├── LlmClient.java           # 接口
│   │   │       │   ├── LlmClientFactory.java    # 工厂
│   │   │       │   ├── OpenAiClient.java        # OpenAI 实现
│   │   │       │   ├── AzureOpenAiClient.java   # Azure 实现
│   │   │       │   ├── LocalLlmClient.java      # 本地模型实现
│   │   │       │   └── LlmResponse.java         # 响应模型
│   │   │       │
│   │   │       ├── fix/                         # 修复生成模块
│   │   │       │   ├── FixGenerator.java
│   │   │       │   ├── FixSuggestion.java
│   │   │       │   ├── PromptBuilder.java
│   │   │       │   └── CodeApplier.java
│   │   │       │
│   │   │       ├── ws/                          # Web Service API
│   │   │       │   ├── AiFixWs.java
│   │   │       │   ├── GenerateFixAction.java
│   │   │       │   ├── GetConfigAction.java
│   │   │       │   └── ApplyFixAction.java
│   │   │       │
│   │   │       └── util/                        # 工具类
│   │   │           ├── JsonUtils.java
│   │   │           └── HttpUtils.java
│   │   │
│   │   └── resources/
│   │       └── static/
│   │           └── ai-fix-plugin/
│   │               ├── js/
│   │               │   ├── main.js
│   │               │   ├── components/
│   │               │   │   ├── FixSuggestionPanel.tsx
│   │               │   │   └── CodeDiffViewer.tsx
│   │               │   └── services/
│   │               │       └── api.ts
│   │               └── css/
│   │                   └── style.css
│   │
│   └── test/
│       └── java/
│           └── com/github/sonar/ai/
│               ├── llm/
│               │   └── OpenAiClientTest.java
│               ├── fix/
│               │   └── FixGeneratorTest.java
│               └── ws/
│                   └── AiFixWsTest.java
│
├── docs/                                 # 文档目录
│   ├── ARCHITECTURE.md                   # 架构设计
│   ├── API.md                            # API 文档
│   └── CONFIGURATION.md                  # 配置说明
│
└── scripts/                              # 构建和部署脚本
    ├── build.sh
    └── deploy.sh
```

---

## 开发计划

### Phase 1: 基础框架（2 周）

**目标：** 搭建插件骨架，实现配置管理

**任务清单：**

| 任务 | 预估工时 | 状态 |
|------|---------|------|
| 创建 Maven 项目结构 | 4h | 待开始 |
| 实现插件入口类 | 4h | 待开始 |
| 配置管理模块 | 8h | 待开始 |
| SonarQube 设置页面集成 | 8h | 待开始 |
| 单元测试框架搭建 | 4h | 待开始 |
| 本地调试环境 | 8h | 待开始 |

**交付物：**
- 可编译的插件 JAR
- SonarQube 配置页面可见配置项

---

### Phase 2: LLM 集成（3 周）

**目标：** 实现多 LLM 后端支持

**任务清单：**

| 任务 | 预估工时 | 状态 |
|------|---------|------|
| LLM 客户端接口设计 | 4h | 待开始 |
| OpenAI 客户端实现 | 12h | 待开始 |
| Azure OpenAI 客户端实现 | 8h | 待开始 |
| 本地模型客户端实现 | 12h | 待开始 |
| LLM 工厂模式实现 | 4h | 待开始 |
| 错误处理和重试机制 | 8h | 待开始 |
| 速率限制 | 4h | 待开始 |
| 缓存机制 | 8h | 待开始 |
| 单元测试 | 12h | 待开始 |

**交付物：**
- 可调用 OpenAI API 的客户端
- 支持 Azure OpenAI
- 支持本地模型（Ollama）

---

### Phase 3: 问题分析与修复生成（2 周）

**目标：** 实现问题收集和修复建议生成

**任务清单：**

| 任务 | 预估工时 | 状态 |
|------|---------|------|
| 问题收集器实现 | 8h | 待开始 |
| 源代码获取 | 8h | 待开始 |
| Prompt 模板设计 | 8h | 待开始 |
| 修复生成器实现 | 12h | 待开始 |
| 响应解析器 | 8h | 待开始 |
| 单元测试 | 8h | 待开始 |

**交付物：**
- 可从 SonarQube 获取问题
- 可生成修复建议

---

### Phase 4: Web Service API（1 周）

**目标：** 提供 REST API 供前端调用

**任务清单：**

| 任务 | 预估工时 | 状态 |
|------|---------|------|
| Web Service 框架搭建 | 4h | 待开始 |
| 生成修复 API | 8h | 待开始 |
| 获取配置 API | 4h | 待开始 |
| 应用修复 API | 8h | 待开始 |
| API 文档 | 4h | 待开始 |
| 集成测试 | 8h | 待开始 |

**交付物：**
- 完整的 REST API
- API 文档

---

### Phase 5: 前端 UI（2 周）

**目标：** 在 SonarQube UI 中展示修复建议

**任务清单：**

| 任务 | 预估工时 | 状态 |
|------|---------|------|
| 前端开发环境搭建 | 4h | 待开始 |
| 修复建议面板组件 | 12h | 待开始 |
| 代码差异展示组件 | 12h | 待开始 |
| 一键应用修复功能 | 8h | 待开始 |
| 样式设计 | 4h | 待开始 |
| 前端测试 | 8h | 待开始 |

**交付物：**
- 问题详情页显示修复建议
- 代码差异对比视图

---

### Phase 6: 测试与优化（1 周）

**目标：** 全面测试和性能优化

**任务清单：**

| 任务 | 预估工时 | 状态 |
|------|---------|------|
| 集成测试 | 12h | 待开始 |
| 性能测试 | 8h | 待开始 |
| 安全审计 | 8h | 待开始 |
| 文档完善 | 4h | 待开始 |
| 打包发布 | 4h | 待开始 |

**交付物：**
- 测试报告
- 发布版本

---

## 总工时估算

| 阶段 | 工时 | 说明 |
|------|------|------|
| Phase 1 | 36h | 基础框架 |
| Phase 2 | 72h | LLM 集成 |
| Phase 3 | 52h | 问题分析与修复 |
| Phase 4 | 36h | Web Service |
| Phase 5 | 48h | 前端 UI |
| Phase 6 | 36h | 测试优化 |
| **合计** | **280h** | 约 7 周 |

---

## 风险与应对

| 风险 | 可能性 | 影响 | 应对措施 |
|------|--------|------|---------|
| SonarQube API 变更 | 低 | 高 | 使用稳定的 Plugin API，锁定版本 |
| LLM API 不稳定 | 中 | 中 | 实现重试机制，支持多后端切换 |
| Token 成本超预算 | 中 | 中 | 实现缓存，限制请求频率 |
| 本地调试环境复杂 | 中 | 低 | 使用 Docker 简化环境搭建 |

---

## 开发环境要求

### 必需

- Java 11+
- Maven 3.6+
- SonarQube 10.x（开发测试用）

### 可选

- Docker（用于本地 SonarQube）
- Node.js 18+（前端开发）

---

## 下一步行动

1. **确认开发环境**：检查 Java、Maven 是否已安装
2. **创建 Maven 项目**：初始化项目结构
3. **实现插件入口**：验证插件可被 SonarQube 加载
4. **实现 OpenAI 客户端**：验证 LLM 调用
5. **逐步迭代**：按 Phase 推进

---

## 成功标准

### Phase 1 完成标准
- [ ] 插件可编译打包
- [ ] SonarQube 可加载插件
- [ ] 配置项在 SonarQube 设置页可见

### Phase 2 完成标准
- [ ] 可调用 OpenAI API 并获取响应
- [ ] 支持 Azure OpenAI
- [ ] 支持本地模型

### Phase 3 完成标准
- [ ] 可从 SonarQube 获取问题列表
- [ ] 可获取问题的源代码上下文
- [ ] 可生成有效的修复建议

### Phase 4 完成标准
- [ ] REST API 可正常调用
- [ ] API 文档完整

### Phase 5 完成标准
- [ ] 问题详情页显示修复建议
- [ ] 修复建议可正确展示
- [ ] 代码差异对比清晰

### Phase 6 完成标准
- [ ] 所有测试通过
- [ ] 无已知 Bug
- [ ] 文档完整
