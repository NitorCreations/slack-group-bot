package hh.slackbot.slackbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * This is a comment.
 */
@SpringBootApplication
@ServletComponentScan
public class SlackbotApplication {

  public static void main(String[] args) {
    SpringApplication.run(SlackbotApplication.class, args);
  }
}
