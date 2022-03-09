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
  @Autowired
  UsergroupHandler groupHandler;

  @MockBean
  private UsergroupUtil groupUtil;

  @MockBean
  private MessageUtil msgUtil;

  @BeforeAll
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Remove user from group they are not in returns false")
  void removeUserFromGroupInvalid() {
    List<String> users = Arrays.asList("123", "234", "345");

    Usergroup group = Usergroup.builder()
        .id("54321")
        .users(users)
        .name("user not in group")
        .build();

    String userId = "12345";

    assertFalse(groupHandler.removeUserFromGroup(userId, group));
    verify(msgUtil).sendDirectMessage(
        String.format("You are not in the group %s", group.getName()), userId);
  }

  @Test
  @DisplayName("Remove user from group they are not in returns false")
  void removeUserFromGroupValid() {
    List<String> users = Arrays.asList("22345", "234", "345");

    Usergroup group = Usergroup.builder()
        .id("54321")
        .users(users)
        .name("user in group")
        .build();

    when(groupUtil.updateUsergroupUserlist(any(), eq("54321"))).thenReturn(true);
    when(groupUtil.userInGroup(eq("22345"), eq(users))).thenReturn(true);

    String userId = "22345";
    boolean res = groupHandler.removeUserFromGroup(userId, group);
    verifyNoInteractions(msgUtil);

    List<String> modifiedUsers = users.stream().filter(u -> !u.equals(userId))
        .collect(Collectors.toList());
    verify(groupUtil).updateUsergroupUserlist(eq(modifiedUsers), eq("54321"));
    assertTrue(res);
  }

  @Test
  @DisplayName("Add user to empty group")
  void addUserToGroupFlowWorks() {
    SlashCommandPayload mockPayload = mock(SlashCommandPayload.class);
    when(mockPayload.getUserId()).thenReturn("12345");
    when(mockPayload.getText()).thenReturn("join empty group");

    SlashCommandRequest mockReq = mock(SlashCommandRequest.class);
    when(mockReq.getPayload()).thenReturn(mockPayload);
    
    Usergroup group = Usergroup.builder()
        .id("54321")
        .users(new ArrayList<String>())
        .name("empty group")
        .build();

    when(groupUtil.getGroupByName("empty group")).thenReturn(null);
    when(groupUtil.checkUsergroup(null, "empty group")).thenReturn(group);
    when(groupUtil.checkIfAvailable(group)).thenReturn(true);
    when(groupUtil.userInGroup(eq("12345"), anyList())).thenReturn(false);
    when(groupUtil.updateUsergroupUserlist(anyList(), eq("54321"))).thenReturn(true);
    
    SlashCommandContext mockCtx = mock(SlashCommandContext.class);
    groupHandler.handleUsergroupCommand(mockReq, mockCtx);
    
    // check that ack() was called with no params
    verify(mockCtx).ack();
  }
}
