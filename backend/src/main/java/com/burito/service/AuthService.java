package com.burito.service;

import com.burito.controller.views.JWTToken;
import com.burito.controller.views.UserCreationView;
import com.burito.domain.User;
import com.burito.exceptions.EmailAlreadyExistsException;
import com.burito.exceptions.InvalidCredentialsException;
import com.burito.repository.UserRepo;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepo userRepo;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JWTService jwtService;

  public AuthService(UserRepo userRepo,
                     PasswordEncoder passwordEncoder,
                     AuthenticationManager authenticationManager,
                     JWTService jwtService) {
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  public UserCreationView register(String fullName, String email, String password)
          throws InvalidCredentialsException, EmailAlreadyExistsException {

    if (fullName == null || fullName.isEmpty()) {
      throw new InvalidCredentialsException("Full name cannot be empty");
    }

    if (!isValidEmail(email)) {
      throw new InvalidCredentialsException("Invalid email");
    }

    if (userRepo.findUserByEmail(email) != null) {
      throw new EmailAlreadyExistsException();
    }

    if (!isValidPassword(password)) {
      throw new InvalidCredentialsException("Password should be greater than 8 characters");
    }

    User user = userRepo.save(new User(fullName, email, passwordEncoder.encode(password)));

    return new UserCreationView(user.getUserId(), user.getEmail());
  }

  private static boolean isValidPassword(String password) {
    return password != null && password.length() >= 8;
  }

  private static boolean isValidEmail(String email) {
    String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    return email != null && email.matches(regex);
  }

  public JWTToken login(String email, String password) throws InvalidCredentialsException {
    try {
      authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(email, password)
      );
    } catch (Exception _) {
      throw new InvalidCredentialsException("Invalid credentials");
    }

    User user = userRepo.findUserByEmail(email);
    return jwtService.sign(user);
  }

  public User getCurrentUser(String email) {
    return userRepo.findUserByEmail(email);
  }
}
