package hh.slackbot.Slackbot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.usergroups.UsergroupsListRequest;
import com.slack.api.methods.response.usergroups.UsergroupsListResponse;
import com.slack.api.model.Usergroup;

import hh.slackbot.Slackbot.util.UsergroupUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsergroupUtilTests {
  @Autowired
  private UsergroupUtil groupUtil;

  @MockBean
  private MethodsClient client;

  @BeforeAll
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Captor
  ArgumentCaptor<UsergroupsListRequest> groupListCaptor;

  @Test
  @DisplayName("Usergroups request returns list of groups")
  public void getUsergroupsReturnsGroups() throws IOException, SlackApiException {
    UsergroupsListResponse resp = new UsergroupsListResponse();
    resp.setUsergroups(new ArrayList<Usergroup>(Arrays.asList(new Usergroup())));
    when(client.usergroupsList(any(UsergroupsListRequest.class))).thenReturn(resp);

    Assertions.assertTrue(groupUtil.getUserGroups().size() == 1);
  }

  @Test
  @DisplayName("Usergroups returns null on error")
  public void getUsergroupsNullOnError() throws IOException, SlackApiException {
    when(client.usergroupsList(any(UsergroupsListRequest.class))).thenThrow(new IOException());

    Assertions.assertTrue(groupUtil.getUserGroups() == null);
  }
}
