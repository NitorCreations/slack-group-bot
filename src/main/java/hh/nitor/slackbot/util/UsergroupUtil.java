package hh.nitor.slackbot.util;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.usergroups.UsergroupsCreateRequest;
import com.slack.api.methods.request.usergroups.UsergroupsDisableRequest;
import com.slack.api.methods.request.usergroups.UsergroupsEnableRequest;
import com.slack.api.methods.request.usergroups.UsergroupsListRequest;
import com.slack.api.methods.request.usergroups.users.UsergroupsUsersListRequest;
import com.slack.api.methods.request.usergroups.users.UsergroupsUsersUpdateRequest;
import com.slack.api.methods.response.usergroups.UsergroupsCreateResponse;
import com.slack.api.methods.response.usergroups.UsergroupsDisableResponse;
import com.slack.api.methods.response.usergroups.UsergroupsEnableResponse;
import com.slack.api.methods.response.usergroups.UsergroupsListResponse;
import com.slack.api.methods.response.usergroups.users.UsergroupsUsersListResponse;
import com.slack.api.methods.response.usergroups.users.UsergroupsUsersUpdateResponse;
import com.slack.api.model.Usergroup;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsergroupUtil {

  @Autowired
  private MethodsClient client;

  private static final Logger logger = LoggerFactory.getLogger(UsergroupUtil.class);

  private static final String TOKEN = System.getenv("SLACK_BOT_TOKEN");

  /**
   * Tries to retrieve a list of enabled usergroups in the workspace.
   *
   * @return A list containing all usergroup objects or an empty list. Returns
   *         null in case of an error.
   */
  public List<Usergroup> getUserGroups() {
    List<Usergroup> usergroups = null;
    logger.info("Trying to retrieve all groups...");

    try {
      UsergroupsListResponse resp = client.usergroupsList(UsergroupsListRequest.builder()
          .token(TOKEN).includeDisabled(true).includeUsers(true).build());
      if (!resp.isOk()) {
        logger.warn("Failure getting usergroups: {}", resp.getError());
      } else {
        usergroups = resp.getUsergroups();
      }
      logger.info("All groups retrieved successfully");
      return resp.getUsergroups();
    } catch (IOException e) {
      logger.error(String.format("Failed to retrieve all groups"
             + " due to IO Error: %n %s", e.getMessage()));
    } catch (SlackApiException e) {
      logger.error(String.format("Failed to retrieve all groups"
             + " due to Slack API Error: %n %s", e.getMessage()));
    }
    return usergroups;
  }

  /**
   * Tries to fetch a usergroup based on a given name.
   *
   * @param name of the group
   * @return Found group or null.
   */
  public Usergroup getGroupByName(String name) {
    Usergroup group = null;
    List<Usergroup> usergroups = getUserGroups();
    logger.info("Searching for the group {}...", name);
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
   * @param groupId of usergroup
   * @return A list of user id strings. Returns null in case of an error.
   */
  public List<String> getUsergroupUsers(String groupId) {
    List<String> users = null;
    logger.info("Trying to retrieve all the users from the group...");
    try {
      UsergroupsUsersListResponse resp = client.usergroupsUsersList(
          UsergroupsUsersListRequest.builder().token(TOKEN).usergroup(groupId).build());

      if (!resp.isOk()) {
        logger.warn("Failure getting usergroup users: {}", resp.getError());
      } else {
        users = resp.getUsers();
      }
      logger.info("The group's users retrieved successfully");
      users = resp.getUsers();

    } catch (IOException e) {
      logger.error(String.format("Failed to retrieve the group's "
             + "users due to IO Error: %n %s", e.getMessage()));
    } catch (SlackApiException e) {
      logger.error(String.format("Failed to retrieve the group's "
             + "users due to Slack API Error: %n %s", e.getMessage()));
    }

    return users;
  }

  /**
   * Creates the user group.
   *
   * @param name of the new group to be created
   * @returns Usergroup object. Returns null in case of an error.
   */

  public Usergroup createUsergroup(String name) {
    Usergroup group = null;

    logger.info("Trying to create the group {}...", name);
    try {
      UsergroupsCreateResponse resp = client.usergroupsCreate(
          UsergroupsCreateRequest.builder()
            .token(TOKEN)
            .name(name)
            .handle(name)
            .build());

      if (resp.isOk()) {
        logger.info("The group {} created successfully", name);
        group = resp.getUsergroup();
      }
    } catch (IOException e) {
      logger.error(String.format("Failed to retrieve the group's users"
             + " due to IO Error: %n %s", e.getMessage()));
    } catch (SlackApiException e) {
      logger.error(String.format("Failed to retrieve the group's users"
             + " due to Slack API Error: %n %s", e.getMessage()));
    }
    return group;
  }

  /**
   * Checks if the user is in the user group.
   *
   * @param userId of checkable user
   * @param users  list
   * @returns true or false
   * 
   */
  public boolean userInGroup(String userId, List<String> users) {

    logger.info("Checking if the user is in the group...");
    if (users == null || users.isEmpty()) {
      logger.info("The user is not in the group");
      return false;
    } else {
      for (String u : users) {
        if (u.equals(userId)) {
          logger.info("The user is in the group");
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Enables the user group.
   *
   * @param id of usergroup
   * @returns true. Returns false in case of an error.
   */
  public boolean enableUsergroup(String id) {
    try {
      logger.info("Trying to enable the group...");
      UsergroupsEnableResponse resp = client
          .usergroupsEnable(UsergroupsEnableRequest.builder().token(TOKEN).usergroup(id).build());

      if (!resp.isOk()) {
        logger.warn("Failure enabling usergroup: {}", resp.getError());
      }
      logger.info("The group successfully enabled");
      return resp.isOk();
    } catch (IOException e) {
      logger.error(String.format("Failed to enable the group "
             + "due to IO Error: %n %s", e.getMessage()));
    } catch (SlackApiException e) {
      logger.error(String.format("Failed to enable the group "
             + "due to Slack API Error: %n %s", e.getMessage()));
    }
    return false;
  }

  /**
   * Disables the user group.
   *
   * @param id of usergroup
   * @returns true. Returns false in case of an error.
   */
  public boolean disableUsergroup(String id) {
    try {
      logger.info("Trying to disable the group...");
      UsergroupsDisableResponse resp = client
          .usergroupsDisable(UsergroupsDisableRequest.builder().token(TOKEN).usergroup(id).build());

      if (!resp.isOk()) {
        logger.warn("Failure disabling usergroup: {}", resp.getError());
      }
      logger.info("The group successfully disabled");
      return resp.isOk();
    } catch (IOException e) {
      logger.error(String.format("Failed to disable the "
             + "group due to IO Error: %n %s", e.getMessage()));
    } catch (SlackApiException e) {
      logger.error(String.format("Failed to disable the "
             + "group due to Slack API Error: %n %s", e.getMessage()));
    }
    return false;
  }

  /**
   * Checks if the user group is currently enabled and if not, try to enable it
   * and return status.
   *
   * @param group to be checked
   * @returns an ArrayList
   */
  public boolean checkIfAvailable(Usergroup group) {
    logger.info("Checking if the {} is available...", group.getName());
    if (group.getDateDelete() != 0) {
      logger.info("The group {} seems to be disabled: it will now be enabled", group.getName());
      return enableUsergroup(group.getId());
    }
    logger.info("The group {} is available", group.getName()); 
    return true;
  }

  /**
   * Updates the user group's user list (the last step of joining/leaving the user
   * group).
   *
   * @param users   list containing new active users in group
   * @param groupId of the group to be modified
   * @return true, if the Slack API's updateUsergroupUserlist method is
   *         successful. Returns false if the method fails
   */
  public boolean updateUsergroupUserlist(List<String> users, String groupId) {
    logger.info("Trying to update the group's user list...");
    try {
      UsergroupsUsersUpdateResponse resp =
          client.usergroupsUsersUpdate(UsergroupsUsersUpdateRequest.builder()
          .token(System.getenv("SLACK_BOT_TOKEN")).usergroup(groupId).users(users).build());
      if (!resp.isOk()) {
        logger.warn("Failure updating usergroup users: {}", resp.getError());
      }
      logger.info("The group's user list successfully updated");
      return resp.isOk();
    } catch (IOException e) {
      logger.error(String.format("Failed to update the group's "
             + "user list due to IO Error: %n %s", e.getMessage()));
    } catch (SlackApiException e) {
      logger.error(String.format("Failed to update the group's "
             + "user list due to Slack API Error: %n %s", e.getMessage()));
    }
    return false;
  }
}