FROM openjdk:11-slim-buster as build

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

# Needed for docker to be able to recognise the file if it's been
# modified on a Windows machine
RUN sed -i 's/\r$//' mvnw
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline -ntp -q

COPY src src

# skip tests because they are tested in the workflow
RUN ./mvnw -B package -DskipTests -ntp

FROM openjdk:11-jre-slim-buster

WORKDIR /usr/src/app

COPY --from=build target/*.jar slackbot.jar

RUN touch mylog.txt && \
    useradd -m botuser && \
    chown botuser mylog.txt

USER botuser

# jar file needs to be named according to build target name in pom.xml
CMD java -jar -Dserver.port=${PORT:-8080} slackbot.jar
