package com.burito.core.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

  @Test
  void testIsValidEmail() {
    assertTrue(ValidationUtils.isValidEmail("test@example.com"));
    assertTrue(ValidationUtils.isValidEmail("user.name+tag@domain.co.in"));
    assertFalse(ValidationUtils.isValidEmail(null));
    assertFalse(ValidationUtils.isValidEmail(""));
    assertFalse(ValidationUtils.isValidEmail("invalid-email"));
    assertFalse(ValidationUtils.isValidEmail("user@domain"));
  }

  @Test
  void testIsValidPassword() {
    assertTrue(ValidationUtils.isValidPassword("12345678"));
    assertTrue(ValidationUtils.isValidPassword("password123"));
    assertFalse(ValidationUtils.isValidPassword(null));
    assertFalse(ValidationUtils.isValidPassword(""));
    assertFalse(ValidationUtils.isValidPassword("short"));
  }

  @Test
  void testIsValidPhoneNumber() {
    assertTrue(ValidationUtils.isValidPhoneNumber("12345678"));
    assertTrue(ValidationUtils.isValidPhoneNumber("+1234567890"));
    assertTrue(ValidationUtils.isValidPhoneNumber("123-456-7890"));
    assertTrue(ValidationUtils.isValidPhoneNumber("123 456 7890"));
    assertFalse(ValidationUtils.isValidPhoneNumber(null));
    assertFalse(ValidationUtils.isValidPhoneNumber(""));
    assertFalse(ValidationUtils.isValidPhoneNumber("123"));
    assertFalse(ValidationUtils.isValidPhoneNumber("invalid-phone"));
  }
}
