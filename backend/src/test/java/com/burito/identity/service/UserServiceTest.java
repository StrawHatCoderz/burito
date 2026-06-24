package com.burito.identity.service;

import com.burito.identity.domain.User;
import com.burito.identity.repository.UserRepo;
import com.burito.identity.domain.UserAddress;
import com.burito.core.exceptions.APIException;
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

  @Test
  void updateProfile_shouldUpdateSuccessfully() {
    java.util.UUID id = java.util.UUID.randomUUID();
    User user = new User("Wade Wilson", "wade@test.com", "hash");
    when(userRepo.findById(id)).thenReturn(java.util.Optional.of(user));
    when(userRepo.save(user)).thenReturn(user);

    User result = userService.updateProfile(id, "Deadpool", "+1234567890");

    assertEquals("Deadpool", result.getFullName());
    assertEquals("+1234567890", result.getPhoneNumber());
  }

  @Test
  void updateProfile_shouldThrowNotFoundWhenUserDoesNotExist() {
    java.util.UUID id = java.util.UUID.randomUUID();
    when(userRepo.findById(id)).thenReturn(java.util.Optional.empty());

    assertThrows(APIException.class, () -> userService.updateProfile(id, "Deadpool", "12345678"));
  }

  @Test
  void updateProfile_shouldThrowBadRequestWhenNameIsEmpty() {
    java.util.UUID id = java.util.UUID.randomUUID();
    User user = new User("Wade Wilson", "wade@test.com", "hash");
    when(userRepo.findById(id)).thenReturn(java.util.Optional.of(user));

    assertThrows(APIException.class, () -> userService.updateProfile(id, "", "123456789"));
    assertThrows(APIException.class, () -> userService.updateProfile(id, null, "123456789"));
  }

  @Test
  void updateProfile_shouldThrowBadRequestWhenPhoneFormatIsInvalid() {
    java.util.UUID id = java.util.UUID.randomUUID();
    User user = new User("Wade Wilson", "wade@test.com", "hash");
    when(userRepo.findById(id)).thenReturn(java.util.Optional.of(user));

    assertThrows(APIException.class, () -> userService.updateProfile(id, "Deadpool", "invalid-phone"));
    assertThrows(APIException.class, () -> userService.updateProfile(id, "Deadpool", "123")); // too short
  }

  @Test
  void updateAddress_shouldUpdateSuccessfully() {
    java.util.UUID id = java.util.UUID.randomUUID();
    User user = new User("Wade Wilson", "wade@test.com", "hash");
    when(userRepo.findById(id)).thenReturn(java.util.Optional.of(user));
    when(userRepo.save(user)).thenReturn(user);

    UserAddress address = new UserAddress(null, "123 St", "City", "State", "India", "12345");
    User result = userService.updateAddress(id, address);

    assertNotNull(result.getAddress());
    assertEquals("123 St", result.getAddress().getStreet());
    assertEquals("City", result.getAddress().getCity());
    assertEquals("State", result.getAddress().getState());
    assertEquals("India", result.getAddress().getCountry());
    assertEquals("12345", result.getAddress().getZipcode());
  }

  @Test
  void updateAddress_shouldDefaultCountryToIndiaWhenEmpty() {
    java.util.UUID id = java.util.UUID.randomUUID();
    User user = new User("Wade Wilson", "wade@test.com", "hash");
    when(userRepo.findById(id)).thenReturn(java.util.Optional.of(user));
    when(userRepo.save(user)).thenReturn(user);

    UserAddress address = new UserAddress(null, "123 St", "City", "State", "", "12345");
    User result = userService.updateAddress(id, address);

    assertEquals("India", result.getAddress().getCountry());
  }

  @Test
  void updateAddress_shouldThrowBadRequestWhenStreetIsEmpty() {
    java.util.UUID id = java.util.UUID.randomUUID();
    User user = new User("Wade Wilson", "wade@test.com", "hash");
    when(userRepo.findById(id)).thenReturn(java.util.Optional.of(user));

    UserAddress address = new UserAddress(null, "", "City", "State", "India", "12345");
    assertThrows(APIException.class, () -> userService.updateAddress(id, address));
  }

  @Test
  void updateAddress_shouldThrowBadRequestWhenCityIsEmpty() {
    java.util.UUID id = java.util.UUID.randomUUID();
    User user = new User("Wade Wilson", "wade@test.com", "hash");
    when(userRepo.findById(id)).thenReturn(java.util.Optional.of(user));

    UserAddress address = new UserAddress(null, "123 St", "", "State", "India", "12345");
    assertThrows(APIException.class, () -> userService.updateAddress(id, address));
  }

  @Test
  void updateAddress_shouldThrowBadRequestWhenStateIsEmpty() {
    java.util.UUID id = java.util.UUID.randomUUID();
    User user = new User("Wade Wilson", "wade@test.com", "hash");
    when(userRepo.findById(id)).thenReturn(java.util.Optional.of(user));

    UserAddress address = new UserAddress(null, "123 St", "City", "", "India", "12345");
    assertThrows(APIException.class, () -> userService.updateAddress(id, address));
  }

  @Test
  void updateAddress_shouldThrowBadRequestWhenZipcodeIsEmpty() {
    java.util.UUID id = java.util.UUID.randomUUID();
    User user = new User("Wade Wilson", "wade@test.com", "hash");
    when(userRepo.findById(id)).thenReturn(java.util.Optional.of(user));

    UserAddress address = new UserAddress(null, "123 St", "City", "State", "India", "");
    assertThrows(APIException.class, () -> userService.updateAddress(id, address));
  }
}
