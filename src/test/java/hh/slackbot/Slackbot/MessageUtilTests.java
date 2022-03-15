package hh.slackbot.slackbot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostEphemeralRequest;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostEphemeralResponse;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import hh.slackbot.slackbot.util.MessageUtil;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MessageUtilTests {

  @Autowired
  private MessageUtil msgUtil;

  @MockBean
  private MethodsClient client;

  @BeforeAll
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Captor
  ArgumentCaptor<ChatPostMessageRequest> msgReqCaptor;

  @Test
  @DisplayName("Direct message test")
  public void directMessageBuiltRight() throws IOException, SlackApiException {
    ChatPostMessageResponse resp = new ChatPostMessageResponse();
    resp.setOk(true);
    when(client.chatPostMessage(any(ChatPostMessageRequest.class))).thenReturn(resp);

    String msg = "message";
    String userId = "12345";

    boolean result = msgUtil.sendDirectMessage(msg, userId);
    verify(client).chatPostMessage(msgReqCaptor.capture());
    ChatPostMessageRequest req = msgReqCaptor.getValue();
    
    Assertions.assertTrue(result, "Returns response status correctly");
    Assertions.assertTrue(req.getText().equals(msg), "Request message matches input message");
    Assertions.assertTrue(req.getChannel().equals(userId), "Request channel matches input user ID");
  }

  @Test
  @DisplayName("Direct message IOException returns false")
  public void directMessageIoException() throws IOException, SlackApiException {
    when(client.chatPostMessage(any(ChatPostMessageRequest.class)))
        .thenThrow(new IOException());

    String msg = "message";
    String userId = "12345";

    boolean result = msgUtil.sendDirectMessage(msg, userId);
    
    Assertions.assertFalse(result, "Return false after error");
  }

  @Captor
  ArgumentCaptor<ChatPostEphemeralRequest> ephReqCaptor;

  @Test
  @DisplayName("Ephemeral message test")
  public void ephemeralMessageBuiltRight() throws IOException, SlackApiException {
    ChatPostEphemeralResponse resp = new ChatPostEphemeralResponse();
    resp.setOk(true);
    when(client.chatPostEphemeral(any(ChatPostEphemeralRequest.class))).thenReturn(resp);

    String msg = "message";
    String userId = "12345";
    String channelId = "54321";

    boolean result = msgUtil.sendEphemeralResponse(msg, userId, channelId);
    verify(client).chatPostEphemeral(ephReqCaptor.capture());
    ChatPostEphemeralRequest req = ephReqCaptor.getValue();

    Assertions.assertTrue(result, "Returns response status correctly");
    Assertions.assertTrue(req.getText().equals(msg), "Request message matches input message");
    Assertions.assertTrue(req.getUser().equals(userId), "Request user matches input user ID");
    Assertions.assertTrue(
        req.getChannel().equals(channelId), "Request channel matches input channel ID");
  }

  @Test
  @DisplayName("Ephemeral message IOException returns false")
  public void ephemeralMessageIoException() throws IOException, SlackApiException {
    when(client.chatPostEphemeral(any(ChatPostEphemeralRequest.class)))
        .thenThrow(new IOException());

    String msg = "message";
    String userId = "12345";
    String channelId = "54321";

    boolean result = msgUtil.sendEphemeralResponse(msg, userId, channelId);
    
    Assertions.assertFalse(result, "Return false after error");
  }
}
