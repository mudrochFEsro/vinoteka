package com.shopapi.backend.service;

import com.shopapi.backend.dto.OrderDTO;
import com.shopapi.backend.dto.UpdateOrderStatusRequest;
import com.shopapi.backend.entity.*;
import com.shopapi.backend.exception.EmptyCartException;
import com.shopapi.backend.exception.InsufficientStockException;
import com.shopapi.backend.exception.OrderNotFoundException;
import com.shopapi.backend.repository.CartRepository;
import com.shopapi.backend.repository.OrderRepository;
import com.shopapi.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .keycloakId("test-keycloak-id")
                .email("test@example.com")
                .name("Test User")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Wine")
                .description("A fine wine")
                .price(BigDecimal.valueOf(19.99))
                .stock(10)
                .build();

        testCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .cartItems(new ArrayList<>())
                .build();

        testOrder = Order.builder()
                .id(1L)
                .user(testUser)
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(39.98))
                .orderItems(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("createFromCart - should create order from cart items")
    void createFromCart_createsOrderFromCartItems() {
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .product(testProduct)
                .quantity(2)
                .build();
        testCart.getCartItems().add(cartItem);

        when(userService.getOrCreateCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderDTO result = orderService.createFromCart();

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.items()).hasSize(1);
        assertThat(testProduct.getStock()).isEqualTo(8); // 10 - 2
        verify(cartRepository).save(testCart);
        assertThat(testCart.getCartItems()).isEmpty();
    }

    @Test
    @DisplayName("createFromCart - should throw when cart is empty")
    void createFromCart_throwsWhenCartIsEmpty() {
        when(userService.getOrCreateCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));

        assertThatThrownBy(() -> orderService.createFromCart())
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    @DisplayName("createFromCart - should throw when cart not found")
    void createFromCart_throwsWhenCartNotFound() {
        when(userService.getOrCreateCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createFromCart())
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    @DisplayName("createFromCart - should throw when insufficient stock")
    void createFromCart_throwsWhenInsufficientStock() {
        testProduct.setStock(1);
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .product(testProduct)
                .quantity(5)
                .build();
        testCart.getCartItems().add(cartItem);

        when(userService.getOrCreateCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));

        assertThatThrownBy(() -> orderService.createFromCart())
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("getMyOrders - should return user orders")
    void getMyOrders_returnsUserOrders() {
        when(userService.getOrCreateCurrentUser()).thenReturn(testUser);
        when(orderRepository.findByUserId(testUser.getId())).thenReturn(List.of(testOrder));

        var result = orderService.getMyOrders();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(testOrder.getId());
    }

    @Test
    @DisplayName("getMyOrder - should return order when owned by user")
    void getMyOrder_returnsOrderWhenOwnedByUser() {
        when(userService.getOrCreateCurrentUser()).thenReturn(testUser);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        OrderDTO result = orderService.getMyOrder(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getMyOrder - should throw when order not found")
    void getMyOrder_throwsWhenOrderNotFound() {
        when(userService.getOrCreateCurrentUser()).thenReturn(testUser);
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getMyOrder(999L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("getMyOrder - should throw when order not owned by user")
    void getMyOrder_throwsWhenOrderNotOwnedByUser() {
        User otherUser = User.builder().id(2L).build();
        testOrder.setUser(otherUser);

        when(userService.getOrCreateCurrentUser()).thenReturn(testUser);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.getMyOrder(1L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("updateStatus - should update order status")
    void updateStatus_updatesOrderStatus() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.SHIPPED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderDTO result = orderService.updateStatus(1L, request);

        assertThat(result.status()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("getAllOrders - should return all orders for admin")
    void getAllOrders_returnsAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(testOrder));

        var result = orderService.getAllOrders();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getOrdersByStatus - should filter orders by status")
    void getOrdersByStatus_filtersOrdersByStatus() {
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(testOrder));

        var result = orderService.getOrdersByStatus(OrderStatus.PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(OrderStatus.PENDING);
    }
}
