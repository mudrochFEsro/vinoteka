package com.shopapi.backend.service;

import com.shopapi.backend.dto.CheckoutRequest;
import com.shopapi.backend.dto.GuestOrderRequest;
import com.shopapi.backend.dto.OrderDTO;
import com.shopapi.backend.dto.OrderSummaryDTO;
import com.shopapi.backend.dto.UpdateOrderStatusRequest;
import com.shopapi.backend.entity.*;
import com.shopapi.backend.exception.EmptyCartException;
import com.shopapi.backend.exception.InsufficientStockException;
import com.shopapi.backend.exception.OrderNotFoundException;
import com.shopapi.backend.exception.ProductNotFoundException;
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
        var user = userService.getOrCreateCurrentUser();
        return orderRepository.findByUserId(user.getId()).stream()
                .map(OrderSummaryDTO::from)
                .toList();
    }

    @Transactional
    public OrderDTO getMyOrder(Long orderId) {
        var user = userService.getOrCreateCurrentUser();
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new OrderNotFoundException(orderId);
        }

        return OrderDTO.from(order);
    }

    @Transactional
    public OrderDTO createFromCart() {
        var user = userService.getOrCreateCurrentUser();
        var cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(EmptyCartException::new);

        if (cart.getCartItems().isEmpty()) {
            throw new EmptyCartException();
        }

        var orderItems = new ArrayList<OrderItem>();
        var totalPrice = BigDecimal.ZERO;

        for (var cartItem : cart.getCartItems()) {
            var product = cartItem.getProduct();
            var quantity = cartItem.getQuantity();

            validateStock(product, quantity);

            totalPrice = totalPrice.add(calculateItemTotal(product, quantity));
            orderItems.add(createOrderItem(product, quantity));

            decreaseStock(product, quantity);
        }

        var order = buildOrder(user, totalPrice, orderItems);
        var savedOrder = orderRepository.save(order);

        cart.getCartItems().clear();
        cartRepository.save(cart);

        return OrderDTO.from(savedOrder);
    }

    @Transactional
    public OrderDTO createGuestOrder(GuestOrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new EmptyCartException();
        }

        var orderItems = new ArrayList<OrderItem>();
        var totalPrice = BigDecimal.ZERO;

        for (var item : request.items()) {
            var product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ProductNotFoundException(item.productId()));

            validateStock(product, item.quantity());

            totalPrice = totalPrice.add(calculateItemTotal(product, item.quantity()));
            orderItems.add(createOrderItem(product, item.quantity()));

            decreaseStock(product, item.quantity());
        }

        var order = Order.builder()
                .user(null)
                .guestEmail(request.email())
                .guestName(request.fullName())
                .shippingAddress(request.formattedAddress())
                .status(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .orderItems(new ArrayList<>())
                .build();

        linkItemsToOrder(order, orderItems);

        return OrderDTO.from(orderRepository.save(order));
    }

    /**
     * Unified checkout for both authenticated and guest users.
     * Auth users: items are taken from their cart
     * Guest users: items are taken from the request
     */
    @Transactional
    public OrderDTO checkout(CheckoutRequest request) {
        var userOptional = userService.getCurrentUserOptional();

        var orderItems = new ArrayList<OrderItem>();
        var totalPrice = BigDecimal.ZERO;
        Cart cart = null;

        if (userOptional.isPresent()) {
            // Authenticated user - get items from cart
            var user = userOptional.get();
            cart = cartRepository.findByUserId(user.getId())
                    .orElseThrow(EmptyCartException::new);

            if (cart.getCartItems().isEmpty()) {
                throw new EmptyCartException();
            }

            for (var cartItem : cart.getCartItems()) {
                var product = cartItem.getProduct();
                var quantity = cartItem.getQuantity();

                validateStock(product, quantity);
                totalPrice = totalPrice.add(calculateItemTotal(product, quantity));
                orderItems.add(createOrderItem(product, quantity));
                decreaseStock(product, quantity);
            }
        } else {
            // Guest user - get items from request
            if (request.items() == null || request.items().isEmpty()) {
                throw new EmptyCartException();
            }

            for (var item : request.items()) {
                var product = productRepository.findById(item.productId())
                        .orElseThrow(() -> new ProductNotFoundException(item.productId()));

                validateStock(product, item.quantity());
                totalPrice = totalPrice.add(calculateItemTotal(product, item.quantity()));
                orderItems.add(createOrderItem(product, item.quantity()));
                decreaseStock(product, item.quantity());
            }
        }

        // Validate company fields if isCompany
        if (request.isCompany()) {
            if (request.companyName() == null || request.companyName().isBlank()) {
                throw new IllegalArgumentException("Nazov firmy je povinny");
            }
            if (request.ico() == null || request.ico().isBlank()) {
                throw new IllegalArgumentException("ICO je povinne");
            }
        }

        var order = Order.builder()
                .user(userOptional.orElse(null))
                .guestEmail(userOptional.isEmpty() ? request.email() : null)
                .guestName(userOptional.isEmpty() ? request.fullName() : null)
                .shippingAddress(request.formattedAddress())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .street(request.street())
                .houseNumber(request.houseNumber())
                .city(request.city())
                .postalCode(request.postalCode())
                .country(request.country())
                .isCompany(request.isCompany())
                .companyName(request.isCompany() ? request.companyName() : null)
                .ico(request.isCompany() ? request.ico() : null)
                .dic(request.isCompany() ? request.dic() : null)
                .icDph(request.isCompany() ? request.icDph() : null)
                .status(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .orderItems(new ArrayList<>())
                .build();

        linkItemsToOrder(order, orderItems);
        var savedOrder = orderRepository.save(order);

        // Clear cart for authenticated users
        if (cart != null) {
            cart.getCartItems().clear();
            cartRepository.save(cart);
        }

        return OrderDTO.from(savedOrder);
    }

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
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.setStatus(request.status());
        return OrderDTO.from(orderRepository.save(order));
    }

    private void validateStock(Product product, int quantity) {
        if (product.getStock() < quantity) {
            throw new InsufficientStockException(product.getName(), product.getStock(), quantity);
        }
    }

    private BigDecimal calculateItemTotal(Product product, int quantity) {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    private OrderItem createOrderItem(Product product, int quantity) {
        return OrderItem.builder()
                .product(product)
                .quantity(quantity)
                .priceAtPurchase(product.getPrice())
                .build();
    }

    private void decreaseStock(Product product, int quantity) {
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    private Order buildOrder(User user, BigDecimal totalPrice, List<OrderItem> items) {
        var order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .orderItems(new ArrayList<>())
                .build();

        linkItemsToOrder(order, items);
        return order;
    }

    private void linkItemsToOrder(Order order, List<OrderItem> items) {
        items.forEach(item -> {
            item.setOrder(order);
            order.getOrderItems().add(item);
        });
    }
}
