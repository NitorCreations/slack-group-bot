package hh.slackbot.slackbot;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.AppMentionEvent;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackApp {

  @Autowired
  private UsergroupHandler userGroupHandler;

  @Bean
  public App initSlackApp() {
    App app = new App();

    app.command("/groups", userGroupHandler::handleUsergroupCommand);

    app.event(AppMentionEvent.class, SlackApp::mentionResponse);

    return app;
  }

  public static Response mentionResponse(EventsApiPayload<AppMentionEvent> req, EventContext ctx)
      throws IOException, SlackApiException {
    ctx.say("Greetings :wave:");
    return ctx.ack();
  }
}
