package hh.nitor.slackbot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.slack.api.model.Usergroup;
import com.slack.api.model.block.LayoutBlock;
import hh.nitor.slackbot.util.MessageUtil;
import hh.nitor.slackbot.util.UsergroupUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AppHomeHandlerTests {

  @Autowired
  private AppHomeHandler handler;

  @MockBean
  private UsergroupUtil groupUtil;

  @MockBean
  private MessageUtil msgUtil;

  @BeforeEach
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  private List<Usergroup> initUserGroups() {
    List<String> users = new ArrayList<String>(Arrays.asList("user1", "user2", "user3"));
    List<String> users2 = new ArrayList<String>(Arrays.asList("user1"));
    List<Usergroup> groups = Arrays.asList(
        Usergroup.builder()
          .id("1111")
          .users(users)
          .name("sample_group")
          .dateDelete(0)
          .build(),
        Usergroup.builder()
          .id("2222")
          .users(new ArrayList<String>())
          .name("empty_group")
          .dateDelete(0)
          .build(),
        Usergroup.builder()
          .id("3333")
          .users(users2)
          .name("single_group")
          .dateDelete(0)
          .build(),
        Usergroup.builder()
          .id("4444")
          .users(users2)
          .name("disabled_group")
          .dateDelete(1)
          .build(),
          Usergroup.builder()
          .id("5555")
          .users(users)
          .name("sample2_group")
          .dateDelete(0)
          .build()
    );

    return groups;
  }

  /**
   * Expected output is 12 elements.
   * 
   * <p>
   * title, group, divider, group, divider,
   * </p>
   * 
   * <p>
   * title, group, divider, group, divider,
   * </p>
   * 
   * <p>
   * title, group
   * </p>
   */
  @Test
  @Description("Right amount of block elements created based on groups")
  public void elementCountMatches() {
    String userId = "user2";
    List<Usergroup> groups = initUserGroups();
    List<LayoutBlock> blocks = handler.groupsToElements(groups, userId);
    assertEquals(12, blocks.size(), "Incorrect amount of blocks");
  }
  
}
