package hh.nitor.slackbot.util;

import com.google.gson.JsonElement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestService {

  private final RestTemplate restTemplate;

  public RestService() {
    this.restTemplate = new RestTemplate();
  }

  /**
   * Sends an HTTP POST request to specified response URL.
   * Can be used to send responses to actions happening in
   * ephemeral messages.
   *
   * @param responseUrl Slack API URL to send request to
   * @param body in JSON
   * @return if succeeded
   */
  public boolean postSlackResponse(String responseUrl, JsonElement body) {
    ResponseEntity<String> response
        = restTemplate.postForEntity(responseUrl, body.toString(), String.class);

    return response.getStatusCode() == HttpStatus.OK;
  }
  
}
