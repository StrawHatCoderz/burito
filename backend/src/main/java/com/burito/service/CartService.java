package com.burito.service;

import com.burito.controller.views.CartItemView;
import com.burito.controller.views.CartView;
import com.burito.domain.Cart;
import com.burito.domain.CartItem;
import com.burito.domain.MenuItem;
import com.burito.domain.User;
import com.burito.exceptions.MenuItemNotFoundException;
import com.burito.exceptions.MenuItemUnavailableException;
import com.burito.repository.CartItemRepo;
import com.burito.repository.CartRepo;
import com.burito.repository.MenuItemRepo;
import com.burito.repository.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepo cartRepo;
    private final CartItemRepo cartItemRepo;
    private final MenuItemRepo menuItemRepo;
    private final UserRepo userRepo;

    public CartService(CartRepo cartRepo,
                       CartItemRepo cartItemRepo,
                       MenuItemRepo menuItemRepo,
                       UserRepo userRepo) {
        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.menuItemRepo = menuItemRepo;
        this.userRepo = userRepo;
    }

    /**
     * Adds a menu item to the user's cart.
     * Handles cart creation, item quantity updates, cross-restaurant cart resets,
     * and automatic recomputation of cart totals.
     */
    @Transactional
    public CartView addItem(UUID userId, UUID menuItemId, int quantity)
            throws MenuItemNotFoundException, MenuItemUnavailableException {
        MenuItem menuItem = getValidatedMenuItem(menuItemId);
        User user = getUser(userId);

        Cart cart = cartRepo.findByUser_UserId(userId).orElse(null);

        if (cart == null) {
            cart = createCartWithItem(user, menuItem, quantity);
        } else if (isDifferentRestaurant(cart, menuItem)) {
            cart = resetCartWithItem(cart, menuItem, quantity);
        } else {
            cart = updateCartWithItem(cart, menuItem, quantity);
        }

        // Ensure database synchronization and fetch latest state for the view
        cartItemRepo.flush();
        cartRepo.flush();

        List<CartItem> finalItems = cartItemRepo.findByCart_CartId(cart.getCartId());
        return mapToCartView(cart, finalItems);
    }

    private MenuItem getValidatedMenuItem(UUID menuItemId)
            throws MenuItemNotFoundException, MenuItemUnavailableException {
        MenuItem menuItem = menuItemRepo.findById(menuItemId)
                .orElseThrow(() -> new MenuItemNotFoundException(menuItemId));

        if (!menuItem.isAvailable()) {
            throw new MenuItemUnavailableException(menuItemId);
        }

        return menuItem;
    }

    private User getUser(UUID userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }

    private boolean isDifferentRestaurant(Cart cart, MenuItem menuItem) {
        UUID cartRestaurantId = cart.getRestaurant().getRestaurantId();
        UUID itemRestaurantId = menuItem.getRestaurant().getRestaurantId();
        return !cartRestaurantId.equals(itemRestaurantId);
    }

    private Cart createCartWithItem(User user, MenuItem menuItem, int quantity) {
        Cart cart = new Cart(user, menuItem.getRestaurant());
        cart = cartRepo.save(cart);

        CartItem newItem = new CartItem(cart, menuItem, quantity, menuItem.getPrice());
        cartItemRepo.save(newItem);

        cart.setTotal(newItem.getSubtotal());
        return cartRepo.save(cart);
    }

    private Cart resetCartWithItem(Cart cart, MenuItem menuItem, int quantity) {
        cartItemRepo.deleteByCart_CartId(cart.getCartId());
        cart.setRestaurant(menuItem.getRestaurant());

        CartItem newItem = new CartItem(cart, menuItem, quantity, menuItem.getPrice());
        cartItemRepo.save(newItem);

        cart.setTotal(newItem.getSubtotal());
        return cartRepo.save(cart);
    }

    private Cart updateCartWithItem(Cart cart, MenuItem menuItem, int quantity) {
        cartItemRepo.findByCartAndMenuItem(cart, menuItem)
                .ifPresentOrElse(
                        existingItem -> {
                            existingItem.setQuantity(existingItem.getQuantity() + quantity);
                            cartItemRepo.save(existingItem);
                        },
                        () -> {
                            CartItem newItem = new CartItem(cart, menuItem, quantity, menuItem.getPrice());
                            cartItemRepo.save(newItem);
                        }
                );

        // Synchronize changes to allow subtotal SUM querying
        cartItemRepo.flush();

        BigDecimal total = calculateCartTotal(cart.getCartId());
        cart.setTotal(total);
        return cartRepo.save(cart);
    }

    private BigDecimal calculateCartTotal(UUID cartId) {
        List<CartItem> items = cartItemRepo.findByCart_CartId(cartId);
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CartView mapToCartView(Cart cart, List<CartItem> items) {
        List<CartItemView> itemViews = items.stream()
                .map(item -> new CartItemView(
                        item.getCartItemId(),
                        item.getMenuItem().getMenuItemId(),
                        item.getMenuItem().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new CartView(
                cart.getCartId(),
                cart.getRestaurant().getRestaurantId(),
                itemViews,
                cart.getTotal()
        );
    }
}
