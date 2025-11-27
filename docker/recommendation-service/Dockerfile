FROM gradle:8.5-jdk17-alpine AS build

WORKDIR /app

# Copy Gradle files
COPY recommendation-service/build.gradle.kts .
COPY recommendation-service/settings.gradle.kts .

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy source code
COPY recommendation-service/src ./src

# Build application
RUN gradle build --no-daemon -x test -x ktlintKotlinScriptCheck -x ktlintMainSourceSetCheck

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy built JAR
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8084

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

