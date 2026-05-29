package com.burito.service;

import com.burito.controller.views.JWTToken;
import com.burito.controller.views.UserCreationView;
import com.burito.domain.User;
import com.burito.exceptions.EmailAlreadyExistsException;
import com.burito.exceptions.InvalidCredentialsException;
import com.burito.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepo userRepo;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private JWTService jwtService;

  @InjectMocks
  private AuthService authService;

  // --- register ---

  @Test
  void shouldRegisterUserWithValidCredentials() throws Exception {
    UUID userId = UUID.randomUUID();
    User savedUser = new User("Deadpool", "deadpool@test.com", "hashed");
    savedUser.setUserId(userId);

    when(userRepo.findUserByEmail("deadpool@test.com")).thenReturn(null);
    when(passwordEncoder.encode("loveyou3000")).thenReturn("hashed");
    when(userRepo.save(any(User.class))).thenReturn(savedUser);

    UserCreationView result = authService.register("Deadpool", "deadpool@test.com", "loveyou3000");

    assertEquals(userId, result.userId());
    assertEquals("deadpool@test.com", result.email());
    verify(userRepo).save(any(User.class));
  }

  @Test
  void shouldThrowWhenFullNameIsNull() {
    assertThrows(InvalidCredentialsException.class,
            () -> authService.register(null, "deadpool@test.com", "loveyou3000"));
  }

  @Test
  void shouldThrowWhenFullNameIsEmpty() {
    assertThrows(InvalidCredentialsException.class,
            () -> authService.register("", "deadpool@test.com", "loveyou3000"));
  }

  @Test
  void shouldThrowWhenEmailIsNull() {
    assertThrows(InvalidCredentialsException.class,
            () -> authService.register("Deadpool", null, "loveyou3000"));
  }

  @Test
  void shouldThrowWhenEmailIsInvalidFormat() {
    assertThrows(InvalidCredentialsException.class,
            () -> authService.register("Deadpool", "notanemail", "loveyou3000"));
  }

  @Test
  void shouldThrowWhenEmailIsAlreadyRegistered() {
    when(userRepo.findUserByEmail("taken@test.com"))
            .thenReturn(new User("Someone", "taken@test.com", "hash"));

    assertThrows(EmailAlreadyExistsException.class,
            () -> authService.register("Deadpool", "taken@test.com", "loveyou3000"));
  }

  @Test
  void shouldThrowWhenPasswordIsNull() {
    when(userRepo.findUserByEmail(anyString())).thenReturn(null);

    assertThrows(InvalidCredentialsException.class,
            () -> authService.register("Deadpool", "deadpool@test.com", null));
  }

  @Test
  void shouldThrowWhenPasswordIsTooShort() {
    when(userRepo.findUserByEmail(anyString())).thenReturn(null);

    assertThrows(InvalidCredentialsException.class,
            () -> authService.register("Deadpool", "deadpool@test.com", "short"));
  }

  // --- login ---

  @Test
  void shouldReturnJwtTokenOnValidLogin() throws Exception {
    User user = new User("Deadpool", "deadpool@test.com", "hash");
    JWTToken token = new JWTToken("signed.jwt.token", 60.0);

    when(userRepo.findUserByEmail("deadpool@test.com")).thenReturn(user);
    when(jwtService.sign(user)).thenReturn(token);

    JWTToken result = authService.login("deadpool@test.com", "loveyou3000");

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    assertEquals("signed.jwt.token", result.token());
  }

  @Test
  void shouldThrowInvalidCredentialsWhenAuthenticationFails() {
    when(authenticationManager.authenticate(any()))
            .thenThrow(new RuntimeException("Bad credentials"));

    assertThrows(InvalidCredentialsException.class,
            () -> authService.login("deadpool@test.com", "wrongpassword"));
  }

  // --- getCurrentUser ---

  @Test
  void shouldReturnCurrentUser() {
    User user = new User("Deadpool", "deadpool@test.com", "hash");
    when(userRepo.findUserByEmail("deadpool@test.com")).thenReturn(user);

    User result = authService.getCurrentUser("deadpool@test.com");

    assertEquals(user, result);
  }
}
