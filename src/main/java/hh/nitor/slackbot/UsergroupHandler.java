package hh.nitor.slackbot;

import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.model.Usergroup;
import hh.nitor.slackbot.util.BlockMessager;
import hh.nitor.slackbot.util.MessageUtil;
import hh.nitor.slackbot.util.NameCompare;
import hh.nitor.slackbot.util.UsergroupUtil;
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
    logger.info("Starting HandleUsergroupCommand...");
    logger.info("Processing the payload...");
    SlashCommandPayload payload = req.getPayload();
    Response resp = ctx.ack();
    
    String command = "";
    
    String userId = payload.getUserId();
    String responseChannel = payload.getChannelId();
    
    logger.info("Processing the usergroup command's parameters...");

    if (payload.getText() == null) {
      messageUtil.sendEphemeralResponse(
          blockMessager.helpText(false), "help", userId, responseChannel);
      return resp;
    }

    String[] params = payload.getText().split(" ", 2);
    command = params[0].toLowerCase();
    
    if (!(command.equalsIgnoreCase("join") || command.equalsIgnoreCase("leave"))) {
      messageUtil.sendEphemeralResponse(
          blockMessager.helpText(false), "help", userId, responseChannel);
      return resp;
    }

    // "/groups"
    if (params.length < 2) {
      messageUtil.sendEphemeralResponse(
          "Missing group name. Find more info by typing: /groups help", userId, responseChannel);
      return resp;
    }

    // "/groups help"
    if (command.equalsIgnoreCase("help")) {
      messageUtil.sendEphemeralResponse(
          blockMessager.helpText(false), "help", userId, responseChannel);
      return resp;
    }

    String usergroupName = params[1].toLowerCase();
    // "/groups join/leave group_name"
    if (!finalizeUsergroupCommand(userId, command, usergroupName, responseChannel)) {
      messageUtil.sendEphemeralResponse(
          blockMessager.helpText(true), "help", userId, responseChannel);

      logger.error("The operation to {} the group {} has failed", command, usergroupName);

      return ctx.ack("The operation has failed: please check "
          + "that you have written the command correctly :x:");
    }
    
    logger.info("The operation to {} the group {} has been successful", command, usergroupName);
    return resp;
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

    logger.info("The user wants to {} the group {}", command, usergroupName);
    logger.info("Checking for groups with similar names as {}", usergroupName);
    List<Usergroup> groups = usergroupUtil.getUserGroups();
    List<String> groupNames = groups.stream().map(group -> group.getName())
        .collect(Collectors.toList());

    List<String> similarNames = comparer.compareToList(usergroupName, groupNames);

    if (!similarNames.isEmpty() && command.equalsIgnoreCase("join")) {
      logger.info("Found groups that have similar names with the group {}", usergroupName);
      blockMessager.similarGroupsMessage(
          usergroupName,
          similarNames,
          responseChannel,
          userId
      );
      
      return true;
    }

    logger.info("The group name {} is unique: "
           + "no similarities with other group names", usergroupName);
    Usergroup usergroup = usergroupUtil.getGroupByName(usergroupName);

    if (usergroup == null) {
      logger.info("The group {} does not exist", usergroupName);
      usergroup = usergroupUtil.createUsergroup(usergroupName);
    }

    if (usergroup == null) {
      messageUtil.sendEphemeralResponse(
          String.format("Due to an unexpected "
           + "I/O or Slack API error, "
           + "the group %s was not found or created :warning:", usergroupName),
          userId, 
          responseChannel
      );
      return false;
    }

    if (command.equalsIgnoreCase("join")) {
      return addUserToGroup(userId, usergroup, responseChannel);

    } else if (command.equalsIgnoreCase("leave")) {
      return removeUserFromGroup(userId, usergroup, responseChannel);

    } else {
      logger.error("The command {} does not exist", command);
      messageUtil.sendEphemeralResponse(
          String.format("The command %s is incorrect or does not exist. "
           + "Please write \"/groups help\" to see the accurate commands", command),
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
          String.format("Due to an unexpected "
          + "I/O or Slack API error, "
          + "the group %s was found but not enabled :warning:", group.getName()),
          userId,
          responseChannel
      );
      return false;
    }
    List<String> users = group.getDateDelete() == 0 ? group.getUsers() : new ArrayList<>();

    if (usergroupUtil.userInGroup(userId, users)) {
      logger.error("The user can only join groups they are not in");
      messageUtil.sendEphemeralResponse(
          String.format("You are already in the group %s. "
          + "You can only join groups "
          + " you are not a part of :warning:", group.getName()),
          userId,
          responseChannel
      );
      return false;

    } else {
      users.add(userId);
      boolean success = usergroupUtil.updateUsergroupUserlist(users, group.getId());
      if (!success) {
        messageUtil.sendEphemeralResponse(
            String.format("Due to an unexpected I/O or Slack API error, "
            + "you could not be added to the group %s :warning:", group.getName()),
            userId,
            responseChannel
        );
      } else {
        messageUtil.sendEphemeralResponse(
            String.format("You have been added to group %s :white_check_mark:", group.getName()),
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
      logger.error("The user can only leave groups they are in");
      messageUtil.sendEphemeralResponse(
          String.format("You are not in the group %s. "
          + "You can only leave groups "
          + "you are a part of :warning:", group.getName()),
          userId,
          responseChannel
      );
      return false;
    }

    logger.info("Checking if the user is the last member of the group {}", group.getName());
    List<String> modifiedUsers = users.stream().filter(u -> !u.equals(userId))
        .collect(Collectors.toList());

    boolean result;
    if (modifiedUsers.isEmpty()) {
      logger.info("The group {} will have no members "
             + "after the user has left it", group.getName());
      logger.info("The group {} will be disabled", group.getName());
      result = usergroupUtil.disableUsergroup(group.getId());
      
    } else {
      logger.info("The group {} will still have members "
             + "after the user has left it", group.getName());
      logger.info("Removing the user from the group {}...", group.getName());
      result = usergroupUtil.updateUsergroupUserlist(modifiedUsers, group.getId());
    }

    if (result) {
      messageUtil.sendEphemeralResponse(
          String.format(
              "You have been removed from the group %s :white_check_mark:", 
              group.getName()
          ),
          userId,
          responseChannel
      );
    } else {
      messageUtil.sendEphemeralResponse(
          String.format("Due to an unexpected I/O or Slack API error, "
          + "you could not be removed from the group  %s :warning:", group.getName()),
          userId,
          responseChannel
      );
    }

    return result;
  }
}