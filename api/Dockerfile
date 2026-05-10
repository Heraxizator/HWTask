# glibc-based image avoids TLS handshake failures to Maven Central common with Alpine in Docker.
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /build
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src src
RUN chmod +x mvnw \
    && ./mvnw -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN groupadd -r spring && useradd -r -g spring spring
COPY --from=builder --chown=spring:spring /build/target/hwtask-0.0.1-SNAPSHOT.jar app.jar
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
