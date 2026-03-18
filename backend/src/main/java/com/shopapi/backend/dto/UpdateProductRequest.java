package com.shopapi.backend.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateProductRequest(
        String name,

        String description,

        @Positive(message = "Price must be positive")
        BigDecimal price,

        @PositiveOrZero(message = "Stock cannot be negative")
        Integer stock,

        String imageUrl,

        Long categoryId
) {
}
