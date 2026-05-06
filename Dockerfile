# ── Stage 1: Maven 编译 ──
FROM maven:3.9.6-amazoncorretto-21-al2023 AS build

WORKDIR /app

# 先拷贝 pom.xml 下载依赖（利用 Docker 层缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 拷贝源码并打包
COPY src/ src/
RUN mvn clean package -DskipTests -B

# ── Stage 2: JRE 运行时 ──
FROM amazoncorretto:21-al2023-jdk AS runtime

WORKDIR /app

# 从编译阶段拷贝 jar
COPY --from=build /app/target/lottery-backend-1.0.0-SNAPSHOT.jar app.jar

# 时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
