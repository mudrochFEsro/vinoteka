package com.shopapi.backend.dto;

import com.shopapi.backend.entity.DeliveryMethod;
import com.shopapi.backend.entity.Order;
import com.shopapi.backend.entity.OrderStatus;
import com.shopapi.backend.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
        Long id,
        Long userId,
        String userEmail,
        OrderStatus status,
        BigDecimal totalPrice,
        BigDecimal deliveryPrice,
        List<OrderItemDTO> items,
        LocalDateTime createdAt,
        // Contact info
        String firstName,
        String lastName,
        String phone,
        // Address
        String street,
        String houseNumber,
        String city,
        String postalCode,
        String country,
        // Company
        Boolean isCompany,
        String companyName,
        String ico,
        String dic,
        String icDph,
        // Delivery
        DeliveryMethod deliveryMethod,
        String packetaPointId,
        String packetaPointName,
        // Payment
        PaymentMethod paymentMethod
) {
    public static OrderDTO from(Order order) {
        var items = order.getOrderItems().stream()
                .map(OrderItemDTO::from)
                .toList();

        var user = order.getUser();
        var userId = user != null ? user.getId() : null;
        var email = user != null ? user.getEmail() : order.getGuestEmail();

        return new OrderDTO(
                order.getId(),
                userId,
                email,
                order.getStatus(),
                order.getTotalPrice(),
                order.getDeliveryPrice(),
                items,
                order.getCreatedAt(),
                order.getFirstName(),
                order.getLastName(),
                order.getPhone(),
                order.getStreet(),
                order.getHouseNumber(),
                order.getCity(),
                order.getPostalCode(),
                order.getCountry(),
                order.getIsCompany(),
                order.getCompanyName(),
                order.getIco(),
                order.getDic(),
                order.getIcDph(),
                order.getDeliveryMethod(),
                order.getPacketaPointId(),
                order.getPacketaPointName(),
                order.getPaymentMethod()
        );
    }
}
