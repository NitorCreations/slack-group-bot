package hh.slackbot.slackbot;

import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.model.Usergroup;
import hh.slackbot.slackbot.util.BlockMessager;
import hh.slackbot.slackbot.util.MessageUtil;
import hh.slackbot.slackbot.util.NameCompare;
import hh.slackbot.slackbot.util.UsergroupUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsergroupHandler {

  @Autowired
  private MessageUtil messageUtil;

  @Autowired
  private UsergroupUtil usergroupUtil;

  @Autowired
  private NameCompare comparer;

  @Autowired
  private BlockMessager blockMessager;

  private static final Logger logger = LoggerFactory.getLogger(UsergroupHandler.class);

  /**
   * Breaks the command's name and target from user input. Calls finalizeUsergroup
   * to finalize the user group method and calls messageUtil to send the correct
   * message to the user.
   *
   * @param req slack request object
   * @param ctx slack context object
   * @return ctx.ack()
   */
  public Response handleUsergroupCommand(SlashCommandRequest req, SlashCommandContext ctx) {
    SlashCommandPayload payload = req.getPayload();
    String userId = payload.getUserId();
    String responseChannel = payload.getChannelId();

    String[] params = payload.getText().split(" ", 2);
    String command = params[0];
    String usergroupName = params[1];

    if (finalizeUsergroupCommand(userId, command, usergroupName, responseChannel)) {
      return ctx.ack();
    } else {
      return ctx.ack("Command failed to execute");
    }
  }

  /**
   * Gets parameters from usergroupHandler, checks them and calls the correct user
   * group method based on them.
   *
   * @param userId        of the user making the command
   * @param command       given by the user
   * @param usergroupName name of groups to use the command on
   * @return message based on the success/error of the user group method
   */
  private boolean finalizeUsergroupCommand(
      String userId,
      String command,
      String usergroupName,
      String responseChannel
  ) {
    List<Usergroup> groups = usergroupUtil.getUserGroups();
    List<String> groupNames = groups.stream().map(group -> group.getName())
        .collect(Collectors.toList());

    List<String> similarNames = comparer.compareToList(usergroupName, groupNames);

    if (!similarNames.isEmpty() && command.equalsIgnoreCase("join")) {
      blockMessager.similarGroupsMessage(
          usergroupName,
          similarNames,
          responseChannel,
          userId
      );
      return true;
    }

    Usergroup usergroup = usergroupUtil.getGroupByName(usergroupName);

    if (usergroup == null) {
      usergroup = usergroupUtil.createUsergroup(usergroupName);
    }

    if (usergroup == null) {
      messageUtil.sendEphemeralResponse("usergroup not available", userId, responseChannel);
      return false;
    }

    if (command.equalsIgnoreCase("join")) {
      messageUtil.sendEphemeralResponse(
          String.format("You have joined group %s", usergroup.getName()),
          userId,
          responseChannel
      );
      return addUserToGroup(userId, usergroup, responseChannel);
    } else if (command.equalsIgnoreCase("leave")) {
      return removeUserFromGroup(userId, usergroup, responseChannel);
    } else {
      messageUtil.sendEphemeralResponse(
          String.format("invalid command: %s", command),
          userId,
          responseChannel
      );
      return false;
    }
  }

  /**
   * Adds the user into a new user group, if the certain conditions allow it.
   *
   * @param userId of the user to be added
   * @param group  object where to add the user to
   * @return true if the user was added to user group successfully. Returns false
   *         if the user already is in the user group.
   */
  public boolean addUserToGroup(String userId, Usergroup group, String responseChannel) {
    if (!usergroupUtil.checkIfAvailable(group)) {
      messageUtil.sendEphemeralResponse(
          String.format("Unable to enable the group %s", group.getName()),
          userId,
          responseChannel
      );
      return false;
    }
    List<String> users = group.getDateDelete() == 0 ? group.getUsers() : new ArrayList<>();

    if (usergroupUtil.userInGroup(userId, users)) {
      messageUtil.sendEphemeralResponse(
          String.format("You are already in the group %s", group.getName()),
          userId,
          responseChannel
      );
      return false;

    } else {
      users.add(userId);
      boolean success = usergroupUtil.updateUsergroupUserlist(users, group.getId());
      if (!success) {
        messageUtil.sendEphemeralResponse(
            String.format("Failed to add you to the group %s", group.getName()),
            userId,
            responseChannel
        );
      }
      return success;
    }
  }

  /**
   * Removes the user from user group, if certain conditions allow it.
   *
   * @param userId of the user to be removed
   * @param group  object where to remove the user from
   * @returns whether the operation was successful.
   */
  public boolean removeUserFromGroup(String userId, Usergroup group, String responseChannel) {
    List<String> users = group.getUsers();

    if (!usergroupUtil.userInGroup(userId, users) || group.getDateDelete() != 0) {
      messageUtil.sendEphemeralResponse(
          String.format("You are not in the group %s", group.getName()),
          userId,
          responseChannel
      );
      return false;
    }

    List<String> modifiedUsers = users.stream().filter(u -> !u.equals(userId))
        .collect(Collectors.toList());

    boolean result;
    if (modifiedUsers.isEmpty()) {
      result = usergroupUtil.disableUsergroup(group.getId());
    } else {
      result = usergroupUtil.updateUsergroupUserlist(modifiedUsers, group.getId());
    }

    if (result) {
      messageUtil.sendEphemeralResponse(
          String.format("You have been removed from the group %s", group.getName()),
          userId,
          responseChannel
      );
    } else {
      messageUtil.sendEphemeralResponse(
          String.format("Failed to remove you from the group %s", group.getName()),
          userId,
          responseChannel
      );
    }

    return result;
  }
}
