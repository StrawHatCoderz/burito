package com.burito.identity.repository;

import com.burito.identity.domain.RefreshToken;
import com.burito.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByToken(String token);
  void deleteAllByUser(User user);
}
