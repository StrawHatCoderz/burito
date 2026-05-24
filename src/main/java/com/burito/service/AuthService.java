package com.burito.service;

import com.burito.domain.User;
import com.burito.exceptions.InvalidCredentialsException;
import com.burito.exceptions.EmailAlreadyExistsException;
import com.burito.repository.UserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepo userRepo;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
  }

  public User register(String email, String password)
          throws InvalidCredentialsException, EmailAlreadyExistsException {
    if (!isValidEmail(email)) {
      throw new InvalidCredentialsException("Invalid email");
    }

    if (userRepo.findUserByEmail(email) != null) {
      throw new EmailAlreadyExistsException();
    }

    if (!isValidPassword(password)) {
      throw new InvalidCredentialsException("Password should be greater than " +
              "8 characters");
    }

    return userRepo.save(new User(email, passwordEncoder.encode(password)));
  }

  private static boolean isValidPassword(String password) {
    return password != null && password.length() >= 8;
  }

  private static boolean isValidEmail(String email) {
    String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    return email != null && email.matches(regex);
  }
}
