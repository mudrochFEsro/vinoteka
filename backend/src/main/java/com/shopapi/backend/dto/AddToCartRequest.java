package com.shopapi.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddToCartRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @Positive(message = "Quantity must be positive")
        Integer quantity
) {
    public AddToCartRequest {
        if (quantity == null) {
            quantity = 1;
        }
    }
}
