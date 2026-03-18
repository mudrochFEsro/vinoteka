package com.shopapi.backend.repository;

import com.shopapi.backend.entity.Order;
import com.shopapi.backend.entity.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    List<Order> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    List<Order> findByUserKeycloakId(String keycloakId);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    List<Order> findByStatus(OrderStatus status);

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    @Override
    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    Optional<Order> findById(Long id);
}
