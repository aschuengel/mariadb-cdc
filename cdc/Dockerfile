FROM amazoncorretto:18 AS build
COPY build.gradle /app/
COPY settings.gradle /app/
COPY gradlew /app/
COPY gradle /app/gradle
WORKDIR /app/
RUN ./gradlew dependencies
COPY src /app/src
RUN ./gradlew bootJar
FROM amazoncorretto:18 AS RUN
COPY --from=build /app/build/libs/cdc.jar /app/
WORKDIR /app/