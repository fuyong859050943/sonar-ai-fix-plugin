# SonarQube AI Fix Plugin - 用户手册

**版本**: 1.0.0  
**更新日期**: 2026-04-20

---

## 📖 目录

1. [简介](#简介)
2. [安装指南](#安装指南)
3. [配置说明](#配置说明)
4. [使用方法](#使用方法)
5. [API 参考](#api-参考)
6. [常见问题](#常见问题)
7. [故障排除](#故障排除)

---

## 简介

SonarQube AI Fix Plugin 为 SonarQube 社区版提供 AI 驱动的代码修复建议功能，类似于 SonarQube 企业版的 AI CodeFix。

### 核心功能

| 功能 | 说明 |
|------|------|
| **AI 修复建议** | 自动生成代码修复建议 |
| **问题解释** | AI 解释问题原因和影响 |
| **多 LLM 支持** | 支持 OpenAI、Azure OpenAI、本地模型 |
| **智能缓存** | 缓存修复建议，减少 API 调用 |
| **自定义规则** | AI 检测的代码质量规则 |

### 支持的 LLM 提供商

| 提供商 | 说明 | 模型 |
|--------|------|------|
| OpenAI | 官方 OpenAI API | GPT-4, GPT-4-turbo, GPT-3.5-turbo |
| Azure OpenAI | Azure 托管的 OpenAI | 所有 Azure OpenAI 模型 |
| 本地模型 | Ollama, LocalAI 等 | LLaMA, Mistral, Qwen 等 |

---

## 安装指南

### 前置要求

- SonarQube 9.9+ 或 SonarQube 10.x
- Java 17+
- LLM API 密钥（如 OpenAI API Key）

### 安装步骤

#### 方式一：手动安装

1. **下载插件 JAR 文件**

   从 [GitHub Releases](https://github.com/fuyong859050943/sonar-ai-fix-plugin/releases) 下载最新版本。

2. **复制到 SonarQube 插件目录**

   ```bash
   # Linux/macOS
   cp sonar-ai-fix-plugin-1.0.0.jar $SONARQUBE_HOME/extensions/plugins/
   
   # Windows
   copy sonar-ai-fix-plugin-1.0.0.jar %SONARQUBE_HOME%\extensions\plugins\
   ```

3. **重启 SonarQube**

   ```bash
   # Linux/macOS
   $SONARQUBE_HOME/bin/linux-x86-64/sonar.sh restart
   
   # Windows
   %SONARQUBE_HOME%\bin\windows-x86-64\SonarService.bat restart
   ```

#### 方式二：Docker 部署

```yaml
# docker-compose.yml
services:
  sonarqube:
    image: sonarqube:community
    volumes:
      - ./sonar-ai-fix-plugin-1.0.0.jar:/opt/sonarqube/extensions/plugins/sonar-ai-fix-plugin-1.0.0.jar
    ports:
      - "9000:9000"
```

```bash
docker-compose up -d
```

### 验证安装

1. 访问 SonarQube：http://localhost:9000
2. 登录后进入 **Administration > Plugins**
3. 确认 "AI Fix Plugin" 在已安装列表中

---

## 配置说明

### 配置入口

进入 **Administration > Configuration > AI Fix Settings**

### 基础配置

| 配置项 | 键名 | 默认值 | 说明 |
|--------|------|--------|------|
| 启用 AI 修复 | `sonar.ai.enabled` | `false` | 开启/关闭 AI 功能 |
| LLM 提供商 | `sonar.ai.provider` | `OPENAI` | 选择 LLM 提供商 |
| 模型名称 | `sonar.ai.model` | `gpt-4` | 使用的模型 |
| 最大 Token 数 | `sonar.ai.max-tokens` | `2000` | 响应最大长度 |
| 温度参数 | `sonar.ai.temperature` | `0.3` | 创造性程度 (0-1) |
| 缓存过期时间 | `sonar.ai.cache.expire-minutes` | `60` | 缓存有效期（分钟） |

### OpenAI 配置

| 配置项 | 键名 | 说明 |
|--------|------|------|
| API 密钥 | `sonar.ai.openai.api-key` | OpenAI API Key |
| API 基础 URL | `sonar.ai.openai.base-url` | 可选，用于代理 |
| 组织 ID | `sonar.ai.openai.organization` | 可选，OpenAI 组织 |

### Azure OpenAI 配置

| 配置项 | 键名 | 说明 |
|--------|------|------|
| API 密钥 | `sonar.ai.azure.api-key` | Azure API Key |
| 端点 | `sonar.ai.azure.endpoint` | Azure OpenAI 端点 |
| 部署名称 | `sonar.ai.azure.deployment-name` | 模型部署名称 |

### 本地模型配置

| 配置项 | 键名 | 说明 |
|--------|------|------|
| API 端点 | `sonar.ai.local.base-url` | 本地模型服务地址 |
| 模型名称 | `sonar.ai.local.model` | 模型标识 |

### 配置示例

#### OpenAI

```properties
sonar.ai.enabled=true
sonar.ai.provider=OPENAI
sonar.ai.model=gpt-4-turbo
sonar.ai.max-tokens=2000
sonar.ai.temperature=0.3
sonar.ai.openai.api-key=sk-xxxxx
```

#### Azure OpenAI

```properties
sonar.ai.enabled=true
sonar.ai.provider=AZURE
sonar.ai.azure.api-key=your-azure-key
sonar.ai.azure.endpoint=https://your-resource.openai.azure.com
sonar.ai.azure.deployment-name=gpt-4-deployment
```

#### Ollama (本地)

```properties
sonar.ai.enabled=true
sonar.ai.provider=LOCAL
sonar.ai.local.base-url=http://localhost:11434
sonar.ai.local.model=llama2
```

---

## 使用方法

### 1. 查看修复建议

1. 运行代码分析
2. 在问题列表中点击具体问题
3. 在问题详情页面找到 **AI Fix Suggestion** 区域
4. 查看修复建议、解释和步骤

### 2. 生成修复建议

**通过 UI**:

1. 在问题详情页面点击 **Generate Fix** 按钮
2. 等待 AI 生成修复建议
3. 查看修复代码、解释和步骤

**通过 API**:

```bash
curl -X POST "http://localhost:9000/api/ai-fix/generate?issueKey=AXxxxx" \
  -u admin:admin
```

### 3. 应用修复建议

> ⚠️ 此功能开发中，敬请期待

1. 在修复建议中点击 **Apply Fix**
2. 预览代码变更
3. 确认应用

### 4. 查看 AI 分析概览

进入 **More Tools > AI 修复概览**，查看所有项目的 AI 修复建议统计。

---

## API 参考

### 基础 URL

```
http://localhost:9000/api/ai-fix
```

### 认证

所有 API 需要 SonarQube 认证：
- 用户名密码（Basic Auth）
- 或 API Token

### 端点列表

#### 1. 获取插件状态

```http
GET /api/ai-fix/status
```

**响应示例**:

```json
{
  "enabled": true,
  "provider": "OPENAI",
  "model": "gpt-4",
  "cacheSize": 42,
  "cacheHitRate": 0.85
}
```

#### 2. 获取修复建议

```http
GET /api/ai-fix/suggestions?issueKey={issueKey}
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| issueKey | string | 是 | 问题标识 |

**响应示例**:

```json
{
  "suggestion": {
    "fixedCode": "logger.info(\"message\");",
    "explanation": "Use logger instead of System.out",
    "steps": ["Step 1: Import logger", "Step 2: Replace System.out"],
    "severity": "MAJOR"
  },
  "cached": true
}
```

#### 3. 生成修复建议

```http
POST /api/ai-fix/generate?issueKey={issueKey}
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| issueKey | string | 是 | 问题标识 |

**响应示例**:

```json
{
  "suggestion": {
    "fixedCode": "...",
    "explanation": "...",
    "steps": [...],
    "severity": "MAJOR"
  },
  "generated": true
}
```

#### 4. 获取缓存统计

```http
GET /api/ai-fix/cache/stats
```

**响应示例**:

```json
{
  "stats": "hitCount=100, missCount=20, loadSuccessCount=20",
  "size": 42,
  "hitRate": 0.83
}
```

#### 5. 应用修复建议

```http
POST /api/ai-fix/apply?suggestionId={suggestionId}
```

> ⚠️ 开发中

---

## 常见问题

### Q: 插件安装后看不到配置选项？

**A**: 确保：
1. 插件 JAR 文件在正确目录
2. SonarQube 已重启
3. 使用管理员账号登录

### Q: AI 修复建议生成很慢？

**A**: 可能原因：
1. LLM API 响应慢（可尝试换模型）
2. 网络延迟（考虑使用本地模型）
3. 代码片段太长

### Q: 如何使用本地模型节省成本？

**A**: 
1. 安装 Ollama：`curl https://ollama.ai/install.sh | sh`
2. 拉取模型：`ollama pull llama2`
3. 配置插件使用 LOCAL 提供商

### Q: 支持哪些编程语言？

**A**: 当前支持：
- Java
- JavaScript/TypeScript
- Python
- 未来将支持更多

---

## 故障排除

### 启用调试日志

编辑 `$SONARQUBE_HOME/conf/sonar.properties`:

```properties
sonar.log.level.ai-fix=DEBUG
```

### 查看日志

```bash
# Linux/macOS
tail -f $SONARQUBE_HOME/logs/sonar.log | grep "AI Fix"

# Docker
docker logs sonarqube | grep "AI Fix"
```

### 常见错误

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| `LLM client not available` | API 密钥未配置 | 在设置中配置 API 密钥 |
| `Rate limit exceeded` | LLM API 限流 | 等待或升级 API 套餐 |
| `Timeout` | LLM 响应超时 | 增加超时时间或使用更快的模型 |

---

## 技术支持

- **GitHub**: https://github.com/fuyong859050943/sonar-ai-fix-plugin
- **Issues**: https://github.com/fuyong859050943/sonar-ai-fix-plugin/issues

---

**文档版本**: 1.0.0 | **最后更新**: 2026-04-20
