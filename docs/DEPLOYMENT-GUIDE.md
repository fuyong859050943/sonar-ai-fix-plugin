# SonarQube AI Fix Plugin - 部署指南

**版本**: 1.0.0  
**更新日期**: 2026-04-20

---

## 📋 目录

1. [部署概述](#部署概述)
2. [环境要求](#环境要求)
3. [部署方式](#部署方式)
4. [配置指南](#配置指南)
5. [生产环境部署](#生产环境部署)
6. [监控与运维](#监控与运维)
7. [安全配置](#安全配置)

---

## 部署概述

### 部署架构

```
┌─────────────────────────────────────────────────────────────┐
│                      SonarQube Server                        │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    AI Fix Plugin                         │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │ │
│  │  │   Sensor    │  │  REST API   │  │   Cache     │      │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
              ┌───────────────────────────────┐
              │        LLM Provider           │
              │  (OpenAI / Azure / Local)     │
              └───────────────────────────────┘
```

### 部署选项对比

| 方式 | 适用场景 | 复杂度 | 推荐度 |
|------|----------|--------|--------|
| 手动安装 | 小团队、测试环境 | ⭐ | ⭐⭐⭐ |
| Docker | 中小团队、快速部署 | ⭐⭐ | ⭐⭐⭐⭐ |
| Kubernetes | 大型企业、生产环境 | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

---

## 环境要求

### SonarQube 要求

| 组件 | 最低要求 | 推荐配置 |
|------|----------|----------|
| SonarQube 版本 | 9.9 LTS | 10.x |
| Java | 17 | 17 |
| 内存 | 2GB | 4GB+ |
| CPU | 2 核 | 4 核+ |

### LLM 提供商要求

#### OpenAI

- API Key（从 https://platform.openai.com 获取）
- 网络可访问 `api.openai.com`
- 预算：约 $0.03-0.06 / 1000 tokens

#### Azure OpenAI

- Azure 订阅
- Azure OpenAI 资源
- 部署的模型

#### 本地模型 (Ollama)

- 服务器：8GB+ 内存
- GPU（可选，推荐）
- 模型文件：约 4-10GB

---

## 部署方式

### 方式一：手动安装

#### 步骤 1：下载插件

```bash
# 从 GitHub 下载
wget https://github.com/fuyong859050943/sonar-ai-fix-plugin/releases/download/v1.0.0/sonar-ai-fix-plugin-1.0.0.jar

# 或从源码构建
git clone https://github.com/fuyong859050943/sonar-ai-fix-plugin.git
cd sonar-ai-fix-plugin
mvn clean package
```

#### 步骤 2：安装插件

```bash
# 复制到 SonarQube 插件目录
cp target/sonar-ai-fix-plugin-1.0.0.jar $SONARQUBE_HOME/extensions/plugins/

# 设置权限
chmod 644 $SONARQUBE_HOME/extensions/plugins/sonar-ai-fix-plugin-1.0.0.jar
```

#### 步骤 3：重启 SonarQube

```bash
# Linux
$SONARQUBE_HOME/bin/linux-x86-64/sonar.sh restart

# Windows
%SONARQUBE_HOME%\bin\windows-x86-64\SonarService.bat restart
```

---

### 方式二：Docker 部署

#### 单容器部署

```bash
# 拉取镜像
docker pull sonarqube:community

# 运行容器（带插件）
docker run -d \
  --name sonarqube \
  -p 9000:9000 \
  -v $(pwd)/sonar-ai-fix-plugin-1.0.0.jar:/opt/sonarqube/extensions/plugins/sonar-ai-fix-plugin-1.0.0.jar \
  -v sonarqube_data:/opt/sonarqube/data \
  -v sonarqube_logs:/opt/sonarqube/logs \
  sonarqube:community
```

#### Docker Compose 部署

```yaml
# docker-compose.yml
version: "3.8"

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_USER: sonar
      POSTGRES_PASSWORD: sonar
      POSTGRES_DB: sonar
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U sonar"]
      interval: 10s
      timeout: 5s
      retries: 5

  sonarqube:
    image: sonarqube:community
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SONAR_JDBC_URL: jdbc:postgresql://postgres:5432/sonar
      SONAR_JDBC_USERNAME: sonar
      SONAR_JDBC_PASSWORD: sonar
      SONAR_ES_BOOTSTRAP_CHECKS_DISABLE: "true"
    volumes:
      - ./sonar-ai-fix-plugin-1.0.0.jar:/opt/sonarqube/extensions/plugins/sonar-ai-fix-plugin-1.0.0.jar
      - sonarqube_data:/opt/sonarqube/data
      - sonarqube_extensions:/opt/sonarqube/extensions
      - sonarqube_logs:/opt/sonarqube/logs
    ports:
      - "9000:9000"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/api/system/status"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 120s

volumes:
  postgres_data:
  sonarqube_data:
  sonarqube_extensions:
  sonarqube_logs:
```

```bash
# 启动
docker-compose up -d

# 查看日志
docker-compose logs -f sonarqube
```

---

### 方式三：Kubernetes 部署

#### 部署清单

```yaml
# k8s-deployment.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: sonarqube-config
data:
  SONAR_JDBC_URL: "jdbc:postgresql://postgres:5432/sonar"
  SONAR_JDBC_USERNAME: "sonar"
  SONAR_ES_BOOTSTRAP_CHECKS_DISABLE: "true"
---
apiVersion: v1
kind: Secret
metadata:
  name: sonarqube-secrets
type: Opaque
stringData:
  SONAR_JDBC_PASSWORD: "your-password"
  SONAR_AI_OPENAI_API_KEY: "your-openai-key"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sonarqube
spec:
  replicas: 1
  selector:
    matchLabels:
      app: sonarqube
  template:
    metadata:
      labels:
        app: sonarqube
    spec:
      initContainers:
        - name: init-plugins
          image: busybox
          command: ['sh', '-c', 'cp /plugins-src/*.jar /plugins/']
          volumeMounts:
            - name: plugin-src
              mountPath: /plugins-src
            - name: plugins
              mountPath: /plugins
      containers:
        - name: sonarqube
          image: sonarqube:community
          ports:
            - containerPort: 9000
          envFrom:
            - configMapRef:
                name: sonarqube-config
            - secretRef:
                name: sonarqube-secrets
          volumeMounts:
            - name: plugins
              mountPath: /opt/sonarqube/extensions/plugins
            - name: data
              mountPath: /opt/sonarqube/data
          resources:
            requests:
              memory: "2Gi"
              cpu: "1"
            limits:
              memory: "4Gi"
              cpu: "2"
          livenessProbe:
            httpGet:
              path: /api/system/status
              port: 9000
            initialDelaySeconds: 120
            periodSeconds: 30
      volumes:
        - name: plugin-src
          configMap:
            name: sonar-plugins
        - name: plugins
          emptyDir: {}
        - name: data
          persistentVolumeClaim:
            claimName: sonarqube-data
---
apiVersion: v1
kind: Service
metadata:
  name: sonarqube
spec:
  type: LoadBalancer
  ports:
    - port: 80
      targetPort: 9000
  selector:
    app: sonarqube
```

```bash
# 部署
kubectl apply -f k8s-deployment.yaml

# 检查状态
kubectl get pods -l app=sonarqube
```

---

## 配置指南

### 初始配置

1. **登录 SonarQube**

   访问 http://localhost:9000，使用 `admin/admin` 登录

2. **配置 AI Fix 插件**

   进入 **Administration > Configuration > AI Fix Settings**

3. **配置 LLM 提供商**

   ```properties
   # 启用插件
   sonar.ai.enabled=true
   
   # 选择提供商
   sonar.ai.provider=OPENAI
   
   # 配置 API 密钥
   sonar.ai.openai.api-key=sk-xxxxx
   
   # 选择模型
   sonar.ai.model=gpt-4-turbo
   ```

4. **保存配置并重启**

### 配置文件方式

编辑 `$SONARQUBE_HOME/conf/sonar.properties`:

```properties
# AI Fix Plugin 配置
sonar.ai.enabled=true
sonar.ai.provider=OPENAI
sonar.ai.model=gpt-4-turbo
sonar.ai.max-tokens=2000
sonar.ai.temperature=0.3
sonar.ai.cache.expire-minutes=60

# OpenAI 配置
sonar.ai.openai.api-key=sk-xxxxx
sonar.ai.openai.base-url=https://api.openai.com/v1
```

---

## 生产环境部署

### 性能调优

```properties
# sonar.properties

# JVM 内存
sonar.web.javaOpts=-Xms2g -Xmx4g -XX:+UseG1GC
sonar.search.javaOpts=-Xms1g -Xmx2g

# 数据库连接池
sonar.jdbc.maxActive=50
sonar.jdbc.maxIdle=10

# AI 插件优化
sonar.ai.cache.expire-minutes=120
sonar.ai.max-tokens=2000
```

### 高可用配置

```yaml
# 使用外部 PostgreSQL + 负载均衡
services:
  sonarqube-1:
    image: sonarqube:community
    environment:
      SONAR_JDBC_URL: jdbc:postgresql://postgres-lb:5432/sonar
    volumes:
      - plugins:/opt/sonarqube/extensions/plugins
      # 数据目录使用共享存储或每个实例独立
  
  sonarqube-2:
    image: sonarqube:community
    environment:
      SONAR_JDBC_URL: jdbc:postgresql://postgres-lb:5432/sonar
    volumes:
      - plugins:/opt/sonarqube/extensions/plugins

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
```

---

## 监控与运维

### 健康检查

```bash
# 检查 SonarQube 状态
curl http://localhost:9000/api/system/status

# 检查插件状态
curl -u admin:admin http://localhost:9000/api/ai-fix/status
```

### 日志配置

```properties
# 启用 AI 插件调试日志
sonar.log.level.ai-fix=DEBUG
```

### 监控指标

| 指标 | 说明 | 阈值 |
|------|------|------|
| 缓存命中率 | 修复建议缓存命中比例 | > 70% |
| API 响应时间 | LLM API 响应时间 | < 10s |
| 错误率 | API 调用错误比例 | < 1% |

### Prometheus 集成

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'sonarqube'
    metrics_path: '/api/monitoring/metrics'
    static_configs:
      - targets: ['sonarqube:9000']
```

---

## 安全配置

### API 密钥安全

**推荐方式：使用环境变量**

```bash
# 启动时传入
docker run -d \
  -e SONAR_AI_OPENAI_API_KEY=$OPENAI_API_KEY \
  sonarqube:community
```

**或使用 Kubernetes Secret**

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: ai-fix-secrets
type: Opaque
stringData:
  api-key: "sk-xxxxx"
```

### 网络安全

```yaml
# 限制 API 访问
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: sonarqube-network-policy
spec:
  podSelector:
    matchLabels:
      app: sonarqube
  policyTypes:
    - Egress
  egress:
    - to:
        - ipBlock:
            cidr: 0.0.0.0/0
      ports:
        - protocol: TCP
          port: 443  # HTTPS for OpenAI API
```

### 权限控制

在 SonarQube 中配置：

1. **全局权限**
   - Administration > Security > Global Permissions
   - 仅管理员可以配置 AI 设置

2. **项目权限**
   - 项目设置 > 权限
   - 控制 AI 修复建议的访问权限

---

## 故障排除

### 插件无法加载

```bash
# 检查日志
tail -f $SONARQUBE_HOME/logs/sonar.log | grep "AI Fix"

# 常见问题
# 1. Java 版本不匹配 - 确保使用 Java 17
# 2. 依赖冲突 - 检查是否有其他插件冲突
# 3. 权限问题 - 确保 sonarqube 用户可读插件文件
```

### LLM 连接失败

```bash
# 测试网络连接
curl -v https://api.openai.com/v1/models

# 检查 API 密钥
curl -H "Authorization: Bearer $OPENAI_API_KEY" \
  https://api.openai.com/v1/models
```

---

**文档版本**: 1.0.0 | **最后更新**: 2026-04-20
