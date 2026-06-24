package com.burito.core.utils;

public class ValidationUtils {
  private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
  private static final String PHONE_REGEX = "^[\\d\\s\\-\\+]{8,15}$";

  private ValidationUtils() {}

  public static boolean isValidEmail(String email) {
    return email != null && email.matches(EMAIL_REGEX);
  }

  public static boolean isValidPassword(String password) {
    return password != null && password.length() >= 8;
  }

  public static boolean isValidPhoneNumber(String phoneNumber) {
    return phoneNumber != null && phoneNumber.matches(PHONE_REGEX);
  }
}
