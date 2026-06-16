package com.burito.service;

import com.burito.controller.views.CartItemView;
import com.burito.controller.views.CartView;
import com.burito.domain.Cart;
import com.burito.domain.CartItem;
import com.burito.enums.CartStatus;
import com.burito.domain.MenuItem;
import com.burito.domain.User;
import com.burito.exceptions.MenuItemNotFoundException;
import com.burito.exceptions.MenuItemUnavailableException;
import com.burito.exceptions.CartItemNotFoundException;
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
     * Adds a menu item to the user's cart or guest's cart.
     * Handles cart creation, item quantity updates, cross-restaurant cart resets,
     * and automatic recomputation of cart totals.
     */
    @Transactional
    public CartView addItem(UUID userId, UUID guestId, UUID menuItemId, int quantity)
            throws MenuItemNotFoundException, MenuItemUnavailableException {
        MenuItem menuItem = getValidatedMenuItem(menuItemId);
        User user = userId != null ? getUser(userId) : null;

        Cart cart = getCartEntity(userId, guestId);

        if (cart == null) {
            cart = createCartWithItem(user, guestId, menuItem, quantity);
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

    @Transactional(readOnly = true)
    public CartView getCart(UUID userId, UUID guestId) {
        Cart cart = getCartEntity(userId, guestId);
        if (cart == null) {
            return new CartView(null, null, List.of(), BigDecimal.ZERO);
        }
        List<CartItem> items = cartItemRepo.findByCart_CartId(cart.getCartId());
        return mapToCartView(cart, items);
    }

    @Transactional
    public CartView removeItem(UUID userId, UUID guestId, UUID cartItemId) throws CartItemNotFoundException {
        Cart cart = getCartEntity(userId, guestId);
        if (cart == null) {
            throw new CartItemNotFoundException(cartItemId);
        }

        CartItem itemToRemove = cartItemRepo.findById(cartItemId)
                .filter(item -> item.getCart().getCartId().equals(cart.getCartId()))
                .orElseThrow(() -> new CartItemNotFoundException(cartItemId));

        cartItemRepo.delete(itemToRemove);
        cartItemRepo.flush();

        List<CartItem> remainingItems = cartItemRepo.findByCart_CartId(cart.getCartId());
        if (remainingItems.isEmpty()) {
            cart.setStatus(CartStatus.EXPIRED);
            cartRepo.save(cart);
            return new CartView(null, null, List.of(), BigDecimal.ZERO);
        }

        cart.setTotal(calculateCartTotal(cart.getCartId()));
        cartRepo.save(cart);
        return mapToCartView(cart, remainingItems);
    }

    @Transactional
    public CartView decrementItem(UUID userId, UUID guestId, UUID cartItemId) throws CartItemNotFoundException {
        Cart cart = getCartEntity(userId, guestId);
        if (cart == null) {
            throw new CartItemNotFoundException(cartItemId);
        }

        CartItem item = cartItemRepo.findById(cartItemId)
                .filter(i -> i.getCart().getCartId().equals(cart.getCartId()))
                .orElseThrow(() -> new CartItemNotFoundException(cartItemId));

        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            cartItemRepo.save(item);
            cartItemRepo.flush();
            cart.setTotal(calculateCartTotal(cart.getCartId()));
            cartRepo.save(cart);
            return mapToCartView(cart, cartItemRepo.findByCart_CartId(cart.getCartId()));
        } else {
            return removeItem(userId, guestId, cartItemId);
        }
    }

    @Transactional
    public CartView clearCart(UUID userId, UUID guestId) {
        Cart cart = getCartEntity(userId, guestId);
        if (cart == null) {
            return new CartView(null, null, List.of(), BigDecimal.ZERO);
        }

        cartItemRepo.deleteByCart_CartId(cart.getCartId());
        cart.setStatus(CartStatus.EXPIRED);
        cartRepo.save(cart);

        return new CartView(null, null, List.of(), BigDecimal.ZERO);
    }

    @Transactional
    public void mergeCart(UUID userId, UUID guestId) {
        if (userId == null || guestId == null) {
            return;
        }
        Cart guestCart = cartRepo.findByGuestIdAndStatus(guestId, CartStatus.PENDING).orElse(null);
        if (guestCart == null) {
            return;
        }

        Cart userCart = cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING).orElse(null);
        if (userCart != null) {
            // User already has a cart, discard guest cart by marking it expired
            guestCart.setStatus(CartStatus.EXPIRED);
            cartRepo.save(guestCart);
        } else {
            // Re-assign guest cart to user
            User user = getUser(userId);
            guestCart.setUser(user);
            guestCart.setGuestId(null);
            cartRepo.save(guestCart);
        }
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

    private Cart getCartEntity(UUID userId, UUID guestId) {
        if (userId != null) {
            return cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING).orElse(null);
        } else if (guestId != null) {
            return cartRepo.findByGuestIdAndStatus(guestId, CartStatus.PENDING).orElse(null);
        }
        throw new IllegalArgumentException("Either userId or guestId must be provided");
    }

    private boolean isDifferentRestaurant(Cart cart, MenuItem menuItem) {
        UUID cartRestaurantId = cart.getRestaurant().getRestaurantId();
        UUID itemRestaurantId = menuItem.getRestaurant().getRestaurantId();
        return !cartRestaurantId.equals(itemRestaurantId);
    }

    private Cart createCartWithItem(User user, UUID guestId, MenuItem menuItem, int quantity) {
        Cart cart;
        if (user != null) {
            cart = new Cart(user, menuItem.getRestaurant());
        } else {
            cart = new Cart(guestId, menuItem.getRestaurant());
        }
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
