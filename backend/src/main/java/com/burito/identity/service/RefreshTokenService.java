package com.burito.identity.service;

import com.burito.identity.domain.RefreshToken;
import com.burito.identity.domain.User;
import com.burito.identity.exceptions.InvalidRefreshTokenException;
import com.burito.identity.repository.RefreshTokenRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {
  private static final int EXPIRY_DAYS = 7;

  private final RefreshTokenRepo refreshTokenRepo;

  public RefreshTokenService(RefreshTokenRepo refreshTokenRepo) {
    this.refreshTokenRepo = refreshTokenRepo;
  }

  public RefreshToken create(User user) {
    RefreshToken token = new RefreshToken();
    token.setToken(UUID.randomUUID().toString());
    token.setUser(user);
    token.setExpiresAt(LocalDateTime.now().plusDays(EXPIRY_DAYS));
    return refreshTokenRepo.save(token);
  }

  public RefreshToken validate(String tokenStr) throws InvalidRefreshTokenException {
    RefreshToken token = refreshTokenRepo.findByToken(tokenStr)
            .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));
    if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
      refreshTokenRepo.delete(token);
      throw new InvalidRefreshTokenException("Refresh token expired");
    }
    return token;
  }

  public void revoke(String tokenStr) {
    refreshTokenRepo.findByToken(tokenStr).ifPresent(refreshTokenRepo::delete);
  }

  @Transactional
  public void revokeAll(User user) {
    refreshTokenRepo.deleteAllByUser(user);
  }
}
