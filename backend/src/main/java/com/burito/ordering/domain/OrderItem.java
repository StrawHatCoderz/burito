package com.burito.ordering.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    // We store the ID in case we need to link back, but it's not a hard FK to avoid breaking history if menu item is deleted
    @Column(name = "menu_item_id")
    private UUID menuItemId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double priceAtCheckout;

    @Column(nullable = false)
    private Integer quantity;

    public OrderItem(UUID menuItemId, String name, Double priceAtCheckout, Integer quantity) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.priceAtCheckout = priceAtCheckout;
        this.quantity = quantity;
    }
}
