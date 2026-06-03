package com.burito.domain;

import com.burito.enums.MenuCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "menu_item")
@Getter
@Setter
@NoArgsConstructor
public class MenuItem {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID menuItemId;

  @Column(nullable = false)
  private String name;

  private String description;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MenuCategory category;

  @Column(nullable = false)
  private boolean isAvailable;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "restaurant_id", nullable = false)
  private Restaurant restaurant;
}
