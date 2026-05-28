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
echo "推送镜像到 Docker Hub"
echo "=========================================="
echo ""

# 检查本地镜像是否存在
if ! docker images | grep -q "${BACKEND_IMAGE_NAME}.*${IMAGE_TAG}"; then
    echo "❌ 未找到本地镜像 ${BACKEND_IMAGE_NAME}:${IMAGE_TAG}"
    echo "请先运行 ./build-local.sh 构建镜像"
    exit 1
fi

if ! docker images | grep -q "${FRONTEND_IMAGE_NAME}.*${IMAGE_TAG}"; then
    echo "❌ 未找到本地镜像 ${FRONTEND_IMAGE_NAME}:${IMAGE_TAG}"
    echo "请先运行 ./build-local.sh 构建镜像"
    exit 1
fi

# 标记镜像
echo "[1/4] 标记后端镜像..."
docker tag ${BACKEND_IMAGE_NAME}:${IMAGE_TAG} ${BACKEND_FULL_IMAGE}
echo "✓ 后端镜像标记完成: ${BACKEND_FULL_IMAGE}"

echo ""
echo "[2/4] 标记前端镜像..."
docker tag ${FRONTEND_IMAGE_NAME}:${IMAGE_TAG} ${FRONTEND_FULL_IMAGE}
echo "✓ 前端镜像标记完成: ${FRONTEND_FULL_IMAGE}"

# 推送镜像
echo ""
echo "[3/4] 推送后端镜像到 Docker Hub..."
docker push ${BACKEND_FULL_IMAGE}
echo "✓ 后端镜像推送完成"

echo ""
echo "[4/4] 推送前端镜像到 Docker Hub..."
docker push ${FRONTEND_FULL_IMAGE}
echo "✓ 前端镜像推送完成"

# 更新 docker-compose.yml
echo ""
echo "更新 docker-compose.yml..."
sed -i.bak "s|image: .*/packclaw:.*|image: ${BACKEND_FULL_IMAGE}|" docker-compose.yml
sed -i.bak "s|image: .*/packclaw-frontend:.*|image: ${FRONTEND_FULL_IMAGE}|" docker-compose.yml
rm -f docker-compose.yml.bak
echo "✓ docker-compose.yml 更新完成"

echo ""
echo "=========================================="
echo "推送完成!"
echo "后端镜像: ${BACKEND_FULL_IMAGE}"
echo "前端镜像: ${FRONTEND_FULL_IMAGE}"
echo "=========================================="
echo ""
echo "使用 docker-compose 启动服务:"
echo "  docker-compose up -d"
echo ""
