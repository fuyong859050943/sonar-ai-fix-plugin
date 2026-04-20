# SonarQube AI Fix Plugin - API 文档

**版本**: 1.0.0  
**基础 URL**: `/api/ai-fix`

---

## 概述

AI Fix Plugin REST API 提供 AI 修复建议的获取、生成和管理功能。

### 认证

所有 API 端点需要 SonarQube 认证：

| 方式 | 说明 |
|------|------|
| Basic Auth | `username:password` |
| Bearer Token | `Authorization: Bearer {token}` |
| API Token | 传递 `sonar.login` 参数 |

### 响应格式

所有响应为 JSON 格式。

---

## 端点详情

### 1. 获取插件状态

获取 AI Fix 插件当前配置和运行状态。

```http
GET /api/ai-fix/status
```

#### 权限

- 需要登录

#### 参数

无

#### 响应

**成功响应 (200)**:

```json
{
  "enabled": true,
  "provider": "OPENAI",
  "model": "gpt-4-turbo",
  "cacheSize": 42,
  "cacheHitRate": 0.85
}
```

**响应字段**:

| 字段 | 类型 | 说明 |
|------|------|------|
| enabled | boolean | AI 功能是否启用 |
| provider | string | LLM 提供商 (OPENAI/AZURE/LOCAL) |
| model | string | 当前使用的模型 |
| cacheSize | integer | 缓存中的建议数量 |
| cacheHitRate | float | 缓存命中率 (0-1) |

#### 示例

```bash
curl -u admin:admin "http://localhost:9000/api/ai-fix/status"
```

---

### 2. 获取修复建议

从缓存中获取已生成的修复建议。

```http
GET /api/ai-fix/suggestions
```

#### 权限

- 需要登录
- 需要问题所在项目的浏览权限

#### 参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| issueKey | string | query | 是 | SonarQube 问题标识 |

#### 响应

**成功响应 (200)** - 有缓存建议:

```json
{
  "suggestion": {
    "id": "fix-abc123",
    "fixedCode": "private static final Logger LOG = LoggerFactory.getLogger(MyClass.class);\n\npublic void process() {\n    LOG.info(\"Processing started\");\n}",
    "explanation": "Use SLF4J Logger instead of System.out for proper logging in production environments.",
    "steps": [
      "Add SLF4J Logger field declaration",
      "Replace System.out.println with LOG.info",
      "Configure logging level as needed"
    ],
    "severity": "MAJOR",
    "createdAt": "2026-04-20T10:30:00Z"
  },
  "cached": true
}
```

**成功响应 (200)** - 无缓存建议:

```json
{
  "suggestion": null,
  "cached": false
}
```

**错误响应 (404)**:

```json
{
  "error": "Issue not found: AX-invalid-key"
}
```

#### 示例

```bash
curl -u admin:admin "http://localhost:9000/api/ai-fix/suggestions?issueKey=AXa1b2c3d4"
```

---

### 3. 生成修复建议

调用 LLM 生成新的修复建议。

```http
POST /api/ai-fix/generate
```

#### 权限

- 需要登录
- 需要问题所在项目的分析权限

#### 参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| issueKey | string | query | 是 | SonarQube 问题标识 |

#### 响应

**成功响应 (200)**:

```json
{
  "suggestion": {
    "id": "fix-xyz789",
    "fixedCode": "// Fixed code here\nlogger.info(\"message\");",
    "explanation": "Explanation of the fix...",
    "steps": [
      "Step 1: Description",
      "Step 2: Description"
    ],
    "severity": "MAJOR",
    "createdAt": "2026-04-20T10:35:00Z"
  },
  "generated": true
}
```

**错误响应 (503)** - LLM 不可用:

```json
{
  "error": "LLM client not available. Please configure API key in Administration > AI Fix Settings."
}
```

**错误响应 (429)** - 速率限制:

```json
{
  "error": "Rate limit exceeded. Please try again later."
}
```

#### 示例

```bash
curl -X POST -u admin:admin "http://localhost:9000/api/ai-fix/generate?issueKey=AXa1b2c3d4"
```

---

### 4. 应用修复建议

将修复建议应用到代码（功能开发中）。

```http
POST /api/ai-fix/apply
```

#### 权限

- 需要登录
- 需要项目写入权限

#### 参数

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| suggestionId | string | query | 是 | 修复建议标识 |

#### 响应

**成功响应 (200)**:

```json
{
  "applied": true,
  "message": "Fix applied successfully",
  "filePath": "src/main/java/com/example/MyClass.java",
  "changeId": "change-123"
}
```

**当前响应 (200)** - 功能未完成:

```json
{
  "applied": false,
  "message": "Apply fix feature coming soon"
}
```

#### 示例

```bash
curl -X POST -u admin:admin "http://localhost:9000/api/ai-fix/apply?suggestionId=fix-abc123"
```

---

### 5. 获取缓存统计

获取修复建议缓存的统计信息。

```http
GET /api/ai-fix/cache/stats
```

#### 权限

- 需要管理员权限

#### 参数

无

#### 响应

**成功响应 (200)**:

```json
{
  "stats": {
    "hitCount": 150,
    "missCount": 30,
    "loadSuccessCount": 28,
    "loadFailureCount": 2,
    "totalLoadTime": 45000,
    "evictionCount": 5
  },
  "size": 42,
  "hitRate": 0.833
}
```

**响应字段**:

| 字段 | 类型 | 说明 |
|------|------|------|
| stats.hitCount | long | 缓存命中次数 |
| stats.missCount | long | 缓存未命中次数 |
| stats.loadSuccessCount | long | 成功加载次数 |
| stats.loadFailureCount | long | 加载失败次数 |
| stats.totalLoadTime | long | 总加载时间 (ms) |
| stats.evictionCount | long | 驱逐次数 |
| size | int | 当前缓存大小 |
| hitRate | float | 命中率 |

#### 示例

```bash
curl -u admin:admin "http://localhost:9000/api/ai-fix/cache/stats"
```

---

## 错误码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 429 | 速率限制 |
| 500 | 服务器内部错误 |
| 503 | 服务不可用 (LLM 未配置) |

---

## 速率限制

| 场景 | 限制 |
|------|------|
| 获取建议 | 100 次/分钟 |
| 生成建议 | 20 次/分钟 |
| 其他 | 200 次/分钟 |

---

## 变更日志

### v1.0.0 (2026-04-20)

- 初始版本
- 支持获取/生成修复建议
- 支持缓存统计
- 支持 OpenAI/Azure/本地 LLM

---

**API 版本**: 1.0.0 | **最后更新**: 2026-04-20
