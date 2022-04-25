package hh.slackbot.Slackbot.util;

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

  public boolean postSlackResponse(String responseUrl, JsonElement body) {
    ResponseEntity<String> response
        = restTemplate.postForEntity(responseUrl, body.toString(), String.class);

    return response.getStatusCode() == HttpStatus.OK;
  }
  
}
