# Stage 1: Build the application
FROM gradle:9-jdk21 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon

# Stage 2: Create the final image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /application
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]