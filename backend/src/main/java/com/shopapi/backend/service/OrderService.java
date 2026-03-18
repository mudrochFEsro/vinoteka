package com.shopapi.backend.service;

import com.shopapi.backend.dto.OrderDTO;
import com.shopapi.backend.dto.OrderSummaryDTO;
import com.shopapi.backend.dto.UpdateOrderStatusRequest;
import com.shopapi.backend.entity.*;
import com.shopapi.backend.exception.EmptyCartException;
import com.shopapi.backend.exception.InsufficientStockException;
import com.shopapi.backend.exception.OrderNotFoundException;
import com.shopapi.backend.repository.CartRepository;
import com.shopapi.backend.repository.OrderRepository;
import com.shopapi.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    @Transactional
    public List<OrderSummaryDTO> getMyOrders() {
        User user = userService.getOrCreateCurrentUser();
        return orderRepository.findByUserId(user.getId()).stream()
                .map(OrderSummaryDTO::from)
                .toList();
    }

    @Transactional
    public OrderDTO getMyOrder(Long orderId) {
        User user = userService.getOrCreateCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Check ownership
        if (!order.getUser().getId().equals(user.getId())) {
            throw new OrderNotFoundException(orderId);
        }

        return OrderDTO.from(order);
    }

    @Transactional
    public OrderDTO createFromCart() {
        User user = userService.getOrCreateCurrentUser();

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(EmptyCartException::new);

        if (cart.getCartItems().isEmpty()) {
            throw new EmptyCartException();
        }

        // Validate stock and calculate total
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        product.getName(),
                        product.getStock(),
                        cartItem.getQuantity()
                );
            }

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();
            orderItems.add(orderItem);

            // Decrease stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // Create order
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .orderItems(new ArrayList<>())
                .build();

        // Set order reference on items
        for (OrderItem item : orderItems) {
            item.setOrder(order);
            order.getOrderItems().add(item);
        }

        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return OrderDTO.from(savedOrder);
    }

    // Admin methods
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(OrderDTO::from)
                .toList();
    }

    @Transactional
    public OrderDTO updateStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.setStatus(request.status());
        Order saved = orderRepository.save(order);

        return OrderDTO.from(saved);
    }
}
