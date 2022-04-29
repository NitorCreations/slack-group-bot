package hh.nitor.slackbot;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.AppMentionEvent;
import java.io.IOException;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackApp {

  @Autowired
  private UsergroupHandler userGroupHandler;

  @Autowired
  private BlockActionHandler blockActionHandler;

  private static Logger logger = LoggerFactory.getLogger(SlackApp.class);

  @Bean
  public App initSlackApp() {

    logger.info("Receiving interaction from the user...");
    
    App app = new App();

    app.command("/groups", userGroupHandler::handleUsergroupCommand);

    app.event(AppMentionEvent.class, (req, ctx) -> mentionResponse(req, ctx));

    app.blockAction(Pattern.compile("^join_.+$"), blockActionHandler::handleBlockJoinAction);

    app.blockAction(Pattern.compile("btn_create"), blockActionHandler::handleBlockCreateAction);

    return app;
  }

  public Response mentionResponse(EventsApiPayload<AppMentionEvent> req, EventContext ctx)
      throws IOException, SlackApiException {
    logger.info("Starting MentionResponse...");
    ctx.say("Greetings :wave:");
    return ctx.ack();
  }
}
