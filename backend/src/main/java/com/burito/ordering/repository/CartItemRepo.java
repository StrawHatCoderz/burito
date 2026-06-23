package com.burito.ordering.repository;

import com.burito.ordering.domain.Cart;
import com.burito.ordering.domain.CartItem;
import com.burito.catalog.domain.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem, UUID> {
    List<CartItem> findByCart_CartId(UUID cartId);
    Optional<CartItem> findByCartAndMenuItem(Cart cart, MenuItem menuItem);
    void deleteByCart_CartId(UUID cartId);
}
