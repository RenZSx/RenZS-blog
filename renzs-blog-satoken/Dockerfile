FROM maven:3.8.8-eclipse-temurin-8 AS builder

WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:8-jre-jammy

WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /app/upload /app/logs
COPY --from=builder /workspace/target/renzs-blog-satoken-0.0.1.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8088
ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS:-} -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]
