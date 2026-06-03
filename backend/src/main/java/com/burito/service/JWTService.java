package com.burito.service;

import com.burito.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JWTService {
  public static final double ACCESS_TOKEN_EXPIRY_MINS = 60.0;

  private final SecretKey key;

  public JWTService(@Value("${jwt.secret}") String secret) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
  }

  public String sign(User user) {
    return Jwts.builder()
            .setSubject(user.getEmail())
            .claim("userId", user.getUserId() != null ? user.getUserId().toString() : null)
            .claim("role", "USER")
            .setIssuedAt(new Date())
            .setExpiration(new Date((long) (System.currentTimeMillis() + 1000 * ACCESS_TOKEN_EXPIRY_MINS * 60)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
  }

  public String extractUsername(String token) {
    return getClaims(token).getSubject();
  }

  public String extractRole(String token) {
    return (String) getClaims(token).get("role");
  }

  private Claims getClaims(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
  }

  public boolean isValidToken(String token, UserDetails userDetails) {
    String username = extractUsername(token);
    return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  // JJWT throws ExpiredJwtException on parse before this can return true;
  // the true branch is therefore unreachable at runtime.
  private boolean isTokenExpired(String token) {
    return getClaims(token).getExpiration().before(new Date());
  }
}
