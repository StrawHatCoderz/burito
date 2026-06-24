package com.burito.identity.service;

import com.burito.identity.domain.User;
import com.burito.identity.domain.UserAddress;
import com.burito.core.exceptions.APIException;
import com.burito.core.utils.ValidationUtils;
import com.burito.identity.repository.UserRepo;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
  private final UserRepo userRepo;

  public UserService(UserRepo userRepo) {
    this.userRepo = userRepo;
  }

  public User findUserByEmail(String email) {
    return userRepo.findUserByEmail(email);
  }

  public User findUserById(java.util.UUID userId) {
    return userRepo.findById(userId).orElse(null);
  }

  @NullMarked
  @Override
  public UserDetails loadUserByUsername(String email)
          throws UsernameNotFoundException {
    User user = userRepo.findUserByEmail(email);

    if (user == null) {
      throw new UsernameNotFoundException(String.format("user with %s does " +
              "not exists", email));
    }

    return org.springframework.security.core.userdetails.User.builder()
            .username(user.getEmail())
            .password(user.getHashPassword())
            .roles(user.getRole().name())
            .build();
  }

  public User updateProfile(java.util.UUID userId, String fullName, String phoneNumber) {
    User user = findUserById(userId);
    if (user == null) {
      throw APIException.notFound("User not found");
    }
    if (fullName == null || fullName.trim().isEmpty()) {
      throw APIException.badRequest("Name cannot be empty");
    }
    if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
      String cleanPhone = phoneNumber.trim();
      if (!ValidationUtils.isValidPhoneNumber(cleanPhone)) {
        throw APIException.badRequest("Invalid phone number format");
      }
      user.setPhoneNumber(cleanPhone);
    } else {
      user.setPhoneNumber(null);
    }
    user.setFullName(fullName.trim());
    return userRepo.save(user);
  }

  public User updateAddress(java.util.UUID userId, UserAddress address) {
    User user = findUserById(userId);
    if (user == null) {
      throw APIException.notFound("User not found");
    }
    if (address == null) {
      throw APIException.badRequest("Address is required");
    }
    if (address.getStreet() == null || address.getStreet().trim().isEmpty()) {
      throw APIException.badRequest("Street is required");
    }
    if (address.getCity() == null || address.getCity().trim().isEmpty()) {
      throw APIException.badRequest("City is required");
    }
    if (address.getState() == null || address.getState().trim().isEmpty()) {
      throw APIException.badRequest("State is required");
    }
    if (address.getZipcode() == null || address.getZipcode().trim().isEmpty()) {
      throw APIException.badRequest("Zipcode is required");
    }
    String country = address.getCountry();
    if (country == null || country.trim().isEmpty()) {
      country = "India";
    }

    UserAddress userAddress = user.getAddress();
    if (userAddress == null) {
      userAddress = new UserAddress();
    }
    userAddress.setStreet(address.getStreet().trim());
    userAddress.setCity(address.getCity().trim());
    userAddress.setState(address.getState().trim());
    userAddress.setZipcode(address.getZipcode().trim());
    userAddress.setCountry(country.trim());

    user.setAddress(userAddress);
    return userRepo.save(user);
  }
}
