package hh.slackbot.slackbot;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.model.Usergroup;
import hh.slackbot.slackbot.util.MessageUtil;
import hh.slackbot.slackbot.util.UsergroupUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
  // TODO: change usergroup mocks to real objects
  @Autowired
  UsergroupHandler groupHandler;

  @MockBean
  private UsergroupUtil groupUtil;

  @MockBean
  private MessageUtil msgUtil;

  @BeforeAll
  public void init() {
    MockitoAnnotations.openMocks(this);

    // TODO: move to tests that use specific configs
    // configuring what mocked items should return
    Usergroup emptyGroup = mock(Usergroup.class);
    when(emptyGroup.getId()).thenReturn("54321");

    when(groupUtil.getGroupByName("empty group")).thenReturn(null);
    when(groupUtil.checkUsergroup(null, "empty group")).thenReturn(emptyGroup);
    when(groupUtil.checkIfAvailable(emptyGroup)).thenReturn(true);
    when(groupUtil.userInGroup(eq("12345"), anyList())).thenReturn(false);
    when(groupUtil.updateUsergroupUserlist(any(), eq("54321"))).thenReturn(true);
    when(groupUtil.getUserGroups()).thenReturn(new ArrayList<Usergroup>());
  }

  @Test
  @DisplayName("Remove user from group they are not in returns false")
  void removeUserFromGroupInvalid() {
    List<String> users = Arrays.asList("123", "234", "345");
    
    Usergroup group = mock(Usergroup.class);
    when(group.getName()).thenReturn("invalid group");
    when(group.getUsers()).thenReturn(users);
    
    String userId = "12345";
    assertFalse(groupHandler.removeUserFromGroup(userId, group));
    verify(msgUtil).sendDirectMessage("You are not in the group invalid group", userId);
  }

  @Test
  @DisplayName("Remove user from group they are not in returns false")
  void removeUserFromGroupValid() {
    List<String> users = Arrays.asList("22345", "234", "345");
    
    Usergroup group = mock(Usergroup.class);
    when(group.getName()).thenReturn("valid group");
    when(group.getUsers()).thenReturn(users);
    when(group.getId()).thenReturn("54321");
    when(groupUtil.userInGroup(eq("22345"), eq(users))).thenReturn(true);
    
    String userId = "22345";
    verifyNoInteractions(msgUtil);
    assertTrue(groupHandler.removeUserFromGroup(userId, group));

    List<String> modifiedUsers = users.stream().filter(u -> !u.equals(userId))
        .collect(Collectors.toList());
    verify(groupUtil).updateUsergroupUserlist(eq(modifiedUsers), eq("54321"));
  }

  @Test
  @DisplayName("Add user to empty group")
  void addUserToGroupFlowWorks() {
    SlashCommandPayload mockPayload = mock(SlashCommandPayload.class);
    when(mockPayload.getUserId()).thenReturn("12345");
    when(mockPayload.getText()).thenReturn("join empty group");
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
