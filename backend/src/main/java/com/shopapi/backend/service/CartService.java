package com.shopapi.backend.service;

import com.shopapi.backend.dto.AddToCartRequest;
import com.shopapi.backend.dto.CartDTO;
import com.shopapi.backend.dto.UpdateCartItemRequest;
import com.shopapi.backend.entity.Cart;
import com.shopapi.backend.entity.CartItem;
import com.shopapi.backend.entity.Product;
import com.shopapi.backend.entity.User;
import com.shopapi.backend.exception.CartNotFoundException;
import com.shopapi.backend.exception.InsufficientStockException;
import com.shopapi.backend.exception.ProductNotFoundException;
import com.shopapi.backend.repository.CartItemRepository;
import com.shopapi.backend.repository.CartRepository;
import com.shopapi.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    @Transactional
    public CartDTO getMyCart() {
        User user = userService.getOrCreateCurrentUser();
        Cart cart = getOrCreateCart(user);
        return CartDTO.from(cart);
    }

    @Transactional
    public CartDTO addItem(AddToCartRequest request) {
        User user = userService.getOrCreateCurrentUser();
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        // Check stock
        int requestedQty = request.quantity();
        if (product.getStock() < requestedQty) {
            throw new InsufficientStockException(product.getName(), product.getStock(), requestedQty);
        }

        // Check if item already in cart
        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + requestedQty;
            if (product.getStock() < newQuantity) {
                throw new InsufficientStockException(product.getName(), product.getStock(), newQuantity);
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(requestedQty)
                    .build();
            cart.getCartItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return CartDTO.from(cart);
    }

    @Transactional
    public CartDTO updateItemQuantity(Long itemId, UpdateCartItemRequest request) {
        User user = userService.getOrCreateCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));

        CartItem item = cart.getCartItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("Cart item not found with id: " + itemId));

        // Check stock
        Product product = item.getProduct();
        if (product.getStock() < request.quantity()) {
            throw new InsufficientStockException(product.getName(), product.getStock(), request.quantity());
        }

        item.setQuantity(request.quantity());
        cartItemRepository.save(item);

        return CartDTO.from(cart);
    }

    @Transactional
    public CartDTO removeItem(Long itemId) {
        User user = userService.getOrCreateCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));

        boolean removed = cart.getCartItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new CartNotFoundException("Cart item not found with id: " + itemId);
        }

        cartRepository.save(cart);
        return CartDTO.from(cart);
    }

    @Transactional
    public void clearCart() {
        User user = userService.getOrCreateCurrentUser();
        cartRepository.findByUserId(user.getId())
                .ifPresent(cart -> {
                    cart.getCartItems().clear();
                    cartRepository.save(cart);
                });
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }
}
