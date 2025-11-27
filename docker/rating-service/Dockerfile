FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY rating-service/pom.xml .
COPY rating-service/.mvn ./.mvn
COPY rating-service/mvnw .

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY rating-service/src ./src

# Build application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy built JAR
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8083

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

