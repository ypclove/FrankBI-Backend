FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY ./target/FrankBI-Backend-0.0.1-SNAPSHOT.jar FrankBI-Backend-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/FrankBI-Backend-0.0.1-SNAPSHOT.jar", "&"]