package com.burito.identity.controller;

import com.burito.identity.controller.views.JWTToken;
import com.burito.identity.controller.views.UserCreationView;
import com.burito.identity.exceptions.EmailAlreadyExistsException;
import com.burito.identity.exceptions.InvalidCredentialsException;
import com.burito.identity.service.AuthService;
import com.burito.catalog.enums.CuisineType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.burito.core.config.Security;
@WebMvcTest(AdminAuthController.class)
@Import(Security.class)
class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private com.burito.identity.service.JWTService jwtService;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    void register_shouldReturn201OnSuccess() throws Exception {
        AdminAuthController.AdminRegisterRequest request = new AdminAuthController.AdminRegisterRequest(
                "Admin", "admin@test.com", "password", "Admin's", CuisineType.INDIAN, 30.0
        );

        UserCreationView view = new UserCreationView(UUID.randomUUID(), "admin@test.com");
        when(authService.registerAdmin(anyString(), anyString(), anyString(), anyString(), any(CuisineType.class), anyDouble()))
                .thenReturn(view);

        mockMvc.perform(post("/api/admin/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("admin@test.com"));
    }

    @Test
    @WithMockUser
    void register_shouldReturn400WhenEmailExists() throws Exception {
        AdminAuthController.AdminRegisterRequest request = new AdminAuthController.AdminRegisterRequest(
                "Admin", "admin@test.com", "password", "Admin's", CuisineType.INDIAN, 30.0
        );

        when(authService.registerAdmin(anyString(), anyString(), anyString(), anyString(), any(CuisineType.class), anyDouble()))
                .thenThrow(new EmailAlreadyExistsException());

        mockMvc.perform(post("/api/admin/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("Email already exists"));
    }

    @Test
    @WithMockUser
    void register_shouldReturn400WhenInvalidCredentials() throws Exception {
        AdminAuthController.AdminRegisterRequest request = new AdminAuthController.AdminRegisterRequest(
                "Admin", "admin@test.com", "password", "Admin's", CuisineType.INDIAN, 30.0
        );

        when(authService.registerAdmin(anyString(), anyString(), anyString(), anyString(), any(CuisineType.class), anyDouble()))
                .thenThrow(new InvalidCredentialsException("Invalid"));

        mockMvc.perform(post("/api/admin/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("Invalid credentials"));
    }

    @Test
    @WithMockUser
    void login_shouldReturn200OnSuccess() throws Exception {
        AdminAuthController.AdminLoginRequest request = new AdminAuthController.AdminLoginRequest("admin@test.com", "password");

        JWTToken token = new JWTToken("access", "refresh", 60.0);
        when(authService.login(anyString(), anyString())).thenReturn(token);

        mockMvc.perform(post("/api/admin/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access"));
    }

    @Test
    @WithMockUser
    void login_shouldReturn401WhenInvalidCredentials() throws Exception {
        AdminAuthController.AdminLoginRequest request = new AdminAuthController.AdminLoginRequest("admin@test.com", "password");

        when(authService.login(anyString(), anyString())).thenThrow(new InvalidCredentialsException("Invalid"));

        mockMvc.perform(post("/api/admin/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
