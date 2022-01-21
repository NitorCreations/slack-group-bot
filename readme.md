## Running on Linux
- requires
    - maven
    - ngrok
    - ability to change app command url
- export token and secret to path

    - export SLACK_BOT_TOKEN="token"
    - export SLACK_SIGNING_SECRET="secret"

- run app
    - "mvn spring-boot:run"
- run ngrok
    - ngrok http 3000
    - check the URL that is generated and add it to the app command on Slack's app settings