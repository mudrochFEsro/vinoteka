package com.shopapi.backend.dto;

import com.shopapi.backend.entity.Cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartDTO(
        Long id,
        List<CartItemDTO> items,
        BigDecimal totalPrice,
        LocalDateTime createdAt
) {
    public static CartDTO from(Cart cart) {
        var items = cart.getCartItems().stream()
                .map(CartItemDTO::from)
                .toList();

        var total = items.stream()
                .map(CartItemDTO::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDTO(cart.getId(), items, total, cart.getCreatedAt());
    }
}
