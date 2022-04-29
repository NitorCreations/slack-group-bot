package hh.slackbot.slackbot;

import com.google.gson.JsonObject;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload.Action;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.model.Usergroup;
import hh.slackbot.slackbot.util.MessageUtil;
import hh.slackbot.slackbot.util.RestService;
import hh.slackbot.slackbot.util.UsergroupUtil;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockActionHandler {

  @Autowired
  private UsergroupUtil usergroupUtil;

  @Autowired
  private MessageUtil messageUtil;

  @Autowired
  private UsergroupHandler usergroupHandler;

  @Autowired
  private RestService restService;

  @Autowired
  private AppHomeHandler homeHandler;

  private static final Logger logger = LoggerFactory.getLogger(BlockActionHandler.class);
  
  public Response handleBlockJoinAction(BlockActionRequest req, ActionContext ctx) {
    Response resp = ctx.ack();
    BlockActionPayload payload = req.getPayload();
    List<Action> actions = payload.getActions();
    if (actions.isEmpty()) {
      logger.error("payload contained no actions, unable to proceed with request");
      return resp;
    }

    String userId = payload.getUser().getId();
    String groupName = actions.get(0).getValue();
    Usergroup usergroup = usergroupUtil.getGroupByName(groupName);
    String channelId;

    if (payload.getChannel() == null) {
      channelId = userId;
    } else {
      channelId = payload.getChannel().getId();
    }

    if (usergroup == null) {
      messageUtil.sendEphemeralResponse(
          String.format("You could not join the group %s. "
          + "This might happen if the group does not exist "
          + "or there has been an "
          + "unexpected I/O or Slack API error :x:", groupName), userId, channelId);
      return resp;
    }

    if (!usergroupHandler.addUserToGroup(userId, usergroup, channelId)) {
      return resp;
    }

    // Deleting the ephemeral blockkit message if event came from a channel
    if (userId != channelId) {
      if (!deleteMessage(req.getResponseUrl())) {
        logger.error("Failed to delete ephemeral message");
      }
    } else {
      // else refresh home view
      homeHandler.publishView(userId);
    }
    
    return resp;
  }

  public Response handleBlockCreateAction(BlockActionRequest req, ActionContext ctx) {
    Response resp = ctx.ack();
    BlockActionPayload payload = req.getPayload();
    List<Action> actions = payload.getActions();
    if (actions.isEmpty()) {
      logger.error("Payload contained no actions, unable to proceed with the request");
      return resp;
    }

    String userId = payload.getUser().getId();
    String channelId = payload.getChannel().getId();
    String groupName = actions.get(0).getValue();
    Usergroup usergroup = usergroupUtil.createUsergroup(groupName);
    
    if (usergroup == null) {
      messageUtil.sendEphemeralResponse(
          String.format("Due to an unexpected I/O or Slack API error, "
          + "you could not join the group %s :x:", groupName), userId, channelId);
      return resp;
    }

    if (!usergroupHandler.addUserToGroup(userId, usergroup, channelId)) {
      return resp;
    }

    // Deleting the ephemeral blockkit message if event came from a channel
    if (userId != channelId) {
      if (!deleteMessage(req.getResponseUrl())) {
        logger.error("Failed to delete ephemeral message");
      }
    } else {
      // else refresh home view
      homeHandler.publishView(userId);
    }

    return resp;
  }

  public Response handleBlockLeaveAction(BlockActionRequest req, ActionContext ctx) {
    Response resp = ctx.ack();
    BlockActionPayload payload = req.getPayload();
    List<Action> actions = payload.getActions();
    if (actions.isEmpty()) {
      logger.error("payload contained no actions, unable to proceed with request");
      return resp;
    }

    String userId = payload.getUser().getId();
    String groupName = actions.get(0).getValue();
    Usergroup usergroup = usergroupUtil.getGroupByName(groupName);
    String channelId;

    if (payload.getChannel() == null) {
      channelId = userId;
    } else {
      channelId = payload.getChannel().getId();
    }
    
    if (usergroup == null) {
      messageUtil.sendDirectMessage(
          String.format("You could not leave the group %s. "
          + "This might happen if the group does not exist "
          + "or there has been an "
          + "unexpected I/O or Slack API error :x:", groupName), userId);
      return resp;
    }

    if (!usergroupHandler.removeUserFromGroup(userId, usergroup, userId)) {
      return resp;
    }

    // Deleting the ephemeral blockkit message if event came from a channel
    if (userId != channelId) {
      if (!deleteMessage(req.getResponseUrl())) {
        logger.error("Failed to delete ephemeral message");
      }
    } else {
      // else refresh home view
      homeHandler.publishView(userId);
    }
    
    return resp;
  }

  private boolean deleteMessage(String responseUrl) {
    JsonObject json = new JsonObject();
    json.addProperty("response_type", "ephemeral");
    json.addProperty("text", "");
    json.addProperty("replace_original", true);
    json.addProperty("delete_original", true);

    return restService.postSlackResponse(responseUrl, json);
  }
  
}
