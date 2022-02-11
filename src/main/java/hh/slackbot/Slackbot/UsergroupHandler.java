package hh.slackbot.Slackbot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.slack.api.Slack;
import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.usergroups.users.UsergroupsUsersUpdateRequest;
import com.slack.api.model.Usergroup;

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

        Usergroup usergroup = UsergroupUtil.getGroupByName(usergroupName);

        String userId = payload.getUserId();

        /* slack.methods().chatPostEphemeral(
            ChatPostEphemeralRequest.builder()
                .blocks(
                    asBlocks()
                )
                .build()
        ); */

        if (command.equalsIgnoreCase("join")) {
            if (usergroup == null)
                usergroup = UsergroupUtil.createUsergroup(usergroupName);

            if (addUserToGroup(userId, usergroup)) {
                return ctx.ack(String.format("You have been added to usergoup %s", usergroupName));
            } else {
                return ctx.ack(String.format("Failed to add you to usergroup %s", usergroupName));
            }

        } else if (command.equalsIgnoreCase("leave")) {
            if (usergroup == null)
                return ctx.ack(String.format("Usergroup %s was not found", usergroupName));

            if (removeUserFromGroup(userId, usergroup)) {
                return ctx.ack(String.format("You have been removed from usergroup %s", usergroupName));
            } else {
                return ctx.ack(String.format("Failed to remove you from usergroup %s", usergroupName));
            }

        }

        return ctx.ack("Your request could not be fulfilled at this time.");
    }

    public static boolean removeUserFromGroup(String userId, Usergroup group) {
        logger.info("getting user group users");
        List<String> users = UsergroupUtil.getUsergroupUsers(group.getId());
        if (users == null)
            return false;

        List<String> modifiedUsers = users.stream()
                .filter(u -> !u.equals(userId))
                .collect(Collectors.toList());

        if (!UsergroupUtil.userInGroup(group, userId)) {
            // TODO: send error message
            return false;
        }

        if (modifiedUsers.isEmpty()) {
            return UsergroupUtil.disableUsergroup(group.getId());
        } else {
            return updateUsergroupUserlist(modifiedUsers, group.getId());
        }
    }

    public static boolean addUserToGroup(String userId, Usergroup group) {
        List<String> users;
        // checking if group was disabled and if it was,
        // don't keep the leftover user from it
        if (group.getDateDelete() != 0) {
            users = new ArrayList<>();
            UsergroupUtil.enableUsergroup(group.getId());
        } else {
            users = UsergroupUtil.getUsergroupUsers(group.getId());
        }
    
        if (users == null)
            return false;

        if (UsergroupUtil.userInGroup(group, userId)) {
            // TODO: send error message
            return false;
        }

        users.add(userId);

        return updateUsergroupUserlist(users, group.getId());
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
