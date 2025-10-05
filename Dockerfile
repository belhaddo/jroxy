FROM maven:3.9.11-eclipse-temurin-17 as build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:resolve

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/jroxy-0.0.1-SNAPSHOT.jar /app/jroxy.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "jroxy.jar"]
