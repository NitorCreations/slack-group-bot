package hh.slackbot.slackbot;

import static org.mockito.Mockito.when;

import com.slack.api.model.Usergroup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class UsergroupHandlerTest {
  private Usergroup falseBlankUsergroup = new Usergroup();

  @Mock
  UsergroupHandler groupHandler;

  @BeforeAll
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("addUserToGroup returns true")
  void addUserToGroupReturnsTrue() {
    when(groupHandler.addUserToGroup("123", falseBlankUsergroup)).thenReturn(true);
  }

  @Test
  @DisplayName("createUsergroup responds OK and returns group")
  void createUsergroupRespondsOkAndReturnsGroup() {

  }
}
