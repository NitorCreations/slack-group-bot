## Building

```bash
docker build -t NAME .
```

## Running

The bot needs to be given your own SLACK_BOT_TOKEN and SLACK_SIGNING_SECRET as environment variables when running, so that it can answer to the events happening in your workspace.

*Image name to be decided. Use the name you assigned if you built the image yourself*
*The slackbot runs on port 8080 unless specified otherwise by PORT env variable.*

```bash
docker run \
-p 80:8080 \
--env PORT=8080 \
--env SLACK_BOT_TOKEN=your_token \
--env SLACK_SIGNING_SECRET=your_secret \
IMAGE_NAME
```
