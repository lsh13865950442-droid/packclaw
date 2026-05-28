#!/bin/bash

set -e

# 配置变量
DOCKERHUB_USER="lush196"
BACKEND_IMAGE_NAME="packclaw"
FRONTEND_IMAGE_NAME="packclaw-frontend"
IMAGE_TAG="latest"

BACKEND_FULL_IMAGE="${DOCKERHUB_USER}/${BACKEND_IMAGE_NAME}:${IMAGE_TAG}"
FRONTEND_FULL_IMAGE="${DOCKERHUB_USER}/${FRONTEND_IMAGE_NAME}:${IMAGE_TAG}"

echo "=========================================="
echo "PackClaw Docker Hub 多架构构建和推送脚本"
echo "=========================================="
echo ""
echo "将构建 linux/amd64 和 linux/arm64 架构的镜像"
echo ""

# 步骤 0: 检查 Docker Hub 登录状态
echo "[0/6] 检查 Docker Hub 登录状态..."
if [ ! -f ~/.docker/config.json ] || ! grep -q "index.docker.io" ~/.docker/config.json 2>/dev/null; then
    echo "⚠️  未登录 Docker Hub，请先登录:"
    echo "   docker login"
    echo "   输入用户名: ${DOCKERHUB_USER}"
    exit 1
fi
echo "✓ Docker Hub 已登录"

# 步骤 1: 创建/使用 buildx builder
echo ""
echo "[1/6] 配置 buildx 多架构构建器..."
if ! docker buildx ls | grep -q "multiarch"; then
    docker buildx create --name multiarch --driver docker-container --use
else
    docker buildx use multiarch
fi
docker buildx inspect --bootstrap
echo "✓ 构建器配置完成"

# 步骤 2: Maven 构建后端
echo ""
echo "[2/6] 开始 Maven 构建后端..."
mvn clean package -DskipTests
echo "✓ Maven 构建完成"

# 步骤 3: 构建并推送后端 Docker 镜像 (多架构)
echo ""
echo "[3/6] 构建并推送后端 Docker 镜像 (amd64 + arm64)..."
docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t ${BACKEND_FULL_IMAGE} \
    --push \
    .
echo "✓ 后端镜像推送完成: ${BACKEND_FULL_IMAGE}"

# 步骤 4: 构建并推送前端 Docker 镜像 (多架构)
echo ""
echo "[4/6] 构建并推送前端 Docker 镜像 (amd64 + arm64)..."
docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t ${FRONTEND_FULL_IMAGE} \
    --push \
    ./frontend
echo "✓ 前端镜像推送完成: ${FRONTEND_FULL_IMAGE}"

# 步骤 5: 更新 docker-compose.yml
echo ""
echo "[5/6] 更新 docker-compose.yml..."
sed -i.bak "s|image: .*/packclaw:.*|image: ${BACKEND_FULL_IMAGE}|" docker-compose.yml
sed -i.bak "s|image: .*/packclaw-frontend:.*|image: ${FRONTEND_FULL_IMAGE}|" docker-compose.yml
rm -f docker-compose.yml.bak
echo "✓ docker-compose.yml 更新完成"

echo ""
echo "=========================================="
echo "多架构构建和推送完成!"
echo "后端镜像: ${BACKEND_FULL_IMAGE}"
echo "前端镜像: ${FRONTEND_FULL_IMAGE}"
echo ""
echo "支持架构: linux/amd64, linux/arm64"
echo "=========================================="
echo ""
echo "使用 docker-compose 启动服务:"
echo "  docker-compose up -d"
echo ""
echo "查看日志:"
echo "  docker-compose logs -f"
echo ""
echo "停止服务:"
echo "  docker-compose down"
echo ""
