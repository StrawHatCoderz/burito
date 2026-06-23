package com.burito.ordering.domain;

import com.burito.catalog.domain.MenuItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CartItemTest {

    @Test
    void testConstructorAndGetters() {
        Cart cart = new Cart();
        MenuItem menuItem = new MenuItem();
        
        CartItem cartItem = new CartItem(cart, menuItem, 2, new BigDecimal("10.50"));

        assertEquals(cart, cartItem.getCart());
        assertEquals(menuItem, cartItem.getMenuItem());
        assertEquals(2, cartItem.getQuantity());
        assertEquals(new BigDecimal("10.50"), cartItem.getUnitPrice());
        assertEquals(new BigDecimal("21.00"), cartItem.getSubtotal());
    }

    @Test
    void testSetQuantityUpdatesSubtotal() {
        CartItem cartItem = new CartItem(new Cart(), new MenuItem(), 2, new BigDecimal("10.50"));
        
        cartItem.setQuantity(3);

        assertEquals(3, cartItem.getQuantity());
        assertEquals(new BigDecimal("31.50"), cartItem.getSubtotal());
    }

    @Test
    void testSetUnitPriceUpdatesSubtotal() {
        CartItem cartItem = new CartItem(new Cart(), new MenuItem(), 2, new BigDecimal("10.50"));
        
        cartItem.setUnitPrice(new BigDecimal("15.00"));

        assertEquals(new BigDecimal("15.00"), cartItem.getUnitPrice());
        assertEquals(new BigDecimal("30.00"), cartItem.getSubtotal());
    }

    @Test
    void testSetters() {
        CartItem cartItem = new CartItem();
        UUID id = UUID.randomUUID();
        Cart cart = new Cart();
        MenuItem menuItem = new MenuItem();
        
        cartItem.setCartItemId(id);
        cartItem.setCart(cart);
        cartItem.setMenuItem(menuItem);
        cartItem.setUnitPrice(new BigDecimal("10.00"));
        cartItem.setQuantity(2); // this will also update subtotal

        assertEquals(id, cartItem.getCartItemId());
        assertEquals(cart, cartItem.getCart());
        assertEquals(menuItem, cartItem.getMenuItem());
        assertEquals(2, cartItem.getQuantity());
        assertEquals(new BigDecimal("10.00"), cartItem.getUnitPrice());
        assertEquals(new BigDecimal("20.00"), cartItem.getSubtotal());
    }
}
