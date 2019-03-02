FROM adoptopenjdk/openjdk11
VOLUME /tmp
ADD target/police-api-consumer-0.0.1-SNAPSHOT.jar target/app.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=local","target/app.jar"]
