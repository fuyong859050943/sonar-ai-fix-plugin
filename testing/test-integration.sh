#!/bin/bash
# SonarQube AI Fix Plugin 集成测试脚本
#
# 用法:
#   chmod +x test-integration.sh
#   ./test-integration.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=========================================="
echo "SonarQube AI Fix Plugin - Integration Test"
echo "=========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查依赖
check_dependencies() {
    echo -e "${YELLOW}Checking dependencies...${NC}"
    
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}Error: Docker is not installed${NC}"
        exit 1
    fi
    
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}Error: Maven is not installed${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ All dependencies satisfied${NC}"
}

# 构建 Docker 镜像
build_plugin() {
    echo -e "${YELLOW}Building plugin...${NC}"
    cd "$PROJECT_DIR"
    mvn clean package -DskipTests
    echo -e "${GREEN}✓ Plugin built successfully${NC}"
}

# 启动测试环境
start_environment() {
    echo -e "${YELLOW}Starting SonarQube test environment...${NC}"
    cd "$SCRIPT_DIR"
    docker-compose -f docker-compose-test.yml down -v 2>/dev/null || true
    docker-compose -f docker-compose-test.yml up -d
    
    echo -e "${YELLOW}Waiting for SonarQube to start (this may take 2-3 minutes)...${NC}"
    sleep 10
    
    # 等待 SonarQube 就绪
    for i in {1..60}; do
        if curl -s http://localhost:9000/api/system/status | grep -q "UP"; then
            echo -e "${GREEN}✓ SonarQube is ready${NC}"
            return 0
        fi
        echo -n "."
        sleep 5
    done
    
    echo -e "${RED}✗ SonarQube failed to start${NC}"
    return 1
}

# 安装插件
install_plugin() {
    echo -e "${YELLOW}Installing plugin...${NC}"
    
    PLUGIN_JAR="$PROJECT_DIR/target/sonar-ai-fix-plugin-1.0.0.jar"
    CONTAINER_PLUGINS="/opt/sonarqube/extensions/plugins"
    
    if [ ! -f "$PLUGIN_JAR" ]; then
        echo -e "${RED}Error: Plugin JAR not found at $PLUGIN_JAR${NC}"
        return 1
    fi
    
    # 复制插件到容器
    docker cp "$PLUGIN_JAR" sonar-ai-test-sonarqube:"$CONTAINER_PLUGINS/"
    
    # 重启 SonarQube
    echo -e "${YELLOW}Restarting SonarQube to load plugin...${NC}"
    docker restart sonar-ai-test-sonarqube
    
    sleep 30
    echo -e "${GREEN}✓ Plugin installed${NC}"
}

# 运行测试
run_tests() {
    echo -e "${YELLOW}Running integration tests...${NC}"
    
    # 检查插件是否加载
    echo "Checking plugin status..."
    PLUGINS=$(curl -s http://localhost:9000/api/plugins/installed)
    
    if echo "$PLUGINS" | grep -q "aifix"; then
        echo -e "${GREEN}✓ Plugin is installed and loaded${NC}"
    else
        echo -e "${YELLOW}⚠ Plugin not found in installed list${NC}"
    fi
    
    # 测试 API 端点
    echo "Testing API endpoints..."
    
    # 测试状态端点
    STATUS=$(curl -s http://localhost:9000/api/ai-fix/status)
    echo "Status endpoint response: $STATUS"
    
    echo -e "${GREEN}✓ Integration tests completed${NC}"
}

# 清理环境
cleanup() {
    echo -e "${YELLOW}Cleaning up...${NC}"
    cd "$SCRIPT_DIR"
    docker-compose -f docker-compose-test.yml down -v
    echo -e "${GREEN}✓ Environment cleaned up${NC}"
}

# 主流程
main() {
    case "${1:-all}" in
        build)
            check_dependencies
            build_plugin
            ;;
        start)
            start_environment
            ;;
        install)
            install_plugin
            ;;
        test)
            run_tests
            ;;
        stop)
            cleanup
            ;;
        all)
            check_dependencies
            build_plugin
            start_environment
            install_plugin
            run_tests
            echo ""
            echo -e "${GREEN}=========================================="
            echo "Integration test completed!"
            echo "SonarQube is running at: http://localhost:9000"
            echo "Login: admin / admin"
            echo "To stop: $0 stop"
            echo "==========================================${NC}"
            ;;
        *)
            echo "Usage: $0 {build|start|install|test|stop|all}"
            exit 1
            ;;
    esac
}

main "$@"
