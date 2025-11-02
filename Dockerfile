# Stage 1: Build
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# copy Maven wrapper และ source
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# ให้ mvnw execute ได้
RUN chmod +x mvnw

# โหลด dependency offline
RUN ./mvnw dependency:go-offline

# build project
RUN ./mvnw package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app

# copy .jar จาก build stage
COPY --from=build /app/target/*.jar app.jar

# expose port
EXPOSE 8080

# run spring boot jar
ENTRYPOINT ["java", "-jar", "app.jar"]
