package com.shopapi.backend.dto;

import com.shopapi.backend.entity.Order;
import com.shopapi.backend.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryDTO(
        Long id,
        OrderStatus status,
        BigDecimal totalPrice,
        int itemCount,
        LocalDateTime createdAt
) {
    public static OrderSummaryDTO from(Order order) {
        int itemCount = order.getOrderItems().stream()
                .mapToInt(item -> item.getQuantity())
                .sum();

        return new OrderSummaryDTO(
                order.getId(),
                order.getStatus(),
                order.getTotalPrice(),
                itemCount,
                order.getCreatedAt()
        );
    }
}
