
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY . .


RUN chmod +x gradlew
RUN ./gradlew clean build -x test


FROM eclipse-temurin:17-jdk
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app


COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
