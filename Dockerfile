# ========== 阶段 1: Maven 构建 ==========
FROM docker.m.daocloud.io/maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# 先复制 pom.xml，利用 Docker 缓存依赖层
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# ========== 阶段 2: 运行时镜像 ==========
FROM docker.m.daocloud.io/eclipse-temurin:21-jdk-jammy

WORKDIR /app

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY --from=builder /build/target/PackClaw.jar /app/PackClaw.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.net.preferIPv4Stack=true -Dhttps.protocols=TLSv1.2,TLSv1.3"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=docker -jar /app/PackClaw.jar"]
