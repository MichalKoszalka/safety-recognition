FROM adoptopenjdk/openjdk11
EXPOSE 8080 8081
VOLUME /tmp
ADD target/indexes-calculator-0.0.1-SNAPSHOT.jar target/app.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=local","target/app.jar"]
