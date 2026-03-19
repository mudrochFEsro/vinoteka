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
        var user = userService.getOrCreateCurrentUser();
        var cart = getOrCreateCart(user);
        return CartDTO.from(cart);
    }

    @Transactional
    public CartDTO addItem(AddToCartRequest request) {
        var user = userService.getOrCreateCurrentUser();
        var cart = getOrCreateCart(user);

        var product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        var requestedQty = request.quantity();
        validateStock(product, requestedQty);

        var existingItem = findCartItem(cart, product.getId());

        if (existingItem != null) {
            var newQuantity = existingItem.getQuantity() + requestedQty;
            validateStock(product, newQuantity);
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            var newItem = CartItem.builder()
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
        var user = userService.getOrCreateCurrentUser();
        var cart = getCartOrThrow(user.getId());

        var item = cart.getCartItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("Cart item not found with id: " + itemId));

        validateStock(item.getProduct(), request.quantity());

        item.setQuantity(request.quantity());
        cartItemRepository.save(item);

        return CartDTO.from(cart);
    }

    @Transactional
    public CartDTO removeItem(Long itemId) {
        var user = userService.getOrCreateCurrentUser();
        var cart = getCartOrThrow(user.getId());

        var removed = cart.getCartItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new CartNotFoundException("Cart item not found with id: " + itemId);
        }

        cartRepository.save(cart);
        return CartDTO.from(cart);
    }

    @Transactional
    public void clearCart() {
        var user = userService.getOrCreateCurrentUser();
        cartRepository.findByUserId(user.getId())
                .ifPresent(cart -> {
                    cart.getCartItems().clear();
                    cartRepository.save(cart);
                });
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().user(user).build()
                ));
    }

    private Cart getCartOrThrow(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));
    }

    private CartItem findCartItem(Cart cart, Long productId) {
        return cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    private void validateStock(Product product, int quantity) {
        if (product.getStock() < quantity) {
            throw new InsufficientStockException(product.getName(), product.getStock(), quantity);
        }
    }
}
