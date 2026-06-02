package com.burito.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID userId;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String hashPassword;

  @Column
  private String fullName;

  @Column(nullable = false, updatable = false)
  @CreationTimestamp
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime createdAt;

  public User(String fullName, String email, @Nullable String hashPassword) {
    this.fullName = fullName;
    this.email = email;
    this.hashPassword = hashPassword;
  }
}
