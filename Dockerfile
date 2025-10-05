# Stage 1: Build
FROM azul/zulu-openjdk:17-latest AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM azul/zulu-openjdk:17-jre-latest

# Set working directory
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/jroxy-1.0-SNAPSHOT.jar /app/jroxy.jar

# Expose application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "jroxy.jar"]
