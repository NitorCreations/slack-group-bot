package hh.slackbot.Slackbot.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.usergroups.UsergroupsCreateRequest;
import com.slack.api.methods.request.usergroups.UsergroupsDisableRequest;
import com.slack.api.methods.request.usergroups.UsergroupsEnableRequest;
import com.slack.api.methods.request.usergroups.UsergroupsListRequest;
import com.slack.api.methods.request.usergroups.users.UsergroupsUsersListRequest;
import com.slack.api.methods.response.usergroups.UsergroupsCreateResponse;
import com.slack.api.methods.response.usergroups.UsergroupsDisableResponse;
import com.slack.api.methods.response.usergroups.UsergroupsEnableResponse;
import com.slack.api.methods.response.usergroups.UsergroupsListResponse;
import com.slack.api.methods.response.usergroups.users.UsergroupsUsersListResponse;
import com.slack.api.model.Usergroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsergroupUtil {
    private UsergroupUtil() {
    }

    private static final Slack slack = Slack.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(UsergroupUtil.class);

    private static final String TOKEN = System.getenv("SLACK_BOT_TOKEN");

    /**
     * Tries to retrieve a list of enabled usergroups in the workspace.
     * 
     * @return A list containing all usergroup objects or an empty list.
     *         Returns null in case of an error.
     */
    public static List<Usergroup> getUserGroups() {
        List<Usergroup> usergroups = null;
        logger.info("Retrieving user groups");

        try {
            UsergroupsListResponse resp = slack.methods().usergroupsList(
                    UsergroupsListRequest.builder()
                            .token(TOKEN)
                            .includeDisabled(true)
                            .build());
            return resp.getUsergroups();
        } catch (IOException e) {
            logger.error(String.format("IO Error while getting usergroups%n %s", e.getMessage()));
        } catch (SlackApiException e) {
            logger.error(String.format("Slack API Error while getting usergroups%n %s", e.getMessage()));
        }

        return usergroups;
    }

    /**
     * Tries to fetch a usergroup's id based on a given name.
     * 
     * @param name
     * @return Found id or null.
     */
    public static Usergroup getGroupByName(String name) {
        Usergroup group = null;
        List<Usergroup> usergroups = getUserGroups();
        if (!(usergroups == null || usergroups.isEmpty())) {
            for (Usergroup g : usergroups) {
                if (g.getName().equalsIgnoreCase(name)) {
                    group = g;
                }
            }
        }
        return group;
    }

    /**
     * Tries to retrieve the users given a usergroup's id.
     * 
     * @param groupId
     * @return A list of user id strings. Returns null in case of an error.
     */
    public static List<String> getUsergroupUsers(String groupId) {
        List<String> users = null;
        try {
            UsergroupsUsersListResponse resp = slack.methods().usergroupsUsersList(
                    UsergroupsUsersListRequest.builder()
                            .token(TOKEN)
                            .usergroup(groupId)
                            .build());

            users = resp.getUsers();

        } catch (IOException e) {
            logger.error(String.format("IO Error while getting usergroups users%n %s", e.getMessage()));
        } catch (SlackApiException e) {
            logger.error(String.format("Slack API Error while getting usergroups users%n %s", e.getMessage()));
        }

        return users;
    }

    /**
     * Creates the user group
     * 
     * @param name
     * @returns Usergroup object. Returns null in case of an error.
     */

    public static Usergroup createUsergroup(String name) {
        Usergroup group = null;

        try {
            UsergroupsCreateResponse resp = slack.methods().usergroupsCreate(
                    UsergroupsCreateRequest
                            .builder()
                            .token(TOKEN)
                            .name(name)
                            .build());

            if (resp.isOk()) {
                group = resp.getUsergroup();
            }
        } catch (IOException e) {
            logger.error(String.format("IO Error while getting usergroups users%n %s", e.getMessage()));
        } catch (SlackApiException e) {
            logger.error(String.format("Slack API Error while getting usergroups users%n %s", e.getMessage()));
        }

        return group;
    }

    /**
     * Checks if the user is in the user group
     * 
     * @params group, userId, users
     * @returns true or false
     * 
     */
    public static boolean userInGroup(String userId, List<String> users) {

        if (users == null || users.isEmpty()) {
            return false;
        } else {
            for (String u : users) {
                if (u.equals(userId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Enables the user group
     * 
     * @param id
     * @returns true. Returns false in case of an error.
     */
    public static boolean enableUsergroup(String id) {
        try {
            UsergroupsEnableResponse resp = slack.methods().usergroupsEnable(
                    UsergroupsEnableRequest
                            .builder()
                            .token(TOKEN)
                            .usergroup(id)
                            .build());

            return resp.isOk();
        } catch (IOException e) {
            logger.error(String.format("IO Error while enabling usergroup%n %s", e.getMessage()));
        } catch (SlackApiException e) {
            logger.error(String.format("Slack API Error while enabling usergroup%n %s", e.getMessage()));
        }

        return false;
    }

    /**
     * Disables the user group
     * 
     * @param id
     * @returns true. Returns false in case of an error.
     */
    public static boolean disableUsergroup(String id) {
        try {
            UsergroupsDisableResponse resp = slack.methods().usergroupsDisable(
                    UsergroupsDisableRequest
                            .builder()
                            .token(TOKEN)
                            .usergroup(id)
                            .build());

            return resp.isOk();
        } catch (IOException e) {
            logger.error(String.format("IO Error while disabling usergroup%n %s", e.getMessage()));
        } catch (SlackApiException e) {
            logger.error(String.format("Slack API Error while disabling usergroup%n %s", e.getMessage()));
        }

        return false;
    }

    /**
     * Checks if the user group is null
     * - If YES (and command is "join"),
     * A new user group is created
     * and true is returned
     * - If YES (and command is "leave"),
     * false is returned
     * - If NO (with any command),
     * true is returned
     * 
     * @param usergroup, command, usergroupName
     * @returns true or false
     */
    public static boolean checkUsergroup(Usergroup usergroup, String command, String usergroupName) {

        if (usergroup == null) {
            if (command.equalsIgnoreCase("join")) {
                UsergroupUtil.createUsergroup(usergroupName);
                return true;

            } else if (command.equalsIgnoreCase("leave")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the user group is currently disabled
     * - If YES, the usergroup is enabled,
     * and an empty userlist is returned
     * - If NOT, the current userlist
     * of the group is returned
     * 
     * @param Usergroup
     * @returns an ArrayList
     */
    public static List<String> checkIfDisabled(Usergroup group) {

        if (group.getDateDelete() != 0) {
            enableUsergroup(group.getId());
            return new ArrayList<>();
        } else {
            return getUsergroupUsers(group.getId());
        }
    }
}
