package hh.slackbot.Slackbot.util;

import java.io.IOException;
import java.util.List;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.usergroups.UsergroupsListRequest;
import com.slack.api.methods.request.usergroups.users.UsergroupsUsersListRequest;
import com.slack.api.methods.response.usergroups.UsergroupsListResponse;
import com.slack.api.methods.response.usergroups.users.UsergroupsUsersListResponse;
import com.slack.api.model.Usergroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UsergroupUtil {
    private UsergroupUtil() {}

    private static final Slack slack = Slack.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(UsergroupUtil.class);

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
                            .token(System.getenv("SLACK_BOT_TOKEN"))
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
     * Tries to fetch a usergroup id based on a given name.
     * 
     * @param name
     * @return Found id or null.
     */
    public static String getGroupIdByName(String name) {
        String id = null;
        List<Usergroup> usergroups = getUserGroups();
        if (!(usergroups == null || usergroups.isEmpty())) {
            for (Usergroup group : usergroups) {
                if (group.getName().equalsIgnoreCase(name)) {
                    id = group.getId();
                }
            }
        }
        return id;
    }

    /**
     * Tries to retrieve the users given a usergroups id.
     * 
     * @param groupId
     * @return A list of user id strings. Returns null in case of an error.
     */
    public static List<String> getUsergroupUsers(String groupId) {
        List<String> users = null;
        try {
            UsergroupsUsersListResponse resp = slack.methods().usergroupsUsersList(
                    UsergroupsUsersListRequest.builder()
                            .token(System.getenv("SLACK_BOT_TOKEN"))
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

}
