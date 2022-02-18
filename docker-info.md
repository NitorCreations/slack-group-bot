## Building

```bash
docker build -t NAME .
```

## Running

Set your own SLACK_BOT_TOKEN and SLACK_SIGNING_SECRET as environment variables in a env file before running, so that it can answer to the events happening in your workspace.

*Image name to be decided. Use the name you assigned if you built the image yourself*
*The slackbot runs on port 8080 unless specified otherwise by PORT env variable.*

```
PORT=specified_port
SLACK_BOT_TOKEN=your_token
SLACK_SIGNING_SECRET=your_secret
```

```bash
docker run --env-file .env -p LOCAL:CONTAINER NAME
```
