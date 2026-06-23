package com.burito.identity.service;

import com.burito.identity.controller.views.JWTToken;
import com.burito.identity.controller.views.UserCreationView;
import com.burito.identity.domain.RefreshToken;
import com.burito.identity.domain.User;
import com.burito.identity.exceptions.EmailAlreadyExistsException;
import com.burito.identity.exceptions.InvalidCredentialsException;
import com.burito.identity.exceptions.InvalidRefreshTokenException;
import com.burito.identity.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
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
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private com.burito.catalog.service.RestaurantService restaurantService;

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
    RefreshToken refreshToken = buildRefreshToken(user, "refresh-token-uuid");

    when(userRepo.findUserByEmail("deadpool@test.com")).thenReturn(user);
    when(jwtService.sign(user, null)).thenReturn("signed.access.token");
    when(refreshTokenService.create(user)).thenReturn(refreshToken);

    JWTToken result = authService.login("deadpool@test.com", "loveyou3000");

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    assertEquals("signed.access.token", result.accessToken());
    assertEquals("refresh-token-uuid", result.refreshToken());
    assertEquals(JWTService.ACCESS_TOKEN_EXPIRY_MINS, result.expiresInMins());
  }

  @Test
  void shouldThrowInvalidCredentialsWhenAuthenticationFails() {
    when(authenticationManager.authenticate(any()))
            .thenThrow(new RuntimeException("Bad credentials"));

    assertThrows(InvalidCredentialsException.class,
            () -> authService.login("deadpool@test.com", "wrongpassword"));
  }

  // --- refresh ---

  @Test
  void shouldReturnNewTokensOnValidRefreshToken() throws Exception {
    User user = new User("Deadpool", "deadpool@test.com", "hash");
    RefreshToken existing = buildRefreshToken(user, "old-refresh-token");
    RefreshToken rotated = buildRefreshToken(user, "new-refresh-token");

    when(refreshTokenService.validate("old-refresh-token")).thenReturn(existing);
    when(jwtService.sign(user, null)).thenReturn("new.access.token");
    when(refreshTokenService.create(user)).thenReturn(rotated);

    JWTToken result = authService.refresh("old-refresh-token");

    verify(refreshTokenService).revoke("old-refresh-token");
    assertEquals("new.access.token", result.accessToken());
    assertEquals("new-refresh-token", result.refreshToken());
  }

  @Test
  void shouldThrowOnInvalidRefreshToken() throws Exception {
    when(refreshTokenService.validate("bad-token"))
            .thenThrow(new InvalidRefreshTokenException("Refresh token not found"));

    assertThrows(InvalidRefreshTokenException.class,
            () -> authService.refresh("bad-token"));
  }

  // --- logout ---

  @Test
  void shouldRevokeRefreshTokenOnLogout() {
    authService.logout("some-refresh-token");

    verify(refreshTokenService).revoke("some-refresh-token");
  }

  // --- getCurrentUser ---

  @Test
  void shouldReturnCurrentUser() {
    User user = new User("Deadpool", "deadpool@test.com", "hash");
    when(userRepo.findUserByEmail("deadpool@test.com")).thenReturn(user);

    assertEquals(user, authService.getCurrentUser("deadpool@test.com"));
  }

  // --- registerAdmin ---

  @Test
  void shouldRegisterAdminWithValidCredentialsAndNullRestaurantName() throws Exception {
    UUID userId = UUID.randomUUID();
    User savedUser = new User("Admin", "admin@test.com", "hashed", com.burito.identity.enums.Role.RESTAURANT_ADMIN);
    savedUser.setUserId(userId);

    when(userRepo.findUserByEmail("admin@test.com")).thenReturn(null);
    when(passwordEncoder.encode("loveyou3000")).thenReturn("hashed");
    when(userRepo.save(any(User.class))).thenReturn(savedUser);
    when(restaurantService.createRestaurantForAdmin(userId, "Admin's Restaurant", com.burito.catalog.enums.CuisineType.INDIAN, 30.0)).thenReturn(null);

    UserCreationView result = authService.registerAdmin("Admin", "admin@test.com", "loveyou3000", null, com.burito.catalog.enums.CuisineType.INDIAN, 30.0);

    assertEquals(userId, result.userId());
    verify(restaurantService).createRestaurantForAdmin(userId, "Admin's Restaurant", com.burito.catalog.enums.CuisineType.INDIAN, 30.0);
  }

  @Test
  void shouldRegisterAdminWithValidCredentialsAndProvidedRestaurantName() throws Exception {
    UUID userId = UUID.randomUUID();
    User savedUser = new User("Admin", "admin@test.com", "hashed", com.burito.identity.enums.Role.RESTAURANT_ADMIN);
    savedUser.setUserId(userId);

    when(userRepo.findUserByEmail("admin@test.com")).thenReturn(null);
    when(passwordEncoder.encode("loveyou3000")).thenReturn("hashed");
    when(userRepo.save(any(User.class))).thenReturn(savedUser);
    when(restaurantService.createRestaurantForAdmin(userId, "My Custom Restaurant", com.burito.catalog.enums.CuisineType.INDIAN, 30.0)).thenReturn(null);

    UserCreationView result = authService.registerAdmin("Admin", "admin@test.com", "loveyou3000", "My Custom Restaurant", com.burito.catalog.enums.CuisineType.INDIAN, 30.0);

    assertEquals(userId, result.userId());
    verify(restaurantService).createRestaurantForAdmin(userId, "My Custom Restaurant", com.burito.catalog.enums.CuisineType.INDIAN, 30.0);
  }

  @Test
  void shouldThrowWhenAdminFullNameIsNull() {
    assertThrows(InvalidCredentialsException.class,
            () -> authService.registerAdmin(null, "admin@test.com", "loveyou3000", null, com.burito.catalog.enums.CuisineType.INDIAN, 30.0));
  }

  @Test
  void shouldThrowWhenAdminEmailIsInvalidFormat() {
    assertThrows(InvalidCredentialsException.class,
            () -> authService.registerAdmin("Admin", "notanemail", "loveyou3000", null, com.burito.catalog.enums.CuisineType.INDIAN, 30.0));
  }

  @Test
  void shouldThrowWhenAdminEmailIsAlreadyRegistered() {
    when(userRepo.findUserByEmail("taken@test.com"))
            .thenReturn(new User("Someone", "taken@test.com", "hash"));

    assertThrows(EmailAlreadyExistsException.class,
            () -> authService.registerAdmin("Admin", "taken@test.com", "loveyou3000", null, com.burito.catalog.enums.CuisineType.INDIAN, 30.0));
  }

  @Test
  void shouldThrowWhenAdminPasswordIsTooShort() {
    when(userRepo.findUserByEmail(anyString())).thenReturn(null);

    assertThrows(InvalidCredentialsException.class,
            () -> authService.registerAdmin("Admin", "admin@test.com", "short", null, com.burito.catalog.enums.CuisineType.INDIAN, 30.0));
  }

  // --- login for admin ---
  
  @Test
  void shouldReturnJwtTokenOnAdminLoginWithRestaurant() throws Exception {
    User user = new User("Admin", "admin@test.com", "hash", com.burito.identity.enums.Role.RESTAURANT_ADMIN);
    user.setUserId(UUID.randomUUID());
    RefreshToken refreshToken = buildRefreshToken(user, "refresh-token-uuid");
    UUID restaurantId = UUID.randomUUID();

    when(userRepo.findUserByEmail("admin@test.com")).thenReturn(user);
    when(restaurantService.getRestaurantIdByOwnerId(user.getUserId())).thenReturn(restaurantId);
    when(jwtService.sign(user, restaurantId.toString())).thenReturn("signed.access.token");
    when(refreshTokenService.create(user)).thenReturn(refreshToken);

    JWTToken result = authService.login("admin@test.com", "loveyou3000");

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    assertEquals("signed.access.token", result.accessToken());
    assertEquals("refresh-token-uuid", result.refreshToken());
  }

  @Test
  void shouldReturnJwtTokenOnAdminLoginWithoutRestaurant() throws Exception {
    User user = new User("Admin", "admin@test.com", "hash", com.burito.identity.enums.Role.RESTAURANT_ADMIN);
    user.setUserId(UUID.randomUUID());
    RefreshToken refreshToken = buildRefreshToken(user, "refresh-token-uuid");

    when(userRepo.findUserByEmail("admin@test.com")).thenReturn(user);
    when(restaurantService.getRestaurantIdByOwnerId(user.getUserId())).thenReturn(null);
    when(jwtService.sign(user, null)).thenReturn("signed.access.token");
    when(refreshTokenService.create(user)).thenReturn(refreshToken);

    JWTToken result = authService.login("admin@test.com", "loveyou3000");

    assertEquals("signed.access.token", result.accessToken());
  }

  // --- refresh for admin ---

  @Test
  void shouldReturnNewTokensOnAdminRefreshWithRestaurant() throws Exception {
    User user = new User("Admin", "admin@test.com", "hash", com.burito.identity.enums.Role.RESTAURANT_ADMIN);
    user.setUserId(UUID.randomUUID());
    RefreshToken existing = buildRefreshToken(user, "old-refresh-token");
    RefreshToken rotated = buildRefreshToken(user, "new-refresh-token");
    UUID restaurantId = UUID.randomUUID();

    when(refreshTokenService.validate("old-refresh-token")).thenReturn(existing);
    when(restaurantService.getRestaurantIdByOwnerId(user.getUserId())).thenReturn(restaurantId);
    when(jwtService.sign(user, restaurantId.toString())).thenReturn("new.access.token");
    when(refreshTokenService.create(user)).thenReturn(rotated);

    JWTToken result = authService.refresh("old-refresh-token");

    verify(refreshTokenService).revoke("old-refresh-token");
    assertEquals("new.access.token", result.accessToken());
    assertEquals("new-refresh-token", result.refreshToken());
  }

  @Test
  void shouldReturnNewTokensOnAdminRefreshWithoutRestaurant() throws Exception {
    User user = new User("Admin", "admin@test.com", "hash", com.burito.identity.enums.Role.RESTAURANT_ADMIN);
    user.setUserId(UUID.randomUUID());
    RefreshToken existing = buildRefreshToken(user, "old-refresh-token");
    RefreshToken rotated = buildRefreshToken(user, "new-refresh-token");

    when(refreshTokenService.validate("old-refresh-token")).thenReturn(existing);
    when(restaurantService.getRestaurantIdByOwnerId(user.getUserId())).thenReturn(null);
    when(jwtService.sign(user, null)).thenReturn("new.access.token");
    when(refreshTokenService.create(user)).thenReturn(rotated);

    JWTToken result = authService.refresh("old-refresh-token");

    assertEquals("new.access.token", result.accessToken());
  }

  private RefreshToken buildRefreshToken(User user, String tokenStr) {
    RefreshToken token = new RefreshToken();
    token.setToken(tokenStr);
    token.setUser(user);
    token.setExpiresAt(LocalDateTime.now().plusDays(7));
    return token;
  }
}
