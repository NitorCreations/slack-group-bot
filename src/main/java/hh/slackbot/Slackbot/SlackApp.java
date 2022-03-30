package hh.slackbot.slackbot;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.element.BlockElements.*;

import com.slack.api.Slack;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.event.AppMentionEvent;
import hh.slackbot.slackbot.util.UsergroupUtil;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
  private UsergroupUtil util;

  private static Logger logger = LoggerFactory.getLogger(SlackApp.class);

  @Bean
  public App initSlackApp() {
    App app = new App();

    app.command("/groups", userGroupHandler::handleUsergroupCommand);

    app.event(AppMentionEvent.class, (req, ctx) -> mentionResponse(req, ctx));

    app.blockAction(Pattern.compile("^join_.+$"), (req, ctx) -> {
      logger.info("{}", req.getPayload().getActions().get(0).getValue());
      return ctx.ack();
    });

    return app;
  }

  public Response mentionResponse(EventsApiPayload<AppMentionEvent> req, EventContext ctx)
      throws IOException, SlackApiException {
    MethodsClient client = Slack.getInstance().methods();
    ChatPostMessageResponse resp = client.chatPostMessage(
        r -> r.channel(ctx.getChannelId())
        .blocks(asBlocks(
          section(section -> section.text(markdownText("*All groups*")).blockId("b1")),
          divider(),
          actions(actions -> actions
            .elements(asElements(
              staticSelect(s -> s
                .actionId("xyz123")
                .options(
                  util.getUserGroups()
                  .stream()
                  .map(group -> option(plainText(group.getName() + (group.getDateDelete() != 0 ? " (empty)" : "")), group.getId()))
                  .collect(Collectors.toList())
                )
                .placeholder(plainText("select group"))
                
              )
            ))
            .blockId("b2")
          )
        ))
        .text("selector")
        .token(System.getenv("SLACK_BOT_TOKEN"))
    );
    logger.info("Status {}", resp.getErrors());
    ctx.say("Greetings :wave:");
    return ctx.ack();
  }
}
