
FROM maven as build_java
COPY . /src
WORKDIR /src
RUN mvn -DskipTests=true clean package 

FROM  openjdk:17-oraclelinux8
COPY  --from=build_java /src/ascob-server/target/*.jar /app.jar
EXPOSE 8081/tcp
CMD ["java", "-Dserver.port=8081", "-Dspring.profiles.active=demo", "-jar", "/app.jar"]
