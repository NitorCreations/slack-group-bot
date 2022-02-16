package hh.slackbot.Slackbot;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import com.slack.api.model.Usergroup;

@SpringBootTest
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
