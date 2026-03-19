package com.shopapi.backend.dto;

import com.shopapi.backend.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemDTO(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal priceAtPurchase,
        BigDecimal subtotal
) {
    public static OrderItemDTO from(OrderItem item) {
        var product = item.getProduct();
        var subtotal = item.getPriceAtPurchase()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new OrderItemDTO(
                item.getId(),
                product.getId(),
                product.getName(),
                item.getQuantity(),
                item.getPriceAtPurchase(),
                subtotal
        );
    }
}
