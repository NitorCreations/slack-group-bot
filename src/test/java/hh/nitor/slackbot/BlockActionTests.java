package hh.nitor.slackbot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload.Action;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload.Channel;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload.User;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.model.Usergroup;
import hh.nitor.slackbot.util.MessageUtil;
import hh.nitor.slackbot.util.RestService;
import hh.nitor.slackbot.util.UsergroupUtil;
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
public class BlockActionTests {

  @Autowired
  private BlockActionHandler handler;

  @MockBean
  private UsergroupUtil usergroupUtil;

  @MockBean
  private MessageUtil messageUtil;

  @MockBean
  private UsergroupHandler usergroupHandler;

  @MockBean
  private RestService restService;

  @BeforeEach
  public void init() {
    MockitoAnnotations.openMocks(this);
    doCallRealMethod().when(usergroupUtil).getGroupByName(anyString());

    // override these in test functions if necessary
    when(usergroupUtil.enableUsergroup(anyString())).thenReturn(true);
    when(usergroupUtil.updateUsergroupUserlist(anyList(), anyString())).thenReturn(true);
    initUserGroups();
  }

  private void initUserGroups() {
    List<String> users = new ArrayList<String>(Arrays.asList("user1", "user2", "user3"));
    List<Usergroup> groups = Arrays.asList(
        Usergroup.builder()
          .id("g1234")
          .users(users)
          .name("sample group")
          .dateDelete(0)
          .build(),
        Usergroup.builder()
          .id("g2222")
          .users(new ArrayList<String>())
          .name("empty group")
          .dateDelete(1)
          .build()
    );

    when(usergroupUtil.getUserGroups()).thenReturn(groups);
  }

  @Test
  @DisplayName("Block action successful join")
  public void blockJoinSuccess() {
    BlockActionRequest mockReq = mock(BlockActionRequest.class);
    BlockActionPayload mockPayload = mock(BlockActionPayload.class);
    Action mockAction = mock(Action.class);
    when(mockReq.getPayload()).thenReturn(mockPayload);
    when(mockReq.getResponseUrl()).thenReturn("https://some-url.test");
    when(mockPayload.getActions()).thenReturn(Arrays.asList(mockAction));
    when(mockAction.getValue()).thenReturn("sample group");

    User mockUser = mock(User.class);
    Channel mockChannel = mock(Channel.class);
    when(mockPayload.getUser()).thenReturn(mockUser);
    when(mockPayload.getChannel()).thenReturn(mockChannel);

    when(mockUser.getId()).thenReturn("u1234");
    when(mockChannel.getId()).thenReturn("c1234");

    when(usergroupHandler.addUserToGroup(eq("u1234"), any(Usergroup.class), eq("c1234")))
        .thenReturn(true);
    
    ActionContext mockCtx = mock(ActionContext.class);
    handler.handleBlockJoinAction(mockReq, mockCtx);
    
    verify(mockCtx).ack();
    verify(restService).postSlackResponse(eq("https://some-url.test"), any());
  }

  @Test
  @DisplayName("Block action successful create")
  public void blockCreateSuccess() {
    BlockActionRequest mockReq = mock(BlockActionRequest.class);
    BlockActionPayload mockPayload = mock(BlockActionPayload.class);
    Action mockAction = mock(Action.class);
    when(mockReq.getPayload()).thenReturn(mockPayload);
    when(mockReq.getResponseUrl()).thenReturn("https://some-url.test");
    when(mockPayload.getActions()).thenReturn(Arrays.asList(mockAction));
    when(mockAction.getValue()).thenReturn("new group");

    User mockUser = mock(User.class);
    Channel mockChannel = mock(Channel.class);
    when(mockPayload.getUser()).thenReturn(mockUser);
    when(mockPayload.getChannel()).thenReturn(mockChannel);

    when(mockUser.getId()).thenReturn("u1234");
    when(mockChannel.getId()).thenReturn("c1234");

    when(usergroupHandler.addUserToGroup(eq("u1234"), any(Usergroup.class), eq("c1234")))
        .thenReturn(true);

    Usergroup group = Usergroup.builder()
        .id("2222")
        .users(new ArrayList<String>())
        .name("new group")
        .dateDelete(0)
        .build();
    when(usergroupUtil.createUsergroup("new group")).thenReturn(group);
    
    ActionContext mockCtx = mock(ActionContext.class);
    handler.handleBlockCreateAction(mockReq, mockCtx);
    
    verify(mockCtx).ack();
    verify(restService).postSlackResponse(eq("https://some-url.test"), any());
  }
  
}
