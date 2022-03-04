package hh.slackbot.slackbot.util;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostEphemeralRequest;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageUtil {

  @Autowired
  private Slack slack;

  private static final Logger logger = LoggerFactory.getLogger(MessageUtil.class);

  private static final String TOKEN = System.getenv("SLACK_BOT_TOKEN");

  /**
   * Send a direct message to the specified user.
   *
   * @param message to be sent
   * @param userId  of the receiving user
   * @return whether the message was sent successfully.
   */
  public boolean sendDirectMessage(String message, String userId) {
    try {
      slack.methods().chatPostMessage(
          ChatPostMessageRequest.builder()
              .token(TOKEN)
              .channel(userId)
              .text(message)
              .build());
      return true;
    } catch (IOException e) {
      logger.error(
          String.format("IOException while sending direct message to user %n%s", e.getMessage()));
    } catch (SlackApiException e) {
      logger.error(
          String.format("API exception while sending direct message to user %n%s", e.getMessage()));
    }

    return false;
  }

  /**
   * Send an ephemeral message to the user in specified channel.
   *
   * @param message to be sent
   * @param userId of the receiving user
   * @param channelId of the chaannel where the message will be visible
   * @return whether the message was sent successfully.
   */
  public boolean sendEphemeralResponse(String message, String userId, String channelId) {
    try {
      slack.methods().chatPostEphemeral(
          ChatPostEphemeralRequest.builder()
              .token(TOKEN)
              .channel(channelId)
              .user(userId)
              .text(message)
              .build());
      return true;
    } catch (IOException e) {
      logger.error(
          String.format("IOException while sending direct message to user %n%s", e.getMessage()));
    } catch (SlackApiException e) {
      logger.error(
          String.format("API exception while sending direct message to user %n%s", e.getMessage()));
    }

    return false;
  }
}
