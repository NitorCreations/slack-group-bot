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
import com.slack.api.model.Usergroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hh.slackbot.Slackbot.util.UsergroupUtil;

public class UsergroupHandler {
    private UsergroupHandler() {
    }

    private static Slack slack = Slack.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(UsergroupHandler.class);

    /**
     * FUNCTION:
     * - CHECKS THE USER INPUT
     * - GETS A STRING FROM finalizeUsergroupCommand
     * - CALLS THE Messenger OBJECT
     */
    public static Response handleUsergroupCommand(SlashCommandRequest req, SlashCommandContext ctx) {

        // Will be used as a parameter to Messager object
        String message = "";

        // Takes the playload (user input data)
        SlashCommandPayload payload = req.getPayload();

        // Try-catch is used to check if the text part of payload
        // can be split into two String variables
        try {

            // Splitting the text part of user input based on
            // the FIRST space between the command and the usergroup's name
            String[] params = payload.getText().split(" ", 2);

            // Contains the command's name
            // (join/leave)
            String command = params[0];

            // Contains everything written after the command name
            // (usergroup's name)
            String usergroupName = params[1];

            // Contains the userId from the current user
            String userId = payload.getUserId();

            // Calls the method finalizeUsergroupCommand and
            // returns a message of the command's failure/success
            message = finalizeUsergroupCommand(userId, command, usergroupName);

        } catch (Exception e) {

            // This message is returned if the "Try" part fails
            // (a.k.a the user input is too short to be split)
            message = "Invalid command parameters";

        } finally {

            // IMPORTANT
            // THIS CALLS THE MESSAGES OBJECT AND SENDS THE MESSAGE TO THE USER
            // Messager.help(message); //
            // <----------------------------------------IMPORTANT!!!
        }

        return ctx.ack(message); // <--- THIS IS ONLY FOR TESTING: THE MESSAGE PARAMETER CAN BE REMOVED
    }

    /**
     * FUNCTION:
     * - CALLS THE JOIN/LEAVE METHODS
     * - RETURNS A CORRECT MESSAGE TO handleUsergroupCommand
     */
    private static String finalizeUsergroupCommand(String userId, String command, String usergroupName) {

        // This contains the message that will be sent to handleUsergroupCommand
        String answer = "The command can not be done";

        // This creates a new Usergroup object that uses the getGroupByName command to
        // get
        // the same values as the user group with the same name as usergroupName's value
        // --> If that group doesn't exist, this new usergroup will be null
        Usergroup usergroup = UsergroupUtil.getGroupByName(usergroupName);

        // This method checks if the checkUsergroup method was successful and returns:
        // TRUE: if the user group exists, and the command was "join" --> joining is
        // possible
        // TRUE: if the user group does NOT exist, but the command was "join" --> the
        // group is created
        // FALSE: if the user group is null AND the command was "leave" --> can't leave
        // a non-existing group
        if (UsergroupUtil.checkUsergroup(usergroup, command, usergroupName)) {

            // If the checkUsegroup returned TRUE, the usergroup variable is updated
            // (in case the new user group was created inside the checkUsergroup method)
            usergroup = UsergroupUtil.getGroupByName(usergroupName);

            // If the command was "join", the addUserToGroup is called
            // and the response is stored inside answer
            if (command.equalsIgnoreCase("join")) {
                answer = addUserToGroup(userId, usergroup);
                // If the command was "leave", the removeUserFromGroup is called
                // and the response is stored inside answer
            } else if (command.equalsIgnoreCase("leave")) {
                answer = removeUserFromGroup(userId, usergroup);
            }

            // However, if the checkUsergroup method returned FALSE
            // (a.k.a. the Usergroup object was null and the command was "leave"),
            // there is no existing user group with the same name as usergroupName,
            // which makes it impossible to call removeUserFromGroup command
            // --> The following error message is created immediately
        } else {
            answer = "Usergroup " + usergroupName + " was not found";
        }

        // The answer is returned to handleUsergroupCommand
        return answer;
    }

    /**
     * FUNCTION:
     * - JOIN THE USER GROUP
     */
    public static String addUserToGroup(String userId, Usergroup group) {

        // The user list (users) is based on the current condition of the group:
        // --> If the user group is disabled, an empty list is returned
        // --> If the user group is enabled, its current users will be returned
        List<String> users = UsergroupUtil.checkIfDisabled(group);

        // This checks if the user is already in the user group they want to join:
        if (UsergroupUtil.userInGroup(userId, users)) {
            // If YES, this String is returned to finalizeUsergroupCommand
            return "You already are in the usergroup " + group.getName();
        } else {
            // If NOT, the user will be added to the group's user list
            users.add(userId);
            updateUsergroupUserlist(users, group.getId());
        }
        // If all went well, this String is returned to finalizeUsergroupCommand:
        return "You have successfully joined the user group " + group.getName();
    }

    /**
     * FUNCTION:
     * - LEAVE THE USER GROUP
     */
    public static String removeUserFromGroup(String userId, Usergroup group) {
        logger.info("getting user group users");

        // ArrayList users will have its value based on getUsergroupUsers
        List<String> users = UsergroupUtil.getUsergroupUsers(group.getId());

        // If the leaving user was not found from the user group's user list...
        if (!UsergroupUtil.userInGroup(userId, users)) {
            // The following String will be returned to finalizeUsergroupCommand
            return "You are not a member of the user group " + group.getName();
        }

        // ArrayList modifiedUsers will contain every
        // user from the usergroup EXCEPT the user who wants to leave
        List<String> modifiedUsers = users.stream()
                .filter(u -> !u.equals(userId))
                .collect(Collectors.toList());

        // If the modifiedUsers is empty (a.k.a. only the leaving user is in the
        // group)...
        if (modifiedUsers.isEmpty()) {
            // ...the user group will be disabled
            UsergroupUtil.disableUsergroup(group.getId());
        } else {
            // ...otherwise the user group will be updated with modifiedUsers
            updateUsergroupUserlist(modifiedUsers, group.getId());
        }
        // If the leaving goes well, the following String is returned to
        // finalizeUsergroupCommand
        return "You have successfully left the user group " + group.getName();
    }

    /**
     * FUNCTION:
     * - UPDATES THE USER LIST OF AN USER GROUP
     */
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
