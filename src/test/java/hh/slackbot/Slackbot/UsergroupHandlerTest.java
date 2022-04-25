package hh.slackbot.Slackbot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.model.Usergroup;

import hh.slackbot.Slackbot.UsergroupHandler;
import hh.slackbot.Slackbot.util.MessageUtil;
import hh.slackbot.Slackbot.util.UsergroupUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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

  @BeforeEach
  public void init() {
    MockitoAnnotations.openMocks(this);
    doCallRealMethod().when(groupUtil).userInGroup(anyString(), anyList());
    doCallRealMethod().when(groupUtil).getGroupByName(anyString());
    doCallRealMethod().when(groupUtil).checkIfAvailable(any());

    // override these in test functions if necessary
    when(groupUtil.enableUsergroup(anyString())).thenReturn(true);
    when(groupUtil.updateUsergroupUserlist(anyList(), anyString())).thenReturn(true);
    when(groupUtil.disableUsergroup(anyString())).thenReturn(true);
    initUserGroups();
  }

  private void initUserGroups() {
    List<String> users = new ArrayList<String>(Arrays.asList("user1", "user2", "user3"));
    List<String> users2 = new ArrayList<String>(Arrays.asList("user1"));
    List<Usergroup> groups = Arrays.asList(
        Usergroup.builder()
          .id("1111")
          .users(users)
          .name("sample group")
          .dateDelete(0)
          .build(),
        Usergroup.builder()
          .id("2222")
          .users(new ArrayList<String>())
          .name("empty group")
          .dateDelete(0)
          .build(),
        Usergroup.builder()
          .id("3333")
          .users(users2)
          .name("single group")
          .dateDelete(0)
          .build(),
        Usergroup.builder()
          .id("4444")
          .users(users2)
          .name("disabled group")
          .dateDelete(1)
          .build(),
          Usergroup.builder()
          .id("5555")
          .users(users)
          .name("sample2 group")
          .dateDelete(0)
          .build()
    );

    when(groupUtil.getUserGroups()).thenReturn(groups);
  }

  private SlashCommandContext callWithMockValues(String userId, String userInput) {
    SlashCommandPayload mockPayload = mock(SlashCommandPayload.class);
    when(mockPayload.getUserId()).thenReturn(userId);
    when(mockPayload.getText()).thenReturn(userInput);
    when(mockPayload.getChannelId()).thenReturn("channel_id");

    SlashCommandRequest mockReq = mock(SlashCommandRequest.class);
    when(mockReq.getPayload()).thenReturn(mockPayload);
    SlashCommandContext mockCtx = mock(SlashCommandContext.class);

    groupHandler.handleUsergroupCommand(mockReq, mockCtx);

    return mockCtx;
  }

  @Test
  @DisplayName("Add user to empty group")
  void addUserToEmptyGroup() {
    String userId = "user4";
    String userInput = "join empty group";
    SlashCommandContext mockCtx = callWithMockValues(userId, userInput);
    
    verify(mockCtx).ack();
  }

  @Test
  @DisplayName("Add user back to group they left from as last member")
  void addUserBackToDisabledGroup() {
    String userId = "user1";
    String userInput = "join disabled group";
    SlashCommandContext mockCtx = callWithMockValues(userId, userInput);

    verify(groupUtil)
          .updateUsergroupUserlist(new ArrayList<String>(Arrays.asList("user1")), "4444");
    verify(mockCtx).ack();
  }

  @Test
  @DisplayName("Try adding duplicate user to group")
  void addDuplicateUserToGroup() {
    String userId = "user1";
    String userInput = "join sample group";
    SlashCommandContext mockCtx = callWithMockValues(userId, userInput);

    verify(msgUtil).sendEphemeralResponse(
        String.format(anyString()), eq(userId), eq("channel_id"));
    verify(mockCtx).ack(anyString());
  }

  @Test
  @DisplayName("Remove user from a group they're in")
  void removeUserFromGroup() {
    String userId = "user1";
    String userInput = "leave sample group";
    SlashCommandContext mockCtx = callWithMockValues(userId, userInput);

    verify(mockCtx).ack();
  }

  @Test
  @DisplayName("Try to remove user from a group they're not in")
  void removeUserFromGroupAbsent() {
    String userId = "user4";
    String userInput = "leave sample group";
    SlashCommandContext mockCtx = callWithMockValues(userId, userInput);

    verify(msgUtil).sendEphemeralResponse(
        String.format(anyString()), eq(userId), eq("channel_id"));
    verify(mockCtx).ack(anyString());
  }

  @Test
  @DisplayName("Remove the last user from a group")
  void removeLastUserFromGroup() {
    String userId = "user1";
    String userInput = "leave single group";
    SlashCommandContext mockCtx = callWithMockValues(userId, userInput);

    verify(groupUtil).disableUsergroup("3333");
    verify(mockCtx).ack();
  }

  @Test
  @DisplayName("Create group and add user")
  void addUserToNewGroup() {
    String userId = "user1";
    String userInput = "join new group";
    Usergroup group = Usergroup.builder()
        .id("2222")
        .users(new ArrayList<String>())
        .name("new group")
        .dateDelete(0)
        .build();

    when(groupUtil.createUsergroup("new group")).thenReturn(group);

    SlashCommandContext mockCtx = callWithMockValues(userId, userInput);

    verify(mockCtx).ack();
  }

  @Test
  @DisplayName("Create group and add user api fails")
  void addUserToNewGroupFails() {
    String userId = "user1";
    String userInput = "join new group";

    when(groupUtil.createUsergroup("new group")).thenReturn(null);

    SlashCommandContext mockCtx = callWithMockValues(userId, userInput);

    verify(msgUtil).sendEphemeralResponse(anyString(), eq(userId), eq("channel_id"));
    verify(mockCtx).ack(anyString());
  }

  @Test
  @DisplayName("Invalid command fails")
  void invalidCommandFails() {
    String userId = "user1";
    String userInput = "jnoin sample group";

    SlashCommandContext mockCtx = callWithMockValues(userId, userInput);
    
    verify(msgUtil).sendEphemeralResponse(anyString(), eq(userId), eq("channel_id"));
    verify(mockCtx).ack(anyString());
  }

  @Test
  @DisplayName("Enabling group fails")
  void groupEnableFailure() {
    String userId = "user1";
    String userInput = "join disabled group";

    when(groupUtil.enableUsergroup(any())).thenReturn(false);

    SlashCommandContext mockCtx = callWithMockValues(userId, userInput);

    verify(msgUtil).sendEphemeralResponse(
        anyString(), eq(userId), eq("channel_id"));
    verify(mockCtx).ack(anyString());
  }

  @Test
  @DisplayName("Joining group fails")
  void groupJoinFailure() {
    String userId = "user4";
    String userInput = "join sample group";
 

    when(groupUtil.updateUsergroupUserlist(anyList(), anyString())).thenReturn(false);

    SlashCommandContext mockCtx = callWithMockValues(userId, userInput);

    verify(msgUtil).sendEphemeralResponse(
        anyString(), eq(userId), eq("channel_id"));
    verify(mockCtx).ack(anyString());
  }

  @Test
  @DisplayName("Typo sends a blockit message")
  void typoFails() {
    String userId = "user";
    String userInput = "join sample gruop";

    SlashCommandContext mockCtx = callWithMockValues(userId, userInput);

    verify(msgUtil).sendEphemeralResponse(
        anyList(), eq("Groups with similar names"), eq(userId), eq("channel_id"));
    verify(mockCtx).ack();
  }

  @Test
  @DisplayName("Similar group doesn't prevent joining an existing one")
  void typoAllowsExisting() {
    String userId = "user";
    String userInput = "join sample group";

    SlashCommandContext mockCtx = callWithMockValues(userId, userInput);

    verify(mockCtx).ack();
  }
}
