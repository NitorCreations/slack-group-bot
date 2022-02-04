package hh.slackbot.Slackbot;

import java.io.IOException;
import java.util.List;

import com.slack.api.Slack;
import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.usergroups.users.UsergroupsUsersUpdateRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hh.slackbot.Slackbot.util.UsergroupUtil;

public class UsergroupHandler {

    private Slack slack = Slack.getInstance();

    private UsergroupUtil util = new UsergroupUtil();

    private static final Logger logger = LoggerFactory.getLogger(UsergroupHandler.class);

    public Response handleUsergroupCommand(SlashCommandRequest req, SlashCommandContext ctx) {
        SlashCommandPayload payload = req.getPayload();
        String[] params = payload.getText().split(" ", 2);
        if (params.length < 2) {
            return ctx.ack("Invalid command parameters");
        }
        String command = params[0];
        String usergroupName = params[1];
        String usergroupId = util.getGroupIdByName(usergroupName);

        String userId = payload.getUserId();
        boolean result = false;

        if (command.equalsIgnoreCase("join")) {
            result = addUserToGroup(userId, usergroupId);
        } else if (command.equalsIgnoreCase("leave")) {
            // TODO
        }

        if (result) {
            return ctx.ack("You have been added to the group");
        } else {
            return ctx.ack("Joining group failed");
        }
    }

    public boolean addUserToGroup(String userId, String groupId) {
        List<String> users = util.getUsergroupUsers(groupId);
        if (users == null) return false;

        users.add(userId);

        try {
            slack.methods().usergroupsUsersUpdate(
                UsergroupsUsersUpdateRequest.builder()
                    .token(System.getenv("SLACK_BOT_TOKEN"))
                    .usergroup(groupId)
                    .users(users)
                    .build()
            );
        } catch (IOException e) {
            logger.error(String.format("IO Error while adding user to group%n %s", e.getMessage()));
        } catch (SlackApiException e) {
            logger.error(String.format("Slack API Error while adding user to group%n %s", e.getMessage()));
        }

        return true;
    }
    
}
