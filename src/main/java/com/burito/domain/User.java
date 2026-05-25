package com.burito.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long userId;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String hashPassword;

  public User(String email, @Nullable String hashPassword) {
    this.email = email;
    this.hashPassword = hashPassword;
  }

  public User(long userId, String email, @Nullable String hashPassword) {
    this.userId = userId;
    this.email = email;
    this.hashPassword = hashPassword;
  }
}
