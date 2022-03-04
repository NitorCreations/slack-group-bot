package hh.slackbot.slackbot;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.model.Usergroup;
import hh.slackbot.slackbot.util.MessageUtil;
import hh.slackbot.slackbot.util.UsergroupUtil;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UsergroupHandlerTest {
  @Autowired
  UsergroupHandler groupHandler;

  @MockBean
  private UsergroupUtil groupUtil;

  @MockBean
  private MessageUtil msgUtil;

  @BeforeAll
  public void init() {
    MockitoAnnotations.openMocks(this);

    // configuring what mocked items should return
    Usergroup emptyGroup = mock(Usergroup.class);
    when(emptyGroup.getId()).thenReturn("54321");
    when(groupUtil.getGroupByName("testgroup")).thenReturn(null);
    when(groupUtil.checkUsergroup(null, "testgroup")).thenReturn(emptyGroup);
    when(groupUtil.checkIfDisabled(emptyGroup)).thenReturn(new ArrayList<String>());
    when(groupUtil.userInGroup(eq("12345"), anyList())).thenReturn(false);
    when(groupUtil.updateUsergroupUserlist(any(), eq("54321"))).thenReturn(true);
    when(groupUtil.getUserGroups()).thenReturn(new ArrayList<Usergroup>());
  }

  @Test
  @DisplayName("add user to empty group")
  void addUserToGroupReturnsTrue() {
    SlashCommandPayload mockPayload = mock(SlashCommandPayload.class);
    when(mockPayload.getUserId()).thenReturn("12345");
    when(mockPayload.getText()).thenReturn("join testgroup");
    SlashCommandRequest mockReq = mock(SlashCommandRequest.class);
    when(mockReq.getPayload()).thenReturn(mockPayload);
    SlashCommandContext mockCtx = mock(SlashCommandContext.class);

    groupHandler.handleUsergroupCommand(mockReq, mockCtx);

    // check that ack() was called with no params
    verify(mockCtx).ack();
  }

  @Test
  @DisplayName("createUsergroup responds OK and returns group")
  void createUsergroupRespondsOkAndReturnsGroup() {

  }
}
