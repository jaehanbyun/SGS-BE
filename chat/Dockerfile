FROM gradle:7.2-jdk11 AS build

WORKDIR /app

COPY . .

RUN gradle clean build -x test

FROM adoptopenjdk:11-jre

ENV LC_ALL=C.UTF-8

ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

COPY --from=build /app/build/libs/*0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8031

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.name=application-dev"]