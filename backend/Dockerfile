FROM docker.io/library/maven:3.9.11 AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline -B
COPY ./src ./src
RUN mvn clean package -DskipTests

FROM docker.io/library/eclipse-temurin:25 AS production
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]