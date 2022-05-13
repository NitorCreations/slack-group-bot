package hh.nitor.slackbot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SlackbotApplicationTests {

  @Test
  void contextLoads() {
  }

  @Test
  @DisplayName("Env variables set up")
  public void envSetup() {
    String token = System.getenv("SLACK_BOT_TOKEN");
    String secret = System.getenv("SLACK_SIGNING_SECRET");

    Assertions.assertNotNull(token);
    Assertions.assertNotNull(secret);
  }

}
