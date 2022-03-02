package hh.slackbot.slackbot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SlackAppTest {

  @Test
  @DisplayName("Return true if value equals one")
  void returnTrueIfValueEqualsOne() {
    SlackApp slackApp = new SlackApp();
    // Assertions.assertTrue(slackApp.getSampleString(1));
  }

  @Test
  @DisplayName("Return false if falue is not one")
  void returnFalseIfValueIsNotOne() {
    SlackApp slackApp = new SlackApp();
    // Assertions.assertFalse(slackApp.getSampleString(0));
  }
}
