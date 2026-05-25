#!/bin/bash

set -e

# 配置变量
HARBOR_REGISTRY="114.214.255.82:18080"
HARBOR_PROJECT="public"
BACKEND_IMAGE_NAME="packclaw"
FRONTEND_IMAGE_NAME="packclaw-frontend"
IMAGE_TAG="v0.01.amd64"
ARCH="amd64"
BACKEND_FULL_IMAGE="${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${BACKEND_IMAGE_NAME}:${IMAGE_TAG}"
FRONTEND_FULL_IMAGE="${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${FRONTEND_IMAGE_NAME}:${IMAGE_TAG}"

echo "=========================================="
echo "PackClaw x86 架构 Docker 构建和推送脚本"
echo "=========================================="

# 步骤 1: Maven 构建后端
echo ""
echo "[1/6] 开始 Maven 构建后端..."
mvn clean package -DskipTests
echo "✓ Maven 构建完成"

# 步骤 2: 构建后端 Docker 镜像 (x86)
echo ""
echo "[2/6] 开始构建后端 Docker 镜像 (x86)..."
docker buildx build --platform linux/amd64 -t ${BACKEND_IMAGE_NAME}:${IMAGE_TAG} .
echo "✓ 后端 Docker 镜像构建完成"

# 步骤 3: 标记并推送后端镜像
echo ""
echo "[3/6] 标记并推送后端镜像..."
docker tag ${BACKEND_IMAGE_NAME}:${IMAGE_TAG} ${BACKEND_FULL_IMAGE}
docker push ${BACKEND_FULL_IMAGE}
echo "✓ 后端镜像推送完成: ${BACKEND_FULL_IMAGE}"

# 步骤 4: 构建前端 Docker 镜像 (x86)
echo ""
echo "[4/6] 开始构建前端 Docker 镜像 (x86)..."
docker buildx build --platform linux/amd64 -t ${FRONTEND_IMAGE_NAME}:${IMAGE_TAG} ./frontend
echo "✓ 前端 Docker 镜像构建完成"

# 步骤 5: 标记并推送前端镜像
echo ""
echo "[5/6] 标记并推送前端镜像..."
docker tag ${FRONTEND_IMAGE_NAME}:${IMAGE_TAG} ${FRONTEND_FULL_IMAGE}
docker push ${FRONTEND_FULL_IMAGE}
echo "✓ 前端镜像推送完成: ${FRONTEND_FULL_IMAGE}"

# 步骤 6: 更新 docker-compose.yml 中的镜像标签
echo ""
echo "[6/6] 更新 docker-compose.yml..."
sed -i.bak "s|image: ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/packclaw:.*|image: ${BACKEND_FULL_IMAGE}|" docker-compose.yml
rm -f docker-compose.yml.bak
echo "✓ docker-compose.yml 更新完成"

echo ""
echo "=========================================="
echo "x86 架构构建和推送完成!"
echo "后端镜像: ${BACKEND_FULL_IMAGE}"
echo "前端镜像: ${FRONTEND_FULL_IMAGE}"
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
