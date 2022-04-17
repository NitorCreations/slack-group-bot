package hh.slackbot.slackbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
  
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .httpBasic()
        .and()
        .authorizeRequests()
        .antMatchers("/slack/events/**").permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .csrf().ignoringAntMatchers("/slack/events").disable();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    UserDetailsManager serv = new InMemoryUserDetailsManager();

    UserDetails user = User
        .withUsername("nitor")
        .password("12345")
        .authorities("post")
        .build();

    serv.createUser(user);

    auth.userDetailsService(serv);
  }

  @Bean
  public PasswordEncoder encoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}
