# PackClaw Docker 部署指南

## 📦 项目结构

```
PackClaw/
├── frontend/              # 前端项目
│   ├── Dockerfile        # 前端 Docker 镜像构建文件
│   ├── nginx.conf        # Nginx 配置文件
│   └── ...
├── src/                   # 后端项目
├── Dockerfile            # 后端 Docker 镜像构建文件
├── docker-compose.yml    # Docker Compose 编排文件
├── build-and-push.sh     # 构建和推送脚本
└── ...
```

## 🏗️ 组件说明

### 1. PostgreSQL 数据库
- **镜像**: postgres:15
- **端口**: 仅在 Docker 内部网络可用（不暴露到宿主机）
- **数据持久化**: Docker volume (postgres_data)
- **自动初始化**: 执行 `src/main/resources/sql/` 下的 SQL 脚本

### 2. 后端服务 (Backend)
- **镜像**: 114.214.255.82:18080/public/packclaw:v0.01.arm
- **端口**: 8080 (仅在 Docker 内部网络可用)
- **工作空间**: `$HOME/.packclaw/workspace` (自动适配用户目录)
- **依赖**: PostgreSQL 健康检查通过后启动

### 3. 前端服务 (Frontend)
- **镜像**: 114.214.255.82:18080/public/packclaw-frontend:v0.01.arm
- **端口**: 80 (唯一暴露到宿主机的端口)
- **功能**: 
  - Nginx 静态资源服务
  - API 代理到后端 (/api → http://backend:8080)
  - SSE 流式响应支持
  - Gzip 压缩
  - SPA 路由支持

## 🚀 快速开始

### 方式一：一键构建和部署

```bash
# 构建前后端，推送到 Harbor，并更新配置
./build-and-push.sh

# 启动所有服务
docker-compose up -d
```

### 方式二：手动步骤

#### 1. 构建后端镜像并推送
```bash
# Maven 构建
mvn clean package -DskipTests

# 构建 Docker 镜像
docker build -t packclaw:v0.01.arm .

# 标记并推送
docker tag packclaw:v0.01.arm 114.214.255.82:18080/public/packclaw:v0.01.arm
docker push 114.214.255.82:18080/public/packclaw:v0.01.arm
```

#### 2. 构建前端镜像并推送
```bash
# 构建 Docker 镜像
docker build -t packclaw-frontend:v0.01.arm ./frontend

# 标记并推送
docker tag packclaw-frontend:v0.01.arm 114.214.255.82:18080/public/packclaw-frontend:v0.01.arm
docker push 114.214.255.82:18080/public/packclaw-frontend:v0.01.arm
```

#### 3. 启动服务
```bash
docker-compose up -d
```

## 📋 常用命令

### 查看服务状态
```bash
docker-compose ps
```

### 查看日志
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

### 停止服务
```bash
# 停止但不删除容器
docker-compose stop

# 停止并删除容器（保留数据卷）
docker-compose down

# 停止并删除容器和数据卷（⚠️ 会删除数据库数据）
docker-compose down -v
```

### 重启服务
```bash
docker-compose restart
```

### 重新构建并启动
```bash
docker-compose up -d --build
```

## 🌐 访问地址

- **前端应用**: http://localhost
- **后端 API**: http://localhost/api
- **Swagger 文档**: http://localhost/api/swagger-ui.html
- **数据库**: 仅在 Docker 内部网络可用 (postgres:5432)

> **注意**: 只有前端端口 (80) 暴露到宿主机，PostgreSQL 和后端服务仅在 Docker 内部网络通信，不会与宿主机的 PostgreSQL 产生端口冲突。

## 🔧 配置说明

### 环境变量

可以在 `docker-compose.yml` 中修改以下配置：

```yaml
# 数据库配置
POSTGRES_DB: packclaw
POSTGRES_USER: root
POSTGRES_PASSWORD: lsh123456

# 后端配置
SPRING_PROFILES_ACTIVE: docker
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/packclaw
SPRING_DATASOURCE_USERNAME: root
SPRING_DATASOURCE_PASSWORD: lsh123456
```

### 工作空间路径

后端工作空间自动映射到宿主机的 `~/.packclaw/workspace`：
- macOS: `/Users/yourname/.packclaw/workspace`
- Linux: `/home/yourname/.packclaw/workspace`

## 🔐 Harbor 登录

首次推送镜像前需要登录 Harbor：

```bash
docker login 114.214.255.82:18080
# 输入用户名和密码
```

## 🐛 故障排查

### 后端无法连接数据库
```bash
# 检查数据库是否健康
docker-compose ps postgres

# 查看数据库日志
docker-compose logs postgres
```

### 前端无法访问后端 API
```bash
# 检查 Nginx 配置
docker-compose exec frontend cat /etc/nginx/conf.d/default.conf

# 查看前端日志
docker-compose logs frontend
```

### 重新初始化数据库
```bash
# 删除数据卷并重启
docker-compose down -v
docker-compose up -d
```

## 📝 版本管理

修改版本号时，需要更新以下位置：
1. `build-and-push.sh` 中的 `IMAGE_TAG`
2. `docker-compose.yml` 中的镜像标签
3. Harbor 上的镜像标签

## ⚙️ 生产环境建议

1. **修改默认密码**: 更新 `docker-compose.yml` 中的数据库密码
2. **使用 HTTPS**: 配置 Nginx SSL 证书
3. **数据备份**: 定期备份 PostgreSQL 数据卷
4. **资源限制**: 在 `docker-compose.yml` 中添加资源限制
5. **日志管理**: 配置日志轮转和外部日志存储
