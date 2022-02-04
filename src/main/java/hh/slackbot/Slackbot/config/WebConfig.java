package hh.slackbot.Slackbot.config;

import com.slack.api.Slack;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {
    @Bean
    public Slack slack() {
        return Slack.getInstance();
    }
}