package com.shopapi.backend.dto;

import com.shopapi.backend.entity.Order;
import com.shopapi.backend.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
        Long id,
        Long userId,
        String userEmail,
        OrderStatus status,
        BigDecimal totalPrice,
        List<OrderItemDTO> items,
        LocalDateTime createdAt
) {
    public static OrderDTO from(Order order) {
        List<OrderItemDTO> items = order.getOrderItems().stream()
                .map(OrderItemDTO::from)
                .toList();

        return new OrderDTO(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getEmail(),
                order.getStatus(),
                order.getTotalPrice(),
                items,
                order.getCreatedAt()
        );
    }
}
