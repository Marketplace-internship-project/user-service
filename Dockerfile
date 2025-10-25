FROM maven:3.9.6-eclipse-temurin-22-jammy AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:22-jre-jammy

WORKDIR /app

RUN useradd -ms /bin/bash appuser
USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
#!!профиль
ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "app.jar"]