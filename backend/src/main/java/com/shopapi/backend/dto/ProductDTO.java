package com.shopapi.backend.dto;

import com.shopapi.backend.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDTO(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String imageUrl,
        Long categoryId,
        String categoryName,
        LocalDateTime createdAt
) {
    public static ProductDTO from(Product product) {
        var category = product.getCategory();
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                product.getCreatedAt()
        );
    }
}
