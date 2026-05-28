package com.burito.service;

import com.burito.controller.views.JWTToken;
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
  private final SecretKey key;

  public JWTService(@Value("${jwt.secret}") String secret) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
  }

  public JWTToken sign(User user) {
    double expiresInMins = 60.0;
    String token = Jwts.builder()
            .setSubject(user.getEmail())
            .claim("userId", user.getUserId() != null ? user.getUserId().toString() : null)
            .claim("role", "USER")
            .setIssuedAt(new Date())
            .setExpiration(new Date((long) (System.currentTimeMillis() + 1000 * expiresInMins * 60)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

    return new JWTToken(token, expiresInMins);
  }

  public String extractUsername(String token) {
    return getClaims(token).getSubject();
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

  private boolean isTokenExpired(String token) {
    return getClaims(token).getExpiration().before(new Date());
  }
}
