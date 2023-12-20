#Build

FROM maven:3.8.5-openjdk-17-slim AS build

ARG bg_version
ARG maven_profile

WORKDIR /home/app/src

COPY src /home/app/src

RUN mvn clean package -Dbg.version=$bg_version -P $maven_profile


#Package

FROM openjdk:17.0.2

COPY --from=build /home/app/src/target/account-service.jar /usr/local/lib/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/usr/local/lib/app.jar"]
