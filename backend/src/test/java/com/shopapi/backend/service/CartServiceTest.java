package com.shopapi.backend.service;

import com.shopapi.backend.dto.AddToCartRequest;
import com.shopapi.backend.dto.CartDTO;
import com.shopapi.backend.entity.Cart;
import com.shopapi.backend.entity.CartItem;
import com.shopapi.backend.entity.Product;
import com.shopapi.backend.entity.User;
import com.shopapi.backend.exception.InsufficientStockException;
import com.shopapi.backend.exception.ProductNotFoundException;
import com.shopapi.backend.repository.CartItemRepository;
import com.shopapi.backend.repository.CartRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;

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
    }

    @Test
    @DisplayName("getMyCart - should create cart if not exists")
    void getMyCart_createsCartIfNotExists() {
        when(userService.getOrCreateCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartDTO result = cartService.getMyCart();

        assertThat(result).isNotNull();
        assertThat(result.items()).isEmpty();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("getMyCart - should return existing cart")
    void getMyCart_returnsExistingCart() {
        when(userService.getOrCreateCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));

        CartDTO result = cartService.getMyCart();

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testCart.getId());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("addItem - should add new item to cart")
    void addItem_addsNewItemToCart() {
        AddToCartRequest request = new AddToCartRequest(1L, 2);

        when(userService.getOrCreateCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArgument(0));

        CartDTO result = cartService.addItem(request);

        assertThat(result).isNotNull();
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("addItem - should throw when product not found")
    void addItem_throwsWhenProductNotFound() {
        AddToCartRequest request = new AddToCartRequest(999L, 1);

        when(userService.getOrCreateCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("addItem - should throw when insufficient stock")
    void addItem_throwsWhenInsufficientStock() {
        AddToCartRequest request = new AddToCartRequest(1L, 100);

        when(userService.getOrCreateCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> cartService.addItem(request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("addItem - should increase quantity for existing item")
    void addItem_increasesQuantityForExistingItem() {
        CartItem existingItem = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .product(testProduct)
                .quantity(2)
                .build();
        testCart.getCartItems().add(existingItem);

        AddToCartRequest request = new AddToCartRequest(1L, 3);

        when(userService.getOrCreateCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArgument(0));

        cartService.addItem(request);

        assertThat(existingItem.getQuantity()).isEqualTo(5);
        verify(cartItemRepository).save(existingItem);
    }

    @Test
    @DisplayName("clearCart - should clear all items")
    void clearCart_clearsAllItems() {
        CartItem item = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .product(testProduct)
                .quantity(2)
                .build();
        testCart.getCartItems().add(item);

        when(userService.getOrCreateCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));

        cartService.clearCart();

        assertThat(testCart.getCartItems()).isEmpty();
        verify(cartRepository).save(testCart);
    }
}
