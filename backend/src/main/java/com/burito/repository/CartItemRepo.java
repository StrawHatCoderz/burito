package com.burito.repository;

import com.burito.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem, UUID> {
}
