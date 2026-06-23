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
  void shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
    when(userRepo.findUserByEmail("nobody@test.com")).thenReturn(null);

    assertThrows(UsernameNotFoundException.class,
            () -> userService.loadUserByUsername("nobody@test.com"));
  }
}
