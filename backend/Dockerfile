# ---- build stage ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# cache Gradle wrapper and dependencies before copying source
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle/ gradle/
RUN ./gradlew dependencies --no-daemon -q || true

COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# ---- runtime stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
