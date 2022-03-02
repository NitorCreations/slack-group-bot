package hh.slackbot.Slackbot;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slack.api.Slack;
import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.usergroups.users.UsergroupsUsersUpdateRequest;
import com.slack.api.model.Usergroup;

import hh.slackbot.Slackbot.util.MessageUtil;
import hh.slackbot.Slackbot.util.UsergroupUtil;

public class UsergroupHandler {
    public UsergroupHandler() {
    }

    private static Slack slack = Slack.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(UsergroupHandler.class);

    /**
     * Breaks the command's name and target from user input,
     * calls finalizeUsergroup to finalize the user group method 
     * and calls messageUtil to send the correct message to the user
     * 
     * @param req, ctx
     * @return ctx.ack()
    */
    public static Response handleUsergroupCommand(SlashCommandRequest req, SlashCommandContext ctx) {

        String message = "";

        SlashCommandPayload payload = req.getPayload();

        String userId = payload.getUserId();

        try {

            String[] params = payload.getText().split(" ", 2);

            String command = params[0];

            String usergroupName = params[1];

            message = finalizeUsergroupCommand(userId, command, usergroupName);

        } catch (Exception e) {

            message = "Invalid command parameters";

        } finally {
            MessageUtil.sendDirectMessage(message, userId);
        }

        return ctx.ack();
    }

    /**
     * Gets parameters from usergroupHandler, checks them
     * and calls the correct user group method based on them
     * 
     * @param userId, command, usergroupName
     * @return message based on the success/error of the user group method
    */
    private static String finalizeUsergroupCommand(String userId, String command, String usergroupName) {

        String answer = "The command can not be done";

        Usergroup usergroup = UsergroupUtil.getGroupByName(usergroupName);

        if (UsergroupUtil.checkUsergroup(usergroup, command, usergroupName)) {
        	UsergroupHandler groupHandler = new UsergroupHandler();
        	
            usergroup = UsergroupUtil.getGroupByName(usergroupName);

            if (command.equalsIgnoreCase("join")) {
            	boolean addedUserToGroupSuccessfully = groupHandler.addUserToGroup(userId, usergroup);
            	
            	if (addedUserToGroupSuccessfully) {
            		answer = "You have successfully joined the user group " + usergroup.getName();
            	} else {
            		answer = "You already are in the usergroup " + usergroup.getName();
            	}
            	
            } else if (command.equalsIgnoreCase("leave")) {
                answer = groupHandler.removeUserFromGroup(userId, usergroup);
            }

        } else {
            answer = "Usergroup " + usergroupName + " was not found";
        }

        return answer;
    }

    /**
     * Adds the user into a new user group,
     * if the certain conditions allow it
     * 
     * @param userId, group
     * @return true if the user was added to user group successfully.
     *         Returns false if the user already is in the user group.
    */
    public boolean addUserToGroup(String userId, Usergroup group) {

        List<String> users = UsergroupUtil.checkIfDisabled(group);

        if (UsergroupUtil.userInGroup(userId, users)) {
            return false;
        } else {
            users.add(userId);
            updateUsergroupUserlist(users, group.getId());
        }
        return true;
    }

    /**
     * Removes the user from user group,
     * if certain conditions allow it
     * 
     * @param userId, group
     * @returns message based on the success/failure of the method
    */
    public String removeUserFromGroup(String userId, Usergroup group) {
        logger.info("getting user group users");

        List<String> users = UsergroupUtil.getUsergroupUsers(group.getId());

        if (!UsergroupUtil.userInGroup(userId, users)) {
            return "You are not a member of the user group " + group.getName();
        }

        List<String> modifiedUsers = users.stream()
                .filter(u -> !u.equals(userId))
                .collect(Collectors.toList());

        if (modifiedUsers.isEmpty()) {
            UsergroupUtil.disableUsergroup(group.getId());
        } else {
            updateUsergroupUserlist(modifiedUsers, group.getId());
        }
        return "You have successfully left the user group " + group.getName();
    }

    /**
     * Updates the user group's user list (the last step of joining/leaving the user group)
     * 
     * @param users, groupId
     * @return true, if the Slack API's updateUsergroupUserlist method is successful.
     * 		   Returns false if the method fails
     */
    public boolean updateUsergroupUserlist(List<String> users, String groupId) {
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
