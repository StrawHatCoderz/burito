package com.burito.repository;

import com.burito.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {
  User findUserByEmail(String email);
}
