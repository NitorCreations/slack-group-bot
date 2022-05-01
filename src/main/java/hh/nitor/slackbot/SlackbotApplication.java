package hh.nitor.slackbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class SlackbotApplication {

  private static Logger logger = LoggerFactory.getLogger(SlackbotApplication.class);

  public static void main(String[] args) {
    logger.info("Starting SlackbotApplication...");
    SpringApplication.run(SlackbotApplication.class, args);
    logger.info("SlackbotApplication has started");
  }
}
