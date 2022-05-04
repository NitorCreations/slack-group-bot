## Running

The bot needs a SLACK_BOT_TOKEN and SLACK_SIGNING_SECRET as environment variables from your own slack app to run. They can be given in an environment file or as cli parameters.

*Image name to be decided. Use the name you assigned if you built the image yourself*
*The slackbot runs on port 8080 unless specified otherwise by PORT env variable.*

```bash
# .env
PORT=specified_port # Optional, defaults to 8080
SLACK_BOT_TOKEN=your_token
SLACK_SIGNING_SECRET=your_secret
```

```bash
# run from source
docker build -t NAME .
docker run --env-file .env -p LOCAL_PORT:CONTAINER_PORT NAME

# run from dockerhub image
docker run --env-file .env -p LOCAL_PORT:CONTAINER_PORT hhbot/slackbot:latest

# run with cli env variables
docker run -e PORT=specified_port \
    -e SLACK_BOT_TOKEN=your_token \
    -e SLACK_SIGNING_SECRET=your_secret \
    -p LOCAL_PORT:CONTAINER_PORT \
    hhbot/slackbot:latest
```
