package com.shopapi.backend.dto;

import com.shopapi.backend.entity.CartItem;

import java.math.BigDecimal;

public record CartItemDTO(
        Long id,
        Long productId,
        String productName,
        BigDecimal productPrice,
        Integer quantity,
        BigDecimal subtotal
) {
    public static CartItemDTO from(CartItem item) {
        var product = item.getProduct();
        var subtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemDTO(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                item.getQuantity(),
                subtotal
        );
    }
}
