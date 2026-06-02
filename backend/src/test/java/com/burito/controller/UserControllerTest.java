package com.burito.controller;

import com.burito.domain.User;
import com.burito.service.AuthService;
import java.time.LocalDateTime;
import com.burito.service.JWTService;
import com.burito.service.UserService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@WithMockUser(username = "deadpool456@gmail.com")
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthService authService;
  @MockitoBean
  private JWTService jwtService;
  @MockitoBean
  private UserService userService;

  @Test
  @SneakyThrows
  void shouldReturnCurrentUserProfile() {
    User user = new User("Wade Wilson", "deadpool456@gmail.com", "hashedPassword");
    user.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0));

    when(authService.getCurrentUser("deadpool456@gmail.com")).thenReturn(user);

    mockMvc.perform(get("/api/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("deadpool456@gmail.com"))
            .andExpect(jsonPath("$.data.name").value("Wade Wilson"))
            .andExpect(jsonPath("$.data.createdAt").value("2024-01-15T10:30:00"))
            .andExpect(jsonPath("$.data.password").doesNotExist());
  }

  @Test
  @SneakyThrows
  @WithAnonymousUser
  void shouldReturnUnauthorizedWithoutAuthentication() {
    mockMvc.perform(get("/api/me"))
            .andExpect(status().isUnauthorized());
  }
}
