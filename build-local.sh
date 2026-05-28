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
echo "PackClaw 本地多架构构建脚本"
echo "=========================================="
echo ""
echo "将构建 linux/amd64 和 linux/arm64 架构的镜像（仅本地）"
echo ""

# 步骤 1: 创建/使用 buildx builder
echo "[1/4] 配置 buildx 多架构构建器..."
if ! docker buildx ls | grep -q "multiarch"; then
    docker buildx create --name multiarch --driver docker-container --use
else
    docker buildx use multiarch
fi
docker buildx inspect --bootstrap
echo "✓ 构建器配置完成"

# 步骤 2: Maven 构建后端
echo ""
echo "[2/4] 开始 Maven 构建后端..."
mvn clean package -DskipTests
echo "✓ Maven 构建完成"

# 步骤 3: 构建后端 Docker 镜像 (多架构，仅加载到本地)
echo ""
echo "[3/4] 构建后端 Docker 镜像 (amd64 + arm64)..."
docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t ${BACKEND_IMAGE_NAME}:${IMAGE_TAG} \
    --load \
    .
echo "✓ 后端镜像构建完成"

# 步骤 4: 构建前端 Docker 镜像 (多架构，仅加载到本地)
echo ""
echo "[4/4] 构建前端 Docker 镜像 (amd64 + arm64)..."
docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t ${FRONTEND_IMAGE_NAME}:${IMAGE_TAG} \
    --load \
    ./frontend
echo "✓ 前端镜像构建完成"

echo ""
echo "=========================================="
echo "本地构建完成!"
echo "后端镜像: ${BACKEND_IMAGE_NAME}:${IMAGE_TAG}"
echo "前端镜像: ${FRONTEND_IMAGE_NAME}:${IMAGE_TAG}"
echo ""
echo "支持架构: linux/amd64, linux/arm64"
echo "=========================================="
echo ""
echo "推送到 Docker Hub:"
echo "  docker tag ${BACKEND_IMAGE_NAME}:${IMAGE_TAG} ${BACKEND_FULL_IMAGE}"
echo "  docker push ${BACKEND_FULL_IMAGE}"
echo ""
echo "  docker tag ${FRONTEND_IMAGE_NAME}:${IMAGE_TAG} ${FRONTEND_FULL_IMAGE}"
echo "  docker push ${FRONTEND_FULL_IMAGE}"
echo ""
echo "或者使用代理推送:"
echo "  export HTTPS_PROXY=http://http.docker.internal:3128"
echo "  docker push ${BACKEND_FULL_IMAGE}"
echo "  docker push ${FRONTEND_FULL_IMAGE}"
echo ""
