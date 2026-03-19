package com.shopapi.backend.service;

import com.shopapi.backend.dto.CheckoutRequest;
import com.shopapi.backend.dto.OrderDTO;
import com.shopapi.backend.dto.UpdateOrderStatusRequest;
import com.shopapi.backend.entity.*;
import java.math.BigDecimal;
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

    // ==================== CHECKOUT TESTS ====================

    private CheckoutRequest createCheckoutRequest(boolean isCompany, List<CheckoutRequest.CheckoutItem> items) {
        return createCheckoutRequest(isCompany, items, DeliveryMethod.PACKETA_COURIER, PaymentMethod.CASH_ON_DELIVERY);
    }

    private CheckoutRequest createCheckoutRequest(boolean isCompany, List<CheckoutRequest.CheckoutItem> items,
                                                   DeliveryMethod deliveryMethod, PaymentMethod paymentMethod) {
        return new CheckoutRequest(
                "test@example.com",
                "Jan",
                "Novak",
                "+421123456789",
                "Hlavna",
                "123",
                "Bratislava",
                "821 07",
                "SK",
                isCompany,
                isCompany ? "Test s.r.o." : null,
                isCompany ? "12345678" : null,
                isCompany ? "2012345678" : null,
                isCompany ? "SK2012345678" : null,
                deliveryMethod,
                deliveryMethod == DeliveryMethod.PACKETA_PICKUP ? "12345" : null,
                deliveryMethod == DeliveryMethod.PACKETA_PICKUP ? "Test Point" : null,
                paymentMethod,
                items
        );
    }

    @Test
    @DisplayName("checkout - should create order for authenticated user from cart")
    void checkout_createsOrderForAuthenticatedUser() {
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .product(testProduct)
                .quantity(2)
                .build();
        testCart.getCartItems().add(cartItem);

        CheckoutRequest request = createCheckoutRequest(false, null);

        when(userService.getCurrentUserOptional()).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderDTO result = orderService.checkout(request);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.firstName()).isEqualTo("Jan");
        assertThat(result.lastName()).isEqualTo("Novak");
        assertThat(result.street()).isEqualTo("Hlavna");
        assertThat(result.city()).isEqualTo("Bratislava");
        assertThat(result.isCompany()).isFalse();
        assertThat(result.deliveryMethod()).isEqualTo(DeliveryMethod.PACKETA_COURIER);
        assertThat(result.deliveryPrice()).isEqualByComparingTo(new BigDecimal("3.99"));
        assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.CASH_ON_DELIVERY);
        assertThat(testProduct.getStock()).isEqualTo(8);
        verify(cartRepository).save(testCart);
    }

    @Test
    @DisplayName("checkout - should create order for guest user from request items")
    void checkout_createsOrderForGuestUser() {
        List<CheckoutRequest.CheckoutItem> items = List.of(
                new CheckoutRequest.CheckoutItem(1L, 2)
        );
        CheckoutRequest request = createCheckoutRequest(false, items);

        when(userService.getCurrentUserOptional()).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderDTO result = orderService.checkout(request);

        assertThat(result).isNotNull();
        assertThat(result.userId()).isNull();
        assertThat(result.userEmail()).isEqualTo("test@example.com");
        assertThat(result.firstName()).isEqualTo("Jan");
        assertThat(testProduct.getStock()).isEqualTo(8);
    }

    @Test
    @DisplayName("checkout - should create company order with company fields")
    void checkout_createsCompanyOrder() {
        List<CheckoutRequest.CheckoutItem> items = List.of(
                new CheckoutRequest.CheckoutItem(1L, 1)
        );
        CheckoutRequest request = createCheckoutRequest(true, items);

        when(userService.getCurrentUserOptional()).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderDTO result = orderService.checkout(request);

        assertThat(result).isNotNull();
        assertThat(result.isCompany()).isTrue();
        assertThat(result.companyName()).isEqualTo("Test s.r.o.");
        assertThat(result.ico()).isEqualTo("12345678");
        assertThat(result.dic()).isEqualTo("2012345678");
        assertThat(result.icDph()).isEqualTo("SK2012345678");
    }

    @Test
    @DisplayName("checkout - should throw when company name missing for company order")
    void checkout_throwsWhenCompanyNameMissing() {
        List<CheckoutRequest.CheckoutItem> items = List.of(
                new CheckoutRequest.CheckoutItem(1L, 1)
        );
        CheckoutRequest request = new CheckoutRequest(
                "test@example.com", "Jan", "Novak", null,
                "Hlavna", "123", "Bratislava", "821 07", "SK",
                true, null, "12345678", null, null,
                DeliveryMethod.PACKETA_COURIER, null, null, PaymentMethod.CASH_ON_DELIVERY, items
        );

        when(userService.getCurrentUserOptional()).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> orderService.checkout(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nazov firmy");
    }

    @Test
    @DisplayName("checkout - should throw when ICO missing for company order")
    void checkout_throwsWhenIcoMissing() {
        List<CheckoutRequest.CheckoutItem> items = List.of(
                new CheckoutRequest.CheckoutItem(1L, 1)
        );
        CheckoutRequest request = new CheckoutRequest(
                "test@example.com", "Jan", "Novak", null,
                "Hlavna", "123", "Bratislava", "821 07", "SK",
                true, "Test s.r.o.", null, null, null,
                DeliveryMethod.PACKETA_COURIER, null, null, PaymentMethod.CASH_ON_DELIVERY, items
        );

        when(userService.getCurrentUserOptional()).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> orderService.checkout(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ICO");
    }

    @Test
    @DisplayName("checkout - should throw when guest checkout has no items")
    void checkout_throwsWhenGuestHasNoItems() {
        CheckoutRequest request = createCheckoutRequest(false, null);

        when(userService.getCurrentUserOptional()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.checkout(request))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    @DisplayName("checkout - should throw when auth user cart is empty")
    void checkout_throwsWhenAuthUserCartEmpty() {
        CheckoutRequest request = createCheckoutRequest(false, null);

        when(userService.getCurrentUserOptional()).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));

        assertThatThrownBy(() -> orderService.checkout(request))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    @DisplayName("checkout - should throw when insufficient stock")
    void checkout_throwsWhenInsufficientStock() {
        testProduct.setStock(1);
        List<CheckoutRequest.CheckoutItem> items = List.of(
                new CheckoutRequest.CheckoutItem(1L, 5)
        );
        CheckoutRequest request = createCheckoutRequest(false, items);

        when(userService.getCurrentUserOptional()).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> orderService.checkout(request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("checkout - should create order with Packeta pickup")
    void checkout_createsOrderWithPacketaPickup() {
        List<CheckoutRequest.CheckoutItem> items = List.of(
                new CheckoutRequest.CheckoutItem(1L, 1)
        );
        CheckoutRequest request = createCheckoutRequest(false, items, DeliveryMethod.PACKETA_PICKUP, PaymentMethod.CASH_ON_DELIVERY);

        when(userService.getCurrentUserOptional()).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderDTO result = orderService.checkout(request);

        assertThat(result).isNotNull();
        assertThat(result.deliveryMethod()).isEqualTo(DeliveryMethod.PACKETA_PICKUP);
        assertThat(result.packetaPointId()).isEqualTo("12345");
        assertThat(result.packetaPointName()).isEqualTo("Test Point");
        assertThat(result.deliveryPrice()).isEqualByComparingTo(new BigDecimal("2.49"));
    }

    @Test
    @DisplayName("checkout - should throw when Packeta pickup without point selected")
    void checkout_throwsWhenPacketaPickupWithoutPoint() {
        List<CheckoutRequest.CheckoutItem> items = List.of(
                new CheckoutRequest.CheckoutItem(1L, 1)
        );
        CheckoutRequest request = new CheckoutRequest(
                "test@example.com", "Jan", "Novak", null,
                null, null, null, null, "SK",
                false, null, null, null, null,
                DeliveryMethod.PACKETA_PICKUP, null, null, PaymentMethod.CASH_ON_DELIVERY, items
        );

        when(userService.getCurrentUserOptional()).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> orderService.checkout(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vydajne miesto");
    }

    @Test
    @DisplayName("checkout - should throw when Packeta courier delivery without address")
    void checkout_throwsWhenPacketaCourierWithoutAddress() {
        List<CheckoutRequest.CheckoutItem> items = List.of(
                new CheckoutRequest.CheckoutItem(1L, 1)
        );
        CheckoutRequest request = new CheckoutRequest(
                "test@example.com", "Jan", "Novak", null,
                null, null, null, null, "SK",
                false, null, null, null, null,
                DeliveryMethod.PACKETA_COURIER, null, null, PaymentMethod.CASH_ON_DELIVERY, items
        );

        when(userService.getCurrentUserOptional()).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> orderService.checkout(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ulica");
    }

    @Test
    @DisplayName("checkout - should include delivery price in total for Packeta courier")
    void checkout_includesTotalPriceWithDeliveryForPacketaCourier() {
        List<CheckoutRequest.CheckoutItem> items = List.of(
                new CheckoutRequest.CheckoutItem(1L, 2)
        );
        CheckoutRequest request = createCheckoutRequest(false, items, DeliveryMethod.PACKETA_COURIER, PaymentMethod.CASH_ON_DELIVERY);

        when(userService.getCurrentUserOptional()).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderDTO result = orderService.checkout(request);

        // 2 x 19.99 = 39.98 + 3.99 delivery = 43.97
        BigDecimal expectedTotal = new BigDecimal("19.99").multiply(BigDecimal.valueOf(2)).add(new BigDecimal("3.99"));
        assertThat(result.totalPrice()).isEqualByComparingTo(expectedTotal);
        assertThat(result.deliveryPrice()).isEqualByComparingTo(new BigDecimal("3.99"));
    }

    @Test
    @DisplayName("checkout - should include delivery price in total for Packeta pickup")
    void checkout_includesTotalPriceWithDeliveryForPacketaPickup() {
        List<CheckoutRequest.CheckoutItem> items = List.of(
                new CheckoutRequest.CheckoutItem(1L, 1)
        );
        CheckoutRequest request = createCheckoutRequest(false, items, DeliveryMethod.PACKETA_PICKUP, PaymentMethod.CASH_ON_DELIVERY);

        when(userService.getCurrentUserOptional()).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderDTO result = orderService.checkout(request);

        // 1 x 19.99 = 19.99 + 2.49 delivery = 22.48
        BigDecimal expectedTotal = new BigDecimal("19.99").add(new BigDecimal("2.49"));
        assertThat(result.totalPrice()).isEqualByComparingTo(expectedTotal);
        assertThat(result.deliveryPrice()).isEqualByComparingTo(new BigDecimal("2.49"));
    }

    @Test
    @DisplayName("checkout - should save payment method as CASH_ON_DELIVERY")
    void checkout_savesPaymentMethod() {
        List<CheckoutRequest.CheckoutItem> items = List.of(
                new CheckoutRequest.CheckoutItem(1L, 1)
        );
        CheckoutRequest request = createCheckoutRequest(false, items);

        when(userService.getCurrentUserOptional()).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderDTO result = orderService.checkout(request);

        assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.CASH_ON_DELIVERY);
    }
}
