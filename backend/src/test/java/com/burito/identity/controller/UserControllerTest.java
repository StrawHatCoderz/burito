package com.burito.identity.controller;

import com.burito.identity.domain.User;
import com.burito.identity.domain.UserAddress;
import com.burito.identity.service.AuthService;
import com.burito.identity.service.JWTService;
import com.burito.identity.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@WithMockUser(username = "deadpool456@gmail.com")
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

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

  @Test
  @SneakyThrows
  void shouldReturnUnauthorizedWhenUserIsNull() {
    when(authService.getCurrentUser("deadpool456@gmail.com")).thenReturn(null);

    mockMvc.perform(get("/api/me"))
            .andExpect(status().isUnauthorized());
  }

  @Test
  @SneakyThrows
  void shouldUpdateProfileSuccessfully() {
    User user = new User("Wade Wilson", "deadpool456@gmail.com", "hashedPassword");
    UUID userId = UUID.randomUUID();
    user.setUserId(userId);

    User updatedUser = new User("Deadpool", "deadpool456@gmail.com", "hashedPassword");
    updatedUser.setUserId(userId);
    updatedUser.setPhoneNumber("+1234567890");

    when(authService.getCurrentUser("deadpool456@gmail.com")).thenReturn(user);
    when(userService.updateProfile(userId, "Deadpool", "+1234567890")).thenReturn(updatedUser);

    Map<String, String> body = new HashMap<>();
    body.put("fullName", "Deadpool");
    body.put("phoneNumber", "+1234567890");

    mockMvc.perform(put("/api/me")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("Deadpool"))
            .andExpect(jsonPath("$.data.phoneNumber").value("+1234567890"));
  }

  @Test
  @SneakyThrows
  @WithAnonymousUser
  void shouldReturnUnauthorizedOnUpdateProfileWithoutAuth() {
    Map<String, String> body = new HashMap<>();
    body.put("fullName", "Deadpool");

    mockMvc.perform(put("/api/me")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isUnauthorized());
  }

  @Test
  @SneakyThrows
  void shouldUpdateAddressSuccessfully() {
    User user = new User("Wade Wilson", "deadpool456@gmail.com", "hashedPassword");
    UUID userId = UUID.randomUUID();
    user.setUserId(userId);

    User updatedUser = new User("Wade Wilson", "deadpool456@gmail.com", "hashedPassword");
    updatedUser.setUserId(userId);
    UserAddress address = new UserAddress(1L, "123 Main St", "Bengaluru", "Karnataka", "India", "560001");
    updatedUser.setAddress(address);

    when(authService.getCurrentUser("deadpool456@gmail.com")).thenReturn(user);
    when(userService.updateAddress(eq(userId), any(UserAddress.class))).thenReturn(updatedUser);

    Map<String, String> body = new HashMap<>();
    body.put("street", "123 Main St");
    body.put("city", "Bengaluru");
    body.put("state", "Karnataka");
    body.put("zipcode", "560001");
    body.put("country", "India");

    mockMvc.perform(put("/api/me/address")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.address.street").value("123 Main St"))
            .andExpect(jsonPath("$.data.address.city").value("Bengaluru"))
            .andExpect(jsonPath("$.data.address.zipcode").value("560001"));
  }

  @Test
  @SneakyThrows
  @WithAnonymousUser
  void shouldReturnUnauthorizedOnUpdateAddressWithoutAuth() {
    Map<String, String> body = new HashMap<>();
    body.put("street", "123 Main St");

    mockMvc.perform(put("/api/me/address")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isUnauthorized());
  }
}
