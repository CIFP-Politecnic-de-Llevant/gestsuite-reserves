FROM maven:3-amazoncorretto-17 as develop-stage-reserves
WORKDIR /app

COPY /config/ /resources/

COPY /api/gestsuite-common/ /external/
RUN mvn clean compile install -f /external/pom.xml

COPY /api/gestsuite-reserves .
RUN mvn clean package -f pom.xml
ENTRYPOINT ["mvn","spring-boot:run","-f","pom.xml"]

FROM maven:3-amazoncorretto-17 as build-stage-reserves
WORKDIR /resources

COPY /api/gestsuite-common/ /external/
RUN mvn clean compile install -f /external/pom.xml


COPY /api/gestsuite-reserves .
RUN mvn clean package -f pom.xml

FROM amazoncorretto:17-alpine-jdk as production-stage-reserves
COPY --from=build-stage-reserves /resources/target/reserves-0.0.1-SNAPSHOT.jar reserves.jar
COPY /config/ /resources/
ENTRYPOINT ["java","-jar","/reserves.jar"]