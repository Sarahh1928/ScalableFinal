FROM openjdk:25-ea-4-jdk-oraclelinux9

WORKDIR /app

COPY target/api-gateway.jar .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "api-gateway.jar"]
