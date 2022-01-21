## Running on Linux
- requires
    - maven
    - ngrok
    - ability to change app command url
- export token and secret to path

    - export SLACK_BOT_TOKEN=xoxb-2998717695601-3009860605472-qWLJtwXgMJo93PprQULfk4ow
    - export SLACK_SIGNING_SECRET=e74f61ffce6e10deb4d5afd56c69b9b0

- run app
    - "mvn spring-boot:run"
- run ngrok
    - ngrok http 3000
    - check the URL that is generated and add it to the app command on Slack's app settings