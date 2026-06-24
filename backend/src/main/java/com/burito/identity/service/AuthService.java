package com.burito.identity.service;

import com.burito.identity.controller.views.JWTToken;
import com.burito.identity.controller.views.UserCreationView;
import com.burito.identity.domain.RefreshToken;
import com.burito.catalog.domain.Restaurant;
import com.burito.identity.domain.User;
import com.burito.catalog.enums.CuisineType;
import com.burito.identity.enums.Role;
import com.burito.identity.exceptions.EmailAlreadyExistsException;
import com.burito.identity.exceptions.InvalidCredentialsException;
import com.burito.identity.exceptions.InvalidRefreshTokenException;
import com.burito.catalog.service.RestaurantService;
import com.burito.identity.repository.UserRepo;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.burito.core.utils.ValidationUtils;

@Service
public class AuthService {
  private final UserRepo userRepo;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JWTService jwtService;
  private final RefreshTokenService refreshTokenService;
  private final RestaurantService restaurantService;

  public AuthService(UserRepo userRepo,
                     PasswordEncoder passwordEncoder,
                     AuthenticationManager authenticationManager,
                     JWTService jwtService,
                     RefreshTokenService refreshTokenService,
                     RestaurantService restaurantService) {
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.refreshTokenService = refreshTokenService;
    this.restaurantService = restaurantService;
  }

  public UserCreationView register(String fullName, String email, String password)
          throws InvalidCredentialsException, EmailAlreadyExistsException {

    if (fullName == null || fullName.isEmpty()) {
      throw new InvalidCredentialsException("Full name cannot be empty");
    }

    if (!ValidationUtils.isValidEmail(email)) {
      throw new InvalidCredentialsException("Invalid email");
    }

    if (userRepo.findUserByEmail(email) != null) {
      throw new EmailAlreadyExistsException();
    }

    if (!ValidationUtils.isValidPassword(password)) {
      throw new InvalidCredentialsException("Password should be greater than 8 characters");
    }

    User user = userRepo.save(new User(fullName, email, passwordEncoder.encode(password), Role.USER));

    return new UserCreationView(user.getUserId(), user.getEmail());
  }

  public UserCreationView registerAdmin(String fullName, String email, String password,
                                        String restaurantName, CuisineType cuisineType, double estDeliveryMinutes)
          throws InvalidCredentialsException, EmailAlreadyExistsException {

    if (fullName == null || fullName.isEmpty()) {
      throw new InvalidCredentialsException("Full name cannot be empty");
    }

    if (!ValidationUtils.isValidEmail(email)) {
      throw new InvalidCredentialsException("Invalid email");
    }

    if (userRepo.findUserByEmail(email) != null) {
      throw new EmailAlreadyExistsException();
    }

    if (!ValidationUtils.isValidPassword(password)) {
      throw new InvalidCredentialsException("Password should be greater than 8 characters");
    }

    User user = userRepo.save(new User(fullName, email, passwordEncoder.encode(password), Role.RESTAURANT_ADMIN));
    
    restaurantService.createRestaurantForAdmin(user.getUserId(), restaurantName != null ? restaurantName : fullName + "'s Restaurant", cuisineType, estDeliveryMinutes);

    return new UserCreationView(user.getUserId(), user.getEmail());
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
    String restaurantId = null;
    if (Role.RESTAURANT_ADMIN.equals(user.getRole())) {
        java.util.UUID rId = restaurantService.getRestaurantIdByOwnerId(user.getUserId());
        if (rId != null) {
            restaurantId = rId.toString();
        }
    }
    String accessToken = jwtService.sign(user, restaurantId);
    RefreshToken refreshToken = refreshTokenService.create(user);
    return new JWTToken(accessToken, refreshToken.getToken(), JWTService.ACCESS_TOKEN_EXPIRY_MINS);
  }

  public JWTToken refresh(String refreshTokenStr) throws InvalidRefreshTokenException {
    RefreshToken existing = refreshTokenService.validate(refreshTokenStr);
    User user = existing.getUser();
    refreshTokenService.revoke(refreshTokenStr);
    
    String restaurantId = null;
    if (Role.RESTAURANT_ADMIN.equals(user.getRole())) {
        java.util.UUID rId = restaurantService.getRestaurantIdByOwnerId(user.getUserId());
        if (rId != null) {
            restaurantId = rId.toString();
        }
    }
    String accessToken = jwtService.sign(user, restaurantId);
    RefreshToken newRefreshToken = refreshTokenService.create(user);
    return new JWTToken(accessToken, newRefreshToken.getToken(), JWTService.ACCESS_TOKEN_EXPIRY_MINS);
  }

  public void logout(String refreshTokenStr) {
    refreshTokenService.revoke(refreshTokenStr);
  }

  public User getCurrentUser(String email) {
    return userRepo.findUserByEmail(email);
  }
}
