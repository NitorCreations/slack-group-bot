package hh.slackbot.slackbot.util;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.element.BlockElements.*;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.element.BlockElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockMessager {

  @Autowired
  private MessageUtil msgUtil;

  private static final Logger logger = LoggerFactory.getLogger(MessageUtil.class);

  public boolean similarGroupsMessage(
      String actual,
      List<String> similar,
      String channelId,
      String userId
  ) {
    List<LayoutBlock> blocks = similarGroupsLayout(actual, similar);
    return msgUtil.sendEphemeralResponse(blocks, "Groups with similar names", userId, channelId);
  }

  private List<LayoutBlock> similarGroupsLayout(String actual, List<String> similar) {
    List<LayoutBlock> layout = new ArrayList<>();
    List<BlockElement> blocks = stringsToButtons(similar);

    layout.add(
        section(section ->
          section
            .text(markdownText(String.format("Given group was *%s*", actual)))
            .accessory(
              button(b -> 
                b.text(plainText(pt -> pt.text("create"))).value(actual).actionId("btn_create")
              )
            ).blockId("header"))
    );

    layout.add(divider());

    layout.add(
        section(section -> section.text(
          plainText(pt -> pt.text("Did you mean one of these? click to join."))
        ).blockId("similar"))
    );
    layout.add(actions(actions -> actions.elements(blocks).blockId("asdf")));


    return layout;
  }

  private List<BlockElement> stringsToButtons(List<String> strings) {
    return strings
        .stream()
        .map(string -> 
          button(b ->
            b.text(plainText(pt -> pt.text(string))).value(string).actionId("join_" + string)
        ))
        .collect(Collectors.toList());
  }
}