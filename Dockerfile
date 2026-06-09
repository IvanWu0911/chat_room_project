FROM maven:3.9.6-eclipse-temurin-17 AS build
COPY . /app
WORKDIR /app
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/ChatServer.jar app.jar
EXPOSE 12345
ENTRYPOINT ["java", "-jar", "app.jar"]
