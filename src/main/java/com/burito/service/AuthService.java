package com.burito.service;

import com.burito.domain.User;
import com.burito.exceptions.InvalidCredentialsException;
import com.burito.exceptions.UsernameAlreadyExistsException;
import com.burito.repository.UserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepo userRepo;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserRepo userRepo,  PasswordEncoder passwordEncoder) {
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
  }

  public User register(String username, String password)
          throws InvalidCredentialsException, UsernameAlreadyExistsException {
    if (username.isEmpty()) {
      throw new InvalidCredentialsException("Invalid Username");
    }

    if (userRepo.findUserByUsername(username) != null) {
      throw new UsernameAlreadyExistsException();
    }

    if (!isValidPassword(password)) {
      throw new InvalidCredentialsException("Password should be greater than " +
              "8 characters");
    }

    return userRepo.save(new User(username, passwordEncoder.encode(password)));
  }

  private static boolean isValidPassword(String password) {
    return password.length() >= 8;
  }
}
