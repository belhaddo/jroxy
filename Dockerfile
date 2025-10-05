FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY ./target/jroxy-0.0.1-SNAPSHOT.jar /app/jroxy.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "jroxy.jar"]
