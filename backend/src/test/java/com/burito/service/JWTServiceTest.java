package com.burito.service;

import com.burito.controller.views.JWTToken;
import com.burito.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JWTServiceTest {

  private static final String TEST_SECRET =
          "test-secret-key-for-unit-testing-purposes-only-xx";

  private JWTService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JWTService(TEST_SECRET);
  }

  @Test
  void shouldSignAndReturnToken() {
    User user = new User("Wade", "wade@test.com", null);
    user.setUserId(UUID.randomUUID());

    JWTToken result = jwtService.sign(user);

    assertNotNull(result.token());
    assertFalse(result.token().isBlank());
    assertEquals(60.0, result.expiresInMins());
  }

  @Test
  void shouldSignTokenWhenUserIdIsNull() {
    User user = new User("Wade", "wade@test.com", null);

    JWTToken result = jwtService.sign(user);

    assertNotNull(result.token());
  }

  @Test
  void shouldExtractUsernameFromSignedToken() {
    User user = new User("Wade", "wade@test.com", null);
    user.setUserId(UUID.randomUUID());

    String token = jwtService.sign(user).token();

    assertEquals("wade@test.com", jwtService.extractUsername(token));
  }

  @Test
  void shouldReturnTrueForValidTokenAndMatchingUser() {
    User user = new User("Wade", "wade@test.com", null);
    user.setUserId(UUID.randomUUID());
    String token = jwtService.sign(user).token();

    UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
            .username("wade@test.com")
            .password("hash")
            .roles("USER")
            .build();

    assertTrue(jwtService.isValidToken(token, userDetails));
  }

  @Test
  void shouldReturnFalseWhenTokenUsernameMismatch() {
    User user = new User("Wade", "wade@test.com", null);
    user.setUserId(UUID.randomUUID());
    String token = jwtService.sign(user).token();

    UserDetails wrongUser = org.springframework.security.core.userdetails.User.builder()
            .username("other@test.com")
            .password("hash")
            .roles("USER")
            .build();

    assertFalse(jwtService.isValidToken(token, wrongUser));
  }
}
