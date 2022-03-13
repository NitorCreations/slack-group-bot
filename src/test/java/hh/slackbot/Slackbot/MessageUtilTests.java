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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.Assert;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MessageUtilTests {

  private static Logger logger = LoggerFactory.getLogger(MessageUtilTests.class);

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
  public void directMessageBuiltRight() throws IOException, SlackApiException {
    ChatPostMessageResponse resp = new ChatPostMessageResponse();
    resp.setOk(true);
    when(client.chatPostMessage(any(ChatPostMessageRequest.class))).thenReturn(resp);

    String msg = "message";
    String userId = "12345";
    msgUtil.sendDirectMessage(msg, userId);
    verify(client).chatPostMessage(msgReqCaptor.capture());
    ChatPostMessageRequest req = msgReqCaptor.getValue();
    logger.info(req.toString());
    
    Assert.isTrue(req.getText().equals(msg), "Request message matches input message");
    Assert.isTrue(req.getChannel().equals(userId), "Request channel matches input user ID");
  }

  @Captor
  ArgumentCaptor<ChatPostEphemeralRequest> ephReqCaptor;

  @Test
  public void ephemeralMessageBuiltRight() throws IOException, SlackApiException {
    ChatPostEphemeralResponse resp = new ChatPostEphemeralResponse();
    resp.setOk(true);
    when(client.chatPostEphemeral(any(ChatPostEphemeralRequest.class))).thenReturn(resp);

    String msg = "message";
    String userId = "12345";
    String channelId = "54321";
    msgUtil.sendEphemeralResponse(msg, userId, channelId);
    verify(client).chatPostEphemeral(ephReqCaptor.capture());
    ChatPostEphemeralRequest req = ephReqCaptor.getValue();
    logger.info(req.toString());
    
    Assert.isTrue(req.getText().equals(msg), "Request message matches input message");
    Assert.isTrue(req.getUser().equals(userId), "Request user matches input user ID");
    Assert.isTrue(req.getChannel().equals(channelId), "Request channel matches input channel ID");
  }
    
}
