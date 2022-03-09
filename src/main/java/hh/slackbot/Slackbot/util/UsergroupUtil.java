package hh.slackbot.slackbot.util;

import com.slack.api.Slack;
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
import com.slack.api.model.Usergroup;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsergroupUtil {

  @Autowired
  private Slack slack;

  private static final Logger logger = LoggerFactory.getLogger(UsergroupUtil.class);

  private static final String TOKEN = System.getenv("SLACK_BOT_TOKEN");

  /**
   * Tries to retrieve a list of enabled usergroups in the workspace.
   *
   * @return A list containing all usergroup objects or an empty list. Returns
   *         null in case of an
   *         error.
   */
  public List<Usergroup> getUserGroups() {
    List<Usergroup> usergroups = null;
    logger.info("Retrieving user groups");

    try {
      UsergroupsListResponse resp = slack.methods().usergroupsList(
          UsergroupsListRequest.builder()
              .token(TOKEN)
              .includeDisabled(true)
              .includeUsers(true)
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
   * Tries to fetch a usergroup based on a given name.
   *
   * @param name of the group
   * @return Found group or null.
   */
  public Usergroup getGroupByName(String name) {
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
   * @param groupId of usergroup
   * @return A list of user id strings. Returns null in case of an error.
   */
  public List<String> getUsergroupUsers(String groupId) {
    List<String> users = null;
    try {
      UsergroupsUsersListResponse resp = slack.methods().usergroupsUsersList(
          UsergroupsUsersListRequest.builder().token(TOKEN).usergroup(groupId).build());

      users = resp.getUsers();

    } catch (IOException e) {
      logger.error(
          String.format("IO Error while getting usergroups users%n %s", e.getMessage()));
    } catch (SlackApiException e) {
      logger.error(String.format("Slack API Error while getting usergroups users%n %s",
          e.getMessage()));
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

    try {
      UsergroupsCreateResponse resp = slack.methods().usergroupsCreate(
          UsergroupsCreateRequest.builder().token(TOKEN).name(name).build());

      if (resp.isOk()) {
        group = resp.getUsergroup();
      }
    } catch (IOException e) {
      logger.error(
          String.format("IO Error while getting usergroups users%n %s", e.getMessage()));
    } catch (SlackApiException e) {
      logger.error(String.format("Slack API Error while getting usergroups users%n %s",
          e.getMessage()));
    }

    return group;
  }

  /**
   * Checks if the user is in the user group.
   *
   * @param userId of checkable user
   * @param users list
   * @returns true or false
   * 
   */
  public boolean userInGroup(String userId, List<String> users) {

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
   * Enables the user group.
   *
   * @param id of usergroup
   * @returns true. Returns false in case of an error.
   */
  public boolean enableUsergroup(String id) {
    try {
      UsergroupsEnableResponse resp = slack.methods().usergroupsEnable(
          UsergroupsEnableRequest.builder().token(TOKEN).usergroup(id).build());

      return resp.isOk();
    } catch (IOException e) {
      logger.error(String.format("IO Error while enabling usergroup%n %s", e.getMessage()));
    } catch (SlackApiException e) {
      logger.error(
          String.format("Slack API Error while enabling usergroup%n %s", e.getMessage()));
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
      UsergroupsDisableResponse resp = slack.methods().usergroupsDisable(
          UsergroupsDisableRequest.builder().token(TOKEN).usergroup(id).build());

      return resp.isOk();
    } catch (IOException e) {
      logger.error(String.format("IO Error while disabling usergroup%n %s", e.getMessage()));
    } catch (SlackApiException e) {
      logger.error(String.format("Slack API Error while disabling usergroup%n %s",
          e.getMessage()));
    }

    return false;
  }

  /**
   * Checks if usergroup is available and creates it if it is not.
   *
   * @param usergroup object
   * @param usergroupName target of commnd
   * @returns group or null in case of error
   */
  public Usergroup checkUsergroup(Usergroup usergroup, String usergroupName) {
    if (usergroup == null) {
      return createUsergroup(usergroupName);
    }

    return usergroup;
  }

  /**
   * Checks if the user group is currently enabled and if not,
   * try to enable it and return status.
   *
   * @param group to be checked
   * @returns an ArrayList
   */
  public boolean checkIfAvailable(Usergroup group) {
    if (group.getDateDelete() != 0) {
      return enableUsergroup(group.getId());
    }

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
    try {
      slack.methods().usergroupsUsersUpdate(UsergroupsUsersUpdateRequest.builder()
          .token(System.getenv("SLACK_BOT_TOKEN")).usergroup(groupId).users(users).build());
      return true;
    } catch (IOException e) {
      logger.error(String.format("IO Error while updating usergroup%n %s", e.getMessage()));
    } catch (SlackApiException e) {
      logger.error(String.format("Slack API Error while updating usergroup%n %s", e.getMessage()));
    }

    return false;
  }
}
