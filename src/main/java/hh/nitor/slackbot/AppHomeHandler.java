package hh.nitor.slackbot;

import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.button;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.views.ViewsPublishRequest;
import com.slack.api.methods.response.views.ViewsPublishResponse;
import com.slack.api.model.Usergroup;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.event.AppHomeOpenedEvent;
import com.slack.api.model.view.View;
import hh.nitor.slackbot.util.UsergroupUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppHomeHandler {

  private static final Logger logger = LoggerFactory.getLogger(AppHomeHandler.class);

  private static final String TOKEN = System.getenv("SLACK_BOT_TOKEN");

  @Autowired
  private UsergroupUtil util;

  @Autowired
  private MethodsClient client;

  public Response handleEvent(EventsApiPayload<AppHomeOpenedEvent> req, EventContext ctx) {
    Response resp = ctx.ack();
    String userId = req.getEvent().getUser();
    publishView(userId);
    return resp;
  }

  public boolean publishView(String userId) {
    try {
      ViewsPublishResponse r = client.viewsPublish(
          ViewsPublishRequest
            .builder()
            .userId(userId)
            .view(generateView(userId))
            .token(TOKEN)
            .build()
      );
      if (!r.isOk()) {
        logger.error(r.getError());
        logger.error(r.getResponseMetadata().getMessages().toString());
      } else {
        return true;
      }
    } catch (IOException e) {
      logger.info("IOException while sending App Home message");
    } catch (SlackApiException e) {
      logger.info("Slack Api Exception while sending App Home message");
    }

    return false;
  }

  private View generateView(String userId) {
    List<LayoutBlock> layout = new ArrayList<>();
    List<Usergroup> groups = util.getUserGroups();
    layout.addAll(groupsToElements(groups, userId));
    View view = View
        .builder()
        .blocks(layout)
        .type("home")
        .build();
    return view;
  }

  private List<LayoutBlock> groupsToElements(List<Usergroup> groups, String userId) {
    List<LayoutBlock> elements = new ArrayList<>();
    List<Usergroup> enabled = new ArrayList<>();
    List<Usergroup> disabled = new ArrayList<>();
    List<Usergroup> isMember = new ArrayList<>();

    for (Usergroup g : groups) {
      if (g.getDateDelete() != 0) {
        disabled.add(g);
      } else if (g.getUsers().contains(userId)) {
        isMember.add(g);
      } else {
        enabled.add(g);
      }
    }

    elements.add(
        section(section -> section.text(
          markdownText(
            "*Usergroups you are currently part of*"
          )
        ).blockId("member"))
    );

    if (isMember.isEmpty()) {
      elements.add(
          section(section -> section.text(
            markdownText(
              "~~~~~~~~~~:leaves:"
            )
          ).blockId("empty_member"))
      );
    }

    for (int i = 0; i < isMember.size(); i++) {
      Usergroup g = isMember.get(i);
      String name = g.getName();
      elements.add(section(section ->
          section.text(
            plainText(String.format("%s has %d members", name, g.getUsers().size()))
          ).accessory(
            button(b -> b.text(plainText("Leave group")).value(name).actionId("leave_" + name))
          ).blockId(g.getId())));

      elements.add(divider());
    }

    elements.add(
        section(section -> section.text(
          markdownText(
            "*Currently available usergroups*\n"
            + "Join your friends in these groups :ghost:"
          )
        ).blockId("available"))
    );

    if (enabled.isEmpty()) {
      elements.add(
          section(section -> section.text(
            markdownText(
              "~~~~~~~~~~:leaves:"
            )
          ).blockId("empty_available"))
      );
    }

    for (int i = 0; i < enabled.size(); i++) {
      Usergroup g = enabled.get(i);
      String name = g.getName();
      elements.add(section(section ->
          section.text(
            plainText(String.format("%s has %d members", name, g.getUsers().size()))
          ).accessory(
            button(b -> b.text(plainText("Join group")).value(name).actionId("join_" + name))
          ).blockId(g.getId())));

      elements.add(divider());
    }

    elements.add(
        section(section -> section.text(
          markdownText(
            "*Usergroups that have currently no members*\n"
            + "Bring some life to these groups :seedling:"
          )
        ).blockId("disabled"))
    );

    if (disabled.isEmpty()) {
      elements.add(
          section(section -> section.text(
            markdownText(
              "~~~~~~~~~~:leaves:"
            )
          ).blockId("empty_disabled"))
      );
    }

    for (int i = 0; i < disabled.size(); i++) {
      Usergroup g = disabled.get(i);
      String name = g.getName();
      elements.add(section(section ->
          section.text(
            plainText(String.format("%s is currently disabled", name, g.getUsers().size()))
          ).accessory(
            button(b -> b.text(plainText("Join group")).value(name).actionId("join_" + name))
          ).blockId(g.getId())));
      if (i < disabled.size() - 1) {
        elements.add(divider());
      }
    }

    return elements;
  }
}
