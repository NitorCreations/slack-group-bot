package hh.slackbot.Slackbot;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
    private UsergroupHandler() {
    }

    private static Slack slack = Slack.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(UsergroupHandler.class);

    public static Response handleUsergroupCommand(SlashCommandRequest req, SlashCommandContext ctx) {
        SlashCommandPayload payload = req.getPayload();
        String[] params = payload.getText().split(" ", 2);
        if (params.length < 2) {
            return ctx.ack("Invalid command parameters");
        }
        String command = params[0];
        String usergroupName = params[1];
        String usergroupId = UsergroupUtil.getGroupIdByName(usergroupName);

        String userId = payload.getUserId();
        boolean result = false;

        if (command.equalsIgnoreCase("join")) {
            result = addUserToGroup(userId, usergroupId);
        } else if (command.equalsIgnoreCase("leave")) {
            result = removeUserFromGroup(userId, usergroupId);
        }

        if (result) {
            return ctx.ack("Your command ran successfully");
        } else {
            return ctx.ack("An error occurred while running your command");
        }
    }

    // TODO: disable group when no users are left because last user can't be removed
    public static boolean removeUserFromGroup(String userId, String groupId) {
        List<String> users = UsergroupUtil.getUsergroupUsers(groupId);
        if (users == null)
            return false;

        List<String> modifiedUsers = users.stream()
            .filter(u -> !u.equals(userId))
            .collect(Collectors.toList());

        return updateUsergroupUserlist(modifiedUsers, groupId);
    }

    // TODO: consider creating a new group if it is not already created
    public static boolean addUserToGroup(String userId, String groupId) {
        List<String> users = UsergroupUtil.getUsergroupUsers(groupId);
        if (users == null)
            return false;

        users.add(userId);

        return updateUsergroupUserlist(users, groupId);
    }

    public static boolean updateUsergroupUserlist(List<String> users, String groupId) {
        try {
            slack.methods().usergroupsUsersUpdate(
                    UsergroupsUsersUpdateRequest.builder()
                            .token(System.getenv("SLACK_BOT_TOKEN"))
                            .usergroup(groupId)
                            .users(users)
                            .build());
            return true;
        } catch (IOException e) {
            logger.error(String.format("IO Error while updating usergroup%n %s", e.getMessage()));
        } catch (SlackApiException e) {
            logger.error(String.format("Slack API Error while updating usergroup%n %s", e.getMessage()));
        }

        return false;
    }

}
