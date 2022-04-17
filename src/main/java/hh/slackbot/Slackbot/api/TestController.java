package hh.slackbot.slackbot.api;

import hh.slackbot.slackbot.models.UserMessageDto;
import hh.slackbot.slackbot.util.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

  private Logger logger = LoggerFactory.getLogger(TestController.class);

  @Autowired
  private MessageUtil msgUtil;

  @GetMapping
  public String test() {
    return "hello";
  }

  @PostMapping
  public ResponseEntity<String> sendMessage(@RequestBody UserMessageDto dto) {
    logger.info("{}", dto);
    msgUtil.sendDirectMessage(dto.getMessage(), dto.getUser());
    return new ResponseEntity<>("OK", HttpStatus.OK);
  }
  
}
