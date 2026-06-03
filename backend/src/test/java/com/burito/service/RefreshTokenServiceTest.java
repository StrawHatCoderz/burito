package com.burito.service;

import com.burito.domain.RefreshToken;
import com.burito.domain.User;
import com.burito.exceptions.InvalidRefreshTokenException;
import com.burito.repository.RefreshTokenRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  @Mock private RefreshTokenRepo refreshTokenRepo;
  @InjectMocks private RefreshTokenService refreshTokenService;

  @Test
  void shouldCreateAndPersistRefreshToken() {
    User user = new User("Wade", "wade@test.com", "hash");
    when(refreshTokenRepo.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

    RefreshToken result = refreshTokenService.create(user);

    ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(refreshTokenRepo).save(captor.capture());
    RefreshToken saved = captor.getValue();

    assertNotNull(saved.getToken());
    assertEquals(user, saved.getUser());
    assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now()));
  }

  @Test
  void shouldReturnTokenWhenValid() throws Exception {
    User user = new User("Wade", "wade@test.com", "hash");
    RefreshToken token = new RefreshToken();
    token.setToken("valid-token");
    token.setUser(user);
    token.setExpiresAt(LocalDateTime.now().plusDays(7));

    when(refreshTokenRepo.findByToken("valid-token")).thenReturn(Optional.of(token));

    RefreshToken result = refreshTokenService.validate("valid-token");

    assertEquals("valid-token", result.getToken());
  }

  @Test
  void shouldThrowWhenTokenNotFound() {
    when(refreshTokenRepo.findByToken("missing")).thenReturn(Optional.empty());

    assertThrows(InvalidRefreshTokenException.class,
            () -> refreshTokenService.validate("missing"));
  }

  @Test
  void shouldThrowAndDeleteWhenTokenIsExpired() {
    RefreshToken token = new RefreshToken();
    token.setToken("expired-token");
    token.setExpiresAt(LocalDateTime.now().minusDays(1));

    when(refreshTokenRepo.findByToken("expired-token")).thenReturn(Optional.of(token));

    assertThrows(InvalidRefreshTokenException.class,
            () -> refreshTokenService.validate("expired-token"));
    verify(refreshTokenRepo).delete(token);
  }

  @Test
  void shouldDeleteTokenOnRevoke() {
    RefreshToken token = new RefreshToken();
    token.setToken("some-token");

    when(refreshTokenRepo.findByToken("some-token")).thenReturn(Optional.of(token));

    refreshTokenService.revoke("some-token");

    verify(refreshTokenRepo).delete(token);
  }

  @Test
  void shouldDoNothingOnRevokeWhenTokenNotFound() {
    when(refreshTokenRepo.findByToken("unknown")).thenReturn(Optional.empty());

    assertDoesNotThrow(() -> refreshTokenService.revoke("unknown"));
    verify(refreshTokenRepo, never()).delete(any());
  }

  @Test
  void shouldDeleteAllTokensForUser() {
    User user = new User("Wade", "wade@test.com", "hash");

    refreshTokenService.revokeAll(user);

    verify(refreshTokenRepo).deleteAllByUser(user);
  }
}
