package com.burito.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class Security {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity https) {
    https.csrf(AbstractHttpConfigurer::disable);

    https.authorizeHttpRequests(auth ->
            auth.requestMatchers("/api/auth/register")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
    );

    return https.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
