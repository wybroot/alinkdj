# 阶段一：Maven 源码编译打包
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY pom.xml .
COPY src ./src

# 跳过测试快速打包生产 JAR
RUN mvn clean package -DskipTests

# 阶段二：生产运行环境 (轻量 JRE 21 Alpine)
FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="honghe-data-party"

WORKDIR /app

# 设置时区为上海
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms512m -Xmx2048m -Dfile.encoding=UTF-8"

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone && \
    mkdir -p /app/uploads

# 从构建阶段复制 jar 包
COPY --from=builder /build/target/party-building-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
