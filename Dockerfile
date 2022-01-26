FROM openjdk:11-slim-buster as build

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src

RUN --mount=type=secret,id=SLACK_BOT_TOKEN \
    --mount=type=secret,id=SLACK_SIGNING_SECRET \
   export SLACK_BOT_TOKEN=$(cat /run/secrets/SLACK_BOT_TOKEN) && \
   export SLACK_SIGNING_SECRET=$(cat /run/secrets/SLACK_SIGNING_SECRET) && \
   mkdir .env && \
   echo -n $SLACK_BOT_TOKEN > .env/token && \
   echo -n $SLACK_SIGNING_SECRET > .env/secret && \
   ./mvnw -B package

FROM openjdk:11-jre-slim-buster

COPY --from=build target/*.jar .
COPY --from=build .env .

EXPOSE 3000

ENTRYPOINT SLACK_BOT_TOKEN=$(cat .env/token) SLACK_SIGNING_SECRET=$(cat .env/secret) java
CMD -jar Slackbot-0.0.1-SNAPSHOT.jar