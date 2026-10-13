
FROM maven:3.10.0-eclipse-temurin-25 AS build

WORKDIR /app

COPY settings.xml /root/.m2/settings.xml
COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 10000

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
