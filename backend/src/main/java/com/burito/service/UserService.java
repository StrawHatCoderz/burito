package com.burito.service;

import com.burito.domain.User;
import com.burito.repository.UserRepo;
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
