package com.burito.identity.service;

import com.burito.identity.domain.User;
import com.burito.identity.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepo userRepo;

  @InjectMocks
  private UserService userService;

  @Test
  void shouldReturnUserDetailsWhenUserExists() {
    User user = new User("Wade Wilson", "wade@test.com", "hashedPassword");
    when(userRepo.findUserByEmail("wade@test.com")).thenReturn(user);

    UserDetails details = userService.loadUserByUsername("wade@test.com");

    assertEquals("wade@test.com", details.getUsername());
    assertEquals("hashedPassword", details.getPassword());
    assertTrue(details.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
  }

  @Test
  void shouldReturnUserDetailsWithAdminRoleWhenUserIsAdmin() {
    User admin = new User("Admin", "admin@test.com", "hashedPassword", com.burito.identity.enums.Role.RESTAURANT_ADMIN);
    when(userRepo.findUserByEmail("admin@test.com")).thenReturn(admin);

    UserDetails details = userService.loadUserByUsername("admin@test.com");

    assertEquals("admin@test.com", details.getUsername());
    assertEquals("hashedPassword", details.getPassword());
    assertTrue(details.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_RESTAURANT_ADMIN")));
  }

  @Test
  void shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
    when(userRepo.findUserByEmail("nobody@test.com")).thenReturn(null);

    assertThrows(UsernameNotFoundException.class,
            () -> userService.loadUserByUsername("nobody@test.com"));
  }

  @Test
  void findUserByEmail_shouldReturnUser() {
    User user = new User();
    when(userRepo.findUserByEmail("wade@test.com")).thenReturn(user);

    User result = userService.findUserByEmail("wade@test.com");
    assertEquals(user, result);
  }

  @Test
  void findUserById_shouldReturnUser() {
    java.util.UUID id = java.util.UUID.randomUUID();
    User user = new User();
    when(userRepo.findById(id)).thenReturn(java.util.Optional.of(user));

    User result = userService.findUserById(id);
    assertEquals(user, result);
  }

  @Test
  void findUserById_shouldReturnNullWhenNotFound() {
    java.util.UUID id = java.util.UUID.randomUUID();
    when(userRepo.findById(id)).thenReturn(java.util.Optional.empty());

    User result = userService.findUserById(id);
    assertNull(result);
  }
}
