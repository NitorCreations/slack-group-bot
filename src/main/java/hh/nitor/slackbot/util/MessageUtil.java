package hh.nitor.slackbot.util;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatDeleteRequest;
import com.slack.api.methods.request.chat.ChatPostEphemeralRequest;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatDeleteResponse;
import com.slack.api.methods.response.chat.ChatPostEphemeralResponse;
import com.slack.api.model.block.LayoutBlock;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageUtil {

	@Autowired
	private MethodsClient client;

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
			logger.info("Direct message sent successfully to user");
			return client.chatPostMessage(
					ChatPostMessageRequest.builder().token(TOKEN).channel(userId).text(message).build()).isOk();
		} catch (IOException e) {
			logger.error(String.format("Failed to send a direct message to user " + "due to IOException: %n %s",
					e.getMessage()));
		} catch (SlackApiException e) {
			logger.error(String.format("Failed to send a direct message to user " + "due to API exception: %n %s",
					e.getMessage()));
		}

		return false;
	}

	/**
	 * Send an ephemeral message to the user in specified channel.
	 *
	 * @param message   to be sent
	 * @param userId    of the receiving user
	 * @param channelId of the chaannel where the message will be visible
	 * @return whether the message was sent successfully.
	 */
	public boolean sendEphemeralResponse(String message, String userId, String channelId) {
		try {
			logger.info("Ephemeral message sent successfully to user");
			return client.chatPostEphemeral(ChatPostEphemeralRequest.builder().token(TOKEN).channel(channelId)
					.user(userId).text(message).build()).isOk();
		} catch (IOException e) {
			logger.error(String.format("Failed to send an ephemeral message to user " + "due to IOException: %n %s",
					e.getMessage()));
		} catch (SlackApiException e) {
			logger.error(String.format("Failed to send an ephemeral message to user " + "due to API exception: %n %s",
					e.getMessage()));
		}

		return false;
	}

	/**
	 * Send an ephemeral message to the user in specified channel.
	 *
	 * @param blocks    list of blockit blocks
	 * @param text      backup text for blocks
	 * @param userId    of the receiving user
	 * @param channelId of the channel where the message will be visible
	 * @return whether the message was sent successfully.
	 */
	public boolean sendEphemeralResponse(List<LayoutBlock> blocks, String text, String userId, String channelId) {
		try {
			ChatPostEphemeralResponse resp = client.chatPostEphemeral(ChatPostEphemeralRequest.builder().token(TOKEN)
					.channel(channelId).user(userId).blocks(blocks).text(text).build());

			if (resp.isOk()) {
				logger.info("Ephemeral message (with block elements) " + "sent successfully to user");
				return true;
			}

			logger.error("Ephemeral message post has failed: {}", resp.getError());

		} catch (IOException e) {
			logger.error(String.format(
					"Failed to send an ephemeral message (with block elements) " + "to user due to IOException: %n %s",
					e.getMessage()));
		} catch (SlackApiException e) {
			logger.error(String.format(
					"Failed to send a ephemeral message (with block elements) " + "to user due to API exception: %n %s",
					e.getMessage()));
		}

		return false;
	}
}