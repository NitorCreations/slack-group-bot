package hh.slackbot.Slackbot;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.AppMentionEvent;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SlackAppTest {

  @Autowired
  SlackApp slackApp;
	
  @MockBean
  private EventsApiPayload<AppMentionEvent> mockReq;
  
  @MockBean
  private EventContext mockCtx;
  
  @BeforeEach
  public void init() {
	  MockitoAnnotations.openMocks(this);
  }
  

  @Test
  @DisplayName("SlackApp is not null")
  void returnAppSuccessfully() {
	  assertNotNull(slackApp.initSlackApp());  
  }
  
  
  @Test
  @DisplayName("SlackApp mention response is successful")
  void mentionResponseSuccessfully() throws IOException, SlackApiException {

	  //mockReq and mockCtx are defined with @MockBean
	  SlackApp.mentionResponse(mockReq, mockCtx);
	 
	  verify(mockCtx).say("Greetings :wave:");
	  verify(mockCtx).ack();  
  }
}
