package com.shopapi.backend.controller;

import com.shopapi.backend.dto.CheckoutRequest;
import com.shopapi.backend.dto.GuestOrderRequest;
import com.shopapi.backend.dto.OrderDTO;
import com.shopapi.backend.dto.OrderSummaryDTO;
import com.shopapi.backend.service.OrderService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Get my orders", description = "Returns list of current user's orders")
    public List<OrderSummaryDTO> getMyOrders() {
        return orderService.getMyOrders();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Returns order details for current user")
    public OrderDTO getMyOrder(@PathVariable Long id) {
        return orderService.getMyOrder(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create order from cart", description = "Creates order from cart items, clears cart, decreases stock")
    public OrderDTO createOrder() {
        return orderService.createFromCart();
    }

    @PostMapping("/guest")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create guest order (deprecated)", description = "Creates order without authentication. Use /checkout instead.")
    @Deprecated
    public OrderDTO createGuestOrder(@Valid @RequestBody GuestOrderRequest request) {
        return orderService.createGuestOrder(request);
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Unified checkout", description = "Creates order for both authenticated and guest users. Auth users: items from cart. Guest users: items from request.")
    public OrderDTO checkout(@Valid @RequestBody CheckoutRequest request) {
        return orderService.checkout(request);
    }
}
