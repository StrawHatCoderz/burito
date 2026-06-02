package com.burito.controller;

import com.burito.enums.ErrorCode;
import com.burito.exceptions.EmailAlreadyExistsException;
import com.burito.exceptions.InvalidCredentialsException;
import com.burito.exceptions.InvalidRefreshTokenException;
import com.burito.exceptions.RestaurantNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
            .standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @RestController
  static class TestController {
    @GetMapping("/test/invalid-credentials")
    void throwInvalidCredentials() throws InvalidCredentialsException {
      throw new InvalidCredentialsException("bad password");
    }

    @GetMapping("/test/email-exists")
    void throwEmailExists() throws EmailAlreadyExistsException {
      throw new EmailAlreadyExistsException();
    }

    @GetMapping("/test/invalid-refresh-token")
    void throwInvalidRefreshToken() throws InvalidRefreshTokenException {
      throw new InvalidRefreshTokenException("token not found");
    }

    @GetMapping("/test/restaurant-not-found")
    void throwRestaurantNotFound() throws RestaurantNotFoundException {
      throw new RestaurantNotFoundException(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }
  }

  @Test
  void shouldReturn401ForInvalidCredentials() throws Exception {
    mockMvc.perform(get("/test/invalid-credentials"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errorCode").value(ErrorCode.INVALID_CREDENTIALS.toString()))
            .andExpect(jsonPath("$.error.message").value("bad password"));
  }

  @Test
  void shouldReturn409ForEmailAlreadyExists() throws Exception {
    mockMvc.perform(get("/test/email-exists"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errorCode").value(ErrorCode.EMAIL_ALREADY_EXISTS.toString()));
  }

  @Test
  void shouldReturn401ForInvalidRefreshToken() throws Exception {
    mockMvc.perform(get("/test/invalid-refresh-token"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errorCode").value(ErrorCode.INVALID_REFRESH_TOKEN.toString()))
            .andExpect(jsonPath("$.error.message").value("token not found"));
  }

  @Test
  void shouldReturn404ForRestaurantNotFound() throws Exception {
    mockMvc.perform(get("/test/restaurant-not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errorCode").value(ErrorCode.RESTAURANT_NOT_FOUND.toString()));
  }
}
