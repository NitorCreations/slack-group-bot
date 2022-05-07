package hh.nitor.slackbot.util;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.button;

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

  private static final Logger logger = LoggerFactory.getLogger(BlockMessager.class);

  /**
   * Attempts to send a response to user trying to join a
   * usergroup that doesn't exist but is similar to already existing
   * usergroups.
   * 
   * @param actual name specified by user
   * @param similar names already found
   * @param channelId where user sent the command
   * @param userId Slack ID of user
   * @return whether message was sent to user successfully.
   */
  public boolean similarGroupsMessage(
      String actual,
      List<String> similar,
      String channelId,
      String userId
  ) {
    List<LayoutBlock> blocks = similarGroupsLayout(actual, similar);
    logger.info("Interactive Block Message sent successfully to the user");
    return msgUtil.sendEphemeralResponse(blocks, "Groups with similar names", userId, channelId);
  }
  
  /**
   * Generates a Slack Block layout for responding to users
   * attempting to join new usergroups with similar names to existing ones.
   * 
   * @param actual usergroup name specified by user
   * @param similar list of similar names
   * @return LayoutBlock list
   */
  private List<LayoutBlock> similarGroupsLayout(String actual, List<String> similar) {
    List<LayoutBlock> layout = new ArrayList<>();
    logger.info("Creating an Interactive Block Message of similar group names...");
    layout.add(
        section(section ->
          section
            .text(markdownText(String.format("Given group was *%s*", actual)))
            .accessory(
              button(b -> 
                b.text(
                  plainText(pt -> pt.text("Create and join"))
                ).value(actual).actionId("btn_create")
              )
            ).blockId("header"))    
    );

    layout.add(divider());
    layout.add(
        section(section -> section.text(
          plainText(pt -> pt.text(":question: Did you mean one of these? Click to join:"))
        ).blockId("similar"))
    );
    
    List<BlockElement> blocks = stringsToJoinButtons(similar);
    layout.add(actions(actions -> actions.elements(blocks).blockId("asdf")));

    return layout;
  }

  /**
   * Maps a list of strings to Slack Block buttons with actions to join
   * groups with names equal to the strings.
   * @param strings
   * @return BlockElement list
   */
  private List<BlockElement> stringsToJoinButtons(List<String> strings) {
    return strings
        .stream()
        .map(string -> 
          button(b ->
            b.text(plainText(pt -> pt.text(string))).value(string).actionId("join_" + string)
        ))
        .collect(Collectors.toList());
  }
  
  /**
   * Generates help text for user regarding the use of commands
   * provided by the bot.
   * 
   * @param showFailInfo
   * @return a Slack Block layout based on whether responding to a failed command.
   */
  public List<LayoutBlock> helpText(boolean showFailInfo) {
    List<LayoutBlock> helpLayout = new ArrayList<>();

    if (!showFailInfo) {
      helpLayout.add(section(s -> s.fields(
          List.of(
            plainText(
              "This bot helps you to join or leave Slack user groups. "
              + "A new user group can be automatically created "
              + "when trying to join a non-existing group. "
              + "You will be notified if similar group names already exist."
            ),
            plainText(
              "Available slash commands below:\n"
              + "/groups join [group_name]\n"
              + "/groups leave [group_name]\n"
            )
          )
      ).blockId("botInfo")));
        
    } else {
        
      helpLayout.add(section(section -> section.text(markdownText(String.format(
          "Command failed to execute :x:"
          ))).blockId("fail"))
      );
      
      helpLayout.add(divider());
      
      helpLayout.add(section(section -> section.text(plainText(pt -> pt.text(
          "Available slash commands below:"
          ))).blockId("commands"))
      );
      
      helpLayout.add(section(section -> section.text(plainText(pt -> pt.text(
          "/groups help"
          ))).blockId("help"))
      );

      helpLayout.add(section(section -> section.text(plainText(pt -> pt.text(
          "/groups join [group_name]"
          ))).blockId("joinCommand"))
      );
      
      helpLayout.add(section(section -> section.text(plainText(pt -> pt.text(
          "/groups leave [group_name]"
          ))).blockId("leaveCommand"))
      );
      
      helpLayout.add(divider());
    }

    return helpLayout;
  }
}
