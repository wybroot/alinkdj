#!/bin/bash
# ==============================================================================
# 红河数据产业集团有限公司党总支 · 智慧党建数字化管理系统
# Linux Docker Compose 一键生产部署脚本
# ==============================================================================

set -e

echo "=============================================================================="
echo "  红河数据产业集团有限公司党总支 · 智慧党建管理系统 Docker 部署脚本"
echo "=============================================================================="

# 1. 检查 Docker 及 Docker Compose
if ! command -v docker &> /dev/null; then
    echo "❌ [错误] 未检测到 Docker 命令，请先安装 Docker: curl -fsSL https://get.docker.com | bash"
    exit 1
fi

DOCKER_COMPOSE_CMD=""
if docker compose version &> /dev/null; then
    DOCKER_COMPOSE_CMD="docker compose"
elif command -v docker-compose &> /dev/null; then
    DOCKER_COMPOSE_CMD="docker-compose"
else
    echo "❌ [错误] 未检测到 docker-compose 或 docker compose 插件，请先安装！"
    exit 1
fi

echo "🚀 [1/3] 正在构建 Docker 镜像 (PostgreSQL 15 + Java 21 Spring Boot 3 + Vue 3 Nginx)..."
$DOCKER_COMPOSE_CMD build

echo "📦 [2/3] 正在一键启动所有容器集群 (后台守护进程模式)..."
$DOCKER_COMPOSE_CMD up -d

echo ""
echo "🔍 [3/3] 服务运行状态检查："
$DOCKER_COMPOSE_CMD ps

echo ""
echo "=============================================================================="
echo "  ✅ 部署成功！系统访问地址如下："
echo ""
echo "  ★ 业务平台与大屏端： http://服务器IP:80"
echo "  ★ 后端 RESTful API： http://服务器IP:8080/api"
echo "  ★ 数据库连接端口：   服务器IP:5432 (库: honghe_party_db, 用户: party_user)"
echo ""
echo "  常用维护运维指令："
echo "  - 查看实时日志: $DOCKER_COMPOSE_CMD logs -f"
echo "  - 停止所有服务: $DOCKER_COMPOSE_CMD down"
echo "  - 重启所有服务: $DOCKER_COMPOSE_CMD restart"
echo "=============================================================================="
