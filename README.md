<div id="top"></div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

# Nitor Slack group bot

## About the project

Here's a blank template to get started: To avoid retyping too much info. Do a search and replace with your text editor for the following: `github_username`, `repo_name`, `twitter_handle`, `linkedin_username`, `email_client`, `email`, `project_title`, `project_description`

## Getting started

### Prerequisites

This is an example of how to list things you need to use the software and how to install them.
* npm
  ```sh
  npm install npm@latest -g
  ```

### Installation

- PLACEHOLDER

### Running the image

The bot needs a SLACK_BOT_TOKEN and SLACK_SIGNING_SECRET as environment variables from your own slack app to run. They can be given in an environment file or as cli parameters.

*Image name for dockerhub hosted image is "hhbot/slackbot:latest". Use the name you assign if you build the image yourself*
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

<!-- USAGE EXAMPLES -->
## Usage

Use this space to show useful examples of how a project can be used. Additional screenshots, code examples and demos work well in this space. You may also link to more resources.

### Commands

- Add to group: /groups join [group_name]
- Remove from group: /groups leave [group_name]
- Help: /groups help

## Technologies

- Java
- Spring Framework
- Slack Bolt API
- Checkstyle linter

<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE.txt` for more information.

<p align="right">(<a href="#top">back to top</a>)</p>