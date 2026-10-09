@echo off
chcp 65001 >nul
echo ==============================================================================
echo   红河数据产业集团有限公司党总支 · 智慧党建数字化管理系统
echo   Docker Compose 一键容器化构建与部署脚本
echo ==============================================================================
echo.

:: 检查 Docker 是否安装
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Docker 环境，请先安装 Docker Desktop 或 Docker Engine！
    pause
    exit /b 1
)

echo [1/3] 正在拉取依赖并开始构建 Docker 镜像 (PostgreSQL + Spring Boot 3 + Vue 3)...
docker-compose build

if %errorlevel% neq 0 (
    echo [错误] 镜像构建失败，请检查 Docker 日志与网络！
    pause
    exit /b 1
)

echo [2/3] 正在一键启动所有容器服务 (后台运行模式)...
docker-compose up -d

if %errorlevel% neq 0 (
    echo [错误] 服务启动异常！
    pause
    exit /b 1
)

echo.
echo [3/3] 容器启动完成！服务状态如下：
docker-compose ps

echo.
echo ==============================================================================
echo   服务一键启动成功！你可以通过以下地址访问系统：
echo.
echo   ★ 智慧党建业务平台与大屏： http://localhost
echo   ★ 后端 RESTful API 接口：   http://localhost:8080/api
echo   ★ PostgreSQL 数据库端口：   localhost:5432 (库名: honghe_party_db)
echo.
echo   常用管理命令：
echo   - 查看实时日志: docker-compose logs -f
echo   - 停止所有服务: docker-compose down
echo   - 重启所有服务: docker-compose restart
echo ==============================================================================
echo.
pause
