package com.burito.service;

import com.burito.controller.views.JWTToken;
import com.burito.controller.views.UserCreationView;
import com.burito.domain.RefreshToken;
import com.burito.domain.Restaurant;
import com.burito.domain.User;
import com.burito.enums.CuisineType;
import com.burito.enums.Role;
import com.burito.exceptions.EmailAlreadyExistsException;
import com.burito.exceptions.InvalidCredentialsException;
import com.burito.exceptions.InvalidRefreshTokenException;
import com.burito.repository.RestaurantRepo;
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
  private final RefreshTokenService refreshTokenService;
  private final RestaurantRepo restaurantRepo;

  public AuthService(UserRepo userRepo,
                     PasswordEncoder passwordEncoder,
                     AuthenticationManager authenticationManager,
                     JWTService jwtService,
                     RefreshTokenService refreshTokenService,
                     RestaurantRepo restaurantRepo) {
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.refreshTokenService = refreshTokenService;
    this.restaurantRepo = restaurantRepo;
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

    User user = userRepo.save(new User(fullName, email, passwordEncoder.encode(password), Role.USER));

    return new UserCreationView(user.getUserId(), user.getEmail());
  }

  public UserCreationView registerAdmin(String fullName, String email, String password,
                                        String restaurantName, CuisineType cuisineType, double estDeliveryMinutes)
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

    User user = userRepo.save(new User(fullName, email, passwordEncoder.encode(password), Role.RESTAURANT_ADMIN));
    
    Restaurant r = new Restaurant();
    r.setOwnerId(user.getUserId());
    r.setRestaurantName(restaurantName != null ? restaurantName : fullName + "'s Restaurant");
    r.setCuisineType(cuisineType != null ? cuisineType : CuisineType.AMERICAN); // Default
    r.setRating(0.0);
    r.setEstDeliveryMinutes(estDeliveryMinutes > 0 ? estDeliveryMinutes : 30);
    r.setOpen(false);
    restaurantRepo.save(r);

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
    String restaurantId = null;
    if (Role.RESTAURANT_ADMIN.equals(user.getRole())) {
        Restaurant restaurant = restaurantRepo.findByOwnerId(user.getUserId());
        if (restaurant != null && restaurant.getRestaurantId() != null) {
            restaurantId = restaurant.getRestaurantId().toString();
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
        Restaurant restaurant = restaurantRepo.findByOwnerId(user.getUserId());
        if (restaurant != null && restaurant.getRestaurantId() != null) {
            restaurantId = restaurant.getRestaurantId().toString();
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
