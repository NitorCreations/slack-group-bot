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

1. Get a free API Key at [https://example.com](https://example.com)
2. Clone the repo
   ```sh
   git clone https://github.com/github_username/repo_name.git
   ```
3. Install NPM packages
   ```sh
   npm install
   ```
4. Enter your API in `config.js`
   ```js
   const API_KEY = 'ENTER YOUR API';
   ```

<!-- USAGE EXAMPLES -->
## Usage

Use this space to show useful examples of how a project can be used. Additional screenshots, code examples and demos work well in this space. You may also link to more resources.

### Commands

#### Joining / creating the user group: ####
Write the following command in the Slack channel's message input: 
```Slack input
    /groups join [group_name]
```

- If the group exists, and you are not currently in it, you will be added to it successfully
- If the group does not exist, it will be created before you are added to it successfully
- If you already are in the group, you can not join it (the joining will fail)
- If the user group is disabled, it will be enabled again before you are added to it successfully
- If the group's name is similar with other group names, an interactive list will be shown to you:
  - The list contains:
    - All the groups that have similar names with the given group name
    - "Join" buttons, which you can press in order to join any of those similar groups
    - "Create and Join" button, which you can press to create and join the new group

#### Leaving the user group: ####

Write the following command in the Slack channel's message input:
```Slack input
   /groups leave [group_name]
```

- If you are in the group, you will be removed from it successfully
- If the group does not exist, you can not leave it (the removing will fail)
- If you are the only member of the group, it will be disabled after you have left it successfully
- Remember: there will be no interactive list for similar group names when you are trying to leave a group
    - Make sure you write group's name correctly

#### Help message ####

Write the following command in the Slack channel's message input:
```Slack input
   /groups help
```

- The command will show you a help message which contains all the available commands of the bot
- The command is also launched automatically if: 
      - your join/leave command has failed due to missing parameters
      - you have used a command that does not exist in the bot




## Technologies

- Java
- Spring Framework
- Slack Bolt API
- Checkstyle linter

<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE.txt` for more information.

<p align="right">(<a href="#top">back to top</a>)</p>
