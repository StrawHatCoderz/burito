package com.burito.controller;

import com.burito.config.Security;
import com.burito.controller.views.JWTToken;
import com.burito.controller.views.UserCreationView;
import com.burito.enums.ErrorCode;
import com.burito.exceptions.EmailAlreadyExistsException;
import com.burito.exceptions.InvalidCredentialsException;
import com.burito.exceptions.InvalidRefreshTokenException;
import com.burito.service.AuthService;
import com.burito.service.JWTService;
import com.burito.service.UserService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(Security.class)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean private AuthService authService;
  @MockitoBean private JWTService jwtService;
  @MockitoBean private UserService userService;

  // --- register ---

  @SneakyThrows
  @Test
  void shouldRegisterTheUserWithValidCredentials() {
    UserCreationView user = new UserCreationView(UUID.randomUUID(), "deadpool456@gmail.com");
    when(authService.register("Deadpool", "deadpool456@gmail.com", "loveyou3000")).thenReturn(user);

    mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"full_name":"Deadpool","email":"deadpool456@gmail.com","password":"loveyou3000"}
                            """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").exists())
            .andExpect(jsonPath("$.data.email").exists())
            .andExpect(jsonPath("$.data.password").doesNotExist());
  }

  @SneakyThrows
  @Test
  void shouldNotRegisterOnInvalidCredentials() {
    when(authService.register("deadpool", "deadpool", "loveyou3000"))
            .thenThrow(new InvalidCredentialsException("Invalid email"));

    mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"full_name":"deadpool","email":"deadpool","password":"loveyou3000"}
                            """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errorCode").value(ErrorCode.INVALID_CREDENTIALS.toString()));
  }

  @SneakyThrows
  @Test
  void shouldNotRegisterOnInvalidPassword() {
    when(authService.register("deadpool", "deadpool456@gmail.com", "short"))
            .thenThrow(new InvalidCredentialsException("Password should be greater than 8 characters"));

    mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"full_name":"deadpool","email":"deadpool456@gmail.com","password":"short"}
                            """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errorCode").value(ErrorCode.INVALID_CREDENTIALS.toString()));
  }

  @SneakyThrows
  @Test
  void shouldNotRegisterIfEmailIsAlreadyRegistered() {
    when(authService.register("deadpool", "deadpool456@gmail.com", "loveyou3000"))
            .thenThrow(new EmailAlreadyExistsException());

    mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"full_name":"deadpool","email":"deadpool456@gmail.com","password":"loveyou3000"}
                            """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errorCode").value(ErrorCode.EMAIL_ALREADY_EXISTS.toString()));
  }

  // --- login ---

  @Test
  @SneakyThrows
  void shouldReturnJwtOnLoginUsingValidCredentials() {
    JWTToken token = new JWTToken("access.jwt.token", "refresh-uuid", 60.0);
    when(authService.login("deadpool456@gmail.com", "loveyou3000")).thenReturn(token);

    mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"email":"deadpool456@gmail.com","password":"loveyou3000"}
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists())
            .andExpect(jsonPath("$.data.expiresInMins").exists());
  }

  @Test
  @SneakyThrows
  void shouldReturnUnauthorizedOnInvalidEmail() {
    when(authService.login("deadpool45@gmail.com", "loveyou3000"))
            .thenThrow(new InvalidCredentialsException("Invalid credentials"));

    mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"email":"deadpool45@gmail.com","password":"loveyou3000"}
                            """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errorCode").value(ErrorCode.INVALID_CREDENTIALS.toString()));
  }

  @Test
  @SneakyThrows
  void shouldReturnUnauthorizedOnInvalidPassword() {
    when(authService.login("deadpool456@gmail.com", "loveyou300"))
            .thenThrow(new InvalidCredentialsException("Invalid credentials"));

    mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"email":"deadpool456@gmail.com","password":"loveyou300"}
                            """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errorCode").value(ErrorCode.INVALID_CREDENTIALS.toString()));
  }

  // --- refresh ---

  @Test
  @SneakyThrows
  void shouldReturnNewTokensOnValidRefreshToken() {
    JWTToken token = new JWTToken("new.access.token", "new-refresh-uuid", 60.0);
    when(authService.refresh("valid-refresh-token")).thenReturn(token);

    mockMvc.perform(post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"refreshToken":"valid-refresh-token"}
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists());
  }

  @Test
  @SneakyThrows
  void shouldReturnUnauthorizedOnInvalidRefreshToken() {
    when(authService.refresh("bad-token"))
            .thenThrow(new InvalidRefreshTokenException("Refresh token not found"));

    mockMvc.perform(post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"refreshToken":"bad-token"}
                            """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errorCode").value(ErrorCode.INVALID_REFRESH_TOKEN.toString()));
  }

  // --- logout ---

  @Test
  @SneakyThrows
  void shouldLogoutSuccessfully() {
    mockMvc.perform(post("/api/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"refreshToken":"some-refresh-token"}
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

    verify(authService).logout("some-refresh-token");
  }
}
