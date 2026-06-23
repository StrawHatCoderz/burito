package com.burito.identity.service;

import com.burito.identity.domain.User;
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

    String token = jwtService.sign(user);

    assertNotNull(token);
    assertFalse(token.isBlank());
  }

  @Test
  void shouldSignTokenWhenUserIdIsNull() {
    User user = new User("Wade", "wade@test.com", null);

    assertNotNull(jwtService.sign(user));
  }

  @Test
  void shouldExtractUsernameFromSignedToken() {
    User user = new User("Wade", "wade@test.com", null);
    user.setUserId(UUID.randomUUID());

    assertEquals("wade@test.com", jwtService.extractUsername(jwtService.sign(user)));
  }

  @Test
  void shouldExtractRoleFromSignedToken() {
    User user = new User("Wade", "wade@test.com", null);
    user.setUserId(UUID.randomUUID());

    assertEquals("USER", jwtService.extractRole(jwtService.sign(user)));
  }

  @Test
  void shouldReturnTrueForValidTokenAndMatchingUser() {
    User user = new User("Wade", "wade@test.com", null);
    user.setUserId(UUID.randomUUID());
    String token = jwtService.sign(user);

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
    String token = jwtService.sign(user);

    UserDetails wrongUser = org.springframework.security.core.userdetails.User.builder()
            .username("other@test.com")
            .password("hash")
            .roles("USER")
            .build();

    assertFalse(jwtService.isValidToken(token, wrongUser));
  }
}
