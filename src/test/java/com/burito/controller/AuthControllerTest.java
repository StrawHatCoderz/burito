package com.burito.controller;

import com.burito.config.Security;
import com.burito.controller.views.JWTToken;
import com.burito.controller.views.UserCreationView;
import com.burito.enums.ErrorCode;
import com.burito.exceptions.EmailAlreadyExistsException;
import com.burito.exceptions.InvalidCredentialsException;
import com.burito.service.AuthService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(Security.class)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthService authService;

  @SneakyThrows
  @Test
  void shouldRegisterTheUserWithValidCredentials() {
    String email = "deadpool456@gmail.com";
    String password = "loveyou3000";

    UserCreationView user = new UserCreationView(1L, email);

    when(authService.register(email, password))
            .thenReturn(user);

    mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "email": "deadpool456@gmail.com",
                              "password": "loveyou3000"
                            }
                            """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success")
                    .value(true))
            .andExpect(jsonPath("$.data.userId").exists())
            .andExpect(jsonPath("$.data.email").exists())
            .andExpect(jsonPath("$.data.password").doesNotExist());
  }

  @SneakyThrows
  @Test
  void shouldNotRegisterOnInvalidCredentials() {
    String email = "deadpool";
    String password = "loveyou3000";

    when(authService.register(email, password))
            .thenThrow(new InvalidCredentialsException("Invalid email"));

    mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "email": "deadpool",
                              "password": "loveyou3000"
                            }
                            """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success")
                    .value(false))
            .andExpect(jsonPath("$.error.errorCode")
                    .value(ErrorCode.INVALID_CREDENTIALS.toString()));
  }

  @SneakyThrows
  @Test
  void shouldNotRegisterOnInvalidPassword() {
    String email = "deadpool456@gmail.com";
    String password = "short";

    when(authService.register(email, password))
            .thenThrow(new InvalidCredentialsException("Invalid email"));

    mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "email": "deadpool456@gmail.com",
                              "password": "short"
                            }
                            """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success")
                    .value(false))
            .andExpect(jsonPath("$.error.errorCode")
                    .value(ErrorCode.INVALID_CREDENTIALS.toString()));
  }


  @SneakyThrows
  @Test
  void shouldNotRegisterIfEmailIsAlreadyRegistered() {
    String email = "deadpool456@gmail.com";
    String password = "loveyou3000";

    when(authService.register(email, password))
            .thenThrow(new EmailAlreadyExistsException());

    mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "email": "deadpool456@gmail.com",
                              "password": "loveyou3000"
                            }
                            """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success")
                    .value(false))
            .andExpect(jsonPath("$.error.errorCode")
                    .value(ErrorCode.EMAIL_ALREADY_EXISTS.toString()));
  }

  @Test
  @SneakyThrows
  void shouldReturnJwtOnLoginUsingValidCredentials() {
    String email = "deadpool456@gmail.com";
    String password = "loveyou3000";

    JWTToken jwtToken = new JWTToken("adsfasdrafdasd.asdfqewradsf3421" +
            ".oiknafafs",
            60.0);

    when(authService.login(email, password)).thenReturn(jwtToken);

    mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "email": "deadpool456@gmail.com",
                              "password": "loveyou3000"
                            }
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success")
                    .value(true))
            .andExpect(jsonPath("$.data.token").exists())
            .andExpect(jsonPath("$.data.expiresInMins").exists());
  }

  @Test
  @SneakyThrows
  void shouldReturnErrorOnInvalidEmail() {
    String email = "deadpool45@gmail.com";
    String password = "loveyou3000";

    when(authService.login(email, password)).thenThrow(
            new UsernameNotFoundException(String.format("user with %s does " +
                    "not exists", email)));

    mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "email": "deadpool45@gmail.com",
                              "password": "loveyou3000"
                            }
                            """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success")
                    .value(false))
            .andExpect(jsonPath("$.error.errorCode")
                    .value(ErrorCode.USERNAME_NOT_FOUND.toString()));
  }

  @Test
  @SneakyThrows
  void shouldReturnErrorOnInvalidPassword() {
    String email = "deadpool456@gmail.com";
    String password = "loveyou300";

    when(authService.login(email, password)).thenThrow(
            new InvalidCredentialsException("Invalid email or password"));

    mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "email": "deadpool456@gmail.com",
                              "password": "loveyou300"
                            }
                            """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success")
                    .value(false))
            .andExpect(jsonPath("$.error.errorCode")
                    .value(ErrorCode.INVALID_CREDENTIALS.toString()));
  }
}