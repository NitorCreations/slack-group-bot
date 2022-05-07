package hh.nitor.slackbot;

import com.google.gson.JsonObject;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload.Action;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.model.Usergroup;
import hh.nitor.slackbot.util.MessageUtil;
import hh.nitor.slackbot.util.RestService;
import hh.nitor.slackbot.util.UsergroupUtil;
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

  private static final Logger logger = LoggerFactory.getLogger(BlockActionHandler.class);
  
  /**
   * Handler for block actions that users use to join usergroups.
   * 
   * @param req Slack API BlockActionRequest
   * @param ctx Slack API ActionContext
   * @return Response - acknowledgement to the Slack server that the request was handled.
   */
  public Response handleBlockJoinAction(BlockActionRequest req, ActionContext ctx) {
    logger.info("Starting HandleBlockJoinAction...");
    logger.info("Processing the payload...");
    Response resp = ctx.ack();
    BlockActionPayload payload = req.getPayload();
    List<Action> actions = payload.getActions();
    if (actions.isEmpty()) {
      logger.error("payload contained no actions: unable to proceed with request");
      return resp;
    }

    String userId = payload.getUser().getId();
    String channelId = payload.getChannel().getId();
    String groupName = actions.get(0).getValue();
    Usergroup usergroup = usergroupUtil.getGroupByName(groupName);

    if (usergroup == null) {
      logger.error("The group {} does not exist", groupName);
      messageUtil.sendEphemeralResponse(
          String.format("You could not not be addded to the group %s. "
          + "The group does not exist :x:", groupName), userId, channelId);
    } else if (!usergroupHandler.addUserToGroup(userId, usergroup, channelId)) {
      messageUtil.sendEphemeralResponse(
          String.format("Failed to add you to the group %s :x:", groupName),
          userId, channelId);
      return resp;
    }
    logger.info("Creating a Json Object with properties...");
    JsonObject json = new JsonObject();
    json.addProperty("response_type", "ephemeral");
    json.addProperty("text", "");
    json.addProperty("replace_original", true);
    json.addProperty("delete_original", true);

    logger.info("Calling Rest Service's PostSlackResponse");
    restService.postSlackResponse(req.getResponseUrl(), json);

    logger.info("Returning response...");
    return resp;
  }

  /**
   * Handler for block actions that users use to create new usergroups.
   * 
   * @param req Slack API BlockActionRequest
   * @param ctx Slack API ActionContext
   * @return Response - acknowledgement to the Slack server that the request was handled.
   */
  public Response handleBlockCreateAction(BlockActionRequest req, ActionContext ctx) {

    logger.info("Starting handleBlockCreateAction...");
    logger.info("Processing the payload...");
    Response resp = ctx.ack();
    BlockActionPayload payload = req.getPayload();
    List<Action> actions = payload.getActions();
    if (actions.isEmpty()) {
      logger.error("Payload contained no actions: unable to proceed with request");
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
      messageUtil.sendEphemeralResponse(
          String.format("You could not join the group %s :x:", groupName), userId, channelId);
      return resp;
    }
    

    logger.info("Creating a Json Object with properties...");
    JsonObject json = new JsonObject();
    json.addProperty("response_type", "ephemeral");
    json.addProperty("text", "");
    json.addProperty("replace_original", true);
    json.addProperty("delete_original", true);

    logger.info("Calling Rest Service's PostSlackResponse");
    restService.postSlackResponse(req.getResponseUrl(), json);

    logger.info("Returning response...");
    return resp;
  }
}
