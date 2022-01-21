package hh.slackbot.Slackbot;

import com.slack.api.bolt.App;
import com.slack.api.model.event.AppMentionEvent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackApp {
    @Bean
    public App initSlackApp() {
        App app = new App();
        app.command("/hello", (req, ctx) -> {
            return ctx.ack(":wave: World");
        });

        app.event(AppMentionEvent.class, (req, ctx) -> {
            ctx.say("Greetings :wave:");
            System.out.println(ctx.getRequestUserId());
            System.out.println(ctx.getRequestUserToken());
            return ctx.ack();
        });

        return app;
    }
}
