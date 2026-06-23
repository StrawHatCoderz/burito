package com.burito.identity.service;

import com.burito.identity.domain.User;
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
            .roles("USER")
            .build();
  }
}
