package com.burito.service;

import com.burito.controller.views.JWTToken;
import com.burito.controller.views.UserCreationView;
import com.burito.domain.User;
import com.burito.exceptions.APIException;
import com.burito.exceptions.InvalidCredentialsException;
import com.burito.exceptions.EmailAlreadyExistsException;
import com.burito.repository.UserRepo;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepo userRepo;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final UserService userService;
  private final JWTService jwtService;

  public AuthService(UserRepo userRepo,
                     PasswordEncoder passwordEncoder,
                     AuthenticationManager authenticationManager,
                     UserService userService, JWTService jwtService) {
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.userService = userService;
    this.jwtService = jwtService;
  }

  public UserCreationView register(String email, String password)
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

    User user = userRepo.save(new User(email,
            passwordEncoder.encode(password)));

    return new UserCreationView(user.getUserId(), user.getEmail());
  }

  private static boolean isValidPassword(String password) {
    return password != null && password.length() >= 8;
  }

  private static boolean isValidEmail(String email) {
    String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    return email != null && email.matches(regex);
  }

  public JWTToken login(String email, String password)
          throws InvalidCredentialsException, UsernameNotFoundException {
    try {
      authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(email, password)
      );
    } catch (Exception e) {
      throw new InvalidCredentialsException(String.format("Invalid email " +
              "or password: %s", e.getMessage()));
    }

    try {
      UserDetails existing = userService.loadUserByUsername(email);
      return jwtService.sign(existing);
    } catch (UsernameNotFoundException e) {
      throw new UsernameNotFoundException(e.getMessage());
    }
  }
}
